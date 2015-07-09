package ca.hoogit.powerhour.Game;

/**
 * Created by jordon on 08/07/15.
 */
public class GameOptions {

    public enum Type {
        NONE, POWER_HOUR, CENTURY, SPARTAN, CUSTOM
    }

    private String title;
    private Type type;
    private int rounds;
    private int maxPauses;

    public GameOptions(String title, int type) {
        this.title = title;
        this.type = intToType(type);
        this.rounds = getRoundsForType(this.type);
        this.maxPauses = -1;
    }

    public GameOptions(String title, int type, int rounds) {
        this.title = title;
        this.type = intToType(type);
        this.rounds = rounds;
        this.maxPauses = -1;
    }

    public GameOptions(String title, int type, int rounds, int maxPauses) {
        this.title = title;
        this.type = intToType(type);
        this.rounds = rounds;
        this.maxPauses = maxPauses;
    }

    // Helpers
    public Type intToType(int i) {
        switch (i) {
            case 1:
                return Type.POWER_HOUR;
            case 2:
                return Type.CENTURY;
            case 3:
                return Type.SPARTAN;
            case 4:
                return Type.CUSTOM;
            case 5:
            default:
                return Type.NONE;
        }
    }

    public int getRoundsForType(Type t) {
        switch (t) {
            case CENTURY:
                return 100;
            case SPARTAN:
                return 300;
            case POWER_HOUR:
            case CUSTOM:
                return 60;
            default:
                return -1;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public int getMaxPauses() {
        return maxPauses;
    }

    public void setMaxPauses(int maxPauses) {
        this.maxPauses = maxPauses;
    }
}
