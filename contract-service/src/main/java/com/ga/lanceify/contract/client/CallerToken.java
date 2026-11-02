package com.ga.lanceify.contract.client;

import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

final class CallerToken {

	private CallerToken() {
	}

	static String header() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes == null) {
			return "";
		}
		String header = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
		return header == null ? "" : header;
	}

}
