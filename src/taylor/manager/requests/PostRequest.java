package taylor.manager.requests;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.json.JSONObject;
import org.quantumbot.interfaces.Logger;

public class PostRequest extends Request implements Logger {
	
	private JSONObject body;
	
	private String method = "POST";
	
	public PostRequest(String url, JSONObject body) {
		super(url);
		
		this.body = body;
	}
	
	@Override
	public void send() {
		try {
			HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
			
			con.setRequestMethod(method);
			
			con.setRequestProperty("Content-Type", "application/json");
			
			con.setDoOutput(true);
			
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			
			wr.writeBytes(body.toString());
			
			wr.flush();
			wr.close();
			
			responseCode = con.getResponseCode();
			
			info("Sending [" + method +  "] request to: " + url);
			info("Response Code: " + responseCode);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer responseBuffer = new StringBuffer();
			
			while((inputLine = in.readLine()) != null) {
				responseBuffer.append(inputLine);
			}
			
			in.close();
			
			response = responseBuffer.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
}
