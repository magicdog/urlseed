package com.hipu.seedfinder.news;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.hipu.crawlcommons.config.Config;
import com.hipu.crawlcommons.dao.SiteInfoDAO;
import com.hipu.crawlcommons.url.URLUtil;
import com.hipu.parser.autoparser.context.PageInfo;
import com.hipu.parser.dom.DomUtils;
import com.hipu.parser.utils.Content;
import com.hipu.parser.utils.EncodingUtils;
import com.hipu.parser.utils.FetchUtils;
import com.hipu.parser.xpathparser.FieldExtractRule;
import com.hipu.seedfinder.tools.HomePageFinder;
import com.hipu.seedfinder.utils.NewsTypeResult;
import com.hipu.seedfinder.utils.NewsTypeResult.NewsUrlType;

/**
 * @author weijian
 *         Date : 2013-05-07 15:32
 */
public class SingleUrlFinder {
    public static final Logger LOG = Logger.getLogger(SingleUrlFinder.class);
    
    public final static String[] KeysInUrl  = {"news","new","info","news.asp","content","articles","article"};
    
    public final static String[] KeyInHomePage = {"新闻","资讯","资讯中心","行业资讯","综合资讯","行业信息",
    	"新闻资讯"};
    
    protected HttpClient client;
    
    private SiteInfoDAO siteInfoDao;
    
    private static double MinCommonPrefixPersent = 0.5;
    
    
    public SingleUrlFinder(HttpClient client) {
    	this.client =client;
    	this.siteInfoDao = SiteInfoDAO.getInstance();
    }
    
    public String formatUrl(String url) {
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
    
    public NewsTypeResult urlContainsKeys(String url) {
    	NewsTypeResult result = new NewsTypeResult(NewsUrlType.NORMAL, url,NewsUrlType.NORMAL.name());
    	//http://news.sina.com.cn
    	String domain = "";
		try {
			domain = URLUtil.getDomainName(url);
		} catch (MalformedURLException e) {
		}
    	String keyInUrl = url.substring(0,url.lastIndexOf(domain));
    	for (String str : KeysInUrl) {
    		if (keyInUrl.contains(str))
    			return new NewsTypeResult(NewsUrlType.DOMAIN, url, str);
    		keyInUrl = url.replace(URLUtil.getHost(url), "");
    		if (keyInUrl.contains(str))
    			return new NewsTypeResult(NewsUrlType.REGEXP, url, str);
    	}
    	return result;
    }
    
    public NewsTypeResult checkUrl(String url) {
    	NewsTypeResult  result = null;
    	// judge the url type
    	result = urlContainsKeys(url);
    	if (result.getKeyWordType() == NewsUrlType.NORMAL) {
    		for (String key : KeysInUrl) {
        		String combineUrl = formatUrl(url);
    			combineUrl = combineUrl + key + "/";
    			LOG.debug(combineUrl);
    			if (testURL(combineUrl) == 200) {
    				result =  new NewsTypeResult(NewsUrlType.REGEXP, combineUrl, key);
    				break;
    			}
    			try {
    				combineUrl = "http://" + key + "." + URLUtil.getDomainName(url) +"/";
    			} catch (MalformedURLException e) {
    			}
    			if ( testURL(combineUrl) == 200) {
    				result =  new NewsTypeResult(NewsUrlType.DOMAIN, combineUrl, key);
    				break;
    			}
    		}
    	}
    	
    	switch (result.getKeyWordType()) {
    	case NORMAL:
    	case DOMAIN: {
    		//fetch the keywords in homepage, 
    		//if has return newsurl == domainurl
    		//else return domain
    		String newsUrl = extractNewsUrl(url);
    		if (newsUrl != null)
    			result = new NewsTypeResult(NewsUrlType.REGEXP, newsUrl, NewsUrlType.REGEXP.name());
    		if (VerifyNewsPage(result.getNewsUrl())){
    			return result;
    		}
    		break;
    	}
    	case REGEXP: {
    		if (VerifyNewsPage(result.getNewsUrl())){
    			return result;
    		}
    		String newsUrl = extractNewsUrl(url);
    		if (newsUrl != null)
    			return new NewsTypeResult(NewsUrlType.REGEXP, newsUrl, NewsUrlType.REGEXP.name());
    		break;
    	}
    	}
    	return new NewsTypeResult(NewsUrlType.NORMAL, url, NewsUrlType.NORMAL.name());
    }
    
    private int testURL(String url){
    	int code = 0;
        try {
            HttpHead httpHead = new HttpHead(url);
            HttpResponse resp = client.execute(httpHead);
            code = resp.getStatusLine().getStatusCode();
        } catch (Exception e) {
        }
        return code;
    }
    
    public String extractNewsUrl(String url) {
    	Content content = null;
    	try {
			content = FetchUtils.getInstance().fetch(url);
		} catch (IOException e) {
		}
    	if (content == null){
			return null;
		}
    	String charset = EncodingUtils.getEncoding(content);
		Document doc = DomUtils.getDocument(content.getContent(), charset);
		StringBuilder sb = new StringBuilder();
		if (KeyInHomePage.length > 0)
			sb.append(String.format("//a[.//text()=\"%s\"]/@href", KeyInHomePage[0]));
		for (int i=1; i<KeyInHomePage.length; i++) {
			sb.append(String.format(" | //a[.//text()=\"%s\"]/@href", KeyInHomePage[i]));
		}
		String xpath = sb.toString();
		
    	FieldExtractRule extractRule = new FieldExtractRule(xpath, null);
    	String anchors = extractRule.extract(new PageInfo(url), doc);
    	if (anchors == null || anchors.length() <2)
    		return null;
    	LOG.debug(url+ anchors);
    	for (String a : anchors.split(" ")) {
    		if (a.startsWith("http://"))
    			return a;
    	}
    	String anchor = null;
		try {
			anchor = new URL(new URL(url), anchors.split(" ")[0]).toString();
		} catch (MalformedURLException e) {
		}
    	return anchor;
    }
    
    public boolean VerifyNewsPage(String url) {
    	Content content = null;
    	try {
			content = FetchUtils.getInstance().fetch(url);
		} catch (Exception e) {
		}
    	if (content == null){
			return false;
		}
    	String charset = EncodingUtils.getEncoding(content);
		Document doc = DomUtils.getDocument(content.getContent(), charset);
		
    	FieldExtractRule extractRule = new FieldExtractRule("//a/@href", null);
    	String anchors = extractRule.extract(new PageInfo(url), doc);
    	if (anchors == null)
    		return false;
    	List<String> filterAnchors = filterAnchor(url,anchors.split(" "));
//    	for (String anchor : filterAnchors) {
//    		LOG.debug(anchor);
//    	}
    	return hasCommonPrefix(url, filterAnchors);
    }
    
    public List<String> filterAnchor(String url, String[] anchors) {
    	List<String> list = new ArrayList<String>();
    	String domain = "";
    	try {
			domain = URLUtil.getDomainName(url);
		} catch (MalformedURLException e) {
		}
    	for (String anchor : anchors) {
    		if (anchor == null || anchor.length()<2) 
    			continue;
    		if (!anchor.startsWith("http://")) {
    			try {
					URL u = new URL(new URL(url), anchor);
					list.add(u.toString());
				} catch (MalformedURLException e) {
				}
    		} else if (anchor.contains(domain)){
    			list.add(anchor);
    		}
    	}
    	return list;
    }
    
    public boolean hasCommonPrefix(String url, List<String> anchors) {
    	long totalAnchor = anchors.size();
    	if (totalAnchor == 0)
    		return false;
    	long commonAnchor = 0;
    	long newsAnchor = 0;
    	for (String anchor : anchors) {
    		if (anchor.startsWith(url))
    			commonAnchor++;
    		for (String key : KeysInUrl) {
    			if (anchor.toLowerCase().contains(key)){
    				newsAnchor++;
    				break;
    			}
    		}
    	}
    	LOG.debug(url);
    	double commonPersent = 1.0 * commonAnchor / totalAnchor;
    	LOG.debug(url+commonPersent);
    	if (commonPersent > MinCommonPrefixPersent)
    		return true;
    	double newsPersent = 1.0 * newsAnchor / totalAnchor;
//    	LOG.debug(url+newsPersent);
    	if (newsPersent > MinCommonPrefixPersent)
    		return true;
    	return false;
    }
    
    public String getNewsUrl(String url) {
    	String domain;
		try {
			domain = URLUtil.getDomainName(url);
		} catch (MalformedURLException e) {
			return new NewsTypeResult(NewsUrlType.ERROR,  url, NewsUrlType.ERROR.name()).toString();
		}
    	if (siteInfoDao.siteExist(domain)) {
    		return new NewsTypeResult(NewsUrlType.EXIST,  url, NewsUrlType.EXIST.name()).toString();
    	}
    	return checkUrl(url).toString();
    }

    public static void main(String[] args) throws ConfigurationException {
    	HttpClient client = null;
    	  PoolingClientConnectionManager manager = new PoolingClientConnectionManager();
          manager.setMaxTotal(20);
          manager.setDefaultMaxPerRoute(5);

          HttpParams params = new BasicHttpParams();
          params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
          params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
          params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);

          client = new DefaultHttpClient(manager, params);
          Config conf = Config.getInstance();
          Configuration config = new PropertiesConfiguration(HomePageFinder.class.getResource("/config.properties"));
          conf.registerConfig(config);
          SingleUrlFinder finder = new SingleUrlFinder(client);
//          System.out.println(finder.getNewsUrl("http://www.cosraw.com"));
//          finder.VerifyNewsPage("http://www.cnjijia.com/new/");
          System.out.println(finder.getNewsUrl("http://www.soft568.com"));
          System.out.println(finder.getNewsUrl("http://www.muxianji.com/"));
          
         
        	  
//          System.out.println(finder.getNewsUrl("http://www.100gyp.com"));
          //http://www.muxianji.com/../Information/Info.asp
//        	  
//        		  
//        			  
        	  
    }
}
