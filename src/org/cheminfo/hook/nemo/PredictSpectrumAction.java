package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;

public class PredictSpectrumAction extends GeneralAction {
	public static void performAction(InteractiveSurface interactions) {
//		BasicEntity entity = interactions.getEntityByName("molDisplay");
//		if (entity != null) {
//			String nucleus = interactions.getUserDialog().getParameter(
//					"nucleus");
//			NMRPredictionClient predictionClient = new NMRPredictionClient(
//					interactions, nucleus);
//			predictionClient.setReturnIncomplete(true);
//			if (predictionClient.predictNMRSpectrum()) {
//				if (!predictionClient.isTwoDimensional()) {
//					String prediction = predictionClient.getOneDimPrediction();
//					((Nemo) interactions.getParent())
//							.addSimulatedSpectrum(prediction);
//				}
//				interactions.getUserDialog().setText("");
//			} else {
//				interactions.getUserDialog().setText(
//						predictionClient.getErrorString());
//			}
//			interactions.repaint();
//		}
	}

}
