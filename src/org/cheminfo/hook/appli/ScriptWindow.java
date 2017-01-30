package org.cheminfo.hook.appli;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;

import org.cheminfo.function.scripting.ScriptingInstance;
import org.cheminfo.hook.nemo.Nemo;
import org.cheminfo.hook.nemo.SpectraData;
import org.cheminfo.hook.nemo.SpectraDisplay;
import org.json.JSONObject;

public class ScriptWindow extends JFrame {

	/**
	 * 
	 * 
	 */
	private static final long serialVersionUID = -7216814474492966585L;
	private JTextArea inputArea = null;
	private JTextArea outputArea = null;
	private ScriptingInstance scriptingInstance = null;
	private AllInformation allInformation = null;
	private JSONObject jsonResult =null;
	
	public ScriptWindow(AllInformation allInformation) {
		super("Script console");
		jsonResult=new JSONObject();
		this.allInformation = allInformation;
		this.setSize(500, 300);
		// set window layout
		this.setLayout(new BorderLayout());
		// text editor area
		JPanel textsPanel = new JPanel();
		textsPanel.setLayout(new GridLayout(2, 1));
		this.add(textsPanel, BorderLayout.CENTER);

		// input area
		this.inputArea = new JTextArea();
		this.inputArea.setVisible(true);
		JScrollBar scrollBar = new JScrollBar();
		scrollBar.setOrientation(JScrollBar.VERTICAL);
		this.inputArea.add(scrollBar);
		textsPanel.add(this.inputArea);
		// output area
		this.outputArea = new JTextArea();
		this.outputArea.setVisible(true);
		this.outputArea.setEditable(false);
		this.outputArea.setBackground(Color.blue);
		this.outputArea.setForeground(Color.white);
		textsPanel.add(this.outputArea);
		// controls
		JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		final ScriptWindow window = this;
		JButton clearButton = new JButton("clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				window.clearInput();
			}
		});
		controlsPanel.add(clearButton);
		JButton executeButton = new JButton("execute");
		executeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				window.executeScript();
			}
		});
		controlsPanel.add(executeButton);
		JButton closeButton = new JButton("close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				window.dispose();
			}
		});
		controlsPanel.add(closeButton);
		this.add(controlsPanel, BorderLayout.SOUTH);
		
		// set up scripting
		//this.scriptingInstance = new ScriptingInstance();
		this.scriptingInstance=allInformation.getNemo().getInterpreter();
		// add outputArea as output instance
		this.scriptingInstance.addObjectToScope("out", this);
		// add nemo to the context
		//this.scriptingInstance.addObjectToScope("nemo", this.allInformation.getNemo());
		//
		this.importSpectraObjects();
		//this.setScriptingWindowAPI();
	}

	public void clearInput() {
		this.inputArea.setText("");
		this.outputArea.setText("");
	}

	public void executeScript() {
		String script = this.inputArea.getText();
		jsonResult=this.scriptingInstance.runScript(script);
		this.inputArea.setText("");
		refresh();
	}

	public void refresh() {
		Nemo nemo = this.allInformation.getNemo();
		SpectraDisplay display = (SpectraDisplay) nemo.getMainDisplay();
		display.refreshSensitiveArea();
		display.checkSizeAndPosition();
		//display.getInteractiveSurface().repaint();
		display.checkAndRepaint();
		this.allInformation.getNemo().getInteractions().setActiveEntity(display.getLastSpectra());
	}
	
	public void println(String line) {
		this.outputArea.setText(this.outputArea.getText() + line + "\n");
		this.outputArea.updateUI();
	}
	
	private void importSpectraObjects() {
		//TODO It is not very useful. We need to think how to improve it.
		Nemo nemo = this.allInformation.getNemo();
		SpectraDisplay spectraDisplay = (SpectraDisplay) nemo.getMainDisplay();
		if (spectraDisplay.getFirstSpectra() != null) {
			SpectraData spectraData = spectraDisplay.getFirstSpectra()
					.getSpectraData();
			this.scriptingInstance.addObjectToScope("spectraData", spectraData);
			this.println("added mainSpectrum as spectraData");
		}
		if (spectraDisplay.getHorRefSpectrum() != null) {
			SpectraData spectraData = spectraDisplay.getHorRefSpectrum()
					.getSpectraData();
			this.scriptingInstance.addObjectToScope("horSpectraData", spectraData);
			this.println("added horizontal spectrum as horSpectraData");
		}
		if (spectraDisplay.getVerRefSpectrum() != null) {
			SpectraData spectraData = spectraDisplay.getHorRefSpectrum()
					.getSpectraData();
			this.scriptingInstance.addObjectToScope("verSpectraData", spectraData);
			this.println("added vertical spectrum as verSpectraData");
			this.refresh();
		}
		
		
	}
	
}
