package org.overturetool.graphics;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.lex.Dialect;
import org.overture.config.Release;
import org.overture.config.Settings;
import org.overture.interpreter.debug.RemoteControl;
import org.overture.interpreter.debug.RemoteInterpreter;
import org.overture.interpreter.runtime.Interpreter;
import org.overture.interpreter.util.ClassListInterpreter;
import org.overture.interpreter.util.InterpreterUtil;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;

public class RunModel
{
	public static void runWithRemoteConsole(File specRoot,
			final RemoteControl remote) throws Exception
	{
		Settings.release = Release.VDM_10;
		Settings.dialect = Dialect.VDM_PP;

		File[] files = FileUtils.listFiles(specRoot, new String[] { "vdmpp" }, true).toArray(new File[0]);

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
}
