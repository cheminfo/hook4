package org.cheminfo.hook.util;

import java.util.Vector;

/**
 * QuadTree element provider interface.
 * @author Damiano Banfi
 *
 */
public interface ElementProvider
{
	/**
	 * Returns all elements contained in the provided limits in returnVector.
	 * @param queryX1
	 * @param queryX2
	 * @param queryY1
	 * @param queryY2
	 * @param returnVector
	 */
	public void getElements(double queryX1, double queryX2, double queryY1, double queryY2, Vector<QuadTreeElement> returnVector);
}