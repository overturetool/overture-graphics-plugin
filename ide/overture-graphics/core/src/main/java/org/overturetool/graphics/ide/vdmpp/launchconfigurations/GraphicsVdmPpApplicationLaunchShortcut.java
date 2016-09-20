package org.overturetool.graphics.ide.vdmpp.launchconfigurations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.overture.ast.node.INode;
import org.overture.ide.core.resources.IVdmProject;
import org.overture.ide.debug.core.IDebugConstants;
import org.overture.ide.debug.core.VdmDebugPlugin;
import org.overture.ide.debug.ui.launchconfigurations.LauncherMessages;
import org.overture.ide.vdmpp.debug.ui.launchconfigurations.VdmPpApplicationLaunchShortcut;
import org.overturetool.graphics.ide.ide.overturegraphics.handlers.AddGraphicsLibraryHandler;
import org.overturetool.graphics.interpreter.JsonServerRemoteControl;

public class GraphicsVdmPpApplicationLaunchShortcut extends
		VdmPpApplicationLaunchShortcut
{

	protected INode[] filterTypes(Object[] elements, IRunnableContext context)
	{
		INode[] nodes = super.filterTypes(elements, context);
		if (nodes.length > 0)
		{
			return new INode[] { nodes[0] };
		}
		return nodes;
	}

	protected INode chooseType(INode[] types, String title)
	{
		if (types.length > 0)
		{
			return types[0];
		}

		return null;
	}

	protected ILaunchConfiguration findLaunchConfiguration(String projectName,
			ILaunchConfigurationType configType)
	{
		List<ILaunchConfiguration> candidateConfigs = Collections.emptyList();
		try
		{
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);
			candidateConfigs = new ArrayList<ILaunchConfiguration>(configs.length);
			for (int i = 0; i < configs.length; i++)
			{
				ILaunchConfiguration config = configs[i];

				String pName = config.getAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_PROJECT, "");

				if (pName.equalsIgnoreCase(projectName)
						&& config.getName().contains("-graphics"))
				{
					candidateConfigs.add(config);
				}
			}

		} catch (CoreException e)
		{
			// JDIDebugUIPlugin.log(e);
		}
		int candidateCount = candidateConfigs.size();
		if (candidateCount == 1)
		{
			return (ILaunchConfiguration) candidateConfigs.get(0);
		} else if (candidateCount > 1)
		{
			return chooseConfiguration(candidateConfigs);
			// return candidateConfigs.get(0);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#createConfiguration(org.eclipse.jdt
	 * .core.IType)
	 */
	protected ILaunchConfiguration createConfiguration(INode type,
			String projectName)
	{
		ILaunchConfiguration config = null;
		ILaunchConfigurationWorkingCopy wc = null;
		try
		{

			ILaunchConfigurationType configType = getConfigurationType();
			wc = configType.newInstance(null, getLaunchManager().generateLaunchConfigurationName(projectName
					+ "-graphics"));
			wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_PROJECT, projectName);
			wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_CREATE_COVERAGE, true);

			wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_REMOTE_CONTROL, JsonServerRemoteControl.class.getName());

			wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_ENABLE_LOGGING, false);

			config = wc.doSave();

			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project != null)
			{
				IVdmProject p = (IVdmProject) project.getAdapter(IVdmProject.class);
				if (p != null)
				{
					AddGraphicsLibraryHandler.addGraphicsLibrary(p);
				}
			}
		} catch (CoreException exception)
		{

			MessageDialog.openError(VdmDebugPlugin.getActiveWorkbenchShell(), LauncherMessages.VdmLaunchShortcut_3, exception.getStatus().getMessage());
		}
		return config;
	}
}
