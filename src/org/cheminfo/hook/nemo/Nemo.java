package org.cheminfo.hook.nemo;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cheminfo.function.scripting.ScriptingInstance;
import org.cheminfo.hook.converter.Converter;
import org.cheminfo.hook.framework.AboutActionButton;
import org.cheminfo.hook.framework.BasicDisplay;
import org.cheminfo.hook.framework.BasicEntity;
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
import org.cheminfo.hook.framework.UndoActionButton;
import org.cheminfo.hook.framework.UserDialog;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.filters.FilterType;
import org.cheminfo.hook.nemo.filters.ResurrectNMRSpectrumFilter;
import org.cheminfo.hook.nemo.filters.SimulateNMRSpectrumFilter;
import org.cheminfo.hook.nemo.nmr.NmrSimulator;
import org.cheminfo.hook.nemo.nmr.Nucleus;
import org.cheminfo.hook.nemo.nmr.PredictionData;
import org.cheminfo.hook.nemo.nmr.ProprietaryTools;
import org.cheminfo.hook.nemo.nmr.simulation.Simulate2DNMRSpectrum;
import org.cheminfo.hook.nemo.nmr.simulation.util.SimulateFromDescription;
import org.cheminfo.hook.nemo.signal.NMRSignal1D;
import org.cheminfo.hook.util.EmptyBox;
import org.cheminfo.hook.util.NemoPreferences;
import org.cheminfo.scripting.spectradata.AppletNemo;
import org.cheminfo.scripting.spectradata.SD;
import org.cheminfo.scripting.spectradata.SpectraDataExt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Nemo extends Applet implements ActionListener, Runnable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4792992050851583116L;
	
	private boolean mustRepaint = true;
	
	Panel leftPanel;
	UserDialog bottomPanel;
	Panel topPanel;
	Panel rightPanel;
	InteractiveSurface mainPanel;
	Panel colorPanel;
	Panel propertiesPanel;
	Panel commonPanel;
	Panel preprocessingPanel;
	Panel specificPanel;
	Panel applicationPanel = null;
	Panel filterPanel;
	ScriptingInstance interpreter=null;
	
	final static boolean DEBUG=false;
	public final static Color DARK_ORANGE=new Color(255,127,0);
	public final static Color DARK_GREEN=new Color(0,127,0);
	public final static Character SHOW_ALL_SC= new Character('q');//Show all short cut key
	
	SelectActionButton selectButton;
	
	// final static boolean test = true;

	int totalWidth, totalHeight;
	final static int topHeight = 24; // the height of the top panel
	final static int bottomHeight = 24; // the height of the bottom panel
	final static int leftWidth = 24; // the width of the left panel
	final static int rightWidth = 5; // the width of the right panel

	ImageButton userDialogButton;

	// ColorActionButton blackButton, grayButton, yellowButton, greenButton,
	// magentaButton, cyanButton, blueButton, redButton, whiteButton;

	private Thread loadingThread = null;

	private URL urlLocation = null; // the complete URL to the applet

	String baseDirectory = null;
	URL docBase;
	URL appletBase;

	protected SpectraDisplay mainDisplay;
	private JSONArray jSONResult;
	private String pluginsFolder="";

	public static String hello() {
		return "Hello";
	}

	// protected InteractionClass interactions;

	public Nemo() {
	}

	public Nemo(boolean mustRepaint) {
		this.mustRepaint = mustRepaint;
	}

	public void startup() {
		// interactions = new InteractionClass();
		// interactions.setAppletURL(urlLocation);
	}

	public float getAlignmentX() {
		return Component.LEFT_ALIGNMENT;
	}

	public void init() {
		try {
			String className = getClass().getName();
			String packageName = "";
			int lastPoint = className.lastIndexOf(".");
			if (lastPoint > 0)
				packageName = className.substring(0, lastPoint + 1).replace(
						(char) 46, (char) 47);
			urlLocation = new URL(getCodeBase() + packageName);
		} catch (Exception e) {
		}

		docBase = getDocumentBase();
		appletBase = this.getCodeBase();

		baseDirectory = getCodeBase().toString();
		
		startup();
		frameWork();

		checkServer(); // applet protection
		if (loadingThread == null) {
			loadingThread = new Thread(this, "Loader");
			loadingThread.start();
		}

		applyAppletParameters();
	}
	public void frameWork() {
		DefaultActionButton.setButtonUpImage(getImage("buttonUp.gif"));
		DefaultActionButton.setButtonDownImage(getImage("buttonDown.gif"));

		totalWidth = this.getSize().width;
		totalHeight = this.getSize().height;

		String optionsParameter = null;
		try {
			optionsParameter = getParameter("OPTIONS");
		} catch (NullPointerException npe) {}
		
		if (optionsParameter != null)
			optionsParameter = optionsParameter.toLowerCase();
		else
			optionsParameter = "smart,tools,undo,flags,colors,processing,test";

		int topWidth = totalWidth;
		int bottomWidth = totalWidth;
		int leftHeight = totalHeight - topHeight - bottomHeight;
		int rightHeight = leftHeight;

		this.setLayout(new BorderLayout());

		rightPanel = new Panel();
		rightPanel.setBackground(Color.lightGray);
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(new EmptyBox(rightWidth - 1, rightWidth - 1));
		rightPanel.setSize(rightWidth, rightHeight);
		rightPanel.doLayout();

		topPanel = new Panel();
		topPanel.setBackground(Color.lightGray);
		StackLayout tempLayout = new StackLayout(StackLayout.HORIZONTAL, 24, 0);
//		tempLayout.setPreferredSize(topWidth, topHeight);
		topPanel.setLayout(tempLayout);
		topPanel.setBackground (Color.lightGray);

		leftPanel = new Panel();
		leftPanel.setBackground(Color.lightGray);
		tempLayout = new StackLayout(StackLayout.VERTICAL, 0, 24);
		tempLayout.setPreferredSize(leftWidth, leftHeight);
		leftPanel.setLayout(tempLayout);
		// leftPanel.setBackground (Color.lightGray);

		mainPanel = new InteractiveSurface();

		mainPanel.setLayout(null);

		bottomPanel = new UserDialog(mainPanel);
		bottomPanel.setSize(bottomWidth, bottomHeight);
		bottomPanel.setPreferredSize(new Dimension(bottomWidth, bottomHeight));
		bottomPanel.addActionListener(mainPanel);

//		commonPanel = new Panel(new StackLayout(StackLayout.VERTICAL, 0, 0));
		commonPanel = new Panel(new DynamicLayout(DynamicLayout.VERTICAL, 0, 0));
		commonPanel.setBackground(Color.lightGray);
		selectButton = new SelectActionButton(getImage("select.gif"), "Select. Double click to select all the same kind, SPACE for the next of the same kind", mainPanel);

		this.mainPanel.setDefaultAction(this.selectButton);

		commonPanel.add(selectButton);
		commonPanel.add(new EraseActionButton(getImage("erase.gif"), "Delete entity (press SHIFT for foce delete)", mainPanel));
		if (optionsParameter == null || (optionsParameter != null && optionsParameter.indexOf("tools") != -1)) {
			// commonPanel.add(new DrawArrowActionButton(getImage("arrow.gif"),
			// "Draw
			// Arrow", interactions));
			// commonPanel.add(new TextActionButton(getImage("text.gif"), "Add
			// Text",
			// interactions));
			commonPanel.add(new SpectraTextActionButton(getImage("text.gif"), "Add Text", mainPanel));
			LinkActionButton linkButton = new LinkActionButton( getImage("link.gif"), "Link", mainPanel);
			linkButton.setLinkCreationHandler(new NemoLinkCreationHandler());
			commonPanel.add(linkButton);
			
		//	commonPanel.add(new SendToBackActionButton(getImage("sendback.gif"), "Send to Back", mainPanel, Spectra.class));
		//	commonPanel.add(new BringToFrontActionButton(getImage("bringfront.gif"), "Bring to Front", mainPanel, Spectra.class));
		}

		leftPanel.add(commonPanel);

		preprocessingPanel = new Panel(new DynamicLayout(
				DynamicLayout.HORIZONTAL, 0, 0));

		specificPanel = new Panel(new DynamicLayout(DynamicLayout.VERTICAL, 0,
				0));
		specificPanel.setBackground(Color.lightGray);

		// load needed buttons in specific pannel from XML file
		String uiConfig = "/org/cheminfo/hook/nemo/ui.xml";
		
		try {
			Class.forName ("javax.script.ScriptEngineManager");
			interpreter = null;//new ScriptingInstance(pluginsFolder);
		} catch (ClassNotFoundException e) {
			System.out.println("Can not initialize ScriptingInstance: "+e.toString());
		}


		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputStream in = getClass().getResourceAsStream(uiConfig);
			if (in == null) {
				System.out.println("no config!");
			}
			Document configXML = builder.parse(in);
			Element rootNode = configXML.getDocumentElement();
			NodeList params = rootNode.getElementsByTagName("specific_button");
			int nNodes = params.getLength();
			for (int iNode = 0; iNode < nNodes; iNode++) {
				Node specificButton = params.item(iNode);
				NamedNodeMap attributes = specificButton.getAttributes();
				String enabled = attributes.getNamedItem("enabled")
						.getNodeValue().trim();
				if (enabled.compareTo("true") == 0) {
					// this is legacy and done to make config work
					// TODO remove this at some point
					if (optionsParameter != null) {
						String optionString = attributes.getNamedItem("applet_option").getNodeValue().trim();
						if (!optionString.equals("none") && optionsParameter.indexOf(optionString) == -1) {
							continue;
						}
					}
					//
					String className = attributes.getNamedItem("class").getNodeValue().trim();
					String imgName = attributes.getNamedItem("img").getNodeValue().trim();
					String overText = attributes.getNamedItem("overtxt").getNodeValue().trim();
					String targetPanel = attributes.getNamedItem("targetPanel").getNodeValue().trim();

					// We call a button based on the name with the correct parameters
					Object[] parameters=new Object[3];
					parameters[0]=getImage(imgName);
					parameters[1]=overText;
					parameters[2]=mainPanel;

					Class[] parameterTypes=new Class[3];
					parameterTypes[0]=Image.class;
					parameterTypes[1]=String.class;
					parameterTypes[2]=InteractiveSurface.class;
					
					try {
						Constructor constructor=Class.forName(className).getConstructor(parameterTypes);
						
	
		//				Object o = Class.forName(className).newInstance(parameters);
						DefaultActionButton actionButton = (DefaultActionButton)constructor.newInstance(parameters);
						actionButton.setInteractiveSurface(mainPanel);
						actionButton.setImage(getImage(imgName));
						actionButton.setInfoMessage(overText);
						
						
						if (targetPanel.equals("specific")) {
							specificPanel.add(actionButton);
						} else if (targetPanel.equals("preprocessing")) {
							preprocessingPanel.add(actionButton);
						}
						// mainPanel.addButton(actionButton);
					} catch (Exception e) {
						System.out.println("Could not create button: "+className+" - "+overText+" - "+e.toString());
					}
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		leftPanel.add(specificPanel);
		topPanel.add(new AboutActionButton(getImage("about.gif"), "About Nemo", mainPanel));

		if (optionsParameter == null || (optionsParameter != null && optionsParameter.indexOf("undo") != -1))
			topPanel.add(new UndoActionButton(getImage("undo.gif"), "Undo",mainPanel));

		if (optionsParameter == null || (optionsParameter != null && optionsParameter .indexOf("flags") != -1)) {

			propertiesPanel = new Panel(new DynamicLayout(DynamicLayout.HORIZONTAL, 0, 0));
			propertiesPanel.setBackground(Color.lightGray);
			((DynamicLayout) propertiesPanel.getLayout()).setPreferredSize(7 * 24, 24);

			propertiesPanel.add(new ShowHScaleActionButton(getImage("hscale.gif"), "Show/Hide horizontal scale",mainPanel));
			propertiesPanel.add(new ShowVScaleActionButton(getImage("vscale.gif"), "Show/Hide vertical scale", mainPanel));
			propertiesPanel.add(new ShowIntegralsActionButton(getImage("integral.gif"), "Show/Hide integrals",mainPanel));
			propertiesPanel.add(new ShowLabelActionButton(getImage("label.gif"), "Show/Hide labels", mainPanel));
			propertiesPanel.add(new ShowPredictionLabelActionButton(getImage("predictionlabel.gif"),"Show/Hide prediction labels", mainPanel));
			propertiesPanel.add(new ShowGaussianActionButton(getImage("connectpeak.gif"), "Show/Hide Gaussian",mainPanel));
			propertiesPanel.add(new ShowAtomIDActionButton(getImage("atomnumber.gif"),"Show/Hide atom ID", mainPanel));

			propertiesPanel.doLayout();

			topPanel.add(propertiesPanel);
		}

		topPanel.add(this.preprocessingPanel);

		if (optionsParameter == null
				|| (optionsParameter != null && optionsParameter
						.indexOf("colors") != -1)) {
//			colorPanel = new Panel(new StackLayout(StackLayout.HORIZONTAL, 0, 0));
			colorPanel = new Panel(new DynamicLayout(DynamicLayout.HORIZONTAL, 0, 0));
			colorPanel.setBackground(Color.LIGHT_GRAY);

			colorPanel.add(new PrimarySecondaryActionButton(getImage("foreground.gif"), "Primary / Secondary Color",mainPanel));
			colorPanel.add(new ColorActionButton(Color.BLACK, "Black",mainPanel));
			colorPanel.add(new ColorActionButton(Color.GRAY, "Gray", mainPanel));
			colorPanel.add(new ColorActionButton(Color.YELLOW, "Yellow",mainPanel));
			colorPanel.add(new ColorActionButton(DARK_ORANGE, "Orange", mainPanel));
			colorPanel.add(new ColorActionButton(Color.RED, "Red", mainPanel));
			colorPanel.add(new ColorActionButton(Color.MAGENTA, "Magenta",mainPanel));
			colorPanel.add(new ColorActionButton(Color.BLUE, "Blue", mainPanel));
			colorPanel.add(new ColorActionButton(Color.CYAN, "Cyan", mainPanel));
			colorPanel.add(new ColorActionButton(Color.GREEN, "Green",mainPanel));
			colorPanel.add(new ColorActionButton(DARK_GREEN, "Dark Green",mainPanel));

			
			colorPanel.add(new ColorActionButton(Color.white, "White",mainPanel));
			colorPanel.add(new ColorActionButton(getImage("transparent.gif"),null, "Transparent", mainPanel));


			
			colorPanel.doLayout();
			topPanel.add(colorPanel);
		}

		this.add("Center", mainPanel);
		this.add("South", bottomPanel);
		this.add("West", leftPanel);
		this.add("North", topPanel);
		this.add("East", rightPanel);

		this.doLayout();

		/*
		 * JTextArea text=new JTextArea(); text.setText("Hello");
		 * text.setLineWrap(true); text.setWrapStyleWord(true);
		 * text.setForeground(Color.BLACK); mainPanel.add(text);
		 * 
		 * text.setSize(100,100); text.setLocation(100,100);
		 */
		mainDisplay = new SpectraDisplay();
		mainPanel.addEntity(mainDisplay);

		mainDisplay.init();

		
		
		mainPanel.setActiveEntity(mainDisplay);
		mainDisplay.setErasable(false);
		mainDisplay.setShiftErasable(false);

		mainDisplay.setLocation(0, 0);

		mainPanel.setUserDialog(bottomPanel);
		selectButton.click();
		// selectButton.requestFocus();
		
		// Allows to add a key listener to all the component
		addKeyListenerToComponent(this);
		
		this.addComponentListener(new ComponentAdapter()
				{
					public void componentResized(ComponentEvent ce)
					{
						Dimension newSize=ce.getComponent().getSize();
						mainDisplay.getInteractiveSurface().setSize(newSize.width-25, newSize.height-2*24);
						mainDisplay.checkSizeAndPosition();
						mainDisplay.getInteractiveSurface().repaint();
						repaint();
					}
				}		
		);
	}

	/**
	 * This method allows to add a common listener to basically all the objects
	 * @param container
	 */
	
    private void addKeyListenerToComponent(Container container)
    {
    	for (Component component : container.getComponents()) {
    		if (!(component instanceof InteractiveSurface)) {
    			component.addKeyListener(mainPanel);
    			// System.out.println("Adding listener to: "+component.getClass().getName());
    		}
    		if (component instanceof Container) {
    			addKeyListenerToComponent((Container)component);
            }
    	}
    }
    

	
	
	
	
	
	public void run() {
		try {
			loadingThread.sleep(200);
		} catch (Exception ex) {
		}

		// getSourceFile();
	}

	private void applyAppletParameters() {
		if (getParameter("SOURCE_FILE") != null) {
			String sourceFile = getParameter("SOURCE_FILE");

			sourceFile += "";
			if (!sourceFile.equals("")) {
				StringTokenizer tk = new StringTokenizer(sourceFile, ",");
				int vPosition = -tk.countTokens() * 20 - 10;
				while (tk.hasMoreTokens()) {
					vPosition += 20;
					loadSpectrum(tk.nextToken(), vPosition);
				}
			}
		}

		if (getParameter("NMR_INLINE") != null) {
			String nmrInline = getParameter("NMR_INLINE");

			addSimulatedSpectrum(nmrInline, 1);
		}
		
		if (getParameter("XML") != null) {
			// System.out.println(getParameter("XML"));
			this.setXML(URLDecoder.decode(getParameter("XML")));
		}
		mainDisplay.storeView(SHOW_ALL_SC);
	}

	
	private Spectra loadSpectrum(String stringUrl, int vPosition) {
		return loadSpectrum(stringUrl, vPosition, false);
	}
	
	private Spectra loadSpectrum(String stringUrl, int vPosition, boolean storeTotalJcamp) {
		return loadSpectrum(stringUrl, vPosition, false, storeTotalJcamp);
	}

	public void checkServer() {
		String stringUrl = "hook.gif";
		URL correctUrl = null;

		mainPanel.isLegal = true;

		// we need the URL
		// we first try to convert it directly

		try {
			correctUrl = new URL(appletBase, stringUrl);
		} catch (MalformedURLException er) {
			// mainPanel.getUserDialog().setText("Not found : "+stringUrl);
			return;
		}

		// mainPanel.getUserDialog().setText("Good URL : "+correctUrl);

		// System.out.println(correctUrl);

		
		/*
		try {
			BufferedReader inputFile;
			long code2 = (long) (Math.pow((double) 13, (double) 11) - 113);
			long code = InetAddress.getByName(correctUrl.getHost()).hashCode();
			
			//code = InetAddress.getByName("128.178.43.80").hashCode();

			long theCode = Math.abs((code * 223 + 49843) ^ (code2));
			//System.out.println(theCode);
			// mainPanel.getUserDialog().setText(theCode+" -
			// "+correctUrl.getHost());


			InputStreamReader stream = new InputStreamReader(correctUrl.openStream());
			inputFile = new BufferedReader(stream);

			String currentLine = inputFile.readLine();
			while (currentLine != null) {
				if (currentLine.indexOf(theCode + "") >= 0) {
					mainPanel.isLegal = true;
					break;
				}
				currentLine = inputFile.readLine();
			}

		} catch (Exception e) {
			mainPanel.getUserDialog().setText(e.toString());
			return;
		}*/
	}
	
	/**
	 * Method to add a new spectrum to myDisplay loading it from a jdx file
	 * pointed by the urlString. The urlString may be relative or absolute.
	 * Return false if file not found.
	 */
	private Spectra loadSpectrum(String stringUrl, int vPosition, boolean isVertical, boolean storeTotalJcamp) {
		
		Spectra spectra=null;
		Converter tempConverter = null;

		if(stringUrl.endsWith("fid")||stringUrl.endsWith("1r")||
				stringUrl.endsWith("1i")||stringUrl.endsWith("2rr")||stringUrl.endsWith("ser"))
			tempConverter = Converter.getConverter("Bruker");

		else
			tempConverter = Converter.getConverter("Jcamp", storeTotalJcamp);
		SpectraData spectraData = new SpectraData();

		if (tempConverter == null) {
			System.out.println("Converter -> null");
			return null;
		}
		
		if (DEBUG) System.out.println("Loading: "+stringUrl);
		
//		System.out.println("Loading: "+stringUrl);
//		BufferedReader reader = new BufferedReader(
//				new InputStreamReader(
//				new GZIPInputStream(
//				new FileInputStream( filename.gz ) ) ) );
		
		
		
		boolean convertResult=false;
		URL correctUrl = null;
		if (stringUrl.indexOf("##")>-1) {
			if (DEBUG) System.out.println("IT IS A JCAMP");
			convertResult=tempConverter.convert(new BufferedReader(new StringReader(stringUrl)), spectraData);
		} else {
			if (DEBUG) System.out.println("IT IS A URL");
			// we need the URL
			// we first try to convert it directly
			try {
				correctUrl = new URL(stringUrl);
			} catch (MalformedURLException e) {
				try {
					correctUrl = new URL(docBase, stringUrl);
				} catch (MalformedURLException er) {
					mainPanel.getUserDialog().setText("Not ffound : " + stringUrl);
					return null;
				}
			}
			mainPanel.getUserDialog().setText("Loading from " + String.valueOf(correctUrl));
			convertResult=tempConverter.convert(correctUrl, spectraData);
		}

		if (convertResult) {
			mainPanel.getUserDialog().setText("Preparing spectrum");
			spectraData.setActiveElement(0);
			// if (spectraData.getDataType()==SpectraData.TYPE_NMR_SPECTRUM)
			// spectraData.convertHzToPPM();
			spectraData.prepareSpectraData();
			spectraData.updateDefaults();
			
			if (spectraData.getDefaults().needsVScale) {
				this.mainDisplay.hasVScale(true);
			}
			if (spectraData.getDefaults().needsHScale) {
				this.mainDisplay.hasHScale(true);
			}
			if (spectraData.getDefaults().absoluteYScale) {
				this.mainDisplay.setAbsoluteYScale(true);
			}
			
			spectra = new Spectra(spectraData);
			// change horizontal vertical based on nucleus
			Spectra firstSpectrum = this.mainDisplay.getFirstSpectra();
			if (firstSpectrum != null && firstSpectrum.isDrawnAs2D()) {
				if ((firstSpectrum.isHomonuclear()) && (!spectraData.is2D())) {
					Spectra secondDimSpectrum = new Spectra(spectraData);
					secondDimSpectrum.isVertical(!isVertical);
					secondDimSpectrum.setLocation(secondDimSpectrum.getLocation().x, 0);
					//System.out.println("Adding vertical");
					mainDisplay.addSpectra(secondDimSpectrum);
					//isVertical=!isVertical;
				} else {
					if (firstSpectrum.getNucleus(1) == spectraData.getNucleus())
						isVertical = false;
					else if (firstSpectrum.getNucleus(2) == spectraData.getNucleus())
						isVertical = true;
				}
			}
			spectra.isVertical(isVertical);
			if (! mainDisplay.isAbsoluteYScale()) {
				spectra.setLocation(spectra.getLocation().x, vPosition);
			}
			mainPanel.getUserDialog().setText("Adding spectrum to display");
			mainDisplay.addSpectra(spectra);
			
			mainPanel.setActiveEntity(spectra); // we select the last added spectrum
			

			
			spectra.refreshSensitiveArea();
			spectra.setSpectraNb(0); // WARNING!! for test only!! The
			// first spectrum may not be the one we want
			// in the main display
			mainPanel.getUserDialog().setText("Spectrum loaded");
			if (DEBUG) System.out.println("Spectrum loaded");

		} else {
			mainPanel.getUserDialog().setText("Could Not Access File " + correctUrl);
			return null;
		}
		mainPanel.checkButtonsStatus();
		return spectra;
	}

	public void setFirstSpectraData(SpectraData spectraData) {
		this.mainDisplay.getFirstSpectra().spectraData = spectraData;
		this.mainPanel.repaint();
	}
	
	public Spectra addJcamp(String urlString) {
		return addJcamp(urlString,null);
	}
	
	/**
	 * Method to add a new spectrum to myDisplay loading it from a jdx file
	 * pointed by the urlString
	 */
	public Spectra addJcamp(String urlString, String name) {
		return addJcamp(urlString, name, false);
	}
	
	/**
	 * Method to add a new spectrum to myDisplay loading it from a jdx file
	 * pointed by the urlString
	 */
	public Spectra addJcamp(String urlString, String name, boolean storeTotalJcamp) {
		int vPosition = mainDisplay.getNbAddedSpectra() * (-20) - 10;
		if (mainDisplay.getFirstSpectra() != null && mainDisplay.getFirstSpectra().isDrawnAs2D()) {
			vPosition = 0;
		}
		Spectra spectra=loadSpectrum(urlString, vPosition, false, storeTotalJcamp);
		//System.out.println(spectra);
		spectra.setReferenceName(name);
		//Character a = new Chara
		mainDisplay.storeView(SHOW_ALL_SC);
		return spectra;
	}

	public boolean setMolfileFromUrl(String urlString) {
		return setMolfileFromUrl(50, 50, 150, 150, urlString);
	}
	
	/**
	 * Method to add a new molfile from a URL
	 */
	public boolean setMolfileFromUrl(int x, int y, int w, int h, String urlString) {
		URL correctUrl = null;
		try {
			correctUrl = new URL(urlString);
		} catch (MalformedURLException e) {
			try {
				correctUrl = new URL(docBase, urlString);
			} catch (MalformedURLException er) {
				mainPanel.getUserDialog().setText("Not found :" + urlString);
				return false;
			}
		}
		try {
			String molfile="";
			InputStreamReader stream=new InputStreamReader(correctUrl.openStream());
			BufferedReader inputFile=new BufferedReader(stream, 262144);
			
			String currentLine=null;
	
			while ((currentLine=inputFile.readLine())!=null) {
				molfile+=currentLine+"\n";
			}
	
			inputFile.close();
			stream.close();
			
			this.setMolfile(x,y,w,h,molfile);
		} catch (Exception e) {
			mainPanel.getUserDialog().setText("IOException :" + e.toString());
			return false;			
		}
		return true;
	}

	public static void main(String argv[]) {
		Nemo nemo = new Nemo();
		nemo.init();
		Frame frame = new Frame();
		frame.add(nemo);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}

	public void addSpectrum(SpectraData spectraData) {
		Spectra spectra = new Spectra(spectraData);
		spectra.isVertical(false);

		spectra.setLocation(0, mainDisplay.getNbAddedSpectra() * (-20) - 10);
		this.mainDisplay.addSpectra(spectra);
		this.mainPanel.checkButtonsStatus();

	}

	/**
	 * Add a spectrum from an experimental part String
	 * @param description
	 * @param resolution
	 */
	public void addSimulatedSpectrum(String description, double resolution) {
		ActMoleculeDisplay molDisplay = getMolDisplay();
		if(molDisplay!=null){
			JSONArray diaIDs = ProprietaryTools.getDiaIDs(molDisplay);
			this.addSimulatedSpectrum(description, "{linewidth:"+resolution+",assign:true, diaIDs:"+diaIDs+"}");
		}
		else{
			this.addSimulatedSpectrum(description, "{linewidth:"+resolution+",assign:true}");
		}
	}
	
	/**
	 * Add a spectrum from an experimental part String
	 * @param description
	 * @param json parameters
	 */
	public void addSimulatedSpectrum(String description, String parameters) {
		//System.out.println(description);
		//System.out.println(parameters);
		ActMoleculeDisplay molDisplay = getMolDisplay();
		//JSONArray diaIDs = ProprietaryTools.getDiaIDs(molDisplay);
		int vPosition = mainDisplay.getNbAddedSpectra() * (-20) - 10;
		
		SpectraDataExt spectrum =  SimulateFromDescription.simulate(description, parameters);
		NMRSignal1D[] peakPicking = null;
		Object filter = spectrum.getAppliedFilterByFilterType(FilterType.SIMULATOR);
		if(filter instanceof SimulateNMRSpectrumFilter)
			peakPicking = ((SimulateNMRSpectrumFilter)filter).getPeakPicking();
		if(filter instanceof ResurrectNMRSpectrumFilter)
			peakPicking = ((ResurrectNMRSpectrumFilter)filter).getPeakPicking();
		//System.out.println("666666 "+peakPicking.length);
		if(molDisplay!=null)
			peakPicking = ProprietaryTools.mergeSignalsByDiaID(molDisplay, peakPicking);
		
		//System.out.println("To convert to skmart peak labels "+peakPicking.length+" "+peakPicking[0]);
		
		Spectra spectra = new Spectra(spectrum);
		
		mainDisplay.addSpectra(spectra);
		spectra.setLocation(spectra.getLocation().x, vPosition);
		mainDisplay.checkInteractiveSurface();
		//System.out.println("PeakPicking size "+peakPicking.length);
		if(peakPicking!=null&&peakPicking.length>0&&peakPicking[0].getIntegralData()!=null&&peakPicking[0].getIntegralData().getValue()!=0){
			//System.out.println("Yes integrals");
			this.addSmartPeaksFromNMRSignal1D(peakPicking, true, false);
		
		}else{
			//System.out.println("No integrals");
			this.addSmartPeaksFromNMRSignal1D(peakPicking, false, false);
		}
		
		// is molDisplay present? If yes, try to create the links to the atoms
		
		
		//System.out.println("After molfile "+peakPicking.length);
		
		if (molDisplay != null) {
			NmrSimulator.createFullPeakAtomLinks(molDisplay, spectra);
		} else {
			System.out.println("molDisplay not found");
		}
		//System.out.println("beforre button status"+peakPicking.length);
		this.mainPanel.checkButtonsStatus();
		//System.out.println("end");
	}
	

	/*public void addSimulatedSpectrum(String description, double resolution) {
		int vPosition = mainDisplay.getNbAddedSpectra() * (-20) - 10;
		 
		NmrSimulator simulator = new NmrSimulator(mainPanel);
		
		simulator.setResolution(resolution);
		
		Spectra spectra = simulator.simulate(description);
		

		mainDisplay.addSpectra(spectra);
		spectra.setLocation(spectra.getLocation().x, vPosition);
		mainDisplay.checkInteractiveSurface();

		// is molDisplay present? If yes, try to create the links to the atoms
		ActMoleculeDisplay molDisplay = (ActMoleculeDisplay) this.mainPanel.getEntityByName("molDisplay");

		if (molDisplay != null) {
			NmrSimulator.createPeakAtomLinks(molDisplay, spectra);
		} else {
			System.out.println("molDisplay not found");
		}
		this.mainPanel.checkButtonsStatus();
	}*/

	public void setSimulatedSpectrum(String prediction, double resolution) {
		if (! this.mainDisplay.is2D()) {
			this.mainDisplay.remove1DSpectra("Reconstructed spectrum");
		}
		this.addSimulatedSpectrum(prediction, resolution);
	}
		
	public void addJcampVertical(String urlString) {
		addJcampVertical(urlString, false);
	}
	
	public void addJcampVertical(String urlString, boolean storeTotalJcamp) {
		/*
		 * int vPosition=mainDisplay.getNbSpectra()*(-20); if
		 * (mainDisplay.getLastSpectra() != null &&
		 * mainDisplay.getLastSpectra().isDrawnAs2D()) vPosition=0;
		 */
		loadSpectrum(urlString, 0, true, storeTotalJcamp);
		this.mainPanel.checkButtonsStatus();
	}

	public void setMoleculeByIDCode(String idCode) {
		ActMoleculeDisplay molDisplay = new ActMoleculeDisplay();
		molDisplay.setEntityName("molDisplay");
		molDisplay.setLocation(50, 50);

		molDisplay.setMovementType(BasicEntity.GLOBAL);
		molDisplay.setErasable(true);

		mainDisplay.addEntity(molDisplay);

		molDisplay.init(150, 150);
		molDisplay.addMoleculeByIDCode(idCode);
		
		Spectra spectra = mainDisplay.getFirstSpectra();
		if(spectra!=null){
			NmrSimulator.createFullPeakAtomLinks(molDisplay, spectra);
		}
	}

	
	public String canonizeMolfile(String molfile) {
	// 	System.out.println(molfile);
		return ProprietaryTools.canonizeMolfile(molfile);
	}
	
	public String addMolfile(String molfile) {
		molfile = canonizeMolfile(molfile);
		addMolfileACT(molfile, 50, 50, 150, 150, false);
		return molfile;
	}

	public String setMolfile(String molfile) {
		molfile = canonizeMolfile(molfile);
		setMolfileACT(molfile, false);
		return molfile;
	}

	public void setMolfile(String molfile, boolean expandAll) {
		this.mainPanel.checkButtonsStatus();
	}

	public void setMolfile(int x, int y, int w, int h, String molfile) {
		if (molfile == null)
			return;
		for (int ent = 0; ent < mainDisplay.getEntitiesCount(); ent++) {
			if (mainDisplay.getEntity(ent) instanceof ActMoleculeDisplay)
				((ActMoleculeDisplay) mainDisplay.getEntity(ent)).delete();
		}
		this.addMolfileACT(molfile, x, y, w, h, false);
	}

	// _____ ACTELION VERSION __________________________

	public void addMolFileACT(String molfile) {
		this.addMolfileACT(molfile, 50, 50, 150, 150, false);
	}

	public ActMoleculeDisplay addMolfileACT(String molfile, int x, int y, int w, int h, boolean expandAll) {
		ActMoleculeDisplay molDisplay = new ActMoleculeDisplay();
		molDisplay.setEntityName("molDisplay");
		molDisplay.setLocation(x, y);
		molDisplay.setMovementType(BasicEntity.GLOBAL);
		molDisplay.setErasable(true);
		mainDisplay.addEntity(molDisplay,0);
		molDisplay.init(w, h);
		molDisplay.addMolfile(molfile, expandAll);
		
		Spectra spectra = mainDisplay.getFirstSpectra();
		if(spectra!=null){
			boolean link = false;
			for (int ent = 0; ent < spectra.getEntitiesCount(); ent++)
				if (spectra.getEntity(ent) instanceof SmartPeakLabel){
					link=true;
					break;
				}
			if(link)
				NmrSimulator.createFullPeakAtomLinks(molDisplay, spectra);
		}
		
		this.mainPanel.checkButtonsStatus();
		return molDisplay;
	}

	public ActMoleculeDisplay setMolfileACT(String molFile, boolean expandAll) {
		for (int ent = 0; ent < mainDisplay.getEntitiesCount(); ent++) {
			if (mainDisplay.getEntity(ent) instanceof ActMoleculeDisplay)
				((ActMoleculeDisplay) mainDisplay.getEntity(ent)).delete();
		}
		return this.addMolfileACT(molFile, 50, 50, 150, 150, expandAll);
	}

	// ____________ END OF ACTELION VERSION ____________________

	/**
	 * Method to retrieve an XML String containing all the characteristics
	 * required to recreate the current visual output.
	 * 
	 * @return a String containing the XML representation.
	 */
	public String getXML() {
		Hashtable xmlProperties = new Hashtable();
		xmlProperties.put("includeURL", new Boolean(true));
		xmlProperties.put("embedJCamp", new Boolean(true));
		return mainDisplay.getXmlTag(xmlProperties);
	}

	public String getXMLView() {
		Hashtable xmlProperties = new Hashtable();
		xmlProperties.put("includeURL", new Boolean(false));

		return mainDisplay.getXmlTag(xmlProperties);
	}

	public String getXMLEmbedded() {
		Hashtable xmlProperties = new Hashtable();
		xmlProperties.put("embedJCamp", new Boolean(true));
		return mainDisplay.getXmlTag(xmlProperties);
	}

	public void setXML(String XMLString) {
		Hashtable helpers = new Hashtable();
		this.setXML(XMLString, helpers);
	}

	public void setXML(String XMLString, Hashtable helpers) {
		setXML(XMLString, helpers, false);
	}
	
	public void setXML(String XMLString, Hashtable helpers, boolean storeTotalJcamp) {
		//System.out.println(XMLString);
		//System.out.println("HJKHKJH");
		mainDisplay.init(XMLString, mainPanel.getSize().width, mainPanel.getSize().height, helpers, storeTotalJcamp);
		mainDisplay.checkInteractiveSurface();
		mainDisplay.restoreLinks();
		CompatibilityLinks.newLinkNmrSignal1D2IntegralData(mainDisplay, this.getInteractions());
		mainPanel.addActiveEntity(mainDisplay.getFirstSpectra());
		this.mainPanel.checkButtonsStatus();
		this.mainPanel.repaint();
		mainDisplay.storeView(SHOW_ALL_SC);
	}

	public void setXMLViewFromFile(String url) {
		String XMLString = loadFile(url);
		setXMLView(XMLString);
	}

	public void setXMLView(String XMLString) {
		SpectraData originalData = mainDisplay.getFirstSpectra()
				.getSpectraData();

		if (originalData == null)
			System.out.println("NULL");
		Hashtable helpers = new Hashtable();

		helpers.put("originalSpectraData", originalData);

		mainDisplay.init(XMLString, mainPanel.getSize().width, mainPanel.getSize().height, helpers);
		mainDisplay.checkInteractiveSurface();
		mainDisplay.restoreLinks();
		mainPanel.checkButtonsStatus();
		mainPanel.repaint();
	}

	public void resize(int width, int height)
	{
		super.resize(width, height);
		validate();
		this.doLayout();
		if (mainDisplay != null)
			mainDisplay.checkSizeAndPosition();
		if (mainPanel != null)
			mainPanel.repaint();

	}
	/**
	 * Returns an HTML string containing a formatted description of the NMR
	 * spectra for publications (if available).
	 * 
	 * @return a string containing the HTML. By default chemical shifts are
	 *         sorted in decreasing order.
	 */
//	public String getNmrHtml() {
//		return getNmrHtml(false);
//	}

	/**
	 * Returns an HTML string containing a formatted description of the NMR
	 * spectra for publications (if available).
	 * 
	 * @param ascending
	 *            if true sort in the ascending order, otherwise descending.
	 * @return a string containing the HTML.
	 */
//	public String getPeakHtml(String format, boolean ascending) {
//		Spectra spectrum = mainDisplay.getFirstSpectra();
//		return NmrHelpers.getPeakHtml(spectrum, format, ascending);
//	}

	/*
	public String getPeakHtml(boolean ascending) {
		return this.getPeakHtml(null, ascending);
	}
	
	public String getNmrHtml(boolean ascending) {
		return getPeakHtml("#0.00", ascending);
	}
	*/
	
	/**
	 * Returns true is the mainDisplay is drawn as a 2D.
	 */
	public boolean is2D() {
		try {
			return mainDisplay.is2D();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns the type of the first spectrum as a String.
	 * 
	 * @return
	 */
	public String getFirstSpectrumType() {
		try {
			return mainDisplay.getFirstSpectra().getSpectraData()
					.getDataTypeAsString();
		} catch (Exception e) {
			return "";
		}
	}
	
	
	public Spectra getSpectrum(String name) {
		SpectraDisplay display = (SpectraDisplay)this.getMainDisplay();
		Spectra spectra=display.getSpectra(name);
		if (spectra==null) {
			return this.createNewSpectrum(name);
		}
		return spectra;
	}
	
	/* You should always use getSpectrum(String name) that will check if it exists or if it has to be created. */
	public Spectra createNewSpectrum(String name) {
		SpectraData spectraData = new SpectraData();
		Spectra spectra = new Spectra(spectraData);
		spectra.setReferenceName(name);

		SpectraDisplay display = (SpectraDisplay)this.getMainDisplay();
		display.addSpectra(spectra);
		int vPosition = display.getNbAddedSpectra() * (-20) - 10;
		spectra.setLocation(spectra.getLocation().x, vPosition);
		return spectra;
		
	}
	//acastillo. I've removed the comment on this lines, cause I need it.
	public void repaint(){
		if(mustRepaint)
			mainDisplay.checkAndRepaint();
		super.repaint();
	}
	
	public JSONObject runScript(String script) throws JSONException {
		if (interpreter==null) {
			System.out.println("Javascript interpreter is null");
			return null;
		}
		interpreter.addObjectToScope("out", System.out);
		interpreter.addObjectToScope("spectraDisplay", this.mainDisplay);
		AppletNemo func = (AppletNemo)interpreter.getObjectFromScope("Nemo");
		func.setSpectraDisplay(this.mainDisplay);
		JSONObject toReturn=interpreter.runScript(script);
		mainDisplay.checkAndRepaint();
		return toReturn;
	}
	//FilterManager BUG
	/*public String runScript(String script) throws JSONException {
		JSONArray jsonResult=new JSONArray();
		if (mainDisplay.getNbSpectra() > 0) {
			Spectra targetSpectrum = null;
			if (this.mainPanel.getActiveEntity() instanceof Spectra) {
				targetSpectrum = (Spectra) this.mainPanel.getActiveEntity();
			} else {
				targetSpectrum = this.mainDisplay.getLastSpectra();
			}
			targetSpectrum.setJsonResult(jsonResult);
			targetSpectrum.runScript(script);
			jsonResult=targetSpectrum.getJsonResult();
			mainDisplay.checkAndRepaint();
		} else {
			 mainPanel.getUserDialog().setText("There is no active Spectra and the script can not be run");
		}
		return (new JSONObject().put("result",jsonResult)).toString();
	}
	*/

	public String getJSON() {
		try {
			CompatibilityLinks.newLinkNmrSignal1D2IntegralData(mainDisplay, this.getInteractions());
			JSONObject json=new JSONObject();
			JSONArray jsonSpectraDisplay=new JSONArray();
			json.put("spectraDisplay", jsonSpectraDisplay);
			JSONObject jsonMainDisplay=new JSONObject();
			jsonSpectraDisplay.put(jsonMainDisplay);
			jsonMainDisplay.put("label","mainDisplay");
			mainDisplay.appendJSON(jsonMainDisplay);
			return json.toString();
		} catch (Exception e) {
			return "ERROR: "+e.toString();
		}
	}
	

	/*
	 * public void autoPeakPicking(double minRelIntensity) {
	 * 
	 * mainDisplay.getFirstSpectra().autoPeakPicking(minRelIntensity); }
	 */
	/**
	 * Moves the last added spectrum on the vertical axis of the number of
	 * pixels specified by the parameter pixels.
	 * 
	 * @param pixels
	 *            an integer representing the number of pixels for the vertical
	 *            translation (positive is UP)
	 */
	public void vMoveSpectrum(int pixels) {
		Spectra tempSpectra = mainDisplay.getLastSpectra();
		tempSpectra.setLocation(tempSpectra.getLocation().x, tempSpectra.getLocation().y - pixels);
	}

	/**
	 * Moves the spectrum (referenced by the 'reference' parameter) on the
	 * vertical axis by the number of pixels specified by the parameter pixels.
	 * 
	 * @param reference
	 *            the String uniquely identifying the desired Spectra.
	 * @param pixels
	 *            an integer representing the number of pixels for the vertical
	 *            translation (positive is UP).
	 */
	/*
	 * public void vMoveSpectrum(int pixels, String reference) { Spectra
	 * tempSpectra=mainDisplay.getReferencedSpectra(reference);
	 * tempSpectra.setLocation(tempSpectra.getLocation().x,
	 * tempSpectra.getLocation().y-pixels); }
	 */
	/**
	 * Deletes the last inserted spectrum
	 */
	/*
	 * public void deleteSpectrum() { Spectra
	 * tempSpectra=mainDisplay.getLastSpectra(); tempSpectra.delete(); }
	 */
	/**
	 * Deletes the spectrum referenced by 'reference'
	 * 
	 * @param reference
	 *            a string uniquely identifying the desired spectrum.
	 */
	/*
	 * public void deleteSpectrum(String reference) { Spectra
	 * tempSpectra=mainDisplay.getReferencedSpectra(reference);
	 * tempSpectra.delete(); }
	 */
	/**
	 * Changes the color of the last added spectrum to the one specified by the
	 * 'color' parameter.
	 * 
	 * @param color
	 *            a string containing the name of the desired color ('black',
	 *            'gray', 'yellow', 'green', 'cyan', 'magenta', 'blue', 'red',
	 *            'white')
	 */
	public void setColor(String color) {
		Spectra tempSpectra = mainDisplay.getLastSpectra();
		Color newColor = Color.black;
		color=color.toLowerCase().replaceAll(" ", "");

		if (color.compareTo("black") == 0)
			newColor = Color.BLACK;
		else if (color.compareTo("gray") == 0)
			newColor = Color.GRAY;
		else if (color.compareTo("yellow") == 0)
			newColor = Color.YELLOW;
		else if (color.compareTo("green") == 0)
			newColor = Color.GREEN;
		else if (color.compareTo("cyan") == 0)
			newColor = Color.CYAN;
		else if (color.compareTo("magenta") == 0)
			newColor = Color.MAGENTA;
		else if (color.compareTo("blue") == 0)
			newColor = Color.BLUE;
		else if (color.compareTo("red") == 0)
			newColor = Color.RED;
		else if (color.compareTo("white") == 0)
			newColor = Color.WHITE;
		else if (color.compareTo("darkgreen") == 0)
			newColor = DARK_GREEN;
		else if (color.compareTo("darkorange") == 0)
			newColor = DARK_ORANGE;
		else if (color.compareTo("orange") == 0)
			newColor = Color.ORANGE;
		else if(color.contains("#")){
				int intValue = Integer.parseInt( color.substring(1),16);
				newColor = new Color( intValue );
			}
			
		tempSpectra.setPrimaryColor(newColor);
		mainPanel.repaint();
	}

	public void setIntegralsVisible(boolean visible) {
		if (visible)
			mainDisplay.getLastSpectra().setIntegralsVisible(1);
		else
			mainDisplay.getLastSpectra().setIntegralsVisible(0);
	}

	public void setPeaksVisible(boolean visible) {
		if (visible)
			mainDisplay.getLastSpectra().setPeakLabelsVisible(1);
		else
			mainDisplay.getLastSpectra().setPeakLabelsVisible(0);
	}

	public void clearAll() {

		mainPanel.removeAllLinks(mainDisplay);
		mainDisplay.delete();

		mainPanel.removeAllEntities();

		mainDisplay = new SpectraDisplay();
		mainDisplay.setErasable(false);
		mainPanel.addEntity(mainDisplay);
		mainDisplay.init();

		mainPanel.repaint();
	}

	public void scaleSpectrum(double scaleFactor) {
		mainDisplay.getLastSpectra().setMultFactor(
				mainDisplay.getLastSpectra().getMultFactor() * scaleFactor);
		mainDisplay.getLastSpectra().checkSizeAndPosition();
		mainPanel.repaint();
	}

	/**
	 * Changes the color of the spectrum referenced by 'reference' to the one
	 * specified by the 'color' parameter.
	 * 
	 * @param color
	 *            a string containing the name of the desired color ('black',
	 *            'gray', 'yellow', 'green', 'cyan', 'magenta', 'blue', 'red',
	 *            'white')
	 * @param reference
	 *            the String uniquely identifying the desired Spectra.
	 */
	/*
	 * public void setColor(String color, String reference) { Spectra
	 * tempSpectra=mainDisplay.getReferencedSpectra(reference); Color
	 * newColor=Color.black;
	 * 
	 * System.out.println (reference+" - "+tempSpectra);
	 * 
	 * if (color.toLowerCase().compareTo("black") == 0) newColor=Color.black;
	 * else if (color.toLowerCase().compareTo("gray") == 0) newColor=Color.gray;
	 * else if (color.toLowerCase().compareTo("yellow") == 0)
	 * newColor=Color.yellow; else if (color.toLowerCase().compareTo("green") ==
	 * 0) newColor=Color.green; else if (color.toLowerCase().compareTo("cyan") ==
	 * 0) newColor=Color.cyan; else if (color.toLowerCase().compareTo("magenta") ==
	 * 0) newColor=Color.magenta; else if (color.toLowerCase().compareTo("blue") ==
	 * 0) newColor=Color.blue; else if (color.toLowerCase().compareTo("red") ==
	 * 0) newColor=Color.red; else if (color.toLowerCase().compareTo("white") ==
	 * 0) newColor=Color.white;
	 * 
	 * tempSpectra.setPrimaryColor(newColor); mainDisplay.repaint(); }
	 */
	/**
	 * Method to load an image from the file pointed by imageName Makes use of
	 * urlLocation.
	 */
	private Image getImage(String imageName) {
		Image image = null;

		try {
			//System.out.println("Path is " + getClass() + "/../gif/");
			// InputStream in = getClass().getResourceAsStream(imageName);
			InputStream in = getClass().getResourceAsStream("/org/cheminfo/hook/gif/" + imageName);
			if (in != null) {
				byte[] buffer = new byte[in.available()];
				in.read(buffer);
				image = Toolkit.getDefaultToolkit().createImage(buffer);
			} else {
				System.out.println("input stream is null");
			}
		} catch (java.io.IOException e) {
		}
		
		if (image == null) {
			image = getImage(urlLocation, imageName);
		}

		if (image == null)
			System.out.println("Image not found " + urlLocation);

		return image;
	}

	public SpectraData getSpectraData() {
		if (mainDisplay.getInteractiveSurface().getActiveEntity() instanceof Spectra) {
			return ((Spectra)mainDisplay.getInteractiveSurface().getActiveEntity()).getSpectraData();
		}
		return mainDisplay.getAllSpectra().get(0).getSpectraData();
	}
	
	public SpectraData getSpectraData (int id) {
		try {
			return mainDisplay.getAllSpectra().get(id).getSpectraData();
		} catch (Exception e) {
			System.out.println("could not retrieve SpectraData: "+e.toString());
			return null;
		}
	}
	
	
	public void actionPerformed(ActionEvent e) {
	}

	/*
	 * protected void makeButtonLight(DefaultActionButton theButton, Panel
	 * thePanel, String imageName, String buttonDescription) {
	 * theButton.setInfoMessage(buttonDescription);
	 * 
	 * theButton.setButtonImage(getImage(imageName)); thePanel.add(theButton);
	 * interactions.addButton(theButton);
	 * theButton.setInteractionClass(interactions); }
	 * 
	 * protected void setApplicationPanel(Panel newPanel) {
	 * this.applicationPanel=newPanel; }
	 * 
	 */public InteractiveSurface getInteractions() {
		return this.mainPanel;
	}

	public BasicDisplay getMainDisplay() {
		return this.mainDisplay;
	}

	String loadFile(String stringUrl) {
		URL correctUrl = null;
		String toReturn = "";
		try {
			correctUrl = new URL(stringUrl);
		} catch (MalformedURLException er) {
			// mainPanel.getUserDialog().setText("Not found : "+stringUrl);
			return "";
		}

		BufferedReader inputFile;

		try {
			InputStreamReader stream = new InputStreamReader(correctUrl
					.openStream());
			inputFile = new BufferedReader(stream);

			String currentLine = inputFile.readLine();
			while (currentLine != null) {
				toReturn += currentLine;
				currentLine = inputFile.readLine();
			}
		} catch (Exception e) {
			return "";
		}
		return toReturn;
	}

	public void setPrediction(String prediction) {
		if (this.mainDisplay.is2D()) {
			Spectra mainSpectrum = this.mainDisplay.getFirstSpectra();
			if (mainSpectrum != null) {
				for (int i = mainSpectrum.getEntitiesCount() - 1; i >= 0; i--) {
					if (mainSpectrum.getEntity(i) instanceof PredictionLabel) {
						mainSpectrum.remove(i);
					}
				}
			}
		} else {
			this.mainDisplay.remove1DSpectra("Simulated spectrum");
		}
		this.addPrediction(prediction);
	}

	public static String canonizePrediction(String molfile, String prediction, boolean suppressLabile) {
		ActMoleculeDisplay molDisplay = new ActMoleculeDisplay();
		molDisplay.addMolfile(molfile, true);
		PredictionData predictionData = new PredictionData();
		predictionData.setMoleculeDisplay(molDisplay);
		predictionData.setInputData(prediction);
		predictionData.process();
		return predictionData.canonize(suppressLabile);
	}
	
	public void addPrediction(String prediction) {
		ActMoleculeDisplay molDisplay = this.getMolDisplay();
		PredictionData predictionData = new PredictionData();
		predictionData.setMoleculeDisplay(molDisplay);
		predictionData.setSpectraDisplay(this.mainDisplay);
		predictionData.setInputData(prediction);
		predictionData.process();
		this.mainPanel.checkButtonsStatus();
	}

	public void addPrediction(String prediction, int minDistance,
			int maxDistance) {
		ActMoleculeDisplay molDisplay = this.getMolDisplay();
		PredictionData predictionData = new PredictionData();
		predictionData.setMinDistance(minDistance);
		predictionData.setMaxDistance(maxDistance);
		predictionData.setMoleculeDisplay(molDisplay);
		predictionData.setSpectraDisplay(this.mainDisplay);
		predictionData.setInputData(prediction);
		predictionData.process();
		this.mainPanel.checkButtonsStatus();
	}
	
	public String getQueryHoseCodes(String queryNucleus) {
		return ProprietaryTools.getQueryHoseCodes(this.getMolDisplay(), this.mainDisplay.getFirstSpectra(), queryNucleus); 
	}

	public String getHoseCodes4Assignment () {
		return ProprietaryTools.getHoseCodes4Assignment(this.getMolDisplay(), this.mainDisplay, this.mainPanel);
	}
	
	public void addTextColRow(int x, int y, int cols, int rows, String text) {
		Spectra spectrum = this.mainDisplay.getFirstSpectra();
		if (spectrum != null) {
			SpectraTextEntity newEntity = new SpectraTextEntity(cols, rows);
			newEntity.setLocation(x, y);
			spectrum.addEntity(newEntity);
			newEntity.setText(text);
			newEntity.checkSizeAndPosition();
			this.mainPanel.repaint();
		}
	}
	
	public void addText(int x, int y, int w, int h, String text) {
		Spectra spectrum = this.mainDisplay.getFirstSpectra();
		if (spectrum != null) {
			SpectraTextEntity newEntity = new SpectraTextEntity(0, 0);
			newEntity.setLocation(x, y);
			newEntity.setSize(w, h);
			spectrum.addEntity(newEntity);
			newEntity.setText(text);
			newEntity.checkSizeAndPosition();
			this.mainPanel.repaint();
		}
	}

	public String getHorizontalNucleus() {
		Spectra spectrum = this.mainDisplay.getFirstSpectra();
		Nucleus nucleus = null;
		if (spectrum != null)
			nucleus = spectrum.getNucleus();
		if (nucleus == null)
			return null;
		else {
			return nucleus.toString();
		}
	}

	public String getVerticalNucleus() {
		Spectra spectrum = this.mainDisplay.getFirstSpectra();
		Nucleus nucleus = null;
		if (spectrum != null)
			nucleus = spectrum.getNucleus(2);
		if (nucleus == null)
			return null;
		else
			return nucleus.toString();
	}

	protected ActMoleculeDisplay getMolDisplay() {
		ActMoleculeDisplay molDisplay = null;
		molDisplay = (ActMoleculeDisplay) this.mainPanel
				.getEntityByName("molDisplay");
		if (molDisplay == null) {
			if (molDisplay == null) {
				for (int ent = 0; ent < this.mainDisplay.getEntitiesCount(); ent++) {
					if (this.mainDisplay.getEntity(ent) instanceof ActMoleculeDisplay) {
						molDisplay = (ActMoleculeDisplay) this.mainDisplay
								.getEntity(ent);
						break;
					}
				}
			}
		}
		return molDisplay;
	}

	public void simulate2DSpectrum(String inputData, String strXNucleus, String strYNucleus, int minDistance, int maxDistance, boolean include1D) {
		ActMoleculeDisplay molDisplay = this.getMolDisplay();
		if (molDisplay == null)
			return;
		Nucleus xNucleus = Nucleus.determineNucleus(strXNucleus);
		if (xNucleus == Nucleus.UNDEF)
			return;
		Nucleus yNucleus = Nucleus.determineNucleus(strYNucleus);
		if (yNucleus == Nucleus.UNDEF)
			return;

		PredictionData predictionData = new PredictionData();
		predictionData.setXNucleus(xNucleus);
		predictionData.setYNucleus(yNucleus);
		predictionData.setMinDistance(minDistance);
		predictionData.setMaxDistance(maxDistance);
		predictionData.setInputData(inputData);
		predictionData.setMoleculeDisplay(molDisplay);

		Spectra simulatedSpectrum = predictionData.simulate2DSpectrum();
		if (simulatedSpectrum != null) {
			this.mainDisplay.addSpectra(simulatedSpectrum);
			predictionData.linkPredictionLabelsToAtoms(simulatedSpectrum);
			simulatedSpectrum.setPredictionData(predictionData);
		}
		this.mainPanel.repaint();
	}

	public void detectPeaks() {
		for (int i = 0; i < this.mainPanel.getButtons(); i++) {
			if (this.mainPanel.getButton(i) instanceof PeakDetectionActionButton) {
				PeakDetectionActionButton peakDetectionButton = (PeakDetectionActionButton)this.mainPanel.getButton(i);
				peakDetectionButton.performInstantAction();
			}
		}
	}
	
	public void addSmartPeaksFromNMRSignal1D(NMRSignal1D[] signals, boolean addIntegrals, boolean createAtomLinks){
		Spectra spectrum=this.mainDisplay.getLastSpectra();
		SmartPickingHelpers.addSmartPeakLabels(this.getInteractions(), spectrum, signals, !addIntegrals);
		//System.out.println("Nemo.addSmartPeaksFromNMRSignal1D "+createAtomLinks);
		/*if(this.getMolDisplay()!=null){
			 for (int ent = 0; ent < spectrum.getEntitiesCount(); ent++) {
					if (spectrum.getEntity(ent) instanceof SmartPeakLabel) {
						SmartPeakLabel label = (SmartPeakLabel) spectrum.getEntity(ent);
						ProprietaryTools.getDiaIDs(this.getInteractions(), this.getMolDisplay(), label, spectrum.getNucleus());
					}
			 }
		 }
			 
        SmartPickingHelpers.mergeIdenticalPeakLabels(spectrum);*/
        if(this.getMolDisplay()!=null&&createAtomLinks)
        	NmrSimulator.createPeakAtomLinks(this.getMolDisplay(), spectrum);
        this.mainDisplay.refreshSensitiveArea();
        this.mainPanel.repaint();
		
	}
	
	public String getJSONResult() {
		return jSONResult.toString();
	}	
	
	public ScriptingInstance getInterpreter(){
		return this.interpreter;
	}

	public String getPluginsFolder() {
		return pluginsFolder;
	}

	public void setPluginsFolder(String pluginsFolder) {
		this.pluginsFolder = pluginsFolder;
	}
}
