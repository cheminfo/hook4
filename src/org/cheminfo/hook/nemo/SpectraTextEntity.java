package org.cheminfo.hook.nemo;

import java.awt.Color;
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

public class SpectraTextEntity extends BasicEntity {

	String entityText;
	final static int BORDER = 5;
	int cursorPosition = 0;
	final static boolean VERBOSE = false;
	private int fontSize;
	private JTextArea m_textArea = null;
	private JTextArea interpretedTextArea = null;

	public SpectraTextEntity() {
		this.fontSize = 12;
		this.setPrimaryColor(Color.black);
		this.setSecondaryColor(MyTransparency.createTransparentColor(Color.white));
		this.setMovementType(BasicEntity.GLOBAL);
	}
	
	public SpectraTextEntity(int columns, int rows) {
		this();
		this.setSize(7 * columns, 24 * rows);
		this.refreshSensitiveArea();
		EntityResizer eResizer = new EntityResizer(EntityResizer.SE_RESIZER);
		this.addEntity(eResizer);
		eResizer.checkSizeAndPosition();		
	}

	public SpectraTextEntity(int columns) {
		this(columns, 1);
	}

	public SpectraTextEntity(String XMLString, Hashtable helpers) {
		this(0, 0);
		BasicEntity parentSpectra = (BasicEntity) helpers.get("currentSpectra");

		double parentWidth = parentSpectra.getWidth();
		double parentHeight = parentSpectra.getHeight();

		XMLCoDec tempCodec = new XMLCoDec(XMLString);
		tempCodec.shaveXMLTag();

		this.setSize(tempCodec.getParameterAsDouble("relWidth") * parentWidth,
				tempCodec.getParameterAsDouble("relHeight") * parentHeight);
		this.setLocation(tempCodec.getParameterAsDouble("relX") * parentWidth,
				tempCodec.getParameterAsDouble("relY") * parentHeight);

		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor"));
		
		this.m_textArea = new JTextArea(tempCodec
				.getParameterAsString("entityText"));
		this.m_textArea.setSize((int) this.getWidth(), (int) this.getHeight());
		this.m_textArea.setLineWrap(true);
		this.m_textArea.setWrapStyleWord(true);
		this.m_textArea.setOpaque(false);
		this.m_textArea.setForeground(this.getPrimaryColor());
		this.interpretedTextArea = new JTextArea("");
		this.interpretedTextArea.setSize((int) this.getWidth(), (int) this.getHeight());
		this.interpretedTextArea.setOpaque(false);
		this.interpretedTextArea.setLineWrap(true);
		this.interpretedTextArea.setWrapStyleWord(true);
		this.interpretedTextArea.setForeground(this.getPrimaryColor());
		this.interpretedTextArea.setBackground(this.getSecondaryColor());
		this.interpretedTextArea.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		this.refreshSensitiveArea();
	}

	public void addNotify() {
		if (this.m_textArea != null && this.interpretedTextArea != null)
			this.updateInterpretedText();
	}

	public void setSelected(boolean isSelected) {
		super.setSelected(isSelected);

		if (isSelected) {
			if (this.m_textArea == null) {
				this.m_textArea = new JTextArea("Add Text Here");
				this.m_textArea.setLineWrap(true);
				this.m_textArea.setWrapStyleWord(true);
				this.m_textArea.setForeground(this.getPrimaryColor());
				this.m_textArea.setBorder(BorderFactory.createLineBorder(Color.black,1));
			}

			AffineTransform parentTrans = this.getParentEntity()
					.getGlobalTransform();

			Point2D absLocation = parentTrans.transform(this.getLocation(),
					null);
			this.m_textArea.setLocation((int) absLocation.getX(),
					(int) absLocation.getY());
			this.m_textArea.setSize((int) this.getWidth(), (int) this
					.getHeight() - 10);

			this.getInteractiveSurface().add(this.m_textArea);
			this.m_textArea.repaint();
		} else {
			if (this.interpretedTextArea == null) {
				this.interpretedTextArea = new JTextArea("");
				this.interpretedTextArea.setLineWrap(true);
				this.interpretedTextArea.setWrapStyleWord(true);
				this.interpretedTextArea.setForeground(this.getPrimaryColor());
				this.interpretedTextArea.setBackground(this.getSecondaryColor());
				this.interpretedTextArea.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
			}

			this.interpretedTextArea.setSize((int) this.getWidth(), (int) this.getHeight() - 10);
			this.updateInterpretedText();

			this.getInteractiveSurface().remove(this.m_textArea);
			this.getInteractiveSurface().requestFocus();
		}
	}

	public void moveLocal(double deltaX, double deltaY) {
		super.moveLocal(deltaX, deltaY);

		if (this.getInteractiveSurface().isAncestorOf(this.m_textArea))
			this.getInteractiveSurface().remove(this.m_textArea);
	}

	public void setSize(double width, double height) {
		super.setSize(width, height);

		if (this.m_textArea != null)
			this.m_textArea.setSize((int) this.getWidth(), (int) this
					.getHeight() - 10);

		if (this.interpretedTextArea != null)
			this.interpretedTextArea.setSize((int) this.getWidth(), (int) this
					.getHeight() - 10);

	}

	public String getOverMessage() {
		return this.entityText;
	}

	public void setText(String newText) {
		if (this.m_textArea == null) {
			this.m_textArea = new JTextArea(newText);
			this.m_textArea.setForeground(this.getPrimaryColor());
			this.m_textArea.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
			this.interpretedTextArea = new JTextArea(newText);
			this.interpretedTextArea.setOpaque(false);
			this.interpretedTextArea.setForeground(this.getPrimaryColor());
			this.interpretedTextArea.setBackground(this.getSecondaryColor());
			this.interpretedTextArea.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		} else {
			this.m_textArea.setText(newText);
		}
		this.updateInterpretedText();

		this.m_textArea.setSize((int) this.getWidth(),
				(int) this.getHeight() - 10);
		this.interpretedTextArea.setSize((int) this.getWidth(), (int) this.getHeight() - 10);

		if (this.getInteractiveSurface() != null)
			this.getInteractiveSurface().repaint();
	}

	public void setFontSize(int newFontSize) {
		this.fontSize = newFontSize;
	}

	public void checkSizeAndPosition() {
		super.checkSizeAndPosition();
		this.refreshSensitiveArea();
	}

	public void refreshSensitiveArea() {
		super.refreshSensitiveArea();

		this.setSensitiveArea(new Area(new Rectangle2D.Double(0, -10, this
				.getWidth(), this.getHeight() + 10)));

	}

	/**
	 * Parses the text in m_textArea and set the result in interpretedTextArea
	 * 
	 */
	private void updateInterpretedText() {
		Spectra parentSpectra = (Spectra) this.getParentEntity();

		String text = this.m_textArea.getText();
		String parsedText = "";

		int charID = 0;
		int tempIdS, tempIdE;

		while (charID < text.length()) {
			tempIdS = text.indexOf("{", charID);

			if (tempIdS == -1) {
				parsedText += text.substring(charID);
				break;
			} else {
				parsedText += text.substring(charID, tempIdS);
				tempIdE = text.indexOf("}", tempIdS);
				// ERROR made by the user
				if (tempIdE == -1) {
					parsedText += text.substring(tempIdS);
					break;
				}
				String replacement = parentSpectra.getSpectraData()
						.getParamString(text.substring(tempIdS + 1, tempIdE),
								null);
				if (replacement == null) {
					replacement = parentSpectra.getSpectraData()
							.getSubParamString(
									text.substring(tempIdS + 1, tempIdE), "");
				}
				parsedText += replacement;
				charID = tempIdE + 1;
			}
		}

		this.interpretedTextArea.setText(parsedText);
		this.interpretedTextArea.setSize((int) this.getWidth(), (int) this.getHeight() - 10);
	}

	public void paint(Graphics2D g) 
	{
		g.setClip(new Rectangle2D.Double(-3, -13, this.getWidth() + 5, this.getHeight() + 15));
		g.setColor(this.getPrimaryColor());

		if(this.isMouseover() || this.isSelected())
		{
			g.draw(new Rectangle2D.Double(-2, -12, this.getWidth()+3, this.getHeight()+13));
		}

		if (this.isSelected()) 
		{
			if (!this.m_textArea.hasFocus()) {
				this.m_textArea.setOpaque(false);
				this.m_textArea.setBorder(BorderFactory.createLineBorder(Color.black,1));
				this.m_textArea.paint(g);
			}
		} else {
			if (this.interpretedTextArea!=null) {
				this.interpretedTextArea.setOpaque(true);
				this.interpretedTextArea.setForeground(getPrimaryColor());
				this.interpretedTextArea.setBackground(getSecondaryColor());
				this.interpretedTextArea.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
				this.interpretedTextArea.paint(g);
			}
		}

		super.paint(g);
		g.setClip(new Rectangle2D.Double(0, 0, this.getInteractiveSurface().getSize().width, this.getInteractiveSurface().getSize().height));
	}

	public String getXmlTag(Hashtable xmlProperties) {
		String tempTag = "";
		XMLCoDec tempCodec = new XMLCoDec();

		Point2D.Double parentSize = new Point2D.Double();

		parentSize.x = this.getParentEntity().getWidth();
		parentSize.y = this.getParentEntity().getHeight();
		
		if (this.getPrimaryColor() == null)
			tempCodec.addParameter("primaryColor", 0 + "," + 0 + "," + 0);
		else
			tempCodec.addParameter("primaryColor", this.getPrimaryColor()
					.getRed()
					+ ","
					+ this.getPrimaryColor().getGreen()
					+ ","
					+ this.getPrimaryColor().getBlue());

		tempCodec.addParameter("relX", new Double(this.getLocation().x
				/ parentSize.x));
		tempCodec.addParameter("relY", new Double(this.getLocation().y
				/ parentSize.y));
		tempCodec.addParameter("relWidth", new Double(this.getWidth()
				/ parentSize.x));
		tempCodec.addParameter("relHeight", new Double(this.getHeight()
				/ parentSize.y));
		tempCodec.addParameter("entityText", this.m_textArea.getText());
		tempCodec.addParameter("relFontSize", new Double(this.fontSize
				/ this.getHeight()));

		tempTag += "<nemo.SpectraTextEntity ";
		tempTag += tempCodec.encodeParameters();
		tempTag += ">\r\n";

		tempTag += "</nemo.SpectraTextEntity>\r\n";
		return tempTag;
	}
}