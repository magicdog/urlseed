package com.hipu.seedfinder.utils;

import java.net.MalformedURLException;
import java.net.URL;

import com.hipu.crawlcommons.url.URLUtil;

public class URLFormat {

	public static String formatUrl(String url) {
		if (url == null)
			return url;
		String formalUrl = url;

		if (!url.startsWith("http://"))
			formalUrl = "http://" + url;

		if (!formalUrl.endsWith("/")) {
			formalUrl = formalUrl + "/";
		}
		return formalUrl;
	}
	
//	public static String getUrlBeforeLastSlash(String url) throws MalformedURLException {
//		if (url == null)
//			return null;
//		URL formatUrl = new URL(url);
//		String path = formatUrl.getPath().replaceFirst("/", "");
//
//		if (path.indexOf("/") != -1)
//			path = path.substring(0, path.indexOf("/"));
//		if (path.indexOf("?") != -1)
//			path = path.substring(0, path.indexOf("?"));
//		// LOG.debug(path);
//		return "http://"+formatUrl.getHost()+"/"+path;
//	}
	
	public static String getUrlBeforeWenhao(String url) throws MalformedURLException {
		if (url == null)
			return null;
		String path = new URL(url).getPath().replaceFirst("/", "");
//		int lastSlash = path.lastIndexOf("/");
//		int lastWenhao = path.lastIndexOf("?");
//		int lastAnd = path.lastIndexOf("&");
//		int lastIndex = Math.max(lastSlash, Math.max(lastWenhao, lastAnd));
//		
//		if (lastIndex != -1)
//			path = path.substring(0, lastIndex);
		
		// LOG.debug(path);
		return path;
	}
}
