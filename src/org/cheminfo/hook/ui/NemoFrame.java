package org.cheminfo.hook.ui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;


public class NemoFrame<T> extends JFrame implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;

	protected NemoInstance<T> nemoInstance;
		
	public NemoFrame()
	{
		this.nemoInstance = NemoInstance.createNemoInstance();
		init();
	}
	
	public NemoFrame(LoadSaveHandler<T> loadSaveHandler)
	{		
		this.nemoInstance = NemoInstance.createNemoInstance(loadSaveHandler);
		init();
	}
	
	protected void init()
	{
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new CloseAdapter());
		
		nemoInstance.addPropertyChangeListener(this);

		List<Image> icons = new ArrayList<Image>();
        icons.add(new ImageIcon(getClass().getResource("/nemo16.png")).getImage());
        icons.add(new ImageIcon(getClass().getResource("/nemo24.png")).getImage());
        icons.add(new ImageIcon(getClass().getResource("/nemo32.png")).getImage());
        setIconImages(icons);
        
        setTitle(nemoInstance.getTitle());

		getContentPane().add(new NemoPanel(nemoInstance));
		setJMenuBar(new NemoMenuBar(nemoInstance));
		
        int width = 800;
        int height = 600;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - width) / 2;
        int y = (screen.height - height) / 2;
        setBounds(x, y, width, height);
        
        doLayout();
        
        setVisible(true);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		setTitle(nemoInstance.getTitle());
	}

	@Override
	public void dispose() {
		nemoInstance.removePropertyChangeListener(this);
		nemoInstance.dispose();
//		nemoInstance = null;		
		setJMenuBar(null);
		removeAll();
		super.dispose();
	}
	
	public NemoInstance<T> getNemoInstance() {
		return nemoInstance;
	}
	
	protected class CloseAdapter extends WindowAdapter{
		@Override
		public void windowClosing(WindowEvent e) {
			try {
				if(nemoInstance.isDirty()) {
					int answer=JOptionPane.showConfirmDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()),"Do you want to save " + nemoInstance.getRessource() + "?");		
					
					switch (answer) {
				    	case JOptionPane.NO_OPTION:
				    		break;
				    	case JOptionPane.YES_OPTION:
				    		nemoInstance.save();
				    		break;	
				    	default :
				    		return;
				    }
				}
				dispose();
			} 
			catch(Exception ex)
			{
				JOptionPane.showMessageDialog(NemoFrame.this, ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
