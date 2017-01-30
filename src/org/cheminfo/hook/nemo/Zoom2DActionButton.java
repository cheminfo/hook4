package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicDisplay;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.framework.SelectActionButton;

public class Zoom2DActionButton extends DefaultActionButton {


	/**
	 * 
	 */
	private static final long serialVersionUID = -4398735783793347224L;
	private Vector previousActiveEntities = null;
	private double firstSelLimitH;
	private double firstSelLimitV;
	private Spectra currentSpectrum=null;

	public Zoom2DActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON, new char[] {'z','Z'});
	}

	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);
	}

	
	protected void handleWheelEvent(MouseWheelEvent ev) {
		VZoomActionButton button=(org.cheminfo.hook.nemo.VZoomActionButton)interactions.getButtonByClassName("org.cheminfo.hook.nemo.VZoomActionButton");
		if (button!=null) {
			button.handleWheelEvent(ev);
		}
	}
	
	
	protected void handleEvent(MouseEvent ev) {
		super.handleEvent(ev);

		if (interactions.getCurrentAction() == this) {
			switch (ev.getID()) {
			case MouseEvent.MOUSE_CLICKED:
				if(ev.getClickCount()==2){
					if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
						SpectraDisplay spectraDisplay=((SpectraDisplay)interactions.getActiveDisplay());
						if (spectraDisplay.is2D()) {
							// we will change to a 1D spectrum ... and hide all the other spectra
							Vector<BasicEntity> activeEntities=interactions.getActiveEntities();
							if ((activeEntities!=null) && (activeEntities.size()==1)) {
								if (activeEntities.get(0) instanceof Spectra) {
									Spectra selectedSpectrum=(Spectra)activeEntities.get(0);
									if (! selectedSpectrum.isDrawnAs2D() && !selectedSpectrum.isVertical()) {
										selectedSpectrum.isVertical(false);
										selectedSpectrum.setLocation(selectedSpectrum.getLocation().x, -10);
										for (Spectra tmpSpectra : spectraDisplay.getAllSpectra()) {
											if (! tmpSpectra.equals(selectedSpectrum)) {
												tmpSpectra.setVisible(false);
												//tmpSpectra.setPrimaryColor(null);
												//tmpSpectra.setSecondaryColor(null);
												spectraDisplay.set2D(false);
												spectraDisplay.setVScale(false);
											}
										}
										spectraDisplay.checkSizeAndPosition();
										interactions.checkButtonsStatus();
										interactions.repaint();
										return;
									}
								}
							}
						}
						Vector<BasicEntity> entities = interactions.getActiveDisplay().getEntities();
						int index=0;
						for(int i=0;i<entities.size();i++){
							if(entities.get(i) instanceof Spectra){
									((Spectra)entities.get(i)).setMultFactor(0.9);
									Point2D.Double actualLocation = ((Spectra)entities.get(i)).getLocation();
									((Spectra)entities.get(i)).setLocation(actualLocation.x,actualLocation.y);//-10-(index++)*20);
							}
						}
						interactions.takeUndoSnapshot();
						spectraDisplay.fullSpectra();
						spectraDisplay.checkSizeAndPosition();
						interactions.checkButtonsStatus();
						interactions.repaint();
					}
					
				}
				else{
					if (ev.isShiftDown()) {
						if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
							SpectraDisplay activeDisplay = (SpectraDisplay) interactions.getActiveDisplay();
		
							double fulloutBottomLimit = activeDisplay.getFulloutBottomLimit();
							double fulloutTopLimit = activeDisplay.getFulloutTopLimit();
							double fulloutRightLimit = activeDisplay.getFulloutRightLimit();
							double fulloutLeftLimit = activeDisplay.getFulloutLeftLimit();
		
							double currentBottomLimit = activeDisplay.getCurrentBottomLimit();
							double currentTopLimit = activeDisplay.getCurrentTopLimit();
							double currentRightLimit = activeDisplay.getCurrentRightLimit();
							double currentLeftLimit = activeDisplay.getCurrentLeftLimit();
		
							double xCenter = (currentRightLimit + currentLeftLimit) / 2;
							double yCenter = (currentBottomLimit + currentTopLimit) / 2;
		
							double scale = 1.5;
							double newBottomLimit = yCenter + scale
									* (currentBottomLimit - yCenter);
							double newTopLimit = yCenter + scale
									* (currentTopLimit - yCenter);
							double newLeftLimit = xCenter + scale
									* (currentLeftLimit - xCenter);
							double newRightLimit = xCenter + scale
									* (currentRightLimit - xCenter);
		
							if (!((newBottomLimit > fulloutBottomLimit && newBottomLimit < fulloutTopLimit) || (newBottomLimit < fulloutBottomLimit && newBottomLimit > fulloutTopLimit)))
								newBottomLimit = fulloutBottomLimit;
		
							if (!((newTopLimit > fulloutBottomLimit && newTopLimit < fulloutTopLimit) || (newTopLimit < fulloutBottomLimit && newTopLimit > fulloutTopLimit)))
								newTopLimit = fulloutTopLimit;
		
							if (!((newRightLimit > fulloutRightLimit && newRightLimit < fulloutLeftLimit) || (newRightLimit < fulloutRightLimit && newRightLimit > fulloutLeftLimit)))
								newRightLimit = fulloutRightLimit;
		
							if (!((newLeftLimit > fulloutRightLimit && newLeftLimit < fulloutLeftLimit) || (newLeftLimit < fulloutRightLimit && newLeftLimit > fulloutLeftLimit)))
								newLeftLimit = fulloutLeftLimit;
		
							activeDisplay.setCurrentLimits(newLeftLimit, newRightLimit,
									newTopLimit, newBottomLimit);
							activeDisplay.setCursorType(BasicDisplay.NONE);
							activeDisplay.checkSizeAndPosition();
		
							interactions.clearActiveEntities();
							// for (int ent=0; ent < this.previousActiveEntities.size();
							// ent++)
							// interactions.addActiveEntity((BasicEntity)this.previousActiveEntities.elementAt(ent));
		
							interactions.repaint();
		
						}
					} else {
						SelectActionButton selectButton=(SelectActionButton)interactions.getButtonByClassName("org.cheminfo.hook.framework.SelectActionButton");
						if (selectButton!=null) {
							selectButton.mouseClicked(ev);
						}
					}
				}
				break;
			case MouseEvent.MOUSE_PRESSED:
				SpectraDisplay activeDisplay=null;
				//BasicEntity entity=interactions.getOverEntity();
				BasicEntity entity=interactions.getActiveEntity();
				
				if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
					activeDisplay=(SpectraDisplay)interactions.getActiveDisplay();
				} else if (entity instanceof SpectraDisplay) {
					activeDisplay = (SpectraDisplay) entity;
				} else if (entity instanceof Spectra) {
					activeDisplay = (SpectraDisplay) entity.getParentEntity();
				} else if (entity instanceof SpectraObject) {
					activeDisplay = (SpectraDisplay) entity.getParentEntity().getParentEntity();
				}

				if (activeDisplay != null) {
					if (entity instanceof Spectra) {
						currentSpectrum=(Spectra)entity;
					} else if (entity!=null && entity.getParentEntity() instanceof Spectra) {
						currentSpectrum=(Spectra)entity.getParentEntity();
					} else {
						currentSpectrum=activeDisplay.getFirstSpectra();
					}
					this.previousActiveEntities = (Vector)interactions.getActiveEntities().clone();
					interactions.setActiveEntity(activeDisplay);
					interactions.takeUndoSnapshot();
					firstSelLimitH = activeDisplay.absolutePixelsToUnitsH(interactions.getContactPoint().x);
					firstSelLimitV = activeDisplay.absolutePixelsToUnitsV(interactions.getContactPoint().y);
					activeDisplay.setSelectionLimits(firstSelLimitH, firstSelLimitH, firstSelLimitV, firstSelLimitV);
					activeDisplay.setCursorType(BasicDisplay.RECT);
				}
				break;

			case MouseEvent.MOUSE_RELEASED:
				//To select the over entity as the active entity
				//System.out.println(interactions.getOverEntity());
				if(interactions.getOverEntity() instanceof Spectra){
					Spectra tmp =  (Spectra)interactions.getOverEntity();
					if(tmp.isDrawnAs2D())
						currentSpectrum = tmp;
				}
				//interactions.setActiveEntity(interactions
				//		.getOverEntity());
				if (interactions.getActiveEntity() instanceof SpectraDisplay) {
					double leftSelection, rightSelection, topSelection, bottomSelection;

					activeDisplay = (SpectraDisplay) interactions.getActiveDisplay();

					if (interactions.getReleasePoint().x < interactions.getContactPoint().x) {
						leftSelection = activeDisplay.absolutePixelsToUnitsH(interactions.getReleasePoint().x);
						rightSelection = activeDisplay.absolutePixelsToUnitsH(interactions.getContactPoint().x);
					} else {
						rightSelection = activeDisplay.absolutePixelsToUnitsH(interactions.getReleasePoint().x);
						leftSelection = activeDisplay.absolutePixelsToUnitsH(interactions.getContactPoint().x);
					}

					if (interactions.getReleasePoint().y < interactions.getContactPoint().y) {
						topSelection = activeDisplay.absolutePixelsToUnitsV(interactions.getReleasePoint().y);
						bottomSelection = activeDisplay.absolutePixelsToUnitsV(interactions.getContactPoint().y);
					} else {
						bottomSelection = activeDisplay.absolutePixelsToUnitsV(interactions.getReleasePoint().y);
						topSelection = activeDisplay.absolutePixelsToUnitsV(interactions.getContactPoint().y);
					}

					if (Math.abs(interactions.getReleasePoint().x - interactions.getContactPoint().x) > 0 && Math.abs(interactions.getReleasePoint().y - interactions.getContactPoint().y) >= 0) {
						// We drew correctly a rectangle.
						// What is the over spectrum ? 1D or 2D ?
						
						if (currentSpectrum!=null) {
								if (currentSpectrum.isDrawnAs2D()) {
									//System.out.println("2D selected");
									activeDisplay.setCurrentLimits(leftSelection,rightSelection, topSelection, bottomSelection);
									Spectra horSpectrum = activeDisplay.getHorRefSpectrum();
									if (horSpectrum != null) {
										horSpectrum.setCurrentLimits(leftSelection, rightSelection);
									}
									Spectra verSpectrum = activeDisplay.getVerRefSpectrum();
									if (verSpectrum != null) {
										verSpectrum.setCurrentLimits(bottomSelection, topSelection);
									}
							} else { // it is a 1D display
								if (currentSpectrum.isVertical()) {
									activeDisplay.setCurrentLimits(activeDisplay.getCurrentLeftLimit(),activeDisplay.getCurrentRightLimit(), topSelection, bottomSelection);
								} else {
									//System.out.println("1D selected");
									activeDisplay.setCurrentLimits(leftSelection, rightSelection,topSelection, bottomSelection);
								}
								// we need to scale vertically
								Point2D.Double invertedCPoint = new Point2D.Double();
								Point2D.Double invertedRPoint = new Point2D.Double();
								try {
									AffineTransform inverseTransform = currentSpectrum.getGlobalTransform().createInverse();
									inverseTransform.transform(interactions.getContactPoint(), invertedCPoint);
									inverseTransform.transform(interactions.getReleasePoint(), invertedRPoint);
								} catch (Exception e) {
									System.out.println("transform not invertable");
								}
								//It just reasize vertically the current spectrum
								Point2D.Double actualLocation = currentSpectrum.getLocation();
								double factor=1;
								double verticalShift = actualLocation.y;
								if (Math.abs(invertedCPoint.y-invertedRPoint.y)>10) {
									if(ev.isShiftDown()){
										factor=currentSpectrum.getHeight()/Math.abs(invertedCPoint.y-invertedRPoint.y);
										verticalShift=(currentSpectrum.getHeight()-Math.max(invertedCPoint.y,invertedRPoint.y)+actualLocation.y)*(factor/currentSpectrum.getMultFactor());
										
									}
									else
										factor=(currentSpectrum.getHeight() / (currentSpectrum.getHeight() - invertedCPoint.y));
									currentSpectrum.setLocation(actualLocation.x,verticalShift);
									currentSpectrum.setMultFactor(currentSpectrum.getMultFactor() * factor);
								}
								//It will resize all the spectra
								/*Vector entities = activeDisplay.getEntities();
								for(int i=entities.size()-1;i>=0;i--){
									if(entities.get(i) instanceof Spectra){
										//System.out.println("verticalShift "+verticalShift);
											
											((Spectra)entities.get(i)).setMultFactor(currentSpectrum.getMultFactor() * factor);
											Point2D.Double actualLocation = ((Spectra)entities.get(i)).getLocation();
											((Spectra)entities.get(i)).setLocation(actualLocation.x,verticalShift+actualLocation.y);
									}
								}*/
										
							}
						} else {
							activeDisplay.setSelectionLimits(9999999, 9999999);
						}

					} else {
						activeDisplay.setSelectionLimits(9999999, 9999999);
					}
					activeDisplay.setCursorType(BasicDisplay.NONE);
					activeDisplay.checkSizeAndPosition();

					interactions.clearActiveEntities();
					for (int ent = 0; ent < this.previousActiveEntities.size(); ent++) {
						interactions.addActiveEntity((BasicEntity) this.previousActiveEntities.elementAt(ent));
					}

					interactions.repaint();
				}
				break;

			case MouseEvent.MOUSE_DRAGGED:
				if (interactions.getActiveEntity() instanceof SpectraDisplay) {
					// Point tempPoint=tempSpectra.getContactPoint();
					// tempPoint=tempSpectra.getReleasePoint();

					// SpectraDisplay
					// activeDisplay=(SpectraDisplay)interactions.getActiveDisplay();
					// secondSelectionLimit=activeDisplay.pixelsToUnitsH(interactions.getReleasePoint().x);
					// activeDisplay.setSelectionLimits(firstSelLimit,
					// secondSelLimit);
					interactions.repaint();
				}
				break;

			default:
				break;
			}
		}
	}

	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() != null && interactions.getActiveDisplay() instanceof SpectraDisplay
													&& (! ((SpectraDisplay)interactions.getActiveDisplay()).isAbsoluteYScale())) {
			this.activate();
		} else {
			this.deactivate();
		}
	}
	
}
