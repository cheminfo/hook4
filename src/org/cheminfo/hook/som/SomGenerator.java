package org.cheminfo.hook.som;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;

import org.cheminfo.hook.graphdraw2.GraphDisplay2;
import org.cheminfo.hook.graphdraw2.SerieData;
import org.cheminfo.hook.graphdraw2.SerieEntity;
import org.cheminfo.hook.graphdraw2.XYPointEntity;

public class SomGenerator extends Thread {
	public int xGridSize = 20;
	public int yGridSize = 20;
	public int numberEpoch = 40;
	public int objectDimension = 512;

	final static int SOM_DATATYPE_LEARNING_SET = 0;
	final static int SOM_DATATYPE_TEST1_SET = 1;
	final static int SOM_DATATYPE_TEST2_SET = 2;
	final static int SOM_DATATYPE_TEST3_SET = 3;
	final static int SOM_DATATYPE_DISTANCE = 4;

	final static String[] SOM_DATA_NAME = { "learning set", "test set 1",
			"test set 2", "test set 3" };

	final static int DATASERIE_TYPE_BACKGROUND = 0;
	final static int DATASERIE_TYPE_BACKGROUND50 = 1;
	final static int DATASERIE_TYPE_DOT = 2;

	private HashMap allSeriesData = new HashMap();

	SomObjectND[][] somGrid;
	SomObjectND[] somLearning, somTest1, somTest2, somTest3;
	Som som;

	private GraphDisplay2 graphDisplay;
	private String matchEvolution;

	public void setGridSize(int size) {
		xGridSize = size;
		yGridSize = size;
	}

	public void setNumberEpoch(int numberEpoch) {
		this.numberEpoch = numberEpoch;
		System.out.println(this.numberEpoch);
	}

	public SomGenerator(GraphDisplay2 display) {
		this.graphDisplay = display;

	}

	public void setObjectDimension(int objectDimension) {
		this.objectDimension = objectDimension;
	}

	public void addRandomData(int type, int number) {
		// should be replaced by switch
		SomObjectND[] currentSomObject = null;

		if (type == SOM_DATATYPE_LEARNING_SET) {
			somLearning = new SomObjectND[number];
			currentSomObject = somLearning;
			System.out
					.println(type + " - " + number + " - " + currentSomObject);
		} else if (type == SOM_DATATYPE_TEST1_SET) {
			somTest1 = new SomObjectND[number];
			currentSomObject = somTest1;
		} else if (type == SOM_DATATYPE_TEST2_SET) {
			somTest2 = new SomObjectND[number];
			currentSomObject = somTest2;
		} else if (type == SOM_DATATYPE_TEST3_SET) {
			somTest3 = new SomObjectND[number];
			currentSomObject = somTest3;
		}
		for (int x = 0; x < number; x++) {
			currentSomObject[x] = new SomObjectND(objectDimension);
			currentSomObject[x].initialize();
		}

	}
	
	public void setLearningData(SomObjectND[] learningData){
		this.somLearning=learningData;
		System.out.println(this.somLearning.length+" "+this.somLearning[0].data.length);
		this.objectDimension=this.somLearning[0].data.length;
	}
	void startCalculation() {
		// we define the SOM grid

		somGrid = new SomObjectND[xGridSize][yGridSize];

		for (int x = 0; x < xGridSize; x++) {
			for (int y = 0; y < yGridSize; y++) {
				somGrid[x][y] = new SomObjectND(objectDimension);
			}
		}
		// we create a SOM and we initialize it
		som = new Som(somGrid);
		som.initialize();

		SerieData serieData = new SerieData();
		serieData.init(xGridSize * yGridSize);
		serieData.setDataAspect(XYPointEntity.DATA_ASPECT_FILLGRID);
		for (int x = 0; x < xGridSize; x++) {
			for (int y = 0; y < yGridSize; y++) {
				serieData.setPoint(x * xGridSize + y, x, y, 0.5, 0.5, "",somGrid[x][y]);
			}
		}
		serieData.setName("test");
		this.graphDisplay.addSerie(serieData);
		//this.graphDisplay.getSerieByName("test").setVisible(false);

		SerieData learningData = new SerieData();
		learningData.init(somLearning.length);
		learningData.setDataAspect(XYPointEntity.DATA_ASPECT_POINT);
		for (int x = 0; x < somLearning.length; x++) {
			learningData.setPoint(x, Math.random() * xGridSize, Math.random()
					* yGridSize, 0, 0, "", somLearning[x]);
		}
		learningData.setName("Learning Set");
		this.graphDisplay.addSerie(learningData);

		SerieEntity learningSerie = this.graphDisplay
				.getSerieByName("Learning Set");
		for (int x = 0; x < somLearning.length; x++) {
			learningSerie.getPoint(x).setPrimaryColor(
					((SomObject) learningSerie.getPoint(x).getReference())
							.getColor());
		}
		// learningSerie.setVisible(false);
		this.graphDisplay.checkSizeAndPosition();

		this.graphDisplay.fullout();

		this.start();
	}

	String getMatchEvolution() {
		return matchEvolution;
	}

	public void run() {
		matchEvolution = "eopch\tepoch effect\tlearning set average distance\tsmoothness\r";
		matchEvolution += "\t" + "\t" + som.getAverageDistance(somLearning) + "\r";

		for (int j = 0; j < numberEpoch; j++) {
			double epochEffect = 1 - (double) j / numberEpoch;
			this.iteration();
			for (int i = 0; i < somLearning.length; i++) {
				som.learn(somLearning[i], epochEffect);
			}

			double averageDistance = som.getAverageDistance(somLearning);
			//this.graphDisplay.getInteractiveSurface().getUserDialog()
			//		.setMessageText("Average error: " + averageDistance);
			System.out.println("Average error: " + averageDistance);
			double smoothness = som.calculateDerivative();

			matchEvolution += j + "\t" + epochEffect + "\t" + averageDistance
					+ "\t" + smoothness + "\r";
			
			// try {  Thread.sleep(1000); } catch (InterruptedException e) {}
			 
		}
		this.iteration();
	}

	public void iteration() {
		SerieEntity serie = this.graphDisplay.getSerieByName("test");
		SerieEntity learningSerie = this.graphDisplay.getSerieByName("Learning Set");

		// we change the colors of the grid
		/*
		for (int x = 0; x < xGridSize; x++) {
			for (int y = 0; y < yGridSize; y++) {
				serie.getPoint(x + y * xGridSize).setPrimaryColor(((SomObject)serie.getPoint(x + y * xGridSize).getReference()).getColor());
			}
		}
		*/

		/* Show the smoothness */
		
		for (int x = 0; x < xGridSize; x++) {
			for (int y = 0; y < yGridSize; y++) {
				int greyLevel = (int) (som.averageNeighbourDistance[x][y] * 255);
				serie.getPoint(x + y * xGridSize).setPrimaryColor(
						new Color(greyLevel, greyLevel, greyLevel));
			}
		}
	
		// we find the coordinates of the bmu (Best Matching Unit)

		XYPointEntity currentPoint;
		Point2D.Double coord;

		for (int i = 0; i < somLearning.length; i++) {
			currentPoint = learningSerie.getPoint(i);
			coord = som.getBestMatchingPoint((SomObject) currentPoint
					.getReference());
			currentPoint.setX(coord.x);
			currentPoint.setY(coord.y);
		}
		learningSerie.update();

		/*
		 * for each point we should get the getBestMatchingPoint we could change
		 * the diameter of the point based on the error ?
		 * 
		 * What is the definition of a good map ?
		 * 
		 * Good distribution ? We should be able to create a graph of the
		 * distribution, number of point by grid cell Javascript to control
		 * which layer is shown ? Or userdialog ?
		 * 
		 * Mouse over text
		 * 
		 */
		;
		serie.forceRedraw();
		System.out.println("YOOP YAAP");
		learningSerie.forceRedraw();

		this.graphDisplay.getInteractiveSurface().repaint();

	}
	
	void setSerieData(int dataType, int serieDataType, boolean visible,
			String colorScheme, String sizeScheme) {
		ExtendedSerieData currentSerieData = new ExtendedSerieData();
		int size = 0;
		if (dataType == SOM_DATATYPE_LEARNING_SET) {
			size = somLearning.length;
		} else if (dataType == SOM_DATATYPE_TEST1_SET) {
			size = somTest1.length;
		} else if (dataType == SOM_DATATYPE_TEST2_SET) {
			size = somTest2.length;
		} else if (dataType == SOM_DATATYPE_TEST3_SET) {
			size = somTest3.length;
		}

		int dataAspect = 0;
		if ((serieDataType == DATASERIE_TYPE_BACKGROUND)
				|| (serieDataType == DATASERIE_TYPE_BACKGROUND50)) {
			dataAspect = XYPointEntity.DATA_ASPECT_FILLGRID;
		} else if (serieDataType == DATASERIE_TYPE_DOT) {
			dataAspect = XYPointEntity.DATA_ASPECT_POINT;
		}

		currentSerieData.init(size);
		currentSerieData.setDataAspect(dataAspect);

		if (serieDataType == DATASERIE_TYPE_BACKGROUND) {
			for (int x = 0; x < xGridSize; x++) {
				for (int y = 0; y < yGridSize; y++) {
					currentSerieData.setPoint(x * xGridSize + y, x, y, 0.5, 0.5, "", somGrid[x][y]);
				}
			}
		} else if (serieDataType == DATASERIE_TYPE_BACKGROUND50) {
			for (int x = 0; x < xGridSize; x++) {
				for (int y = 0; y < yGridSize; y++) {
					currentSerieData.setPoint(x * xGridSize + y, x, y, 0.25, 0.25, "", somGrid[x][y]);
				}
			}
		} else if (serieDataType == DATASERIE_TYPE_DOT) {
			for (int x = 0; x < somLearning.length; x++) {
				currentSerieData.setPoint(x, Math.random() * xGridSize, Math.random() * yGridSize, 0, 0, "", somLearning[x]);
			}
		}

		currentSerieData.setName(SOM_DATA_NAME[dataType]);
		this.graphDisplay.addSerie(currentSerieData);

		allSeriesData.put(SOM_DATA_NAME[dataType], currentSerieData);
	}

	/**
	 * This method will redraw everything. It may be time consuming because a
	 * change of type is possible at this level and new series will be created
	 * depending the flag.
	 * 
	 */
	void redrawSeries(boolean newSeries) {

	}

	private class ExtendedSerieData extends SerieData {
		SerieData serieData;
		String colorScheme = "";
		String sizeScheme = "";
		boolean visibility = true;
	}

}
