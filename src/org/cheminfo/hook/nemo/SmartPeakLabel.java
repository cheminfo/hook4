package org.cheminfo.hook.nemo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.moldraw.ActAtomEntity;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.filters.SpectraFilter;
import org.cheminfo.hook.nemo.nmr.Nucleus;
import org.cheminfo.hook.nemo.signal.J;
import org.cheminfo.hook.nemo.signal.NMRSignal1D;
import org.cheminfo.hook.nemo.signal.Peak1D;
import org.cheminfo.hook.util.XMLCoDec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SmartPeakLabel extends BasicEntity implements SpectraObject, Comparable {

	private static Font defaultFont = new Font("courier", Font.PLAIN, 8);

	//private double jError = 1.1;
	//private final static double iError = 0.30; // relative error
	public final static int UNASSIGNED = -1;

	/*private double observe = 0;
	private double startX, stopX, center;
	private Vector peaks, intensities;
	private Multiplet[] basicMultiplets;

	private String publicationType = "";
	private String publicationAssignment = "";*/

	private boolean predicted = false;
	//private boolean forceMassif = false;
	
	private NMRSignal1D nmrSignal1D;
	/*private String units="";

	private int[] type = { 0, 0, 0, 0, 0, 0, 0, 0 };
	private double[] j;*/

	private int border = 1; // pixels for border

	private final static boolean DEBUG = true;
	private String comment = "";

	//private int temporaryAtomID; // used with simulated Spectra, holds the Id

	// an array that will be used to store temporary diastereotopicIDs
	//private TreeSet<String> tmpDiastereotopicIDs=new TreeSet<String>();
	
	// reference to the atom generating this
	// peak.
	public SmartPeakLabel(NMRSignal1D nmrSignal1D){
		this.nmrSignal1D=nmrSignal1D;
		this.setPrimaryColor(Color.magenta);
		this.setSecondaryColor(Color.BLACK);
		//this.temporaryAtomID = SmartPeakLabel.UNASSIGNED;
	}
	

	public SmartPeakLabel(double startX, double endX) {
		super();
		this.init();
		
		this.nmrSignal1D=new NMRSignal1D(startX, endX, 1, "");

		this.setPrimaryColor(Color.magenta);
		this.setSecondaryColor(Color.BLACK);
		//this.temporaryAtomID = SmartPeakLabel.UNASSIGNED;
	}
	
	public SmartPeakLabel(String XMLString, Hashtable helpers) {
		super();
		this.init();
		Spectra parentSpectra = (Spectra) helpers.get("currentSpectra");
		
		XMLCoDec tempCodec = new XMLCoDec(XMLString);
		tempCodec.shaveXMLTag();
		super.getSuperFromXML(tempCodec);

		this.setUniqueID(tempCodec.getParameterAsInt("uniqueID"));
		this.storeLinkIDs(tempCodec);
		
		//Parameters to create a new NMRSignal1D
		double observe = parentSpectra.getSpectraData().getParamDouble("observeFrequency", 0);
		String units=parentSpectra.getSpectraData().getXUnits();
		double startX = tempCodec.getParameterAsDouble("startXUnits");
		double stopX = tempCodec.getParameterAsDouble("stopXUnits");
		String publicationAssignment = tempCodec.getParameterAsNonNullString("publicationAssignment");
		String publicationType = tempCodec.getParameterAsNonNullString("publicationType");
		
		if(tempCodec.hasParameter("nmrSignal1D")){
			try {
				this.nmrSignal1D = new NMRSignal1D(new JSONObject(tempCodec.getParameterAsString("nmrSignal1D")));
			} catch (JSONException e) {
				System.out.println("Error parsing the nmrSignal1D json: "+tempCodec.getParameterAsString("nmrSignal1D"));
				e.printStackTrace();
			}
		}
		else{
			this.nmrSignal1D = new NMRSignal1D(startX, stopX, observe, units);
			this.nmrSignal1D.setPublicationAssignment(publicationAssignment);
			this.nmrSignal1D.setPublicationType(publicationType);
			this.nmrSignal1D.setNucleus(parentSpectra.getNucleus());
			
			Vector<Peak1D> peaks = new Vector();

			for (int peak = 0; peak < tempCodec.getParameterAsInt("nbPeaks"); peak++) {
				Peak1D tmpPeak = new Peak1D(tempCodec.getParameterAsDouble("peak" + peak),tempCodec.getParameterAsDouble("intensity" + peak));
				this.nmrSignal1D.addPeak(tmpPeak);
			}
			this.nmrSignal1D.setType(UserSmartPeakAction.parsePattern(tempCodec.getParameterAsString("pattern")));
			
			if(tempCodec.getParameterAsBoolean("forceMassif"))
				this.nmrSignal1D.forceAssymetric();
			//if(this.forceMassif)
			//	this.nmrSignal1D.setStype(NMRSignal1D.MASSIF);
			this.nmrSignal1D.setInterval(parentSpectra.getSpectraData().getInterval());
			
			//Parameters for the visual entity
			this.comment = tempCodec.getParameterAsNonNullString("comment");
			this.predicted = tempCodec.getParameterAsBoolean("predicted");
			//this.forceMassif = tempCodec.getParameterAsBoolean("forceMassif");


			String patternString = tempCodec.getParameterAsString("pattern");
			if (Integer.parseInt(patternString.substring(patternString.length() - 2)) == 0) {
				if (tempCodec.getParameterAsInt("nbPeaks") != 0) {
					int[] newType = { 0, 0, 0, 0, 0, 0, 0, 0 };
					this.nmrSignal1D.setType(newType);
					// this.compute((Spectra)helpers.get("currentSpectra"));
				} else {
					int[] newType = { 0, 0, 0, 0, 0, 0, 0, 0 };
					this.nmrSignal1D.setType(newType);
				}
			} else {
				boolean verifiedPattern = this.nmrSignal1D.verifyPatternEx(this.nmrSignal1D.getType(), true);
				if (DEBUG)System.out.println("pattern verified: " + verifiedPattern);
			}
		}

		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor"));
		this.setSecondaryColor(Color.BLACK);
	}

	public void addDiastereotopicID(String id) {
		this.nmrSignal1D.addDiaID(id);
	}
	
	public TreeSet<String> getDiastereotopicIDs() {
		//return this.tmpDiastereotopicIDs;
		return this.nmrSignal1D.getDiaIDs();
	}
	
	public void setDiastereotopicIDs(TreeSet<String> ids) {
		this.nmrSignal1D.setDiaIDs(ids);
	}
	
	public void clearDiastereotopicIDs() {
		this.nmrSignal1D.setDiaIDs(new TreeSet<String>());
	}
	
	private void init() {
		this.setHighlightPropagationType(BasicEntity.HL_PROPAGATION_OUTBOUND_AND_PARENT);
	}

	public void addNotify() {
		this.nmrSignal1D.setInterval(((Spectra)this.getParentEntity()).getSpectraData().getInterval());
	}

	public void copyPeaks(SmartPeakLabel label) {
		this.nmrSignal1D.getPeaks().clear();

		for (int peak = 0; peak < label.getNmrSignal1D().getNbPeaks(); peak++) {
			this.nmrSignal1D.addPeak(label.getNmrSignal1D().getPeak(peak));
		}
	}
	
	private boolean forceMassif(){
		return this.getNmrSignal1D().isAsymemtric();
	}

	public String getOverMessage() {
		DecimalFormat constantsFormat = new DecimalFormat();
		constantsFormat.applyPattern("#0.0");

		DecimalFormat shiftFormat = new DecimalFormat();
		shiftFormat.applyPattern("#0.00");

		String tempString = "";
		if (this.nmrSignal1D.getNbPeaks() != 0 && this.getNmrSignal1D().isAsymemtric() == false) {
			Vector<J> J = this.getNmrSignal1D().getJ();
			tempString = shiftFormat.format(this.nmrSignal1D.getShift()) +" "+this.nmrSignal1D.getUnits()+", type: " + this.nmrSignal1D.getPattern();

			for (int cc = 0; cc < J.size(); cc++) {

				if (J.get(cc).getCoupling()!= 0){
					if(this.nmrSignal1D.getUnits().toLowerCase().compareTo("hz")==0)
						tempString += ", J: " + constantsFormat.format(J.get(cc).getCoupling()/(this.nmrSignal1D.getObserve()))+ " Hz";
					else
						tempString += ", J: " + constantsFormat.format(J.get(cc).getCoupling())+ " Hz";						
				}
			}

		} else {
			tempString = shiftFormat.format(this.nmrSignal1D.getEndX()) + " - "
					+ shiftFormat.format(this.nmrSignal1D.getStartX()) + " ppm, type: "+this.nmrSignal1D.getPattern();
			// return tempString;
		}
		if ((comment!=null) && (!comment.equals(""))) {
			tempString += " - " + this.comment;
		}
		
		String publicationAssignment= this.nmrSignal1D.getPublicationAssignment();
		String publicationType = this.nmrSignal1D.getPublicationType();
		
		if ((publicationAssignment!=null) && (publicationType!=null)) {
			if ((!publicationAssignment.equals("")) || (!publicationType.equals(""))) {
				tempString += " (Publication information: ";
				if (!publicationType.equals("")) {
					tempString += publicationType;
				}
				if (!publicationAssignment.equals("")) {
					if (!publicationType.equals("")) tempString += ", ";
					tempString += publicationAssignment;
				}
				tempString += ")";
			}
		}

		return tempString;
	}

	public void setEditableState() {
		if (this.nmrSignal1D.getNbPeaks() > 0) {
			InteractiveSurface interactions = this.getInteractiveSurface();
			interactions.setCurrentAction(new ModifySmartPeakLabelActionButton(interactions, this));
		}
	}

	public String getClickedMessage() {
		return this.getEditDialog();
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String newComment) {
		this.comment = newComment;
	}

	public void setReferenceAtomID(int id) {
		this.nmrSignal1D.setReferenceAtomID(id);
	}

	public int getReferenceAtomID() {
		return this.nmrSignal1D.getReferenceAtomID();
	}

	public String getNmrHtml(int dNbDecimals, int jNbDecimals) {
		String tempString = "";

		DecimalFormat constantsFormat = new DecimalFormat();
		
		if (jNbDecimals>0) {
			constantsFormat.applyPattern("#0."+"000000000000000".substring(0,jNbDecimals));
		} else {
			constantsFormat.applyPattern("#0");
		}

		DecimalFormat shiftFormat = new DecimalFormat();
		if (dNbDecimals>0) {
			shiftFormat.applyPattern("#0."+"000000000000000".substring(0,dNbDecimals));
		} else {
			shiftFormat.applyPattern("#0");
		}

		if (this.nmrSignal1D.getNbPeaks() != 0 && !this.getNmrSignal1D().isAsymemtric()) {
			tempString += shiftFormat.format(this.nmrSignal1D.getShift());
		} else {
			tempString += shiftFormat.format(this.nmrSignal1D.getEndX()) + "-"
					+ shiftFormat.format(this.nmrSignal1D.getStartX());
		}

		// find the integral linked to this label
		Integral linkedIntegral = null;
		if (this.getInteractiveSurface()!=null) {
			Vector linkedEntities = this.getInteractiveSurface().getLinkedEntities(this);
			for (int ent = 0; ent < linkedEntities.size(); ent++) {
				if (linkedEntities.elementAt(ent) instanceof Integral) {
					linkedIntegral = (Integral) linkedEntities.elementAt(ent);
					break;
				}
			}
		}

		tempString += " (";

		if (this.getNmrSignal1D().getPublicationType() == null || this.getNmrSignal1D().getPublicationType().equals("")) {
			tempString += this.nmrSignal1D.getPattern();
			
			boolean numberJ=false;
			int counterJ=0;
			Vector<J> J = this.getNmrSignal1D().getJ();
			for (int cc = 0; cc < J.size(); cc++) {
				if (J.get(cc).getCoupling() != 0) {
					counterJ++;
				}
			}
			if (counterJ>1) numberJ=true;
			counterJ=0;
			
			for (int cc=0; cc<J.size(); cc++) {
				if (J.get(cc).getCoupling() != 0) {
					tempString += ", <i>J</i>";
					counterJ++;
					if (numberJ) tempString+="<sub>"+counterJ+"</sub>";
					tempString += " = "+constantsFormat.format(J.get(cc).getCoupling()) + " Hz";
				}
			}			
			
		} else {
			tempString += this.getNmrSignal1D().getPublicationType();
		}
		if (linkedIntegral != null) {
			String userValue = linkedIntegral.getPublicationValue();
			if (userValue != null && !userValue.equals("")) {
				tempString += ", " + userValue + " H";
			} else {
				tempString += ", "
						+ (int) Math.round(linkedIntegral.getRelArea()) + " H";
			}
		}
		if (this.getNmrSignal1D().getPublicationAssignment() != null
				&& !this.getNmrSignal1D().getPublicationAssignment().equals("")) {
			tempString += ", " + this.getNmrSignal1D().getPublicationAssignment();
		}
		tempString += ")";

		return tempString;
	}

	public void putCouplingJSON(JSONArray json) throws JSONException {
		// MULTIPLICITY & COUPLINGS
		//System.out.println("putCouplingJSON1");
		if (this.nmrSignal1D.getPattern().compareTo("s") == 0) {
			JSONObject coupling=new JSONObject();
			coupling.put("multiplicity", "s");
			json.put(coupling);
		} else if (this.nmrSignal1D.getPattern().compareTo("m") == 0 || this.getNmrSignal1D().isAsymemtric()) {
			JSONObject coupling=new JSONObject();
			coupling.put("multiplicity", "m");
			json.put(coupling);
		} else {
			
			Vector<J> J = this.nmrSignal1D.getJ();
			//System.out.println("dd "+J.size()+" "+this.nmrSignal1D.getType().length);
			//for (int i = 0; i < this.nmrSignal1D.getType().length; i++) {
			for (int i = 0; i < J.size(); i++) {
				if (J.get(i).getCoupling()>0) {
					JSONObject coupling=new JSONObject();
					coupling.put("multiplicity", type2string(this.nmrSignal1D.getType()[i]));
					coupling.put("value", J.get(i).getCoupling());
					json.put(coupling);
				}
			}
		}
		//System.out.println("putCouplingJSON2");
	}
	
	public String getNmrTable() {
		DecimalFormat newFormat = new DecimalFormat();
		newFormat.applyPattern("#0.00");
		String outString = "";

		// MULTIPLICITY & COUPLINGS
		if (this.nmrSignal1D.getPattern().compareTo("s") == 0)
			outString += "s\t";

		else if (this.nmrSignal1D.getPattern().compareTo("m") == 0 || this.getNmrSignal1D().isAsymemtric())
			outString += "m\t";

		else {
			Vector<J> J = this.nmrSignal1D.getJ();
			for (int i = 0; i < this.nmrSignal1D.getType().length; i++) {
				outString += type2string(this.nmrSignal1D.getType()[i])+"\t";
				if (J.get(i).getCoupling() != 0) {
					outString += newFormat.format(J.get(i).getCoupling()) + "\t";
				}
			}
		}

		outString += "\r\n";
		return outString;
	}

	private String type2string(int type) {
		switch (type) {
		case 2:
			return "d";
		case 3:
			return "t";
		case 4:
			return "q";
		case 5:
			return "quint";
		case 6:
			return "hex";
		case 7:
			return "hept";
		case 8:
			return "octa";
		case 9:
			return "nona";
		case 10:
			return "deca";
		}
		return "";
	}
	
	public String getPeakPattern() {
		return this.nmrSignal1D.getPattern();
	}

	public String getPeakList() {
		DecimalFormat newFormat = new DecimalFormat();
		newFormat.applyPattern("#0.00");

		String outString = "";
		for (int peak = 0; peak < this.nmrSignal1D.getNbPeaks(); peak++) {
			outString += peak+ "\t"	+ newFormat.format(this.nmrSignal1D.getPeak(peak).getX()
							* ((Spectra) this.getParentEntity())
									.getSpectraData().getParamDouble("observeFrequency", 0)) + " Hz\n";
		}
		return outString;
	}

	

	public void shiftObject(double shift) {
		this.nmrSignal1D.shiftObject(shift);
	}

	/**
	 * Tries to add superposed peaks to match the number required by the current
	 * multiplet definition. This method is normally used by UserSmartPaekAction
	 * when the user suggests a multiplet definition resulting from coalesced
	 * peaks which the algorithm cannot find.
	 */
	protected void autoAddMissingPeaks() {
		int requiredNbPeaks = 1;
		for (int i = 0; i < this.nmrSignal1D.getType().length; i++)
			requiredNbPeaks *= this.nmrSignal1D.getType()[i];

		int missingPeaks = requiredNbPeaks - this.nmrSignal1D.getNbPeaks();

		if (this.nmrSignal1D.getNbPeaks() % 2 == 0) // Even number of peaks
		{
			if (missingPeaks % 2 != 0)
				return; // adding an odd number of peaks is not reasonable. the
			// multiplet would be asymmetrical

		} else {

		}

	}

	protected void resetConstants() {
		if (DEBUG) System.out.println("Reseting Coupling Constants");
		this.nmrSignal1D.resetConstants();
	}

	protected int getBorder() {
		return this.border;
	}

	public void checkSizeAndPosition() {
		Spectra parentSpectra = (Spectra) this.getParentEntity();
		double tempWidth = Math.abs(parentSpectra.unitsToPixelsH(this.nmrSignal1D.getStartX())
				- parentSpectra.unitsToPixelsH(this.nmrSignal1D.getEndX()))
				+ 2 * this.getBorder();
		double tempHeight = 7;
		double tempX = parentSpectra.unitsToPixelsH(this.nmrSignal1D.getStartX())
				- this.getBorder();
		double tempY = 2 * parentSpectra.getHeight() / 3;

		this.setSize(tempWidth, tempHeight);
		this.setLocation(tempX, tempY);
		this.refreshSensitiveArea();

		Area thisTransformedArea = this.getSensitiveArea().createTransformedArea(this.getGlobalTransform());
		Area testTransformedArea = new Area();

		for (int ent = 0; ent < parentSpectra.getEntitiesCount(); ent++) {
			if (parentSpectra.getEntity(ent) instanceof SmartPeakLabel
					&& parentSpectra.getEntity(ent) != this) {
				testTransformedArea = parentSpectra.getEntity(ent)
						.getSensitiveArea().createTransformedArea(
								parentSpectra.getEntity(ent)
										.getGlobalTransform());
				if (testTransformedArea.intersects(thisTransformedArea
						.getBounds2D())) {
					tempY -= 11;
					this.setLocation(tempX, tempY);

					thisTransformedArea = this.getSensitiveArea()
							.createTransformedArea(this.getGlobalTransform());
				}
			}
		}
	}

	public void paint(Graphics2D g) {
		try {
		Spectra parentSpectra = (Spectra) this.getParentEntity();
		InteractiveSurface interactiveSurface = this.getInteractiveSurface();
		boolean isAssigned = false;
		int displayIDS = 0;
		TreeSet<Integer> atomIDS = new TreeSet<Integer>();
		if (interactiveSurface != null && !this.predicted) {
			Vector linkedEntities = interactiveSurface.getLinkedOutboundEntities(this);
			for (int index = 0; index < linkedEntities.size(); index++) {
				if (linkedEntities.get(index) instanceof ActAtomEntity) {
					ActAtomEntity atomEntity = (ActAtomEntity) linkedEntities.get(index);
					displayIDS = ((ActMoleculeDisplay) atomEntity
							.getParentEntity()).getDisplayIDLevel();
					isAssigned = true;
					if (displayIDS==0)
						break;
					if (parentSpectra.getNucleus() == Nucleus.NUC_1H) {
						if (atomEntity.getHydrogenIDS() != null)
							atomIDS.addAll(atomEntity.getHydrogenIDS());
					} else {
						atomIDS.add(atomEntity.getAtomID());
					}

				}
			}
		}
		if (parentSpectra.isSimulated()
				&& parentSpectra.hasVisiblePredictionLabels() == 0)
			return;
		if (parentSpectra.hasVisiblePeakLabels() == 1) {
			double pixelsPerUnit = (this.getWidth() - 2 * border)
					/ (this.nmrSignal1D.getEndX() - this.nmrSignal1D.getStartX()); // this is a
			// negative number
			// in the usual case

			if (this.nmrSignal1D.getNbPeaks() != 0 && this.getNmrSignal1D().isAsymemtric() == false) {
				if (this.isMouseover() || this.isSelected() || this.isHighlighted()) {
					g.setStroke(this.getInteractiveSurface().getBroadStroke());
					g.setColor(Color.GRAY);
					for (int peak = 0; peak < this.nmrSignal1D.getNbPeaks(); peak++) {
						int x = (int) ((this.nmrSignal1D.getPeak(peak).getX() - this.nmrSignal1D.getStartX()) * pixelsPerUnit) + border;
						g.drawLine(x, 0, x, 7);
					}
				}
				
				g.setStroke(this.getInteractiveSurface().getNarrowStroke());
				g.setColor(this.getPrimaryColor());

				for (int peak = 0; peak < this.nmrSignal1D.getNbPeaks(); peak++) {
					int x = (int) ((this.nmrSignal1D.getPeak(peak).getX() - this.nmrSignal1D.getStartX()) * pixelsPerUnit) + border;
					g.drawLine(x, 0, x, 7);
				}
				if (isAssigned) {
					int xa = (int) Math.round((this.nmrSignal1D.getPeak(0).getX() - this.nmrSignal1D.getStartX())
							* pixelsPerUnit)
							+ border;
					int xb = (int) Math.round((this.nmrSignal1D.getPeak(this.nmrSignal1D.getNbPeaks() - 1).getX() - this.nmrSignal1D.getStartX())
							* pixelsPerUnit)
							+ border;
					int border = 3;
					if (xa < xb) {
						xa -= border;
						xb += border;
					} else {
						xa += border;
						xb -= border;
					}
					g.setColor(this.getSecondaryColor());
					g.drawLine(xa, 3, xb, 3);
					if (displayIDS>0 && atomIDS.size() > 0) {
						g.setFont(SmartPeakLabel.defaultFont);
						int iOffset = 0;
						for (int atomID : atomIDS) {
							g.drawString(atomID + "", 0,
									(int) (-(++iOffset) * (defaultFont
											.getSize() + 1)));
						}
					}
					/*if (displayIDS && atomIDS.size() > 0) {
						g.setFont(SmartPeakLabel.defaultFont);
						int iOffset = 0;
						for (int atomID : atomIDS) {
							g.drawString(atomID + "", 0,
									(int) (-(++iOffset) * (defaultFont
											.getSize() + 1)));
						}
					}*/
					g.setColor(this.getPrimaryColor());
				}
			} else // massive
			{
				if (this.isMouseover() || this.isSelected()
						|| this.isHighlighted()) {

					g.setStroke(this.getInteractiveSurface().getBroadStroke());
					g.setColor(Color.GRAY);
					g.drawLine(0, 0, 0, 7);
					g.draw(new Line2D.Double(this.getWidth() - 1, 0, this
							.getWidth() - 1, 7));
					g.draw(new Line2D.Double(0, 3.5, this.getWidth() - 1, 3.5));
				}

				g.setStroke(this.getInteractiveSurface().getNarrowStroke());
				g.setColor(this.getPrimaryColor());
				g.drawLine(0, 0, 0, 7);
				g.draw(new Line2D.Double(this.getWidth() - 1, 0, this
						.getWidth() - 1, 7));
				g.draw(new Line2D.Double(0, 3.5, this.getWidth() - 1, 3.5));
				if (isAssigned) {
					g.setColor(this.getSecondaryColor());
					g.draw(new Line2D.Double(0, 3.5, this.getWidth() - 1, 3.5));
					g.setColor(this.getPrimaryColor());
				}
			}
		}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public String getXmlTag(Hashtable xmlProperties) {
		//System.out.println("Creating SPL");
		String tempTag = "";
		XMLCoDec tempCodec = new XMLCoDec();
		super.addSuperToXML(tempCodec);
		String patternString = "";

		for (int i = 0; i < this.nmrSignal1D.getType().length; i++) {
			if (this.nmrSignal1D.getType()[i] < 10)
				patternString += "0" + this.nmrSignal1D.getType()[i];
			else
				patternString += this.nmrSignal1D.getType()[i];
		}
		
		tempCodec.addParameter("nmrSignal1D", this.nmrSignal1D.toString());

		tempCodec.addParameter("pattern", patternString);
		tempCodec.addParameter("primaryColor", this.getPrimaryColor().getRed()
				+ "," + this.getPrimaryColor().getGreen() + ","
				+ this.getPrimaryColor().getBlue());
		tempCodec.addParameter("startXUnits", new Double(this.nmrSignal1D.getStartX()));
		tempCodec.addParameter("stopXUnits", new Double(this.nmrSignal1D.getEndX()));
		tempCodec.addParameter("nbPeaks", new Integer(this.nmrSignal1D.getNbPeaks()));
		tempCodec.addParameter("comment", this.comment);
		tempCodec.addParameter("predicted", this.predicted);
		tempCodec.addParameter("publicationType", this.nmrSignal1D.getPublicationType());
		tempCodec.addParameter("publicationAssignment",
				this.nmrSignal1D.getPublicationAssignment());
		tempCodec.addParameter("forceMassif", new Boolean(this.getNmrSignal1D().isAsymemtric()));
		for (int peak = 0; peak < this.nmrSignal1D.getNbPeaks(); peak++) {
			tempCodec.addParameter("peak" + peak,
					new Double(this.nmrSignal1D.getPeak(peak).getX()));
			tempCodec.addParameter("intensity" + peak, new Double(this.nmrSignal1D.getPeak(peak).getIntensity()));
		}

		// tempCodec.addParameter("secondaryColor",
		// this.getSecondaryColor().getRed()+","+this.getPrimaryColor().getGreen()+","+this.getPrimaryColor().getBlue());

		this.addLinkXMLElements(tempCodec);

		tempTag += "<nemo.SmartPeakLabel " + tempCodec.encodeParameters()
				+ ">\r\n";
		tempTag += "</nemo.SmartPeakLabel>\r\n";

		return tempTag;
	}

	/** For massif we take startX, for multiplet we take center */
	public int compareTo(Object otherSmartPeakLabel) {
		return this.nmrSignal1D.compareTo(((SmartPeakLabel)otherSmartPeakLabel).getNmrSignal1D());
	}

	public boolean isPredicted() {
		return predicted;
	}

	public void setPredicted(boolean predicted) {
		this.predicted = predicted;
	}

	public String getEditDialog() {
		String tempString = "";
		if (this.getNmrSignal1D().isAsymemtric()) {
			tempString += "<Text type=\"plain\">Not massif </Text>";
			XMLCoDec buttonCodec2 = new XMLCoDec();
			buttonCodec2.addParameter("action",
					"org.cheminfo.hook.nemo.UserSmartPeakActionMassif");
			buttonCodec2.addParameter("image", "validate.gif");
			tempString += "<Button " + buttonCodec2.encodeParameters()
					+ "></Button>";

		} else {
			DecimalFormat newFormat = new DecimalFormat();
			newFormat.applyPattern("#0.0");

			XMLCoDec typeCodec = new XMLCoDec();
			typeCodec.addParameter("name", "type");
			typeCodec.addParameter("size", new Integer(4));

			// XMLCoDec choiceCodec = new XMLCoDec();
			// choiceCodec.addParameter("name", "comment");
			// choiceCodec.addParameter("nbChoices", new Integer(7));
			// choiceCodec.addParameter("choice0", "");
			// choiceCodec.addParameter("choice1", "ax");
			// choiceCodec.addParameter("choice2", "eq");
			// choiceCodec.addParameter("choice3", "pro-R");
			// choiceCodec.addParameter("choice4", "pro-S");
			// choiceCodec.addParameter("choice5", "pro-E");
			// choiceCodec.addParameter("choice6", "pro-Z");
			// choiceCodec.addParameter("active", this.getComment());

			tempString += "<Text type=\"plain\">Type: </Text><Input "
					+ typeCodec.encodeParameters() + ">" + this.nmrSignal1D.getPattern()+ "</Input>";

			// +
			// "<Choice " + choiceCodec.encodeParameters()
			// + "></Choice>"

			XMLCoDec buttonCodec1 = new XMLCoDec();
			buttonCodec1.addParameter("action",
					"org.cheminfo.hook.nemo.UserSmartPeakAction");
			buttonCodec1.addParameter("image", "validate.gif");

			// assignement stuff
			tempString += "<Text type=\"plain\">Publication Data, </Text>";
			tempString += "<Text type=\"plain\"> Type:</Text>";
			XMLCoDec publTypeCodec = new XMLCoDec();
			publTypeCodec.addParameter("name", "publicationType");
			publTypeCodec.addParameter("size", new Integer(4));
			tempString += "<Input type=\"plain\" "
					+ publTypeCodec.encodeParameters() + ">"
					+ this.nmrSignal1D.getPublicationType() + "</Input>";
			tempString += "<Text type=\"plain\"> Assignment:</Text>";
			XMLCoDec publAssignmentCodec = new XMLCoDec();
			publAssignmentCodec.addParameter("name", "publicationAssignment");
			publAssignmentCodec.addParameter("size", new Integer(4));
			tempString += "<Input type=\"plain\" "
					+ publAssignmentCodec.encodeParameters() + ">"
					+ this.nmrSignal1D.getPublicationAssignment() + "</Input>";

			tempString += "<Button " + buttonCodec1.encodeParameters()+ "></Button>";
			if ( this.nmrSignal1D.getNbPeaks() > 0) {
				tempString += "<Text type=\"plain\">Massif </Text>";
				XMLCoDec buttonCodec2 = new XMLCoDec();
				buttonCodec2.addParameter("action",
						"org.cheminfo.hook.nemo.UserSmartPeakActionMassif");
				buttonCodec2.addParameter("image", "validate.gif");
				tempString += "<Button " + buttonCodec2.encodeParameters()
						+ "></Button>";
			}
		}
		return tempString;
	}
	/***
	 *TODO May be should be implemented inside of NMRSignal1D
	 */
	public void addSubPeak() {
		if (this.nmrSignal1D.getNbPeaks() == 0)
			return;
		Spectra parent = (Spectra) this.getParentEntity();
		InteractiveSurface interactions = this.getInteractiveSurface();
		if (interactions == null || parent == null)
			return;
		double xNew = parent.pixelsToUnits(interactions.getReleasePoint().x);
		int iX = parent.unitsToArrayPoint(xNew);
		double minDistance = Double.MAX_VALUE;
		for (int i = 0; i < this.nmrSignal1D.getNbPeaks(); i++) {
			double d = Math.abs(this.nmrSignal1D.getPeak(i).getX() - xNew);
			if (d < minDistance)
				minDistance = d;
		}
		if (minDistance < Math.abs(parent.getSpectraData().getInterval())) {
			if (DEBUG) System.out.println("not Adding");
			return;
		}
		this.nmrSignal1D.addPeak(new Peak1D(xNew, parent.spectraData.getY(iX)));
		this.nmrSignal1D.sortPeaks();
		this.nmrSignal1D.clearType();
		this.nmrSignal1D.compute();
	}
	
	public void removeSubPeak() {
		if (this.nmrSignal1D.getNbPeaks() < 2)
			return;
		Spectra parent = (Spectra) this.getParentEntity();
		InteractiveSurface interactions = this.getInteractiveSurface();
		if (interactions == null || parent == null)
			return;
		parent = (Spectra) this.getParentEntity();
		interactions = this.getInteractiveSurface();
		if (interactions == null || parent == null)
			return;
		// allow one pixel error in selection
		double clickedX = interactions.getReleasePoint().x;
		final double deltaMax = 2.0;
		int targetID = -1;
		for (int iPeak = 0; iPeak < this.nmrSignal1D.getNbPeaks(); iPeak++) {
			double currentUnits = this.nmrSignal1D.getPeak(iPeak).getX();
			double currentPixels = parent.unitsToPixelsH(currentUnits);
			double delta = Math.sqrt(Math.pow(clickedX - currentPixels, 2));
			if (delta <= deltaMax) {
				targetID = iPeak;
				break;
			}
		}
		if (targetID != -1) {
			this.nmrSignal1D.getPeaks().remove(targetID);
			this.nmrSignal1D.refreshLimits();
			this.nmrSignal1D.sortPeaks();
			this.nmrSignal1D.clearType();
			this.refreshSensitiveArea();
			this.checkSizeAndPosition();
			this.nmrSignal1D.compute();
		}
	}

	public boolean isForceMassif() {
		return this.getNmrSignal1D().isAsymemtric();//forceMassif;
	}

	public void setForceMassif(boolean forceMassif) {
		if(forceMassif)
			this.getNmrSignal1D().forceAssymetric();
		if(this.forceMassif()&&this.nmrSignal1D!=null)
			this.nmrSignal1D.setStype(NMRSignal1D.MASSIF);		
	}

	public void switchForceMassifState() {
		//System.out.println("Switching "+this.getNmrSignal1D().isAsymemtric());
		if(this.getNmrSignal1D().isAsymemtric()){
			this.getNmrSignal1D().setStype(NMRSignal1D.OTHER);
		}
		else{
			this.getNmrSignal1D().forceAssymetric();
		}
		
		/*if(this.forceMassif&&this.nmrSignal1D!=null)
			this.nmrSignal1D.setStype(NMRSignal1D.MASSIF);
		else
			this.nmrSignal1D.setStype(NMRSignal1D.OTHER);*/
	}
	
	/**
	 * @return the nmrSignal1D
	 */
	public NMRSignal1D getNmrSignal1D() {
		return nmrSignal1D;
	}

	/**
	 * @param nmrSignal1D the nmrSignal1D to set
	 */
	public void setNmrSignal1D(NMRSignal1D nmrSignal1D) {
		this.nmrSignal1D = nmrSignal1D;
	}
	
	public String toString() {
		String toReturn=this.getNmrSignal1D().toString()+" - "+this.getNmrHtml(2,2);
		return toReturn;
	}
	
	public JSONObject toJSON(){
		return this.nmrSignal1D.toJSON();
	}
	
}