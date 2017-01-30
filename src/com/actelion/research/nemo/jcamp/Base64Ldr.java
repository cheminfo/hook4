package com.actelion.research.nemo.jcamp;

import org.apache.commons.codec.binary.Base64;

public class Base64Ldr extends Ldr<String> {
	public Base64Ldr(String raw) {
		super(raw);
	}

	public Base64Ldr(Label label, String value) {
		super(label, value);
	}

	public String valueToString(String value) {		
		String string = value.split("\\$\\$",2)[0].trim();
		return System.getProperty("line.separator") + new String(Base64.encodeBase64Chunked(string.getBytes())).trim();
	}

	public String stringToValue(String string) {
		return new String(Base64.decodeBase64(string.getBytes()));
	}
}
