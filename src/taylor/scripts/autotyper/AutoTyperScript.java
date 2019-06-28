package taylor.scripts.autotyper;

import org.quantumbot.api.map.Tile;
import org.quantumbot.events.WebWalkEvent;
import org.quantumbot.utils.Timer;
import taylor.manager.ManagerScript;

public class AutoTyperScript extends ManagerScript {

    private int interval = 5000, x = 3367, y = 3269;

    private String message = "white: R U N E B E T . ( om for black jack, dice, staking and more!";

    private Tile tile;

    private Timer timer;

    @Override
    public void init() {

    }

    @Override
    public void start() {
        if(getBot().hasArg("message")) {
            message = getBot().getArg("message", 0).replaceAll("_", "");
        }

        if(getBot().hasArg("interval")) {
            interval = Integer.parseInt(getBot().getArg("interval", 0));
        }

        if(getBot().hasArg("x")) {
            x = Integer.parseInt(getBot().getArg("x", 0));
        }

        if(getBot().hasArg("y")) {
            y = Integer.parseInt(getBot().getArg("y", 0));
        }

        tile = new Tile(x, y, 0);
        timer = new Timer(interval);
    }

    @Override
    public void loop() throws InterruptedException {
        if(getBot().getPlayers().getLocal().getTile() == tile) {
            if(!timer.isRunning()) {
                getBot().getKeyboard().type(message, true);
                timer.reset();
            }
        } else {
            new WebWalkEvent(getBot(), tile).execute();
        }
    }

    @Override
    public void exit() {

    }
}
