package org.overturetool.graphics.interpreter;

import java.util.LinkedList;

import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.definitions.AInstanceVariableDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.expressions.ASeqEnumSeqExp;
import org.overture.ast.expressions.PExp;
import org.overture.ast.types.AClassType;
import org.overture.ast.types.ASeq1SeqType;
import org.overture.interpreter.debug.RemoteInterpreter;
import org.overture.interpreter.runtime.ClassInterpreter;
import org.overturetool.graphics.exceptions.RootClassException;
import org.overturetool.graphics.protocol.ModelStructure;
import org.overturetool.graphics.protocol.Node;

/**
 * Created by John on 26-05-2016.
 */
public class ModelStructureBuilder
{
	private static final String ROOT_VAR_NAME = "root";
	private RemoteInterpreter interpreter;
	private int levels = 0;
	private boolean debug = false;
	private static ModelStructure model = null;
	private SClassDefinition rootClass = null;

	public ModelStructureBuilder(RemoteInterpreter interpreter)
	{
		this.interpreter = interpreter;
	}

	public ModelStructureBuilder setRootClass(String name)
			throws RootClassException
	{
		for (SClassDefinition cdef : ((ClassInterpreter) interpreter.getInterpreter()).getClasses())
		{
			if (cdef.getName().getName().toLowerCase().equals(name.toLowerCase()))
			{
				rootClass = cdef;
				return this;
			}
		}
		throw new RootClassException("Chosen root class was not found.");
	}

	/**
	 * Finds root class and builds model structure from it
	 * 
	 * @return
	 */
	public ModelStructure build() throws RootClassException
	{
		// Only build model once
		if (model == null
				|| !model.getRootClass().toLowerCase().equals(rootClass.getName().getName().toLowerCase()))
		{
			model = new ModelStructure();

			SClassDefinition cdef = this.rootClass;
			if (cdef != null)
			{
				String rootClassName = cdef.getName().getName();
				if (debug)
				{
					System.out.println("Class " + rootClassName);
				}

				// Create root class instance
				try
				{
					interpreter.create(ModelInteraction.ROOT_VAR_NAME, "new "
							+ rootClassName + "()");
				} catch (Exception e)
				{
					throw new RootClassException("Root class instance could not be created.");
				}

				// Set root class
				model.setRootClass(rootClassName);

				// Read instance variables from root class
				readInstanceVarsFromClass(cdef);
			} else
			{
				throw new RootClassException("Root class was not set before attempting to build ModelStructure.");
			}
		}
		return model;
	}

	/**
	 * Reads instance variables and adds them to local model
	 * 
	 * @param cdef
	 * @param parent
	 */
	private void readInstanceVarsFromClass(SClassDefinition cdef,
			Node currNode, String parent)
	{
		for (PDefinition def : cdef.getDefinitions())
		{
			if (def instanceof AInstanceVariableDefinition)
			{
				levels++;
				AInstanceVariableDefinition insVar = (AInstanceVariableDefinition) def;

				// Create fully qualified name
				String prefix = parent.equals("") ? parent : parent + ".";
				String varName = prefix + def.getName().getName();

				if (debug)
				{
					System.out.println(tab(levels) + varName + " type: "
							+ def.getType());
				}

				// Add instance variable to model
				Node newNode = currNode.addNode(varName, def.getType().toString());
				newNode.ptype = def.getType();

				// Is instance variable also a class?
				if (insVar.getExpType() instanceof AClassType)
				{
					SClassDefinition innercdef = ((AClassType) insVar.getExpType()).getClassdef();

					// No circular references.
					if (!innercdef.equals(cdef))
					{
						readInstanceVarsFromClass(innercdef, newNode, varName);
					}
				}

				// Is instance variable a seq?
				if (insVar.getExpType() instanceof ASeq1SeqType)
				{
					LinkedList<PExp> members = ((ASeqEnumSeqExp) insVar.getExpression()).getMembers();

					// VDM indexing starting at 1:
					int i = 1;
					for (PExp member : members)
					{
						Node seqMember = newNode.addNode(varName + "(" + i++
								+ ")", member.getType().toString());
						seqMember.ptype = member.getType();
					}
				}

				// Is instance variable a set?
				/*
				 * if (insVar.getExpType() instanceof ASetType) { LinkedList<PExp> members =
				 * ((ASetEnumSetExp)insVar.getExpression()).getMembers(); // VDM indexing starting at 1: int i = 1; for
				 * (PExp member : members) { Node setMember = newNode.addNode(varName + "(" + i++ + ")",
				 * member.getType().toString()); setMember.ptype = member.getType(); } }
				 */
				levels--;
			}

			if (def instanceof AExplicitOperationDefinition)
			{
				levels++;
				if (def.getName().getName().equals("run"))
				{
					if (debug)
					{
						System.out.println(tab(levels) + "this is root class.");
					}
				}
				levels--;
			}
		}
	}

	private void readInstanceVarsFromClass(SClassDefinition cdef)
	{
		readInstanceVarsFromClass(cdef, model, "");
	}

	private String tab(int n)
	{
		return new String(new char[n]).replace("\0", "\t");
	}
}
