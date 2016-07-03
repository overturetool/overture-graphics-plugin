package org.overturetool.plotting.handlers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import javax.websocket.Session;

import com.google.gson.Gson;
import org.overturetool.plotting.interpreter.ModelInteraction;
import org.overturetool.plotting.interpreter.SessionVarListener;
import org.overturetool.plotting.protocol.Message;
import org.overturetool.plotting.protocol.ModelStructure;
import org.overturetool.plotting.protocol.Node;
import org.overturetool.plotting.protocol.Subscription;

import com.google.gson.reflect.TypeToken;
import org.overturetool.plotting.server.SubscriptionService;

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
					//FIXME, report an error on the subscription
					System.err.println("Unable to find node for: "+message.variableName);
					return;
				}

				// Else attach listener
				try
				{
					modelInteraction.attachListener(n, new SessionVarListener(n.name, n.ptype, session));
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
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
