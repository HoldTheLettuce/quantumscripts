package taylor.scripts.plankmaker;

import org.quantumbot.api.Script;
import org.quantumbot.api.enums.Bank;
import org.quantumbot.api.map.Area;
import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.events.WebWalkEvent;
import org.quantumbot.events.containers.BankEvent;
import org.quantumbot.events.interactions.NPCInteractEvent;
import org.quantumbot.events.interactions.WidgetInteractEvent;
import org.quantumbot.interfaces.Logger;

@ScriptManifest(author = "Taylor", description = "", image = "", name = "PlankMaker", version = 0.1)
public class PlankMaker extends Script implements Logger {
	
	private Area LUMBERYARD = new Area(3299, 3491, 3306, 3486);
	
	@Override
	public void onLoop() throws InterruptedException {
		if(getBot().getInventory().contains("Coins") && getBot().getInventory().contains("Oak logs")) {
			if(LUMBERYARD.contains(getBot().getPlayers().getLocal())) {
				if(isPlankInterfaceOpen()) {
					if(new WidgetInteractEvent(getBot(), w -> w.getTooltip().contains("All")).execute().isComplete()) {
						
					}
				} else {
					if(new NPCInteractEvent(getBot(), "Sawmill operator", false, "Buy-plank").setWalk(false).execute().isComplete()) {
						sleepUntil(4000, () -> isPlankInterfaceOpen());
					}
				}
			} else {
				new WebWalkEvent(getBot(), LUMBERYARD).execute();
			}
		} else {
			new BankEvent(getBot(), Bank.VARROCK_EAST_BANK).addReq(Integer.MAX_VALUE, "Coins").addReq(Integer.MAX_VALUE, "Oak logs").execute();
		}
	}
	
	private boolean isPlankInterfaceOpen() {
		return getBot().getWidgets().contains(w -> w.containsText("What wood do you want converting to planks?"));
	}
}
