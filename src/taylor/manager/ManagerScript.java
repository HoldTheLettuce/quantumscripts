package taylor.manager;

import java.awt.Color;
import java.awt.Graphics2D;

import org.json.JSONArray;
import org.json.JSONObject;
import org.quantumbot.api.Script;
import org.quantumbot.enums.ResponseCode;
import org.quantumbot.events.LoginEvent;
import org.quantumbot.interfaces.Painter;
import org.quantumbot.listeners.LoginResponseListener;
import org.quantumbot.utils.StringUtils;
import org.quantumbot.utils.Timer;

import taylor.Config;
import taylor.manager.requests.GetRequest;
import taylor.manager.types.Account;

public abstract class ManagerScript extends Script implements Painter, LoginResponseListener {
	
	private long startAt;
	
	private int stopIn;

	private String proxyId;
	
	private Timer stopTimer;
	
	private Manager manager;
	
	@Override
	public void onInit() {
		startAt = System.currentTimeMillis();
		
		if(getBot().hasArg("stop")) {
			stopIn = Integer.parseInt(getBot().getArg("stop", 0));
			
			stopTimer = new Timer(stopIn * 60000);
		}

		if(getBot().hasArg("mproxy")) {
			proxyId = getBot().getArg("mproxy", 0);
		}

		manager = new Manager(proxyId);
		
		manager.start();
		
		getBot().addPainter(this);
		getBot().addLoginListener(this);
	}
	
	@Override
	public void onStart() {
		start();
	}
	
	@Override
	public void onLoop() throws InterruptedException {
		// Handle Stopping
		
		if(stopTimer != null && !stopTimer.isRunning()) {
			manager.disconnect();
			System.exit(1);
		}
		
		// Handle Login
		
		if(getBot().getClient().isLoginScreen()) {
			if(manager.getAccount() == null) {
				manager.setBotStatus("Retrieving Account");
				
				retrieveAccount();
				
				sleep(2000);
			} else {
				manager.setBotStatus("Logging In");
				new LoginEvent(getBot(), manager.getAccount().getUsername(), manager.getAccount().getPassword()).execute();
			}
		}
		
		loop();
	}
	
	@Override
	public void onExit() {
		exit();
		
		if(manager.isConnected())
			manager.disconnect();
		
		manager.setRun(false);
		
		getBot().removePainter(this);
		getBot().removeLoginListener(this);
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		g.setColor(Color.GREEN);
		
		g.drawString(String.format("Runtime: %s", StringUtils.formatTime(System.currentTimeMillis() - startAt)), 20, 60);
		
		if(stopTimer != null)
			g.drawString(String.format("Stopping In: %s", StringUtils.formatTime(stopTimer.getRemaining())), 20, 80);

		if(manager != null) {
			g.drawString(String.format("Connected: %s", manager.isConnected()), 20, 100);
			g.drawString(String.format("Bot Id: %s", manager.getBotId()), 20, 120);
			g.drawString(String.format("Proxy Id: %s", proxyId), 20, 140);

			if(manager.getAccount() != null)
				g.drawString(String.format("Account Id: %s", manager.getAccount().getId()), 20, 160);
		}
	}
	
	@Override
	public void onResponse(ResponseCode code) {
		if(code == ResponseCode.ACCOUNT_INACCESSIBLE) {
			manager.setAccount(null);
		} else if(code == ResponseCode.ACCOUNT_LOCKED || code == ResponseCode.DISABLED) {
			manager.deleteAccount();
			//manager.setAccount(null);
			manager.disconnect();
			System.exit(1);
		}
	}
	
	private void retrieveAccount() {
		GetRequest req = new GetRequest(Config.MASTER_SERVER_HOST + "/api/accounts?limit=1&inUse=false");
		
		req.send();
		
		if(req.isSuccessful()) {
			JSONArray res = req.toJSONArray();
			
			if(res.length() > 0) {
				JSONObject acc = res.getJSONObject(0);
				
				manager.setAccount(new Account(acc.getString("_id"), acc.getString("username"), acc.getString("password"), acc.getBoolean("isMember")));
				manager.pingAccount();
			}
		}
	}
	
	public Manager getManager() {
		return manager;
	}
	
	public abstract void start();
	
	public abstract void loop() throws InterruptedException;
	
	public abstract void exit();
}
