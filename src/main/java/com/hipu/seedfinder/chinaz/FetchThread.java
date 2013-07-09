package com.hipu.seedfinder.chinaz;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;
import org.w3c.dom.Document;

import com.hipu.crawlcommons.url.URLUtil;
import com.hipu.parser.dom.DomUtils;
import com.hipu.seedfinder.tools.ChinazCrawler;

public class FetchThread implements Runnable{
	
	private LocalHttpClient client;
	
	public static final Logger LOG = Logger.getLogger(FetchThread.class);
	
	private String url;
	
	public FetchThread(String url) {
		client = LocalHttpClient.getInstance();
		this.url = url;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			if (! ChinazCrawler.notFinished())
				return;
			HttpResponse resp = client.fetchUrl(url, ChinazCrawler.entime);
			Document doc = DomUtils.getDocument(EntityUtils.toByteArray(resp.getEntity()), "utf-8");
			List<String> anchors = DomParser.extractUrl(url, doc);
			if (anchors.size() == 0){
				LOG.warn("can not get the anchors in page : "+url);
				LOG.warn(EntityUtils.toByteArray(resp.getEntity()));
			}
			for (String anchor : anchors) {
				String url = anchor.split("\t")[0];
//				resp = client.fetchWeight(URLUtil.getHost(url), ChinazCrawler.encode);
//				doc = DomUtils.getDocument(EntityUtils.toByteArray(resp.getEntity()), "utf-8");
//				int weight = Integer.parseInt(DomParser.extractWeight(url, doc));
				
			    resp = client.fetchPR(URLUtil.getHost(url), ChinazCrawler.encode);
//				doc = DomUtils.getDocument(EntityUtils.toByteArray(resp.getEntity()), "utf-8");
			    String pr = EntityUtils.toString(resp.getEntity());
				if (pr.length() == 0 || pr.length()>5 || pr.contains("-") )
					pr = "0";
//				ChinazCrawler.crawlUrls.put(url);
				if ( !ChinazCrawler.totalUrls.contains(url) ) {
					ChinazCrawler.totalUrls.add(url);
					ChinazCrawler.records.put(anchor+"\t"+pr);
				}
			}
			ChinazCrawler.crawledUrls.add(url);
			ChinazCrawler.crawledRecords.put(url);
			ChinazCrawler.crawledInfo();
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
