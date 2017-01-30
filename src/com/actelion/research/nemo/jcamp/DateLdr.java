package com.actelion.research.nemo.jcamp;

import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateLdr extends Ldr<Date> {
	private static DateTimeFormatter dtfVarian = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss");
	private static DateTimeFormatter dtfJcampLongDate1 = DateTimeFormat.forPattern("yyyy/MM/dd");
	private static DateTimeFormatter dtfJcampLongDate2 = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
	private static DateTimeFormatter dtfJcampLongDate3 = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss.SSSS");
	private static DateTimeFormatter dtfJcampLongDate4 = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss Z");
	private static DateTimeFormatter dtfJcampLongDate5 = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss.SSSS Z");
	

	protected DateTimeFormatter dateTimeFormatter;
	private boolean isInitialized;

	public DateLdr(String raw) {
		super(raw);
	}
	
	
	public DateLdr(Label label, Date value) {
		super(label, value);
	}


	public String valueToString(Date value) {
		if(!isInitialized)
		{
			isInitialized = true;
			this.dateTimeFormatter = dtfJcampLongDate5;
		}
		
		if(dateTimeFormatter==null)
		{
			return ""+(value.getTime()/1000);
		}
		else
		{
			return dateTimeFormatter.print(value.getTime());
		}
	}



	public Date stringToValue(String string) {
		String valueString = string.split("\\$\\$",2)[0].trim();
		if(valueString.startsWith("<") && valueString.endsWith(">"))
		{
			valueString = valueString.substring(1,valueString.length()-1).trim();
		}
		
		Date date;
		
		//JCAMP Long Date
		date = parseDate(dtfJcampLongDate5, valueString);
		if(date==null)
			date = parseDate(dtfJcampLongDate4, valueString);
		if(date==null)
			date = parseDate(dtfJcampLongDate3, valueString);
		if(date==null)
			date = parseDate(dtfJcampLongDate2, valueString);
		if(date==null)
			date = parseDate(dtfJcampLongDate1, valueString);

		//Bruker
		if(date==null)
			date = parseDate(valueString);
		
		//Varian
		if(date==null)
			date = parseDate(dtfVarian, valueString);

		if(date==null)		
			throw new RuntimeException("Could not parse date '" + valueString + "'");
		
		return date;
	}

	protected Date parseDate(DateTimeFormatter dateTimeFormatter, String valueString)
	{
		try {
			Date date = dateTimeFormatter.parseDateTime(valueString).toDate();
			System.out.println("Parsed string date '" + valueString + "' to date: " + dateTimeFormatter.print(date.getTime()));
			if(!isInitialized)
			{
				isInitialized = true;
				this.dateTimeFormatter = dateTimeFormatter;
			}
			return date;
		} catch (Exception e) {			
		}
		return null;
	}

	protected Date parseDate(String seconds)
	{
		try {
			Date date = new Date(Long.parseLong(seconds)*1000);
			System.out.println("Parsed long date (seconds) '" + seconds + "' to date: " + date);
			if(!isInitialized)
			{
				isInitialized = true;
				this.dateTimeFormatter = null;
			}
			return date;
		} catch (Exception e) {
		}
		return null;
	}
}
