package org.cheminfo.hook.moldraw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import com.actelion.research.chem.contrib.DiastereotopicAtomID;
import com.actelion.research.chem.contrib.HydrogenHandler;
import org.cheminfo.hook.framework.BasicDisplay;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.EntityResizer;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.Spectra;
import org.cheminfo.hook.nemo.SpectraDisplay;
import org.cheminfo.hook.nemo.nmr.NmrSimulator;
import org.cheminfo.hook.util.XMLCoDec;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.coords.CoordinateInventor;
import com.actelion.research.chem.Depictor;
import com.actelion.research.chem.Depictor2D;
import com.actelion.research.chem.DepictorTransformation;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.epfl.EPFLUtils;
import com.actelion.research.epfl.StereoInfo;

public class ActMoleculeDisplay extends BasicDisplay {
	
	public ActMoleculeDisplay(){
	}
	//private boolean displayID = false; Replace for displayIDLevel
	private int displayIDLevel=0;
	
	// this is the molecule that will be displayed!
	private StereoMolecule mDisplayMolecule = null;
	// the molecule in canonical form
	private StereoMolecule mCompactMolecule = null;
	// this is the molecule with all hydrogens expanded
	private StereoMolecule mEnhancedMolecule = null;
	private Depictor2D mDepictor;
	private DepictorTransformation dTrans;
	private Canonizer mCanonizer;

	private int[] mapAtomID2EffectiveID = null;
	private int[] mapEffectiveID2AtomID = null;
	private int[] enhancedSymmetryRanks = null;
	private String[] diastereotopicIds = null;
	private StereoInfo[] stereoInfo = null;
	private boolean[] isExpanded = null;

	public static final boolean DEBUG=false;
	
	private Graphics graphics; // this is necessary to know the character size
	
	// during size and position checki ng

	public void init() {
		super.init();
		this.setLinkable(false);
		this.setPrimaryColor(null);
		//this.addEntity(new EntityResizer(EntityResizer.SE_RESIZER));
		this.graphics = null;
		// for (int i=0; i < this.getEntitiesCount(); i++)
		// ((BasicEntity)this.getEntity(i)).updatePosition(this);
	}

	public void init(double width, double height) {
		super.init(width, height);
		this.setLinkable(false);
		this.setPrimaryColor(null);
		//this.addEntity(new EntityResizer(EntityResizer.SE_RESIZER));
		this.graphics = null;
	}

	@SuppressWarnings("unchecked")
	public void init(String XMLTag, Hashtable helpers) {
		this.init(XMLTag, this.getParentEntity().getWidth(), this
				.getParentEntity().getHeight(), helpers);
		this.setLinkable(false);
	}

	@SuppressWarnings("unchecked")
	public void init(String XMLTag, double parentWidth, double parentHeight,
			Hashtable helpers) {
		
		this.setLinkable(false);
		XMLCoDec tempCodec = new XMLCoDec(XMLTag);
		tempCodec.shaveXMLTag();
		
		if (this.getEntitiesCount() != 0)
			this.removeAll();
		//Check for the old version of tag
		if(tempCodec.getParameterAsBoolean("displayID"))
			this.displayIDLevel=2;
		else
			this.displayIDLevel = tempCodec.getParameterAsInt("displayIDLevel");
	
		double width = parentWidth * tempCodec.getParameterAsDouble("relWidth");
		double height = parentHeight * tempCodec.getParameterAsDouble("relHeight");

		this.setSize(width, height);

		this.setLocation(parentWidth * tempCodec.getParameterAsDouble("relX"),
				parentHeight * tempCodec.getParameterAsDouble("relY"));
		
		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor"));
		// reload molecule
		IDCodeParser idCodeParser = new IDCodeParser();
		
		
		//this.addMoleculeByIDCode(tempCodec
		//		.getParameterAsString("IDCode"));
		//StereoMolecule molecule = this.getCompactMolecule();
		StereoMolecule molecule =null;
		try{
			 molecule = idCodeParser.getCompactMolecule(tempCodec
			.getParameterAsString("IDCode"), tempCodec
				.getParameterAsString("EncodedCoordinates"));
		}catch(Exception e){
			System.err.println("Error parsing the actelion code: "+tempCodec.getParameterAsString("IDCode"));
			e.printStackTrace();
		}
		
		// get expanded state
		String strExpAtoms = tempCodec.getParameterAsString("expandedAtoms");
		if (strExpAtoms != null && strExpAtoms.trim().length() != 0) {
			String[] ids = strExpAtoms.trim().split(",");
			this.isExpanded = new boolean[molecule.getAllAtoms()];
			Arrays.fill(this.isExpanded, false);
			for (int i = 0; i < ids.length; i++)
				this.isExpanded[Integer.parseInt(ids[i].trim())] = true;
		}
		
		this.prepareInternalMolecules(molecule, false);
		
		// Add ActAtomEntities
		int elements = tempCodec.getRootElementsCount();
		java.awt.geom.Point2D.Double tempPoint;
		
		for (int elem = 0; elem < elements; elem++) {
			XMLCoDec tempCodec2 = new XMLCoDec(tempCodec.readXMLTag());
			tempCodec2.shaveXMLTag();
			if (tempCodec2.getParameterAsString("tagName").trim().compareTo(
					"moldraw.ActAtomEntity") == 0) {
				ActAtomEntity newAtomEntity = new ActAtomEntity(tempCodec
						.popXMLTag(), this.mDisplayMolecule);
				tempPoint = new java.awt.geom.Point2D.Double(mDisplayMolecule
						.getAtomX(newAtomEntity.getEffectiveID()),
						mDisplayMolecule.getAtomY(newAtomEntity
								.getEffectiveID()));
				if (this.mEnhancedMolecule.getAtomicNo(newAtomEntity
						.getAtomID()) == 1)
					newAtomEntity.cacheExtendedDisplayInfo(
							this.mEnhancedMolecule ,this.mDisplayMolecule, false);
				else
					newAtomEntity.cacheExtendedDisplayInfo(
							this.mEnhancedMolecule,this.mDisplayMolecule,
							this.isExpanded[newAtomEntity.getAtomID()]);
				this.addEntity(newAtomEntity);
				if (DEBUG)
					System.out.println("effectiveID"
							+ newAtomEntity.getEffectiveID() + "nAtoms="
							+ mDisplayMolecule.getAllAtoms());
				dTrans.applyTo(tempPoint);
				newAtomEntity.setLocation(tempPoint.x, tempPoint.y);
				newAtomEntity.refreshSensitiveArea();
			}
		}
		
		EntityResizer eRes = new EntityResizer(EntityResizer.SE_RESIZER);
		eRes.setOverMessage("Resize molecule");
		this.addEntity(eRes);
		eRes.setUniqueID(-1);

		// for (int i=0; i < this.getComponentCount(); i++)
		// ((BasicEntity)this.getComponent(i)).updatePosition(this);
		//Atom ID
		this.setMovementType(BasicEntity.GLOBAL);
		this.setErasable(true);

		eRes.checkSizeAndPosition();
		this.getInteractiveSurface().repaint();
	}

	public void addMolfile(String molfile, boolean expandAll) {
		if(DEBUG) System.out.println("AMD "+molfile);
		if(DEBUG) System.out.println("Expand?: "+expandAll);
		StereoMolecule molecule = new StereoMolecule();
		com.actelion.research.chem.MolfileParser mfParser = new com.actelion.research.chem.MolfileParser();
		mfParser.parse(molecule, molfile);
		
		this.isExpanded = null;
		this.prepareInternalMolecules(molecule, expandAll);
		
		
		this.createAtomLabels();
		
		if(DEBUG){
			System.out.println("Check if expaneded at addMolfile");
			for(int i=this.isExpanded.length-1;i>=0;i--){
				System.out.println(this.mEnhancedMolecule.getAtomicNo(i)+" "+this.isExpanded[i]+" "+this.mEnhancedMolecule.getAtomLabel(i));
			}
		}
		//System.out.println("NO more resizer");
		//TODO Trick for avoiding the resizer get hidden by the molecule
		this.addNewResizer();
	}

	private void createAtomLabels() {
		this.removeAll();
		java.awt.geom.Point2D.Double tempPoint;
		ActAtomEntity tempLabel;
		for (int atm = 0; atm < mDisplayMolecule.getAllAtoms(); atm++)
		{
			tempPoint = new java.awt.geom.Point2D.Double(mDisplayMolecule.getAtomX(atm), mDisplayMolecule.getAtomY(atm));
			dTrans.applyTo(tempPoint);

			tempLabel = new ActAtomEntity(mDisplayMolecule,	this.mapEffectiveID2AtomID[atm], atm);
			tempLabel.setStereoInfo(this.stereoInfo[this.mapEffectiveID2AtomID[atm]]);
			tempLabel.setDiastereotopicID(this.diastereotopicIds[this.mapEffectiveID2AtomID[atm]]);
			tempLabel.setLocation(tempPoint.x, tempPoint.y);
			this.addEntity(tempLabel);

			if (this.mDisplayMolecule.getAtomicNo(atm) == 1) {
				tempLabel.setSymmetryRank(this.enhancedSymmetryRanks[this.mapEffectiveID2AtomID[atm]]);
				tempLabel.cacheExtendedDisplayInfo(this.mEnhancedMolecule,this.mDisplayMolecule, false);
			} else {
				tempLabel.setSymmetryRank(this.enhancedSymmetryRanks[atm]);
				tempLabel.cacheExtendedDisplayInfo(this.mEnhancedMolecule,this.mDisplayMolecule, this.isExpanded[atm]);
			}
			tempLabel.checkSizeAndPosition();
			tempLabel.refreshSensitiveArea();
		}
		this.linkSymmetricAtoms();
		if (this.getInteractiveSurface() != null)
			this.getInteractiveSurface().repaint();
	}

	public void addMoleculeByIDCode(String idCode) {
		this.isExpanded = null;
		IDCodeParser codeParser = new IDCodeParser();
		StereoMolecule tempMolecule = codeParser.getCompactMolecule(idCode);

		CoordinateInventor inventor = new CoordinateInventor();
		inventor.invent(tempMolecule);

		this.mCanonizer = new Canonizer(tempMolecule, Canonizer.CREATE_SYMMETRY_RANK);

		String stIDCode = this.mCanonizer.getIDCode();
		String stEncodedCoordinates = this.mCanonizer.getEncodedCoordinates(true);

		IDCodeParser idCodeParser = new IDCodeParser();

		this.prepareInternalMolecules(idCodeParser.getCompactMolecule(stIDCode,	stEncodedCoordinates), false);
		this.createAtomLabels();
		
		//TODO Trick for avoiding the resizer get hidden by the molecule
		this.addNewResizer();
	}

	public void addMolecule(StereoMolecule mol) {
		this.isExpanded = null;

		this.prepareInternalMolecules(mol, false);
		this.createAtomLabels();

		//TODO Trick for avoiding the resizer get hidden by the molecule
		this.addNewResizer();
	}


	/**
	 * It has been added for being called each time we add a new molecule to the display, because the previous resizer
	 * is getting hidden behind the new molecule.
	 * Andres Castillo
	 */
	private void addNewResizer(){
		EntityResizer eRes = new EntityResizer(EntityResizer.SE_RESIZER);
		eRes.setOverMessage("Resize molecule");
		this.addEntity(eRes);
		eRes.setUniqueID(-1);
	}
	
	private void addStereoMolecule(StereoMolecule molecule) {
		this.mDisplayMolecule = molecule;

		mCanonizer = new Canonizer(mDisplayMolecule,
				Canonizer.CREATE_SYMMETRY_RANK);

		mDepictor = new Depictor2D(mDisplayMolecule);
		Rectangle2D.Double viewRect = new Rectangle2D.Double();
		viewRect.setRect(0, 0, this.getWidth(), this.getHeight());
		mDepictor.simpleUpdateCoords(viewRect, Depictor.cModeInflateToMaxAVBL);
		dTrans = mDepictor.getTransformation();
		this.checkSizeAndPosition();
		this.checkInteractiveSurface();
		
		//TODO Trick for avoiding the resizer get hidden by the molecule
		this.addNewResizer();
	}

	
	
	public String getMoleculeIDCode() {
		return this.mCanonizer.getIDCode();
	}

	public String getEncodedCoordinates() {
		return this.mCanonizer.getEncodedCoordinates(true);
	}

	public void checkSizeAndPosition() {
		this.setLocation(this.getLocation());
		super.checkSizeAndPosition();
		
		Rectangle2D.Double viewRect = new Rectangle2D.Double();
		viewRect.setRect(0, 0, this.getWidth(), this.getHeight());

		if (mDepictor != null && this.graphics != null) {
			mDepictor.updateCoords(this.graphics, viewRect,	Depictor.cModeInflateToMaxAVBL);

			dTrans = mDepictor.getTransformation();

			java.awt.geom.Point2D.Double tempPoint;
			ActAtomEntity tempLabel;

			for (int ent = 0; ent < this.getEntitiesCount(); ent++) {
				if (this.getEntity(ent) instanceof ActAtomEntity) {
					tempLabel = (ActAtomEntity) this.getEntity(ent);

					tempPoint = new java.awt.geom.Point2D.Double(
							mDisplayMolecule.getAtomX(tempLabel.getEffectiveID()), mDisplayMolecule.getAtomY(tempLabel.getEffectiveID()));
					// dTrans.applyTo(tempPoint);

					tempLabel.setLocation(tempPoint.x, tempPoint.y);
				}
			}
		}
	}

	public void setSize(double width, double height) {
		super.setSize(width, height);
		this.checkSizeAndPosition();
		this.refreshSensitiveArea();
	}

	public void paint(Graphics2D g) {
		if (this.graphics == null) {
			graphics = g;
			this.checkSizeAndPosition();
		}

		super.paint(g);

		g.setStroke(new BasicStroke((float) 0.3));

		Rectangle2D.Double viewRect = new Rectangle2D.Double();
		viewRect.setRect(0, 0, this.getWidth(), this.getHeight());
		if (mDepictor != null) {
			mDepictor.validateView(g, viewRect, Depictor.cModeInflateToMaxAVBL);
			mDepictor.updateCoords(g, viewRect, 0);
		}

		if (this.getPrimaryColor() != null) {
			g.setColor(this.getPrimaryColor());
			g.fill(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));
		}

		/*
		 * if (this.isMouseover()) { g.setColor(Color.red); g.draw(new
		 * Rectangle2D.Double(0,0,this.getWidth(), this.getHeight())); }
		 */
		if (mDisplayMolecule != null) {
			super.paint(g);

			g.setTransform(this.getGlobalTransform());
			g.setStroke(new BasicStroke(1));
			if (this.mDepictor != null)
				mDepictor.paint(g);
		}

		if ((this.isSelected()) || (this.isMouseover())) {
			g.setColor(Color.black);
			g.draw(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));
		}

	}

	/**
	 * creates a link between all atomLabelEntities that have the same symmetry
	 * rank The molecule must be already canonized.
	 */
	protected void linkSymmetricAtoms() {
		InteractiveSurface interactions = this.getInteractiveSurface();
		if (interactions == null)
			return;
		Vector<ActAtomEntity> atomEntities = new Vector<ActAtomEntity>();
		for (int ent = 0; ent < this.getEntitiesCount(); ent++)
			if (this.getEntity(ent) instanceof ActAtomEntity)
				atomEntities.add((ActAtomEntity) this.getEntity(ent));

		for (int i = 0; i < atomEntities.size(); i++) {
			for (int j = i + 1; j < atomEntities.size(); j++) {
				if (atomEntities.get(i).getDiastereotopicID().equals(atomEntities.get(j).getDiastereotopicID())) {
					interactions.createLink(atomEntities.get(i), atomEntities.get(j));
					interactions.createLink(atomEntities.get(j), atomEntities.get(i));
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	public String getXmlTag(Hashtable xmlProperties) {
		String tempTag = "";

		XMLCoDec tempCodec = new XMLCoDec();

		// if (this.mCanonizer != null) {
		// tempCodec.addParameter("IDCode", this.mCanonizer.getIDCode());
		// tempCodec.addParameter("EncodedCoordinates", this.mCanonizer
		// .getEncodedCoordinates(true));
		// }
		//
		if (this.mCompactMolecule != null) {
			this.mCanonizer = new Canonizer(this.mCompactMolecule);
			tempCodec.addParameter("IDCode", this.mCanonizer.getIDCode());
			tempCodec.addParameter("EncodedCoordinates", this.mCanonizer
					.getEncodedCoordinates(true));
		}

		tempCodec.addParameter("expandedAtoms", this.listExpandedAtoms());
		tempCodec.addParameter("displayIDLevel", String.valueOf(this.displayIDLevel));

		if (this.getParentEntity() != null) {
			tempCodec.addParameter("relX", new Double((double) this
					.getLocation().x
					/ this.getParentEntity().getWidth()));
			tempCodec.addParameter("relY", new Double((double) this
					.getLocation().y
					/ this.getParentEntity().getHeight()));
			tempCodec.addParameter("relWidth", new Double((double) this
					.getWidth()
					/ this.getParentEntity().getWidth()));
			tempCodec.addParameter("relHeight", new Double((double) this
					.getHeight()
					/ this.getParentEntity().getHeight()));
		}
		if (this.getPrimaryColor() != null)
			tempCodec.addParameter("primaryColor", this.getPrimaryColor()
					.getRed()
					+ ","
					+ this.getPrimaryColor().getGreen()
					+ ","
					+ this.getPrimaryColor().getBlue());
		else
			tempCodec.addParameter("primaryColor", "NULL");

		tempTag += "<moldraw.ActMoleculeDisplay "
				+ tempCodec.encodeParameters() + ">\r\n";

		for (int comp = 0; comp < this.getEntitiesCount(); comp++) {
			tempTag += ((BasicEntity) this.getEntity(comp))
					.getXmlTag(xmlProperties);
		}
		tempTag += "</moldraw.ActMoleculeDisplay>\r\n";

		return tempTag;
	}

	/**
	 * This method is only here for compatibility reasons. Since the addition of
	 * the explicit hydrogen feature things are a bit more complex
	 * 
	 * @deprecated
	 */
	public StereoMolecule getStereoMolecule() {
		return this.getCompactMolecule();
	}

	private String listExpandedAtoms() {
		Vector<Integer> expandedAtoms = new Vector<Integer>(this
				.getEntitiesCount());
		for (int i = 0; i < this.isExpanded.length; i++)
			if (this.isExpanded[i])
				expandedAtoms.add(i);
		Collections.sort(expandedAtoms);
		String result = "";
		if (expandedAtoms.size() > 0) {
			result += expandedAtoms.get(0) + "";
			for (int i = 1; i < expandedAtoms.size(); i++)
				result += "," + expandedAtoms.get(i);
		}
		return result;
	}

	/** 
	 * Returns true it the current atom is a proton on a carbon
	 * @param iAtom
	 * @return
	 */
	public boolean isProtonOnCarbon(int atm) {
		if (this.mEnhancedMolecule.getAtomicNo(atm)!=1) {
			if (DEBUG) System.out.println("Atom "+atm+" is not a proton: "+this.mEnhancedMolecule.getAtomicNo(atm));
			return false;
		}
		if (this.mEnhancedMolecule.getConnAtoms(atm)!=1) {
			if (DEBUG) System.out.println("Proton "+atm+" is connected to more than one atom: "+this.mEnhancedMolecule.getConnAtoms(atm));
			return false;
		}
		if ((this.mEnhancedMolecule.getAtomicNo(this.mEnhancedMolecule.getConnAtom(atm, 0)))!=6) {
			if (DEBUG) System.out.println("Proton"+atm+" is not connected to a carbon: "+this.mEnhancedMolecule.getAtomicNo(this.mEnhancedMolecule.getConnAtom(atm, 0)));
			return false;
		}
		return true;
	}
	
	public void switchExpansionState(int iAtom) {
		switchExpansionState(iAtom,true);
	}
	
	public void switchExpansionState(int iAtom, boolean restoreLinks) {
		//System.out.println("restoreLinks "+restoreLinks);
		TreeSet<Integer> unclean = new TreeSet<Integer>();
		
		// getAllAtoms does not get the not drawn hydrogens
		// Did we click on an hydrogen ? In this case we select the heavy connected atom
		if (iAtom >= this.mCompactMolecule.getAllAtoms()) {
			int iHeavyAtom = -1;
			if (this.mEnhancedMolecule.getAllConnAtoms(iAtom) == 1) {
				iHeavyAtom = this.mEnhancedMolecule.getConnAtom(iAtom, 0);
				for (int i = 0; i < this.mEnhancedMolecule.getAllConnAtoms(iHeavyAtom); i++) {
					if (this.mEnhancedMolecule.getAtomicNo(this.mEnhancedMolecule.getConnAtom(iHeavyAtom, i)) == 1)
						unclean.add(this.mEnhancedMolecule.getConnAtom(iHeavyAtom, i));
				}
			}
			if (iHeavyAtom != -1) iAtom = iHeavyAtom;
		}
		
		if (iAtom >= this.mCompactMolecule.getAllAtoms()) return;
		if (this.mCompactMolecule.getImplicitHydrogens(iAtom) == 0) return;
		
		// we always expand / contract all the atoms that have the same diastereotopic ID at once !
		for (int i=0; i<this.diastereotopicIds.length; i++) {
			if (this.diastereotopicIds[iAtom].equals(this.diastereotopicIds[i])) {
				this.isExpanded[i] = !this.isExpanded[i];
				unclean.add(i);
			}
		}
		// TODO expansion / contraction loose the assignment
		TreeMap<Integer, ActAtomEntity> oldAtomEntities = this.storeOldAtomEntities();
		StereoMolecule newMolecule = this.updateMoleculeDisplay();
		this.addStereoMolecule(newMolecule);
		this.createAtomLabels();
		if(restoreLinks)
			this.restoreLinks(oldAtomEntities, unclean);
		this.checkSizeAndPosition();
		this.checkInteractiveSurface();
	}

	public boolean isAtomExpanded(int iAtom) {
		if (iAtom >= this.isExpanded.length)
			return false;
		return this.isExpanded[iAtom];
	}

	private TreeMap<Integer, ActAtomEntity> storeOldAtomEntities() {
		TreeMap<Integer, ActAtomEntity> savedLinks = new TreeMap<Integer, ActAtomEntity>();
		for (int ent = this.getEntitiesCount() - 1; ent >= 0; ent--) {
			if (this.getEntity(ent) instanceof ActAtomEntity) {
				ActAtomEntity atomEntity = (ActAtomEntity) this.getEntity(ent);
				savedLinks.put(atomEntity.getAtomID(), atomEntity);
				this.remove(atomEntity);
			}
		}
		return savedLinks;
	}

	@SuppressWarnings("unchecked")
	private void restoreLinks(TreeMap<Integer, ActAtomEntity> links, TreeSet<Integer> unclean) {
		InteractiveSurface interactions = this.getInteractiveSurface();
		HashMap<Spectra, Boolean> relinked = new HashMap<>();
		//boolean relinked = false;
		if (interactions != null) {
			Iterator<Integer> keys = links.keySet().iterator();
			while(keys.hasNext()){
				ActAtomEntity oldEntity = links.get(keys.next());
				Vector<BasicEntity> linkedEntities = interactions.getLinkedEntities(oldEntity);
				for (int i = 0; i < linkedEntities.size(); i++) {
					BasicEntity parent = (BasicEntity) linkedEntities.get(i).getParentEntity();
					if(parent instanceof Spectra){
						Spectra tmp = (Spectra)parent;
						if(!relinked.containsKey(tmp)){
							NmrSimulator.createPeakAtomLinks(this, tmp);
							relinked.put(tmp, true);
						}
						//relinked=true;
						//break;
					}
				}
			}
		}
		Iterator<Integer> iterator = links.keySet().iterator();
		while (iterator.hasNext()) {
			interactions.removeAllLinks(links.get(iterator.next()));
		}
		interactions.repaint();
	}

	
	/**
	 * returns a the diastereotopicID of the specified atom
	 */
	public String getDiastereotopicID(int iCurrentAtom) {
		return diastereotopicIds[iCurrentAtom];
	}
	
	/**
	 * return a TreeSet containing all the IDs of the protons
	 * @return
	 */
	public TreeSet<String> getHydrogenDiastereotopicIDs(int iCurrentAtom) {
		TreeSet<String> ids=new TreeSet<String>();
		int nNeighbours = this.mEnhancedMolecule.getAllConnAtoms(iCurrentAtom);
		for (int iNeighbour = 0; iNeighbour < nNeighbours; iNeighbour++) {		
			int neighbourID = this.mEnhancedMolecule.getConnAtom(iCurrentAtom, iNeighbour);
			if (this.mEnhancedMolecule.getAtomicNo(neighbourID) == 1)
				ids.add(diastereotopicIds[neighbourID]);
		}
		return ids;
	}
	
	private StereoMolecule updateMoleculeDisplay() {
		StereoMolecule displayMolecule = new StereoMolecule();
		this.mCompactMolecule.copyMolecule(displayMolecule);
		int nCompactAtoms = this.mCompactMolecule.getAllAtoms();
		Arrays.fill(this.mapAtomID2EffectiveID, nCompactAtoms,
				this.mapAtomID2EffectiveID.length, -1);
		Arrays.fill(this.mapEffectiveID2AtomID, nCompactAtoms,
				this.mapEffectiveID2AtomID.length, -1);
		for (int iCurrentAtom = 0; iCurrentAtom < nCompactAtoms; iCurrentAtom++) {
			displayMolecule.setAtomMarker(iCurrentAtom, true);
			if (this.isExpanded[iCurrentAtom]) {
				int nImplicit = this.mCompactMolecule.getImplicitHydrogens(iCurrentAtom);
				int[] hydrogenIDS = new int[nImplicit];
				int j = 0;
				int nNeighbours = this.mEnhancedMolecule.getAllConnAtoms(iCurrentAtom);
				for (int iNeighbour = 0; iNeighbour < nNeighbours; iNeighbour++) {		
					int neighbourID = this.mEnhancedMolecule.getConnAtom(iCurrentAtom, iNeighbour);
					if (this.mEnhancedMolecule.getAtomicNo(neighbourID) == 1)
						hydrogenIDS[j++] = neighbourID;
				}
				Arrays.sort(hydrogenIDS);
				//				
				int nBefore = displayMolecule.getAllAtoms();
				HydrogenHandler.addImplicitHydrogens(displayMolecule, iCurrentAtom);
				for (int i = 0; i < nImplicit; i++) {
					this.mapAtomID2EffectiveID[hydrogenIDS[i]] = nBefore + i;
					this.mapEffectiveID2AtomID[nBefore + i] = hydrogenIDS[i];
				}
			}
		}
		this.transferCoordinates(displayMolecule);
		// EPFLUtils.inventCoords(displayMolecule);
		return displayMolecule;
	}

	private void transferCoordinates(StereoMolecule molecule) {
		for (int iAtomEff = 0; iAtomEff < molecule.getAllAtoms(); iAtomEff++) {
			int atomID = this.mapEffectiveID2AtomID[iAtomEff];
			molecule
					.setAtomX(iAtomEff, this.mEnhancedMolecule.getAtomX(atomID));
			molecule
					.setAtomY(iAtomEff, this.mEnhancedMolecule.getAtomY(atomID));
			molecule
					.setAtomZ(iAtomEff, this.mEnhancedMolecule.getAtomZ(atomID));
		}
	}

	private void prepareInternalMolecules(StereoMolecule molecule, boolean expandAll) {
		// bring the molecule to canonical form

		StereoMolecule canMolecule = EPFLUtils.getCanonicalForm(molecule);
		this.mCompactMolecule = canMolecule;

		DiastereotopicAtomID.addMissingChirality(canMolecule);
		// create a molecule with all hydrogens expanded
		StereoMolecule enhancedMolecule = new StereoMolecule();
		canMolecule.copyMolecule(enhancedMolecule);
		this.mEnhancedMolecule = enhancedMolecule;
		HydrogenHandler.addImplicitHydrogens(this.mEnhancedMolecule);
		
		// we will add missing chirality
		this.diastereotopicIds=DiastereotopicAtomID.getAtomIds(enhancedMolecule);
		
		// we determine symmetry ranks here
		this.enhancedSymmetryRanks = EPFLUtils.getProtonSymmRanksBySubstitution(this.mEnhancedMolecule);
		int nEnhancedAtoms = this.mEnhancedMolecule.getAllAtoms();
		// for now all expanded protons are non equivalent by default
		for (int i = 0; i < nEnhancedAtoms; i++)
			if (this.mEnhancedMolecule.getAtomicNo(i) == 1)
				this.enhancedSymmetryRanks[i] = 2 * nEnhancedAtoms + i;
		int nTotalAtoms = this.mEnhancedMolecule.getAllAtoms();
		this.mapAtomID2EffectiveID = new int[nTotalAtoms];
		Arrays.fill(this.mapAtomID2EffectiveID, -1);
		this.mapEffectiveID2AtomID = new int[nTotalAtoms];
		Arrays.fill(this.mapEffectiveID2AtomID, -1);
		for (int i = 0; i < this.mCompactMolecule.getAllAtoms(); i++)
			this.mapAtomID2EffectiveID[i] = this.mapEffectiveID2AtomID[i] = i;
		if (this.isExpanded == null) {
			this.isExpanded = new boolean[this.mCompactMolecule.getAllAtoms()];
			Arrays.fill(this.isExpanded, expandAll);
		}

		this.transferCoordinates(this.mCompactMolecule);
		this.stereoInfo = EPFLUtils.determineNonEquivalentProtons(
				this.mEnhancedMolecule, this.mCompactMolecule);
		if (DEBUG) {
			System.out.println("Prepare internal molecules (compactMolecule)");
			for (int i = 0; i < this.mCompactMolecule.getAllAtoms(); i++) {
				System.out.println("atom" + i + "=("
						+ this.mCompactMolecule.getAtomicNo(i) + "),["
						+ this.mCompactMolecule.getAtomX(i) + ","
						+ this.mCompactMolecule.getAtomY(i) + ","
						+ this.mCompactMolecule.getAtomZ(i) + "]" + "["
						+ this.mEnhancedMolecule.getAtomX(i) + ","
						+ this.mEnhancedMolecule.getAtomY(i) + ","
						+ this.mEnhancedMolecule.getAtomZ(i) + "]"

				);
			}
		}
		StereoMolecule displayMolecule = this.updateMoleculeDisplay();
		this.addStereoMolecule(displayMolecule);
	}

	public StereoInfo getStereoInfo(int atomID) {
		return this.stereoInfo[atomID];
	}

	public String getProtonIdString(int atomID) {
		if (this.mEnhancedMolecule.getAtomicNo(atomID) == 1) {
			return "" + atomID;
		} else {
			Vector<Integer> attachedProtons = new Vector<Integer>(
					this.mEnhancedMolecule.getAllConnAtoms(atomID));
			for (int i = 0; i < this.mEnhancedMolecule.getAllConnAtoms(atomID); i++) {
				if (this.mEnhancedMolecule.getAtomicNo(this.mEnhancedMolecule
						.getConnAtom(atomID, i)) == 1)
					attachedProtons.add(this.mEnhancedMolecule.getConnAtom(
							atomID, i));
			}
			char token = ',';
			if (this.stereoInfo[atomID] == StereoInfo.HAS_DIFFERENT_PROTONS)
				token = '|';
			String result = "";
			if (attachedProtons.size() > 0) {
				result += attachedProtons.get(0);
				for (int i = 1; i < attachedProtons.get(i); i++)
					result += token + attachedProtons.get(i);
			}
			return result;
		}
	}

	public Vector<Integer> getProtonIds(int atomID) {
		Vector<Integer> protonIds = new Vector<Integer>();
		if (DEBUG)
			System.out.println("atomID=" + atomID);
		if (this.mEnhancedMolecule.getAtomicNo(atomID) == 1) {
			protonIds.add(atomID);
		} else {
			for (int i = 0; i < this.mEnhancedMolecule.getAllConnAtoms(atomID); i++) {
				if (this.mEnhancedMolecule.getAtomicNo(this.mEnhancedMolecule.getConnAtom(atomID, i)) == 1)
					protonIds.add(this.mEnhancedMolecule.getConnAtom(atomID, i));
			}
		}
		return protonIds;
	}

	public StereoMolecule getEnhancedMolecule() {
		return mEnhancedMolecule;
	}

	public int getSymRank(int iAtom) {
		return this.enhancedSymmetryRanks[iAtom];
	}

	public StereoMolecule getCompactMolecule() {
		return mCompactMolecule;
	}

	public TreeMap<Integer, ActAtomEntity> getAtomEntityMap() {
		TreeMap<Integer, ActAtomEntity> atomEntities = new TreeMap<Integer, ActAtomEntity>();
		for (int ent = 0; ent < this.getEntitiesCount(); ent++) {
			if (this.getEntity(ent) instanceof ActAtomEntity) {
				ActAtomEntity atomEntity = (ActAtomEntity) this.getEntity(ent);
				int iAtom = atomEntity.getAtomID();
				atomEntities.put(iAtom, atomEntity);
				if (this.mEnhancedMolecule.getAtomicNo(iAtom) != 1
						&& !this.isExpanded[iAtom]
						&& this.mEnhancedMolecule.getAllHydrogens(iAtom) > 0) {
					for (int iConn = 0; iConn < this.mEnhancedMolecule
							.getAllConnAtoms(iAtom); iConn++) {
						int iNeighbourID = this.mEnhancedMolecule.getConnAtom(
								iAtom, iConn);
						if (this.mEnhancedMolecule.getAtomicNo(iNeighbourID) == 1) {
							//System.out.println("H "+this.mEnhancedMolecule.);
							atomEntities.put(iNeighbourID, atomEntity);
						}
					}
				}
			}
		}
		return atomEntities;
	}
	
	public boolean[] expandAll(){
		boolean[] tmpIsExpanded = new boolean[this.isExpanded.length];
		for(int i=tmpIsExpanded.length-1;i>=0;i--){
			tmpIsExpanded[i]=isExpanded[i];
			//System.out.println(this.mEnhancedMolecule.getAtomicNo(i)+" "+this.isExpanded[i]+" "+this.mEnhancedMolecule.getAtomLabel(i));
			if(this.mEnhancedMolecule.getAtomicNo(i) != 1&& !this.isExpanded[i])
				switchExpansionState(i,true);
		}
		return tmpIsExpanded;
	}
	
	public void setExpansionState(boolean[] states){
	
		for(int i=states.length-1;i>=0;i--){
			if(this.mEnhancedMolecule.getAtomicNo(i) != 1){
				if(this.isExpanded[i]&&(!states[i])||(!this.isExpanded[i])&&states[i])
					switchExpansionState(i,true);
			}
		}
		
	}

	public int getDisplayIDLevel(){
		return displayIDLevel;
	}
	
	public void setDisplayIDLevel(int displayIDLevel){
		this.displayIDLevel=displayIDLevel;
	}
	
	public void shiftDisplayIDLevel() {
		displayIDLevel++;
		if(displayIDLevel==3)
			displayIDLevel=0;
	}

	
	
	public static ActMoleculeDisplay getMolDisplay(InteractiveSurface interactions) {
		if (interactions==null) return null;
		ActMoleculeDisplay molDisplay = (ActMoleculeDisplay) interactions.getEntityByName("molDisplay");
		if (molDisplay == null && interactions.getActiveDisplay() != null && interactions.getActiveDisplay() instanceof SpectraDisplay) {
			SpectraDisplay display = (SpectraDisplay) interactions.getActiveDisplay();
			for (int ent = 0; ent < display.getEntitiesCount(); ent++) {
				if (display.getEntity(ent) instanceof ActMoleculeDisplay) {
					molDisplay = (ActMoleculeDisplay) display.getEntity(ent);
					break;
				}
			}
		}
		return molDisplay;
	}
	
}