package taylor.scripts.runner;

import java.awt.Color;
import java.awt.Graphics2D;

import org.quantumbot.api.containers.Item;
import org.quantumbot.api.enums.Bank;
import org.quantumbot.api.map.Area;
import org.quantumbot.client.script.ScriptManifest;
import org.quantumbot.events.CloseInterfacesEvent;
import org.quantumbot.events.containers.BankOpenEvent;
import org.quantumbot.events.containers.DepositEvent;
import org.quantumbot.events.containers.EquipmentInteractEvent;
import org.quantumbot.events.containers.InventoryInteractEvent;
import org.quantumbot.events.containers.TradeOfferEvent;
import org.quantumbot.events.containers.WithdrawEvent;
import org.quantumbot.events.interactions.ObjectInteractEvent;
import org.quantumbot.events.interactions.PlayerInteractEvent;
import org.quantumbot.events.interactions.WidgetInteractEvent;
import org.quantumbot.interfaces.Logger;
import org.quantumbot.interfaces.Painter;

import taylor.manager.ManagerScript;

@ScriptManifest(author = "Taylor", description = "", image = "", name = "Runner", version = 0)
public class RunnerScript extends ManagerScript implements Logger, Painter {

	private final Area CASTLE_WARS = new Area(2435, 3099, 2446, 3080), ALTAR = new Area(2568, 4855, 2600, 4822), DUEL_ARENA = new Area(3325, 3221, 3293, 3260);

	private long lastTimeInTrade = System.currentTimeMillis();

	private String target;

	@Override
	public void start() {
		if(getBot().hasArg("target"))
			target = getBot().getArg("target", 0).replaceAll("_", " ");

		getBot().addPainter(this);
	}

	@Override
	public void loop() throws InterruptedException {
		if(CASTLE_WARS.contains(getBot().getPlayers().getLocal())) {
			getManager().setBotStatus("Banking");
			bank();
		} else if(ALTAR.contains(getBot().getPlayers().getLocal())) {
			getManager().setBotStatus("Trading");
			trade();
		} else {
			getManager().setBotStatus("Traversing");

			if(getBot().getGameObjects().contains("Mysterious ruins") && new ObjectInteractEvent(getBot(), "Mysterious ruins").execute().isComplete()) {
				sleepUntil(7000, () -> ALTAR.contains(getBot().getPlayers().getLocal()));
			}
		}
	}

	@Override
	public void exit() {
		getBot().removePainter(this);
	}

	@Override
	public void onPaint(Graphics2D g) {
		g.setColor(Color.YELLOW);

		if(getBot().getClient().isLoginScreen())
			return;

		g.drawString(String.format("Target: %s", target), 20, 220);
		g.drawString(String.format("Should Bank: %s", shouldBank()), 20, 240);
		g.drawString(String.format("Stamina Active: %s", isStaminaActive()), 20, 260);
		g.drawString(String.format("Target Found: %s", getBot().getPlayers().contains(this.target)), 20, 280);
	}

	@Override
	public void init() {

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
					if(new TradeOfferEvent(getBot(), "Pure essence", 25).execute().isComplete())
						sleepUntil(1000, () -> getBot().getTradeInventory().contains("Pure essence"));
				} else {
					if(!getBot().getTradeInventory().isAccepted())
						acceptTrade();
				}
			} else {
				if(System.currentTimeMillis() - lastTimeInTrade >= 1000) {
					if(shouldBank()) {
						// Tele to castle wars
						if(new EquipmentInteractEvent(getBot(), "Ring of dueling(8~1)", "Castle Wars").execute().isComplete())
							sleepUntil(3500, () -> CASTLE_WARS.contains(getBot().getPlayers().getLocal()));
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
		if(getBot().getBank().isOpen()) {
			// Deposit unknown items
			for(Item i : getBot().getInventory().getAll()) {
				if(!i.getName().equals("Fire tiara") && !i.getName().equals("Ring of dueling(8~1)") && !i.getName().equals("Earth talisman") && !i.getName().equals("Binding necklace") && !i.getName().equals("Pure essence") && !i.getName().equals("Stamina potion(4~1)")) {
					if(new DepositEvent(getBot(), i.getName(), Integer.MAX_VALUE).execute().isComplete()) {
						sleepUntil(1500, () -> !getBot().getInventory().contains(i.getName()));
					}
				}
			}

			if(getBot().getInventory().contains("Ring of dueling(8~1)") && !getBot().getEquipment().contains("Ring of dueling(8~1)")) {
				if(new CloseInterfacesEvent(getBot()).execute().isComplete())
					sleepUntil(1000, () -> !getBot().getWidgets().hasOpenInterface());
			} else {
				if(!getBot().getInventory().contains("Earth talisman")) {
					if(new WithdrawEvent(getBot(), "Earth talisman", 1).execute().isComplete())
						sleepUntil(1000, () -> getBot().getInventory().contains("Earth talisman"));
				} else if(!getBot().getInventory().contains("Binding necklace")) {
					if(new WithdrawEvent(getBot(), "Binding necklace", 1).execute().isComplete())
						sleepUntil(1000, () -> getBot().getInventory().contains("Binding necklace"));
				} else if(!getBot().getInventory().contains("Ring of dueling(8~1)") && !getBot().getEquipment().contains("Ring of dueling(8~1)")) {
					if(new WithdrawEvent(getBot(), "Ring of dueling(8)", 1).execute().isComplete())
						sleepUntil(1000, () -> getBot().getInventory().contains("Ring of dueling(8)"));
				} else if(!getBot().getInventory().contains("Stamina potion(4~1)") && getBot().getInventory().getAmount("Pure essence") >= 25 && getBot().getInventory().getEmptySlots() > 0) {
					if(new WithdrawEvent(getBot(), "Stamina potion(4~1)", 1).execute().isComplete())
						sleepUntil(1000, () -> getBot().getInventory().contains("Stamina potion(4~1)"));
				} else if(!getBot().getInventory().contains("Pure essence") || getBot().getInventory().getAmount("Pure essence") < 25) {
					if(new WithdrawEvent(getBot(), "Pure essence", 25).execute().isComplete())
						sleepUntil(1000, () -> getBot().getInventory().contains("Pure essence"));
				} else {
					if(new CloseInterfacesEvent(getBot()).execute().isComplete())
						sleepUntil(1000, () -> !getBot().getWidgets().hasOpenInterface());
				}
			}
		} else {
			if(getBot().getInventory().contains("Fire tiara")) {
				if(new InventoryInteractEvent(getBot(), "Fire tiara", "Wear").execute().isComplete())
					sleepUntil(1500, () -> getBot().getEquipment().contains("Fire tiara"));
			} else if(getBot().getSettings().getRunEnergy() <= 35 && getBot().getInventory().contains("Stamina potion(6~1)") && !isStaminaActive()) {
				if(new InventoryInteractEvent(getBot(), "Stamina potion(6~1)", "Drink").execute().isComplete())
					sleepUntil(2500, () -> isStaminaActive());
			} else if(getBot().getInventory().contains("Ring of dueling(8~1)") && !getBot().getEquipment().contains("Ring of dueling(8~1)")) {
				if(getBot().getWidgets().hasOpenInterface()) {
					if(new CloseInterfacesEvent(getBot()).execute().isComplete())
						sleepUntil(1000, () -> !getBot().getWidgets().hasOpenInterface());
				} else {
					if(new InventoryInteractEvent(getBot(), "Ring of dueling(8~1)", "Wear").execute().isComplete())
						sleepUntil(2000, () -> getBot().getEquipment().contains("Ring of dueling(8~1)"));
				}
			} else {
				if(shouldBank()) {
					if(new BankOpenEvent(getBot(), Bank.CASTLE_WARS_BANK).execute().isComplete())
						sleepUntil(3000, () -> getBot().getBank().isOpen());
				} else {
					if(new EquipmentInteractEvent(getBot(), "Ring of dueling(8~1)", "Duel Arena").execute().isComplete())
						sleepUntil(3500, () -> DUEL_ARENA.contains(getBot().getPlayers().getLocal()));
				}
			}
		}
	}

	private void acceptTrade() throws InterruptedException {
		if(getBot().getTradeInventory().isOfferOpen()) {
			if(new WidgetInteractEvent(getBot(), w -> w.getRootId() == 335 && w.getFirstId() == 10 && w.getSecondId() == -1, "Accept").execute().isComplete()) {
				sleepUntil(1500, () -> getBot().getTradeInventory().isConfirmOpen());
			}
		} else if(getBot().getTradeInventory().isConfirmOpen()) {
			if(new WidgetInteractEvent(getBot(), w -> w.getRootId() == 334 && w.getFirstId() == 13 && w.getSecondId() == -1, "Accept").execute().isComplete()) {
				sleepUntil(1500, () -> !getBot().getTradeInventory().isOpen());
			}
		}
	}

	private boolean shouldBank() {
		return !getBot().getInventory().contains("Earth talisman") ||
			   !getBot().getInventory().contains("Binding necklace") ||
			   (!getBot().getInventory().contains("Ring of dueling(8~1)") && !getBot().getEquipment().contains("Ring of dueling(8~1)")) ||
			   (!getBot().getInventory().contains("Pure essence") || getBot().getInventory().getAmount("Pure essence") < 25);
	}

	private boolean isStaminaActive() {
		return getBot().getVarps().getVarp(1575) > 0;
	}
}
