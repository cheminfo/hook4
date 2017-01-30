package org.cheminfo.hook.appli;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.cheminfo.hook.nemo.Nemo;

/**
 * 
 *@author Damiano Banfi
 */ 
public class ProtoAppli extends JFrame
{
	AllInformation allInformation;

	public ProtoAppli (AllInformation allInformation) {
		super();
		this.addWindowListener(new MyWindowAdapter());
		this.allInformation=allInformation;
		
		Container container=this.getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		// Allows to have the menu over the applet
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		final Nemo nemo=new Nemo();
	
		final JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel label=new JLabel("test test: ");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(label);
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		JTextArea textArea=new JTextArea(6,10);
		JScrollPane scrollPane=new JScrollPane(textArea);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		panel.add(scrollPane);

		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		panel.setBackground(Color.green);
		panel.doLayout();
		container.add(panel);
		ActMenuBar menu=new ActMenuBar(allInformation);
		
		// We need to add the menu
		this.setJMenuBar(menu);

		this.addComponentListener(new ComponentAdapter()
			{
				public void componentResized(ComponentEvent ce)
				{
					System.out.println("nemo: "+nemo.getSize()+", sb: "+nemo.getMinimumSize());
					if (nemo != null)
					{
						nemo.doLayout();
						
						if (nemo.getMainDisplay().getInteractiveSurface() != null)
						{
							nemo.getMainDisplay().checkSizeAndPosition();
							nemo.getMainDisplay().getInteractiveSurface().repaint();
						}
					}
				}
			}		
		);
		
		container.setBackground(Color.blue);

		container.add(nemo);
		container.setVisible(true);
		container.doLayout();
		this.pack();
		this.validate();

		nemo.startup();
		nemo.frameWork();
		
		nemo.getMainDisplay().getInteractiveSurface().isLegal=true;
	

		allInformation.setRootDisplay(nemo.getMainDisplay());
		allInformation.setNemo(nemo);

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
     	
     	ProtoAppli myMainWindow = new ProtoAppli(allInformation);

		int width=800;
		int height=700;
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width-width)/2;
		int y = (screen.height-height)/2;
		myMainWindow.setBounds(x,y,width,height);
        myMainWindow.setVisible(true);
	}
}


