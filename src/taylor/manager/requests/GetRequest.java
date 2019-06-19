package taylor.manager.requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.quantumbot.interfaces.Logger;

public class GetRequest extends Request implements Logger {
	
	public GetRequest(String url) {
		super(url);
	}
	
	@Override
	public void send() {
		try {
			HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
			
			responseCode = con.getResponseCode();
			
			info("Sending [GET] request to: " + url);
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
}
