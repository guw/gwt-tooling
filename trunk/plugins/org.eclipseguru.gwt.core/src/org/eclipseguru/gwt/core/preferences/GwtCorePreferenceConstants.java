/***************************************************************************************************
 * Copyright (c) 2007,2006 Eclipse Guru and others.
 * All rights reserved. 
 *
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Eclipse Guru - initial API and implementation
 *               Eclipse.org - ideas, concepts and code from existing Eclipse projects
 *               Hugo Garcia - contribution for issue #9
 **************************************************************************************************/
package org.eclipseguru.gwt.core.preferences;

/**
 * Interface with core preference constants.
 */
public interface GwtCorePreferenceConstants {

	/**
	 * Defines the path to the GWT home directory.
	 */
	String PREF_GWT_HOME = "gwtHome";

	/** PREF_HOSTED_DEPLOY_MODE */
	String PREF_HOSTED_DEPLOY_MODE = "hostedDeployMode";

	/** PREF_OUTPUT_LOCATION */
	String PREF_OUTPUT_LOCATION = "outputLocation";

	/** PREF_INCLUDED_MODULES */
	String PREF_INCLUDED_MODULES = "includedModules";

	/** PREF_DEPLOYMENT_PATH */
	String PREF_DEPLOYMENT_PATH = "deploymentPath";

	/**
	 * Defines path to a custom module template.
	 */
	String PREF_CUSTOM_MODULE_TEMPLATE_PATH = "customModuleTemplatePath";
}
