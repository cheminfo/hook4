package org.cheminfo.hook.util;

import java.util.Vector;

public class IndexedTable
{
	private FlexiDataPoint[] tablePoints;
	private int[] indexesInt;

	public IndexedTable(FlexiDataPoint[] tablePoints)
	{
		this.tablePoints=tablePoints;
	}
	
	public IndexedTable(Vector table)
	{
//		this.tablePoints=new FlexiDataPoint[table.size()];
		this.tablePoints=(FlexiDataPoint[])table.toArray(new FlexiDataPoint[table.size()]);
	}
	
	/**
	 * The type of the desired field must be Integer (to be indexable).
	 * @param fieldName
	 * @return
	 */
	public boolean indexByField(String fieldName)
	{
		if (this.tablePoints.length > 0)
		{
			int fieldIndex=this.tablePoints[0].getDescription().getIndexForFieldName(fieldName);
			this.indexesInt=new int[tablePoints.length];
			if (fieldIndex == -1)
			{
				System.out.println("Field not found");
				return false;
			}
			
			for (int p=0; p < this.tablePoints.length; p++)
			{
				if (this.tablePoints[p].getFieldData(fieldIndex) instanceof Integer)
					this.indexesInt[p]=((Integer)this.tablePoints[p].getFieldData(fieldIndex)).intValue();
				else if (this.tablePoints[p].getFieldData(fieldIndex) instanceof String)
					this.indexesInt[p]=Integer.parseInt( ((String)this.tablePoints[p].getFieldData(fieldIndex)) );
				else 
				{
					System.out.println("type does not match");
					return false;
				}
			}
			
			//			
			try {
				Sorter.sort(this.indexesInt, this.tablePoints);
			} catch (Exception e) { System.out.println(e);}
		}

		return true;
	}
	
	public FlexiDataPoint[] get(int key)
	{
		Vector vec=this.getAsVector(key);
		
		if (vec == null)
			return null;
		
		else
			return (FlexiDataPoint[])vec.toArray(new FlexiDataPoint[vec.size()]);
	}
	
	public Vector getAsVector(int key)
	{
		Vector tempVec=new Vector();
		
		int upEl=this.indexesInt.length;
		int downEl=0;
		int currentEl=this.indexesInt.length/2;
		
		while (this.indexesInt[currentEl] != key && upEl != currentEl && downEl != currentEl)
		{
			if (this.indexesInt[currentEl] > key)
			{
				upEl=currentEl;
				currentEl=(currentEl+downEl)/2;
			}
			else if (this.indexesInt[currentEl] < key)
			{
				downEl=currentEl;
				currentEl=(currentEl+upEl)/2;
			}
					
		}
		
		if (this.indexesInt[currentEl] == key)
		{
			int el=currentEl;
			while (el >= 0 && this.indexesInt[el] == key )
				el--;
			
			el++;
			
			while (el < this.indexesInt.length && this.indexesInt[el] == key)
			{
				tempVec.add(this.tablePoints[el]);
				el++;
			}
			
			return tempVec;
		}
		else
			return null;
		
	}
}