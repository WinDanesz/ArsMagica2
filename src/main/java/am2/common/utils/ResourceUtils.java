package am2.common.utils;

import am2.ArsMagica;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class ResourceUtils {

    public static ResourceLocation getSkillIcon(String iconName) {
        return new ResourceLocation(ArsMagica.MODID, "items/spells/skills/" + iconName);
    }

    public static <K, V> HashMap<K, V> createHashMap(K i, V j) {
        HashMap<K, V> map = new HashMap<K, V>();
        map.put(i, j);
        return map;
    }
}
