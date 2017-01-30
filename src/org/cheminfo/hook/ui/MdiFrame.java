package org.cheminfo.hook.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.Locale;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MdiFrame extends JFrame{
	private JDesktopPane desktopPane;

	//set look and feel
	static {
    	try {
    		Locale.setDefault(Locale.ENGLISH);
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e1) {
			System.out.println("Warning: Nimbus look and feel not supported.");
		}
    }
		
	public MdiFrame() {
		super("MDI");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		desktopPane = new JDesktopPane();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				for(JInternalFrame f : MdiFrame.this.desktopPane.getAllFrames())
				{
					try {							
						f.setSelected(true);
						if(f.isIcon())
							f.setIcon(false);
					} catch (PropertyVetoException e1) {
						e1.printStackTrace();
					}
					f.doDefaultCloseAction();
				}
				
				if(MdiFrame.this.desktopPane.getAllFrames().length==0)
					dispose();
			}
		});
					
          
        //create first internal nemo frame with standard file handling
		NemoInternalFrame<File> internalFrame = new NemoInternalFrame<File>();
		internalFrame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		internalFrame.setLocation(200, 200);
        desktopPane.add(internalFrame);

        //create second internal nemo frame with delayed load of a jcamp
        //to interact with nemo use the NemoInstance class available through Nemo(Internal)Frame.getNemoInstance()
//		final NemoInternalFrame<File> internalFrame2 = new NemoInternalFrame<File>();
//		internalFrame2.setLocation(100, 100);
//        desktopPane.add(internalFrame2);
//        
//        new Thread(){
//        	public void run() {
//        		try {
//					Thread.sleep(2000);
//                    SwingUtilities.invokeLater(new Runnable() {        			
//        				public void run() {
//        					try {
//    							internalFrame2.getNemoInstance().openJcamp(new File("/home/baerr/Desktop/jdx/ELN031-1981.1_Proton012.jdx"));    					        
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//        				}
//        			});	
//				} catch (InterruptedException e) {}
//        	}            	
//        }.start();
//        
//                
//        //create third  internal nemo frame
//        //jcamp files can also originate from other ressources than files
//        //just implement LoadSaveHandler and pass it to the constructor of Nemo(Internal)Frame. E.g.
//        LoadSaveHandler<URL> handler = new LoadSaveHandler<URL>() {
//			public URL save(URL ressource, Jcamp jdx, NemoInstance<URL> nemoInstance) throws IOException {
//				//get the jcamp data using Jcamp.getContents() and write it back to the db using the URI
//				return ressource;
//			}
//			
//			public Jcamp load(URL ressource, NemoInstance<URL> nemoInstance) throws IOException {
//				//load jcamp from db and return a Jcamp instance
//				return new Jcamp(ressource);
//			}
//		};
//		NemoInternalFrame<URL> internalFrame3 = new NemoInternalFrame<URL>(handler);
//		try {
//			internalFrame3.getNemoInstance().openJcamp(new URL("http://ares:8080/orbit/rawFile?rawFile=/orbitvol1/2012-02/1198942.jdx"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//        desktopPane.add(internalFrame3);
        
        
        
        setContentPane(desktopPane);
        
        setSize(1024, 768);
                    
        setVisible(true);
	}

	public static void main(String[] args) throws InterruptedException {
    	SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame frame = new MdiFrame();                
            }
        });
        
    }
}
