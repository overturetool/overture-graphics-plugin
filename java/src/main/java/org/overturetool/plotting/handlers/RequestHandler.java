package org.overturetool.plotting.handlers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import javax.websocket.Session;

import org.overturetool.plotting.exceptions.RootClassException;
import org.overturetool.plotting.interpreter.ModelInteraction;
import org.overturetool.plotting.interpreter.TempoRemoteControl;
import org.overturetool.plotting.protocol.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.overturetool.plotting.protocol.Error;
import org.overturetool.plotting.server.SubscriptionService;

/**
 * Created by John on 19-05-2016.
 */
public class RequestHandler extends MessageHandler<Request>
{
	private ModelInteraction modelInteraction;
	private TempoRemoteControl remoteControl;

	public RequestHandler(ModelInteraction modelInteraction, TempoRemoteControl remoteControl)
	{

		this.modelInteraction = modelInteraction;
		this.remoteControl = remoteControl;
	}

	@Override
	public void handle(Request message, Session session)
	{
		if (message.request.equals(Request.GET_MODEL_INFO))
		{
			HandleGetModelInfo(session);
		}

		if (message.request.equals(Request.RUN_MODEL))
		{
			HandleRunModel(message, session);
		}

		if (message.request.equals(Request.SET_ROOT_CLASS))
		{
			HandleSetRootClass(message, session);
		}

		if (message.request.equals(Request.GET_CLASS_INFO))
		{
			HandleGetClassInfo(session);
		}

		if (message.request.equals(Request.GET_FUNCTION_INFO))
		{
			HandleGetFunctionInfo(session);
		}

		if (message.request.equals(Request.STOP_SERVER))
		{
			HandleStop(session);
		}
	}

	public String getMessageTypeName()
	{
		return Request.messageType;
	}

	@Override
	protected Type getType()
	{
		return new TypeToken<Message<Request>>()
		{
		}.getType();
	}

	private void HandleSetRootClass(Request message, Session session) {
		try {
			this.modelInteraction.setRootClass(message.parameter);

			RespondOk(session);

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void RespondOk(Session session) throws IOException {
		SubscriptionService.TryExecuteRespondWithError(session, (sess) -> {
            Message<String> msg = new Message<>();
            msg.type = "RESPONSE";
            msg.data = "OK";
            String serialized = new Gson().toJson(msg);

            sess.getBasicRemote().sendText(serialized);
        });
	}

	private void HandleGetFunctionInfo(Session session) {
		try
		{
			SubscriptionService.TryExecuteRespondWithError(session, (sess) -> {
				// Get and serialize model structure
				List<String> t = modelInteraction.getOperationNames();

				Message<List<String>> msg = new Message<>();
				msg.data = t;
				msg.type = Response.functionInfo;

				String serialized = new Gson().toJson(msg);

				sess.getBasicRemote().sendText(serialized);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void HandleGetClassInfo(Session session) {
		try
		{
			SubscriptionService.TryExecuteRespondWithError(session, (sess) -> {
				// Get and serialize model structure
				List<String> t = modelInteraction.getClassNames();

				Message<List<String>> msg = new Message<>();
				msg.data = t;
				msg.type = Response.classInfo;

				String serialized = new Gson().toJson(msg);

				sess.getBasicRemote().sendText(serialized);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void HandleRunModel(Request message, Session session) {
		String runMethod = "run()";
		if(message.parameter != null && !message.parameter.isEmpty()) {
			runMethod = message.parameter;

			// Add parentheses
			if(!runMethod.substring(runMethod.length()-2).equals("()")) {
				runMethod = runMethod + "()";
			}
		}

		try
		{
			this.modelInteraction.start(runMethod);

			RespondOk(session);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void HandleGetModelInfo(Session session) {
		try
		{
			SubscriptionService.TryExecuteRespondWithError(session, (sess) -> {
				// Get and serialize model structure
				ModelStructure t = modelInteraction.getModelStructure();

				Message<ModelStructure> msg = new Message<>();
				msg.data = t;
				msg.type = ModelStructure.messageType;

				String serialized = new Gson().toJson(msg);

				sess.getBasicRemote().sendText(serialized);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void HandleStop(Session session) {
		try {
			session.close();
			remoteControl.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
