package taylor.scripts.test;

import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.interfaces.Logger;

import taylor.api.requests.Request;
import taylor.manager.ManagerScript;
import taylor.manager.requests.AccountRequests;
import taylor.manager.requests.BotRequests;

@ScriptManifest(author = "Taylor", description = "", image = "", name = "Test", version = 0.1)
public class Test extends ManagerScript implements Logger {

	@Override
	public void init() {
		getManager().setScript("Test");
	}
	@Override
	public void start() {
	}

	@Override
	public void loop() throws InterruptedException {
		sleep(1000);
	}

	@Override
	public void exit() {
		
	}
}
