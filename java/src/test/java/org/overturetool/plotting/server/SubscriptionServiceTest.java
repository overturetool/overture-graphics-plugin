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

		@Override
		public synchronized void onText(String message, Session session)
		{
			super.onText(message, session);
			this.lastMessageMatched = message;
			notify();
		}

		public String waitFor(String type) throws InterruptedException
		{
			while (lastMessageMatched == null
					|| !lastMessageMatched.contains("\"type\":\"" + type + "\""))
			{
				synchronized (this)
				{
					wait();
				}
			}

			return lastMessageMatched;
		}
	};

	SubscriptionService svc;
	SubscriptionClientSync client;// new SubscriptionClient();
	String dest = "ws://localhost:8080/subscription";
	Gson gson = new Gson();

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

		// Send subscription
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Message<Subscription> msg = new Message();
		msg.type = Subscription.messageType;
		msg.data = new Subscription();
		msg.data.variableName = "var_real";
		String serialized = gson.toJson(msg);

		client.connect(dest);
		client.sendMessage(serialized);

		// Start model
		Message<Request> rq = new Message<>();
		rq.type = Request.messageType;
		rq.data = new Request();
		rq.data.request = Request.RUN_MODEL;
		serialized = gson.toJson(rq);
		client.sendMessage(serialized);

		// Wait to receive message
		Assert.assertTrue("The model was not started correctly", client.waitFor("RESPONSE").contains("OK"));
		Assert.assertTrue("Expected to recieve value = 5", client.waitFor("VALUE").contains("\"value\":\"5.0\""));
	}
}