package org.cheminfo.hook.nemo;

import java.awt.Color;
import java.util.Vector;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;

public class AutoIntegrateAction extends GeneralAction
{
	public static void performAction(InteractiveSurface interactions)
	{
		if (interactions != null)
		{
			interactions.takeUndoSnapshot();
			
			double peakWidth=Double.valueOf(interactions.getUserDialog().getParameter("peakWidth")).doubleValue();
			double peakThreshold=Double.valueOf(interactions.getUserDialog().getParameter("peakThreshold")).doubleValue();
			
			Spectra tempSpectra=null;
			if (interactions.getActiveEntity() instanceof Spectra)
				tempSpectra=(Spectra)interactions.getActiveEntity();
			
			if (tempSpectra != null)
			{
				tempSpectra.getSpectraData().putSubParam("peakWidth",peakWidth);
				tempSpectra.getSpectraData().putSubParam("peakThreshold",peakThreshold);
	
				// We set the current action back to the previous action
				interactions.setCurrentAction(interactions.getPreviousAction());
			
				autoPeakPicking(tempSpectra, peakWidth, peakThreshold, false, 0.1);
			}
			
			interactions.getUserDialog().setText("");
//			interactions.getRootDisplay().checkSizeAndPosition();
			interactions.repaint();
		}
	}
	
	protected static void autoPeakPicking(Spectra spectra, double peakWidth, double peakThreshold, boolean findMaxima, double minRelIntensity)
	{
		Vector allPeaks = new Vector();
		double intensityThreshold;
		

		int baselineStartPoint=0;		int baselineEndPoint=0;
		
		int currentPeakStartPoint=0;		int currentPeakEndPoint=0;	int currentPeakTopPoint=0;;
		double currentArea;

		// For test ONLY!!
		SpectraData tempSpectraData=spectra.getSpectraData();

		if (findMaxima)
			intensityThreshold=minRelIntensity*(tempSpectraData.getMaxY()-tempSpectraData.getMinY())+tempSpectraData.getMinY();
		else
			intensityThreshold=(1-minRelIntensity)*(tempSpectraData.getMaxY()-tempSpectraData.getMinY())+tempSpectraData.getMinY();

		System.out.println("minY: "+tempSpectraData.getMinY()+", maxY: "+tempSpectraData.getMaxY()+", thres: "+intensityThreshold);
		
		
		
		
		int unitFactor=(int)(peakWidth/tempSpectraData.getInterval());		// the interval is negative (WRONG!!)
		double[] pDer=new double[tempSpectraData.getNbPoints()];			// Pseudo derivative
		double[] secDer= new double[tempSpectraData.getNbPoints()];			// Actual derivative of the pseudo derivative
		int previousPoint=0;


		// Empty the ChromatogramPeak Vector & erase all PeakLabel
		spectra.chromatogramPeaks.removeAllElements();
		for (int entity=0; entity < spectra.getEntitiesCount(); entity++)
		{
			if (spectra.getEntity(entity) instanceof PeakLabel)	spectra.remove(entity);
		}	

		// Calculate pseudo derivative and second derivative
		pDer[0]=0;
		for (int point=1; point< tempSpectraData.getNbPoints(); point++)
		{
			previousPoint=point-unitFactor;
			if (previousPoint < 0) previousPoint=0;
			
			pDer[point]=(tempSpectraData.getY(point)-tempSpectraData.getY(previousPoint));
		}


		secDer[0]=0;
		for (int point=1; point< tempSpectraData.getNbPoints(); point++)
		{
			secDer[point]=(tempSpectraData.getY(point)-tempSpectraData.getY(point-1));
		}
		
		PeakLabel tempPeakLabel;
		int currentPoint=1;
		int lastAnalyzedPoint=0;
		int reachBaselinePoint=0;
		
		while (currentPoint < tempSpectraData.getNbPoints())
		{
			// Look for baseline detachment -> peakStart 
			for (int point=currentPoint; point < tempSpectraData.getNbPoints(); point++)
			{
				if (pDer[point] > peakThreshold)
				{
					System.out.println("Baseline left at: "+String.valueOf(tempSpectraData.getFirstX()+point*tempSpectraData.getInterval()-peakWidth/2));
					baselineStartPoint=point;
					
					currentPeakStartPoint=point;
					tempPeakLabel=new PeakLabel(spectra.arrayPointToUnits(point));
					tempPeakLabel.setPrimaryColor(Color.blue);
					break;
				}
				currentPoint++;
			}
			
			// Look for Baseline reunion
			lastAnalyzedPoint=currentPoint;
			for (int point=currentPoint; point< tempSpectraData.getNbPoints(); point++)
			{
				if (pDer[point] > -peakThreshold && pDer[point] < peakThreshold &&
					secDer[point] >= -peakThreshold/5000 && secDer[point] <= peakThreshold/5000)
				{
					System.out.println("Baseline returned at: "+String.valueOf(tempSpectraData.getFirstX()+point*tempSpectraData.getInterval()-peakWidth/2));
					baselineEndPoint=point;
					
					tempPeakLabel=new PeakLabel(spectra.arrayPointToUnits(point));
					tempPeakLabel.setPrimaryColor(Color.yellow);
					break;
				}
				currentPoint++;
			}
			
			// Look for individual peaks
			reachBaselinePoint=currentPoint;
			currentPoint=lastAnalyzedPoint;
			
			int peakWidthAP=(int)-Math.abs(peakWidth/tempSpectraData.getInterval());

			while (currentPoint < reachBaselinePoint)
			{
				// Peak Top
				for (int point=currentPoint; point< reachBaselinePoint; point++)
				{
					if (pDer[point-1] >= 0 && pDer[point] <= 0)
					{
						tempPeakLabel=new PeakLabel(spectra.arrayPointToUnits(findPeakInInterval(tempSpectraData, point+peakWidthAP, point-peakWidthAP, findMaxima)));
						
						if ((findMaxima && spectra.spectraValueAt(tempPeakLabel.getXPos()) > intensityThreshold) || (!findMaxima && spectra.spectraValueAt(tempPeakLabel.getXPos()) < intensityThreshold))
						{
							spectra.addEntity(tempPeakLabel);
						}
//						allPeaks.addElement(new PeakLabel(spectra.arrayPointToUnits(findPeakInInterval(tempSpectraData, point+peakWidthAP, point-peakWidthAP, findMaxima))));
						break;
					}
					currentPoint++;
				}
				// Peak End
				for (int point=currentPoint; point< reachBaselinePoint; point++)
				{
					currentPoint++;

					if (pDer[point] > -peakThreshold && secDer[point] > 0)
					{
						currentPeakEndPoint=point;

						// calculate the absolute area
						currentArea=0;
						for (int peakPoint=currentPeakStartPoint; peakPoint < currentPeakEndPoint; peakPoint++)
						{
							currentArea+=tempSpectraData.getY(peakPoint);
						}
						
						// baseline correction (we substract the area of the trapeze under the baseline

						currentArea-=((currentPeakEndPoint-currentPeakStartPoint)/2)*((tempSpectraData.getY(baselineEndPoint)-tempSpectraData.getY(baselineStartPoint))/(baselineEndPoint-baselineStartPoint)*(currentPeakStartPoint+currentPeakEndPoint-2*baselineStartPoint)+2*tempSpectraData.getY(baselineStartPoint));
//						if (currentArea > 0) tempSpectra.chromatogramPeaks.addElement(new ChromatogramPeak(currentPeakStartPoint*tempSpectraData.getInterval()-peakWidth/2, currentPeakTopPoint*tempSpectraData.getInterval()-peakWidth/2, currentPeakEndPoint*tempSpectraData.getInterval()-peakWidth/2, currentArea));
//						else System.out.println("Negative integral");
						if (reachBaselinePoint-point > 10)
						{
//							System.out.println("Peak Start at: "+String.valueOf(-point*tempSpectraData.getInterval()-peakWidth/2));
							currentPeakStartPoint=point;
						}
						break;
					}
				}
	
			}	
		}
		
/*		if (topPeaks == 0)
		{
			for (int el=0; el < allPeaks.size(); el++)
			{
				spectra.addPeakLabel((PeakLabel)allPeaks.elementAt(el));
			}
		}
		else
		{
			Vector selectedPeaks = new Vector();
			
			int counter=0;
			double currentThreshold;
			
			if (findMaxima)
				currentThreshold=-99999999;
			else
				currentThreshold=9999999;
				
			for (int el=0; el < allPeaks.size(); el++)
			{
//				if (counter
			}
		}
*/
/*		double totalArea=0;
		for (int peak=0; peak < tempSpectra.chromatogramPeaks.size(); peak++)
		{
			ChromatogramPeak currentPeak=(ChromatogramPeak)tempSpectra.chromatogramPeaks.elementAt(peak);
			totalArea+=currentPeak.getAbsoluteArea();
		}
*/
/*			DecimalFormat newFormat = new DecimalFormat();
		newFormat.applyPattern("#0.00");
		
		Point firstFieldPosition=new Point(400, 100);
		
		for (int peak=0; peak < tempSpectra.chromatogramPeaks.size(); peak++)
		{
			ChromatogramPeak currentPeak=(ChromatogramPeak)tempSpectra.chromatogramPeaks.elementAt(peak);
			tempSpectra.addPeakLabel(new PeakLabel(currentPeak.getPeakTop()));

			System.out.println("Peak Nb: "+peak+", rt: "+currentPeak.getPeakTop()+", Area: "+currentPeak.getAbsoluteArea()/totalArea*100);

			TextFieldEntity newField = new TextFieldEntity();
			newField.setLocation(firstFieldPosition.x, firstFieldPosition.y+peak*20);
			newField.insertString("Rt:  "+newFormat.format(currentPeak.getPeakTop())+" min ("+newFormat.format(currentPeak.getAbsoluteArea()/totalArea*100)+"%)");
			tempSpectra.add(newField);

		}
*/			
	}
	
	private static int findPeakInInterval(SpectraData tempSpectraData, int firstPoint, int lastPoint, boolean isMaxima)
	{
		int tempPoint;
		
		if (firstPoint > lastPoint)
		{
			tempPoint=firstPoint;
			firstPoint=lastPoint;
			lastPoint=tempPoint;
		}
		
		int peakPoint=firstPoint;
			
		double peakValue=tempSpectraData.getY(firstPoint);
		
		for (int point=firstPoint; point < lastPoint; point++)
		{
			if (isMaxima)
			{
				if (tempSpectraData.getY(point) > peakValue)
				{
					peakValue=tempSpectraData.getY(point);
					peakPoint=point;
				}
			}
			else
			{
				if (tempSpectraData.getY(point) < peakValue)
				{
					peakValue=tempSpectraData.getY(point);
					peakPoint=point;
				}
			}
		}
		
		return peakPoint;
	}
	
/*	private void sortPeakVector(Vector peakVec)
	{
		for (int el1=0; el1 < peakVec.size()-1; el1++)
		{
			for (int el2=0; el2 < peakVec.size(); el2++)
			{
				if (((PeakLabel)peakVec.elementAt(el2)).getYPos() > ((PeakLabel)peakVec.elementAt(el1)).getYPos()
			}
		
		}
	}
*/
}