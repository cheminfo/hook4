package org.cheminfo.hook.appli;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.SpectraDisplay;

import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;


class ActionExportPDF extends AbstractAction
{
	AllInformation allInformation;

	public ActionExportPDF(AllInformation allInformation) {
		super("Export as PDF");

		this.allInformation=allInformation;
		putValue(SHORT_DESCRIPTION, "Export as PDF");
	}

	public void actionPerformed (ActionEvent event)
	{
		File file=null;
		
		JFileChooser fileSave=new JFileChooser (allInformation.getDefaultPath());
		fileSave.showSaveDialog(null);
		if (fileSave.getSelectedFile()!=null) {
			file=fileSave.getSelectedFile();
			file=new File(file.getPath().replaceFirst("(\\.pdf$)|(^[^\\.]*$)","$2\\.pdf"));
			if (file.exists())
				file.delete();
			allInformation.setCurrentFile(file);
			allInformation.setDefaultPath(file);
		}
	
		if (file!=null) {	
			int width=800;
			int height=600;
			
			InteractiveSurface interactions=new InteractiveSurface();
			interactions.setSize(width, height);
			SpectraDisplay  virtualDisplay= new SpectraDisplay();
			virtualDisplay.setInteractiveSurface(interactions);
			
			interactions.addEntity(virtualDisplay);
					
			Hashtable helpers=new Hashtable();
			virtualDisplay.init(allInformation.getNemo().getXML(), helpers);
			virtualDisplay.checkInteractiveSurface();
			virtualDisplay.getInteractiveSurface().isLegal = true;
			
			try {
				FileOutputStream fout =  new FileOutputStream(file);
				
				com.lowagie.text.Document document;
				document = new com.lowagie.text.Document(PageSize.A4.rotate(), 50, 50, 50, 50);
				PdfWriter writer = PdfWriter.getInstance(document, fout);
				document.open();
	
				PdfContentByte cb = writer.getDirectContent();
				PdfTemplate tp = cb.createTemplate(width, height);
				Graphics2D g2 = tp.createGraphics(width, height);
	
				virtualDisplay.getInteractiveSurface().addNotify();
				virtualDisplay.setPrimaryColor(MyTransparency.createTransparentColor(virtualDisplay.getPrimaryColor()));
				virtualDisplay.getInteractiveSurface().paintSB(g2);
	
				g2.dispose();
				cb.addTemplate(tp, 10, 0);
				document.close();
				fout.close();
			} catch (Exception e) {
				System.out.println(e.toString());
			}	
		}
	}
}


