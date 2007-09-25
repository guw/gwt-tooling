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
package org.eclipseguru.gwt.core;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;

/**
 * This is the base class for all GWT model elements.
 */
public abstract class GwtElement extends PlatformObject implements GwtModelConstants {

	/** NO_ELEMENTS */
	public static final GwtElement[] NO_ELEMENTS = new GwtElement[0];

	/**
	 * Combines two hash codes to make a new one.
	 * 
	 * @param hashCode1
	 * @param hashCode2
	 * @return combined hash code
	 */
	protected static int combineHashCodes(int hashCode1, int hashCode2) {
		return (hashCode1 * 17 + hashCode2) * 31;
	}

	/** parent */
	private final GwtElement parent;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 *            the parent (maybe <code>null</code>)
	 */
	protected GwtElement(GwtElement parent) {
		this.parent = parent;
	}

	/**
	 * Returns the name of the element.
	 * 
	 * @return the name of the element
	 */
	public String getName() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the parent
	 * 
	 * @return the parent
	 */
	public GwtElement getParent() {
		return this.parent;
	}

	/**
	 * Returns the element type.
	 * 
	 * @return the element type
	 */
	public abstract int getType();

	/**
	 * Returns the hash code for this element. By default, the hash code for an
	 * element is a combination of its name and parent's hash code.
	 * <p>
	 * Elements with other requirements must override this method.
	 * </p>
	 */
	@Override
	public int hashCode() {
		if (this.parent == null)
			return super.hashCode();
		return combineHashCodes(getName().hashCode(), this.parent.hashCode());
	}

	/**
	 * Creates a new model exception.
	 * 
	 * @param cause
	 * @return a new model exception
	 */
	protected GwtModelException newGwtModelException(CoreException cause) {
		return new GwtModelException(cause.getStatus());
	}

	/**
	 * Creates a new not present exception.
	 * 
	 * @return new not present exception
	 */
	protected GwtModelException newNotPresentException() {
		return new GwtModelException(GwtCore.newErrorStatus(MessageFormat.format("The element {0} does not exist!", this.getName())));
	}
}
