package com.actelion.research.nemo.jcamp;

public abstract class Ldr<T> implements Converter<T>{
	protected Label label;
	protected T value;
	protected String raw;
	

	public Ldr(String raw) {
		this.raw = raw;
		
		this.label = Label.fromString(raw);
		this.value = stringToValue(extractlValueString(raw));
	}
	
	public Ldr(Label label, T value) {
		this.label = label;
		this.value = value;
		
		this.raw = label.get() + "=" + valueToString(value); 
	}
		
	protected String extractlValueString(String raw)
	{
		String string = raw.trim();
		
		if(raw.matches(".*[^\\u0000-\\u007F].*"))
			throw(new JcampException("Jcamp standard does not allow non ASCII characters!"));

		if(string.startsWith("##"))
		{			
			String[] keyValuePair = string.replaceFirst("##", "").split("=", 2);
			if(keyValuePair.length==2)
				return keyValuePair[1];
			else
				throw(new JcampException("LDR String must contain a key value pair like'##Key=value'!"));
		}
		else
		{
			throw(new JcampException("LDR must start with '##'!"));
		}
	}

	public Label getLabel() {
		return label;
	}

	public T getValue() {
		return value;
	}

	public String getRaw() {
		return raw;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ldr<?> other = (Ldr<?>) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return label.get() + "/" + value;
	}
}
