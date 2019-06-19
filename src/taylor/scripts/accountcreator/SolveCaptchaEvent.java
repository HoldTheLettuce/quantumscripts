package taylor.scripts.accountcreator;

import org.quantumbot.api.QuantumBot;
import org.quantumbot.events.BotEvent;

public class SolveCaptchaEvent extends BotEvent {

    private boolean hasRequested;

    private String taskId;

    public SolveCaptchaEvent(QuantumBot bot) {
        super(bot);
    }

    @Override
    public void step() {

    }
}
