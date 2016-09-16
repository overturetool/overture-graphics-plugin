package org.overturetool.graphics.interpreter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.websocket.Session;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.types.ABooleanBasicType;
import org.overture.ast.types.ACharBasicType;
import org.overture.ast.types.AIntNumericBasicType;
import org.overture.ast.types.ANatNumericBasicType;
import org.overture.ast.types.ANatOneNumericBasicType;
import org.overture.ast.types.ARealNumericBasicType;
import org.overture.ast.types.PType;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.ValueList;
import org.overture.interpreter.values.ValueListener;
import org.overturetool.graphics.protocol.Message;
import org.overturetool.graphics.protocol.UpdateValue;

import com.google.gson.Gson;

public class SessionVarListener implements ValueListener
{
	final String name;
	private PType type;
	private Session session;
	private Gson gson = new Gson();

	public SessionVarListener(String name, PType type, Session session)
	{
		this.name = name;
		this.type = type;
		this.session = session;
	}

	@Override
	public void changedValue(ILexLocation loc, Value val, Context ctxt)
			throws AnalysisException
	{
		if (!session.isOpen())
		{
			return;
		}

		// Initialize message
		Message<UpdateValue> msg = new Message<>();
		msg.type = UpdateValue.messageType;
		msg.data = new UpdateValue();
		msg.data.type = type.toString();
		msg.data.variableName = name;

		// Extract value from VDM Value
		try
		{
			if (isType(val, ctxt, AIntNumericBasicType.class))
			{
				msg.data.value = gson.toJson(val.intValue(ctxt));
			}

			if (isType(val, ctxt, ARealNumericBasicType.class))
			{
				msg.data.value = gson.toJson(val.realValue(ctxt));
			}

			if (isType(val, ctxt, ABooleanBasicType.class))
			{
				msg.data.value = gson.toJson(val.boolValue(ctxt));
			}

			if (isType(val, ctxt, ANatNumericBasicType.class))
			{
				msg.data.value = gson.toJson(val.natValue(ctxt));
			}

			if (isType(val, ctxt, ANatOneNumericBasicType.class))
			{
				msg.data.value = gson.toJson(val.nat1Value(ctxt));
			}

			if (isSeqOf(val, ctxt, AIntNumericBasicType.class))
			{
				ValueList list = val.seqValue(ctxt);
				msg.data.value = getSeqValue(list, ctxt);
			}

			if (isSeqOf(val, ctxt, ARealNumericBasicType.class))
			{
				ValueList list = val.seqValue(ctxt);
				msg.data.value = getSeqValue(list, ctxt);
			}

			if (isSeqOf(val, ctxt, ABooleanBasicType.class))
			{
				ValueList list = val.seqValue(ctxt);
				msg.data.value = getSeqValue(list, ctxt);
			}

			if (isString(val, ctxt))
			{
				msg.data.value = gson.toJson(val.stringValue(ctxt));
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

		// Send message
		String serialized = new Gson().toJson(msg);
		try
		{
			if (session.isOpen() && !session.getOpenSessions().isEmpty())
			{
				session.getBasicRemote().sendText(serialized);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private boolean isString(Value val, Context ctxt)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, ValueException
	{
		return isSeqOf(val, ctxt, ACharBasicType.class);
	}

	private boolean isSeqOf(Value val, Context ctxt, Class<? extends PType> t)
	{
		return ctxt.assistantFactory.createPTypeAssistant().isSeq(type) // is Seq
				&& ctxt.assistantFactory.createPTypeAssistant().isType(ctxt.assistantFactory.createPTypeAssistant().getSeq(type).getSeqof(), t); // of
																																					// type
																																					// t
	}

	private boolean isType(Value val, Context ctxt, Class<? extends PType> t)
	{
		return ctxt.assistantFactory.createPTypeAssistant().isType(type, t);
	}

	private String getSeqValue(ValueList list, Context ctxt)
			throws ValueException
	{
		Value first = list.get(0);
		if (first == null)
		{
			return null;
		}

		switch (getPrimitive(first, ctxt))
		{
			case AIntNumericBasicType:
			{
				ArrayList<Long> res = new ArrayList<>();

				for (Value v : list)
				{
					res.add(v.intValue(ctxt));
				}
				return gson.toJson(res);
			}
			case ARealNumericBasicType:
			{
				ArrayList<Double> res = new ArrayList<>();

				for (Value v : list)
				{
					res.add(v.realValue(ctxt));
				}
				return gson.toJson(res);
			}
			case ABooleanBasicType:
			{
				ArrayList<Boolean> res = new ArrayList<>();

				for (Value v : list)
				{
					res.add(v.boolValue(ctxt));
				}
				return gson.toJson(res);
			}
			case ACharBasicType:
			{
				ArrayList<String> res = new ArrayList<>();

				for (Value v : list)
				{
					res.add(v.stringValue(ctxt));
				}
				return gson.toJson(res);
			}
		}
		return null;
	}

	private Primitive getPrimitive(Value val, Context ctxt)
	{
		if (val != null)
		{
			if (isType(val, ctxt, AIntNumericBasicType.class))
			{
				return Primitive.AIntNumericBasicType;
			}

			if (isType(val, ctxt, ARealNumericBasicType.class))
			{
				return Primitive.ARealNumericBasicType;
			}

			if (isType(val, ctxt, ABooleanBasicType.class))
			{
				return Primitive.ABooleanBasicType;
			}

			if (isType(val, ctxt, ACharBasicType.class))
			{
				return Primitive.ACharBasicType;
			}
		}
		return null;
	}

	enum Primitive
	{
		AIntNumericBasicType, ARealNumericBasicType, ABooleanBasicType, ACharBasicType
	}
}
