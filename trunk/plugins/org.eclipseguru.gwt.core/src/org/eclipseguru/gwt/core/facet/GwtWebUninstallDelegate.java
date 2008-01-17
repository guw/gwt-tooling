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
package org.eclipseguru.gwt.core.facet;

import org.eclipseguru.gwt.core.GwtCore;
import org.eclipseguru.gwt.core.utils.ProgressUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;

public class GwtWebUninstallDelegate implements IDelegate {

	public void execute(final IProject project, final IProjectFacetVersion facetVersion, final Object config, IProgressMonitor monitor) throws CoreException {

		monitor = ProgressUtil.monitor(monitor);

		try {

			monitor.beginTask("Uninstalling facet ...", 1);

			final IProjectDescription description = project.getDescription();
			final String[] prevNatures = description.getNatureIds();
			final String[] newNatures = new String[prevNatures.length - 1];
			int i = 0;
			for (final String aNature : prevNatures)
				if (!aNature.equals(GwtCore.NATURE_ID))
					newNatures[i++] = aNature;

			description.setNatureIds(newNatures);
			project.setDescription(description, IResource.FORCE, null);

		} finally {
			monitor.done();
		}
	}

}
