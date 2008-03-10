/*******************************************************************************
 * Copyright (c) 2006, 2008 EclipseGuru and others.
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     EclipseGuru - initial API and implementation
 *******************************************************************************/
package org.eclipseguru.gwt.core.facet;

/**
 * @author Eclipse Guru (eclipseguru@gmail.com)
 */
public class GwtModuleUninstallActionConfigFactory extends GwtWebUninstallActionConfigFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.googlipse.gwt.facet.UnInstallActionConfigFactory#getFacetId()
	 */
	@Override
	protected String getFacetId() {
		return GwtFacetConstants.FACET_ID_GWT_MODULE;
	}
}
