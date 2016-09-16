package org.overturetool.graphics;

import org.overturetool.graphics.protocol.Message;
import org.overturetool.graphics.protocol.Request;
import org.overturetool.graphics.protocol.Subscription;

import com.google.gson.Gson;

public class MessageUtil
{
	static Gson gson = new Gson();

	public static String buildSetRootClassMessage(String name)
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Message<Request> msg = new Message();
		msg.type = Request.messageType;
		msg.data = new Request();
		msg.data.request = Request.SET_ROOT_CLASS;
		msg.data.parameter = name;
		String serialized = gson.toJson(msg);
		return serialized;
	}

	public static String buildSubscribeMessage(String name)
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Message<Subscription> msg = new Message();
		msg.type = Subscription.messageType;
		msg.data = new Subscription();
		msg.data.variableName = name;
		String serialized = gson.toJson(msg);
		return serialized;
	}

	public static String buildRunModelMessage()
	{
		Message<Request> rq = new Message<>();
		rq.type = Request.messageType;
		rq.data = new Request();
		rq.data.request = Request.RUN_MODEL;
		return gson.toJson(rq);
	}

	public static String buildGetModelInfo()
	{
		Message<Request> msg = new Message<Request>();
		msg.data = new Request("GetModelInfo");
		msg.type = Request.messageType;
		return gson.toJson(msg);
	}

	public static String buildGetClasses()
	{
		Message<Request> msg = new Message<Request>();
		msg.data = new Request(Request.GET_CLASS_INFO);
		msg.type = Request.messageType;
		return gson.toJson(msg);
	}

	public static String buildGetFunctions()
	{
		Message<Request> msg = new Message<Request>();
		msg.data = new Request(Request.GET_FUNCTION_INFO);
		msg.type = Request.messageType;
		return gson.toJson(msg);
	}
}
