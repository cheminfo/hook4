package com.actelion.research.nemo.jcamp;

import java.util.Date;

public class LdrFactory {
	
	
	public static Ldr<?> createFromRaw(String raw)
	{
		Label label = Label.fromString(raw);
		
		try {
			//based on label
			if(label.equals(Label.BRUKER_MEASUREMENT_DATE) || label.equals(Label.VARIAN_MEASUREMENT_DATE))
				return new DateLdr(raw);
			else if(label.equals(Label.ACT_NEMOVIEW))
				return new Base64Ldr(raw);
		}
		catch(RuntimeException e)
		{			
			System.out.println(e.getMessage());
		}

		return new StringLdr(raw);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Ldr<T> createFromLabelValue(Label label, T value)
	{
		//based on label
		if(label.equals(Label.ACT_NEMOVIEW))
			return (Ldr<T>) new Base64Ldr(label, (String)value);
		//based on type
		else if(value instanceof Date)
			return (Ldr<T>) new DateLdr(label, (Date)value);
		else
			return (Ldr<T>) new StringLdr(label, (String)value);
	}
	
}
