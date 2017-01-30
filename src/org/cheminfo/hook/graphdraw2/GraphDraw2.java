package org.cheminfo.hook.graphdraw2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Vector;

import javax.swing.JApplet;
import javax.swing.JPanel;

import org.cheminfo.hook.framework.AboutActionButton;
import org.cheminfo.hook.framework.BasicDisplay;
import org.cheminfo.hook.framework.ColorActionButton;
import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.DynamicLayout;
import org.cheminfo.hook.framework.EraseActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.framework.LinkActionButton;
import org.cheminfo.hook.framework.PrimarySecondaryActionButton;
import org.cheminfo.hook.framework.SelectActionButton;
import org.cheminfo.hook.framework.StackLayout;
import org.cheminfo.hook.framework.UserDialog;
import org.cheminfo.hook.graphics.TextEntity;
import org.cheminfo.hook.optimizer.ConjugateGradientOptimizer;
import org.cheminfo.hook.optimizer.LevenbergMarquardt;
import org.cheminfo.hook.optimizer.LinearSimpleOptimizer2;
import org.cheminfo.hook.optimizer.Matrix;
import org.cheminfo.hook.optimizer.Optimizer;
import org.cheminfo.hook.optimizer.SigmoidProvider;
import org.cheminfo.hook.optimizer.SigmoidProviderLMA;
import org.cheminfo.hook.util.FlexiDataConverter;
import org.cheminfo.hook.util.FlexiDataDescription;
import org.cheminfo.hook.util.FlexiDataPoint;
import org.cheminfo.hook.util.Sorter;



public class GraphDraw2 extends JApplet implements ActionListener, Runnable
{
	
	JPanel leftPanel;
	UserDialog bottomPanel;
	JPanel topPanel;
	JPanel rightPanel;
	InteractiveSurface	mainPanel;
	JPanel	colorPanel;
	JPanel	propertiesPanel;
	JPanel	commonPanel;
	JPanel	specificPanel;
	JPanel	applicationPanel=null;
	SelectActionButton selectButton;
	
	final static boolean test=true;
	
	int totalWidth, totalHeight;	
	final static int topHeight=24;		// the height of the top panel
	final static int bottomHeight=24;		// the height of the bottom panel
	final static int leftWidth=24;		// the width of the left panel
	final static int rightWidth=5;		// the width of the right panel

	ImageButton			userDialogButton;
	
	private Thread	loadingThread=null;
	
	private URL urlLocation=null;	// the complete URL to the applet
	
	
	String baseDirectory=null;
	URL docBase;
	URL appletBase;
	
	protected GraphDisplay2 mainDisplay;
	
	public GraphDraw2()
	{
	}
	
	public void startup()
	{
	}
	
	public void init()
	{
		try {
    		String className=getClass().getName();
    		String packageName="";
    		int lastPoint=className.lastIndexOf(".");
    		if (lastPoint>0) packageName=className.substring(0,lastPoint+1).replace((char)46,(char)47);   		
    		urlLocation=new URL(getCodeBase()+packageName);
       	} catch (Exception e) {};
		
		docBase=getDocumentBase();
		appletBase=this.getCodeBase();
		

		baseDirectory=getCodeBase().toString();
		
		startup();
		frameWork();
				
		if (loadingThread == null)
		{
			loadingThread = new Thread(this, "Loader");
			loadingThread.start();
		}
	}

	public void frameWork()
	{
		DefaultActionButton.setButtonUpImage(getImage("buttonUp.gif"));
		DefaultActionButton.setButtonDownImage(getImage("buttonDown.gif"));

		totalWidth=this.getSize().width;
		totalHeight=this.getSize().height;
		
		String optionsParameter=null;
		try {
			optionsParameter=getParameter("OPTIONS");
		} catch (NullPointerException npe) {};
		if (optionsParameter!=null) optionsParameter=optionsParameter.toLowerCase();

		int topWidth=totalWidth;
		int bottomWidth=totalWidth;
		int leftHeight=totalHeight-topHeight-bottomHeight;
		int rightHeight=leftHeight;

		this.setLayout(new BorderLayout());


		rightPanel=new JPanel();
		rightPanel.setBackground(Color.lightGray);
		rightPanel.setLayout(new BorderLayout());
//		rightPanel.add(new EmptyBox(rightWidth-1, rightWidth-1));
		rightPanel.setSize(rightWidth,rightHeight);
		rightPanel.doLayout();
		
		topPanel=new JPanel();
		topPanel.setBackground(Color.lightGray);
		StackLayout tempLayout=new StackLayout(StackLayout.HORIZONTAL, 24, 0);
		tempLayout.setPreferredSize(topWidth,topHeight);
		topPanel.setLayout(tempLayout);
//		topPanel.setBackground (Color.lightGray);
		
		leftPanel=new JPanel();
		leftPanel.setBackground(Color.lightGray);
		tempLayout=new StackLayout(StackLayout.VERTICAL, 0, 24);
		tempLayout.setPreferredSize(leftWidth,leftHeight);
		leftPanel.setLayout(tempLayout);
//		leftPanel.setBackground (Color.lightGray);

		mainPanel=new InteractiveSurface();

		mainPanel.setLayout(null);

		bottomPanel=new UserDialog(mainPanel);
		bottomPanel.setSize(bottomWidth,bottomHeight);
		bottomPanel.setPreferredSize(new Dimension(bottomWidth,bottomHeight));
		bottomPanel.addActionListener(mainPanel);

		
		commonPanel=new JPanel(new DynamicLayout(DynamicLayout.VERTICAL, 0, 0));
		commonPanel.setBackground(Color.lightGray);
		selectButton=new SelectActionButton(getImage("select.gif"), "Select", mainPanel);
		commonPanel.add(selectButton);
		commonPanel.add(new EraseActionButton(getImage("erase.gif"), "Erase", mainPanel));

		if (optionsParameter == null || (optionsParameter != null && optionsParameter.indexOf("tools") != -1))
		{
			commonPanel.add(new LinkActionButton(getImage("link.gif"), "Link", mainPanel));
//			commonPanel.add(new SendBackActionButton(getImage("sendback.gif"), "Send to Back", interactions));
//			commonPanel.add(new BringToFrontActionButton(getImage("bringfront.gif"), "Bring to Front", interactions));
		}

		leftPanel.add(commonPanel);


		specificPanel=new JPanel(new DynamicLayout(DynamicLayout.VERTICAL, 0, 0));
		specificPanel.setBackground(Color.lightGray);
		specificPanel.add(new FullGraphActionButton(getImage("zoomfull.gif"), "Fullout Graph", mainPanel));
		specificPanel.add(new Graph2DZoomActionButton(getImage("zoom.gif"), "Zoom in", mainPanel));
		specificPanel.add(new GraphSlideActionButton(getImage("hmove.gif"), "Slide Graph", mainPanel));
		
			
		leftPanel.add(specificPanel);


		topPanel.add(new AboutActionButton(getImage("about.gif"), "About", mainPanel));

//		if (optionsParameter == null || (optionsParameter != null && optionsParameter.indexOf("undo") != -1))
//			topPanel.add(new UndoActionButton(getImage("undo.gif"), "Undo", mainPanel));
			

//		if (applicationPanel != null) topPanel.add(applicationPanel);
		

		if (optionsParameter == null || (optionsParameter != null && optionsParameter.indexOf("colors") != -1))
		{
			colorPanel=new JPanel(new StackLayout(StackLayout.HORIZONTAL, 0 ,0));
			colorPanel.setBackground(Color.lightGray);
	
			colorPanel.add(new PrimarySecondaryActionButton(getImage("foreground.gif"), "Primary / Secondary Color", mainPanel));
			colorPanel.add(new ColorActionButton(Color.black, "Black", mainPanel));
			colorPanel.add(new ColorActionButton(Color.gray, "Gray", mainPanel));
			colorPanel.add(new ColorActionButton(Color.yellow, "Yellow", mainPanel));
			colorPanel.add(new ColorActionButton(Color.green, "Green", mainPanel));
			colorPanel.add(new ColorActionButton(Color.magenta, "Magenta", mainPanel));
			colorPanel.add(new ColorActionButton(Color.cyan, "Cyan", mainPanel));
			colorPanel.add(new ColorActionButton(Color.blue, "Blue", mainPanel));
			colorPanel.add(new ColorActionButton(Color.red, "Red", mainPanel));
			colorPanel.add(new ColorActionButton(Color.white, "White", mainPanel));
			colorPanel.add(new ColorActionButton(getImage("transparent.gif"), null, "Transparent", mainPanel));
			
			
			colorPanel.doLayout();
			topPanel.add(colorPanel);
		}
		
		
		this.add("Center",mainPanel);
		this.add("South", bottomPanel);
		this.add("West", leftPanel);
		this.add("North", topPanel);
		this.add("East", rightPanel);

//		this.doLayout();
		this.validate();
		
		mainDisplay = new GraphDisplay2();
		mainDisplay.setSize(mainPanel.getWidth(), mainPanel.getHeight());
		mainPanel.addEntity(mainDisplay);

		
		mainPanel.setActiveEntity(mainDisplay);
		mainDisplay.setErasable(false);

		mainDisplay.setLocation(0,0);

		
		mainPanel.setUserDialog(bottomPanel);
		
		mainDisplay.checkInteractiveSurface();
		selectButton.click();
	}

		
	public void run()
	{
		try {
			loadingThread.sleep(200);
		} catch (Exception ex) {}

//		getSourceFile();
	}
	


	private Color parseColor(String color)
	{
		Color	newColor=Color.black;
		
		if (color.toLowerCase().compareTo("black") == 0)	newColor=Color.black;
		else if (color.toLowerCase().compareTo("gray") == 0) newColor=Color.gray;
		else if (color.toLowerCase().compareTo("yellow") == 0) newColor=Color.yellow;
		else if (color.toLowerCase().compareTo("green") == 0) newColor=Color.green;
		else if (color.toLowerCase().compareTo("cyan") == 0) newColor=Color.cyan;
		else if (color.toLowerCase().compareTo("magenta") == 0) newColor=Color.magenta;
		else if (color.toLowerCase().compareTo("blue") == 0) newColor=Color.blue;
		else if (color.toLowerCase().compareTo("red") == 0) newColor=Color.red;
		else if (color.toLowerCase().compareTo("white") == 0) newColor=Color.white;
	
		return newColor;
	}
	
	public void setDataAspect(String aspect)
	{
		int dataAspect=XYPointEntity.DATA_ASPECT_POINT;
		if (aspect.compareTo("point") == 0)
			dataAspect=XYPointEntity.DATA_ASPECT_POINT;
		else if (aspect.compareTo("bar") == 0)
			dataAspect=XYPointEntity.DATA_ASPECT_BAR;
		else if (aspect.compareTo("fillgrid") == 0)
			dataAspect=XYPointEntity.DATA_ASPECT_FILLGRID;
		
		this.mainDisplay.setDataAspect(dataAspect);
	}	
	
	public void useBigFonts()
	{
		this.mainPanel.useBigFonts(true);
		this.mainPanel.repaint();
	}
	
	public void addSerieFromFlexi(String flexiString, String name)
	{
		this.addSerieFromFlexi(flexiString, name, "point", "x", "y", "dx", "dy", "red", "");
	}
	
	/**
	 * Adds a serie from a flexi-formatted string.
	 * @param flexiString	string containing the tab-delimited representation of the data.
	 * @param name name of the serie.
	 * @param aspect aspect of the data (point, bar, fillgrid).
	 * @param xField name of the field containtng the x coordinate (if empty the points are indexed).
	 * @param yField name of the field containing the y coordinate. MANDATORY.
	 * @param xErrField name of the field containing the x error.
	 * @param yErrField name of the field containing the y error.
	 * @param color string containing the name of the color for this serie.
	 * @param addon string containing the name of the addon (not yet available).
	 */
	public void addSerieFromFlexi(String flexiString, String name, String aspect, String xField, String yField, String xErrField, String yErrField, String color, String addon)
	{
		this.setDataAspect(aspect);
		
		FlexiDataConverter converter=new FlexiDataConverter();
		FlexiDataPoint[] points = converter.convert(new BufferedReader(new StringReader(flexiString)), new Vector());

		SerieData serieData=new SerieData();
		serieData.setName(name);
		serieData.init(points.length);
		serieData.setColor(this.parseColor(color));
		
		if (addon.toLowerCase().compareTo("3sigma") == 0)
			serieData.setAddon(SerieData.ADDON_3SIGMA);
		
		if (points.length > 0)
		{
			FlexiDataDescription description=points[0].getDescription();
			
			int xFieldIndex=description.getIndexForFieldName(xField);
			int yFieldIndex=description.getIndexForFieldName(yField);
			int xErrFieldIndex=description.getIndexForFieldName(xErrField);
			int yErrFieldIndex=description.getIndexForFieldName(yErrField);
			
			double xErr, yErr;
			if (xFieldIndex == -1)
			{
				for (int point=0; point < points.length; point++)
				{
					if (xErrFieldIndex == -1)
						xErr=0;
					else
						xErr=((Double)points[point].getFieldData(xErrFieldIndex)).doubleValue();
					
					if (yErrFieldIndex == -1)
						yErr=0;
					else
						yErr=((Double)points[point].getFieldData(yErrFieldIndex)).doubleValue();
	
					serieData.setPoint(point, point, ((Double)points[point].getFieldData(yFieldIndex)).doubleValue(), xErr, yErr, "", points[point]);
				}
			}
			else
			{
				Comparable[] x=new Comparable[points.length];
				for (int point=0; point < points.length; point++)
					x[point]=(Comparable)points[point].getFieldData(xFieldIndex);
				
				try {
					Sorter.sort(x, points);
				} catch (Exception e) {};
				
				for (int point=0; point < points.length; point++)
				{
					if (xErrFieldIndex == -1)
						xErr=0;
					else
						xErr=((Double)points[point].getFieldData(xErrFieldIndex)).doubleValue();
					
					if (yErrFieldIndex == -1)
						yErr=0;
					else
						yErr=((Double)points[point].getFieldData(yErrFieldIndex)).doubleValue();
	
					serieData.setPoint(point, ((Double)points[point].getFieldData(xFieldIndex)).doubleValue(), ((Double)points[point].getFieldData(yFieldIndex)).doubleValue(), xErr, yErr, "", points[point]);
				}
			}
			
			serieData.setXUnits(xField);
			serieData.setYUnits(yField);
			this.mainDisplay.addSerie(serieData);
			this.mainDisplay.getSerieByName(name).setVisible(true);
			
			System.out.println("nbPoints: "+serieData.getNbPoints());
			
			this.mainDisplay.checkSizeAndPosition();
			this.mainDisplay.getSerieByName(name).forceRedraw();
			this.mainDisplay.fullout();
		}
	}
	
	public void fitSigmoid(String serieName, boolean lockTop, String topValue, boolean lockBottom, String bottomValue, boolean lockRate, String rateValue)
	{
		SerieData serieData=this.mainDisplay.getSerieByName(serieName).getSerieData();

		SigmoidProvider provider = new SigmoidProvider();
		Point2D.Double tempPoint=new Point2D.Double();

//		SigmoidWithErrorsProvider provider = new SigmoidWithErrorsProvider();
	
		double[] xs=new double[serieData.getNbPoints()];
		double[] ys=new double[serieData.getNbPoints()];
		double[] yErrs=new double[serieData.getNbPoints()];
		
		
		double maxY=serieData.getY(0);
		double minY=serieData.getY(0);
		double maxX=serieData.getX(0);
		double minX=serieData.getX(0);
		
		double maxDif=0;
		double center=serieData.getX(0);
		double rate=0;

		
		for (int point=0; point < serieData.getNbPoints(); point++)
		{
			if (serieData.getY(point) < minY) minY=serieData.getY(point);	
			if (serieData.getY(point) > maxY) maxY=serieData.getY(point);
			if (serieData.getX(point) < minX) minX=serieData.getX(point);
			if (serieData.getX(point) > maxX) maxX=serieData.getX(point);
			
			if (point > 0 && point < serieData.getNbPoints()-1)
			{
				if (Math.abs(serieData.getY(point)-serieData.getY(point-1)) > maxDif)
				{
					center=(serieData.getX(point)+serieData.getX(point-1))/2;
					rate=((serieData.getY(point)-serieData.getY(point-1))/(serieData.getX(point)-serieData.getX(point-1)));//-(serieData.getY(point+1)-serieData.getY(point))/(serieData.getX(point+1)-serieData.getX(point)))/100;
					
					System.out.println("new RATE: "+rate);
					System.out.println("point1: "+serieData.getX(point-1)+", "+serieData.getY(point-1));
					System.out.println("point2: "+serieData.getX(point)+", "+serieData.getY(point));
					maxDif=Math.abs(serieData.getY(point)-serieData.getY(point-1));
				}
			}
			
			tempPoint=new Point2D.Double(serieData.getX(point), serieData.getY(point));
			provider.addPoint(tempPoint);
			
			xs[point]=serieData.getX(point);
			ys[point]=serieData.getY(point);
			yErrs[point]=serieData.getErrY(point);
		}

//		provider.setPoints(xs, ys, yErrs);

		rate/=(maxY-minY);

//		rate=1/rate;
//		rate=0.1;
		System.out.println("START RATE: "+rate);
//		maxY=2000;
		double[] startCoord={maxY, minY, rate, center};
		boolean[] locked={false, false, false, false};
		
		
		if (lockTop)
		{
			startCoord[0]=Double.parseDouble(topValue);
			locked[0]=true;
		}
		if (lockBottom)
		{
			startCoord[1]=Double.parseDouble(bottomValue);
			locked[1]=true;
		}
		if (lockRate)
		{
			startCoord[2]=Double.parseDouble(rateValue);
			locked[2]=true;
		}

		System.out.println("top: "+maxY+", bottom: "+minY+", rate: "+rate+", center: "+center);
		System.out.println("top: "+locked[0]+", bottom: "+locked[1]+", rate: "+locked[2]+", center: "+locked[3]);

		provider.setFlags(locked);
		
		LinearSimpleOptimizer2 lOptimizer=new LinearSimpleOptimizer2(provider, null, Optimizer.FIND_MIN, startCoord, provider.getGradientAt(startCoord), 0.0000000001, 1000);
		ConjugateGradientOptimizer sdOptimizer=new ConjugateGradientOptimizer(provider, lOptimizer, Optimizer.FIND_MIN, startCoord, provider.getGradientAt(startCoord), 0.0, 100000);

		double[] minimum=sdOptimizer.compute();
		
		double[] params=new double[6];
		
		for (int i=0; i < 4; i++)
			params[i]=minimum[i];
		
		params[4]=minX-Math.abs(minX-maxX);	// these are the visual limits of the trendline on the x-coordinate
		params[5]=maxX+Math.abs(minX-maxX);
		
		Trendline trendline=new Trendline(Trendline.SIGMOIDAL_E, params);
		this.mainDisplay.getSerieByName(serieName).setTrendline(trendline);

		//calc SSQ
		int nbPoints=this.mainDisplay.getSerieByName(serieName).getSerieData().getNbPoints();
		
		double ssq=0;
		double x, y;
		double dist;
		
		double allPointsYAverage=0;
		for (int point=0; point < nbPoints; point++)
		{
			x=this.mainDisplay.getSerieByName(serieName).getSerieData().getX(point);
			y=this.mainDisplay.getSerieByName(serieName).getSerieData().getY(point);
		
			dist=y-(minimum[1]+(minimum[0]-minimum[1])/(1+Math.exp(-minimum[2]*(x-minimum[3]))));
//			System.out.println("y: "+y+", est: "+(minimum[1]+(minimum[0]-minimum[1])/(1+Math.exp(-minimum[2]*(x-minimum[3]))))+" -> "+dist);
			ssq+=dist*dist;
			
			allPointsYAverage+=y;
		}
		allPointsYAverage/=nbPoints;

		double SS_tot=0;
		double SS_err=0;
		for (int point=0; point < nbPoints; point++)
		{
			SS_tot+=Math.pow( this.mainDisplay.getSerieByName(serieName).getSerieData().getY(point)-allPointsYAverage, 2);
			SS_err+=Math.pow( this.mainDisplay.getSerieByName(serieName).getSerieData().getY(point)-(minimum[1]+(minimum[0]-minimum[1])/(1+Math.exp(-minimum[2]*(this.mainDisplay.getSerieByName(serieName).getSerieData().getX(point)-minimum[3])))), 2);
		}

		System.out.println("R2: "+(1-SS_err/SS_tot));
		System.out.println("XXX SSQ: "+ssq);
		System.out.println("FINAL VALUE: "+provider.getValueAt(minimum));
		TextEntity textEntity=new TextEntity(25,4);
		textEntity.setLocation(0,0);
		textEntity.setText("Top:"+minimum[0]+"\r\nBottom: "+minimum[1]+"\r\nRate: "+minimum[2]+"\r\nMid: "+minimum[3]);
		this.mainDisplay.addEntity(textEntity);
		

		
	}
	
	public void fitSigmoidLMA(String serieName, boolean lockTop, String topValue, boolean lockBottom, String bottomValue, boolean lockRate, String rateValue)
	{

		SerieData serieData=this.mainDisplay.getSerieByName(serieName).getSerieData();

		SigmoidProviderLMA provider = new SigmoidProviderLMA();
		Point2D.Double tempPoint=new Point2D.Double();

//		SigmoidWithErrorsProvider provider = new SigmoidWithErrorsProvider();
	
		double[] xs=new double[serieData.getNbPoints()];
		double[] ys=new double[serieData.getNbPoints()];
		double[] yErrs=new double[serieData.getNbPoints()];
		
		
		double maxY=serieData.getY(0);
		double minY=serieData.getY(0);
		double maxX=serieData.getX(0);
		double minX=serieData.getX(0);
		
		double maxDif=0;
		double center=serieData.getX(0);
		double rate=0;

		double[][] measuredData=new double[serieData.getNbPoints()][2];
		
		double allPointsYAverage=0;
		for (int point=0; point < serieData.getNbPoints(); point++)
		{
			if (serieData.getY(point) < minY) minY=serieData.getY(point);	
			if (serieData.getY(point) > maxY) maxY=serieData.getY(point);
			if (serieData.getX(point) < minX) minX=serieData.getX(point);
			if (serieData.getX(point) > maxX) maxX=serieData.getX(point);
			
			if (point > 0 && point < serieData.getNbPoints()-1)
			{
				if (Math.abs(serieData.getY(point)-serieData.getY(point-1)) > maxDif)
				{
					center=(serieData.getX(point)+serieData.getX(point-1))/2;
					rate=((serieData.getY(point)-serieData.getY(point-1))/(serieData.getX(point)-serieData.getX(point-1)));//-(serieData.getY(point+1)-serieData.getY(point))/(serieData.getX(point+1)-serieData.getX(point)))/100;
					
					System.out.println("new RATE: "+rate);
					System.out.println("point1: "+serieData.getX(point-1)+", "+serieData.getY(point-1));
					System.out.println("point2: "+serieData.getX(point)+", "+serieData.getY(point));
					maxDif=Math.abs(serieData.getY(point)-serieData.getY(point-1));
				}
			}
			
			measuredData[point][0]=serieData.getX(point);
			measuredData[point][1]=serieData.getY(point);
			tempPoint=new Point2D.Double(serieData.getX(point), serieData.getY(point));
//			provider.addPoint(tempPoint);
			
			xs[point]=serieData.getX(point);
			ys[point]=serieData.getY(point);
			yErrs[point]=serieData.getErrY(point);

			allPointsYAverage+=serieData.getY(point);
		}

		allPointsYAverage/=serieData.getNbPoints();
//		provider.setPoints(xs, ys, yErrs);

		rate/=(maxY-minY);

//		rate=1/rate;
//		rate=0.1;
		System.out.println("START RATE: "+rate);
//		maxY=2000;
		double[] startParams={maxY, minY, rate, center};
		boolean[] locked={false, false, false, false};
		
		
		if (lockTop)
		{
			startParams[0]=Double.parseDouble(topValue);
			locked[0]=true;
		}
		if (lockBottom)
		{
			startParams[1]=Double.parseDouble(bottomValue);
			locked[1]=true;
		}
		if (lockRate)
		{
			startParams[2]=Double.parseDouble(rateValue);
			locked[2]=true;
		}

		System.out.println("top: "+maxY+", bottom: "+minY+", rate: "+rate+", center: "+center);
		System.out.println("top: "+locked[0]+", bottom: "+locked[1]+", rate: "+locked[2]+", center: "+locked[3]);

		provider.setLockedParameters(locked);
		
		
		double[] startParameters = new double[]{startParams[0],startParams[1],startParams[2],startParams[3]};
		LevenbergMarquardt lma = new LevenbergMarquardt();
		double[] resultParameters = lma.doCalculation(provider, startParameters, measuredData);
		
		
		double[] params=new double[6];
		
		for (int i=0; i < 4; i++)
			params[i]=resultParameters[i];
		
		params[4]=minX-Math.abs(minX-maxX);	// these are the visual limits of the trendline on the x-coordinate
		params[5]=maxX+Math.abs(minX-maxX);
		
		Trendline trendline=new Trendline(Trendline.SIGMOIDAL_10, params);
		this.mainDisplay.getSerieByName(serieName).setTrendline(trendline);

		
		double SS_tot=0;
		double SS_err=0;
		for (int point=0; point < measuredData.length; point++)
		{
			SS_tot+=Math.pow( measuredData[point][1]-allPointsYAverage, 2);
			SS_err+=Math.pow(measuredData[point][1]-provider.getValueAt(new double[]{measuredData[point][0]}), 2);
		}

		System.out.println("R2: "+(1-SS_err/SS_tot));
		
		double degreesOfFreedom=measuredData.length-startParams.length;
		provider.setParameters(resultParameters);
		
		double[] partialDerivatives;
		double[][] jacobian=new double[measuredData.length][4];
		for (int iMeasurement = 0; iMeasurement < measuredData.length; iMeasurement++)
		{
			partialDerivatives = provider.getGradientAt(new double[]{measuredData[iMeasurement][0]});
			System.arraycopy(partialDerivatives, 0, jacobian[iMeasurement], 0, 4);
		}
		
		Matrix jacobianMatrix=new Matrix(jacobian);
		Matrix pseudoHessianMatrix=jacobianMatrix.transpose().times(jacobianMatrix);
		
		Matrix covarianceMatrix=pseudoHessianMatrix.inverse();
		double[][] covData=covarianceMatrix.getArrayCopy();

		provider.setParameters(resultParameters);
		double chi_2=0;
		for (int point=0; point < measuredData.length; point++)
		{
			chi_2+=Math.pow(measuredData[point][1]-provider.getValueAt(new double[]{measuredData[point][0]}), 2)/degreesOfFreedom;
		}
		double ci=Math.sqrt(chi_2*covData[3][3])*3.182; //TODO: replace 3.182 with the correct students t-value based on the number of points
		System.out.println("CI: "+ci);
		
		TextEntity textEntity=new TextEntity(25,4);
		textEntity.setLocation(0,0);
		textEntity.setText("Top:"+resultParameters[0]+"\r\nBottom: "+resultParameters[1]+"\r\nRate: "+resultParameters[2]+"\r\nMid: "+resultParameters[3]);
		this.mainDisplay.addEntity(textEntity);
	
	}


	public void setHighlight(String name, String highlightType)
	{
		if (this.mainDisplay.getSerieByName(name) != null)
		{
			this.mainDisplay.getSerieByName(name).setHighlight(highlightType);
		}
		else
			System.out.println("serie name not found");
	}
	
	/**
	 * Method to retrieve an XML String containing all the characteristics required 
	 * to recreate the current visual output.
	 * @return a String containing the XML representation.
	 */
/*	public String getXML()
	{
		Hashtable xmlProperties = new Hashtable();
		xmlProperties.put("includeURL", new Boolean(true));

		return mainDisplay.getXmlTag(xmlProperties);
	}	
	

	public void setXML(String XMLString)
	{
		mainPanel.removeAllEntities();
		mainPanel.repaint();
		
		Hashtable helpers=new Hashtable();
		mainDisplay=new GraphDisplay2(XMLString, helpers);
		mainPanel.addEntity(mainDisplay);
		mainDisplay.checkInteractiveSurface();
		mainDisplay.restoreLinks();
		mainPanel.repaint();
	}
*/
	/**
	 * Method to retrieve an XML String containing all the characteristics required 
	 * to recreate the current visual output.
	 * @return a String containing the XML representation.
	 */
	public String getXML()
	{
		return mainPanel.getXML();
	}	
	
	public void setXML(String xmlString)
	{
		this.mainPanel.setXML(xmlString);
		this.mainPanel.repaint();
		
//		this.addSpecialListeners();
	}
	/**
	* Method to load an image from the file pointed by imageName
	* Makes use of urlLocation.
	*/
	private Image getImage(String imageName) {
		Image image=null;
    	
		try { 
//			System.out.println("Path is " + getClass() + "/../gif/");
//			InputStream in = getClass().getResourceAsStream(imageName); 
			InputStream in = getClass().getResourceAsStream("/org/cheminfo/hook/gif/" + imageName); 
			if (in != null) { 
				byte[] buffer = new byte[in.available()]; 
				in.read(buffer); 
				image = Toolkit.getDefaultToolkit().createImage(buffer); 
			} else {
				System.out.println("input stream is null");
			}
		} catch (java.io.IOException e) { }
		
		if (image==null)
		{
			image=getImage(urlLocation,imageName);
		}
		
		if (image == null)
			System.out.println("Image not found "+urlLocation);
		
		return image;
	}


	public void actionPerformed(ActionEvent e)
	{
	}
	
	public InteractiveSurface getInteractions()
	{
		return this.mainPanel;
	}
	
	public BasicDisplay getMainDisplay()
	{
		return this.mainDisplay;
	}
}

