package org.cheminfo.hook.nemo;

import java.util.Vector;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;

public class ManualSmartPeakAction extends GeneralAction
{
	public static void performAction(InteractiveSurface interactions)
	{
		Spectra tempSpectra=null;
		
		if (interactions.getActiveEntity() instanceof Spectra)
			tempSpectra=(Spectra)interactions.getActiveEntity();
		
		if (tempSpectra != null)
		{
			Vector labels=tempSpectra.getTempPeakLabels();
			
			SmartPeakLabel tempSmart = new SmartPeakLabel(((PeakLabel)labels.elementAt(0)).getXPos(), ((PeakLabel)labels.elementAt(labels.size()-1)).getXPos());
			
			int nbLabels=labels.size();
			//System.out.println(tempSmart.getNmrSignal1D().getStartX()+" "+tempSmart.getNmrSignal1D().getEndX()+" "+nbLabels);
			for (int label=0; label < nbLabels; label++)
			{
				if (tempSpectra.containsEntity((PeakLabel)labels.elementAt(label)))
				{
					tempSmart.getNmrSignal1D().addPeak(((PeakLabel)labels.elementAt(label)).getXPos(),
							tempSpectra.getSpectraData().getY(tempSpectra.unitsToArrayPoint(((PeakLabel)labels.elementAt(label)).getXPos())));
					//tempSmart.addPeak(((PeakLabel)labels.elementAt(label)).getXPos());
					//tempSmart.setIntensity(label, tempSpectra.getSpectraData().getY(tempSpectra.unitsToArrayPoint(((PeakLabel)labels.elementAt(label)).getXPos())));
					
					tempSpectra.remove((PeakLabel)labels.elementAt(label));
				}
			}
		
			tempSpectra.clearTempPeakLabels();
		
			interactions.takeUndoSnapshot();
			tempSpectra.addEntity(tempSmart);
			
			tempSmart.getNmrSignal1D().setInterval(tempSpectra.getSpectraData().getInterval());
			tempSmart.getNmrSignal1D().setUnits(tempSpectra.getSpectraData().getXUnits());
			tempSmart.getNmrSignal1D().setObserve(tempSpectra.getSpectraData().getParamDouble("observeFrequency", 0));
			tempSmart.getNmrSignal1D().compute();
			
			interactions.getRootDisplay().checkSizeAndPosition();
			interactions.repaint();

			interactions.getUserDialog().setText(tempSmart.getOverMessage());
		}
		
	}
}
