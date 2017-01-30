package org.cheminfo.hook.util;

import java.util.Vector;

public class GeneralMethods
{
	
 

	
	public static String encodeString(String theString)
	{	
		theString=theString.replaceAll( "&", "&amp;");
		theString=theString.replaceAll("\\r\\n", "&crlf;");
		theString=theString.replaceAll("\n", "&lf;");
		theString=theString.replaceAll("\"", "&quot;");
		theString=theString.replaceAll("<", "&lt;");
		theString=theString.replaceAll(">", "&gt;");
		theString=theString.replaceAll(" ", "&spc;");
		theString=theString.replaceAll("\\?", "&ques;");
		
		return theString;
	}
	
	public static String decodeString(String theString)
	{	
		theString=theString.replaceAll("&quot;", "\"");
		theString=theString.replaceAll("&lt;", "<");
		theString=theString.replaceAll("&gt;", ">");
		theString=theString.replaceAll("&spc;", " ");
		theString=theString.replaceAll("&crlf;", "\r\n");
		theString=theString.replaceAll("&lf;", "\n");
		theString=theString.replaceAll("&ques;", "?");
		theString=theString.replaceAll("&amp;", "&");
		
		return theString;
	}

	public static Object resizeArray (Object oldArray, int newSize)
	{
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(
			elementType,newSize);
		int preserveLength = Math.min(oldSize,newSize);
		if (preserveLength > 0)
			System.arraycopy (oldArray,0,newArray,0,preserveLength);
		return newArray;
	}
	
	private static void sortDoubleVector(Vector doubleVector)
	{
		Double tempValue;
		for (int i=0; i < doubleVector.size()-1; i++)
		{
			for (int j=i+1; j < doubleVector.size(); j++)
			{
				if (((Double)doubleVector.elementAt(j)).doubleValue() > ((Double)doubleVector.elementAt(i)).doubleValue())
				{	
					tempValue=(Double)doubleVector.elementAt(j);
					doubleVector.removeElementAt(j);
					doubleVector.insertElementAt(tempValue, i);
				}
			}
		}
	}
	
		
}