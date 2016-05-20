package org.overturetool.plotting.handlers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.overturetool.plotting.protocol.Message;
import org.overturetool.plotting.protocol.ModelTree;
import org.overturetool.plotting.protocol.Request;

import javax.websocket.Session;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by John on 19-05-2016.
 */
public class RequestHandler extends MessageHandler<Request> {
    @Override
    public void handle(Request message, Session session) {
        if(message.request.equals("GetModelInfo")) {
            try {
                // TODO: Return tree with model info
                ModelTree t = new ModelTree();
                t.tree.type = "class";
                t.tree.name = "VDMModel";
                ModelTree.Node plot3d = t.tree.addNode("3d","seqOfSeq");
                plot3d.addNode("Congestion","seqOfInt");
                plot3d.addNode("Pollution","seqOfInt");
                ModelTree.Node plot2d = t.tree.addNode("2d","seqOfSeq");
                plot2d.addNode("Congestion","seqOfInt");
                plot2d.addNode("Pollution","seqOfInt");

                String serialized = new Gson().toJson(t);

                session.getBasicRemote().sendText(serialized);
            } catch (IOException e) {
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
