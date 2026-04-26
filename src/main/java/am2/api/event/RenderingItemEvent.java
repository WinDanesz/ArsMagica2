package am2.api.event;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired during the rendering of an AM2 item that uses a custom item renderer.
 *
 * <p>Listeners can inspect the {@link ItemStack}, the current
 * {@link TransformType} (first-person, GUI, ground, etc.), and the holding
 * entity to perform additional rendering or cancel default rendering logic.</p>
 *
 * <p>Posted on {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 * This event is client-side only.</p>
 */
public class RenderingItemEvent extends Event {

    private final ItemStack stack;
    private final TransformType cameraTransformType;
    private final EntityLivingBase entity;

    /**
     * @param stack                the item stack being rendered
     * @param cameraTransformType  the perspective/transform context (e.g. first-person, GUI)
     * @param entity               the entity holding the item, or {@code null} if none
     */
    public RenderingItemEvent(ItemStack stack, TransformType cameraTransformType, EntityLivingBase entity) {
        this.stack = stack;
        this.cameraTransformType = cameraTransformType;
        this.entity = entity;
    }

    /** @return the item stack being rendered */
    public ItemStack getStack() {
        return stack;
    }

    /** @return the camera/perspective transform type (GUI, first-person, ground, etc.) */
    public TransformType getCameraTransformType() {
        return cameraTransformType;
    }

    /** @return the entity holding the item, or {@code null} if rendered without an entity context */
    public EntityLivingBase getEntity() {
        return entity;
    }
}
