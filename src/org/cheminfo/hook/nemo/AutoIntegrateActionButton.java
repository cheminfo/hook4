package org.cheminfo.hook.nemo;

import java.awt.Image;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.util.XMLCoDec;

public class AutoIntegrateActionButton extends DefaultActionButton
{
	
//	String infoMessage="Auto-Integrate";
//	int buttonType=ImageButton.CLASSIC;
	
	/*
	public AutoIntegrateActionButton()
	{
		super();
		this.setButtonType(ImageButton.CLASSIC);
		this.setGroupNb(0);
	}

	public AutoIntegrateActionButton(Image inImage)
	{
		super(inImage);
		this.setButtonType(ImageButton.CLASSIC);
		this.setGroupNb(0);
	}
*/
	public AutoIntegrateActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 0, ImageButton.CLASSIC);
		/*
		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);
		
		this.setButtonType(ImageButton.CLASSIC);
		this.setGroupNb(0);
		*/
	}

	protected void performInstantAction()
	{
		super.performInstantAction();
		interactions.setCurrentAction(this);

		if (interactions.getActiveDisplay() != null)
		{
//			((SpectraDisplay)interactions.getActiveDisplay()).getFirstSpectra().click();
			interactions.setActiveEntity(((SpectraDisplay)interactions.getActiveDisplay()).getFirstSpectra());
		}

		XMLCoDec inputCodec1 = new XMLCoDec();
		inputCodec1.addParameter("name", "peakWidth");
		inputCodec1.addParameter("size", new Integer(3));
		
		XMLCoDec inputCodec2 = new XMLCoDec();
		inputCodec2.addParameter("name", "peakThreshold");
		inputCodec2.addParameter("size", new Integer(3));
		
		XMLCoDec buttonCodec=new XMLCoDec();
		buttonCodec.addParameter("action", "org.cheminfo.hook.nemo.AutoIntegrateAction");
		buttonCodec.addParameter("image", "validate.gif");
		
		XMLCoDec buttonCodec2=new XMLCoDec();
		buttonCodec2.addParameter("action", "org.cheminfo.hook.nemo.NoisePeakAction");
		buttonCodec2.addParameter("image", "validate.gif");

		interactions.getUserDialog().setText("<Text type=\"plain\">Peak Width: </Text><Input "+inputCodec1.encodeParameters()+">"+((Spectra)interactions.getActiveEntity()).getSpectraData().getSubParamDouble("peakWidth",0.01)+"</Input><Text type=\"plain\"> Peak Threshold: </Text><Input "+inputCodec2.encodeParameters()+">"+((Spectra)interactions.getActiveEntity()).getSpectraData().getSubParamDouble("peakThreshold",5000)+"</Input><Text type=\"plain\"> </Text><Button "+buttonCodec.encodeParameters()+"></Button><Button "+buttonCodec2.encodeParameters()+"></Button>");
	}

/*	protected void handleEvent(MouseEvent ev)	
	{
		if (ev.getID() == MouseEvent.MOUSE_CLICKED)
		{

// changed by LP
			Spectra tempSpectra=interactions.getActiveSpectra();
			if (tempSpectra!=null)
			{
				int dataType=tempSpectra.getSpectraData().getDataType();
				if ((dataType == SpectraData.TYPE_GC) ||
					(dataType == SpectraData.TYPE_HPLC) ||
					(dataType == SpectraData.TYPE_IR) ||
					(dataType == SpectraData.TYPE_MASS) ||
					(dataType == SpectraData.TYPE_NMR_SPECTRUM) ||
					(dataType == SpectraData.TYPE_NMR_FID) ||
					(dataType == SpectraData.TYPE_UV))
					this.activate();
				else this.deactivate();
//			if (interactions.getEntitiesVector().size() > 0 && (interactions.getEntitiesVector().elementAt(0) instanceof Spectra || ((BasicEntity)interactions.getEntitiesVector().elementAt(0)).getParent() instanceof Spectra))// && (interactions.getActiveSpectra().getSpectraData().getDataType() == SpectraData.TYPE_GC || interactions.getActiveSpectra().getSpectraData().getDataType() == SpectraData.TYPE_HPLC))
				
			}
			else
				this.deactivate();
		}
	}
*/
	protected void checkButtonStatus()
	{
		Spectra tempSpectra=null;
		
		if (interactions.getActiveEntity() instanceof Spectra)
			tempSpectra=(Spectra)interactions.getActiveEntity();
		
		if (tempSpectra!=null)
		{
			int dataType=tempSpectra.getSpectraData().getDataType();
			if ((dataType == SpectraData.TYPE_GC) ||
				(dataType == SpectraData.TYPE_HPLC) ||
				(dataType == SpectraData.TYPE_IR) ||
				(dataType == SpectraData.TYPE_MASS) ||
				(dataType == SpectraData.TYPE_NMR_SPECTRUM) ||
				(dataType == SpectraData.TYPE_NMR_FID) ||
				(dataType == SpectraData.TYPE_UV))
				this.activate();
			else this.deactivate();
		}
		else
			this.deactivate();
	}
	
/*	protected int getGroupNb()
	{
		return this.groupNb;
	}
*/
}