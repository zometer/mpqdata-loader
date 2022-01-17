package com.mpqdata.app.data.mpqdataloader.archive;

public class MpqSarExtractorExcpetion extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MpqSarExtractorExcpetion() {
		super();
	}

	public MpqSarExtractorExcpetion(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MpqSarExtractorExcpetion(String message, Throwable cause) {
		super(message, cause);
	}

	public MpqSarExtractorExcpetion(String message) {
		super(message);
	}

	public MpqSarExtractorExcpetion(Throwable cause) {
		super(cause);
	}
}
