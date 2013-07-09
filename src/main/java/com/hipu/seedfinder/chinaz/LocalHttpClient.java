package com.hipu.seedfinder.chinaz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class LocalHttpClient {
	protected HttpClient client;
	
	private static LocalHttpClient httpClient = null;
	
	private final static String __VIEWSTATE = 
			"/wEPDwUKMTMyNDI5NDM1MWQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFggFBnNlbGFsbAUNc2VsYmFpZHVwYWdlcwUMc2VsYmFpZHVyYW5rBQVzZWxwcgUHc2Vsc25hcAUFc2VsZmwFCXNlbGxpbmtpcAURc2Vsbm9mYW5tb3ZlZmlyc3Q=";
	
	private final static String linkcount = "100";
	
	private final static String linkswebsite = "";
	
	private final static String btnQuiry = "%E6%9F%A5+%E8%AF%A2";
	
	private final static String ddlCheckMode = "1";
	
	private final static String ddlNofollowLink = "1";
	
	private final static String ddlSearchMode = "0";
	
	private final static String linkdomains = "xxx.com,www.xxx.com";
	
	private final static String selall = "on";
	
	private final static String selbaidupages = "on";
	
	private final static String selbaidurank = "on";
	
	private final static String selfl = "on";
	
	private final static String selnofanmovefirst = "on";
	
	private final static String selpr = "on";
	
	private final static String selsnap = "on";
	
	private final static String destUrl = "http://link.chinaz.com/";
	
	private final static String weightUrl = "http://link.chinaz.com/br.aspx";
	
	private final static String prUrl = "http://link.chinaz.com/GooglePR.aspx";
	
	public LocalHttpClient() {
		PoolingClientConnectionManager manager = new PoolingClientConnectionManager();
		manager.setMaxTotal(20);
		manager.setDefaultMaxPerRoute(5);

		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 90000);
		params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
		params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		params.setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost("10.127.10.42", 3180));

		client = new DefaultHttpClient(manager, params);
	}
	public static LocalHttpClient getInstance() {
		synchronized (LocalHttpClient.class) {
			if (httpClient == null)
				httpClient = new LocalHttpClient();
			return httpClient;
		}
	}
	public HttpResponse fetchPR(String host, String encode) throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(prUrl); 
		List <NameValuePair> params = new ArrayList <NameValuePair>();
	    params.add(new BasicNameValuePair("domain", host)); 
	    params.add(new BasicNameValuePair("encode", encode)); 
	    HttpEntity entity = new UrlEncodedFormEntity(params);

	    httpPost.setEntity(entity);
		return client.execute(httpPost);
	}
	
	public HttpResponse fetchWeight(String host, String encode) throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(weightUrl); 
		List <NameValuePair> params = new ArrayList <NameValuePair>();
	    params.add(new BasicNameValuePair("domain", host)); 
	    params.add(new BasicNameValuePair("encode", encode)); 
	    HttpEntity entity = new UrlEncodedFormEntity(params);

	    httpPost.setEntity(entity);
		return client.execute(httpPost);
	}
	
	public HttpResponse fetchUrl(String url, String entime) throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(destUrl); 
		
		List <NameValuePair> params = new ArrayList <NameValuePair>();
	    params.add(new BasicNameValuePair("__VIEWSTATE", __VIEWSTATE)); 
	    params.add(new BasicNameValuePair("btnQuiry", btnQuiry)); 
	    params.add(new BasicNameValuePair("ddlCheckMode", ddlCheckMode)); 
	    params.add(new BasicNameValuePair("ddlNofollowLink", ddlNofollowLink)); 
	    params.add(new BasicNameValuePair("ddlSearchMode", ddlSearchMode)); 
	    params.add(new BasicNameValuePair("linkcount", linkcount)); 
	    params.add(new BasicNameValuePair("linkdomains", linkdomains)); 
	    params.add(new BasicNameValuePair("linkswebsite", linkswebsite)); 
	    params.add(new BasicNameValuePair("selall", selall)); 
	    params.add(new BasicNameValuePair("selbaidupages", selbaidupages)); 
	    params.add(new BasicNameValuePair("selbaidurank", selbaidurank)); 
	    params.add(new BasicNameValuePair("selfl", selfl)); 
	    params.add(new BasicNameValuePair("selnofanmovefirst", selnofanmovefirst)); 
	    params.add(new BasicNameValuePair("selpr", selpr)); 
	    params.add(new BasicNameValuePair("selsnap", selsnap)); 
	    
	    params.add(new BasicNameValuePair("entime", entime)); 
	    params.add(new BasicNameValuePair("txtSiteUrl", url)); 
	    HttpEntity entity = new UrlEncodedFormEntity(params);

	    httpPost.setEntity(entity);
		return client.execute(httpPost);
	}
	
	public HttpResponse fetchEntime() throws ClientProtocolException, IOException {
		HttpGet get = new HttpGet(destUrl);
		return client.execute(get);
	}
	
	
	public static void main(String aegs[]) throws ClientProtocolException, IOException {
		LocalHttpClient client = new LocalHttpClient();
		System.out.println(EntityUtils.toString(client.fetchUrl("www.chinahti.com", "	hEXlDbI6kp||2xLE2//kxCJxW1ulzEh7").getEntity()));
//		System.out.println(EntityUtils.toString(client.fetchEntime().getEntity()));
//		System.out.println(EntityUtils.toString(client.fetchWeight("sina.allyes.com", "OGG9xdOiHTS2sT5YmWscOXweuzNeDPVA").getEntity()));
	}
	
}
