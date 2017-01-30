package org.cheminfo.hook.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class FlexiDataUtils
{
	final static boolean VERBOSE	=	false;
	
	/**
	 * Iteratively merges tables to the table containing a field with dbFieldName = rootDbField of type TYPE_RECORD_ID. Stops merging 
	 * when reaches a field with dbFieldName = ignoreField.
	 * @param hashedPoints
	 * @param rootDbField
	 * @param ignoreField
	 * @return
	 */
/*	public static Vector mergeTables(Hashtable hashedPoints, String rootDbField, String ignoreField)
	{
		FlexiDataDescription[] descriptions=(FlexiDataDescription[])(hashedPoints.keySet().toArray(new FlexiDataDescription[hashedPoints.keySet().size()]));
		FlexiDataDescription currentDesc=FlexiDataUtils.getDefiningDescription(descriptions, rootDbField);
		
		if (currentDesc == null)
		{
			if (VERBOSE)
			{
				System.out.println("rootDbField: "+rootDbField+" not found");
			}
			return null;
		}
		else
		{
			if (VERBOSE)
				System.out.println(currentDesc.toString());
		}
		
		
		// scan for fields of type TYPE_ID. These will have to be replaced.
		String linkedFieldName=null;
		
		FlexiDataDescription livingDesc;

		Vector currentPoints=(Vector)hashedPoints.get(currentDesc);	// points of the table we want to add columns to
		Vector linkedPoints;	// points of the table from which the data comes 
		Vector livingPoints;	// points of the resulting table
		FlexiDataPoint tempPoint, tempLinkedPoint;
		
		FlexiDataDescription linkedDesc;
		for (int field=1; field < currentDesc.getNbFields(); field++)
		{
			
			if (currentDesc.getType(field) == FlexiDataDescription.TYPE_ID && currentDesc.getDbFieldName(field).compareTo(ignoreField) != 0)
			{
				linkedFieldName=currentDesc.getDbFieldName(field);
				
				if (VERBOSE)
					System.out.println("LinkedField: "+linkedFieldName);
				linkedDesc=FlexiDataUtils.getDefiningDescription(descriptions, linkedFieldName);
				if (VERBOSE)
					if (linkedDesc == null)
						System.out.println("linked desc not found!");
				linkedPoints=(Vector)hashedPoints.get(linkedDesc);
				if (VERBOSE)
					System.out.println(linkedDesc.toString());

				
				// MERGE
				
				livingDesc=new FlexiDataDescription(currentDesc.getNbFields()+linkedDesc.getNbFields()-2);
				livingPoints=new Vector();
				
				// copy fields up to the linked one
				for (int copyField=0; copyField < field; copyField++)
				{
					livingDesc.setFieldName(copyField, currentDesc.getFieldName(copyField));
					livingDesc.setDbFieldName(copyField, currentDesc.getDbFieldName(copyField));
					livingDesc.setMimetype(copyField, currentDesc.getMimetype(copyField));
					livingDesc.setType(copyField, currentDesc.getType(copyField));
					livingDesc.setFieldVisibility(copyField, currentDesc.getFieldVisibility(copyField));
				}
				
				// copy field from linked description (omitting the first which is the id itself)
				for (int copyField=1; copyField < linkedDesc.getNbFields(); copyField++)
				{
					livingDesc.setFieldName(copyField+field-1, currentDesc.getFieldName(field)+"."+linkedDesc.getFieldName(copyField));
					livingDesc.setDbFieldName(copyField+field-1, linkedDesc.getDbFieldName(copyField));
					livingDesc.setMimetype(copyField+field-1, linkedDesc.getMimetype(copyField));
					livingDesc.setType(copyField+field-1, linkedDesc.getType(copyField));
					livingDesc.setFieldVisibility(copyField+field-1, linkedDesc.getFieldVisibility(copyField));
				}
				
				// copy all remaning fields
				for (int copyField=field+1; copyField < currentDesc.getNbFields(); copyField++)
				{
					livingDesc.setFieldName(copyField+linkedDesc.getNbFields()-2, currentDesc.getFieldName(copyField));
					livingDesc.setDbFieldName(copyField+linkedDesc.getNbFields()-2, currentDesc.getDbFieldName(copyField));
					livingDesc.setMimetype(copyField+linkedDesc.getNbFields()-2, currentDesc.getMimetype(copyField));
					livingDesc.setType(copyField+linkedDesc.getNbFields()-2, currentDesc.getType(copyField));
					livingDesc.setFieldVisibility(copyField+linkedDesc.getNbFields()-2, currentDesc.getFieldVisibility(copyField));
				}

				// generate the points
				
				for (int p=0; p < currentPoints.size(); p++)
				{
					tempPoint=new FlexiDataPoint(livingDesc);
					
					// copy fields up to the linked one
					for (int copyField=0; copyField < field; copyField++)
					{
						tempPoint.setFieldData(copyField, ((FlexiDataPoint)currentPoints.get(p)).getFieldData(copyField));
					}
					
					// find the right linked point
					tempLinkedPoint=null;
					for (int lp=0; lp < linkedPoints.size(); lp++)
					{
						if ( ((Integer)((FlexiDataPoint)linkedPoints.get(lp)).getFieldData(0)).intValue() == ((Integer)((FlexiDataPoint)currentPoints.get(p)).getFieldData(field)).intValue())
						{
							tempLinkedPoint=(FlexiDataPoint)linkedPoints.get(lp);

							// copy field from linked description (omitting the first which is the id itself)
							for (int copyField=1; copyField < linkedDesc.getNbFields(); copyField++)
							{
								tempPoint.setFieldData(copyField+field-1, tempLinkedPoint.getFieldData(copyField));
							}
							
							break;
						}
					}
					
					// copy all remaning fields
					for (int copyField=field+1; copyField < currentDesc.getNbFields(); copyField++)
					{
						tempPoint.setFieldData(copyField+linkedDesc.getNbFields()-2, ((FlexiDataPoint)currentPoints.get(p)).getFieldData(copyField));
					}
					
					livingPoints.add(tempPoint);
				}
				currentDesc=livingDesc;
				currentPoints=livingPoints;
				field=0;	// start from the beginning (useful?!?)
			}
		}

		
		return currentPoints;
	}
*/
	/**
	 * Uses the selectionRules to choose a FlexiDataPoint in the flexiPoints Vector and returns the Object related to the specified fieldName.
	 * The FlexiDataPoints in flexiPoints typically do not share the same structure (i.e. they do not have the same FlexiDataDescription) but are usually
	 * linked to a same "point". As an example, in the case of a plate's well some points would represent a specific compound and some might contain informations
	 * about a measurement.
	 * The Hashtable selectionRules contains names of fields in the keys and the desired "values" in the relative objects. 
	 * @param flexiPoints
	 * @param selectionRules
	 * @param fieldName
	 * @return
	 */
	public static Object getValueForCondition(Vector flexiPoints, Hashtable selectionRules, String fieldName)
	{
		String[] keys=(String[])(selectionRules.keySet().toArray(new String[selectionRules.keySet().size()]));
		boolean rightPoint=false;
		
		FlexiDataPoint tempPoint;
		for (int point=0; point < flexiPoints.size(); point++)
		{
			tempPoint=(FlexiDataPoint)flexiPoints.get(point);
			rightPoint=true;
			
			if ( tempPoint.getDescription().getIndexForFieldName(fieldName) != -1)
			{
				for (int rule=0; rule < keys.length; rule++)
				{
					rightPoint=false;
	
					if (tempPoint.getDescription().getIndexForFieldName(keys[rule]) != -1
	//					&& tempPoint.getDescription().getIndexForFieldName(fieldName) != -1
						&& tempPoint.getFieldData(keys[rule]).getClass() == selectionRules.get(keys[rule]).getClass() )
					{
						if (tempPoint.getFieldData(keys[rule]) instanceof String
							&&	((String)tempPoint.getFieldData(keys[rule])).compareTo( ((String)selectionRules.get(keys[rule])) ) == 0 )
						{
							rightPoint=true;
						}
						else if (tempPoint.getFieldData(keys[rule]) instanceof Double
							&& ((Double)tempPoint.getFieldData(keys[rule])).doubleValue() == ((Double)selectionRules.get(keys[rule])).doubleValue() )
						{
							rightPoint=true;
						}
						else if (tempPoint.getFieldData(keys[rule]) instanceof Integer
								&& ((Integer)tempPoint.getFieldData(keys[rule])).intValue() == ((Integer)selectionRules.get(keys[rule])).intValue() )
						{
							rightPoint=true;
						}
						
					} 
					
					if (!rightPoint)
						break;
				}
			}
			else
				rightPoint=false;
			
			if (rightPoint)
			{
				return tempPoint.getFieldData(fieldName);
			}
		}
		
		return null;
	}
	
	
	/**
	 * Finds the FlexiDataDescription that contains the specified field characterized by a TYPE_RECORD_ID
	 * @param descriptions
	 * @param dbFieldName
	 * @return
	 */
/*	private static FlexiDataDescription getDefiningDescription(FlexiDataDescription[] descriptions, String dbFieldName)
	{
		FlexiDataDescription currentDesc=null;

		for (int desc=0; desc < descriptions.length; desc++)
		{
			currentDesc=(FlexiDataDescription)descriptions[desc];
			for (int field=0; field < currentDesc.getNbFields(); field++)
			{
				if (currentDesc.getDbFieldName(field).compareTo(dbFieldName) == 0
						&& currentDesc.getType(field) == FlexiDataDescription.TYPE_RECORD_ID)
				{
					return currentDesc;
				}
			}
		}
		return null;
		
	}
*/	
	public static Hashtable groupByDescription(FlexiDataPoint[] flexiPoints)
	{
		return FlexiDataUtils.groupByDescription(flexiPoints, new FlexiDataDescription[0]);
	}
	/**
	 * Groups the provided point by description and returns them in a Hashtable defined by description.
	 * @param flexiPoints
	 * @return
	 */
	public static Hashtable groupByDescription(FlexiDataPoint[] flexiPoints, FlexiDataDescription[] descriptions)
	{
		Hashtable hashedPoints=new Hashtable();
		Enumeration keys;
		FlexiDataDescription currentKey;
		boolean foundSimilar;
		
		for (int desc=0; desc < descriptions.length; desc++)
		{
			foundSimilar=false;
			keys=hashedPoints.keys();
			while (keys.hasMoreElements())
			{
				currentKey=(FlexiDataDescription)keys.nextElement();
				if (descriptions[desc].equals(currentKey))
				{
					foundSimilar=true;
				}
			}

			if (!foundSimilar)
				hashedPoints.put(descriptions[desc], new Vector());
		}

		// this part creates a hashtable containing Vector of FlexiDataPoints that share the same FlexiDataDescription
		for (int point=0; point < flexiPoints.length; point++)
		{
			if (!hashedPoints.containsKey(flexiPoints[point].getDescription()))
			{
				//the description is not the same Object, it might still be an Object with identical content 
				foundSimilar=false;
				keys=hashedPoints.keys();
				while (keys.hasMoreElements())
				{
					currentKey=(FlexiDataDescription)keys.nextElement();
					if (flexiPoints[point].getDescription().equals(currentKey))
					{
						flexiPoints[point].setDescription(currentKey);
						foundSimilar=true;
					}
				}
				if (!foundSimilar)	// it is really a new one
				{
					hashedPoints.put(flexiPoints[point].getDescription(), new Vector());
				}
			}
			
			((Vector)hashedPoints.get(flexiPoints[point].getDescription())).add(flexiPoints[point]);
		}
		
		return hashedPoints;
	}

}