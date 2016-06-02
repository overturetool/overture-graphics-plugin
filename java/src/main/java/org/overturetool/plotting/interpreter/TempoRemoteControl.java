package org.overturetool.plotting.interpreter;

import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.definitions.AInstanceVariableDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.types.ARealNumericBasicType;
import org.overture.ast.types.PType;
import org.overture.interpreter.debug.RemoteControl;
import org.overture.interpreter.debug.RemoteInterpreter;
import org.overture.interpreter.runtime.ClassInterpreter;
import org.overture.interpreter.values.NameValuePairMap;
import org.overture.interpreter.values.ObjectValue;
import org.overture.interpreter.values.UpdatableValue;
import org.overture.interpreter.values.Value;
import org.overturetool.plotting.handlers.RequestHandler;
import org.overturetool.plotting.handlers.SubscriptionHandler;
import org.overturetool.plotting.protocol.ModelStructure;
import org.overturetool.plotting.protocol.Node;
import org.overturetool.plotting.server.SubscriptionService;

public class TempoRemoteControl implements RemoteControl
{
	public interface IInterpreterReadyCallback
	{
		void initialized(TempoRemoteControl controller);
	}

	private static final String ROOT_NAME = "root";
	private final IInterpreterReadyCallback callback;
	private RemoteInterpreter interpreter;
	private ModelStructure structure = null;
	private ModelInteraction modelInteraction;
	private SubscriptionService subSvc;

	/**
	 * controller for the TEMPO UI to run in Overture this must have a public default constructor
	 * 
	 * This constructor is used for testing
	 * @param callback a callback called once the controller is initialized
	 */
	public TempoRemoteControl(IInterpreterReadyCallback callback)
	{
		this.callback = callback;
	}

	/**
	 * Method to be called from overture
	 * @param interpreter
	 * @throws Exception
     */
	public void run(RemoteInterpreter interpreter) throws Exception
	{
		this.interpreter = interpreter;
		this.modelInteraction = new ModelInteraction(interpreter);

		// Create instance
		String rootClassName = modelInteraction.getRootClassName();
		interpreter.create(ROOT_NAME, "new " + rootClassName + "()");

		this.setupServer();

		if (callback != null)
		{
			callback.initialized(this);
		}
	}

	/**
	 * Starts the interpreter with a 'new' expression e.g. 'new A()', and a run method like run() from A returns only if
	 * the model exits
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
	public void stop() throws Exception {
		interpreter.finish();
		subSvc.stopServer();
	}

	/**
	 * Setup server
	 */
	private void setupServer() {
		// Setup handlers
		RequestHandler rqHandler = new RequestHandler(modelInteraction);
		SubscriptionHandler subscriptionHandler = new SubscriptionHandler(modelInteraction);

		// Create and start server
		this.subSvc = new SubscriptionService();
		this.subSvc.addMessageHandler(rqHandler);
		this.subSvc.addMessageHandler(subscriptionHandler);
		this.subSvc.startServer(8080);
	}
}
