package org.cheminfo.hook.nemo;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;

public class IntegralAction extends GeneralAction {
	public static void performAction(InteractiveSurface interactions) {

		if (interactions != null) {
			interactions.takeUndoSnapshot();

			if (interactions.getActiveEntities().size() == 1
					&& interactions.getActiveEntity() instanceof Integral) {
				Integral tempIntegral = (Integral) interactions
						.getActiveEntity();
				try {
					double newRelArea = (double) new DecimalFormat().parse(
							interactions.getUserDialog()
									.getParameter("relArea")).doubleValue();
					IntegrationHelpers.setNewRefIntegral((Spectra) tempIntegral
							.getParentEntity(), tempIntegral, newRelArea);
					tempIntegral.setPublicationValue(interactions.getUserDialog().getParameter("publicationValue"));
					interactions.repaint();
					interactions.getUserDialog().setText("");
				} catch (ParseException ex) {
					interactions.getUserDialog().setText(
							"Number format exception");
				}
			}
		}
	}
}