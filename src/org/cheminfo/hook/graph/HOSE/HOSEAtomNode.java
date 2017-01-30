package org.cheminfo.hook.graph.HOSE;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import com.actelion.research.chem.Molecule;

/** @deprecated */
public class HOSEAtomNode implements Comparable<HOSEAtomNode> {

	private int parentID = -1;
	private int atomId = -1;
	private int bondType = -1;
	private int sphere = -1;
	private int atomicNumber = -1;
	private int charge = -1;
	private boolean ringClosure = false;

	LinkedList<HOSEAtomNode> childNodes = null;
	
	public HOSEAtomNode() {
		childNodes = new LinkedList<HOSEAtomNode>();
	}

	public int getParentID() {
		return parentID;
	}

	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

	public int getAtomId() {
		return atomId;
	}

	public void setAtomId(int atomId) {
		this.atomId = atomId;
	}

	public int getBondType() {
		return bondType;
	}

	public void setBondType(int bondType) {
		this.bondType = bondType;
	}

	public int getSphere() {
		return sphere;
	}

	public void setSphere(int sphere) {
		this.sphere = sphere;
	}

	public int getAtomicNumber() {
		return atomicNumber;
	}

	public void setAtomicNumber(int atomicNumber) {
		this.atomicNumber = atomicNumber;
	}

	public boolean addNode(HOSEAtomNode node) {
		return this.childNodes.add(node);
	}

	public boolean isRingClosure() {
		return ringClosure;
	}

	public void setRingClosure(boolean ringClosure) {
		this.ringClosure = ringClosure;
	}

	/**
	 * this is an in house HOSE code variant
	 */
	public String toString() {
		if (this.isRingClosure())
			return "&";
		String result = "";
		switch (bondType) {
		case 1:
			result = "";
			break;
		case 2:
			result = "=";
			break;
		case 3:
			result = "%";
			break;
		case 4:
			result = "*";
			break;
		default:
			result = "";
			break;
		}
		result += getSymbolFromAtomNo(this.atomicNumber);
		if (!childNodes.isEmpty()) {
			Collections.sort(this.childNodes);
			result += "{";
			Iterator<HOSEAtomNode> iterator = this.getChildNodeIterator();
			while (iterator.hasNext()) {
				result += iterator.next().toString();
			}
			result += "}";
		}
		return result;
	}

	public Iterator<HOSEAtomNode> getChildNodeIterator() {
		return childNodes.iterator();
	}

	private String getSymbolFromAtomNo(int atomicNumber) {
		return Molecule.cAtomLabel[atomicNumber];
	}

	public int compareTo(HOSEAtomNode o) {
		// get the ring closure business out of the way
		if (this.ringClosure && o.ringClosure)
			return 0;
		if (this.ringClosure && !o.ringClosure)
			return 1;
		if (!this.ringClosure && o.ringClosure)
			return -1;
		// test for bond type
		if (this.bondType < o.bondType)
			return -1;
		if (this.bondType > o.bondType)
			return 1;
		// test for element
		if (this.atomicNumber < o.atomicNumber)
			return -1;
		if (this.atomicNumber > o.atomicNumber)
			return 1;
		// atom types are equal
		if (this.charge < o.charge)
			return -1;
		if (this.charge > o.charge)
			return 1;

		// we have a tie

		if (this.childNodes.size() > o.childNodes.size())
			return -1;
		if (this.childNodes.size() < o.childNodes.size())
			return 1;
		return compareChildNodeLists(childNodes, o.childNodes);
	}

	private int compareChildNodeLists(LinkedList<HOSEAtomNode> listA,
			LinkedList<HOSEAtomNode> listB) {
		Collections.sort(listA);
		Collections.sort(listB);
		int result = 0;
		for (int i = 0; i < listA.size(); i++) {
			result = (listA.get(i)).compareTo(listB.get(i));
			if (result != 0)
				break;
		}
		return result;
	}

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}
}
