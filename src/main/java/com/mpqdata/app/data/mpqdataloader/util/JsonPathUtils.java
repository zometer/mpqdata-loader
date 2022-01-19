package com.mpqdata.app.data.mpqdataloader.util;

import java.util.Set;

import com.jayway.jsonpath.DocumentContext;

public class JsonPathUtils {

	public static String extractFirstKey(DocumentContext doc) {
		return doc.read("$.keys()", Set.class).toArray()[0].toString();
	}

}
