package org.cheminfo.hook.fragments;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * 
 * 
 * 
 * @author engeler
 * 
 */
public class MolecularFragmentFactory {
	// dynamic stuff
	private int nMolecules = 0;
	private int nFragments = 0;
	private TreeMap<String, Integer> moleculeSet;
	private TreeMap<String, Integer> fragmentSet;

	// static stuff used for the singleton

	private static MolecularFragmentFactory factory = null;

	public static MolecularFragmentFactory getInstance() {
		if (MolecularFragmentFactory.factory == null) {
			MolecularFragmentFactory.factory = new MolecularFragmentFactory();
		}
		return MolecularFragmentFactory.factory;
	}

	public static void destroyInstance() {

	}

	private MolecularFragmentFactory() {
		this.nMolecules = 0;
		this.nFragments = 0;
		this.moleculeSet = new TreeMap<String, Integer>();
		this.fragmentSet = new TreeMap<String, Integer>();
	}

	public int addMolecule(String idString) {
		Integer id = this.moleculeSet.get(idString);
		if (id == null) {
			this.nMolecules++;
			this.moleculeSet.put(idString, this.nMolecules);
			return this.nMolecules;
		} else {
			return id.intValue();
		}
	}

	public int addFragment(String idString) {
		Integer id = this.fragmentSet.get(idString);
		if (id == null) {
			this.nFragments++;
			this.fragmentSet.put(idString, this.nFragments);
			return this.nFragments;
		} else {
			return id.intValue();
		}
	}

	public int getFragmentId(String idString) {
		Integer id = this.fragmentSet.get(idString);
		if (id == null)
			return -1;
		else
			return id.intValue();
	}

	public int getMoleculeId(String idString) {
		Integer id = this.moleculeSet.get(idString);
		if (id == null)
			return -1;
		else
			return id.intValue();
	}

	public int getNumberOfMolecule() {
		return this.nMolecules;
	}

	public int getNumberOfFragment() {
		return this.nFragments;
	}

	public TreeMap<Integer,Integer> remapFragmentAssociations(TreeMap<String,Integer> inputMap) {
		TreeMap<Integer,Integer> outputMap = new TreeMap<Integer,Integer>();
		Iterator<String> iterator = inputMap.keySet().iterator();
		while (iterator.hasNext()) {
			String idString = iterator.next();
			outputMap.put(this.addFragment(idString), inputMap.get(idString));
		}
		return outputMap;
	}
	
}
