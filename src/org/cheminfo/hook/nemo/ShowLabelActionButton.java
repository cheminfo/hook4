package org.cheminfo.hook.nemo;

import java.awt.Image;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class ShowLabelActionButton extends DefaultActionButton
{

	public ShowLabelActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 0, ImageButton.CHECKBUTTON);
	}

	protected void performInstantAction()
	{
		Spectra tempSpectra=null;
		
		if (interactions.getActiveEntity() instanceof Spectra)
			tempSpectra=(Spectra)interactions.getActiveEntity();
		else if (interactions.getActiveEntity().getParentEntity() instanceof Spectra)
			tempSpectra=(Spectra)interactions.getActiveEntity().getParentEntity();
		
		if (tempSpectra != null)
		{
			tempSpectra.switchVisiblePeakLabels();
			interactions.getActiveDisplay().checkSizeAndPosition();
			interactions.repaint();
		}
	}
	
/*	protected void handleEvent(MouseEvent ev)
	{
		super.handleEvent(ev);

		if (ev.getID() == MouseEvent.MOUSE_CLICKED)
		{
			if (interactions.getActiveDisplay() instanceof SpectraDisplay)
				this.activate();
			else this.deactivate();
		}

		if (ev.getSource() instanceof Spectra)
		{
			Spectra tempSpectra=(Spectra)ev.getSource();
			
			if (tempSpectra.hasVisiblePeakLabels() == 1) this.setStatus(DefaultActionButton.DOWN);
			else this.setStatus(DefaultActionButton.UP);
		}
	}
*/	
	protected void checkButtonStatus()
	{
		Spectra tempSpectra=null;
		
		if (interactions.getActiveEntity() != null)
		{
			if (interactions.getActiveEntity() instanceof Spectra)
				tempSpectra=(Spectra)interactions.getActiveEntity();
			else if (interactions.getActiveEntity().getParentEntity() instanceof Spectra)
				tempSpectra=(Spectra)interactions.getActiveEntity().getParentEntity();
		}
		if (interactions.getActiveDisplay() instanceof SpectraDisplay && tempSpectra != null)
		{
			this.activate();
			
			if (tempSpectra.hasVisiblePeakLabels() == 1) this.setStatus(DefaultActionButton.DOWN);
			else this.setStatus(DefaultActionButton.UP);
		}
		else this.deactivate();
	}

}