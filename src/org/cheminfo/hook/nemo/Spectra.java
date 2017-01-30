package org.cheminfo.hook.nemo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.cheminfo.hook.converter.Converter;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.math.util.MathUtils;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.filters.FilterManager;
import org.cheminfo.hook.nemo.filters.FilterType;
import org.cheminfo.hook.nemo.filters.SpectraFilter;
import org.cheminfo.hook.nemo.nmr.ExperimentType;
import org.cheminfo.hook.nemo.nmr.NmrHelpers;
import org.cheminfo.hook.nemo.nmr.Nucleus;
import org.cheminfo.hook.nemo.nmr.PredictionData;
import org.cheminfo.hook.nemo.nmr.simulation.util.SimulateFromDescription;
import org.cheminfo.hook.util.Base64;
import org.cheminfo.hook.util.Segment;
import org.cheminfo.hook.util.XMLCoDec;
import org.cheminfo.scripting.spectradata.SpectraDataExt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class handles all spectrum functions:
 * <ul>
 * <li> Integral management.
 * <li> Peak picking management.
 * <li> Selection.
 * <li> Painting the spectrum.
 * </ul>
 */
public class Spectra extends BasicEntity {
	final static int SMOOTHING_NONE = 0;
	final static int SMOOTHING_LINE = 1;
	final static int SMOOTHING_GAUSS = 2;
	private final static boolean DEBUG = false;
	private boolean isVisible = true;

	private DecimalFormat numberFormat = null;

	public static final int DEFAULT_NB_CONTOURS = 10;

	public final static Color DEFAULT_INTEGRAL_COLOR = Color.blue;
	protected SpectraData spectraData = null;

	private int spectraNb; // represents the index in SpectraData for the
	// spectrum this object must display

	protected Vector chromatogramPeaks;
	protected Vector posContourLines, negContourLines;

	protected int currentRefIntegral = -1; // current reference integral (for
	// normalization)

	private double currentLeftLimit, currentRightLimit, currentTopLimit,currentBottomLimit;
	private double currentTopSpectra, currentBottomSpectra;
	private double lowerContourline = Double.NEGATIVE_INFINITY;

	private double multFactor = 0.9;
	private double integralsMultFactor = 1;
	private double integralsBaseArea;
	protected double integralsBottom = 50;

	private Color defaultLabelColor = Color.red;

	private Vector tempLabels; // these labels are used by the manual smart
	// peakpicking

	// private BaselinePoint tempBaselinePoint=null;

	private int hasVisibleIntegrals = 1;
	private int hasVisiblePeakLabels = 1;
	private int hasVisiblePredictionLabels = 1;

	private PredictionData predictionData = null;

	private String reference;

	private boolean drawAs2D = false;
	private boolean isInverted = false;

	private boolean isVertical = false;
	private boolean isSmooth = false;

	private int smoothing = Spectra.SMOOTHING_NONE;	
	
	private float[] bufferedX = null;
	private float[] bufferedY = null; // these keep the local coordinates (in
	// pixels) of the points used for
	// drawing

	private NMR2DTileManager tileManager = null;

	
	// Extra lines are just lines that are added to simplify assignment
	// The lines may be horizontal or vetical
	private Vector<Double> hExtraLines=new Vector<Double>();
	private Vector<Double> vExtraLines=new Vector<Double>();
	final static private Color extraLinesColor=Color.GRAY;

	private JSONArray jsonResult=null;
	
	public JSONArray getJsonResult() {
		return jsonResult;
	}

	public void setJsonResult(JSONArray jsonResult) {
		this.jsonResult = jsonResult;
	}

	
	/**
	 * Creates a empty Spectra object.
	 */
	protected Spectra() {
		super();

		this.chromatogramPeaks = new Vector();

		this.posContourLines = new Vector();
		this.negContourLines = new Vector();

		this.tempLabels = new Vector();

		this.setMovementType(BasicEntity.VERTICAL);

		this.setPrimaryColor(Color.black);
		this.setSecondaryColor(null);

		this.setLinkable(false);

		this.numberFormat = new DecimalFormat();
		this.numberFormat.applyPattern("#0.00");
		this.setErasable(false);
	}

	/**
	 * Creates a Spectra object based on the data contained in spectraData.
	 * 
	 * @param spectraData -
	 *            the SpectraData object containing the raw data for this
	 *            Spectra.
	 */
	public Spectra(SpectraData spectraData) {
		this();
		if (spectraData.getDefaults()!=null && spectraData.getDefaults().absoluteYScale) {
			this.multFactor=1;
		}
		if (spectraData.is2D()) {
			this.setSecondaryColor(Color.green);
		}
		this.spectraData = spectraData;
		testSmoothness();
	}
	
	public void testSmoothness() {
		if (this.spectraData != null) {
			if (this.spectraData.isDataClassXY()) {
				this.isSmooth(true);
				return;				
			}
			
			int nbPoints = this.spectraData.getNbPoints();
			if (DEBUG) System.out.println("nbPoints=" + nbPoints);
			// if (nbPoints>10000) {
			// 	this.isSmooth(true);
			// 	return;
			// }
			if (this.spectraData.isDataClassPeak() && nbPoints > 200) {
				double minDeltaX = Double.MAX_VALUE;
				double maxDeltaX = Double.MIN_VALUE;
				for (int i = 0; i < nbPoints - 1; i++) {
					double dx = Math.abs(this.spectraData.getX(i + 1) - this.spectraData.getX(i));
					if (minDeltaX > dx)
						minDeltaX = dx;
					if (maxDeltaX < dx)
						maxDeltaX = dx;
				}
				if (maxDeltaX < 2 * minDeltaX) {
					if (DEBUG) System.out.println("is Smooth");
					this.isSmooth(true);
				} else {
					if (DEBUG) System.out.println("Not smooth: "+minDeltaX+"  - "+maxDeltaX);
				}
			}
		}
	}

	/**
	 * Creates a new Spectra object by cloning the specified spectra.
	 * 
	 * @param original -
	 *            the original Spectra object.
	 * @deprecated
	 */
	public Spectra(Spectra original) {
		this();
	}

	/**
	 * Creates a new Spectra object from the characteristics contained in the
	 * XML String.
	 * 
	 * @param originalSpectraData
	 *            if this parameter is not null, the Constructor ignores the
	 *            Spectra parameters in the XMLTag and uses the SpectraData
	 *            instead. All subentities includede in the tag are conserved.
	 */
	public Spectra(String XMLString, double parentWidth, double parentHeight, Hashtable helpers) {
		this(XMLString, parentWidth, parentHeight, helpers, false);
	}
	
	/**
	 * Creates a new Spectra object from the characteristics contained in the
	 * XML String.
	 * 
	 * @param originalSpectraData
	 *            if this parameter is not null, the Constructor ignores the
	 *            Spectra parameters in the XMLTag and uses the SpectraData
	 *            instead. All subentities includede in the tag are conserved.
	 */
	public Spectra(String XMLString, double parentWidth, double parentHeight, Hashtable helpers, boolean storeTotalJcamp) {
		this();
		// apply filters
	
		XMLCoDec tempCodec = new XMLCoDec(XMLString);
		tempCodec.shaveXMLTag();
		
		this.setUniqueID(tempCodec.getParameterAsInt("uniqueID"));
		this.storeLinkIDs(tempCodec);

		if (helpers.containsKey("originalSpectraData") == false) {
			if (tempCodec.hasParameter("encodedJCamp") == false || tempCodec.getParameterAsString("encodedJCamp").compareTo("") == 0){
				if (tempCodec.hasParameter("simulationDescriptor") == true
						&& tempCodec.getParameterAsString("simulationDescriptor").compareTo("") != 0) {
					int usePredictionData = 0;
					if (tempCodec.hasParameter("simulateWithPredictionData"))
						usePredictionData = tempCodec.getParameterAsInt("simulateWithPredictionData");
					SpectraData spectraData = null;
					if (usePredictionData == 1) {
						PredictionData predictionData = new PredictionData();
						predictionData.setXNucleus(Nucleus.NUC_1H);
						predictionData.setInputData(tempCodec.getParameterAsString("simulationDescriptor"));
						spectraData = predictionData.resurrectOneDim().getSpectraData();
						this.setPredictionData(predictionData);
						this.bufferedX = null;
						if (DEBUG) System.out.println("maxY=" + spectraData.getMaxY());
						if (DEBUG) System.out.println("minY=" + spectraData.getMinY());
					} else {
						spectraData = SimulateFromDescription.simulate(tempCodec.getParameterAsString("simulationDescriptor"), "{}");
					}
					this.spectraData = spectraData;
				} else {
					if (DEBUG) System.out.println("YOOP using url spectra creator");
					Converter tempConverter = Converter.getConverter("Jcamp", storeTotalJcamp);
					SpectraData spectraData = new SpectraData();
					URL correctUrl = null;

					if (DEBUG) System.out.println("YOOP URL:"+tempCodec.getParameterAsString("url"));

					try {
						correctUrl = new URL(tempCodec.getParameterAsString("url"));
					} catch (MalformedURLException e) {
						System.out.println(e.toString() + " converting "+ correctUrl);
					}

					if ((tempConverter == null) || (correctUrl==null)) {
						this.spectraData=new SpectraData();
					} else {
						tempConverter.convert(correctUrl, spectraData);

						spectraData.setActiveElement(0);

						spectraData.prepareSpectraData();
						spectraData.updateDefaults();

						this.spectraData = spectraData;
					}
				}

			} else {
			//	System.out.println("YOOP parameter is not empty!");

				try {
					byte[] decodedBA = Base64.decode(tempCodec.getParameterAsString("encodedJCamp"));

					// -> unzip

					ByteArrayOutputStream baosUnzippedBA = new ByteArrayOutputStream();
					byte[] outBuf = new byte[512];

					ByteArrayInputStream baisByteArrayToUnzip = new ByteArrayInputStream(
							decodedBA);
					ZipInputStream zIs = new ZipInputStream(
							baisByteArrayToUnzip);

					zIs.getNextEntry();
					int lengthUnzip;

					while ((lengthUnzip = zIs.read(outBuf)) != -1) {
						baosUnzippedBA.write(outBuf, 0, lengthUnzip);
					}

					StringReader sReader = new StringReader(new String(
							baosUnzippedBA.toByteArray()));
					BufferedReader bufferedReader = new BufferedReader(sReader);

					Converter tempConverter = Converter.getConverter("Jcamp", storeTotalJcamp);
					SpectraData spectraData = new SpectraData();

					tempConverter.convert(bufferedReader, spectraData);
					spectraData.setActiveElement(0);

					spectraData.prepareSpectraData();
					spectraData.updateDefaults();

					this.spectraData = spectraData;
				} catch (IOException ioe) {
					System.out.println("Spectra XML constructor: " + ioe);
				}
			}

		} else {
			System.out.println("Loaded from existing");
			this.spectraData = (SpectraData) helpers.get("originalSpectraData");
		}

		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor"));
		this.setSecondaryColor(tempCodec.getParameterAsColor("secondaryColor"));

		this.setLocation(tempCodec.getParameterAsDouble("relX") * parentWidth,
				tempCodec.getParameterAsDouble("relY") * parentHeight);
		this.setSize(tempCodec.getParameterAsDouble("relWidth") * parentWidth,
				tempCodec.getParameterAsDouble("relHeight") * parentHeight);

		this.multFactor = tempCodec.getParameterAsDouble("magFactor");
		if (this.multFactor == 0)
			this.multFactor = 1;

		this.lowerContourline = tempCodec.getParameterAsDouble("lowerContour");
		this.integralsBottom = (int) (tempCodec.getParameterAsDouble("integralsRelBottom") * this.getHeight());

		this.integralsMultFactor = tempCodec.getParameterAsDouble("integralsMagFactor");
		if (this.integralsMultFactor == 0)
			this.integralsMultFactor = 1;
		this.integralsBaseArea = tempCodec.getParameterAsDouble("integralsBaseArea");

		this.hasVisibleIntegrals = tempCodec.getParameterAsInt("hasIntegrals");
		this.hasVisiblePeakLabels = tempCodec.getParameterAsInt("hasPeakLabels");
		this.hasVisiblePredictionLabels = tempCodec.getParameterAsInt("hasPredictionLabels");
		this.isVertical = tempCodec.getParameterAsBoolean("isVertical");

		this.setSpectraNb(0); // WARNING!! for test only!! The first spectrum
		// may not be the one we want in the main
		// display

		this.isSmooth(tempCodec.getParameterAsBoolean("isSmooth"));

		if (tempCodec.getParameterAsInt("is2D") == 1) {
			this.drawAs2D();
		} else { // we need to force the secondary color to null otherwise it is yellow !!!!
			this.setSecondaryColor(null);
		}

		if (tempCodec.hasParameter("firstX")) { // compatibility with older format
			this.spectraData.setFirstX(tempCodec.getParameterAsDouble("firstX"));
			this.spectraData.setLastX(tempCodec.getParameterAsDouble("lastX"));
		}
		
		//double firstX = this.spectraData.getFirstX();
		//double lastX = this.spectraData.getLastX();

		
		int elements = tempCodec.getRootElementsCount();
		helpers.put("currentSpectra", this);

		for (int elem = 0; elem < elements; elem++) {
			XMLCoDec tempCodec2 = new XMLCoDec(tempCodec.readXMLTag());
			tempCodec2.shaveXMLTag();
			String tagName = tempCodec2.getParameterAsString("tagName").trim();
			if (tagName.equals("nemo.Spectra.Process")) {
				this.runScript(tempCodec2.getXMLString());
				tempCodec.popXMLTag();
				continue;
			}
			try {
				Class entityClass = Class.forName("org.cheminfo.hook."
						+ tempCodec2.getParameterAsString("tagName").trim());
				Class[] parameterClasses = { String.class, Hashtable.class };
				java.lang.reflect.Constructor entityConstructor = entityClass.getConstructor(parameterClasses);

				Object[] parameters = { tempCodec.popXMLTag(), helpers };
				this.addEntity((BasicEntity) entityConstructor.newInstance(parameters));
			} catch (Exception e) {
				System.out.println("Spectra XML constructor e: " + e
						+ ", cause: " + e.getCause() + "--"
						+ tempCodec2.getParameterAsString("tagName"));
			}
		}
		
	}

	public void setPrimaryColor(Color color) {
		super.setPrimaryColor(color);
		if (this.tileManager != null)
			this.tileManager.forceRedraw();

		if (this.getInteractiveSurface() != null)
			this.getInteractiveSurface().repaint();
	}

	public void setSecondaryColor(Color color) {
		super.setSecondaryColor(color);
		if (this.tileManager != null)
			this.tileManager.forceRedraw();

		if (this.getInteractiveSurface() != null)
			this.getInteractiveSurface().repaint();
	}

	/**
	 * Set the SubSpectraData to display
	 */
	public void setSpectraNb(int newSpectraNb) {
		double oldFirst = this.getSpectraData().getFirstX();
		double oldLast = this.getSpectraData().getLastX();

		this.spectraNb = newSpectraNb;
		this.isSmooth = this.getSpectraData().isFakePeaks();

		this.testSmoothness();

		if (this.getParentEntity() != null) {
			if (oldFirst != this.getSpectraData().getFirstX()
					&& oldLast != this.getSpectraData().getLastX()) {
				this.getSpectraDisplay().checkFulloutLimits();
				this.getSpectraDisplay().fullSpectra();
			} else
				this.getInteractiveSurface().repaint();
		}
	}

	/**
	 * Get the currently displayed SubSpectraData
	 */
	protected int getSpectraNb() {
		return this.spectraNb;
	}

	public void setReferenceName(String name) {
		this.reference = name;
	}

	protected String getReferenceName() {
		return this.reference;
	}

	/**
	 * Returns the SpectraData object referred by this object and sets its
	 * active Element accordingly.
	 * 
	 * @return a SpectraData object containing all the raw data used by this
	 *         Spectra.
	 */
	public SpectraData getSpectraData() {
		this.spectraData.setActiveElement(this.spectraNb);
		return this.spectraData;
	}

	/**
	 * Sets the current visual limits for this Spectra to the specified values.
	 * 
	 * @param newLeftLimit -
	 *            a double containing the value in units for the new left visual
	 *            limit.
	 * @param newRightLimit -
	 *            a double containing the value in units for the new right
	 *            visual limit.
	 */
	protected void setCurrentLimits(double newLeftLimit, double newRightLimit) {
		currentLeftLimit = newLeftLimit;
		currentRightLimit = newRightLimit;
	}

	protected void setCurrentLimits(double newLeftLimit, double newRightLimit, double newTopLimit, double newBottomLimit) {
		this.currentLeftLimit = newLeftLimit;
		this.currentRightLimit = newRightLimit;
		this.currentTopLimit = newTopLimit;
		this.currentTopSpectra = (this.spectraData.getNbSubSpectra() * ((newTopLimit - this.spectraData
				.getFirstX()) / (this.spectraData.getLastX() - this.spectraData
				.getFirstX())));
		this.currentBottomLimit = newBottomLimit;
		this.currentBottomSpectra = (this.spectraData.getNbSubSpectra() * ((newBottomLimit - this.spectraData
				.getFirstX()) / (this.spectraData.getLastX() - this.spectraData
				.getFirstX())));
	}

	/**
	 * Returns the current visual left limit for this Spectra.
	 * 
	 * @return a double containing the X coordinate in units of the current left
	 *         visual limit.
	 */
	protected double getCurrentLeftLimit() {
		if (this.isVertical)
			return this.getSpectraDisplay().getCurrentBottomLimit();
		else
			return this.getSpectraDisplay().getCurrentLeftLimit();
		// return currentLeftLimit;
	}

	protected double getCurrentTopLimit() {
		return currentTopLimit;
	}

	protected double getCurrentBottomLimit() {
		return currentBottomLimit;
	}

	protected double getCurrentRightLimit() {
		if (this.isVertical)
			return this.getSpectraDisplay().getCurrentTopLimit();
		else
			return this.getSpectraDisplay().getCurrentRightLimit();
	}

	public void checkVerticalLimits() {
		if (this.getParentEntity() != null && !this.getSpectraDisplay().is2D()) {
			SpectraDisplay parentDisplay = (SpectraDisplay) this.getParentEntity();

			double unitsPerPixel = (this.getSpectraData().getMaxY() - this.getSpectraData().getMinY()) / (this.getHeight()) / this.getMultFactor();
			double topLimit = this.getLocation().y * unitsPerPixel
					+ (this.getSpectraData().getMaxY() - this.getSpectraData().getMinY()) / this.getMultFactor()
					+ this.getSpectraData().getMinY();
			double bottomLimit = this.getLocation().y * unitsPerPixel
					+ this.getSpectraData().getMinY();
			this.setCurrentLimits(this.getCurrentLeftLimit(), this.getCurrentRightLimit(), topLimit, bottomLimit);
			parentDisplay.setCurrentLimits(parentDisplay.getCurrentLeftLimit(), parentDisplay.getCurrentRightLimit(), topLimit, bottomLimit);
		}
	}

	/**
	 * Returns the reference string for this object
	 * 
	 * @return a String representing the reference name for this Spectra object.
	 */
	protected String getReference() {
		if (reference == null)
			return "";
		return this.reference;
	}

	protected double getMultFactor() {
		return this.multFactor;
	}

	/*protected void setMultFactor(double newMultFactor) {
		this.multFactor = newMultFactor;
	}*/
	public void setMultFactor(double newMultFactor) {
		this.multFactor = newMultFactor;
	}
	
	protected void setDefaultLabelColor(Color newColor) {
		this.defaultLabelColor = newColor;
	}

	public String getOverMessage() {
		if (this.isDrawnAs2D()) {
			InteractiveSurface interactions = this.getInteractiveSurface();
			double x = interactions.getContactPoint().x;
			double y = interactions.getContactPoint().y;

			return "Spectrum title: "
					+ spectraData.getTitle()
					+ " "
					+ spectraData.getParamString("OWNER", "No owner info")
					+" x="
					+ this.numberFormat.format(this.pixelsToUnits(x-this.getLocation().x))
					+ " y="
					+ this.numberFormat.format(this.pixelsToUnitsV(y-this.getLocation().y));
		} else {
			return "Spectrum title: " + spectraData.getTitle() + " "
					+ spectraData.getParamString("OWNER", "No owner info");
		}
	}

	public String getClickedMessage() {
		return getOverMessage();
	}

	public void isVertical(boolean flag) {
		// System.out.println("SET is vertical from: "+isVertical+" to: "+flag);
		this.isVertical = flag;
	}

	public boolean isVertical() {
		return this.isVertical;
	}
	
	protected void isSmooth(boolean flag) {
		this.isSmooth = flag;
	}

	protected boolean isSmooth() {
		return this.isSmooth;
	}

	protected boolean isSimulated() {
		if ((this.spectraData.getTitle()!=null) && (this.spectraData.getTitle().indexOf("Simulated spectrum") != -1))
			return true;
		else
			return false;
	}

	// ---------------------------------------------------------------------------
	// Conversion methods
	// ---------------------------------------------------------------------------
	/**
	 * Returns the value in pixels corresponding to a X-value in units. This
	 * method makes use of currentLeftLimit, currentRightLimit and the Spectra
	 * width in pixels to derive the return value.
	 * 
	 * @param inValue
	 *            (vlaue in Units to be converted)
	 * @return an integer representing the X value of the inValue in the spectra
	 *         pixel-space
	 * @author Damiano Banfi
	 */
	public double unitsToPixelsH(double inValue) {
		if (this.isVertical)
			return (this.getSpectraDisplay().getCurrentBottomLimit() - inValue) * this.getWidth()
					/ (this.getSpectraDisplay().getCurrentBottomLimit() - ((SpectraDisplay) this
							.getParentEntity()).getCurrentTopLimit());
		else
			return (this.getSpectraDisplay().getCurrentLeftLimit() - inValue)* this.getWidth()
					/ (this.getSpectraDisplay().getCurrentLeftLimit() - ((SpectraDisplay) this
							.getParentEntity()).getCurrentRightLimit());

	}

	public double unitsToPixelsV(double inValue) {
		// return
		// (int)((this.currentTopLimit-inValue)*this.getHeight()/(this.currentTopLimit-this.currentBottomLimit));
		if (this.isVertical)
			return (inValue - this.getSpectraDisplay()
					.getCurrentRightLimit())
					* this.getHeight()
					/ (this.getSpectraDisplay()
							.getCurrentLeftLimit() - ((SpectraDisplay) this
							.getParentEntity()).getCurrentRightLimit());
		else
			return (inValue - this.getSpectraDisplay()
					.getCurrentTopLimit())
					* this.getHeight()
					/ (this.getSpectraDisplay()
							.getCurrentBottomLimit() - ((SpectraDisplay) this
							.getParentEntity()).getCurrentTopLimit());

	}

	/**
	 * Returns the value in units corresponding to a X-value in pixels. This
	 * method makes use of currentLeftLimit, currentRightLimit and the Spectra
	 * width in pixels to derive the return value. A pixel value always
	 * represents a X-value range. This method returns one of the borders.
	 * 
	 * @param inValue
	 *            value in pixels to be converted
	 * @return a double representing the value of the inValue in units
	 * @author Damiano Banfi
	 */
	public double pixelsToUnits(double inValue) {
		
		
		double unitsPerPoint = (this.getCurrentLeftLimit() - this.getCurrentRightLimit()) / this.getWidth();
		return (this.getCurrentLeftLimit() - (inValue * unitsPerPoint));
	}

	double pixelsToHz(int inValue) {
		double HzPerPoint = (currentLeftLimit - currentRightLimit)
				* this.getSpectraData().getParamDouble("observeFrequency", 0) / this.getWidth();
		return (inValue * HzPerPoint);
	}

	public double pixelsToUnitsV(double inValue) {
		double unitsPerPoint = (currentBottomLimit - currentTopLimit) / this.getHeight();
		return (currentTopLimit + (inValue * unitsPerPoint));
	}

	/**
	 * Returns the index-value for the data array corresponding to a X-value in
	 * units for the element of spectraData to which it is linked (spectraNb).
	 * This method makes use of spectraData.getFirstX(), spectraData.getLastX()
	 * and spectraData.getNbPoints() to derive the return value.
	 * 
	 * @param inValue
	 *            (value in Units to be converted)
	 * @return an integer representing the index value of the inValue
	 * @author Damiano Banfi
	 */
	public int unitsToArrayPoint(double inValue) {
		this.spectraData.setActiveElement( this.spectraNb);
		return this.spectraData.unitsToArrayPoint(inValue);
	}

	/**
	 * Returns a value in units corresponding to an index-value from the data
	 * array. This method makes use of spectraData.firstX, spectraData.lastX and
	 * spectraData.nbPoints to derive the return value.
	 * 
	 * @param inValue
	 *            (value in Units to be converted)
	 * @return an integer representing the index value of the inValue
	 * @author Damiano Banfi
	 */
	public double arrayPointToUnits(int point) {
		this.spectraData.setActiveElement(this.spectraNb);
		return this.spectraData.arrayPointToUnits(point);
		/*if (this.spectraData.isDataClassXY())
			return (this.spectraData.getFirstX() - ((this.spectraData
					.getFirstX() - this.spectraData.getLastX()) / this.spectraData
					.getNbPoints())
					* point);
		else
			// PEAK
			return this.spectraData.getX(point);*/
	}

	public double arrayPointToUnits(double doublePoint) {
		this.spectraData.setActiveElement(this.spectraNb);
		return this.spectraData.arrayPointToUnits(doublePoint);
		/*return (this.spectraData.getFirstX() - (doublePoint
				* (this.spectraData.getFirstX() - this.spectraData.getLastX()) / (this.spectraData
				.getNbPoints())));*/
	}

	public double arrayPointToUnitsV(double doublePoint) {
		this.spectraData.setActiveElement(this.spectraNb);

		return (this.spectraData.getParamDouble("firstY", 0) - (doublePoint
				* (this.spectraData.getParamDouble("firstY", 0) - this.spectraData
						.getParamDouble("lastX", 0)) / (this.spectraData
				.getParamInt("nbPointsY", 0))));
	}

	public double arrayPointToPixelH(double doublePoint) {
		return this.unitsToPixelsH(this.arrayPointToUnits(doublePoint));
	}

	/*
	 * int arrayPointToPixelV(double doublePoint) { return
	 * this.unitsToPixelsV(this.arrayPointToUnits(doublePoint)); }
	 */
	protected int pixelsToArrayPoint(int inValue) {
		return unitsToArrayPoint(pixelsToUnits(inValue));
	}

	/**
	 * Returns the absolute value of the REAL array for an index-value
	 * corresopnding to a X-value in units. Makes use of
	 * unitsToArrayPoint(double inValue).
	 * 
	 * @param inValue -
	 *            a doublecontaining the X coordinate in units.
	 * @return the height of the Spectra at the point closest to inValue.
	 */
	double spectraValueAt(double pointUnits) {
		this.spectraData.setActiveElement(this.spectraNb);
		return (this.spectraData.getY(unitsToArrayPoint(pointUnits)));
	}

	/**
	 * Returns the pixel corresponding to a given subspectra (Y coordinate).
	 * Used in 2D NMR drawing.
	 * 
	 * @param subSpectra
	 *            an double representing the subspectra in the spectraData
	 *            subSpectra Vector.
	 * @return the Y coordinate in pixels for this subspectra.
	 */
	int subSpectraToPixel(double subSpectra) {
		return (int) (this.getHeight() * ((subSpectra - currentTopSpectra) / (this.currentBottomSpectra - this.currentTopSpectra)));
	}

	protected int pixelsToSubSpectra(int inValue) {
		return (int) ((this.currentBottomSpectra - this.currentTopSpectra)
				* ((double) inValue / this.getHeight()) + currentTopSpectra);
	}

	public int unitsToSubSpectra(double inValue) {
		double firstYHz = this.spectraData.getParamDouble("firstY", 0);
		double lastYHz = this.spectraData.getParamDouble("lastY", 0);
		return (int) (this.spectraData.getNbSubSpectra() - ((this.spectraData
				.getNbSubSpectra()
				* (inValue - lastYHz) / (firstYHz - lastYHz))));
	}

	protected double subSpectraToUnits(double inValue) {
		return ((inValue)
				* (this.spectraData.getParamDouble("lastY", 0) - this.spectraData
						.getParamDouble("firstY", 0)) / this.spectraData
				.getNbSubSpectra())
				+ this.spectraData.getParamDouble("firstY", 0);
	}

	// ------------------------------------------------------------------------
	// Peak-Picking methods
	// ------------------------------------------------------------------------

	void addTempPeakLabel(BasicEntity tempPeakLabel) {
		this.tempLabels.addElement(tempPeakLabel);
	}

	void clearTempPeakLabels() {
		this.tempLabels.clear();
	}

	Vector getTempPeakLabels() {
		return this.tempLabels;
	}

	/**
	 * Calculates noise amplitude for the current spectrum.
	 * 
	 * @return the estimated noise amplitude.
	 */
	protected double getNoiseLevel() {
		double averageAbsDifference = 0;

		SpectraData tempSpectraData = this.getSpectraData();

		for (int point = 1; point < tempSpectraData.getNbPoints(); point++) {
			averageAbsDifference += Math.abs(tempSpectraData.getY(point)
					- tempSpectraData.getY(point - 1));
		}

		averageAbsDifference /= tempSpectraData.getNbPoints() - 1;

		return averageAbsDifference;
	}

	public double getRobustNoiseLevel() {
		spectraData.setActiveElement(0);
		int nbPoints = spectraData.getNbPoints();
		double[] data = new double[nbPoints];
		for (int iPoint = 0; iPoint < nbPoints; iPoint++)
			data[iPoint] = spectraData.getY(iPoint);
		double[] stats = MathUtils.getRobustMeanAndStddev(data, 0, nbPoints);
		return stats[1];
	}

	void setPeakLabelsVisible(int visible) {
		this.hasVisiblePeakLabels = visible;
	}

	protected int hasVisiblePeakLabels() {
		return hasVisiblePeakLabels;
	}

	protected void switchVisiblePeakLabels() {
		if (this.hasVisiblePeakLabels == 1)
			hasVisiblePeakLabels = 0;
		else
			hasVisiblePeakLabels = 1;
	}

	void setPredictionLabelsVisible(int visible) {
		this.hasVisiblePredictionLabels = visible;
	}

	protected int hasVisiblePredictionLabels() {
		return hasVisiblePredictionLabels;
	}

	protected void switchVisiblePredictionLabels() {
		if (this.hasVisiblePredictionLabels == 1)
			hasVisiblePredictionLabels = 0;
		else
			hasVisiblePredictionLabels = 1;
	}

	/**
	 * Returns a string containing the NMR characterisation of this Spectra
	 * object
	 * 
	 * @return a string containing the characterisation
	 */
	/*
	 * public String getNmrHtml() { DecimalFormat newFormat = new
	 * DecimalFormat(); newFormat.applyPattern("#0.00");
	 * 
	 * Vector sPL = new Vector(); // SmartPeakLabel vector // String
	 * tempString="<sup>1</sup>H NMR <i>"+(char)948+"</i> "; String
	 * tempString="<sup>1</sup>H NMR <i><font face=\"symbol\">d</font></i> "; //
	 * Sort the SmartPeakLabels
	 * 
	 * for(int entity=0; entity < this.getComponentCount(); entity++) { if
	 * (this.getComponent(entity) instanceof SmartPeakLabel)
	 * sPL.addElement(this.getComponent(entity)); }
	 * 
	 * if (sPL.size()==0) return "";
	 * 
	 * SmartPeakLabel[] sPLArray=new SmartPeakLabel[sPL.size()];
	 * 
	 * for (int i = 0; i<sPL.size(); i++) {
	 * sPLArray[i]=(SmartPeakLabel)sPL.elementAt(i); } // specific to java 1.2 :
	 * sPL.toArray(sPLArray);
	 * 
	 * SmartPeakLabel tempLabel; for (int labelA=0; labelA < sPLArray.length-1;
	 * labelA++) { for (int labelB=labelA+1; labelB < sPLArray.length; labelB++) {
	 * if (sPLArray[labelA].getStopX() > sPLArray[labelB].getStopX()) {
	 * tempLabel=sPLArray[labelA]; sPLArray[labelA]=sPLArray[labelB];
	 * sPLArray[labelB]=tempLabel; } } }
	 * 
	 * for (int label=0; label < sPLArray.length; label++) { if (label != 0)
	 * tempString+=", "; tempString+=sPLArray[label].getNmrHtml(); } return
	 * tempString; }
	 * 
	 * public String getNmrTable() { String outString="";
	 * 
	 * Vector sPL = new Vector(); // SmartPeakLabel vector
	 * 
	 * for(int entity=0; entity < this.getComponentCount(); entity++) { if
	 * (this.getComponent(entity) instanceof SmartPeakLabel)
	 * sPL.addElement(this.getComponent(entity)); }
	 * 
	 * if (sPL.size()==0) return "";
	 * 
	 * SmartPeakLabel[] sPLArray=new SmartPeakLabel[sPL.size()];
	 * 
	 * for (int i = 0; i<sPL.size(); i++) {
	 * sPLArray[i]=(SmartPeakLabel)sPL.elementAt(i); } // Make sure that the
	 * unique codes for the atoms exist
	 * 
	 * Vector lnkEntities=interactions.getLinkedEntities(sPLArray[0]);
	 * 
	 * for (int ent=0; ent < lnkEntities.size(); ent++) { if
	 * (lnkEntities.elementAt(ent) instanceof AtomLabelEntity) // WARNING!! nemo
	 * and moldraw should be independent! find another way to do this. { if
	 * (interactions.findParentDisplay((BasicEntity)lnkEntities.elementAt(ent))
	 * instanceof MoleculeDisplay) { MoleculeDisplay
	 * tempDisplay=(MoleculeDisplay)(interactions.findParentDisplay((BasicEntity)lnkEntities.elementAt(ent)));
	 * 
	 * Canonizer tempCanonizer=new Canonizer();
	 * tempCanonizer.canonizeMolecule(tempDisplay.getMolecule()); } break; } }
	 * 
	 * 
	 * for (int labelNb=0; labelNb < sPLArray.length; labelNb++) {
	 * outString+=sPLArray[labelNb].getNmrTable(); }
	 * 
	 * return outString; }
	 */
	// ------------------------------------------------------------------------
	// Integrals management functions
	// ------------------------------------------------------------------------
	protected void setIntegralsMultFactor(double newMultFactor) {
		this.integralsMultFactor = newMultFactor;
	}

	protected double getIntegralsMultFactor() {
		return integralsMultFactor;
	}

	protected void setIntegralsBottom(double newIntegralsBottom) {
		this.integralsBottom = newIntegralsBottom;
	}

	protected double getIntegralsBottom() {
		return this.integralsBottom;
	}

	void setIntegralsBaseArea(double newBaseArea) {
		this.integralsBaseArea = newBaseArea;
	}

	void setIntegralsVisible(int visible) {
		this.hasVisibleIntegrals = visible;
	}

	double getIntegralsBaseArea() {
		return this.integralsBaseArea;
	}

	protected int hasVisibleIntegrals() {
		return hasVisibleIntegrals;
	}

	public void switchVisibleIntegrals() {
		if (this.hasVisibleIntegrals == 1)
			hasVisibleIntegrals = 0;
		else
			hasVisibleIntegrals = 1;
	}

	protected void inverseIntegrals() {
		for (int ent = 0; ent < this.getEntitiesCount(); ent++) {
			if (this.getEntity(ent) instanceof Integral) {
				Integral tempIntegral = (Integral) this.getEntity(ent);
				double tempX = tempIntegral.getStartX();
				int tempPoint = tempIntegral.getStartPoint();
				tempIntegral.setStartX(tempIntegral.getStopX());
				tempIntegral.setStartPoint(tempIntegral.getStopPoint());
				tempIntegral.setStopX(tempX);
				tempIntegral.setStopPoint(tempPoint);
				tempIntegral.switchInversed();
			}
		}
	}

	// ------------------------------------------------------------------------------
	// 2D methods
	// ------------------------------------------------------------------------------

	protected void setLowerContourline(double level) {
		this.needsRepaint=true;
		boolean changeLevel = false;
		if ((int) level != (int) this.lowerContourline)
			changeLevel = true;

		this.lowerContourline = level;
		if (this.lowerContourline < 0.1)
			this.lowerContourline = 0.1;
		if (this.lowerContourline > this.posContourLines.size() - 1)
			this.lowerContourline = this.posContourLines.size() - 1;
		if (changeLevel && this.tileManager != null && this.getInteractiveSurface() != null) {
			this.tileManager.forceRedraw();
			this.getInteractiveSurface().repaint();
		}
	}

	protected void shiftContourLines(double dX, double dY) {
		for (int iContour = 0; iContour < this.posContourLines.size(); iContour++) {
			((ContourLine)this.posContourLines.get(iContour)).shift(dX,dY);
		}
		for (int iContour = 0; iContour < this.negContourLines.size(); iContour++) {
			((ContourLine)this.negContourLines.get(iContour)).shift(dX,dY);
		}
		this.tileManager = null;
	}
	
	protected double getLowerContourline() {
		return this.lowerContourline;
	}

	protected double getNbcontourLines() {
		return 10;
	}

	protected void setTopSpectra(int newTopSpectra) {
		this.currentTopSpectra = newTopSpectra;
	}

	protected void setBottomSpectra(int newBottomSpectra) {
		this.currentBottomSpectra = newBottomSpectra;
	}

	public void drawAs2D() {
		this.drawAs2D = true;
		if (this.lowerContourline == Double.NEGATIVE_INFINITY)
			this.lowerContourline = 4;
		setMovementType(BasicEntity.FIXED);
	}

	public boolean isDrawnAs2D() {
		return this.drawAs2D;
	}

	private void generateContourLines(int nbLevels) {
		this.clearContourLines();
		this.generateContourLinesNew(nbLevels);
	}

	/*
	 * the new version of the contour plot
	 */
	void generateContourLinesNew(int nbLevels) {
		this.drawAs2D();

		SpectraData tempSpectraData = this.spectraData;
		tempSpectraData.setActiveElement(0);

		// noise level
		double noise = this.getNoiseLevel();

		double maxZ = tempSpectraData.getMaxY();
		double minZ = tempSpectraData.getMinY();
		
		//System.out.println("Max Z "+maxZ+" min Z "+minZ);

		int nbPoints = tempSpectraData.getNbPoints();
		int nbSubSpectra = tempSpectraData.getNbSubSpectra();
		double[][] zValues = new double[nbSubSpectra][];
		for (int iSubSpectra = 0; iSubSpectra < nbSubSpectra; iSubSpectra++) {
			zValues[iSubSpectra] = tempSpectraData.getSubSpectraDataY(iSubSpectra);
			// tempSpectraData.setActiveElement(iSubSpectra);
			// if (tempSpectraData.getMaxY() > maxZ)
			// maxZ = tempSpectraData.getMaxY();
			// if (tempSpectraData.getMinY() < minZ)
			// minZ = tempSpectraData.getMinY();
			// for (int iPoint = 0; iPoint < nbPoints; iPoint++)
			// zValues[iSubSpectra * nbPoints + iPoint] = tempSpectraData
			// .getY(iPoint);
		}
		for (int i = 0; i < nbSubSpectra; i++) {
			for (int j = 0; j < nbPoints; j++) {
				if (zValues[i][j] > maxZ)
					maxZ = zValues[i][j];
				if (zValues[i][j] < minZ)
					minZ = zValues[i][j];
			}
		}
		//System.out.println("Max2 Z "+maxZ+" min2 Z "+minZ);
		ContourLine[] tempLines = new ContourLine[nbLevels];

		double[] pointHeight = new double[9];
		boolean[] isOver = new boolean[9];

		double pAx, pAy, pBx, pBy;

		double x0 = tempSpectraData.getFirstX();
		double xN = tempSpectraData.getLastX();
		double dx = (xN - x0) / (nbPoints-1);
		double y0 = tempSpectraData.getParamDouble("firstY", 0);
		double yN = tempSpectraData.getParamDouble("lastY", 0);
		double dy = (yN - y0) / (nbSubSpectra-1);
		//System.out.println("y0 "+y0+" yN "+yN);
		// -------------------------
		// Points attribution
		//
		// 0----1
		// |  / |
		// | /  |
		// 2----3
		//
		// ---------------------------
		double lineZValue;
		for (int side = 0; side <= 1; side++) {
			if (side == 0) // positive countours
			{
				for (int line = 0; line < nbLevels; line++)
					tempLines[line] = new ContourLine((maxZ - 5 * noise) * Math.exp(line - nbLevels) + 5 * noise);
			} else {
				for (int line = 0; line < nbLevels; line++)
					tempLines[line] = new ContourLine(-(maxZ - 5 * noise) * Math.exp(line - nbLevels) - 5 * noise);
			}

			for (int line = 0; line < nbLevels; line++) {
				lineZValue = tempLines[line].getZValue();
				if (lineZValue <= minZ || lineZValue >= maxZ)
					continue;

				for (int iSubSpectra = 0; iSubSpectra < nbSubSpectra - 1; iSubSpectra++) {
					for (int point = 0; point < nbPoints - 1; point++) {
						pointHeight[0] = zValues[iSubSpectra][point];
						pointHeight[1] = zValues[iSubSpectra][point + 1];
						pointHeight[2] = zValues[(iSubSpectra + 1)][point];
						pointHeight[3] = zValues[(iSubSpectra + 1)][(point + 1)];

						for (int i = 0; i < 4; i++)
							isOver[i] = (pointHeight[i] > lineZValue);

						// Example point0 is over the plane and point1 and
						// point2 are below, we find the intersections and add
						// the segment
						if (isOver[0] != isOver[1] && isOver[0] != isOver[2]) {
							pAx = point + (lineZValue - pointHeight[0])
									/ (pointHeight[1] - pointHeight[0]);
							pAy = iSubSpectra;
							pBx = point;
							pBy = iSubSpectra + (lineZValue - pointHeight[0])
									/ (pointHeight[2] - pointHeight[0]);

							tempLines[line].addSegment(pAx * dx + x0, pAy * dy
									+ y0, pBx * dx + x0, pBy * dy + y0);
						}
						if (isOver[3] != isOver[1] && isOver[3] != isOver[2]) {
							pAx = point + 1;
							pAy = iSubSpectra + 1
									- (lineZValue - pointHeight[3])
									/ (pointHeight[1] - pointHeight[3]);
							pBx = point + 1 - (lineZValue - pointHeight[3])
									/ (pointHeight[2] - pointHeight[3]);
							pBy = iSubSpectra + 1;

							tempLines[line].addSegment(pAx * dx + x0, pAy * dy
									+ y0, pBx * dx + x0, pBy * dy + y0);
						}
						// test around the diagonal
						if (isOver[1] != isOver[2]) {
							pAx = point + 1 - (lineZValue - pointHeight[1])
									/ (pointHeight[2] - pointHeight[1]);
							pAy = iSubSpectra + (lineZValue - pointHeight[1])
									/ (pointHeight[2] - pointHeight[1]);
							if (isOver[1] != isOver[0]) {
								pBx = point + 1 - (lineZValue - pointHeight[1])
										/ (pointHeight[0] - pointHeight[1]);
								pBy = iSubSpectra;
								tempLines[line].addSegment(pAx * dx + x0, pAy
										* dy + y0, pBx * dx + x0,
										pBy * dy + y0);
							}
							if (isOver[2] != isOver[0]) {
								pBx = point;
								pBy = iSubSpectra + 1
										- (lineZValue - pointHeight[2])
										/ (pointHeight[0] - pointHeight[2]);
								tempLines[line].addSegment(pAx * dx + x0, pAy
										* dy + y0, pBx * dx + x0,
										pBy * dy + y0);
							}
							if (isOver[1] != isOver[3]) {
								pBx = point + 1;
								pBy = iSubSpectra
										+ (lineZValue - pointHeight[1])
										/ (pointHeight[3] - pointHeight[1]);
								tempLines[line].addSegment(pAx * dx + x0, pAy
										* dy + y0, pBx * dx + x0,
										pBy * dy + y0);
							}
							if (isOver[2] != isOver[3]) {
								pBx = point + (lineZValue - pointHeight[2])
										/ (pointHeight[3] - pointHeight[2]);
								pBy = iSubSpectra + 1;
								tempLines[line].addSegment(pAx * dx + x0, pAy
										* dy + y0, pBx * dx + x0,
										pBy * dy + y0);
							}
						}
					}
				}
			}

			for (int line = 0; line < nbLevels; line++) {
				if (tempLines[line].segments.size() == 0)
					continue;
				tempLines[line].generateQuadTree(
						(float)this.spectraData.getFirstX(),
						(float)this.spectraData.getLastX(), 
						(float)this.spectraData.getParamDouble("lastY", 0), 
						(float)this.spectraData.getParamDouble("firstY", 0), 50, 100);
				if (side == 0)
					this.addContourLine(tempLines[line], true);
				else
					this.addContourLine(tempLines[line], false);
			}
		}
		if (Spectra.DEBUG) {
			System.out.println("positive contourLines");
			for (int iLine = 0; iLine < this.posContourLines.size(); iLine++)
				System.out
						.println("\t"
								+ iLine
								+ ": "
								+ ((ContourLine) this.posContourLines
										.get(iLine)).zValue);
			System.out.println("negative contourLines");
			for (int iLine = 0; iLine < this.negContourLines.size(); iLine++)
				System.out
						.println("\t"
								+ iLine
								+ ": "
								+ ((ContourLine) this.negContourLines
										.get(iLine)).zValue);
		}
		zValues = null;

		this.setTopSpectra(tempSpectraData.getNbSubSpectra() - 1);
		this.setBottomSpectra(0);

		if (this.getInteractiveSurface() != null)
			this.getInteractiveSurface().repaint();
	}

	protected void generateContourLinesOld(int nbLevels) {
		if (true) {
			throw new RuntimeException("Still using old generateContourLinesOld");
		}
		// if (this.getInteractiveSurface() != null)
		// this.getInteractiveSurface().getUserDialog().setText("Generating
		// Contour-lines");
		// if (this.getInteractiveSurface().getActiveSpectra() != null)
		// this.getInteractiveSurface().getActiveSpectra().drawAs2D();

		this.drawAs2D();

		SpectraData tempSpectraData = this.spectraData;
		tempSpectraData.setActiveElement(0);

		// noise level
		double noise = this.getNoiseLevel();

		double maxZ = tempSpectraData.getMaxY();
		double minZ = tempSpectraData.getMinY();

		for (int subSpectra = 1; subSpectra < tempSpectraData.getNbSubSpectra(); subSpectra++) {
			tempSpectraData.setActiveElement(subSpectra);

			if (tempSpectraData.getMaxY() > maxZ)
				maxZ = tempSpectraData.getMaxY();
			if (tempSpectraData.getMinY() < minZ)
				minZ = tempSpectraData.getMinY();
		}

		ContourLine[] tempLines = new ContourLine[nbLevels];

		double[] pointHeight = new double[9];
		boolean[] isOver = new boolean[9];

		double pAx, pAy, pBx, pBy;

		// -------------------------
		// Points attribution
		//
		// 0----1
		// | /|
		// | / |
		// 2----3
		//
		// ---------------------------
		double lineZValue;

		for (int side = 0; side <= 1; side++) {
			if (side == 0) // positive countours
			{
				for (int line = 0; line < nbLevels; line++)
					// tempLines[line] = new
					// ContourLine((maxZ-minZ)*Math.exp(line-nbLevels));
					tempLines[line] = new ContourLine((maxZ - 10 * noise)
							* Math.exp(line - nbLevels) + 10 * noise);
			} else {
				for (int line = 0; line < nbLevels; line++)
					tempLines[line] = new ContourLine(-(maxZ - 10 * noise)
							* Math.exp(line - nbLevels) - 10 * noise);
			}

			for (int line = 0; line < nbLevels; line++) {
				lineZValue = tempLines[line].getZValue();
				for (int subSpectra = 0; subSpectra < tempSpectraData
						.getNbSubSpectra() - 1; subSpectra++) {
					for (int point = 0; point < tempSpectraData.getNbPoints() - 1; point++) {
						tempSpectraData.setActiveElement(subSpectra);
						pointHeight[0] = tempSpectraData.getY(point);
						pointHeight[1] = tempSpectraData.getY(point + 1);

						tempSpectraData.setActiveElement(subSpectra + 1);
						pointHeight[2] = tempSpectraData.getY(point);
						pointHeight[3] = tempSpectraData.getY(point + 1);

						for (int i = 0; i < 4; i++)
							isOver[i] = (pointHeight[i] > lineZValue);

						// Example point0 is over the plane and point1 and
						// point2 are below, we find the intersections and add
						// the segment
						if (isOver[0] != isOver[1] && isOver[0] != isOver[2]) {
							pAx = point + (lineZValue - pointHeight[0])
									/ (pointHeight[1] - pointHeight[0]);
							pAy = subSpectra;
							pBx = point;
							pBy = subSpectra + (lineZValue - pointHeight[0])
									/ (pointHeight[2] - pointHeight[0]);

							tempLines[line].addSegment(this
									.arrayPointToUnits(pAx), this
									.subSpectraToUnits(pAy), this
									.arrayPointToUnits(pBx), this
									.subSpectraToUnits(pBy));
						}
						if (isOver[1] != isOver[0] && isOver[1] != isOver[2]) {
							pAx = point + 1 - (lineZValue - pointHeight[1])
									/ (pointHeight[0] - pointHeight[1]);
							pAy = subSpectra;
							pBx = point + 1 - (lineZValue - pointHeight[1])
									/ (pointHeight[2] - pointHeight[1]);
							pBy = subSpectra + (lineZValue - pointHeight[1])
									/ (pointHeight[2] - pointHeight[1]);

							tempLines[line].addSegment(this
									.arrayPointToUnits(pAx), this
									.subSpectraToUnits(pAy), this
									.arrayPointToUnits(pBx), this
									.subSpectraToUnits(pBy));
						}
						if (isOver[2] != isOver[0] && isOver[2] != isOver[1]) {
							pAx = point;
							pAy = subSpectra + 1
									- (lineZValue - pointHeight[2])
									/ (pointHeight[0] - pointHeight[2]);
							pBx = point + (lineZValue - pointHeight[2])
									/ (pointHeight[1] - pointHeight[2]);
							pBy = subSpectra + 1
									- (lineZValue - pointHeight[2])
									/ (pointHeight[1] - pointHeight[2]);

							tempLines[line].addSegment(this
									.arrayPointToUnits(pAx), this
									.subSpectraToUnits(pAy), this
									.arrayPointToUnits(pBx), this
									.subSpectraToUnits(pBy));
							// tempLines[line].addSegment(pAx, pAy, pBx, pBy,
							// Color.yellow);
						}
						if (isOver[1] != isOver[2] && isOver[1] != isOver[3]) {
							pAx = point + 1 - (lineZValue - pointHeight[1])
									/ (pointHeight[2] - pointHeight[1]);
							pAy = subSpectra + (lineZValue - pointHeight[1])
									/ (pointHeight[2] - pointHeight[1]);
							;
							pBx = point + 1;
							pBy = subSpectra + (lineZValue - pointHeight[1])
									/ (pointHeight[3] - pointHeight[1]);

							tempLines[line].addSegment(this
									.arrayPointToUnits(pAx), this
									.subSpectraToUnits(pAy), this
									.arrayPointToUnits(pBx), this
									.subSpectraToUnits(pBy));
							// tempLines[line].addSegment(pAx, pAy, pBx, pBy,
							// Color.cyan);
						}
						if (isOver[2] != isOver[1] && isOver[2] != isOver[3]) {
							pAx = point + (lineZValue - pointHeight[2])
									/ (pointHeight[1] - pointHeight[2]);
							pAy = subSpectra + 1
									- (lineZValue - pointHeight[2])
									/ (pointHeight[1] - pointHeight[2]);
							pBx = point + (lineZValue - pointHeight[2])
									/ (pointHeight[3] - pointHeight[2]);
							pBy = subSpectra + 1;

							tempLines[line].addSegment(this
									.arrayPointToUnits(pAx), this
									.subSpectraToUnits(pAy), this
									.arrayPointToUnits(pBx), this
									.subSpectraToUnits(pBy));
							// tempLines[line].addSegment(pAx, pAy, pBx, pBy,
							// Color.magenta);
						}
						if (isOver[3] != isOver[1] && isOver[3] != isOver[2]) {
							pAx = point + 1;
							pAy = subSpectra + 1
									- (lineZValue - pointHeight[3])
									/ (pointHeight[1] - pointHeight[3]);
							pBx = point + 1 - (lineZValue - pointHeight[3])
									/ (pointHeight[2] - pointHeight[3]);
							pBy = subSpectra + 1;

							tempLines[line].addSegment(this
									.arrayPointToUnits(pAx), this
									.subSpectraToUnits(pAy), this
									.arrayPointToUnits(pBx), this
									.subSpectraToUnits(pBy));
							// tempLines[line].addSegment(pAx, pAy, pBx, pBy,
							// Color.gray);
						}
					}
				}

				// System.out.println("Number of segments:
				// "+tempLines[line].getNbSegments());
			}

			for (int line = 0; line < nbLevels; line++) {
				tempLines[line].generateQuadTree(
						(float)this.spectraData.getFirstX(),
						(float)this.spectraData.getLastX(), 
						(float)this.spectraData.getParamDouble("lastY", 0), 
						(float)this.spectraData.getParamDouble("firstY", 0), 50, 100);
				if (side == 0)
					this.addContourLine(tempLines[line], true);
				else
					this.addContourLine(tempLines[line], false);
			}
		}

		this.setTopSpectra(tempSpectraData.getNbSubSpectra() - 1);
		this.setBottomSpectra(0);

		if (this.getInteractiveSurface() != null)
			this.getInteractiveSurface().repaint();
	}

	protected void addContourLine(ContourLine newLine, boolean isPositive) {
		if (isPositive)
			this.posContourLines.addElement(newLine);
		else
			this.negContourLines.addElement(newLine);
	}

	private SpectraDisplay getSpectraDisplay() {
		return ((SpectraDisplay) this.getParentEntity());
	}
	
	// ----------------------------------------------------------------
	// paint section
	// ----------------------------------------------------------------

	private void refreshDrawArrays() {

		if (this.getSpectraData() != null) {
			double thisHeight = this.getHeight();
			double thisWidth = this.getWidth();
			double dMinY = spectraData.getMinY();
			double dMaxY = spectraData.getMaxY();

			double cLL, cRL;

			if (!this.isVertical) {
				cLL = this.getSpectraDisplay().getCurrentLeftLimit();
				cRL = this.getSpectraDisplay().getCurrentRightLimit();
			} else {
				cLL = this.getSpectraDisplay().getCurrentBottomLimit();
				cRL = this.getSpectraDisplay().getCurrentTopLimit();
			}

			double firstX = this.spectraData.getFirstX();
			double lastX = this.spectraData.getLastX();
			int nbPoints = this.spectraData.getNbPoints();

			int arraySize = this.getSpectraData().getNbPoints();
			if (bufferedX == null || bufferedX.length != nbPoints) {
				this.bufferedX = new float[arraySize];
				this.bufferedY = new float[arraySize];
			}

			double alpha;
			if (this.getSpectraDisplay().isAbsoluteYScale()) {
				// System.out.println("Current top limit: "+this.getSpectraDisplay().getCurrentTopLimit()+" - "+this.getSpectraDisplay().getCurrentBottomLimit());
				
				alpha = multFactor * thisHeight / (this.getSpectraDisplay().getCurrentTopLimit() - this.getSpectraDisplay().getCurrentBottomLimit());
			} else {
				alpha = multFactor * thisHeight / (dMaxY - dMinY);
			}

			// System.out.println(multFactor+" - "+thisHeight+" - "+dMaxY+" - "+dMinY);
			
			for (int point = 0; point < nbPoints; point++) {
				if (this.getSpectraData().isDataClassXY()) {
					// this.bufferedX[point]=(float)this.arrayPointToPixelH(point);
					this.bufferedX[point] = (float) ((cLL - (firstX - (point
							* (firstX - lastX) / nbPoints))) * thisWidth / (cLL - cRL));
				} else {
					this.bufferedX[point] = (float) this.unitsToPixelsH(this.spectraData.getX(point));
				}
	
				this.bufferedY[point] = (float) (thisHeight - alpha
						* (this.spectraData.getY(point) - dMinY));

				// System.out.println(thisHeight+" - "+alpha+" - "+this.spectraData.getY(point)+" - "+dMinY+this.bufferedY[point]);
				
				if (!isLegal()) { // we add some noise
					if (point > 5) {
						this.bufferedY[point] = (this.bufferedY[point - 1]
								+ this.bufferedY[point - 2] + this.bufferedY[point]) / 3;
						if (point % 127 == 1) {
							this.bufferedY[point] = (float) (this.bufferedY[point] * 1.3);
						}
					}
				}

			}
		}
	}

	public void checkSizeAndPosition() {
		if (this.getParentEntity() != null) {
			SpectraDisplay parentDisplay = this.getSpectraDisplay();

			if (parentDisplay.isAbsoluteYScale()) {
				this.setMovementType(BasicEntity.FIXED);
			}
			
			double tempWidth = parentDisplay.getWidth();
			double tempHeight = parentDisplay.getHeight();
			double tempXPos = 0;
			double tempYPos = this.getLocation().y;

			if (this.spectraData.getDataType() == SpectraData.TYPE_2DNMR_SPECTRUM)
				tempYPos = 0;
			//
			if (parentDisplay.is2D()) {
				if (parentDisplay.hasVScale()) {
					if (this.isVertical) {
						tempHeight = parentDisplay.getHeight() * 2 * SpectraDisplay.SCALE_RELATIVE_SIZE;
					} else {
						tempWidth = parentDisplay.getWidth() * (1 - 2 * SpectraDisplay.SCALE_RELATIVE_SIZE);
						tempXPos = parentDisplay.getWidth() * 2 * SpectraDisplay.SCALE_RELATIVE_SIZE;
					}
				} else {
					if (this.isVertical) {
						tempHeight = 0;
					}
				}

				if (parentDisplay.hasHScale()) {
					if (this.isDrawnAs2D()) {
						tempHeight = parentDisplay.getHeight() * (1 - 3 * SpectraDisplay.SCALE_RELATIVE_SIZE);
						tempYPos = parentDisplay.getHeight() * 2 * SpectraDisplay.SCALE_RELATIVE_SIZE;
					} else {
						if (this.isVertical) {
							tempWidth = parentDisplay.getHeight() * (1 - 3 * SpectraDisplay.SCALE_RELATIVE_SIZE);
							tempXPos = -parentDisplay.getHeight() * 2 * SpectraDisplay.SCALE_RELATIVE_SIZE;
						} else {
							tempHeight = parentDisplay.getHeight() * 2 * SpectraDisplay.SCALE_RELATIVE_SIZE;
							tempYPos = 0;
						}
					}
				}

			} else {
				
				//System.out.println("UPDATING position");
				
				if (parentDisplay.hasVScale()) {
					if (this.isVertical) {

					} else {
						tempWidth = parentDisplay.getWidth() * (1 - SpectraDisplay.SCALE_RELATIVE_SIZE);
						tempXPos = parentDisplay.getWidth() * SpectraDisplay.SCALE_RELATIVE_SIZE;
					}
				}

				if (parentDisplay.hasHScale()) {
					tempHeight = parentDisplay.getHeight() * (1 - SpectraDisplay.SCALE_RELATIVE_SIZE);
					if(tempHeight<300)
						tempHeight-=7;
				}
			}
			this.setSize(tempWidth, tempHeight);

			this.setLocation(tempXPos, tempYPos);
			
			// System.out.println("Spectrum info: "+this.isVertical+" - "+this.getWidth()+" - "+tempXPos+" - "+tempYPos+" - "+tempWidth+" - "+tempHeight+" - "+this);
			
			if (this.isVertical) {
				AffineTransform tempTransf = new AffineTransform();
				tempTransf.setToIdentity();
				tempTransf.rotate(-Math.PI / 2);
				tempTransf.translate(-this.getWidth(), 0);
				tempTransf.translate(tempXPos, tempYPos);
				this.setLocalTransform(tempTransf);
			}
			
			
		}

		if (!this.isVertical) this.checkVerticalLimits();

		this.refreshDrawArrays();
		
		// TODO this seems crazy because the top code is expected to call this code ...
		super.checkSizeAndPosition();
		this.refreshSensitiveArea();
	}

	Area sensitiveArea=null;
	
	public void refreshSensitiveArea() {
		if ((this.spectraData != null) && (this.spectraData.getNbSubSpectra()>0)) {
			if (this.getParentEntity() != null
					&& this.getParentEntity() instanceof SpectraDisplay
					&& this.getSpectraDisplay().is2D()) {
				// We define the sensitive area for a 2D
				this.setSensitiveArea(new Area(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight())));
			} else {
				// We define the sensitive area for a 1D
				this.spectraData.setActiveElement(0);
				double firstY = this.unitsToPixelsV(this.spectraData.getY(0));
				double lastY = this.unitsToPixelsV(this.spectraData.getY(this.spectraData.getNbPoints() - 1));
				double ymin = Math.min(firstY, lastY);
				double ymax = Math.max(firstY, lastY);

//				if (ymin == Double.NaN || ymax == Double.NaN) {
				if (this.getSpectraData().getDefaults().anchorPoint == 0) {
					this.setSensitiveArea(new Area(new Rectangle2D.Double( 0, (1 - 0.05) * this.getHeight(), this.getWidth(), (2 * 0.03) * this.getHeight())));
				} else if (this.getSpectraData().getDefaults().anchorPoint == 2) {
					this.setSensitiveArea(new Area(new Rectangle2D.Double( 0, 0.05 * this.getHeight(), this.getWidth(), (2 * 0.07) * this.getHeight())));
				} else if (this.getSpectraData().getDefaults().anchorPoint == 3) {
					// If it is not smooth we just draw an rectangle at the level of the baseline (0)
					if (! this.isSmooth()) {
						this.sensitiveArea=new Area(new Rectangle(0, (int)this.getHeight(), (int)this.getWidth(), 6));
					} else {
					
					
						// we will create an area that is following what is currently drawn on the screen
						
						
						GeneralPath polyline=new GeneralPath();
						
						if ((bufferedX!=null) && (bufferedY!=null)) {
							int iia = this.binarySearch(bufferedX, 0);
							int iib = this.binarySearch(bufferedX, (float)this.getWidth());
							int ia = Math.min(iia, iib);
							int ib = Math.max(iia, iib);
							ia = ia < 0 ? 0 : ia;
							ia = ia > bufferedX.length ? bufferedX.length : ia;
							ib = ib < 0 ? 0 : ib;
							ib = ib > bufferedX.length ? bufferedX.length : ib;
							if (ia == ib) {
								ia = 0;
								ib = bufferedX.length;
							}
							polyline.moveTo(bufferedX[ia], bufferedY[ia]);
							int step=(ib-ia)/50+1;
							for (int index = ia + 1; index < ib; index+=step) {
								polyline.lineTo(bufferedX[index], bufferedY[index]-5);
							}
							for (int index = ib - 1; index > ia; index-=step) {
								polyline.lineTo(bufferedX[index], bufferedY[index]+5);
							}
						}
	
						this.sensitiveArea=new Area(polyline);
						
					}
					this.setSensitiveArea(sensitiveArea);
				} else {
					this.setSensitiveArea(new Area(new Rectangle2D.Double(0, (1 - 0.05) * this.getHeight(),
							this.getWidth(), (2 * 0.03)	* this.getHeight())));
				}

/*				} else {
					this.setSensitiveArea(new Area(new Rectangle2D.Double(
							0, (1 - 0.05) * this.getHeight(), this
									.getWidth(), (2 * 0.03)
									* this.getHeight())));
				}
*/
			}
		}

	}

	public void addHExtraLine(double y) {
		this.hExtraLines.add(new Double(y));
	}
	public void addVExtraLine(double x) {
		this.vExtraLines.add(new Double(x));
	}
	
	public void clearExtraLines() {
		this.vExtraLines.clear();
		this.hExtraLines.clear();
	}
	
	public void paint(Graphics2D g) {
		
		if(!this.isVisible())
			return;
		if ((DEBUG) && (sensitiveArea!=null)) {
			g.setColor(Color.YELLOW);
			g.draw(sensitiveArea);
		}

		if ((this.getSecondaryColor() != null) && (! this.spectraData.is2D())) {
			g.setColor(this.getSecondaryColor());
			g.fillRect(0, 0, (int)this.getWidth(), (int)this.getHeight());
		}
		
		if (spectraData.getNbSubSpectra()<1) return;
		// if (!this.isVertical)
		// g.setClip(new Rectangle2D.Double(-1,-1,this.getWidth()+2,
		// this.getHeight()+2));
		// else
		// g.setClip(new Rectangle2D.Double(-1,-1,this.getHeight()+2,
		// this.getWidth()+2));
		
		
		// EXTRA LINES ONLY FOR 2D
		if (this.isDrawnAs2D()) {
			g.setColor(extraLinesColor);
			for (Double x : vExtraLines) {
				double xPoint=this.unitsToPixelsH(x);
				g.draw(new Line2D.Double(xPoint,0,xPoint,this.getHeight()));
			}
			for (Double y : hExtraLines) {
				double yPoint=this.unitsToPixelsV(y);
				g.draw(new Line2D.Double(0,yPoint,this.getWidth(),yPoint));
			}
		}

		
		// System.out.println(this.getHeight()+" - "+this.getWidth()+" - "+this.getLocation().x+" - "+this.getLocation().y);
		
		
		SpectraDisplay parentDisplay = (SpectraDisplay) this.getParentEntity();
		if (!drawAs2D) {
			if ((bufferedX == null) || (this.spectraData.resetDataChanged())) {
				// to add otherwise first time calculated twice
				this.spectraData.resetDataChanged();
				this.refreshDrawArrays();
			}

			if (bufferedX.length==0) return;
			
			if (this.getPrimaryColor() != null) {
				g.setColor(this.getPrimaryColor());

				// FIRST CASE we need to draw a continuous polyline
				
				// Not really correct but it seems that not all the spectra generator call the procedure
				// to check if it is smooth. We need therefore to force the class XY to be drawn as smooth
				if (this.getSpectraData()!=null && (this.isSmooth || this.getSpectraData().isDataClassXY())) {

					GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, bufferedX.length);

					// only draw when needed. this is a test!!!!
					int ia, ib;
					
					int iia = this.binarySearch(bufferedX, 0);
					int iib = this.binarySearch(bufferedX, (float)this.getWidth());
					
					ia = Math.min(iia, iib);
					ib = Math.max(iia, iib);
					
					ia = ia < 0 ? 0 : ia;
					ia = ia > bufferedX.length ? bufferedX.length : ia;
					ib = ib < 0 ? 0 : ib;
					ib = ib > bufferedX.length ? bufferedX.length : ib;
					if (ia == ib) {
						ia = 0;
						ib = bufferedX.length;
					}
					
					polyline.moveTo(bufferedX[ia], bufferedY[ia]);
					for (int index = ia + 1; index < ib; index++) {
						polyline.lineTo(bufferedX[index], bufferedY[index]);
					}
					g.setColor(this.getPrimaryColor());
					g.setStroke(this.getInteractiveSurface().getNarrowStroke());

					g.draw(polyline);
				} else { // SECOND CASE we need to draw a line with some peaks
					g.draw(new Line2D.Double(0, this.getHeight(), this.getWidth(), this.getHeight()));
					g.setColor(this.getPrimaryColor());
					g.setStroke(this.getInteractiveSurface().getNarrowStroke());
					for (int point = 0; point < this.getSpectraData().getNbPoints(); point++) {
						g.draw(new Line2D.Double(this.bufferedX[point], this.getHeight(), this.bufferedX[point], this.bufferedY[point]));
					}
				}
				
				if (this.isSelected() || this.isMouseover()) {
					int indexLeft = (int) (bufferedX[0] / (bufferedX[0] - bufferedX[1]));
					int indexRight = (int) ((bufferedX[0] - this.getWidth() + 1) / (bufferedX[0] - bufferedX[1]));

					if (indexLeft < 0) indexLeft = 0;
					if (indexRight < 0) indexRight = 0;
					if (indexLeft > bufferedX.length - 1) indexLeft = bufferedX.length - 1;
					if (indexRight > bufferedX.length - 1) indexRight = bufferedX.length - 1;

					// TODO the highlight does not take into account the rotation of vertical spectra !!!
					int yLeft=(int)this.getHeight();
					int yRight=(int)this.getHeight();
					if (isSmooth()) {
						yLeft=(int)bufferedY[indexLeft];
						yRight=(int)bufferedY[indexRight];
					}

					if (isSelected()) {
						g.fill(new Rectangle2D.Double(0, yLeft - 3, 7, 7));
						g.fill(new Rectangle2D.Double((int) this.getWidth() - 7, yRight - 3, 7, 7));
					} else if (isMouseover()) {
						g.fill(new Rectangle2D.Double(0, yLeft - 2, 5, 5));
						g.fill(new Rectangle2D.Double((int) this.getWidth() - 5, yRight - 2, 5, 5));
					}
				}
				
			}
		} else // 2D NMR
		{
			if ((! this.hasContourLines()) || (this.spectraData.resetDataChanged())) {
				// we need to add the following line otherwise the data will be calculated twice
				this.spectraData.resetDataChanged();
				this.generateContourLines(Spectra.DEFAULT_NB_CONTOURS);
			}
			
			if ((this.tileManager == null) ) {
				tileManager = new NMR2DTileManager(this, this.posContourLines, this.negContourLines, 
						(float)this.getWidth(), (float)this.getHeight(),
						(float)this.getSpectraDisplay().getCurrentLeftLimit(),
						(float)this.getSpectraDisplay().getCurrentRightLimit(), 
						(float)this.getSpectraDisplay().getCurrentTopLimit(),
						(float)this.getSpectraDisplay().getCurrentBottomLimit(), Color.black,
						Color.yellow, 4, 4);
			}

			g.drawImage(tileManager.getImage((float)parentDisplay.getCurrentLeftLimit(), 
					(float)parentDisplay.getCurrentRightLimit(),
					(float) parentDisplay.getCurrentTopLimit(), 
					(float) parentDisplay.getCurrentBottomLimit()), 0, 0, null);
			
			
			if (isSelected()) {
				g.setColor(this.getPrimaryColor());
				g.fill(new Rectangle2D.Double(1,1,4,4));
				g.fill(new Rectangle2D.Double(1,this.getHeight()-4,4,4));
				g.fill(new Rectangle2D.Double(this.getWidth()-4,1,4,4));
				g.fill(new Rectangle2D.Double(this.getWidth()-4,this.getHeight()-4,4,4));
			} else if (isMouseover()) {
				
			}
			
		}

		super.paint(g);
		g.setClip(null);
		
		// System.out.println("Time to paint: "+(System.currentTimeMillis()-start)+"ms - Memory used: "+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()));
		
		
	}

	public void paintSB(Graphics2D g) {
		BasicStroke currentNarrowStroke = this.getInteractiveSurface().getNarrowStroke();
		this.getInteractiveSurface().setNarrowStroke(new BasicStroke(0.3f));
		if (!this.drawAs2D) {
			paint(g);
		} else {
			g.setStroke(this.getInteractiveSurface().getNarrowStroke());

			if ((! this.hasContourLines()) || (this.spectraData.resetDataChanged())) {
				// we need to add the following line otherwise the data will be calculated twice
				this.spectraData.resetDataChanged();
				this.generateContourLines(Spectra.DEFAULT_NB_CONTOURS);
			}
			
			SpectraDisplay parentDisplay = (SpectraDisplay) this.getParentEntity();

			float cLL = (float)parentDisplay.getCurrentLeftLimit();
			float cRL = (float)parentDisplay.getCurrentRightLimit();
			float cTL = (float)parentDisplay.getCurrentTopLimit();
			float cBL = (float)parentDisplay.getCurrentBottomLimit();

			Vector<Segment> tempVector = new Vector<Segment>();

			for (int line = (int) lowerContourline; line < posContourLines.size(); line++) {
				ContourLine tempLine = (ContourLine) posContourLines.elementAt(line);

				tempVector.clear();
				tempLine.getSegments(cLL, cRL, cTL, cBL, tempVector);

				if (this.getPrimaryColor() != null) {
					g.setColor(this.getPrimaryColor());

					for (Segment tempSegment : tempVector) {
						// TODO not convert double to float each time
						tempSegment.paint(g, cLL, cRL, cTL, cBL, (float)this.getWidth(), (float)this.getHeight());
					}
				}

				if (negContourLines.size() > line) {
					tempLine = (ContourLine) negContourLines.elementAt(line);

					tempVector.clear();
					tempLine.getSegments(cLL, cRL, cTL, cBL, tempVector);

					if (this.getSecondaryColor() != null) {
						g.setColor(this.getSecondaryColor());
						for (Segment tempSegment : tempVector) {
							g.draw(new Line2D.Double((cLL - tempSegment.getP1x()) * this.getWidth() / (cLL - cRL),
									(cTL - tempSegment.getP1y()) * this.getHeight() / (cTL - cBL),
									(cLL - tempSegment.getP2x()) * this.getWidth() / (cLL - cRL),
									(cTL - tempSegment.getP2y()) * this.getHeight() / (cTL - cBL)));
						}
					}
				}
			}

		}

		super.paint(g);
		this.getInteractiveSurface().setNarrowStroke(currentNarrowStroke);
	}

	public String getXmlTag(Hashtable xmlProperties) {
		XMLCoDec tempCodec = new XMLCoDec();

		if (xmlProperties.containsKey("embedJCamp")
				&& ((Boolean) xmlProperties.get("embedJCamp")).booleanValue()) {
			String encodedJCamp = this.getSpectraData().getTotalJCamp();
			
			if(encodedJCamp.length()!=0){
				ByteArrayOutputStream baosStringAsZippedBA = new ByteArrayOutputStream();
				ZipOutputStream zOs = new ZipOutputStream(baosStringAsZippedBA);

				try {
					zOs.putNextEntry(new ZipEntry("JCamp"));
					zOs.write(encodedJCamp.getBytes());
					zOs.closeEntry();

					encodedJCamp = Base64.encodeBytes(baosStringAsZippedBA
							.toByteArray());

				} catch (IOException ioe) {
					System.out.println("IOException zipping the JCamp: " + ioe);
				};
				tempCodec.addParameter("encodedJCamp", encodedJCamp);
			}
			
		}

		double relY = this.getLocation().y
				/ ((BasicEntity) this.getParentEntity()).getHeight();
		double relX = this.getLocation().x
				/ ((BasicEntity) this.getParentEntity()).getWidth();

		int flag2D;

		if (this.isDrawnAs2D())
			flag2D = 1;
		else
			flag2D = 0;

		tempCodec.addParameter("relX", new Double(relX));
		tempCodec.addParameter("relY", new Double(relY));
		tempCodec.addParameter("relWidth", new Double(this.getWidth()
				/ ((BasicEntity) this.getParentEntity()).getWidth()));
		tempCodec.addParameter("relHeight", new Double(this.getHeight()
				/ ((BasicEntity) this.getParentEntity()).getHeight()));

		tempCodec.addParameter("magFactor", new Double(this.multFactor));
		tempCodec.addParameter("lowerContour",
				new Double(this.lowerContourline));

		if (this.getPrimaryColor() != null)
			tempCodec.addParameter("primaryColor", this.getPrimaryColor()
					.getRed()
					+ ","
					+ this.getPrimaryColor().getGreen()
					+ ","
					+ this.getPrimaryColor().getBlue());
		else
			tempCodec.addParameter("primaryColor", "NULL");
		if (this.getSecondaryColor() != null)
			tempCodec.addParameter("secondaryColor", this.getSecondaryColor().getRed()
					+ ","
					+ this.getSecondaryColor().getGreen()
					+ ","
					+ this.getSecondaryColor().getBlue());
		else
			tempCodec.addParameter("secondaryColor", "NULL");

		tempCodec.addParameter("integralsBaseArea", new Double(
				this.integralsBaseArea));
		tempCodec.addParameter("integralsMagFactor", new Double(
				this.integralsMultFactor));
		tempCodec.addParameter("integralsRelBottom", new Double(
				(double) this.integralsBottom / this.getHeight()));
		// We need to store firstX and lastX, mainly for the shift of spectra when specifying the reference
		if(this.spectraData.getAppliedFilterByFilterType(FilterType.VENDOR_SPECIFIC)!=null||this.spectraData.getAppliedFilterByFilterType(FilterType.FFT)!=null){
			//System.out.println("Si hay filtros aplicados");
			tempCodec.addParameter("firstX", new Double(this.spectraData.getLastX()));
			tempCodec.addParameter("lastX", new Double(this.spectraData.getFirstX()));
		}else{
			//System.out.println("No hay filtro aplicados");
			tempCodec.addParameter("firstX", new Double(this.spectraData.getFirstX()));
			tempCodec.addParameter("lastX", new Double(this.spectraData.getLastX()));
		}
		
		tempCodec.addParameter("is2D", new Integer(flag2D));
		tempCodec.addParameter("isVertical", new Boolean(this.isVertical));

		if (xmlProperties.containsKey("includeURL")
				&& ((Boolean) xmlProperties.get("includeURL")).booleanValue()
				&& this.getSpectraData().getURL() != null
				&& this.getSpectraData().getURL().toString().compareTo("") != 0) {
			// LP thniks there is a bug because the URL for local file should contain 3 (or 4) /
			// like file:///
			// and creation of the URL and get of the URL removes one (or two) /
			// Therefore in the get we will take this into account
			String url=this.spectraData.getURL().toString();
			//System.out.println(url);
			/*if (url.startsWith("file://")) {
				url=url.replaceAll("file://([^/])", "file:///$1");
			}*/
			if (url.startsWith("file:/")) {
				url=url.replaceAll("file:/([^/])", "file:///$1");
			}
			//System.out.println(url);
			tempCodec.addParameter("url", url);
		}

		if (this.getSpectraData().getSimulationDescriptor().compareTo("") != 0) {
			if (this.predictionData != null) {
				tempCodec.addParameter("simulateWithPredictionData", 1);
				this.predictionData.getInputData();
				tempCodec.addParameter("simulationDescriptor",
						this.predictionData.getXMLData());
			} else {
				tempCodec.addParameter("simulationDescriptor", this.spectraData
						.getSimulationDescriptor());
			}
		}
		tempCodec.addParameter("hasIntegrals", new Integer(
				this.hasVisibleIntegrals));
		tempCodec.addParameter("hasPeakLabels", new Integer(
				this.hasVisiblePeakLabels));
		tempCodec.addParameter("hasPredictionLabels", new Integer(
				this.hasVisiblePredictionLabels));
		tempCodec.addParameter("isSmooth", new Boolean(this.isSmooth));

		this.addLinkXMLElements(tempCodec);

		String tempTag = "<nemo.Spectra ";
		tempTag += tempCodec.encodeParameters();
		tempTag += ">\r\n";

		tempTag += "<nemo.Spectra.Process>\r\n";

		Vector<SpectraFilter> appliedFilters = this.spectraData.getAppliedFilters();
		for (int iFilter = 0; iFilter < appliedFilters.size(); iFilter++) {
			tempTag += appliedFilters.elementAt(iFilter).getScriptingCommand();
		}
		tempTag += "</nemo.Spectra.Process>\r\n";

		for (int ent = 0; ent < this.getEntitiesCount(); ent++) {
			tempTag += ((BasicEntity) this.getEntity(ent))
					.getXmlTag(xmlProperties);
		}
		tempTag += "</nemo.Spectra>\r\n";

		return tempTag;
	}

	public boolean hasPredictions() {
		int nEntities = this.getEntitiesCount();
		BasicEntity entity;
		for (int iEntity = 0; iEntity < nEntities; iEntity++) {
			entity = this.getEntity(iEntity);
			if (entity instanceof PredictionLabel) {
				return true;
			}
		}
		return false;
	}

	// public void addPrediction(PredictionData predictionData,
	// ActMoleculeDisplay molDisplay) {
	// StereoMolecule molecule = molDisplay.getStereoMolecule();
	// this.predictionData = predictionData;
	// this.removeRangeLabels();
	// String pulseSequence = spectraData
	// .getParamString(".PULSE SEQUENCE", "");
	// Vector<PredictionLabel> predictionLabels = null;
	// double xa = this.spectraData.getFirstX();
	// double xb = this.spectraData.getLastX();
	// double ya = this.spectraData.getParamDouble("firstY", 0.0);
	// double yb = this.spectraData.getParamDouble("lastY", 0.0);
	// Nucleus yNucleus = this.getNucleus(2);
	// if (pulseSequence.matches("^hsqc.*")) {
	// predictionLabels = predictionData.generateHSQCLabels(molecule,
	// yNucleus, xa, xb, ya, yb);
	// } else if (pulseSequence.matches("^cosy.*")) {
	// predictionLabels = predictionData.generateCOSYLabels(molecule, xa,
	// xb, ya, yb);
	// } else if (pulseSequence.matches("^hmbc.*")) {
	// predictionLabels = predictionData.generateHMBCLabels(molecule,
	// yNucleus, xa, xb, ya, yb);
	// }
	//
	// if (predictionLabels != null && predictionLabels.size() > 0) {
	// // extract atom entities in order to link the prediction labels
	// // afterwards
	// SpectraDisplay parentDisplay = (SpectraDisplay) this
	// .getParentEntity();
	// InteractiveSurface interactions = parentDisplay
	// .getInteractiveSurface();
	// int nAtomEntities = molDisplay.getEntitiesCount();
	// TreeMap<Integer, ActAtomEntity> atomMap = new TreeMap<Integer,
	// ActAtomEntity>();
	// for (int iAtomEntity = 0; iAtomEntity < nAtomEntities; iAtomEntity++) {
	// if (molDisplay.getEntity(iAtomEntity) instanceof ActAtomEntity) {
	// ActAtomEntity atomEntity = (ActAtomEntity) molDisplay
	// .getEntity(iAtomEntity);
	// atomMap
	// .put(new Integer(atomEntity.getAtomID()),
	// atomEntity);
	// }
	// }
	// // add entities
	// PredictionLabel currentPredictionLabel;
	// ActAtomEntity currentAtomEntity;
	// for (int iLabel = 0; iLabel < predictionLabels.size(); iLabel++) {
	// currentPredictionLabel = predictionLabels.get(iLabel);
	// this.addEntity(currentPredictionLabel);
	// // create links
	// Iterator<Integer> iterator = currentPredictionLabel
	// .getAtomIdIterator();
	// while (iterator.hasNext()) {
	// if ((currentAtomEntity = atomMap.get(iterator.next())) != null) {
	// interactions.createLink(currentPredictionLabel,
	// currentAtomEntity, 10);
	// }
	// }
	// }
	// this.refreshSensitiveArea();
	// this.checkSizeAndPosition();
	// }
	// }

	private void removeRangeLabels() {
		int nEntities = this.getEntitiesCount();
		BasicEntity entity;
		for (int iEntity = nEntities - 1; iEntity >= 0; iEntity--) {
			entity = this.getEntity(iEntity);
			if (entity instanceof PredictionLabel) {
				this.remove(entity);
			}
		}
	}

	/**
	 * Remove the contour lines and the corresponding tile manager. This is need
	 * after a baseline correction was applied to a 2D spectrum.
	 * 
	 * Marco Engeler
	 */
	public void clearContourLines() {
		this.posContourLines.clear();
		this.negContourLines.clear();
		this.tileManager = null;
	}

	public boolean isHomonuclear() {
		if (this.drawAs2D) {
			return spectraData.getParamString("2D_X_NUCLEUS", "").equals(
					spectraData.getParamString("2D_Y_NUCLEUS", ""));
		} else {
			return false;
		}
	}

	public Nucleus getNucleus(int iNucleus) {
		return this.spectraData.getNucleus(iNucleus);
	}

	public Nucleus getNucleus() {
		return this.spectraData.getNucleus();
	}

	public ExperimentType getExperimentType() {
		return this.spectraData.getExperimentType();
	}

	public PredictionData getPredictionData() {
		return predictionData;
	}

	public void setPredictionData(PredictionData predictionData) {
		this.predictionData = predictionData;
	}

	public void checkAllIntegrals() {
		for (int ent = 0; ent < this.getEntitiesCount(); ent++) {
			BasicEntity entity = this.getEntity(ent);
			if (entity instanceof Integral) {
				Integral integral = (Integral) entity;
				integral.refreshDrawingValues();
				integral.refreshSensitiveArea();
				integral.checkSizeAndPosition();
			}
		}
		this.checkSizeAndPosition();
	//	this.refreshSensitiveArea();
	}

	public void runScript(String strScript) {
		this.runScriptOld(strScript);
	}
	
	/*
	public void runScript(String strScript) {
		try {
			Class.forName("org.mozilla.javascript.Interpreter");
			if (Spectra.DEBUG) System.out.println(getClass().getCanonicalName() + " using new scripting");
			this.runScriptNew(strScript);
		} catch (ClassNotFoundException e) {
			if (Spectra.DEBUG) System.out.println(getClass().getCanonicalName() + " using old scripting");
			this.runScriptOld(strScript);
		}
		// TODO We have suppressed the following line and it should be check and method deleted
	//	this.scriptUIPostProcessing();
	}
	*/

	/*
	public void runScriptNew(String strScript) {
		if (DEBUG) System.out.println("Running script new");
		ScriptingInstance scriptingInstance = new ScriptingInstance();
		scriptingInstance.addObjectToScope("out", System.out);
		scriptingInstance.addObjectToScope("spectraData", this.spectraData);
		scriptingInstance.addObjectToScope("jsonResult", jsonResult);
		scriptingInstance.runScript(strScript);
		try {
			jsonResult=new JSONArray((String)scriptingInstance.getObjectFromScope("jsonResult"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//jsonResult=(JSONArray)scriptingInstance.getObjectFromScope("jsonResult");
		//System.out.println("Value from scope: "+scriptingInstance.getObjectFromScope("jsonResult"));
		//System.out.println("Value of test "+);
	}
	*/

	public void runScriptOld(String strScript) {
		if ((strScript == null) || (strScript.length() == 0)) return;
		System.out.println("Running script old");
		//We need the string variables.
		String[] scriptLines = strScript.replaceAll("[\r\n]", "").split(";");
		//String[] scriptLines = strScript.split(";");
		TreeMap<String, String> variables = new TreeMap<String, String>();
		String line=null;
		for (int iLine = 0; iLine < scriptLines.length; iLine++) {
			line = scriptLines[iLine].trim();
			FilterManager.execute(this.spectraData, variables, line);
		}
	}

	private int binarySearch(float[] array, float value) {
		if (array.length < 2)
			return 0;
		int lower = 0;
		int upper = array.length - 1;
		int mid;
		if (array[0] < array[1]) {
			while (lower < upper) {
				mid = (lower + upper) / 2;
				if (value > array[mid]) {
					lower = mid + 1;
				} else {
					upper = mid;
				}
			}
			return lower;
		} else {
			while (lower < upper) {
				mid = (lower + upper) / 2;
				if (value > array[mid]) {
					upper = mid;
				} else {
					lower = mid + 1;
				}
			}
			return lower;
		}
	}

	public boolean hasContourLines() {
		// negContourLines
		if (this.negContourLines == null || this.posContourLines == null)
			return false;
		// check number of contourLines
		if ((this.negContourLines.size() + this.posContourLines.size()) > 0)
			return true;
		else
			return false;
	}

	@Override
	public void remove(BasicEntity entity) {
		if (entity instanceof SmartPeakLabel && this.spectraData.getTitle().equals(PredictionData.projectedTrace)) {
	
			// we need to remove a Fake SmartPeakLabel only if it is not connected to a PeakLabel
			int counter=0;
			for (BasicEntity linked : this.getInteractiveSurface().getLinkedEntities(entity)) {
				if (linked instanceof PeakLabel) {
					counter++;
				}
			}
			if (counter==1) {
				this.predictionData.removeFakeSmartPeak(this,(SmartPeakLabel) entity);
				this.refreshDrawArrays();
			}
		}
		super.remove(entity);
	}

	
	public PeakLabel[] getPeakLabels(boolean ascending) { // Sort
		Vector<PeakLabel> peakLabels = new Vector<PeakLabel>(); // SmartPeakLabel vector
		for (int entity = 0; entity < this.getEntitiesCount(); entity++) {
			if (this.getEntity(entity) instanceof PeakLabel)
				peakLabels.addElement((PeakLabel)this.getEntity(entity));
		}

		PeakLabel[] sPLArray = new PeakLabel[peakLabels.size()];

		peakLabels.toArray(sPLArray);

		if (ascending) {
			Arrays.sort(sPLArray);
		} else {
			Arrays.sort(sPLArray, Collections.reverseOrder());
		}
		return sPLArray;
	}
	
	public Integral[] getIntegrals() {
		Vector<Integral> integrals = new Vector<Integral>();
		for (int entity = 0; entity < this.getEntitiesCount(); entity++) {
			if (this.getEntity(entity) instanceof Integral)
				integrals.addElement((Integral)this.getEntity(entity));
		}
		Integral[] sPLArray = new Integral[integrals.size()];
		integrals.toArray(sPLArray);
		return sPLArray;
	}
	
	public JSONObject toJSON() throws JSONException {
		// We don't create a new level because a Spectra contains only one SpectraData
		JSONObject json = new JSONObject();
		json.put("spectraData", getSpectraData().toJSON());
		if(this.getSpectraData().getDataType()==SpectraData.TYPE_NMR_SPECTRUM){
			JSONArray labels = NmrHelpers.labelsToJSON(this);
			if(labels!=null&&labels.length()>0){
				json.put("signals", NmrHelpers.labelsToJSON(this));
			}
			else{
				Integral[] integralsLabel=this.getIntegrals();
				if (integralsLabel.length>0) {
					JSONArray integrals=new JSONArray();
					json.put("integrals", integrals);
					for (Integral integralLabel : integralsLabel) {
						integrals.put(integralLabel.toJSON());
					}			
				}
				PeakLabel[] peakLabels=this.getPeakLabels(true);
				if (peakLabels.length>0) {
					JSONArray peaks=new JSONArray();
					json.put("peakLabels", peaks);
					for (PeakLabel peakLabel : peakLabels) {
						//System.out.println("3"+json);
						JSONObject onePeak=new JSONObject();
						peaks.put(onePeak);
						onePeak.put("xPosition", peakLabel.getXPos());
						onePeak.put("yPosition", peakLabel.getYPos());
						onePeak.put("intensity", peakLabel.getIntensity());
						onePeak.put("comment", peakLabel.getComment());
					}			
				}
			}
		}else{
			//I guess it should be enough for IR peak labels
			PeakLabel[] peakLabels=this.getPeakLabels(true);
			if (peakLabels.length>0) {
				JSONArray peaks=new JSONArray();
				json.put("peakLabels", peaks);
				for (PeakLabel peakLabel : peakLabels) {
					//System.out.println("3"+json);
					JSONObject onePeak=new JSONObject();
					peaks.put(onePeak);
					onePeak.put("xPosition", peakLabel.getXPos());
					onePeak.put("yPosition", peakLabel.getYPos());
					onePeak.put("intensity", peakLabel.getIntensity());
					onePeak.put("comment", peakLabel.getComment());
				}			
			}
		}
		return json;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
	
	/*void appendJSON(JSONObject json) throws JSONException {
		// We don't create a new level because a Spectra contains only one SpectraData
		this.getSpectraData().appendJSON(json);
		//System.out.println("1 "+json);
		NmrHelpers.appendSmartPeakLabels(this, json);
		//System.out.println("2 "+json);
		PeakLabel[] peakLabels=this.getPeakLabels(true);
		if (peakLabels.length>0) {
			JSONArray peaks=new JSONArray();
			json.put("peakLabels", peaks);
			for (PeakLabel peakLabel : peakLabels) {
				//System.out.println("3"+json);
				JSONObject onePeak=new JSONObject();
				peaks.put(onePeak);
				onePeak.put("xPosition", peakLabel.getXPos());
				onePeak.put("yPosition", peakLabel.getYPos());
				onePeak.put("intensity", peakLabel.getIntensity());
				onePeak.put("comment", peakLabel.getComment());
			}			
		}
		
		Integral[] integralsLabel=this.getIntegrals();
		if (integralsLabel.length>0) {
			JSONArray integrals=new JSONArray();
			json.put("integrals", integrals);
			for (Integral integralLabel : integralsLabel) {
				//System.out.println("3 "+json);
				JSONObject oneIntegral=new JSONObject();
				integrals.put(oneIntegral);
				oneIntegral.put("startX", integralLabel.getStartX());
				oneIntegral.put("stopX", integralLabel.getStopX());
				oneIntegral.put("area", integralLabel.getArea());
				oneIntegral.put("integration", integralLabel.getRelArea());
				oneIntegral.put("pubIntegration", integralLabel.getPublicationValue());
			}			
		}
	}*/
}