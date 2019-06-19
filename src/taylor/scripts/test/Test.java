package taylor.scripts.test;

import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.interfaces.Logger;

import taylor.Config;
import taylor.manager.ManagerScript;

@ScriptManifest(author = "Taylor", description = "", image = "", name = "Test", version = 0.1)
public class Test extends ManagerScript implements Logger {
	
	@Override
	public void start() {
		
	}
	
	@Override
	public void loop() throws InterruptedException {
		info(Config.MASTER_SERVER_HOST);
		sleep(1000);
	}

	@Override
	public void exit() {
		
	}
}
