package taylor.scripts.runner;

import java.awt.Graphics2D;

import org.json.JSONObject;
import org.quantumbot.api.map.Area;
import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.events.CloseInterfacesEvent;
import org.quantumbot.events.LoginEvent;
import org.quantumbot.events.LogoutEvent;
import org.quantumbot.events.WebWalkEvent;
import org.quantumbot.events.containers.*;
import org.quantumbot.events.interactions.ObjectInteractEvent;
import org.quantumbot.events.interactions.PlayerInteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quantumbot.interfaces.Painter;

import org.quantumbot.utils.StringUtils;
import org.quantumbot.utils.Timer;
import taylor.api.events.AcceptTradeEvent;
import taylor.manager.ManagerScript;
import taylor.manager.types.Account;

@ScriptManifest(author = "Taylor", description = "", image = "", name = "Runner", version = 0)
public class RunnerScript extends ManagerScript implements Logger {

	private final Area
			CASTLE_WARS = new Area(2435, 3099, 2446, 3080),
			ALTAR = new Area(2568, 4855, 2600, 4822),
			DUEL_ARENA = new Area(3325, 3221, 3293, 3260),
			DUEL_RING_SPAWN = new Area(3325, 3221, 3295, 3263);

	private long lastTimeInTrade = System.currentTimeMillis();

	private String target;

	private int targetDurationMinutes;

	private Timer stopTimer;

	public RunnerScript() {
		super("LavaRunner");
	}

	@Override
	public void onManagerMessage(String trigger, JSONObject content) {
		info(trigger);
		info(content.toString());

		switch(trigger) {
			case "CHANGE_TARGET":
				this.target = content.getString("target");

				int minutes = Integer.parseInt(content.getString("minutes"));

				if(minutes > 0) {
					this.targetDurationMinutes = minutes;
					this.stopTimer = new Timer(minutes * 60000);
				}
				break;

			case "STOP":
				target = null;

				if(getBot().getClient().isInGame()) {
					stopTimer = null;
					targetDurationMinutes = 0;

					try {
						new LogoutEvent(getBot()).execute();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
		}
	}

	@Override
	public void init() {
		if(getBot().hasArg("target")) {
			this.target = getBot().getArg("target", 0).replaceAll("_", " ");

			info("Target : " + target);
		}
	}

	@Override
	public void start() {}

	@Override
	public void loop() throws InterruptedException {
		info(getBot().getClient().isLoginScreen());
		JSONObject customData = new JSONObject();

		customData.put("target", target == null ? "null" : target);
		customData.put("durationMinutes", targetDurationMinutes);
		customData.put("durationLeft", stopTimer == null ? 0 : stopTimer.getRemaining());

		getManagerThread().getConnection().setCustomData(customData);

		if(stopTimer != null && !stopTimer.isRunning()) {
			target = null;
			stopTimer = null;
			targetDurationMinutes = 0;

			new LogoutEvent(getBot()).execute();
		}

		if(!getBot().getClient().isInGame()) {
			getManagerThread().getConnection().setState("LOGIN_SCREEN");

			Account account = getManagerThread().getConnection().getAccount();

			if(target != null && account != null) {
				new LoginEvent(getBot(), account.getUsername(), account.getPassword()).execute();
			}

			sleep(500);
			return;
		}

		if(CASTLE_WARS.contains(getBot().getPlayers().getLocal())) {
			getManagerThread().getConnection().setState("BANKING");
			bank();
		} else if(ALTAR.contains(getBot().getPlayers().getLocal())) {
			getManagerThread().getConnection().setState("TRADING");
			trade();
		} else if(DUEL_RING_SPAWN.contains(getBot().getPlayers().getLocal())) {
			getManagerThread().getConnection().setState("TRAVERSING");
			if(getBot().getGameObjects().contains("Mysterious ruins") && new ObjectInteractEvent(getBot(), "Mysterious ruins").execute().isComplete()) {
				sleepUntil(7000, () -> ALTAR.contains(getBot().getPlayers().getLocal()));
			}
		} else {
			getManagerThread().getConnection().setState("TRAVERSING");
			new WebWalkEvent(getBot(), CASTLE_WARS).execute();
		}
	}

	@Override
	public void exit() {}

	@Override
	public void onManagerPaint(Graphics2D g) {
		g.drawString(String.format("Target: %s", target), 20, 220);

		if(getBot().getClient().isLoginScreen())
			return;

		g.drawString(String.format("Has Items: %s", hasItems()), 20, 240);
		g.drawString(String.format("Stamina Active: %s", isStaminaActive()), 20, 260);
		g.drawString(String.format("Target Found: %s", getBot().getPlayers().contains(this.target)), 20, 280);
		g.drawString(String.format("Stopping In: %s / %s (Minutes)", StringUtils.formatTime(stopTimer.getRemaining()), targetDurationMinutes), 20, 300);
	}

	private void trade() throws InterruptedException {
		if(getBot().getPlayers().contains(this.target)) {
			if(getBot().getTradeInventory().isOpen()) {
				lastTimeInTrade = System.currentTimeMillis();

				if(getBot().getTradeOffer().contains("Earth talisman") && !getBot().getTradeInventory().contains("Earth talisman")) {
					if(new TradeOfferEvent(getBot(), "Earth talisman", 1).execute().isComplete())
						sleepUntil(1000, () -> getBot().getTradeInventory().contains("Earth talisman"));
				} else if(getBot().getTradeOffer().contains("Binding necklace") && !getBot().getTradeInventory().contains("Binding necklace")) {
					if(new TradeOfferEvent(getBot(), "Binding necklace", 1).execute().isComplete())
						sleepUntil(1000, () -> getBot().getTradeInventory().contains("Binding necklace"));
				} else if(!getBot().getTradeInventory().contains("Pure essence")) {
					if(new TradeOfferEvent(getBot(), "Pure essence", 23).execute().isComplete())
						sleepUntil(1000, () -> getBot().getTradeInventory().contains("Pure essence"));
				} else {
					if(getBot().getTradeInventory().isAccepted()) {
						sleepUntil(500, () -> getBot().getTradeInventory().isConfirmOpen() || !getBot().getTradeInventory().isOpen());
					} else {
						new AcceptTradeEvent(getBot()).execute();
					}
				}
			} else {
				if(System.currentTimeMillis() - lastTimeInTrade >= 2000) {
					if(!hasItems()) {
						// Tele to castle wars
						if(getBot().getEquipment().contains("Ring of dueling(8~1)")) {
							if(new EquipmentInteractEvent(getBot(), "Ring of dueling(8~1)", "Castle Wars").execute().isComplete())
								sleepUntil(3500, () -> CASTLE_WARS.contains(getBot().getPlayers().getLocal()));
						} else if(getBot().getInventory().contains("Ring of dueling(8~1)")) {
							if(new InventoryInteractEvent(getBot(), "Ring of dueling(8~1)", "Wear").execute().isComplete())
								sleepUntil(3500, () -> getBot().getEquipment().contains("Ring of dueling(8~1)"));
						} else {
							new WebWalkEvent(getBot(), CASTLE_WARS).execute();
						}
					} else {
						// Trade player
						if(new PlayerInteractEvent(getBot(), this.target, "Trade with").execute().isComplete())
							sleepUntil(8000, () -> getBot().getTradeInventory().isOpen());
					}
				}
			}
		} else {
			sleepUntil(2000, () -> getBot().getPlayers().contains(this.target));
		}
	}

	private void bank() throws InterruptedException {
		if(!getBot().getEquipment().contains("Fire tiara")) {
			if(getBot().getInventory().contains("Fire tiara")) {
				if(getBot().getWidgets().hasOpenInterface())
					new CloseInterfacesEvent(getBot()).execute();

				if(new InventoryInteractEvent(getBot(), "Fire tiara", "Wear").execute().isComplete())
					sleepUntil(2000, () -> getBot().getEquipment().contains("Fire tiara"));
			} else {
				new BankEvent(getBot()).addReq(1, "Fire tiara").execute();
			}
		} else {
			if(hasItems()) {
				if(getBot().getWidgets().hasOpenInterface())
					new CloseInterfacesEvent(getBot()).execute();

				if(!getBot().getEquipment().contains("Ring of dueling(8~1)")) {
					if(new InventoryInteractEvent(getBot(), "Ring of dueling(8~1)", "Wear").execute().isComplete())
						sleepUntil(2000, () -> getBot().getEquipment().contains("Ring of dueling(8~1)"));
				} else if(getBot().getSettings().getRunEnergy() <= 35 && !isStaminaActive()) {
					if(new InventoryInteractEvent(getBot(), "Stamina potion(4~1)", "Drink").execute().isComplete())
						sleepUntil(2000, this::isStaminaActive);
				} else {
					if(new EquipmentInteractEvent(getBot(), "Ring of dueling(8~1)", "Duel Arena").execute().isComplete())
						sleepUntil(3500, () -> DUEL_ARENA.contains(getBot().getPlayers().getLocal()));
				}
			} else {
				BankEvent e = new BankEvent(getBot());

				e.addReq(1, "Stamina potion(4~1)");
				e.addReq(2, "Ring of dueling(8~1)");
				e.addReq(1, "Earth talisman").setNoted(false);
				e.addReq(1, "Binding necklace").setNoted(false);
				e.addReq(23, "Pure essence");

				e.execute().then(new CloseInterfacesEvent(getBot()));
			}
		}
	}


	private boolean hasItems() {
		return
				getBot().getInventory().contains("Ring of dueling(8~1)") &&
				getBot().getInventory().contains("Stamina potion(4~1)") &&
				getBot().getInventory().contains("Earth talisman") &&
				getBot().getInventory().filter(i -> i.getName().contains("Binding necklace") && !i.isNote()).size() > 0 &&
				getBot().getInventory().contains("Pure essence") &&
				getBot().getInventory().getAmount("Pure essence") == 23;
	}

	private boolean isStaminaActive() {
		return getBot().getVarps().getVarp(1575) > 0;
	}
}
