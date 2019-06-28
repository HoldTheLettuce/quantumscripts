package taylor.manager.requests;

import org.json.JSONObject;
import taylor.Config;
import taylor.api.requests.Request;

public class ProxyRequests {

    public static Request deleteOne(String id) {
        return new Request(String.format("%s/api/proxies/%s", Config.HOST, id), "DELETE").send();
    }

    public static Request updateOne(String id, boolean inUse) {
        return new Request(String.format("%s/api/proxies/%s", Config.HOST, id), "PUT").setBody(new JSONObject().put("inUse", inUse).toString()).send();
    }
}
