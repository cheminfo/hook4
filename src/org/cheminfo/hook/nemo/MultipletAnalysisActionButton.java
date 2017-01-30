package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.math.util.MathUtils;
import org.cheminfo.hook.nemo.nmr.MultiplicityAnalyzers;
import org.cheminfo.hook.optimizer.LevenbergMarquardt;
import org.cheminfo.hook.optimizer.LorentzianLinearCombination1D;

public class MultipletAnalysisActionButton extends DefaultActionButton {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public MultipletAnalysisActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON, new char[] {'m','M'});
	}
	
	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);
		if (interactions.getActiveDisplay() != null
				&& !(interactions.getActiveEntity() instanceof Spectra)) {
			if (((SpectraDisplay) interactions.getActiveDisplay())
					.getFirstSpectra() != null) {
				interactions.clearActiveEntities();
				interactions.addActiveEntity(((SpectraDisplay) interactions
						.getActiveDisplay()).getFirstSpectra());
				interactions.repaint();
			}
		} else if (interactions.getActiveDisplay() == null) {
			if (interactions.getRootDisplay() != null
					&& interactions.getRootDisplay() instanceof SpectraDisplay) {
				SpectraDisplay rootDisplay = (SpectraDisplay) interactions
						.getRootDisplay();
				if (rootDisplay.getFirstSpectra() != null)
					interactions.setActiveEntity(rootDisplay.getFirstSpectra());
			}
		}
	}

	protected void handleEvent(MouseEvent ev) {
		if (interactions.getCurrentAction() == this) {
			Spectra spectrum = null;
			// Try to obtain a handle on a peak label
			SmartPeakLabel currentPeakLabel = null;
			if (ev.getID() == MouseEvent.MOUSE_CLICKED) {
				BasicEntity overEntity = interactions.getOverEntity();
				if (overEntity instanceof SmartPeakLabel) {
					currentPeakLabel = (SmartPeakLabel) overEntity;
				} else if (overEntity instanceof Integral) {
					Vector<BasicEntity> linkedEntities = interactions
							.getLinkedEntities(overEntity);
					for (int i = 0; i < linkedEntities.size(); i++) {
						if (linkedEntities.get(i) instanceof SmartPeakLabel) {
							currentPeakLabel = (SmartPeakLabel) linkedEntities
									.get(i);
							break;
						}
					}
				}
				if (currentPeakLabel != null) {
//					MultipletAnalysisActionButton.fitLorentzians(currentPeakLabel);
					MultipletAnalysisActionButton.runACCAMethod(currentPeakLabel);
				}
			}
		}
	}

	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() instanceof SpectraDisplay)
			this.activate();
		else
			this.deactivate();
	}

	public static void runACCAMethod(SmartPeakLabel peakLabel) {
		System.out.println("starting analysis");
		Spectra spectrum = (Spectra)peakLabel.getParentEntity();
		SpectraData spectraData = (SpectraData)spectrum.getSpectraData();
		// multiplet detection
		double x0 = peakLabel.getNmrSignal1D().getStartX();
		double xn = peakLabel.getNmrSignal1D().getEndX();
		int i0 = spectrum.unitsToArrayPoint(x0);
		int in = spectrum.unitsToArrayPoint(xn);
		if (in < i0) {
			int tmp = i0;
			i0 = in;
			in = tmp;
		}

		double observeFrequency = spectraData.getParamDouble(
				"observeFrequency", 0);
		double deltaJ = 0.6; // Hz. see Magn. Reson. Chem. 2005, 43: 843-848
		double dx = spectraData.getInterval();
		System.out.println("spectral resolution: " + Math.abs(dx*observeFrequency));
		int iDeltaJ = (int)Math.abs(Math.ceil(0.6/(dx*observeFrequency)));
		System.out.println("iDeltaJ="+iDeltaJ);
		int nValues = in - i0 + 1;
		double[] list = new double[nValues];
		for (int i = 0; i < nValues; i++)
			list[i] = spectraData.getY(i0 + i);
		int[] couplingConstants = MultiplicityAnalyzers.accaMethod(
				list, 0, nValues,iDeltaJ);
		if (couplingConstants != null) {
			int nCouplings = couplingConstants.length;
			int[] couplings = new int[nCouplings];
			Arrays.fill(couplings, 0);
			int[] counts = new int[nCouplings];
			Arrays.fill(counts, 0);
			System.out.println("interval = "
					+ dx);
			for (int i = 0; i < nCouplings; i++) {
				System.out
						.println(i
								+ "\t"
								+ (couplingConstants[i]
										* spectraData.getInterval() * observeFrequency));
			}
		}
	}
	
	
	public static double[] fitLorentzians(SmartPeakLabel peakLabel) {
		System.out.println("Starting lorentzian fit!");
		double startX = peakLabel.getNmrSignal1D().getStartX();
		double stopX = peakLabel.getNmrSignal1D().getEndX();
		Spectra spectrum = (Spectra) peakLabel.getParentEntity();
		int ia = Math.min(spectrum.unitsToArrayPoint(startX), spectrum
				.unitsToArrayPoint(stopX));
		int ib = Math.max(spectrum.unitsToArrayPoint(startX), spectrum
				.unitsToArrayPoint(stopX));
		int nPoints = ib - ia + 1;
		int nPeaks = peakLabel.getNmrSignal1D().getNbPeaks();
		// test that enough points are around to do a statistic
		int nParameters = 3 * nPeaks + 1;
		if (nParameters > nPoints)
			return null;
		double[][] measuredData = new double[nPoints][2];
		SpectraData spectraData = spectrum.getSpectraData();
		double minValue = Double.MAX_VALUE;
		for (int i = ia; i <= ib; i++) {
			measuredData[i - ia][0] = spectrum.arrayPointToUnits(i);
			measuredData[i - ia][1] = spectraData.getY(i);
		}
		double[] startParameters = new double[nParameters];
		minValue = 0;
		double average = 0.0;
		for (int i = 0; i < spectraData.getNbPoints(); i++)
			average += spectraData.getY(i);
		average /= spectraData.getNbPoints();

		minValue = 0;
		startParameters[nParameters - 1] = minValue;
		
		for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
			startParameters[3 * iPeak + 0] = peakLabel.getNmrSignal1D().getPeak(iPeak).getX();
			// temporarily save the array point in here
			startParameters[3 * iPeak + 1] = spectrum
					.unitsToArrayPoint(startParameters[3 * iPeak + 0]);
			startParameters[3 * iPeak + 2] = spectraData.getY(spectrum
					.unitsToArrayPoint(startParameters[3 * iPeak + 0]))
					- minValue;
		}
		System.out.println("minValue: " + minValue);
		// determine width at peak height. use a global value for a
		// start
		double dx = spectraData.getInterval();
		for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
			double peakPos = peakLabel.getNmrSignal1D().getPeak(0).getX();
			int iPeakPos = spectrum.unitsToArrayPoint(peakPos);
			int left = iPeakPos - 1;
			while (spectraData.getY(left + 1) >= spectraData.getY(left)
					&& spectraData.getY(left) >= 0.5 * startParameters[3 * iPeak + 2]) {
				left--;
			}
			double halfWidth = Math.max(Math.abs(dx * (left - iPeakPos)), Math
					.abs(1 * dx));
			//
			halfWidth = 0.01;
			startParameters[3 * iPeak + 1] = halfWidth;
			startParameters[3 * iPeak + 2] *= Math.PI * halfWidth;
			System.out.println("x0(" + iPeak + ")="
					+ startParameters[3 * iPeak + 0]);
			System.out.println("gamma(" + iPeak + ")="
					+ startParameters[3 * iPeak + 1]);
			System.out.println("C(" + iPeak + ")="
					+ startParameters[3 * iPeak + 2]);
		}
		
		
		LorentzianLinearCombination1D lc = new LorentzianLinearCombination1D(
				nPeaks);
		lc.setParameters(startParameters);
		{
			try {
				PrintStream printStream = new PrintStream("fitInput.dat");
				for (int i = 0; i < measuredData.length; i++) {
					printStream.println(measuredData[i][0] + "\t"
							+ measuredData[i][1] + "\t"
							+ lc.getValueAt(measuredData[i]));
				}
				printStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}

		LevenbergMarquardt lma = new LevenbergMarquardt();
		double[] resultingParameters = lma.doCalculation(lc, startParameters,
				measuredData);
		for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
			System.out.println("peak: " + iPeak);
			System.out.println("\tx0 = " + resultingParameters[3 * iPeak]);
			System.out.println("\tgamma = "
					+ resultingParameters[3 * iPeak + 1]);
			System.out.println("\tC = " + resultingParameters[3 * iPeak + 2]);
		}
		System.out.println("constant " + resultingParameters[nParameters - 1]);
		System.out.println("residual error: " + lma.getResidualError());
		double residual = 0.0;
		{
			try {
				lc.setParameters(resultingParameters);
				PrintStream printStream = new PrintStream("fitResult.dat");
				for (int i = 0; i < measuredData.length; i++) {
					printStream.println(measuredData[i][0] + "\t"
							+ measuredData[i][1] + "\t"
							+ lc.getValueAt(measuredData[i]));

					residual += Math.pow(measuredData[i][1]
							- lc.getValueAt(measuredData[i]), 2);
				}
				printStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}
		System.out.println("steps: " + lma.getNSteps());
		System.out.println("new res: " + Math.sqrt(residual));
		estimateHalfWidth(peakLabel);
		return resultingParameters;
	}

	
	
	public static double[] fitLorentziansNew(SmartPeakLabel peakLabel) {
		double[] parameterEstimates = estimateHalfWidth(peakLabel);
		double startX = peakLabel.getNmrSignal1D().getStartX();
		double stopX = peakLabel.getNmrSignal1D().getEndX();
		Spectra spectrum = (Spectra) peakLabel.getParentEntity();
		int ia = Math.min(spectrum.unitsToArrayPoint(startX), spectrum
				.unitsToArrayPoint(stopX));
		int ib = Math.max(spectrum.unitsToArrayPoint(startX), spectrum
				.unitsToArrayPoint(stopX));
		int nPoints = ib - ia + 1;
		int nPeaks = peakLabel.getNmrSignal1D().getNbPeaks();
		// test that enough points are around to do a statistic
		int nParameters = 3 * nPeaks + 1;
		if (nParameters > nPoints)
			return null;
		double[][] measuredData = new double[nPoints][2];
		SpectraData spectraData = spectrum.getSpectraData();
		for (int i = ia; i <= ib; i++) {
			measuredData[i - ia][0] = spectrum.arrayPointToUnits(i);
			measuredData[i - ia][1] = spectraData.getY(i);
		}
		double[] startParameters = new double[3*nPeaks+1];
		System.arraycopy(parameterEstimates, 0, startParameters, 0, parameterEstimates.length);
		startParameters[parameterEstimates.length] = 0;
		//
		LorentzianLinearCombination1D lc = new LorentzianLinearCombination1D(
				nPeaks);
		lc.setParameters(startParameters);
		LevenbergMarquardt lma = new LevenbergMarquardt();
		double[] resultingParameters = lma.doCalculation(lc, startParameters,
				measuredData);
		for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
			System.out.println("peak: " + iPeak);
			System.out.println("\tx0 = " + resultingParameters[3 * iPeak]);
			System.out.println("\tgamma = "
					+ resultingParameters[3 * iPeak + 1]);
			System.out.println("\tC = " + resultingParameters[3 * iPeak + 2]);
		}
		try {
			lc.setParameters(resultingParameters);
			PrintStream printStream = new PrintStream("fitResult.dat");
			for (int i = 0; i < measuredData.length; i++) {
				printStream.println(measuredData[i][0] + "\t"
						+ measuredData[i][1] + "\t"
						+ lc.getValueAt(measuredData[i]));
			}
			printStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		
		
		return null;
	}
	
	
	public static double[] estimateHalfWidth(SmartPeakLabel peakLabel) {
		double startX = peakLabel.getNmrSignal1D().getStartX();
		double stopX = peakLabel.getNmrSignal1D().getEndX();
		Spectra spectrum = (Spectra) peakLabel.getParentEntity();
		SpectraData spectraData = spectrum.getSpectraData();
		int a = Math.min(spectrum.unitsToArrayPoint(startX), spectrum
				.unitsToArrayPoint(stopX));
		int b = Math.max(spectrum.unitsToArrayPoint(startX), spectrum
				.unitsToArrayPoint(stopX));
		int nValues = b - a + 1;
		double[] xValues = new double[nValues];
		double[] yValues = new double[nValues];
		for (int i = a; i <= b; i++) {
			xValues[i - a] = spectrum.arrayPointToUnits(i);
			yValues[i - a] = spectraData.getY(i);
		}

		int nPeaks = peakLabel.getNmrSignal1D().getNbPeaks();
		double dx = spectraData.getInterval();
		int[] peakPositions = new int[nPeaks];
		for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
			peakPositions[iPeak] = (int) Math
					.abs((peakLabel.getNmrSignal1D().getPeak(iPeak).getX() - startX) / dx);
		}
		
		int xPos[] = new int[nPeaks + 1];
		double[] xCoords = new double[nPeaks + 1];
		double[] yCoords = new double[nPeaks + 1];
		xPos[0] = 0;
		xPos[nPeaks] = xValues.length;
		xCoords[0] = xValues[0];
		yCoords[0] = yValues[0];
		xCoords[nPeaks] = xValues[xValues.length - 1];
		yCoords[nPeaks] = yValues[yValues.length - 1];
		for (int iPeak = 0; iPeak < nPeaks - 1; iPeak++) {
			xPos[iPeak + 1] = MathUtils.findGlobalMinimumIndex(yValues,
					peakPositions[iPeak], peakPositions[iPeak + 1]);
			xCoords[iPeak + 1] = xValues[xPos[iPeak + 1]];
			yCoords[iPeak + 1] = xValues[xPos[iPeak + 1]];
		}

		double[][] measuredValues = new double[5][2];
		double[] startParameters = new double[4];
		
		LorentzianLinearCombination1D lor = new LorentzianLinearCombination1D(1);
		lor.setParameters(startParameters);
		int index;
		LevenbergMarquardt lma = new LevenbergMarquardt();
		double[] parameterEstimates = new double[3 * nPeaks];
		for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
			// start of the interval
			measuredValues[0][0] = xCoords[iPeak];
			measuredValues[0][1] = yCoords[iPeak];
			// end of the interval
			measuredValues[1][0] = xCoords[iPeak + 1];
			measuredValues[1][1] = yCoords[iPeak + 1];
			// middle between [start,peak] 
			index = (peakPositions[iPeak] - xPos[iPeak]) / 2 + xPos[iPeak];
			measuredValues[2][0] = xValues[index];
			measuredValues[2][1] = yValues[index];
			// middle between [peak,end]
			index = (xPos[iPeak+1] - peakPositions[iPeak]) / 2 + peakPositions[iPeak];
			measuredValues[3][0] = xValues[index];
			measuredValues[3][1] = yValues[index];
			// peakPosition
			measuredValues[4][0] = xValues[peakPositions[iPeak]];
			measuredValues[4][1] = yValues[peakPositions[iPeak]];

			//
			double gamma = 0.001;
			double x0 = measuredValues[4][0];
			double C = measuredValues[4][1];
			//
			startParameters[0] = x0;
			startParameters[1] = gamma;
			startParameters[2] = C;
			startParameters[3] = 0;
			//
			double[] values = lma.doCalculation(lor, startParameters,
					measuredValues);
			parameterEstimates[3*iPeak+0] = values[0];
			parameterEstimates[3*iPeak+1] = values[1];
			parameterEstimates[3*iPeak+2] = values[2];
			
			System.out.println("x0 = " + values[0]);
			System.out.println("gamma = " + values[1]);
			System.out.println("C = " + values[2]);
			System.out.println("offset = " + values[3]);
		}
		return parameterEstimates;
	}

}
