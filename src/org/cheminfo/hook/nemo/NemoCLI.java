package org.cheminfo.hook.nemo;

import java.net.MalformedURLException;
import java.net.URL;

import org.cheminfo.hook.converter.Converter;

public class NemoCLI
{
	
	public NemoCLI()
	{
	}

	/**
	* Method to add a new spectrum to myDisplay loading it from a jdx file
	* pointed by the urlString.
	* The urlString may be relative or absolute.
	* Return false if file not found.
	*/
	public boolean loadSpectrum(String stringUrl) {
		Converter tempConverter=Converter.getConverter("Jcamp");
		SpectraData	spectraData = new SpectraData();
		URL correctUrl=null;
		// we need the URL
		// we first try to convert it directly
		try {
			correctUrl=new URL(stringUrl);
		} catch (MalformedURLException e) {
			System.out.println("Not found : "+stringUrl);
			return false;
		}
		
		System.out.println("Loading from "+String.valueOf(correctUrl));

		if (tempConverter == null) System.out.println("Converter -> null");
		
		if (tempConverter.convert(correctUrl,spectraData))
		{
			spectraData.setActiveElement(0);
	
		}
		else 
		{
			System.out.println("Could Not Access File "+correctUrl);
			return false;
		}
		System.out.println("Datatype: "+spectraData.getDataType());
		return true;
	}

	
	
	public void main (String[] args) {
		NemoCLI nemo=new NemoCLI();
		nemo.loadSpectrum(args[0]);
		
	}
	
}

