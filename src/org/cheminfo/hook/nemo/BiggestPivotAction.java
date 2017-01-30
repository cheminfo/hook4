package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;

public class BiggestPivotAction extends GeneralAction {
	public static void performAction(InteractiveSurface interactions) {
		DefaultActionButton button = interactions.getButtonByClassName("org.cheminfo.hook.nemo.ManualPhaseCorrectionActionButton");
		if (button != null && interactions.getCurrentAction() == button) {
			((ManualPhaseCorrectionActionButton)button).selectBiggestPivot();
		}
	}
}
