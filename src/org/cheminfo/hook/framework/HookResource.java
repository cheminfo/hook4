package org.cheminfo.hook.framework;

public interface HookResource
{
	/**
	 *	All general purpose subclass of HookResource MUST implement a constructor of the form.
	 *	public HookResource(String xmlString)
	 *	{
	 *		...
	 *	}
	 */	
	
	public String getXml();
	
	public String getName();
	
	public void setName(String name);
}
