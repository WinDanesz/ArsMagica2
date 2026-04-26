package am2.common.power;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class PowerTypes {

    private static int nextID = 1;
    private static ArrayList<PowerTypes> types = new ArrayList<>();
    public static final PowerTypes NONE = new PowerTypes("None");
    public static final PowerTypes LIGHT = new PowerTypes("Light", "\u00A7b", 1.25f, 1.0f, 0x00AAFF);
    public static final PowerTypes NEUTRAL = new PowerTypes("Neutral", "\u00A71", 1.0f, 1.0f, 0x0000AA);
    public static final PowerTypes DARK = new PowerTypes("Dark", "\u00A74", 0.75f, 1.0f, 0x770000);
    private int ID;
    private String name;
    private String chatColor;
    private int color;
    private float inputCostMultiplier;
    private float outputMultiplier;

    private PowerTypes(String name) {
        ID = 0;
        this.name = name;
        this.chatColor = TextFormatting.WHITE.toString();
        this.color = 0xffffff;
        this.inputCostMultiplier = 1f;
        this.outputMultiplier = 1f;
    }

    public PowerTypes(String name, String chatColor, float inputCostMultiplier, float outputMultiplier, int color) {
        ID = nextID;
        nextID *= 2;
        this.name = name;
        this.chatColor = chatColor;
        this.color = color;
        this.inputCostMultiplier = inputCostMultiplier;
        this.outputMultiplier = outputMultiplier;
        types.add(this);
    }

    public int ID() {
        return ID;
    }

    public String name() {
        return name.toLowerCase();
    }

    public static ArrayList<PowerTypes> all() {
        return types;
    }

    public int getColor() {
        return color;
    }

    public String getChatColor() {
        return chatColor;
    }

    public float getInputCostMultiplier() {
        return inputCostMultiplier;
    }

    public void setInputCostMultiplier(float multiplier) {
        this.inputCostMultiplier = multiplier;
    }

    public float getOutputMultiplier() {
        return outputMultiplier;
    }

    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PowerTypes && ((PowerTypes) obj).ID == this.ID;
    }

    public static List<PowerTypes> getTypes(int integer) {
        ImmutableList.Builder<PowerTypes> builder = ImmutableList.builder();
        for (PowerTypes type : all()) {
            if ((type.ID & integer) == type.ID)
                builder.add(type);
        }
        return builder.build();
    }

    public static PowerTypes getByID(int integer) {
        for (PowerTypes type : all()) {
            if (type.ID == integer)
                return type;
        }
        return NONE;
    }
}
