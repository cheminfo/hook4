package org.cheminfo.hook.fragments;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;

public class MolecularDistance {
	public static double tanimoto(Set<String> fragmentSetA,
			Set<String> fragmentSetB) {
		int commons = 0;
		Iterator<String> iterator = fragmentSetA.iterator();
		while (iterator.hasNext()) {
			if (fragmentSetB.contains(iterator.next()))
				commons++;
		}
		return ((double) commons / (fragmentSetA.size() + fragmentSetB.size() - commons));
	}

	public static double euclidian(TreeMap<String, Occurrence> fragmentSetA,
			TreeMap<String, Occurrence> fragmentSetB) {
		TreeSet<String> keyUnion = new TreeSet<String>();
		keyUnion.addAll(fragmentSetA.keySet());
		keyUnion.addAll(fragmentSetB.keySet());
		Occurrence value;
		int ia, ib;
		Iterator<String> iterator = keyUnion.iterator();
		String key;
		double diff = 0;
		while (iterator.hasNext()) {
			key = iterator.next();
			value = fragmentSetA.get(key);
			if (value == null)
				ia = 0;
			else
				ia = value.getOccurrence();
			value = fragmentSetB.get(key);
			if (value == null)
				ib = 0;
			else
				ib = value.getOccurrence();
			diff += (ib - ia) * (ib - ia);
		}
		return Math.sqrt(diff);
	}

	/**
	 * Extended Tanimoto distance. e.g. extended Jaccard coefficient
	 *
	 * <pre>
	 * d:= <c|c>/ (<a|a>+<b|b>-<c|c>)
	 * </pre>
	 * 
	 * @param fragmentSetA
	 * @param fragmentSetB
	 * @return
	 */
	public static double extendedTanimoto(
			TreeMap<String, Occurrence> fragmentSetA,
			TreeMap<String, Occurrence> fragmentSetB) {
		TreeSet<String> keyUnion = new TreeSet<String>();
		keyUnion.addAll(fragmentSetA.keySet());
		keyUnion.addAll(fragmentSetB.keySet());
		Occurrence value;
		int ia, ib;
		Iterator<String> iterator = keyUnion.iterator();
		String key;
		int weightA = 0;
		int weightB = 0;
		int weightC = 0;
		while (iterator.hasNext()) {
			key = iterator.next();
			value = fragmentSetA.get(key);
			if (value == null)
				ia = 0;
			else
				ia = value.getOccurrence();
			value = fragmentSetB.get(key);
			if (value == null)
				ib = 0;
			else
				ib = value.getOccurrence();
			weightA += ia * ia;
			weightB += ib * ib;
			weightC += ia * ib;
		}
		return ((double) weightC) / (weightA + weightB - weightC);
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] argv) {
		int nMolecules = 4;
		String idCodes[] = new String[nMolecules];
		idCodes[0] = "RF@Q``"; // ethane
		idCodes[1] = "QM@HzA@"; // propane
		idCodes[2] = "qC`@ISTA@"; // n-butane
		idCodes[3] = "sJP@DiZhA@"; // n-pentane
		String names[] = new String[nMolecules];
		names[0] = "ethane";
		names[1] = "propane";
		names[2] = "n-butane";
		names[3] = "n-pentane";
		TreeMap<String, Occurrence>[] fragmentSets = new TreeMap[nMolecules];
		//
		FragmentGenerator fragmentGenerator = new FragmentGenerator();
		fragmentGenerator.setMinAtoms(2);
		fragmentGenerator.setMaxAtoms(3);
		IDCodeParser parser = new IDCodeParser();
		StereoMolecule molecule = new StereoMolecule();
		for (int iMolecule = 0; iMolecule < nMolecules; iMolecule++) {
			parser.parse(molecule, idCodes[iMolecule]);
			fragmentGenerator.setMolecule(molecule);
			fragmentSets[iMolecule] = fragmentGenerator.generate();
		}
		DecimalFormat format = new DecimalFormat("#0.000");
		for (int iMoleculeA = nMolecules - 1; iMoleculeA >= 0; iMoleculeA--) {
			for (int iMoleculeB = 0; iMoleculeB <= iMoleculeA; iMoleculeB++) {
				System.out.println("molA={"
						+ names[iMoleculeA]
						+ "}molB={"
						+ names[iMoleculeB]
						+ "}tanimoto={"
						+ format.format(MolecularDistance.tanimoto(
								fragmentSets[iMoleculeA].keySet(),
								fragmentSets[iMoleculeB].keySet()))
						+ "}generalizedTanimoto{"
						+ format.format(MolecularDistance.extendedTanimoto(
								fragmentSets[iMoleculeA],
								fragmentSets[iMoleculeB]))
						+ "}euclidian={"
						+ format.format(MolecularDistance.euclidian(
								fragmentSets[iMoleculeA],
								fragmentSets[iMoleculeB]))
								+ "}");
			}
		}

	}

}
