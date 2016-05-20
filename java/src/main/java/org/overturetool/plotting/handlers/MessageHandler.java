package org.overturetool.plotting.handlers;

import com.google.gson.Gson;
import org.overturetool.plotting.protocol.Message;

import javax.websocket.Session;
import java.lang.reflect.Type;

/**
 * Created by John on 19-05-2016.
 */
public abstract class MessageHandler<T> {
    public abstract void handle(T message, Session session);
    public abstract String getMessageTypeName();
    protected abstract Type getType();

    public T deserializeMessage(String msg) {
        Gson gson = new Gson();
        Message<T> deserialized = gson.fromJson(msg, getType());

        return deserialized.data;
    }
}
