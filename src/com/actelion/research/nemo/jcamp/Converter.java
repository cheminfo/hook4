package com.actelion.research.nemo.jcamp;

public interface Converter<T> {
	public String valueToString(T value);
	public T stringToValue(String string);	
}
