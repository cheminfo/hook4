package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.BasicDisplay;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.EntityResizer;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.filters.BaselineCorrectionHWFilter;
import org.cheminfo.hook.nemo.filters.FilterType;
import org.cheminfo.hook.nemo.nmr.PredictionData;
import org.cheminfo.hook.util.XMLCoDec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

public class SpectraDisplay extends BasicDisplay {
	public final static double SCALE_RELATIVE_SIZE = 0.075;
	public final static double GAP_RELATIVE_SIZE = 0.03; // distance between
	// spectra

	private boolean hasVScale, hasHScale, is2D;
	
	// the Y Scale may be sometime absolute. This will have influence in:
	// add spectra
	// vertical scale
	// no vertical movement
	// display the values on the VerticalScale
	// etc.
	private boolean absoluteYScale;
	private static boolean DEBUG=false;
	
	private double rightSelection, leftSelection, topSelection,bottomSelection;
	private double currentRightLimit, currentLeftLimit, currentTopLimit,currentBottomLimit;
	private double fulloutRightLimit, fulloutLeftLimit, fulloutTopLimit,fulloutBottomLimit;

	private int nbAddedSpectra;

	private Spectra horRefSpectrum = null;
	private Spectra verRefSpectrum = null;

	
	private HashMap<Character,NemoView> views=new HashMap<Character,NemoView>();
	
	public void init() {
		super.init();
		
		this.setLinkable(false);

		this.nbAddedSpectra = 0;

		this.setPrimaryColor(Color.red);
		this.setSecondaryColor(Color.white);

		this.setCursorType(BasicDisplay.NONE);
		this.hasVScale = false;
		this.hasHScale = true;
		this.is2D = false;

		this.currentLeftLimit = 0;
		this.currentRightLimit = 0;

		this.horRefSpectrum = null;
		this.verRefSpectrum = null;

		this.addEntity(new HorizontalScale());
		this.addEntity(new VerticalScale());
		
		this.absoluteYScale=false;
	}

	public void init(String XMLTag, Hashtable helpers) {
		if (this.getParentEntity() instanceof BasicEntity) {
			BasicEntity parentEntity = (BasicEntity) this.getParentEntity();
			this.init(XMLTag, parentEntity.getWidth(), parentEntity.getHeight(), helpers);
		} else {
			this.init(XMLTag, this.getInteractiveSurface().getSize().width, this.getInteractiveSurface().getSize().height, helpers);
		}
		this.setLinkable(false);
	}

	public void init(String XMLTag, double parentWidth, double parentHeight, Hashtable helpers) {
		init(XMLTag, parentWidth, parentHeight, helpers, false);
	}

	public void init(String XMLTag, double parentWidth, double parentHeight, Hashtable helpers, boolean storeTotalJcamp) {
		XMLCoDec tempCodec = new XMLCoDec(XMLTag);
		tempCodec.shaveXMLTag();
		this.setLinkable(false);

		if (this.getEntitiesCount() != 0)
			this.removeAll();
		
		this.setUniqueID(tempCodec.getParameterAsInt("uniqueID"));
		this.storeLinkIDs(tempCodec);
		
		this.setSize(parentWidth * tempCodec.getParameterAsDouble("relWidth"),
				parentHeight * tempCodec.getParameterAsDouble("relHeight"));
		this.setLocation(parentWidth * tempCodec.getParameterAsDouble("relX"),
				parentHeight * tempCodec.getParameterAsDouble("relY"));

		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor"));

		this.hasHScale = tempCodec.getParameterAsBoolean("hScale");

		this.hasVScale = tempCodec.getParameterAsBoolean("vScale");

		this.is2D = tempCodec.getParameterAsBoolean("is2D");
		int horRefID = tempCodec.getParameterAsInt("horRefSpectrumID");
		int verRefID = tempCodec.getParameterAsInt("verRefSpectrumID");

		this.absoluteYScale=tempCodec.getParameterAsBoolean("absoluteYScale");
		
		this.setMovementType(tempCodec.getParameterAsInt("movementType"));
		// this.setErasable(false);
		// this.setResizable(false);
		// nbSpectra=0;

		this.setCurrentLimits(tempCodec.getParameterAsDouble("leftLimit"),
				tempCodec.getParameterAsDouble("rightLimit"), tempCodec
						.getParameterAsDouble("topLimit"), tempCodec
						.getParameterAsDouble("bottomLimit"));

		int elements = tempCodec.getRootElementsCount();

		for (int elem = 0; elem < elements; elem++) {
			XMLCoDec tempCodec2 = new XMLCoDec(tempCodec.readXMLTag());

			tempCodec2.shaveXMLTag();
			if (tempCodec2.getParameterAsString("tagName").trim().compareTo("nemo.Spectra") == 0) {
				//System.out.println(tempCodec.getXMLString());
				Spectra spectra = new Spectra(tempCodec.popXMLTag(), this.getWidth(), this.getHeight(), helpers, storeTotalJcamp);
				this.addEntity(spectra);
				nbAddedSpectra++;
				if (helpers.contains("originalSpectraData") == true) {
					spectra.setSpectraNb(0);
				}
				/*Spectra spectra = new Spectra(tempCodec.popXMLTag(), this.getWidth(), this.getHeight(), helpers);
				this.addEntity(spectra);
				nbAddedSpectra++;
				if (helpers.contains("originalSpectraData") == true) {
					spectra.setSpectraNb(0);
				}*/
			} else if (tempCodec2.getParameterAsString("tagName").trim().indexOf("Display") != -1) {
				try {

					Class actionClass = Class.forName("org.cheminfo.hook."
									+ tempCodec2.getParameterAsString("tagName").trim());

					BasicDisplay display = (BasicDisplay) actionClass.newInstance();
					
					this.addEntity(display, 0);

					Class[] argumentTypes = { String.class, Double.TYPE,
							Double.TYPE, Hashtable.class };

					Method actionMethod = actionClass.getMethod("init",
							argumentTypes);

					Object[] arguments = { tempCodec.popXMLTag(),
							new Double(this.getWidth()),
							new Double(this.getHeight()), helpers };

					actionMethod.invoke(display, arguments);

				} catch (ClassNotFoundException ex1) {
					System.out.println(ex1);
				} catch (IllegalAccessException ex2) {
					System.out.println(ex2);
				} catch (InstantiationException ex3) {
					System.out.println(ex3);
				} catch (InvocationTargetException ex4) {
					System.out.println(ex4);
				} catch (NoSuchMethodException ex5) {
					System.out.println(ex5);
				}
				;
			} else {
				try {
					Class entityClass = Class
							.forName("org.cheminfo.hook."
									+ tempCodec2
											.getParameterAsString("tagName")
											.trim());
					Class[] parameterClasses = { String.class, Hashtable.class };
					java.lang.reflect.Constructor entityConstructor = entityClass
							.getConstructor(parameterClasses);

					Object[] parameters = { tempCodec.popXMLTag(), helpers };
					this.addEntity((BasicEntity) entityConstructor
							.newInstance(parameters));
				} catch (Exception e) {
					System.out.println("SpectraDisplay XML constructor e: " + e
							+ ", cause: " + e.getCause());
				}

			}

		}
		this.setCurrentLimits(this.currentLeftLimit, this.currentRightLimit,this.currentTopLimit, this.currentBottomLimit);
		// this.updateSpectra();

		this.checkFulloutLimits();

		if (this.getFirstSpectra() != null) {
			if (this.getInteractiveSurface() != null) {
				this.getInteractiveSurface().clearActiveEntities();
				// this.getInteractiveSurface().addActiveEntity(this.getFirstSpectra());
				this.getInteractiveSurface().checkButtonsStatus();
			}
		}

		if (this.is2D()) {
			if (horRefID != 0) {
				for (int ent = 0; ent < this.getEntitiesCount(); ent++) {
					if (this.getEntity(ent) instanceof Spectra
							&& this.getEntity(ent).getUniqueID() == horRefID) {
						this.horRefSpectrum = (Spectra) this.getEntity(ent);
					}
				}
			}
			if (verRefID != 0) {
				for (int ent = 0; ent < this.getEntitiesCount(); ent++) {
					if (this.getEntity(ent) instanceof Spectra
							&& this.getEntity(ent).getUniqueID() == verRefID) {
						this.verRefSpectrum = (Spectra) this.getEntity(ent);
					}
				}
			}
			this.shuffleMolDisplay();

		}
		this.setCurrentLimits(this.currentLeftLimit, this.currentRightLimit, this.currentTopLimit, this.currentBottomLimit);
		this.refreshSensitiveArea();
		this.checkSizeAndPosition();
		this.getInteractiveSurface().repaint();
	}

	/*
	 * make ActMolDisplay the last entities of this object
	 */
	private void shuffleMolDisplay() {
		// we don't do a proper shuffle as we need to keep the sequence of
		// spectra. Otherwise the whole thing goes tits up
		for (int ent = 0; ent < this.getEntitiesCount(); ent++)
			if (this.getEntity(ent) instanceof ActMoleculeDisplay)
				this.moveEntityToLastPostion(ent);
	}

	public void hasVScale(boolean hasVScale) {
		this.hasVScale = hasVScale;
	}

	void switchVScale() {
		this.hasVScale = !this.hasVScale;
	}

	public boolean hasVScale() {
		return this.hasVScale;
	}

	public void hasHScale(boolean hasHScale) {
		this.hasHScale = hasHScale;
	}

	void switchHScale() {
		this.hasHScale = !this.hasHScale;
	}
	
	void setVScale(boolean showVScale) {
		this.hasVScale=showVScale;
	}

	void setHScale(boolean showHScale) {
		this.hasHScale=showHScale;
	}
	
	public boolean hasHScale() {
		return this.hasHScale;
	}

	public boolean is2D() {
		return this.is2D;
	}

	public double getCurrentRightLimit() {
		return this.currentRightLimit;
	}

	public double getCurrentLeftLimit() {
		return this.currentLeftLimit;
	}

	public double getCurrentTopLimit() {
		return this.currentTopLimit;
	}

	public double getCurrentBottomLimit() {
		return this.currentBottomLimit;
	}

	public void setSelectionLimits(double leftSelectionLimit,
			double rightSelectionLimit) {
		this.leftSelection = leftSelectionLimit;
		this.rightSelection = rightSelectionLimit;
	}

	public void setSelectionLimits(double leftSelectionLimit,
			double rightSelectionLimit, double topSelectionLimit,
			double bottomSelectionLimit) {
		this.leftSelection = leftSelectionLimit;
		this.rightSelection = rightSelectionLimit;
		this.topSelection = topSelectionLimit;
		this.bottomSelection = bottomSelectionLimit;
	}

	public void addSpectra(Spectra newSpectra) {
		this.addSpectra(newSpectra, true);
	}
	
	public void addSpectra(Spectra newSpectra, boolean fullSpectra) {
		this.nbAddedSpectra++;
		this.addEntity(newSpectra);
		
		// System.out.println("SPECTRA TYPE: "+newSpectra.spectraData.getDataType());
		
		if ((newSpectra.spectraData.getDataType() == SpectraData.TYPE_2DNMR_SPECTRUM) ||
				(newSpectra.spectraData.getDataType() == SpectraData.TYPE_2DNMR_FID))
			{
			this.is2D = true;
			this.hasVScale = true;
			newSpectra.setLocation(0, 0);
			newSpectra.drawAs2D();
		}

		if (this.is2D) {
			//If it is 2D we have to deactivate the integral at the the axis mode
			if (newSpectra.spectraData.getDataType() == SpectraData.TYPE_NMR_SPECTRUM) {
				SpectraData newSpectraData = newSpectra.getSpectraData();
				if (newSpectraData.getAppliedFilterByFilterType(FilterType.BASELINE) == null && newSpectraData.getTitle().indexOf(PredictionData.projectedTrace) == -1) {
					BaselineCorrectionHWFilter filter = new BaselineCorrectionHWFilter();
					filter.setWaveletScale(BaselineCorrectionHWFilter.getOptimalWaveletScale(newSpectra.spectraData));
					filter.setWhittakerLambda(BaselineCorrectionHWFilter.getOptimalLambda(newSpectra.spectraData));
					newSpectraData.applyFilter(filter);
				}
				if (newSpectra.isVertical() == false) {
					this.horRefSpectrum = newSpectra;
				} else {
					this.verRefSpectrum = newSpectra;
				}
				
			}
		}

		for (int ent = 0; ent < this.getEntitiesCount(); ent++) {
			if (this.getEntity(ent) instanceof Spectra)
				((BasicEntity)this.getEntity(ent)).refreshSensitiveArea();
		}

		//System.out.println("The position before checkFulloutLimits: "+newSpectra.getLocation());
		
		this.checkFulloutLimits();
		
		//System.out.println("The position after checkFulloutLimits: "+newSpectra.getLocation());
		
		if (fullSpectra) this.fullSpectra();
		
		//System.out.println("The position after fullSpectra: "+newSpectra.getLocation());
		
		if ((this.is2D && this.getFirstSpectra() == newSpectra) || !this.is2D) {
			this.getInteractiveSurface().setActiveEntity(newSpectra);
		}
		this.checkSizeAndPosition();
		
		//System.out.println("The position after checkSizeAndPosition: "+newSpectra.getLocation());
		
		if (this.getInteractiveSurface() != null)
			this.getInteractiveSurface().repaint();
	}


	/**
	 * Returns the number of spectra that have been added to this display
	 * independently from the fact that they are still there or not.
	 * 
	 * @return
	 */
	public int getNbAddedSpectra() {
		return this.nbAddedSpectra;
	}

	/**
	 * Returns the actual number of spectra present in the display.
	 * 
	 * @return
	 */
	public int getNbSpectra() {
		int counter = 0;
		for (int ent = 0; ent < this.getEntitiesCount(); ent++)
			if (this.getEntity(ent) instanceof Spectra)
				counter++;

		return counter;
	}

	public Vector<Spectra> getAllSpectra() {
		Vector<Spectra> allSpectra = new Vector<Spectra>();
		for (int ent = 0; ent < this.getEntitiesCount(); ent++)
			if (this.getEntity(ent) instanceof Spectra)
				allSpectra.add((Spectra) this.getEntity(ent));
		return allSpectra;
	}
	
	public Spectra getSpectra(String name) {
		for (int ent = 0; ent < this.getEntitiesCount(); ent++) {
			if (this.getEntity(ent) instanceof Spectra) {
				
				Spectra spectra=((Spectra)this.getEntity(ent));
				if(DEBUG )System.out.println("FOUND: "+spectra.getReferenceName()+" looking for: "+name);
				if (spectra.getReferenceName()!=null && spectra.getReferenceName().equals(name)) {
					return spectra;
				}
			}
		}
		return null;
	}
	

	public void remove1DSpectra(String titleContent) {
		for (int ent = this.getEntitiesCount() - 1; ent >= 0; ent--) {
			if (this.getEntity(ent) instanceof Spectra) {
				Spectra spectrum = (Spectra) this.getEntity(ent);
				if (spectrum.getSpectraData().getTitle().indexOf(titleContent) != -1) {
					this.remove(ent);
				}
			}
		}

	}

	public Spectra getFirstSpectra() {
		for (int ent = 0; ent < this.getEntitiesCount(); ent++) {
			if (this.getEntity(ent) instanceof Spectra) {
				return (Spectra) this.getEntity(ent);
			}
		}

		return null;
	}

	public Spectra getLastSpectra() {
		for (int ent = this.getEntitiesCount() - 1; ent >= 0; ent--) {
			if (this.getEntity(ent) instanceof Spectra) {
				return (Spectra) this.getEntity(ent);
			}
		}

		return null;
	}

	public Spectra getHorRefSpectrum() {
		return this.horRefSpectrum;
	}

	public Spectra getVerRefSpectrum() {
		return this.verRefSpectrum;
	}

	/**
	 * Method to create a small display inside the display.
	 * <li> Create the new SpectraDisplay and adds it to the entities Vector
	 * <li> Adds an EntityResizer to that SpectraDisplay
	 * <li> Makes the new SpectraDisplay erasable ans resizable </li>
	 * 
	 * @param theSpectra -
	 *            Spectra object represented by the new SpectraDisplay
	 * @param firstLimit -
	 *            a double used to set the current limits
	 * @param secondLimit -
	 *            a double used to set the current limits
	 */
	protected SpectraDisplay addInsertDisplay(Spectra theSpectra,
			double firstLimit, double secondLimit) {
		SpectraDisplay newDisplay = new SpectraDisplay();
		Spectra newSpectra = new Spectra(theSpectra.getSpectraData());

		this.addEntity(newDisplay, 0);

		// newDisplay.add(new EntityResizer(EntityResizer.SE_RESIZER));

		newDisplay.init();
		newDisplay.setSize(200, 150);
		newDisplay.hasHScale(true);

		newDisplay.refreshSensitiveArea();

		newDisplay.addSpectra(newSpectra);
		newSpectra.setLocation(0, -newDisplay.getHeight() * SpectraDisplay.GAP_RELATIVE_SIZE);

		newDisplay.setLocation(300, 100);

		newDisplay.setPrimaryColor(this.getPrimaryColor());

		newDisplay.hasVScale(false);

		newDisplay.setCurrentLimits(firstLimit, secondLimit);
		newDisplay.setMovementType(BasicEntity.GLOBAL);

		newDisplay.addEntity(new EntityResizer(EntityResizer.SE_RESIZER));
		// newDisplay.setDefaultIntegralColor(this.defaultIntegralColor);
		// newDisplay.setDefaultLabelColor(this.defaultLabelColor);

		// newDisplay.setErasable(1);
		this.getInteractiveSurface().grabFocus();
		this.getInteractiveSurface().repaint();

		return newDisplay;
	}

	public void setCurrentLimits(double leftLimit, double rightLimit) {
		this.currentLeftLimit = leftLimit;
		this.currentRightLimit = rightLimit;
	}

	public void scaleVertically(double factor) {
		this.currentTopLimit=this.currentTopLimit*factor;
	}
	
	public void setCurrentLimits(double leftLimit, double rightLimit, double topLimit, double bottomLimit) {
		this.currentLeftLimit = leftLimit;
		this.currentRightLimit = rightLimit;
		
		if (! this.isAbsoluteYScale() || (this.currentTopLimit==this.currentBottomLimit)) {
			this.currentTopLimit = topLimit;
			this.currentBottomLimit = bottomLimit;
		}
		
		// System.out.println("We set the current limits: "+this.currentTopLimit+" - "+this.currentBottomLimit);
		
		if (this.is2D) {
			Spectra firstSpectra = this.getFirstSpectra();
			if (firstSpectra != null) {
				firstSpectra.setCurrentLimits(leftLimit, rightLimit, topLimit,bottomLimit);
			}
		}
	}

	public void checkFulloutLimits() {
		boolean isPeakTable = false;
		if (this.getFirstSpectra() != null) {
			this.fulloutLeftLimit = this.getFirstSpectra().getSpectraData().getFirstX();
			this.fulloutRightLimit = this.getFirstSpectra().getSpectraData().getLastX();
		}
		
		/*
		 * if (this.getFirstSpectra().isDrawnAs2D()) {
		 * this.fulloutTopLimit=this.getFirstSpectra().getSpectraData().getParamDouble("lastY",
		 * 0);
		 * this.fulloutBottomLimit=this.getFirstSpectra().getSpectraData().getParamDouble("firstY",
		 * 0); }
		 */
		boolean temp2D = false;

		for (int i = 0; i < this.getEntitiesCount(); i++) {
			if (this.getEntity(i) instanceof Spectra) {
				Spectra tempSpectra = (Spectra) this.getEntity(i);
				SpectraData spectraData = tempSpectra.getSpectraData();

				if (tempSpectra.getSpectraData().isDataClassPeak())
					isPeakTable = true;

				if (tempSpectra.isVertical()) {
					// this.fulloutBottomLimit=Math.max(this.fulloutBottomLimit,
					// tempSpectra.getSpectraData().getFirstX());
					// this.fulloutTopLimit=Math.min(this.fulloutTopLimit,
					// tempSpectra.getSpectraData().getLastX());
				} else {
					// System.out.println(spectraData.getFirstX()+" - "+spectraData.getLastX()+" - "+fulloutLeftLimit+" - "+fulloutRightLimit);
					if (this.fulloutLeftLimit>this.fulloutRightLimit) {
						this.fulloutLeftLimit = Math.max(this.fulloutLeftLimit, spectraData.getFirstX());
						this.fulloutRightLimit = Math.min(this.fulloutRightLimit, spectraData.getLastX());
					} else {
						this.fulloutLeftLimit = Math.min(this.fulloutLeftLimit, spectraData.getFirstX());
						this.fulloutRightLimit = Math.max(this.fulloutRightLimit, spectraData.getLastX());						
					}
				}

				if (this.isAbsoluteYScale()) {
					if (this.fulloutBottomLimit==this.fulloutTopLimit) {
						this.fulloutTopLimit=spectraData.getMaxY();
						this.fulloutBottomLimit=spectraData.getMinY();
					}
					
					this.fulloutTopLimit = Math.max(this.fulloutTopLimit,spectraData.getMaxY());
					this.fulloutBottomLimit = Math.min(this.fulloutBottomLimit, spectraData.getMinY());
					
					this.currentTopLimit = fulloutTopLimit;
					this.currentBottomLimit = fulloutBottomLimit;
					
					System.out.println("FULL OUT: "+this.fulloutTopLimit+" - "+this.fulloutBottomLimit);
					System.out.println("---> "+tempSpectra.getLocation());
				}
				
				if (tempSpectra.isDrawnAs2D()) {
					if (temp2D == false) {
						temp2D = true;
						this.fulloutTopLimit = spectraData.getParamDouble("lastY", 0);
						this.fulloutBottomLimit = spectraData.getParamDouble("firstY", 0);
					} else {
						this.fulloutTopLimit = Math.min(this.fulloutTopLimit,spectraData.getParamDouble("lastY", 0));
						this.fulloutBottomLimit = Math.max(this.fulloutBottomLimit, spectraData.getParamDouble("firstY", 0));
					}
				}

			}
		}

		if (isPeakTable) {
			this.fulloutLeftLimit -= 0.02 * Math.abs(this.fulloutLeftLimit - fulloutRightLimit);
			this.fulloutRightLimit += 0.02 * Math.abs(this.fulloutLeftLimit - fulloutRightLimit);
		}
	}

	/**
	 * Sets the current limits so that all Spectra object are entirely visible.
	 */
	public void fullSpectra() {
		if (this.getFirstSpectra() != null && this.getFirstSpectra().isDrawnAs2D()) {
			this.setCurrentLimits(fulloutLeftLimit, fulloutRightLimit, fulloutTopLimit, fulloutBottomLimit);
		} else {
			if (this.isAbsoluteYScale()) {
				// need to go through all the spectra ...
				this.setCurrentLimits(fulloutLeftLimit, fulloutRightLimit, fulloutTopLimit, fulloutBottomLimit);
			} else {
				this.setCurrentLimits(fulloutLeftLimit, fulloutRightLimit);
			}
		}
			
		// this.getInteractiveSurface().repaint();
	}

	
	public void checkAndRepaint() {
		// maybe we should only call this function is the SpectraData has been changed ?
		this.checkFulloutLimits();
		this.fullSpectra();
		this.checkSizeAndPosition();
		this.refreshSensitiveArea();
		this.getInteractiveSurface().checkButtonsStatus();
		this.getInteractiveSurface().repaint();
	}
	
	public double absolutePixelsToUnitsH(double xPixel) {
		double actualWidth = this.getWidth();

		xPixel -= this.getLocation().x;

		if (this.hasVScale) {
			xPixel -= this.getWidth() * SpectraDisplay.SCALE_RELATIVE_SIZE;
			actualWidth = this.getWidth() * (1 - SpectraDisplay.SCALE_RELATIVE_SIZE);

			if (this.is2D) {
				xPixel -= this.getWidth() * SpectraDisplay.SCALE_RELATIVE_SIZE;
				actualWidth = this.getWidth() * (1 - 2 * SpectraDisplay.SCALE_RELATIVE_SIZE);
			}
		}

		return this.currentLeftLimit
				+ (this.currentRightLimit - this.currentLeftLimit) * xPixel
				/ actualWidth;
	}

	public double relativeUnitsToPixelsH(double xUnits) {
		double retValue;
		double tempWidth = this.getWidth();
		if (this.hasVScale) {
			if (this.is2D)
				tempWidth = this.getWidth() * (1 - 2 * SpectraDisplay.SCALE_RELATIVE_SIZE);
			else
				tempWidth = this.getWidth() * (1 - SpectraDisplay.SCALE_RELATIVE_SIZE);
		}

		retValue = tempWidth * xUnits / (this.currentLeftLimit - this.currentRightLimit);
		if (this.hasVScale) {
			if (this.is2D)
				retValue += this.getWidth() * 2 * SpectraDisplay.SCALE_RELATIVE_SIZE;
			else
				retValue += this.getWidth() * SpectraDisplay.SCALE_RELATIVE_SIZE;

		}

		return retValue;
	}

	public double absolutePixelsToUnitsV(double yPixel) {
		double actualHeight = this.getHeight();
		yPixel -= this.getLocation().y;
		if (this.hasHScale) {
			if (this.is2D) {
				yPixel -= this.getHeight() * 2 * SpectraDisplay.SCALE_RELATIVE_SIZE;
				actualHeight = this.getHeight() * (1 - 3 * SpectraDisplay.SCALE_RELATIVE_SIZE);
			} else {
				yPixel -= this.getHeight() * SpectraDisplay.SCALE_RELATIVE_SIZE;
				actualHeight = this.getHeight() * (1 - SpectraDisplay.SCALE_RELATIVE_SIZE);
			}
		}
		return this.currentTopLimit + (this.currentBottomLimit - this.currentTopLimit) * yPixel / actualHeight;
	}

	public double pixelsPerUnit() {
		double tempWidth = this.getWidth();
		if (this.hasVScale) {
			if (this.is2D)
				tempWidth = this.getWidth() * (1 - 2 * SpectraDisplay.SCALE_RELATIVE_SIZE);
			else
				tempWidth = this.getWidth() * (1 - SpectraDisplay.SCALE_RELATIVE_SIZE);
		}

		return tempWidth / (this.currentLeftLimit - this.currentRightLimit);
	}

	public double unitsPerPixelH() {
		double tempWidth = this.getWidth();
		if (this.hasVScale) {
			if (this.is2D)
				tempWidth = this.getWidth()
						* (1 - 2 * SpectraDisplay.SCALE_RELATIVE_SIZE);
			else
				tempWidth = this.getWidth()
						* (1 - SpectraDisplay.SCALE_RELATIVE_SIZE);
		}

		return (this.currentLeftLimit - this.currentRightLimit) / tempWidth;
	}

	public double unitsPerPixelV() {
		double tempHeight = this.getHeight();
		if (this.hasVScale) {
			if (this.is2D)
				tempHeight = this.getHeight()
						* (1 - 3 * SpectraDisplay.SCALE_RELATIVE_SIZE);
			else
				tempHeight = this.getHeight()
						* (1 - SpectraDisplay.SCALE_RELATIVE_SIZE);
		}

		return (this.currentTopLimit - this.currentBottomLimit) / tempHeight;
	}

	public void checkSizeAndPosition() {
		if (this.getParentEntity() == null)
			this.setSize(this.getInteractiveSurface().getSize().width, this.getInteractiveSurface().getSize().height);
		this.setLocation(this.getLocation());
		super.checkSizeAndPosition();
		
	}


	
	public void paint(Graphics2D g) {
		g.setClip(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));

		if (this.getSecondaryColor() != null) {
			g.setColor(this.getSecondaryColor());
			g.fill(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));
		}

		if (this.getPrimaryColor() != null) {
			g.setColor(this.getPrimaryColor());

			// g.drawLine(0, (int)this.getHeight()/2, (int)this.getWidth(),
			// (int)this.getHeight()/2);
			if (this.isSelected() || this.isMouseover())
				g.draw(new Rectangle2D.Double(0, 0, this.getWidth() - 1, this.getHeight() - 1));
		}
		g.setTransform(new AffineTransform());		
		
		g.setColor(Color.red);
		
		Point2D.Double contactP = this.getInteractiveSurface().getContactPoint();
		Point2D.Double releaseP = this.getInteractiveSurface().getReleasePoint();
		switch (this.getCursorType()) {
		case BasicDisplay.CROSSHAIR:
			g.draw(new Line2D.Double(contactP.x, this.getLocation().y, contactP.x, this.getLocation().y + this.getHeight()));
			g.draw(new Line2D.Double(this.getLocation().x, contactP.y, this.getLocation().x + this.getWidth(), contactP.y));
			this.setCursorType(BasicDisplay.NONE);
			break;
		case BasicDisplay.CROSSHAIR_RECT:
			g.draw(new Line2D.Double(contactP.x, this.getLocation().y,
					contactP.x, this.getLocation().y + this.getHeight()));
			g.draw(new Line2D.Double(this.getLocation().x, contactP.y, this.getLocation().x
					+ this.getWidth(), contactP.y));
			g.draw(new Line2D.Double(releaseP.x, this.getLocation().y,
					releaseP.x, this.getLocation().y + this.getHeight()));
			g.draw(new Line2D.Double(this.getLocation().x, releaseP.y, this.getLocation().x
					+ this.getWidth(), releaseP.y));
			break;
		case BasicDisplay.LINES:
			g.draw(new Line2D.Double(this.getInteractiveSurface()
					.getReleasePoint().x, this.getLocation().y, this
					.getInteractiveSurface().getReleasePoint().x, this
					.getLocation().y
					+ this.getHeight()));
			g.draw(new Line2D.Double(this.getInteractiveSurface()
					.getContactPoint().x, this.getLocation().y, this
					.getInteractiveSurface().getContactPoint().x, this
					.getLocation().y
					+ this.getHeight()));
			break;

		case BasicDisplay.RECT:

			g.draw(new Rectangle2D.Double(Math.min(contactP.x, releaseP.x),
					Math.min(contactP.y, releaseP.y), Math.abs(contactP.x
							- releaseP.x), Math.abs(contactP.y - releaseP.y)));
			break;

		case BasicDisplay.FILLED_RECT:
			g.setColor(Color.red.darker());
			g.fill(new Rectangle2D.Double(Math.min(contactP.x, releaseP.x),
					Math.min(contactP.y, releaseP.y), Math.abs(contactP.x
							- releaseP.x), Math.abs(contactP.y - releaseP.y)));
			g.setColor(Color.red);
			g.draw(new Rectangle2D.Double(Math.min(contactP.x, releaseP.x),
					Math.min(contactP.y, releaseP.y), Math.abs(contactP.x - releaseP.x), Math.abs(contactP.y - releaseP.y)));
			break;

		default:
			break;
		}
		super.paint(g);
		g.setTransform(this.getInteractiveSurface().getGlobalTransform());
		g.setClip(new Rectangle2D.Double(0, 0, this.getInteractiveSurface().getSize().width, this.getInteractiveSurface().getSize().height));
	}

	public String getXmlTag(Hashtable xmlProperties) {
		XMLCoDec tempCodec = new XMLCoDec();
		String tempTag = "";

		Point2D.Double parentSize = new Point2D.Double();

		if (this.getParentEntity() instanceof BasicEntity) {
			parentSize.x = this.getParentEntity().getWidth();
			parentSize.y = this.getParentEntity().getHeight();
		} else {
			parentSize.x = this.getInteractiveSurface().getSize().width;
			parentSize.y = this.getInteractiveSurface().getSize().height;
		}

		tempCodec.addParameter("relX", new Double(this.getLocation().x
				/ parentSize.x));
		tempCodec.addParameter("relY", new Double(this.getLocation().y
				/ parentSize.y));
		tempCodec.addParameter("relWidth", new Double(this.getWidth()
				/ parentSize.x));
		tempCodec.addParameter("relHeight", new Double(this.getHeight()
				/ parentSize.y));
		tempCodec.addParameter("hScale", new Boolean(this.hasHScale));
		tempCodec.addParameter("vScale", new Boolean(this.hasVScale));

		tempCodec.addParameter("absoluteYScale", new Boolean(this.absoluteYScale));
		
		if (this.is2D()) {
			if (this.horRefSpectrum != null)
				tempCodec.addParameter("horRefSpectrumID", new Integer(
						this.horRefSpectrum.getUniqueID()));
			if (this.verRefSpectrum != null)
				tempCodec.addParameter("verRefSpectrumID", new Integer(
						this.verRefSpectrum.getUniqueID()));
		}

		if (this.getPrimaryColor() == null)
			tempCodec.addParameter("primaryColor", 0 + "," + 0 + "," + 0);
		else
			tempCodec.addParameter("primaryColor", this.getPrimaryColor()
					.getRed()
					+ ","
					+ this.getPrimaryColor().getGreen()
					+ ","
					+ this.getPrimaryColor().getBlue());

		tempCodec.addParameter("leftLimit", new Double(this
				.getCurrentLeftLimit()));
		tempCodec.addParameter("rightLimit", new Double(this
				.getCurrentRightLimit()));
		tempCodec.addParameter("topLimit", new Double(this.currentTopLimit));
		tempCodec.addParameter("bottomLimit", new Double(
				this.currentBottomLimit));
		tempCodec.addParameter("movementType", new Integer(this
				.getMovementType()));
		tempCodec.addParameter("is2D", new Boolean(this.is2D()));

		this.addLinkXMLElements(tempCodec);

		tempTag += "<nemo.SpectraDisplay ";
		tempTag += tempCodec.encodeParameters();
		tempTag += ">\r\n";

		// this.lastXMLTag=tempTag+"</nemo.SpectraDisplay>\r\n";
		for (int ent = 0; ent < this.getEntitiesCount(); ent++) {
			tempTag += ((BasicEntity) this.getEntity(ent))
					.getXmlTag(xmlProperties);
		}
		tempTag += "</nemo.SpectraDisplay>\r\n";

		return tempTag;
	}

	public boolean isAbsoluteYScale() {
		return absoluteYScale;
	}

	public void setAbsoluteYScale(boolean absoluteYScale) {
		this.absoluteYScale = absoluteYScale;
	}

	public String getOverMessage() {
		return "SpectraDisplay";
	}

	public double getFulloutRightLimit() {
		return fulloutRightLimit;
	}

	public void setFulloutRightLimit(double fulloutRightLimit) {
		this.fulloutRightLimit = fulloutRightLimit;
	}

	public double getFulloutLeftLimit() {
		return fulloutLeftLimit;
	}

	public void setFulloutLeftLimit(double fulloutLeftLimit) {
		this.fulloutLeftLimit = fulloutLeftLimit;
	}

	public double getFulloutTopLimit() {
		return fulloutTopLimit;
	}

	public void setFulloutTopLimit(double fulloutTopLimit) {
		this.fulloutTopLimit = fulloutTopLimit;
	}

	public double getFulloutBottomLimit() {
		return fulloutBottomLimit;
	}

	public void setFulloutBottomLimit(double fulloutBottomLimit) {
		this.fulloutBottomLimit = fulloutBottomLimit;
	}

	public void set2D(boolean is2D) {
		this.is2D = is2D;
	}

	public double getRightSelection() {
		return rightSelection;
	}

	public double getLeftSelection() {
		return leftSelection;
	}

	public double getTopSelection() {
		return topSelection;
	}

	public double getBottomSelection() {
		return bottomSelection;
	}

	public Spectra createTraceSpectrum(boolean isVertical) {
		PredictionData predictionData = new PredictionData();
		//System.out.println("Create trace: "+isVertical);
		Spectra newSpectrum = predictionData.createTraceSpectrum(this, isVertical);
		
		int vPosition = isVertical ? 0 : this.getNbAddedSpectra() * (-20) - 10;
		newSpectrum.setLocation(newSpectrum.getLocation().x, vPosition);
		
		this.addSpectra(newSpectrum, false);


		
	// If we try to repaint to quickly it does not work because the 2D may be zoomed and this will force
	//	a zoom out
	//	newSpectrum.refreshSensitiveArea();
	//	newSpectrum.checkSizeAndPosition();
		return newSpectrum;
	}
	
	public void storeView(Character character) {
		views.put(character, new NemoView(this));
	}
	
	/** 
	 * Loads a previously saved view or otherwise save it
	 * @param character
	 */
	public boolean loadViewIfExists(Character character) {
		if(character.compareTo(Nemo.SHOW_ALL_SC)==0){
			int k=0;
			NemoView full = views.get(character);
			for (Spectra spectra : this.getAllSpectra()) {
				if(spectra.isDrawnAs2D()){
					this.set2D(true);
					this.setCurrentLimits(full.leftLimit, full.rightLimit, full.topLimit, full.bottomLimit);
					this.set2D(full.is2D);
					this.setVScale(full.vScale);
					this.setHScale(full.hScale);
				}
				spectra.setVisible(true);
			}
				Vector<BasicEntity> entities = this.getEntities();
				int index=0;
				for(int i=0;i<entities.size();i++){
					if(entities.get(i) instanceof Spectra){
							((Spectra)entities.get(i)).setMultFactor(0.9);
							Point2D.Double actualLocation = ((Spectra)entities.get(i)).getLocation();
							((Spectra)entities.get(i)).setLocation(actualLocation.x,actualLocation.y);//-10-(index++)*20);
					}
				}
				this.getInteractiveSurface().takeUndoSnapshot();
				//interactions.takeUndoSnapshot();
				this.fullSpectra();
				this.checkSizeAndPosition();
				this.getInteractiveSurface().checkButtonsStatus();
				this.getInteractiveSurface().repaint();
				return true;
		}
		else{
			if (views.containsKey(character)) {
				views.get(character).applyTo(this);
				return true;
			} else {
				views.put(character, new NemoView(this));
				return false;
			}
		}
	}
	
	public void appendJSON(JSONObject jsonMainDisplay) throws JSONException {
		//System.out.println("SpectraDisplay");
		JSONArray jsonAllSpectra=new JSONArray();
		jsonMainDisplay.put("spectra", jsonAllSpectra);
		
		Vector<Spectra> allSpectra=this.getAllSpectra();
		
		for (Spectra spectrum : allSpectra) {
			jsonAllSpectra.put(spectrum.toJSON());
			
		}
	}
	
}


/** Contains the parameter for a quick view of the spectrum.
 *  We need to add more parameters in order to quickly switch between 1d and 2d
 */
class NemoView {
	Double leftLimit, rightLimit, topLimit, bottomLimit;
	boolean is2D, vScale, hScale;
	Color[] primaryColor;
	Color[] secondaryColor;
	boolean[] isVertical;
	double[] multFactor;
	double[] nbCounterLines;
	
	NemoView(SpectraDisplay spectraDisplay) {
		this.leftLimit=spectraDisplay.getCurrentLeftLimit();
		this.rightLimit=spectraDisplay.getCurrentRightLimit();
		this.topLimit=spectraDisplay.getCurrentTopLimit();
		this.bottomLimit=spectraDisplay.getCurrentBottomLimit();
		this.is2D=spectraDisplay.is2D();
		this.vScale=spectraDisplay.hasVScale();
		this.hScale=spectraDisplay.hasHScale();
		this.primaryColor=new Color[spectraDisplay.getNbSpectra()];
		this.secondaryColor=new Color[spectraDisplay.getNbSpectra()];
		this.isVertical=new boolean[spectraDisplay.getNbSpectra()];
		this.multFactor=new double[spectraDisplay.getNbSpectra()];
		this.nbCounterLines=new double[spectraDisplay.getNbSpectra()];
		int i=0;
		for (Spectra spectra : spectraDisplay.getAllSpectra()) {
			this.primaryColor[i]=spectra.getPrimaryColor();
			this.secondaryColor[i]=spectra.getSecondaryColor();
			this.isVertical[i]=spectra.isVertical();
			this.multFactor[i]=spectra.getMultFactor();
			this.nbCounterLines[i]=spectra.getLowerContourline();
			i++;
		}
		// we should store the colors, we use this to hide some spectra
	}
	
	void applyTo(SpectraDisplay spectraDisplay) {
		spectraDisplay.setCurrentLimits(this.leftLimit, this.rightLimit, this.topLimit, this.bottomLimit);
		spectraDisplay.set2D(this.is2D);
		spectraDisplay.setVScale(this.vScale);
		spectraDisplay.setHScale(this.hScale);
		int i=0;
		for (Spectra spectra : spectraDisplay.getAllSpectra()) {
			if (i<this.primaryColor.length) {
				spectra.setPrimaryColor(this.primaryColor[i]);
				spectra.setSecondaryColor(this.secondaryColor[i]);
				spectra.isVertical(this.isVertical[i]);
				spectra.setMultFactor(this.multFactor[i]);
				spectra.setLowerContourline(this.nbCounterLines[i]);
			}
			i++;
		}
	}
	
}