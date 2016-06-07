package org.overturetool.plotting;

import org.overturetool.plotting.protocol.Message;
import org.overturetool.plotting.protocol.Request;
import org.overturetool.plotting.protocol.Subscription;

import com.google.gson.Gson;

public class MessageUtil
{
	static Gson gson = new Gson();
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
}
