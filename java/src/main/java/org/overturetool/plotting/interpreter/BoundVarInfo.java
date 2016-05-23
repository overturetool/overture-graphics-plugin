package org.overturetool.plotting.interpreter;

import org.overture.ast.types.PType;

public interface BoundVarInfo
{

	abstract String name();

	abstract PType type();
}