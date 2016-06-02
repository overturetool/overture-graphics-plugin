package org.overturetool.plotting.handlers;

import com.google.gson.reflect.TypeToken;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.types.ABooleanBasicType;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.ValueListener;
import org.overturetool.plotting.interpreter.SessionVarListener;
import org.overturetool.plotting.interpreter.ModelInteraction;
import org.overturetool.plotting.protocol.Message;
import org.overturetool.plotting.protocol.ModelStructure;
import org.overturetool.plotting.protocol.Node;
import org.overturetool.plotting.protocol.Subscription;

import javax.websocket.Session;
import java.lang.reflect.Type;

/**
 * Created by John on 19-05-2016.
 */
public class SubscriptionHandler extends MessageHandler<Subscription> {

    private ModelInteraction modelInteraction;

    public SubscriptionHandler(ModelInteraction modelInteraction) {

        this.modelInteraction = modelInteraction;
    }

    @Override
    public void handle(Subscription message, Session session) {
        // Get mode structure and find node corresponding to variable name
        ModelStructure structure = modelInteraction.getModelStructure();
        Node n = structure.findNode(message.variableName);

        // Could not find node
        if(n == null)
            return;

        // Else attach listener
        try {
            modelInteraction.attachListener(n, new SessionVarListener(n.name, n.ptype, session));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMessageTypeName() {
        return Subscription.messageType;
    }

    @Override
    protected Type getType() {
        return new TypeToken<Message<Subscription>>() {}.getType();
    }
}
