package org.cheminfo.hook.demo;

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


public class Demo extends Applet implements ActionListener, Runnable
{
	
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
	
	final static boolean test=true; // not in test mode
	
	int totalWidth, totalHeight;	
	final static int topHeight=24;		// the height of the top panel
	final static int bottomHeight=24;		// the height of the bottom panel
	final static int leftWidth=24;		// the width of the left panel
	final static int rightWidth=5;		// the width of the right panel

	ImageButton			userDialogButton;
	
//	ColorActionButton	blackButton, grayButton, yellowButton, greenButton, magentaButton, cyanButton, blueButton, redButton, whiteButton;
	
	private Thread	loadingThread=null;
	
	private URL urlLocation=null;	// the complete URL to the applet
	
	
	String baseDirectory=null;
	URL docBase;
	URL appletBase;
	
	protected DemoDisplay mainDisplay;
//	protected InteractionClass interactions;
	
	public Demo()
	{
	}
	
	public void startup()
	{
//		interactions = new InteractionClass();
//		interactions.setAppletURL(urlLocation);
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
		
		specificPanel.add(new AddPolygonActionButton(4, "Add Polygon", mainPanel));
		specificPanel.add(new AddPolygonActionButton(5, "Add Polygon", mainPanel));
		specificPanel.add(new DemoRotateActionButton(getImage("about.gif"), "Add Polygon", mainPanel));
			
		leftPanel.add(specificPanel);


		topPanel.add(new AboutActionButton(getImage("about.gif"), "About Nemo", mainPanel));

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

		
		mainDisplay = new DemoDisplay();
		mainPanel.addEntity(mainDisplay);

		mainDisplay.init();
		mainPanel.setActiveEntity(mainDisplay);
		mainDisplay.setErasable(false);

		mainDisplay.setLocation(0,0);

		
		mainPanel.setUserDialog(bottomPanel);
		selectButton.click();
		
	}

		
	public void run()
	{
		try {
			loadingThread.sleep(200);
		} catch (Exception ex) {}

//		getSourceFile();
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
	
/*
	public void setXML(String XMLString)
	{
		Hashtable helpers=new Hashtable();
		mainDisplay.init(XMLString, mainPanel.getSize().width, mainPanel.getSize().height, helpers);
		mainDisplay.checkInteractiveSurface();
		mainDisplay.restoreLinks();
	}
*/

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
	

/*	protected void makeButtonLight(DefaultActionButton theButton, Panel thePanel, String imageName, String buttonDescription)
    {
		theButton.setInfoMessage(buttonDescription);
		
		theButton.setButtonImage(getImage(imageName));
		thePanel.add(theButton);
		interactions.addButton(theButton);
		theButton.setInteractionClass(interactions);
	}

	protected void setApplicationPanel(Panel newPanel)
	{
		this.applicationPanel=newPanel;
	}
	
*/	public InteractiveSurface getInteractions()
	{
		return this.mainPanel;
	}
	
	public BasicDisplay getMainDisplay()
	{
		return this.mainDisplay;
	}
}

