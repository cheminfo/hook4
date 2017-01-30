package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.SpectraDisplay;



class ActionCopy extends AbstractAction {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1463402062172638451L;
	

	@SuppressWarnings("unused")
	private static final byte NEMOEMBED[] = {
    		(byte)'N', (byte)'E', (byte)'M', (byte)'O', 0, 0};
	
	AllInformation allInformation;

	public ActionCopy(AllInformation allInformation) {
		super("Copy");

		this.allInformation=allInformation;
		putValue(SHORT_DESCRIPTION, "Copy to Clipboard");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
	}

	public void actionPerformed (ActionEvent e)
	{
		SpectraDisplay virtualDisplay=null;
		InteractiveSurface interactions=new InteractiveSurface();
//		try
//		{
			interactions.setSize(1024, 768);
			
			virtualDisplay= new SpectraDisplay();
			virtualDisplay.setInteractiveSurface(interactions);
			interactions.addEntity(virtualDisplay);
					
//			virtualDisplay.setSize();		
			virtualDisplay.init();
			System.out.println("size "+virtualDisplay.getWidth()+", "+virtualDisplay.getHeight());
//			interactions.addNotify();
			
			Hashtable helpers=new Hashtable();
			virtualDisplay.init(allInformation.getNemo().getXMLEmbedded(), 1024, 768, helpers);
			virtualDisplay.checkInteractiveSurface();
			virtualDisplay.checkSizeAndPosition();
			
			interactions.clearActiveEntities();
//			virtualDisplay.updateSpectra();
//			interactions.addNotify();
//		} catch (Exception ex) {System.out.println("Exception "+ex+", cause "+ex.getCause());}

	/*
		if (virtualDisplay != null)
		{	
			WMF wmf = new WMF();
		
			WMFGraphics2D g= new WMFGraphics2D(wmf,1024,768);
			try
			{
				interactions.paint(g);
			}catch (OutOfMemoryError ex) 
			{
				System.out.println("here we go!");
				ex.printStackTrace();
			}
			

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try
			{
				byte sketch[] = allInformation.getNemo().getXMLEmbedded().getBytes();
	            byte temp[] = new byte[NEMOEMBED.length + sketch.length];
	            System.arraycopy(NEMOEMBED,0,temp,0,NEMOEMBED.length);
	            System.arraycopy(sketch,0,temp,NEMOEMBED.length,sketch.length);
	            wmf.escape(WMF.MFCOMMENT,temp);
				wmf.writeWMF(os);
			} catch (IOException ex) {System.out.println("IOException in wmf.writeWMF(os)");};
			
			byte[] data = os.toByteArray();
			NativeClipboardAccessor.copyMetaFile(data);
		}
	*/
	}
}


