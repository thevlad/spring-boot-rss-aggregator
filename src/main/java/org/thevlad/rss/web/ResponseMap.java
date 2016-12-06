package org.thevlad.rss.web;

import java.util.HashMap;
import java.util.Map;

public class ResponseMap {

	public static <T> Map<String, Object> mapOK(T data) {
		Map<String, Object> responseMap = new HashMap<String, Object>(2);
		responseMap.put("success", true);
		responseMap.put("data", data);
		return responseMap;
	}

	public static <T> Map<String, Object> mapError(T data) {

		Map<String, Object> responseMap = new HashMap<String, Object>(2);
		responseMap.put("success", false);
		responseMap.put("data", data);
		return responseMap;
	}
}
