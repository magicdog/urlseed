package com.hipu.seedfinder.utils;

public class ExtractUrlResult {
	public enum ExtractUrlType {NULL, SUCCESS, NOCONTENT, NONEWSURL};
	
	private ExtractUrlType extracUrlType;
	private String newsUrl;
	
	public ExtractUrlResult(ExtractUrlType urlType, String url) {
		this.extracUrlType = urlType;
		this.newsUrl = url;
	}
	
	public String toString() {
		return String.format("[%s]\t%s", this.extracUrlType.name(), this.newsUrl);
	}
	
}


