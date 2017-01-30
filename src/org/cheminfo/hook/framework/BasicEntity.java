package org.cheminfo.hook.framework;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Vector;

import org.cheminfo.hook.util.XMLCoDec;

public class BasicEntity {
	public final static int GLOBAL = 0;
	public final static int HORIZONTAL = 1;
	public final static int VERTICAL = 2;
	public final static int FIXED = 3;

	public final static int HL_PROPAGATION_FULL = 1;
	public final static int HL_PROPAGATION_OUTBOUND = 2;
	public final static int HL_PROPAGATION_MAX_RANGE = 3;
	public final static int HL_PROPAGATION_INBOUND_ONLY = 4;
	public final static int HL_PROPAGATION_OUTBOUND_AND_PARENT = 5;

	private int maximumPropagationLevel = 2;

	private InteractiveSurface interactions = null;

	private String entityName;
	private AffineTransform localTrans;  // parent coords to entity coords
	private AffineTransform globalTrans; // InteractiveSurface coords to entity coords

	private Point2D.Double position; // position in PARENT coordinates
	private Point2D.Double size;

	private Vector<BasicEntity> childEntities;
	private BasicEntity parentEntity = null;

	private Area sensitiveArea = null;

	private int highlightPropagationType = BasicEntity.HL_PROPAGATION_FULL;

	private int movementType;
	private boolean mouseover = false;
	private boolean selected = false;
	private boolean highlighted = false;
	private boolean erasable = true;
	private boolean shiftErasable = true;	// erasable with shift+delete
	
	
	// Keeps track if required to redraw the spectrum (phase change, color change, etc)
	// this is only used currently in 2D
	protected boolean needsRepaint=true;

	// parameters for homo and hetero recursivity in link search
	private boolean homolink = true; // by default continue search on
	// entities of same type
	private boolean heterolink = true; // by default continue search on
	// entities of different type

	private boolean isLinkable = true; // by default all entities can be linked
	
	
	private Color primaryColor = null;
	private Color secondaryColor = null;

	private int uniqueID = -1;
	protected int[] linkedIDs;
	protected int[] linkTypes;

	private int entityLayer;
	
	

	private Vector<EventListener> associatedListeners;

	public BasicEntity() {
		this(0, 0);
	}

	public BasicEntity(String entityName) {
		this(0, 0);
		this.entityName = entityName;
	}

	public BasicEntity(double width, double height) {
		this.entityName = "";
		this.position = new Point2D.Double(0, 0);
		this.localTrans = new AffineTransform();
		this.globalTrans = new AffineTransform();

		this.size = new Point2D.Double(width, height);

		childEntities = new Vector<BasicEntity>();
		this.refreshSensitiveArea();
		this.movementType = BasicEntity.GLOBAL;

		this.associatedListeners = new Vector<EventListener>();
	}

	public BasicEntity(String xmlString, double parentWidth, double parentHeight, Hashtable helpers) {
	}

	/**
	 * Sets the movement constraints of this entity. Possible types are: GLOBAL,
	 * VERTICAL, HORIZONTAL and FIXED.
	 * 
	 * @param movementType
	 */
	public void setMovementType(int movementType) {
		this.movementType = movementType;
	}

	public boolean resetNeedsRepaint() {
		if (this.needsRepaint) {
			this.needsRepaint=false;
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the movement constraints of this entity.
	 * 
	 * @return an int value among GLOBAL, VERTICAL, HORIZONTAL and FIXED.
	 */
	public int getMovementType() {
		return this.movementType;
	}

	/**
	 * Used to check is legal use of the applet
	 */
	public boolean isLegal() {
		return interactions.isLegal;
	}

	/**
	 * Set the interactive hub for this entity. This method is normally called
	 * by the addEntity method.
	 * 
	 * @param interactions
	 */
	public void setInteractiveSurface(InteractiveSurface interactions) {
		this.interactions = interactions;

		if (interactions != null) {
			if (this.getUniqueID() == -1) {
				this.setUniqueID(interactions.requestID());
			} else {
				interactions.setLastID(this.getUniqueID());
			}
		}
	}

	/**
	 * Returns the interactive hub for this entity. Normally there should be one
	 * per application.
	 * 
	 * @return
	 */
	public InteractiveSurface getInteractiveSurface() {
		return this.interactions;
	}

	/**
	 * Makes sure that all child Entities have a pointer to the interactive hub.
	 * 
	 */
	public void checkInteractiveSurface() {
		for (int ent = 0; ent < this.getEntitiesCount(); ent++) {
			if (this.getEntity(ent).getInteractiveSurface() == null)
				this.getEntity(ent).setInteractiveSurface(this.getInteractiveSurface());

			this.getEntity(ent).checkInteractiveSurface();
		}
	}

	/**
	 * Sets the name of this Entity. Used for search purposes.
	 * 
	 * @param name
	 */
	public void setEntityName(String name) {
		this.entityName = name;
	}

	/**
	 * Returns the name of this Entity. Used for Search purposes.
	 * 
	 * @return
	 */
	public String getEntityName() {
		return this.entityName;
	}

	public BasicEntity getEntityByName(String name) {
		if (this.entityName.equals(name))
			return this;

		for (int ent = 0; ent < this.childEntities.size(); ent++) {
			if (((BasicEntity) this.childEntities.get(ent))
					.getEntityByName(name) != null)
				return ((BasicEntity) this.childEntities.get(ent))
						.getEntityByName(name);
		}

		return null;
	}

	public BasicEntity getEntityByID(int id) {
		if (this.uniqueID == id)
			return this;

		for (int ent = 0; ent < this.childEntities.size(); ent++) {
			if (((BasicEntity) this.childEntities.get(ent)).getEntityByID(id) != null)
				return ((BasicEntity) this.childEntities.get(ent))
						.getEntityByID(id);
		}

		return null;
	}

	/**
	 * Sets the primary color to the specified value.
	 * 
	 * @param color
	 */
	public void setPrimaryColor(Color color) {
		this.needsRepaint=true;
		this.primaryColor = color;
	}

	/**
	 * eturns the primary color.
	 * 
	 * @return
	 */
	public Color getPrimaryColor() {
		return this.primaryColor;
	}

	/**
	 * Sets the secondary color to the specified value.
	 * 
	 * @param color
	 */
	public void setSecondaryColor(Color color) {
		this.needsRepaint=true;
		this.secondaryColor = color;
	}

	/**
	 * Returns the secondary color.
	 * 
	 * @return
	 */
	public Color getSecondaryColor() {
		return this.secondaryColor;
	}

	/**
	 * Sets the parent entity for this Entity. Normally this is done
	 * automatically by the addEntity method and there should be no need to do
	 * it otherwise.
	 * 
	 * @param parent
	 */
	private void setParentEntity(BasicEntity parent) {
		this.parentEntity = parent;
	}

	/**
	 * returns the parent Entity.
	 * 
	 * @return
	 */
	public BasicEntity getParentEntity() {
		return this.parentEntity;
	}

	/**
	 * Returns the ID for this Entity. Typically provided by
	 * InteractiveSurface.requestID() or retieved from an XML image.
	 * 
	 * @return
	 */
	public int getUniqueID() {
		return this.uniqueID;
	}

	/**
	 * Sets this Entity unique ID.
	 * 
	 * @param newID
	 */
	public void setUniqueID(int newID) {
		this.uniqueID = newID;
	}

	/**
	 * Adds a new entity to the childEntities Vector of this entity and calls
	 * the respective addNotify method. WARNING doesn't work yet!! (index is
	 * ignored)
	 * 
	 * @param entity
	 */
	public void addEntity(BasicEntity entity, int index) {
		this.childEntities.add(index, entity);
		entity.setParentEntity(this);
		entity.setInteractiveSurface(this.interactions);
		entity.addNotify();
		entity.updateEntity();
	}

	public void addEntity(BasicEntity entity) {
		this.addEntity(entity, this.childEntities.size());
	}

	/**
	 * This method is called when this Entity is added to another.
	 * Implementation is Entity-specific.
	 */
	public void addNotify() {

	}

	/**
	 * Returns whether the specified Entity is one of the child Entities.
	 * 
	 * @param entity
	 * @return
	 */
	public boolean containsEntity(BasicEntity entity) {
		return this.childEntities.contains(entity);
	}

	/**
	 * Returns the number of child Entities for this Entity.
	 * 
	 * @return
	 */
	public int getEntitiesCount() {
		return this.childEntities.size();
	}

	/**
	 * returns the (ent)nth child Entity.
	 * 
	 * @param ent
	 * @return
	 */
	public BasicEntity getEntity(int ent) {
		return (BasicEntity) this.childEntities.elementAt(ent);
	}

	public Vector<BasicEntity> getEntities() {
		return this.childEntities;
	}
	
	/**
	 * Removes the specified Entity from the list of child Entities (IT DOES NOT
	 * DELETE THE ENTITY!!)
	 * 
	 * @param entity
	 */
	public void remove(BasicEntity entity) {
		this.childEntities.removeElement(entity);
	}

	/**
	 * Removes the Entity at position (index) in the list from the list of child
	 * Entities (IT DOES NOT DELETE THE ENTITY!!)
	 * 
	 * @param entity
	 */
	public void remove(int index) {
		this.childEntities.remove(index);
	}

	public void notifyDelete() {
		
	}
	
	/**
	 * Removes all child entities from the list (IT DOES NOT DELETE THE
	 * ENTITIES!!!)
	 * 
	 */
	public void removeAll() {
		BasicEntity[] originalEntites =this.childEntities.toArray(new BasicEntity[this.childEntities.size()]); 
		for (BasicEntity e: originalEntites)
			e.delete();
		
		this.childEntities.clear();
	}

	
	
	public void delete() {
		this.delete(false);
	}
	/**
	 * Deletes this Entity (physically)
	 * 
	 */
	public void delete(boolean shiftPressed) {
		if ((this.erasable) || (this.shiftErasable && shiftPressed)) {
			this.notifyDelete();
			if (this.getInteractiveSurface() != null)
				this.getInteractiveSurface().removeAllLinks(this);
			for (int ent = this.childEntities.size() - 1; ent >= 0; ent--) {
				if (((BasicEntity) this.childEntities.elementAt(ent)) != null)
					((BasicEntity) this.childEntities.elementAt(ent)).delete();
			}
			
			this.getParentEntity().remove(this);

			for (int listener = 0; listener < this.associatedListeners.size(); listener++) {
				if (this.associatedListeners.elementAt(listener) instanceof MouseListener)
					this.getInteractiveSurface().removeMouseListener(
							(MouseListener) this.associatedListeners.elementAt(listener));

				if (this.associatedListeners.elementAt(listener) instanceof MouseMotionListener)
					this.getInteractiveSurface().removeMouseMotionListener(
							(MouseMotionListener) this.associatedListeners.elementAt(listener));
			}
			this.associatedListeners.clear();
		}
	}

	/**
	 * Sets the location of this Entity in the parent's space to the specified
	 * coordinates. Updates the local and (indirectly) the global
	 * AffineTransform for this Entity.
	 * 
	 * @param x
	 * @param y
	 */
	public void setLocation(double x, double y) {
		this.position.setLocation(x, y);
		this.localTrans.setToTranslation(x, y);

		this.updateEntity();
	}

	public void setLocation(Point2D.Double point) {
		this.setLocation(point.x, point.y);
	}

	/**
	 * Returns the local Affine Transform for this Entity. Represents the
	 * location and orientation of this Entity in respect of the parent Entity.
	 * 
	 * @return
	 */
	public AffineTransform getLocalTransform() {
		return this.localTrans;
	}

	/**
	 * Sets the local AffineTransform to the specified value. Indirectly updates
	 * the global transform as well.
	 * 
	 * @param transform
	 */
	public void setLocalTransform(AffineTransform transform) {
		this.localTrans = transform;
		this.updateEntity();
	}

	/**
	 * Returns the location (in parent's space) of this Entity.
	 */
	public Point2D.Double getLocation() {
		return this.position;
	}

	/**
	 * Moves this Entity relatively to its current location.
	 * 
	 * @param deltaX
	 * @param deltaY
	 */
	public void moveLocal(double deltaX, double deltaY) {
		this.setLocation(this.getLocation().x + deltaX, this.getLocation().y
				+ deltaY);
	}

	/**
	 * Allows an Entity to implement a special reaction to a mouse release (e.g.
	 * a "snap on" effect)
	 * 
	 */
	public void reactOnRelease() {

	}

	/**
	 * Sets the size of this Entity.
	 * 
	 * @param width
	 * @param height
	 */
	public void setSize(double width, double height) {
		this.size.x = width;
		this.size.y = height;
	}

	/**
	 * Returns the width of this Entity.
	 * 
	 * @return
	 */
	public double getWidth() {
		return this.size.x;
	}

	/**
	 * Returns the hight of this Entity.
	 * 
	 * @return
	 */
	public double getHeight() {
		return this.size.y;
	}

	/**
	 * Sets the sensitive area for this Entity to the specified Area
	 * 
	 * @param newArea
	 */
	public void setSensitiveArea(Area newArea) {
		this.sensitiveArea = newArea;
	}

	/**
	 * Updates the sensitive area of this entity and triggers the same for all
	 * the child Entities. Call this method every time the entity is reshaped.
	 * Every extension of BasicEntity should complement this method to provide
	 * specific behaviour.
	 */
	public void refreshSensitiveArea() {
		// if (this.sensitiveArea == null ||
		// this.sensitiveArea.getBounds().width == 0)
		this.sensitiveArea = new Area(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));

		for (int ent = 0; ent < this.getEntitiesCount(); ent++)
			this.getEntity(ent).refreshSensitiveArea();
	}

	/**
	 * Updates the global transform of this entity as well as the one of all its
	 * child entities. Normally called when the entity is moved (which modifies
	 * the local transform).
	 * 
	 */
	private void updateEntity() {
		if (this.parentEntity != null)
			this.globalTrans = new AffineTransform(this.parentEntity.getGlobalTransform());
		else {
			if (this.getInteractiveSurface() == null) {
			} else {
				this.globalTrans = (AffineTransform) this
						.getInteractiveSurface().getGlobalTransform().clone();
			}
		}
		this.globalTrans.concatenate(this.localTrans); // could be
		// preConcateneate

		for (int ent = 0; ent < this.childEntities.size(); ent++)
			((BasicEntity) this.childEntities.elementAt(ent)).updateEntity();
	}

	/**
	 * Associates a Listener to this Entity. This Entity should be the one
	 * reflecting the behaviour of the Listener.
	 * 
	 * @param newListener
	 */
	public void addAssociatedListener(EventListener newListener) {
		this.associatedListeners.add(newListener);
	}

	public void updatePriorityVector(Vector priorityVector) {
		priorityVector.addElement(this);

		for (int ent = 0; ent < this.childEntities.size(); ent++) {
			if (!(this.childEntities.elementAt(ent) instanceof BasicDisplay))
				((BasicEntity) this.childEntities.elementAt(ent))
						.updatePriorityVector(priorityVector);
		}
		for (int ent = 0; ent < this.childEntities.size(); ent++) {
			if (this.childEntities.elementAt(ent) instanceof BasicDisplay)
				((BasicEntity) this.childEntities.elementAt(ent))
						.updatePriorityVector(priorityVector);
		}
	}

	/**
	 * Checks an Entity size and position. Every extension of BasicEntity should
	 * complement this method to provide specific behaviour.
	 */
	public void checkSizeAndPosition() {
		this.updateEntity();
		this.refreshSensitiveArea();
		for (int ent = 0; ent < this.getEntitiesCount(); ent++)
			this.getEntity(ent).checkSizeAndPosition();
	}

	/**
	 * Returns an AffineTransform converting a point in InteractiveSurface's
	 * coordinates into this BasicEntity coordinates.
	 * 
	 * @return the global AffineTransform.
	 */
	public AffineTransform getGlobalTransform() {
		return this.globalTrans;
	}

	public Area getSensitiveArea() {
		return this.sensitiveArea;
	}

	public boolean isErasable() {
		return this.erasable;
	}

	public void setErasable(boolean isErasable) {
		this.erasable = isErasable;
	}
	
	public void setShiftErasable(boolean shiftErasable) {
		this.shiftErasable = shiftErasable;
	}
	@SuppressWarnings("unchecked")
	public void setMouseover(boolean isOver) {
		this.mouseover = isOver;
		if (interactions != null) {
			if (isOver) {
				if (this.highlightPropagationType == BasicEntity.HL_PROPAGATION_OUTBOUND) {
					Vector<BasicEntity> linkedEntities = interactions
							.getLinkedOutboundEntities(this);
					for (int i = 0; i < linkedEntities.size(); i++)
						linkedEntities.get(i).setHighlighted(true);
				} else if (this.highlightPropagationType == BasicEntity.HL_PROPAGATION_OUTBOUND_AND_PARENT) {
					Vector<BasicEntity> linkedEntities = interactions
							.getLinkedOutboundEntities(this);
					linkedEntities.addAll(interactions
							.getLinkedSrcEntities(this));
					for (int i = 0; i < linkedEntities.size(); i++)
						linkedEntities.get(i).setHighlighted(true);

				} else if (this.highlightPropagationType == BasicEntity.HL_PROPAGATION_MAX_RANGE) {
					Vector<BasicEntity> linkedEntities = interactions
							.getLinkedEntitiesMaxDist(this, this.maximumPropagationLevel);
					for (int i = 0; i < linkedEntities.size(); i++)
						linkedEntities.get(i).setHighlighted(true);
				} else {
					Vector linked = interactions.getLinkedThruEntities(this);
					for (int ent = 0; ent < linked.size(); ent++)
						if (linked.elementAt(ent) != null)
							((BasicEntity) linked.elementAt(ent)).setHighlighted(isOver);
				}
			} else {
				Vector linked = interactions.getLinkedThruEntities(this);
				for (int ent = 0; ent < linked.size(); ent++)
					if (linked.elementAt(ent) != null)
						((BasicEntity) linked.elementAt(ent)).setHighlighted(isOver);
			}
		}
	}

	public boolean isMouseover() {
		return this.mouseover;
	}

	public void forceMouseover(boolean isOver) {
		this.mouseover = isOver;
	}

	public void setSelected(boolean isSelected) {
		this.selected = isSelected;
	}

	public boolean isSelected() {
		return this.selected;
	}

	public boolean isHighlighted() {
		return this.highlighted;
	}

	public void setHighlighted(boolean isHighlighted) {
		this.highlighted = isHighlighted;
	}

	/**
	 * Returns true if the provided coordinates in global space are contained in
	 * the active area of this entity.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean contains(double x, double y) {
		if (this.sensitiveArea != null)
			return this.sensitiveArea.createTransformedArea(this.globalTrans)
					.contains(x, y);
		else {
			System.out.println("Sensitive area NULL for " + this);
			return false;
		}
	}

	public int getEntityLayer() {
		return this.entityLayer;
	}

	public BasicEntity returnOverEntity(double x, double y, int currentLayer,
			int highestLayer) {
		int thisHighestLayer = highestLayer;
		this.entityLayer = currentLayer;

		BasicEntity latestEntity = null;
		BasicEntity highestEntity = null;

		for (int child = 0; child < this.childEntities.size(); child++) {
			latestEntity = ((BasicEntity) this.childEntities.elementAt(child))
					.returnOverEntity(x, y, currentLayer + 1, thisHighestLayer);

			if (latestEntity != null
					&& latestEntity.getEntityLayer() >= thisHighestLayer) {
				thisHighestLayer = latestEntity.getEntityLayer();
				highestEntity = latestEntity;
			}
		}

		// if (currentLayer >= thisHighestLayer && this.contains(x, y))
		if ((highestEntity == null) && this.contains(x, y)) {
			thisHighestLayer = currentLayer;
			return this;
		}

		return highestEntity;
	}

	/**
	 * Returns a String describing this Entity, a message used when the mouse is
	 * over it. Should be overridden by every extension of this class.
	 * 
	 * @return
	 */
	public String getOverMessage() {
		return "BasicEntity";
	}

	/**
	 * Returns a String specific to this Entity used when the user clicks on it.
	 * Should be overridden by every extension of this class.
	 * 
	 * @return
	 */
	public String getClickedMessage() {
		return "BasicEntity";
	}

	public void paint(Graphics2D g) {
		// System.out.println("painting "+this+"->"+this.getWidth()+",
		// "+this.getHeight()+", "+this.getLocation());
		for (int ent = 0; ent < this.childEntities.size(); ent++) {
			if (!(this.childEntities.elementAt(ent) instanceof BasicDisplay)) {
				g.setTransform(((BasicEntity) this.childEntities.elementAt(ent)).getGlobalTransform());
				if (g.getRenderingHint(RenderingHints.KEY_RENDERING) != null
						&& g.getRenderingHint(RenderingHints.KEY_RENDERING) == RenderingHints.VALUE_RENDER_QUALITY) {
					((BasicEntity) this.childEntities.elementAt(ent)).paintSB(g);
				} else {
					((BasicEntity) this.childEntities.elementAt(ent)).paint(g);
				}
			}
		}

		for (int ent = 0; ent < this.childEntities.size(); ent++) {
			if (this.childEntities.elementAt(ent) instanceof BasicDisplay) {
				g.setTransform(((BasicEntity) this.childEntities.elementAt(ent)).getGlobalTransform());
				if (g.getRenderingHint(RenderingHints.KEY_RENDERING) != null
						&& RenderingHints.KEY_RENDERING == RenderingHints.VALUE_RENDER_QUALITY) {
					((BasicEntity) this.childEntities.elementAt(ent)).paintSB(g);
				} else {
					((BasicEntity) this.childEntities.elementAt(ent)).paint(g);
				}
			}
		}

		g.setTransform(this.getGlobalTransform());
	}

	/**
	 * Sub-classes of BasicEntity can override this method to implement a
	 * totally vectorial paint. By default it just calls paint(Graphics2D g)
	 * 
	 * @param g
	 */
	public void paintSB(Graphics2D g) {
		paint(g);
	}

	public String getXmlTag(Hashtable xmlProperties) {
		return "";
	}

	/**
	 * 
	 * 
	 * @param xmlProperties
	 */
	protected void addSuperToXML(XMLCoDec tempCodec) {
		tempCodec.addParameter("heterolink", new Boolean(this.heterolink));
		tempCodec.addParameter("homolink", new Boolean(this.homolink));
		tempCodec.addParameter("erasable", new Boolean(this.erasable));
		tempCodec.addParameter("highlightPropagationType", new Integer(
				this.highlightPropagationType));
		tempCodec.addParameter("maximumPropagationLevel", new Integer(
				this.maximumPropagationLevel));
	}

	protected void getSuperFromXML(XMLCoDec tempCodec) {
		if (tempCodec.hasParameter("homolink"))
			this.homolink = tempCodec.getParameterAsBoolean("homolink");
		if (tempCodec.hasParameter("heterolink"))
			this.heterolink = tempCodec.getParameterAsBoolean("heterolink");
		if (tempCodec.hasParameter("highlightPropagationType"))
			this.highlightPropagationType = tempCodec.getParameterAsInt("highlightPropagationType");
		if (tempCodec.hasParameter("maximumPropagationLevel"))
			this.maximumPropagationLevel = tempCodec.getParameterAsInt("maximumPropagationLevel");
		if (tempCodec.hasParameter("erasable"))
			this.erasable = tempCodec.getParameterAsBoolean("erasable");
	}

	/**
	 * Stores in the tempCodec the parameters describing the links for this
	 * entity.
	 * 
	 * @param tempCodec
	 */
	protected void addLinkXMLElements(XMLCoDec tempCodec) {
		tempCodec.addParameter("uniqueID", new Integer(this.uniqueID));

		if (interactions != null) {
			Vector<BasicEntity> linkedEntities = interactions.getLinkedDestEntities(this);
			Vector<BasicEntity> hardLinkedEntities = interactions.getLinkedDestEntities(
					this, InteractiveSurface.INTEGRITY_CHECK);

			tempCodec.addParameter("nbLinks",
					new Integer(linkedEntities.size()));
			for (int ent = 0; ent < linkedEntities.size(); ent++) {
				tempCodec.addParameter("link" + ent, new Integer(
						(linkedEntities.elementAt(ent)).getUniqueID()));
				if (hardLinkedEntities.contains(linkedEntities.elementAt(ent)))
					tempCodec.addParameter("linkType" + ent, new Integer(
							InteractiveSurface.INTEGRITY_CHECK));
				else
					tempCodec.addParameter("linkType" + ent, new Integer(0));
			}
		}
	}

	/**
	 * Restores the links for this entity. Normally called by
	 * InteractiveSurface. Should not be used manually.
	 */
	public void restoreLinks() {
		if (linkedIDs != null) {
			for (int lnk = 0; lnk < linkedIDs.length; lnk++) {
				this.interactions.restoreLink(this, linkedIDs[lnk],
						linkTypes[lnk]);
			}
		}

		for (int ent = 0; ent < this.getEntitiesCount(); ent++) {
			((BasicEntity) this.getEntity(ent)).restoreLinks();
		}
	}

	/**
	 * Retrieves the IDs necessary for the reconstitution of the links from the
	 * XMLCODEC.
	 * 
	 * @param tempCodec
	 */
	protected void storeLinkIDs(XMLCoDec tempCodec) {
		int nbLinks = tempCodec.getParameterAsInt("nbLinks");

		if (nbLinks > 0) {
			linkedIDs = new int[nbLinks];
			linkTypes = new int[nbLinks];

			for (int link = 0; link < nbLinks; link++) {
				linkedIDs[link] = tempCodec.getParameterAsInt("link" + link);
				linkTypes[link] = tempCodec
						.getParameterAsInt("linkType" + link);
			}
		}

	}

	public boolean isHomolink() {
		return homolink;
	}

	public void setHomolink(boolean homolink) {
		this.homolink = homolink;
	}

	public boolean isHeterolink() {
		return heterolink;
	}

	public void setHeterolink(boolean heterolink) {
		this.heterolink = heterolink;
	}

	public boolean isLinkable() {
		return isLinkable;
	}

	public void setLinkable(boolean isLinkable) {
		this.isLinkable = isLinkable;
	}

	public int getHighlightPropagationType() {
		return highlightPropagationType;
	}

	public void setHighlightPropagationType(int highlightPropagationType) {
		this.highlightPropagationType = highlightPropagationType;
	}

	public int getMaximumPropagationLevel() {
		return maximumPropagationLevel;
	}

	public void setMaximumPropagationLevel(int maximumPropagationLevel) {
		this.maximumPropagationLevel = maximumPropagationLevel;
	}
	
	public void swapChildEntities(int ent1, int ent2) {
		BasicEntity o = this.childEntities.get(ent1);
		this.childEntities.set(ent1, this.childEntities.get(ent2));
		this.childEntities.set(ent2, o);
	}
	
	public void moveEntityToLastPostion(int ent) {
		BasicEntity o = this.childEntities.get(ent);
		this.childEntities.remove(ent);
		this.childEntities.add(o);
	}
}