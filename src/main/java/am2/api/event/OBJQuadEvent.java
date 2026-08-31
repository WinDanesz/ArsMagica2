package am2.api.event;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired to resolve the tint index for a quad belonging to a named OBJ model material.
 *
 * <p>Listeners should check {@link #materialName} and, if applicable, set
 * {@link #tintIndex} to the desired color tint index (as used by
 * {@code IBakedModel.getColorFromItemstack}). The default value of {@code -1}
 * means no tinting is applied.</p>
 *
 * <p>Use the static helper {@link #post(String)} to fire this event and retrieve
 * the resolved tint index.</p>
 *
 * <p>Posted on {@link MinecraftForge#EVENT_BUS}.</p>
 */
public class OBJQuadEvent extends Event {

    /**
     * The tint index to apply to the quad. Set to a non-negative value to enable tinting.
     * Defaults to {@code -1} (no tint).
     */
    public int tintIndex = -1;
    /** The name of the OBJ material whose tint index is being queried. */
    public String materialName;

    /**
     * @param materialName the OBJ material name for which to resolve a tint index
     */
    public OBJQuadEvent(String materialName) {
        this.materialName = materialName;
    }

    /**
     * Convenience method that fires an {@code OBJQuadEvent} for the given material
     * name and returns the tint index set by any listener.
     *
     * @param str the OBJ material name
     * @return the tint index, or {@code -1} if no listener modified it
     */
    public static int post(String str) {
        OBJQuadEvent event = new OBJQuadEvent(str);
        MinecraftForge.EVENT_BUS.post(event);
        return event.tintIndex;
    }
}
