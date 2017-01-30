package org.cheminfo.hook.ui;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class NemoInternalFrame<T> extends JInternalFrame implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	
    public final static String IFRAME_RESIZED = "iFrame_resized";
    public final static String IFRAME_MOVED = "iFrame_moved";
    public final static String IFRAME_HIDDEN = "iFrame_hidden";
    public final static String IFRAME_SHOWN = "iFrame_shown";

	protected NemoInstance<T> nemoInstance;
	final protected NemoPanel nemoPanel;
	final protected NemoMenuBar nemoMenuBar;
	
	public <S>NemoInternalFrame()
	{
		NemoInstance<S> ni = NemoInstance.createNemoInstance();
		this.nemoInstance = (NemoInstance<T>) ni;
        this.nemoPanel = new NemoPanel(nemoInstance);
		this.nemoMenuBar = new NemoMenuBar(nemoInstance);

		init();
	}
	
	public NemoInternalFrame(LoadSaveHandler<T> loadSaveHandler)
	{		
		this.nemoInstance = NemoInstance.createNemoInstance(loadSaveHandler);
        this.nemoPanel = new NemoPanel(nemoInstance);
		this.nemoMenuBar = new NemoMenuBar(nemoInstance);

        init();
	}
	
	protected void init()
	{
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addInternalFrameListener(new CloseAdapter());
		
		nemoInstance.addPropertyChangeListener(this);

		List<Image> icons = new ArrayList<Image>();
        ImageIcon icon = new ImageIcon(getClass().getResource("/nemo16.png"));
        setFrameIcon(icon);
        
        setTitle(nemoInstance.getTitle());

		getContentPane().add(nemoPanel);
		setJMenuBar(nemoMenuBar);
		
        int width = 800;
        int height = 600;
        setSize(new Dimension(width, height));
        
        setClosable(true);
        setResizable(true);
        setMaximizable(true);
        setIconifiable(true);
        
        enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        
		addPropertyChangeListener(this);

		
		// Hack for JInternalFrames to display correctly when containing AWT components (Nemo)
		this.addComponentListener(new ComponentAdapter()
	    {
	        public void componentResized(ComponentEvent ce)
	        {
	        	firePropertyChange(IFRAME_RESIZED, null, this);	
	        }
	        
	        public void componentMoved(ComponentEvent e) 
	        {
	         	firePropertyChange(IFRAME_MOVED, null, this);	
	 	    };
	 	    
	 	    public void componentHidden(ComponentEvent e) 
	 	    {
	 	    	firePropertyChange(IFRAME_HIDDEN, null, this);	
	 	    };
	 	    
	 	    public void componentShown(ComponentEvent e) 
	 	    {
	 	     	firePropertyChange(IFRAME_SHOWN, null, this);	
	 	    };
	    });

//        setJMenuBar(wrapper.getJMenuBar());
//		add(wrapper.getContentPane());
		      
        setVisible(true);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getSource() instanceof NemoInstance)
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
	
	protected class CloseAdapter extends InternalFrameAdapter {
		
		@Override
		public void internalFrameClosing(InternalFrameEvent e) {
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
				JOptionPane.showMessageDialog(NemoInternalFrame.this, ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public void refresh()
	{
		nemoPanel.refresh();
	}

	public NemoPanel getNemoPanel() {
		return nemoPanel;
	}
}
