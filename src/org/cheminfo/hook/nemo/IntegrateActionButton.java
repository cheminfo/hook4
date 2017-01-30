package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class IntegrateActionButton extends DefaultSpectraActionButton
{
	
	public IntegrateActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON, new char[] {'i','I'});
	}

	protected void performInstantAction()
	{
		super.performInstantAction();
		interactions.setCurrentAction(this);

		if (interactions.getActiveDisplay() != null && !(interactions.getActiveEntity() instanceof Spectra))
		{
			if(((SpectraDisplay)interactions.getActiveDisplay()).getFirstSpectra() != null)
			{
				interactions.setActiveEntity(((SpectraDisplay)interactions.getActiveDisplay()).getFirstSpectra());
			}
		}
		else if (interactions.getActiveDisplay() == null)
		{
			if (interactions.getRootDisplay() != null && interactions.getRootDisplay() instanceof SpectraDisplay)
			{
				SpectraDisplay rootDisplay=(SpectraDisplay)interactions.getRootDisplay();
				if (rootDisplay.getFirstSpectra() != null)
					interactions.setActiveEntity(rootDisplay.getFirstSpectra());
			}
		}
		interactions.repaint();
	}
	
	protected void handleEvent(MouseEvent ev)
	{
		super.handleEvent(ev);

		if (interactions.getCurrentAction() == this)
		{
			Vector<BasicEntity> activeEntities = new Vector<BasicEntity>();
			Spectra tempSpectra = null;
				
			if (interactions.getActiveEntity() instanceof Spectra) {
				tempSpectra = (Spectra)interactions.getActiveEntity();
				activeEntities=interactions.getActiveEntities();
			} else if (interactions.getOverEntity() instanceof Spectra) {
				tempSpectra = (Spectra)interactions.getOverEntity();
				activeEntities.add(tempSpectra);
			}
			
			if (tempSpectra != null)
			{
				SpectraDisplay parentDisplay=(SpectraDisplay)tempSpectra.getParentEntity();
				
				if (tempSpectra.isDrawnAs2D() == false)
				{
					switch(ev.getID())
					{
						case MouseEvent.MOUSE_PRESSED:
							interactions.takeUndoSnapshot();
							parentDisplay.setCursorType(SpectraDisplay.FILLED_RECT);
							break;
						
						case MouseEvent.MOUSE_RELEASED:
							parentDisplay.setCursorType(SpectraDisplay.NONE);
							if (Math.abs(interactions.getContactPoint().x-interactions.getReleasePoint().x) > 3 || Math.abs(interactions.getContactPoint().y-interactions.getReleasePoint().y) > 3)
							{
								double baseArea=0;
								double multFactor=1;
								for (BasicEntity activeEntity : activeEntities) {
									tempSpectra = (Spectra) activeEntity;
									IntegrationHelpers.addIntegral(parentDisplay.absolutePixelsToUnitsH(Math.max(interactions.getContactPoint().x, interactions.getReleasePoint().x)), parentDisplay.absolutePixelsToUnitsH(Math.min(interactions.getContactPoint().x, interactions.getReleasePoint().x)), tempSpectra);	// For Test ONLY!!
									// if there is more than one activeEntity, we need to normalize the integrals !
									if (baseArea==0) {
										// multFactor=tempSpectra.getIntegralsMultFactor();
										baseArea=tempSpectra.getIntegralsBaseArea();
										System.out.println("Load base area to: "+baseArea);
									} else {
										System.out.println("Set base area to: "+baseArea);
										tempSpectra.setIntegralsBaseArea(baseArea);
										// tempSpectra.setIntegralsMultFactor(multFactor);
									}
								}
								

								
								
								parentDisplay.checkSizeAndPosition();
								interactions.repaint();
							}
							else
							{
								if (interactions.getOverEntity() instanceof Spectra)
								{
									interactions.setActiveEntity(interactions.getOverEntity());
									interactions.repaint();
								}
							}
							break;
						
						case MouseEvent.MOUSE_DRAGGED:
							interactions.repaint();
							break;
							
						default:
							break;
					}
				}
/*				else
				{
					switch(ev.getID())
					{
						case MouseEvent.MOUSE_PRESSED:
							interactions.takeUndoSnapshot();
							tempSpectra.setCursorType(Spectra.FILLED_RECT);
							break;
						
						case MouseEvent.MOUSE_RELEASED:
							tempSpectra.addIntegral(tempSpectra.pixelsToUnits(Math.max(tempSpectra.getContactPoint().x, tempSpectra.getReleasePoint().x)), tempSpectra.pixelsToUnits(Math.min(tempSpectra.getContactPoint().x, tempSpectra.getReleasePoint().x)));	// For Test ONLY!!
							tempSpectra.setCursorType(Spectra.NONE);
							tempSpectra.repaint();
							break;
						
						case MouseEvent.MOUSE_DRAGGED:
							tempSpectra.repaint();
							break;
							
						default:
							break;
					}
				}
*/			}
			
			if (interactions.getOverEntity() instanceof Integral)
			{
				Integral tempIntegral = (Integral)interactions.getOverEntity();

/*				DecimalFormat newFormat = new DecimalFormat();
				newFormat.applyPattern("#0.00");
				String value;
*/				
				switch (ev.getID())
				{
/*					case BasicEntity.MOUSE_ENTERED:
						tempIntegral.displayMessage();
						break;
*/						
/*					case BasicEntity.MOUSE_EXITED:
						interactions.getUserDialog().clearText();
						break;
*/
					case MouseEvent.MOUSE_CLICKED:	
						interactions.clearActiveEntities();
						interactions.setActiveEntity(tempIntegral);
						setUserDialogText(tempIntegral.getClickedMessage());
//						tempIntegral.displayText();
						break;
						
					default:
						break;
				}
			}
		}
	}
	
	protected void checkButtonStatus() {
		activateIf1DNMR();
	}
}