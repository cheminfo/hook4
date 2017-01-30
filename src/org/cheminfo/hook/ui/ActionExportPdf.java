package org.cheminfo.hook.ui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.cheminfo.hook.appli.MiscUtilities;
import org.cheminfo.hook.util.NemoPreferences;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;


public class ActionExportPdf extends AbstractAction {
	private static final long serialVersionUID = 8517403863138336603L;
	
	protected NemoInstance nemoInstance;
	public ActionExportPdf(NemoInstance nemoInstance) {
		super("Export Pdf...");
		this.nemoInstance = nemoInstance;
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
		putValue(SHORT_DESCRIPTION, "Export a Pdf File.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent e) {	
		try {
			File file=null;

			JFileChooser fileSave=new JFileChooser(NemoPreferences.getInstance().get(NemoPreferences.getInstance().MOST_RECENT_PATH));
			fileSave.setAcceptAllFileFilterUsed(false);
			fileSave.addChoosableFileFilter(new PdfFilter());
			if (fileSave.showDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()), "Export")==JFileChooser.APPROVE_OPTION && fileSave.getSelectedFile()!=null) {
				file=fileSave.getSelectedFile();
				file=new File(file.getPath().replaceFirst("(\\.pdf$)|(^[^\\.]*$)","$2\\.pdf"));
				if (file.exists())
					if(JOptionPane.showConfirmDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()),"Do you want to overwrite " + file + "?","File exists",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						file.delete();
					else
						return;
			}
		
			if (file!=null) {	
				Rectangle page = PageSize.A4.rotate();
				float factor = 1.6f;
				
				int width=(int) page.getWidth();
				int height=(int) page.getHeight();

					// Write to pdf
					FileOutputStream fout =  new FileOutputStream(file);
									
					Document document = new Document(new Rectangle(-30,-30,width+30, height+30));
					PdfWriter writer = PdfWriter.getInstance(document, fout);
					document.open();
		
					PdfContentByte cb = writer.getDirectContent();
//					PdfTemplate tp = cb.createTemplate(width * factor, height * factor);
					PdfTemplate tp = cb.createTemplate(width, height);
					
//					Graphics2D g2 = tp.createGraphics (width * factor, height * factor);
					Graphics2D g2 = tp.createGraphics (width, height);
					g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

//					nemoInstance.drawSurface(g2, (int)(width * factor), (int)(height * factor));
					nemoInstance.drawSurface(g2, width, height, factor);
					g2.dispose();
					
//					cb.addTemplate(tp, 1/factor, 0, 0, 1/factor, 0, 0);
					cb.addTemplate(tp, 0, 0);

					document.close();
					fout.close();
//				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()), ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private class PdfFilter extends javax.swing.filechooser.FileFilter {
		
	    public boolean accept(File f) {
	        if (f.isDirectory()) {
	            return true;
	        }
	
	        String extension = MiscUtilities.getExtension(f);
	        if (extension != null) {
	            if (extension.equalsIgnoreCase("pdf")) {
	                return true;
	            } else {
	                return false;
	            }
	        }
	
	        return false;
	    }
	
	    //The description of this filter
	    public String getDescription() {
	        return "PDF file";
	    }
	}
}
