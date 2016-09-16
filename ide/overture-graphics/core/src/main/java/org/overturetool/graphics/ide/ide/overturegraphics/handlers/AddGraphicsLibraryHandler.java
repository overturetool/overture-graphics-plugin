package org.overturetool.graphics.ide.ide.overturegraphics.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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

						InputStream is = this.getClass().getClassLoader().getResourceAsStream("/jars/interpreter-json-server-jar-with-dependencies.jar");

						try
						{
							jarFile.create(is, true, null);
						} catch (CoreException e)
						{
							OvertureGraphicsPlugin.log("unable to create library", e);

						}
						
						addElectronLink(project);
					}
				}
			}
		}

		return null;
	}

	private void addElectronLink(IVdmProject project)
	{
		Bundle bundle = Platform.getBundle(IOvertureGraphics.PLUGIN_ID);
		   Path path = new Path("electron/overture-graphics-plugin-darwin-x64");
		   URL fileURL = FileLocator.find(bundle, path, null);
		   URL file =null;
		  try
		{
			file = FileLocator.toFileURL(fileURL);
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		   InputStream in = fileURL.openStream();
		   
		 IFolder f= ((IProject) project.getAdapter(IProject.class)).getFolder("electron");
		try
		{
			f.createLink(file.toURI(), IResource.REPLACE, null);
		} catch (CoreException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
