package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.cheminfo.hook.framework.BasicDisplay;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.nmr.IntegralData;
import org.cheminfo.hook.nemo.nmr.Nucleus;
import org.cheminfo.hook.util.XMLCoDec;

public class SmartPickingActionButton extends DefaultSpectraActionButton {


	private PeakLabel tempLabel;

	public SmartPickingActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON, new char[] {'s','S'});
	}

	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);

		if (interactions.getActiveDisplay() != null) {
			if (!(interactions.getActiveEntity() instanceof Spectra)) {
				interactions.setActiveEntity(((SpectraDisplay) interactions.getActiveDisplay()).getFirstSpectra());
				//interactions.takeUndoSnapshot();
				interactions.repaint();
			}
		}
	}

	protected void handleEvent(MouseEvent ev) {
		super.handleEvent(ev);

		if (interactions.getCurrentAction() == this) {
			Spectra tempSpectra = null;

			if (interactions.getOverEntity() instanceof SmartPeakLabel) {
				if (ev.getID() == MouseEvent.MOUSE_CLICKED) {
					SmartPeakLabel peakLabel = (SmartPeakLabel) interactions.getOverEntity();
					interactions.setActiveEntity(interactions.getOverEntity());
					interactions.getUserDialog().setText(interactions.getOverEntity().getClickedMessage());
					peakLabel.setEditableState();
					interactions.repaint();
					return;
				}
			} else if (!(interactions.getActiveEntity() instanceof Spectra)) {
				if (ev.getID() == MouseEvent.MOUSE_PRESSED) {
					SpectraDisplay parentDisplay = (SpectraDisplay) interactions.getActiveDisplay();
					interactions.setActiveEntity(parentDisplay.getFirstSpectra());
				}
			}else{
				if (interactions.getOverEntity() instanceof Integral) {
					if (ev.getID() == MouseEvent.MOUSE_CLICKED) {
						Integral integral = (Integral) interactions.getOverEntity();
						interactions.setActiveEntity(interactions.getOverEntity());
						interactions.getUserDialog().setText(interactions.getOverEntity().getClickedMessage());
						//integral.setEditableState();
						interactions.repaint();
						return;
					}
				}
			}
			
			if (interactions.getActiveEntity() instanceof Spectra) {
				tempSpectra = (Spectra) interactions.getActiveEntity();
				SpectraDisplay parentDisplay = (SpectraDisplay) tempSpectra.getParentEntity();

				Point2D.Double invertedCPoint = new Point2D.Double();
				Point2D.Double invertedRPoint = new Point2D.Double();
				try {
					AffineTransform inverseTransform = tempSpectra.getGlobalTransform().createInverse();
					inverseTransform.transform(interactions.getContactPoint(),invertedCPoint);
					inverseTransform.transform(interactions.getReleasePoint(),invertedRPoint);
				} catch (Exception e) {
					System.out.println("transform not invertable");
				}

				if (tempSpectra.isDrawnAs2D() == false) {
					int tempArrayPoint;

					switch (ev.getID()) {
					case MouseEvent.MOUSE_CLICKED:
						// Clicking on a non selected Spectra induces a
						// selection
						/*
						 * if (interactions.getActiveEntities().size() != 1 ||
						 * !interactions.getActiveEntities().contains(tempSpectra)) {
						 * interactions.clearActiveEntities();
						 * interactions.repaint(); }
						 */
						if (interactions.getOverEntity() instanceof Spectra && interactions.getOverEntity() != tempSpectra) {
							interactions.setActiveEntity(interactions.getOverEntity());
							tempSpectra = (Spectra) interactions.getOverEntity();
							interactions.repaint();
						}

						if (ev.isShiftDown()) {
							XMLCoDec buttonCodec1 = new XMLCoDec();
							buttonCodec1
									.addParameter("action",
											"org.cheminfo.hook.nemo.ManualSmartPeakAction");
							buttonCodec1.addParameter("image", "validate.gif");

							interactions.getUserDialog().setText(
									"<Text type=\"plain\">Validate Peaks  </Text><Button "
											+ buttonCodec1.encodeParameters()
											+ "></Button>");

							interactions.repaint();
						}
						break;

					case MouseEvent.MOUSE_PRESSED:
						parentDisplay.setCursorType(BasicDisplay.RECT);
						break;

					case MouseEvent.MOUSE_DRAGGED:
						interactions.repaint();
						break;

					case MouseEvent.MOUSE_MOVED:
						if (ev.isShiftDown()) {
							// int tempArrayPoint;

							// PeakLabel
							// tempPeakLabel=tempSpectra.getTempPeakLabel();

							// if (interactions.getActiveEntity() ==
							// tempSpectra)
							{
								if (this.tempLabel != null) {
									tempSpectra.remove(this.tempLabel);
									this.tempLabel = null;
								}

								double range = parentDisplay.unitsPerPixelH() * 5;
								tempArrayPoint = PeakPickingHelpers
										.findPeakInRange(
												tempSpectra
														.pixelsToUnits(invertedCPoint.x),
												range, tempSpectra, false);

								this.tempLabel = new PeakLabel(tempSpectra
										.arrayPointToUnits(tempArrayPoint));
								tempSpectra.addEntity(this.tempLabel);
								// tempSpectra.setTempPeakLabel(tempPeakLabel);
								this.tempLabel.setContracted(true);
							}
							parentDisplay.checkSizeAndPosition();
							interactions.repaint();
						} else {
							if (this.tempLabel != null) {
								if (tempSpectra.getEntity(tempSpectra
										.getEntitiesCount() - 1) == this.tempLabel) {
									tempSpectra.remove(this.tempLabel);
									this.tempLabel = null;

									interactions.repaint();
								}
							}
						}
						break;

					case MouseEvent.MOUSE_RELEASED:
						// Case when we pressed the SHIFT and we click on all the peaks composing the multiplet
						if (ev.isShiftDown()) {
							if ((Math.abs(invertedCPoint.x - invertedRPoint.x) < 5)
									&& (Math.abs(invertedCPoint.y
											- invertedRPoint.y) < 5)) {
								tempSpectra.addEntity(this.tempLabel);
								tempSpectra.addTempPeakLabel(this.tempLabel);
							} else {
								interactions.takeUndoSnapshot();

								Integral tempIntegral;
								SmartPeakLabel tempSmart;
								
								tempSmart = new SmartPeakLabel(tempSpectra.pixelsToUnits(Math.min(invertedCPoint.x,
												invertedRPoint.x)), tempSpectra.pixelsToUnits(Math.max(
												invertedCPoint.x,invertedRPoint.x)));

								if (tempSmart !=null)
									tempSmart.getNmrSignal1D().setNucleus(tempSpectra.getNucleus());
								
								if (tempSmart != null && tempSpectra.getNucleus() == Nucleus.NUC_1H) {
									tempIntegral = IntegrationHelpers.addIntegral(
													tempSpectra.pixelsToUnits(Math.max(invertedCPoint.x,invertedRPoint.x)),
													tempSpectra.pixelsToUnits(Math.min(invertedCPoint.x,invertedRPoint.x)),
													tempSpectra);
									tempSpectra.addEntity(tempSmart);
									interactions.createLink(tempSmart,
											tempIntegral);
								}

								if (this.tempLabel != null) {
									tempSpectra.remove(this.tempLabel);
									this.tempLabel = null;
								}
								interactions.getUserDialog().setText("");
								// interactions.takeUndoSnapshot();
							}

							parentDisplay.setCursorType(SpectraDisplay.NONE);
							parentDisplay.checkSizeAndPosition();
							interactions.repaint();
							
						} else {
						// SHIFT not pressed, we are need to find ourself where is the multiplet
							if ((Math.abs(invertedCPoint.x - invertedRPoint.x) > 3) || (Math.abs(invertedCPoint.y - invertedRPoint.y) > 3)) {
								interactions.takeUndoSnapshot();

								SmartPeakLabel tempLabel = SmartPickingHelpers.findSmartPeak(invertedCPoint, invertedRPoint, tempSpectra);
								
								if (tempLabel != null && tempSpectra.getNucleus() == Nucleus.NUC_1H) {
									Integral tempIntegral = IntegrationHelpers.addIntegral(
													tempSpectra.pixelsToUnits(Math
																	.max(invertedCPoint.x,invertedRPoint.x)),
													tempSpectra.pixelsToUnits(Math
																	.min(invertedCPoint.x,invertedRPoint.x)),
													tempSpectra);
									interactions.createLink(tempLabel,tempIntegral);
									tempLabel.getNmrSignal1D().setIntegralData(tempIntegral.getIntegralData());
								}
							}

							parentDisplay.setCursorType(BasicDisplay.NONE);
							parentDisplay.checkSizeAndPosition();
							interactions.repaint();
						}
						break;

					case MouseEvent.MOUSE_EXITED:
						if (tempSpectra.getSpectraData().isDataClassXY()) {
							if (tempSpectra.getEntitiesCount() > 0) {
								if (tempSpectra.getEntity(tempSpectra
										.getEntitiesCount() - 1) == this.tempLabel)
									tempSpectra.remove(this.tempLabel);
							}
						}
						break;

					default:
						break;
					}

				}
			} else if (ev.getSource() instanceof SmartPeakLabel) {
				SmartPeakLabel tempSmartPeakLabel = (SmartPeakLabel) ev.getSource();
				switch (ev.getID()) {
				case MouseEvent.MOUSE_CLICKED:
					interactions.clearActiveEntities();
					interactions.setActiveEntity(tempSmartPeakLabel);
					setUserDialogText(tempSmartPeakLabel.getClickedMessage());
					break;
				}

			}
		}
	}

	protected void checkButtonStatus() {
		activateIf1DNMR();
	}
}
