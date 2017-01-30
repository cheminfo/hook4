package org.cheminfo.hook.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.cheminfo.hook.util.Preferences;
import org.cheminfo.hook.util.Preferences.Property;

/**
 * <p></p>
 *
 * <pre>
 * Copyright 1997-2012 Actelion Ltd., Inc.
 * Gewerbestrasse 16
 * CH-4123 Allschwil, Switzerland
 *
 * All Rights Reserved.
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.
 * </pre>
 * @author baerr
 * @version 1.0
 */
public class JPreferencesDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private Preferences preferences;
	private Component parent = null;	
	protected Map<Property, Object> changedValues = new HashMap<Property, Object>();
	protected JOptionPane optionPane;
	
	public JPreferencesDialog(Preferences preferences) {
		super();
		setTitle("Preferences");
		this.preferences = preferences;

		init();
}

	public JPreferencesDialog(Frame parent, Preferences preferences) {
		super(parent, "Preferences", true);
		this.preferences = preferences;
		this.parent = parent;
		init();
	}
	
	protected void init() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
		JMenuBar mnb = new JMenuBar();
		setJMenuBar(mnb);
		
		JMenu mnFile = new JMenu("File");
		mnb.add(mnFile);

		JMenuItem mniDefaults = new JMenuItem(new DefaultsAction());
		mnFile.add(mniDefaults);

		mnFile.add(new JSeparator());
		
		JMenuItem mniImport = new JMenuItem(new ImportAction());
		mnFile.add(mniImport);

		JMenuItem mniExport = new JMenuItem(new ExportAction());
		mnFile.add(mniExport);
        
		JButton btnSave = new JButton(new SaveAction());
		JButton btnCancel = new JButton(new CancelAction());

        optionPane = new JOptionPane(new Object[] { buildComponent()}, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, new Object[]{btnSave, btnCancel}, btnSave);
		setContentPane(optionPane);		

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
        setVisible(true);
	}
	
	@Override
	protected JRootPane createRootPane() {		
		JRootPane rootPane = super.createRootPane();

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				dispose();
			}
		};
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		return rootPane;
	}
	
	@SuppressWarnings("unchecked")
	protected JComponent buildComponent()
	{
		HashMap<String, List<Property>> propertyGroups = new LinkedHashMap<String, List<Property>>();
	    for (Property p : preferences) {
	    	List<Property> props = propertyGroups.get(p.getGroup());
	    	if(props == null)
	    	{
	    		props = new ArrayList<Property>();
	    		propertyGroups.put(p.getGroup(), props);
	    	}
	    	props.add(p);
	    }

	    propertyGroups.remove("hidden");
	    
	    if(propertyGroups.size()==1)
	    {
	    	List<Property> props = propertyGroups.values().iterator().next();
	    	
	    	return buildPanel(props);
	    }
	    else
	    {
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			
			for (Entry<String, List<Property>> entry : propertyGroups.entrySet())
				tabbedPane.addTab(entry.getKey(), buildPanel(entry.getValue()));

			return tabbedPane;
	    }
	}

	
	@SuppressWarnings("unchecked")
	private JComponent buildPanel(List<Property> properties)
	{
		GridBagLayout gridBagLayout = new GridBagLayout();

        JPanel componentsPanel = new JPanel(gridBagLayout);

		int row = 0;
		
		Insets insets = new Insets(2,4,2,4);
		
		GridBagConstraints gbcComponentFull = new GridBagConstraints();
		gbcComponentFull.fill = GridBagConstraints.HORIZONTAL;
		gbcComponentFull.anchor = GridBagConstraints.CENTER;
		gbcComponentFull.weightx = 1.0;
		gbcComponentFull.weighty = 0.0;
		gbcComponentFull.gridwidth = 2;
		gbcComponentFull.gridx = 0;
		gbcComponentFull.insets = insets;

		GridBagConstraints gbcLabel = new GridBagConstraints();
		gbcLabel.anchor = GridBagConstraints.EAST;
		gbcLabel.weightx = 0.0;
		gbcLabel.weighty = 0.0;
		gbcLabel.gridx = 0;
		gbcLabel.insets = insets;				
		
		GridBagConstraints gbcComponentRight = new GridBagConstraints();
		gbcComponentRight.fill = GridBagConstraints.HORIZONTAL;
		gbcComponentRight.anchor = GridBagConstraints.WEST;
		gbcComponentRight.weightx = 1.0;
		gbcComponentRight.weighty = 0.0;
		gbcComponentRight.gridx = 1;
		gbcComponentRight.insets = insets;

		GridBagConstraints gbcSpacer = new GridBagConstraints();
		gbcSpacer.weighty = 1.0;
		gbcSpacer.gridwidth = 2;
		gbcSpacer.gridx = 0;

		for ( final Property p : properties) {
        	if(p.getDefaultValue() instanceof Boolean)
        	{
        		final JCheckBox cb = new JCheckBox(p.getLabel());

        		Boolean val = (Boolean)changedValues.get(p);
        		if(val==null)
        			val = (Boolean)preferences.get(p);
        		
        		cb.setSelected(val);
        		
        		cb.addChangeListener(new ChangeListener() {
					//@Override
					public void stateChanged(ChangeEvent e) {
						changedValues.put(p, cb.isSelected());
					}
				});
        		
        		gbcComponentFull.gridy = row++;
        		componentsPanel.add(cb, gbcComponentFull);
        	}
        	else if(p.getDefaultValue() instanceof File)
        	{
            	JLabel label = new JLabel(p.getLabel());

        		File val = (File)changedValues.get(p);
        		if(val==null)
        			val = (File)preferences.get(p);
        		
        		
        		final JTextField txt = new JTextField(val.getAbsolutePath());
        		txt.setEditable(false);
        		final JButton btnFile = new JButton(new SelectFileAction(txt, p, val));
        		
        		final JPanel filePanel = new JPanel(new BorderLayout());
        		filePanel.add(txt, BorderLayout.CENTER);
        		filePanel.add(btnFile, BorderLayout.EAST);
        		
        		gbcLabel.gridy = row;
        		componentsPanel.add(label, gbcLabel);

        		gbcComponentRight.gridy = row++;
        		componentsPanel.add(filePanel, gbcComponentRight);
        	}	
        	else
        	{
            	JLabel label = new JLabel(p.getLabel());

        		Object val = changedValues.get(p);
        		if(val==null)
        			val = preferences.get(p);

        		final JTextField txt;
        		if(val instanceof String)
        		{
	        		txt = new JTextField((String)val);
	        		txt.getDocument().addDocumentListener(new DocumentListener() {
						
						//@Override
						public void removeUpdate(DocumentEvent e) {
							changedValues.put(p, txt.getText());        				
						}
						
						//@Override
						public void insertUpdate(DocumentEvent e) {
							changedValues.put(p, txt.getText());        				
						}
						
						//@Override
						public void changedUpdate(DocumentEvent e) {
							changedValues.put(p, txt.getText());        				
						}
					});
        		}
        		else
        		{
	        		txt = new JFormattedTextField(val);
	        		txt.getDocument().addDocumentListener(new DocumentListener() {
						
						//@Override
						public void removeUpdate(DocumentEvent e) {
							changedValues.put(p, ((JFormattedTextField)txt).getValue());        				
						}
						
						//@Override
						public void insertUpdate(DocumentEvent e) {
							changedValues.put(p, ((JFormattedTextField)txt).getValue());        				
						}
						
						//@Override
						public void changedUpdate(DocumentEvent e) {
							changedValues.put(p, ((JFormattedTextField)txt).getValue());        				
						}
					});
        		}
        		gbcLabel.gridy = row;
        		componentsPanel.add(label, gbcLabel);

        		gbcComponentRight.gridy = row++;
        		componentsPanel.add(txt, gbcComponentRight);
        	}
		}
        
		gbcSpacer.gridy = row++;
		componentsPanel.add(new JPanel(), gbcSpacer);
		
        return componentsPanel;
	}

	protected void refresh()
	{
		optionPane.setMessage(buildComponent());
	}
	
//	private JPanel buildButtons()
//	{
//		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 32, 4));		
//		buttonPanel.setBorder(new EmptyBorder(16, 4, 4, 4));
//		
//		buttonPanel.add(new JButton(new CancelAction()));
//		buttonPanel.add(new JButton(new SaveAction()));
//
//		return buttonPanel;
//	}
	
	protected void saveToFile(File file)
	{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try{
		    // Serialize to a file
		    fos = new FileOutputStream(file) ;
		    out = new ObjectOutputStream(fos) ;
		    out.writeObject(preferences);				   
		}
		catch(IOException e)
		{
			throw(new RuntimeException("Could not serialize preferences!", e));
		}
		finally
		{
			try{out.close();}catch(Throwable t){}
			try{fos.close();}catch(Throwable t){}
		}
	}
	
	protected Preferences loadFromFile(File file)
	{
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try{
		    // Deserialize from a file
			fis = new FileInputStream(file);
			in = new ObjectInputStream(fis);
		    return (Preferences)in.readObject();
		}
		catch(Exception e)
		{
			throw(new RuntimeException("Could not deserialize preferences!", e));
		}
		finally
		{
			try{in.close();}catch(Throwable t){}
			try{fis.close();}catch(Throwable t){}
		}
	}

	private class SaveAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public SaveAction()
		{
			super("Save");
		}
		
		@SuppressWarnings("unchecked")
		//@Override
		public void actionPerformed(ActionEvent e) {
			for (Entry<Property, Object> entry : changedValues.entrySet()) {
				preferences.set(entry.getKey(), entry.getValue());
			}
			preferences.save();
			dispose();
		}
	}

	private class CancelAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public CancelAction()
		{
			super("Cancel");
		}
		
		//@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}

	private class ExportAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public ExportAction()
		{
			super("Export Preferences");
		}
		
		//@Override
		public void actionPerformed(ActionEvent e) {
			final JFileChooser fileChooser = new PreferencesFileChooser();
			if(fileChooser.showSaveDialog(JPreferencesDialog.this) == JFileChooser.APPROVE_OPTION)
			{
				saveToFile(fileChooser.getSelectedFile());
			}
		}
	}

	private class ImportAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		
		public ImportAction()
		{
			super("Import Preferences");
		}
		
		@SuppressWarnings("unchecked")
		//@Override
		public void actionPerformed(ActionEvent e) {
			final JFileChooser fileChooser = new PreferencesFileChooser();
			
			if(fileChooser.showOpenDialog(JPreferencesDialog.this) == JFileChooser.APPROVE_OPTION)
			{
				Preferences loadedPreferences = loadFromFile(fileChooser.getSelectedFile());
				
				changedValues.clear();
				
				for (Property property : loadedPreferences) {
					changedValues.put(property, loadedPreferences.get(property));
				}
				
				refresh();
			}
		}
	}

	private class DefaultsAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		
		public DefaultsAction()
		{
			super("Load Default Preferences");
		}
		
		@SuppressWarnings("unchecked")
		//@Override
		public void actionPerformed(ActionEvent e) {
			changedValues.clear();
			
			for (Property property : preferences) {
				changedValues.put(property, property.getDefaultValue());
			}
			
			refresh();
		}
	}

	private class PreferencesFileChooser extends JFileChooser
	{
		private static final long serialVersionUID = 1L;

		public PreferencesFileChooser() {
			super();
			
			setFileSelectionMode(JFileChooser.FILES_ONLY);
			setMultiSelectionEnabled(false);
			setAcceptAllFileFilterUsed(false);
			setFileFilter(new FileFilter() {
				@Override
				public String getDescription() {
					return "Preferences File (.prf)";
				}
				
				@Override
				public boolean accept(File f) {
					String fileName = f.getName(); 
					return fileName.substring(fileName.lastIndexOf('.')+1).equalsIgnoreCase("prf") || f.isDirectory();
				}
			});
		}
		
		@Override
		public File getSelectedFile() {
			File f = super.getSelectedFile();
			
			if(f!=null && !f.getName().toLowerCase().endsWith(".prf"))
				return new File(f.getAbsolutePath()+".prf");
			else
				return f;
		}
	}

	private class SelectFileAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		private JTextField txtFile;
		private Property<File> p; 
		private File file;
		
		public SelectFileAction(JTextField txtFile, Property<File> p, File file) {
			super("...");
			this.txtFile = txtFile;
			this.p = p;
			this.file = file;
		}

		//@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser(file);
			fc.setAcceptAllFileFilterUsed(true);
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fc.setMultiSelectionEnabled(false);
			
			if(fc.showOpenDialog(JPreferencesDialog.this)==JFileChooser.APPROVE_OPTION)
			{				
				File newFile = fc.getSelectedFile();
				changedValues.put(p, newFile);
				txtFile.setText(newFile.getAbsolutePath());
			}
		}
		
	}
	
}
