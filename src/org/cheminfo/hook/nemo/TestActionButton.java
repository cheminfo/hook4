package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.cheminfo.hook.framework.BasicDisplay;
import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.util.XMLCoDec;

public class TestActionButton extends DefaultActionButton
{
	private PeakLabel tempLabel;
	

	public TestActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON);
	}

	protected void performInstantAction()
	{
		super.performInstantAction();
		interactions.setCurrentAction(this);
		
		if (interactions.getActiveDisplay() != null)
		{
			if ( !(interactions.getActiveEntity() instanceof Spectra) )
			{
				interactions.setActiveEntity(((SpectraDisplay)interactions.getActiveDisplay()).getFirstSpectra());
//			interactions.takeUndoSnapshot();
				interactions.repaint();
			}
		}
	}

	protected void handleEvent(MouseEvent ev)	
	{
		super.handleEvent(ev);
		
		if (interactions.getCurrentAction() == this)
		{
			Spectra tempSpectra=null;
			
			if (interactions.getOverEntity() instanceof SmartPeakLabel)
			{
				if (ev.getID() == MouseEvent.MOUSE_CLICKED)
				{
					interactions.setActiveEntity(interactions.getOverEntity());
					interactions.getUserDialog().setText(interactions.getOverEntity().getClickedMessage());
				}
			}
			else if (!(interactions.getActiveEntity() instanceof Spectra))
			{
				if (ev.getID() == MouseEvent.MOUSE_PRESSED)
				{
					SpectraDisplay parentDisplay=(SpectraDisplay)interactions.getActiveDisplay();
					interactions.setActiveEntity(parentDisplay.getFirstSpectra());
				}
			}
			
			if (interactions.getActiveEntity() instanceof Spectra)
			{
				tempSpectra=(Spectra)interactions.getActiveEntity();
				SpectraDisplay parentDisplay=(SpectraDisplay)tempSpectra.getParentEntity();
				
				Point2D.Double invertedCPoint=new Point2D.Double();
				Point2D.Double invertedRPoint=new Point2D.Double();
				try
				{
					AffineTransform inverseTransform=tempSpectra.getGlobalTransform().createInverse();
					inverseTransform.transform(interactions.getContactPoint(), invertedCPoint);
					inverseTransform.transform(interactions.getReleasePoint(), invertedRPoint);
					
				} catch (Exception e) {System.out.println("transform not invertable");}

				if(tempSpectra.isDrawnAs2D() == false)
				{
					int tempArrayPoint;

					switch (ev.getID())
					{
						case MouseEvent.MOUSE_CLICKED:	
						// Clicking on a non selected Spectra induces a selection					
							if (interactions.getActiveEntities().size() != 1 || !interactions.getActiveEntities().contains(tempSpectra))
							{
								interactions.clearActiveEntities();
								interactions.repaint();
							}
							
							if (ev.isShiftDown())
							{
								XMLCoDec buttonCodec1=new XMLCoDec();
								buttonCodec1.addParameter("action", "org.cheminfo.hook.nemo.ManualSmartPeakAction");
								buttonCodec1.addParameter("image", "validate.gif");
	
								interactions.getUserDialog().setText("<Text type=\"plain\">Validate Peaks  </Text><Button "+buttonCodec1.encodeParameters()+"></Button>");
	
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
							if (ev.isShiftDown())
							{
//								int tempArrayPoint;
	
	
//								PeakLabel tempPeakLabel=tempSpectra.getTempPeakLabel();
	
//								if (interactions.getActiveEntity() == tempSpectra)		
								{
									if (this.tempLabel != null)
									{
										tempSpectra.remove(this.tempLabel);
										this.tempLabel=null;
									}
								
									
									double range=parentDisplay.unitsPerPixelH()*5;
									tempArrayPoint=PeakPickingHelpers.findPeakInRange(tempSpectra.pixelsToUnits(invertedCPoint.x), range, tempSpectra, false);
	
									this.tempLabel=new PeakLabel(tempArrayPoint);
									tempSpectra.addEntity(this.tempLabel);
//									tempSpectra.setTempPeakLabel(tempPeakLabel);
									this.tempLabel.setContracted(true);
								}
								parentDisplay.checkSizeAndPosition();
								interactions.repaint();
							}
							else
							{
								if (this.tempLabel != null)
								{
									if (tempSpectra.getEntity(tempSpectra.getEntitiesCount()-1) == this.tempLabel)
									{
										tempSpectra.remove(this.tempLabel);
										this.tempLabel=null;
										
										interactions.repaint();
									}
								}
							}
							break;

						case MouseEvent.MOUSE_RELEASED:
							if (ev.isShiftDown())
							{
								if ( (Math.abs(invertedCPoint.x-invertedRPoint.x) < 5) && (Math.abs(invertedCPoint.y-invertedRPoint.y) < 5))
								{
									tempSpectra.addEntity(this.tempLabel);
									tempSpectra.addTempPeakLabel(this.tempLabel);
								}
								else
								{
									interactions.takeUndoSnapshot();

									Integral tempIntegral;
									SmartPeakLabel tempSmart;

									tempIntegral=IntegrationHelpers.addIntegral(tempSpectra.pixelsToUnits(Math.max(invertedCPoint.x, invertedRPoint.x)), tempSpectra.pixelsToUnits(Math.min(invertedCPoint.x, invertedRPoint.x)), tempSpectra);
									tempSmart = new SmartPeakLabel(tempSpectra.pixelsToUnits(Math.min(invertedCPoint.x, invertedRPoint.x)), tempSpectra.pixelsToUnits(Math.max(invertedCPoint.x, invertedRPoint.x)));
									
									tempSpectra.addEntity(tempSmart);
									interactions.createLink(tempSmart, tempIntegral);
									if (this.tempLabel != null)
									{
										tempSpectra.remove(this.tempLabel);
										this.tempLabel=null;
									}
									interactions.getUserDialog().setText("");
//									interactions.takeUndoSnapshot();
								}
								
								parentDisplay.setCursorType(SpectraDisplay.NONE);
								parentDisplay.checkSizeAndPosition();
								interactions.repaint();
							}
							else
							{
								if ( (Math.abs(invertedCPoint.x-invertedRPoint.x) > 3) || (Math.abs(invertedCPoint.y-invertedRPoint.y) > 3))
								{
									interactions.takeUndoSnapshot();
									Integral tempIntegral=IntegrationHelpers.addIntegral(tempSpectra.pixelsToUnits(Math.max(invertedCPoint.x, invertedRPoint.x)), tempSpectra.pixelsToUnits(Math.min(invertedCPoint.x, invertedRPoint.x)), tempSpectra);
									SmartPeakLabel tempLabel=SmartPickingHelpers.findSmartPeak(invertedCPoint, invertedRPoint, tempSpectra);
									interactions.createLink(tempLabel, tempIntegral);
								}
	
								parentDisplay.setCursorType(BasicDisplay.NONE);
								parentDisplay.checkSizeAndPosition();
								interactions.repaint();
							}
							break;

						case MouseEvent.MOUSE_EXITED:
							if (tempSpectra.getSpectraData().isDataClassXY())
							{
								if (tempSpectra.getEntitiesCount()>0)
								{
									if (tempSpectra.getEntity(tempSpectra.getEntitiesCount()-1) == this.tempLabel)
										tempSpectra.remove(this.tempLabel);
								}
							}
							break;

						
						default:
							break;
					}
				
				}
/*				else	// 2D
				{
					switch (ev.getID())
					{
						case MouseEvent.MOUSE_MOVED:
							interactions.repaint();
							break;
							
						case MouseEvent.MOUSE_EXITED:
//							tempSpectra.setCursorType(Spectra.NONE);
							break;
							
						case MouseEvent.MOUSE_ENTERED:
//							tempSpectra.setCursorType(Spectra.CROSS);
							break;
							
						case MouseEvent.MOUSE_PRESSED:
							tempSpectra.setCursorType(Spectra.RECT);
							break;

						case MouseEvent.MOUSE_DRAGGED:
							tempSpectra.repaint();
							break;
						
						case MouseEvent.MOUSE_RELEASED:
							tempSpectra.repaint();
							tempSpectra.setCursorType(Spectra.NONE);
							
							SpectraDisplay parentDisplay=(SpectraDisplay)tempSpectra.getParent();
							
							if (parentDisplay.horRefSpectrum != null)
							{
								Point tempPointA=tempSpectra.getReleasePoint();
								Point tempPointB=tempSpectra.getContactPoint();
								
								PeakLabel tempLabel=new PeakLabel(tempSpectra.pixelsToUnits((tempSpectra.getContactPoint().x+tempSpectra.getReleasePoint().x)/2), tempSpectra.pixelsToUnitsV((tempSpectra.getContactPoint().y+tempSpectra.getReleasePoint().y)/2));
								tempLabel.setSize(15, 15);

								tempSpectra.add(tempLabel);
								
//								double threshold=(tempSpectra.getLowerContourline()/tempSpectra.getNbContourlines())*(parentDisplay.horRefSpectrum.getSpectraData().getMaxY()-parentDisplay.horRefSpectrum.getSpectraData().getMinY());
								double threshold=parentDisplay.horRefSpectrum.getNoiseLevel()*30;

								SmartPeakLabel tempSmartLabel=parentDisplay.horRefSpectrum.findSmartPeak(tempPointB, tempPointA, threshold);
								
								if (tempSmartLabel != null)
								{
									Integral tempIntegral=parentDisplay.horRefSpectrum.addIntegral(tempSpectra.pixelsToUnits(Math.max(tempSpectra.getContactPoint().x, tempSpectra.getReleasePoint().x)), tempSpectra.pixelsToUnits(Math.min(tempSpectra.getContactPoint().x, tempSpectra.getReleasePoint().x)));
	
									interactions.createLink(tempSmartLabel, tempIntegral);
									interactions.createLink(tempSmartLabel, tempLabel);
	
									parentDisplay.horRefSpectrum.repaint();
								}

								interactions.getUserDialog().setText("");
							}
							
							break;
							
						default:
							break;
					}
				}
*/			}
			else if (ev.getSource() instanceof SmartPeakLabel)
			{
				SmartPeakLabel tempSmartPeakLabel=(SmartPeakLabel)ev.getSource();
				switch (ev.getID())
				{		
				case MouseEvent.MOUSE_CLICKED:
						interactions.clearActiveEntities();
						interactions.setActiveEntity(tempSmartPeakLabel);
						setUserDialogText(tempSmartPeakLabel.getClickedMessage());
					break;
				}


			}
		}
	}
	
	protected void checkButtonStatus()
	{
		if (interactions.getActiveDisplay() != null && interactions.getActiveDisplay() instanceof SpectraDisplay)
		{
			SpectraDisplay activeDisplay=(SpectraDisplay)interactions.getActiveDisplay();
			
			if (interactions.getActiveEntity() instanceof Spectra)
			{
				if (((Spectra)interactions.getActiveEntity()).spectraData.getDataType() == SpectraData.TYPE_NMR_SPECTRUM)
					this.activate();
				else
					this.deactivate();
			}
			else
			{
				if ((activeDisplay!=null) && (activeDisplay.getFirstSpectra()!=null) && (activeDisplay.getFirstSpectra().getSpectraData().getDataType() == SpectraData.TYPE_NMR_SPECTRUM))
					this.activate();
				else
					this.deactivate();
			}
		}
		else
			this.deactivate();
	}
	

}
