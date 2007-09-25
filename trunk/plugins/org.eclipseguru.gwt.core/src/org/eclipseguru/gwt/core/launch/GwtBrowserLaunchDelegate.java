/***************************************************************************************************
 * Copyright (c) 2006 Eclipse Guru and others.
 * All rights reserved. 
 *
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eclipse Guru - initial API and implementation
 *               Eclipse.org - ideas, concepts and code from existing Eclipse projects
 **************************************************************************************************/
package org.eclipseguru.gwt.core.launch;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipseguru.gwt.core.GwtCore;
import org.eclipseguru.gwt.core.GwtProject;
import org.eclipseguru.gwt.core.runtimes.GwtRuntime;
import org.eclipseguru.gwt.core.utils.ProgressUtil;

/**
 * A launch delegate for launchin the GWT browser.
 */
public class GwtBrowserLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate implements ILaunchConfigurationDelegate, GwtLaunchConstants {

	/** CLASS_NAME_GWTSHELL */
	private static final String CLASS_NAME_GWTSHELL = "com.google.gwt.dev.GWTShell";

	/**
	 * Computes the classpath for the specified project and configuration.
	 * 
	 * @param project
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private String[] computeClasspath(GwtProject project, ILaunchConfiguration configuration) throws CoreException {
		List<String> classpath = new ArrayList<String>();

		// add the source folder to the classpath
		GwtLaunchUtil.addSourceFolderToClasspath(project, classpath, true);

		// GWT runtime entries
		GwtRuntime runtime = GwtCore.getRuntime(project);
		String[] entries = runtime.getGwtRuntimeClasspath();
		classpath.addAll(Arrays.asList(entries));

		// launch config classpath
		classpath.addAll(Arrays.asList(getClasspath(configuration)));

		return classpath.toArray(new String[classpath.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String, org.eclipse.debug.core.ILaunch,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		monitor = ProgressUtil.monitor(monitor);
		try {

			monitor.beginTask(MessageFormat.format("{0}...", configuration.getName()), 3); //$NON-NLS-1$

			// check for cancellation
			if (monitor.isCanceled())
				return;

			monitor.subTask("Verifying launch attributes...");

			// verify project
			GwtProject project = GwtLaunchUtil.verifyProject(configuration);
			IVMRunner runner = getVMRunner(configuration, mode);

			// working directory
			File workingDir = verifyWorkingDirectory(configuration);
			String workingDirName = null;
			if (workingDir != null)
				workingDirName = workingDir.getAbsolutePath();

			// Environment variables
			String[] envp = getEnvironment(configuration);

			// Program & VM arguments
			String[] pgmArgs = GwtLaunchUtil.getGwtShellArguments(configuration);
			String vmArgs = getVMArguments(configuration);
			ExecutionArguments execArgs = new ExecutionArguments(vmArgs, "");

			// VM-specific attributes
			Map vmAttributesMap = getVMSpecificAttributesMap(configuration);

			// Classpath
			String[] classpath = computeClasspath(project, configuration);

			// Create VM config
			VMRunnerConfiguration runConfig = new VMRunnerConfiguration(CLASS_NAME_GWTSHELL, classpath);
			runConfig.setProgramArguments(pgmArgs);
			runConfig.setEnvironment(envp);
			runConfig.setVMArguments(execArgs.getVMArgumentsArray());
			runConfig.setWorkingDirectory(workingDirName);
			runConfig.setVMSpecificAttributesMap(vmAttributesMap);

			// Bootpath
			runConfig.setBootClassPath(getBootpath(configuration));

			// check for cancellation
			if (monitor.isCanceled())
				return;

			// stop in main
			prepareStopInMain(configuration);

			// done the verification phase
			monitor.worked(1);

			monitor.subTask("Creating source locator...");
			// set the default source locator if required
			setDefaultSourceLocator(launch, configuration);
			monitor.worked(1);

			// Launch the configuration - 1 unit of work
			runner.run(runConfig, launch, monitor);

			// check for cancellation
			if (monitor.isCanceled())
				return;

		} finally {
			monitor.done();
		}

	}

}
