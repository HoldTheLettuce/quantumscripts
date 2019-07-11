package taylor.manager;

import org.quantumbot.interfaces.Logger;
import org.quantumbot.utils.Timer;

public class ManagerThread extends Thread implements Logger {

	public boolean run = true;

	private ManagerConnection connection;

	private ManagerScript script;

	private Timer pingTimer = new Timer(3000);

	public ManagerThread(ManagerScript script) {
		this.script = script;

		this.connection = new ManagerConnection(script);

		info("MThread made.");
	}

	public void run() {
		while(run) {
			try {
				sleep(500);

				if(!pingTimer.isRunning()) {
					if(connection.isConnected()) {
						connection.ping();
					} else {
						connection.connect();
					}

					pingTimer.reset();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void exit() {
		connection.disconnect();
		run = false;
	}

	public ManagerConnection getConnection() {
		return connection;
	}
}
