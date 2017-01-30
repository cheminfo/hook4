package org.cheminfo.hook.nemo;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.nmr.Nucleus;
import org.cheminfo.hook.nemo.nmr.ProprietaryTools;

public class HTMLPeakTableFormater {
	private static DecimalFormat newFormat = new DecimalFormat();

	private static final boolean DEBUG = false;

	
	public static String getPeakHtml(Spectra spectrum, String format, boolean ascending) {
		if (spectrum == null) return "Could not retrieve any spectra";
		SpectraData spectraData = spectrum.getSpectraData();
		if (spectraData == null) return "The spectra does not contain any spectraData";
		String toReturn="";
		switch (spectraData.getDataType()) {
			case SpectraData.TYPE_NMR_SPECTRUM:
				if (format==null) format="########0.00";
				toReturn+=HTMLPeakTableFormater.getNmrHtml(spectrum, format, ascending);
				break;
			case SpectraData.TYPE_2DNMR_SPECTRUM:
				toReturn+=HTMLPeakTableFormater.getNmrHtml2D((SpectraDisplay) spectrum.getParentEntity(), format, ascending);
				break;
			case SpectraData.TYPE_IR:
				if (format==null) format="########0";
				toReturn+="IR (cm<sup>-1</sup>): ";
				toReturn+=HTMLPeakTableFormater.getPeakValueHtml(spectrum, format, ascending);
				break;
			case SpectraData.TYPE_UV:
				if (format==null) format="########0";
				toReturn+="UV: ";
				toReturn+=HTMLPeakTableFormater.getPeakValueHtml(spectrum, format, ascending);
				break;
			case SpectraData.TYPE_MASS:
				if (format==null) format="########0.00";
				toReturn+="MASS: ";
				toReturn+=HTMLPeakTableFormater.getPeakValueHtml(spectrum, format, ascending);
				break;
			case SpectraData.TYPE_HPLC:
				if (format==null) format="########0.0";
				toReturn+="HPLC: ";
				toReturn+=HTMLPeakTableFormater.getPeakValueHtml(spectrum, format, ascending);
				break;
			default:
				toReturn+="Undefined procedure";
				break;
		}
		return toReturn;
	}

	public static String getPeakValueHtml(Spectra spectra, String format, boolean ascending) {
		String tempString="";
		newFormat.applyPattern(format);
		SmartPeakLabel[] sPLArray = getSmartLabels(spectra, ascending);
		if (sPLArray.length>0) {
			for (int label = 0; label < sPLArray.length; label++) {
				if (label != 0) tempString += ", ";
				
				int dNbDecimals=2;
				int jNbDecimals=1;
				if (spectra.getNucleus().equals(Nucleus.NUC_13C)) {
					dNbDecimals=1;
					jNbDecimals=0;
				}
				tempString += sPLArray[label].getNmrHtml(dNbDecimals, jNbDecimals);
				
			}
		} else {
			PeakLabel[] pLArray = spectra.getPeakLabels(ascending);
			for (int label = 0; label < pLArray.length; label++) {
				if (label != 0) tempString += ", ";
				tempString += pLArray[label].toHtml(newFormat);
			}
		}
		return tempString;
	}
	
	
	public static String getNmrHtml(Spectra spectra, String format, boolean ascending) {
		// TODO take into account format
		newFormat.applyPattern(format);
		
		Nucleus nucleus = spectra.getNucleus();

		String tempString = nucleus.toHtml() + " NMR <i>&delta;</i>: ";
		
		tempString+=HTMLPeakTableFormater.getPeakValueHtml(spectra, format, ascending);
		return tempString;
	}

	public static String getNmrHtml2D(SpectraDisplay spectraDisplay, String format, boolean ascending) {
		// TODO: take into account format !!!
		newFormat.applyPattern("#0.00");

		Spectra twoDimSpectrum = spectraDisplay.getFirstSpectra();
		Spectra horSpectrum = spectraDisplay.getHorRefSpectrum();
		Spectra verSpectrum = spectraDisplay.getVerRefSpectrum();

		String horizontal = null;
		String vertical = null;
		if (horSpectrum != null) {
			horizontal = HTMLPeakTableFormater.getNmrHtml(horSpectrum, format, ascending);
		} else {
			horizontal = HTMLPeakTableFormater.getNmrHtmlFrom2D(twoDimSpectrum, false,
					ascending);
		}
		if (verSpectrum != null) {
			vertical = HTMLPeakTableFormater.getNmrHtml(verSpectrum, format, ascending);
		} else {
			vertical = HTMLPeakTableFormater.getNmrHtmlFrom2D(twoDimSpectrum, true,
					ascending);
		}
		if (twoDimSpectrum.isHomonuclear()) {
			if (horSpectrum != null)
				return horizontal;
			else
				return vertical;
		} else
			return horizontal + "\t" + vertical;
	}

	private static String getNmrHtmlFrom2D(Spectra spectrum, boolean vertical,
			boolean ascending) {
		Nucleus nucleus = null;
		if (vertical)
			nucleus = spectrum.getNucleus(2);
		else
			nucleus = spectrum.getNucleus(1);

		String outString = nucleus.toHtml() + "<font face=\"symbol\">d</font></i>: ";
		Vector<Double> peaks = new Vector<Double>(spectrum.getEntitiesCount());
		for (int ent = 0; ent < spectrum.getEntitiesCount(); ent++) {
			if (spectrum.getEntity(ent) instanceof PeakLabel) {
				if (vertical)
					peaks.add(((PeakLabel) spectrum.getEntity(ent))
							.getYPosUnits());
				else
					peaks.add(((PeakLabel) spectrum.getEntity(ent))
							.getXPosUnits());
			}
		}
		if (peaks.size() == 0)
			return "";
		Double[] peakArray = new Double[peaks.size()];
		peakArray = peaks.toArray(peakArray);
		Arrays.sort(peakArray);
		if (ascending) {
			outString += newFormat.format(peakArray[0]);
			for (int i = 1; i < peakArray.length; i++)
				outString += ", " + newFormat.format(peakArray[i]);
		} else {
			outString += newFormat.format(peakArray[peakArray.length - 1]);
			for (int i = peakArray.length - 2; i > -1; i--)
				outString += ", " + newFormat.format(peakArray[i]);
		}
		return outString;
	}
	
	public static String getPeaksTable2D(SpectraDisplay spectraDisplay) {
		Spectra mainSpectrum = spectraDisplay.getFirstSpectra();
		String outString = "";

		outString += "nucleus1\tdelta1\tnucleus2\tdelta2\theight\n";

		int nEntities = mainSpectrum.getEntitiesCount();
		for (int iEntity = 0; iEntity < nEntities; iEntity++) {
			BasicEntity tmpEntity = mainSpectrum.getEntity(iEntity);
			if (tmpEntity instanceof PeakLabel) {
				outString+=mainSpectrum.getNucleus(1)+"\t";
				outString+=((PeakLabel)tmpEntity).getXPos()+"\t";
				outString+=mainSpectrum.getNucleus(2)+"\t";
				outString+=((PeakLabel)tmpEntity).getYPos()+"\t";
				
				PeakLabel peakLabel=((PeakLabel)tmpEntity);
				outString+=((Spectra)peakLabel.getParentEntity()).getSpectraData().getZForUnits(peakLabel.getXPos(),peakLabel.getYPos())+"\n";
			}
		}
		return outString;
	}
	
	public static String getNmrTable2D(SpectraDisplay spectraDisplay) {
		Spectra mainSpectrum = spectraDisplay.getFirstSpectra();
		Spectra horRefSpectrum = spectraDisplay.getHorRefSpectrum();
		if (mainSpectrum == horRefSpectrum)
			horRefSpectrum = null;
		if (horRefSpectrum == null)
			return "no horizontal spectrum";
		Spectra verRefSpectrum = null;
		if (mainSpectrum.isHomonuclear())
			verRefSpectrum = horRefSpectrum;
		else
			verRefSpectrum = spectraDisplay.getVerRefSpectrum();
		if (verRefSpectrum == null)
			return "no vertical spectrum";

		InteractiveSurface interactions = mainSpectrum.getInteractiveSurface();

		newFormat.applyPattern("#0.00");
		//
		String outString = "";

		outString += "atomID\tnucleus\tdelta1\tdelta2\tpattern\tnH\tmultiplicity and coupling constants\n";

		TreeSet<SmartPeakLabel> peakLabels = new TreeSet<SmartPeakLabel>();

		int nEntities = mainSpectrum.getEntitiesCount();
		for (int iEntity = 0; iEntity < nEntities; iEntity++) {
			BasicEntity tmpEntity = mainSpectrum.getEntity(iEntity);
			if (tmpEntity instanceof PeakLabel) {
				Vector linkedEntities = interactions
						.getLinkedEntities(tmpEntity);
				for (int i = 0; i < linkedEntities.size(); i++) {
					if (linkedEntities.get(i) instanceof SmartPeakLabel) {
						peakLabels.add((SmartPeakLabel) linkedEntities.get(i));
					}
				}
			}
		}
		// get additional peak labels from 1D
		for (int ent = 0; ent < horRefSpectrum.getEntitiesCount(); ent++) {
			if (horRefSpectrum.getEntity(ent) instanceof SmartPeakLabel) {
				peakLabels.add((SmartPeakLabel)horRefSpectrum.getEntity(ent));
			}
		}
		for (int ent = 0; ent < verRefSpectrum.getEntitiesCount(); ent++) {
			if (verRefSpectrum.getEntity(ent) instanceof SmartPeakLabel) {
				peakLabels.add((SmartPeakLabel)verRefSpectrum.getEntity(ent));
			}
		}
		ActMoleculeDisplay molDisplay = ActMoleculeDisplay.getMolDisplay(interactions);

		Iterator<SmartPeakLabel> iterator = peakLabels.iterator();
		while (iterator.hasNext()) {
			SmartPeakLabel currentSPL = iterator.next();
			outString += getNmrTableRow(currentSPL, molDisplay, ((Spectra) currentSPL.getParentEntity()).getNucleus());
		}

		return outString;
	}

	public static String getNmrTable(Spectra spectra, boolean ascending,
			boolean showTitle) {
		return HTMLPeakTableFormater.getNmrTableNew(spectra, ascending, showTitle);
	}
	
	private static String getNmrTableNew(Spectra spectra, boolean ascending, boolean showTitle) {
		newFormat.applyPattern("#0.00");
		String outString = "";
		SmartPeakLabel[] sPLArray = getSmartLabels(spectra, ascending);
		InteractiveSurface interactions = spectra.getInteractiveSurface();
		// get access to the molecule display
		
		ActMoleculeDisplay molDisplay = ActMoleculeDisplay.getMolDisplay(interactions);
		
		if (sPLArray.length == 0)
			return "";
		if (showTitle) {
			outString += "atomID\t" + "nucleus\t" + "delta1\t" + "delta2\t" + "pattern\t" + "integ.\t" + "multiplicity and coupling constants\t" + "\n";
		}
		Nucleus nucleus = spectra.getNucleus();

		for (int iLabel = 0; iLabel < sPLArray.length; iLabel++) {
			outString += HTMLPeakTableFormater.getNmrTableRow(sPLArray[iLabel],molDisplay, nucleus);
		}
		return outString;
	}

	private static String getNMRTableRow(PeakLabel current2DPeakLabel,
			InteractiveSurface interactions, Spectra refSpectrum) {
		String outString = "";

		ActMoleculeDisplay molDisplay = null;
		molDisplay = (ActMoleculeDisplay) interactions
				.getEntityByName("molDisplay");
		if (molDisplay == null) {
			if (refSpectrum.getParentEntity() instanceof SpectraDisplay) {
				molDisplay = (ActMoleculeDisplay) refSpectrum.getParentEntity()
						.getEntityByName("molDisplay");
			}
		}
		Nucleus nucleus = refSpectrum.getNucleus();
		Vector<BasicEntity> childEntities = interactions
				.getLinkedDestEntities(current2DPeakLabel);
		int nEntities = childEntities.size();
		TreeSet<SmartPeakLabel> done = new TreeSet<SmartPeakLabel>();
		for (int ent = 0; ent < nEntities; ent++) {
			if (childEntities.get(ent) instanceof SmartPeakLabel) {
				SmartPeakLabel peakLabel = (SmartPeakLabel) childEntities
						.get(ent);
				System.out.println("found " + ent + " " + peakLabel);
				if (!done.contains(peakLabel)) {
					outString += HTMLPeakTableFormater.getNmrTableRow(peakLabel,
							molDisplay, nucleus);
					done.add(peakLabel);
				}
			}
		}
		return outString;
	}


	private static String getNmrTableRow(SmartPeakLabel peakLabel, ActMoleculeDisplay molDisplay, Nucleus nucleus) {
		String outString = "";
		InteractiveSurface interactions = peakLabel.getInteractiveSurface();

		String atomIDs = "";
		if (molDisplay != null) {
			atomIDs = ProprietaryTools.resolveAtomIDsAsString(interactions, molDisplay, peakLabel,	nucleus, false);
		}

		outString += atomIDs + "\t" + nucleus.toString() + "\t";

		String peakPattern = peakLabel.getPeakPattern();
		
		// PEAK LOCATION/MASSIVE BOUNDS
		if (peakPattern.equals("m") && (peakLabel.isForceMassif() || peakLabel.getNmrSignal1D().getNbPeaks() == 0)) {
			outString += newFormat.format(peakLabel.getNmrSignal1D().getEndX()) + "\t" + newFormat.format(peakLabel.getNmrSignal1D().getStartX()) + "\t";
		} else {
			outString += newFormat.format(peakLabel.getNmrSignal1D().getShift()) + "\t\t";
		}

		outString += peakPattern + "\t";

		Integral linkedIntegral = getLinkedIntegral(interactions, peakLabel);
		// INTEGRAL
		if (linkedIntegral != null)
			outString += newFormat.format(linkedIntegral.getRelArea());
		outString += "\t";

		// add peak pattern, eg. multiplicities, and coupling constants
		if (peakPattern.equals("m") || peakPattern.equals("s"))
			outString += "\r\n";
		else
			outString += peakLabel.getNmrTable();

		return outString;
	}
	
	private static SmartPeakLabel[] getSmartLabels(Spectra spectra, boolean ascending) { // Sort
		// the
		// SmartPeakLabels
		Vector<SmartPeakLabel> sPL = new Vector<SmartPeakLabel>(); // SmartPeakLabel vector
		for (int entity = 0; entity < spectra.getEntitiesCount(); entity++) {
			if (spectra.getEntity(entity) instanceof SmartPeakLabel)
				sPL.addElement((SmartPeakLabel)spectra.getEntity(entity));
		}

		SmartPeakLabel[] sPLArray = new SmartPeakLabel[sPL.size()];

		sPL.toArray(sPLArray);

		if (ascending) {
			Arrays.sort(sPLArray);
		} else {
			Arrays.sort(sPLArray, Collections.reverseOrder());
		}
		return sPLArray;
	}
	
	private static Integral getLinkedIntegral(InteractiveSurface interactions,
			SmartPeakLabel peakLabel) {
		Integral linkedIntegral = null;
		Vector linkedEntities = interactions.getLinkedEntities(peakLabel);
		for (int index = 0; index < linkedEntities.size(); index++) {
			if (linkedEntities.get(index) instanceof Integral) {
				linkedIntegral = (Integral) linkedEntities.get(index);
				break;
			}
		}
		return linkedIntegral;
	}
}
