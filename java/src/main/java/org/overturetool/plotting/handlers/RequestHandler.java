package org.overturetool.plotting.handlers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.overturetool.plotting.interpreter.ModelInteraction;
import org.overturetool.plotting.protocol.Message;
import org.overturetool.plotting.protocol.ModelStructure;
import org.overturetool.plotting.protocol.Node;
import org.overturetool.plotting.protocol.Request;

import javax.websocket.Session;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by John on 19-05-2016.
 */
public class RequestHandler extends MessageHandler<Request> {
    private ModelInteraction modelInteraction;

    public RequestHandler(ModelInteraction modelInteraction) {

        this.modelInteraction = modelInteraction;
    }

    @Override
    public void handle(Request message, Session session) {
        if(message.request.equals(Request.GET_MODEL_INFO)) {
            try {
                ModelStructure t = modelInteraction.getModelStructure();

                Message<ModelStructure> msg = new Message<>();
                msg.data = t;
                msg.type = ModelStructure.messageType;

                String serialized = new Gson().toJson(msg);

                session.getBasicRemote().sendText(serialized);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(message.request.equals(Request.RUN_MODEL)) {
            try {
                Message<String> msg = new Message<>();
                msg.type = "RESPONSE";
                msg.data = "OK";
                String serialized = new Gson().toJson(msg);

                session.getBasicRemote().sendText(serialized);
                this.modelInteraction.start("run()");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getMessageTypeName() {
        return Request.messageType;
    }

    @Override
    protected Type getType() {
        return new TypeToken<Message<Request>>() {}.getType();
    }
}
