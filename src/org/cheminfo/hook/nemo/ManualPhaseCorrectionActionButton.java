package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Vector;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.math.util.MathUtils;
import org.cheminfo.hook.nemo.filters.BaselineCorrectionHWFilter;
import org.cheminfo.hook.nemo.filters.BaselineCorrectionWFilter;
import org.cheminfo.hook.nemo.filters.FilterType;
import org.cheminfo.hook.nemo.filters.FourierTransformFilter;
import org.cheminfo.hook.nemo.filters.PhaseCorrectionFilter;
import org.cheminfo.hook.nemo.filters.PostFourierTransformFilter;
import org.cheminfo.hook.nemo.filters.SpectraFilter;
import org.cheminfo.hook.util.XMLCoDec;

public class ManualPhaseCorrectionActionButton extends DefaultActionButton {

	private PeakLabel tempPeakLabel = null;
	private Spectra workSpectrum = null;
	private PhaseCorrectionFilter workingFilter = null;
	private int pivotIndex = -1;
	private int nbPoints = -1;
	private double lastEvaluationPoint = Double.NEGATIVE_INFINITY;
	private long lastEvaluationTime;

	private final static int STATE_UNDEF = 1;
	private final static int STATE_SELECT_PIVOT = 2;
	private final static int STATE_PHASE = 3;

	private int manualState = STATE_UNDEF;

	public ManualPhaseCorrectionActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON);

		this.tempPeakLabel = new PeakLabel(0.0);
		this.tempPeakLabel.setContracted(true);
		// this.shortcutKey="P";
	}

	protected void performInstantAction() {
		if (this.workSpectrum != null
				&& interactions.getCurrentAction() != this)
			this.cleanUp();
		interactions.setCurrentAction(this);
		interactions.requestFocus();
		Spectra spectrum = null;
		if (interactions.getActiveEntity() instanceof Spectra) {
			spectrum = (Spectra) interactions.getActiveEntity();
		} else if (interactions.getActiveDisplay() instanceof SpectraDisplay
				&& ((SpectraDisplay) interactions.getActiveDisplay())
						.getLastSpectra() != null) {
			spectrum = ((SpectraDisplay) interactions.getActiveDisplay())
					.getLastSpectra();
		}

		if (spectrum != null) {
			// button manual
			XMLCoDec buttonCodecManual = new XMLCoDec();
			buttonCodecManual.addParameter("action",
					"org.cheminfo.hook.nemo.BiggestPivotAction");
			buttonCodecManual.addParameter("image", "validate.gif");
			interactions.getUserDialog().setText(
					"<Text type=\"plain\">Select biggest pivot: </Text>"
							+ "<Button " + buttonCodecManual.encodeParameters()
							+ "></Button>");
			this.setManual();
			spectrum.refreshSensitiveArea();
			spectrum.checkSizeAndPosition();
			interactions.repaint();
			return;
		}
	}

	protected void handleKeyEvent(KeyEvent ev) {
		// if (ev.getID() == KeyEvent.KEY_RELEASED) {
		// SpectraDisplay display = null;
		// if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
		// display = (SpectraDisplay) interactions.getActiveDisplay();
		// }
		// Spectra tempSpectra = null;
		// if (display == null) {
		// if (interactions.getActiveEntity() instanceof Spectra)
		// tempSpectra = (Spectra) interactions.getActiveEntity();
		// if (tempSpectra != null)
		// display = (SpectraDisplay) tempSpectra.getParentEntity();
		// if (display == null)
		// return;
		// }
		// switch (ev.getKeyCode()) {
		// case KeyEvent.VK_SPACE:
		// this.switchState();
		// break;
		// }
		// display.checkSizeAndPosition();
		// display.refreshSensitiveArea();
		// interactions.repaint();
		// }
	}

	protected void handleEvent(MouseEvent ev) {
		if (this.manualState == ManualPhaseCorrectionActionButton.STATE_UNDEF)
			return;
		if (interactions.getCurrentAction() == this) {
			Spectra tempSpectra = null;
			if (interactions.getActiveEntity() instanceof Spectra)
				tempSpectra = (Spectra) interactions.getActiveEntity();
			if (tempSpectra == null)
				return;
			SpectraDisplay parentDisplay = (SpectraDisplay) tempSpectra
					.getParentEntity();
			switch (ev.getID()) {
			case MouseEvent.MOUSE_EXITED:
				if (tempSpectra != null
						&& tempSpectra.containsEntity(this.tempPeakLabel)) {
					tempSpectra.remove(this.tempPeakLabel);
					tempSpectra.checkSizeAndPosition();
					tempSpectra.refreshSensitiveArea();
					this.tempPeakLabel.checkSizeAndPosition();
					this.tempPeakLabel.refreshSensitiveArea();
					interactions.repaint();
				}
				break;
			case MouseEvent.MOUSE_ENTERED:
				if (this.manualState == ManualPhaseCorrectionActionButton.STATE_SELECT_PIVOT)
					this.tempPeakLabel.setXPos(tempSpectra
							.pixelsToUnits(interactions.getContactPoint().x));
				tempSpectra.addEntity(this.tempPeakLabel);
				this.tempPeakLabel.checkSizeAndPosition();
				this.tempPeakLabel.refreshSensitiveArea();
				break;
			case MouseEvent.MOUSE_PRESSED:
				this.lastEvaluationPoint = interactions.getContactPoint().x;
				this.lastEvaluationTime = System.currentTimeMillis();
				break;
			case MouseEvent.MOUSE_CLICKED:
				if (Math.abs(interactions.getReleasePoint().x
						- interactions.getContactPoint().x) < 1) {
					if (this.manualState == ManualPhaseCorrectionActionButton.STATE_SELECT_PIVOT) {
						this.nbPoints = tempSpectra.getSpectraData()
								.getNbPoints();
						// this.pivotIndex = this.nbPoints
						// - (int) tempSpectra
						// .unitsToArrayPoint(tempPeakLabel
						// .getXPosUnits()) - 1;
						this.pivotIndex = (int) tempSpectra
								.unitsToArrayPoint(tempPeakLabel.getXPosUnits());
						this.setPhaseState();
					} else if (this.manualState == ManualPhaseCorrectionActionButton.STATE_PHASE) {
						this.manualState = ManualPhaseCorrectionActionButton.STATE_SELECT_PIVOT;
						this.tempPeakLabel
								.setXPos(tempSpectra.pixelsToUnits(interactions
										.getReleasePoint().x));
						XMLCoDec buttonCodecManual = new XMLCoDec();
						buttonCodecManual.addParameter("action",
								"org.cheminfo.hook.nemo.BiggestPivotAction");
						buttonCodecManual.addParameter("image", "validate.gif");
						interactions.getUserDialog().setText(
								"<Text type=\"plain\">Select biggest pivot: </Text>"
										+ "<Button "
										+ buttonCodecManual.encodeParameters()
										+ "></Button>");

					}
					interactions.setCurrentAction(this);
					interactions.requestFocus();
					interactions.repaint();
				}
				break;
			case MouseEvent.MOUSE_MOVED:
				if (this.manualState == ManualPhaseCorrectionActionButton.STATE_SELECT_PIVOT) {
					int tempArrayPoint;
					if (this.tempPeakLabel != null
							&& tempSpectra.containsEntity(tempPeakLabel))
						tempSpectra.remove(tempPeakLabel);

					// // this part brings the absolute coordinates (in
					// interactions' space) to the spectra space through
					// the inverse of the global transform
					Point2D invertedPoint = new Point2D.Double();
					try {
						AffineTransform inverseTransform = tempSpectra
								.getGlobalTransform().createInverse();
						inverseTransform.transform(interactions
								.getContactPoint(), invertedPoint);
					} catch (Exception e) {
						System.out.println("transform not invertable");
					}

					double searchCenter = tempSpectra
							.pixelsToUnits(invertedPoint.getX());
					double range = parentDisplay.unitsPerPixelH() * 5;
					tempArrayPoint = PeakPickingHelpers.findPeakInRange(
							searchCenter, range, tempSpectra, false);
					// double
					// peakAt=tempSpectra.arrayPointToUnits(tempArrayPoint);
					this.tempPeakLabel.setXPos(tempSpectra
							.arrayPointToUnits(tempArrayPoint));
					tempSpectra.addEntity(this.tempPeakLabel);
					// tempPeakLabel.setContracted(true);

					interactions.getUserDialog().setMessageText(
							this.tempPeakLabel.getOverMessage());

					tempPeakLabel.checkSizeAndPosition();
					interactions.repaint();
				}
				break;
			case MouseEvent.MOUSE_DRAGGED:
				long deltaT;
				if (this.manualState == ManualPhaseCorrectionActionButton.STATE_PHASE) {
					if (ev.isShiftDown()) {
						deltaT = System.currentTimeMillis()
								- this.lastEvaluationTime;
						double deltaPhiOne = (interactions.getReleasePoint().x - this.lastEvaluationPoint)
								* 1 / (tempSpectra.getMultFactor()*(deltaT + 1));
						this.lastEvaluationPoint = interactions
								.getReleasePoint().x;
						this.lastEvaluationTime = System.currentTimeMillis();
						double phiZero = 0.0 - (deltaPhiOne * this.pivotIndex)
								/ this.nbPoints;
						this.workingFilter.addCorrection(phiZero, deltaPhiOne);
						this.workingFilter.apply(this.workSpectrum
								.getSpectraData());
						tempSpectra.checkSizeAndPosition();
						tempSpectra.refreshSensitiveArea();
						interactions.repaint();

					} else {
						//System.out.println("Mul factor: "+tempSpectra.getMultFactor());
						deltaT = System.currentTimeMillis()
								- this.lastEvaluationTime;
						double deltaPhiZero = (interactions.getReleasePoint().x - this.lastEvaluationPoint)
								* 1 /(tempSpectra.getMultFactor()*(deltaT + 1));
						this.lastEvaluationPoint = interactions
								.getReleasePoint().x;
						this.lastEvaluationTime = System.currentTimeMillis();
						this.workingFilter.addCorrection(deltaPhiZero, 0.0);
						this.workingFilter.apply(this.workSpectrum
								.getSpectraData());
						tempSpectra.checkSizeAndPosition();
						tempSpectra.refreshSensitiveArea();
						interactions.repaint();
					}
				}
				break;
			}
		}
	}

	protected void checkButtonStatus() {
		Spectra spectrum = null;
		if (interactions.getActiveEntity() instanceof Spectra)
			spectrum = (Spectra) interactions.getActiveEntity();
		if (spectrum == null
				&& interactions.getActiveDisplay() instanceof SpectraDisplay) {
			SpectraDisplay display = (SpectraDisplay) interactions
					.getActiveDisplay();
			spectrum = display.getFirstSpectra();
		}
		if (spectrum == null) {
			this.deactivate();
		} else {
			SpectraData spectraData = spectrum.getSpectraData();
			/*if (spectraData.getDataType() == SpectraData.TYPE_NMR_SPECTRUM
					&& spectraData.getNbSubSpectra() > 1) {
				Vector<SpectraFilter> appliedFilters = spectraData
						.getAppliedFilters();
				if (appliedFilters.size() == 0) {
					this.activate();
				} else {
					SpectraFilter lastFilter = appliedFilters.lastElement();
					if (lastFilter instanceof FFTFilter
							|| lastFilter instanceof PhaseCorrectionFilter) {
						this.activate();
					} else {
						this.deactivate();
					}
				}
			} else {
				this.deactivate();
			}*/
			if ((spectraData.getDataType() == SpectraData.TYPE_NMR_SPECTRUM 
					|| spectraData.getDataType()==SpectraData.TYPE_2DNMR_SPECTRUM)
					&& spectraData.getNbSubSpectra() > 1) {
				Vector<SpectraFilter> appliedFilters = spectraData
						.getAppliedFilters();
				if (appliedFilters.size() == 0) {
					this.activate();
				} else {
					SpectraFilter lastFilter = appliedFilters.lastElement();
					if (lastFilter instanceof FourierTransformFilter
							|| lastFilter instanceof PhaseCorrectionFilter
							|| lastFilter instanceof BaselineCorrectionHWFilter
							|| lastFilter instanceof BaselineCorrectionWFilter
							|| lastFilter instanceof PostFourierTransformFilter) {
						this.activate();
					} else {
						this.deactivate();
					}
				}
			} else {
				this.deactivate();
			}
		}
	}

	public void cleanUp() {
		if (this.tempPeakLabel != null && this.workSpectrum != null)
			this.workSpectrum.remove(this.tempPeakLabel);
		this.workingFilter = null;
		this.workSpectrum = null;

		interactions.setCurrentAction(null);
		interactions.getUserDialog().setText("");
	}

	public void setManual() {
		Spectra spectrum = null;
		if (interactions.getActiveDisplay() instanceof SpectraDisplay
				&& ((SpectraDisplay) interactions.getActiveDisplay())
						.getLastSpectra() != null) {
			spectrum = ((SpectraDisplay) interactions.getActiveDisplay())
					.getLastSpectra();
		}
		if (spectrum == null)
			return;
		this.workSpectrum = spectrum;
		SpectraFilter filter = this.workSpectrum.getSpectraData().getAppliedFilterByFilterType(FilterType.PHASE_CORRECTION);
		if (filter == null) {
			PhaseCorrectionFilter phaseFilter = new PhaseCorrectionFilter();
			phaseFilter.addCorrection(0, 0);
			SpectraData spectraData = this.workSpectrum.getSpectraData();
			spectraData.applyFilter(phaseFilter);
			filter = phaseFilter;
		}
		this.workingFilter = (PhaseCorrectionFilter) filter;
		this.manualState = ManualPhaseCorrectionActionButton.STATE_SELECT_PIVOT;
		this.selectBiggestPivot();
		interactions.repaint();
	}

	public void selectBiggestPivot() {
		SpectraData spectraData = this.workSpectrum.getSpectraData();
		int pos = MathUtils.findGlobalMaximumIndex(spectraData
				.getSubSpectraDataY(0), 0, spectraData.getNbPoints());
		if (tempPeakLabel == null) {
			this.tempPeakLabel = new PeakLabel(this.workSpectrum
					.arrayPointToUnits(pos));
			this.tempPeakLabel.setContracted(true);
			this.workSpectrum.addEntity(this.tempPeakLabel);
		} else {
			this.tempPeakLabel
					.setXPos(this.workSpectrum.arrayPointToUnits(pos));
			if (!this.workSpectrum.containsEntity(this.tempPeakLabel))
				this.workSpectrum.addEntity(this.tempPeakLabel);
		}
		this.nbPoints = spectraData.getNbPoints();
		// this.pivotIndex = this.nbPoints - pos - 1;
		this.pivotIndex = pos;
		this.tempPeakLabel.checkSizeAndPosition();
		this.tempPeakLabel.refreshSensitiveArea();
		this.manualState = ManualPhaseCorrectionActionButton.STATE_PHASE;
		// button manual
		XMLCoDec buttonCodecManual = new XMLCoDec();
		buttonCodecManual.addParameter("action",
				"org.cheminfo.hook.nemo.PhaseCorrectionAllDoneAction");
		buttonCodecManual.addParameter("image", "validate.gif");

		interactions.getUserDialog().setText(
				"<Text type=\"plain\">Changing phi zero (Hold shift for phi one)| </Text>"
						+ "<Text type=\"plain\">All done: </Text>" + "<Button "
						+ buttonCodecManual.encodeParameters() + "></Button>");

		this.manualState = ManualPhaseCorrectionActionButton.STATE_PHASE;
		interactions.setCurrentAction(this);
		interactions.requestFocus();
		interactions.repaint();
	}

	private void setPhaseState() {
		this.manualState = ManualPhaseCorrectionActionButton.STATE_PHASE;
		XMLCoDec buttonCodecManual = new XMLCoDec();
		buttonCodecManual.addParameter("action",
				"org.cheminfo.hook.nemo.PhaseCorrectionAllDoneAction");
		buttonCodecManual.addParameter("image", "validate.gif");
		interactions.getUserDialog().setText(
				"<Text type=\"plain\">Changing phi zero (Hold shift for phi one)| </Text>"
						+ "<Text type=\"plain\">All done: </Text>" + "<Button "
						+ buttonCodecManual.encodeParameters() + "></Button>");

	}

}
