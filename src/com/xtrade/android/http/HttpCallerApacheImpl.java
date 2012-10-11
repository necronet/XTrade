package com.xtrade.android.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

import android.content.Context;
import android.content.SharedPreferences;

import com.xtrade.android.http.RestOption.Parameter;
import com.xtrade.android.util.Debug;
import com.xtrade.android.util.Settings;

public class HttpCallerApacheImpl extends AbstractHttpCaller {

	
	
	public HttpCallerApacheImpl(Context context) {
		super(context);
	} 

	public boolean call(URL urlResource, RestOption.Method methodType, Map<RestOption.Parameter, String> parameters) {
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		

		Debug.info("Request url " + urlResource.toString());
		switch (methodType) {
		case GET:
			HttpGet httpGet;
			try {
				httpGet = new HttpGet(urlResource.toURI());

				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				
				httpGet.setHeader("User-Agent", "Android-app");
				httpGet.setHeader("Content-Type", "application/json; charset=utf-8");
				SharedPreferences xTradeSettings = context.getSharedPreferences(Settings.SHARED_PREFERENCES,Context.MODE_PRIVATE);
				
				if(xTradeSettings.getBoolean(Settings.LOGGED_PREF, false)){
					httpGet.setHeader("Cookie",xTradeSettings.getString(Settings.COOKIE_PREF,null));
					Debug.info("Message "+xTradeSettings.getString(Settings.COOKIE_PREF,null));
				}
				
				String responseBody = httpClient.execute(httpGet, responseHandler);

				this.result = responseBody;
				return true;
			} catch (URISyntaxException urise) {
				urise.printStackTrace();
			} catch (ClientProtocolException cpe) {
				cpe.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			break;
		case PUT:
			break;
		case POST:
			HttpPost httpPost = null;
			try {
				httpPost = new HttpPost(urlResource.toURI());
				httpPost.addHeader("content-type", "application/json");
				
				if (parameters !=null && parameters.containsKey(Parameter.BODY)) {
					// this request comes with an object to go in the body
					StringEntity entity = new StringEntity(parameters.get(Parameter.BODY));

					httpPost.setEntity(entity);
				}
				BasicHttpContext httpContext = new BasicHttpContext();
				CookieStore cookieStore = new BasicCookieStore();
				httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
				

				HttpResponse response = httpClient.execute(httpPost, httpContext);

				if (response.getStatusLine().getStatusCode() != 200) {
					throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
				}

				/*
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
				String output="";
				while ((output = br.readLine()) != null);//maybe we'll use it later
				*/
				Cookie cookie=cookieStore.getCookies().get(0);
				this.result =cookie.getName()+"=\""+cookie.getValue()+"\"";
				
				return true;
			} catch (URISyntaxException urise) {
				urise.printStackTrace();
			} catch (ClientProtocolException cpe) {
				cpe.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			break;
		case DELETE:
			break;
		}
		
		httpClient.getConnectionManager().shutdown();
		
		return false;
		
	}
	

}
