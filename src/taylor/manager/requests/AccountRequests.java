package taylor.manager.requests;

import org.json.JSONObject;
import taylor.Config;
import taylor.api.requests.Request;

public class AccountRequests {

    public static Request get(int skip, int limit, boolean inUse) {
        return new Request(String.format("%s/api/accounts/?skip=%s&limit=%s&inUse=%s", Config.HOST, skip, limit, inUse), "GET").send();
    }

    public static Request getOneById(String id) {
        return new Request(String.format("%s/api/accounts/%s", Config.HOST, id), "GET").send();
    }

    public static Request deleteOne(String id) {
        return new Request(String.format("%s/api/accounts/%s", Config.HOST, id), "DELETE").send();
    }

    public static Request updateOne(String id, boolean inUse) {
        return new Request(String.format("%s/api/accounts/%s", Config.HOST, id), "PUT").setBody(new JSONObject().put("inUse", inUse).toString()).send();
    }
}
