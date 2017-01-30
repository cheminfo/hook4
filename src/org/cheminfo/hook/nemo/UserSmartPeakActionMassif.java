package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;

public class UserSmartPeakActionMassif extends GeneralAction {
	private static final boolean isDebug = false;
	
	public static void performAction(InteractiveSurface interactions) {
		//System.out.println("Hereee");
		if (interactions != null) {
			interactions.takeUndoSnapshot();
			if (interactions.getCurrentAction() instanceof ModifySmartPeakLabelActionButton) {
				ModifySmartPeakLabelActionButton button = (ModifySmartPeakLabelActionButton) interactions
						.getCurrentAction();
				button.cleanUp();
				if (isDebug)
					System.out.println("Setting action");
				interactions
						.setCurrentAction(interactions.getButtonByClassName("org.cheminfo.hook.nemo.SmartPickingActionButton"));
			}

			if (interactions.getActiveEntities().size() == 1
					&& interactions.getActiveEntity() instanceof SmartPeakLabel) {
				SmartPeakLabel peakLabel = (SmartPeakLabel)interactions.getActiveEntity();
				peakLabel.switchForceMassifState();
				interactions.getUserDialog().setText("");
			}
		} else {
			System.out.println("YOOP SmartPeakLabel interactions is null");
		}
		interactions.repaint();
	}

}