package org.cheminfo.hook.graphdraw2;

import java.awt.Color;
import java.awt.geom.Point2D;

public class SerieData
{
	public final static int ADDON_NONE		=	99;
	public final static int ADDON_3SIGMA	=	100;

	private double x[];
	private double y[];
	private double eX[];
	private double eY[];
	private Object reference[];
	private String comment[];
	private String xUnits, yUnits;
	private String serieName="";
	private int dataAspect;
	private Color originalColor;
	private int addon;
	
	public void init(int nbPoints)
	{
		this.x=new double[nbPoints];
		this.y=new double[nbPoints];
		this.eX=new double[nbPoints];
		this.eY=new double[nbPoints];
		this.reference=new Object[nbPoints];
		this.comment=new String[nbPoints];
		
		this.xUnits="";
		this.yUnits="";
		
		this.addon=ADDON_NONE;
	}

	public void setName(String name)
	{
		this.serieName=name;
	}
	
	public String getName()
	{
		return this.serieName;
	}
	
	public void setDataAspect(int newDataAspect)
	{
		this.dataAspect=newDataAspect;
	}
	
	public int getDataAspect()
	{
		return this.dataAspect;
	}
	
	public void setColor(Color color)
	{
		this.originalColor=color;
	}
	
	public Color getColor()
	{
		return this.originalColor;
	}
	
	public void setAddon(int newAddon)
	{
		this.addon=newAddon;
	}
	
	public int getAddon()
	{
		return this.addon;
	}
	
	public void setXUnits(String xUnits)
	{
		this.xUnits=xUnits;
	}

	public String getXUnits()
	{
		return this.xUnits;
	}
	
	public void setYUnits(String yUnits)
	{
		this.yUnits=yUnits;
	}

	public String getYUnits()
	{
		return this.yUnits;
	}
	
	public void setPoint(int index, double x, double y, double errX, double errY, String comment, Object reference)
	{
/*		if (index >= this.x.length)
		{
			this.x=(double[])resizeArray(this.x, index+1);
			this.x=(double[])resizeArray(this.x, index+1);
			this.x=(double[])resizeArray(this.x, index+1);
			this.x=(double[])resizeArray(this.x, index+1);
			this.x=(double[])resizeArray(this.x, index+1);
		}
*/		this.x[index]=x;
		this.y[index]=y;
		this.eX[index]=errX;
		this.eY[index]=errY;
		this.reference[index]=reference;
		this.comment[index]=comment;
	}
	
	public double getX(int index)
	{
		return this.x[index];
	}
	
	public double getY(int index)
	{
		return this.y[index];
	}
	
	public double getErrX(int index)
	{
		return this.eX[index];
	}
	
	public double getErrY(int index)
	{
		return this.eY[index];
	}
	
	public Object getReference(int index)
	{
		return this.reference[index];
	}

	public String getComment(int index)
	{
		return this.comment[index];
	}
	
	public Point2D.Double getElementAsPoint2D(int index)
	{
		if (index < this.x.length)
			return new Point2D.Double(x[index], y[index]);
		else
			return new Point2D.Double(-9999, -9999);
	}
	
	public Point2D.Double[] getAsArray()
	{
		Point2D.Double[] array=new Point2D.Double[x.length];
		for (int i=0; i < x.length; i++)
		{
			array[i]=new Point2D.Double(x[i], y[i]);
		}
		
		return array;
	}
	
	public int getNbPoints()
	{
		return this.x.length;
	}
	
	private static Object resizeArray (Object oldArray, int newSize) {
		   int oldSize = java.lang.reflect.Array.getLength(oldArray);
		   Class elementType = oldArray.getClass().getComponentType();
		   Object newArray = java.lang.reflect.Array.newInstance(
		         elementType,newSize);
		   int preserveLength = Math.min(oldSize,newSize);
		   if (preserveLength > 0)
		      System.arraycopy (oldArray,0,newArray,0,preserveLength);
		   return newArray; }
}