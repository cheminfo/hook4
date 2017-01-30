package org.cheminfo.hook.nemo;

import java.util.Hashtable;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;

public class UserSmartPeakAction extends GeneralAction {
	private static final boolean isDebug = false;
	
	public static void performAction(InteractiveSurface interactions) {
		//System.out.println("Here UserSmartPeakAction performAction");
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
				SmartPeakLabel tempLabel = (SmartPeakLabel) interactions.getActiveEntity();
				tempLabel.getNmrSignal1D().setPublicationType(interactions.getUserDialog().getParameter("publicationType"));
				tempLabel.getNmrSignal1D().setPublicationAssignment(interactions.getUserDialog().getParameter("publicationAssignment"));
				if (interactions.getUserDialog().getParameter("type")
						.compareTo("m") == 0) {
					// int[] pattern = {0,0,0,0,0}; OLD
					int[] pattern = { 0, 0, 0, 0, 0, 0, 0, 0 };

					tempLabel.getNmrSignal1D().setType(pattern);
					tempLabel.resetConstants();
				} else {
					int[] pattern = { 0, 0, 0, 0, 0, 0, 0, 0 };

					tempLabel.getNmrSignal1D().setType(pattern);
					tempLabel.resetConstants();
					
					pattern = parsePattern(interactions.getUserDialog()
							.getParameter("type"));

					int requiredNbPeaks = 1;
					// for (int i=0; i<4; i++)
					for (int i = 0; i < pattern.length; i++)
						requiredNbPeaks *= pattern[i];

					if (isDebug)System.out.println("required Peaks: " + requiredNbPeaks);
					if (isDebug)System.out.println("Present Peaks: "
							+ tempLabel.getNmrSignal1D().getNbPeaks());
					int nbPeaksToAdd = requiredNbPeaks - tempLabel.getNmrSignal1D().getNbPeaks();

					if (requiredNbPeaks != tempLabel.getNmrSignal1D().getNbPeaks()) {

						int[] array = new int[tempLabel.getNmrSignal1D().getNbPeaks()];
						SmartPeakLabel testLabel = fillArray(array,
								nbPeaksToAdd, 1, tempLabel, pattern);

						if (testLabel == null)
							System.out.println("NULL");
						else {
							System.out.println("type found: "
									+ testLabel.getNmrSignal1D().getType());
							tempLabel.copyPeaks(testLabel);
							// if (tempLabel.verifyPattern(pattern, true)) OLD
							if (tempLabel.getNmrSignal1D().verifyPatternEx(pattern, true))
								tempLabel.getNmrSignal1D().setType(pattern);
						}
						// SmartPeakLabel testLabel=new
						// SmartPeakLabel(tempLabel.getXmlTag(helpersForGet),
						// helpersForConstr);
						// testLabel.setType(pattern);
						// testLabel.autoAddMissingPeaks();
					} else {
						// if (tempLabel.verifyPattern(pattern, true)) OLD
						if (tempLabel.getNmrSignal1D().verifyPatternEx(pattern, true))
							tempLabel.getNmrSignal1D().setType(pattern);
					}
				}

				tempLabel.setComment(interactions.getUserDialog().getParameter(
						"comment"));

				interactions.getUserDialog().setText(
						((SmartPeakLabel) interactions.getActiveEntity())
								.getOverMessage());
			}
		} else {
			System.out.println("YOOP SmartPeakLabel interactions is null");
		}
	}

	public static int[] parsePattern(String patternString) {
		int[] pattern = { 1, 1, 1, 1, 1, 1, 1, 1 };

		patternString = patternString.replaceAll("deca", "10");
		patternString = patternString.replaceAll("hept", "07");
		patternString = patternString.replaceAll("quint", "05");
		patternString = patternString.replaceAll("d", "02");
		patternString = patternString.replaceAll("t", "03");
		patternString = patternString.replaceAll("q", "04");
		patternString = patternString.replaceAll("h", "06");
		patternString = patternString.replaceAll("o", "08");
		patternString = patternString.replaceAll("n", "09");
		patternString = patternString.replaceAll("m", "00");

		patternString = patternString.replaceAll(" ", "");

		for (int index = 0; index < patternString.length() / 2; index++) {
			
			pattern[pattern.length - 1 - index] = (new Integer(patternString
					.substring((patternString.length() - 2 * index - 2),
							patternString.length() - 2 * index)).intValue());
		}
		
		return pattern;
	}

	private static SmartPeakLabel fillArray(int[] array, int passesLeft,
			int index, SmartPeakLabel tempLabel, int[] pattern) {
		for (int i = index; i < array.length - 1; i++) {
			int[] thisArray = new int[array.length];
			for (int a = 0; a < array.length; a++)
				thisArray[a] = array[a];

			thisArray[i]++;
			if (passesLeft != 1) {
				if (fillArray(thisArray, passesLeft - 1, i, tempLabel, pattern) != null)
					return fillArray(thisArray, passesLeft - 1, i, tempLabel,
							pattern);
			} else {
				for (int x = 0; x < thisArray.length; x++)
					System.out.print(thisArray[x] + " ");
				System.out.println();

				if (isSymmetric(thisArray)) {
					// do the real thing
					Hashtable helpersForGet = new Hashtable();
					Hashtable helpersForConstr = new Hashtable();
					helpersForConstr.put("currentSpectra", (Spectra) tempLabel
							.getParentEntity());

					SmartPeakLabel testLabel = new SmartPeakLabel(tempLabel
							.getXmlTag(helpersForGet), helpersForConstr);
					testLabel.resetConstants();
					System.out.println("ArrayLength: " + thisArray.length
							+ ", tempLabel nbPeaks: " + tempLabel.getNmrSignal1D().getNbPeaks());
					double x;
					for (int j = 0; j < thisArray.length; j++) {
						System.out.println("J: " + j + " -> " + thisArray[j]);
						for (int peak = 0; peak < thisArray[j]; peak++) {
							System.out.println("adding peak like " + j);
							x=tempLabel.getNmrSignal1D().getPeak(j).getX();
							testLabel.getNmrSignal1D().addPeak(x, 0);
						}
					}
					tempLabel.getNmrSignal1D().sortPeaks();
					System.out.println("testLabel has "
							+ testLabel.getNmrSignal1D().getNbPeaks() + " peaks now");
					// if (testLabel.verifyPattern(pattern, true)) OLD
					if (testLabel.getNmrSignal1D().verifyPatternEx(pattern, true)) {
						testLabel.getNmrSignal1D().setType(pattern);
						return testLabel;
					} else
						System.out.println("Pattern Not GOOD");
				}
			}
		}
		return null;
	}

	private static boolean isSymmetric(int[] array) {
		boolean isSym = true;
		for (int i = 0; i < array.length / 2; i++) {
			if (array[i] != array[array.length - i - 1]) {
				isSym = false;
				break;
			}
		}

		return isSym;
	}
}