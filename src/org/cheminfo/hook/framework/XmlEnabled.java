package org.cheminfo.hook.framework;

import java.util.Hashtable;

/**
 * Classes implementing this interface instanciate objects that can give an xml representation
 * of themselves through the getXmlTag(...) method.
 * @author Damiano Banfi
 *
 */
public interface XmlEnabled
{
	public String getXmlTag(Hashtable xmlProperties);
}