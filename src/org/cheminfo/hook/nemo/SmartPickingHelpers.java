package org.cheminfo.hook.nemo;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.math.peakdetection.AsymmetricPeak;
import org.cheminfo.hook.math.peakdetection.PeakFinders;
import org.cheminfo.hook.nemo.functions.GetNMRSignal1D;
import org.cheminfo.hook.nemo.nmr.IntegralData;
import org.cheminfo.hook.nemo.nmr.Nucleus;
import org.cheminfo.hook.nemo.nmr.PredictionData;
import org.cheminfo.hook.nemo.nmr.ProprietaryTools;
import org.cheminfo.hook.nemo.signal.NMRSignal1D;

public class SmartPickingHelpers {
	private static boolean DEBUG=false;
	protected static SmartPeakLabel findSmartPeakRobust() {
		return null;
	}
	
	
	/*protected static void evaluatePeakSelect(InteractiveSurface interactions, AsymmetricPeak[] peaks) {
		ProprietaryTools.evaluatePeakSelect(interactions, peaks);
	}*/

	protected static void evaluatePeakSelect(InteractiveSurface interactions, NMRSignal1D[] peaks) {
		ProprietaryTools.evaluatePeakSelect(interactions, peaks);
	}


	protected static SmartPeakLabel findSmartPeak(Point2D.Double firstPoint,
			Point2D.Double secondPoint, double threshold, Spectra spectra) {
		//SpectraDisplay parentDisplay = (SpectraDisplay) spectra
		//		.getParentEntity();

		double lowerXLimit, upperXLimit;
		SmartPeakLabel tempLabel;
		spectra.getSpectraData().setActiveElement(spectra.getSpectraNb());

		lowerXLimit = spectra.pixelsToUnits(Math.min(firstPoint.x,secondPoint.x));
		upperXLimit = spectra.pixelsToUnits(Math.max(firstPoint.x,secondPoint.x));
		
		NMRSignal1D signal1D = GetNMRSignal1D.getSignal(spectra.getSpectraData(), lowerXLimit, upperXLimit, threshold); 

		//signal1D.setNucleus(spectra.getSpectraData().getNucleus());
		if(signal1D!=null){
			tempLabel = new SmartPeakLabel(signal1D);
			if (signal1D.getNbPeaks() != 0) {
				spectra.addEntity(tempLabel);
				tempLabel.checkSizeAndPosition();
				return tempLabel;
			}
		}
		
	    //No peaks found
		return null;
		
		/*tempLabel = new SmartPeakLabel(spectra.pixelsToUnits(firstPoint.x),
				spectra.pixelsToUnits(secondPoint.x));
		double alpha, beta, gamma,p;
		for (int currentPoint = spectra.unitsToArrayPoint(lowerXLimit) + 2; currentPoint <= spectra
				.unitsToArrayPoint(upperXLimit) - 2; currentPoint++) {
			if (spectra.spectraData.getY(currentPoint) > threshold
					&& spectra.spectraData.getY(currentPoint - 1) <= spectra.spectraData
							.getY(currentPoint)
					&& spectra.spectraData.getY(currentPoint) > spectra.spectraData
							.getY(currentPoint + 1))// &&
			{
				//tempLabel.addPeak(spectra.arrayPointToUnits(currentPoint));
				//tempLabel.setIntensity(tempLabel.getNbPeaks() - 1,spectra.spectraData.getY(currentPoint));
				//Using an interpolation peak detection
				
				alpha=20*Math.log10(spectra.spectraData.getY(currentPoint-1));
				beta=20*Math.log10(spectra.spectraData.getY(currentPoint));
				gamma=20*Math.log10(spectra.spectraData.getY(currentPoint+1));
				p=0.5*(alpha-gamma)/(alpha-2*beta+gamma);
				tempLabel.getNmrSignal1D().addPeak(spectra.arrayPointToUnits(currentPoint+p),spectra.spectraData.getY(currentPoint)-0.25*
						(spectra.spectraData.getY(currentPoint-1)-spectra.spectraData.getY(currentPoint+1))*p);
				//tempLabel.getNmrSignal1D().addPeak(spectra.arrayPointToUnits(currentPoint+p));
				//tempLabel.setIntensity(tempLabel.getNbPeaks() - 1,spectra.spectraData.getY(currentPoint)-0.25*
				//		(spectra.spectraData.getY(currentPoint-1)-spectra.spectraData.getY(currentPoint+1))*p);
			}
		}

		if (tempLabel.getNmrSignal1D().getNbPeaks() != 0) {
			spectra.addEntity(tempLabel);

			tempLabel.getNmrSignal1D().compute();

			tempLabel.checkSizeAndPosition();
			return tempLabel;
		} else
			return null;
		*/
	}

	protected static SmartPeakLabel findSmartPeak(Point2D.Double firstPoint, Point2D.Double secondPoint, Spectra spectra) {
		//SpectraDisplay parentDisplay = (SpectraDisplay) spectra
		//		.getParentEntity();
		double upperYLimit;

		spectra.spectraData.setActiveElement(spectra.getSpectraNb());

		// lowerXLimit=parentDisplay.absolutePixelsToUnitsH(Math.min(firstPoint.x,
		// secondPoint.x));
		// upperXLimit=parentDisplay.absolutePixelsToUnitsH(Math.max(firstPoint.x,
		// secondPoint.x));
		// lowerYLimit=Math.min(firstPoint.y,
		// secondPoint.y)-spectra.getLocation().y;
		upperYLimit = Math.max(firstPoint.y, secondPoint.y);

		double threshold = ((spectra.getHeight() - upperYLimit) * spectra.spectraData.getMaxY())
				/ (spectra.getHeight() * spectra.getMultFactor());

		return SmartPickingHelpers.findSmartPeak(firstPoint, secondPoint, threshold, spectra);
	}

	protected static SmartPeakLabel putSmartPeakLabels(double firstLimit,double secondLimit, Spectra spectrum,
			InteractiveSurface interactions) {
		return SmartPickingHelpers.putSmartPeakLabels(firstLimit, secondLimit, spectrum, interactions, false);
	}

	protected static SmartPeakLabel findOverlappingSmartPeakLabel(double pos,
			Spectra spectrum, InteractiveSurface interactions) {
		double delta = spectrum.getNucleus().getDetectionBounds();
		double xMin = pos - delta;
		double xMax = pos + delta;
		Vector<SmartPeakLabel> foundLabels = new Vector<SmartPeakLabel>();
		for (int ent = 0; ent < spectrum.getEntitiesCount(); ent++) {
			if (spectrum.getEntity(ent) instanceof SmartPeakLabel) {
				SmartPeakLabel spl = (SmartPeakLabel) spectrum.getEntity(ent);
				double a = spl.getNmrSignal1D().getStartX();
				double b = spl.getNmrSignal1D().getEndX();
				if (
				// SmartPeakLabel's limits fall into the search range
				(a >= xMin && a <= xMax) || (b >= xMin && b <= xMax) ||
				// the position is completeley within the bound of the
				// SmartPeakLabel
						(Math.min(a, b) <= pos && Math.max(a, b) >= pos))
					foundLabels.add(spl);
			}
		}
		// find closest
		if (foundLabels.size() == 0)
			return null;
		int iClosest = 0;
		double minDist = Math.abs(foundLabels.get(0).getNmrSignal1D().getShift() - pos);
		for (int i = 1; i < foundLabels.size(); i++) {
			double currentDist = Math.abs(foundLabels.get(i).getNmrSignal1D().getShift() - pos);
			if (currentDist < minDist) {
				minDist = currentDist;
				iClosest = i;
			}
		}
		return foundLabels.get(iClosest);
	}

	
	protected static SmartPeakLabel findSmartPeakLabel(double firstLimit,double secondLimit, Spectra spectrum) {
		if (spectrum==null) return null;
		double rangeCenter = (firstLimit + secondLimit) / 2;
		int nEntities = spectrum.getEntitiesCount();
		Vector<SmartPeakLabel> existingLabels = new Vector<SmartPeakLabel>(8);
		double a = Math.min(firstLimit, secondLimit);
		double b = Math.max(firstLimit, secondLimit);
		BasicEntity e;
		for (int iEntity = 0; iEntity < nEntities; iEntity++) {
			e = spectrum.getEntity(iEntity);
			if (e instanceof SmartPeakLabel) {
				SmartPeakLabel label = (SmartPeakLabel) e;
				double x0 = Math.min(label.getNmrSignal1D().getStartX(), label.getNmrSignal1D().getEndX());
				double xn = Math.max(label.getNmrSignal1D().getStartX(), label.getNmrSignal1D().getEndX());
				if (!((a < x0 && b < x0) || (a > xn && b > xn))) {
					existingLabels.add(label);
				}
			}
		}
		if (existingLabels.size() > 0) {
			int nLabels = existingLabels.size();
			SmartPeakLabel closestExisting = existingLabels.elementAt(0);
			SmartPeakLabel currentLabel = null;
			for (int iLabel = 1; iLabel < nLabels; iLabel++) {
				currentLabel = existingLabels.elementAt(iLabel);
				if (Math.abs(closestExisting.getNmrSignal1D().getShift() - rangeCenter) > Math.abs(currentLabel.getNmrSignal1D().getShift() - rangeCenter)) {
					closestExisting = currentLabel;
				}
			}
			return closestExisting;
		}
		return null;
	}
	
	
	/**
	 * Will add a smart peak label on the 1D traces if there is none found in the same area or
	 * if we press SHIFT (to force horizontal smart peak label) or CTRL (to force vertical smart peak label).
	 * 
	 * @param firstLimit
	 * @param secondLimit
	 * @param spectrum
	 * @return
	 */
	protected static SmartPeakLabel putSmartPeakLabels(double firstLimit,double secondLimit, Spectra spectrum,
			InteractiveSurface interactions, boolean forceSmartPeak) {
		
		SpectraData spectraData=spectrum.getSpectraData();
		
		double rangeCenter = (firstLimit + secondLimit) / 2;
		// try to find existing peaks if force is disabled
		if (!forceSmartPeak) {
			SmartPeakLabel closestExisting=findSmartPeakLabel(firstLimit, secondLimit, spectrum);
			if (closestExisting != null) return closestExisting;
		}
		if (spectraData.getTitle().equals(PredictionData.projectedTrace)) return null;
	
		int from = Math.min(spectrum.unitsToArrayPoint(firstLimit), spectrum.unitsToArrayPoint(secondLimit));
		int to = Math.max(spectrum.unitsToArrayPoint(firstLimit), spectrum.unitsToArrayPoint(secondLimit));
		Nucleus nucleus = spectrum.getNucleus();
		double nStddev = PeakFinders.getNMRPeakThreshold(nucleus);
		double baselineRejoin = PeakFinders.getNMRBaselineRejoinThreshold(nucleus);
		
		NMRSignal1D[] peakList = PeakFinders.simpleThresholdedDetectionBSRRejoin(spectraData, spectraData.getSubSpectraDataY(0), from,
						to, nStddev, baselineRejoin);

	
		// What should be do if there is no peak in the range
		// We can just add one in the middle ...
		// If there is only one peak in the mi
		if (peakList.length == 0) {
			//AsymmetricPeak asym=PeakFinders.middleFinder(firstLimit, secondLimit, spectraData);
			NMRSignal1D asym=PeakFinders.middleFinder2(firstLimit, secondLimit, spectraData);
			return SmartPickingHelpers.addSmartPeakLabel(interactions, spectrum, asym,true);
		} else {
			// locate the peak instance which is nearest to rangeCenter
			int nPeaks = peakList.length;
			NMRSignal1D closestPeak = peakList[0];
			NMRSignal1D currentPeak = null;
			for (int iPeak = 1; iPeak < nPeaks; iPeak++) {
				currentPeak = peakList[iPeak];
				if (Math.abs(currentPeak.getCenter() - rangeCenter) < Math.abs(closestPeak.getCenter() - rangeCenter)) {
					closestPeak = currentPeak;
				}
			}
			//
			for (NMRSignal1D asym : peakList) {
				if(DEBUG) System.out.println(asym.toString());
			}
			
			return SmartPickingHelpers.addSmartPeakLabel(interactions, spectrum, closestPeak, false);
		}
	}
	
	/**
	 * Just for compatibility reason maintained. We need to re-write the functions that depends or use AsymmetricPeak
	 * to use NMRSignal1D. 
	 * @param interactions
	 * @param spectrum
	 * @param peak
	 * @return
	 */
	public static SmartPeakLabel addSmartPeakLabel(InteractiveSurface interactions, Spectra spectrum, AsymmetricPeak peak) {
		//TODO Re-Write this function
		return SmartPickingHelpers.addSmartPeakLabel(interactions, spectrum, peak, false);
	}
    
	public static SmartPeakLabel addSmartPeakLabel(InteractiveSurface interactions, Spectra spectrum, AsymmetricPeak peak, boolean noIntegral) {
		//TODO Re-Write this function
		//Creates a new NMRSignal1D for this AsymmetricPeak
		SpectraData spectraData = spectrum.getSpectraData();
		NMRSignal1D nmrSignal1D=new NMRSignal1D(spectraData.arrayPointToUnits(peak.getPeakStart()),spectraData.arrayPointToUnits( peak.getPeakEnd()), 
							spectrum.spectraData.getParamDouble("observefrequency",0), 
							spectrum.spectraData.getXUnits());
		nmrSignal1D.setInterval(spectrum.spectraData.getInterval());
		
		for(int i=0;i<peak.getNbOfPeaks();i++){
			nmrSignal1D.addPeak(spectraData.arrayPointToUnits(peak.getPeakLocation(i)),1);
		}
		return addSmartPeakLabel(interactions,spectrum,nmrSignal1D, noIntegral);
		
	}
	
	/*protected static SmartPeakLabel findSmartPeakUnits(double firstLimit,
			double secondLimit, double threshold, Spectra spectra) {
	
		SmartPeakLabel tempLabel = new SmartPeakLabel(firstLimit, secondLimit);

		for (int currentPoint = spectra.unitsToArrayPoint(firstLimit) + 2; currentPoint <= spectra
				.unitsToArrayPoint(secondLimit) - 2; currentPoint++) {
			// if (
			// ((int)Math.floor(tempDimension.height-multFactor*(spectra.spectraData.getY(currentPoint)-spectra.spectraData.getMinY())*(tempDimension.height-10)/(spectra.spectraData.getMaxY()-spectra.spectraData.getMinY()))-10)
			// < upperYLimit &&
			if (spectra.spectraData.getY(currentPoint) > threshold
					&&
					// spectra.spectraData.getY(currentPoint-2) <=
					// spectra.spectraData.getY(currentPoint-1) &&
					spectra.spectraData.getY(currentPoint - 1) <= spectra.spectraData
							.getY(currentPoint)
					&& spectra.spectraData.getY(currentPoint) >= spectra.spectraData
							.getY(currentPoint + 1))// &&
			// spectra.spectraData.getY(currentPoint+1) >=
			// spectra.spectraData.getY(currentPoint+2))
			{
				tempLabel.addPeak(spectra.arrayPointToUnits(currentPoint));
				tempLabel.setIntensity(tempLabel.getNbPeaks() - 1,
						spectra.spectraData.getY(currentPoint));
			}
		}

		if (tempLabel.getNbPeaks() != 0) {
			spectra.addEntity(tempLabel);

			tempLabel.compute();

			tempLabel.checkSizeAndPosition();
			return tempLabel;
		} else
			return null;
	}*/

	/*public static SmartPeakLabel addSmartPeakLabel(InteractiveSurface interactions, Spectra spectrum, AsymmetricPeak peak) {
		return SmartPickingHelpers.addSmartPeakLabel(interactions, spectrum, peak, false);
	}

	public static SmartPeakLabel addSmartPeakLabel(InteractiveSurface interactions, Spectra spectrum, AsymmetricPeak peak, boolean noIntegral) {
		SmartPeakLabel smartPeakLabel;
		// did we pick up the TMS signal
		double firstPoint = spectrum.arrayPointToUnits(peak.getPeakStart());
		double secondPoint = spectrum.arrayPointToUnits(peak.getPeakEnd());
		double alpha, beta, gamma, p;
		if ((firstPoint <= 0.0 && secondPoint >= 0.0) || (firstPoint >= 0.0 && secondPoint <= 0.0)) {
			return null;
		}

		smartPeakLabel = new SmartPeakLabel(Math.max(firstPoint, secondPoint), Math.min(firstPoint, secondPoint));
		if (peak.isSymmetric()) {
			int nSubPeaks = peak.getNbOfPeaks();
			for (int i = 0; i < nSubPeaks; i++) {
				int pos = (int) peak.getPeakLocation(i);
				alpha=20*Math.log10(spectrum.spectraData.getY(pos-1));
				beta=20*Math.log10(spectrum.spectraData.getY(pos));
				gamma=20*Math.log10(spectrum.spectraData.getY(pos+1));
				p=0.5*(alpha-gamma)/(alpha-2*beta+gamma);
				smartPeakLabel.addPeak(spectrum.arrayPointToUnits(pos+p));
				smartPeakLabel.setIntensity(smartPeakLabel.getNbPeaks() - 1,spectrum.spectraData.getY(pos)-0.25*
						(spectrum.spectraData.getY(pos-1)-spectrum.spectraData.getY(pos+1))*p);
			}
		}
		spectrum.addEntity(smartPeakLabel);
		if (smartPeakLabel.getNbPeaks() > 0) smartPeakLabel.compute();
		smartPeakLabel.checkSizeAndPosition();
		// add integrals
		if (spectrum.getNucleus() == Nucleus.NUC_1H && noIntegral == false) {
			Integral tempIntegral;

			firstPoint = spectrum.arrayPointToUnits(peak.getIntStart());
			secondPoint = spectrum.arrayPointToUnits(peak.getIntEnd());
			tempIntegral = IntegrationHelpers.addIntegral(Math.max(firstPoint, secondPoint), Math.min(firstPoint, secondPoint), spectrum);
			tempIntegral.checkSizeAndPosition();
			interactions.createLink(smartPeakLabel, tempIntegral);
		}
		return smartPeakLabel;
	}*/
	
	public static SmartPeakLabel addSmartPeakLabel(InteractiveSurface interactions, Spectra spectrum, NMRSignal1D nmrSignal) {
		return addSmartPeakLabel(interactions,spectrum,nmrSignal,false);
	}
	
	public static SmartPeakLabel addSmartPeakLabels(InteractiveSurface interactions, Spectra spectrum, Vector<NMRSignal1D> nmrSignals, boolean noIntegral) {
		SmartPeakLabel smartPeakLabel=null;
		for(NMRSignal1D nmrSignal: nmrSignals)
			smartPeakLabel=addSmartPeakLabel(interactions,spectrum,nmrSignal,noIntegral);
		return smartPeakLabel;
	}
	
	public static SmartPeakLabel addSmartPeakLabels(InteractiveSurface interactions, Spectra spectrum, NMRSignal1D[] nmrSignals, boolean noIntegral) {
		SmartPeakLabel smartPeakLabel=null;
		for(NMRSignal1D nmrSignal: nmrSignals){
			smartPeakLabel=addSmartPeakLabel(interactions,spectrum,nmrSignal,noIntegral);
		}
		return smartPeakLabel;
	}
	
	public static SmartPeakLabel addSmartPeakLabel(InteractiveSurface interactions, Spectra spectrum, NMRSignal1D nmrSignal, boolean noIntegral) {
		//System.out.println("addSmartPeakLabel1");
		double firstPoint = nmrSignal.getStartX();
		double secondPoint = nmrSignal.getEndX();

		if ((firstPoint <= 0.0 && secondPoint >= 0.0) || (firstPoint >= 0.0 && secondPoint <= 0.0)) {
			return null;
		}
		//System.out.println("addSmartPeakLabel2");
		GetNMRSignal1D.setIntensities(nmrSignal, spectrum.spectraData);		
		///System.out.println("addSmartPeakLabel3");
		SmartPeakLabel smartPeakLabel=new SmartPeakLabel(nmrSignal);
		//System.out.println("addSmartPeakLabel4");
		if (nmrSignal.getNbPeaks() > 0)
			nmrSignal.compute();
		if(nmrSignal.isAsymemtric())
			smartPeakLabel.setForceMassif(true);
		spectrum.addEntity(smartPeakLabel);
		smartPeakLabel.setInteractiveSurface(interactions);
		smartPeakLabel.checkSizeAndPosition();
		//System.out.println("addSmartPeakLabel6");
		// add integrals
		if (spectrum.getNucleus() == Nucleus.NUC_1H && noIntegral == false) {
			IntegralData integral = nmrSignal.getIntegralData();
			Integral tempIntegral=null;
			if(integral!=null){
				if(integral.getValue()>0){//First we try to use the integration from the NMRSignal1D
					tempIntegral=IntegrationHelpers.addIntegral(integral,spectrum);
				}
				else
					tempIntegral = IntegrationHelpers.addIntegral(Math.max(firstPoint, secondPoint), Math.min(firstPoint, secondPoint), spectrum);
			}
			else
				tempIntegral = IntegrationHelpers.addIntegral(Math.max(firstPoint, secondPoint), Math.min(firstPoint, secondPoint), spectrum);
			//System.out.println("addSmartPeakLabel7");
			tempIntegral.checkSizeAndPosition();
			//System.out.println("addSmartPeakLabel8");
			interactions.createLink(smartPeakLabel, tempIntegral);
			//System.out.println("addSmartPeakLabel9");
		}
		
		return smartPeakLabel;
	}
	
	public static boolean isNoise(Spectra spectra, double xLimit1, double xLimit2, double yLimit1, double yLimit2){

		int ap1 = spectra.unitsToArrayPoint(xLimit1);
		int ap2 = spectra.unitsToArrayPoint(xLimit2);
		int ss1 = spectra.unitsToSubSpectra(yLimit1);
		int ss2 = spectra.unitsToSubSpectra(yLimit2);

		int tempInt;

		if (ap1 > ap2) {
			tempInt = ap2;
			ap2 = ap1;
			ap1 = tempInt;
		}

		if (ss1 > ss2) {
			tempInt = ss2;
			ss2 = ss1;
			ss1 = tempInt;
		}

		double noiseLevel=spectra.getRobustNoiseLevel(), averageValue=0, val=0;
		
		int points=0;
		SpectraData spectraData = spectra.spectraData;
		//We can improve it. We don't need to calculate the average over all points.
		//Just a few are enough.
		for (int sub = ss1; sub <= ss2; sub++) {
			for (int point = ap1; point <= ap2; point++) {
				val=spectraData.getY(point, sub);
				if(val<0)
					val*=-1;
				averageValue+= val;
				points++;
			}
		}
		averageValue/=points;

		if(DEBUG){
			System.out.println("Analisyd 2D action Button: Robust noise level "+noiseLevel);
			System.out.println("Analisyd 2D action Button: Average abs. signal level "+averageValue);
		}
		//If the average value is in the noise level+1 std. deviation(1.33) I mean.
		if(averageValue<=noiseLevel*1.33)
			return true;
		return false;
	}

	public static void mergeIdenticalPeakLabels(Spectra spectrum) {
		BasicEntity e1,e2;
		int iEntity = 0;
		while(iEntity<spectrum.getEntitiesCount()){
			e1 = spectrum.getEntity(iEntity);
			boolean nextSmartPeakElement=true;
			
			if (e1 instanceof SmartPeakLabel) {
				SmartPeakLabel label1 = (SmartPeakLabel) e1;
				int jEntity = iEntity+1;
				while(jEntity<spectrum.getEntitiesCount()){
					e2 = spectrum.getEntity(jEntity);
					if (e2 instanceof SmartPeakLabel) {
						SmartPeakLabel label2 = (SmartPeakLabel) e2;

						/*boolean sameDiaID = false;
						
						Iterator<String> iterator = label2.getDiastereotopicIDs().iterator();
						while(iterator.hasNext()){
							sameDiaID=label1.getDiastereotopicIDs().contains(iterator.next());
							if(sameDiaID)
								break;
						}
						System.out.println(label1.getDiastereotopicIDs());
						System.out.println(sameDiaID);
						System.out.println(label2.getDiastereotopicIDs());*/
						if(isEqual(label1,label2,0.0001)){
							//label1.getNmrSignal1D().setPublicationAssignment(label1.getNmrSignal1D().getPublicationAssignment()+","+label2.getNmrSignal1D().getPublicationAssignment());
							label1.getNmrSignal1D().joinWith(label2.getNmrSignal1D());
							spectrum.remove(jEntity--);
						}
						else{
							if(nextSmartPeakElement){
								iEntity=jEntity-1;
								nextSmartPeakElement=false;
							}
						}
					}
					jEntity++;
				}
			}
			iEntity++;
		}	
	}
	/**
	 * This function test if peak1 is equal to peak2
	 * @param peak1
	 * @param peak2
	 * @param tol	Tolerance to error
	 * @return true if all the peaks in peak1 are similar to peaks in peak2. false a.c.
	 */
	private static boolean isEqual(SmartPeakLabel peak1,SmartPeakLabel peak2, double tol){
		return peak1.getNmrSignal1D().isMatched(peak2.getNmrSignal1D(), tol);
	}
	/**
	 * This function test if peak1 is equal to peak2
	 * @param peak1
	 * @param peak2
	 * @return true if all the peaks in peak1 are equal to peaks in peak2. false a.c.
	 */
	public static boolean isEqual(SmartPeakLabel peak1,SmartPeakLabel peak2){
		return isEqual(peak1,peak2,0);
	}
}