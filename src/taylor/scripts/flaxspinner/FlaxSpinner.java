package taylor.scripts.flaxspinner;

import org.quantumbot.api.Script;
import org.quantumbot.api.enums.Bank;
import org.quantumbot.api.map.Area;
import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.events.MakeXEvent;
import org.quantumbot.events.WebWalkEvent;
import org.quantumbot.events.containers.BankEvent;
import org.quantumbot.events.interactions.ObjectInteractEvent;
import org.quantumbot.interfaces.Logger;

import taylor.manager.ManagerScript;

@ScriptManifest(author = "Taylor", description = "", image = "", name = "FlaxSpinner", version = 0.1)
public class FlaxSpinner extends Script implements Logger {
	
	private final Area spinRoom = new Area(3203, 3209, 3206, 3206, 1);
	
	private long lastAnimation = System.currentTimeMillis();
	
	@Override
	public void onLoop() throws InterruptedException {
		if(getBot().getInventory().contains("Flax")) {
			if(getBot().getGameObjects().closest("Spinning wheel") != null) {
				if(getBot().getPlayers().getLocal().isAnimating()) {
					lastAnimation = System.currentTimeMillis();
					sleepAnimating(3000, 3000);
				} else if(isSpinInterfaceOpen()) {
					if(new MakeXEvent(getBot(), 3).execute().isComplete())
						sleepUntil(2000, () -> getBot().getPlayers().getLocal().isAnimating());
				} else {
					if(System.currentTimeMillis() - lastAnimation >= 4000 && new ObjectInteractEvent(getBot(), "Spinning wheel", "Spin").execute().isComplete())
						sleepUntil(3000, () -> isSpinInterfaceOpen());
				}
			} else {
				new WebWalkEvent(getBot(), spinRoom).execute();
			}
		} else {
			new BankEvent(getBot(), Bank.LUMBRIDGE_UPPER_BANK).addReq(Integer.MAX_VALUE, "Flax").execute();
		}
	}
	
	private boolean isSpinInterfaceOpen() {
		return getBot().getWidgets().contains(w -> w.containsText("What would you like to spin"));
	}
}
