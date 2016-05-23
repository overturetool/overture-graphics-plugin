package org.overturetool.plotting;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import org.junit.Assert;
import org.junit.Test;
import org.overture.ast.definitions.AInstanceVariableDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.lex.Dialect;
import org.overture.ast.types.ARealNumericBasicType;
import org.overture.ast.types.PType;
import org.overture.config.Release;
import org.overture.config.Settings;
import org.overture.interpreter.debug.RemoteControl;
import org.overture.interpreter.debug.RemoteInterpreter;
import org.overture.interpreter.runtime.ClassInterpreter;
import org.overture.interpreter.runtime.Interpreter;
import org.overture.interpreter.util.ClassListInterpreter;
import org.overture.interpreter.util.InterpreterUtil;
import org.overture.interpreter.values.Value;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;
import org.overturetool.plotting.interpreter.BoundVarInfo;
import org.overturetool.plotting.interpreter.TempoRemoteControl;

public class FetchModelStructureTest
{
	private void runWithRemoteConsole(File specRoot, final RemoteControl remote)
			throws Exception
	{
		Settings.release = Release.VDM_10;
		Settings.dialect = Dialect.VDM_PP;

		File[] files = specRoot.listFiles((d, name) -> name.endsWith(".vdmpp"));

		TypeCheckResult<List<SClassDefinition>> result = TypeCheckerUtil.typeCheckPp(Arrays.asList(files));

		if (result.parserResult.errors.isEmpty() && result.errors.isEmpty())
		{
			ClassListInterpreter list = new ClassListInterpreter();
			list.addAll(result.result);
			Interpreter interpreter = InterpreterUtil.getInterpreter(list);
			interpreter.init(null);
			interpreter.setDefaultName(list.get(0).getName().getName());

			final RemoteInterpreter remoteInterpreter = new RemoteInterpreter(interpreter, null);

			Thread remoteThread = new Thread(new Runnable()
			{

				public void run()
				{
					try
					{
						remote.run(remoteInterpreter);
					} catch (Exception e)
					{
						System.err.println(e.getMessage());
						Assert.fail(e.getMessage());
					}
				}
			});
			remoteThread.setName("RemoteControl runner");
			remoteThread.setDaemon(true);
			remoteThread.start();
			remoteInterpreter.processRemoteCalls();
		}

		else
		{
			Assert.fail(result.parserResult.errors.toString() + "\n"
					+ result.errors.toString());
		}
	}

	@Test
	public void test() throws Exception
	{

		final RemoteControl remote = new RemoteControl()
		{

			public void run(RemoteInterpreter interpreter) throws Exception
			{
				System.out.println(interpreter.execute("1+1"));

				for (SClassDefinition cdef : ((ClassInterpreter) interpreter.getInterpreter()).getClasses())
				{
					System.out.println("Class " + cdef.getName().getName());
					for (PDefinition def : cdef.getDefinitions())
					{
						if (def instanceof AInstanceVariableDefinition)
						{
							System.out.println("\t" + def.getName().getName()
									+ " type: " + def.getType());
						}
					}
				}

				interpreter.finish();
			}
		};
		runWithRemoteConsole(new File("src/test/resources/test1".replace('/', File.separatorChar)), remote);
	}

	@Test
	public void test2() throws Exception
	{

		final RemoteControl remote = new TempoRemoteControl(null, null, null)
		{

			public void run(RemoteInterpreter interpreter) throws Exception
			{
				super.run(interpreter);
				interpreter.finish();
			}
		};
		runWithRemoteConsole(new File("src/test/resources/test1".replace('/', File.separatorChar)), remote);
	}

	public static class Test2BindClass
	{
		public void setVar_real(double o)
		{
			System.out.println("Level set to: " + o);
		}
	}

	@Test
	public void test2WithRun() throws Exception
	{
		Semaphore sem = new Semaphore(1);
		sem.acquire();

		Object bindClass = new Test2BindClass();

		List<BoundVarInfo> bindings = new Vector<>();
		bindings.add(new BoundVarInfo()
		{

			@Override
			public PType type()
			{
				return new ARealNumericBasicType();
			}

			@Override
			public String name()
			{
				return "var_real";
			}
		});

		final TempoRemoteControl remote = new TempoRemoteControl(bindClass, bindings, new TempoRemoteControl.IInterpreterReadyCallback()
		{

			@Override
			public void initialized(TempoRemoteControl controller)
			{
				sem.release();
			}
		});

		Thread t = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					runWithRemoteConsole(new File("src/test/resources/test2".replace('/', File.separatorChar)), remote);

				} catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t.setDaemon(true);
		t.start();

		sem.acquire();
		Value val = remote.start("new Test2()", "run()");
		System.out.println("Interpreter exited with: " + val);
		return;
	}
}
