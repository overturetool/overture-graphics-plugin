package org.overturetool.plotting.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

@ClientEndpoint
public class SubscriptionClient
{
	CountDownLatch latch = new CountDownLatch(1);
	private Session session;

	@OnOpen
	public void onOpen(Session session)
	{
		System.out.println("Connected to server");
		this.session = session;
		latch.countDown();
	}

	@OnMessage
	public void onText(String message, Session session)
	{
		System.out.println("Message received from server:" + message);
	}

	@OnClose
	public void onClose(CloseReason reason, Session session)
	{
		System.out.println("Closing a WebSocket due to "
				+ reason.getReasonPhrase());
	}

	public CountDownLatch getLatch()
	{
		return latch;
	}

	public void sendMessage(String str)
	{
		try
		{
			session.getBasicRemote().sendText(str);
		} catch (IOException e)
		{

			e.printStackTrace();
		}
	}

	public void connect(String dest) throws URISyntaxException, IOException,
			DeploymentException, InterruptedException
	{
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		container.connectToServer(this, new URI(dest));

		this.getLatch().await();
	}
}