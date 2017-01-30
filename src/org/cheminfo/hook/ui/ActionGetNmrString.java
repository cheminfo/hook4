package org.cheminfo.hook.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.cheminfo.hook.nemo.HTMLPeakTableFormater;
import org.cheminfo.hook.nemo.SpectraDisplay;

import com.actelion.research.nemo.jcamp.Label;
import com.actelion.research.nemo.jcamp.Ldr;



class ActionGetNmrString extends AbstractAction {

	private JEditorPane taskOutput = new JEditorPane("text/html","");
	private JRadioButton rdoAsc;
	
	protected NemoInstance nemoInstance;
	
	public ActionGetNmrString(NemoInstance nemoInstance) {
		super("NMR description");

		this.nemoInstance = nemoInstance;

		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
		putValue(SHORT_DESCRIPTION, "Get NMR spectrum description.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
	}
	
	public void actionPerformed (ActionEvent e) {
		Window parent = SwingUtilities.windowForComponent(nemoInstance.getNemo());
		JDialog dialog=new JDialog(parent, "NMR spectrum description");
		dialog.setPreferredSize(new Dimension(600,300));
		dialog.setModal(true);
		taskOutput.setMargin(new Insets(5,5,5,5));
		dialog.getContentPane().add(new JScrollPane(taskOutput), BorderLayout.CENTER);		

		JPanel options = new JPanel(new GridLayout(1, 2));
		rdoAsc = new JRadioButton("ascending");
		rdoAsc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setDescription();
			}
		});
		rdoAsc.setSelected(true);
		
		JRadioButton rdoDesc = new JRadioButton("descending"); 
		rdoDesc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setDescription();
			}
		});
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(rdoAsc);
		buttonGroup.add(rdoDesc);
		
		options.add(rdoAsc);
		options.add(rdoDesc);
		
		dialog.getContentPane().add(options, BorderLayout.NORTH);
		
		setDescription();
		
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		
		dialog.setVisible(true);
	}
	
	private void setDescription() {
		SpectraDisplay spectraDisplay = (SpectraDisplay) nemoInstance.getNemo().getMainDisplay();		
		String html = HTMLPeakTableFormater.getPeakHtml(spectraDisplay.getFirstSpectra(), "#0.00", rdoAsc.isSelected());
		
		Ldr ldr = nemoInstance.getCurrentJdx().get(Label.STD_OBSERVE_FREQUENCY);
				
		double realFreq = Double.parseDouble(ldr.getValue().toString().trim());		
		long freq = Math.round(realFreq/5)*5; 
		
		ldr = nemoInstance.getCurrentJdx().get(Label.BRUKER_SOLVENT);
		if(ldr==null)
			ldr = nemoInstance.getCurrentJdx().get(Label.VARIAN_SOLVENT);
		
		html = html.replace("<i>&delta;</i>", "(" + freq + " MHz, " + ldr.getValue() + ") <i>&delta;</i>");
		
		taskOutput.setText(html);
	}
}
