package org.cheminfo.hook.nemo;

import org.cheminfo.hook.nemo.nmr.IntegralData;

public class IntegrationHelpers
{
			
	public static Integral addIntegral(double startX, double stopX, Spectra spectra)
	{
		Integral tempIntegral=new Integral(startX,stopX,spectra);
		
		spectra.addEntity(tempIntegral, 0);
		
		tempIntegral.setPrimaryColor(Spectra.DEFAULT_INTEGRAL_COLOR);
		
		return tempIntegral;
	}
	public static Integral addIntegral(IntegralData integral, Spectra spectra)
	{
		Integral tempIntegral=new Integral(integral.getFrom(),integral.getTo(),spectra);
		//tempIntegral.setPublicationValue(""+integral.getValue());
		if (getIntegralNb(spectra) == 0) spectra.setIntegralsBaseArea(tempIntegral.getArea());
		spectra.addEntity(tempIntegral, 0);
		
		tempIntegral.setPrimaryColor(Spectra.DEFAULT_INTEGRAL_COLOR);
		
		return tempIntegral;
	}
	
	
	protected Integral addIntegral(double startX, double stopX, double startY, double stopY, Spectra spectra)
	{
		Integral tempIntegral=new Integral(startX, stopX, startY, stopY, spectra);
		if (getIntegralNb(spectra) == 0) spectra.setIntegralsBaseArea(tempIntegral.getArea());
		spectra.addEntity(tempIntegral, 0);
		
		tempIntegral.setPrimaryColor(Spectra.DEFAULT_INTEGRAL_COLOR);
		
		return tempIntegral;
	}
	
	protected void addIntegral(Integral newIntegral, Spectra spectra)
	{
		spectra.addEntity(newIntegral);
	}	

	public static int getIntegralNb(Spectra spectra)
	{
		int integralNb=0;
		for (int ent=0; ent < spectra.getEntitiesCount(); ent++)
		{
			if (spectra.getEntity(ent) instanceof Integral)
				integralNb++;
		}
		
		return integralNb;
	}
	
	public static void setNewRefIntegral(Spectra spectra, Integral refIntegral, double newRelArea)
	{
		double newFactor=newRelArea/refIntegral.getRelArea();
		spectra.setIntegralsBaseArea(spectra.getIntegralsBaseArea()/newFactor);
		spectra.setIntegralsMultFactor(spectra.getIntegralsMultFactor()/newFactor);
		
		for (int ent=0; ent < spectra.getEntitiesCount(); ent++)
		{
			if (spectra.getEntity(ent) instanceof Integral)
				((Integral)spectra.getEntity(ent)).
				getIntegralData().setBaseArea(spectra.getIntegralsBaseArea());		
		}
	}
}