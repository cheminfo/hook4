package org.cheminfo.hook.nemo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.util.Hashtable;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.util.XMLCoDec;

public class VerticalScale extends BasicEntity
{
	private double currentUpperLimit, currentLowerLimit;
	private Font currentFont; 
	
	private final static Color DEFAULT_COLOR=Color.blue;
	
	/**
	* Constructs an VerticaleScale object
	* @param width - defines the width in pixel for this object
	* @param height - defines the height in pixel for this object
	* @param upperLimit - the value of the left limit in units
	* @param lowerLimit - the value of the right limit in units
	*/ 	
	protected VerticalScale()
	{
		super();
		
		this.setMovementType(FIXED);
		
		this.setPrimaryColor(DEFAULT_COLOR);
		this.currentFont=new Font("Serif", Font.PLAIN, 10);
		
		this.setErasable(false);
		
	}
	
	public VerticalScale(String XMLString, Hashtable helpers)
	{
		this();
		
		XMLCoDec tempCodec = new XMLCoDec(XMLString);
		tempCodec.shaveXMLTag();
		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor",DEFAULT_COLOR));
		this.setUniqueID(tempCodec.getParameterAsInt("uniqueID"));
		this.storeLinkIDs(tempCodec);
	}


	/**
	* Method to delete this object from its parent display.
	*/
	public void delete()
	{
		((SpectraDisplay)(this.getParentEntity())).hasVScale(false);
	}

	public void checkSizeAndPosition()
	{
		this.currentUpperLimit=((SpectraDisplay)this.getParentEntity()).getCurrentTopLimit();
		this.currentLowerLimit=((SpectraDisplay)this.getParentEntity()).getCurrentBottomLimit();
		
		//System.out.println("Vertical scale: "+this.currentUpperLimit+" - "+this.currentLowerLimit);
		
		double tempWidth=0;
		double tempHeight=0;
		double tempXPos=0;
		double tempYPos=0;;
		if (this.getParentEntity() != null)
		{
			SpectraDisplay parentDisplay=(SpectraDisplay)this.getParentEntity();
			
			if (!parentDisplay.is2D() && parentDisplay.getNbSpectra() != 1 && !parentDisplay.isAbsoluteYScale()) {
				this.currentUpperLimit = this.currentLowerLimit; // prevents the scale from drawing the labels
			}
			
			if (parentDisplay.hasHScale())
			{
				if (parentDisplay.is2D())
				{
					tempHeight=parentDisplay.getHeight()*(1-3*SpectraDisplay.SCALE_RELATIVE_SIZE);
					tempYPos=parentDisplay.getHeight()*2*SpectraDisplay.SCALE_RELATIVE_SIZE;
				}
				else
					tempHeight=parentDisplay.getHeight()*(1-SpectraDisplay.SCALE_RELATIVE_SIZE);
//				tempYPos=parentDisplay.getHeight()*(1-SpectraDisplay.SCALE_RELATIVE_SIZE);
			}
			else
			{
				tempHeight=parentDisplay.getHeight();
			}
			
			if (parentDisplay.hasVScale())
			{
				if (parentDisplay.is2D())
					tempWidth=(2*SpectraDisplay.SCALE_RELATIVE_SIZE)*parentDisplay.getWidth();
				else
					tempWidth=(SpectraDisplay.SCALE_RELATIVE_SIZE)*parentDisplay.getWidth();
//				tempXPos=parentDisplay.getWidth()*SpectraDisplay.SCALE_RELATIVE_SIZE;
			}
			
			this.setSize(tempWidth, tempHeight);
			this.setLocation(tempXPos, tempYPos);
			
			int fontSize=(int)(Math.min(tempWidth/4.5, 12));
			
			if (fontSize > 12) fontSize=12;
			if(fontSize <  10) fontSize=10;
			
			this.currentFont= new Font("Serif", Font.PLAIN,fontSize );
			this.refreshSensitiveArea();
			
		}
	}

	/**
	* Paints this object
	*/
	public void paint(Graphics2D g)
	{
		if (this.getWidth() != 0)
		{
//			g.setTransform(this.getGlobalTransform());
			
	        g.setFont(currentFont);
			g.setColor(this.getPrimaryColor());
			

			g.setStroke(this.getInteractiveSurface().getNarrowStroke());
			if (isSelected()) {
				g.fillRect((int)this.getWidth()-3, -3, 7, 7);
				g.fillRect((int)this.getWidth()-3, (int)this.getHeight()-3, 7,7);
			} else if (isMouseover()) {
				g.fillRect((int)this.getWidth()-2, -2, 5, 5);
				g.fillRect((int)this.getWidth()-2, (int)this.getHeight()-2, 5,5);
			}
	

//			if (this.currentLowerLimit != this.currentUpperLimit)
				g.draw(new Line2D.Double(this.getWidth(), 0, this.getWidth(), this.getHeight()));
			
			double limitDifference=currentUpperLimit-currentLowerLimit;
			
			if (limitDifference != 0)
			{
				double tempDifference=Math.abs(limitDifference);
				
				int digitCount=0;
				if (tempDifference > 1)
				{
					while (tempDifference > 1)
					{
						digitCount++;
						tempDifference/=10;
					}
					digitCount--;
				}
				else if (tempDifference < 1)
				{
					while (tempDifference < 1)
					{
						digitCount--;
						tempDifference*=10;
					}
				}
		
				double firstLabel, lastLabel;		
				if (currentUpperLimit > currentLowerLimit)
				{
					firstLabel=(int)Math.ceil(currentUpperLimit/Math.pow(10, digitCount))*Math.pow(10, digitCount);
					lastLabel=(int)Math.floor(currentLowerLimit/Math.pow(10, digitCount))*Math.pow(10, digitCount);
				}
				else
				{
					firstLabel=(int)Math.floor(currentUpperLimit/Math.pow(10, digitCount))*Math.pow(10, digitCount);
					lastLabel=(int)Math.ceil(currentLowerLimit/Math.pow(10, digitCount))*Math.pow(10, digitCount);
				}
		
				int	relevantDigit=digitCount-2;
				int labelSize=0;
				double labelIncrement=0;
				double smallLabelIncrement=0;
				
				String tempString;
				int dotPosition=0;
				
				DecimalFormat newFormat = new DecimalFormat();
				newFormat.applyPattern("#0.00000000");
				
				tempString=newFormat.format(currentUpperLimit);
	
				dotPosition=tempString.indexOf(newFormat.getDecimalFormatSymbols().getDecimalSeparator());
	
				do
				{
					labelIncrement=Math.pow(10, relevantDigit);
					
					if (relevantDigit >= 0) 
					{
	//					labelSize=g.getFontMetrics().stringWidth(tempString.substring(0,dotPosition));
						labelSize=g.getFontMetrics().getHeight();//(tempString.substring(0,dotPosition));
					}
					else
					{
	//					labelSize=g.getFontMetrics().stringWidth(tempString.substring(0,dotPosition-relevantDigit+1));
						labelSize=g.getFontMetrics().getHeight();//(tempString.substring(0,dotPosition));
					}
					relevantDigit++;				
				} while (!((Math.abs(unitsToPixelsV(firstLabel)-unitsToPixelsV(firstLabel+Math.pow(10, relevantDigit-1)))) > labelSize));
				relevantDigit--;
			
				if (firstLabel < lastLabel) labelIncrement=-labelIncrement;
					
				smallLabelIncrement=labelIncrement/10;
				
				if (Math.abs(unitsToPixelsV(firstLabel)-unitsToPixelsV(firstLabel+smallLabelIncrement)) < 15)
					smallLabelIncrement*=2;
				if (Math.abs(unitsToPixelsV(firstLabel)-unitsToPixelsV(firstLabel+smallLabelIncrement)) < 15)
					smallLabelIncrement*=2.5;
				if (Math.abs(unitsToPixelsV(firstLabel)-unitsToPixelsV(firstLabel+smallLabelIncrement)) < 15)
					smallLabelIncrement*=2;
		
				double labelValue;
			
				for (int label=0; label < (int)Math.ceil(((firstLabel-lastLabel)/smallLabelIncrement)); label++)
				{
					labelValue=firstLabel-label*smallLabelIncrement;
					drawXLabelSmall(labelValue, g);
				}
				
				
				for (int label=0; label < (int)Math.ceil(((firstLabel-lastLabel)/labelIncrement)); label++)
				{
					labelValue=firstLabel-label*labelIncrement;
					dotPosition=(String.valueOf(labelValue)).indexOf(".");
								
					if (relevantDigit >= 0) 
					{
						tempString=(String.valueOf(labelValue)).substring(0,dotPosition);
					}
					else 
					{
						tempString=(newFormat.format(labelValue)).substring(0,dotPosition-relevantDigit+1);
					}
					drawXLabel(labelValue, tempString, g);
				}
			}
		}
	}
	
	private void drawXLabel(double labelValue, String labelString, Graphics2D g)
	{
		int fontHalfHeight=g.getFont().getSize()/2;
		
		int stringWidth;
		if ((labelValue > Math.min(currentLowerLimit, currentUpperLimit)) && (labelValue < Math.max(currentUpperLimit, currentLowerLimit)) )
		{
			g.draw(new Line2D.Double(this.getWidth(),unitsToPixelsV(labelValue), this.getWidth()-10,unitsToPixelsV(labelValue)));

			stringWidth=g.getFontMetrics().stringWidth(labelString);
			g.drawString(labelString,(float)(this.getWidth()-10-stringWidth),(float)(unitsToPixelsV(labelValue)+fontHalfHeight));
			
		}
	}
	
	private void drawXLabelSmall(double labelValue, Graphics2D g)
	{
		if ((labelValue > Math.min(currentLowerLimit, currentUpperLimit)) && (labelValue < Math.max(currentUpperLimit, currentLowerLimit)) )
		{
			g.draw(new Line2D.Double(this.getWidth(), unitsToPixelsV(labelValue),this.getWidth()-5,unitsToPixelsV(labelValue)));
		}
	}
	
	/**
	* Method to convert a value to units to the correspondent local coordinate in pixels.
	* @param inValue - value to be converted.
	*/
	private int unitsToPixelsV(double inValue)
	{
		return (int)((currentUpperLimit-inValue)*this.getHeight()/(currentUpperLimit-currentLowerLimit));
	}


	public String getOverMessage()
	{
		try {
			return ((SpectraDisplay)this.getInteractiveSurface().getActiveDisplay()).getAllSpectra().get(0).getSpectraData().getYUnits();
		} catch (Exception e) {
			return "VerticalScale";
		}
	}


	public String getClickedMessage()
	{
		return getOverMessage();
	}
	
	public String getXmlTag(Hashtable xmlProperties)
	{
		XMLCoDec tempCodec=new XMLCoDec();
		this.addLinkXMLElements(tempCodec);
		tempCodec.addParameter("primaryColor", this.getPrimaryColor().getRed()
				+ "," + this.getPrimaryColor().getGreen() + ","
				+ this.getPrimaryColor().getBlue());

		String tempTag="<nemo.VerticalScale ";
		tempTag+=tempCodec.encodeParameters();
		tempTag+=">\r\n";

		tempTag+="</nemo.VerticalScale>\r\n";

		
		return tempTag;
	}
}