package am2.common.compat.artemislib;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

/**
 * Bootstrap for the optional ArtemisLib integration.
 *
 * <p>When ArtemisLib is present, AM2's shrink spell delegates hitbox resizing to
 * ArtemisLib's {@code ENTITY_HEIGHT}/{@code ENTITY_WIDTH} attribute system instead of
 * the player-only {@link am2.common.handler.ShrinkHandler}.  This lets shrink work on
 * any non-boss {@link net.minecraft.entity.EntityLivingBase}, not just players.
 */
public final class ArtemisLibCompat {

    public static final String MODID = "artemislib";

    private ArtemisLibCompat() {}

    public static boolean isLoaded() {
        return Loader.isModLoaded(MODID);
    }

    /**
     * Registers the ArtemisLib shrink handler. Safe to call unconditionally;
     * does nothing when ArtemisLib is absent.
     */
    public static void register() {
        if (!isLoaded()) return;
        MinecraftForge.EVENT_BUS.register(new ArtemisLibShrinkHandler());
    }
}
