package org.cheminfo.hook.nemo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Hashtable;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.util.XMLCoDec;

/**
 * This class is represents a selection label in one or two dimensions
 * <ul>
 * <li> label apparence.
 * <li> label painting.
 * </ul>
 */
public class RangeSelectionLabel extends BasicEntity {

	private static final int tickLength = 5;
	// private boolean showRange = true;
	private double xMin = Double.NaN;
	private double xMax = Double.NaN;

	private double xPosUnits = Double.NaN;
	private double yPosUnits = Double.NaN; // X and Y position in the units of
	// the spectrum
	private int xArrayPoint;

	private String text;

	private String comment = "";
	private boolean contracted;


	protected RangeSelectionLabel(double xMin, double xMax) {
		super("RangeSelectionLabel");
		this.xMin = xMin;
		this.xMax = xMax;
		this.xPosUnits = (xMin + xMax) / 2;

		this.setPrimaryColor(Color.green);
		this.contracted = false;

		this.setHomolink(false);
		//default value: this.heterolink = true;
	}

	public RangeSelectionLabel(RangeSelectionLabel original) {
		super("RangeSelectionLabel");
		this.setXArrayPoint(original.getXArrayPoint());
		this.setYPos(original.getYPos());

		this.setPrimaryColor(original.getPrimaryColor());
		this.contracted = original.contracted;

		this.setHomolink(false);
		//default value: this.heterolink = true;
	}

	public RangeSelectionLabel(String XMLString, Hashtable helpers) {
		super();
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

		// LP20100122: following 2 lines are probably useless because taken into the super()
		// this.homolink = false;
		// this.heterolink = true;
	}

	public void setSize(Dimension size) {
		this.setSize(size.height, size.width);
	}

	public void setSize(int width, int height) {
		super.setSize(width, height);

	}

	public void delete() {
		if (this.getParentEntity() != null
				&& this.getParentEntity() instanceof Spectra) {
			Spectra tempSpectra = (Spectra) this.getParentEntity();
			if (tempSpectra.getTempPeakLabels().contains(this)) {
				tempSpectra.getTempPeakLabels().removeElement(this);
			}
		}
		super.delete();
	}

	public String getOverMessage() {
		DecimalFormat newFormat = new DecimalFormat();
		newFormat.applyPattern("#0.00");

		String toReturn = "Selected range: ";
		toReturn += "[" + newFormat.format(Math.min(this.xMin, this.xMax))
				+ "," + newFormat.format(Math.max(this.xMin, this.xMax)) + "]";
		return toReturn;
	}

	public String getClickedMessage() {
		DecimalFormat newFormat = new DecimalFormat();
		newFormat.applyPattern("#0.00");

		XMLCoDec inputCodec1 = new XMLCoDec();
		inputCodec1.addParameter("name", "peakXPos");
		inputCodec1.addParameter("size", new Integer(3));

		XMLCoDec inputCodec2 = new XMLCoDec();
		inputCodec2.addParameter("name", "comment");
		inputCodec2.addParameter("size", new Integer(10));

		XMLCoDec buttonCodec = new XMLCoDec();
		buttonCodec.addParameter("action",
				"org.cheminfo.hook.nemo.PeakLabelAction");
		buttonCodec.addParameter("image", "validate.gif");

		// encoding parameters in a local encoding requires to take
		// care for the parsing of the String to a double

		String value = (newFormat.format(this.getXPos()));
		return "<Text type=\"plain\">Peak Position: </Text><Input "
				+ inputCodec1.encodeParameters() + ">" + value
				+ "</Input><Text type=\"plain\"> - </Text><Input "
				+ inputCodec2.encodeParameters() + ">" + comment
				+ "</Input><Button " + buttonCodec.encodeParameters()
				+ "></Button>";

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
	protected double getXPos() {
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

	protected double getYPos() {
		return this.yPosUnits;
	}

	protected void setComment(String newComment) {
		this.comment = newComment;
	}

	public void refreshSensitiveArea() {
		if (this.getParentEntity() != null) {
			Spectra parentSpectra = (Spectra) this.getParentEntity();
			if (!parentSpectra.isDrawnAs2D())
				this
						.setSensitiveArea(new Area(new Rectangle2D.Double(-this
								.getWidth() / 2, 0, this.getWidth(), this
								.getHeight())));
			else
				this.setSensitiveArea(new Area(new Rectangle2D.Double(-this
						.getWidth() / 2, -this.tickLength, this.getWidth(),
						this.tickLength*2)));
		}
	}

	public void checkSizeAndPosition() {
		Spectra parentSpectra = (Spectra) this.getParentEntity();
		if (!parentSpectra.isDrawnAs2D()) {
			if (parentSpectra.hasVisiblePeakLabels() == 0) {
				this.setSize(0, 0);
			} else if (this.contracted)
				this.setSize(7, 7);
			else
				this.setSize(20, 20);

			double x = this.getXPos();
			double y = (int) Math.ceil(parentSpectra.getHeight()
					- parentSpectra.getMultFactor()
					* (parentSpectra.spectraValueAt(x) - parentSpectra
							.getSpectraData().getMinY())
					* parentSpectra.getHeight()
					/ (parentSpectra.getSpectraData().getMaxY() - parentSpectra
							.getSpectraData().getMinY()));

			// if (y < this.getHeight()) y=this.getHeight(); // prevents the
			// label from going out of the Spectra view area
			if (y < this.getHeight() - parentSpectra.getLocation().y)
				y = this.getHeight() - parentSpectra.getLocation().y; // prevents
			// the
			// label
			// from
			// going
			// out
			// of
			// the
			// Spectra
			// view
			// area

			this.setLocation(parentSpectra.unitsToPixelsH(this.getXPos()), y
					- this.getHeight());
		} else {
			this.setLocation(parentSpectra.unitsToPixelsH(this.xPosUnits),
					parentSpectra.unitsToPixelsV(this.yPosUnits));
			this.setSize(15, 15);
		}

		this.refreshSensitiveArea();
	}

	/**
	 * Paints this object.
	 * 
	 * @param g -
	 *            a graphic context.
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
			double xpos = parentSpectra.unitsToPixelsH(this.xPosUnits);
			double minpos = parentSpectra.unitsToPixelsH(this.xMin);
			double maxpos = parentSpectra.unitsToPixelsH(this.xMax);
			// SpectraDisplay disp = (SpectraDisplay)
			// parentSpectra.getParentEntity();
			// parentSpectra.getSpectraData().getMaxY();
			//			
			// double topLimit =
			// parentSpectra.unitsToPixelsV(parentSpectra.getSpectraData().getMaxY());
			// double bottomLimit = 0;

			if (this.isMouseover())
				g.setColor(Color.red);
			else
				g.setColor(Color.green);
			g.draw(new Line2D.Double(minpos - xpos, 0, maxpos - xpos, 0));
			g.draw(new Line2D.Double(minpos - xpos,
					-RangeSelectionLabel.tickLength, minpos - xpos,
					RangeSelectionLabel.tickLength));
			g.draw(new Line2D.Double(maxpos - xpos,
					-RangeSelectionLabel.tickLength, maxpos - xpos,
					RangeSelectionLabel.tickLength));
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
	public double getWidth() {
		Spectra parent = (Spectra) this.getParentEntity();
		return Math.abs(parent.unitsToPixelsH(this.xMin)
				- parent.unitsToPixelsH(this.xMax));
	}

}