package org.cheminfo.hook.graphdraw2;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Hashtable;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.util.XMLCoDec;

public class GraphAxis extends BasicEntity
{
	public final static int HORIZONTAL	=	10;
	public final static int VERTICAL	=	11;
	public final static int PARALLEL	=	20;
	public final static int PERPENDICULAR=	21;
	
	private String title="TITLE";
	private String label="";
	private int axisOrientation; 
	private int labelOrientation;
	
	
	public GraphAxis(int axisOrientation)
	{
		this.axisOrientation=axisOrientation;
		if (axisOrientation == HORIZONTAL)
			labelOrientation=PARALLEL;
		else
			labelOrientation=PERPENDICULAR;
		
		
		this.setPrimaryColor(Color.blue);
		this.setSecondaryColor(Color.white);
	}

	public GraphAxis(String XMLString, Hashtable helpers)
	{
		this(GraphAxis.HORIZONTAL);
		
		XMLCoDec tempCodec = new XMLCoDec(XMLString);
		tempCodec.shaveXMLTag();

		this.setUniqueID(tempCodec.getParameterAsInt("uniqueID"));
		this.storeLinkIDs(tempCodec);
		
		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor"));
		this.setSecondaryColor(tempCodec.getParameterAsColor("secondaryColor"));
		this.axisOrientation=tempCodec.getParameterAsInt("axisOrientation");
		
	}
	
	public void setTitle(String newTitle)
	{
		this.title=newTitle;
	}
	
	public String getTitle()
	{
		return this.title;
	}
	
	public void checkSizeAndPosition()
	{
		GraphDisplay2 parentDisplay=(GraphDisplay2)this.getParentEntity();
		double hBorder=GraphDisplay2.AXIS_RELATIVE_SIZE*parentDisplay.getWidth();
		double vBorder=GraphDisplay2.AXIS_RELATIVE_SIZE*parentDisplay.getHeight();
		
		if (hBorder < 15) hBorder=15;
		if (vBorder < 15) vBorder=15;
		
		// in the case of the vertical axis, the entity will be rotated by 90 degrees CW
		
		double tempHeight=0;
		double tempWidth=0;
		
		double tempXPos=0;
		double tempYPos=0;
		
		if (this.axisOrientation == HORIZONTAL && parentDisplay.hasHAxis())
		{
			tempHeight=vBorder;
			tempWidth=parentDisplay.getWidth()-hBorder;
			tempXPos=hBorder;
			tempYPos=parentDisplay.getHeight()-vBorder;
			
			if (parentDisplay.hasVAxis())
				tempWidth-=hBorder;

			tempWidth=Math.ceil(tempWidth/4)*4;

		}
		else if (this.axisOrientation == VERTICAL && parentDisplay.hasVAxis())
		{
			tempHeight=hBorder;
			tempWidth=parentDisplay.getHeight()-vBorder;
			tempYPos=hBorder;
			tempXPos=vBorder;
			
			if (parentDisplay.hasHAxis())
				tempWidth-=vBorder;

			tempWidth=Math.ceil(tempWidth/4)*4;
		}
		
		this.setSize(tempWidth, tempHeight);
		this.setLocation(tempXPos, tempYPos);

		if (this.axisOrientation == VERTICAL && parentDisplay.hasVAxis())
		{
			AffineTransform tempTransf=new AffineTransform();
			tempTransf.setToIdentity();
			tempTransf.rotate(Math.PI/2);
			tempTransf.translate(tempXPos, -tempYPos);
			this.setLocalTransform(tempTransf);
		}

		super.checkSizeAndPosition();
	}
	
	private double roundDown(double inValue)
	{
		if (inValue < 0)
			return -(Math.floor(-inValue));
		else
			return Math.floor(inValue);
	}
	
	private double roundUp(double inValue)
	{
		if (inValue < 0)
			return -(Math.ceil(-inValue));
		else
			return Math.ceil(inValue);
	}
	
	public int getAxisOrientation()
	{
		return this.axisOrientation;
	}
	
	private void drawMainLabels(Graphics2D g)
	{
		GraphDisplay2 parentDisplay=(GraphDisplay2)this.getParentEntity();

		double leftLimit, rightLimit, minLimit, maxLimit;
		if (this.axisOrientation != VERTICAL)
		{
			leftLimit=parentDisplay.getLeftLimit();
			rightLimit=parentDisplay.getRightLimit();
			
		}
		else
		{
			leftLimit=parentDisplay.getTopLimit();
			rightLimit=parentDisplay.getBottomLimit();
		}

		maxLimit=Math.max(leftLimit, rightLimit);
		minLimit=Math.min(leftLimit, rightLimit);
		
		if (leftLimit != rightLimit)
		{
			double range=maxLimit-minLimit;

			double power=Math.floor(Math.log(Math.abs(range))/Math.log(10));
			
			double stepRange=Math.pow(10, power);
			
			int nbSteps=(int)Math.floor(Math.abs(range/stepRange))+2;
			
			double firstLabel=Math.floor(minLimit*Math.pow(10, -power))*Math.pow(10, power);
			
				
			double pixelsPerUnit=this.getWidth()/(rightLimit-leftLimit);

			
			double labelVal;
			double pixVal;
			
			DecimalFormat newFormat = new DecimalFormat();
			
			String formatString="#0";
			if (power < 0)
			{
				formatString+=".";
				
				for (int digit=0; digit < Math.abs(power); digit++)
					formatString+="0";
			}
			
			newFormat.applyPattern(formatString);
	
			if (range < 0) stepRange=-stepRange;
			
	
			FontMetrics metrics=g.getFontMetrics();
			Rectangle2D bounds;
			
			if (Math.abs(stepRange*pixelsPerUnit) >= this.getWidth()/2)
			{
				stepRange/=2;
				nbSteps*=2;
				
				if (power < 1 && power > -1)
					formatString+=".0";
				else if (power < 0)
					formatString+="0";
			}
			
			newFormat.applyPattern(formatString);

			//first pass to check if the labels overlap
			double maxLabelWidth=0;
			for (int step=0; step < nbSteps; step++)
			{
				labelVal=firstLabel+stepRange*step;
				bounds=metrics.getStringBounds(newFormat.format(labelVal), g);
				if (bounds.getWidth() > maxLabelWidth)
					maxLabelWidth = bounds.getWidth();
			}
			
			//if they do reduce the number of labels by half 
			if (maxLabelWidth*0.9 > Math.abs(stepRange*pixelsPerUnit))
			{
				stepRange*=2;
				nbSteps/=2;
			}

			// actually draw the labels
			for (int step=0; step < nbSteps; step++)
			{
				labelVal=firstLabel+stepRange*step;
				
				if (labelVal <= Math.max(leftLimit, rightLimit) && labelVal >= Math.min(leftLimit, rightLimit))
				{
					pixVal=(labelVal-leftLimit)*pixelsPerUnit;

					g.draw(new Line2D.Double(pixVal, 0, pixVal, this.getHeight()/5));

					bounds=metrics.getStringBounds(newFormat.format(labelVal), g);
					
		//			if (this.labelOrientation == PARALLEL)
//					g.drawString(newFormat.format(labelVal), (int)(pixVal-bounds.getWidth()/2), (int)(2*bounds.getHeight()));
					g.drawString(newFormat.format(labelVal), (int)(pixVal-bounds.getWidth()/2), (int)(this.getHeight()/3+bounds.getHeight()/2));
				}
			}
			
			double smallStepRange;
			int nbSmallSteps;
			
			if (Math.abs(stepRange*pixelsPerUnit) >= this.getWidth()/2)
				nbSmallSteps=nbSteps*10;
			else if (Math.abs(stepRange*pixelsPerUnit) >= this.getWidth()/4)
				nbSmallSteps=nbSteps*5;
			else if (Math.abs(stepRange*pixelsPerUnit) >= this.getWidth()/8)
				nbSmallSteps=nbSteps*2;
			else
				nbSmallSteps=0;
			
			smallStepRange=(stepRange/nbSmallSteps)*nbSteps;
			
			for (int step=0; step < nbSmallSteps; step++)
			{
				labelVal=firstLabel+smallStepRange*step;

				if (labelVal <= Math.max(leftLimit, rightLimit) && labelVal >= Math.min(leftLimit, rightLimit))
				{
					pixVal=(labelVal-leftLimit)*pixelsPerUnit;

					g.draw(new Line2D.Double(pixVal, 0, pixVal, this.getHeight()/10));
				}
			}
		}	
	}
	
	public void paint(Graphics2D g)
	{
		if (this.getPrimaryColor() != null)
		{
			g.setColor(this.getPrimaryColor());
			g.drawLine(0,0, (int)this.getWidth(), 0);
			
			int fontsize=this.getInteractiveSurface().getFontSize();
			if (this.getInteractiveSurface().useBigFonts())
				fontsize*=2;
			
			g.setFont(new Font("Arial", Font.PLAIN, fontsize));
			
			this.drawMainLabels(g);
			
			FontMetrics metrics=g.getFontMetrics();
			Rectangle2D bounds;
			bounds=metrics.getStringBounds(title, g);
			
			if (this.axisOrientation == GraphAxis.HORIZONTAL)
				g.drawString(title, (int)(this.getWidth()-bounds.getWidth()), (int)(this.getHeight()-bounds.getHeight()));
			else
				g.drawString(title, (int)(0), (int)(this.getHeight()-bounds.getHeight()));
				
//			Rectangle2D titleBounds=g.getFontMetrics().getStringBounds(this.title, g);
//			g.drawString(this.title, (int)(this.getWidth()-titleBounds.getWidth())/2, (int)(this.getHeight()+2*titleBounds.getHeight())/2);
		}
	}
	
/*	public String getXmlTag(Hashtable xmlProperties)
	{
		XMLCoDec tempCodec=new XMLCoDec();
		
		tempCodec.addParameter("primaryColor", this.getPrimaryColor().getRed()+","+this.getPrimaryColor().getGreen()+","+this.getPrimaryColor().getBlue());
		tempCodec.addParameter("secondaryColor", this.getSecondaryColor().getRed()+","+this.getSecondaryColor().getGreen()+","+this.getSecondaryColor().getBlue());
		tempCodec.addParameter("axisOrientation", new Integer(this.axisOrientation));
		
		this.addLinkXMLElements(tempCodec);
		
		String tempTag="<graphdraw2.GraphAxis ";
		tempTag+=tempCodec.encodeParameters();
		tempTag+=">\r\n";

		tempTag+="</graphdraw2.GraphAxis>\r\n";

		
		return tempTag;
	}
*/
}