package org.cheminfo.hook.graphdraw2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.Hashtable;

import org.cheminfo.hook.util.XMLCoDec;

public class Trendline
{
	public final static int LINEAR			=	0;
	public final static int POLYNOMIAL		=	1;
	public final static int SIGMOIDAL_E		=	2;
	public final static int SIGMOIDAL_10	=	3;
	
	private int type;
	private double[] parameters;
	
	public Trendline(int lineType, double[] parameters)
	{
		this.type=lineType;
		
		this.parameters=parameters;
		
		switch (this.type)
		{
			case LINEAR:
				if (parameters.length != 4)
					System.out.println("LINEAR trendline requires 4 parameters: angle, yAtZero, firstX, lastX");
				break;
				
			case POLYNOMIAL:
				if (parameters.length < 1 || parameters.length != parameters[0]+4)
					System.out.println("POLYNOMIAL trendline requires order+4 parameters: order, factors ( x order), yAtZero, firstX, lastX");
				break;
					
			case SIGMOIDAL_E:
				if (parameters.length != 6)
					System.out.println("SIGMOIDAL trendline requires 6 parameters: upper Asy, lower Asy, rate, center, firstX, lastX");
				break;
				
			case SIGMOIDAL_10:
				if (parameters.length != 6)
					System.out.println("SIGMOIDAL trendline requires 6 parameters: upper Asy, lower Asy, rate, center, firstX, lastX");
				break;
				
			default:
				break;
		}
	}
	
	public Trendline(String XMLTag, Hashtable helpers)
	{
		XMLCoDec tempCodec = new XMLCoDec(XMLTag);
		tempCodec.shaveXMLTag();

		this.type=tempCodec.getParameterAsInt("type");
		this.parameters=new double[tempCodec.getParameterAsInt("nbParameters")];
		
		for (int param=0; param < this.parameters.length; param++)
			this.parameters[param]=tempCodec.getParameterAsDouble("param"+param);
	}
	
	public double[] getParameters()
	{
		return this.parameters;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public void paint(Graphics2D g, SerieEntity area)
	{
		GraphDisplay2 parentDisplay=(GraphDisplay2)area.getParentEntity();
		
		double pixelsPerUnitV=area.getHeight()/(parentDisplay.getBottomLimit()-parentDisplay.getTopLimit());
		double pixelsPerUnitH=area.getWidth()/(parentDisplay.getRightLimit()-parentDisplay.getLeftLimit());
		
		switch (this.getType())
		{
			case LINEAR:
				double y1Units;
				double y2Units;

				if (parameters[0] == 0)
				{
					y1Units=y2Units=this.parameters[1];
				}
				else
				{
					System.out.println("not zero");
					y1Units=this.parameters[0]*this.parameters[2]+this.parameters[1];
					y2Units=this.parameters[0]*this.parameters[3]+this.parameters[1];
				}
				

				g.setColor(Color.black);

				g.draw(new Line2D.Double( (this.parameters[2]-parentDisplay.getLeftLimit())*pixelsPerUnitH, (y1Units-parentDisplay.getTopLimit())*pixelsPerUnitV, (this.parameters[3]-parentDisplay.getLeftLimit())*pixelsPerUnitH, (y2Units-parentDisplay.getTopLimit())*pixelsPerUnitV));
				break;
				
			case POLYNOMIAL:
			{
				double xUnits, yUnits;
				double prevYPixels, yPixels;
				prevYPixels=-999999;
				
				int order=(int)parameters[0];
				g.setColor(Color.black);
				
				for (int pixel=0; pixel < area.getWidth(); pixel++)
				{
					xUnits=parentDisplay.getLeftLimit()+pixel/pixelsPerUnitH;
					
					if (xUnits > Math.min(this.parameters[order+2], this.parameters[order+3]) &&
						xUnits < Math.max(this.parameters[order+2], this.parameters[order+3]) )
					{
						yUnits=this.parameters[order+1];
						
						for (int or=0; or <= order; or++)
							yUnits+=this.parameters[or+1]*Math.pow(xUnits, order-or);
						
						yPixels=(yUnits-parentDisplay.getTopLimit())*pixelsPerUnitV;
						if (prevYPixels != -999999)
						{
							g.draw(new Line2D.Double(pixel, yPixels, pixel-1, prevYPixels));
						}
						
						prevYPixels=yPixels;
					}
						
				}
			}
				break;
				
			case SIGMOIDAL_E:
			{
				double xUnits, yUnits;
				double prevYPixels, yPixels;
				prevYPixels=-999999;
				
				g.setColor(Color.black);
				
				for (int pixel=0; pixel < area.getWidth(); pixel++)
				{
					xUnits=parentDisplay.getLeftLimit()+pixel/pixelsPerUnitH;
					
					if (xUnits > Math.min(this.parameters[4], this.parameters[5]) &&
						xUnits < Math.max(this.parameters[4], this.parameters[5]) )
					{
						yUnits=parameters[1]+(parameters[0]-parameters[1])/(1+Math.exp(-parameters[2]*(xUnits-parameters[3])));
						
						yPixels=(yUnits-parentDisplay.getTopLimit())*pixelsPerUnitV;
						if (prevYPixels != -999999)
						{
							g.draw(new Line2D.Double(pixel, yPixels, pixel-1, prevYPixels));
						}
						
						prevYPixels=yPixels;
					}
				}
			}
			break;
			
			case SIGMOIDAL_10:
			{
				double xUnits, yUnits;
				double prevYPixels, yPixels;
				prevYPixels=-999999;
				
				g.setColor(Color.black);
				
				for (int pixel=0; pixel < area.getWidth(); pixel++)
				{
					xUnits=parentDisplay.getLeftLimit()+pixel/pixelsPerUnitH;
					
					if (xUnits > Math.min(this.parameters[4], this.parameters[5]) &&
						xUnits < Math.max(this.parameters[4], this.parameters[5]) )
					{
						yUnits=parameters[1]+(parameters[0]-parameters[1])/(1+Math.pow(10, -parameters[2]*(xUnits-parameters[3])));
						
						yPixels=(yUnits-parentDisplay.getTopLimit())*pixelsPerUnitV;
						if (prevYPixels != -999999)
						{
							g.draw(new Line2D.Double(pixel, yPixels, pixel-1, prevYPixels));
						}
						
						prevYPixels=yPixels;
					}
				}
			}
			break;			
			default:
				break;
					
		}
	}
	
	public String getXmlTag(Hashtable xmlProperties)
	{
		XMLCoDec tempCodec = new XMLCoDec();
		String tempTag="";
	
		tempCodec.addParameter("type", new Integer(this.type));
		tempCodec.addParameter("nbParameters", new Integer(this.parameters.length));
		
		for (int param=0; param < this.parameters.length; param++)
			tempCodec.addParameter("param"+param, new Double(this.parameters[param]));

		tempTag+="<graphdraw2.Trendline ";
		tempTag+=tempCodec.encodeParameters();
		tempTag+=">\r\n";


		tempTag+="</graphdraw2.Trendline>\r\n";
		return tempTag;
	}

}