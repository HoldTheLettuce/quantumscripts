package taylor.api.requests;

import org.json.JSONArray;
import org.json.JSONObject;
import org.quantumbot.interfaces.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Request implements Logger {

    private String url, method, body, response;

    private int responseCode;

    public Request(String url, String method) {
        this.url = url;
        this.method = method.toUpperCase();
    }

    public Request send() {
        info(String.format("Attempting %s request to %s.", method, url));

        try {
            URL urlObj = new URL(url);

            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

            if(!method.equals("GET")) {
                con.setRequestMethod(method);
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                if(body != null) {
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());

                    wr.writeBytes(body);

                    wr.flush();
                    wr.close();
                }
            }

            responseCode = con.getResponseCode();

            info(String.format("Received response code %s.", responseCode));

            // Read response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer responseBuffer = new StringBuffer();

            while((inputLine = in.readLine()) != null) {
                responseBuffer.append(inputLine);
            }

            in.close();

            response = responseBuffer.toString();
        } catch(IOException e) {
            error(String.format("Failed to establish %s connection to %s.", method, url));
        }

        return this;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public boolean isSuccessful() {
        return getResponseCode() == 200;
    }

    public String getRawResponse() {
        return response;
    }

    public JSONObject toJSONObject() {
        return new JSONObject(response);
    }

    public JSONArray toJSONArray() {
        return new JSONArray(response);
    }

    public Request setBody(String body) {
        this.body = body;
        return this;
    }
}
