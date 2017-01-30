package org.cheminfo.hook.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;

import org.cheminfo.hook.appli.MyTransparency;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.EntityResizer;
import org.cheminfo.hook.util.XMLCoDec;


public class TextEntity extends BasicEntity
{

	String entityText;
	final static int BORDER=5;
	int cursorPosition=0;
	final static boolean VERBOSE=false;
	private int fontSize;
	private JTextArea m_textArea=null; 
	private JTextArea interpretedTextArea=null;
	private double tempRelWidth, tempRelHeight, tempRelX, tempRelY, tempRelFontSize;
	private boolean udatedAtLeasOnce;
	
	public TextEntity(int columns, int rows)
	{
		this.fontSize=12;
		this.setSize(7*columns, 24*rows);
		this.setPrimaryColor(Color.black);
		this.setSecondaryColor(MyTransparency.createTransparentColor(Color.white));
		this.setMovementType(BasicEntity.GLOBAL);
		this.refreshSensitiveArea();
		
		EntityResizer eResizer=new EntityResizer(EntityResizer.SE_RESIZER);
		this.addEntity(eResizer);
		eResizer.checkSizeAndPosition();
		this.udatedAtLeasOnce=true;
	}

	public TextEntity(int columns)
	{
		this(columns, 1);
	}
	
	public TextEntity(String XMLString, Hashtable helpers)
	{
		this(0,0);
/*		BasicEntity parent=(BasicEntity)helpers.get("currentSpectra");
		
		double parentWidth=parentSpectra.getWidth();
		double parentHeight=parentSpectra.getHeight();
*/		
		XMLCoDec tempCodec = new XMLCoDec(XMLString);
		tempCodec.shaveXMLTag();
		
//		this.setSize(tempCodec.getParameterAsDouble("relWidth")*parentWidth, tempCodec.getParameterAsDouble("relHeight")*parentHeight);
//		this.setLocation(tempCodec.getParameterAsDouble("relX")*parentWidth, tempCodec.getParameterAsDouble("relY")*parentHeight);

		this.udatedAtLeasOnce=false;
		this.tempRelWidth=tempCodec.getParameterAsDouble("relWidth");
		this.tempRelHeight=tempCodec.getParameterAsDouble("relHeight");
		this.tempRelX=tempCodec.getParameterAsDouble("relX");
		this.tempRelY=tempCodec.getParameterAsDouble("relY");
		this.tempRelFontSize=tempCodec.getParameterAsDouble("relFontSize");
		
		System.out.println("REL "+this.tempRelWidth+", "+this.tempRelHeight);
		this.m_textArea=new JTextArea(tempCodec.getParameterAsString("entityText"));
		this.m_textArea.setSize((int)this.getWidth(), (int)this.getHeight());
		this.m_textArea.setLineWrap(true);
		this.m_textArea.setWrapStyleWord(true);
		this.m_textArea.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));


		this.interpretedTextArea=new JTextArea("");
		this.interpretedTextArea.setSize((int)this.getWidth(), (int)this.getHeight());
		this.interpretedTextArea.setLineWrap(true);
		this.interpretedTextArea.setWrapStyleWord(true);
		this.refreshSensitiveArea();
		System.out.println("TextArea XML LOAD");
	}

	public void addNotify()
	{
		if (this.m_textArea != null && this.interpretedTextArea != null)
		{
			this.updateInterpretedText();
		}
	}
	
	public void setSelected(boolean isSelected)
	{
		super.setSelected(isSelected);
		
		if (isSelected)
		{
			if (this.m_textArea == null)
			{
				this.m_textArea=new JTextArea("Add Text Here");
				this.m_textArea.setLineWrap(true);
				this.m_textArea.setWrapStyleWord(true);
			}

			AffineTransform parentTrans=this.getParentEntity().getGlobalTransform();
			
			
			Point2D absLocation=parentTrans.transform(this.getLocation(), null);
			this.m_textArea.setLocation((int)absLocation.getX(), (int)absLocation.getY());
			this.m_textArea.setSize((int)this.getWidth(), (int)this.getHeight()-10);

			this.getInteractiveSurface().add(this.m_textArea);
			this.m_textArea.repaint();
		}
		else
		{
			if (this.interpretedTextArea == null)
			{
				this.interpretedTextArea=new JTextArea("");
				this.interpretedTextArea.setLineWrap(true);
				this.interpretedTextArea.setWrapStyleWord(true);
			}

			this.interpretedTextArea.setSize((int)this.getWidth(), (int)this.getHeight()-10);
			this.updateInterpretedText();
			
			this.getInteractiveSurface().remove(this.m_textArea);
			this.getInteractiveSurface().requestFocus();
		}
	}
	
	public void moveLocal(double deltaX, double deltaY)
	{
		super.moveLocal(deltaX, deltaY);
		
		if (this.getInteractiveSurface().isAncestorOf(this.m_textArea))
			this.getInteractiveSurface().remove(this.m_textArea);
	}
	
	public void setSize(double width, double height)
	{
		super.setSize(width, height);
		
		if (this.m_textArea != null)
			this.m_textArea.setSize((int)this.getWidth(), (int)this.getHeight()-10);
		
		if (this.interpretedTextArea != null)
			this.interpretedTextArea.setSize((int)this.getWidth(), (int)this.getHeight()-10);
	
	}
	
	public String getOverMessage()
	{
		return this.entityText;
	}
	
	public void setText(String newText)
	{
		if (this.m_textArea == null)
		{
			this.m_textArea=new JTextArea(newText);
			this.interpretedTextArea=new JTextArea(newText);
		}
		else
		{
			this.m_textArea.setText(newText);
		}
		this.updateInterpretedText();
		
		this.m_textArea.setSize((int)this.getWidth(), (int)this.getHeight()-10);
		this.interpretedTextArea.setSize((int)this.getWidth(), (int)this.getHeight()-10);
		

		if (this.getInteractiveSurface() != null)
			this.getInteractiveSurface().repaint();
	}
	
	public void setFontSize(int newFontSize)
	{
		this.fontSize=newFontSize;
	}
	
	
	public void checkSizeAndPosition()
	{
		if ( !this.udatedAtLeasOnce )
		{
			this.setSize(this.getParentEntity().getWidth()*this.tempRelWidth, this.getParentEntity().getHeight()*this.tempRelHeight);
			this.setLocation(this.getParentEntity().getWidth()*this.tempRelX, this.getParentEntity().getHeight()*this.tempRelY);
			
			Font font=this.m_textArea.getFont();
			Font newFont=new Font(font.getFontName(), font.getStyle(), (int)(this.getHeight()*this.tempRelFontSize));
			this.m_textArea.setFont(newFont);
			this.interpretedTextArea.setFont(newFont);
		}

		super.checkSizeAndPosition();		
	
		this.refreshSensitiveArea();
	}
	
	public void refreshSensitiveArea()
	{
		super.refreshSensitiveArea();
		
		this.setSensitiveArea(new Area(new Rectangle2D.Double(0,-10,this.getWidth(), this.getHeight()+10)));

	}
	
	/**
	 * Parses the text in m_textArea and set the result in interpretedTextArea
	 *
	 */
	private void updateInterpretedText()
	{
		
		this.interpretedTextArea.setText(this.m_textArea.getText());
		this.interpretedTextArea.setSize((int)this.getWidth(), (int)this.getHeight()-10);
	}
	
	public void paint(Graphics2D g)
	{
		g.setClip(new Rectangle2D.Double(-3,-13, this.getWidth()+5, this.getHeight()+15));
		g.setColor(this.getPrimaryColor());

		if(this.isMouseover() || this.isSelected())
		{
			g.draw(new Rectangle2D.Double(-2, -12, this.getWidth()+3, this.getHeight()+13));
		}
		
		if (this.isSelected())
		{
			if (!this.m_textArea.hasFocus())
			{
				this.m_textArea.setOpaque(false);
				this.m_textArea.setBorder(BorderFactory.createLineBorder(Color.black,1));
				this.m_textArea.paint(g);
			}
		}
		else
		{
			this.interpretedTextArea.setOpaque(true);
			this.interpretedTextArea.setForeground(getPrimaryColor());
			this.interpretedTextArea.setBackground(getSecondaryColor());
			this.interpretedTextArea.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
			this.interpretedTextArea.paint(g);
		}
		
		super.paint(g);
		g.setClip(new Rectangle2D.Double(0,0,this.getInteractiveSurface().getSize().width, this.getInteractiveSurface().getSize().height));
	}
	
	public String getXmlTag(Hashtable xmlProperties)
	{
		String tempTag="";
		XMLCoDec tempCodec=new XMLCoDec();

		Point2D.Double parentSize=new Point2D.Double();
		
		parentSize.x=this.getParentEntity().getWidth();
		parentSize.y=this.getParentEntity().getHeight();

		tempCodec.addParameter("relX", new Double(this.getLocation().x/parentSize.x));
		tempCodec.addParameter("relY", new Double(this.getLocation().y/parentSize.y));
		tempCodec.addParameter("relWidth", new Double(this.getWidth()/parentSize.x));
		tempCodec.addParameter("relHeight", new Double(this.getHeight()/parentSize.y));
		tempCodec.addParameter("entityText", this.m_textArea.getText());
		tempCodec.addParameter("relFontSize", new Double(this.fontSize/this.getHeight()));
		
		tempTag+="<graphics.TextEntity ";
		tempTag+=tempCodec.encodeParameters();
		tempTag+=">\r\n";
		
		tempTag+="</graphics.TextEntity>\r\n";
		return tempTag;
	}
}