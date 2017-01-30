package org.cheminfo.hook.moldraw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.SmartPeakLabel;
import org.cheminfo.hook.util.XMLCoDec;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.epfl.StereoInfo;

public class ActAtomEntity extends BasicEntity implements Comparable<ActAtomEntity> {
	private static Font defaultFont = new Font("courier", Font.PLAIN, 8);

	private StereoMolecule mMolecule;
	// this refers to the atom ID with expanded hydrogens
	private int mAtomID = -1;
	// refers to the atom id currently on display
	private int mEffectiveID = -1;
	private String diastereotopicID="";
	
	
	private StereoInfo stereoInfo = StereoInfo.UNDEF;
	final static int DEFAULT_ATOM_SIZE = 10;
	private TreeSet<BasicEntity> labels = new TreeSet<BasicEntity>();

	private TreeSet<Integer> hydrogenIDS = null;
	
	// this should be false by default
	private String[] compiledIDString = null;

	private int symmetryRank;

	public ActAtomEntity() {
		this.init();
	}

	private static final int ANCHOR_NE = 1;
	private static final int ANCHOR_NW = 2;
	private static final int ANCHOR_SE = 3;
	private static final int ANCHOR_SW = 4;
	private static final int ANCHOR_S = 5;
	private static final int ANCHOR_N = 6;

	private int anchorType = ANCHOR_SE;
	private Point anchorPoint = null;

	public ActAtomEntity(StereoMolecule molecule, int atomID, int effectiveID) {
		this(molecule, atomID, effectiveID, DEFAULT_ATOM_SIZE,
				DEFAULT_ATOM_SIZE);
	}

	public StereoInfo getStereoInfo() {
		return stereoInfo;
	}

	public void setStereoInfo(StereoInfo stereoInfo) {
		this.stereoInfo = stereoInfo;
	}

	public void setDiastereotopicID(String diastereotopicID) {
		this.diastereotopicID = diastereotopicID;
	}
	
	public String getDiastereotopicID() {
		return diastereotopicID;
	}
	
	public ActAtomEntity(StereoMolecule molecule, int atomID, int effectiveID,
			int width, int height) {
		this();
		this.mMolecule = molecule;
		this.mAtomID = atomID;
		this.mEffectiveID = effectiveID;
		this.setPrimaryColor(Color.GRAY);
		this.setSecondaryColor(Color.GREEN);

		this.setMovementType(BasicEntity.GLOBAL);
		// this.setResizable(0);

		this.setSize(width, height);
	}

	public ActAtomEntity(String XMLTag, StereoMolecule molecule) {
		this();
		XMLCoDec tempCodec = new XMLCoDec(XMLTag);
		tempCodec.shaveXMLTag();
		super.getSuperFromXML(tempCodec);
		this.setUniqueID(tempCodec.getParameterAsInt("uniqueID"));
		this.storeLinkIDs(tempCodec);

		this.mMolecule = molecule;
		this.mAtomID = tempCodec.getParameterAsInt("atomID");

		this.mEffectiveID = tempCodec.getParameterAsInt("effectiveID");

		this.setPrimaryColor(tempCodec.getParameterAsColor("primaryColor"));
		this.setSecondaryColor(tempCodec.getParameterAsColor("secondaryColor"));
		this.setSymmetryRank(tempCodec.getParameterAsInt("symmetryRank"));

		this.setMovementType(BasicEntity.GLOBAL);
		// this.setResizable(0);

		this.setSize(DEFAULT_ATOM_SIZE, DEFAULT_ATOM_SIZE);
	}

	private void init() {
		this.setHighlightPropagationType(BasicEntity.HL_PROPAGATION_MAX_RANGE);
		this.setMaximumPropagationLevel(2);
		this.setErasable(false);
	}

	public void setLocation(Point point) {
		this.setLocation(point.x, point.y);
	}

	public void moveLocal(double deltaX, double deltaY) {
		this.getParentEntity().moveLocal(deltaX, deltaY);
	}

	public int getAtomID() {
		return this.mAtomID;
	}

	public int getCharge() {
		return this.mMolecule.getAtomCharge(this.mEffectiveID);
	}

	protected void setCharge() {
		// do nothing, you are not supposed to be able to modify atoms in
		// Actelion version!!
	}

	public void setSymmetryRank(int rank) {
		this.symmetryRank = rank;
	}

	public int getSymmetryRank() {
		return this.symmetryRank;
	}

	public String getComment() {
		return "" + this.mAtomID;
	}

	public String getClickedMessage() {
		return getOverMessage();
	}

	
	
	public String getOverMessage() {
		String message="<Text type=\"plain\">";
		if (this.compiledIDString == null)
			message = "Atom ID: " + this.mAtomID + " - Symmetry rank: "
					+ this.getSymmetryRank() + this.stereoInfo.getGUIString();
		else{
			String tmp ="";
			for(String id:this.compiledIDString)
				tmp+=id+" ";
			message = "Atom ID: " + tmp
					+ " - Symmetry rank: " + this.getSymmetryRank()
					+ this.stereoInfo.getGUIString();
		
			message +=" - "+this.diastereotopicID;
			message+="</Text>";
		}
		return message;

	}


	/*
	 * public void checkSizeAndPosition() { }
	 */
	public void refreshSensitiveArea() {
		this.setSensitiveArea(new Area(new Rectangle2D.Double(
				-this.getWidth() / 2, -this.getHeight() / 2, this.getWidth(),
				this.getHeight())));
	}

	@SuppressWarnings("unchecked")
	public void paint(Graphics2D g) {
		// Dimension thisSize = this.getSize();
		int displayLevel=((ActMoleculeDisplay)this.getParentEntity()).getDisplayIDLevel();
		InteractiveSurface interactiveSurface = this.getInteractiveSurface();
		int nAssigned = 0;
		// is this thing assigned?
		if (interactiveSurface != null) {
			this.labels.clear();
			Vector<BasicEntity> linkedEntities = interactiveSurface
					.getLinkedEntitiesMaxDist(this, 2);
			for (BasicEntity entity : linkedEntities) {
				if (entity instanceof SmartPeakLabel
						&& !((SmartPeakLabel) entity).isPredicted()) {
					this.labels.add(entity);
				}
			}
			nAssigned = labels.size();
		}

		if (this.isMouseover() || this.isHighlighted() || this.isSelected()) {
			g.setColor(this.getPrimaryColor());
			g.fill(new Rectangle2D.Double(-this.getWidth() / 2, -this
					.getHeight() / 2, this.getWidth(), this.getHeight()));
		}
		if (nAssigned > 0) {
			g.setColor(this.getSecondaryColor());
			g.setStroke(new BasicStroke(nAssigned % 10));
			g.draw(new Rectangle2D.Double(-this.getWidth() / 2, -this
					.getHeight() / 2, this.getWidth(), this.getHeight()));
		}
		if (this.compiledIDString != null && this.getParentEntity() 
				instanceof ActMoleculeDisplay && displayLevel>0) {
			g.setColor(new Color(255, 0, 0));
			g.setFont(ActAtomEntity.defaultFont);
			FontMetrics metrics = g.getFontMetrics();
			
			String label=this.compiledIDString[0];
			if(displayLevel==2)
				label+=this.compiledIDString[1];
			
			Rectangle2D bounds = metrics.getStringBounds(label,g);
			g.drawString(label, -Math.round(bounds.getWidth() / 2.0), -5);
		}
	}

	@SuppressWarnings("unchecked")
	public String getXmlTag(Hashtable xmlProperties) {
		XMLCoDec tempCodec = new XMLCoDec();
		super.addSuperToXML(tempCodec);
		tempCodec.addParameter("atomID", new Integer(this.mAtomID));
		tempCodec.addParameter("effectiveID", new Integer(this.mEffectiveID));
		tempCodec.addParameter("primaryColor", this.getPrimaryColor().getRed()
				+ "," + this.getPrimaryColor().getGreen() + ","
				+ this.getPrimaryColor().getBlue());
		tempCodec.addParameter("secondaryColor", this.getSecondaryColor()
				.getRed()
				+ ","
				+ this.getSecondaryColor().getGreen()
				+ ","
				+ this.getSecondaryColor().getBlue());
		tempCodec.addParameter("symmetryRank", new Integer(this.symmetryRank));
		this.addLinkXMLElements(tempCodec);

		String tempTag = "";
		tempTag += "<moldraw.ActAtomEntity ";
		tempTag += tempCodec.encodeParameters();
		tempTag += ">\r\n";
		tempTag += "</moldraw.ActAtomEntity>\r\n";

		return tempTag;
	}

	public int getEffectiveID() {
		return this.mEffectiveID;
	}

	public void setEffectiveID(int effectiveID) {
		this.mEffectiveID = effectiveID;
	}

	
	public TreeSet<String> getProtonDiastereotopicIDs() {
		if (this.getParentEntity() != null) {
			ActMoleculeDisplay molDisplay = (ActMoleculeDisplay) this.getParentEntity();
			return molDisplay.getHydrogenDiastereotopicIDs(this.mAtomID);
		} else {
			return new TreeSet<String>();
		}
	}
	
	
	public String getProtonIdString() {
		if (this.getParentEntity() != null) {
			ActMoleculeDisplay molDisplay = (ActMoleculeDisplay) this.getParentEntity();
			return molDisplay.getProtonIdString(this.mAtomID);
		} else {
			return null;
		}
	}

	public Vector<Integer> getProtonIds() {
		if (this.getParentEntity() != null) {
			ActMoleculeDisplay molDisplay = (ActMoleculeDisplay) this
					.getParentEntity();
			return molDisplay.getProtonIds(this.mAtomID);
		} else {
			return null;
		}
	}

	public boolean isExpanded() {
		if (this.getParentEntity() != null) {
			ActMoleculeDisplay molDisplay = (ActMoleculeDisplay) this
					.getParentEntity();
			return molDisplay.isAtomExpanded(this.mAtomID);
		} else {
			return false;
		}
	}

	/**
	 * 
	 * 
	 * 
	 * @param enhancedMolecule
	 */
	public void cacheExtendedDisplayInfo(StereoMolecule enhancedMolecule,
			StereoMolecule displayMolecule, boolean isExpanded) {
		this.compiledIDString=new String[2];
		this.compiledIDString[0]=String.valueOf(this.mAtomID);
		this.compiledIDString[1]="";
		if (enhancedMolecule.getAtomicNo(this.mAtomID) != 1
				&& isExpanded == false
				&& enhancedMolecule.getAllHydrogens(this.mAtomID) > 0) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("[");
			this.hydrogenIDS = new TreeSet<Integer>();
			for (int iAtm = 0; iAtm < enhancedMolecule
					.getAllConnAtoms(this.mAtomID); iAtm++) {
				int iConn = enhancedMolecule.getConnAtom(this.mAtomID, iAtm);
				if (enhancedMolecule.getAtomicNo(iConn) == 1) {
					hydrogenIDS.add(iConn);
				}
			}
//			this.calculateStringPosition(displayMolecule);
			if (hydrogenIDS.size() > 2) {
				buffer.append(hydrogenIDS.first());
				buffer.append("..");
				buffer.append(hydrogenIDS.last());
			} else {
				for (int id : hydrogenIDS)
					buffer.append(id).append('|');
				buffer.deleteCharAt(buffer.length() - 1);
			}
			buffer.append(']');
			this.compiledIDString[1]=buffer.toString();
		}
	}

	private void calculateStringPosition(StereoMolecule displayMolecule) {
		int nNeighbours = displayMolecule.getAllConnAtoms(this.mEffectiveID);
		switch (nNeighbours) {
		case 1: {
			double x = displayMolecule.getAtomX(this.mEffectiveID);
			double xN = displayMolecule.getAtomX(displayMolecule.getConnAtom(
					this.mEffectiveID, 0));
			if (x < xN) {
				this.anchorType = ActAtomEntity.ANCHOR_SE;
			} else {
				this.anchorType = ActAtomEntity.ANCHOR_SW;
			}
			this.anchorPoint = new Point(0,5);
		}
			break;
		case 2: {
			int iNeighbour1 = displayMolecule.getConnAtom(this.mEffectiveID, 0);
			int iNeighbour2 = displayMolecule.getConnAtom(this.mEffectiveID, 1);
			double dx = 2 * displayMolecule.getAtomX(this.mEffectiveID)
					- displayMolecule.getAtomX(iNeighbour1)
					- displayMolecule.getAtomX(iNeighbour2);
			double dy = 2 * displayMolecule.getAtomX(this.mEffectiveID)
			- displayMolecule.getAtomX(iNeighbour1)
			- displayMolecule.getAtomX(iNeighbour2);

		}
		case 3: {
			this.anchorPoint = new Point(0, -5);
		}

		default:
			this.anchorPoint = new Point(0, -5);
			this.anchorType = ActAtomEntity.ANCHOR_SE;
		}
	}

	public int compareTo(ActAtomEntity o) {
		if (this.mEffectiveID == o.mEffectiveID) {
			return 0;
		} else if (this.mEffectiveID > o.mEffectiveID) {
			return -1;
		} else {
			return 1;
		}
	}

	public String getCompiledIDString() {
		return "888";
	}

	public TreeSet<Integer> getHydrogenIDS() {
		return hydrogenIDS;
	}
}