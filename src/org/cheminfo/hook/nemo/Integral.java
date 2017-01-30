package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.nemo.nmr.IntegralData;
import org.cheminfo.hook.util.NemoPreferences;
import org.cheminfo.hook.util.XMLCoDec;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class handles all functions related to integrals
 * <ul>
 * <li> calculates the absolute and relative areas based on its boundaries
 * <li> communicates with the parent Spectra to move all the Integrals when one
 * is moved
 * <li> paints the integrals
 * </ul>
 */
public class Integral extends BasicEntity {
	//private double startX;
	//private double stopX;
	private double startY, stopY;
	private int nbPoints;
	//private double area;
//	private double relArea;
	private double integralDrawingValues[];
	private double firstPointY = 0;
	private double firstPointOffset;
	private double minArea = 0;
	private double maxArea = 0;
	private int startIndex, stopIndex;
	private int startSubSpectra, stopSubSpectra;
	private double lastYPoint = 0;
	private Spectra parentSpectra;

	private static final int RADIUS = 2;

	//private String publicationValue = "";
	private IntegralData integralData;

	private boolean inversed = false;

	private float[] bufferedX, bufferedY; // these keep the local coordinates

	// (in pixels) of the points used
	// for drawing

	/**
	 * Creates an Integral object. Uses the provided limits and Spectra object.
	 * 
	 * @param inStartX -
	 *            a double representing the starting point for the integration.
	 * @param inStopX -
	 *            a double representing the ending point for the integration.
	 * @param currentSpectra -
	 *            Spectra object that provides the values for the integration.
	 */
	public Integral(double inStartX, double inStopX, Spectra currentSpectra) {
		super();

		this.parentSpectra = currentSpectra;
		double startX = inStartX;
		double stopX = inStopX;
		double area = 0;

		startIndex = (int) ((currentSpectra.getSpectraData().getFirstX() - startX)
				* currentSpectra.getSpectraData().getNbPoints() / (currentSpectra
				.getSpectraData().getFirstX() - currentSpectra.getSpectraData()
				.getLastX()));
		stopIndex = (int) ((currentSpectra.getSpectraData().getFirstX() - stopX)
				* currentSpectra.getSpectraData().getNbPoints() / (currentSpectra
				.getSpectraData().getFirstX() - currentSpectra.getSpectraData()
				.getLastX()));
		if (startIndex > stopIndex) {
			int tmp = stopIndex;
			stopIndex = startIndex;
			startIndex = tmp;
		}
		nbPoints = stopIndex - startIndex;

		integralDrawingValues = new double[nbPoints];

		// firstPointY=currentSpectra.getSpectraData().getMinY();
		firstPointY = 0;
		// Calculates the absolute area
		for (int i = startIndex; i < stopIndex; i++) {
			integralDrawingValues[i - startIndex] = currentSpectra
					.getSpectraData().getInterval()
					* (currentSpectra.getSpectraData().getY(i) - firstPointY);
			area += integralDrawingValues[i - startIndex];
			if (area < minArea)
				minArea = area;
			if (area > maxArea)
				maxArea = area;
		}

		firstPointOffset = currentSpectra.getSpectraData().getInterval()
				/ (this.maxArea - this.minArea);
		
		integralData = new IntegralData(startX,stopX,area);
		// Sets the relative area (first integral, no previous reference)
		if (IntegrationHelpers.getIntegralNb(currentSpectra) == 0) {
			currentSpectra.setIntegralsBaseArea(area);
			currentSpectra.currentRefIntegral = 0;
//			relArea = 1.00;
		}
		integralData.setBaseArea(currentSpectra.getIntegralsBaseArea());
		
		// Calculates the relative area
//		else
//			relArea = area / currentSpectra.getIntegralsBaseArea();

		this.setMovementType(VERTICAL);
		this.setPrimaryColor(Color.blue);
	}

	public void refreshDrawingValues() {
		Spectra currentSpectra = this.parentSpectra;
		double area = 0;
		this.minArea = 0;
		this.maxArea = 0;
		startIndex = (int) ((currentSpectra.getSpectraData().getFirstX() - integralData.getFrom())
				* currentSpectra.getSpectraData().getNbPoints() / (currentSpectra
				.getSpectraData().getFirstX() - currentSpectra.getSpectraData()
				.getLastX()));
		stopIndex = (int) ((currentSpectra.getSpectraData().getFirstX() - integralData.getTo())
				* currentSpectra.getSpectraData().getNbPoints() / (currentSpectra
				.getSpectraData().getFirstX() - currentSpectra.getSpectraData()
				.getLastX()));
		if (startIndex > stopIndex) {
			int tmp = stopIndex;
			stopIndex = startIndex;
			startIndex = tmp;
		}
		nbPoints = stopIndex - startIndex;

		integralDrawingValues = new double[nbPoints];

		// firstPointY=currentSpectra.getSpectraData().getMinY();
		firstPointY = 0;
		// Calculates the absolute area
		for (int i = startIndex; i < stopIndex; i++) {
			integralDrawingValues[i - startIndex] = currentSpectra
					.getSpectraData().getInterval()
					* (currentSpectra.getSpectraData().getY(i) - firstPointY);
			area += integralDrawingValues[i - startIndex];
			if (area < minArea)
				minArea = area;
			if (area > maxArea)
				maxArea = area;
		}

		firstPointOffset = currentSpectra.getSpectraData().getInterval()
				/ (this.maxArea - this.minArea);
		
		integralData.setValue(area);
		// // Sets the relative area (first integral, no previous reference)
		// if (IntegrationHelpers.getIntegralNb(currentSpectra) == 0) {
		// currentSpectra.currentRefIntegral = 0;
		// relArea = 1.00;
		// }
		// // Calculates the relative area
		// else
		// relArea = area / currentSpectra.getIntegralsBaseArea();

		this.setMovementType(VERTICAL);
		this.setPrimaryColor(Color.blue);
		this.bufferedX = null;
		this.bufferedY = null;
		this.setSize(0, 0);
		this.refreshDrawingArrays();
	}

	public Integral(double inStartX, double inStopX, double inStartY,
			double intStopY, Spectra currentSpectra) {
		this.parentSpectra = currentSpectra;
	}

	public Integral(String XMLString, Hashtable helpers) {
		super();

		Spectra currentSpectra = (Spectra) helpers.get("currentSpectra");
		this.parentSpectra = currentSpectra;
		XMLCoDec tempCodec = new XMLCoDec(XMLString);
		tempCodec.shaveXMLTag();

		this.setUniqueID(tempCodec.getParameterAsInt("uniqueID"));
		this.storeLinkIDs(tempCodec);

		startIndex = tempCodec.getParameterAsInt("startIndex");
		stopIndex = tempCodec.getParameterAsInt("stopIndex");
		double startX = currentSpectra.arrayPointToUnits(startIndex);
		double stopX = currentSpectra.arrayPointToUnits(stopIndex);
		startY = tempCodec.getParameterAsDouble("startYUnits");
		stopY = tempCodec.getParameterAsDouble("stopYUnits");
		String publicationValue = tempCodec.getParameterAsString("publicationValue");
		double area = 0;
		//relArea = 1.000;

		startIndex = (int) ((currentSpectra.getSpectraData().getFirstX() - startX)
				* currentSpectra.getSpectraData().getNbPoints() / (currentSpectra
				.getSpectraData().getFirstX() - currentSpectra.getSpectraData()
				.getLastX()));
		stopIndex = (int) ((currentSpectra.getSpectraData().getFirstX() - stopX)
				* currentSpectra.getSpectraData().getNbPoints() / (currentSpectra
				.getSpectraData().getFirstX() - currentSpectra.getSpectraData()
				.getLastX()));

		if (startY != stopY) {
			startSubSpectra = currentSpectra.unitsToSubSpectra(startY);
			stopSubSpectra = currentSpectra.unitsToSubSpectra(stopY);
		}

		if (startIndex > stopIndex) {
			int tmp = stopIndex;
			stopIndex = startIndex;
			startIndex = tmp;
		}
		nbPoints = stopIndex - startIndex;

		integralDrawingValues = new double[nbPoints];

		// firstPointY=currentSpectra.getSpectraData().getMinY();
		firstPointY = 0;
		// Calculates the absolute area
		for (int i = startIndex; i < stopIndex; i++) {
			integralDrawingValues[i - startIndex] = currentSpectra
					.getSpectraData().getInterval()
					* (currentSpectra.getSpectraData().getY(i) - firstPointY);
			area += integralDrawingValues[i - startIndex];
			if (area < minArea)
				minArea = area;
			if (area > maxArea)
				maxArea = area;
		}

		firstPointOffset = currentSpectra.getSpectraData().getInterval()
				/ (this.maxArea - this.minArea);

		integralData = new IntegralData(startX, stopX, area, publicationValue);
//		relArea = area / currentSpectra.getIntegralsBaseArea();

		this.setMovementType(VERTICAL);
		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor"));
	}
	
	public String getOverMessage() {
		DecimalFormat newFormat = new DecimalFormat();
		newFormat.applyPattern("#0.00");
		//System.out.println(integralData.getValue()+" "+integralData.getPublicationLabel()+" "+this.getRelArea());
		String tempString="Integral relative area: " + newFormat.format(this.getRelArea());
		tempString+=" (Absolute: "+newFormat.format(integralData.getValue())+")";
		if (! integralData.getPublicationLabel().equals("")) {
			tempString+=" (Publication intensity: "+integralData.getPublicationLabel()+")";
		}
		return tempString;
		//return "Hola";
	}

	public String getClickedMessage() {
		DecimalFormat newFormat = new DecimalFormat();
		newFormat.applyPattern("#0.00###");

		XMLCoDec inputCodec = new XMLCoDec();
		inputCodec.addParameter("name", "relArea");
		inputCodec.addParameter("size", new Integer(10));

		XMLCoDec publCodec = new XMLCoDec();
		publCodec.addParameter("name", "publicationValue");
		publCodec.addParameter("size", new Integer(10));

		XMLCoDec buttonCodec = new XMLCoDec();
		buttonCodec.addParameter("action", "org.cheminfo.hook.nemo.IntegralAction");
		buttonCodec.addParameter("image", "validate.gif");

		return "<Text type=\"plain\">Integral relative area: </Text><Input "
				+ inputCodec.encodeParameters() + ">"
				+ newFormat.format(this.getRelArea()) + "</Input>"
				+ "<Text type=\"plain\">Public.: </Text>" + "<Input "
				+ publCodec.encodeParameters() + ">" + integralData.getPublicationLabel()
				+ "</Input>" +
				"<Button " + buttonCodec.encodeParameters() + "></Button>";
	}

	/**
	 * Returns a boolean indicating whether the provided value is inside the
	 * boundaries of this Integral object. a boolean.
	 */
	protected boolean isInIntegral(double queryX) {
		if (queryX <= integralData.getFrom() && queryX >= integralData.getTo()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the absolute area of this Integral.
	 * 
	 * @return a double representing the absolute area.
	 */
	public double getArea() {
		return integralData.getValue();
	}

	/**
	 * Returns the Value of the highest integration point.
	 * 
	 * @return a double representing the integration at its highest point
	 */
	protected double getMaxArea() {
		return this.maxArea;
	}

	/**
	 * Returns the Value of the lowest integration point.
	 * 
	 * @return a double representing the integration at its lowest point
	 */
	protected double getMinArea() {
		return this.minArea;
	}

	/**
	 * Returns the relative area of this Integral.
	 * 
	 * @return a double representing the relative area.
	 */
	public double getRelArea() {
		if (this.getParentEntity() instanceof Spectra) {
			this.integralData.setBaseArea(((Spectra)this.getParentEntity()).getIntegralsBaseArea());
			return integralData.getRelArea() ;
		}
		return 1;
	}

	/**
	 * Returns the relative position of this integral's first point. This value
	 * is obtained by dividing the first element of the integralDrawingValues
	 * array by the differenc between the minima and maxima.
	 * 
	 * @return a double representing the relative position.
	 */
	protected double getFirstPointOffset() {
		return this.firstPointOffset;
	}

	/**
	 * Sets the starting point for the integration.
	 * 
	 * @param newStart -
	 *            the new starting point.
	 */
	protected void setStartX(double newStart) {
		this.integralData.setFrom(newStart);
		//this.startX = newStart;
	}

	/**
	 * Returns the starting point for the integration in units.
	 * 
	 * @return a double indicationg the X coordinate in units for the starting
	 *         point of the integration.
	 */
	public double getStartX() {
		return this.integralData.getFrom();//this.startX;
	}

	/**
	 * Sets the ending point for the integration.
	 * 
	 * @param newStop -
	 *            the new ending point.
	 */
	protected void setStopX(double newStop) {
		this.integralData.setTo(newStop);///this.stopX = newStop;
	}

	/**
	 * Returns the ending point for the integration in units.
	 * 
	 * @return a double indicationg the X coordinate in units for the ending
	 *         point of the integration.
	 */
	public double getStopX() {
		return this.integralData.getTo();
	}
	
	public void moveLocal(double deltaX, double deltaY) {
		if (this.getParentEntity() != null) {
			Spectra parentSpectra = (Spectra) this.getParentEntity();
			parentSpectra.setIntegralsBottom(parentSpectra.getIntegralsBottom()
					- deltaY);
		}
		
	}

	public void shiftObject(double shift) {
		this.setStartX(this.getStartX() - shift);
		this.setStopX(this.getStopX() - shift);
	}

	protected void setStartPoint(int newStartPoint) {
		this.startIndex = newStartPoint;
	}

	protected int getStartPoint() {
		return this.startIndex;
	}

	protected void setStopPoint(int newStopPoint) {
		this.stopIndex = newStopPoint;
	}

	protected int getStopPoint() {
		return this.stopIndex;
	}

	protected boolean isInversed() {
		return this.inversed;
	}

	protected void switchInversed() {
		if (inversed == false)
			inversed = true;
		else
			inversed = false;
	}

	int unitsToPixels(double inValue) {
		return (int) ((this.integralData.getFrom() - inValue) * this.getWidth() / (this.integralData.getFrom() - this.integralData.getTo()));
	}

	private void refreshDrawingArrays() {
		//System.out.println("refreshDrawingArrays "+this.nbPoints);
		if(this.nbPoints<=1)
			return;
		this.bufferedX = new float[this.nbPoints - 1];
		this.bufferedY = new float[this.nbPoints - 1];

		double previousPointY = 0;

		if (((Spectra) this.getParentEntity()).getSpectraData().getFirstX() > ((Spectra) this
				.getParentEntity()).getSpectraData().getLastX()) {
			for (int point = 0; point < this.nbPoints - 1; point++) {
				bufferedX[point] = (float) (point * this.getWidth() / this.nbPoints);
				bufferedY[point] = (float) ((this.getHeight() - 1) * ((previousPointY
						+ this.integralDrawingValues[point] - minArea) / (maxArea - minArea)));
				previousPointY += this.integralDrawingValues[point];
			}
		} else {
			for (int point = 0; point < this.nbPoints - 1; point++) {
				bufferedX[point] = (float) (point * this.getWidth() / this.nbPoints);
				bufferedY[point] = (float) ((this.getHeight() - 1) * (1.0 - (previousPointY
						+ this.integralDrawingValues[point] - minArea)
						/ (maxArea - minArea)));

				previousPointY += this.integralDrawingValues[point];
			}
		}
		this.lastYPoint = previousPointY;
		this.checkSizeAndPosition();
	}

	public void checkSizeAndPosition() {
		double tempWidth, tempHeight, tempX, tempY;
		//System.out.println("Integral1");
		if (((Spectra) this.getParentEntity()).hasVisibleIntegrals() == 1) {
			tempWidth = Math.abs(parentSpectra.unitsToPixelsH(this.getStartX())
					- parentSpectra.unitsToPixelsH(this.getStopX()));
			tempHeight = (int) Math.abs((parentSpectra.getHeight() / 5.0)
					* parentSpectra.getIntegralsMultFactor()
					* (this.getMaxArea() - this.getMinArea())
					/ parentSpectra.getIntegralsBaseArea());
			if (tempHeight < 1)
				tempHeight = 1;
		} else {
			tempHeight = tempWidth = 0;
		}
		//System.out.println("Integral2");
		tempX = parentSpectra.unitsToPixelsH(parentSpectra
				.arrayPointToUnits(this.getStartPoint()));
		tempY = parentSpectra.getHeight() - parentSpectra.getIntegralsBottom()
				- tempHeight
				+ (int) (this.getHeight() * this.getFirstPointOffset());
		
		//System.out.println("Integral3 "+tempX+" "+tempY+" "+tempWidth+" "+tempHeight);
		
		if (this.getWidth() != tempWidth || this.getHeight() != tempHeight) {
			this.setSize(tempWidth, tempHeight);
			this.refreshDrawingArrays();
		}
		//System.out.println("Integral4");
		this.setLocation(tempX, tempY);

		this.refreshSensitiveArea();
	}

	/**
	 * Paints this object
	 * 
	 * @param g -
	 *            The graphics context.
	 */
	public void paint(Graphics2D g) {
		String tempString;

		g.setFont(new Font("Serif", Font.PLAIN, 10));

		g.setColor(this.getPrimaryColor());
		if (parentSpectra.hasVisibleIntegrals() == 1) // Not very Beautiful
		{
			Area newArea = new Area(new Rectangle2D.Double(-10, this
					.getHeight() - 10, 20, 20));
			newArea.add(new Area(new Rectangle2D.Double(this.getWidth() - 10,
					-10, 20, 20)));
			if (parentSpectra.isDrawnAs2D() == false) {
				// double previousPointY=0;
				if (bufferedX == null)
					this.refreshDrawingArrays();
				else {
					GeneralPath polyline = new GeneralPath(
							GeneralPath.WIND_EVEN_ODD, bufferedX.length);

					polyline.moveTo(bufferedX[0], bufferedY[0]);
					for (int index = 1; index < bufferedX.length; index++) {
						polyline.lineTo(bufferedX[index], bufferedY[index]);
					}

					if (isSelected()) {
						g.fillRect(0, (int) this.getHeight() - 5, 5, 5);
						g.fillRect((int) this.getWidth() - 5, 0, 5, 5);
					}
					if (isMouseover() || isHighlighted()) {
						g.fillRect(0, (int) this.getHeight() - 3, 3, 3);

						g.fillRect((int) this.getWidth() - 3, 0, 3, 3);
					}


					g.setColor(this.getPrimaryColor());
					if (this.getInteractiveSurface() != null)
						g.setStroke(this.getInteractiveSurface().getNarrowStroke());

					g.draw(polyline);
				}
				if(NemoPreferences.getInstance().USE_NEW_INTEGRAL_DISPLAY!=null&&NemoPreferences.getInstance().get(NemoPreferences.getInstance().USE_NEW_INTEGRAL_DISPLAY))
				{
					// Draws the bottom label
					double x = this.getWidth() / 2;
					double y = this.getHeight() / 2;
	
					Vector<BasicEntity> topEntities = getParentEntity().getParentEntity().getEntities();
					for (BasicEntity entity : topEntities) {
						if(entity instanceof HorizontalScale)
						{
							AffineTransform origTransform = (AffineTransform) g.getTransform().clone();
							
							HorizontalScale horizontalScale = (HorizontalScale)entity;
							
							
													
							double offset = 0;
							for (BasicEntity basicEntity : topEntities) {
								if(basicEntity instanceof Spectra && basicEntity == this.parentSpectra)
									offset = basicEntity.getLocation().y;
							}
							
							double labelOffset = horizontalScale.getLocation().y-getLocation().y - offset;
							
							AffineTransform tr = g.getTransform();
							tr.translate(0,  labelOffset);
	
							g.setTransform(tr);
							
							Color foregroundColor = this.getPrimaryColor();
							Color transparentForegroundColor = new Color(foregroundColor.getRGB() & 0x33FFFFFF, true);
							
							g.setFont(horizontalScale.getCurrentFont());
							DecimalFormat newFormat = new DecimalFormat();
							newFormat.applyPattern("#0.00");
	
							tempString = (newFormat.format(this.getRelArea()));
	
							Rectangle2D stringRect = g.getFontMetrics().getStringBounds(tempString, g);						
							
							Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, getWidth(), Math.max(horizontalScale.getHeight(),30));
							g.setColor(transparentForegroundColor);
							g.fill(rect);
							
							Rectangle2D.Double sensitiveArea = new Rectangle2D.Double(0, labelOffset, getWidth(), Math.max(horizontalScale.getHeight(),30));
							newArea.add(new Area(sensitiveArea));
							
							g.setColor(foregroundColor.darker().darker());
							tr.rotate(Math.PI/2.0);
							tr.translate(Math.max(horizontalScale.getHeight()-stringRect.getWidth()-2,0), -(getWidth()/2) + ((double)horizontalScale.getCurrentFont().getSize())/2 -1 );
							g.setTransform(tr);
							g.drawString(tempString, 0, 0);
	
							g.setTransform(origTransform);
						}
					}
				}
				else
				{
	 				// Draws the central label
	 				if (this.getHeight() > 20 && this.getWidth() > 30) {
	 					double x = this.getWidth() / 2;
	 					double y = this.getHeight() / 2;
	 
	 					g.setColor(Color.white);
	 					g.fill(new Rectangle2D.Double(x - 15, y - 10, 30, 20));
	 					Rectangle2D.Double rect = new Rectangle2D.Double(x - 15, y - 10, 30, 20);
	 					g.draw(rect);
	 					newArea.add(new Area(rect));
	 
	 					g.setStroke(this.getInteractiveSurface().getNarrowStroke());
	 					g.setColor(this.getPrimaryColor());
	 					g.draw(new Rectangle2D.Double(x - 15, y - 10, 30, 20));
	 
	 					DecimalFormat newFormat = new DecimalFormat();
	 					newFormat.applyPattern("#0.00");
	 
	 					tempString = (newFormat.format(this.getRelArea()));
	 
	 					Dimension stringDimensions = new Dimension(g
	 							.getFontMetrics().stringWidth(tempString), g
	 							.getFontMetrics().getHeight());
	 					g.drawString(tempString,
	 							(float) (x - stringDimensions.width / 2),
	 							(float) (y + stringDimensions.height / 2));
	 				} else {
	 					// although we do not draw a label we still need a hot spot
	 					double x = this.getWidth() / 2;
	 					double y = this.getHeight() / 2;
	 					Ellipse2D.Double el = new Ellipse2D.Double(x - RADIUS, y
	 							- RADIUS, 2 * RADIUS, 2 * RADIUS);
	 					g.fill(el);
	 					newArea.add(new Area(el));
	 				}
				}
			}
			this.setSensitiveArea(newArea);
		}
	}

	public String getXmlTag(Hashtable xmlProperties) {
		XMLCoDec tempCodec = new XMLCoDec();
		String tempTag = "";

		tempCodec.addParameter("startIndex", new Integer(this.startIndex));
		tempCodec.addParameter("stopIndex", new Integer(this.stopIndex));
		tempCodec.addParameter("primaryColor", this.getPrimaryColor().getRed()
				+ "," + this.getPrimaryColor().getGreen() + ","
				+ this.getPrimaryColor().getBlue());
		tempCodec.addParameter("publicationValue", this.integralData.getPublicationLabel());
		
		this.addLinkXMLElements(tempCodec);

		tempTag += "<nemo.Integral " + tempCodec.encodeParameters() + ">\r\n";
		tempTag += "</nemo.Integral>\r\n";

		return tempTag;
	}

	public String getNmrTable() {
		DecimalFormat newFormat = new DecimalFormat();
		newFormat.applyPattern("#0.00");
		return ((int) Math.round(this.getRelArea())) + "\t"
				+ newFormat.format(this.getRelArea());
	}

	public double getLastYPoint() {
		return lastYPoint;
	}

	public double getFirstYPoint() {
		return this.firstPointY;
	}

	public void setLastYPoint(double lastYPoint) {
		this.lastYPoint = lastYPoint;
	}

	public double getMaxBufferedY() {
		return Math.max(this.bufferedY[0],
				this.bufferedY[this.bufferedY.length - 1]);
	}

	public double getMinBufferedY() {
		return Math.min(this.bufferedY[0],
				this.bufferedY[this.bufferedY.length - 1]);
	}

	public int getStartSubSpectra() {
		return startSubSpectra;
	}

	public int getStopSubSpectra() {
		return stopSubSpectra;
	}

	public String getPublicationValue() {
		return this.integralData.getPublicationLabel();
	}

	public void setPublicationValue(String publicationValue) {
		this.integralData.setPublicationLabel(publicationValue);
	}
	
	public JSONObject toJSON(){
		return this.integralData.toJSON();
	}

	public IntegralData getIntegralData() {
		return integralData;
	}

	public void setIntegralData(IntegralData integralData) {
		this.integralData = integralData;
	}
}
