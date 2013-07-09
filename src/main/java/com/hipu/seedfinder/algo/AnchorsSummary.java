package com.hipu.seedfinder.algo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hipu.crawlcommons.url.URLUtil;
import com.hipu.crawlcommons.urlnormalizer.BasicURLNormalizer;
import com.hipu.crawlcommons.urlnormalizer.URLNormalizer;
import com.hipu.seedfinder.utils.NewsTypeResult;
import com.hipu.seedfinder.utils.NewsTypeResult.NewsUrlType;
import com.hipu.seedfinder.utils.URLFormat;

public class AnchorsSummary {

	public static final Logger LOG = Logger.getLogger(AnchorsSummary.class);

	public final static String[] KeysInUrl = { "news", "info",
			"content", "article","xinwen","show","new" ,"view","zixun"};
	
    private final static double MinPesent = 0;
    
//	public NewsTypeResult pageUrlType(String url) {
//		NewsTypeResult result = new NewsTypeResult(NewsUrlType.NORMAL,  url,
//				NewsUrlType.NORMAL.name());
//		// http://news.sina.com.cn
//		String domain = "";
//		try {
//			domain = URLUtil.getDomainName(url);
//		} catch (MalformedURLException e) {
//		}
//		String keyInUrl = url.substring(0, url.lastIndexOf(domain));
//		String keyInPath = url.replace(URLUtil.getHost(url), ""); 
//		for (String str : KeysInUrl) {
//			if (keyInUrl.contains(str))
//				return new Result(NewsUrlType.DOMAIN, 200, url, str);
//			if (keyInPath.contains(str))
//				return new Result(NewsUrlType.REGEXP, 200, url, str);
//		}
//		return result;
//	}

	

//	public String getFirstField(String url) {
//		String path = "";
//		try {
//			path = new URL(url).getPath().replaceFirst("/", "");
//		} catch (MalformedURLException e) {
//			LOG.error("MalformedURLException : " + url);
//		}
//
//		if (path.indexOf("/") != -1)
//			path = path.substring(0, path.indexOf("/"));
//		if (path.indexOf("?") != -1)
//			path = path.substring(0, path.indexOf("?"));
//		// LOG.debug(path);
//		return path;
//	}
	
	public NewsTypeResult containsKeyWord(String url, List<String> anchors) {
		LOG.debug("check whether contains key words such as news.");
		NewsTypeResult result = new NewsTypeResult(NewsUrlType.NORMAL, url, NewsUrlType.NORMAL.name());
		Map<String, Integer> keysCount = new HashMap<String, Integer>();
		for (String str : KeysInUrl) {
			keysCount.put("0:"+str, 0);
			keysCount.put("1:"+str, 0);
		}
		
		long totalAnchor = anchors.size();
    	if (totalAnchor == 0)
    		return result;
		for (String anchor : anchors) {
			String domain = "";
			try {
				domain = URLUtil.getDomainName(anchor);
			} catch (MalformedURLException e) {
			}
			LOG.debug(anchor);
			String keyInUrl = anchor.substring(0, anchor.lastIndexOf(domain)).toLowerCase();
			String keyInPath = anchor.replace(URLUtil.getHost(anchor), "").toLowerCase(); 
			for (String str : KeysInUrl) {
				if (keyInUrl.contains(str)){
					keysCount.put("0:"+str, keysCount.get("0:"+str)+1);
					break;
				}
				if (keyInPath.contains(str)){
					keysCount.put("1:"+str, keysCount.get("1:"+str)+1);
					break;
				}
			}
		}

		List<Map.Entry<String, Integer>> length = new ArrayList<Map.Entry<String, Integer>>(
				keysCount.entrySet());
		Collections.sort(length, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> mapping1,
					Map.Entry<String, Integer> mapping2) {
				return mapping2.getValue().compareTo(mapping1.getValue());
			}
		});

		for (Map.Entry<String, Integer> entry : length){
			LOG.debug(entry.getKey()+" "+entry.getValue());
		}
		
    	double commonPersent = 1.0 * length.get(0).getValue() / totalAnchor;
    	String type = length.get(0).getKey();
//    	LOG.debug(commonPersent);
    	if (commonPersent > MinPesent){
    		if (type.startsWith("0"))
    			return new NewsTypeResult(NewsUrlType.DOMAIN,url, type.split(":")[1]);
    		else
    			return new NewsTypeResult(NewsUrlType.REGEXP,url, type.split(":")[1]);
    	}
    	return result;
	}
	
	
	
	public NewsTypeResult summaryUrlTypeByLength(String url,String anchors) {
		List<String> filterAnchors = filterAnchor(url,anchors.split(" "));
		NewsTypeResult result = containsKeyWord(url, filterAnchors);
		if ( result.getKeyWordType() != NewsUrlType.NORMAL)
			return result;
		
		
		filterAnchors = getNewsAnchor(filterAnchors);
		if (filterAnchors.size() == 0)
			return new NewsTypeResult(NewsUrlType.NORMAL, url, NewsUrlType.NORMAL.name());
		String first = filterAnchors.get(0);
		String common = "";
		for (int i=1; i<filterAnchors.size();i++){
			LOG.debug(first);
			common = longestCommonPrefix(first, filterAnchors.get(i));
			first = common;
		}
		LOG.debug(common);
		if (common != "" && common.length() >1){
			try {
				common = URLFormat.getUrlBeforeWenhao(common);
				return new NewsTypeResult(NewsUrlType.REGEXP,url,processCommon(common));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return result;
		
	}
	
	public String processCommon(String common) {
		if (common == null || common.equals(""))	
			return "";
		common = common.replace("\\", "/");
		if (common.endsWith("/"))
			common = common.substring(0,common.length()-1);
		String segments[] = common.split("/");
		StringBuilder sb = new StringBuilder();
		for (String seg: segments){
			seg = seg.trim().toLowerCase();
			if ( seg.length()>1 && !seg.equals("html") && !seg.equals("htm") && !seg.matches("^[0-9]+[_-]?$")) {
				if (seg.contains("."))
					seg = seg.substring(0, seg.indexOf("."));
				if (seg.contains("_"))
					seg = seg.substring(0, seg.indexOf("_"));
				return seg;
			}
		}
		return "";
		
		
	}
	
		

//	public Result summaryUrlType(String url,String anchors) {
//		Result result = pageUrlType(url);
//		if (anchors == null || url == null)
//			return result;
//		
//		List<String> filterAnchors = filterAnchor(url,anchors.split(" "));
//		
//		String front = "http://" + URLUtil.getHost(url) + "/";
//		long totalAnchor = filterAnchors.size();
//		if (totalAnchor <= 1)
//			return result;
//		Map<String, Integer> commonLength = new HashMap<String, Integer>();
//		for (String anchor : filterAnchors) {
//			String firstField = null;
//			try {
//				firstField = URLFormat.getUrlBeforeLastSlash(anchor);
//			} catch (MalformedURLException e) {
//				LOG.error("MalformedURLException : "+ anchor + " parsing "+url);
//			}
//			if (firstField == null)
//				continue;
//			if (commonLength.containsKey(firstField))
//				commonLength.put(firstField, commonLength.get(firstField) + 1);
//			else
//				commonLength.put(firstField, 1);
//		}
//
//		List<Map.Entry<String, Integer>> length = new ArrayList<Map.Entry<String, Integer>>(
//				commonLength.entrySet());
//		Collections.sort(length, new Comparator<Map.Entry<String, Integer>>() {
//			public int compare(Map.Entry<String, Integer> mapping1,
//					Map.Entry<String, Integer> mapping2) {
//				return mapping2.getValue().compareTo(mapping1.getValue());
//			}
//		});
//
//		LOG.debug("anchor summary in page : " + url);
//		for (Map.Entry<String, Integer> entry : length) {
//			LOG.debug(entry.getKey() + " " + entry.getValue());
//		}
//		Map.Entry<String,Integer> common = length.get(0);
//		double persent = 1.0 * common.getValue() / filterAnchors.size();
//		boolean b = length.size()>=3 && length.get(0).getValue() > length.get(1).getValue()+length.get(2).getValue();
//		if (persent > MinPesent || b) {
//			Result r = pageUrlType(common.getKey());
//			if (r.getKeyWordType() == NewsUrlType.DOMAIN)
//				return r;
//			return new Result(NewsUrlType.REGEXP,200,url,getFirstField(common.getKey()));
//		}
//		return result;
//	}

	public List<String> filterAnchor(String url, String[] anchors) {
		List<String> list = new ArrayList<String>();
		URLNormalizer normalozer = new BasicURLNormalizer();
		String formatUrl = URLFormat.formatUrl(url);
		String domain = "";
		try {
			domain = URLUtil.getDomainName(url);
		} catch (MalformedURLException e) {
			LOG.error("MalformedURLException : " + formatUrl);
		}
		for (String anchor : anchors) {
			
			if (anchor == null || anchor.length() < 2
					|| anchor.contains("javascript"))
				continue;
			if (!anchor.startsWith("http://") && !anchor.contains(":")) {
				try {
					String normal = normalozer.normalize(new URL(new URL(
							formatUrl), anchor).toString());
					list.add(normal);
				} catch (MalformedURLException e) {
					LOG.error("MalformedURLException : " + formatUrl + " "
							+ anchor);
				}
			} else if (anchor.startsWith("http://") && anchor.contains(domain)) {
				
				list.add(anchor);
			}
		}
		return list;
	}
	
	 public int calculateDistance(String strA, String strB) {
		   int lenA = (int)strA.length()+1;  
		   int lenB = (int)strB.length()+1;
		   int c[][] = new int[lenA][lenB] ;
		   // Record the distance of all begin points of each string  
		   //初始化方式与背包问题有点不同  
		   for(int i = 0; i < lenA; i++) c[i][0] = i;  
		   for(int j = 0; j < lenB; j++) c[0][j] = j;  
		   c[0][0] = 0;  
		   for(int i = 1; i < lenA; i++)  
		   {  
		   for(int j = 1; j < lenB; j++)  
		   {  
		     if(strB.charAt(j-1) == strA.charAt(i-1))  
		       c[i][j] = c[i-1][j-1];  
		     else  
		       c[i][j] = Math.min(c[i][j-1], Math.min(c[i-1][j], c[i-1][j-1])) + 1;  
		   }  
		   }  
		   int ret =  c[lenA-1][lenB-1];  
		   return ret;  
	 }
	
	public List<String> getNewsAnchor(List<String> anchors) {
		long totalAnchor = anchors.size();
		if (totalAnchor <= 2)
			return anchors;
		
		//{34: ["http://ddd...c", "http://ddd...f"],
		//33: ["http://ddd...c", "http://ddd...f", ..]}
		Map<Integer, List<String> > length_url = new HashMap<Integer, List<String>>();
		for (String anchor : anchors) {
			int anchorLength = anchor.length();
			if (length_url.containsKey(anchorLength))
				length_url.get(anchorLength).add(anchor);
			else{
				List<String> anchorSet = new ArrayList<String>();
				anchorSet.add(anchor);
				length_url.put(anchorLength, anchorSet);
			}
		}
		
		// sort by list size
		List<Map.Entry<Integer, List<String>>> length = new ArrayList<Map.Entry<Integer, List<String>>>(
				length_url.entrySet());
		Collections.sort(length, new Comparator<Map.Entry<Integer, List<String>>>() {
			public int compare(Map.Entry<Integer, List<String>> mapping1,
					Map.Entry<Integer, List<String>> mapping2) {
				return mapping2.getValue().size() - mapping1.getValue().size();
			}
		});
		
		LOG.debug("anchor summary : " );
		for (Map.Entry<Integer, List<String>> entry : length) {
			LOG.debug("length: "+entry.getKey() +" count: "+entry.getValue().size());
		}
		
		List<String> newAnchors = length.get(0).getValue();
		if (newAnchors.size() == 0)
			return anchors;
		Collections.sort(newAnchors);
		for (int i=1; i<newAnchors.size();i++){
			LOG.debug(newAnchors.get(i));
		}
		
		int count = 1;
		int index = 0;
		for (int i=1; i<newAnchors.size();i++) {
			int dis = calculateDistance(newAnchors.get(i-1), newAnchors.get(i));
			if (dis < 9) {
				count ++;
			} else {
				if ((1.0 * count) / newAnchors.size() > 0.5)
					return newAnchors.subList(index, i);
				index = i;
				count = 1;
			}
		}
		
		int middle = newAnchors.size() /2;
		int start = middle/2;
		int end = middle + middle -start;
		newAnchors = newAnchors.subList(start, end);
		return newAnchors;
	}

	public String longestCommonPrefix(String str1, String str2) {
		if (str1 == null || str2 == null)
			return null;

		int index = 0;
		while (index < str1.length() && index < str2.length()
				&& str1.charAt(index) == str2.charAt(index))
			index++;

		return str1.substring(0, index);
	}
}
