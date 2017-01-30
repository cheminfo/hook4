package org.cheminfo.hook.som;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;

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
import org.cheminfo.hook.graphdraw2.FullGraphActionButton;
import org.cheminfo.hook.graphdraw2.Graph2DZoomActionButton;
import org.cheminfo.hook.graphdraw2.GraphDisplay2;
import org.cheminfo.hook.graphdraw2.GraphSlideActionButton;


public class SomDemo extends Applet implements ActionListener, Runnable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7130123330353697776L;
	Panel leftPanel;
	UserDialog bottomPanel;
	Panel topPanel;
	Panel rightPanel;
	InteractiveSurface	mainPanel;
	Panel	colorPanel;
	Panel	propertiesPanel;
	Panel	commonPanel;
	Panel	specificPanel;
	Panel	applicationPanel=null;
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
	private SomGenerator demo;
	
	public SomDemo()
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


		rightPanel=new Panel();
		rightPanel.setBackground(Color.lightGray);
		rightPanel.setLayout(new BorderLayout());
//		rightPanel.add(new EmptyBox(rightWidth-1, rightWidth-1));
		rightPanel.setSize(rightWidth,rightHeight);
		rightPanel.doLayout();
		
		topPanel=new Panel();
		topPanel.setBackground(Color.lightGray);
		StackLayout tempLayout=new StackLayout(StackLayout.HORIZONTAL, 24, 0);
		tempLayout.setPreferredSize(topWidth,topHeight);
		topPanel.setLayout(tempLayout);
//		topPanel.setBackground (Color.lightGray);
		
		leftPanel=new Panel();
		leftPanel.setBackground(Color.lightGray);
		tempLayout=new StackLayout(StackLayout.VERTICAL, 0, 24);
		tempLayout.setPreferredSize(leftWidth,leftHeight);
		leftPanel.setLayout(tempLayout);
//		leftPanel.setBackground (Color.lightGray);

		mainPanel=new InteractiveSurface();

		mainPanel.setLayout(null);

		bottomPanel=new UserDialog(mainPanel);
		bottomPanel.setSize(bottomWidth,bottomHeight);
		bottomPanel.addActionListener(mainPanel);

		
		commonPanel=new Panel(new StackLayout(StackLayout.VERTICAL, 0, 0));
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


		specificPanel=new Panel(new DynamicLayout(DynamicLayout.VERTICAL, 0, 0));
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
			colorPanel=new Panel(new StackLayout(StackLayout.HORIZONTAL, 0 ,0));
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

		this.doLayout();

		
		mainDisplay = new GraphDisplay2();
		mainDisplay.setSize(mainPanel.getWidth(), mainPanel.getHeight());
		mainPanel.addEntity(mainDisplay);


		mainPanel.setActiveEntity(mainDisplay);
		mainDisplay.setErasable(false);

		mainDisplay.setLocation(0,0);
		
		demo=new SomGenerator(mainDisplay);

//		demo.run();
/*		try
		{
			wait(10000);
		}	catch (Exception e) {}
*/		
		mainPanel.hasUndo(false);
		mainPanel.setUserDialog(bottomPanel);
		
		mainDisplay.checkInteractiveSurface();
		selectButton.click();
	}

	/**
	 * Returns a tab-delimited file containing Epoch and Match
	 * @return
	 */
	public String getMatchEvolution()
	{
		return demo.getMatchEvolution();
	}
	
	public void setGridSize(int size) {
		demo.setGridSize(size);
	}
	
	public void setNumberEpoch(int epoch) {
		demo.setNumberEpoch(epoch);
	}
	
	public void startCalculation() {
		demo.startCalculation();
	}
	
	public void addData(String data) {
		// we need to analyze a tab-delimited field
		// the first line is the header
		String lines[]=data.replaceAll("\n", "\r").split("\r");
		
		
	}
	
	/**
	 * 
	 * @param type	0: learning, 1: test1, 2: test2, 3: test3
	 * @param numberObject
	 * @param objectDimension
	 */
	
	public void addRandomData(int type, int numberObject, int objectDimension) {
		demo.setObjectDimension(objectDimension);
		demo.addRandomData(type, numberObject);
	}
	
	public void addData(String name, String data) {
		
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
		String test;
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

	public boolean getSerieVisibility(String serieName)
	{
		return this.mainDisplay.getSerieByName(serieName).isVisible();
	}
	
	public void setSerieVisibility(String serieName, boolean visible)
	{
		System.out.println("Serie name: "+serieName);
		this.mainDisplay.getSerieByName(serieName).setVisible(visible);
		this.mainDisplay.getSerieByName(serieName).forceRedraw();
		this.mainDisplay.getInteractiveSurface().repaint();	
	//	this.mainPanel.repaint(); // will not repaint during calculations
	}
	
	
		
	/**
	 * Method to retrieve an XML String containing all the characteristics required 
	 * to recreate the current visual output.
	 * @return a String containing the XML representation.
	 */
	public String getXML()
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

