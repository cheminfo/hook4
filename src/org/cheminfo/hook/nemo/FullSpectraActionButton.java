package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.geom.Point2D;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;


public class FullSpectraActionButton extends DefaultActionButton
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	public FullSpectraActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 0, ImageButton.CLASSIC, new char[] {'f','F'});
	}



	protected void performInstantAction() {
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
									//tmpSpectra.setPrimaryColor(null);
									//tmpSpectra.setSecondaryColor(null);
									tmpSpectra.setVisible(false);
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
			else{
				Vector<BasicEntity> entities = interactions.getActiveDisplay().getEntities();
				for(int i=0;i<entities.size();i++){
					if(entities.get(i) instanceof Spectra){
							((Spectra)entities.get(i)).setMultFactor(0.9);
							Point2D.Double actualLocation = ((Spectra)entities.get(i)).getLocation();
							((Spectra)entities.get(i)).setLocation(actualLocation.x,actualLocation.y);
					}
				}
				
			}
			interactions.takeUndoSnapshot();
			spectraDisplay.fullSpectra();
			spectraDisplay.checkSizeAndPosition();
			interactions.checkButtonsStatus();
			interactions.repaint();
		}
		
		// each time we load a jcamp we store a view in the ESC view. Therefore a full just mean load this view
		
		//spectraDisplay.loadViewIfExists((char)((int)NemoPreferences.getInstance()
		//		.get(NemoPreferences.getInstance().SHOW_ALL_SHORT_CUT)));
		/**/
	}
	
	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() instanceof SpectraDisplay)
			this.activate();
		else this.deactivate();
	}		
}
