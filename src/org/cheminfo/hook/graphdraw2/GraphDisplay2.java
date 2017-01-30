package org.cheminfo.hook.graphdraw2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Hashtable;

import org.cheminfo.hook.framework.BasicDisplay;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.graphics.TextEntity;
import org.cheminfo.hook.util.HookStatistics;
import org.cheminfo.hook.util.XMLCoDec;

public class GraphDisplay2 extends BasicDisplay
{
	private double tempRelX, tempRelY, tempRelWidth, tempRelHeight;
	private double currentTop, currentBottom, currentLeft, currentRight;
	private double fulloutTop, fulloutBottom, fulloutLeft, fulloutRight;
	
	private boolean hasHAxis, hasVAxis;
	private GraphAxis hAxis, vAxis;
	public final static double AXIS_RELATIVE_SIZE	=	0.1;

	private int dataAspect;

	private String tempHTitle, tempVTitle;
	
	public GraphDisplay2()
	{
		this.setPrimaryColor(Color.red);
		this.setSecondaryColor(Color.white);
		this.hAxis=null;
		this.vAxis=null;
		this.hasHAxis=true;
		this.hasVAxis=true;

		this.tempHTitle="";
		this.tempVTitle="";
		
		this.dataAspect=XYPointEntity.DATA_ASPECT_POINT;
	}

	public GraphDisplay2(String XMLTag, Hashtable helpers)
	{
		XMLCoDec tempCodec = new XMLCoDec(XMLTag);
		tempCodec.shaveXMLTag();

		double parentWidth, parentHeight;
		
		if (this.getParentEntity() instanceof BasicEntity)
		{
			// Is this ever called? In theory, there is no way an entity can have a parent while being constructed...
			BasicEntity parentEntity=(BasicEntity)this.getParentEntity();
			parentWidth=parentEntity.getWidth();
			parentHeight=parentEntity.getHeight();

			this.setSize(parentWidth*tempCodec.getParameterAsDouble("relWidth"), parentHeight*tempCodec.getParameterAsDouble("relHeight"));
			this.setLocation(parentWidth*tempCodec.getParameterAsDouble("relX"), parentHeight*tempCodec.getParameterAsDouble("relY"));
		}
		
		this.tempRelX=tempCodec.getParameterAsDouble("relX");
		this.tempRelY=tempCodec.getParameterAsDouble("relY");
		this.tempRelWidth=tempCodec.getParameterAsDouble("relWidth");
		this.tempRelHeight=tempCodec.getParameterAsDouble("relHeight");
		
		
		if (this.getEntitiesCount() != 0)	this.removeAll();

		this.setUniqueID(tempCodec.getParameterAsInt("uniqueID"));
		this.storeLinkIDs(tempCodec);
		
		
		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor"));
		this.hasHAxis=tempCodec.getParameterAsBoolean("hAxis");
		if (this.hasHAxis)
			this.tempHTitle=tempCodec.getParameterAsString("hTitle");
		else
			this.tempHTitle="";
		
		this.hasVAxis=tempCodec.getParameterAsBoolean("vAxis");
		if (this.hasVAxis)
			this.tempVTitle=tempCodec.getParameterAsString("vTitle");
		else
			this.tempVTitle="";
		
		this.setMovementType(tempCodec.getParameterAsInt("movementType"));
		this.setCurrentLimits(tempCodec.getParameterAsDouble("leftLimit"), tempCodec.getParameterAsDouble("rightLimit"), tempCodec.getParameterAsDouble("topLimit"), tempCodec.getParameterAsDouble("bottomLimit"));

		int elements=tempCodec.getRootElementsCount();

		for (int elem=0; elem < elements; elem++)
		{
			XMLCoDec tempCodec2=new XMLCoDec(tempCodec.readXMLTag());
			
			tempCodec2.shaveXMLTag();
			try
			{
				Class entityClass=Class.forName("org.cheminfo.hook."+tempCodec2.getParameterAsString("tagName").trim());
				Class[] parameterClasses = {String.class, Hashtable.class};
				java.lang.reflect.Constructor entityConstructor=entityClass.getConstructor(parameterClasses);
	
				Object[] parameters = {tempCodec.popXMLTag(), helpers};
				this.addEntity((BasicEntity)entityConstructor.newInstance(parameters));
			} catch (Exception e) {e.printStackTrace();}
		}
		this.checkFulloutLimits();
		
	}
	
	public void addNotify()
	{

		if (this.getParentEntity() == null && this.getWidth() == 0)
		{
//			this.setSize(this.getInteractiveSurface().getWidth(), this.getInteractiveSurface().getHeight());
			this.setSize(this.tempRelWidth*this.getInteractiveSurface().getWidth(), this.tempRelHeight*this.getInteractiveSurface().getHeight());
			this.setLocation(this.tempRelX*this.getInteractiveSurface().getWidth(), this.tempRelY*this.getInteractiveSurface().getHeight());
		}

		if (this.hasHAxis)
		{
			this.hAxis=new GraphAxis(GraphAxis.HORIZONTAL);
			this.hAxis.setTitle(this.tempHTitle);
			this.addEntity(hAxis);
		}
		if (this.hasVAxis)
		{
			this.vAxis=new GraphAxis(GraphAxis.VERTICAL);
			this.vAxis.setTitle(this.tempVTitle);
			this.addEntity(this.vAxis);
		}

/*		this.fulloutBottom=0;
		this.fulloutTop=0;
		this.fulloutRight=0;
		this.fulloutLeft=0;
*/
		this.checkSizeAndPosition();
		this.refreshSensitiveArea();
	}
	
	public void setCurrentLimits(double newLeft, double newRight, double newTop, double newBottom)
	{
		this.currentLeft=newLeft;
		this.currentRight=newRight;
		this.currentTop=newTop;
		this.currentBottom=newBottom;
		
		this.checkSizeAndPosition();
		
		if (this.getInteractiveSurface() != null)
			this.getInteractiveSurface().repaint();
	}

	public void setFulloutLimits(double newLeft, double newRight, double newTop, double newBottom)
	{
		this.fulloutLeft=newLeft;
		this.fulloutRight=newRight;
		this.fulloutTop=newTop;
		this.fulloutBottom=newBottom;
	}
	
	public void checkFulloutLimits()
	{
		SerieEntity tempSerie;
		boolean firstSerie=true;
		for (int ent=0; ent < this.getEntitiesCount(); ent++)
		{
			if (this.getEntity(ent) instanceof SerieEntity)
			{
				tempSerie=(SerieEntity)this.getEntity(ent);
				
				if (firstSerie)
				{
					this.fulloutBottom=tempSerie.getBottom();
					this.fulloutTop=tempSerie.getTop();
					this.fulloutLeft=tempSerie.getLeft();
					this.fulloutRight=tempSerie.getRight();
					firstSerie=false;
				}
				else
				{
					if ( tempSerie.getLeft() < this.fulloutLeft) this.fulloutLeft=tempSerie.getLeft();
					if ( tempSerie.getRight() > this.fulloutRight) this.fulloutRight=tempSerie.getRight();
					if ( tempSerie.getBottom() < this.fulloutBottom) this.fulloutBottom=tempSerie.getBottom();
					if ( tempSerie.getTop() > this.fulloutTop) this.fulloutTop=tempSerie.getTop();
				}
			}
		}
	}
	
	public void fullout()
	{
		this.checkFulloutLimits();
		this.setCurrentLimits(this.fulloutLeft-Math.abs(this.fulloutRight-this.fulloutLeft)/20, this.fulloutRight+Math.abs(this.fulloutRight-this.fulloutLeft)/20, this.fulloutTop+Math.abs(this.fulloutTop-this.fulloutBottom)/20, this.fulloutBottom-Math.abs(this.fulloutTop-this.fulloutBottom)/20);
	}
	
	public double getLeftLimit()
	{
		return this.currentLeft;
	}
	
	public double getRightLimit()
	{
		return this.currentRight;
	}

	public double getTopLimit()
	{
		return this.currentTop;
	}
	
	public double getBottomLimit()
	{
		return this.currentBottom;
	}
	
	public boolean hasHAxis()
	{
		return this.hasHAxis;
	}
	
	public boolean hasVAxis()
	{
		return this.hasVAxis;
	}

	public double absolutePixelsToUnitsH(double xPixel)
	{
		double actualWidth=this.getWidth();
		
		xPixel-=this.getLocation().x;
		
		if (this.hasVAxis)
		{
			xPixel-=this.getWidth()*GraphDisplay2.AXIS_RELATIVE_SIZE;
			actualWidth=this.getWidth()*(1-2*GraphDisplay2.AXIS_RELATIVE_SIZE);
		}
		
		return this.currentLeft+(this.currentRight-this.currentLeft)*xPixel/actualWidth;
	}

	public double absolutePixelsToUnitsV(double yPixel)
	{
		double actualHeight=this.getHeight();
		
		yPixel-=this.getLocation().y;
		
		if (this.hasHAxis)
		{
			yPixel-=this.getHeight()*GraphDisplay2.AXIS_RELATIVE_SIZE;
			actualHeight=this.getHeight()*(1-2*GraphDisplay2.AXIS_RELATIVE_SIZE);
		}
		
		return this.currentTop+(this.currentBottom-this.currentTop)*yPixel/actualHeight;
	}
	
	public double unitsPerPixelH()
	{
		double tempWidth=this.getWidth();
		if (this.hasVAxis)
			tempWidth=this.getWidth()*(1-GraphDisplay2.AXIS_RELATIVE_SIZE);

		return (this.currentLeft-this.currentRight)/tempWidth;
	}

	public double unitsPerPixelV()
	{
		double tempHeight=this.getHeight();
		if (this.hasHAxis)
			tempHeight=this.getHeight()*(1-GraphDisplay2.AXIS_RELATIVE_SIZE);

		return (this.currentTop-this.currentBottom)/tempHeight;
	}
	
	public void clear()
	{
		int i=this.getEntitiesCount()-1;
		for (int ent=i; ent >=0; ent--)
		{
			if (this.getEntity(ent) instanceof SerieEntity)
			{
				this.getEntity(ent).delete();
				this.remove(ent);
			}
		}

	}

	/**
	 * Sets the aspect of the data series elements (points, bars, ...). Must be set before the series are added. Uses the XYPointEntity statics DATA_ASPECT_POINT, DATA_ASPECT_BAR, DATA_ASPECT_FILLGRID.
	 * @param newDataAspect
	 */
	public void setDataAspect(int newDataAspect)
	{
		this.dataAspect=newDataAspect;
	}
	
	/**
	 * Returns the current data aspect
	 * @return
	 */
	public int getDataAspect()
	{
		return this.dataAspect;
	}

	public void addSerie(SerieData serieData)
	{
		if (serieData.getDataAspect() == 0)
			serieData.setDataAspect(this.dataAspect);
		
		this.hAxis.setTitle(serieData.getXUnits());
		this.vAxis.setTitle(serieData.getYUnits());
		this.addEntity(new SerieEntity(serieData));
		
		this.checkFulloutLimits();
	}
	
	public void setTrendlineForSerie(String serieName, Trendline trendline)
	{
		this.getSerieByName(serieName).setTrendline(trendline);
	}
	
	public SerieEntity getSerieByName(String name)
	{
		for (int ent=0; ent < this.getEntitiesCount(); ent++)
		{
			if (this.getEntity(ent) instanceof SerieEntity)
			{
				if ( ((SerieEntity)this.getEntity(ent)).getName().compareTo(name) == 0 )
					return (SerieEntity)this.getEntity(ent);
			}
		}
		
		return null;
	}
	
	public void setSerieVisibility(String serieName, boolean visible)
	{
		this.getSerieByName(serieName).setVisible(visible);
	}
	
	public void displayZedFactor(String serie1Name, String serie2Name)
	{
		SerieData serie1=null;
		SerieData serie2=null;
		
		SerieEntity tempSerieEntity;
		TextEntity textEntity=null;;
		for(int ent=0; ent < this.getEntitiesCount(); ent++)
		{
			if (this.getEntity(ent) instanceof SerieEntity)
			{
				tempSerieEntity=(SerieEntity)this.getEntity(ent);
				
				if (tempSerieEntity.getName().compareTo(serie1Name) == 0)
					serie1=tempSerieEntity.getSerieData();
				if (tempSerieEntity.getName().compareTo(serie2Name) == 0)
					serie2=tempSerieEntity.getSerieData();
			}
			else if (this.getEntity(ent) instanceof TextEntity)
				textEntity=(TextEntity)this.getEntity(ent);
		}
		
		if (serie1 != null && serie2 != null)
		{
			DecimalFormat format= new DecimalFormat("#0.000");
			double zed=HookStatistics.zedFactor(serie1.getAsArray(), serie2.getAsArray());
			
			if (textEntity != null)
				this.remove(textEntity);
			
			if (zed < 0)	textEntity=new TextEntity(13, 1);
			else	textEntity=new TextEntity(12, 1);
			
			textEntity.setLocation(0,0);
			
			this.addEntity(textEntity);
			textEntity.setText("Z: "+format.format(zed));
		}
	}

	public void paint(Graphics2D g)
	{
//		g.setClip(new Rectangle2D.Double(0,0,this.getWidth(), this.getHeight()));
		
		if (this.getSecondaryColor() != null)
		{
			g.setColor(this.getSecondaryColor());
			g.fill(new Rectangle2D.Double(0,0,this.getWidth(), this.getHeight()));
		}
		
		if (this.getPrimaryColor() != null)
		{
			g.setColor(this.getPrimaryColor());
			
			if (this.isSelected() || this.isMouseover())
				g.draw(new Rectangle2D.Double(0,0,this.getWidth()-1, this.getHeight()-1));
		}
		
		Point2D.Double contactP=this.getInteractiveSurface().getContactPoint();
		Point2D.Double releaseP=this.getInteractiveSurface().getReleasePoint();

		switch (this.getCursorType())
		{
			case BasicDisplay.RECT:
				g.draw(new Rectangle2D.Double(Math.min(contactP.x, releaseP.x), Math.min(contactP.y, releaseP.y), Math.abs(contactP.x-releaseP.x), Math.abs(contactP.y-releaseP.y)));
				break;
				
			default:
				break;
				
		}
		
		super.paint(g);	
		g.setTransform(this.getInteractiveSurface().getGlobalTransform());
//		g.setClip(new Rectangle2D.Double(0,0,this.getInteractiveSurface().getSize().width, this.getInteractiveSurface().getSize().height));
	}
	
	public String getXmlTag(Hashtable xmlProperties)
	{
		XMLCoDec tempCodec = new XMLCoDec();
		String tempTag="";
		
		Point2D.Double parentSize=new Point2D.Double();
		
		if (this.getParentEntity() instanceof BasicEntity)
		{
			parentSize.x=this.getParentEntity().getWidth();
			parentSize.y=this.getParentEntity().getHeight();
		}
		else
		{
			parentSize.x=this.getInteractiveSurface().getSize().width;
			parentSize.y=this.getInteractiveSurface().getSize().height;
		}
		
		tempCodec.addParameter("relX", new Double(this.getLocation().x/parentSize.x));
		tempCodec.addParameter("relY", new Double(this.getLocation().y/parentSize.y));
		tempCodec.addParameter("relWidth", new Double(this.getWidth()/parentSize.x));
		tempCodec.addParameter("relHeight", new Double(this.getHeight()/parentSize.y));
		tempCodec.addParameter("hAxis", new Boolean(this.hasHAxis));
		tempCodec.addParameter("vAxis", new Boolean(this.hasVAxis));
		tempCodec.addParameter("primaryColor", this.getPrimaryColor().getRed()+","+this.getPrimaryColor().getGreen()+","+this.getPrimaryColor().getBlue());
		tempCodec.addParameter("leftLimit", new Double(this.getLeftLimit()));
		tempCodec.addParameter("rightLimit", new Double(this.getRightLimit()));
		tempCodec.addParameter("topLimit", new Double(this.getTopLimit()));
		tempCodec.addParameter("bottomLimit", new Double(this.getBottomLimit()));
		tempCodec.addParameter("movementType", new Integer(this.getMovementType()));
		
		if (this.hasHAxis)
			tempCodec.addParameter("hTitle", this.hAxis.getTitle());
		if (this.hasVAxis)
			tempCodec.addParameter("vTitle", this.vAxis.getTitle());

		this.addLinkXMLElements(tempCodec);
		
		tempTag+="<graphdraw2.GraphDisplay2 ";
		tempTag+=tempCodec.encodeParameters();
		tempTag+=">\r\n";

		for (int ent=0; ent < this.getEntitiesCount(); ent++)
		{
			tempTag+=((BasicEntity)this.getEntity(ent)).getXmlTag(xmlProperties);
		}
		tempTag+="</graphdraw2.GraphDisplay2>\r\n";
	
		return tempTag;
	}
}