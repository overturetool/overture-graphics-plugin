package org.overturetool.graphics.interpreter;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.overture.interpreter.debug.RemoteControl;
import org.overture.interpreter.debug.RemoteInterpreter;
import org.overture.interpreter.values.Value;
import org.overturetool.graphics.handlers.RequestHandler;
import org.overturetool.graphics.handlers.SubscriptionHandler;
import org.overturetool.graphics.server.SubscriptionService;

public class JsonServerRemoteControl implements RemoteControl
{
	public interface IInterpreterReadyCallback
	{
		void initialized(JsonServerRemoteControl controller);
	}

	private IInterpreterReadyCallback callback = null;
	private RemoteInterpreter interpreter;
	private ModelInteraction modelInteraction;
	private SubscriptionService subSvc;

	/**
	 * controller for the TEMPO UI to run in Overture this must have a public default constructor This constructor is
	 * used for testing
	 * 
	 * @param callback
	 *            a callback called once the controller is initialized
	 */
	public JsonServerRemoteControl(IInterpreterReadyCallback callback)
	{
		this.callback = callback;
	}

	public JsonServerRemoteControl()
	{
	}

	/**
	 * Method to be called from overture
	 * 
	 * @param interpreter
	 * @throws Exception
	 */
	public void run(RemoteInterpreter interpreter) throws Exception
	{
		this.interpreter = interpreter;
		this.modelInteraction = new ModelInteraction(interpreter);

		this.setupServer();

		if (callback != null)
		{
			callback.initialized(this);
		}
	}

	/**
	 * Starts the interpreter with a 'new' expression e.g. 'new A()', and a run method like run() from A returns only if
	 * the model exits
	 * 
	 * @param runExp
	 * @throws Exception
	 */
	public Value start(String runExp) throws Exception
	{
		return this.modelInteraction.start(runExp);
	}

	/**
	 * Stops the interpreter from running
	 */
	public void stop() throws Exception
	{
		interpreter.finish();
		subSvc.stopServer();
	}

	/**
	 * Setup server
	 */
	private void setupServer()
	{
		// Create and start server
		this.subSvc = new SubscriptionService();

		// Setup handlers
		RequestHandler rqHandler = new RequestHandler(modelInteraction, this);
		SubscriptionHandler subscriptionHandler = new SubscriptionHandler(modelInteraction);

		this.subSvc.addMessageHandler(rqHandler);
		this.subSvc.addMessageHandler(subscriptionHandler);
		this.subSvc.startServer(8080);

		InputStream proFile = this.getClass().getClassLoader().getResourceAsStream("overture.graphics.properties");
		if (proFile != null)
		{
			Properties prop = new Properties();
			try
			{
				prop.load(proFile);

				for (Entry<Object, Object> p : prop.entrySet())
				{
					System.out.println(p.getKey() + " = " + p.getValue());
				}
				if (prop.getOrDefault("auto.launch", "false").equals("true"))
				{
					launchElectron(prop);
				}
			} catch (IOException e)
			{
			}
		}
	}

	private void launchElectron(Properties prop) throws IOException
	{

		String path = prop.getProperty("path");
		if (path == null)
		{
			return;
		}

		File pathFile = new File(path);
		if (pathFile.exists())
		{
			addExecutePermissions(pathFile);
			launch(pathFile);
		} else
		{
			System.err.println("The path: '" + pathFile.getAbsolutePath()
					+ "' does not exist");
		}

	}

	private void launch(File pathFile) throws IOException
	{
		switch (PlatformUtil.getOS())
		{
			case MAC:
			{
				ProcessBuilder pb = new ProcessBuilder("open", "-n", ".");
				pb.directory(pathFile);
				try
				{
					pb.start().waitFor();
				} catch (InterruptedException e)
				{
				}
			}
			break;
			case LINUX:
			{
				System.out.println(pathFile.getName());
				ProcessBuilder pb = new ProcessBuilder("./"+pathFile.getName(), "&");
				pb.directory(pathFile.getParentFile());
				try
				{
					pb.start().waitFor();
				} catch (InterruptedException e)
				{
				}
			}
				break;
			case SOLARIS:
			case WINDOWS:
				Desktop.getDesktop().open(pathFile);
				break;
			default:
				break;
		}

	}

	private void addExecutePermissions(File pathFile) throws IOException
	{
		File dir = pathFile;
		// fix permissions
		switch (PlatformUtil.getOS())
		{
			case LINUX:
				dir = pathFile.getParentFile();
			case MAC:
				ProcessBuilder pb = new ProcessBuilder("chmod", "-R", "+x", ".");
				pb.directory(dir);
				try
				{
					pb.start().waitFor();
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case SOLARIS:
				break;
			case WINDOWS:
				break;
			default:
				break;
		}
	}
}
