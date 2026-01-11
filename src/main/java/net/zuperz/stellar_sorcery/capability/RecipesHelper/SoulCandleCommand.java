package net.zuperz.stellar_sorcery.capability.RecipesHelper;

public class SoulCandleCommand {
    public enum Target {
        SOUL_CANDLE,
        PLAYER_CLOSEST,
        ALL_PLAYERS,
        PLAYERS_IN_5_BLOCKS,
        PLANET
    }

    public enum Trigger {
        ON_START,
        EACH_TICK,
        AT_PROGRESS_HALF,
        ON_END
    }

    private final String command;
    private final Target target;
    private final Trigger trigger;

    public SoulCandleCommand(String command, Target target, Trigger trigger) {
        this.command = command;
        this.target = target;
        this.trigger = trigger;
    }

    public String getCommand() { return command; }
    public Target getTarget() { return target; }
    public Trigger getTrigger() { return trigger; }
}
