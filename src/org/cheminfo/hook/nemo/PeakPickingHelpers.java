package org.cheminfo.hook.nemo;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class PeakPickingHelpers {
	/**
	 * Returns the X-coordinate in units for the maximum/minimum found in a
	 * range of 'radius' units from the specified point 'xPos'.
	 * 
	 * @param searchCenter
	 *            X-coordinate in units of the center.
	 * @param radius
	 *            radius of the search domain in units.
	 * @param spectra
	 *            the Spectra on which to conduct the search.
	 * @param reverseLogic
	 *            flag that induces the search form maximum or minimum.
	 * @return the arrayPoint index of the peak.
	 */
	public static int findPeakInRange(double searchCenter, double radius,
			Spectra spectra, boolean reverseLogic) {

		double maxima = 0;
		int maximaAtPoint;
		double average = 0;

		spectra.getSpectraData().setActiveElement(spectra.getSpectraNb());

		maxima = 0;
		maximaAtPoint = -1;

//		if (spectra.getSpectraData().getDataClass() == SpectraData.DATACLASS_XY)
		{
			int startPoint = spectra.unitsToArrayPoint(searchCenter - radius);
			int stopPoint = spectra.unitsToArrayPoint(searchCenter + radius);

			maximaAtPoint = Math.min(startPoint, stopPoint);

			// first pass through the array to calculate the average height
			for (int currentPoint = 0; currentPoint < spectra.getSpectraData().getNbPoints(); currentPoint++) {
				average += spectra.getSpectraData().getY(currentPoint);
			}

			average /= spectra.getSpectraData().getNbPoints();

			boolean lookForMaxima = true;
			if (Math.abs(spectra.getSpectraData().getMinY() - average) < Math
					.abs(spectra.getSpectraData().getMaxY() - average))
				lookForMaxima = true;
			else
				lookForMaxima = false;

			if (reverseLogic)
				lookForMaxima = !lookForMaxima;

			if (lookForMaxima) {
				maxima = spectra.getSpectraData().getMinY();
				for (int currentPoint = Math.min(startPoint, stopPoint); currentPoint < Math.max(startPoint, stopPoint); currentPoint++) {
					if (maxima < spectra.getSpectraData().getY(currentPoint)) {
						maxima = spectra.getSpectraData().getY(currentPoint);
						maximaAtPoint = currentPoint;
					}
				}
			} else {
				maxima = spectra.getSpectraData().getMaxY();
				for (int currentPoint = Math.min(startPoint, stopPoint); currentPoint < Math.max(startPoint, stopPoint); currentPoint++) {
					if (maxima > spectra.getSpectraData().getY(currentPoint)) {
						maxima = spectra.getSpectraData().getY(currentPoint);
						maximaAtPoint = currentPoint;
					}
				}
			}
/*		} else if (spectra.getSpectraData().getDataClass() == SpectraData.DATACLASS_PEAK) {
			boolean first = true;

			double startUnits = searchCenter - radius;
			double stopUnits = searchCenter + radius;

			for (int currentPoint = 0; currentPoint < spectra.getSpectraData().getNbPoints(); currentPoint++) {
				if (spectra.getSpectraData().getX(currentPoint) > Math.min(startUnits, stopUnits)
						&& spectra.getSpectraData().getX(currentPoint) < Math.max(startUnits, stopUnits))
				{
					if (first)
					{
						first = false;
						maxima = spectra.getSpectraData().getY(currentPoint);
						maximaAtPoint = currentPoint;
					}

					if (spectra.getSpectraData().getY(currentPoint) > maxima)
					{
						maxima = spectra.getSpectraData().getY(currentPoint);
						maximaAtPoint = currentPoint;
					}
				}
			}
*/		}

		return (maximaAtPoint);
	}
	/**
	 * Finds all maxima in the rectangle defined by firstPoint and secondPoint.
	 * Criteria: for each point the two preceding and the two following point
	 * must have a lower REAL value. Add a PeakLabel on each one of them.
	 * 
	 * @param firstPoint -
	 *            first point defining the search rectangle.
	 * @param secondPoint -
	 *            second point defining the search rectangle.
	 */
	public static void findPeaksInRect(Point2D.Double firstPoint,
			Point2D.Double secondPoint, Spectra spectra) {
		SpectraDisplay parentDisplay = (SpectraDisplay) spectra.getParentEntity();

		Point2D.Double invertedPoint1 = new Point2D.Double();
		Point2D.Double invertedPoint2 = new Point2D.Double();

		try {
			AffineTransform inverseTransform = spectra.getGlobalTransform().createInverse();
			inverseTransform.transform(firstPoint, invertedPoint1);
			inverseTransform.transform(secondPoint, invertedPoint2);
		} catch (Exception e) {
			System.out.println("transform not invertable");
		}

		double lowerXLimit, upperXLimit, lowerYLimit, upperYLimit;

		/*
		 * lowerXLimit=parentDisplay.absolutePixelsToUnitsH(Math.min(firstPoint.x,
		 * secondPoint.x));
		 * upperXLimit=parentDisplay.absolutePixelsToUnitsH(Math.max(firstPoint.x,
		 * secondPoint.x)); lowerYLimit=Math.min(firstPoint.y, secondPoint.y);
		 * upperYLimit=Math.max(firstPoint.y, secondPoint.y);
		 */
		lowerXLimit = spectra.pixelsToUnits(Math.min(invertedPoint1.x, invertedPoint2.x));
		upperXLimit = spectra.pixelsToUnits(Math.max(invertedPoint1.x, invertedPoint2.x));
		lowerYLimit = Math.min(invertedPoint1.y, invertedPoint2.y);
		upperYLimit = Math.max(invertedPoint1.y, invertedPoint2.y);

		Point2D.Double tempDimension = new Point2D.Double(spectra.getWidth(), spectra.getHeight());

		if (spectra.getSpectraData().getDefaults().anchorPoint == 0) {
			// look for maxima
			for (int currentPoint = spectra.unitsToArrayPoint(lowerXLimit) + 2; currentPoint <= spectra.unitsToArrayPoint(upperXLimit) - 2; currentPoint++)
			{
				if (((int) Math.floor(tempDimension.y - spectra.getMultFactor() * (spectra.getSpectraData().getY(currentPoint) - spectra.getSpectraData().getMinY()) * (tempDimension.y)	/ (spectra.getSpectraData().getMaxY() - spectra.getSpectraData().getMinY()))) > lowerYLimit
						&& ((int) Math.floor(tempDimension.y - spectra.getMultFactor() * (spectra.getSpectraData().getY(currentPoint) - spectra.getSpectraData().getMinY())	* (tempDimension.y)	/ (spectra.getSpectraData().getMaxY() - spectra.getSpectraData().getMinY()))) < upperYLimit
						&& spectra.getSpectraData().getY(currentPoint - 2) <= spectra.getSpectraData().getY(currentPoint - 1)
						&& spectra.getSpectraData().getY(currentPoint - 1) <= spectra.getSpectraData().getY(currentPoint)
						&& spectra.getSpectraData().getY(currentPoint) >= spectra.getSpectraData().getY(currentPoint + 1)
						&& spectra.getSpectraData().getY(currentPoint + 1) >= spectra.getSpectraData().getY(currentPoint + 2)
					)
				{
					spectra.addEntity(new PeakLabel(spectra.arrayPointToUnits(currentPoint)));
				}
			}
		} else {
			for (int currentPoint = spectra.unitsToArrayPoint(lowerXLimit) + 2; currentPoint <= spectra.unitsToArrayPoint(upperXLimit) - 2; currentPoint++)
			{
				if (((int) Math.floor(tempDimension.y - spectra.getMultFactor()	* (spectra.getSpectraData().getY(currentPoint) - spectra.getSpectraData().getMinY()) * (tempDimension.y) / (spectra.getSpectraData().getMaxY() - spectra.getSpectraData().getMinY()))) > lowerYLimit - spectra.getLocation().y
						&& ((int) Math.floor(tempDimension.y - spectra.getMultFactor() * (spectra.getSpectraData().getY(currentPoint) - spectra.getSpectraData().getMinY()) * (tempDimension.y)	/ (spectra.getSpectraData().getMaxY() - spectra.getSpectraData().getMinY()))) < upperYLimit	- spectra.getLocation().y
						&& spectra.getSpectraData().getY(currentPoint - 2) >= spectra.getSpectraData().getY(currentPoint - 1)
						&& spectra.getSpectraData().getY(currentPoint - 1) >= spectra.getSpectraData().getY(currentPoint)
						&& spectra.getSpectraData().getY(currentPoint) <= spectra.getSpectraData().getY(currentPoint + 1)
						&& spectra.getSpectraData().getY(currentPoint + 1) <= spectra.getSpectraData().getY(currentPoint + 2)
					)
				{
					spectra.addEntity(new PeakLabel(spectra.arrayPointToUnits(currentPoint)));
				}
			}

		}
	}

	public static void shiftSpectraObjects(Spectra spectra, double shift) {
		for (int ent = 0; ent < spectra.getEntitiesCount(); ent++) {
			if (spectra.getEntity(ent) instanceof SpectraObject) {
				((SpectraObject) spectra.getEntity(ent)).shiftObject(shift);
			}
		}
	}
	
	
}