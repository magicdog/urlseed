package com.hipu.seedfinder.utils;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.beust.jcommander.internal.Lists;
import com.hipu.crawlcommons.dao.SiteInfoDAO;
import com.hipu.crawlcommons.url.URLUtil;
import com.hipu.seedfinder.tools.URLSFilter;

public class URLFilter {
	public static final Logger LOG = Logger.getLogger(URLFilter.class);
	
	public final static String[] SourceExceptRegExp = 
		{".*[集团 |厂|公司|企业|店|制造商|银行 |论坛|出版社|制作|设备]$",
    	".*[联通|商城|加盟|采购| 招标|投标|交易|批发|下载|快递|租售|邮箱|图片" +
    	"|培训|视频|查询|课件|网址|黄页|目录|直播|游戏|导航|代理|卖场|礼物|微博|地图].*"};
	
	public final static String[] DomainExcept = 
		{
		"weibo.com","taobao.com","\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}","9588.com","ku6.com",
		"youku.com"
		};
	
	private List<Pattern> sourceExceptPatterns;
	private List<Pattern> domainExcept;
	
	//"商业","休闲","健康"
	public final static String[] CategoryIncludedKeyWord = {"艺术"};
	
	private SiteInfoDAO siteInfo;
	
	private static URLFilter _filter = null;
	
	public static URLFilter getInstance() {
		synchronized (URLFilter.class){
			if (_filter == null)
				_filter = new URLFilter();
			return _filter;
		}
		
	}
	
	public URLFilter() {
		this.siteInfo = SiteInfoDAO.getInstance();
		this.sourceExceptPatterns = new ArrayList<Pattern>();
		for (String reg : SourceExceptRegExp) {
    		this.sourceExceptPatterns.add(Pattern.compile(reg));
    	}
		this.domainExcept = Lists.newArrayList();
		for (String dom : DomainExcept)
			this.domainExcept.add(Pattern.compile(dom));
	}
	
	public boolean obeyConditions(String source) throws MalformedURLException {
		return obeyConditions(null, null, source);
	}
	
	public boolean obeyConditions(String url, String source) throws MalformedURLException {
		return obeyConditions(url, null, source);
	}
	
	public boolean obeyConditions(String url, String category, String source) throws MalformedURLException {
		LOG.warn(url);
		boolean obeyTag = false;
		if (source != null) {
			for (Pattern pattern : sourceExceptPatterns) {
	    		if (pattern.matcher(source).matches()){
	    			obeyTag = true;
	    			break;
	    		}
	    	}
			if (obeyTag)
				return false;
		}
		if (category != null) {
			for (String str: CategoryIncludedKeyWord) {
				if (category.contains(str)) {
					obeyTag = true;
					break;
				}
			}
			if (!obeyTag)
				return false;
		}
		if (url != null) {
			String domain = URLUtil.getDomainName(url);
			if (siteInfo.siteExist(domain)) {
				LOG.warn("Domain Exists: "+url);
				return false;
			}
				
			for (Pattern pattern : domainExcept) {
				if (pattern.matcher(domain).matches())
					return false;
			}
			return true;
		}
		return obeyTag;
	}
	
}
