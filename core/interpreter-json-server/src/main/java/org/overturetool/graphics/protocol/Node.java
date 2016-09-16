package org.overturetool.graphics.protocol;

import java.util.ArrayList;
import java.util.Collection;

import org.overture.ast.types.PType;

public class Node
{
	public String name = "";
	public String type = "";
	public transient PType ptype = null;
	public Collection<Node> children = new ArrayList<Node>();

	public Node(String name, String type, Collection<Node> children)
	{
		this(name, type);
		this.children = children;
	}

	public Node(String name, String type)
	{
		this.name = name;
		this.type = type;
	}

	public Node()
	{

	}

	public Node addNode(String name, String type)
	{
		Node n = new Node(name, type);
		children.add(n);
		return n;
	}

	@Override
	public boolean equals(Object obj)
	{
		Node other = (Node) obj;
		if (children.size() != other.children.size()
				|| !other.name.equals(name) || !other.type.equals(type))
		{
			return false;
		}

		for (int i = 0; i < children.size(); i++)
		{
			if (!((ArrayList<Node>) children).get(i).equals(((ArrayList<Node>) other.children).get(i)))
			{
				return false;
			}
		}

		return true;
	}
}
