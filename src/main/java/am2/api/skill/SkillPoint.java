package am2.api.skill;

import am2.common.config.AMConfig;
import net.minecraft.util.text.TextFormatting;

public class SkillPoint {

    public static final SkillPoint SILVER_POINT = new SkillPoint(-1, "Silver", TextFormatting.GRAY, 0x999999, -1, -1).disableRender();
    public static final SkillPoint BLUE_SKILL_POINT = new SkillPoint(1, "Blue", TextFormatting.BLUE, 0x0000ff, 0, 1);
    public static final SkillPoint GREEN_SKILL_POINT = new SkillPoint(2, "Green", TextFormatting.GREEN, 0x00ff00, 20, 2);
    public static final SkillPoint RED_SKILL_POINT = new SkillPoint(3, "Red", TextFormatting.RED, 0xff0000, 30, 2);
    public static final SkillPoint YELLOW_SKILL_POINT = new SkillPoint(4, "Yellow", TextFormatting.YELLOW, 0xffff00, 40, 3);
    public static final SkillPoint MAGENTA_SKILL_POINT = new SkillPoint(5, "Magenta", TextFormatting.LIGHT_PURPLE, 0xff00ff, 50, 3);
    public static final SkillPoint CYAN_SKILL_POINT = new SkillPoint(6, "Cyan", TextFormatting.AQUA, 0x00ffff, 60, 4);

    private final int tier;
    private final int color;
    private int minEarnLevel;
    private int levelsForPoint;
    private final String name;
    private final TextFormatting chatColor;

    private boolean render = true;

    public SkillPoint(int tier, String name, TextFormatting chatColor, int color, int minEarnLevel, int levelsForPoint) {
        this.tier = tier;
        this.color = color;
        this.name = name;
        this.minEarnLevel = minEarnLevel;
        this.levelsForPoint = levelsForPoint;
        this.chatColor = chatColor;
    }

    public int getTier() {
        return this.tier;
    }

    public int getColor() {
        return this.color;
    }

    public int getLevelsForPoint() {
        return this.levelsForPoint;
    }

    public int getMinEarnLevel() {
        return this.minEarnLevel;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public TextFormatting getChatColor() {
        return this.chatColor;
    }

    public boolean canRender() {
        return this.render;
    }

    public SkillPoint disableRender() {
        this.render = false;
        return this;
    }

    public static void loadFromConfig(AMConfig config) {
        BLUE_SKILL_POINT.minEarnLevel = config.getSkillPointBlueMinLevel();
        BLUE_SKILL_POINT.levelsForPoint = config.getSkillPointBlueLevelsPerPoint();
        GREEN_SKILL_POINT.minEarnLevel = config.getSkillPointGreenMinLevel();
        GREEN_SKILL_POINT.levelsForPoint = config.getSkillPointGreenLevelsPerPoint();
        RED_SKILL_POINT.minEarnLevel = config.getSkillPointRedMinLevel();
        RED_SKILL_POINT.levelsForPoint = config.getSkillPointRedLevelsPerPoint();
        YELLOW_SKILL_POINT.minEarnLevel = config.getSkillPointYellowMinLevel();
        YELLOW_SKILL_POINT.levelsForPoint = config.getSkillPointYellowLevelsPerPoint();
        MAGENTA_SKILL_POINT.minEarnLevel = config.getSkillPointMagentaMinLevel();
        MAGENTA_SKILL_POINT.levelsForPoint = config.getSkillPointMagentaLevelsPerPoint();
        CYAN_SKILL_POINT.minEarnLevel = config.getSkillPointCyanMinLevel();
        CYAN_SKILL_POINT.levelsForPoint = config.getSkillPointCyanLevelsPerPoint();
    }
}
