package org.overturetool.plotting.protocol;

/**
 * Created by John on 03-07-2016.
 */
public class Error {
    public static final String messageType = "ERROR";
    public String errorMessage;

    public Error(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public Error()
    {

    }
}
