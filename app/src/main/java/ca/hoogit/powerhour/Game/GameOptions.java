package ca.hoogit.powerhour.Game;

import java.io.Serializable;

import ca.hoogit.powerhour.R;

/**
 * Created by jordon on 08/07/15.
 */
public class GameOptions implements Serializable {

    public enum Type {
        NONE, POWER_HOUR, CENTURY, SPARTAN, CUSTOM
    }

    private String title;
    private Type type;
    private int rounds;
    private int maxPauses;
    private int backgroundColor;
    private int accentColor;

    public GameOptions() {
    }

    public GameOptions(String title, Type type) {
        this.title = title;
        this.type = type;
        this.rounds = getRoundsForType(this.type);
        this.maxPauses = -1;
        this.backgroundColor = R.color.primary;
    }

    public GameOptions(String title, Type type, int rounds) {
        this.title = title;
        this.type = type;
        this.rounds = rounds;
        this.maxPauses = -1;
        this.backgroundColor = R.color.primary;
    }

    public GameOptions(String title, Type type, int rounds, int maxPauses) {
        this.title = title;
        this.type = type;
        this.rounds = rounds;
        this.maxPauses = maxPauses;
        this.backgroundColor = R.color.primary;
    }

    // Helpers
    public static Type intToType(int i) {
        switch (i) {
            case 1:
                return Type.POWER_HOUR;
            case 2:
                return Type.CENTURY;
            case 3:
                return Type.SPARTAN;
            case 4:
                return Type.CUSTOM;
            case 0:
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

    public static GameOptions getDefault(Type t) {
        switch (t) {
            case POWER_HOUR:
                return new GameOptions("Power Hour", Type.POWER_HOUR, 60, -1);
            case CENTURY:
                return new GameOptions("Century Club", Type.CENTURY, 100, -1);
            case SPARTAN:
                return new GameOptions("Spartan", Type.SPARTAN, 300, 3);
            case CUSTOM:
            default:
                return new GameOptions("Custom", Type.CUSTOM, 60, 5);
        }
    }

    // Accessors

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

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
