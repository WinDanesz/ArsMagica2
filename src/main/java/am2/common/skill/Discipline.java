package am2.common.skill;

import am2.api.skill.SkillPoint;

public enum Discipline {
    FIRE("fire", 0xFF4400, 510, 172, 510, 172),
    ICE("ice", 0x44CCFF, 510, 172, 510, 172),
    LIGHTNING("lightning", 0x00FFFF, 510, 172, 510, 172),
    EARTH("earth", 0x00AA00, 510, 172, 510, 172),
    SORCERY("sorcery", 0x55FF55, 510, 172, 510, 172),
    HEALING("healing", 0xFFFF00, 510, 172, 510, 172),
    NECROMANCY("necromancy", 0xAA00FF, 820, 535, 853, 570);

    public static final int MAX_LEVEL = 100;

    // TODO: Temporary mutable overrides for hotswap testing. Move final values back to enum when done.
    // Index by ordinal: [texX, texY, focusX, focusY]
    public static int[][] POS_OVERRIDE = {
        {510, 172, 510, 172},   // FIRE
        {790, 280, 790, 280},   // ICE
        {230, 280, 230, 280},   // LIGHTNING
        {190, 532, 170, 542},   // EARTH
        {685, 790, 685, 790},   // SORCERY
        {370, 790, 370, 790},   // HEALING
        {835, 545, 873, 570},   // NECROMANCY
    };

    private final String name;
    private final int color;
    /** X position on the 1024x1024 background texture for the button. */
    private final int texX;
    /** Y position on the 1024x1024 background texture for the button. */
    private final int texY;
    /** X center on the 1024x1024 texture to focus when zooming in. */
    private final int focusX;
    /** Y center on the 1024x1024 texture to focus when zooming in. */
    private final int focusY;

    Discipline(String name, int color, int texX, int texY, int focusX, int focusY) {
        this.name = name;
        this.color = color;
        this.texX = texX;
        this.texY = texY;
        this.focusX = focusX;
        this.focusY = focusY;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    /** Returns the X position on the background texture. */
    public int getTexX() {
        return POS_OVERRIDE[ordinal()][0];
    }

    /** Returns the Y position on the background texture. */
    public int getTexY() {
        return POS_OVERRIDE[ordinal()][1];
    }

    /** Returns the X center on the texture to focus when zooming in. */
    public int getFocusX() {
        return POS_OVERRIDE[ordinal()][2];
    }

    /** Returns the Y center on the texture to focus when zooming in. */
    public int getFocusY() {
        return POS_OVERRIDE[ordinal()][3];
    }

    public String getUnlocalizedName() {
        return "discipline." + name;
    }

    /**
     * Returns the SkillPoint required to level up at the given current level.
     * Levels 0-29: Blue, 30-59: Green, 60-99: Red
     */
    public static SkillPoint getRequiredSkillPoint(int currentLevel) {
        if (currentLevel < 30) return SkillPoint.BLUE_SKILL_POINT;
        if (currentLevel < 60) return SkillPoint.GREEN_SKILL_POINT;
        return SkillPoint.RED_SKILL_POINT;
    }

    public static Discipline fromName(String name) {
        for (Discipline d : values()) {
            if (d.name.equals(name)) return d;
        }
        return null;
    }
}
