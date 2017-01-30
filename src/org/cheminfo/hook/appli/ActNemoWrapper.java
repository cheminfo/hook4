package org.cheminfo.hook.appli;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import org.cheminfo.hook.nemo.Nemo;

/**
 * Class DataTransfer  : <p>
 * The main class that generates the main window with menu, button and table.
 *
 *@author Luc Patiny
 */ 
public class ActNemoWrapper extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7926261836480171538L;
	AllInformation allInformation;

	public ActNemoWrapper (AllInformation allInformation, String plugins) {
		super();
		this.addWindowListener(new MyWindowAdapter());
		this.allInformation=allInformation;

		Container container=this.getContentPane();
		container.setSize(800,550);
		
		// Allows to have the menu over the applet
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		final Nemo nemo=new Nemo();
		nemo.setPluginsFolder(plugins);
		container.add(nemo);
		container.setVisible(true);
	
		ActMenuBar menu=new ActMenuBar(allInformation);
		
		// We need to add the menu
		this.setJMenuBar(menu);

		/*this.addComponentListener(new ComponentAdapter()
			{
				public void componentResized(ComponentEvent ce)
				{
					Dimension newSize=ce.getComponent().getSize();
					if(System.getProperty("os.name").toUpperCase().contains("MAC"))
						nemo.getMainDisplay().getInteractiveSurface().setSize(newSize.width-25, newSize.height-2*30-32);
					if(System.getProperty("os.name").toUpperCase().contains("LINUX"))
						nemo.getMainDisplay().getInteractiveSurface().setSize(newSize.width-25, newSize.height-2*30-32);
					else
						nemo.getMainDisplay().getInteractiveSurface().setSize(newSize.width-20, newSize.height-2*30-40);
					
					nemo.getMainDisplay().checkSizeAndPosition();
					nemo.getMainDisplay().getInteractiveSurface().repaint();
					nemo.repaint();
				}
			}		
		);*/

		container.doLayout();

		this.validate();
		
		nemo.startup();
		nemo.frameWork();
		
		nemo.getMainDisplay().getInteractiveSurface().isLegal=true;
	
		
		
		long code2 = (long) (Math.pow((double) 13, (double) 11) - 113);
		long code=0;
		try {//192.168.117.10210.1.104.44
			code = InetAddress.getByName("192.168.117.102").hashCode();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long theCode = Math.abs((code * 223 + 49843) ^ (code2));
		//System.out.println("xxx: "+theCode);
	

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

    public static void main(String[] args)
    {
     	// first we create our AllInformation objects that will carry all the shared information
     	AllInformation allInformation=new AllInformation();
        String pluginsFolder = "/usr/local/script/plugins";
        if(args.length>0)
        	pluginsFolder=args[0];
     	
     	ActNemoWrapper myMainWindow = new ActNemoWrapper(allInformation, pluginsFolder);

		int width=800;
		int height=600;
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width-width)/2;
		int y = (screen.height-height)/2;
		myMainWindow.setBounds(x,y,width,height);
        myMainWindow.setVisible(true);
	}

}


