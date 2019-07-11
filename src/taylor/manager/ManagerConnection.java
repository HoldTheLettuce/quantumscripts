package taylor.manager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.quantumbot.interfaces.Logger;
import taylor.api.requests.Request;
import taylor.manager.requests.AccountRequests;
import taylor.manager.requests.BotRequests;
import taylor.manager.requests.ProxyRequests;
import taylor.manager.types.Account;

public class ManagerConnection implements Logger {

    private boolean isConnected;

    private String proxyId;
    private String connectionId;
    private String state = "UNKNOWN";

    private Account account;

    private JSONObject customData;

    private ManagerScript script;

    public ManagerConnection(ManagerScript script) {
        this.script = script;
    }

    public void connect() {
        if(!isConnected) {
            Request req = BotRequests.connect(script.getScriptName());

            if(req.isSuccessful()) {
                isConnected = true;

                JSONObject res = req.toJSONObject();

                this.connectionId = res.getString("id");
            }
        }
    }

    public void ping() {
        if(isConnected) {
            if(account == null) {
                Request req = AccountRequests.get(0, 1, false);

                if(req.isSuccessful()) {
                    JSONArray accounts = req.toJSONArray();

                    if(accounts.length() > 0) {
                        JSONObject account = accounts.getJSONObject(0);

                        info(account);

                        String id = account.getString("_id");
                        String username = account.getString("username");
                        String password = account.getString("password");
                        boolean isMember = account.getBoolean("isMember");

                        this.account = new Account(id, username, password, isMember);
                    }
                }
            }

            if(proxyId != null)
                ProxyRequests.updateOne(proxyId, true);

            if(account != null)
                AccountRequests.updateOne(account.getId(), true);

            Request req = BotRequests.updateOne(connectionId, state, script.getBot().getClient().isInGame(), this.customData);

            if(req.isSuccessful()) {
                isConnected = true;

                JSONObject res = req.toJSONObject();
                JSONObject command = res.getJSONObject("command");

                if(!command.has("trigger"))
                    return;

                String trigger = command.getString("trigger");

                if(trigger.equals("CLOSE")) {
                    disconnect();
                    script.getManagerThread().run = false;
                    System.exit(1);
                }

                script.onManagerMessage(trigger, command.getJSONObject("content"));

                BotRequests.deleteCommand(connectionId);
            } else {
                isConnected = false;
            }
        }
    }

    public void disconnect() {
        if(isConnected) {
            if(proxyId != null && ProxyRequests.deleteOne(proxyId).isSuccessful())
                proxyId = null;

            if(account != null)
                AccountRequests.updateOne(account.getId(), false);

            if(isConnected && BotRequests.deleteOne(connectionId).isSuccessful())
                isConnected = false;
        }
    }

    public void setState(String state) {
        this.state = state.toUpperCase();
    }

    public void setProxyId(String proxyId) {
        this.proxyId = proxyId;
    }

    public void setCustomData(JSONObject data) {
        this.customData = data;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getProxyId() {
        return proxyId;
    }

    public Account getAccount() {
        return account;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
