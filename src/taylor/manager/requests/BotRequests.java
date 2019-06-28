package taylor.manager.requests;

import org.json.JSONObject;
import taylor.Config;
import taylor.api.requests.Request;

public class BotRequests {

    public static Request connect(String script) {
        return new Request(String.format("%s/api/bots", Config.HOST), "POST").setBody(new JSONObject().put("script", script).toString()).send();
    }

    public static Request deleteOne(String id) {
        return new Request(String.format("%s/api/bots/%s", Config.HOST, id), "DELETE").send();
    }

    public static Request updateOne(String id, String state) {
        return new Request(String.format("%s/api/bots/%s", Config.HOST, id), "PUT").setBody(new JSONObject().put("state", state).toString()).send();
    }
}
