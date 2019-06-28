package taylor.manager;

import org.json.JSONObject;
import org.quantumbot.interfaces.Logger;
import org.quantumbot.utils.Timer;

import taylor.api.requests.Request;
import taylor.manager.requests.AccountRequests;
import taylor.manager.requests.BotRequests;
import taylor.manager.requests.ProxyRequests;
import taylor.manager.types.Account;

public class Manager extends Thread implements Logger {

	private boolean run = true, isConnected;

	private Timer pingTimer;

	private Account account;

	private String proxyId, botId = "Unknown", botStatus = "Unknown", script = "Unknown";

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
		Request req = BotRequests.connect(script);

		if(req.isSuccessful()) {
			botId = req.toJSONObject().getString("id");
			isConnected = true;
		}
	}

	public void disconnect() {
		BotRequests.deleteOne(botId);

		if(account != null) {
			AccountRequests.updateOne(account.getId(), false);
		}

		if(proxyId != null) {
			ProxyRequests.updateOne(proxyId, false);
		}
	}

	private void ping() {
		pingAccount();
		pingProxy();

		// Ping bot & handle command

		Request pingReq = BotRequests.updateOne(botId, botStatus);

		if(pingReq.isSuccessful()) {
			isConnected = true;

			info(pingReq.toJSONObject().toString());

			JSONObject res = pingReq.toJSONObject();

			String message = res.getString("message");

			switch(message) {
				case "stop":
					disconnect();

					System.exit(1);
					break;
					
				default:
					info("Received unknown message");
					break;
			}
		} else {
			isConnected = false;
		}
	}

	public void pingAccount() {
		if(account != null) {
			AccountRequests.updateOne(account.getId(), true);
		}
	}

	public void pingProxy() {
		if(proxyId != null) {
			ProxyRequests.updateOne(proxyId, true);
		}
	}

	public void deleteAccount() {
		AccountRequests.deleteOne(account.getId());
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

	public void setScript(String script) { this.script = script; }

	public void setAccount(Account account) {
		this.account = account;
	}

	public void setRun(boolean run) {
		this.run = run;
	}
}
