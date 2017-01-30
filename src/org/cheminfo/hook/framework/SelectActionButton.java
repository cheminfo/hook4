package org.cheminfo.hook.framework;

import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class SelectActionButton extends DefaultActionButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6418869018290889974L;
	String infoMessage = "Select";
	int buttonType = ImageButton.RADIOBUTTON;
	Point contact, release;
	BasicEntity movingEntity = null;
	char [] shortcutKey = {'s','S'};

	private double originalWidth, originalHeight;

	public SelectActionButton() {
		super();
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}

	public SelectActionButton(Image inImage) {
		super(inImage);
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}

	public SelectActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON, new char[] {'a','A'});
		this.contact = new Point();
		this.release = new Point();
	}
	
	/*
	public SelectActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage);

		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);

		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);

		this.contact = new Point();
		this.release = new Point();
	}
	*/

	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);
	}

	public void mouseClicked(MouseEvent ev) {
		if (this.interactions != null) {
			if (ev.isShiftDown() == false) {
				interactions.clearActiveEntities();
			}
		}
		BasicEntity tempEntity = interactions.getOverEntity();
		if (ev.getClickCount() == 2 && tempEntity != null) {
			BasicEntity tempParent = tempEntity.getParentEntity();
			if (tempParent != null) {
				for (int ent = 0; ent < tempParent.getEntitiesCount(); ent++) {
					if (tempParent.getEntity(ent).getClass()
							.getName() == tempEntity.getClass()
							.getName()) {
						interactions.addActiveEntity(tempParent
								.getEntity(ent));
						interactions.getUserDialog().setText("");
					}
				}
			}
		} else if (ev.getClickCount() == 1) {
			interactions.addActiveEntity(interactions
					.getOverEntity());
			if (interactions.getOverEntity() != null) {
				if (ev.isShiftDown()) {
					setUserDialogText(interactions.getOverEntity()
							.getOverMessage());
				} else {
					setUserDialogText(interactions.getOverEntity()
						.getClickedMessage());
				}
			}
		}
		interactions.repaint();
	}
	
	protected void handleEvent(MouseEvent ev) {
		boolean needsRepaint = false;
		// super.handleEvent(ev);

		if (interactions.getCurrentAction() == this) {
			if (interactions.getOverEntity() instanceof EntityResizer || this.movingEntity instanceof EntityResizer) {
				BasicEntity parentEntity = null;
				if (interactions.getOverEntity() instanceof EntityResizer)
					parentEntity = interactions.getOverEntity().getParentEntity();
				else
					parentEntity = this.movingEntity.getParentEntity();

				switch (ev.getID()) {
				case MouseEvent.MOUSE_PRESSED:
					this.movingEntity = interactions.getOverEntity();
					this.originalWidth = parentEntity.getWidth();
					this.originalHeight = parentEntity.getHeight();
					break;

				case MouseEvent.MOUSE_DRAGGED:
					if (this.movingEntity == parentEntity || this.movingEntity instanceof EntityResizer) {
						parentEntity.setSize(
										originalWidth + (interactions
														.getReleasePoint().x - interactions
														.getContactPoint().x),
										originalHeight
												+ (interactions
														.getReleasePoint().y - interactions
														.getContactPoint().y));
						parentEntity.checkSizeAndPosition();
						interactions.repaint();
					}
					break;

				case MouseEvent.MOUSE_RELEASED:
					parentEntity.refreshSensitiveArea();
					this.movingEntity = null;
					break;

				default:
					break;
				}
			} else {
				switch (ev.getID()) {
				case MouseEvent.MOUSE_EXITED:
					interactions.getUserDialog().clearText();
					break;

				case MouseEvent.MOUSE_CLICKED:
					mouseClicked(ev);
					break;

				case MouseEvent.MOUSE_DRAGGED:
					// if (interactions.getOverEntity().isSelected())
				{

					this.release = ev.getPoint();
					double deltaX, deltaY;

					deltaX = release.x - contact.x;
					deltaY = release.y - contact.y;
					//System.out.println(deltaX+" "+deltaY);
					if (interactions.getActiveEntities().size() > 1) {
						for (int entity = 0; entity < interactions.getActiveEntities().size(); entity++) {
							BasicEntity currentEntity = (BasicEntity) interactions.getActiveEntities().elementAt(entity);
							// Point2D.Double
							// position=currentEntity.getLocation();
							switch (currentEntity.getMovementType()) {
							case BasicEntity.GLOBAL:
								// currentEntity.setLocation(position.x+this.release.x-this.contact.x,
								// position.y+this.release.y-this.contact.y);
								break;

							case BasicEntity.VERTICAL:
								currentEntity.moveLocal(0, deltaY);
								// currentEntity.setLocation(position.x,
								// position.y+this.release.y-this.contact.y);
								break;

							case BasicEntity.HORIZONTAL:
								currentEntity.moveLocal(deltaX, 0);
								// currentEntity.setLocation(position.x+this.release.x-this.contact.x,
								// position.y);
								break;

							default:
								break;
							}
						}
					} else if (this.movingEntity != null) {
						switch (this.movingEntity.getMovementType()) {
						case BasicEntity.GLOBAL:
							movingEntity.moveLocal(deltaX, deltaY);
							break;

						case BasicEntity.VERTICAL:
							movingEntity.moveLocal(0, deltaY);
							break;

						case BasicEntity.HORIZONTAL:
							movingEntity.moveLocal(deltaX, 0);
							break;

						default:
							break;
						}
					}
					
					if (this.movingEntity != null && this.interactions.findParentDisplay(this.movingEntity) != null)
						this.interactions.findParentDisplay(this.movingEntity).checkSizeAndPosition();
					
					interactions.repaint();
					this.contact = ev.getPoint();
				}
					break;

				case MouseEvent.MOUSE_PRESSED:
					this.contact = ev.getPoint();
					this.movingEntity = interactions.getOverEntity();
					interactions.takeUndoSnapshot();
					break;

				case MouseEvent.MOUSE_RELEASED:
					if (movingEntity != null) {
						this.movingEntity.reactOnRelease();
						this.movingEntity = null;
					}
					break;

				default:
					break;
				}
			}
		}
	}

	/** Key event will deal with space that allows to select the next element of the same type */
	public void handleKeyEvent(KeyEvent ev) {
		super.handleKeyEvent(ev);
		// we can not use tab key because it is taken by the focus manager
		// with SHIFT we should find the previous one ...
		if (ev.getID()==KeyEvent.KEY_RELEASED && ev.getKeyCode()==KeyEvent.VK_SPACE) {
			BasicEntity activeEntity = interactions.getActiveEntity();
			if (activeEntity != null) {
				BasicEntity nextActiveEntity=null;
				BasicEntity previousActiveEntity=null;
				boolean foundCurrent=false;
				boolean foundPrevious=false;
				boolean foundNext=false;
				BasicEntity parentEntity=activeEntity.getParentEntity();
				Class currentClass=activeEntity.getClass();
				if (parentEntity != null) {
					for (BasicEntity entity: parentEntity.getEntities()) {
						if (entity.getClass().equals(currentClass)) {
							if (nextActiveEntity==null) nextActiveEntity=entity;
							if ((foundCurrent) && (! foundNext)) {
								nextActiveEntity=entity;
								foundNext=true;
							}
							if ((! foundCurrent) && (entity==activeEntity)) {
								foundCurrent=true;
							}
							if ((entity==activeEntity) && (previousActiveEntity!=null)) {
								foundPrevious=true;
							}
							if (! foundPrevious) {
								previousActiveEntity=entity;
							}
						}
					}
					if (ev.isShiftDown()) {
						interactions.setActiveEntity(previousActiveEntity);
					} else {
						interactions.setActiveEntity(nextActiveEntity);
					}
					interactions.repaint();
				}
			}
		}
	}
	
	protected void checkButtonStatus() {
		super.checkButtonStatus();
		if (interactions.getCurrentAction() == this) {
			this.setStatus(DefaultActionButton.DOWN);
		} else {
			this.setStatus(DefaultActionButton.UP);
		}
	}

	protected int getGroupNb() {
		return this.groupNb;
	}
}