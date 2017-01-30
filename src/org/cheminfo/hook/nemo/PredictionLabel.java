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
import java.util.Iterator;
import java.util.TreeSet;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.util.XMLCoDec;

/**
 * 
 * 
 * @author Marco Engeler
 * 
 * 
 */
public class PredictionLabel extends BasicEntity {
	public static int DEFAULT_WIDTH = 10;
	public static int DEFAULT_HEIGHT = 10;

	public static final int UNDEFINED_ATOM_ID = Integer.MIN_VALUE;

	// range specification
	private double xPos, yPos;
	private double xMin, yMin;
	private double xMax, yMax;

	// private int atomId1, atomId2; // we can have two atom id's for 2D spectra
	// multiple atoms ids need to be stored to deal with symmetry
	private TreeSet<Integer> atomIDS = new TreeSet<Integer>();
	private TreeSet<String> couplings = new TreeSet<String>();
	private TreeSet<Integer> xAtoms = new TreeSet<Integer>();
	private String compiledCouplings = null;

	private String text;

	private String comment = "";

	public PredictionLabel() {
		super();
		this.init();
		this.xPos = Double.NaN;
		this.yPos = Double.NaN;

		this.xMin = Double.NaN;
		this.yMin = Double.NaN;

		this.xMax = Double.NaN;
		this.yMax = Double.NaN;

		this.setPrimaryColor(Color.CYAN);

	}

	public PredictionLabel(double xPos, double yPos, double xMin, double yMin,
			double xMax, double yMax) {
		super();
		this.init();
		this.xPos = xPos;
		this.yPos = yPos;

		this.xMin = xMin;
		this.yMin = yMin;

		this.xMax = xMax;
		this.yMax = yMax;

		this.setPrimaryColor(Color.CYAN);

	}

	public PredictionLabel(PredictionLabel original) {
		super();
		this.init();
		this.xPos = original.xPos;
		this.yPos = original.yPos;

		this.xMin = original.xMin;
		this.yMin = original.yMin;

		this.xMax = original.xMax;
		this.yMax = original.yMax;

		this.atomIDS = (TreeSet<Integer>) original.atomIDS.clone();

		// this.setXArrayPoint(original.getXArrayPoint());
		// this.setYPos(original.getYPos());

		this.setPrimaryColor(original.getPrimaryColor());

	}

	public PredictionLabel(String XMLString, Hashtable helpers) {
		super();
		XMLCoDec tempCodec = new XMLCoDec(XMLString);
		tempCodec.shaveXMLTag();
		super.getSuperFromXML(tempCodec);
		this.setUniqueID(tempCodec.getParameterAsInt("uniqueID"));
		this.storeLinkIDs(tempCodec);

		// x data
		this.xPos = tempCodec.getParameterAsDouble("xPos");
		this.xMin = tempCodec.getParameterAsDouble("xMin");
		this.xMax = tempCodec.getParameterAsDouble("xMax");
		// y data
		this.yPos = tempCodec.getParameterAsDouble("yPos");
		this.yMin = tempCodec.getParameterAsDouble("yMin");
		this.yMax = tempCodec.getParameterAsDouble("yMax");
		//

		// atomIDS
		String atomIDS = tempCodec.getParameterAsString("atomIDS");
		String[] tokens;
		tokens = atomIDS.split(",");
		for (String s : tokens) {
			this.atomIDS.add(Integer.parseInt(s.trim()));
		}
		// atomIDS
		String xIDS = tempCodec.getParameterAsString("xids");
		tokens = atomIDS.split(",");
		for (String s : tokens) {
			this.xAtoms.add(Integer.parseInt(s.trim()));
		}
		
		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor"));
	}

	private void init() {
		this.setHomolink(false);
		// this.heterolink = true;
		this.setHighlightPropagationType(BasicEntity.HL_PROPAGATION_OUTBOUND);
	}
	
	public void setSize(Dimension size) {
		this.setSize(size.height, size.width);
	}

	public void setSize(int width, int height) {
		super.setSize(width, height);

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

	private void formatPredictionString() {
		DecimalFormat newFormat = new DecimalFormat();
		newFormat.applyPattern("#0.000");
		if (this.compiledCouplings == null) {
			StringBuffer buffer = new StringBuffer();
			for (String s : this.couplings) {
				buffer.append(s).append(',');
			}
			if (this.couplings.size() > 0) {
				buffer.deleteCharAt(buffer.length() - 1);
			}
			this.compiledCouplings = buffer.toString();
		}
		text = "Atom(" + this.compiledCouplings + ")=("
				+ newFormat.format(xPos) + "," + newFormat.format(yPos) + ")"
				+ " Range x=[" + newFormat.format(xMin) + ","
				+ newFormat.format(xMax) + "]," + " y=["
				+ newFormat.format(yMin) + "," + newFormat.format(yMax) + "]";
	}

	public String getDebugString() {
		DecimalFormat newFormat = new DecimalFormat();
		newFormat.applyPattern("#0.000");
		String text = "Atom(";
		Iterator<Integer> iterator = this.getAtomIDIterator();
		while (iterator.hasNext()) {
			text += iterator.next().toString() + " ";
		}
		text += ")=(" + newFormat.format(xPos) + "," + newFormat.format(yPos)
				+ ")" + " Range x=[" + newFormat.format(xMin) + ","
				+ newFormat.format(xMax) + "]," + " y=["
				+ newFormat.format(yMin) + "," + newFormat.format(yMax) + "]";
		return text;
	}

	public String getOverMessage() {
		DecimalFormat newFormat = new DecimalFormat();
		newFormat.applyPattern("#0.00");
		this.formatPredictionString();
		return this.getText();
	}

	public String getClickedMessage() {
		// DecimalFormat newFormat = new DecimalFormat();
		// newFormat.applyPattern("#0.00");
		//
		// XMLCoDec inputCodec1 = new XMLCoDec();
		// inputCodec1.addParameter("name", "peakXPos");
		// inputCodec1.addParameter("size", new Integer(3));
		//
		// XMLCoDec inputCodec2 = new XMLCoDec();
		// inputCodec2.addParameter("name", "comment");
		// inputCodec2.addParameter("size", new Integer(10));
		//
		// XMLCoDec buttonCodec = new XMLCoDec();
		// buttonCodec.addParameter("action",
		// "org.cheminfo.hook.nemo.PeakLabelAction");
		// buttonCodec.addParameter("image", "validate.gif");
		//
		// // encoding parameters in a local encoding requires to take
		// // care for the parsing of the String to a double
		//
		// String value = (newFormat.format(this.getXPos()));
		// return "<Text type=\"plain\">Peak Position: </Text><Input "
		// + inputCodec1.encodeParameters() + ">" + value
		// + "</Input><Text type=\"plain\"> - </Text><Input "
		// + inputCodec2.encodeParameters() + ">" + comment
		// + "</Input><Button " + buttonCodec.encodeParameters()
		// + "></Button>";
		return "";
	}

	protected void setComment(String newComment) {
		this.comment = newComment;
	}

	public void refreshSensitiveArea() {
		if (this.getParentEntity() != null) {
			Spectra parentSpectra = (Spectra) this.getParentEntity();
			if (parentSpectra.isDrawnAs2D()) {
				final int extent = 10;
				this.setSensitiveArea(new Area(new Rectangle2D.Double(-extent,
						-extent, extent * 2, extent * 2)));
			}
		}
	}

	public void checkSizeAndPosition() {
		Spectra parentSpectra = (Spectra) this.getParentEntity();
		double x0 = parentSpectra.unitsToPixelsH(this.xPos);
		double y0 = parentSpectra.unitsToPixelsV(this.yPos);
		double tempWidth = Math.abs(parentSpectra
				.unitsToPixelsH(this.getXMax())
				- parentSpectra.unitsToPixelsH(this.getXMin()));
		double tempHeight = Math.abs(parentSpectra.unitsToPixelsV(this
				.getYMax())
				- parentSpectra.unitsToPixelsV(this.getYMin()));
		this.setSize(tempWidth, tempHeight);
		this.setLocation(x0, y0);
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
		g.setFont(new Font("Serif", Font.PLAIN, 12));
		this.formatPredictionString();
		if (parentSpectra.isDrawnAs2D()) {
			if (parentSpectra.hasVisiblePredictionLabels() == 1) {
				// Dimension labelSize=this.getSize();
				if (this.isSelected() || this.isMouseover()
						|| this.isHighlighted()) {
					g.setColor(this.getSecondaryColor());
					g.setColor(Color.GREEN);
				} else {
					g.setColor(this.getPrimaryColor());
				}
				if (parentSpectra.getParentEntity() != null) {
					SpectraDisplay display = (SpectraDisplay) parentSpectra
							.getParentEntity();
					// get limits
					double cbL = display.getCurrentBottomLimit();
					double ctL = display.getCurrentTopLimit();
					double clL = display.getCurrentLeftLimit();
					double crL = display.getCurrentRightLimit();

					double xLoc = this.getLocation().x;
					double yLoc = this.getLocation().y;
					double x = parentSpectra.unitsToPixelsH(xPos) - xLoc;
					double y = parentSpectra.unitsToPixelsV(yPos) - yLoc;
					double halfWidth = PredictionLabel.DEFAULT_WIDTH / 2;
					double halfHeight = PredictionLabel.DEFAULT_HEIGHT / 2;
					if (Math.min(clL, crL) <= xPos && Math.max(cbL, clL) >= xPos
							&& Math.min(cbL, ctL) <= yPos
							&& Math.max(cbL, ctL) >= yPos) {
						g.draw(new Line2D.Double(x - halfWidth, y - halfHeight,
								x + halfWidth, y + halfHeight));
						g.draw(new Line2D.Double(x - halfWidth, y + halfHeight,
								x + halfWidth, y - halfHeight));
					}
					double xPixMin = this.xMin;
					double yPixMin = this.yMin;
					double xPixMax = this.xMax;
					double yPixMax = this.yMax;

					if (xPixMin < crL && xPixMin < clL) {
						xPixMin = Math.min(crL, clL);
					} else if (xPixMin > crL && xPixMin > clL) {
						xPixMin = Math.max(crL, clL);
					}
					if (yPixMin < ctL && yPixMin < cbL) {
						yPixMin = Math.min(ctL, cbL);
					} else if (yPixMin > ctL && yPixMin > cbL) {
						yPixMin = Math.max(ctL, cbL);
					}
					if (xPixMax < crL && xPixMax < clL) {
						xPixMax = Math.min(crL, clL);
					} else if (xPixMax > crL && xPixMax > clL) {
						xPixMax = Math.max(crL, clL);
					}
					if (yPixMax < ctL && yPixMax < cbL) {
						yPixMax = Math.min(ctL, cbL);
					} else if (yPixMax > ctL && yPixMax > cbL) {
						yPixMax = Math.max(ctL, cbL);
					}
					xPixMin = parentSpectra.unitsToPixelsH(xPixMin) - xLoc;
					yPixMin = parentSpectra.unitsToPixelsV(yPixMin) - yLoc;
					xPixMax = parentSpectra.unitsToPixelsH(xPixMax) - xLoc;
					yPixMax = parentSpectra.unitsToPixelsV(yPixMax) - yLoc;

					g.draw(new Rectangle2D.Double(Math.min(xPixMin, xPixMax),
							Math.min(yPixMin, yPixMax), Math.abs(xPixMin
									- xPixMax), Math.abs(yPixMin - yPixMax)));
				}
				//
			}
		}
	}

	public double getXPos() {
		return xPos;
	}

	public void setXPos(double pos) {
		xPos = pos;
	}

	public double getYPos() {
		return yPos;
	}

	public void setYPos(double pos) {
		yPos = pos;
	}

	public double getXMin() {
		return xMin;
	}

	public void setXMin(double min) {
		xMin = min;
	}

	public double getYMin() {
		return yMin;
	}

	public void setYMin(double min) {
		yMin = min;
	}

	public double getXMax() {
		return xMax;
	}

	public void setXMax(double max) {
		xMax = max;
	}

	public double getYMax() {
		return yMax;
	}

	public void setYMax(double max) {
		yMax = max;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void addAtomID(int atomId, boolean isX) {
		this.atomIDS.add(atomId);
		if (isX)
			this.xAtoms.add(atomId);
	}

	public boolean containsAtomID(int atomId) {
		return this.atomIDS.contains(new Integer(atomId));
	}

	public Iterator<Integer> getAtomIDIterator() {
		return this.atomIDS.iterator();
	}

	// TODO add similar features as peak label
	public String getXmlTag(Hashtable xmlProperties) {
		String tempTag = "";
		XMLCoDec tempCodec = new XMLCoDec();
		super.addSuperToXML(tempCodec);
		tempCodec.addParameter("primaryColor", this.getPrimaryColor().getRed()
				+ "," + this.getPrimaryColor().getGreen() + ","
				+ this.getPrimaryColor().getBlue());
		// save x data
		tempCodec.addParameter("xPos", new Double(this.xPos));
		tempCodec.addParameter("xMin", new Double(this.xMin));
		tempCodec.addParameter("xMax", new Double(this.xMax));
		// save y data
		tempCodec.addParameter("yPos", new Double(this.yPos));
		tempCodec.addParameter("yMin", new Double(this.yMin));
		tempCodec.addParameter("yMax", new Double(this.yMax));
		// link

		// LP20100122 The following 2 lines are I think useless because it is taken in addSuperToXML
	//	tempCodec.addParameter("heterolink", new Boolean(this.heterolink));
	//	tempCodec.addParameter("homolink", new Boolean(this.homolink));
		
		// atomIDS
		Iterator<Integer> iterator;
		String atomIDS = "";
		iterator = this.getAtomIDIterator();
		while (iterator.hasNext()) {
			atomIDS += iterator.next();
			if (iterator.hasNext())
				atomIDS += ",";
		}
		tempCodec.addParameter("atomIDS", atomIDS);
		// xids
		String xids = "";
		iterator = this.xAtoms.iterator();
		while (iterator.hasNext()) {
			xids += iterator.next();
			if (iterator.hasNext())
				xids += ",";
		}
		tempCodec.addParameter("xids", xids);
		//
		this.addLinkXMLElements(tempCodec);

		tempTag += "<nemo.PredictionLabel " + tempCodec.encodeParameters()
				+ ">\r\n";
		tempTag += "</nemo.PredictionLabel>\r\n";

		return tempTag;
	}

	public void addCoupling(int iAtom1, int iAtom2) {
		this.couplings.add(iAtom1 + ":" + iAtom2);
	}

	public String getComment() {
		return comment;
	}

	public boolean isXAtom(int atomID) {
		return this.xAtoms.contains(atomID);
	}
}