package org.cheminfo.scripting.spectradata;

import org.cheminfo.function.Function;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.SmartPeakLabel;
import org.cheminfo.hook.nemo.Spectra;
import org.cheminfo.hook.nemo.SpectraData;
import org.cheminfo.hook.nemo.SpectraDisplay;
import org.cheminfo.hook.nemo.nmr.NmrHelpers;
import org.json.JSONException;
import org.json.JSONObject;

import com.actelion.research.chem.StereoMolecule;

public class AppletNemo extends Function{
	
	private SpectraDisplay spectraDisplay;
	
	/**
	 * @return the spectraDisplay
	 */
	public SpectraDisplay getSpectraDisplay() {
		return spectraDisplay;
	}

	/**
	 * @param spectraDisplay the spectraDisplay to set
	 */
	public void setSpectraDisplay(SpectraDisplay spectraDisplay) {
		this.spectraDisplay = spectraDisplay;
	}

	public SpectraData getSpectraData(String spectraName) {
		Spectra spectra=spectraDisplay.getSpectra(spectraName);
		if (spectra==null) {
			appendWarning("AppletNemo.getSpectraData","spectraData not found for this name: "+spectraName);
			return null;
		}
		return spectra.getSpectraData();
	}
	
	public void plotSpectraData(SpectraData spectraData){
		plotSpectraData(spectraData,"");
	}
	
	public void plotSpectraData(SpectraData spectraData, String spectraName){
		
		appendInfo("AppletNemo.plotSpectraData", "FirstX: "+
				spectraData.getFirstX()+", LastX: "+spectraData.getLastX()+
				" Units: "+spectraData.getXUnits());
		
		int vPosition = spectraDisplay.getNbAddedSpectra() * (-20) - 10;
		if (spectraDisplay.getFirstSpectra() != null && spectraDisplay.getFirstSpectra().isDrawnAs2D()) {
			vPosition = 0;
		}
		Spectra spectra=new Spectra(spectraData);
		spectra.setLocation(spectra.getLocation().x, vPosition);
		spectra.setReferenceName(spectraName);
		spectraDisplay.addSpectra(spectra);
		spectraDisplay.refreshSensitiveArea();
	}
	
	/**
	 * TODO: Rewrite to create the  smartPeakLabels from NMRSignal1D
	 * This function adds a set of smartPeakLabels to the correct spectrum. The smartpeaklabels could be 
	 * obtained for example using the function assignment2H, for simulated spectra.
	 * @param name: Name of the Spectra that contains the corresponding spectraData
	 * @param peaks: Array of smartPeakLabels
	 * @param display: The SpectraDisplay 
	 */
	public void paintSmartPeaks(String name, JSONObject peaks, SpectraDisplay display){
		Spectra spectrum = display.getSpectra(name);
		SmartPeakLabel[] peakList=null;
		try {
			peakList =(SmartPeakLabel[])peaks.get("peaks");
		} catch (JSONException e) {
			e.printStackTrace();
			appendError("AppletNemo.paintSmartPeaks",e.toString());
		}
		for(int i=0;i<peakList.length;i++){
			spectrum.addEntity(peakList[i]);
			peakList[i].setInteractiveSurface(display.getInteractiveSurface());
		}
	}
	
	/**
	 * This function return a array of  SmartPeakLabel for the given spectra. If no spectraName is specified
	 * it will return the SmartPeakLabels of the firstSpectra
	 * @param spectraName
	 * @param ascending
	 * @return SmartPeakLabe[]
	 */
	public SmartPeakLabel[] getSmartLabels(String spectraName, boolean ascending){
		if(spectraName==null)
			spectraName="";
		Spectra spectra=null;
		if(spectraName=="")
			spectra=spectraDisplay.getFirstSpectra();
		else
			spectra=spectraDisplay.getSpectra(spectraName);
		return NmrHelpers.getSmartLabels(spectra, ascending);
	}
	
	/**
	 * This function plots a molecule in the current display
	 * @param molfile: A stereoMolecule or the molfile content.
	 * @param param
	 */
	public void plotMolecule(String molfile, Object param){
		JSONObject parameters = checkParameter(param);
		String name=parameters.optString("name","");
		boolean expandAll = parameters.optBoolean("expand",false);
		int x = parameters.optInt("x",50);
		int y = parameters.optInt("y",50);
		int w = parameters.optInt("w",150);
		int h = parameters.optInt("h",150);
		
		for (int ent = 0; ent < spectraDisplay.getEntitiesCount(); ent++) {
			if (spectraDisplay.getEntity(ent) instanceof ActMoleculeDisplay){
				if(spectraDisplay.getEntity(ent).getEntityName()==name)
					((ActMoleculeDisplay) spectraDisplay.getEntity(ent)).delete();
			}	
		}
		
		ActMoleculeDisplay molDisplay = new ActMoleculeDisplay();
		molDisplay.setEntityName(name);
		molDisplay.setLocation(x, y);
		molDisplay.setMovementType(BasicEntity.GLOBAL);
		molDisplay.setErasable(true);
		spectraDisplay.addEntity(molDisplay,0);
		molDisplay.init(w, h);

		molDisplay.addMolfile(molfile, expandAll);
	}
	/**
	 * This function returns the EnhancedMolecule associated to this name
	 * @param name
	 * @return EnhancedMolecule, return null if no ActMoleculeDisplay with this name exist within spectraDisplay 
	 */
	public StereoMolecule getStereoMolecule(String name){
		for (int ent = 0; ent < spectraDisplay.getEntitiesCount(); ent++) {
			if (spectraDisplay.getEntity(ent) instanceof ActMoleculeDisplay){
				if(spectraDisplay.getEntity(ent).getEntityName()==name)
					((ActMoleculeDisplay) spectraDisplay.getEntity(ent)).getEnhancedMolecule();
			}	
		}
		return null;
	}
}
