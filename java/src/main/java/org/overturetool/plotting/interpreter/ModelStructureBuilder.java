package org.overturetool.plotting.interpreter;

import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.definitions.AInstanceVariableDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.types.AClassType;
import org.overture.interpreter.debug.RemoteInterpreter;
import org.overture.interpreter.runtime.ClassInterpreter;
import org.overturetool.plotting.protocol.ModelStructure;
import org.overturetool.plotting.protocol.Node;

/**
 * Created by John on 26-05-2016.
 */
public class ModelStructureBuilder {
    private RemoteInterpreter interpreter;
    private int levels = 0;
    private boolean debug = false;
    private ModelStructure model = null;

    public ModelStructureBuilder(RemoteInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Finds root class and builds model structure from it
     * @return
     */
    public ModelStructure build() {
        // Only build model once
        if(model == null)
        {
            model = new ModelStructure();

            SClassDefinition cdef = findRootClass();
            if (cdef != null)
            {
                if(debug)
                    System.out.println("Class " + cdef.getName().getName());

                // Set root class
                model.setRootClass(cdef.getName().getName());

                // Read instance variables from root class
                readInstanceVarsFromClass(cdef);
            }
        }
        return model;
    }

    /**
     * Reads instance variables and adds them to local model
     * @param cdef
     * @param parent
     */
    private void readInstanceVarsFromClass(SClassDefinition cdef, Node currNode, String parent) {
        for (PDefinition def : cdef.getDefinitions())
        {
            if (def instanceof AInstanceVariableDefinition)
            {
                levels++;
                AInstanceVariableDefinition insVar = ((AInstanceVariableDefinition) def);

                // Create fully qualified name
                String prefix = parent.equals("") ? parent : parent + ".";
                String varName = prefix + def.getName().getName();

                if(debug)
                    System.out.println(tab(levels) + varName
                            + " type: " + def.getType());

                // Add instance variable to model
                Node newNode = currNode.addNode(varName, def.getType().toString());
                newNode.ptype = def.getType();

                // Is instance variable also a class?
                if(insVar.getExpType() instanceof AClassType) {
                    SClassDefinition innercdef = ((AClassType) insVar.getExpType()).getClassdef();

                    // No circular references.
                    if(!innercdef.equals(cdef))
                        readInstanceVarsFromClass(innercdef, newNode, varName);
                }
                levels--;
            }

            if (def instanceof AExplicitOperationDefinition)
            {
                levels++;
                if(def.getName().getName().equals("run")) {
                    if (debug)
                        System.out.println(tab(levels) + "this is root class.");
                }
                levels--;
            }
        }
    }

    private void readInstanceVarsFromClass(SClassDefinition cdef) {
        readInstanceVarsFromClass(cdef, model, "");
    }

    private String tab(int n) {
        return new String(new char[n]).replace("\0", "\t");
    }

    private SClassDefinition findRootClass() {

        for (SClassDefinition cdef : ((ClassInterpreter) interpreter.getInterpreter()).getClasses())
        {
            for (PDefinition def : cdef.getDefinitions())
            {
                if (def instanceof AExplicitOperationDefinition)
                {
                    if(def.getName().getName().toLowerCase().equals("run"))
                        return cdef;
                }
            }
        }
        return null;
    }
}
