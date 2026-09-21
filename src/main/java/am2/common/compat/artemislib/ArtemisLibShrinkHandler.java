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
 * Replaces {@link am2.common.handler.ShrinkHandler} and {@link am2.common.handler.EnlargeHandler}
 * when ArtemisLib is loaded.
 *
 * <p>Applies ArtemisLib {@code ENTITY_HEIGHT}/{@code ENTITY_WIDTH} attribute modifiers when
 * {@link AMPotions#shrink} or {@link AMPotions#enlarge} is active. ArtemisLib's own tick
 * handler propagates those modifiers to the entity's bounding box and handles render scaling —
 * and it works on any non-boss {@link EntityLivingBase}, not just players.
 */
public class ArtemisLibShrinkHandler {

    // Shrink: -50% via MULTIPLY_BASE → base * (1 + (-0.5)) = base * 0.5
    private static final UUID SHRINK_HEIGHT_UUID = UUID.fromString("a7f3e1b2-4c8d-4e5f-b6a7-c8d9e0f1a2b3");
    private static final UUID SHRINK_WIDTH_UUID  = UUID.fromString("b8c4f2d3-5d9e-4f6a-c7b8-d9e0f1a2b3c4");
    private static final double SHRINK_AMOUNT = -0.5;

    // Enlarge: +100% via MULTIPLY_BASE → base * (1 + 1.0) = base * 2.0
    private static final UUID ENLARGE_HEIGHT_UUID = UUID.fromString("c9d5e3f4-6e0a-4a7b-d8c9-e0f1a2b3c4d5");
    private static final UUID ENLARGE_WIDTH_UUID  = UUID.fromString("d0e6f4a5-7f1b-4b8c-e9d0-f1a2b3c4d5e6");
    private static final double ENLARGE_AMOUNT = 1.0;

    /** MULTIPLY_BASE (operation 1): result = base + base * value */
    private static final int OPERATION_MULTIPLY_BASE = 1;

    @SubscribeEvent
    public void onPotionAdded(PotionEvent.PotionAddedEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        var potion = event.getPotionEffect().getPotion();
        if (potion == AMPotions.shrink) applyShrink(event.getEntityLiving());
        else if (potion == AMPotions.enlarge) applyEnlarge(event.getEntityLiving());
    }

    @SubscribeEvent
    public void onPotionExpiry(PotionEvent.PotionExpiryEvent event) {
        if (event.getPotionEffect() == null) return;
        if (event.getEntityLiving().world.isRemote) return;
        var potion = event.getPotionEffect().getPotion();
        if (potion == AMPotions.shrink) removeShrink(event.getEntityLiving());
        else if (potion == AMPotions.enlarge) removeEnlarge(event.getEntityLiving());
    }

    @SubscribeEvent
    public void onPotionRemove(PotionEvent.PotionRemoveEvent event) {
        if (event.getPotionEffect() == null) return;
        if (event.getEntityLiving().world.isRemote) return;
        var potion = event.getPotionEffect().getPotion();
        if (potion == AMPotions.shrink) removeShrink(event.getEntityLiving());
        else if (potion == AMPotions.enlarge) removeEnlarge(event.getEntityLiving());
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        removeShrink(event.getEntityLiving());
        removeEnlarge(event.getEntityLiving());
    }

    private void applyShrink(EntityLivingBase entity) {
        applyModifier(entity, ArtemisLibAttributes.ENTITY_HEIGHT, SHRINK_HEIGHT_UUID, SHRINK_AMOUNT);
        applyModifier(entity, ArtemisLibAttributes.ENTITY_WIDTH,  SHRINK_WIDTH_UUID,  SHRINK_AMOUNT);
    }

    private void removeShrink(EntityLivingBase entity) {
        removeModifier(entity, ArtemisLibAttributes.ENTITY_HEIGHT, SHRINK_HEIGHT_UUID);
        removeModifier(entity, ArtemisLibAttributes.ENTITY_WIDTH,  SHRINK_WIDTH_UUID);
    }

    private void applyEnlarge(EntityLivingBase entity) {
        applyModifier(entity, ArtemisLibAttributes.ENTITY_HEIGHT, ENLARGE_HEIGHT_UUID, ENLARGE_AMOUNT);
        applyModifier(entity, ArtemisLibAttributes.ENTITY_WIDTH,  ENLARGE_WIDTH_UUID,  ENLARGE_AMOUNT);
    }

    private void removeEnlarge(EntityLivingBase entity) {
        removeModifier(entity, ArtemisLibAttributes.ENTITY_HEIGHT, ENLARGE_HEIGHT_UUID);
        removeModifier(entity, ArtemisLibAttributes.ENTITY_WIDTH,  ENLARGE_WIDTH_UUID);
    }

    private void applyModifier(EntityLivingBase entity, IAttribute attribute, UUID uuid, double amount) {
        IAttributeInstance inst = entity.getEntityAttribute(attribute);
        if (inst == null || inst.getModifier(uuid) != null) return;
        inst.applyModifier(new AttributeModifier(uuid, "am2:resize", amount, OPERATION_MULTIPLY_BASE));
    }

    private void removeModifier(EntityLivingBase entity, IAttribute attribute, UUID uuid) {
        IAttributeInstance inst = entity.getEntityAttribute(attribute);
        if (inst == null) return;
        AttributeModifier mod = inst.getModifier(uuid);
        if (mod != null) inst.removeModifier(mod);
    }
}
