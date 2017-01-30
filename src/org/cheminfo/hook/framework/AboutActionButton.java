package org.cheminfo.hook.framework;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class AboutActionButton extends DefaultActionButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7508327777411341169L;
	String infoMessage = "About Nemo";
	int buttonType = ImageButton.CLASSIC;
	private String version = "$Name:  $";

	private static final boolean isDebug = false;
	
	
	public AboutActionButton() {
		super();
		this.setButtonType(ImageButton.CLASSIC);
		this.setGroupNb(0);
		this.init();
	}

	public AboutActionButton(Image inImage) {
		super(inImage);
		this.setButtonType(ImageButton.CLASSIC);
		this.setGroupNb(0);
		this.init();
	}

	public AboutActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage);

		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);

		this.setButtonType(ImageButton.CLASSIC);
		this.setGroupNb(0);
		this.init();
	}

	protected void performInstantAction() {
		super.performInstantAction();
		String toAdd = "";
		if (this.interactions.isLegal) {
			toAdd = "+";
		}

		String outString = 
			"Copyright 2004-"+this.version.substring(8,12)+", Authors D. Banfi, L. Patiny, M. Engeler, A. Castillo  " +toAdd+ this.version
			+ "JVM: " + System.getProperty("java.version");
		
		
		interactions.getUserDialog().setText(outString);
		
		System.out.println(outString);
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean p: pools) {
             System.out.println("Memory type="+p.getType()+" Memory usage="+p.getUsage());
        }
        
		
	}

	private void init() {
		
		InputStream propertiesStream = getClass().getResourceAsStream(
				"/build.properties");
		if (propertiesStream == null)
			return;
		String buildTime = "-1";
		Properties buildProperties = new Properties();
		try {
			buildProperties.load(propertiesStream);
			Set<Object> keySet = buildProperties.keySet();
			for (Object o : keySet) {
				if (o instanceof String) {
					String name = (String) o;
					if (name.equals("build_time")) {
						buildTime = buildProperties.getProperty(name);
					}
					if (isDebug)
						System.out.println(name+"="+buildProperties.getProperty(name));
				}
			}
		} catch (IOException e) {
			System.out.println("javascript properties could not be read");
		}
		this.version = "Hook4("+buildTime+ ")  ";
		if (isDebug) {
			System.out.println("Compiled version: "+this.version);
		}
	}

	/*
	 * protected void handleEvent(ActionEvent ev) { super.handleEvent(ev); }
	 */
}
