package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.framework.SelectActionButton;

public class VZoomActionButton extends DefaultActionButton {


	/**
	 * 
	 */
	private static final long serialVersionUID = 3766883327645034265L;

	public VZoomActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON, new char[] {'v','V'});
	}

	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);
	}

	
	
	private void scaleSpectra(double mouvement, boolean shift, boolean useMouvement) {
		if (shift) {
			mouvement*=10;
		}
		SpectraDisplay activeDisplay = (SpectraDisplay) interactions.getActiveDisplay();
		//System.out.println(activeDisplay.is2D());
		//activeDisplay.g
		BasicEntity currentActiveEntity=interactions.getActiveEntity();
		Vector<BasicEntity> activeEntities=new Vector<BasicEntity> ();
		if (currentActiveEntity!=null) {
			if (currentActiveEntity instanceof Spectra) {
				activeEntities=interactions.getActiveEntities();
			} else if (currentActiveEntity instanceof Integral) {
				activeEntities.add(currentActiveEntity);
			} else if (currentActiveEntity.getParentEntity() instanceof Spectra) {
				activeEntities.add(currentActiveEntity.getParentEntity());
			}
		}
		
		if (activeEntities.isEmpty() && interactions.getActiveDisplay() instanceof SpectraDisplay) {
			//System.out.println("As expected");
			Vector<Spectra> spectra=((SpectraDisplay) interactions.getActiveDisplay()).getAllSpectra();
			for (Spectra spectrum : spectra) {
				activeEntities.add(spectrum);
			}
		}
		
		for (BasicEntity activeEntity : activeEntities) {
			if (activeEntity instanceof Spectra) {
				Spectra tempSpectra = (Spectra) activeEntity;
				if (tempSpectra.isDrawnAs2D()) {
					if (tempSpectra.isSelected()) {
						// for counter line it is maximum one level at a time
						double currentScale=(double)mouvement/10;
						if (currentScale<-1) currentScale=-1;
						if (currentScale>1) currentScale=1;
						if (tempSpectra.getLowerContourline()<=Spectra.DEFAULT_NB_CONTOURS) {
							tempSpectra.setLowerContourline(tempSpectra.getLowerContourline()+currentScale);
							interactions.repaint();
						}
					}
				} else {
					if (useMouvement) {
						tempSpectra.setMultFactor(tempSpectra.getMultFactor()-mouvement/50);
					} else {
						Point2D.Double invertedCPoint = new Point2D.Double();
						Point2D.Double invertedRPoint = new Point2D.Double();
						try {
							AffineTransform inverseTransform = tempSpectra.getGlobalTransform().createInverse();
							inverseTransform.transform(interactions.getContactPoint(), invertedCPoint);
							inverseTransform.transform(interactions.getReleasePoint(), invertedRPoint);
						} catch (Exception e) {
							System.out.println("transform not invertable");
						}
						double alpha = (double) tempSpectra.getHeight() - invertedCPoint.y;
						double factor=(1 + (invertedCPoint.y - invertedRPoint.y) / alpha);
						tempSpectra.setMultFactor(tempSpectra.getMultFactor() * factor);
					}
					tempSpectra.checkVerticalLimits();
				}
			}
			if (activeEntity instanceof Integral) {
				Integral tempIntegral = (Integral) interactions.getActiveEntity();
				Spectra tempSpectra = (Spectra) tempIntegral.getParentEntity();
				tempSpectra.setIntegralsMultFactor(tempSpectra.getIntegralsMultFactor()-mouvement/10);
			}
		}
		interactions.getActiveDisplay().checkSizeAndPosition();
		interactions.repaint();
		interactions.setContactPoint(interactions.getReleasePoint());
	}

	
	protected void handleWheelEvent(MouseWheelEvent ev) {
		
		int rotation=ev.getWheelRotation();
		scaleSpectra(rotation, ev.isShiftDown(), true);
	}
	
	protected void handleEvent(MouseEvent ev) {
		// if a spectrum is selected we should keep it ?
		// maybe we should click to select something !!! Double click allow to select all ?
		if (interactions.getCurrentAction() == this) {
			switch (ev.getID()) {
				case MouseEvent.MOUSE_CLICKED:
					SelectActionButton selectButton=(SelectActionButton)interactions.getButtonByClassName("org.cheminfo.hook.framework.SelectActionButton");
					if (selectButton!=null) {
						selectButton.mouseClicked(ev);
					}
					break;
				case MouseEvent.MOUSE_DRAGGED:
					double rotation=interactions.getContactPoint().y-interactions.getReleasePoint().y+interactions.getContactPoint().x-interactions.getReleasePoint().x;
					scaleSpectra(-rotation,ev.isShiftDown(),false);
			}
		}
	}

	public void handleKeyEvent(KeyEvent ev) {
		// following lines allows to check if the button is active so that we need to deal with the keys
		if (this.getStatus() == DefaultActionButton.INACTIVE) {
			return;
		}
		super.handleKeyEvent(ev);
		// System.out.println("VZoom event: "+ev);
		if (ev.getID() == KeyEvent.KEY_RELEASED && (ev.getKeyChar()=='+' || ev.getKeyChar()=='-')) {
			if (ev.getKeyChar() == '+') {
				this.scaleSpectra(-4, ev.isShiftDown(),true);
			} else if (ev.getKeyChar() == '-') {
				this.scaleSpectra(4, ev.isShiftDown(),true);
			}
		}
	}

	protected void checkButtonStatus() {
		if ((interactions.getActiveDisplay() instanceof SpectraDisplay) && (! ((SpectraDisplay)interactions.getActiveDisplay()).isAbsoluteYScale()))
			this.activate();
		else
			this.deactivate();
	}

}