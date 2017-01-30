package org.cheminfo.hook.util;

import java.io.Serializable;

public class  FlexiDataDescription implements Serializable
{
	private final static boolean	DEBUG			=	false;
	
	public final static int TYPE_GENERIC			=	99;
	public final static int TYPE_BARCODE			=	100;
	public final static int TYPE_COLUMN				=	101;
	public final static int TYPE_ROW				=	102;
	public final static int TYPE_MEASURE			=	103;
	public final static int TYPE_CONTAINER_TYPE		=	104;
	public final static int TYPE_RECORD_ID			=	105;
	public final static int TYPE_ID					=	106;
	public final static int TYPE_VALUE				=	107;
	

	public final static int CONTAINER_POSITIVE		=	200;
	public final static int CONTAINER_NEGATIVE		=	201;
	public final static int CONTAINER_BLANK			=	202;
	public final static int CONTAINER_SAMPLE		=	203;

	private String[] fieldNames;
	private String[] dbFieldNames;		// name in the database
	private String[] mimetypes;
	private int[] types;
	private boolean[] showFields;
	private Class[]	dataTypes;
	
	public FlexiDataDescription(int nbFields)
	{
		this.fieldNames=new String[nbFields];
		this.dbFieldNames=new String[nbFields];
		this.mimetypes=new String[nbFields];
		this.showFields=new boolean[nbFields];
		this.types=new int[nbFields];
		this.dataTypes=new Class[nbFields];
	}
	
	/**
	 * Creates a FlexiDataDescription from an array of strings defining each column. Column definitions are as follow:
	 * COLUMN_NAME	DATA_TYPE
	 * these map onto
	 * dbFieldNames	datatypes
	 * All other parameters are set to a default
	 * @param definition
	 */
	public FlexiDataDescription(String[] definition)
	{
		this(definition.length);
		
		String[] params;
		for (int col=0; col < definition.length; col++)
		{
			params=definition[col].split("\t");
			this.dbFieldNames[col]=params[0];
			try {
				this.dataTypes[col]=Class.forName("java.lang."+params[1]);
			} catch (Exception e) {e.printStackTrace();}
			this.fieldNames[col]=this.dbFieldNames[col];
			this.mimetypes[col]="text/text";
			this.showFields[col]=true;
			this.types[col]=TYPE_GENERIC;
		}
	}
	
	public int getNbFields()
	{
		return this.fieldNames.length;
	}
	
	
	public void setFieldNames(String[] newFieldNames)
	{
		if (newFieldNames.length == this.fieldNames.length)
			this.fieldNames=newFieldNames;
	}
	
	public String[] getFieldNames()
	{
		return this.fieldNames;
	}
	
	public void setFieldName(int fieldIndex, String fieldName)
	{
		this.fieldNames[fieldIndex]=fieldName;
	}
		
	public String getFieldName(int fieldIndex)
	{
		return this.fieldNames[fieldIndex];
	}
	
	public void setDataTypes(Class[] dataTypes)
	{
		this.dataTypes=dataTypes;
	}
	
	public void setDataType(int index, Class dataType)
	{
		this.dataTypes[index]=dataType;
	}
	
	public Class getDataType(int fieldIndex)
	{
		return this.dataTypes[fieldIndex];
	}
	
	public Class[] getDataTypes()
	{
		return this.dataTypes;
	}
	
	public int getIndexForFieldName(String name)
	{
		for (int index=0; index < this.fieldNames.length; index++)
		{
			if (this.fieldNames[index].compareTo(name) == 0)
				return index;
		}
		
//		System.out.println("field not found: "+name);
		return -1;
	}

	public int getIndexForDbFieldName(String name)
	{
		for (int index=0; index < this.dbFieldNames.length; index++)
		{
			if (this.dbFieldNames[index].compareTo(name) == 0)
				return index;
		}
		
		if (DEBUG) System.out.println("dbField not found: "+name);
		return -1;
	}

	public void renameField(String oldName, String newName)
	{
		if (this.getIndexForFieldName(oldName) != -1)
			this.setFieldName(this.getIndexForFieldName(oldName),newName);
	}

	public void setDbFieldNames(String[] newDbFieldNames)
	{
		if (newDbFieldNames.length == this.fieldNames.length)
			this.dbFieldNames=newDbFieldNames;
	}

	public String[] getDbFieldNames()
	{
		return this.dbFieldNames;
	}

	public void setDbFieldName(int fieldIndex, String dbFieldName)
	{
		this.dbFieldNames[fieldIndex]=dbFieldName;
	}
	
	public String getDbFieldName(int fieldIndex)
	{
		return this.dbFieldNames[fieldIndex];
	}


	
	public void setMimetypes(String[] newMimetypes)
	{
		if (newMimetypes.length == this.mimetypes.length)
		{
			this.mimetypes=newMimetypes;
		}
	}
	
	public String[] getMimetypes()
	{
		return this.mimetypes;
	}
	
	public void setMimetype(int fieldIndex, String mimetype)
	{
		this.mimetypes[fieldIndex]=mimetype;
	}
	
	public String getMimetype(int fieldIndex)
	{
		return this.mimetypes[fieldIndex];
	}

	
	public void setFieldVisibility(boolean[] visible)
	{
		if (visible.length == this.showFields.length)
			this.showFields=visible;
	}
	
	public boolean[] getFieldVisibility()
	{
		return this.showFields;
	}
	
	public void setFieldVisibility(int fieldIndex, boolean visibility)
	{
		this.showFields[fieldIndex]=visibility;
	}
	
	public boolean getFieldVisibility(int fieldIndex)
	{
		return this.showFields[fieldIndex];
	}

	public void setTypes(int[] newtypes)
	{
		if (newtypes.length == this.types.length)
			this.types=newtypes;
	}
	
	public int[] getTypes()
	{
		return this.types;
	}
	
	public void setType(int fieldIndex, int type)
	{
		this.types[fieldIndex]=type;
	}
	
	public int getType(int fieldIndex)
	{
		return this.types[fieldIndex];
	}

	public String toString()
	{
		String outString="%columnName";
		for (int field=0; field < this.fieldNames.length; field++)
		{
			outString+="\t";
			outString+=this.fieldNames[field];
		}
		outString+="\n";
		outString+="%dbColumnName";
		for (int field=0; field < this.dbFieldNames.length; field++)
		{
			outString+="\t";
			outString+=this.dbFieldNames[field];
		}
		outString+="\n";
		outString+="%type";
		for (int field=0; field < this.types.length; field++)
		{
			outString+="\t";
			outString+=this.types[field];
		}
		outString+="\n";
		outString+="%datatype";
		for (int field=0; field < this.dataTypes.length; field++)
		{
			outString+="\t";
			if (this.dataTypes[field].getPackage().getName().equals("java.lang"))
				outString+=this.dataTypes[field].getName().substring(this.dataTypes[field].getName().lastIndexOf('.')+1);
			else
				outString+=this.dataTypes[field].getName();
		}
		outString+="\n";
		outString+="%showAttribute";
		for (int field=0; field < this.showFields.length; field++)
		{
			outString+="\t";
			outString+=this.showFields[field];
		}
		outString+="\n";
		return outString;
	}
	
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (FlexiDataDescription.class != obj.getClass()) return false;
		
		if ( ((FlexiDataDescription)obj).getNbFields() != this.getNbFields()) return false;
		
		for (int col=0; col < this.getNbFields(); col++)
		{
			if ( this.dbFieldNames[col].compareTo( ((FlexiDataDescription)obj).getDbFieldName(col)) != 0 )	return false;
			if ( this.dataTypes[col] != ((FlexiDataDescription)obj).getDataType(col) )	return false;
			if ( this.fieldNames[col].compareTo( ((FlexiDataDescription)obj).getFieldName(col)) != 0 )	return false;
			if ( this.mimetypes[col].compareTo( ((FlexiDataDescription)obj).getMimetype(col)) != 0 )	return false;
			if ( this.showFields[col] != ((FlexiDataDescription)obj).getFieldVisibility(col) )	return false;
		}
		
		return true;
	}
}