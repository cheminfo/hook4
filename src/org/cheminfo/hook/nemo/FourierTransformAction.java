package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.filters.FourierTransformFilter;
import org.cheminfo.hook.nemo.filters.PostFourierTransformFilter;
import org.cheminfo.hook.nemo.filters.ZeroFillingFilter;

public class FourierTransformAction extends GeneralAction {
	public static void performAction(InteractiveSurface interactions) {
		
		Spectra spectrum = null;
		if (interactions.getActiveEntity() instanceof Spectra) 
			spectrum = (Spectra)interactions.getActiveEntity();
		if (spectrum == null && interactions.getActiveDisplay() instanceof SpectraDisplay)
			spectrum = ((SpectraDisplay)interactions.getActiveEntity()).getLastSpectra();
		if (spectrum == null)
			return;
		SpectraData spectraData = spectrum.getSpectraData();
//		System.out.println("observeFrequency "+spectraData.getParamDouble("observeFrequency", 0));
		String value = interactions.getUserDialog().getParameter("zeroFilling");
		int zeroFilling = 1;
		if (value != null)
			zeroFilling = Integer.parseInt(value);
		int nbPoints = spectrum.getSpectraData().getNbPoints();
		zeroFilling = nbPoints * zeroFilling - nbPoints;
		//System.out.println("zeroFilling ffff "+zeroFilling);
		ZeroFillingFilter zeroFillingFilter = new ZeroFillingFilter(zeroFilling);
		if (zeroFillingFilter.isApplicable(spectraData) && zeroFilling != 0) {
			spectraData.applyFilter(zeroFillingFilter);
		}
		nbPoints = spectraData.getNbPoints();
		// check for a power of two
		double pow = Math.log(nbPoints) / Math.log(2);
		int neededPoints = (int)Math.round(Math.pow(2, Math.max(Math.round(pow), Math.ceil(pow))));
		if (neededPoints != nbPoints)
			spectraData.applyFilter(new ZeroFillingFilter(neededPoints - nbPoints));
		FourierTransformFilter fftFilter = new FourierTransformFilter();
		if (fftFilter.isApplicable(spectraData)) {
			spectraData.applyFilter(fftFilter);
			interactions.getUserDialog().setText("");
		} else {
			interactions.getUserDialog().setText("Not applicable");
			return;
		}

		PostFourierTransformFilter posfftFilter = new PostFourierTransformFilter();
		if (posfftFilter.isApplicable(spectraData)) {
			spectraData.applyFilter(posfftFilter);
		}
		/*
		BasicEntity parentEntity = spectrum.getParentEntity();
		if (parentEntity != null) {
			parentEntity.checkInteractiveSurface();
			spectrum.checkSizeAndPosition();
		}
		*/
		// reset the scales
		//FourierTransformAction.resetScales(spectrum);

		
		((SpectraDisplay)(interactions.getActiveDisplay())).checkAndRepaint();
	//	spectrum.refreshSensitiveArea();
	//	interactions.checkButtonsStatus();
	//	interactions.repaint();
		interactions.setActiveEntity(spectrum);
	}
	
	/*
	public static void resetScales(Spectra spectrum) {
		if (spectrum.getParentEntity() instanceof SpectraDisplay) {
			SpectraData spectraData = spectrum.getSpectraData();
			
			System.out.println("YYY"+spectraData.toString());
			
			SpectraDisplay display = (SpectraDisplay) spectrum.getParentEntity();
			display.setCurrentLimits(spectraData.getFirstX(), spectraData.getLastX());
			display.setFulloutRightLimit(spectraData.getLastX());
			display.setFulloutLeftLimit(spectraData.getFirstX());
			int nEntities = display.getEntitiesCount();
			boolean is2D = false;
			if (spectraData.getNbSubSpectra() > 2) is2D = true;
			for (int iEntity = 0; iEntity < nEntities; iEntity++) {
				if (display.getEntity(iEntity) instanceof HorizontalScale) {
					HorizontalScale scale = (HorizontalScale) display.getEntity(iEntity);
					scale.setCurrentLimits(spectraData.getFirstX(),spectraData.getLastX());
					scale.refreshSensitiveArea();
					scale.checkInteractiveSurface();
				}
				if (display.getEntity(iEntity) instanceof VerticalScale) {
					VerticalScale scale = (VerticalScale) display.getEntity(iEntity);
					if (is2D) {
						// Change the X axis according TopSpin_processing.pdf
						// get the reference/base frequency
						double baseFrequency = spectraData.getParamDouble("$BF2", Double.NaN);
						// TMS signal
						double spectralFrequency = spectraData.getParamDouble("$SFO2", Double.NaN);
						// System.out.println("spectralFrequency=" + spectralFrequency);
						double spectralWidth = spectraData.getParamDouble("$SW", Double.NaN);
						// System.out.println("spectralWidth=" + spectralWidth);
						double xMiddle = (spectralFrequency - baseFrequency) / baseFrequency* 1e6;
						double dx = 0.5 * spectralWidth * spectralFrequency / baseFrequency;
						double a = xMiddle-dx;
						double b = xMiddle+dx;
						scale.setCurrentLimits(Math.min(a, b), Math.max(a, b));
						spectraData.setParamDouble("firstY", b);
						spectraData.setParamDouble("lastY", a);
					} else {
						scale.setCurrentLimits(spectraData.getMinY(),spectraData.getMaxY());
					}
					
					scale.refreshSensitiveArea();
					scale.checkSizeAndPosition();
				}
			}
			if (is2D) {
				display.set2D(true);
				spectrum.drawAs2D();
				spectrum.clearContourLines();
				spectrum.generateContourLines(Spectra.DEFAULT_NB_CONTOURS);
			}
			display.checkFulloutLimits();
			display.fullSpectra();
			display.refreshSensitiveArea();
			display.checkSizeAndPosition();
		}
		
	}
	*/
	
	
}
