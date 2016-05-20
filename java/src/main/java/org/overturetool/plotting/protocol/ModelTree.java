package org.overturetool.plotting.protocol;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by John on 19-05-2016.
 */
public class ModelTree {
    public static final String messageType = "MODEL";
    public Node tree = new Node();

    public ModelTree() {}

    public class Node {
        public String name;
        public String type;
        public Collection<Node> children = new ArrayList<Node>();

        public Node(String name, String type, Collection<Node> children) {
            this(name,type);
            this.children = children;
        }

        public Node(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public Node() {

        }

        public Node addNode(String name, String type) {
            Node n = new Node(name, type);
            children.add(n);
            return n;
        }
    }
}
