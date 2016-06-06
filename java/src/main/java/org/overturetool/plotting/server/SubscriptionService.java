package org.overturetool.plotting.server;

import java.io.IOException;
import java.util.HashMap;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.overturetool.plotting.handlers.MessageHandler;
import org.overturetool.plotting.protocol.Message;

import com.google.gson.Gson;

@ServerEndpoint("/subscription")
@SuppressWarnings("rawtypes")
public class SubscriptionService
{
	private Server server;
	
	private static HashMap<String, MessageHandler> handlers = new HashMap<String, MessageHandler>();

	@OnOpen
	public void onOpen(Session session)
	{
		session.setMaxIdleTimeout(Long.MAX_VALUE);
		System.out.println("WebSocket opened: " + session.getId());
	}

	@SuppressWarnings("unchecked")
	@OnMessage
	public void onMessage(String txt, Session session) throws IOException
	{
		System.out.println("Message received at server: " + txt);

		// Deserialize message to get message type
		Gson gson = new Gson();
		Message msg = gson.fromJson(txt, Message.class);

		// Handle message
		if (handlers.containsKey(msg.type))
		{
			MessageHandler handler = handlers.get(msg.type);
			handler.handle(handler.deserializeMessage(txt), session);
		}
	}

	@OnClose
	public void onClose(CloseReason reason, Session session)
	{
		System.out.println("Closing a WebSocket due to "
				+ reason.getReasonPhrase());
	}

	public void startServer(int port)
	{
		server = new Server();
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		server.addConnector(connector);

		// Setup the basic application "context" for this application at "/"
		// This is also known as the handler tree (in jetty speak)
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);

		try
		{
			// Initialize javax.websocket layer
			ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);

			// Add WebSocket endpoint to javax.websocket layer
			wscontainer.addEndpoint(SubscriptionService.class);

			server.start();
//			server.dump(System.err);
		} catch (Throwable t)
		{
			t.printStackTrace(System.err);
		}
	}

	public void waitServer() throws InterruptedException
	{
		server.join();
	}

	public void stopServer() throws Exception
	{
		server.stop();
	}

	public void addMessageHandler(MessageHandler handler)
	{
		handlers.put(handler.getMessageTypeName(), handler);
	}
}
