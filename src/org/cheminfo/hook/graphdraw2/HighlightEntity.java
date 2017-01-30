package org.cheminfo.hook.graphdraw2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.util.QuadTreeElement;
import org.cheminfo.hook.util.XMLCoDec;

public class HighlightEntity extends BasicEntity implements QuadTreeElement
{
	private double topValue, bottomValue;
	private Color color;
	
	public HighlightEntity(double top, double bottom, Color color)
	{
		this.topValue=top;
		this.bottomValue=bottom;
		this.color=color;
	}
	
	public HighlightEntity(String XMLTag, Hashtable helpers)
	{
		XMLCoDec tempCodec = new XMLCoDec(XMLTag);
		tempCodec.shaveXMLTag();

		this.topValue=tempCodec.getParameterAsDouble("topValue");
		this.bottomValue=tempCodec.getParameterAsDouble("bottomValue");
		this.color=tempCodec.getParameterAsColor("color");
		this.color=new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50);
	}
	
	public double getTopValue()
	{
		return this.topValue;
	}
	
	public double getBottomValue()
	{
		return this.bottomValue;
	}

	public Color getColor()
	{
		return this.color;
	}

	public boolean isContained(float queryX1, float queryX2, float queryY1, float queryY2)
	{
		if ( this.topValue >= Math.min(queryY1, queryY2) && this.bottomValue <= Math.max(queryY1, queryY2))
			return true;

		return false;
	}

	public void paint(Graphics2D g, float tileLeft, float tileRight, float tileTop, float tileBottom, float tileWidth, float tileHeight)
	{
		if (this.color != null)
		{
			g.setColor(this.color);
			g.fill(new Rectangle2D.Double( 0, (tileTop-this.topValue)*tileHeight/(tileTop-tileBottom), tileWidth, (this.topValue-this.bottomValue)*tileHeight/(tileTop-tileBottom)));

			g.setColor(Color.BLACK);
//			g.draw(new Rectangle2D.Double( 0, (tileTop-this.topValue)*tileHeight/(tileTop-tileBottom), tileWidth, (this.topValue-this.bottomValue)*tileHeight/(tileTop-tileBottom)));
			g.draw(new Line2D.Double( 0, (tileTop-this.topValue)*tileHeight/(tileTop-tileBottom), tileWidth, (tileTop-this.topValue)*tileHeight/(tileTop-tileBottom)));
			g.draw(new Line2D.Double( 0, (tileTop-this.bottomValue)*tileHeight/(tileTop-tileBottom), tileWidth, (tileTop-this.bottomValue)*tileHeight/(tileTop-tileBottom)));

			g.draw(new Line2D.Double( 0, (tileTop-(topValue+bottomValue)/2 )*tileHeight/(tileTop-tileBottom), tileWidth, (tileTop-(topValue+bottomValue)/2)*tileHeight/(tileTop-tileBottom)));
		}
	}
	
	public String getXmlTag(Hashtable xmlProperties)
	{
		XMLCoDec tempCodec = new XMLCoDec();
		String tempTag="";
	
		tempCodec.addParameter("topValue", new Double(this.topValue));
		tempCodec.addParameter("bottomValue", new Double(this.bottomValue));
		tempCodec.addParameter("color", this.color.getRed()+","+this.color.getGreen()+","+this.color.getBlue());

		tempTag+="<graphdraw2.HighlightEntity ";
		tempTag+=tempCodec.encodeParameters();
		tempTag+=">\r\n";


		tempTag+="</graphdraw2.HighlightEntity>\r\n";
		return tempTag;
	}
}