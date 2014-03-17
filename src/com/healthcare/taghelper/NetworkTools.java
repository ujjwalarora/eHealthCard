package com.healthcare.taghelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkTools {
	
	private static HttpContext localContext = null;
	
	public static HttpResponse postData(String url, BasicNameValuePair params[]) {
		HttpResponse response = null;
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(url);

	    //Debug
	    Tools.debugLog(null, "Sending POST request");
	    Tools.debugLog(null, url);
	    
	    try {
	        // Add data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        for(BasicNameValuePair p : params) {
	        	
	        	//Debug
	        	Tools.debugLog(null, p.getName() + " : " + p.getValue());
	        	
	        	nameValuePairs.add(p);
	        }
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        response = httpclient.execute(httppost, localContext);
	        
	    } catch (ClientProtocolException e) {
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	    
	    return response;
	} 
	
	public static boolean isNetworkAvailable(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
	    // if no network is available networkInfo will be null
	    // otherwise check if we are connected
	    if (networkInfo != null && networkInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
	
	public static String getStringFromHttpResponse(HttpResponse response) {
		BufferedReader reader;
		StringBuilder builder = new StringBuilder();
		try {
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			for (String line = null; (line = reader.readLine()) != null;) {
			    builder.append(line).append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Debug
		Tools.debugLog(null, "Response string");
		Tools.debugLog(null, builder.toString());
		
		return builder.toString();
	}
	
	public static JSONObject getJSONObjectFromHttpResponse(HttpResponse response) {
		JSONObject jObj = null;
		String res = getStringFromHttpResponse(response);
		try {
			jObj = new JSONObject(res);
		} catch (JSONException e) {
			e.printStackTrace();
			Tools.debugLog(null, res);
		}
		return jObj;
	}
	
	public static void printJSONObject(JSONObject jObj) {
		@SuppressWarnings("rawtypes")
		Iterator i = jObj.keys();
		while(i.hasNext()) {
			String key = (String) i.next();
			try {
				Tools.debugLog(null, key + " : " + jObj.getString(key));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void initNetworking() {
		// Create a local instance of cookie store
	    CookieStore cookieStore = new BasicCookieStore();

	    // Create local HTTP context
	    localContext = new BasicHttpContext();
	    // Bind custom cookie store to the local context
	    localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}
	
	public static boolean isResponseValid(HttpResponse response, JSONObject jObj) {
		boolean result = false;
		if(jObj != null && response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			//Debug
			printJSONObject(jObj);
			
			result = true;
		}
		return result;
	}
	
	public static JSONArray getArrayOfKey(JSONObject jObj, String key) {
		JSONArray result = null;
		try {
			result = jObj.getJSONArray(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String getValueOfKey(JSONObject jObj, String key) {
		String result = "Error occurred in JSON parsing";
		try {
			result = jObj.getString(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
}
