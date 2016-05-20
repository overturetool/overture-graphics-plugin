package org.overturetool.plotting.protocol;

public class Request {
    public static final String messageType = "REQ";
    public String request;

    public Request(String request) {
        this.request = request;
    }

    public Request() {

    }
}
