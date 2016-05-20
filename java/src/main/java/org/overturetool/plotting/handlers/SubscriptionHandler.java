package org.overturetool.plotting.handlers;

import com.google.gson.reflect.TypeToken;
import org.overturetool.plotting.protocol.Message;
import org.overturetool.plotting.protocol.Subscription;

import javax.websocket.Session;
import java.lang.reflect.Type;

/**
 * Created by John on 19-05-2016.
 */
public class SubscriptionHandler extends MessageHandler<Subscription> {

    @Override
    public void handle(Subscription message, Session session) {
        // TODO: Add subscription to (subscription,session) map
    }

    public String getMessageTypeName() {
        return Subscription.messageType;
    }

    @Override
    protected Type getType() {
        return new TypeToken<Message<Subscription>>() {}.getType();
    }
}
