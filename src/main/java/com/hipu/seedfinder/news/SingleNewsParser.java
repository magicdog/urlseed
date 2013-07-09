package com.hipu.seedfinder.news;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.hipu.crawlcommons.config.Config;
import com.hipu.parser.autoparser.context.PageInfo;
import com.hipu.parser.dom.DomUtils;
import com.hipu.parser.xpathparser.FieldExtractRule;
import com.hipu.seedfinder.algo.AnchorsSummary;
import com.hipu.seedfinder.algo.ExtractNewsUrl;
import com.hipu.seedfinder.tools.HomePageFinder;
import com.hipu.seedfinder.utils.ExtractUrlResult;
import com.hipu.seedfinder.utils.ExtractUrlResult.ExtractUrlType;
import com.hipu.seedfinder.utils.NewsTypeResult;
import com.hipu.seedfinder.utils.NewsTypeResult.NewsUrlType;
import com.hipu.seedfinder.utils.URLFilter;

/**
 * @author weijian
 *         Date : 2013-05-07 15:32
 */
public class SingleNewsParser {
    public static final Logger LOG = Logger.getLogger(SingleNewsParser.class);
    
    private Map<String, String> urlContents;
    
    private ExtractNewsUrl extractNewsUrl; 
    
    private AnchorsSummary summary;
    
    private URLFilter urlFilter;
    
    public SingleNewsParser(Map<String, String> contents) {
    	this.urlContents = contents;
    	this.extractNewsUrl = new ExtractNewsUrl();
    	this.summary = new AnchorsSummary();
//    	this.urlFilter = URLFilter.getInstance();
    	
    }
    
    public String extractNewsUrl(String url) {
    	if (url == null)
    		return new ExtractUrlResult(ExtractUrlType.NULL,null).toString();
    	String content = urlContents.get(url);
    	LOG.debug(url+" "+content);
    	if (content == null)	{
    		LOG.warn("can not get the content identified by : "+url);
    		return new ExtractUrlResult(ExtractUrlType.NOCONTENT,null).toString();
    	}
		Document doc = DomUtils.getDocument(content.getBytes(), "utf-8");
		String newsUrl = null;
		try {
			return newsUrl = this.extractNewsUrl.extractNewsUrl(url, doc);
		} catch (IOException e) {
			LOG.error("can not paeser url : "+url);
		}
		return new ExtractUrlResult(ExtractUrlType.NULL,null).toString();
    }
    
    // json{"url":"", "content":""}
    public String getNewsPageType(String url) {
    	NewsTypeResult result = new NewsTypeResult(NewsUrlType.NORMAL, url, NewsUrlType.NORMAL.name());
    	String content = urlContents.get(url);

    	if (content == null) {
    		LOG.warn("can not get the content identified by : "+url);
    		return new NewsTypeResult(NewsUrlType.NOCONTENT, url, NewsUrlType.NOCONTENT.name()).toString();
    	}
    	Document doc = DomUtils.getDocument(content.getBytes(), "utf-8");
    	
    	FieldExtractRule extractRule = new FieldExtractRule("//a/@href", null);
    	String anchors = extractRule.extract(new PageInfo(url), doc);
    	
//    	result = summary.summaryUrlType(url, anchors);
    	result = summary.summaryUrlTypeByLength(url, anchors);
    	return result.toString();
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
//          SingleNewsParser finder = new SingleNewsParser(client);
//          System.out.println(finder.getNewsUrl("http://www.cosraw.com"));
//          finder.VerifyNewsPage("http://www.cnjijia.com/new/");
//          System.out.println(finder.getNewsUrl("http://www.soft568.com"));
//          System.out.println(finder.getNewsUrl("http://www.muxianji.com/"));
          
         
        	  
//          System.out.println(finder.getNewsUrl("http://www.100gyp.com"));
          //http://www.muxianji.com/../Information/Info.asp
//        	  
//        		  http://www.cdj360.com
          
//        http://www.world-metal.com/News/news.html			  
        	  
    }
}
