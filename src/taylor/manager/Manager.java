package taylor.manager;

import org.json.JSONObject;
import org.quantumbot.interfaces.Logger;
import org.quantumbot.utils.Timer;

import taylor.Config;
import taylor.api.requests.PostRequest;
import taylor.manager.types.Account;

public class Manager extends Thread implements Logger {

	private boolean run = true, isConnected;

	private Timer pingTimer;

	private Account account;

	private String proxyId, botId = "Unknown", botStatus = "Unknown";

	public Manager(String proxyId) {
		this.proxyId = proxyId;
	}

	public void run() {
		while(run) {
			try {
				sleep(500);

				if(isConnected) {
					if(pingTimer == null || !pingTimer.isRunning()) {
						ping();

						pingTimer = new Timer(3000);
					}
				} else {
					connect();

					sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void connect() {
		PostRequest connectReq = new PostRequest(Config.LOCAL_SERVER_HOST + "/api/bots/connect", new JSONObject());

		connectReq.send();

		if(connectReq.isSuccessful()) {
			botId = connectReq.toJSONObject().getString("id");
			isConnected = true;
		}
	}

	public void disconnect() {
		new PostRequest(Config.LOCAL_SERVER_HOST + "/api/bots/disconnect", new JSONObject().put("id", botId)).send();

		if(account != null) {
			new PostRequest(Config.MASTER_SERVER_HOST + "/api/accounts/status", new JSONObject().put("id", account.getId()).put("inUse", false)).send();
		}

		if(proxyId != null) {
			new PostRequest(Config.MASTER_SERVER_HOST + "/api/proxies/status", new JSONObject().put("id", proxyId).put("inUse", false)).send();
		}
	}

	private void ping() {
		pingAccount();
		pingProxy();

		// Ping bot & handle command

		PostRequest pingReq = new PostRequest(Config.LOCAL_SERVER_HOST + "/api/bots/ping", new JSONObject().put("id", botId).put("status", botStatus));

		pingReq.send();

		if(pingReq.isSuccessful()) {
			isConnected = true;

			info(pingReq.toJSONObject().toString());

			JSONObject res = pingReq.toJSONObject();

			String command = res.getString("command");

			switch(command) {
			case "stop":
				disconnect();

				System.exit(1);
				break;
			}

			new PostRequest(Config.LOCAL_SERVER_HOST + "/api/bots/clear", new JSONObject().put("id", botId)).send();
		} else {
			isConnected = false;
		}
	}

	public void pingAccount() {
		if(account != null) {
			new PostRequest(Config.MASTER_SERVER_HOST + "/api/accounts/status", new JSONObject().put("id", account.getId()).put("inUse", true)).send();
		}
	}

	public void pingProxy() {
		if(proxyId != null) {
			new PostRequest(Config.MASTER_SERVER_HOST + "/api/proxies/status", new JSONObject().put("id", proxyId).put("inUse", true)).send();
		}
	}

	public void deleteAccount() {
		new PostRequest(Config.MASTER_SERVER_HOST + "/api/accounts/delete", new JSONObject().put("id", account.getId())).send();
	}

	public boolean isConnected() {
		return isConnected;
	}

	public Account getAccount() {
		return account;
	}

	public String getBotId() {
		return botId;
	}

	public void setBotStatus(String status) {
		this.botStatus = status;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public void setRun(boolean run) {
		this.run = run;
	}
}
