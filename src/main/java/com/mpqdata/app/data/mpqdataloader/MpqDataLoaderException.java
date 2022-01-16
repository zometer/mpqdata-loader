package com.mpqdata.app.data.mpqdataloader;

public class MpqDataLoaderException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MpqDataLoaderException() {
		super();
	}

	public MpqDataLoaderException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MpqDataLoaderException(String message, Throwable cause) {
		super(message, cause);
	}

	public MpqDataLoaderException(String message) {
		super(message);
	}

	public MpqDataLoaderException(Throwable cause) {
		super(cause);
	}

}
