package org.overturetool.plotting.protocol;

public class Request
{
	public static final String messageType = "REQUEST";
	public static final String RUN_MODEL = "RunModel";
	public static final String GET_MODEL_INFO = "GetModelInfo";
	public static final String GET_CLASS_INFO = "GetClassInfo";
	public static final String GET_FUNCTION_INFO = "GetFunctionInfo";
	public static final String SET_ROOT_CLASS = "SetRootClass";
	public static final String STOP_SERVER = "StopServer";
	public String request;
	public String parameter;

	public Request(String request, String parameter)
	{
		this.request = request;
		this.parameter = parameter;
	}
	public Request(String request)
	{
		this.request = request;
	}

	public Request()
	{

	}
}
