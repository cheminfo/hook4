package org.cheminfo.hook.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


public class ActionPrint extends AbstractAction implements Printable {
	private static final long serialVersionUID = 8517403863138336603L;
	private static final double MM2PIXEL = 72 / 25.4;
	
	protected NemoInstance nemoInstance;
    protected PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();

    public ActionPrint(NemoInstance nemoInstance) {
		super("Print...");
		this.nemoInstance = nemoInstance;
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
		putValue(SHORT_DESCRIPTION, "Export a Pdf File.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		
		MediaSize ms = MediaSize.ISO.A4;
        int border = 10;

        aset.add(OrientationRequested.LANDSCAPE);
        aset.add(new JobName("Nemo", null));
        aset.add(MediaSizeName.ISO_A4);
        aset.add(new MediaPrintableArea(border, border, ms.getSize(MediaSize.MM)[0] - (2*border), ms.getSize(MediaSize.MM)[1] - (2*border), MediaSize.MM));
	}

	public void actionPerformed(ActionEvent e) {	
		try {
            /* Construct the print request specification.
            * The print data is a Printable object.
            * the request additonally specifies a job name, 2 copies, and
            * landscape orientation of the media.
            */
            
            /* Create a print job */
            PrinterJob pj = PrinterJob.getPrinterJob();       
            pj.setPrintable(this);
            /* locate a print service that can handle the request */
            PrintService[] services = PrinterJob.lookupPrintServices();

            if (services.length > 0) {
                    if(pj.printDialog(aset)) {
                    	
            			pj.print(aset);
                    }
            }
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()), ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	//@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		
		// Workaround: In Windows pageFormat gets changed again before the call to the print method!!!  
		
		if (pageIndex == 0) {
			nemoInstance.drawSurface((Graphics2D)graphics, pageFormat, 1.6);
			
			return PAGE_EXISTS;
		} else
			return NO_SUCH_PAGE;
	}
}
