package org.cheminfo.hook.framework;

import java.util.Enumeration;
import java.util.Hashtable;

import org.cheminfo.hook.util.XMLCoDec;

public class HookResourceManager
{
	private Hashtable resources;
	private int resourceHandle;
	
	public HookResourceManager()
	{
		this.resources=new Hashtable();
		this.resourceHandle=0;
	}
	
	public HookResourceManager(String xmlString)
	{
		XMLCoDec codec=new XMLCoDec(xmlString);
		
		int elements=codec.getRootElementsCount();

		for (int elem=0; elem < elements; elem++)
		{
			XMLCoDec tempCodec2=new XMLCoDec(codec.readXMLTag());
			
			tempCodec2.shaveXMLTag();
			try
			{
				Class resourceClass=Class.forName(tempCodec2.getParameterAsString("tagName").trim());
				String resourceName=tempCodec2.getParameterAsString("name");
				Class[] parameterClasses = {String.class};
				java.lang.reflect.Constructor resourceConstructor=resourceClass.getConstructor(parameterClasses);
	
				Object[] parameters = {codec.popXMLTag()};
				this.resources.put( resourceName, ((HookResource)resourceConstructor.newInstance(parameters)) );
			} catch (Exception e) {e.printStackTrace();}
		}
		
	}
	
	public boolean contains(String resourceName)
	{
		return this.resources.containsKey(resourceName);
	}
	
	public HookResource add(String name, HookResource newResource)
	{
		this.resources.put(name, newResource);
		newResource.setName(name);
		
		return newResource;
	}
	
	public HookResource get(String name)
	{
		return (HookResource)this.resources.get(name);
	}
	
	public int getNewHandle()
	{
		return ++this.resourceHandle;
	}
	
	public String getXml()
	{
		String xmlString="<HookResources >\n\r";
		Enumeration keys=this.resources.keys();
		
		while(keys.hasMoreElements())
		{
			xmlString+=((HookResource)this.resources.get(keys.nextElement())).getXml();
		}
		
		xmlString+="</HookResources>";
		
		return xmlString;
	}
}
