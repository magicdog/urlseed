package com.hipu.seedfinder.algo;

import java.io.IOException;
import java.net.URL;

import org.w3c.dom.Node;

import com.hipu.crawlcommons.url.URLUtil;
import com.hipu.crawlcommons.urlnormalizer.BasicURLNormalizer;
import com.hipu.crawlcommons.urlnormalizer.URLNormalizer;
import com.hipu.parser.autoparser.context.PageInfo;
import com.hipu.parser.xpathparser.FieldExtractRule;
import com.hipu.seedfinder.utils.ExtractUrlResult;
import com.hipu.seedfinder.utils.ExtractUrlResult.ExtractUrlType;
import com.hipu.seedfinder.utils.URLFormat;

public class ExtractNewsUrl {

	public final static String[] KeyInHomePage = {"新闻资讯","资讯中心","行业动态","行情资讯","信息中心","业界动态"
		,"快讯播报","行业新闻","新闻中心","展会新闻","最新动态","资讯","要闻","快讯","动态","新闻"};
	
	public ExtractNewsUrl() {
	}
	
	
	public String extractNewsUrl(String url, Node node) throws IOException {
		ExtractUrlResult result = new ExtractUrlResult(ExtractUrlType.NONEWSURL,null);
		String formatUrl = URLFormat.formatUrl(url);
		URLNormalizer nomalizer = new BasicURLNormalizer();
		
		for (String str : KeyInHomePage) {
			String xpath = String.format("//a[contains(.//text(),\"%s\")]/@href", str);
			FieldExtractRule extractRule = new FieldExtractRule(xpath, null);
	    	String anchors = extractRule.extract(new PageInfo(formatUrl), node);
	    	if (anchors == null || anchors.length() <2)
	    		continue;
	    	
			for (String anchor : anchors.split(" ")) {
				if (anchor.contains("javascript"))
					continue;
				if (!anchor.startsWith("http:")) {
					String newUrl = nomalizer.normalize(new URL(new URL(formatUrl),	anchor).toString()); 
					return new ExtractUrlResult(ExtractUrlType.SUCCESS, newUrl).toString();
				}
				else if (URLUtil.isSameDomainName(anchor, formatUrl))
					return new ExtractUrlResult(ExtractUrlType.SUCCESS, anchor).toString();
			}
		}
		return result.toString();
		
	}
}
