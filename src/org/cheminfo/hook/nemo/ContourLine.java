package org.cheminfo.hook.nemo;

import java.util.Vector;

import org.cheminfo.hook.util.QuadTreeNode;
import org.cheminfo.hook.util.Segment;

public class ContourLine
{
	Vector<Segment> segments;
	double zValue;
	QuadTreeNode rootNode;
	
	public ContourLine(double zValue)
	{
		this.segments=new Vector<Segment>();
		this.zValue=zValue;
	}
	
	public double getZValue()
	{
		return this.zValue;
	}
	
	public void addSegment(float x1, float y1, float x2, float y2)
	{
		this.segments.add(new Segment(x1,y1,x2,y2));
	}
	
	/**
	 * This method only exists for compatibility reason and should be replace by addSegment(float, float, float, float)
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @deprecated
	 */
	public void addSegment(double x1, double y1, double x2, double y2)
	{
		this.segments.add(new Segment((float)x1,(float)y1,(float)x2,(float)y2));
	}
	
	public int getNbSegments()
	{
		return this.segments.size();
	}
	
	public double getP1x(int segment)
	{
		return ((Segment)this.segments.elementAt(segment)).x1;
	}
	
	public double getP1y(int segment)
	{
		return ((Segment)this.segments.elementAt(segment)).y1;
	}
	
	public double getP2x(int segment)
	{
		return ((Segment)this.segments.elementAt(segment)).x2;
	}
	
	public double getP2y(int segment)
	{
		return ((Segment)this.segments.elementAt(segment)).y2;
	}
	
	public void generateQuadTree(float leftLimit, float rightLimit, float topLimit, float bottomLimit, int maxElements, int maxDepth)
	{
		this.rootNode=new QuadTreeNode(leftLimit, rightLimit, topLimit, bottomLimit, this.segments, maxElements, maxDepth);
		this.segments.clear();
	}
	
	public void getSegments(float leftLimit, float rightLimit, float topLimit, float bottomLimit, Vector<Segment> outVector)
	{
		this.rootNode.getElements(leftLimit, rightLimit, topLimit, bottomLimit, outVector);
	}
	
	public void shift(double dX, double dY) {
		if (this.rootNode != null)
			this.rootNode.shift(dX,dY);
	}
	
}


