package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;

public class NoisePeakAction extends GeneralAction
{
	public static void performAction(InteractiveSurface interactions)
	{
		if (interactions != null)
		{
			interactions.takeUndoSnapshot();
			
			Spectra tempSpectra=null;
			
			if (interactions.getActiveEntity() instanceof Spectra)
				tempSpectra=(Spectra)interactions.getActiveEntity();
			
			if (tempSpectra != null)
			{
				// We set the current action back to the previous action
				interactions.setCurrentAction(interactions.getPreviousAction());
				
				double averageAbsDifference=0;

				SpectraData tempSpectraData=tempSpectra.getSpectraData();
				
					for (int point=1; point < tempSpectraData.getNbPoints(); point++)
					{
						averageAbsDifference+=Math.abs(tempSpectraData.getY(point)-tempSpectraData.getY(point-1));
					}
				
					averageAbsDifference/=tempSpectraData.getNbPoints()-1;
			
					
				noisePeakPicking(tempSpectra,  averageAbsDifference*30/(tempSpectraData.getMaxY()-tempSpectraData.getMinY()));
			}
			
			interactions.getUserDialog().setText("");
			interactions.getRootDisplay().checkSizeAndPosition();
			interactions.repaint();
		}
	}
	
	protected static void noisePeakPicking(Spectra spectra, double minRelIntensity)
	{
		SpectraData tempSpectraData=spectra.getSpectraData();
	
		if (spectra.getSpectraData().getDefaults().anchorPoint != 2)
		{
			double offset=minRelIntensity*(spectra.getSpectraData().getMaxY()-spectra.getSpectraData().getMinY());
			//Find Maxima
			for (int point=2; point < tempSpectraData.getNbPoints()-2; point++)
			{
//				if (tempSpectraData.getY(point) > spectra.getSpectraData().getMinY()+averageAbsDifference*30)
				if (tempSpectraData.getY(point) > spectra.getSpectraData().getMinY()+offset)
				{
					if (tempSpectraData.getY(point) >= tempSpectraData.getY(point-1) &&
						tempSpectraData.getY(point) >= tempSpectraData.getY(point+1) && 
						!(tempSpectraData.getY(point) == tempSpectraData.getY(point-1) && tempSpectraData.getY(point) == tempSpectraData.getY(point+1))
						)
					{
	 					spectra.addEntity(new PeakLabel(spectra.arrayPointToUnits(point)));
					}
				}
			}
		}
		else
		{
			for (int point=2; point < tempSpectraData.getNbPoints()-2; point++)
			{
//				if (tempSpectraData.getY(point) < spectra.getSpectraData().getMaxY()-averageAbsDifference*30)
				if (tempSpectraData.getY(point) < spectra.getSpectraData().getMaxY()-minRelIntensity*(spectra.getSpectraData().getMaxY()-spectra.getSpectraData().getMinY()))
				{
					if (tempSpectraData.getY(point) <= tempSpectraData.getY(point-1) &&
						tempSpectraData.getY(point) <= tempSpectraData.getY(point+1) &&
						!(tempSpectraData.getY(point) == tempSpectraData.getY(point-1) && tempSpectraData.getY(point) == tempSpectraData.getY(point+1))
						)
					{
	 					spectra.addEntity(new PeakLabel(spectra.arrayPointToUnits(point)));
					}
				}
			}
		}
	}
	

}