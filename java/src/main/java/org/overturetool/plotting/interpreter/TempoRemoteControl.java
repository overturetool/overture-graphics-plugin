package org.overturetool.plotting.interpreter;

import java.util.List;
import java.util.Map.Entry;

import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.interpreter.debug.RemoteControl;
import org.overture.interpreter.debug.RemoteInterpreter;
import org.overture.interpreter.values.NameValuePairMap;
import org.overture.interpreter.values.ObjectValue;
import org.overture.interpreter.values.UpdatableValue;
import org.overture.interpreter.values.Value;

public class TempoRemoteControl implements RemoteControl
{
	public interface IInterpreterReadyCallback
	{
		void initialized(TempoRemoteControl controller);
	}

	private final Object databinding;
	private final List<BoundVarInfo> bindings;
	private final IInterpreterReadyCallback callback;

	/**
	 * controller for the TEMPO UI to run in Overture this must have a public default constructor
	 * 
	 * This constructor is used for testing
	 * @param databinding the object that value changes are set on
	 * @param bindings a list of binding informations
	 * @param callback a callback called once the controller is initialized
	 */
	public TempoRemoteControl(Object databinding, List<BoundVarInfo> bindings,
			IInterpreterReadyCallback callback)
	{
		this.databinding = databinding;
		this.bindings = bindings;
		this.callback = callback;
	}

	private static final String ROOT_NAME = "root";
	private RemoteInterpreter interpreter;

	public void run(RemoteInterpreter interpreter) throws Exception
	{
		this.interpreter = interpreter;

		if (callback != null)
		{
			callback.initialized(this);
		}

	}

	/**
	 * Starts the interpreter with a 'new' expression e.g. 'new A()', and a run method like run() from A returns only if
	 * the model exits
	 * 
	 * @param exp
	 * @return
	 * @throws Exception
	 */
	public Value start(String newRootExp, String runExp) throws Exception
	{
		attachListeners(databinding, bindings, newRootExp);
		return interpreter.valueExecute(ROOT_NAME + "." + runExp);
	}

	public void stop()
	{
		interpreter.finish();
	}

	public void attachListeners(Object d, List<BoundVarInfo> vars,
			String rootConstructor) throws Exception
	{
		String root = ROOT_NAME;
		interpreter.create(root, rootConstructor);

		if (d == null || vars == null || vars.isEmpty())
		{
			return;
		}

		Value v = interpreter.valueExecute(root);
		if (v.deref() instanceof ObjectValue)
		{
			NameValuePairMap members = ((ObjectValue) v.deref()).members;
			for (Entry<ILexNameToken, Value> p : members.entrySet())
			{
				for (BoundVarInfo bv : vars)
				{
					if (bv.name().equals(p.getKey().getName()))
					{
						if (p.getValue() instanceof UpdatableValue)
						{
							UpdatableValue u = (UpdatableValue) p.getValue();
							u.addListener(new BoundVarListener(d, bv.name(), bv.type()));
							break;
						}
					}
				}

			}
		}
	}
}
