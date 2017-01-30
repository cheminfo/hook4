package org.cheminfo.hook.framework;


/**
 * This interface can be used to create a more sophisticated link creation policy. 
 * 
 * @author engeler
 *
 */
public interface LinkCreationHandler {
	public void handleLinkCreation(InteractiveSurface interactiveSurface,
			BasicEntity entityA, BasicEntity entityB);
	public void handleLinkDeletion(InteractiveSurface interactiveSurface,
			BasicEntity entityA, BasicEntity entityB);


}
