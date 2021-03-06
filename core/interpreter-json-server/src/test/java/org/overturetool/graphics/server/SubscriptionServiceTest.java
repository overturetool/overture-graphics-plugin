package org.overturetool.graphics.server;

import java.io.File;
import java.util.concurrent.Semaphore;

import javax.websocket.ClientEndpoint;
import javax.websocket.Session;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.overturetool.graphics.MessageUtil;
import org.overturetool.graphics.RunModel;
import org.overturetool.graphics.client.SubscriptionClient;
import org.overturetool.graphics.interpreter.JsonServerRemoteControl;
import org.overturetool.graphics.protocol.Response;

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

	@Before
	public void setup()
	{
		client = new SubscriptionClientSync();

	}

	@Test
	public void testServerTempoRemoteCtrlRequestModel() throws Exception
	{
		Semaphore sem = new Semaphore(1);
		sem.acquire();
		final JsonServerRemoteControl remote = new JsonServerRemoteControl(controller -> sem.release());
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
		client.connect(dest);

		// Set root class to Test3
		client.sendMessage(MessageUtil.buildSetRootClassMessage("Test3"));

		// Wait to receive message
		Assert.assertTrue("The root class was not set correctly", client.waitFor("RESPONSE").contains("OK"));

		// Request model info
		client.sendMessage(MessageUtil.buildGetModelInfo());

		// Wait to receive message
		Assert.assertTrue("The model was not started correctly", client.waitFor("MODEL").contains("\"rootClass\":\"Test3\""));
		remote.stop();
	}

	@Test
	public void testServerTempoRemoteCtrlRequestClasses() throws Exception
	{
		Semaphore sem = new Semaphore(1);
		sem.acquire();
		final JsonServerRemoteControl remote = new JsonServerRemoteControl(controller -> sem.release());
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
		client.connect(dest);

		// Request class info
		client.sendMessage(MessageUtil.buildGetClasses());

		// Wait to receive message
		String classInfo = client.waitFor(Response.classInfo);
		Assert.assertTrue("Model does not contain Test3", classInfo.contains("Test3"));
		Assert.assertTrue("Model does not contain Test2", classInfo.contains("Test2"));
		Assert.assertTrue("Model does not contain Test1", classInfo.contains("Test1"));
		remote.stop();
	}

	@Test
	public void testServerTempoRemoteCtrlRequestOperations() throws Exception
	{
		Semaphore sem = new Semaphore(1);
		sem.acquire();
		final JsonServerRemoteControl remote = new JsonServerRemoteControl(controller -> sem.release());
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

		// Send request
		client.connect(dest);

		// Set root class to NestedTest
		client.sendMessage(MessageUtil.buildSetRootClassMessage("NestedTest"));

		// Wait to receive message
		Assert.assertTrue("The root class was not set correctly", client.waitFor("RESPONSE").contains("OK"));

		// Request class info
		client.sendMessage(MessageUtil.buildGetFunctions());

		// Wait to receive message
		String classInfo = client.waitFor(Response.functionInfo);
		Assert.assertTrue("Root class does not contain run", classInfo.contains("run"));
		remote.stop();
	}

	@Test
	public void testServerTempoRemoteCtrlSubscription() throws Exception
	{
		// Initialize semaphore
		Semaphore sem = new Semaphore(1);
		sem.acquire();

		final JsonServerRemoteControl remote = new JsonServerRemoteControl(controller -> sem.release());
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

		// Set root class to Test3
		client.sendMessage(MessageUtil.buildSetRootClassMessage("Test3"));

		// Wait to receive message
		Assert.assertTrue("The root class was not set correctly", client.waitFor("RESPONSE").contains("OK"));

		// Send subscription
		client.sendMessage(MessageUtil.buildSubscribeMessage("var_real"));

		// Start model
		client.sendMessage(MessageUtil.buildRunModelMessage());

		// Wait to receive message
		Assert.assertTrue("Expected to recieve value = 5", client.waitFor("VALUE").contains("\"value\":\"5.0\""));
		Assert.assertTrue("The model was not started correctly", client.waitFor("RESPONSE").contains("OK"));
		remote.stop();
	}

	@Test
	public void testServerTempoRemoteNestedTest() throws Exception
	{
		// Initialize semaphore
		Semaphore sem = new Semaphore(1);
		sem.acquire();

		final JsonServerRemoteControl remote = new JsonServerRemoteControl(controller -> sem.release());
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

		// Set root class to NestedTest
		client.sendMessage(MessageUtil.buildSetRootClassMessage("NestedTest"));

		// Wait to receive message
		Assert.assertTrue("The root class was not set correctly", client.waitFor("RESPONSE").contains("OK"));

		// Send subscription
		client.sendMessage(MessageUtil.buildSubscribeMessage("nestedObject.r1"));

		// Start model
		client.sendMessage(MessageUtil.buildRunModelMessage());

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

		// Wait to receive OK message
		Assert.assertTrue("The model was not started correctly", client.waitFor("RESPONSE").contains("OK"));

		remote.stop();
	}

	// @Test
	public void startServer() throws Exception
	{
		// Initialize semaphore
		Semaphore sem = new Semaphore(1);
		sem.acquire();

		final JsonServerRemoteControl remote = new JsonServerRemoteControl(controller -> sem.release());
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
		Thread.sleep(10000000);
		remote.stop();
	}

}