package org.cheminfo.hook.framework;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JPanel;

import org.cheminfo.hook.ui.ModelChangedListener;
import org.cheminfo.hook.util.XMLCoDec;

public class InteractiveSurface extends JPanel implements MouseListener,
		MouseMotionListener, MouseWheelListener, ActionListener, KeyListener, Printable, Pageable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3160661097602419857L;
	public final static int INTEGRITY_CHECK = 0x000001; // a link type ???

	public String baseDirectory;
	private Vector<DefaultActionButton> buttons = null;
	private UserDialog userDialog = null;

	private Vector<BasicEntity> childEntities = null;
	private Vector<BasicEntity> activeEntities = null;

	private BasicDisplay activeDisplay = null;
	private BasicEntity currentOverEntity = null;

	private DefaultActionButton currentAction, previousAction;

	private DefaultActionButton defaultAction = null;

	private Point2D.Double contact, release;

	private Graphics2D g2;
	private BufferedImage bufferImage = null;

	private boolean isColorPrimary = true;

	private boolean hasUndo;
	private Vector undoStack;
	private int undoStackIndex;

	private Vector<BasicEntity> linkListA;
	private Vector<BasicEntity> linkListB;
	private Vector<Integer> linkType;
	private int lastUsedID;

	private boolean rubberband = false;

	private AffineTransform transform;
	private BasicStroke narrowStroke, mediumStroke, broadStroke;

	public boolean isLegal = false; // used for security of applet

	private boolean DEBUG = false;

	private boolean hiDefinition;

	private URL appletBase;

	private HookResourceManager resourceManager;

	private int fontSize;
	private boolean useBigFonts;

	private ProcessingAnimationPanel processingAnimation;
	
	private List<ModelChangedListener> modelChangedListeners = new ArrayList<ModelChangedListener>(); 
	
	public InteractiveSurface() {
		// this.setBackground(new Color(255,255,255,0));
		// this.setBackground(Color.white);
		this.setFocusable(true);
		this.setLayout(null);
		childEntities = new Vector<BasicEntity>();
		buttons = new Vector();
		activeEntities = new Vector<BasicEntity>();

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		this.addKeyListener(this);

		this.requestFocus();

		this.contact = new Point2D.Double();
		this.release = new Point2D.Double();

		this.hasUndo = true;
		this.undoStack = new Vector();

		this.linkListA = new Vector();
		this.linkListB = new Vector();
		this.linkType = new Vector();

		this.transform = new AffineTransform();

		lastUsedID = 0;

		narrowStroke = new BasicStroke(1f);
		// broadStroke = new BasicStroke(5, BasicStroke.CAP_ROUND,
		// BasicStroke.JOIN_BEVEL);
		broadStroke = new BasicStroke(5);
		mediumStroke = new BasicStroke(2);
		/*
		 * try { UIManager.setLookAndFeel( //
		 * "com.sun.java.swing.plaf.motif.MotifLookAndFeel"
		 * "com.sun.java.swing.plaf.windows.WindowsLookAndFeel" ); } catch
		 * (Exception e) { System.out.println("Exception: "+e.toString());};
		 */

		this.hiDefinition = false;
		this.useBigFonts=false;
		
		this.resourceManager = new HookResourceManager();
		
		this.processingAnimation=null;
	}

	public void doLayout()
	{
		super.doLayout();
		this.fontSize=(int)(this.getWidth()/60d);
	}

	public void setAppletBase(URL newAppletBase) {
		this.appletBase = newAppletBase;
	}

	public URL getAppletBase() {
		return this.appletBase;
	}

	public HookResourceManager getResourceManager() {
		return this.resourceManager;
	}

	/*
	 * Returns the Stroke commonly used for drawing
	 */
	public BasicStroke getNarrowStroke() {
		return this.narrowStroke;
	}
	
	public int getFontSize()
	{
		return this.fontSize;
	}

	public boolean useBigFonts()
	{
		return this.useBigFonts;
	}
	
	public void useBigFonts(boolean bigFonts)
	{
		this.useBigFonts=bigFonts;
	}
	
	/*
	 * Returns the Stroke commonly used for drawing selected entities
	 */
	public BasicStroke getBroadStroke() {
		return this.broadStroke;
	}

	/**
	 * Returns whether a color change will affect the primary color of the
	 * selected entities (as opposed to the secondary color).
	 * 
	 * @return
	 */
	public boolean isColorPrimary() {
		return this.isColorPrimary;
	}

	/**
	 * Set the
	 * 
	 * @param isPrimary
	 */
	public void isColorPrimary(boolean isPrimary) {
		this.isColorPrimary = isPrimary;
	}

	/**
	 * returns the user dialog.
	 * 
	 * @return
	 */
	public UserDialog getUserDialog() {
		return this.userDialog;
	}

	/**
	 * set the pointer to the user dialog. To be used in the initialization of
	 * the program.
	 * 
	 * @param newUserDialog
	 */
	public void setUserDialog(UserDialog newUserDialog) {
		this.userDialog = newUserDialog;
	}
	
	/**
	 * Displays a "processing" animation in the middle of the surface when set to true. The animation is removed
	 * when set to false.
	 * @param processing
	 */
	public void isProcessing(boolean processing)
	{
//		if ( !System.getProperty("java.awt.headless").equals("true") && processing)
		if ( processing)
		{
			if (this.processingAnimation == null)
				this.processingAnimation=new ProcessingAnimationPanel();
			
			if ( !this.isAncestorOf(this.processingAnimation) )
			{
				this.add(this.processingAnimation);
				this.repaint();
			}
		}
		else
		{
			if (this.processingAnimation != null && this.isAncestorOf(this.processingAnimation))
			{
				this.remove(this.processingAnimation);
				this.repaint();
			}
		}
	}

	// ----------------- BUTTON / ACTION METHODS---------------------

	/**
	 * Adds a new button to the list of action buttons.
	 */
	public void addButton(DefaultActionButton newButton) {
		// System.out.println("Add button: "+newButton.getClass().getName());
		buttons.addElement(newButton);
	}

	/**
	 * Induces all the Action Buttons to check their status
	 * (active/inactive/down/up)
	 * 
	 */
	public void checkButtonsStatus() {
		for (DefaultActionButton button : this.buttons) {
			button.checkButtonStatus();
		}
	}

	/**
	 * TODO: Co9uld be optimized is making a hash
	 */
	public DefaultActionButton getButtonByClassName(String className) {
		for (int buttonID = 0; buttonID < this.buttons.size(); buttonID++) {
			if (buttons.elementAt(buttonID).getClass().getCanonicalName().compareTo(className) == 0) {
				return (DefaultActionButton) buttons.elementAt(buttonID);
			}
		}
		return null;
	}

	public int getButtons() {
		return this.buttons.size();
	}
	
	public DefaultActionButton getButton(int iButton) {
		return (DefaultActionButton)this.buttons.get(iButton);
	}
	
	/**
	 * Set the current Action to the specified one. Stores the previous action.
	 * 
	 * @param newCurrentAction
	 */
	public void setCurrentAction(DefaultActionButton newCurrentAction) {
		this.previousAction = this.currentAction;
		this.currentAction = newCurrentAction;
	}

	/**
	 * Results in all the buttons part of the specified group to reset in the UP
	 * position.
	 * 
	 * @param buttonGroup
	 */
	void releaseButtonGroup(int buttonGroup) {
		for (int i = 0; i < this.buttons.size(); i++) {
			if (((DefaultActionButton) buttons.elementAt(i)).getGroupNb() == buttonGroup) {
				if (((DefaultActionButton) buttons.elementAt(i)).getStatus() != DefaultActionButton.INACTIVE)
					((DefaultActionButton) buttons.elementAt(i)).setStatus(DefaultActionButton.UP);
			}
		}
	}

	/**
	 * Returns the currentAction
	 * 
	 * @return
	 */
	public DefaultActionButton getCurrentAction() {
		return this.currentAction;
	}

	/**
	 * returns the previous action. Typically used when the current action is a
	 * immediate action and we wish to return to the previous one right after.
	 * 
	 * @return
	 */
	public DefaultActionButton getPreviousAction() {
		return this.previousAction;
	}

	// --------------------END OF BUTTON / ACTION

	/**
	 * Clears the active entities vector and visually deselects all entities.
	 */
	public void clearActiveEntities() {
		for (int ent = 0; ent < this.activeEntities.size(); ent++) {
			if (this.activeEntities.elementAt(ent) != null)
				((BasicEntity) this.activeEntities.elementAt(ent)).setSelected(false);
		}
		this.activeEntities.clear();
	}

	/**
	 * Sets this BasicEntity as the sole Active Entity. All other selected
	 * entities are deselected.
	 * 
	 * @param entity
	 */
	public void setActiveEntity(BasicEntity entity) {
		this.clearActiveEntities();
		this.addActiveEntity(entity);
	}

	/**
	 * Adds a BasicEntity to the set of active entites and sets it as selected.
	 * Typically used in multiple selections. Also sets the active display.
	 * 
	 * @param entity
	 */
	public void addActiveEntity(BasicEntity entity) {
		if (entity != null) {
			if (this.activeEntities.size() == 0)
				this.activeDisplay = this.findParentDisplay(entity);

			this.activeEntities.addElement(entity);
			entity.setSelected(true);
		}
	}

	/**
	 * Returns a Vector containing all the active Entities
	 * 
	 * @return
	 */
	public Vector<BasicEntity> getActiveEntities() {
		return this.activeEntities;
	}

	/**
	 * Returns the first BasicEntity in the activeEntities Vector. Returns null
	 * if no active entities are found.
	 * 
	 * @return
	 */
	public BasicEntity getActiveEntity() {
		if (this.activeEntities.size() == 0)
			return null;

		return (BasicEntity) this.activeEntities.elementAt(0);
	}

	/**
	 * Returns the active Display. If the active entity is not a Display, this
	 * returns the parent display of tha entity.
	 * 
	 * @return
	 */
	public BasicDisplay getActiveDisplay() {
		return this.activeDisplay;
	}

	/**
	 * Returns the lowest level Display. In case of two or more display at the
	 * same level, the first found is returned.
	 * 
	 * @return
	 */
	public BasicDisplay getRootDisplay() {
		BasicDisplay rootDisplay = null;
		for (int ent = 0; ent < this.childEntities.size(); ent++) {
			if (this.childEntities.elementAt(ent) instanceof BasicDisplay) {
				rootDisplay = (BasicDisplay) this.childEntities.elementAt(ent);
				break;
			}
		}

		return rootDisplay;
	}

	/**
	 * Returns the parent display for the specified Entity.
	 * 
	 * @param entity
	 * @return
	 */
	public BasicDisplay findParentDisplay(BasicEntity entity) {
		if (entity instanceof BasicDisplay)
			return (BasicDisplay) entity;

		if (entity == null || entity.getParentEntity() == null)
			return null;

		return this.findParentDisplay(entity.getParentEntity());
	}

	/**
	 * Add an entity to this surface.
	 * 
	 * @param entity the BasicEntity to be added.
	 */
	public void addEntity(BasicEntity entity) {
		this.childEntities.addElement(entity);
		entity.setInteractiveSurface(this);
		entity.addNotify();
	}

	public void removeEntity(BasicEntity entity) {
		this.childEntities.remove(entity);
	}

	public void removeAllEntities() {
		this.childEntities.clear();
	}

	public BasicEntity getEntityByName(String name) {
		for (int ent = 0; ent < this.childEntities.size(); ent++) {
			if (((BasicEntity) this.childEntities.get(ent))
					.getEntityByName(name) != null)
				return ((BasicEntity) this.childEntities.get(ent))
						.getEntityByName(name);
		}

		// none was found
		return null;
	}

	/**
	 * Returns the entity currently under the cursor.
	 * 
	 * @param x
	 *            horizontal coordinate of the cursor.
	 * @param y
	 *            vertical coordinate of the cursor.
	 * @return the BasicEntity currently under the cursor. Returns null if none
	 *         is present.
	 */
	private BasicEntity findOverEntity(double x, double y) {
		BasicEntity currentOverEntity = null;
		BasicEntity tempEntity = null;

		for (int ent = 0; ent < this.childEntities.size(); ent++) {

			tempEntity = ((BasicEntity) this.childEntities.elementAt(ent))
					.returnOverEntity(x, y, 0, -1);
			if (tempEntity != null)
				currentOverEntity = tempEntity;
		}

		return currentOverEntity;
	}

	/**
	 * Returns the Entity currently under the mouse pointer.
	 * 
	 * @return
	 */
	public BasicEntity getOverEntity() {
		return this.currentOverEntity;
	}

	/**
	 * Returns the point (in this component's space) in which the mouse was
	 * pressed (if it was) or where it is (if it was not).
	 * 
	 * @return
	 */
	public Point2D.Double getContactPoint() {
		return this.contact;
	}

	/**
	 * Returns the point currently occupied by the mouse in the event of a
	 * dragged mouse. The value is retained when the button is released.
	 * 
	 * @return
	 */
	public Point2D.Double getReleasePoint() {
		return this.release;
	}

	/**
	 * Sets ContactPoint's parametes to those provided by the specified Point2D.
	 * IT IS A COPY OF THE COORDINATES NOT A RESET OF THE POINTER TO THE POINT.
	 * 
	 * @param newPoint
	 */
	public void setContactPoint(Point2D.Double newPoint) {
		this.contact.x = newPoint.x;
		this.contact.y = newPoint.y;
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() instanceof DefaultActionButton) {
			if (ae.getID() == ActionEvent.ACTION_PERFORMED) {
				((DefaultActionButton) ae.getSource()).performInstantAction();
			}
		}
	}

	// MOUSE FUNCTIONS --- These functions handle the events at a low level and
	// should not be tampered with.
	public void mouseEntered(MouseEvent e) {
		this.repaint();
		if (currentAction != null)
			currentAction.handleEvent(e);
	}

	public void mouseExited(MouseEvent e) {
		if (currentOverEntity != null) {
			currentOverEntity.setMouseover(false);
			currentOverEntity = null;
			this.repaint();
		}
		if (currentAction != null)
			currentAction.handleEvent(e);
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (currentAction != null)
			currentAction.handleWheelEvent(e);
	}
	
	public void mouseMoved(MouseEvent e) {
		this.contact.x = e.getX();
		this.contact.y = e.getY();

		boolean doRepaint = false;

		BasicEntity overEntity = this.findOverEntity(e.getX(), e.getY());

		if (currentOverEntity == null || overEntity != currentOverEntity) {
			if (currentOverEntity == null && overEntity != null) {
				doRepaint = true;
			}
			if (currentOverEntity != null && this.currentAction != null) {
				currentOverEntity.setMouseover(false);
				if (this.userDialog != null)
					this.userDialog.setMessageText("");
				doRepaint = true;
			}

			currentOverEntity = overEntity;
			if (overEntity != null && this.currentAction != null) {
				overEntity.setMouseover(true);
				doRepaint = true;
				if (this.userDialog != null)
					this.userDialog.setMessageText(overEntity.getOverMessage());
			}
			if (doRepaint) {
				this.repaint();
			}
		} else if (currentOverEntity == overEntity) {
			if (this.userDialog != null)
				this.userDialog.setMessageText(overEntity.getOverMessage());
			this.repaint();
		}
		if (currentAction != null)
			currentAction.handleEvent(e);

	}

	public void mouseDragged(MouseEvent e) {
		this.release.x = e.getX();
		this.release.y = e.getY();

		BasicEntity overEntity = this.findOverEntity(e.getX(), e.getY());

		if (currentOverEntity == null || overEntity != currentOverEntity) {
			if (currentOverEntity != null && currentAction != null) {
				currentOverEntity.setMouseover(false);
				this.userDialog.setMessageText("");
				this.repaint();
			}

			currentOverEntity = overEntity;

			if (overEntity != null && currentAction != null) {
				overEntity.setMouseover(true);
				this.repaint();
				this.userDialog.setMessageText(overEntity.getOverMessage());
			}
		}

		if (currentAction != null)
			currentAction.handleEvent(e);
		// this.repaint();
	}

	public void mousePressed(MouseEvent e) {
		this.contact.x = e.getX();
		this.contact.y = e.getY();

		if (currentAction != null)
			currentAction.handleEvent(e);
	}

	public void mouseReleased(MouseEvent e) {
		this.release.x = e.getX();
		this.release.y = e.getY();

		this.requestFocus();
		if (currentAction != null)
			currentAction.handleEvent(e);
	}

	public void mouseClicked(MouseEvent e) {
		if (currentAction != null)
			currentAction.handleEvent(e);
		this.checkButtonsStatus();
	}

	// END of Mouse functions

	public void keyTyped(KeyEvent e) {
		if (e.isConsumed())
			return;
		if (currentAction != null)
			currentAction.handleKeyEvent(e);
	}

	public void keyPressed(KeyEvent e) {
		if (e.isConsumed())
			return;
		if (currentAction != null)
			currentAction.handleKeyEvent(e);
	}

	public void keyReleased(KeyEvent e) {
		// if (currentAction != null)
		// currentAction.handleKeyEvent(e);
		if (e.isConsumed()) {
			return;
		}
		for (DefaultActionButton button : this.buttons) {
			button.handleKeyEvent(e);
		}
	}

	/**
	 * Sets the Rubberband mode ot the specified value. It is basically a line
	 * from the contact point to the release point.
	 * 
	 * @param mode
	 */
	public void setRubberbandMode(boolean mode) {
		this.rubberband = mode;
	}

	/**
	 * Returns the AffineTransform for this component. Typically used for
	 * printing.
	 * 
	 * @return
	 */
	public AffineTransform getGlobalTransform() {
		return this.transform;
	}

	/**
	 * Sets the AffineTransform for this Component. Typically used for printing
	 * (Rotation for landscape printing). Naturally affects all all the Entities
	 * 
	 * @param newTransform
	 */
	public void setGlobalTransform(AffineTransform newTransform) {
		this.transform = newTransform;
	}

	/**
	 * Paints the Screen. Uses double-buffering;
	 */
	public void paint(Graphics g)
	{
		super.paint(g);
	}

	/**
	 * Paints using single buffer. This method is used when maximum resolution
	 * (t<pically vectorial drawing) is needed.
	 * 
	 * @param g
	 */
	public void paintSB(Graphics g) {

		this.setDoubleBuffered(false);
		g2 = (Graphics2D) g;

		g2.setColor(Color.white);
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());

		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);

		for (int ent = 0; ent < this.childEntities.size(); ent++) {
			g2.setTransform(((BasicEntity) this.childEntities.elementAt(ent))
					.getGlobalTransform());
			((BasicEntity) this.childEntities.elementAt(ent)).paintSB(g2);
		}

		g2.setTransform(new AffineTransform());

		g2.setStroke(this.narrowStroke);

		System.out.println("comp " + this.getComponentCount());
		if (this.getComponentCount() != 0) {
			super.paint(g);
		}
	}

	public void paintComponent(Graphics g) {
		if (bufferImage == null || bufferImage.getWidth() != this.getWidth()
				|| bufferImage.getHeight() != this.getHeight()) {
			bufferImage = new BufferedImage(this.getWidth(), this.getHeight(),
					BufferedImage.TYPE_4BYTE_ABGR);
			g2 = (Graphics2D) bufferImage.getGraphics();
		}

		g2.setColor(Color.white);
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());

		if (this.hiDefinition) {
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
		} else {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}

		for (int ent = 0; ent < this.childEntities.size(); ent++) {
			g2.setTransform(((BasicEntity) this.childEntities.elementAt(ent))
					.getGlobalTransform());
			((BasicEntity) this.childEntities.elementAt(ent)).paint(g2);
		}

		g2.setTransform(new AffineTransform());

		if (rubberband) {
			g2.setColor(Color.green);
			g2.setStroke(new BasicStroke(2));
			g2.draw(new Line2D.Double(this.contact.x, this.contact.y,
					this.release.x, this.release.y));
		}

		g2.setStroke(this.narrowStroke);
		g.drawImage(bufferImage, 0, 0, this.getWidth(), this.getHeight(), 0, 0,
				this.getWidth(), this.getHeight(), null);
	}

	/**
	 * Returns the number of pages. Used for printing
	 */
	public int getNumberOfPages() {
		return 1;
	}

	/**
	 * Returns the printing format. LANDSCAPE
	 */
	public PageFormat getPageFormat(int pageIndex) {
		PageFormat pageFormat = PrinterJob.getPrinterJob().defaultPage();// new
		pageFormat.setOrientation(PageFormat.LANDSCAPE);

		return pageFormat;
	}

	public Printable getPrintable(int pageIndex) {
		return this;
	}

	/**
	 * "Prints" the screen to the specified Graphics context.
	 */
	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		g2 = (Graphics2D) g;

		if (pageIndex == 0) {
			AffineTransform tempTransform = new AffineTransform();
			
			tempTransform.translate(pageFormat.getImageableX(), pageFormat
					.getImageableY());
			getGlobalTransform().preConcatenate(tempTransform);			
			g2.transform(this.getGlobalTransform());
			this.setGlobalTransform(g2.getTransform());
			((BasicEntity) this.childEntities.elementAt(0))
					.checkSizeAndPosition();

			for (int ent = 0; ent < this.childEntities.size(); ent++) {
				g2.setTransform(((BasicEntity) this.childEntities.elementAt(0))
						.getGlobalTransform());
				((BasicEntity) this.childEntities.elementAt(0)).paint(g2);
			}

			return PAGE_EXISTS;
		} else
			return NO_SUCH_PAGE;
	}

	// ------------------------LINK METHODS------------------------------------

	/**
	 * Returns a new ID that can be used for identifying entities when storing
	 * data (in XML for example) required for recreating links.
	 */
	protected int requestID() {
		return ++this.lastUsedID;
	}

	/**
	 * Sets the last used ID (for links) to the specified values)
	 * 
	 * @param newID
	 */
	protected void setLastID(int newID) {
		if (DEBUG)
			System.out.println("setting last ID to " + newID);
		this.lastUsedID = newID;
	}

	/**
	 * Returns the entity that has the specified ID
	 * 
	 * @param entityID
	 * @return
	 */
	private BasicEntity getEntityByID(int entityID) {
		BasicEntity tempEntity;

		for (int ent = 0; ent < this.childEntities.size(); ent++) {
			tempEntity = ((BasicEntity) this.childEntities.get(ent))
					.getEntityByID(entityID);
			if (tempEntity != null)
				return tempEntity;
		}

		return null;
	}

	/**
	 * Recreate a link between the specified Entity and the ones referred to by
	 * the specified ID.
	 * 
	 * @param srcEntity
	 * @param dstID
	 * @param linkType
	 */
	public void restoreLink(BasicEntity srcEntity, int dstID, int linkType) {
		BasicEntity root = this.getRootDisplay();

		BasicEntity dstEntity = this.getEntityByID(dstID);

		if (dstEntity != null) {
			this.createLink(srcEntity, dstEntity, linkType);
			if (DEBUG)
				System.out.println("restoring: " + srcEntity.getUniqueID()
						+ " -> " + dstID + " --- " + srcEntity + " -> "
						+ dstEntity);
		} else {
			if (DEBUG)
				System.out.println("null entity in restore: " + dstID);
		}
	}

	/**
	 * Creates a link between the two specified entities.
	 * 
	 * @param entityA
	 * @param entityB
	 * @param inLinkType
	 */
	public void createLink(BasicEntity entityA, BasicEntity entityB,
			int inLinkType) {
		if (entityA.isLinkable() && entityB.isLinkable()) {
			this.linkListA.addElement(entityA);
			this.linkListB.addElement(entityB);
			this.linkType.addElement(new Integer(inLinkType));
		}
	}

	/**
	 * Creates a default link (directional) between the two specified Enitites
	 * 
	 * @param entityA
	 * @param entityB
	 */
	public void createLink(BasicEntity entityA, BasicEntity entityB) {
		int thisLinkType = 0;

		// thisLinkType=thisLinkType ^ this.INTEGRITY_CHECK;

		this.createLink(entityA, entityB, thisLinkType);
	}

	/**
	 * Removes all links to and from the specified Entity.
	 * 
	 * @param entity
	 */
	public void removeAllLinks(BasicEntity entity) {
		removeAllLinks(entity,null);
	}
	
	/**
	 * Removes all links to and from the specified Entity.
	 * 
	 * @param entity
	 */
	public void removeAllLinks(BasicEntity entity, LinkCreationHandler linkHandler) {
		Vector linkedEntities = this.getLinkedEntitesButStartType(entity);
		Vector linkedThruEntities = this.getLinkedThruEntities(entity);

		for (int ent = 0; ent < linkedThruEntities.size(); ent++)
			((BasicEntity) linkedThruEntities.elementAt(ent))
					.setHighlighted(false);

		for (int ent = 0; ent < linkedEntities.size(); ent++) {
			BasicEntity linkedEntity =  (BasicEntity) linkedEntities
			.elementAt(ent);
			if(linkHandler!=null)
				linkHandler.handleLinkDeletion(this, entity, linkedEntity);
			else{
				this.removeLink(entity, linkedEntity);
				this.removeLink(linkedEntity, entity);
				
			}
		}
	}

	/**
	 * Removes the link between the two specified Entities (in both directions)
	 * 
	 * @param entityA
	 * @param entityB
	 */
	public void removeLink(BasicEntity entityA, BasicEntity entityB) {
		for (int link = this.linkListA.size() - 1; link >= 0; link--) {
			if ((this.linkListA.elementAt(link) == entityA && this.linkListB
					.elementAt(link) == entityB)
					|| (this.linkListA.elementAt(link) == entityB && this.linkListB
							.elementAt(link) == entityA)) {
				this.linkListA.removeElementAt(link);
				this.linkListB.removeElementAt(link);
			}
		}
	}

	/**
	 * Returns a Vector containing all the Entities linked (in either direction)
	 * to the specified Entity.
	 */
	public Vector<BasicEntity> getLinkedEntities(BasicEntity entity) {
		Vector<BasicEntity> tempVector = new Vector<BasicEntity>();

		for (int link = 0; link < this.linkListA.size(); link++) {
			if (this.linkListA.elementAt(link) == entity)
				tempVector.addElement(this.linkListB.elementAt(link));
			if (this.linkListB.elementAt(link) == entity)
				tempVector.addElement(this.linkListA.elementAt(link));
		}
		return tempVector;
	}

	/**
	 * Returns a vector containing the entities linked to 'entity' with a
	 * 'linktype' link.
	 */
	public Vector<BasicEntity> getLinkedDestEntities(BasicEntity entity, int linkType) {
		Vector<BasicEntity> tempVector = new Vector<BasicEntity>();

		for (int link = 0; link < this.linkListA.size(); link++) {
			if (this.linkListA.elementAt(link) == entity
					&& ((Integer) this.linkType.elementAt(link)).intValue() == linkType)
				tempVector.addElement(this.linkListB.elementAt(link));
		}
		return tempVector;
	}

	/**
	 * Returns a vector containing the entities who which entity is linked to
	 * (monodirectional).
	 */
	public Vector<BasicEntity> getLinkedDestEntities(BasicEntity entity) {
		Vector<BasicEntity> tempVector = new Vector<BasicEntity>();

		for (int link = 0; link < this.linkListA.size(); link++) {
			if (this.linkListA.elementAt(link) == entity)
				tempVector.addElement(this.linkListB.elementAt(link));
		}
		return tempVector;
	}

	/**
	 * Returns all the entities directly or indrectly linked to the specified
	 * entity.
	 * 
	 */
	public Vector<BasicEntity> getLinkedThruEntities(BasicEntity entity) {
		Vector<BasicEntity> allEntities = new Vector<BasicEntity>();
		Vector<BasicEntity> tempEntities = new Vector<BasicEntity>();

		allEntities = this.getLinkedEntities(entity);

		int lastIndex = 0;

		while (lastIndex < allEntities.size()) {
			tempEntities = this.getLinkedEntities((BasicEntity) allEntities
					.elementAt(lastIndex));

			for (int ent = 0; ent < tempEntities.size(); ent++) {
				if (tempEntities.elementAt(ent) != entity
						&& !allEntities.contains(tempEntities.elementAt(ent))) {
					if (((BasicEntity) allEntities.elementAt(lastIndex)).isHomolink()
							&& tempEntities.elementAt(ent).getClass() == ((BasicEntity) allEntities
									.elementAt(lastIndex)).getClass()) {
						allEntities.addElement(tempEntities.elementAt(ent));
					} else if (((BasicEntity) allEntities.elementAt(lastIndex)).isHeterolink()
							&& tempEntities.elementAt(ent).getClass() != ((BasicEntity) allEntities
									.elementAt(lastIndex)).getClass()) {
						allEntities.addElement(tempEntities.elementAt(ent));
					}
				}
			}

			lastIndex++;
		}
		return allEntities;
	}

	public Vector<BasicEntity> getLinkedInboundEntities(BasicEntity entity) {
		Vector<BasicEntity> allEntities = new Vector<BasicEntity>();
		Vector<BasicEntity> tempEntities = new Vector<BasicEntity>();

		allEntities = this.getLinkedSrcEntities(entity);

		int lastIndex = 0;

		while (lastIndex < allEntities.size()) {
			tempEntities = this.getLinkedSrcEntities(allEntities
					.elementAt(lastIndex));

			for (int ent = 0; ent < tempEntities.size(); ent++) {
				if (tempEntities.elementAt(ent) != entity
						&& !allEntities.contains(tempEntities.elementAt(ent))) {
					if ((allEntities.elementAt(lastIndex)).isHomolink()
							&& tempEntities.elementAt(ent).getClass() == (allEntities
									.elementAt(lastIndex)).getClass()) {
						allEntities.addElement(tempEntities.elementAt(ent));
					} else if ((allEntities.elementAt(lastIndex)).isHeterolink()
							&& tempEntities.elementAt(ent).getClass() != (allEntities
									.elementAt(lastIndex)).getClass()) {
						allEntities.addElement(tempEntities.elementAt(ent));
					}
				}
			}

			lastIndex++;
		}

		return allEntities;

	}

	public Vector<BasicEntity> getLinkedSrcEntities(BasicEntity entity) {
		Vector<BasicEntity> tempVector = new Vector<BasicEntity>();

		for (int link = 0; link < this.linkListA.size(); link++) {
			if (this.linkListB.elementAt(link) == entity)
				tempVector.addElement((BasicEntity) this.linkListA
						.elementAt(link));
		}
		return tempVector;
	}

	/*
	 * Returns all directly and indirectly linked dest entities
	 */
	public Vector<BasicEntity> getLinkedOutboundEntities(BasicEntity entity) {
		Vector<BasicEntity> allEntities = new Vector<BasicEntity>();
		Vector<BasicEntity> tempEntities = new Vector<BasicEntity>();

		allEntities = this.getLinkedDestEntities(entity);

		int lastIndex = 0;

		while (lastIndex < allEntities.size()) {
			tempEntities = this.getLinkedDestEntities(allEntities.elementAt(lastIndex));

			for (int ent = 0; ent < tempEntities.size(); ent++) {
				if (tempEntities.elementAt(ent) != entity
						&& !allEntities.contains(tempEntities.elementAt(ent))) {
					if (((BasicEntity) allEntities.elementAt(lastIndex)).isHomolink()
							&& tempEntities.elementAt(ent).getClass() == ((BasicEntity) allEntities
									.elementAt(lastIndex)).getClass()) {
						allEntities.addElement(tempEntities.elementAt(ent));
					} else if (((BasicEntity) allEntities.elementAt(lastIndex)).isHeterolink()
							&& tempEntities.elementAt(ent).getClass() != ((BasicEntity) allEntities
									.elementAt(lastIndex)).getClass()) {
						allEntities.addElement(tempEntities.elementAt(ent));
					}
				}
			}

			lastIndex++;
		}

		return allEntities;
	}

	public void hasUndo(boolean hasUndo) {
		this.hasUndo = hasUndo;
	}

	protected void undo() {
		
		/* System.out.println("UNDO: "+this.undoStackIndex); if (undoStackIndex >
		  0) { this.undoStackIndex--;
		  this.getRootDisplay(this.activeDisplay).init((String)this.undoStack.elementAt(undoStackIndex));
		  this.getRootDisplay(this.activeDisplay).repaint(); }
		 */
		
		if (undoStack.size() > 0) {
			// System.out.println("UNDO performed");
			BasicDisplay rootDisplay = (BasicDisplay) this.childEntities
					.elementAt(0);
			Hashtable helpers = new Hashtable();
			rootDisplay.setInteractiveSurface(this);
			rootDisplay.init((String) this.undoStack
					.elementAt(undoStack.size() - 1), helpers);
			rootDisplay.checkInteractiveSurface();
			rootDisplay.restoreLinks();
			this.undoStack.removeElementAt(undoStack.size() - 1);

			if (DEBUG)
				System.out.println("undoStack size: " + undoStack.size());
			rootDisplay.checkSizeAndPosition();
			this.repaint();
		}
	}

	/**
	 * Takes a snapshot (XML IMAGE) of the current situation and stores it to
	 * provide undo capabilities. Basically this should be used every time an
	 * action is performed.
	 * 
	 */
	public void takeUndoSnapshot() {
		if (this.hasUndo) {
			
			Hashtable xmlProperties = new Hashtable();
			xmlProperties.put("includeURL", new Boolean(true));
			xmlProperties.put("embedJCamp", new Boolean(true));
			

			String tempXML = ((BasicEntity) this.childEntities.elementAt(0))
					.getXmlTag(xmlProperties);
			if (undoStack.size() == 0
					|| tempXML.compareTo((String) undoStack.elementAt(undoStack
							.size() - 1)) != 0) {
				// System.out.println("UNDO shot");
				undoStack.addElement(tempXML);
			}
		}
		
		//System.out.println("InteractiveSurface.takeUndoSnapshot()");
		for (ModelChangedListener listener : modelChangedListeners) 
		{
			listener.modelChanged(this);
		}
	}

	public String getXML() {
		String xmlString = "";

		Hashtable xmlProperties = new Hashtable();

		for (int entity = 0; entity < this.childEntities.size(); entity++)
		{
			System.out.println(this.childEntities.get(entity));
			xmlString += ((BasicEntity) this.childEntities.get(entity)).getXmlTag(xmlProperties);
		}

		return xmlString;
	}

	public void setXML(String xmlString) {
		this.removeAllEntities();
		this.removeAll();
		Hashtable xmlHelpers = new Hashtable();

		xmlHelpers.put("interactiveSurface", this);

		XMLCoDec tempCodec = new XMLCoDec(xmlString);

		int elements = tempCodec.getRootElementsCount();

		for (int elem = 0; elem < elements; elem++) {
			XMLCoDec tempCodec2 = new XMLCoDec(tempCodec.readXMLTag());

			tempCodec2.shaveXMLTag();
			try {
				if (tempCodec2.getParameterAsString("tagName").trim()
						.compareTo("HookResources") == 0) {
					this.resourceManager = new HookResourceManager(tempCodec
							.popXMLTag());
				} else {
					Class entityClass = Class
							.forName("org.cheminfo.hook."
									+ tempCodec2
											.getParameterAsString("tagName")
											.trim());
					Class[] parameterClasses = { String.class, Hashtable.class };
					java.lang.reflect.Constructor entityConstructor = entityClass
							.getConstructor(parameterClasses);

					Object[] parameters = { tempCodec.popXMLTag(), xmlHelpers };
					this.addEntity((BasicEntity) entityConstructor
							.newInstance(parameters));
				}
			} catch (ClassNotFoundException ex1) {
				System.out.println("InteractiveSurface XML reader e: " + ex1);
				ex1.printStackTrace();
			} catch (IllegalAccessException ex2) {
				System.out.println("InteractiveSurface XML reader e: " + ex2);
				ex2.printStackTrace();
			} catch (InstantiationException ex3) {
				System.out.println("InteractiveSurface XML reader e: " + ex3);
				ex3.printStackTrace();
			} catch (InvocationTargetException ex4) {
				System.out.println("InteractiveSurface XML reader e: " + ex4);
				ex4.printStackTrace();
			} catch (NoSuchMethodException ex5) {
				System.out.println("InteractiveSurface XML reader e: " + ex5);
				ex5.printStackTrace();
			}
			;
		}

		for (int child = 0; child < this.childEntities.size(); child++)
			((BasicEntity) this.childEntities.get(child))
					.checkInteractiveSurface();
	}

	public void setNarrowStroke(BasicStroke narrowStroke) {
		this.narrowStroke = narrowStroke;
	}

	public DefaultActionButton getDefaultAction() {
		return defaultAction;
	}

	public void setDefaultAction(DefaultActionButton defaultAction) {
		this.defaultAction = defaultAction;
	}

	public BasicStroke getMediumStroke() {
		return mediumStroke;
	}

	public void setMediumStroke(BasicStroke mediumStroke) {
		this.mediumStroke = mediumStroke;
	}

	public void invertLink(BasicEntity entityA, BasicEntity entityB) {
		for (int i = 0; i < this.linkListA.size(); i++) {
			if (linkListA.get(i) == entityA && linkListB.get(i) == entityB) {
				linkListA.set(i, entityB);
				linkListB.set(i, entityA);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Vector<BasicEntity> getLinkedEntitesButStartType(BasicEntity entity) {
		Class startType = entity.getClass();
		Vector<BasicEntity> allEntities = new Vector<BasicEntity>();
		Vector<BasicEntity> tempEntities = new Vector<BasicEntity>();
		allEntities = this.getLinkedEntities(entity);
		int lastIndex = 0;
		while (lastIndex < allEntities.size()) {
			tempEntities = this.getLinkedEntities((BasicEntity) allEntities
					.elementAt(lastIndex));

			for (int ent = 0; ent < tempEntities.size(); ent++) {
				if (tempEntities.elementAt(ent) != entity
						&& ((BasicEntity) tempEntities.elementAt(ent))
								.getClass() != startType
						&& !allEntities.contains(tempEntities.elementAt(ent))) {
					if (((BasicEntity) allEntities.elementAt(lastIndex)).isHomolink()
							&& tempEntities.elementAt(ent).getClass() == ((BasicEntity) allEntities
									.elementAt(lastIndex)).getClass()) {
						allEntities.addElement(tempEntities.elementAt(ent));
					} else if (((BasicEntity) allEntities.elementAt(lastIndex)).isHeterolink()
							&& tempEntities.elementAt(ent).getClass() != ((BasicEntity) allEntities
									.elementAt(lastIndex)).getClass()) {
						allEntities.addElement(tempEntities.elementAt(ent));
					}
				}
			}

			lastIndex++;
		}
		return allEntities;
	}

	public Vector<BasicEntity> getLinkedEntitiesMaxDist(BasicEntity entity,
			int nDistMax) {
		int masterID = entity.getUniqueID();
		TreeMap<Integer, BasicEntity> foundEntities = new TreeMap<Integer, BasicEntity>();
		for (int i = 0; i < this.linkListA.size(); i++) {
			BasicEntity foundEntity = null;
			if (this.linkListA.get(i) == entity) {
				foundEntity = (BasicEntity) this.linkListB.get(i);
			} else if (this.linkListB.get(i) == entity) {
				foundEntity = (BasicEntity) this.linkListA.get(i);
			}
			if (foundEntity != null && foundEntity.getUniqueID() != -1
					&& !foundEntities.containsKey(foundEntity.getUniqueID())
					&& foundEntity.getUniqueID() != masterID) {
				foundEntities.put(foundEntity.getUniqueID(), foundEntity);
			}
		}
		TreeMap<Integer, BasicEntity> currentLevel = new TreeMap<Integer, BasicEntity>();
		currentLevel.putAll(foundEntities);
		TreeMap<Integer, BasicEntity> nextLevel = new TreeMap<Integer, BasicEntity>();
		for (int iDist = 2; iDist <= nDistMax; iDist++) {
			for (BasicEntity currentEntity : currentLevel.values()) {
				Vector entities = this.getLinkedEntities(currentEntity);
				for (int iLink = 0; iLink < entities.size(); iLink++) {
					if (entities.get(iLink) instanceof BasicEntity) {
						BasicEntity nextEntity = (BasicEntity) entities
								.get(iLink);
						int uniqueID = nextEntity.getUniqueID();
						if (uniqueID != -1
								&& !foundEntities.containsKey(uniqueID)
								&& uniqueID != masterID)
							nextLevel.put(uniqueID, nextEntity);
					}
				}
			}
			foundEntities.putAll(nextLevel);
			currentLevel.clear();
			TreeMap<Integer, BasicEntity> tmp;
			tmp = currentLevel;
			currentLevel = nextLevel;
			nextLevel = tmp;
		}
		Vector<BasicEntity> linkedEntities = new Vector<BasicEntity>();
		linkedEntities.addAll(foundEntities.values());
		return linkedEntities;
	}
	
	public void addModelChangedListener(ModelChangedListener modelChangedListener)
	{
		modelChangedListeners.add(modelChangedListener);
	}

	public void removeModelChangedListener(ModelChangedListener modelChangedListener)
	{
		modelChangedListeners.remove(modelChangedListener);
	}
	
	public void resetUndoStack()
	{
		undoStack.clear();
	}

}