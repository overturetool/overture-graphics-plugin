package org.overturetool.graphics.ide.ide.overturegraphics.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.Bundle;
import org.overture.ide.core.resources.IVdmProject;
import org.overturetool.graphics.ide.ide.overturegraphics.IOvertureGraphics;
import org.overturetool.graphics.ide.ide.overturegraphics.OvertureGraphicsPlugin;
import org.overturetool.graphics.interpreter.PlatformUtil;

public class AddGraphicsLibraryHandler extends
		org.eclipse.core.commands.AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		ISelection selections = HandlerUtil.getCurrentSelection(event);

		if (selections instanceof IStructuredSelection)
		{
			IStructuredSelection ss = (IStructuredSelection) selections;

			Object o = ss.getFirstElement();
			if (o instanceof IAdaptable)
			{
				IAdaptable a = (IAdaptable) o;
				IVdmProject project = (IVdmProject) a.getAdapter(IVdmProject.class);
				addGraphicsLibrary(project);
			}
		}

		return null;
	}

	public static void addGraphicsLibrary(IVdmProject project)
	{
		if (project != null)
		{
			if (project.getModelBuildPath().getLibrary() instanceof IFolder)
			{
				IFolder folder = (IFolder) project.getModelBuildPath().getLibrary();
				if (!folder.exists())
				{
					try
					{
						folder.create(true, false, null);
					} catch (CoreException e2)
					{
					}
				}
				IFile jarFile = folder.getFile("overture-graphics.jar");
				if (jarFile.exists())
				{
					try
					{
						jarFile.delete(true, null);
					} catch (CoreException e1)
					{
					}
				}

				InputStream is = AddGraphicsLibraryHandler.class.getClassLoader().getResourceAsStream("/jars/interpreter-json-server-jar-with-dependencies.jar");

				try
				{
					jarFile.create(is, true, null);
				} catch (CoreException e)
				{
					OvertureGraphicsPlugin.log("unable to create library", e);

				}

				try
				{
					addElectronLink(project);
				} catch (CoreException e)
				{
					OvertureGraphicsPlugin.log(e);
				} catch (URISyntaxException e)
				{
					OvertureGraphicsPlugin.log(e);
				}
			}
		}
	}

	private static void addElectronLink(IVdmProject project) throws CoreException,
			URISyntaxException
	{
		Bundle bundle = Platform.getBundle(IOvertureGraphics.PLUGIN_ID);
		Path path = null;

		switch (PlatformUtil.getOS())
		{
			case LINUX:
				path = new Path("electron/overture-graphics-plugin-linux-x64.zip/overture-graphics-plugin");

				break;
			case MAC:
				path = new Path("electron/overture-graphics-plugin-darwin-x64/overture-graphics-plugin.app");
				break;
			case SOLARIS:
				break;
			case WINDOWS:
				if (System.getProperty("os.arch").endsWith("64"))
				{
					path = new Path("electron/overture-graphics-plugin-win32-x64/overture-graphics-plugin.exe");
				} else
				{
					path = new Path("electron/overture-graphics-plugin-win32-ia32/overture-graphics-plugin.exe");
				}
				break;
			default:
				break;
		}

		if (path == null)
		{
			return;
		}

		URL fileURL = FileLocator.find(bundle, path, null);
		URL file = null;
		try
		{
			file = FileLocator.toFileURL(fileURL);
		} catch (IOException e1)
		{
			OvertureGraphicsPlugin.log(e1);
		}
		IFolder folder = (IFolder) project.getModelBuildPath().getLibrary();

		IFile prop = folder.getFile("overture.graphics.properties");
		if (prop.exists())
		{
			prop.delete(true, null);
		}

		try
		{
			String properties = "auto.launch=true\npath=" + file.getPath()
					+ "\n";
			InputStream stream = new ByteArrayInputStream(properties.getBytes(StandardCharsets.UTF_8));
			prop.create(stream, true, null);
		} catch (CoreException e)
		{
			OvertureGraphicsPlugin.log(e);
		}
	}

}
