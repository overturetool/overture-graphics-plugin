package org.overturetool.plotting.interpreter;

import java.util.*;

import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.interpreter.debug.RemoteInterpreter;
import org.overture.interpreter.runtime.ClassInterpreter;
import org.overture.interpreter.values.NameValuePairMap;
import org.overture.interpreter.values.ObjectValue;
import org.overture.interpreter.values.UpdatableValue;
import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.ValueListener;
import org.overturetool.plotting.exceptions.RootClassException;
import org.overturetool.plotting.protocol.ModelStructure;
import org.overturetool.plotting.protocol.Node;

/**
 * Created by John on 26-05-2016.
 */
public class ModelInteraction
{
	public static final String ROOT_VAR_NAME = "root";
	private String rootClass;
	private RemoteInterpreter interpreter;

	public ModelInteraction(RemoteInterpreter interpreter)
	{
		this.interpreter = interpreter;
	}

	/**
	 * Attaches a listener to variable (format: objX.objY.objZ.val)
	 * 
	 * @param var
	 * @throws Exception
	 */
	public void attachListener(Node var, ValueListener listener)
			throws Exception
	{
		// Get root class instance
		Value v = interpreter.valueExecute(ROOT_VAR_NAME);
		NameValuePairMap members;

		// Tokenize variable name
		StringTokenizer tokenizer = new StringTokenizer(var.name, ".");
		String[] tokens = new String[tokenizer.countTokens()];
		for (int i = 0; tokenizer.hasMoreTokens(); i++)
		{
			tokens[i] = tokenizer.nextToken();
		}

		// Find parent object
		for (int i = 0; i < tokens.length - 1; i++)
		{
			if (v.deref() instanceof ObjectValue)
			{
				members = ((ObjectValue) v.deref()).members;
				for (Map.Entry<ILexNameToken, Value> p : members.entrySet())
				{
					if (tokens[i].equals(p.getKey().getName()))
					{
						v = p.getValue();
						break;
					}
				}
			}
		}

		// Find child object to bind to
		if (v.deref() instanceof ObjectValue)
		{
			members = ((ObjectValue) v.deref()).members;
			for (Map.Entry<ILexNameToken, Value> p : members.entrySet())
			{
				if (tokens[tokens.length - 1].equals(p.getKey().getName()))
				{
					if (p.getValue() instanceof UpdatableValue)
					{
						UpdatableValue u = (UpdatableValue) p.getValue();
						u.addListener(listener);
						break;
					}
				}
			}
		}
	}

	/**
	 * Returns root class name
	 * @return
     */
	public String getRootClassName() {
		return rootClass;
	}

	/**
	 * Searches for root class and returns it.
	 * 
	 * @return
	 */
	public SClassDefinition getRootClass()
	{
		for (SClassDefinition cdef : ((ClassInterpreter) interpreter.getInterpreter()).getClasses())
		{
			if(cdef.getName().getName().toLowerCase().equals(rootClass.toLowerCase())) {
				return cdef;
			}
		}
		return null;
	}

	/**
	 * Searches returns all class names
	 *
	 * @return
	 */
	public List<String> getClassNames()
	{
		ArrayList<String> cls = new ArrayList<>();

		for (SClassDefinition cdef : ((ClassInterpreter) interpreter.getInterpreter()).getClasses())
		{
			cls.add(cdef.getName().getName());
		}

		return cls;
	}

	/**
	 * Gets operations of root class
	 * @return
     */
	public List<String> getOperationNames() {
		ArrayList<String> ops = new ArrayList<>();

		for (PDefinition def : getRootClass().getDefinitions())
		{
			if (def instanceof AExplicitOperationDefinition)
			{
				ops.add(def.getName().getName());
			}
		}

		return ops;
	}

	/**
	 * Sets the root class name
	 * @param rootClass
     */
	public void setRootClass(String rootClass) {
		this.rootClass = rootClass;
	}

	/**
	 * Returns the model structure
	 * 
	 * @return
	 */
	public ModelStructure getModelStructure() throws RootClassException {
		ModelStructureBuilder bld = new ModelStructureBuilder(interpreter);

		bld.setRootClass(this.rootClass);

		return bld.build();
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
		return interpreter.valueExecute(ROOT_VAR_NAME + "." + runExp);
	}
}
