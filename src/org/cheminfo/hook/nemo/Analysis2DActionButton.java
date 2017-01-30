package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.nmr.PredictionData;

public class Analysis2DActionButton extends DefaultActionButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3464892945353353163L;

	SmartPeakLabel lastClickedLabel = null; // used for SML replacement

//	int buttonType = ImageButton.RADIOBUTTON;

	private static final boolean isDebug = false;

	public Analysis2DActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON, new char[] {'s','S'});
	}

	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);
		this.lastClickedLabel = null;

		if (interactions.getActiveDisplay() != null) {
			interactions.setActiveEntity(((SpectraDisplay) interactions.getActiveDisplay()).getFirstSpectra());
		}
	}

	protected void handleEvent(MouseEvent ev) {
		super.handleEvent(ev);

		if (interactions.getCurrentAction() == this) {
			if (interactions.getOverEntity() instanceof Spectra) {
				// Spectra tempSpectra=(Spectra)interactions.getOverEntity();
				SpectraDisplay spectraDisplay = (SpectraDisplay) interactions.getActiveDisplay();
				Spectra overSpectra = (Spectra) interactions.getOverEntity();
				Spectra tempSpectra = spectraDisplay.getFirstSpectra();

				Point2D.Double invertedCPoint = new Point2D.Double();
				Point2D.Double invertedRPoint = new Point2D.Double();
				try {
					AffineTransform inverseTransform = tempSpectra.getGlobalTransform().createInverse();
					inverseTransform.transform(interactions.getContactPoint(), invertedCPoint);
					inverseTransform.transform(interactions.getReleasePoint(), invertedRPoint);
				} catch (Exception e) {
					System.out.println("transform not invertable");
				}

				if (tempSpectra != null && tempSpectra.isDrawnAs2D()) {	
					switch (ev.getID()) {
					case MouseEvent.MOUSE_MOVED:
						if (overSpectra.isDrawnAs2D()) {
							spectraDisplay.setCursorType(SpectraDisplay.CROSSHAIR);
							this.highlightLinked(spectraDisplay, interactions.getContactPoint());
						} else {
							spectraDisplay.setCursorType(SpectraDisplay.NONE);
						}
						interactions.repaint();
						break;

					case MouseEvent.MOUSE_EXITED:
						// tempSpectra.setCursorType(Spectra.NONE);
						break;

					case MouseEvent.MOUSE_ENTERED:
						// tempSpectra.setCursorType(Spectra.CROSS);
						break;

					case MouseEvent.MOUSE_PRESSED:
						if (overSpectra.isDrawnAs2D()) {
							spectraDisplay.setCursorType(SpectraDisplay.CROSSHAIR_RECT);
						} else {
							spectraDisplay.setCursorType(SpectraDisplay.RECT);
						}
						break;

					case MouseEvent.MOUSE_DRAGGED:
						interactions.repaint();
						break;

					case MouseEvent.MOUSE_CLICKED:
						if (overSpectra.isDrawnAs2D()) {
							if (ev.isControlDown()) {
								if (ev.getClickCount()==1) {
									Point2D.Double contactPoint = interactions.getContactPoint();
									overSpectra.addVExtraLine(spectraDisplay.absolutePixelsToUnitsH(contactPoint.x));
									overSpectra.addHExtraLine(spectraDisplay.absolutePixelsToUnitsV(contactPoint.y));
								} else {
									overSpectra.clearExtraLines();
								}
								interactions.repaint();
							}
						}
						
						break;
					case MouseEvent.MOUSE_RELEASED:
						//if (! ev.isControlDown()) {
							interactions.takeUndoSnapshot();
							spectraDisplay.setCursorType(SpectraDisplay.NONE);
	
							Point2D.Double tempPointA = interactions.getReleasePoint();
							Point2D.Double tempPointB = interactions.getContactPoint();
	
							if (Math.abs(tempPointA.x - tempPointB.x) > 3
									|| Math.abs(tempPointA.y - tempPointB.y) > 3
									|| true) {
	
								this.analyzeSpectrum(spectraDisplay, tempPointA, tempPointB, ev);
								tempSpectra.checkSizeAndPosition();
								interactions.repaint();
								interactions.getUserDialog().setText("");
							}
						//}
						break;
					default:
						break;
					}
				}
			} else if (interactions.getOverEntity() instanceof SmartPeakLabel) {
				SmartPeakLabel tempSmartPeakLabel = (SmartPeakLabel) interactions.getOverEntity();

				switch (ev.getID()) {
				case MouseEvent.MOUSE_CLICKED:
					if (this.lastClickedLabel != null) {
						Vector linkedEnts = interactions.getLinkedEntities(this.lastClickedLabel);
						PeakLabel diagonalRefLabel = null;

						for (int ent = 0; ent < linkedEnts.size(); ent++) {
							if (linkedEnts.elementAt(ent) instanceof PeakLabel
									&& ((PeakLabel) linkedEnts.elementAt(ent)).getPeakType() == PeakLabel.DIAGONAL_PEAK) {
								diagonalRefLabel = (PeakLabel) linkedEnts.elementAt(ent);
								break;
							}
						}
						if (diagonalRefLabel != null) {
							interactions.createLink(diagonalRefLabel,
									tempSmartPeakLabel,
									InteractiveSurface.INTEGRITY_CHECK);
						}

						Spectra tempSpectra = (Spectra) tempSmartPeakLabel
								.getParentEntity();
						tempSmartPeakLabel.getParentEntity().remove(
								this.lastClickedLabel);
						tempSpectra.checkSizeAndPosition();
						interactions.repaint();
						this.lastClickedLabel = null;
					} else {
						this.lastClickedLabel = tempSmartPeakLabel;
					}
					break;
				}
			}
		}
	}

	/**
	 * Finds the coordinates of the top point defined by the provided limits.
	 * The returned coordinates are in units.
	 * 
	 * @param spectra
	 *            The Spectra (2D) providing the data.
	 * @param xLimit1
	 *            First horizontal limit in units.
	 * @param xLimit2
	 *            Second horizontal limit in units.
	 * @param yLimit1
	 *            First vertical limit in units.
	 * @param yLimit2
	 *            Secon vertical limit in units.
	 * @return a Point2D.Doubl containing the coordinates of the top in units.
	 */
	private Point2D.Double findPeakTop(Spectra spectra, double xLimit1, double xLimit2, double yLimit1, double yLimit2) {
		Point peakTop = new Point();
		//double x=0,y=0;
		
		int ap1 = spectra.unitsToArrayPoint(xLimit1);
		int ap2 = spectra.unitsToArrayPoint(xLimit2);
		int ss1 = spectra.unitsToSubSpectra(yLimit1);
		int ss2 = spectra.unitsToSubSpectra(yLimit2);

		int tempInt;

		if (ap1 > ap2) {
			tempInt = ap2;
			ap2 = ap1;
			ap1 = tempInt;
		}

		if (ss1 > ss2) {
			tempInt = ss2;
			ss2 = ss1;
			ss1 = tempInt;
		}

		double currentMax = java.lang.Double.MIN_VALUE;
		SpectraData spectraData = spectra.spectraData;
		double alpha, beta, gamma,p;
		for (int sub = ss1; sub <= ss2; sub++) {
			for (int point = ap1; point <= ap2; point++) {
				if (spectraData.getY(point, sub) > currentMax) {
					currentMax = spectraData.getY(point, sub);
					peakTop.x = point;
					peakTop.y = sub;
				}
			}
		}
		//double val;
		//Get y
		alpha= spectraData.getY(peakTop.x, peakTop.y-1)>1 ? 20*Math.log10(spectraData.getY(peakTop.x, peakTop.y-1)):0;
		beta= spectraData.getY(peakTop.x, peakTop.y)>1 ? 20*Math.log10(spectraData.getY(peakTop.x, peakTop.y)):0;
		gamma=spectraData.getY(peakTop.x, peakTop.y+1)>1 ? 20*Math.log10(spectraData.getY(peakTop.x, peakTop.y+1)):0;
		p=0.5*(alpha-gamma)/(alpha-2*beta+gamma);

		if(p>1||p<-1||alpha*beta*gamma==0) p=0;		
		//Get x
		alpha=spectraData.getY(peakTop.x-1, peakTop.y) > 1 ? 20*Math.log10(spectraData.getY(peakTop.x-1, peakTop.y)):0;
		beta=spectraData.getY(peakTop.x, peakTop.y) > 1 ? 20*Math.log10(spectraData.getY(peakTop.x, peakTop.y)):0;
		gamma=spectraData.getY(peakTop.x+1, peakTop.y) > 1 ? 20*Math.log10(spectraData.getY(peakTop.x+1, peakTop.y)):0;
		p=0.5*(alpha-gamma)/(alpha-2*beta+gamma);
		
		if(p>1||p<-1||alpha*beta*gamma==0)
			p=0;
		
		return new Point2D.Double(spectra.arrayPointToUnits(peakTop.x+p),spectra.subSpectraToUnits(peakTop.y+p));
	}

	// private Point steeperAscent(SpectraData tempSpectraData, int arrayPoint,
	// int subSpectra)
	/*
	 * private PointD steeperAscent(Spectra spectra, Point contactPoint, Point
	 * releasePoint) { Point startPoint=new Point();
	 * 
	 * startPoint.x=spectra.pixelsToArrayPoint((contactPoint.x+releasePoint.x)/2);
	 * startPoint.y=spectra.unitsToSubSpectra(spectra.pixelsToUnitsV((contactPoint.y+releasePoint.y)/2));
	 * 
	 * boolean peakFound=false; Point currentPoint=new Point(); Point
	 * nextPoint=new Point();
	 * 
	 * SpectraData tempSpectraData=spectra.getSpectraData();
	 * 
	 * currentPoint.setLocation(startPoint); nextPoint.setLocation(startPoint); //
	 * tempSpectraData.setActiveElement(currentPoint.y); double
	 * currentMaximum=tempSpectraData.getY(currentPoint.x, currentPoint.y);
	 * 
	 * do { for (int yCor=-1; yCor <= 1; yCor++) { //
	 * tempSpectraData.setActiveElement(currentPoint.y+yCor); for (int xCor=-1;
	 * xCor <= 1; xCor++) { if (tempSpectraData.getY(currentPoint.x+xCor,
	 * currentPoint.y+yCor) > currentMaximum) {
	 * currentMaximum=tempSpectraData.getY(currentPoint.x+xCor,
	 * currentPoint.y+yCor); nextPoint.setLocation(currentPoint.x+xCor,
	 * currentPoint.y+yCor); } } } if (currentPoint.x != nextPoint.x ||
	 * currentPoint.y != nextPoint.y) currentPoint. Location(nextPoint.x,
	 * nextPoint.y); else { peakFound=true; } } while (!peakFound); // return
	 * currentPoint; return new
	 * PointD(spectra.arrayPointToUnits(currentPoint.x),
	 * spectra.subSpectraToUnits(currentPoint.y)); }
	 */
	private Point2D.Double findPeakCenter(Spectra spectra, double xLimit1,
			double xLimit2, double yLimit1, double yLimit2) {
		int ap1 = spectra.unitsToArrayPoint(xLimit1);
		int ap2 = spectra.unitsToArrayPoint(xLimit2);
		int ss1 = spectra.unitsToSubSpectra(yLimit1);
		int ss2 = spectra.unitsToSubSpectra(yLimit2);

		int tempInt;

		if (ap1 > ap2) {
			tempInt = ap2;
			ap2 = ap1;
			ap1 = tempInt;
		}

		if (ss1 > ss2) {
			tempInt = ss2;
			ss2 = ss1;
			ss1 = tempInt;
		}

		SpectraData spectraData = spectra.spectraData;

		double totalWeight = 0;
		double weightedX = 0;
		double weightedY = 0;
		double tempY;
		for (int sub = ss1; sub <= ss2; sub++) {
			for (int point = ap1; point <= ap2; point++) {
				tempY = spectraData.getY(point, sub)
						* spectraData.getY(point, sub);
				totalWeight += tempY;
				weightedX += point * tempY;
				weightedY += sub * tempY;
			}
		}

		return new Point2D.Double(spectra.arrayPointToUnits(weightedX
				/ totalWeight), spectra.subSpectraToUnits(weightedY/ totalWeight));

	}

	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() != null
				&& interactions.getActiveDisplay() instanceof SpectraDisplay
				&& ((SpectraDisplay) interactions.getActiveDisplay()).is2D()) {
			SpectraDisplay activeDisplay = (SpectraDisplay) interactions.getActiveDisplay();

			if ((activeDisplay != null) && (activeDisplay.getFirstSpectra() != null)
					&& (activeDisplay.getFirstSpectra().getSpectraData().getDataType() == SpectraData.TYPE_2DNMR_SPECTRUM))
				this.activate();
			else
				this.deactivate();
		} else
			this.deactivate();
	}

	/** We will highlight the 1D smart peak label as well as the molecule atoms
	 * when we move the mouse over the spectrum
	 * @param spectraDisplay
	 * @param tempPoint
	 */
	private void highlightLinked(SpectraDisplay spectraDisplay, Point2D.Double tempPoint) {
		
		// long start=System.currentTimeMillis();
		
		double xmin = spectraDisplay.absolutePixelsToUnitsH(tempPoint.x-1);
		double ymin = spectraDisplay.absolutePixelsToUnitsV(tempPoint.y-1);
		double xmax = spectraDisplay.absolutePixelsToUnitsH(tempPoint.x+1);
		double ymax = spectraDisplay.absolutePixelsToUnitsV(tempPoint.y+1);
		Spectra horSpectrum = spectraDisplay.getHorRefSpectrum();
		Spectra mainSpectrum = spectraDisplay.getFirstSpectra();
		if (horSpectrum != null) { 
			// we will unselect any select SmartPeakLabel
			int nEntities=horSpectrum.getEntitiesCount();
			BasicEntity entity;
			for (int iEntity = 0; iEntity < nEntities; iEntity++) {
				entity = horSpectrum.getEntity(iEntity);
				if (entity instanceof SmartPeakLabel) {
					if (entity.isMouseover()) {
						entity.setMouseover(false);
					}
				}
			}
			SmartPeakLabel tempSmartLabel = SmartPickingHelpers.findSmartPeakLabel(xmin, xmax, horSpectrum);
			if (tempSmartLabel != null) {
				tempSmartLabel.setMouseover(true);
			}
		}
		if (mainSpectrum.isHomonuclear()) {
			SmartPeakLabel tempSmartLabel = SmartPickingHelpers.findSmartPeakLabel(ymin, ymax, horSpectrum);
			if (tempSmartLabel != null) {
				tempSmartLabel.setMouseover(true);
			}
		} else {
			Spectra verSpectrum = spectraDisplay.getVerRefSpectrum();
			if (verSpectrum !=null) {
				int nEntities=verSpectrum.getEntitiesCount();
				BasicEntity entity;
				for (int iEntity = 0; iEntity < nEntities; iEntity++) {
					entity = verSpectrum.getEntity(iEntity);
					if (entity instanceof SmartPeakLabel) {
						if (entity.isMouseover()) {
							entity.setMouseover(false);
						}
					}
				}
				SmartPeakLabel tempSmartLabel = SmartPickingHelpers.findSmartPeakLabel(ymin, ymax, verSpectrum);
				if (tempSmartLabel != null) {
					tempSmartLabel.setMouseover(true);
				}

			}
		}
		
		// System.out.println(start-System.currentTimeMillis());
		
	}
	
	
	private void analyzeSpectrum(SpectraDisplay spectraDisplay, Point2D.Double tempPointA, Point2D.Double tempPointB, MouseEvent event) {
		
		//Spectra mainSpectrum = spectraDisplay.getFirstSpectra();
		Spectra horSpectrum = spectraDisplay.getHorRefSpectrum();
		if (horSpectrum == null) {
			horSpectrum = spectraDisplay.createTraceSpectrum(false);
		}
		Spectra verSpectrum = spectraDisplay.getVerRefSpectrum();
		if (verSpectrum != null && !verSpectrum.isVertical())
			verSpectrum = null;
		if (verSpectrum == null) {
			verSpectrum = spectraDisplay.createTraceSpectrum(true);
		}
		double aX = spectraDisplay.absolutePixelsToUnitsH(tempPointA.x);
		double aY = spectraDisplay.absolutePixelsToUnitsV(tempPointA.y);
		double bX = spectraDisplay.absolutePixelsToUnitsH(tempPointB.x);
		double bY = spectraDisplay.absolutePixelsToUnitsV(tempPointB.y);
		double goodness = 0;
		double bestGoodness = java.lang.Double.MIN_VALUE;
		
		Spectra topSpectrum=null;
		Point2D.Double bestPeakTop=null,peakTop;
		
		for (Spectra currentSpectrum : spectraDisplay.getAllSpectra()) {
			if(currentSpectrum.isDrawnAs2D()) {
				// we will just take the top spectrum to add the labels
				topSpectrum=currentSpectrum;
				if(!SmartPickingHelpers.isNoise(currentSpectrum, aX, bX, aY, bY)){
					//Point2D.Double peakTop = this.findPeakCenter(mainSpectrum, aX, bX, aY, bY);
					peakTop = this.findPeakTop(currentSpectrum, aX, bX, aY, bY);
					goodness = goodness(currentSpectrum, peakTop, aX, bX);
					if (bestGoodness<goodness) {
						bestGoodness=goodness;
						bestPeakTop=peakTop;
					}
		//			System.out.println("goodness: "+goodness);
		//		} else {
		//			System.out.println("Only noise");
				}
			}
		}
		
		if (bestPeakTop==null) {	// None of the spectra contains something else than noise, we should just take the middle of the selection ...
			bestPeakTop = new Point.Double((aX+bX)/2,(aY+bY)/2);
		}
		
		// System.out.println("betterPeakTop: "+betterPeakTop);

		PeakLabel mainLabel = new PeakLabel(bestPeakTop.x, bestPeakTop.y);
		
		//Check if this peakLabel was added previously
		for(BasicEntity entity:topSpectrum.getEntities()){
			if(entity instanceof PeakLabel)
				if(mainLabel.compareTo(entity)==0){
					return;
				}
		}
		
		mainLabel.setHeterolink(false);
		topSpectrum.addEntity(mainLabel);
		mainLabel.checkSizeAndPosition();

		TreeSet<BasicEntity> linkTargets = new TreeSet<BasicEntity>();

		boolean forceHorSmartPeak = event.isShiftDown() ? true : false;
		boolean forceVerSmartPeak = event.isControlDown() ? true : false;

		//System.out.println(event.isControlDown()+" forceHorSmartPeak: "+forceHorSmartPeak+" - forceVerSmartPeak: "+forceVerSmartPeak);
		
		// special handling for the mac
		String osName = System.getProperty("os.name").toLowerCase();
		if (isDebug) System.out.println("osName=" + osName);
		if (osName.indexOf("mac os") != -1) {
			forceVerSmartPeak = event.isAltDown() ? true : false;
		}
		//Got key event
		//forceVerSmartPeak=!forceVerSmartPeak;

		if (horSpectrum != null) {
			
			SmartPeakLabel tempSmartLabel = SmartPickingHelpers.putSmartPeakLabels(Math.max(aX, bX), Math.min(aX, bX),
							horSpectrum, interactions, forceHorSmartPeak);
			
			// we have a fake spectrum and no peak value
			if (horSpectrum.getSpectraData().getTitle().equals(PredictionData.projectedTrace)
					&& (tempSmartLabel == null || forceHorSmartPeak)) {
				tempSmartLabel = horSpectrum.getPredictionData().addFakeSmartPeak(horSpectrum, bestPeakTop.x);
			}
			if (tempSmartLabel != null) linkTargets.add(tempSmartLabel);
		}

		if (topSpectrum.isHomonuclear()) {
			if (horSpectrum != null) {
				SmartPeakLabel tempSmartLabel = SmartPickingHelpers.putSmartPeakLabels(Math.max(aY, bY), Math.min(aY, bY),
								horSpectrum, interactions, forceVerSmartPeak);
				// we have a fake spectrum and no peak value
				if (horSpectrum.getSpectraData().getTitle().equals(
						PredictionData.projectedTrace)
						&& (tempSmartLabel == null || forceVerSmartPeak)) {
					tempSmartLabel = horSpectrum.getPredictionData().addFakeSmartPeak(horSpectrum, bestPeakTop.y);
				}
				if (tempSmartLabel != null)
					linkTargets.add(tempSmartLabel);
			}
			/*if (verSpectrum != null&&forceVerSmartPeak) {
				SmartPeakLabel tempSmartLabel = SmartPickingHelpers.putSmartPeakLabels(Math.max(aY, bY), Math.min(aY, bY),
						verSpectrum, interactions, forceVerSmartPeak);
				// we have a fake spectrum and no peak value
				if (verSpectrum.getSpectraData().getTitle().equals(
						PredictionData.projectedTrace)
						&& (tempSmartLabel == null || forceVerSmartPeak)) {
					tempSmartLabel = verSpectrum.getPredictionData().addFakeSmartPeak(verSpectrum, bestPeakTop.y);
				}
				if (tempSmartLabel != null)
					linkTargets.add(tempSmartLabel);
			}*/
		} else {
			if (verSpectrum != null) {
				SmartPeakLabel tempSmartLabel = SmartPickingHelpers
						.putSmartPeakLabels(Math.max(aY, bY), Math.min(aY, bY), verSpectrum, interactions, forceVerSmartPeak);
				// we have a fake spectrum and no peak value
				if (verSpectrum.getSpectraData().getTitle().equals(PredictionData.projectedTrace)
						&& (tempSmartLabel == null || forceVerSmartPeak)) {
					tempSmartLabel = verSpectrum.getPredictionData().addFakeSmartPeak(verSpectrum, bestPeakTop.y);
				}
				if (tempSmartLabel != null)
					linkTargets.add(tempSmartLabel);

			}
		}

		Iterator<BasicEntity> iter = linkTargets.iterator();
		//int nLinks = 0;
		while (iter.hasNext()) {
		//	System.out.println("nLinks=" + (++nLinks));
			interactions.createLink(mainLabel, iter.next());
		}

	}

	private double goodness(Spectra spectra, Double peakTop,	double xLimit1, double xLimit2) {
		int ap1 = spectra.unitsToArrayPoint(xLimit1);
		int ap2 = spectra.unitsToArrayPoint(xLimit2);
		int ss1 = spectra.unitsToSubSpectra(peakTop.y);

		int tempInt;

		if (ap1 > ap2) {
			tempInt = ap2;
			ap2 = ap1;
			ap1 = tempInt;
		}

		double goodnessMeasure = 0;
		SpectraData spectraData = spectra.spectraData;
		//We can improve it. We don't need to calculate the average over all points.
		//Just a few are enough.
		for (int point = ap1; point <= ap2-1; point++) {
			goodnessMeasure+=Math.abs(spectraData.getY(point, ss1)-spectraData.getY(point+1, ss1));
			
			/*
			 * val= spectraData.getY(point, ss1)-spectraData.getY(point+1, ss1);
			 * if(val > 0)
				goodnessMeasure+=spectraData.getY(point, ss1);
			else
				goodnessMeasure-=spectraData.getY(point, ss1);
				*/
		}
		
		return goodnessMeasure;
	}
}
