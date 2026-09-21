package am2.common.handler;

import am2.common.registry.AMPotions;
import am2.common.utils.EntityUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.WeakHashMap;

/**
 * Handles entity enlargement when ArtemisLib is not present.
 *
 * <p>Uses potion events to apply/remove a 2× size change on any non-boss
 * {@link EntityLivingBase} that receives the {@link AMPotions#enlarge} effect.
 * Original dimensions are stored in a {@link WeakHashMap} so they can be
 * restored cleanly on expiry, forced removal (e.g. milk), or death.
 *
 * <p>When ArtemisLib IS present, {@link am2.common.compat.artemislib.ArtemisLibShrinkHandler}
 * handles enlargement via attribute modifiers instead and this handler is not registered.
 */
public class EnlargeHandler {

    private final WeakHashMap<EntityLivingBase, float[]> originalSizes = new WeakHashMap<>();

    @SubscribeEvent
    public void onPotionAdded(PotionEvent.PotionAddedEvent event) {
        if (event.getPotionEffect().getPotion() != AMPotions.enlarge) return;
        if (event.getEntityLiving().world.isRemote) return;
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.isNonBoss()) return;
        if (originalSizes.containsKey(entity)) return; // already enlarged
        originalSizes.put(entity, new float[]{entity.width, entity.height});
        EntityUtils.setSize(entity, entity.width * 2f, entity.height * 2f);
    }

    @SubscribeEvent
    public void onPotionExpiry(PotionEvent.PotionExpiryEvent event) {
        if (event.getPotionEffect() == null) return;
        if (event.getPotionEffect().getPotion() != AMPotions.enlarge) return;
        if (event.getEntityLiving().world.isRemote) return;
        restoreSize(event.getEntityLiving());
    }

    @SubscribeEvent
    public void onPotionRemove(PotionEvent.PotionRemoveEvent event) {
        if (event.getPotionEffect() == null) return;
        if (event.getPotionEffect().getPotion() != AMPotions.enlarge) return;
        if (event.getEntityLiving().world.isRemote) return;
        restoreSize(event.getEntityLiving());
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        restoreSize(event.getEntityLiving());
    }

    private void restoreSize(EntityLivingBase entity) {
        float[] orig = originalSizes.remove(entity);
        if (orig != null) {
            EntityUtils.setSize(entity, orig[0], orig[1]);
        }
    }
}
