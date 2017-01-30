package org.cheminfo.hook.nemo;

import java.awt.Image;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.util.XMLCoDec;

public class ShowGaussianActionButton extends DefaultActionButton {

	public ShowGaussianActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 0, ImageButton.CHECKBUTTON);
	}

	protected void performInstantAction() {
		interactions.takeUndoSnapshot();
		
		if (interactions.getActiveEntity() instanceof Spectra) {
			Spectra activeSpectra = (Spectra) interactions.getActiveEntity();
			if (activeSpectra.getSpectraData().getDataClass(0)==SpectraData.DATACLASS_PEAK) {
				if (activeSpectra.getSpectraData().getNbSubSpectra() == 1) {
					if (this.getStatus()==DefaultActionButton.DOWN) {
						activeSpectra.isSmooth(true);
						showUserDialog();
					} else if (this.getStatus()==DefaultActionButton.UP) {
						activeSpectra.isSmooth(false);
						interactions.getUserDialog().setText("");
					}
					interactions.repaint();
				}
			}
		}
	}

	private void showUserDialog() {
		XMLCoDec inputCodec = new XMLCoDec();
		inputCodec.addParameter("name", "peakWidth");
		inputCodec.addParameter("size", new Integer(10));

		XMLCoDec inputCodec2 = new XMLCoDec();
		inputCodec2.addParameter("name", "nbPoints");
		inputCodec2.addParameter("size", new Integer(10));
		
		XMLCoDec inputCodec3 = new XMLCoDec();
		inputCodec3.addParameter("name", "globalShift");
		inputCodec3.addParameter("size", new Integer(5));

		XMLCoDec buttonCodec = new XMLCoDec();
		buttonCodec.addParameter("action",
				"org.cheminfo.hook.nemo.GaussianAction");
		buttonCodec.addParameter("image", "validate.gif");
		
		interactions
				.getUserDialog()
				.setText(
						"<Text type=\"plain\">Nb Points: </Text><Input "
								+ inputCodec2.encodeParameters()
								+ ">32768</Input><Text type=\"plain\"> PeakWidth: </Text><Input "
								+ inputCodec.encodeParameters()
								+ ">0.1</Input><Text type=\"plain\"> Global Shift: </Text><Input "
								+ inputCodec3.encodeParameters()
								+ ">0.0</Input><Button "
								+ buttonCodec.encodeParameters()+ "></Button>"
								);
	}


	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
			if (interactions.getActiveEntity() != null && interactions.getActiveEntity() instanceof Spectra) {
				Spectra tempSpectra = (Spectra) interactions.getActiveEntity();
				if (tempSpectra.getSpectraData().getDataClass(0) == SpectraData.DATACLASS_PEAK) {
					this.activate();
				} else
					this.deactivate();
			} else
				this.deactivate();
		} else
			this.deactivate();

		if (this.getStatus() != DefaultActionButton.INACTIVE) {
			Spectra activeSpectra = (Spectra) interactions.getActiveEntity();
			if (activeSpectra.getSpectraData().isFakePeaks()) {
				if (activeSpectra.isSmooth())
					this.setStatus(DefaultActionButton.DOWN);
				else
					this.setStatus(DefaultActionButton.UP);
			} else {
				if (activeSpectra.getSpectraData().isDataClassXY() && activeSpectra.isSmooth()) {
					this.setStatus(DefaultActionButton.DOWN);
				} else {
					this.setStatus(DefaultActionButton.UP);
				}
			}
		}
	}

}
