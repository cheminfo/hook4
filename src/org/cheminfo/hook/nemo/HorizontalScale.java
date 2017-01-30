package org.cheminfo.hook.nemo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.util.Hashtable;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.util.XMLCoDec;

public class HorizontalScale extends BasicEntity
{
	private double currentLeftLimit, currentRightLimit;
	private Font currentFont;
	
	private final static Color DEFAULT_COLOR=Color.blue;
	
	/**
	* Constructs an HorizontalScale object
	* @param width - defines the width in pixel for this object
	* @param height - defines the height in pixel for this object
	* @param leftLimit - the value of the left limit in units
	* @param rightLimit - the value of the right limit in units
	*/ 	
	public HorizontalScale()
	{
		super();
		
		this.setMovementType(FIXED);
		
		this.setPrimaryColor(DEFAULT_COLOR);
		this.currentFont=new Font("Serif", Font.PLAIN, 10);
		
		this.setErasable(false);
	}
	
	public HorizontalScale(String XMLString, Hashtable helpers)
	{
		this();
		
		XMLCoDec tempCodec = new XMLCoDec(XMLString);
		tempCodec.shaveXMLTag();
		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor",DEFAULT_COLOR));
		this.setUniqueID(tempCodec.getParameterAsInt("uniqueID"));
		this.storeLinkIDs(tempCodec);
	}
	
	/**
	* Method to set new left and right limits
	* @param newLeftLimit - value for the new left limit.
	* @param newRightLimit - value for the new right limit.
	*/
	protected void setCurrentLimits(double newLeftLimit, double newRightLimit)
	{
		currentLeftLimit=newLeftLimit;
		currentRightLimit=newRightLimit;
	}

	/**
	* Method to delete this object from its parent display. In this case it only hides it.
	*/
	public void delete()
	{
		((SpectraDisplay)(this.getParentEntity())).hasHScale(false);
	}
	
	public void checkSizeAndPosition()
	{
		this.currentRightLimit=((SpectraDisplay)this.getParentEntity()).getCurrentRightLimit();
		this.currentLeftLimit=((SpectraDisplay)this.getParentEntity()).getCurrentLeftLimit();
		
		double tempWidth=0;
		double tempHeight=0;
		double tempXPos=0;
		double tempYPos=0;;
		if (this.getParentEntity() != null)
		{

			SpectraDisplay parentDisplay=(SpectraDisplay)this.getParentEntity();
			if (parentDisplay.hasHScale() && parentDisplay.getCurrentLeftLimit() != parentDisplay.getCurrentRightLimit())
			{
				tempHeight=parentDisplay.getHeight()*SpectraDisplay.SCALE_RELATIVE_SIZE;
				tempYPos=parentDisplay.getHeight()*(1-SpectraDisplay.SCALE_RELATIVE_SIZE);
				
				if(tempHeight<25){
					tempHeight=25;
					tempYPos-=5;
				}
			}
		
			if (parentDisplay.hasVScale())
			{
				if (parentDisplay.is2D())
				{
					tempWidth=(1-2*SpectraDisplay.SCALE_RELATIVE_SIZE)*parentDisplay.getWidth();
					tempXPos=parentDisplay.getWidth()*2*SpectraDisplay.SCALE_RELATIVE_SIZE;
				}
				else
				{
					tempWidth=(1-SpectraDisplay.SCALE_RELATIVE_SIZE)*parentDisplay.getWidth();
					tempXPos=parentDisplay.getWidth()*SpectraDisplay.SCALE_RELATIVE_SIZE;
				}
			}
			else
			{
				tempWidth=parentDisplay.getWidth();
				tempXPos=0;
			}
			
			this.setSize(tempWidth, tempHeight);
			this.setLocation(tempXPos, tempYPos);
			
			int fontSize=(int)(tempHeight/3);
			
			if (fontSize > 12) fontSize=12;
			if(fontSize <  10) fontSize=10;
			this.currentFont= new Font("Serif", Font.PLAIN, fontSize);
			this.refreshSensitiveArea();
		}
	}
	
	/**
	* Paints this object
	*/
	public void paint(Graphics2D g)
	{
		if (this.getHeight() != 0)
		{
	        g.setFont(currentFont);
			g.setColor(this.getPrimaryColor());
			

			g.setStroke(this.getInteractiveSurface().getNarrowStroke());
			if (isSelected())
			{
				g.fillRect(0, -3, 7, 7);
				g.fillRect((int)this.getWidth()-7, -3, 7,7);
			}
			else if (isMouseover())
			{
				g.fillRect(0, -2, 5, 5);
				g.fillRect((int)this.getWidth()-5, -2, 5,5);
			}


				
			g.draw(new Line2D.Double(0,0,this.getWidth(),0));
			
			double limitDifference=currentLeftLimit-currentRightLimit;

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
				if (currentLeftLimit > currentRightLimit)
				{
					firstLabel=(int)Math.ceil(currentLeftLimit/Math.pow(10, digitCount))*Math.pow(10, digitCount);
					lastLabel=(int)Math.floor(currentRightLimit/Math.pow(10, digitCount))*Math.pow(10, digitCount);
				}
				else
				{
					firstLabel=(int)Math.floor(currentLeftLimit/Math.pow(10, digitCount))*Math.pow(10, digitCount);
					lastLabel=(int)Math.ceil(currentRightLimit/Math.pow(10, digitCount))*Math.pow(10, digitCount);
				}
	
				int	relevantDigit=digitCount-2;
				int labelSize=0;
				double labelIncrement=0;
				double smallLabelIncrement=0;
				
				String tempString;
				int dotPosition=0;
				
				DecimalFormat newFormat = new DecimalFormat();
				newFormat.applyPattern("#0.00000000000");
				
				tempString=newFormat.format(currentLeftLimit);
	
				dotPosition=tempString.indexOf(newFormat.getDecimalFormatSymbols().getDecimalSeparator());
	
				boolean flag=false;
				
				do
				{
					labelIncrement=Math.pow(10, relevantDigit);
					
					if (relevantDigit >= 0) 
					{
						labelSize=g.getFontMetrics().stringWidth(tempString.substring(0,dotPosition));
					}
					else
					{
						if (dotPosition-relevantDigit+1 < tempString.length())
						{
							labelSize=g.getFontMetrics().stringWidth(tempString.substring(0,dotPosition-relevantDigit+1));
						}
						else
						{
							labelSize=g.getFontMetrics().stringWidth(tempString);
							flag=true;
						}
					}
					relevantDigit++;				
				} while (!((Math.abs(unitsToPixels(firstLabel)-unitsToPixels(firstLabel+Math.pow(10, relevantDigit-1)))) > labelSize+8) && !flag);
				relevantDigit--;
			
				if (firstLabel < lastLabel) labelIncrement=-labelIncrement;
					
				smallLabelIncrement=labelIncrement/10;
				
				if (Math.abs(unitsToPixels(firstLabel)-unitsToPixels(firstLabel+smallLabelIncrement)) < 15)
					smallLabelIncrement*=2;
				if (Math.abs(unitsToPixels(firstLabel)-unitsToPixels(firstLabel+smallLabelIncrement)) < 15)
					smallLabelIncrement*=2.5;
				if (Math.abs(unitsToPixels(firstLabel)-unitsToPixels(firstLabel+smallLabelIncrement)) < 15)
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
		
		int stringWidth;
		if ((labelValue>Math.min(currentRightLimit, currentLeftLimit)) && (labelValue < Math.max(currentLeftLimit, currentRightLimit)))
		{
			g.draw(new Line2D.Double(unitsToPixels(labelValue),1,unitsToPixels(labelValue), this.getHeight()/6));

			stringWidth=g.getFontMetrics().stringWidth(labelString);
			g.drawString(labelString,unitsToPixels(labelValue)-stringWidth/2,(float)(this.getHeight()/2.2));
			
		}
	}
	
	private void drawXLabelSmall(double labelValue, Graphics2D g)
	{
		if ((labelValue>Math.min(currentRightLimit, currentLeftLimit)) && (labelValue < Math.max(currentLeftLimit, currentRightLimit)))
		{
			g.draw(new Line2D.Double(unitsToPixels(labelValue),0,unitsToPixels(labelValue), this.getHeight()/10));
		}
	}
	
	/**
	* Method to convert a value to units to the correspondent local coordinate in pixels.
	* @param inValue - value to be converted.
	*/
	int unitsToPixels(double inValue)
	{
		return (int)((currentLeftLimit-inValue)*this.getWidth()/(currentLeftLimit-currentRightLimit));
	}


	public String getOverMessage() {
		try {
			return ((SpectraDisplay)this.getInteractiveSurface().getActiveDisplay()).getAllSpectra().get(0).getSpectraData().getXUnits();
		} catch (Exception e) {
			return "HorizontalScale";
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

		
		
		String tempTag="<nemo.HorizontalScale ";
		tempTag+=tempCodec.encodeParameters();
		tempTag+=">\r\n";

		tempTag+="</nemo.HorizontalScale>\r\n";

		
		return tempTag;
	}

	public Font getCurrentFont() {
		return currentFont;
	}

}
	
	