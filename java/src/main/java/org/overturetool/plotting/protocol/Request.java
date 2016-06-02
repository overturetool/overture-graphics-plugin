package org.overturetool.plotting.protocol;

public class Request {
    public static final String messageType = "REQUEST";
    public static final String RUN_MODEL  = "RunModel";
    public static final String GET_MODEL_INFO  = "GetModelInfo";
    public String request;

    public Request(String request) {
        this.request = request;
    }

    public Request() {

    }
}
