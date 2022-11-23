package am2.common.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.DamageSources;
import am2.api.IBoundItem;
import am2.api.SkillPointRegistry;
import am2.api.affinity.Affinity;
import am2.api.event.PlayerMagicLevelChangeEvent;
import am2.api.extensions.IAffinityData;
import am2.api.extensions.IEntityExtension;
import am2.api.extensions.ISpellCaster;
import am2.api.skill.SkillPoint;
import am2.api.spell.SpellData;
import am2.common.armor.ArmorHelper;
import am2.common.armor.infusions.GenericImbuement;
import am2.common.defs.ItemDefs;
import am2.common.defs.PotionEffectsDefs;
import am2.common.entity.EntitySpellProjectile;
import am2.common.extensions.AffinityData;
import am2.common.extensions.EntityExtension;
import am2.common.extensions.RiftStorage;
import am2.common.extensions.SkillData;
import am2.common.items.ItemOre;
import am2.common.items.SpellBase;
import am2.common.lore.ArcaneCompendium;
import am2.common.packet.AMDataWriter;
import am2.common.packet.AMNetHandler;
import am2.common.packet.AMPacketIDs;
import am2.common.spell.ContingencyType;
import am2.common.spell.SpellCastResult;
import am2.common.spell.SpellCaster;
import am2.common.trackers.EntityItemWatcher;
import am2.common.utils.CloakUtils;
import am2.common.utils.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityHandler {
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onMouseEvent(MouseEvent event){
		event.setCanceled(ArsMagica2.proxy.setMouseDWheel(event.getDwheel()));
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
				//compendium.init((EntityPlayer) event.getObject());
				event.addCapability(new ResourceLocation("arsmagica2", "Compendium"), compendium);
				event.addCapability(SkillData.ID, skillData);
				event.addCapability(AffinityData.ID, affData);
				event.addCapability(new ResourceLocation("arsmagica2", "RiftStorage"), storage);
			}
		}
	}
	
	@SubscribeEvent
	public void attachItemStack(AttachCapabilitiesEvent<Item> event) {
		if (event.getObject() == ItemDefs.spell) {
			event.addCapability(SpellCaster.ID, new SpellCaster());
		}
	}
	
	@SubscribeEvent
	public void onEntityConstructed(EntityConstructing event) {
		if (event.getEntity() instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase)event.getEntity();
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
		}
	}
	
	@SubscribeEvent
	public void entityJoinWorld(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayer && !event.getWorld().isRemote) {
			EntityExtension.For((EntityLivingBase) event.getEntity()).forceUpdate();
			AffinityData.For((EntityLivingBase) event.getEntity()).forceUpdate();
		}
	}
	
	@SubscribeEvent
	public void onEntityInteract(EntityInteract event){
		if (event.getTarget() instanceof EntityItemFrame){
			ArsMagica2.proxy.itemFrameWatcher.startWatchingFrame((EntityItemFrame)event.getTarget());
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
	public void entityTick (LivingUpdateEvent event) {
		//Pre Tick, Data Sync
		if (!event.getEntity().world.isRemote) {
			if (EntityExtension.For(event.getEntityLiving()).shouldUpdate())
				sendUpdate(event.getEntityLiving(), AMPacketIDs.SYNC_EXTENDED_PROPS, EntityExtension.For(event.getEntityLiving()).generateUpdatePacket());
			if (event.getEntityLiving() instanceof EntityPlayer) {
				if (AffinityData.For(event.getEntityLiving()).shouldUpdate())
					sendUpdate(event.getEntityLiving(), AMPacketIDs.SYNC_AFFINITY_DATA, AffinityData.For(event.getEntityLiving()).generateUpdatePacket());
				if (SkillData.For(event.getEntityLiving()).shouldUpdate())
					sendUpdate(event.getEntityLiving(), AMPacketIDs.SYNC_SKILL_DATA, SkillData.For(event.getEntityLiving()).generateUpdatePacket());
				if (ArcaneCompendium.For((EntityPlayer) event.getEntityLiving()).shouldUpdate())
					sendUpdate(event.getEntityLiving(), AMPacketIDs.SYNC_COMPENDIUM, ArcaneCompendium.For((EntityPlayer) event.getEntityLiving()).generateUpdatePacket());
			}
		}
		if (event.getEntityLiving() instanceof EntityPlayer) playerTick((EntityPlayer) event.getEntityLiving());
		
		if (event.getEntity().world.isRemote)
			EntityExtension.For(event.getEntityLiving()).spawnManaLinkParticles();
		else
			EntityExtension.For(event.getEntityLiving()).manaBurnoutTick();
		EntityExtension ext = EntityExtension.For(event.getEntityLiving());
		EntityLivingBase entity = event.getEntityLiving();
		if (event.getEntity().ticksExisted % 20 == 0){
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
					InventoryPlayer inv = ((EntityPlayer)entity).inventory;
					for (int i = 0; i < inv.getSizeInventory(); i++) {
						ItemStack is2 = inv.getStackInSlot(i);
						if (is2 != null && is2.getItem() instanceof SpellBase && is2.getTagCompound() != null && is.getSource().getTagCompound().getString("ToggleShapeID").equals(is2.getTagCompound().getString("ToggleShapeID"))) {
							is.getSource().getTagCompound().setBoolean("HasEffect", true);
						}
					}
				}
			}
		}

		// Reflect Spell
		if (event.getEntityLiving().isPotionActive(PotionEffectsDefs.SPELL_REFLECT)){
			int d0 = 3;
			AxisAlignedBB bb = new AxisAlignedBB(event.getEntityLiving().posX - 0.5, event.getEntityLiving().posY - 0.5, event.getEntityLiving().posZ - 0.5, event.getEntityLiving().posX + 0.5, event.getEntityLiving().posY + 0.5, event.getEntityLiving().posZ + 0.5).expand(d0, d0, d0);
			List<Entity> entityList = event.getEntityLiving().getEntityWorld().getEntitiesWithinAABB(Entity.class, bb);

            for (Object thing : entityList){
                if (!(thing instanceof EntitySpellProjectile)) continue;
                EntitySpellProjectile projectile = (EntitySpellProjectile)thing;
                if (projectile.getShooter() == event.getEntityLiving()) continue;
                double rX = projectile.posX - event.getEntityLiving().posX;
                double rY = projectile.posY - event.getEntityLiving().posY;
                double rZ = projectile.posZ - event.getEntityLiving().posZ;
                System.out.println("Reflecting entity");
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

		if (event.getEntityLiving().isPotionActive(PotionEffectsDefs.WATERY_GRAVE) && event.getEntityLiving().isInWater())
		    event.getEntityLiving().addVelocity(0, -0.1, 0);
        else if(event.getEntityLiving().isPotionActive(PotionEffectsDefs.ENTANGLE)){
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
		else if (event.getEntityLiving().getHealth() * 4 < event.getEntityLiving().getMaxHealth() && type == ContingencyType.HEALTH) {
			ext.getContingencyStack().execute(event.getEntityLiving().world, event.getEntityLiving(), null, event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ, null);
			if (ext.getContingencyType() == ContingencyType.HEALTH) {
				ext.setContingency(ContingencyType.NULL, null);
			}
		}
		if (EntityUtils.isSummonExpired(event.getEntityLiving()))
			event.getEntityLiving().setDead();
	}
	
	private void sendUpdate(EntityLivingBase ent, byte id, byte[] data) {
		AMNetHandler.INSTANCE.sendPacketToAllClientsNear(ent.dimension, ent.posX, ent.posY, ent.posZ, 64, id, new AMDataWriter().add(ent.getEntityId()).add(data).generate());
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderTick (RenderGameOverlayEvent event) {
		Minecraft.getMinecraft().profiler.startSection("ArsMagica2-Overlay");
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();
		if (event.getType() == ElementType.CROSSHAIRS)
			ArsMagica2.proxy.renderGameOverlay();
		GL11.glPopMatrix();
		GL11.glPopAttrib();
		Minecraft.getMinecraft().profiler.endSection();
	}
	
	public void playerTick (EntityPlayer player) {
		EntityExtension ext = EntityExtension.For(player);
		IAffinityData affData = player.getCapability(AffinityData.INSTANCE, null);
		ext.flipTick();
		if (!player.world.isRemote) {
			affData.tickDiminishingReturns();
		}
		if (!player.capabilities.isCreativeMode) {
			for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (stack != null && stack.getItem() instanceof IBoundItem) {
					if (ext.hasEnoughMana(((IBoundItem)stack.getItem()).maintainCost(player, stack)))
						ext.deductMana(((IBoundItem)stack.getItem()).maintainCost(player, stack));
					else 
						stack.getItem().onDroppedByPlayer(stack, player);
				}
			}
		}
		if (player.world.isRemote)
			AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.PLAYER_FLIP, new AMDataWriter().add(ext.getIsFlipped()).generate()); // This needs optimizing
		if (ext.getIsFlipped()){
			if ((player).motionY < 2 && !player.capabilities.isFlying)
				(player).motionY += 0.15f;
			double posY = player.posY + player.height;
			World world = player.world;
			RayTraceResult mop = world.rayTraceBlocks(new Vec3d(player.posX, posY, player.posZ), new Vec3d(player.posX, posY + 1, player.posZ), true);
			if (mop != null){
				if (!player.onGround){
					world.getBlockState(mop.getBlockPos()).getBlock().onFallenUpon(world, mop.getBlockPos(), player, Math.abs(player.fallDistance));
					player.fallDistance = 0;
				}
				player.onGround = true;
				player.isAirBorne = false;
			}else{
				//System.out.println(player.motionY);
				if (player.motionY > 0){
					player.fallDistance += player.posY - player.prevPosY;
					player.setJumping(false);
				}
				player.isAirBorne = true;
				player.onGround = false;
			}
		}
		if (ArmorHelper.isInfusionPreset(player.getItemStackFromSlot(EntityEquipmentSlot.LEGS), GenericImbuement.stepAssist)){
			player.stepHeight = 1.0111f;
		}else if (player.stepHeight == 1.0111f){
			player.stepHeight = 0.6f;
		}

		IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
		if (ArmorHelper.isInfusionPreset(player.getItemStackFromSlot(EntityEquipmentSlot.FEET), GenericImbuement.runSpeed)){
			if (attr.getModifier(GenericImbuement.imbuedHasteID) == null){
				attr.applyModifier(GenericImbuement.imbuedHaste);
			}
		}else{
			if (attr.getModifier(GenericImbuement.imbuedHasteID) != null){
				attr.removeModifier(GenericImbuement.imbuedHaste);
			}
		}
		double lifeDepth = affData.getAffinityDepth(Affinity.LIFE);
		ext.lowerHealCooldown((int) (Math.max(1, lifeDepth  * 10F)));
		ext.lowerAffinityHealCooldown((int) (Math.max(1, lifeDepth * 10F)));
	}
	
	@SubscribeEvent
	public void entityDeath(LivingDeathEvent e) {
		IEntityExtension ext = EntityExtension.For(e.getEntityLiving());
		ContingencyType type = ext.getContingencyType();
		EntityLivingBase target = null;
		if (e.getEntityLiving() instanceof EntityPlayer){
			ArsMagica2.proxy.playerTracker.onPlayerDeath((EntityPlayer)e.getEntityLiving());
		}
		
		if (e.getSource() != null && e.getEntityLiving() != null && e.getEntityLiving() instanceof EntityLiving && e.getSource().getTrueSource() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) e.getSource().getTrueSource();
			for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (stack == null || stack.getItem() != ItemDefs.crystalPhylactery) continue;
				if (ItemDefs.crystalPhylactery.getSpawnClass(stack) == null)
					ItemDefs.crystalPhylactery.setSpawnClass(stack, e.getEntityLiving().getClass());
				if (ItemDefs.crystalPhylactery.canStore(stack, (EntityLiving) e.getEntityLiving())) {
					ItemDefs.crystalPhylactery.addFill(stack);
				}
			}
		}
		
		if (e.getSource() != null && e.getSource().getTrueSource() instanceof EntityLivingBase)
			target = e.getSource().getTrueSource() != null ? (EntityLivingBase)e.getSource().getTrueSource() : null;
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
			if (e.getAmount() > 0.0F && stack != null && stack.getItem() == ItemDefs.BoundShield && EntityUtils.canBlockDamageSource(player, e.getSource())) {
				ISpellCaster spell = stack.getCapability(SpellCaster.INSTANCE, null);
				if (EntityExtension.For(player).hasEnoughMana(e.getAmount() * 10)) {
					stack.getItem().onDroppedByPlayer(stack, player);
					EntityExtension.For(player).deductMana(e.getAmount() * 10);
				} else if (EntityExtension.For(player).hasEnoughMana(spell.getManaCost(player.world, player))) {
					EntityLivingBase target = e.getSource().getTrueSource() instanceof EntityLivingBase ? (EntityLivingBase)e.getSource().getTrueSource() : null;
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
	@SideOnly(Side.CLIENT)
	public void onPlayerRender(RenderPlayerEvent.Pre event){
		ItemStack chestPlate = event.getEntityPlayer().inventory.armorInventory.get(2);

		ModelBiped mainModel = event.getRenderer().getMainModel();

		if (!ArsMagica2.proxy.playerTracker.hasCLS(event.getEntityPlayer().getUniqueID().toString())){
			if (chestPlate != null && chestPlate.getItem() == ItemDefs.earthArmor){
				if (mainModel != null){
					mainModel.bipedLeftArm.isHidden = event.getEntityPlayer().getHeldItemOffhand() != null;
					mainModel.bipedRightArm.isHidden = event.getEntityPlayer().getHeldItemMainhand() != null;
				}
			}else{
				if (mainModel != null){
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

		if (EntityExtension.For(event.getEntityPlayer()).getFlipRotation() > 0){
			GL11.glPushMatrix();

			GL11.glTranslated(-transX, -transY, -transZ);
			GL11.glRotatef(EntityExtension.For(event.getEntityPlayer()).getFlipRotation(), 0, 0, 1.0f);
			GL11.glTranslated(transX, transY, transZ);

			float offset = event.getEntityPlayer().height * (EntityExtension.For(event.getEntityPlayer()).getFlipRotation() / 180.0f);
			GL11.glTranslatef(0, -offset, 0);
		}

		float shrink = EntityExtension.For(event.getEntityPlayer()).getShrinkPct();
		if (shrink > 0){
			GL11.glPushMatrix();
			//GL11.glTranslatef(0, 0 - 0.5f * shrink, 0);
			GL11.glScalef(1 - 0.5f * shrink, 1 - 0.5f * shrink, 1 - 0.5f * shrink);
		}
		
		CloakUtils.renderCloakModel(event.getEntityPlayer(), mainModel, event.getPartialRenderTick());
	}
	
	@SubscribeEvent
	public void onLivingDrops(LivingDropsEvent event){
		if (EntityUtils.isSummon(event.getEntityLiving()) && !(event.getEntityLiving() instanceof EntityHorse)){
			event.setCanceled(true);
		}
		if (event.getSource() == DamageSources.darkNexus){
			event.setCanceled(true);
		}
		if (!event.getEntityLiving().world.isRemote && event.getEntityLiving() instanceof EntityPig && event.getEntityLiving().getRNG().nextDouble() < 0.3f){
			EntityItem animalFat = new EntityItem(event.getEntityLiving().world);
			ItemStack stack = new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_ANIMALFAT);
			animalFat.setPosition(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ);
			animalFat.setItem(stack);
			event.getDrops().add(animalFat);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPlayerRender(RenderPlayerEvent.Post event){
		ModelBiped mainModel = event.getRenderer().getMainModel();
		if (mainModel != null){
			mainModel.bipedLeftArm.isHidden = false;
			mainModel.bipedRightArm.isHidden = false;
		}
		
		if (EntityExtension.For(event.getEntityPlayer()).getFlipRotation() > 0){
			GL11.glPopMatrix();
		}
		if (EntityExtension.For(event.getEntityPlayer()).getShrinkPct() > 0){
			GL11.glPopMatrix();
		}

		//CloakUtils.renderCloakModel(event.entityPlayer, mainModel, event.partialRenderTick);
	}
	
	@SubscribeEvent
	public void onItemPickup(ItemPickupEvent event) {
		if (event.player == null)
			return;

		if (!event.player.world.isRemote && EntityExtension.For(event.player).getCurrentLevel() <= 0 && event.pickedUp.getItem().getItem() == ItemDefs.arcaneCompendium){
			event.player.sendStatusMessage(new TextComponentString("You have unlocked the secrets of the arcane!"), false);
			// Not implemented client side
//			AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)event.player, "shapes", true);
//			AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)event.player, "components", true);
//			AMNetHandler.INSTANCE.sendCompendiumUnlockPacket((EntityPlayerMP)event.player, "modifiers", true);
			EntityExtension.For(event.player).setMagicLevelWithMana(1);
			return;
		}
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void playerJumpEvent(LivingJumpEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer){
			ItemStack boots = ((EntityPlayer)event.getEntityLiving()).getItemStackFromSlot(EntityEquipmentSlot.FEET);
			if (boots != null && boots.getItem() == ItemDefs.enderBoots && event.getEntityLiving().isSneaking()){
				EntityExtension.For(event.getEntityLiving()).setInverted(!EntityExtension.For(event.getEntityLiving()).isInverted());
			}

			if (EntityExtension.For(event.getEntityLiving()).getFlipRotation() > 0)
				((EntityPlayer)event.getEntityLiving()).addVelocity(0, -2 * event.getEntityLiving().motionY, 0);
		}
	}
	
	@SubscribeEvent
	public void entityJoinWorldEvent(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityItem) {
			EntityItemWatcher.instance.addWatchedItem((EntityItem) event.getEntity());
		}
	}
}
