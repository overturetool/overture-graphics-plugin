package org.overturetool.graphics.interpreter;

import org.overture.interpreter.debug.RemoteControl;
import org.overture.interpreter.debug.RemoteInterpreter;
import org.overture.interpreter.values.Value;
import org.overturetool.graphics.handlers.RequestHandler;
import org.overturetool.graphics.handlers.SubscriptionHandler;
import org.overturetool.graphics.server.SubscriptionService;

public class JsonServerRemoteControl implements RemoteControl
{
	public interface IInterpreterReadyCallback
	{
		void initialized(JsonServerRemoteControl controller);
	}

	private IInterpreterReadyCallback callback = null;
	private RemoteInterpreter interpreter;
	private ModelInteraction modelInteraction;
	private SubscriptionService subSvc;

	/**
	 * controller for the TEMPO UI to run in Overture this must have a public default constructor This constructor is
	 * used for testing
	 * 
	 * @param callback
	 *            a callback called once the controller is initialized
	 */
	public JsonServerRemoteControl(IInterpreterReadyCallback callback)
	{
		this.callback = callback;
	}

	public JsonServerRemoteControl()
	{
	}

	/**
	 * Method to be called from overture
	 * 
	 * @param interpreter
	 * @throws Exception
	 */
	public void run(RemoteInterpreter interpreter) throws Exception
	{
		this.interpreter = interpreter;
		this.modelInteraction = new ModelInteraction(interpreter);

		this.setupServer();

		if (callback != null)
		{
			callback.initialized(this);
		}
	}

	/**
	 * Starts the interpreter with a 'new' expression e.g. 'new A()', and a run method like run() from A returns only if
	 * the model exits
	 * 
	 * @param runExp
	 * @return
	 * @throws Exception
	 */
	public Value start(String runExp) throws Exception
	{
		return this.modelInteraction.start(runExp);
	}

	/**
	 * Stops the interpreter from running
	 */
	public void stop() throws Exception
	{
		interpreter.finish();
		subSvc.stopServer();
	}

	/**
	 * Setup server
	 */
	private void setupServer()
	{
		// Create and start server
		this.subSvc = new SubscriptionService();

		// Setup handlers
		RequestHandler rqHandler = new RequestHandler(modelInteraction, this);
		SubscriptionHandler subscriptionHandler = new SubscriptionHandler(modelInteraction);

		this.subSvc.addMessageHandler(rqHandler);
		this.subSvc.addMessageHandler(subscriptionHandler);
		this.subSvc.startServer(8080);
	}
}
