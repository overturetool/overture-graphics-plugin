package org.overturetool.plotting.server;

import java.io.File;
import java.util.concurrent.Semaphore;

import javax.websocket.ClientEndpoint;
import javax.websocket.Session;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.overturetool.plotting.RunModel;
import org.overturetool.plotting.client.SubscriptionClient;
import org.overturetool.plotting.interpreter.TempoRemoteControl;
import org.overturetool.plotting.protocol.Message;
import org.overturetool.plotting.protocol.Request;
import org.overturetool.plotting.protocol.Subscription;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SubscriptionServiceTest
{
	/**
	 * Test version of the subscriptionclass which enabled the test script to wait for a message in a synchrony way.
	 * 
	 * @author kel
	 */
	@ClientEndpoint
	private class SubscriptionClientSync extends SubscriptionClient
	{

		String lastMessageMatched = null;
		Semaphore sem = new Semaphore(1);

		@Override
		public synchronized void onText(String message, Session session)
		{
			try
			{
				sem.acquire();
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.onText(message, session);
			this.lastMessageMatched = message;
			notifyAll();
		}

		public String waitFor(String type) throws InterruptedException
		{
			String temp = null;
			while (lastMessageMatched == null
					|| !lastMessageMatched.contains("\"type\":\"" + type + "\""))
			{
				synchronized (this)
				{
					wait();
					sem.release();
				}
			}
			temp = lastMessageMatched;
			lastMessageMatched = null;
			sem.release();
			return temp;
		}
	};

	SubscriptionService svc;
	SubscriptionClientSync client;// new SubscriptionClient();
	String dest = "ws://localhost:8080/subscription";
	static Gson gson = new Gson();

	@Before
	public void setup()
	{
		client = new SubscriptionClientSync();

	}

	@Test
	public void testServerTempoRemoteCtrlRequest() throws Exception
	{
		Semaphore sem = new Semaphore(1);
		sem.acquire();
		final TempoRemoteControl remote = new TempoRemoteControl(controller -> sem.release());
		Thread t = new Thread(() -> {
			try
			{
				RunModel.runWithRemoteConsole(new File("src/test/resources/test3".replace('/', File.separatorChar)), remote);

			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		t.setDaemon(true);
		t.start();

		// Wait for interpreter initialization
		sem.acquire();

		// Send request
		Message<Request> msg = new Message<Request>();
		msg.data = new Request("GetModelInfo");
		msg.type = Request.messageType;
		String serialized = gson.toJson(msg);

		client.connect(dest);
		client.sendMessage(serialized);

		// Wait to receive message
		Assert.assertTrue("The model was not started correctly", client.waitFor("MODEL").contains("\"rootClass\":\"Test3\""));
	}

	@Test
	public void testServerTempoRemoteCtrlSubscription() throws Exception
	{
		// Initialize semaphore
		Semaphore sem = new Semaphore(1);
		sem.acquire();

		final TempoRemoteControl remote = new TempoRemoteControl(controller -> sem.release());
		Thread t = new Thread(() -> {
			try
			{
				RunModel.runWithRemoteConsole(new File("src/test/resources/test3".replace('/', File.separatorChar)), remote);

			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		t.setDaemon(true);
		t.start();

		// Wait for interpreter initialization
		sem.acquire();

		client.connect(dest);
		// Send subscription
		client.sendMessage(buildSubscribeMessage("var_real"));

		// Start model
		client.sendMessage(buildRunModelMessage());

		// Wait to receive message
		Assert.assertTrue("The model was not started correctly", client.waitFor("RESPONSE").contains("OK"));
		Assert.assertTrue("Expected to recieve value = 5", client.waitFor("VALUE").contains("\"value\":\"5.0\""));
	}

	private static String buildSubscribeMessage(String name)
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Message<Subscription> msg = new Message();
		msg.type = Subscription.messageType;
		msg.data = new Subscription();
		msg.data.variableName = "nestedObject.r1";
		String serialized = gson.toJson(msg);
		return serialized;
	}

	private static String buildRunModelMessage()
	{
		Message<Request> rq = new Message<>();
		rq.type = Request.messageType;
		rq.data = new Request();
		rq.data.request = Request.RUN_MODEL;
		return gson.toJson(rq);
	}

	@Test
	public void testServerTempoRemoteNestedTest() throws Exception
	{
		// Initialize semaphore
		Semaphore sem = new Semaphore(1);
		sem.acquire();

		final TempoRemoteControl remote = new TempoRemoteControl(controller -> sem.release());
		Thread t = new Thread(() -> {
			try
			{
				RunModel.runWithRemoteConsole(new File("src/test/resources/test-nested-real-run".replace('/', File.separatorChar)), remote);

			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		t.setDaemon(true);
		t.start();

		// Wait for interpreter initialization
		sem.acquire();

		client.connect(dest);
		// Send subscription
		client.sendMessage(buildSubscribeMessage("nestedObject.r1"));

		// Start model
		client.sendMessage(buildRunModelMessage());
		// Wait to receive message
		Assert.assertTrue("The model was not started correctly", client.waitFor("RESPONSE").contains("OK"));

		double currentValue = 0;
		for (int i = 0; i <= 10; i++)
		{
			String valueMsg = client.waitFor("VALUE");
			JsonElement jelement = new JsonParser().parse(valueMsg);
			JsonObject jobject = jelement.getAsJsonObject();
			jobject = jobject.getAsJsonObject("data");
			Double val = jobject.get("value").getAsDouble();
			System.out.println("Checking value: " + val);
			Assert.assertTrue("The value is not incrementing", currentValue <= val);
			currentValue = val;
		}

		remote.stop();
	}

}