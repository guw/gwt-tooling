/***************************************************************************************************
 * Copyright (c) 2006 2008 Eclipse Guru and others.
 * All rights reserved. 
 *
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eclipse Guru - initial API and implementation
 *               Eclipse.org - ideas, concepts and code from existing Eclipse projects
 *               Elias Balasis - Patch for http://code.google.com/p/gwt-tooling/issues/detail?id=46
 **************************************************************************************************/
package org.eclipseguru.gwt.ui.properties;

import org.eclipseguru.gwt.core.GwtCore;
import org.eclipseguru.gwt.core.GwtModule;
import org.eclipseguru.gwt.core.GwtProject;
import org.eclipseguru.gwt.core.GwtUtil;
import org.eclipseguru.gwt.core.j2ee.ConfigureWebProjectJob;
import org.eclipseguru.gwt.core.preferences.GwtCorePreferenceConstants;
import org.eclipseguru.gwt.ui.dialogs.ModuleSelectionDialog;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.jdt.internal.ui.wizards.TypedViewerFilter;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.FolderSelectionDialog;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.osgi.service.prefs.BackingStoreException;

import java.util.ArrayList;
import java.util.List;

/**
 * Properties page for projects with GWT nature.
 */
public class ProjectProperties extends PropertyPage implements IWorkbenchPropertyPage, IStatusChangeListener {

	private final class DeploymentPathDialogFieldAdapter implements IDialogFieldListener {
		public void dialogFieldChanged(DialogField field) {
			deploymentPathDialogFieldChanged(field);
		}
	}

	private final class HostedModeDialogFieldAdapter implements IDialogFieldListener {
		public void dialogFieldChanged(DialogField field) {
			hostedModeDialogFieldChanged(field);
		}
	}

	private final class ModulesListDialogFieldAdapter implements IListAdapter {
		public void customButtonPressed(ListDialogField field, int index) {
			if (field != modulesListDialogField)
				return;

			switch (index) {
				case IDX_BUTTON_ML_ADD:
					handleModuleListAddButtonPressed();
					break;
				case IDX_BUTTON_ML_REMOVE:
					handleModuleListRemoveButtonPressed();
					break;
			}

			updateButtonEnabledState();
		}

		public void doubleClicked(ListDialogField field) {
			// nothing
		}

		public void selectionChanged(ListDialogField field) {
			updateButtonEnabledState();
		}
	}

	private class OutputLocationDialogFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {

		// -------- IStringButtonAdapter --------
		public void changeControlPressed(DialogField field) {
			outputChangeControlPressed(field);
		}

		// ---------- IDialogFieldListener --------
		public void dialogFieldChanged(DialogField field) {
			outputDialogFieldChanged(field);
		}
	}

	private class VMArgsDialogFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {

		// -------- IStringButtonAdapter --------
		public void changeControlPressed(DialogField field) {
			vmArgsChangeControlPressed(field);
		}

		// ---------- IDialogFieldListener --------
		public void dialogFieldChanged(DialogField field) {
			vmArgsDialogFieldChanged(field);
		}
	}

	private static final int IDX_BUTTON_ML_ADD = 0;

	private static final int IDX_BUTTON_ML_REMOVE = 1;

	private static final String[] BUTTONS_ML = new String[] { "Add...", "Remove" };

	private SelectionButtonDialogField hostedModeDialogField;

	private ListDialogField modulesListDialogField;

	private StringButtonDialogField outputLocationDialogField;

	private StringDialogField deploymentPathDialogField;

	private StringButtonDialogField vmArgsDialogField;

	private GwtProject currentProject;

	private boolean isHosted;

	private IPath outputLocationPath;

	IPath deploymentPath;

	String vmArgs = "";

	private final StatusInfo outputLocationStatus = new StatusInfo();

	private final StatusInfo deploymentPathStatus = new StatusInfo();

	private final StatusInfo vmArgsStatus = new StatusInfo();

	private boolean isRebuildNecessary;

	private IContainer chooseContainer() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		Class[] acceptedClasses = new Class[] { IProject.class, IFolder.class };
		ISelectionStatusValidator validator = new TypedElementSelectionValidator(acceptedClasses, false);
		IProject[] allProjects = workspaceRoot.getProjects();
		List<IProject> rejectedElements = new ArrayList<IProject>(allProjects.length);
		GwtProject currProject = getProject();
		for (int i = 0; i < allProjects.length; i++)
			if (!allProjects[i].equals(currProject))
				rejectedElements.add(allProjects[i]);
		ViewerFilter filter = new TypedViewerFilter(acceptedClasses, rejectedElements.toArray());

		ILabelProvider lp = new WorkbenchLabelProvider();
		ITreeContentProvider cp = new WorkbenchContentProvider();

		IResource initSelection = null;
		if (outputLocationPath != null)
			initSelection = currProject.getProjectResource().findMember(outputLocationPath);

		FolderSelectionDialog dialog = new FolderSelectionDialog(getShell(), lp, cp);
		dialog.setTitle(NewWizardMessages.BuildPathsBlock_ChooseOutputFolderDialog_title);
		dialog.setValidator(validator);
		dialog.setMessage(NewWizardMessages.BuildPathsBlock_ChooseOutputFolderDialog_description);
		dialog.addFilter(filter);
		dialog.setInput(workspaceRoot);
		dialog.setInitialSelection(initSelection);
		dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));

		if (dialog.open() == Window.OK)
			return (IContainer) dialog.getFirstResult();
		return null;
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);
		result.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// the layout
		GridLayout layout = new GridLayout();
		result.setLayout(layout);

		// setup dialog fields
		createDialogFields();
		initDialogFields();
		updateButtonEnabledState();

		// create copntrols based on assiged facets
		Group deployment = new Group(result, SWT.NONE);
		deployment.setText("Deployment");
		deployment.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		IProject project = getProject().getProjectResource();
		if (GwtProject.hasGwtModuleFacet(project)) {
			LayoutUtil.doDefaultLayout(deployment, new DialogField[] { hostedModeDialogField, outputLocationDialogField }, false, 5, 5);
			LayoutUtil.setHorizontalGrabbing(outputLocationDialogField.getTextControl(deployment));
		} else if (GwtProject.hasGwtWebFacet(project)) {

			Group modules = new Group(result, SWT.NONE);
			modules.setText("Modules");
			modules.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

			LayoutUtil.doDefaultLayout(deployment, new DialogField[] { hostedModeDialogField, deploymentPathDialogField }, false, 5, 5);
			LayoutUtil.setHorizontalGrabbing(deploymentPathDialogField.getTextControl(deployment));
			LayoutUtil.setHorizontalGrabbing(vmArgsDialogField.getTextControl(deployment));

			LayoutUtil.doDefaultLayout(modules, new DialogField[] { modulesListDialogField, outputLocationDialogField, vmArgsDialogField }, true, 5, 5);
			LayoutUtil.setHorizontalGrabbing(modulesListDialogField.getListControl(modules));
			((GridData) modulesListDialogField.getListControl(modules).getLayoutData()).grabExcessVerticalSpace = true;
			modules.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			LayoutUtil.setHorizontalGrabbing(outputLocationDialogField.getTextControl(modules));

		} else {
			LayoutUtil.doDefaultLayout(deployment, new DialogField[] { outputLocationDialogField, vmArgsDialogField }, false, 5, 5);
			LayoutUtil.setHorizontalGrabbing(outputLocationDialogField.getTextControl(deployment));
		}

		Dialog.applyDialogFont(result);
		return result;
	}

	/**
	 * Creates the dialog fields
	 */
	private void createDialogFields() {
		hostedModeDialogField = new SelectionButtonDialogField(SWT.CHECK);
		hostedModeDialogField.setLabelText("Deploy using GWT's hosted mode.");
		hostedModeDialogField.setDialogFieldListener(new HostedModeDialogFieldAdapter());

		ModulesListDialogFieldAdapter moduleListAdapter = new ModulesListDialogFieldAdapter();
		modulesListDialogField = new ListDialogField(moduleListAdapter, BUTTONS_ML, new WorkbenchLabelProvider());
		modulesListDialogField.setLabelText("Additional GWT modules from projects included via J2EE Module Dependencies:");

		OutputLocationDialogFieldAdapter adapter = new OutputLocationDialogFieldAdapter();
		outputLocationDialogField = new StringButtonDialogField(adapter);
		outputLocationDialogField.setButtonLabel("Browse...");
		outputLocationDialogField.setDialogFieldListener(adapter);
		outputLocationDialogField.setLabelText("Output folder:");

		deploymentPathDialogField = new StringDialogField();
		deploymentPathDialogField.setDialogFieldListener(new DeploymentPathDialogFieldAdapter());
		deploymentPathDialogField.setLabelText("Deployment path:");

		vmArgsDialogField = new StringButtonDialogField(new VMArgsDialogFieldAdapter());
		vmArgsDialogField.setLabelText("Compiler VM Args:");
		vmArgsDialogField.setButtonLabel("Variables...");
		vmArgsDialogField.setDialogFieldListener(adapter);
	}

	private void deploymentPathDialogFieldChanged(DialogField field) {
		if (field == deploymentPathDialogField) {
			updateDeploymentLocationStatus();
			isRebuildNecessary = true;
		}
		doStatusLineUpdate();
	}

	/**
	 * Configures the flexible web project structure.
	 */
	private void doFlexProjectSetup() {
		ConfigureWebProjectJob flexProjectJob = new ConfigureWebProjectJob(getProject(), outputLocationPath, deploymentPath, isHosted);
		flexProjectJob.schedule();
	}

	/**
	 * Schedules a rebuild.s
	 */
	private void doFullBuild() {
		Job buildJob = new Job("Building Projects...") {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
			 */
			@Override
			public boolean belongsTo(Object family) {
				return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
			}

			//$NON-NLS-1$
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					IProject[] projects = null;
					if (getProject() == null)
						projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
					else
						projects = new IProject[] { getProject().getProjectResource() };
					monitor.beginTask("", projects.length * 2); //$NON-NLS-1$
					for (IProject projectToBuild : projects) {
						if (!projectToBuild.isOpen())
							continue;
						if (GwtProject.hasGwtNature(projectToBuild))
							projectToBuild.build(IncrementalProjectBuilder.FULL_BUILD, GwtCore.BUILDER_ID, null, new SubProgressMonitor(monitor, 1));
						else
							monitor.worked(1);
					}
				} catch (CoreException e) {
					return e.getStatus();
				} catch (OperationCanceledException e) {
					return Status.CANCEL_STATUS;
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		buildJob.setUser(true);
		buildJob.schedule();
		isRebuildNecessary = false;
	}

	private void doStatusLineUpdate() {
		if (Display.getCurrent() != null) {
			IStatus res = findMostSevereStatus();
			statusChanged(res);
		}
	}

	private IStatus findMostSevereStatus() {
		return StatusUtil.getMostSevere(new IStatus[] { outputLocationStatus, deploymentPathStatus, vmArgsStatus });
	}

	/**
	 * Returns the current compiler VM arguments which will be added to the GWT
	 * compiler Java runner as additional VM arguments.
	 * 
	 * @return the VM arguments for the GWT compiler
	 */
	public String getCompilerVmArgs() {
		return vmArgsDialogField.getText();
	}

	/**
	 * @return Returns the current deployment path. Note that the path returned
	 *         must not be valid.
	 */
	public IPath getDeploymentPath() {
		return new Path(deploymentPathDialogField.getText()).makeAbsolute();
	}

	/**
	 * @return Returns the current output location. Note that the path returned
	 *         must not be valid.
	 */
	public IPath getOutputLocation() {
		return new Path(outputLocationDialogField.getText()).makeAbsolute();
	}

	/**
	 * Returns the project.
	 * 
	 * @return the project
	 */
	public GwtProject getProject() {
		if (null == currentProject) {
			IAdaptable adaptable = getElement();
			if (adaptable instanceof IProject)
				currentProject = GwtCore.create((IProject) adaptable);
			else
				currentProject = GwtCore.create((IProject) adaptable.getAdapter(IProject.class));

			if (null == currentProject)
				throw new IllegalStateException("Project not found!");
		}

		return currentProject;
	}

	private void handleModuleListAddButtonPressed() {
		GwtProject project = getProject();
		try {
			List<GwtProject> projects = new ArrayList<GwtProject>(5);
			IVirtualComponent component = ComponentCore.createComponent(project.getProjectResource());
			IVirtualReference[] references = component.getReferences();
			for (IVirtualReference reference : references) {
				IProject referencedProject = reference.getReferencedComponent().getProject();
				if (GwtProject.hasGwtNature(referencedProject))
					projects.add(GwtCore.create(referencedProject));
			}

			ModuleSelectionDialog dialog = new ModuleSelectionDialog(getShell(), projects.toArray(new GwtProject[projects.size()]));
			if (dialog.open() == Window.OK) {
				GwtModule module = dialog.getSelectedModule();
				modulesListDialogField.addElement(module);
				isRebuildNecessary = true;
			}

		} catch (CoreException e) {
			ErrorDialog.openError(getShell(), "Error", "We are sorry, an internal error occured.", e.getStatus());
		}
	}

	private void handleModuleListRemoveButtonPressed() {
		modulesListDialogField.removeElements(modulesListDialogField.getSelectedElements());
		isRebuildNecessary = true;
	}

	private void hostedModeDialogFieldChanged(DialogField field) {
		if (field == hostedModeDialogField) {
			isHosted = hostedModeDialogField.isSelected();
			isRebuildNecessary = true;
		}
	}

	/**
	 * Initializes dialog fields with data from preferences
	 */
	private void initDialogFields() {
		GwtProject project = getProject();

		// hosted mode
		isHosted = GwtUtil.isHostedDeploymentMode(project);
		hostedModeDialogField.setSelection(isHosted);

		// output location
		outputLocationPath = GwtUtil.getOutputLocation(project);
		outputLocationDialogField.setText(outputLocationPath.makeRelative().toString());
		outputLocationDialogField.enableButton(getProject().exists());

		// included modules
		GwtModule[] includedModules = project.getIncludedModules();
		for (GwtModule module : includedModules)
			modulesListDialogField.addElement(module);

		// deployment path
		deploymentPath = GwtUtil.getDeploymentPath(project);
		deploymentPathDialogField.setText(deploymentPath.makeAbsolute().toString());

		// VM args
		vmArgs = GwtUtil.getCompilerVmArgs(project);
		vmArgsDialogField.setText(vmArgs);

		// no rebuild after fresh initialization
		isRebuildNecessary = false;
	}

	private void outputChangeControlPressed(DialogField field) {
		if (field == outputLocationDialogField) {
			IContainer container = chooseContainer();
			if (container != null)
				outputLocationDialogField.setText(container.getProjectRelativePath().makeRelative().toString());
		}
	}

	private void outputDialogFieldChanged(DialogField field) {
		if (field == outputLocationDialogField) {
			updateOutputLocationStatus();
			isRebuildNecessary = true;
		}
		doStatusLineUpdate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		GwtProject project = getProject();

		boolean build = false;
		if (isRebuildNecessary) {
			MessageDialog dialog = new MessageDialog(getShell(), "Build Needed", null, "The deployment settings have changed.  The project needs to be rebuilt for the changes to take effect.  Do you want to rebuild now?", MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL }, 2);
			switch (dialog.open()) {
				case 2:
					return false;
				case Window.OK:
					build = true;
					break;
			}
		}

		IEclipsePreferences projectPreferences = project.getProjectPreferences();
		if (null == projectPreferences)
			return false;

		// hosted mode
		projectPreferences.putBoolean(GwtCorePreferenceConstants.PREF_HOSTED_DEPLOY_MODE, isHosted);

		// output location
		if (project.getProjectResource().getFullPath().isPrefixOf(outputLocationPath))
			outputLocationPath = outputLocationPath.removeFirstSegments(1);
		projectPreferences.put(GwtCorePreferenceConstants.PREF_OUTPUT_LOCATION, outputLocationPath.makeRelative().toString());

		// modules list
		project.setIncludedModules(modulesListDialogField.getElements());

		// deployment path
		projectPreferences.put(GwtCorePreferenceConstants.PREF_DEPLOYMENT_PATH, deploymentPath.makeAbsolute().toString());

		// VM args
		projectPreferences.put(GwtCorePreferenceConstants.PREF_COMPILER_VM_ARGS, getCompilerVmArgs());

		// Dump changes
		try {
			projectPreferences.flush();
		} catch (BackingStoreException e) {
			// problem with pref store - quietly ignore
		}

		// setup the flexible project structure
		if (GwtProject.hasGwtWebFacet(project.getProjectResource()))
			doFlexProjectSetup();

		// build
		if (build)
			doFullBuild();

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener#statusChanged(org.eclipse.core.runtime.IStatus)
	 */
	public void statusChanged(IStatus status) {
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}

	private void updateButtonEnabledState() {
		boolean itemSelected = modulesListDialogField.getSelectedElements().size() > 0;
		modulesListDialogField.enableButton(IDX_BUTTON_ML_REMOVE, itemSelected);
	}

	private void updateDeploymentLocationStatus() {
		deploymentPath = null;

		String text = deploymentPathDialogField.getText();
		if (text.startsWith(" ") || text.endsWith(" ")) {
			deploymentPathStatus.setError("The deployment path cannot start or end with a white space.");
			return;
		}

		deploymentPath = getDeploymentPath();
		deploymentPathStatus.setOK();
	}

	private void updateOutputLocationStatus() {
		outputLocationPath = null;

		String text = outputLocationDialogField.getText();
		if ("".equals(text)) { //$NON-NLS-1$
			outputLocationStatus.setError(NewWizardMessages.BuildPathsBlock_error_EnterBuildPath);
			return;
		}

		outputLocationPath = getOutputLocation();
		IResource res = getProject().getProjectResource().findMember(outputLocationPath);
		if (res != null) {
			// if exists, must be a folder or project
			if (res.getType() == IResource.FILE) {
				outputLocationStatus.setError(NLS.bind(NewWizardMessages.BuildPathsBlock_error_InvalidBuildPath, outputLocationPath.toString()));
				return;
			}

			// must not be on classpath
			if ((res.getType() == IResource.FOLDER) && getProject().getJavaProject().isOnClasspath(res)) {
				outputLocationStatus.setError("Build output folder must be outside build path.");
				return;
			}
		}

		outputLocationStatus.setOK();

		String pathStr = outputLocationDialogField.getText();
		Path outputPath = (new Path(pathStr));
		pathStr = outputPath.lastSegment();
		if (null != pathStr) {
			if (pathStr.equals(".settings") && (outputPath.segmentCount() == 2))
				outputLocationStatus.setWarning(NewWizardMessages.OutputLocation_SettingsAsLocation);

			if ((pathStr.charAt(0) == '.') && (pathStr.length() > 1))
				outputLocationStatus.setWarning(Messages.format(NewWizardMessages.OutputLocation_DotAsLocation, pathStr));
		}
	}

	private void updateVmArgsStatus() {
		vmArgs = "";

		// TODO: validate VM args (if required)
		// ...

		vmArgs = getCompilerVmArgs();
		vmArgsStatus.setOK();
	}

	private void vmArgsChangeControlPressed(DialogField field) {
		if (field == vmArgsDialogField) {
			StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
			dialog.open();
			String variableExpression = dialog.getVariableExpression();
			if (variableExpression == null)
				return;
			int startSel = vmArgsDialogField.getTextControl(null).getSelection().x;
			int lenSel = vmArgsDialogField.getTextControl(null).getSelectionCount();
			String currentText = getCompilerVmArgs();
			String newText = "";
			if (startSel > 0)
				newText += currentText.substring(0, startSel);
			newText += variableExpression + currentText.substring(startSel + lenSel);
			vmArgsDialogField.setText(newText);
		}
	}

	private void vmArgsDialogFieldChanged(DialogField field) {
		if (field == vmArgsDialogField) {
			updateVmArgsStatus();
			isRebuildNecessary = true;
		}
		doStatusLineUpdate();
	}

}