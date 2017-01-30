package org.cheminfo.hook.nemo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.nmr.PredictionData;
import org.cheminfo.hook.util.XMLCoDec;

//import sun.tools.tree.ThisExpression;

/**
 * This class handles the Labels basic functions:
 * <ul>
 * <li> label apparence.
 * <li> label painting.
 * </ul>
 */
public class PeakLabel extends BasicEntity implements Comparable {
	public static int DEFAULT_WIDTH = 30;
	public static int DEFAULT_HEIGHT = 20;
	public static int DEFAULT_WIDTH_C = 7;
	public static int DEFAULT_HEIGHT_C = 7;

	// Peak types (for 2D)

	public static int NONE = 0;
	public static int DIAGONAL_PEAK = 1;
	public static int PRESENT_PEAK = 2;
	public static int ABSENT_PEAK = 3;

	private boolean showRange = false;

	private double xMin;
	private double xMax;

	private double xPosUnits, yPosUnits; // X and Y position in the units of the spectrum
	private int xArrayPoint;

	private String text;

	private String comment = "";
	private boolean contracted;

	private int peakType = NONE;

	/**
	 * Creates a PeakLabel object.
	 * 
	 * @param xPosUnits -
	 *            value in units for the position of this Label.
	 */
	/*
	 * protected PeakLabel(int xArrayPoint) { super();
	 * this.setXArrayPoint(xArrayPoint);
	 * 
	 * this.setPrimaryColor(Color.red); this.contracted=false; }
	 */
	protected PeakLabel(double xPosUnits) {
		super();
		init();
		this.xPosUnits = xPosUnits;
		this.setPrimaryColor(Color.red);
		this.contracted = false;
	}

	/*
	 * protected PeakLabel(int xArrayPoint, double yPosUnits) { super();
	 * this.setXArrayPoint(xArrayPoint); this.setYPos(yPosUnits);
	 * 
	 * this.setPrimaryColor(Color.red); this.contracted=false;
	 * 
	 * this.homolink=false; this.heterolink=true; }
	 */
	protected PeakLabel(double xPosUnits, double yPosUnits) {
		super();
		init();
		this.xPosUnits = xPosUnits;
		this.setYPos(yPosUnits);
		this.setPrimaryColor(Color.red);
		this.contracted = false;

	}

	public PeakLabel(PeakLabel original) {
		super();
		init();
		this.setXArrayPoint(original.getXArrayPoint());
		this.setYPos(original.getYPos());

		this.setPrimaryColor(original.getPrimaryColor());
		this.contracted = original.contracted;
	}

	public PeakLabel(String XMLString, Hashtable helpers) {
		super();
		init();
		XMLCoDec tempCodec = new XMLCoDec(XMLString);
		tempCodec.shaveXMLTag();
		super.getSuperFromXML(tempCodec);
		this.setUniqueID(tempCodec.getParameterAsInt("uniqueID"));
		this.storeLinkIDs(tempCodec);

		this.setXArrayPoint(tempCodec.getParameterAsInt("xArrayPoint")); // legacy
		// XML
		// not
		// stored
		// anymore

		this.setXPos(tempCodec.getParameterAsDouble("xUnits"));
		this.setYPos(tempCodec.getParameterAsDouble("yUnits"));

		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor"));
		// this.setSecondaryColor(tempCodec.getParameterAsColor("secondaryColor"));
		this.comment = tempCodec.getParameterAsString("comment");
		this.contracted = tempCodec.getParameterAsBoolean("contracted");
		//

	}

	/*
	 * 
	 */
	private void init() {
		// By default it is true ...
		// this.setHomolink(true);
		// this.setHeterolink(true);
		this.setHighlightPropagationType(BasicEntity.HL_PROPAGATION_OUTBOUND);
	}

	public void setSize(Dimension size) {
		this.setSize(size.height, size.width);
	}

	public void setSize(int width, int height) {
		super.setSize(width, height);

	}

	public String getComment() {
		return comment;
	}
	
	public void delete() {
		if (this.getParentEntity() != null && this.getParentEntity() instanceof Spectra) {
			Spectra tempSpectra = (Spectra) this.getParentEntity();
			if (tempSpectra.getTempPeakLabels().contains(this)) {
				tempSpectra.getTempPeakLabels().removeElement(this);
			}
		}
		super.delete();
	}

	double getIntensity () {
		Spectra tempSpectra=(Spectra)this.getParentEntity();
		if (!((Spectra) this.getParentEntity()).isDrawnAs2D()) {
			if (tempSpectra.getSpectraData().isDataClassPeak()) {
				return tempSpectra.getSpectraData().getY(tempSpectra.unitsToArrayPoint(this.getXPos()));
			} else if (tempSpectra.getSpectraData().isDataClassXY()) {
				return ((Spectra) this.getParentEntity()).getSpectraData().getYForUnits(this.getXPos());
			} else return 0;	
		} else {
			return tempSpectra.getSpectraData().getZForUnits(this.getXPos(),this.getYPos());
		}
	}
	
	public String getOverMessage() {
		
		
		DecimalFormat newFormat = new DecimalFormat();
		newFormat.applyPattern("#0.00#####");

		String toReturn = "Peak Position: ";

		toReturn += newFormat.format(this.getXPos());
		if (((Spectra) this.getParentEntity()).isDrawnAs2D())
			toReturn += " / " + newFormat.format(this.getYPos());

		toReturn += " " + ((Spectra) this.getParentEntity()).getSpectraData().getXUnits();

		if (((Spectra) this.getParentEntity()).getSpectraData().getDataType() == SpectraData.TYPE_NMR_SPECTRUM)
			toReturn += " ("
					+ (newFormat.format(this.getXPos()
							* ((Spectra) this.getParentEntity()).getSpectraData().getParamDouble("observeFrequency", 0))) + " Hz)";


		toReturn += " - Intensity: " + newFormat.format(this.getIntensity());


		toReturn += " "+ ((Spectra) this.getParentEntity()).getSpectraData().getYUnits();

		if (!comment.equals("")) toReturn += " - " + comment;

		return toReturn;
	}

	public String getClickedMessage() {
		DecimalFormat newFormat = new DecimalFormat();
		newFormat.applyPattern("#0.00#####");

		XMLCoDec xposCodec = new XMLCoDec();
		xposCodec.addParameter("name", "peakXPos");
		xposCodec.addParameter("size", new Integer(10));

		XMLCoDec yposCodec = null;
		if (this.getParentEntity() instanceof Spectra && ((Spectra) this.getParentEntity()).isDrawnAs2D()) {
			yposCodec = new XMLCoDec();
			yposCodec.addParameter("name", "peakYPos");
			yposCodec.addParameter("size", new Integer(10));
		}
		XMLCoDec commentCodec = new XMLCoDec();
		commentCodec.addParameter("name", "comment");
		commentCodec.addParameter("size", new Integer(20));

		XMLCoDec buttonCodec = new XMLCoDec();
		buttonCodec.addParameter("action","org.cheminfo.hook.nemo.PeakLabelAction");
		buttonCodec.addParameter("image", "validate.gif");

		// encoding parameters in a local encoding requires to take
		// care for the parsing of the String to a double

		String xValue = (newFormat.format(this.getXPos()));
		StringBuffer output = new StringBuffer();
		output.append("<Text type=\"plain\">Peak Position: </Text><Input ");
		output.append(xposCodec.encodeParameters());
		output.append('>');
		output.append(xValue);
		output.append("</Input><Text type=\"plain\"> - </Text>");
		if (yposCodec != null) {
			String yValue = (newFormat.format(this.getYPos()));
			output.append("<Input ");
			output.append(yposCodec.encodeParameters());
			output.append('>');
			output.append(yValue);
			output.append("</Input><Text type=\"plain\"> - </Text>");
		}
		output.append("Public.: <Input ");
		output.append(commentCodec.encodeParameters());
		output.append(">"+this.comment);
		output.append("</Input><Button ");
		output.append(buttonCodec.encodeParameters());
		output.append("></Button>");
		return output.toString();
	}

	void setXArrayPoint(int arrayPoint) {
		this.xArrayPoint = arrayPoint;
	}

	int getXArrayPoint() {
		return this.xArrayPoint;
	}

	/**
	 * Returns the Xcoordinate in of this label.
	 * 
	 * @return a double representing the X coordinate in units for this label.
	 */
	public double getXPos() {
		// return
		// ((Spectra)this.getParentEntity()).arrayPointToUnits(this.xArrayPoint);
		return this.xPosUnits;
	}

	/**
	 * Sets the X position of this label. Calculates apparence.
	 * 
	 * @param newValue -
	 *            a double indicating the new X coordinate.
	 */
	protected void setXPos(double newValue) {
		this.xPosUnits = newValue;
	}

	protected void setYPos(double newYValue) {
		this.yPosUnits = newYValue;
	}

	public double getYPos() {
		return this.yPosUnits;
	}

	protected void setPeakType(int peakType) {
		this.peakType = peakType;
	}

	protected int getPeakType() {
		return this.peakType;
	}

	protected void setComment(String newComment) {
		this.comment = newComment;
	}

	protected void setContracted(boolean flag) {
		if (this.contracted != flag) {
			this.contracted = flag;

			if (this.contracted)
				this.setSize(PeakLabel.DEFAULT_WIDTH_C,
						PeakLabel.DEFAULT_HEIGHT_C);
			else {
				this.setSize(PeakLabel.DEFAULT_WIDTH, PeakLabel.DEFAULT_HEIGHT);
			}
		}
	}

	/*public void refreshSensitiveArea() {
		if (this.getParentEntity() != null)
			System.out.println("Parent is null");
		if (this.getParentEntity() != null) {
			Spectra parentSpectra = (Spectra) this.getParentEntity();
			if (!parentSpectra.isDrawnAs2D())
				this
						.setSensitiveArea(new Area(new Rectangle2D.Double(-this
								.getWidth() / 2, 0, this.getWidth(), this
								.getHeight())));
			else
				this.setSensitiveArea(new Area(new Rectangle2D.Double(-this
						.getWidth() / 2, -this.getHeight() / 2,
						this.getWidth(), this.getHeight())));
		}
	}*/

	public void checkSizeAndPosition() {
		Spectra parentSpectra = (Spectra) this.getParentEntity();
		double tempX=0;
		double tempY=0;
		if (!parentSpectra.isDrawnAs2D()) {
			if (parentSpectra.hasVisiblePeakLabels() == 0) {
				this.setSize(0, 0);
			} else if (this.contracted)
				this.setSize(7, 7);
			else
				this.setSize(20, 20);

			double x = this.getXPos();

//			System.out.println("Val at "+x+": "+parentSpectra.spectraValueAt(x));
			double y = (int) Math.ceil(parentSpectra.getHeight()
					- parentSpectra.getMultFactor()
					* (parentSpectra.spectraValueAt(x) - parentSpectra.getSpectraData().getMinY())
					* parentSpectra.getHeight()
					/ (parentSpectra.getSpectraData().getMaxY() - parentSpectra	.getSpectraData().getMinY()));

			// if (y < this.getHeight()) y=this.getHeight(); // prevents the
			// label from going out of the Spectra view area
			if (y < this.getHeight() - parentSpectra.getLocation().y)
				y = this.getHeight() - parentSpectra.getLocation().y; // prevents the label from going out of the Spectra view area
			
			//this.setLocation(parentSpectra.unitsToPixelsH(this.getXPos()), y - this.getHeight());
			tempX=parentSpectra.unitsToPixelsH(this.getXPos());
			tempY=y - this.getHeight();
		} else {
			//this.setLocation(parentSpectra.unitsToPixelsH(this.xPosUnits),parentSpectra.unitsToPixelsV(this.yPosUnits));
			tempX=parentSpectra.unitsToPixelsH(this.xPosUnits);
			tempY=parentSpectra.unitsToPixelsV(this.yPosUnits);
			this.setSize(15, 15);
		}
		/////
		this.setLocation(tempX, tempY);
		this.refreshSensitiveArea();

		/*
		Area thisTransformedArea = this.getSensitiveArea().createTransformedArea(this.getGlobalTransform());
		Area testTransformedArea = new Area();
		
		for (int ent = 0; ent < parentSpectra.getEntitiesCount(); ent++) {
			BasicEntity entity = parentSpectra.getEntity(ent);
			if (entity instanceof PeakLabel	&& entity != this) {
				testTransformedArea = entity.getSensitiveArea().createTransformedArea(entity.getGlobalTransform());
				if (testTransformedArea.intersects(thisTransformedArea.getBounds2D())) {
					tempY-=30;
					tempY-=0;
					this.setLocation(tempX, tempY);
					thisTransformedArea = this.getSensitiveArea()
							.createTransformedArea(this.getGlobalTransform());
					ent=0;
				}
			}
		}*/
		///
		this.refreshSensitiveArea();
	}

	/**
	 * Paints this object.
	 * 
	 * @param g - a graphic context.
	 */
	public void paint(Graphics2D g) {
		Spectra parentSpectra = (Spectra) this.getParentEntity();
		// double xPosUnits=parentSpectra.arrayPointToUnits(this.xArrayPoint);

		g.setFont(new Font("Serif", Font.PLAIN, 12));
		DecimalFormat newFormat = new DecimalFormat();
		if (xPosUnits < 100) {
			newFormat.applyPattern("#0.000");
			text = (newFormat.format(xPosUnits)).substring(0, 4);
		} else {
			newFormat.applyPattern("#0");
			text = (newFormat.format(xPosUnits));
		}

		if (!parentSpectra.isDrawnAs2D()) {
			if (parentSpectra.hasVisiblePeakLabels() == 1) {
				if (!this.contracted) {
					Dimension stringDimensions = new Dimension(g
							.getFontMetrics().stringWidth(text), g
							.getFontMetrics().getHeight());

					// Draws Frame
					g.setColor(Color.white);
					g.fill(new Rectangle2D.Double(-this.getWidth() / 2, 0, this
							.getWidth(), this.getHeight() - 5));
					g.setColor(this.getPrimaryColor());

					if (this.isSelected())
						g.setStroke(new BasicStroke(2));
					else if (this.isMouseover() || this.isHighlighted())
						g.setStroke(new BasicStroke(2));
					else
						g.setStroke(this.getInteractiveSurface()
								.getNarrowStroke());

					g.draw(new Line2D.Double(-this.getWidth() / 2, this
							.getHeight() - 5, this.getWidth() / 2, this
							.getHeight() - 5));

					g.drawString(
									text,
									(float) (-stringDimensions.width) / 2,
									(float) ((this.getHeight() - stringDimensions.height / 2) - 1));
					g.draw(new Line2D.Double(0, this.getHeight() - 5, 0, this
							.getHeight()));

					if (this.showRange) {
						double xpos = parentSpectra
								.unitsToPixelsH(this.xPosUnits);
						double minpos = parentSpectra.unitsToPixelsH(this.xMin);
						double maxpos = parentSpectra.unitsToPixelsH(this.xMax);
						g.setColor(Color.green);
						g.draw(new Line2D.Double(minpos - xpos, 0, maxpos
								- xpos, 0));
					}
				} else // contracted
				{
					g.setColor(this.getPrimaryColor());
					g.draw(new Line2D.Double(-this.getWidth() / 2, 0, this
							.getWidth() / 2, 0));
					g.draw(new Line2D.Double(this.getWidth() / 2, 0, 0, this
							.getHeight()));
					g.draw(new Line2D.Double(0, this.getHeight(), -this
							.getWidth() / 2, 0));
				}
			}
		} else {
			if (parentSpectra.hasVisiblePeakLabels() == 1
					|| this.peakType == PeakLabel.DIAGONAL_PEAK
					|| this.peakType == PeakLabel.PRESENT_PEAK) {
				int nPeakLabels = 0;
				if (this.getInteractiveSurface() != null) {
					TreeSet<SmartPeakLabel> smartPeakLabels = new TreeSet<SmartPeakLabel>();
					InteractiveSurface interactions = this.getInteractiveSurface();
					Vector linkedEntities = interactions.getLinkedEntities(this);
					for (int i = 0; i < linkedEntities.size(); i++) {
						if (linkedEntities.get(i) instanceof SmartPeakLabel) {
							smartPeakLabels.add((SmartPeakLabel) linkedEntities.get(i));
						}
					}
					nPeakLabels = smartPeakLabels.size();
				}
				if (parentSpectra.getParentEntity() instanceof SpectraDisplay) {
					SpectraDisplay spectraDisplay = (SpectraDisplay) parentSpectra.getParentEntity();
					double cbL = spectraDisplay.getCurrentBottomLimit();
					double ctL = spectraDisplay.getCurrentTopLimit();
					double crL = spectraDisplay.getCurrentRightLimit();
					double clL = spectraDisplay.getCurrentLeftLimit();
					if (!(Math.min(crL, clL) <= this.xPosUnits && Math.max(crL,clL) >= this.xPosUnits))
						return;
					if (!(Math.min(cbL, ctL) <= this.yPosUnits && Math.max(cbL,ctL) >= this.yPosUnits))
						return;
				}

				// Dimension labelSize=this.getSize();
				double labelHeight = this.getHeight();
				double labelWidth = this.getWidth();
				g.setColor(this.getPrimaryColor());

				g.draw(new Line2D.Double(-labelWidth / 2, 0, labelWidth / 2, 0));
				g.draw(new Line2D.Double(0, -labelHeight / 2, 0, labelHeight / 2));
				if (!this.isHighlighted() && !this.isMouseover() && !this.isSelected()) {
					boolean offDiagonal = true;
					if (Math.sqrt(Math.pow(this.xPosUnits - this.yPosUnits, 2)) < Math
							.abs(parentSpectra.getSpectraData().getInterval())) {
						offDiagonal = false;
					}
					/*
					// The following code is just depends of the number of link
					// Because peaks on 2d are nearly always linked I have disable it  LP20100130
					 
					if (nPeakLabels == 1 && offDiagonal) {
						g.draw(new Ellipse2D.Double(-labelWidth / 6.0, -labelHeight / 6.0, labelWidth / 3.0,
								labelHeight / 3.0));
					} else if (nPeakLabels > 1 || (nPeakLabels == 1 && !offDiagonal)) {
						g.fill(new Ellipse2D.Double(-labelWidth / 6.0, -labelHeight / 6.0, labelWidth / 3.0,
								labelHeight / 3.0));
					}
					*/
				}
				if (this.isSelected()) {
					g.fill(new Rectangle2D.Double(-3, -3, 7, 7));
				}

				if (this.isMouseover() || this.isHighlighted()) {
					g.fill(new Rectangle2D.Double(-2, -2, 5, 5));
				}
			}
		}
	}

	public String getXmlTag(Hashtable xmlProperties) {
		String tempTag = "";
		XMLCoDec tempCodec = new XMLCoDec();
		super.addSuperToXML(tempCodec);
		tempCodec.addParameter("primaryColor", this.getPrimaryColor().getRed()
				+ "," + this.getPrimaryColor().getGreen() + ","
				+ this.getPrimaryColor().getBlue());
		tempCodec.addParameter("xUnits", new Double(this.xPosUnits));
		tempCodec.addParameter("yUnits", new Double(this.yPosUnits));
		tempCodec.addParameter("comment", this.comment);
		tempCodec.addParameter("contracted", new Boolean(this.contracted));

		// link

		this.addLinkXMLElements(tempCodec);

		tempTag += "<nemo.PeakLabel " + tempCodec.encodeParameters() + ">\r\n";
		tempTag += "</nemo.PeakLabel>\r\n";

		return tempTag;
	}

	public double getXPosUnits() {
		return xPosUnits;
	}

	public double getYPosUnits() {
		return yPosUnits;
	}

	public boolean isShowRange() {
		return showRange;
	}

	public void setShowRange(boolean showRange) {
		this.showRange = showRange;
	}

	public double getXMin() {
		return xMin;
	}

	public void setXMin(double min) {
		xMin = min;
	}

	public double getXMax() {
		return xMax;
	}

	public void setXMax(double max) {
		xMax = max;
	}

	@Override
	public void notifyDelete() {
		// System.out.println("notify delete");
		if (this.getParentEntity() instanceof Spectra
				&& ((Spectra) this.getParentEntity()).isDrawnAs2D()) {
			InteractiveSurface interactions = this.getInteractiveSurface();
			Vector linkedEntities = interactions.getLinkedDestEntities(this);
			for (int ent = 0; ent < linkedEntities.size(); ent++) {
				if (linkedEntities.get(ent) instanceof SmartPeakLabel) {
					SmartPeakLabel spl = (SmartPeakLabel) linkedEntities
							.get(ent);
					if (spl.getParentEntity() instanceof Spectra) {
						Spectra refSpectrum = (Spectra) spl.getParentEntity();
						// System.out.println("title");
						if (refSpectrum.getSpectraData().getTitle().equals(PredictionData.projectedTrace))
							refSpectrum.remove(spl);
					}
				}
			}
		}
	}

	public int compareTo(Object otherPeakLabel) {
		Double currentValue = this.getXPos();
		Double otherValue = ((PeakLabel) otherPeakLabel).getXPos();
		return currentValue.compareTo(otherValue);
	}

	public String toHtml(DecimalFormat format) {
		String toReturn="";
		toReturn+=format.format(this.getXPos());
		if (! this.comment.equals("")) {
			toReturn+=" ("+comment+")";
		}
		return toReturn;
	}
	
	
}