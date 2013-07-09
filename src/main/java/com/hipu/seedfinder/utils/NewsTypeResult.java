package com.hipu.seedfinder.utils;

public class NewsTypeResult {
	
	public enum NewsUrlType {DOMAIN, REGEXP, EXIST, ERROR, NORMAL, NOCONTENT};
	
	private NewsUrlType keyWordType;
	private String newsUrl;
	private String key;
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public NewsTypeResult(NewsUrlType type, String url, String key) {
		this.setKeyWordType(type);
		this.setNewsUrl(url);
		this.key = key;
	}
	
	public String toString() {
		return String.format("[%s]\t%s\t%s", this.getKeyWordType().name(), this.getNewsUrl(),this.key.toLowerCase());
	}

	public NewsUrlType getKeyWordType() {
		return keyWordType;
	}

	public void setKeyWordType(NewsUrlType keyWordType) {
		this.keyWordType = keyWordType;
	}

	public String getNewsUrl() {
		return newsUrl;
	}

	public void setNewsUrl(String newsUrl) {
		this.newsUrl = newsUrl;
	}
}
