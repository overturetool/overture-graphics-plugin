package org.overturetool.plotting.interpreter;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.overture.interpreter.debug.RemoteControl;
import org.overture.interpreter.debug.RemoteInterpreter;
import org.overturetool.plotting.RunModel;
import org.overturetool.plotting.protocol.ModelStructure;
import org.overturetool.plotting.protocol.Node;

/**
 * Created by John on 26-05-2016.
 */
public class ModelStructureBuilderTest
{

	@Test
	public void testBuild() throws Exception
	{
		final RemoteControl remote = new RemoteControl()
		{
			public void run(RemoteInterpreter interpreter) throws Exception
			{
				ModelStructureBuilder builder = new ModelStructureBuilder(interpreter);

				@SuppressWarnings("unused")
				ModelStructure structure = builder.build();

				interpreter.finish();
			}
		};
		RunModel.runWithRemoteConsole(new File("src/test/resources/test3".replace('/', File.separatorChar)), remote);
	}

	@Test
	public void testBuildSetsCorrectRootClass() throws Exception
	{
		final RemoteControl remote = new RemoteControl()
		{
			public void run(RemoteInterpreter interpreter) throws Exception
			{
				ModelStructureBuilder builder = new ModelStructureBuilder(interpreter);

				ModelStructure structure = builder.build();

				interpreter.finish();

				Assert.assertTrue(structure.getRootClass().equals("Test3"));
			}
		};
		RunModel.runWithRemoteConsole(new File("src/test/resources/test3".replace('/', File.separatorChar)), remote);
	}

	@Test
	public void testBuildSetsCorrectAmountOfChildren() throws Exception
	{
		final RemoteControl remote = new RemoteControl()
		{
			public void run(RemoteInterpreter interpreter) throws Exception
			{
				ModelStructureBuilder builder = new ModelStructureBuilder(interpreter);

				ModelStructure structure = builder.build();

				interpreter.finish();

				Assert.assertTrue(structure.children.size() == 3);
			}
		};
		RunModel.runWithRemoteConsole(new File("src/test/resources/test3".replace('/', File.separatorChar)), remote);
	}

	@Test
	public void testBuildWithNoRootClassReturnsEmptyModelStructure()
			throws Exception
	{
		final RemoteControl remote = new RemoteControl()
		{
			public void run(RemoteInterpreter interpreter) throws Exception
			{
				ModelStructureBuilder builder = new ModelStructureBuilder(interpreter);

				ModelStructure actual = builder.build();

				interpreter.finish();

				Assert.assertTrue(actual.getRootClass() == null);
				Assert.assertTrue(actual.children.size() == 0);
			}
		};
		RunModel.runWithRemoteConsole(new File("src/test/resources/test1".replace('/', File.separatorChar)), remote);
	}

	@Test
	public void testBuildWithRootClassReturnsCorrectModelStructure()
			throws Exception
	{
		ModelStructure expected = new ModelStructure();
		expected.setRootClass("Test2");
		expected.addNode("var_int", "int");
		expected.addNode("var_real", "real");

		final RemoteControl remote = new RemoteControl()
		{
			public void run(RemoteInterpreter interpreter) throws Exception
			{
				ModelStructureBuilder builder = new ModelStructureBuilder(interpreter);

				ModelStructure actual = builder.build();

				interpreter.finish();

				Assert.assertTrue(actual.equals(expected));
			}
		};
		RunModel.runWithRemoteConsole(new File("src/test/resources/test2".replace('/', File.separatorChar)), remote);
	}

	@Test
	public void testBuildWithRootClassReturnsCorrectModelStructureSeveralLayers()
			throws Exception
	{
		ModelStructure expected = new ModelStructure();
		expected.setRootClass("Test3");
		expected.addNode("var_int", "seq of (int)");
		expected.addNode("var_real", "real");
		Node n = expected.addNode("var_test1", "Test1");
		n.addNode("var_test1.var_int", "int");
		n.addNode("var_test1.var_real", "real");
		n = n.addNode("var_test1.var_test2", "Test2");
		n.addNode("var_test1.var_test2.var_int", "int");
		n.addNode("var_test1.var_test2.var_real", "real");

		final RemoteControl remote = new RemoteControl()
		{
			public void run(RemoteInterpreter interpreter) throws Exception
			{
				ModelStructureBuilder builder = new ModelStructureBuilder(interpreter);

				ModelStructure actual = builder.build();

				interpreter.finish();

				Assert.assertTrue(expected.equals(actual));
			}
		};
		RunModel.runWithRemoteConsole(new File("src/test/resources/test3".replace('/', File.separatorChar)), remote);
	}

	@Test
	public void testModelStructureFindNode()
	{
		String find = "var_test1.var_real";

		ModelStructure expected = new ModelStructure();
		expected.setRootClass("Test3");
		expected.addNode("var_int", "seq of (int)");
		expected.addNode("var_real", "real");
		Node n = expected.addNode("var_test1", "Test1");
		n.addNode("var_test1.var_int", "int");
		n.addNode("var_test1.var_real", "real");
		n = n.addNode("var_test1.var_test2", "Test2");
		n.addNode("var_test1.var_test2.var_int", "int");
		n.addNode("var_test1.var_test2.var_real", "real");

		Node a = expected.findNode(find);

		Assert.assertTrue(a.name.equals(find));
	}
}