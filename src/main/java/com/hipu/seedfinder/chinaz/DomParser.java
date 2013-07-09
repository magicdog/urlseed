package com.hipu.seedfinder.chinaz;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import com.hipu.parser.autoparser.context.PageInfo;
import com.hipu.parser.xpathparser.FieldExtractRule;
import com.hipu.seedfinder.tools.ChinazCrawler;

public class DomParser {

	public static List<String> extractUrl(String url, Node node) {
		FieldExtractRule extractRule = new FieldExtractRule("//div[@class='lkcon']//p/a", null);
    	List<Node> nodes = extractRule.extractNode(new PageInfo(url), node);
    	List<String> urls = new ArrayList<String>();
    	for (Node n: nodes) {
    		String u = n.getAttributes().getNamedItem("onclick").getTextContent();
    		u = u.replace("javascript:window.open('", "");
    		u = u.replace("','_blank','')", "");
    		String s = n.getTextContent().trim();
    		s = s.replaceAll("\\.", "").replaceAll(" ", "").trim();
    		String record = u+"\t"+s+"\t"+url.trim();
//    		System.out.println(record);
    		if (record.length() < 5 )
    			continue;
    		urls.add(record);
    	}
    	return urls;
	}
	
	public static String extractEntime(String url, Node node) {
		FieldExtractRule extractRule = new FieldExtractRule("//input[@name='entime']/@value", null);
    	String anchors = extractRule.extract(new PageInfo(url), node);
    	return anchors;
	}
	public static String extractWeight(String url, Node node) {
		FieldExtractRule extractRule = new FieldExtractRule("//a/text()", null);
    	String anchors = extractRule.extract(new PageInfo(url), node);
    	if (anchors.contains("/"))
    		return anchors.split("/")[0].trim();
    	return "1";
	}
	
	public static String extractPR(String url, Node node) {
		String pr = node.getTextContent();
		if (pr.length() ==0 || pr.contains("-"))
			return "1";
    	return pr.trim();
	}
	
}
