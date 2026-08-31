package am2.common.handler;

import am2.ArsMagica;
import am2.api.event.SpellCastEvent;
import am2.common.blocks.tileentity.TileEntityAstralBarrier;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMItems;
import am2.common.registry.AMPotions;
import am2.common.utils.DimensionUtilities;
import am2.common.utils.KeystoneUtilities;
import am2.common.utils.SelectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;


public class PotionEffectHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void playerPreDeathEvent(LivingDeathEvent e) {
        PotionEffect effect = e.getEntityLiving().getActivePotionEffect(AMPotions.temporal_anchor);
        if (effect != null) {
            e.setCanceled(true);
            e.getEntityLiving().removePotionEffect(AMPotions.temporal_anchor);
            restoreAnchor(e.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void onPotionExpiry(PotionEvent.PotionExpiryEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (event.getPotionEffect() != null && event.getPotionEffect().getPotion() == AMPotions.temporal_anchor) {
            restoreAnchor((EntityLivingBase) event.getEntity());
        }
    }

    private void restoreAnchor(EntityLivingBase entity) {
        EntityExtension ext = EntityExtension.For(entity);
        if (ext == null || ext.getAnchorDimensionID() == -512) {
            entity.setHealth(Math.max(entity.getHealth(), 1.0f));
            return;
        }

        // Teleport back
        if (entity.dimension == ext.getAnchorDimensionID()) {
            entity.setPositionAndUpdate(ext.getAnchorX(), ext.getAnchorY(), ext.getAnchorZ());
        } else {
            entity.setPosition(ext.getAnchorX(), ext.getAnchorY(), ext.getAnchorZ());
            DimensionUtilities.doDimensionTransfer(entity, ext.getAnchorDimensionID());
        }

        // Restore health
        entity.setHealth(ext.getAnchorHealth() > 0 ? ext.getAnchorHealth() : 1.0f);
        entity.hurtResistantTime = 20;
        entity.motionX = 0;
        entity.motionY = 0;
        entity.motionZ = 0;
        entity.fallDistance = 0;
        entity.extinguish();

        NBTTagCompound extra = ext.getAnchorExtraData();
        if (extra != null) {
            entity.setAbsorptionAmount(extra.getFloat("Absorption"));
            entity.setAir(extra.getInteger("Air"));

            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                if (extra.hasKey("FoodStats"))
                    player.getFoodStats().readNBT(extra.getCompoundTag("FoodStats"));
            }

            // Clear current effects then re-apply saved ones
            List<Potion> toRemove = new ArrayList<>(entity.getActivePotionMap().keySet());
            for (Potion p : toRemove)
                entity.removePotionEffect(p);

            if (extra.hasKey("ActiveEffects", 9)) {
                NBTTagList list = extra.getTagList("ActiveEffects", 10);
                for (int i = 0; i < list.tagCount(); i++) {
                    PotionEffect restored = PotionEffect.readCustomPotionEffectFromNBT(list.getCompoundTagAt(i));
                    if (restored != null)
                        entity.addPotionEffect(restored);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void entityDamageEvent(LivingHurtEvent event) {
        if (event.isCanceled()) return;

        if (event.getSource().damageType.equals(DamageSource.OUT_OF_WORLD.damageType)) return;

        if (event.getEntityLiving().isPotionActive(AMPotions.magic_shield))
            event.setAmount(event.getAmount() * 0.25f);

        float damage = EntityExtension.For(event.getEntityLiving()).protect(event.getAmount());
        event.setAmount(damage);
    }

    @SubscribeEvent
    public void playerJumpEvent(LivingJumpEvent event) {
        if (event.getEntityLiving().isPotionActive(AMPotions.agility)) {
            event.getEntityLiving().motionY *= 1.5f;
        }
        if (event.getEntityLiving().isPotionActive(AMPotions.leap)) {

            Entity velocityTarget = event.getEntityLiving();

            if (event.getEntityLiving().getRidingEntity() != null) {
                if (event.getEntityLiving().getRidingEntity() instanceof EntityMinecart) {
                    event.getEntityLiving().getRidingEntity().setPosition(event.getEntityLiving().getRidingEntity().posX, event.getEntityLiving().getRidingEntity().posY + 1.5, event.getEntityLiving().getRidingEntity().posZ);
                }
                velocityTarget = event.getEntityLiving().getRidingEntity();
            }

            double yVelocity = 0;
            double xVelocity = 0;
            double zVelocity = 0;

            Vec3d vec = event.getEntityLiving().getLookVec().normalize();
            yVelocity = ArsMagica.config.getJumpBoostFactor() * (event.getEntityLiving().getActivePotionEffect(AMPotions.leap).getAmplifier() + 1);
            xVelocity = velocityTarget.motionX * (Math.pow(2, event.getEntityLiving().getActivePotionEffect(AMPotions.leap).getAmplifier())) * Math.abs(vec.x);
            zVelocity = velocityTarget.motionZ * (Math.pow(2, event.getEntityLiving().getActivePotionEffect(AMPotions.leap).getAmplifier())) * Math.abs(vec.z);

            float maxHorizontalVelocity = 1.45f;

            if (event.getEntityLiving().getRidingEntity() != null && (event.getEntityLiving().getRidingEntity() instanceof EntityMinecart || event.getEntityLiving().getRidingEntity() instanceof EntityBoat) || event.getEntityLiving().isPotionActive(AMPotions.haste)) {
                maxHorizontalVelocity += 25;
                xVelocity *= 2.5;
                zVelocity *= 2.5;
            }

            if (xVelocity > maxHorizontalVelocity) {
                xVelocity = maxHorizontalVelocity;
            } else if (xVelocity < -maxHorizontalVelocity) {
                xVelocity = -maxHorizontalVelocity;
            }

            if (zVelocity > maxHorizontalVelocity) {
                zVelocity = maxHorizontalVelocity;
            } else if (zVelocity < -maxHorizontalVelocity) {
                zVelocity = -maxHorizontalVelocity;
            }

            if (EntityExtension.For(event.getEntityLiving()).getIsFlipped()) {
                yVelocity *= -1;
            }

            velocityTarget.addVelocity(xVelocity, yVelocity, zVelocity);
        }
        if (event.getEntityLiving().isPotionActive(AMPotions.entangle)) {
            event.getEntityLiving().motionY = 0;
        }
    }

    @SubscribeEvent
    public void livingFall(LivingFallEvent e) {
        if (e.getEntityLiving().isPotionActive(AMPotions.agility)) {
            e.setDistance(e.getDistance() / 1.5F);
        }
        if (e.getEntityLiving().isPotionActive(AMPotions.leap)) {
            if (e.getDistance() < (e.getEntityLiving().getActivePotionEffect(AMPotions.leap).getAmplifier() + 1) * 10) {
                e.setCanceled(true);
            } else {
                e.setDistance(e.getDistance() - (e.getEntityLiving().getActivePotionEffect(AMPotions.leap).getAmplifier() + 1) * 10);
            }
        }
    }

    @SubscribeEvent
    public void spellCast(SpellCastEvent.Pre e) {
        if (e.entityLiving.isPotionActive(AMPotions.clarity)) {
            e.manaCost = 0;
            e.burnout = 0;
            PotionEffect effect = e.entityLiving.getActivePotionEffect(AMPotions.clarity);
            e.entityLiving.removePotionEffect(AMPotions.clarity);
            if (effect.getAmplifier() <= 0)
                return;
            e.entityLiving.addPotionEffect(new PotionEffect(effect.getPotion(), effect.getDuration(), effect.getAmplifier() - 1));
        }
    }

    @SubscribeEvent
    public void teleportEvent(EnderTeleportEvent e) {
        ArrayList<Long> keystoneKeys = KeystoneUtilities.instance.GetKeysInInvenory(e.getEntityLiving());
        TileEntityAstralBarrier blockingBarrier = DimensionUtilities.GetBlockingAstralBarrier(e.getEntityLiving().world, new BlockPos(e.getTargetX(), e.getTargetY(), e.getTargetZ()), keystoneKeys);

        if (e.getEntityLiving().isPotionActive(AMPotions.astral_distortion) || blockingBarrier != null) {
            e.setCanceled(true);
            if (blockingBarrier != null) {
                blockingBarrier.onEntityBlocked(e.getEntityLiving());
            }
            return;
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void playerRender(RenderPlayerEvent.Pre e) {
        if (e.getEntityLiving().isPotionActive(AMPotions.true_sight)) {
            GL11.glPushMatrix();
            GL11.glRotated(e.getEntityPlayer().rotationYawHead, 0, -1, 0);
            int[] runes = SelectionUtils.getRuneSet(e.getEntityPlayer());
            int numRunes = runes.length;
            double start = ((double) numRunes - 1) / 8D;
            GL11.glTranslated(-start, 2.2, 0);
            for (int rune : runes) {
                GL11.glPushMatrix();
                GL11.glScaled(0.25, 0.25, 0.25);
                Minecraft.getMinecraft().getItemRenderer().renderItem(e.getEntityPlayer(), new ItemStack(AMItems.rune, 1, rune), TransformType.GUI);
                GL11.glPopMatrix();
                GL11.glTranslated(0.25, 0, 0);
            }
            GL11.glPopMatrix();
        }
    }
}
