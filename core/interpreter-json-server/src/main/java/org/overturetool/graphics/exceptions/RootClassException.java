package org.overturetool.graphics.exceptions;

/**
 * Created by John on 03-07-2016.
 */
public class RootClassException extends Exception
{
	/**
	 * serial
	 */
	private static final long serialVersionUID = 7733144344533219813L;

	public RootClassException()
	{
		super();
	}

	public RootClassException(String message)
	{
		super(message);
	}

	public RootClassException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public RootClassException(Throwable cause)
	{
		super(cause);
	}
}
