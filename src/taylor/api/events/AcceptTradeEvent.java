package taylor.api.events;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.events.BotEvent;
import org.quantumbot.events.interactions.WidgetInteractEvent;

public class AcceptTradeEvent extends BotEvent {

    public AcceptTradeEvent(QuantumBot bot) {
        super(bot);
    }

    @Override
    public void step() throws InterruptedException {
        if(getBot().getTradeInventory().isOfferOpen()) {
            WidgetInteractEvent e = new WidgetInteractEvent(getBot(), w -> w.getRootId() == 335 && w.getFirstId() == 10 && w.getSecondId() == -1, "Accept");

            e.execute();

            if(e.isComplete()) {
                sleepUntil(1500, () -> getBot().getTradeInventory().isConfirmOpen());
                setComplete();
            } else if(e.isFailed()) {
                setFailed();
            }
        } else if(getBot().getTradeInventory().isConfirmOpen()) {
            WidgetInteractEvent e = new WidgetInteractEvent(getBot(), w -> w.getRootId() == 334 && w.getFirstId() == 13 && w.getSecondId() == -1, "Accept");

            e.execute();

            if(e.isComplete()) {
                sleepUntil(1500, () -> !getBot().getTradeInventory().isOpen());
                setComplete();
            } else {
                setFailed();
            }
        } else {
            setFailed();
        }
    }
}
