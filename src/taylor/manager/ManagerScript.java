package taylor.manager;

import org.json.JSONObject;
import org.quantumbot.api.Script;
import org.quantumbot.interfaces.Logger;
import org.quantumbot.interfaces.Painter;
import org.quantumbot.utils.Timer;

import java.awt.*;

public abstract class ManagerScript extends Script implements Logger, Painter {

	private ManagerThread managerThread;

	private Timer stopTimer;

	private String scriptName;

	public ManagerScript(String scriptName) {
		this.scriptName = scriptName;
		this.managerThread = new ManagerThread(this);
		managerThread.start();
	}

	@Override
	public void onInit() {
		getBot().addPainter(this);
		init();

		if(getBot().hasArg("mproxy")) {
			info("Found proxy arg: " + getBot().getArg("mproxy", 0));
			getManagerThread().getConnection().setProxyId(getBot().getArg("mproxy", 0));
		}

		if(getBot().hasArg("stop")) {
			stopTimer = new Timer(Integer.parseInt(getBot().getArg("stop", 0)) * 60000);
		}
	}

	@Override
	public void onStart() {
		start();
	}

	@Override
	public void onLoop() throws InterruptedException {
		if(stopTimer != null && !stopTimer.isRunning()) {
			managerThread.exit();
			System.exit(1);
		}

		loop();
	}

	@Override
	public void onExit() {
		getBot().removePainter(this);
		managerThread.exit();
		exit();
	}

	@Override
	public void onPaint(Graphics2D g) {
		g.setColor(Color.GREEN);

		g.drawString("---------- MANAGER ----------", 20, 40);
		g.drawString(String.format("Connected: %s", getManagerThread().getConnection().isConnected()), 20, 80);
		g.drawString(String.format("Connection ID: %s", getManagerThread().getConnection().getConnectionId()), 20, 100);
		g.drawString(String.format("Account ID: %s", getManagerThread().getConnection().getAccount() == null ? "null" : getManagerThread().getConnection().getAccount().getId()), 20, 120);
		g.drawString(String.format("Proxy ID: %s", getManagerThread().getConnection().getProxyId()), 20, 140);

		g.setColor(Color.YELLOW);

		g.drawString("---------- SCRIPT ----------", 20, 180);

		onManagerPaint(g);
	}

	public ManagerThread getManagerThread() {
		return managerThread;
	}

	public String getScriptName() {
		return scriptName;
	}

	public abstract void onManagerMessage(String trigger, JSONObject content);
	public abstract void onManagerPaint(Graphics2D g);
	public abstract void init();
	public abstract void start();
	public abstract void loop() throws InterruptedException;
	public abstract void exit();
}