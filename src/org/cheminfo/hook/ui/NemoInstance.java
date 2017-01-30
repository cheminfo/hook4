package org.cheminfo.hook.ui;

import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.nemo.jcamp.Jcamp;
import com.actelion.research.nemo.jcamp.Label;
import com.actelion.research.nemo.jcamp.Ldr;
import com.actelion.research.nemo.jcamp.LdrFactory;
import org.cheminfo.hook.framework.BasicDisplay;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.HorizontalScale;
import org.cheminfo.hook.nemo.Nemo;
import org.cheminfo.hook.nemo.SpectraDisplay;
import org.cheminfo.hook.nemo.VerticalScale;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;

public class NemoInstance<T> implements ModelChangedListener {
	protected Nemo nemo;
	protected LoadSaveHandler<T> loadSaveHandler;
	
	protected boolean dirty = false;
	
	protected Jcamp currentJdx;
	protected Jcamp getCurrentJdx() {
		return currentJdx;
	}

	protected T ressource;
	
	protected List<WeakReference<PropertyChangeListener>> propertyChangeListeners = new ArrayList<WeakReference<PropertyChangeListener>>();
	
	@SuppressWarnings("unchecked")
	public static <File> NemoInstance<File> createNemoInstance()
	{
		return new NemoInstance<File>((LoadSaveHandler<File>) new FileLoadSaveHandler());
	}

	public static <T> NemoInstance<T> createNemoInstance(LoadSaveHandler<T> loadSaveHandler)
	{
		return new NemoInstance<T>(loadSaveHandler);
	}
	
	private NemoInstance(LoadSaveHandler<T> loadSaveHandler) {
		this.loadSaveHandler = loadSaveHandler;
		
		nemo = new Nemo(false);
		
		nemo.startup();
		nemo.frameWork();
		
		InteractiveSurface i = nemo.getMainDisplay().getInteractiveSurface();
		
		i.isLegal=true;
		i.addModelChangedListener(this);
	}

	protected Nemo getNemo() {
		return nemo;
	}
	
	/**
	 * Removes all loaded jdx's and clears the workingspace.
	 */
	public synchronized void clear() 
	{
		currentJdx = null;
		setRessource(null);
		setDirty(false);
		nemo.clearAll();
		nemo.getInteractions().resetUndoStack();
	}
	
	protected boolean hasMultipleJdxLoaded()
	{
		SpectraDisplay disp = (SpectraDisplay)nemo.getMainDisplay();
	
		return disp.getAllSpectra().size()>1; 
	}
	
	protected boolean hasOpenedJdx()
	{
		return getRessource()!=null && currentJdx!=null;
	}	
	
	protected boolean hasEntities()
	{
		Vector<BasicEntity> entities = nemo.getMainDisplay().getEntities();
		
		for (BasicEntity basicEntity : entities) {
			if(basicEntity instanceof VerticalScale || basicEntity instanceof HorizontalScale)
				continue;
			
			return true;
		}

		return false;
	}
	
	protected String getViewXml() {
		return getViewXml(false);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected String getViewXml(boolean embedJcamp) {
		String xml;
		Hashtable xmlProperties = new Hashtable();
		xmlProperties.put("includeURL", new Boolean(false));
		if(embedJcamp)
			xmlProperties.put("embedJCamp", new Boolean(true));
		
		BasicDisplay display = nemo.getMainDisplay();

		if(display!=null)
			xml = display.getXmlTag(xmlProperties);
		else
			xml = null;
		
		return xml;
	}
	
	@SuppressWarnings("unchecked")
	public void openJcamp(final T ressource) throws IOException {
		clear();
		
		Jcamp jdx = loadSaveHandler.load(ressource, this);
		if(jdx==null)
			throw(new IOException("LoadSaveHandler.load() implementation must return a valid Jcamp!"));
		
		currentJdx = jdx;
		nemo.addJcamp(currentJdx.getContents(), ressource.toString(), true);
		
		Ldr<String> nemoview = (Ldr<String>) currentJdx.get(Label.ACT_NEMOVIEW);
		if(nemoview != null)
			nemo.setXMLView(nemoview.getValue());
		
		nemo.getInteractions().resetUndoStack();
		nemo.getInteractions().takeUndoSnapshot();
		
		SwingUtilities.invokeLater(new Runnable() {			
			public void run() {				
				SwingUtilities.invokeLater(new Runnable() {			
					public void run() {			
						setRessource(ressource);
						setDirty(false);
					}
				});

			}
		});
	}
	
	/**
	 * Adds a jdx from a ressource to the currently loaded jdx
	 * @param ressource ressource to load
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void addJcamp(final T ressource) throws IOException {
		Jcamp jdx = loadSaveHandler.load(ressource, this);
		if(jdx==null)
			throw(new IOException("LoadSaveHandler.load() implementation must return a valid Jcamp!"));

		nemo.addJcamp(jdx.getContents(), ressource.toString(), true);
		nemo.getInteractions().takeUndoSnapshot();
	}
	
//	/**
//	 * Adds a jdx from a ressource vertically to the currently loaded jdx.
//	 * @param ressource ressource to load
//	 * @throws IOException
//	 */
//	@SuppressWarnings("unchecked")
//	public void addJcampVertical(final T ressource) throws IOException {
//		nemo.addJcampVertical(loadSaveHandler.load(ressource, this).getContents());		
//	}

	/**
	 * Reverts the currently loaded jdx ignoring all unwritten changes.  
	 * @throws IOException
	 */
	public void reloadJcamp() throws IOException {
		T ressource = getRessource();
		if(ressource != null)
			openJcamp(ressource);
	}
	
	/**
	 * Removes all view related information from the currently loaded jdx and saves it.
	 * @throws IOException
	 */
	public void revertBareJdx() throws IOException {
		nemo.clearAll();
		nemo.addJcamp(currentJdx.getContents());
		save(false);
		reloadJcamp();
	}
	
	/**
	 * Saves the currently loaded jdx.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void save() throws IOException
	{
		save(true);
	}
	
	@SuppressWarnings("unchecked")
	protected void save(boolean includeViewData) throws IOException
	{
		if(hasMultipleJdxLoaded())
			throw(new RuntimeException("Saving is only allowed for one loaded JCAMP!"));
		
		if(!hasOpenedJdx())
			throw(new RuntimeException("No JCAMP is loaded through open operation!"));

		String viewXml = getViewXml();
		
		//remove old viewdata
		Ldr<String> ldr = (Ldr<String>) currentJdx.get(Label.ACT_NEMOVIEW);
		currentJdx.remove(ldr);

		if(includeViewData)
		{
			//create new viewdata
			ldr = LdrFactory.createFromLabelValue(Label.ACT_NEMOVIEW, viewXml);
			
			//insert after OBSERVE_NUCLEUS
			Ldr<?> after = currentJdx.get(Label.STD_OBSERVE_NUCLEUS);
			currentJdx.add(after, ldr);
		}
		
		setDirty(false);
		T newRessource = loadSaveHandler.save(getRessource(), currentJdx, this);
		if(newRessource==null)
			throw(new IOException("LoadSaveHandler.save() implementation must return the current ressource!"));
		setRessource(newRessource);
	}

	/**
	 * Checks if the currently loaded jdx has unwritten changes.
	 * @return true if has unwritten changes
	 */
	public boolean isDirty() {
		return ressource != null && dirty;
	}

	public T getRessource() {
		return ressource;
	}

	protected void setRessource(T ressource) {
		T old = this.ressource;
		this.ressource = ressource;
		firePropertyChanged(new PropertyChangeEvent(this, "ressource", old, ressource));
	}

	protected void setDirty(boolean dirty) {
		boolean old = this.dirty;
		this.dirty = dirty;
		firePropertyChanged(new PropertyChangeEvent(this, "dirty", old, dirty));
	}
	
	protected String getTitle()
	{
		StringBuilder sb = new StringBuilder();
		if(getRessource()!=null)
		{
			sb.append(ressource.toString());
			if(isDirty())
				sb.append(" [modified]");
			sb.append(" - ");
		}

		sb.append(Version.APPNAME + " " + Version.VERSION);
		
		return sb.toString();
	}
	
	public RenderedImage getSurfaceImage(int width, int height, double scaleFactor)
	{
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = img.createGraphics();

		drawSurface(g2, width, height, scaleFactor);
		
		return img;
	}
	
	protected void drawSurface(Graphics2D graphics, int width, int height, double scaleFactor) {
		Paper paper=new Paper();
		paper.setSize(width,height);
		paper.setImageableArea(0, 0, width, height);
		PageFormat pf=new PageFormat();
		pf.setPaper(paper);
		drawSurface(graphics, pf, scaleFactor);
	}
	
	protected void drawSurface(Graphics2D graphics, PageFormat pageFormat, double scaleFactor) {
		InteractiveSurface interactions=new InteractiveSurface();

		int width = (int)pageFormat.getImageableWidth();
		int height = (int)pageFormat.getImageableHeight();
		            			
		interactions.setSize((int)(width * scaleFactor), (int)(height * scaleFactor));

		interactions.isLegal = true;
		SpectraDisplay virtualDisplay= new SpectraDisplay();
		virtualDisplay.setInteractiveSurface(interactions);
		interactions.addEntity(virtualDisplay);

		virtualDisplay.setInteractiveSurface(interactions);

		virtualDisplay.init();
		virtualDisplay.setSize((int)(width * scaleFactor), (int)(height * scaleFactor));

		Hashtable helpers=new Hashtable();
//		if(this.hasOpenedJdx())
//		{
//    			SpectraData originalData = this.getNemo().getSpectraData();
//    			helpers.put("originalSpectraData", originalData);
//		}
//		helpers.put("encodedJCamp", true);
		virtualDisplay.init(this.getViewXml(true), (int)(width * scaleFactor), (int)(height * scaleFactor), helpers);

		virtualDisplay.checkInteractiveSurface();
		virtualDisplay.checkSizeAndPosition();
		
		virtualDisplay.restoreLinks();
		interactions.checkButtonsStatus();

//		interactions.clearActiveEntities();
		
		interactions.getGlobalTransform().scale(1/scaleFactor, 1/scaleFactor);
		
		interactions.print(graphics, pageFormat, 0);
	}

	
//	protected void drawSurface(Graphics2D graphics, PageFormat pageFormat) {
//		InteractiveSurface interactions=new InteractiveSurface();
//
//		int width = (int)pageFormat.getImageableWidth();
//		int height = (int)pageFormat.getImageableHeight();
//		            			
//		interactions.setSize(width, height);
//
//		interactions.isLegal = true;
//		SpectraDisplay virtualDisplay= new SpectraDisplay();
//		virtualDisplay.setInteractiveSurface(interactions);
//		interactions.addEntity(virtualDisplay);
//
//		virtualDisplay.setInteractiveSurface(interactions);
//
//		virtualDisplay.init();
//		virtualDisplay.setSize(width, height);
//
//		Hashtable helpers=new Hashtable();
//		if(this.hasOpenedJdx())
//		{
//    			SpectraData originalData = this.getNemo().getSpectraData();
//    			helpers.put("originalSpectraData", originalData);
//		}
//		virtualDisplay.init(this.getViewXml(), width, height, helpers);
//
//		virtualDisplay.checkInteractiveSurface();
//		virtualDisplay.checkSizeAndPosition();
//		
//		virtualDisplay.restoreLinks();
//		interactions.checkButtonsStatus();
//
////		interactions.clearActiveEntities();
//
//		interactions.print(graphics, pageFormat, 0);
//	}
	
	protected void firePropertyChanged(final PropertyChangeEvent event) {
		
		removeNullReferences();
		
		for (WeakReference<PropertyChangeListener> listener : propertyChangeListeners) {
			PropertyChangeListener l = listener.get();
			if(l!=null)
				l.propertyChange(event);
		}
	}
	
	private void removeNullReferences() {
		synchronized (propertyChangeListeners) {
			List<WeakReference<PropertyChangeListener>> toRemove = new ArrayList<WeakReference<PropertyChangeListener>>();

			for (WeakReference<PropertyChangeListener> listener : propertyChangeListeners) {
				PropertyChangeListener l = listener.get();
				if(l==null)
					toRemove.add(listener);
			}
			
			propertyChangeListeners.removeAll(toRemove);
		}
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		synchronized (propertyChangeListeners) {
			WeakReference<PropertyChangeListener> l = new WeakReference<PropertyChangeListener>(listener);
			propertyChangeListeners.add(l);
		}
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		synchronized (propertyChangeListeners) {
			WeakReference<PropertyChangeListener> toRemove = null;
			for (WeakReference<PropertyChangeListener> l : Collections.unmodifiableCollection(propertyChangeListeners)) {
				if(listener.equals(l.get()))
				{
					toRemove = l;
					break;
				}
			}
		
			if(toRemove != null)
				propertyChangeListeners.remove(toRemove);
		}
	}
	
	public void dispose() {
		propertyChangeListeners.clear();
		nemo.destroy();
//		nemo = null;		
	}

	public void modelChanged(InteractiveSurface interactiveSurface) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setDirty(true);
			}
		});
		System.out.println("NemoInstance.modelChanged()");
	}
	
	public void addMoleculeByIdCode(String idcode, double x, double y, double width, double height) {
		ActMoleculeDisplay molDisplay = new ActMoleculeDisplay();

		molDisplay.setLocation(x, y);
		
		molDisplay.setMovementType(BasicEntity.GLOBAL);
		// molDisplay.setErasable(true);
		molDisplay.setEntityName("molDisplay");
		getNemo().getMainDisplay().addEntity(molDisplay,0);

		molDisplay.init(width, height);
		molDisplay.addMoleculeByIDCode(idcode);

		nemo.getInteractions().takeUndoSnapshot();
	}
	
	public void addMoleculeByMolfile(String molfile, double x, double y, double width, double height) {
		ActMoleculeDisplay molDisplay = new ActMoleculeDisplay();

		molDisplay.setLocation(x, y);
		
		molDisplay.setMovementType(BasicEntity.GLOBAL);
		// molDisplay.setErasable(true);
		molDisplay.setEntityName("molDisplay");
		getNemo().getMainDisplay().addEntity(molDisplay,0);

		molDisplay.init(width, height);
		molDisplay.addMolfile(molfile, false);
		
		nemo.getInteractions().takeUndoSnapshot();
	}
	
	public void addMolecule(Molecule mol, double x, double y, double width, double height) {
		StereoMolecule stereoMolecule = new StereoMolecule(mol);

		ActMoleculeDisplay molDisplay = new ActMoleculeDisplay();

		molDisplay.setLocation(x, y);

		molDisplay.setMovementType(BasicEntity.GLOBAL);
		// molDisplay.setErasable(true);
		molDisplay.setEntityName("molDisplay");
		getNemo().getMainDisplay().addEntity(molDisplay,0);

		molDisplay.init(width, height);
		molDisplay.addMolecule(stereoMolecule);

		nemo.getInteractions().takeUndoSnapshot();
	}
}
