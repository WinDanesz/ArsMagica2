package am2.common.compat.artemislib;

import am2.common.registry.AMPotions;
import com.artemis.artemislib.util.attributes.ArtemisLibAttributes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

/**
 * Replaces {@link am2.common.handler.ShrinkHandler} when ArtemisLib is loaded.
 *
 * <p>Instead of manually resizing player hitboxes on every tick, this class applies
 * {@code ENTITY_HEIGHT} and {@code ENTITY_WIDTH} attribute modifiers when the
 * {@link AMPotions#shrink} potion effect is active. ArtemisLib's own tick handler then
 * propagates those modifiers to the entity's bounding box and renders the scaling —
 * and it works on any non-boss {@link EntityLivingBase}, not just players.
 */
public class ArtemisLibShrinkHandler {

    private static final UUID HEIGHT_UUID = UUID.fromString("a7f3e1b2-4c8d-4e5f-b6a7-c8d9e0f1a2b3");
    private static final UUID WIDTH_UUID  = UUID.fromString("b8c4f2d3-5d9e-4f6a-c7b8-d9e0f1a2b3c4");
    private static final String MODIFIER_NAME = "am2:shrink";
    /** -50 % via MULTIPLY_BASE (operation 1): base + base * (-0.5) = base * 0.5 */
    private static final double SHRINK_AMOUNT = -0.5;
    private static final int OPERATION_MULTIPLY_BASE = 1;

    @SubscribeEvent
    public void onPotionAdded(PotionEvent.PotionAddedEvent event) {
        if (event.getPotionEffect().getPotion() != AMPotions.shrink) return;
        if (event.getEntityLiving().world.isRemote) return;
        applyShrink(event.getEntityLiving());
    }

    @SubscribeEvent
    public void onPotionExpiry(PotionEvent.PotionExpiryEvent event) {
        if (event.getPotionEffect() == null) return;
        if (event.getPotionEffect().getPotion() != AMPotions.shrink) return;
        if (event.getEntityLiving().world.isRemote) return;
        removeShrink(event.getEntityLiving());
    }

    @SubscribeEvent
    public void onPotionRemove(PotionEvent.PotionRemoveEvent event) {
        if (event.getPotionEffect() == null) return;
        if (event.getPotionEffect().getPotion() != AMPotions.shrink) return;
        if (event.getEntityLiving().world.isRemote) return;
        removeShrink(event.getEntityLiving());
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        removeShrink(event.getEntityLiving());
    }

    private void applyShrink(EntityLivingBase entity) {
        applyModifier(entity, ArtemisLibAttributes.ENTITY_HEIGHT, HEIGHT_UUID);
        applyModifier(entity, ArtemisLibAttributes.ENTITY_WIDTH,  WIDTH_UUID);
    }

    private void removeShrink(EntityLivingBase entity) {
        removeModifier(entity, ArtemisLibAttributes.ENTITY_HEIGHT, HEIGHT_UUID);
        removeModifier(entity, ArtemisLibAttributes.ENTITY_WIDTH,  WIDTH_UUID);
    }

    private void applyModifier(EntityLivingBase entity, IAttribute attribute, UUID uuid) {
        IAttributeInstance inst = entity.getEntityAttribute(attribute);
        if (inst == null || inst.getModifier(uuid) != null) return;
        inst.applyModifier(new AttributeModifier(uuid, MODIFIER_NAME, SHRINK_AMOUNT, OPERATION_MULTIPLY_BASE));
    }

    private void removeModifier(EntityLivingBase entity, IAttribute attribute, UUID uuid) {
        IAttributeInstance inst = entity.getEntityAttribute(attribute);
        if (inst == null) return;
        AttributeModifier mod = inst.getModifier(uuid);
        if (mod != null) inst.removeModifier(mod);
    }
}
