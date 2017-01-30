package com.actelion.research.nemo.jcamp;

public class StringLdr extends Ldr<String> {	
	boolean isQuoted = false;

	public StringLdr(String raw) {
		super(raw);
	}

	public StringLdr(Label label, String value) {
		super(label, value);
	}

	public String valueToString(String value) {
		if(isQuoted)
			return "<" + value + ">";
		else
			return value;
	}

	public String stringToValue(String string) {
		String value = string.split("\\$\\$",2)[0].trim();
		if(value.startsWith("<") && value.endsWith(">"))
		{
			isQuoted = true;
			value = value.substring(1,value.length()-1).trim();
		}
		return value;
	}
}
