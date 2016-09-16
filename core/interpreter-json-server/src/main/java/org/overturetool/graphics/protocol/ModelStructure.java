package org.overturetool.graphics.protocol;

import java.util.LinkedList;

/**
 * Created by John on 19-05-2016.
 */
public class ModelStructure extends Node
{
	private String rootClass;

	public static final String messageType = "MODEL";

	public ModelStructure()
	{
	}

	public String getRootClass()
	{
		return rootClass;
	}

	public void setRootClass(String rootClass)
	{
		this.rootClass = rootClass;
	}

	public Node addNode(String name, String type)
	{
		Node n = new Node(name, type);
		children.add(n);
		return n;
	}

	public Node findNode(String name)
	{
		// Breadth first traversal
		LinkedList<Node> queue = new LinkedList<>();
		queue.addAll(children);

		while (queue.peek() != null)
		{
			Node n = queue.remove();

			if (n.name.equals(name))
			{
				return n;
			}

			if (n.children.size() > 0)
			{
				queue.addAll(n.children);
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object obj)
	{
		ModelStructure other = (ModelStructure) obj;
		if (!other.getRootClass().equals(rootClass))
		{
			return false;
		}

		if (!super.equals(other))
		{
			return false;
		}

		return true;
	}
}
