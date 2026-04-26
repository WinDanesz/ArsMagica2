package am2.common.handler;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.DamageSources;
import am2.api.SkillPointRegistry;
import am2.api.event.PlayerMagicLevelChangeEvent;
import am2.api.extensions.IAffinityData;
import am2.api.extensions.IEntityExtension;
import am2.api.extensions.ISpellCaster;
import am2.api.items.IBoundItem;
import am2.api.skill.SkillPoint;
import am2.api.spell.SpellData;
import am2.common.advancement.AMAdvancementTriggers;
import am2.common.armor.ArmorHelper;
import am2.common.entity.EntitySpellProjectile;
import am2.common.extensions.AffinityData;
import am2.common.extensions.EntityExtension;
import am2.common.extensions.RiftStorage;
import am2.common.extensions.SkillData;
import am2.common.items.ItemCrystalPhylactery;
import am2.common.items.ItemSpellBase;
import am2.common.lore.ArcaneCompendium;
import am2.common.packet.AMNetHandler;
import am2.common.registry.*;
import am2.common.spell.ContingencyType;
import am2.common.spell.SpellCastResult;
import am2.common.spell.SpellCaster;
import am2.common.trackers.EntityItemWatcher;
import am2.common.utils.CloakUtils;
import am2.common.utils.EntityUtils;
import am2.common.utils.InventoryUtilities;
import am2.common.utils.MathUtilities;
import am2.common.world.BiomeWitchwoodForest;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import am2.network.AMNetworkHandler;
import am2.network.packets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class EntityHandler {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onMouseEvent(MouseEvent event) {
        event.setCanceled(ArsMagica.proxy.setMouseDWheel(event.getDwheel()));
    }

    @SubscribeEvent
    public void attachEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityLivingBase) {
            EntityExtension ext = new EntityExtension();
            ext.init((EntityLivingBase) event.getObject());
            event.addCapability(EntityExtension.ID, ext);
            if (event.getObject() instanceof EntityPlayer) {
                ArcaneCompendium compendium = new ArcaneCompendium();
                AffinityData affData = new AffinityData();
                SkillData skillData = new SkillData();
                RiftStorage storage = new RiftStorage();
                affData.init((EntityPlayer) event.getObject());
                skillData.init((EntityPlayer) event.getObject());
                compendium.init((EntityPlayer) event.getObject());
                event.addCapability(new ResourceLocation("arsmagica2", "Compendium"), compendium);
                event.addCapability(SkillData.ID, skillData);
                event.addCapability(AffinityData.ID, affData);
                event.addCapability(new ResourceLocation("arsmagica2", "RiftStorage"), storage);
            }
        }
    }

    // Note: ItemSpellBase now handles its own capability via initCapabilities() override
    // This event handler is kept as a fallback but shouldn't be needed
    @SubscribeEvent
    public void attachItemStack(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() == AMItems.spell) {
            // Only attach if not already present (shouldn't happen with initCapabilities)
            if (!event.getCapabilities().containsKey(SpellCaster.ID)) {
                event.addCapability(SpellCaster.ID, new SpellCaster());
            }
        }
    }

    @SubscribeEvent
    public void onEntityConstructed(EntityConstructing event) {
        if (event.getEntity() instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) event.getEntity();
            living.getAttributeMap().registerAttribute(ArsMagicaAPI.burnoutReductionRate);
            living.getAttributeMap().registerAttribute(ArsMagicaAPI.manaRegenTimeModifier);
            living.getAttributeMap().registerAttribute(ArsMagicaAPI.maxBurnoutBonus);
            living.getAttributeMap().registerAttribute(ArsMagicaAPI.maxManaBonus);
            living.getAttributeMap().registerAttribute(ArsMagicaAPI.xpGainModifier);
        }
    }

    @SubscribeEvent
    public void onPlayerMagicLevelChange(PlayerMagicLevelChangeEvent event) {
        if (event.getEntityPlayer() != null) {
            for (SkillPoint point : SkillPointRegistry.getSkillPointMap().values()) {
                if (point.getMinEarnLevel() > event.getLevel()) continue;
                if ((event.getLevel() - point.getMinEarnLevel()) % point.getLevelsForPoint() == 0)
                    SkillData.For(event.getEntityPlayer()).setSkillPoint(point, SkillData.For(event.getEntityPlayer()).getSkillPoint(point) + 1);
            }
            AMAdvancementTriggers.MAGIC_LEVEL_REACHED.triggerFor(event.getEntityPlayer(), event.getLevel());
        }
    }

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayer && !event.getWorld().isRemote) {
            EntityExtension.For((EntityLivingBase) event.getEntity()).forceUpdate();
            AffinityData.For((EntityLivingBase) event.getEntity()).forceUpdate();
            SkillData.For((EntityLivingBase) event.getEntity()).forceUpdate();
            ArcaneCompendium.For((EntityPlayer) event.getEntity()).forceUpdate();
        }
    }
    

    @SubscribeEvent
    public void onClonePlayer(PlayerEvent.Clone event) {
        transferCapability(AffinityData.INSTANCE, AffinityData.For(event.getOriginal()), AffinityData.For(event.getEntityPlayer()));
        transferCapability(EntityExtension.INSTANCE, EntityExtension.For(event.getOriginal()), EntityExtension.For(event.getEntityPlayer()));
        transferCapability(SkillData.INSTANCE, SkillData.For(event.getOriginal()), SkillData.For(event.getEntityPlayer()));
        transferCapability(RiftStorage.INSTANCE, RiftStorage.For(event.getOriginal()), RiftStorage.For(event.getEntityPlayer()));
        transferCapability(ArcaneCompendium.INSTANCE, ArcaneCompendium.For(event.getOriginal()), ArcaneCompendium.For(event.getEntityPlayer()));
    }

    private <T> void transferCapability(Capability<T> capability, T original, T target) {
        capability.getStorage().readNBT(capability, target, null, capability.getStorage().writeNBT(capability, original, null));
    }

    @SubscribeEvent
    public void entityTick(LivingUpdateEvent event) {

        // Run mana regen/burnout BEFORE the sync check so changes coalesce in the same tick
        if (!event.getEntity().world.isRemote)
            EntityExtension.For(event.getEntityLiving()).manaBurnoutTick();

        //Pre Tick, Data Sync
        if (!event.getEntity().world.isRemote) {
            EntityLivingBase ent = event.getEntityLiving();
            NetworkRegistry.TargetPoint tp = new NetworkRegistry.TargetPoint(ent.dimension, ent.posX, ent.posY, ent.posZ, 64);
            if (EntityExtension.For(ent).shouldUpdate())
                AMNetworkHandler.getNetwork().sendToAllAround(new PacketSyncExtendedProps(ent.getEntityId(), EntityExtension.For(ent).generateUpdatePacket()), tp);
            if (ent instanceof EntityPlayer) {
                if (AffinityData.For(ent).shouldUpdate())
                    AMNetworkHandler.getNetwork().sendToAllAround(new PacketSyncAffinityData(ent.getEntityId(), AffinityData.For(ent).generateUpdatePacket()), tp);
                if (SkillData.For(ent).shouldUpdate())
                    AMNetworkHandler.getNetwork().sendToAllAround(new PacketSyncSkillData(ent.getEntityId(), SkillData.For(ent).generateUpdatePacket()), tp);
                if (ArcaneCompendium.For((EntityPlayer) ent).shouldUpdate())
                    AMNetworkHandler.getNetwork().sendToAllAround(new PacketSyncCompendium(ent.getEntityId(), ArcaneCompendium.For((EntityPlayer) ent).generateUpdatePacket()), tp);
            }
        }

        if (event.getEntityLiving() instanceof EntityPlayer) playerTick((EntityPlayer) event.getEntityLiving());

        if (event.getEntity().world.isRemote)
            EntityExtension.For(event.getEntityLiving()).spawnManaLinkParticles();
        EntityExtension ext = EntityExtension.For(event.getEntityLiving());
        EntityLivingBase entity = event.getEntityLiving();
        if (event.getEntity().ticksExisted % 20 == 0) {
            ArrayList<SpellData> rs = ext.runningStacks;
            int foundID = -1;
            for (int i = 0; i < rs.size(); i++) {
                SpellData is = rs.get(i);
                if (is != null) {
                    SpellCastResult result = is.execute(entity.world, entity, entity, entity.posX, entity.posY, entity.posZ, null);
                    if (result != SpellCastResult.SUCCESS && result != SpellCastResult.SUCCESS_REDUCE_MANA) {
                        foundID = i;
                        break;
                    }
                }
            }
            if (foundID != -1) {
                SpellData is = ext.runningStacks.get(foundID);
                ext.runningStacks.remove(foundID);
                if (entity instanceof EntityPlayer) {
                    InventoryPlayer inv = ((EntityPlayer) entity).inventory;
                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        ItemStack is2 = inv.getStackInSlot(i);
                        if (!is2.isEmpty() && is2.getItem() instanceof ItemSpellBase && is2.getTagCompound() != null && is.getSource().getTagCompound().getString("ToggleShapeID").equals(is2.getTagCompound().getString("ToggleShapeID"))) {
                            is.getSource().getTagCompound().setBoolean("HasEffect", true);
                        }
                    }
                }
            }
        }

        // Reflect Spell
        if (event.getEntityLiving().isPotionActive(AMPotions.spell_reflect)) {
            int d0 = 3;
            AxisAlignedBB bb = new AxisAlignedBB(event.getEntityLiving().posX - 0.5, event.getEntityLiving().posY - 0.5, event.getEntityLiving().posZ - 0.5, event.getEntityLiving().posX + 0.5, event.getEntityLiving().posY + 0.5, event.getEntityLiving().posZ + 0.5).expand(d0, d0, d0);
            List<Entity> entityList = event.getEntityLiving().getEntityWorld().getEntitiesWithinAABB(Entity.class, bb);

            for (Object thing : entityList) {
                if (!(thing instanceof EntitySpellProjectile)) continue;
                EntitySpellProjectile projectile = (EntitySpellProjectile) thing;
                if (projectile.getShooter() == event.getEntityLiving()) continue;
                double rX = projectile.posX - event.getEntityLiving().posX;
                double rY = projectile.posY - event.getEntityLiving().posY;
                double rZ = projectile.posZ - event.getEntityLiving().posZ;
                double angle = (rX * projectile.motionX + rZ * projectile.motionZ) / (Math.sqrt(rX * rX + rZ * rZ) * Math.sqrt(projectile.motionX * projectile.motionX + projectile.motionZ * projectile.motionZ));
                angle = Math.acos(angle);

                if (angle < 3 * (Math.PI / 4)) continue;
                double curvVel = Math.sqrt(rX * rX + rY * rY + rZ * rZ);

                rX /= curvVel;
                rY /= curvVel;
                rZ /= curvVel;

                double newVel = Math.sqrt(projectile.motionX * projectile.motionX + projectile.motionY * projectile.motionY + projectile.motionZ * projectile.motionZ);

                projectile.motionX = newVel * rX;
                //projectile.motionY = newVel * rY;
                projectile.motionZ = newVel * rZ;
            }
        }

        if (event.getEntityLiving().isPotionActive(AMPotions.watery_grave) && event.getEntityLiving().isInWater()) {
            // Directly set downward motion to pull entity down in water
            event.getEntityLiving().motionY = Math.min(event.getEntityLiving().motionY - 0.1, -0.3);
        } else if (event.getEntityLiving().isPotionActive(AMPotions.entangle)) {
            event.getEntityLiving().motionX = 0;
            event.getEntityLiving().motionY = 0;
            event.getEntityLiving().motionZ = 0;
        }
        //Contingency
        ContingencyType type = ext.getContingencyType();
        if (event.getEntityLiving().isBurning() && type == ContingencyType.FIRE) {
            ext.getContingencyStack().execute(event.getEntityLiving().world, event.getEntityLiving(), null, event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ, null);
            if (ext.getContingencyType() == ContingencyType.FIRE)
                ext.setContingency(ContingencyType.NULL, null);
        }
        if (!entity.onGround && entity.fallDistance >= 4f && type == ContingencyType.FALL && ext.getContingencyStack() != null) {
            int distanceToGround = MathUtilities.getDistanceToGround(entity, entity.world);
            if (distanceToGround < -8 * entity.motionY) {
                ext.getContingencyStack().execute(entity.world, entity, null, entity.posX, entity.posY, entity.posZ, null);
                if (ext.getContingencyType() == ContingencyType.FALL)
                    ext.setContingency(ContingencyType.NULL, null);
            }
        }
        if (EntityUtils.isSummonExpired(event.getEntityLiving()))
            event.getEntityLiving().setDead();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void renderTick(RenderGameOverlayEvent event) {
        Minecraft.getMinecraft().profiler.startSection("ArsMagica2-Overlay");
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        if (event.getType() == ElementType.CROSSHAIRS)
            ArsMagica.proxy.renderGameOverlay();
        GL11.glPopMatrix();
        GL11.glPopAttrib();
        Minecraft.getMinecraft().profiler.endSection();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return;
        EntityExtension ext = EntityExtension.For(player);
        float flip = ext.getFlipRotation();
        float lastFlip = ext.getPrevFlipRotation();
        if (flip > 0 || lastFlip > 0) {
            float smoothedFlip = lastFlip + (flip - lastFlip) * (float) event.getRenderPartialTicks();
            float flipProgress = smoothedFlip / 180f;
            // The eye height offset needs to shift the camera from eye position to the
            // inverse eye position (height - eyeHeight). We compute the needed shift and
            // convert to the coordinate space at this pipeline stage.
            // At this point in orientCamera, the entity position translate and yaw/pitch
            // rotations happen AFTER our GL calls (in GL call order), so our translate
            // operates in post-rotation screen space. After a Z roll, Y is progressively
            // inverted, so we need the opposite sign compared to the original view-space shift.
            float eyeShift = player.getEyeHeight() * 2f - player.height;
            GlStateManager.translate(0, eyeShift * flipProgress, 0);
            event.setRoll(event.getRoll() + smoothedFlip);
        }
    }

    private float getWitchwoodGroundFogFactor(Entity entity) {
        if (!isInWitchwoodBiome(entity) || entity.isInWater()) return 0f;

        // world.getHeight() returns the top of the tree canopy in forests, which breaks the
        // height band check. Use the entity's own Y position as the ground reference instead –
        // the player's feet are always at (or very close to) the terrain surface.
        double groundY = entity.posY;
        double cameraY = entity.posY + entity.getEyeHeight();
        double fogLayerHeight = 7.0;
        if (cameraY > groundY + fogLayerHeight) return 0f;

        double normalizedHeight = (cameraY - groundY) / fogLayerHeight;
        return (float) net.minecraft.util.math.MathHelper.clamp(1.0 - normalizedHeight, 0.0, 1.0);
    }

    private boolean isInWitchwoodBiome(Entity entity) {
        if (entity == null || entity.world == null) return false;
        return entity.world.getBiome(new BlockPos(entity.posX, entity.posY, entity.posZ)) == BiomeWitchwoodForest.instance;
    }

    private boolean isUnderground(Entity entity) {
        BlockPos eyePos = new BlockPos(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        // Scan upward for an opaque solid block. Leaves and glass are NOT opaque cubes,
        // so a tree canopy overhead will not trigger this; a cave ceiling will.
        for (int dy = 1; dy <= 64; dy++) {
            if (entity.world.getBlockState(eyePos.up(dy)).isOpaqueCube()) {
                return true;
            }
        }
        return false;
    }

    public void playerTick(EntityPlayer player) {
        EntityExtension ext = EntityExtension.For(player);
        IAffinityData affData = player.getCapability(AffinityData.INSTANCE, null);
        ext.flipTick();

        if (!player.world.isRemote) {
            affData.tickDiminishingReturns();
        }
        if (!player.capabilities.isCreativeMode) {
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof IBoundItem) {
                    if (ext.hasEnoughMana(((IBoundItem) stack.getItem()).maintainCost(player, stack)))
                        ext.deductMana(((IBoundItem) stack.getItem()).maintainCost(player, stack));
                    else
                        player.inventory.setInventorySlotContents(i, InventoryUtilities.restoreSpellFromBoundItem(stack));
                }
            }
        }
        if (player.world.isRemote)
            AMNetworkHandler.getNetwork().sendToServer(new PacketPlayerFlip(ext.getIsFlipped())); // This needs optimizing
        if (ext.getIsFlipped()) {
            if ((player).motionY < 2 && !player.capabilities.isFlying)
                (player).motionY += 0.15f;
            double posY = player.posY + player.height;
            World world = player.world;
            RayTraceResult mop = world.rayTraceBlocks(new Vec3d(player.posX, posY, player.posZ), new Vec3d(player.posX, posY + 1, player.posZ), true);
            if (mop != null) {
                if (!player.onGround) {
                    world.getBlockState(mop.getBlockPos()).getBlock().onFallenUpon(world, mop.getBlockPos(), player, Math.abs(player.fallDistance));
                    player.fallDistance = 0;
                }
                player.onGround = true;
                player.isAirBorne = false;
            } else {
                if (player.motionY > 0) {
                    player.fallDistance += player.posY - player.prevPosY;
                    player.setJumping(false);
                }
                player.isAirBorne = true;
                player.onGround = false;
            }
        }
        if (ArmorHelper.isInfusionPreset(player.getItemStackFromSlot(EntityEquipmentSlot.LEGS), ImbuementRegistry.STEP_ASSIST)) {
            player.stepHeight = 1.0111f;
        } else if (player.stepHeight == 1.0111f) {
            player.stepHeight = 0.6f;
        }

        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (ArmorHelper.isInfusionPreset(player.getItemStackFromSlot(EntityEquipmentSlot.FEET), ImbuementRegistry.RUN_SPEED)) {
            if (attr.getModifier(ImbuementRegistry.IMBUED_HASTE_ID) == null) {
                attr.applyModifier(ImbuementRegistry.IMBUED_HASTE);
            } 
        } else {
            if (attr.getModifier(ImbuementRegistry.IMBUED_HASTE_ID) != null) {
                attr.removeModifier(ImbuementRegistry.IMBUED_HASTE);
            }
        }
        double lifeDepth = affData.getAffinityDepth(Affinities.life);
        ext.lowerHealCooldown((int) (Math.max(1, lifeDepth * 10F)));
        ext.lowerAffinityHealCooldown((int) (Math.max(1, lifeDepth * 10F)));
    }

    /** Tints the fog colour toward a purple-grey mist when inside the Witchwood biome.
     *  Ground band gets the full purple; higher up gets a lighter overcast tint. */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onFogColors(EntityViewRenderEvent.FogColors event) {
        if (!isInWitchwoodBiome(event.getEntity())) return;
        if (isUnderground(event.getEntity())) return;

        float groundFactor = getWitchwoodGroundFogFactor(event.getEntity());
        // Above the ground band, keep a fixed 30 % overcast tint to desaturate the sun.
        float blend = Math.max(groundFactor * 0.80f, 0.30f);

        float mistR = 0.18f, mistG = 0.11f, mistB = 0.10f;
        event.setRed(event.getRed()     * (1f - blend) + mistR * blend);
        event.setGreen(event.getGreen() * (1f - blend) + mistG * blend);
        event.setBlue(event.getBlue()   * (1f - blend) + mistB * blend);
    }

    /**
     * Two-layer fog for the Witchwood biome:
     *  - Ground band (0–7 blocks above feet): very dense, end = 8 % of render dist.
     *  - Full biome sky haze: moderate persistent fog, end = 60 % of render dist,
     *    which washes out the sun disc even at altitude.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderFog(EntityViewRenderEvent.RenderFogEvent event) {
        if (!isInWitchwoodBiome(event.getEntity())) return;
        if (isUnderground(event.getEntity())) return;

        float far = event.getFarPlaneDistance();
        float groundFactor = getWitchwoodGroundFogFactor(event.getEntity());

        // Mimic Blindness: use absolute block distances so the effect is the same at any
        // render distance setting. Blindness itself uses 5.0f blocks; we use 16.0f for a
        // slightly less claustrophobic biome-wide haze.
        float fogEnd = 20.0f;
        float fogStart = 0.0f;

        // Near the ground the dense mist can be even shorter; take whichever is tighter.
        if (groundFactor > 0f) {
            float groundEnd = far * net.minecraft.util.math.MathHelper.clamp(1.0f - groundFactor * 0.973f, 0.0f, 1.0f);
            fogEnd = Math.min(fogEnd, groundEnd);
        }

        GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
        GlStateManager.setFogStart(fogStart);
        GlStateManager.setFogEnd(fogEnd);
        GlStateManager.enableFog();
    }

    /** Draws a dark translucent overlay to simulate reduced ambient light in the Witchwood biome. */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != ElementType.ALL) return;
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null || !isInWitchwoodBiome(player)) return;

        ScaledResolution res = event.getResolution();
        // 40 % opacity black overlay — simulates a dimmer ambient light level.
        Gui.drawRect(0, 0, res.getScaledWidth(), res.getScaledHeight(), 0x66000000);
    }

    @SubscribeEvent
    public void entityDeath(LivingDeathEvent e) {
        IEntityExtension ext = EntityExtension.For(e.getEntityLiving());
        ContingencyType type = ext.getContingencyType();
        EntityLivingBase target = null;
        if (e.getEntityLiving() instanceof EntityPlayer) {
            ArsMagica.proxy.playerTracker.onPlayerDeath((EntityPlayer) e.getEntityLiving());
        }

        if (e.getSource() != null && e.getEntityLiving() != null && e.getEntityLiving() instanceof EntityLiving && e.getSource().getTrueSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) e.getSource().getTrueSource();
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() != AMItems.crystal_phylactery) continue; //
                if (((ItemCrystalPhylactery) AMItems.crystal_phylactery).getSpawnClass(stack) == null)
                    ((ItemCrystalPhylactery) AMItems.crystal_phylactery).setSpawnClass(stack, e.getEntityLiving().getClass());
                if (((ItemCrystalPhylactery) AMItems.crystal_phylactery).canStore(stack, (EntityLiving) e.getEntityLiving())) {
                    ((ItemCrystalPhylactery) AMItems.crystal_phylactery).addFill(stack);
                }
            }
        }

        if (e.getSource() != null && e.getSource().getTrueSource() instanceof EntityLivingBase)
            target = e.getSource().getTrueSource() != null ? (EntityLivingBase) e.getSource().getTrueSource() : null;
        if (type == ContingencyType.DEATH) {
            ext.getContingencyStack().execute(e.getEntityLiving().world, e.getEntityLiving(), (target != null ? target : (EntityLivingBase) e.getEntity()), (target != null ? target : e.getEntity()).posX, (target != null ? target : e.getEntity()).posY, (target != null ? target : e.getEntity()).posZ, null);
            if (ext.getContingencyType() == ContingencyType.DEATH)
                ext.setContingency(ContingencyType.NULL, null);
        }
    }

    @SubscribeEvent
    public void attackEntity(LivingAttackEvent e) {
        if (e.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) e.getEntityLiving();
            ItemStack stack = player.getActiveItemStack();
            if (e.getAmount() > 0.0F && !stack.isEmpty() && stack.getItem() == AMItems.bound_shield && EntityUtils.canBlockDamageSource(player, e.getSource())) {
                ISpellCaster spell = stack.getCapability(SpellCaster.INSTANCE, null);
                if (EntityExtension.For(player).hasEnoughMana(e.getAmount() * 10)) {
                    stack.getItem().onDroppedByPlayer(stack, player);
                    EntityExtension.For(player).deductMana(e.getAmount() * 10);
                } else if (spell != null && EntityExtension.For(player).hasEnoughMana(spell.getManaCost(player.world, player))) {
                    EntityLivingBase target = e.getSource().getTrueSource() instanceof EntityLivingBase ? (EntityLivingBase) e.getSource().getTrueSource() : null;
                    double posX = target != null ? target.posX : player.posX;
                    double posY = target != null ? target.posY : player.posY;
                    double posZ = target != null ? target.posZ : player.posZ;
                    ItemStack copiedStack = stack.copy();
                    spell.createSpellData(copiedStack).execute(player.world, player, target, posX, posY, posZ, null);
                } else {
                    stack.getItem().onDroppedByPlayer(stack, player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        // Earth Armor melee damage bonus and knockback
        Entity entitySource = event.getSource().getTrueSource();
        if (entitySource instanceof EntityPlayer) {
            EntityPlayer attacker = (EntityPlayer) entitySource;
            ItemStack chestPlate = attacker.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (!chestPlate.isEmpty() && chestPlate.getItem() == AMItems.earth_armor
                    && attacker.getHeldItemMainhand().isEmpty()) {
                event.setAmount(event.getAmount() + 4);

                EntityLivingBase target = event.getEntityLiving();
                double deltaZ = target.posZ - attacker.posZ;
                double deltaX = target.posX - attacker.posX;
                double angle = Math.atan2(deltaZ, deltaX);
                double speed = attacker.isSprinting() ? 3 : 2;
                double vertSpeed = attacker.isSprinting() ? 0.5 : 0.325;

                if (target instanceof EntityPlayer) {
                    AMNetHandler.INSTANCE.sendVelocityAddPacket(target.world, target, speed * Math.cos(angle), vertSpeed, speed * Math.sin(angle));
                } else {
                    target.motionX += speed * Math.cos(angle);
                    target.motionZ += speed * Math.sin(angle);
                    target.motionY += vertSpeed;
                    target.velocityChanged = true;
                }
                target.world.playSound(null, target.posX, target.posY, target.posZ, AMSounds.CAST_EARTH, SoundCategory.PLAYERS, 0.4f, target.world.rand.nextFloat() * 0.1F + 0.9F);
            }
        }

        IEntityExtension ext = EntityExtension.For(event.getEntityLiving());
        ContingencyType type = ext.getContingencyType();

        // Trigger DAMAGE contingency whenever entity takes damage
        if (type == ContingencyType.DAMAGE) {
            ext.getContingencyStack().execute(event.getEntityLiving().world, event.getEntityLiving(), null, event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ, null);
            ext.setContingency(ContingencyType.NULL, null);
        }

        // Trigger HEALTH contingency if health drops below 1/3 of max
        if (type == ContingencyType.HEALTH && event.getEntityLiving().getHealth() <= event.getEntityLiving().getMaxHealth() / 3) {
            ext.getContingencyStack().execute(event.getEntityLiving().world, event.getEntityLiving(), null, event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ, null);
            ext.setContingency(ContingencyType.NULL, null);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerRender(RenderPlayerEvent.Pre event) {
        ItemStack chestPlate = event.getEntityPlayer().inventory.armorInventory.get(2);

        ModelBiped mainModel = event.getRenderer().getMainModel();

        if (!ArsMagica.proxy.playerTracker.hasCLS(event.getEntityPlayer().getUniqueID().toString())) {
            if (chestPlate != null && chestPlate.getItem() == AMItems.earth_armor) {
                if (mainModel != null) {
                    mainModel.bipedLeftArm.isHidden = event.getEntityPlayer().getHeldItemOffhand() != null;
                    mainModel.bipedRightArm.isHidden = event.getEntityPlayer().getHeldItemMainhand() != null;
                }
            } else {
                if (mainModel != null) {
                    mainModel.bipedLeftArm.isHidden = false;
                    mainModel.bipedRightArm.isHidden = false;
                }
            }
        }

        double dX = Minecraft.getMinecraft().player.posX - event.getEntityPlayer().posX;
        double dY = Minecraft.getMinecraft().player.posY - event.getEntityPlayer().posY;
        double dZ = Minecraft.getMinecraft().player.posZ - event.getEntityPlayer().posZ;

        double dpX = Minecraft.getMinecraft().player.prevPosX - event.getEntityPlayer().prevPosX;
        double dpY = Minecraft.getMinecraft().player.prevPosY - event.getEntityPlayer().prevPosY;
        double dpZ = Minecraft.getMinecraft().player.prevPosZ - event.getEntityPlayer().prevPosZ;

        double transX = dpX + (dX - dpX) * event.getPartialRenderTick();
        double transY = dpY + (dY - dpY) * event.getPartialRenderTick();
        double transZ = dpZ + (dZ - dpZ) * event.getPartialRenderTick();

        if (EntityExtension.For(event.getEntityPlayer()).getFlipRotation() > 0) {
            GL11.glPushMatrix();

            GL11.glTranslated(-transX, -transY, -transZ);
            GL11.glRotatef(EntityExtension.For(event.getEntityPlayer()).getFlipRotation(), 0, 0, 1.0f);
            GL11.glTranslated(transX, transY, transZ);

            float offset = event.getEntityPlayer().height * (EntityExtension.For(event.getEntityPlayer()).getFlipRotation() / 180.0f);
            GL11.glTranslatef(0, -offset, 0);
        }

        float shrink = EntityExtension.For(event.getEntityPlayer()).getShrinkPct();
        if (shrink > 0) {
            GL11.glPushMatrix();
            //GL11.glTranslatef(0, 0 - 0.5f * shrink, 0);
            GL11.glScalef(1 - 0.5f * shrink, 1 - 0.5f * shrink, 1 - 0.5f * shrink);
        }

        CloakUtils.renderCloakModel(event.getEntityPlayer(), mainModel, event.getPartialRenderTick());
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (EntityUtils.isSummon(event.getEntityLiving()) && !(event.getEntityLiving() instanceof EntityHorse)) {
            event.setCanceled(true);
        }
        if (event.getSource() == DamageSources.darkNexus) {
            event.setCanceled(true);
        }
        if (!event.getEntityLiving().world.isRemote && event.getEntityLiving() instanceof EntityPig && event.getEntityLiving().getRNG().nextDouble() < 0.3f) {
            EntityItem animalFat = new EntityItem(event.getEntityLiving().world);
            ItemStack stack = new ItemStack(AMItems.animal_fat);
            animalFat.setPosition(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ);
            animalFat.setItem(stack);
            event.getDrops().add(animalFat);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerRender(RenderPlayerEvent.Post event) {
        ModelBiped mainModel = event.getRenderer().getMainModel();
        if (mainModel != null) {
            mainModel.bipedLeftArm.isHidden = false;
            mainModel.bipedRightArm.isHidden = false;
        }

        if (EntityExtension.For(event.getEntityPlayer()).getFlipRotation() > 0) {
            GL11.glPopMatrix();
        }
        if (EntityExtension.For(event.getEntityPlayer()).getShrinkPct() > 0) {
            GL11.glPopMatrix();
        }

        //CloakUtils.renderCloakModel(event.entityPlayer, mainModel, event.partialRenderTick);
    }

    @SubscribeEvent
    public void onItemPickup(ItemPickupEvent event) {
        if (event.player == null)
            return;

        if (!event.player.world.isRemote && EntityExtension.For(event.player).getCurrentLevel() <= 0 && event.getStack().getItem() == AMItems.arcane_compendium) {
            event.player.sendStatusMessage(new TextComponentString("You have unlocked the secrets of the arcane!"), false);
            // Not implemented client side
			//AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)event.player, "shapes", true);
			//AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)event.player, "components", true);
			//AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)event.player, "modifiers", true);
            EntityExtension.For(event.player).setMagicLevelWithMana(1);
            return;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void playerJumpEvent(LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            ItemStack boots = ((EntityPlayer) event.getEntityLiving()).getItemStackFromSlot(EntityEquipmentSlot.FEET);
            if (!boots.isEmpty() && boots.getItem() == AMItems.ender_boots && event.getEntityLiving().isSneaking()) {
                EntityExtension.For(event.getEntityLiving()).setInverted(!EntityExtension.For(event.getEntityLiving()).isInverted());
            }

            if (EntityExtension.For(event.getEntityLiving()).getFlipRotation() > 0)
                ((EntityPlayer) event.getEntityLiving()).addVelocity(0, -2 * event.getEntityLiving().motionY, 0);
        }
    }

    @SubscribeEvent
    public void entityJoinWorldEvent(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityItem) {
            EntityItemWatcher.instance.addWatchedItem((EntityItem) event.getEntity());
        }
    }
}
