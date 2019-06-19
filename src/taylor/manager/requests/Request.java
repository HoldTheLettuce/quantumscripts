package taylor.manager.requests;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Request {
	
	public String url, response;
	
	public int responseCode;
	
	public HttpURLConnection httpConnection;
	
	public URL urlObj;
	
	protected Request(String url) {
		this.url = url;
		
		try {
			this.urlObj = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getRawResponse() {
		return response;
	}
	
	public int getResponseCode() {
		return responseCode;
	}
	
	public boolean isSuccessful() {
		return responseCode == 200;
	}
	
	public JSONObject toJSONObject() {
		return new JSONObject(response);
	}
	
	public JSONArray toJSONArray() {
		return new JSONArray(response);
	}
	
	public abstract void send();
}
