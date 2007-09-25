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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipseguru.gwt.core.GwtCore;
import org.eclipseguru.gwt.core.GwtModule;
import org.eclipseguru.gwt.core.GwtProject;
import org.eclipseguru.gwt.core.GwtUtil;

/**
 * A utility object for launching GWT.
 */
public class GwtLaunchUtil implements GwtLaunchConstants {

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 * 
	 * @param message
	 *            the status message
	 * @param exception
	 *            lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code
	 *            error code
	 * @throws CoreException
	 *             the "abort" core exception
	 */
	public static void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, GwtCore.PLUGIN_ID, code, message, exception));
	}

	/**
	 * Adds the source folder from the specified project to the classpath list.
	 * 
	 * @param gwtProject
	 * @param classpath
	 * @param processReferencedProjects
	 * @throws JavaModelException
	 */
	public static void addSourceFolderToClasspath(GwtProject gwtProject, List<String> classpath, boolean processReferencedProjects) throws JavaModelException {
		if ((null == gwtProject) || !gwtProject.exists())
			return;

		addSourceFolderToClasspath(gwtProject.getJavaProject(), classpath);

		if (processReferencedProjects) {
			String[] requiredProjectNames = gwtProject.getJavaProject().getRequiredProjectNames();
			if (requiredProjectNames.length > 0) {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				for (String requiredProjectName : requiredProjectNames) {
					IJavaProject project = JavaCore.create(root.getProject(requiredProjectName));
					addSourceFolderToClasspath(project, classpath);
				}
			}
		}
	}

	/**
	 * Adds the source folder from the specified project to the classpath list.
	 * 
	 * @param gwtProject
	 * @param classpath
	 * @throws JavaModelException
	 */
	public static void addSourceFolderToClasspath(IJavaProject project, List<String> classpath) throws JavaModelException {
		if ((null == project) || !project.exists())
			return;

		IPackageFragmentRoot[] packageFragmentRoots = project.getPackageFragmentRoots();
		for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots)
			if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE)
				classpath.add(packageFragmentRoot.getResource().getLocation().toOSString());
	}

	/**
	 * Returns a default URL for the module with the specified id.
	 * <p>
	 * Note, the URL is not guaranteed to work on all systems and for all module
	 * ids.
	 * </p>
	 * 
	 * @param moduleId
	 *            the module id
	 * @return the default URL (maybe <code>null</code> if the specified
	 *         module id was <code>null</code>)
	 */
	public static String computeDefaultUrl(String moduleId) {
		return moduleId + "/" + GwtUtil.getSimpleName(moduleId) + ".html";
	}

	/**
	 * Creates and returns a new GWT browser launch configuration for the
	 * specified module.
	 * 
	 * @param module
	 *            the module to launch
	 * @param url
	 *            the url to launch (optional, maybe <code>null</code>)
	 * @param noserver
	 *            <code>true</code> if the GWT integrated server should not be
	 *            started, <code>false</code> otherwise
	 * @return the creates launch configuration
	 */
	public static ILaunchConfiguration createGwtBrowserLaunchConfiguration(GwtModule module, String url, boolean noserver) {
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getGwtBrowserLaunchConfigurationType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom(module.getSimpleName()));
			wc.setAttribute(ATTR_PROJECT_NAME, module.getProjectName());
			wc.setAttribute(ATTR_MODULE_ID, module.getModuleId());
			wc.setAttribute(ATTR_NOSERVER, noserver);
			if ((null != url) && (url.trim().length() > 0)) {
				wc.setAttribute(ATTR_CUSTOM_URL, true);
				wc.setAttribute(ATTR_URL, url);
			} else
				wc.setAttribute(ATTR_CUSTOM_URL, false);
			config = wc.doSave();
		} catch (CoreException e) {
			GwtCore.logError(MessageFormat.format("Error while creating GWT Browser launch configuration for module {0}", module.getModuleId()), e);
		}
		return config;
	}

	/**
	 * Returns the custom url option specified by the given launch
	 * configuration, defaults to <code>false</code>.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the custom url option specified by the given launch
	 *         configuration, or <code>false</code> if none
	 * @throws CoreException
	 *             if unable to retrieve the attribute
	 */
	public static boolean getCustomUrl(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ATTR_CUSTOM_URL, false);
	}

	/**
	 * Returns the {@link GwtLaunchConstants#TYPE_GWT_BROWSER} launch
	 * configuration type.
	 * 
	 * @return the {@link GwtLaunchConstants#TYPE_GWT_BROWSER} launch
	 *         configuration type
	 */
	public static ILaunchConfigurationType getGwtBrowserLaunchConfigurationType() {
		return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(GwtLaunchConstants.TYPE_GWT_BROWSER);
	}

	/**
	 * Returns the arguments for launching the GWT Shell with the specified
	 * configuration.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the command line arguments
	 * @throws CoreException
	 *             if unable to retrieve the attribute
	 */
	public static String[] getGwtShellArguments(ILaunchConfiguration configuration) throws CoreException {
		List<String> args = new ArrayList<String>();

		// read project settings
		GwtProject project = verifyProject(configuration);
		IPath outputLocation = GwtUtil.getOutputLocation(project);
		IFolder targetFolder = project.getProjectResource().getFolder(outputLocation);

		// log level
		String logLevel = getLogLevel(configuration);
		if ((null != logLevel) && (logLevel.trim().length() > 0)) {
			args.add("-logLevel");
			args.add(logLevel);
		}

		// java script style
		String style = getStyle(configuration);
		if ((null != style) && (style.trim().length() > 0)) {
			args.add("-style");
			args.add(style);
		}

		// port or noserver option
		boolean noserver = getNoServer(configuration);
		if (noserver)
			args.add("-noserver");
		else {
			// port
			args.add("-port");
			args.add(String.valueOf(getPort(configuration)));

			// folder for generated stuff
			args.add("-gen");
			args.add(targetFolder.getLocation().append(".gen").toOSString());

			// output folder
			args.add("-out");
			args.add(targetFolder.getLocation().toOSString());
		}

		// url
		boolean customUrl = getCustomUrl(configuration);
		String url = customUrl ? getUrl(configuration) : null;
		if ((null == url) || (url.trim().length() == 0)) {
			String moduleId = verifyModuleId(configuration);
			url = computeDefaultUrl(moduleId);
		}
		args.add(url);

		return args.toArray(new String[args.size()]);
	}

	/**
	 * Returns the log level specified by the given launch configuration, or
	 * <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the log level specified by the given launch configuration, or
	 *         <code>null</code> if none
	 * @throws CoreException
	 *             if unable to retrieve the attribute
	 */
	public static String getLogLevel(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ATTR_LOG_LEVEL, (String) null);
	}

	/**
	 * Returns the GWT module id specified by the given launch configuration, or
	 * <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param project
	 *            the GWT project to lookup the module id
	 * @throws CoreException
	 *             if unable to retrieve the attribute
	 */
	public static String getModuleId(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ATTR_MODULE_ID, (String) null);
	}

	/**
	 * Returns the noserver option specified by the given launch configuration,
	 * or <code>false</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the noserver option specified by the given launch configuration,
	 *         or <code>false</code> if none
	 * @throws CoreException
	 *             if unable to retrieve the attribute
	 */
	public static boolean getNoServer(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ATTR_NOSERVER, false);
	}

	/**
	 * Returns the port specified by the given launch configuration, defaults to
	 * <code>8888</code>.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the port specified by the given launch configuration, defaults to
	 *         <code>8888</code>
	 * @throws CoreException
	 *             if unable to retrieve the attribute
	 */
	public static int getPort(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ATTR_PORT, 8888);
	}

	/**
	 * Returns the GWT project specified by the given launch configuration, or
	 * <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the GWT project specified by the given launch configuration, or
	 *         <code>null</code> if none
	 * @throws CoreException
	 *             if unable to retrieve the attribute
	 */
	public static GwtProject getProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				GwtProject gwtProject = GwtCore.create(project);
				if ((gwtProject != null) && gwtProject.exists())
					return gwtProject;
			}
		}
		return null;
	}

	/**
	 * Returns the style specified by the given launch configuration, or
	 * <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the style specified by the given launch configuration, or
	 *         <code>null</code> if none
	 * @throws CoreException
	 *             if unable to retrieve the attribute
	 */
	public static String getStyle(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ATTR_STYLE, (String) null);
	}

	/**
	 * Returns the url specified by the given launch configuration, or
	 * <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the port specified by the given launch configuration, or
	 *         <code>null</code> if none
	 * @throws CoreException
	 *             if unable to retrieve the attribute
	 */
	public static String getUrl(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ATTR_URL, (String) null);
	}

	/**
	 * Verifies a module id is specified by the given launch configuration, and
	 * returns the module id.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the module id specified by the given launch configuration
	 * @throws CoreException
	 *             if unable to retrieve the attribute or the attribute is
	 *             unspecified
	 */
	public static String verifyModuleId(ILaunchConfiguration configuration) throws CoreException {
		String moduleId = getModuleId(configuration);
		if ((moduleId == null) || (moduleId.trim().length() == 0))
			abort("Module id not specified", null, GwtLaunchConstants.ERR_UNSPECIFIED_MODULE_ID);
		return moduleId;
	}

	/**
	 * Verifies a project is specified by the given launch configuration, and
	 * returns the project.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the project specified by the given launch configuration
	 * @throws CoreException
	 *             if unable to retrieve the attribute or the attribute is
	 *             unspecified
	 */
	public static GwtProject verifyProject(ILaunchConfiguration configuration) throws CoreException {
		GwtProject project = getProject(configuration);
		if (project == null)
			abort("Project not specified", null, GwtLaunchConstants.ERR_UNSPECIFIED_PROJECT);
		return project;
	}

	/**
	 * Hidden constructor
	 */
	private GwtLaunchUtil() {
		// hidden
	}
}
