package com.actelion.research.nemo.jcamp;

public class JcampException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public JcampException(String message, Throwable cause) {
		super(message, cause);
	}

	public JcampException(String message) {
		super(message);
	}

	public JcampException(Throwable cause) {
		super(cause);
	}
	
}
