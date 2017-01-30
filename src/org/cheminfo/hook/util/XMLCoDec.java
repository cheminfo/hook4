package org.cheminfo.hook.util;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class XMLCoDec
{
	final static boolean	DEBUG	=	false;
	final static int START		=	0;
	final static int TAG_NAME	=	1;
	final static int PARAM		=	2;
	final static int PARAM_VAL	=	3;
	final static int END		=	4;
	
	int statusFlag, nextStatusFlag;
	
	String XMLString;
	Hashtable parameters;
	
	public XMLCoDec()
	{
		this.parameters = new Hashtable();
	}
	
	public XMLCoDec(String XMLString)
	{
		this();
		this.XMLString=XMLString;
	}
	
	public void setXMLString(String XMLString)
	{
		this.XMLString=XMLString;
	}
	
	public String getXMLString()
	{
		return this.XMLString;
	}
	
	/**
	 * Returns the number of elements present at rool level in the XML string.
	 * @param original the XML string to be analyzed.
	 * @return an integer representing the number of root elements.
	 */
	public int getRootElementsCount(String original)
	{
		int nbElements=0;
		int currLevel=0;
		int currPos=0;
		int nextStart=0;
		int nextEnd=0;
		
		while (nextStart != -1)
		{
			nextStart=original.indexOf("<", currPos);
			nextEnd=original.indexOf("</", currPos);
			
			if (nextStart == nextEnd)
			{
				currPos=nextEnd+1;
				currLevel--;
				if (currLevel == 0) nbElements++;
			}
			else
			{
				currPos=nextStart+1;
				currLevel++;
			}
		}
		
		return nbElements;
	}		
	
	public void clearParameters()
	{
		this.parameters = new Hashtable();
	}
	
	/**
	 * Returns the number of elements present at rool level in the internal XML string.
	 * @return an integer representing the number of root elements.
	 */
	public int getRootElementsCount()
	{
		int nbElements=0;
		int currLevel=0;
		int currPos=0;
		int nextStart=0;
		int nextEnd=0;
		
		this.clearParameters();
		
		while (nextStart != -1)
		{
			nextStart=this.XMLString.indexOf("<", currPos);
			nextEnd=this.XMLString.indexOf("</", currPos);
			
			if (nextStart == nextEnd)
			{
				currPos=nextEnd+1;
				currLevel--;
				if (currLevel == 0) nbElements++;
			}
			else
			{
				currPos=nextStart+1;
				currLevel++;
			}
		}
		
		return nbElements;
	}		
	
	/**
	 * Returns the first root tag String from 'original' and removes it from it.
	 * @param original the XML String to be analyzed.
	 * @return the first root tag encountered in original
	 **/
	public String popXMLTag(String original)
	{
		this.clearParameters();

		int currLevel=0;
		int currPos=0;
		int nextStart, nextEnd;

		do {
			nextStart=original.indexOf("<", currPos);
			nextEnd=original.indexOf("</", currPos);
			
			if (nextStart == nextEnd && nextStart != -1)
			{
				currPos=nextEnd+1;
				currLevel--;
			}
			else if (nextStart == nextEnd && nextStart == -1)
			{ return null; }
			else
			{
				currPos=nextStart+1;
				currLevel++;
			}
		} while (currLevel != 0);
		
		return original.substring(0, original.indexOf(">", nextEnd)+1);
	}

		
	/**
	 * Returns the first root tag String from the internal XML String
	 * and removes it.
	 * @return the first root tag encountered in original
	 **/
	public String popXMLTag()
	{
		this.clearParameters();

		int currLevel=0;
		int currPos=0;
		int nextStart, nextEnd;

		do {
			nextStart=this.XMLString.indexOf("<", currPos);
			nextEnd=this.XMLString.indexOf("</", currPos);
			
			if (nextStart == nextEnd && nextStart != -1)
			{
				currPos=nextEnd+1;
				currLevel--;
			}
			else if (nextStart == nextEnd && nextStart == -1)
			{ return null; }
			else
			{
				currPos=nextStart+1;
				currLevel++;
			}
		} while (currLevel != 0);
		
		String retValue=this.XMLString.substring(0, this.XMLString.indexOf(">", nextEnd)+1);

		this.XMLString=this.XMLString.substring(this.XMLString.indexOf(">", nextEnd)+1, this.XMLString.length());
		
		return retValue;
	}
			
	public String readXMLTag()
	{
		this.clearParameters();

		int currLevel=0;
		int currPos=0;
		int nextStart, nextEnd;

		do {
			nextStart=this.XMLString.indexOf("<", currPos);
			nextEnd=this.XMLString.indexOf("</", currPos);
			
			if (nextStart == nextEnd && nextStart != -1)
			{
				currPos=nextEnd+1;
				currLevel--;
			}
			else if (nextStart == nextEnd && nextStart == -1)
			{ return null; }
			else
			{
				currPos=nextStart+1;
				currLevel++;
			}
		} while (currLevel != 0);
		
		
		return this.XMLString.substring(0, this.XMLString.indexOf(">", nextEnd)+1);
	}

	/**
	 * Decodes and removes the root tag contained in 'original'. The parameters contained in the
	 * tag can then be accessed through the getParameter method. This method returns the original
	 * String stripped of the root tag. Makes use of decodeXML(String tagString).
	 * @param original the XML String to be analyzed.
	 * @return a String containing the shaved tag.
	 */
	public String shaveXMLTag(String original)
	{
		this.clearParameters();

		original.trim();
		int startChar=original.indexOf("<");
		int endChar=original.indexOf(">");
		if (startChar != -1 && endChar > startChar)
		{
			this.decodeXML(original.substring(startChar, endChar));
		}

		int currLevel=1;
		int currPos=1;
		while (currLevel != 0)
		{
			int nextStart=original.indexOf("<", currPos);
			int nextEnd=original.indexOf("</", currPos);
			
			if (nextStart == nextEnd)
			{
				currPos=nextEnd+1;
				currLevel--;
			}
			else
			{
				currPos=nextStart+1;
				currLevel++;
			}
		}
		int endTag=currPos-1;
		
		if (endTag != -1)
			original=original.substring(endChar+1, endTag);
		else 
			original=original.substring(endChar+1, original.length());
			
		return original;
	}

	/**
	 * Decodes and removes the root tag contained in the internal XML String. The parameters contained in
	 * tag can then be accessed by the getParameterAsXXX methods. This method returns the original
	 * String stripped of the root tag. Makes use of decodeXML(String tagString).
	 * @return a String containing the shaved tag.
	 */
	public String shaveXMLTag()
	{
		this.clearParameters();

		this.XMLString.trim();
		int startChar=this.XMLString.indexOf("<");
		
		int endChar=this.XMLString.indexOf(">");
		if (startChar != -1 && endChar > startChar)
		{
			this.decodeXML(this.XMLString.substring(startChar, endChar));
		}

		int currLevel=1;
		int currPos=startChar+1;
		while (currLevel != 0)
		{
			int nextStart=this.XMLString.indexOf("<", currPos);
			int nextEnd=this.XMLString.indexOf("</", currPos);
			
			if (nextStart == nextEnd)
			{
				currPos=nextEnd+1;
				currLevel--;
			}
			else
			{
				currPos=nextStart+1;
				currLevel++;
			}
		}
		int endTag=currPos-1;
		
		if (endTag != -1)
			this.XMLString=this.XMLString.substring(endChar+1, endTag);
		else 
		{
			this.XMLString=this.XMLString.substring(endChar+1, this.XMLString.length());
		}
			
		return this.XMLString;
	}
	
	/**
	 * Decodes the root tag contained in 'original'. The parameters contained in
	 * tag can then be accessed by the getParameter method. Makes use of 
	 * decodeXML(String tagString).
	 * @param original the XML tag to be read.
	 * @see shaveXMLTag(String original)
	 */
	protected void readRootTag(String original)
	{
		this.clearParameters();

		original.trim();
		int startChar=original.indexOf("<");
		int endChar=original.indexOf(">");
		if (startChar != -1 && endChar > startChar)
		{
			this.decodeXML(original.substring(startChar, endChar));
		}
	}

	protected void readRootTag()
	{
		this.clearParameters();
		
		this.XMLString.trim();
		int startChar=this.XMLString.indexOf("<");
		int endChar=this.XMLString.indexOf(">");
		if (startChar != -1 && endChar > startChar)
		{
			this.decodeXML(this.XMLString.substring(startChar, endChar));
		}
	}
	
	protected void decodeXML(String tagString)
	{
		String[] splittedString=tagString.split(" ");
		
		this.addParameter("tagName", splittedString[0].substring(1));
		if (DEBUG) System.out.println("TAGNAME: "+this.getParameterAsString("tagName"));
		for (int param=1; param < splittedString.length; param++)
		{
			int position=splittedString[param].indexOf("=");
			if (position>0) {
				this.addParameter(
						splittedString[param].substring(0,position),
						GeneralMethods.decodeString(splittedString[param].substring(position+2,splittedString[param].length()-1))
				);
				if (DEBUG) System.out.println("PARAM: "+splittedString[param].substring(0,position)+" -> "+GeneralMethods.decodeString(splittedString[param].substring(position+2,splittedString[param].length()-1)) );				
			} else {
				this.addParameter(splittedString[param],"");
			}
		}
		
/*		StringBuffer currentWord = new StringBuffer();
		String tempString;
		int currChar=0;
		statusFlag=XMLCoDec.START;
		currChar++;
		statusFlag=XMLCoDec.TAG_NAME;
		while (currChar < tagString.length())
		{
			currentWord.append(tagString.charAt(currChar));
			if (currentWord.toString().endsWith(" ") || currentWord.toString().endsWith(">"))	//	END of word
			{
				tempString=currentWord.toString();
				if (tempString.compareTo(" ") != 0 && tempString.compareTo(">") != 0)
				{
					tempString=tempString.substring(0, tempString.length());

					switch (statusFlag)
					{
						case TAG_NAME:
							this.addParameter("tagName", currentWord.toString());
							currentWord = new StringBuffer();
							nextStatusFlag=XMLCoDec.PARAM;
							break;
						
						case PARAM:
							tempString=currentWord.toString();
							String value=GeneralMethods.decodeString(tempString.substring(tempString.indexOf("=\"")+2, tempString.indexOf("\"",tempString.indexOf("=\"")+2 )));
							this.addParameter(tempString.substring(0, tempString.indexOf("=\"")), value);
							currentWord = new StringBuffer();
							break;
							
						default:
							break;
					}
				}
				else currentWord = new StringBuffer();

				statusFlag=nextStatusFlag;
			}
			currChar++;
		}
*/	}
		
	public void addParameter(String paramName, Object paramValue)
	{
		if (paramValue instanceof String)
			paramValue=GeneralMethods.encodeString((String)paramValue);

		this.parameters.put(paramName, paramValue);
	}
	
	public boolean hasParameter(String paramName)
	{
		return this.parameters.containsKey(paramName);
	}
	
	public int getParameterAsInt(String paramName)
	{
		int retValue=0;
		if (this.parameters.containsKey(paramName))
		{
			if (this.parameters.get(paramName) instanceof Double)
				retValue=((Integer)this.parameters.get(paramName)).intValue();
			else if (this.parameters.get(paramName) instanceof String)
				retValue=Integer.parseInt((String)this.parameters.get(paramName));
		}
		else return 0;
		return retValue;
	}

	public double getParameterAsDouble(String paramName)
	{
		double retValue=0;
		if (this.parameters.containsKey(paramName))
		{
			if (this.parameters.get(paramName) instanceof Double)
				retValue=((Double)this.parameters.get(paramName)).doubleValue();
			else if (this.parameters.get(paramName) instanceof String)
				retValue=(new Double((String)this.parameters.get(paramName))).doubleValue();
		}
		else return 0;
		
		return retValue;
	}
	
	public float getParameterAsFloat(String paramName)
	{
		float retValue=0;
		if (this.parameters.containsKey(paramName))
		{
			if (this.parameters.get(paramName) instanceof Float)
				retValue=((Float)this.parameters.get(paramName)).floatValue();
			else if (this.parameters.get(paramName) instanceof String)
				retValue=(new Float((String)this.parameters.get(paramName))).floatValue();
		}
		else return 0;
		
		return retValue;
	}
	
	public String getParameterAsString(String paramName)
	{
		String retValue;
		if (this.parameters.get(paramName) instanceof String)	retValue=GeneralMethods.decodeString((String)this.parameters.get(paramName));
		else retValue=String.valueOf(this.parameters.get(paramName));
		
		return retValue;
	}
	
	public String getParameterAsNonNullString(String paramName)
	{
		String retValue=getParameterAsString(paramName);
		if (retValue==null) return "";
		return retValue;
	}
	
	public Color getParameterAsColor(String paramName) {
		return getParameterAsColor(paramName, (Color)null);
	}
	
	public Color getParameterAsColor(String paramName, Color color)
	{
		if (this.parameters.get(paramName) == null)
			return color;

		if ( ((String)this.parameters.get(paramName)).compareTo("NULL") == 0)
			return color;
		
		StringTokenizer tempTok=new StringTokenizer((String)this.parameters.get(paramName), ",");
		Color retValue;
		if (tempTok.countTokens() == 1) retValue=null;		
		if (tempTok.countTokens() !=3)	retValue=Color.black;
		else retValue=new Color(Integer.parseInt(tempTok.nextToken()),Integer.parseInt(tempTok.nextToken()),Integer.parseInt(tempTok.nextToken()));		
		return retValue;
	}
	
	public boolean getParameterAsBoolean(String paramName) {
		boolean retValue=false;
		if (this.parameters.containsKey(paramName))
		{
			if (this.parameters.get(paramName) instanceof Boolean)
			{
				retValue=((Boolean)this.parameters.get(paramName)).booleanValue();
			}
			else if (this.parameters.get(paramName) instanceof String)
				retValue=(new Boolean((String)this.parameters.get(paramName))).booleanValue();
		}
		else return false;
		
		return retValue;
	}

	public String encodeParameters()
	{
		String tempString="";
		
		Enumeration keys=this.parameters.keys();
		for (Enumeration vals=this.parameters.elements(); vals.hasMoreElements();)
		{
			tempString+=(String)keys.nextElement()+"=\""+String.valueOf(vals.nextElement())+"\" ";
		}
		return tempString;
	}

	protected String listParamNames()
	{
		String tempString=this.parameters.keys().toString();
		
		return tempString;
	}
	
}