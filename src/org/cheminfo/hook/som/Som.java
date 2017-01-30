package org.cheminfo.hook.som;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;



public class Som {

	int xGridSize;		// width of the grid
	int yGridSize;		// height of the grid
	int gridDimension;	// dimension of the objects

	SomObject grid[][];
	double averageNeighbourDistance[][];

	private final static boolean VERBOSE	=	false;
	
	int maxDistance;
	
	// means that the distance of influence at the beginning is the dimension /  LEARNING_RATIO
	private final static int LEARNING_RATIO=10;
	
	/** The best matching unit
	 */
	
	Point bmu=new Point();
	

	/**
	 * A SOM is characterized by: width, height, dimesion of objects
	 * @param values
	 */
	
	public Som (SomObject[][] grid) {
		
		this.grid=grid;
		this.xGridSize=grid.length;
		this.yGridSize=grid[0].length;
		this.maxDistance=xGridSize/LEARNING_RATIO;
		averageNeighbourDistance = new double[xGridSize][yGridSize];
	}
	/**
	 * Default constructor. Useful for loading a SOM from a file.
	 */
	public Som(){
		
	}
	
	/** 
	 * This method will generate random number to initialize the som
	 *
	 */
	
	public void initialize () {
		for (int x = 0; x<xGridSize; x++) {
		    for (int y = 0; y<yGridSize; y++) {
		    	grid[x][y].initialize();
		    }
	    }
	}

	/**
	 * This method will calculate the distance between a point and the neighbor
	 * We will take into account the 8 neighbors
	 *
	 */
	public double calculateDerivative () {
		int xMinusOne, xPlusOne, yMinusOne, yPlusOne;
		double maxValue=0;
		double minValue=Double.MAX_VALUE;
		double smoothness=0;
		for (int x = 0; x<xGridSize; x++) {
		    for (int y = 0; y<yGridSize; y++) {
		    	if (x==0) {xMinusOne=xGridSize-1;} else {xMinusOne=x-1;}
		    	if (x==xGridSize-1) {xPlusOne=0;} else {xPlusOne=x+1;}
		    	if (y==0) {yMinusOne=yGridSize-1;} else {yMinusOne=y-1;}
		    	if (y==yGridSize-1) {yPlusOne=0;} else {yPlusOne=y+1;}	  
		    	
		    	averageNeighbourDistance[x][y]=(
		    		grid[x][y].distanceTo(grid[xMinusOne][yMinusOne]) +
		    		grid[x][y].distanceTo(grid[xMinusOne][y]) +
		    		grid[x][y].distanceTo(grid[xMinusOne][yPlusOne]) +
		    		grid[x][y].distanceTo(grid[x][yMinusOne]) +
		    		grid[x][y].distanceTo(grid[x][yPlusOne]) +
		    		grid[x][y].distanceTo(grid[xPlusOne][yMinusOne]) +
		    		grid[x][y].distanceTo(grid[xPlusOne][yPlusOne]) +
		   			grid[x][y].distanceTo(grid[xPlusOne][y])
		    	)/8;
	
		    	smoothness+=averageNeighbourDistance[x][y];
		    	if (averageNeighbourDistance[x][y]>maxValue) {maxValue=averageNeighbourDistance[x][y];}
		    	if (averageNeighbourDistance[x][y]<minValue) {minValue=averageNeighbourDistance[x][y];}
		    }
	    }
		// second step, we normalize
		double ratio=1/(maxValue-minValue);
		for (int x = 0; x<xGridSize; x++) {
		    for (int y = 0; y<yGridSize; y++) {
		    	averageNeighbourDistance[x][y]=(averageNeighbourDistance[x][y]-minValue)*ratio;
		    	if (VERBOSE)
		    		System.out.println(averageNeighbourDistance[x][y]);
		    }
		}
	    return smoothness/(xGridSize*yGridSize);
	}
	
	/**
	 * This method calculates the average error between the BMU and the object value
	 * 
	 * @param objectSet
	 * @return
	 */
	public double getAverageDistance(SomObject objectSet[]) {
		double error=0;
		
		for (int i = 0; i<objectSet.length; i++) {
			this.getBestMatchingUnit(objectSet[i]);
			error+=objectSet[i].distanceTo(grid[bmu.x][bmu.y]);
		}
		
		return error/objectSet.length;
	}
	
	
	/** This method will find the element in the grid that is the closest to
	 * the currentObject.
	 */

	private void getBestMatchingUnit(SomObject currentObject) {
		double minError=Double.MAX_VALUE;
		double error=0;
		for (int x = 0; x<xGridSize; x++) {
		    for (int y = 0; y<yGridSize; y++) {
		    	error=currentObject.distanceTo(grid[x][y]);
		    	if (error<minError) {
		    		minError=error;
					bmu.x=x;
					bmu.y=y;	    		
		    	}
		    }
	    }
		//System.out.println(minError);
	}
	
	/** This method will find the element in the grid that is the closest to
	 * the currentObject.
	 * This method  gives the exact position and not only an integer.
	 */
	
	public Point2D.Double getBestMatchingPoint(SomObject currentObject) {
		this.getBestMatchingUnit(currentObject);
		
		int xMinusOne, xPlusOne, yMinusOne, yPlusOne;
		
    	if (bmu.x==0) {xMinusOne=xGridSize-1;} else {xMinusOne=bmu.x-1;}
    	if (bmu.x==xGridSize-1) {xPlusOne=0;} else {xPlusOne=bmu.x+1;}
    	if (bmu.y==0) {yMinusOne=yGridSize-1;} else {yMinusOne=bmu.y-1;}
    	if (bmu.y==yGridSize-1) {yPlusOne=0;} else {yPlusOne=bmu.y+1;}	    	
    	
    	Point2D.Double bestPosition=new Point2D.Double();
    	
    	double d1, d2;
    	
    	d1=currentObject.distanceTo(grid[xMinusOne][bmu.y]);
    	d2=currentObject.distanceTo(grid[xPlusOne][bmu.y]);
    	
    	bestPosition.x=bmu.x+(d1-d2)/(2*(d1+d2));
 
		d1=currentObject.distanceTo(grid[bmu.x][yMinusOne]);
		d2=currentObject.distanceTo(grid[bmu.x][yPlusOne]);

    	
    	bestPosition.y=bmu.y+(d1-d2)/(2*(d1+d2));
		
    	// We add some random just to check that the points are there !!!
    	 //bestPosition.x+=Math.random()*0.5-0.25;
    	 //bestPosition.y+=Math.random()*0.5-0.25;
    	
		return bestPosition;
	}
	
	/** This method will change the specified cell to reflect the currentObject
	 * value.
	 * It will depend of the distance to the value and the epoch.
	 */
	
	public void learn(SomObject currentObject, double epochEffect) {
	//	System.out.println(epochEffect);
		getBestMatchingUnit(currentObject);
		
		
		// we need to take the bmu
		// we need to calculate a radius
		// we need to calculate the distance and the epoch effects
		
		int minX=bmu.x-maxDistance;
		int maxX=bmu.x+maxDistance;
		int minY=bmu.y-maxDistance;
		int maxY=bmu.y+maxDistance;
		int realX;
		int realY;
		double currentDistance;
		double distanceEffect;
		
		for (int x=minX; x<=maxX; x++) {
		    for (int y=minY; y<=maxY; y++) {
		    	// Check for folding of x
		    	if (x<0) {
		    		realX=x+xGridSize;
		    	} else if (x>=xGridSize) {
		    		realX=x-xGridSize;
		    	} else {realX=x;}
		    	// Check for folding of y
		    	if (y<0) {
		    		realY=y+yGridSize;
		    	} else if (y>=yGridSize) {
		    		realY=y-yGridSize;
		    	} else {realY=y;}

		    	// we will calculate the distance correctly.
		    	// not sure this is not too slow !!!
		    	currentDistance=Math.sqrt((bmu.x-x)*(bmu.x-x)+(bmu.y-y)*(bmu.y-y))*epochEffect;
		    	
		    	distanceEffect=Math.exp(-2*(currentDistance/maxDistance)*(currentDistance/maxDistance));
		    	// System.out.println("BMU:"+x+" - "+y+" current eal:"+realX+" - "+realY+" - "+currentDistance+" - "+distanceEffect);		    	
		    	grid[realX][realY].learn(currentObject, distanceEffect, epochEffect);
		    	
		    }
	    }
	}
	/**
	 * Saves a SOM in text file
	 * @param filename
	 * @throws IOException
	 */
	protected void dumpSOM(String filename) throws IOException{
		BufferedWriter bfwriter = new BufferedWriter(new FileWriter(filename));
		//Write the header
		bfwriter.append("#className="+grid[0][0].getClass().getCanonicalName()+"\n");
		bfwriter.append("//gridsize\n#xGridSize="+xGridSize+"\n#yGridSize="+yGridSize+"\n");
		bfwriter.append("//SOM grid\n//Centroid\tX\tY\n");
		//Write the data
		for(int i=xGridSize-1;i>=0;i--){
			for(int j=yGridSize-1;j>=0;j--){
				bfwriter.append("\t"+grid[i][j].toString()+"\t"+i+"\t"+j+"\n");
			}
		}
		bfwriter.close();
	}
	/**
	 * Loads a SOM from a text file generated by the function dumpSOM
	 * @param filename
	 * @throws FileNotFoundException
	 */
	protected void loadSOM(String filename) throws FileNotFoundException{
			BufferedReader dfReader = new BufferedReader(new FileReader(filename));
			String line="";
			xGridSize=0;
			yGridSize=0;
			String className=null;
			try {
				while(xGridSize*yGridSize==0||className==null){
					line=dfReader.readLine();
					if(!line.startsWith("//")){
						if(line.startsWith("#")){
							if(line.contains("#xGridSize"))
								xGridSize=Integer.parseInt(line.substring(line.indexOf("=")+1));
							if(line.contains("#yGridSize"))
								yGridSize=Integer.parseInt(line.substring(line.indexOf("=")+1));
							if(line.contains("#className"))
								className=line.substring(line.indexOf("=")+1);
						}
					}
					
				}
				Class param[]={double[].class};
				Constructor con=Class.forName(className).getConstructor(param);
				this.grid = new SomObject[xGridSize][yGridSize];
				String[] strData;
				while((line=dfReader.readLine())!=null){
					if(!line.startsWith("//")){
							String[] tokensLine = line.split("\t");
							int x=Integer.parseInt(tokensLine[1]);
							int y=Integer.parseInt(tokensLine[2]);
							strData=tokensLine[0].split(" ");
							double[] data = new double[strData.length];
							for(int i=strData.length-1;i>=0;i--)
								data[i]=Double.parseDouble(strData[i]);
							grid[x][y]=(SomObject)con.newInstance(data);
 						
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println("Could not create an object for class: "+className);
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			System.out.println("SOM loaded. SomObject: "+className+" grid size: ("+xGridSize+"x"+yGridSize+")");
		
	}
}
