package com.hipu.seedfinder.basic;

public class Result {
	
	public enum NewsUrlType {DOMAIN, REGEXP, EXIST, ERROR, NORMAL, EXTRACT};
	
	private NewsUrlType keyWordType;
	private int statusCode;
	private String newsUrl;
	private String key;
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public Result(NewsUrlType type, int status, String url, String key) {
		this.setKeyWordType(type);
		this.statusCode = status;
		this.setNewsUrl(url);
		this.key = key;
	}
	
	public static Result getNormalResult(String url) {
		return new Result(NewsUrlType.NORMAL,0,url,null);
	}
	
	public String toString() {
		return String.format("[%s]%s\t%s\t%s", this.getKeyWordType().name(), statusCode, this.getNewsUrl(),this.key);
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
