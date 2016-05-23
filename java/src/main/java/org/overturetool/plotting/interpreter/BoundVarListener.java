package org.overturetool.plotting.interpreter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.types.ABooleanBasicType;
import org.overture.ast.types.ACharBasicType;
import org.overture.ast.types.AIntNumericBasicType;
import org.overture.ast.types.ARealNumericBasicType;
import org.overture.ast.types.PType;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.ValueListener;

/**
 * @author ldc
 */
public class BoundVarListener implements ValueListener
{

	final Object boundObject;
	final PType type;
	final String setterName;

	public BoundVarListener(Object d2, String name, PType t2)
	{
		boundObject = d2;
		type = t2;
		setterName = makeSetter(name);
	}

	private String makeSetter(String name)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("set");
		sb.append(Character.toUpperCase(name.charAt(0)));
		sb.append(name.substring(1, name.length()));
		return sb.toString();
	}

	@Override
	public void changedValue(ILexLocation loc, Value val, Context ctxt)
			throws AnalysisException
	{
		try
		{

			if (ctxt.assistantFactory.createPTypeAssistant().isType(type, AIntNumericBasicType.class))
			{
				Method method;
				method = boundObject.getClass().getDeclaredMethod(setterName, long.class);
				method.invoke(boundObject, val.intValue(ctxt));
			}

			if (ctxt.assistantFactory.createPTypeAssistant().isType(type, ARealNumericBasicType.class))
			{
				Method method;
				method = boundObject.getClass().getDeclaredMethod(setterName, double.class);
				method.invoke(boundObject, val.realValue(ctxt));
			}

			if (ctxt.assistantFactory.createPTypeAssistant().isType(type, ABooleanBasicType.class))
			{
				Method method;
				method = boundObject.getClass().getDeclaredMethod(setterName, boolean.class);
				method.invoke(boundObject, val.boolValue(ctxt));
			}

			if (isString(val, ctxt))
			{
				Method method;
				method = boundObject.getClass().getDeclaredMethod(setterName, String.class);
				method.invoke(boundObject, val.stringValue(ctxt));
			}

		} catch (NoSuchMethodException e)
		{
			throw new AnalysisException(e.getMessage(), e);
		} catch (SecurityException e)
		{
			throw new AnalysisException(e.getMessage(), e);
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
			throw new AnalysisException(e.getMessage(), e);
		} catch (IllegalArgumentException e)
		{
			throw new AnalysisException(e.getMessage(), e);
		} catch (InvocationTargetException e)
		{
			throw new AnalysisException(e.getMessage(), e);
		}
	}

	private boolean isString(Value val, Context ctxt)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, ValueException
	{

		return ctxt.assistantFactory.createPTypeAssistant().isSeq(type) // is
																		// Seq
				&& ctxt.assistantFactory.createPTypeAssistant().isType(ctxt.assistantFactory.createPTypeAssistant() // of
																													// char
				.getSeq(type).getSeqof(), ACharBasicType.class);

	}
}
