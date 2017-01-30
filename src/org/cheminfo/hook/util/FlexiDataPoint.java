package org.cheminfo.hook.util;

import java.io.Serializable;

public class FlexiDataPoint implements Cloneable, Serializable
{
	
	FlexiDataDescription dataDescription;
	Object[] data;
	
	public FlexiDataPoint(FlexiDataDescription description)
	{
		this.dataDescription=description;
		this.data=new Object[description.getNbFields()];
	}
	
	
	public void setDescription(FlexiDataDescription newDescription)
	{
		this.dataDescription=newDescription;
	}
	
	public FlexiDataDescription getDescription()
	{
		return this.dataDescription;
	}
	
	public void setData(Object[] newData)
	{
		if (newData.length == this.data.length)
			this.data=newData;
	}
	
	public Object[] getData()
	{
		return this.data;
	}
	
	public void setFieldData(int fieldIndex, Object newData)
	{
		this.data[fieldIndex]=newData;
	}
	
	public void setFieldData(String fieldName, Object newData)
	{
		this.data[this.dataDescription.getIndexForFieldName(fieldName)]=newData;
	}

	
	public void setDbFieldData(String dbFieldName, Object newData)
	{
		this.data[this.dataDescription.getIndexForDbFieldName(dbFieldName)]=newData;
	}

	
	public Object getFieldData(int fieldIndex)
	{
		return this.data[fieldIndex];
	}
	
	public Object getFieldData(String fieldName)
	{
		return this.data[this.dataDescription.getIndexForFieldName(fieldName)];
	}

	public Object getDbFieldData(String dbFieldName)
	{
		if (this.dataDescription.getIndexForDbFieldName(dbFieldName) == -1)
			System.out.println("Unknown Field: "+dbFieldName);
		
		return this.data[this.dataDescription.getIndexForDbFieldName(dbFieldName)];
	}

	public Object clone()
	{
		FlexiDataPoint newPoint;
		
		newPoint= new FlexiDataPoint(this.dataDescription);
			
		for (int i=0; i < this.data.length; i++)
			newPoint.data[i]=this.data[i];
		
		return newPoint;
	}
	
	public String toString()
	{
		String outString="%data";
		
		for (int i=0; i < this.data.length; i++)
		{
			outString+="\t";
			outString+=this.data[i];
		}
		outString+="\n";
		return outString;
	}
}