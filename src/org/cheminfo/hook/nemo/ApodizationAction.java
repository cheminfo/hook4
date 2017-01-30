package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.filters.ApodizationFilter;
import org.cheminfo.hook.nemo.filters.BrukerSpectraFilter;
import org.cheminfo.hook.nemo.filters.CircularShiftFilter;

public class ApodizationAction extends GeneralAction {
	public static void performAction(InteractiveSurface interactions) {
		if (interactions.getActiveEntity() instanceof Spectra) {
			Spectra spectrum = (Spectra) interactions.getActiveEntity();
			SpectraData spectraData = spectrum.getSpectraData();
			
			
			String input = interactions.getUserDialog().getParameter("zeropadding").trim();
			if(input.compareTo("+")==0){
				BrukerSpectraFilter brukerFilter = new BrukerSpectraFilter();
				if (brukerFilter.isApplicable(spectraData)) {
					spectraData.applyFilter(brukerFilter);
					input="";
				} else {
					input = "Bruker digital filter not applicable";
				}
			}
			else{
				int circularShift = 0;
				if(input.compareTo("-")==0)
					circularShift = -(int)spectraData.getParamDouble("$$ZEROPADDING", 0);
				else{
					if(isNumeric(input)){
						circularShift =(int)Double.parseDouble(input);
					}
				}
				if(circularShift!=0){
					CircularShiftFilter csFilter = new CircularShiftFilter(circularShift);
					if (csFilter.isApplicable(spectraData)) {
						spectraData.applyFilter(csFilter);
						input="";
					} else {
						input = "Circular shift filter not applicable";
					}
				}
			}
			
			ApodizationFilter apodizationFilter = new ApodizationFilter();
			apodizationFilter.setLineBroadening(Double.parseDouble(interactions.getUserDialog().getParameter("lineBroadening").trim()));
			//apodizationFilter.setLineBroadening(Double.parseDouble("1.0"));
			System.out.println("line B"+interactions.getUserDialog().getParameter("lineBroadening").trim());
			System.out.println("funct "+interactions.getUserDialog().getParameter("function"));
			apodizationFilter.setApodizationFunction(interactions.getUserDialog().getParameter("function"));
			if (apodizationFilter.isApplicable(spectraData)) {
				spectraData.applyFilter(apodizationFilter);
				interactions.getUserDialog().setText(""+input);
			} else {
				interactions.getUserDialog().setText("Apodization not applicable "+input);
			}
			spectrum.checkSizeAndPosition();
			spectrum.refreshSensitiveArea();
			BasicEntity parentEntity = spectrum.getParentEntity();
			if (parentEntity != null) {
				parentEntity.checkInteractiveSurface();
				spectrum.checkSizeAndPosition();
			}
			interactions.repaint();
			interactions.setActiveEntity(spectrum);
		}
	}
	
	 public static boolean isNumeric( String input )  
	 {  
	    try  
	    {  
	       Double.parseDouble(input);  
	       return true;  
	    } 
	    catch(NumberFormatException e){
	    	return false;
	    }
	 }
	    

}
