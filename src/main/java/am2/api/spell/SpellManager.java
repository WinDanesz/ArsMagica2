package am2.api.spell;

import java.util.ArrayList;
import java.util.List;

public class SpellManager {
    private static final ArrayList<Class<SpellComponent>> CHANNEL_COMPONENTS = new ArrayList<>();

    public static void registerChannelComponent(Class<SpellComponent> component) {
        if (!CHANNEL_COMPONENTS.contains(component))
            CHANNEL_COMPONENTS.add(component);
    }

    public static boolean areChannelComponentsPresent(SpellData data) {
        for (List<SpellPart> parts : data.getStages()) {
            for (SpellPart part : parts) {
                for (Class<SpellComponent> clazz : CHANNEL_COMPONENTS) {
                    if (clazz.isInstance(part))
                        return true;
                }
            }
        }
        return false;
    }
}
