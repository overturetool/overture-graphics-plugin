package org.overturetool.graphics.handlers;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.websocket.Session;

import org.overturetool.graphics.interpreter.ModelInteraction;
import org.overturetool.graphics.interpreter.SessionVarListener;
import org.overturetool.graphics.protocol.Message;
import org.overturetool.graphics.protocol.ModelStructure;
import org.overturetool.graphics.protocol.Node;
import org.overturetool.graphics.protocol.Subscription;
import org.overturetool.graphics.server.SubscriptionService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by John on 19-05-2016.
 */
public class SubscriptionHandler extends MessageHandler<Subscription>
{

	private ModelInteraction modelInteraction;

	public SubscriptionHandler(ModelInteraction modelInteraction)
	{

		this.modelInteraction = modelInteraction;
	}

	@Override
	public void handle(Subscription message, Session session)
	{
		try
		{
			SubscriptionService.TryExecuteRespondWithError(session, (sess) -> {
				// Get model structure and find node corresponding to variable name
				ModelStructure structure = modelInteraction.getModelStructure();
				Node n = structure.findNode(message.variableName);

				// Could not find node
				if (n == null)
				{
					throw new Exception("Could not find variable with that name.");
				}

				// Else attach listener
				try
				{
					modelInteraction.attachListener(n, new SessionVarListener(n.name, n.ptype, session));

					Message<String> msg = new Message<>();
					msg.type = "RESPONSE";
					msg.data = "OK";
					String serialized = new Gson().toJson(msg);

					sess.getBasicRemote().sendText(serialized);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			});
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public String getMessageTypeName()
	{
		return Subscription.messageType;
	}

	@Override
	protected Type getType()
	{
		return new TypeToken<Message<Subscription>>()
		{
		}.getType();
	}
}
