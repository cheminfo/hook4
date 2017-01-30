package org.cheminfo.hook.appli;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.Nemo;

/**
 *
 *
 *@author Marco Engeler
 */ 
public class ConsoleNemoWrapper extends JFrame
{
	
	AllInformation allInformation;

	private Nemo nemo;
	
	public ConsoleNemoWrapper () {
		super();
		this.addWindowListener(new MyWindowAdapter());
		this.allInformation=new AllInformation();

		Container container=this.getContentPane();
		container.setSize(800,550);
		
		// Allows to have the menu over the applet
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

//		final Nemo nemo=new Nemo();
		this.nemo = new Nemo();
		container.add(nemo);
		container.setVisible(true);
	
		
		ActMenuBar menu=new ActMenuBar(allInformation);
		
		// We need to add the menu
		this.setJMenuBar(menu);

		this.addComponentListener(new ComponentAdapter()
			{
				public void componentResized(ComponentEvent ce)
				{
					Dimension newSize=ce.getComponent().getSize();
					
					nemo.getMainDisplay().getInteractiveSurface().setSize(newSize.width-24, newSize.height-2*24-55);
//					nemo.getMainDisplay().checkSizeAndPosition();
//					nemo.getMainDisplay().getInteractiveSurface().repaint();
					nemo.repaint();
				}
			}		
		);

		container.doLayout();

		this.validate();

//		nemo.init();
		nemo.startup();
		nemo.frameWork();
		
		nemo.getMainDisplay().getInteractiveSurface().isLegal=true;
	

		allInformation.setRootDisplay(nemo.getMainDisplay());
		allInformation.setNemo(nemo);

		nemo.repaint();
		
		this.setResizable(true);

	}

	class MyWindowAdapter extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
			// allInformation.getActionExit().actionPerformed(null);
		}
	}
	
	public static void main(String[] argv) {
		
		ConsoleNemoWrapper wrapper = new ConsoleNemoWrapper();
		int width=800;
		int height=600;
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width-width)/2;
		int y = (screen.height-height)/2;
		wrapper.setBounds(x,y,width,height);
		wrapper.setVisible(true);
		//wrapper.
		Nemo nemo = wrapper.getNemo();

		InteractiveSurface interactions = nemo.getInteractions();
		BufferedImage tempImage= new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics tempGraphics=tempImage.getGraphics();
		interactions.paint(tempGraphics);
		try {
			ImageIO.write(tempImage, "png", new File("test.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		wrapper.setVisible(true);

	}
	
	
	
	
	public Nemo getNemo() {
		return nemo;
	}


	public String getMolfile(File molFile)  {
		String contents = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(molFile));
			String line;
			while ((line = reader.readLine()) != null) {
				contents += line + "\n";
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contents;
	}
	

}


