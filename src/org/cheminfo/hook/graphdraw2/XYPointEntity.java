package org.cheminfo.hook.graphdraw2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.util.QuadTreeElement;
import org.cheminfo.hook.util.XMLCoDec;

public class XYPointEntity extends BasicEntity implements QuadTreeElement
{
	public final static int DATA_ASPECT_POINT		=	100;
	public final static int DATA_ASPECT_BAR			=	101;
	public final static int DATA_ASPECT_FILLGRID	=	102;

	private double x, y, errX, errY;
	private String description;
	private Object reference;
	private int dataAspect;
	
	private BufferedImage image;
	
	public XYPointEntity(double x, double y, double errX, double errY, String description, Object reference, Color color, int dataAspect)
	{
		super(0,0);
		
		this.x=x;
		this.y=y;
		this.errX=errX;
		this.errY=errY;
		
		this.reference=reference;
		this.description=description;
		
		this.dataAspect=dataAspect;

		this.setPrimaryColor(color);
		this.setSecondaryColor(Color.black);
		
/*		this.image=new BufferedImage(19,19, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2=(Graphics2D)image.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
//		g2.setColor(color);
//		g2.fill(new Ellipse2D.Double(2,2, 16,16));
		g2.setStroke(new BasicStroke(2));
		g2.setColor(color);
		g2.draw(new Ellipse2D.Double(2,2, 16,16));
*/		
	}

	public XYPointEntity(double x, double y, double errX, double errY, String description, Object reference, Color color)
	{
		this(x, y, errX, errY, description, reference, color, XYPointEntity.DATA_ASPECT_POINT);
	}
	
	public XYPointEntity(String XMLTag, Hashtable helpers)
	{
		XMLCoDec tempCodec = new XMLCoDec(XMLTag);
		tempCodec.shaveXMLTag();

		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor"));
		this.setSecondaryColor(tempCodec.getParameterAsColor("secondaryColor"));
		
		this.x=tempCodec.getParameterAsDouble("x");
		this.y=tempCodec.getParameterAsDouble("y");
		this.errX=tempCodec.getParameterAsDouble("errX");
		this.errY=tempCodec.getParameterAsDouble("errY");
		this.dataAspect=tempCodec.getParameterAsInt("dataAspect");
		this.setUniqueID(tempCodec.getParameterAsInt("uniqueID"));
		this.storeLinkIDs(tempCodec);
		
		int nbElements=tempCodec.getRootElementsCount();

		for (int elem=0; elem < nbElements; elem++)
		{
			XMLCoDec tempCodec2=new XMLCoDec(tempCodec.readXMLTag());
			
			tempCodec2.shaveXMLTag();
			try
			{
				Class entityClass=Class.forName("org.cheminfo.hook."+tempCodec2.getParameterAsString("tagName").trim());
				Class[] parameterClasses = {String.class, Hashtable.class};
				java.lang.reflect.Constructor entityConstructor=entityClass.getConstructor(parameterClasses);
	
				Object[] parameters = {tempCodec.popXMLTag(), helpers};
				this.reference=((Object)entityConstructor.newInstance(parameters));
			} catch (ClassNotFoundException ex1) {System.out.println("SerieEntity XML constructor e: "+ex1);}
			catch (IllegalAccessException ex2) {System.out.println("SerieEntity XML constructor e: "+ex2);}
			catch (InstantiationException ex3) {System.out.println("SerieEntity XML constructor e: "+ex3);}
			catch (InvocationTargetException ex4) {System.out.println("SerieEntity XML constructor e: "+ex4+" -> "+ex4.getTargetException());}
			catch (NoSuchMethodException ex5) {System.out.println("SerieEntity XML constructor e: "+ex5);};
		}
	}
	
	public void addNotify()
	{
	}
	
	public String getOverMessage()
	{
		if (this.reference != null)
			return this.reference.toString();
		else
			return this.description;
	}
	
	public String getClickedMessage()
	{
		return this.description;
	}
	
	public void checkSizeAndPosition()
	{
		SerieEntity parentSerie=(SerieEntity)this.getParentEntity();
		GraphDisplay2 parentDisplay=(GraphDisplay2)parentSerie.getParentEntity();
		
		double pixelsPerUnitH=parentSerie.getWidth()/(parentDisplay.getRightLimit()-parentDisplay.getLeftLimit());
		double pixelsPerUnitV=parentSerie.getHeight()/(parentDisplay.getBottomLimit()-parentDisplay.getTopLimit());
		
		double tempXPos=(this.x-parentDisplay.getLeftLimit())*pixelsPerUnitH;
		double tempYPos=(this.y-parentDisplay.getTopLimit())*pixelsPerUnitV;
		
		this.setLocation(tempXPos, tempYPos);
	}

	public void refreshSensitiveArea()
	{
		GraphDisplay2 parentDisplay=null;
		
		if (this.getParentEntity() != null && this.getParentEntity().getParentEntity() != null)
			parentDisplay=(GraphDisplay2)this.getParentEntity().getParentEntity();

		this.setSensitiveArea(new Area(new Rectangle2D.Double(-2,-2,5, 5)));
		if (parentDisplay != null)
		{
/*			if (parentDisplay.getDataAspect() == GraphDisplay.DATA_ASPECT_POINT)
				this.setSensitiveArea(new Area(new Rectangle2D.Double(-2,-2,5, 5)));
			else if (parentDisplay.getDataAspect() == GraphDisplay.DATA_ASPECT_BARS)
			{
*/
			if (this.dataAspect == XYPointEntity.DATA_ASPECT_POINT)
				this.setSensitiveArea(new Area(new Rectangle2D.Double(-2,-2,5, 5)));
			else if (this.dataAspect == XYPointEntity.DATA_ASPECT_BAR)
			{
				if (this.getParentEntity() != null)
				{
					SerieEntity parentSerie=(SerieEntity)this.getParentEntity();
					double pixelsPerUnitV=parentSerie.getHeight()/(parentDisplay.getBottomLimit()-parentDisplay.getTopLimit());
					double pixelsPerUnitH=parentSerie.getWidth()/(parentDisplay.getRightLimit()-parentDisplay.getLeftLimit());
		
					if (this.y*pixelsPerUnitV < 0)
						this.setSensitiveArea((new Area(new Rectangle2D.Double(-pixelsPerUnitH/2+1, 0, pixelsPerUnitH-2, -this.y*pixelsPerUnitV))));
					else
						this.setSensitiveArea(new Area(new Rectangle2D.Double(-pixelsPerUnitH/2+1, -this.y*pixelsPerUnitV, pixelsPerUnitH-2, this.y*pixelsPerUnitV)));
				}
			}
			else if (this.dataAspect == XYPointEntity.DATA_ASPECT_FILLGRID)
			{
				this.setSensitiveArea(new Area(new Rectangle2D.Double(this.errX/parentDisplay.unitsPerPixelH(),  -this.errY/parentDisplay.unitsPerPixelV(), -2*this.errX/parentDisplay.unitsPerPixelH(), 2*this.errY/parentDisplay.unitsPerPixelV())));
			}
		}
	}
	
	public double getX()
	{
		return this.x;
	}
	
	public double getY()
	{
		return this.y;
	}
	
	public void setX(double newX)
	{
		this.x=newX;
	}
	
	public void setY(double newY)
	{
		this.y=newY;
	}
	
	public double getErrX()
	{
		return this.errX;
	}
	
	public double getErrY()
	{
		return this.errY;
	}
	
	public String getDescription()
	{
		return this.description;
	}
	
	public Object getReference()
	{
		return this.reference;
	}
	
	public void paint(Graphics2D g)
	{
/*		GraphDisplay2 parentDisplay=null;
		
		if (this.getParentEntity() != null && this.getParentEntity().getParentEntity() != null)
			parentDisplay=(GraphDisplay2)this.getParentEntity().getParentEntity();

		SerieEntity parentSerie=(SerieEntity)this.getParentEntity();
		double pixelsPerUnitV=parentSerie.getHeight()/(parentDisplay.getBottomLimit()-parentDisplay.getTopLimit());
		double pixelsPerUnitH=parentSerie.getWidth()/(parentDisplay.getRightLimit()-parentDisplay.getLeftLimit());
		
		
		if (this.dataAspect != XYPointEntity.DATA_ASPECT_FILLGRID && this.getSecondaryColor() != null)
		{
			g.setColor(this.getSecondaryColor());
			if (errY > 0)
			{
				g.draw(new Line2D.Double(0, -errY*pixelsPerUnitV, 0, errY*pixelsPerUnitV));
			}
			if (errX > 0)
			{
				g.draw(new Line2D.Double(-errX*pixelsPerUnitH, 0, errX*pixelsPerUnitH, 0));
			}
			
		}
		
		switch (this.dataAspect)
		{
		case XYPointEntity.DATA_ASPECT_POINT:
			if (this.getPrimaryColor() != null)
			{
				g.setColor(this.getPrimaryColor());
				g.fill(new Rectangle2D.Double(-2, -2, 5, 5));
			}
			if (this.getSecondaryColor() != null)
			{
				g.setColor(this.getSecondaryColor());
				g.draw(new Rectangle2D.Double(-2, -2, 5, 5));
				if (this.isMouseover() || this.isSelected())
					g.draw(new Rectangle2D.Double(-3, -3, 7, 7));
			}
			break;
			
		case XYPointEntity.DATA_ASPECT_BAR:
			if (this.getPrimaryColor() != null)
			{
				g.setColor(this.getPrimaryColor());
				if (this.y*pixelsPerUnitV < 0)
					g.fill(new Rectangle2D.Double(-pixelsPerUnitH/2+1, 0, pixelsPerUnitH-2, -this.y*pixelsPerUnitV));
				else
					g.fill(new Rectangle2D.Double(-pixelsPerUnitH/2+1, -this.y*pixelsPerUnitV, pixelsPerUnitH-2, this.y*pixelsPerUnitV));
			}
			
			if (this.getSecondaryColor() != null)
			{
				g.setColor(this.getSecondaryColor());
				if (this.y*pixelsPerUnitV < 0)
					g.draw(new Rectangle2D.Double(-pixelsPerUnitH/2+1, 0, pixelsPerUnitH-2, -this.y*pixelsPerUnitV));
				else
					g.draw(new Rectangle2D.Double(-pixelsPerUnitH/2+1, -this.y*pixelsPerUnitV, pixelsPerUnitH-2, this.y*pixelsPerUnitV));
				
				if (this.isMouseover() || this.isSelected())
				{
					if (this.y*pixelsPerUnitV < 0)
						g.draw(new Rectangle2D.Double(-pixelsPerUnitH/2, -1, pixelsPerUnitH, -this.y*pixelsPerUnitV+1));
					else
						g.draw(new Rectangle2D.Double(-pixelsPerUnitH/2, -this.y*pixelsPerUnitV+1, pixelsPerUnitH, this.y*pixelsPerUnitV-1));
				}
			}
			break;
			
		case XYPointEntity.DATA_ASPECT_FILLGRID:
			if (parentDisplay != null)
			{
				g.setColor(this.getPrimaryColor());
				g.fill(new Rectangle2D.Double(this.errX/parentDisplay.unitsPerPixelH(),  -this.errY/parentDisplay.unitsPerPixelV(), -2*this.errX/parentDisplay.unitsPerPixelH(), 2*this.errY/parentDisplay.unitsPerPixelV()));
			}
			break;
		
		default:
			break;
		}
*/	}
	
	public boolean isContained(float queryX1, float queryX2, float queryY1, float queryY2)
	{
		if (this.dataAspect == XYPointEntity.DATA_ASPECT_POINT)
		{
			if ( ( this.x >= Math.min(queryX1, queryX2) && this.x <= Math.max(queryX1, queryX2)
					&& this.y+errY >= Math.min(queryY1, queryY2) && this.y-errY <= Math.max(queryY1, queryY2) )
				)
			{
				return true;
			}
		}
		else if (this.dataAspect == XYPointEntity.DATA_ASPECT_FILLGRID)
		{
			if ( ( this.x >= Math.min(queryX1-this.errX, queryX2-this.errX) && this.x <= Math.max(queryX1+this.errX, queryX2+this.errX)
					&& this.y >= Math.min(queryY1-this.errY, queryY2-this.errY) && this.y <= Math.max(queryY1+this.errY, queryY2+this.errY) )
				)
			{
				return true;
			}
		}
		else if (this.dataAspect == XYPointEntity.DATA_ASPECT_BAR)
		{
			if ( ( this.x >= Math.min(queryX1-this.errX, queryX2-this.errX) && this.x <= Math.max(queryX1+this.errX, queryX2+this.errX)
					&& Math.max(this.y, 0.0) >= Math.min(queryY1-this.errY, queryY2-this.errY) && Math.min(this.y, 0.0) <= Math.max(queryY1+this.errY, queryY2+this.errY) )
				)
			{
				return true;
			}

		}
		
		return false;
	}
	
	public void paint(Graphics2D g, float tileLeft, float tileRight, float tileTop, float tileBottom, float tileWidth, float tileHeight)
	{
		double unitsPerPixelH=(tileLeft-tileRight)/tileWidth;
		double unitsPerPixelV=(tileTop-tileBottom)/tileHeight;

		switch (this.dataAspect)
		{
		case XYPointEntity.DATA_ASPECT_POINT:
			if (this.getSecondaryColor() != null)
			{
				g.setColor(this.getSecondaryColor());
				if (errY > 0)
					g.draw(new Line2D.Double((tileLeft-this.x)*tileWidth/(tileLeft-tileRight), (tileTop-(this.y-errY))*tileHeight/(tileTop-tileBottom), (tileLeft-this.x)*tileWidth/(tileLeft-tileRight), (tileTop-(this.y+errY))*tileHeight/(tileTop-tileBottom)));
				if (errX > 0)
					g.draw(new Line2D.Double(-errX/unitsPerPixelH, 0, errX/unitsPerPixelH, 0));
			}
			
			double xPos=(tileLeft-this.x)*tileWidth/(tileLeft-tileRight);
			double yPos=(tileTop-this.y)*tileHeight/(tileTop-tileBottom);
			
//			g.drawImage(this.image, (int)(xPos-5), (int)(yPos-5), (int)(xPos+5),(int)(yPos+5), 0,0, 19,19, null);
			
			if (this.getPrimaryColor() != null)
			{
				g.setColor(this.getPrimaryColor());
				g.fill(new Rectangle2D.Double( (tileLeft-this.x)*tileWidth/(tileLeft-tileRight)-2, (tileTop-this.y)*tileHeight/(tileTop-tileBottom)-2, 5, 5));
			}
			if (this.getSecondaryColor() != null)
			{
				g.setColor(this.getSecondaryColor());
				g.draw(new Rectangle2D.Double( (tileLeft-this.x)*tileWidth/(tileLeft-tileRight)-2, (tileTop-this.y)*tileHeight/(tileTop-tileBottom)-2, 5, 5));

//				if (this.isMouseover() || this.isSelected())
//					g.draw(new Rectangle2D.Double( (tileLeft-this.x)*tileWidth/(tileLeft-tileRight)-3, (tileTop-this.y)*tileHeight/(tileTop-tileBottom)-3, 7, 7));
			}

			
			break;

		case XYPointEntity.DATA_ASPECT_BAR:
			
			if (this.y >= 0)
			{
				g.setColor(this.getPrimaryColor());
				g.fill(new Rectangle2D.Double( (tileLeft-this.x)*tileWidth/(tileLeft-tileRight)+this.errX/unitsPerPixelH, (tileTop-this.y)*tileHeight/(tileTop-tileBottom), -2*this.errX/unitsPerPixelH, this.y/unitsPerPixelV));
				g.setColor(Color.BLACK);
				g.draw(new Rectangle2D.Double( (tileLeft-this.x)*tileWidth/(tileLeft-tileRight)+this.errX/unitsPerPixelH, (tileTop-this.y)*tileHeight/(tileTop-tileBottom), -2*this.errX/unitsPerPixelH, this.y/unitsPerPixelV));
			}
			else
			{
				g.setColor(this.getPrimaryColor());
				g.fill(new Rectangle2D.Double( (tileLeft-this.x)*tileWidth/(tileLeft-tileRight)+this.errX/unitsPerPixelH, (tileTop-0)*tileHeight/(tileTop-tileBottom), -2*this.errX/unitsPerPixelH, -this.y/unitsPerPixelV));
				g.setColor(Color.BLACK);
				g.draw(new Rectangle2D.Double( (tileLeft-this.x)*tileWidth/(tileLeft-tileRight)+this.errX/unitsPerPixelH, (tileTop-0)*tileHeight/(tileTop-tileBottom), -2*this.errX/unitsPerPixelH, -this.y/unitsPerPixelV));
			}
			break;
		
		case XYPointEntity.DATA_ASPECT_FILLGRID:
			g.setColor(this.getPrimaryColor());
			g.fill(new Rectangle2D.Double( (tileLeft-this.x)*tileWidth/(tileLeft-tileRight)+this.errX/unitsPerPixelH, (tileTop-this.y)*tileHeight/(tileTop-tileBottom)-this.errY/unitsPerPixelV, -2*this.errX/unitsPerPixelH, 2*this.errY/unitsPerPixelV));
			g.setColor(Color.BLACK);
			g.draw(new Rectangle2D.Double( (tileLeft-this.x)*tileWidth/(tileLeft-tileRight)+this.errX/unitsPerPixelH, (tileTop-this.y)*tileHeight/(tileTop-tileBottom)-this.errY/unitsPerPixelV, -2*this.errX/unitsPerPixelH, 2*this.errY/unitsPerPixelV));
			break;
		
		default:
			break;
		}
	}
	
	public String getXmlTag(Hashtable xmlProperties)
	{
		XMLCoDec tempCodec = new XMLCoDec();
		String tempTag="";

		tempCodec.addParameter("primaryColor", this.getPrimaryColor().getRed()+","+this.getPrimaryColor().getGreen()+","+this.getPrimaryColor().getBlue());
		tempCodec.addParameter("secondaryColor", this.getSecondaryColor().getRed()+","+this.getSecondaryColor().getGreen()+","+this.getSecondaryColor().getBlue());
		
		tempCodec.addParameter("x", new Double(this.x));
		tempCodec.addParameter("y", new Double(this.y));
		tempCodec.addParameter("errX", new Double(this.errX));
		tempCodec.addParameter("errY", new Double(this.errY));
		tempCodec.addParameter("dataAspect", new Integer(this.dataAspect));
		
		this.addLinkXMLElements(tempCodec);

		tempTag+="<graphdraw2.XYPointEntity ";
		tempTag+=tempCodec.encodeParameters();
		tempTag+=">\r\n";

//		if (this.reference != null)
//			tempTag+=this.reference.getXmlTag(xmlProperties);

		tempTag+="</graphdraw2.XYPointEntity>\r\n";
		
		return tempTag;
	}
}