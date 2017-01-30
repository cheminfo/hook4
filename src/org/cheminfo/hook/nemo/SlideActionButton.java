package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.util.NemoPreferences;

public class SlideActionButton extends DefaultActionButton
{

	private double originalLeftLimit, originalRightLimit, originalTopLimit, originalBottomLimit;	

	public SlideActionButton(Image inImage, String infoMessage, InteractiveSurface interactions)
	{
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON, new char[] {'m','M'});
	}

	protected void performInstantAction()
	{
		super.performInstantAction();
		interactions.setCurrentAction(this);
	}

	protected void handleEvent(MouseEvent ev)	
	{
		super.handleEvent(ev);


		if (interactions.getCurrentAction() == this)
		{
			if (interactions.getActiveDisplay() instanceof SpectraDisplay)
			{
				SpectraDisplay activeDisplay=(SpectraDisplay)interactions.getActiveDisplay();

				switch (ev.getID())
				{
					case MouseEvent.MOUSE_PRESSED:
						this.originalLeftLimit=activeDisplay.getCurrentLeftLimit();
						this.originalRightLimit=activeDisplay.getCurrentRightLimit();
						this.originalTopLimit=activeDisplay.getCurrentTopLimit();
						this.originalBottomLimit=activeDisplay.getCurrentBottomLimit();
						
						interactions.takeUndoSnapshot();
						break;
						
					case MouseEvent.MOUSE_DRAGGED:
						if (activeDisplay.is2D())
						{
							double	slideUnitsX=(interactions.getReleasePoint().x-interactions.getContactPoint().x)*activeDisplay.unitsPerPixelH();
							double	slideUnitsY=(interactions.getReleasePoint().y-interactions.getContactPoint().y)*activeDisplay.unitsPerPixelV();
							activeDisplay.setCurrentLimits(originalLeftLimit+slideUnitsX, originalRightLimit+slideUnitsX, originalTopLimit+slideUnitsY, originalBottomLimit+slideUnitsY);
							activeDisplay.checkSizeAndPosition();
							interactions.repaint();
						}
/*						else if (tempSpectra.isVertical())
						{
							double	slideUnitsY=(tempSpectra.getReleasePoint().y-tempSpectra.getContactPoint().y)*(tempDisplay.getTopLimit()-tempDisplay.getBottomLimit())/tempSpectra.getSize().height;
							tempSpectra.setContactPoint(tempSpectra.getReleasePoint());
							tempDisplay.setCurrentLimits(tempDisplay.getLeftLimit(), tempDisplay.getRightLimit(), tempDisplay.getTopLimit()+slideUnitsY, tempDisplay.getBottomLimit()+slideUnitsY);
							tempDisplay.repaint();
							break;
						}
*/ 						else
						{
							double	slideUnitsX=(interactions.getReleasePoint().x-interactions.getContactPoint().x)*activeDisplay.unitsPerPixelH();
							activeDisplay.setCurrentLimits(originalLeftLimit+slideUnitsX, originalRightLimit+slideUnitsX);
							activeDisplay.checkSizeAndPosition();
							interactions.repaint();
							break;
						}
						
					default:
						break;
				}
			}
		}
	}

	public void handleKeyEvent(KeyEvent ev)
	{
		super.handleKeyEvent(ev);
		if (ev.getID() == KeyEvent.KEY_RELEASED && (ev.getKeyCode() == KeyEvent.VK_RIGHT || ev.getKeyCode() == KeyEvent.VK_LEFT || ev.getKeyCode() == KeyEvent.VK_DOWN || ev.getKeyCode() == KeyEvent.VK_UP) )
		{
			if (interactions.getActiveEntity() != null) {
				if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
					SpectraDisplay spectraDisplay=(SpectraDisplay)interactions.getActiveDisplay();

					double hSlideFactor=(spectraDisplay.getCurrentLeftLimit()-spectraDisplay.getCurrentRightLimit())/16;
					double vSlideFactor=(spectraDisplay.getCurrentTopLimit()-spectraDisplay.getCurrentBottomLimit())/16;
					double slideUnitsX=1;
					if (ev.isShiftDown()) {
						hSlideFactor*=10;
						vSlideFactor*=10;
						slideUnitsX*=10;
					}
					if ((ev.getKeyCode() == KeyEvent.VK_LEFT) || (ev.getKeyCode() == KeyEvent.VK_RIGHT)) {
						if (ev.getKeyCode() == KeyEvent.VK_LEFT) {
							spectraDisplay.setCurrentLimits(spectraDisplay.getCurrentLeftLimit()-hSlideFactor, spectraDisplay.getCurrentRightLimit()-hSlideFactor);
						} else {
							spectraDisplay.setCurrentLimits(spectraDisplay.getCurrentLeftLimit()+hSlideFactor, spectraDisplay.getCurrentRightLimit()+hSlideFactor);
						}
					} else {
						if (spectraDisplay.is2D()) {
							if (ev.getKeyCode() == KeyEvent.VK_UP) {
								spectraDisplay.setCurrentLimits(spectraDisplay.getCurrentLeftLimit(),spectraDisplay.getCurrentRightLimit(),spectraDisplay.getCurrentTopLimit()-vSlideFactor, spectraDisplay.getCurrentBottomLimit()-vSlideFactor);
							} else {
								spectraDisplay.setCurrentLimits(spectraDisplay.getCurrentLeftLimit(),spectraDisplay.getCurrentRightLimit(),spectraDisplay.getCurrentTopLimit()+vSlideFactor, spectraDisplay.getCurrentBottomLimit()+vSlideFactor);
							}							
						} else {
							// moving a 1D spectrum is in fact moving a BasicEntity: moveLocal
							// This means we need to find the selected spectrum
							BasicEntity activeEntity = interactions.getActiveEntity();
							if (activeEntity != null) {
								Spectra tempSpectra = null;
								if (activeEntity instanceof Spectra)
									tempSpectra = (Spectra) activeEntity;
								else if (activeEntity.getParentEntity() instanceof Spectra)
									tempSpectra = (Spectra) (activeEntity.getParentEntity());
								else if (interactions.getActiveDisplay() instanceof SpectraDisplay)
									tempSpectra = ((SpectraDisplay) interactions.getActiveDisplay()).getFirstSpectra();
								if (tempSpectra != null) {
									if (ev.getKeyCode() == KeyEvent.VK_UP) {
										tempSpectra.moveLocal(0, -slideUnitsX);
									} else {
										tempSpectra.moveLocal(0, +slideUnitsX);
									}								
								}
							}
						}
					}
					spectraDisplay.checkSizeAndPosition();
					interactions.repaint();
				}
			}
		}
		
		// code for storing a view
		// it could be either a function or a digit
		if (KeyEvent.getKeyText(ev.getKeyCode()).matches("^F[1-9]$")) {
			ev.setKeyChar((char)KeyEvent.getKeyText(ev.getKeyCode()).charAt(1));
		}
		//System.out.println("When I press a key "+ev+" "+ev.getKeyCode()+" "+ev.getID()+" "+NemoPreferences.getInstance().get(NemoPreferences.getInstance().SHOW_ALL_SHORT_CUT)+" "+Character.getNumericValue(NemoPreferences.getInstance().get(NemoPreferences.getInstance().SHOW_ALL_SHORT_CUT)));
		if ((ev.getID() == KeyEvent.KEY_RELEASED) && (ev.getKeyChar()==Nemo.SHOW_ALL_SC.charValue()
				|| Character.isDigit(ev.getKeyChar()))) {
			//System.out.println("Inside "+ev.getKeyCode());
			if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
				SpectraDisplay spectraDisplay=(SpectraDisplay)interactions.getActiveDisplay();
				if (ev.isControlDown()) {
					spectraDisplay.storeView(ev.getKeyChar());
				} else if (spectraDisplay.loadViewIfExists(ev.getKeyChar())) {
					//System.out.println("Load the view "+ev.getKeyCode());
					interactions.getActiveDisplay().checkSizeAndPosition();
					interactions.repaint();
				}
			}
		}		
	}

	protected void checkButtonStatus()
	{
		if (interactions.getActiveDisplay() instanceof SpectraDisplay)
			this.activate();
		else this.deactivate();
	}


	
}