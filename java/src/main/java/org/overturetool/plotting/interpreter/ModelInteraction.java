package org.overturetool.plotting.interpreter;

import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.interpreter.debug.RemoteInterpreter;
import org.overture.interpreter.runtime.ClassInterpreter;
import org.overture.interpreter.values.*;
import org.overturetool.plotting.protocol.ModelStructure;
import org.overturetool.plotting.protocol.Node;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Supplier;

/**
 * Created by John on 26-05-2016.
 */
public class ModelInteraction {
    private static final String ROOT_NAME = "root";
    private RemoteInterpreter interpreter;

    public ModelInteraction(RemoteInterpreter interpreter){
        this.interpreter = interpreter;
    }

    /**
     * Attaches a listener to variable (format: objX.objY.objZ.val)
     * @param var
     * @throws Exception
     */
    public void attachListener(Node var, ValueListener listener) throws Exception {
        // Get root class instance
        Value v = interpreter.valueExecute(ROOT_NAME);
        NameValuePairMap members;

        // Tokenize variable name
        StringTokenizer tokenizer = new StringTokenizer(var.name, ".");
        String[] tokens = new String[tokenizer.countTokens()];
        for (int i = 0; tokenizer.hasMoreTokens(); i++) {
            tokens[i] = tokenizer.nextToken();
        }

        // Find parent object
        for(int i = 0; i < tokens.length-1; i++) {
            if (v.deref() instanceof ObjectValue)
            {
                members = ((ObjectValue) v.deref()).members;
                for (Map.Entry<ILexNameToken, Value> p : members.entrySet())
                {
                    if (tokens[i].equals(p.getKey().getName()))
                    {
                        v = p.getValue();
                    }
                }
            }
        }

        // Find child object to bind to
        if (v.deref() instanceof ObjectValue)
        {
            members = ((ObjectValue) v.deref()).members;
            for (Map.Entry<ILexNameToken, Value> p : members.entrySet())
            {
                if (tokens[tokens.length-1].equals(p.getKey().getName()))
                {
                    if (p.getValue() instanceof UpdatableValue)
                    {
                        UpdatableValue u = (UpdatableValue) p.getValue();
                        u.addListener(listener);
                        break;
                    }
                }
            }
        }
    }
    /**
     * Searches for root class (with run method) and returns its name.
     * @return
     */
    public String getRootClassName() {
        for (SClassDefinition cdef : ((ClassInterpreter) interpreter.getInterpreter()).getClasses())
        {
            for (PDefinition def : cdef.getDefinitions())
            {
                if (def instanceof AExplicitOperationDefinition)
                {
                    if(def.getName().getName().toLowerCase().equals("run"))
                        return cdef.getName().getName();
                }
            }
        }
        return null;
    }

    /**
     * Returns the model structure
     * @return
     */
    public ModelStructure getModelStructure() {
        ModelStructureBuilder bld = new ModelStructureBuilder(interpreter);

        return bld.build();
    }
}
