package am2.common.affinity;

import java.util.Map.Entry;

import am2.ArsMagica2;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.api.event.SpellCastEvent;
import am2.common.affinity.abilities.AbilityAgile;
import am2.common.affinity.abilities.AbilityAntiEndermen;
import am2.common.affinity.abilities.AbilityClearCaster;
import am2.common.affinity.abilities.AbilityColdBlooded;
import am2.common.affinity.abilities.AbilityExpandedLungs;
import am2.common.affinity.abilities.AbilityFastHealing;
import am2.common.affinity.abilities.AbilityFirePunch;
import am2.common.affinity.abilities.AbilityFireResistance;
import am2.common.affinity.abilities.AbilityFireWeakness;
import am2.common.affinity.abilities.AbilityFluidity;
import am2.common.affinity.abilities.AbilityFulmination;
import am2.common.affinity.abilities.AbilityLavaFreeze;
import am2.common.affinity.abilities.AbilityLeafLike;
import am2.common.affinity.abilities.AbilityLightAsAFeather;
import am2.common.affinity.abilities.AbilityLightningStep;
import am2.common.affinity.abilities.AbilityMagicWeakness;
import am2.common.affinity.abilities.AbilityNightVision;
import am2.common.affinity.abilities.AbilityOneWithMagic;
import am2.common.affinity.abilities.AbilityPacifist;
import am2.common.affinity.abilities.AbilityPhotosynthesis;
import am2.common.affinity.abilities.AbilityPoisonResistance;
import am2.common.affinity.abilities.AbilityReflexes;
import am2.common.affinity.abilities.AbilityRelocation;
import am2.common.affinity.abilities.AbilityRooted;
import am2.common.affinity.abilities.AbilityShortCircuit;
import am2.common.affinity.abilities.AbilitySolidBones;
import am2.common.affinity.abilities.AbilitySunlightWeakness;
import am2.common.affinity.abilities.AbilitySwiftSwim;
import am2.common.affinity.abilities.AbilityThorns;
import am2.common.affinity.abilities.AbilityThunderPunch;
import am2.common.affinity.abilities.AbilityWaterFreeze;
import am2.common.affinity.abilities.AbilityWaterWeakness;
import am2.common.extensions.AffinityData;
import am2.common.packet.AMDataWriter;
import am2.common.packet.AMNetHandler;
import am2.common.packet.AMPacketIDs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AffinityAbilityHelper {
	
	static {
		//AIR
		// TODO: registry GameRegistry.register(new AbilityLightAsAFeather());
		// TODO: registry GameRegistry.register(new AbilityAgile());
		
		//ARCANE
		// TODO: registry GameRegistry.register(new AbilityClearCaster());
		// TODO: registry GameRegistry.register(new AbilityMagicWeakness());
		// TODO: registry GameRegistry.register(new AbilityOneWithMagic());
		
		//EARTH
		// TODO: registry GameRegistry.register(new AbilitySolidBones());
		
		//ENDER
		// TODO: registry GameRegistry.register(new AbilityRelocation());
		// TODO: registry GameRegistry.register(new AbilityNightVision());
		// TODO: registry GameRegistry.register(new AbilityWaterWeakness(Affinity.ENDER));
		// TODO: registry GameRegistry.register(new AbilityPoisonResistance());
		// TODO: registry GameRegistry.register(new AbilitySunlightWeakness());
		
		//FIRE
		// TODO: registry GameRegistry.register(new AbilityFireResistance());
		// TODO: registry GameRegistry.register(new AbilityFirePunch());
		// TODO: registry GameRegistry.register(new AbilityWaterWeakness(Affinity.FIRE));
		
		//ICE
		// TODO: registry GameRegistry.register(new AbilityLavaFreeze());
		// TODO: registry GameRegistry.register(new AbilityWaterFreeze());
		// TODO: registry GameRegistry.register(new AbilityColdBlooded());
		
		//LIFE
		// TODO: registry GameRegistry.register(new AbilityFastHealing());
		// TODO: registry GameRegistry.register(new AbilityPacifist());
		
		//WATER
		// TODO: registry GameRegistry.register(new AbilityExpandedLungs());
		// TODO: registry GameRegistry.register(new AbilityFluidity());
		// TODO: registry GameRegistry.register(new AbilitySwiftSwim());
		// TODO: registry GameRegistry.register(new AbilityFireWeakness());
		// TODO: registry GameRegistry.register(new AbilityAntiEndermen());
		
		//NATURE
		// TODO: registry GameRegistry.register(new AbilityRooted());
		// TODO: registry GameRegistry.register(new AbilityThorns());
		// TODO: registry GameRegistry.register(new AbilityLeafLike());
		// TODO: registry GameRegistry.register(new AbilityPhotosynthesis());
		
		//LIGHTNING
		// TODO: registry GameRegistry.register(new AbilityLightningStep());
		// TODO: registry GameRegistry.register(new AbilityReflexes());
		// TODO: registry GameRegistry.register(new AbilityFulmination());
		// TODO: registry GameRegistry.register(new AbilityShortCircuit());
		// TODO: registry GameRegistry.register(new AbilityThunderPunch());
		// TODO: registry GameRegistry.register(new AbilityWaterWeakness(Affinity.LIGHTNING));
	}
	
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
			if (ability.getKey() != null && ability.getKey().isPressed()) {
				EntityPlayer player = ArsMagica2.proxy.getLocalPlayer();
				if (ability.canApply(player)) {
					AMDataWriter syncPacket = new AMDataWriter();
					syncPacket.add(player.getEntityId());
					syncPacket.add(ability.getRegistryName().toString());
					ability.applyKeyPress(player);
					AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.KEY_ABILITY_PRESS, syncPacket.generate());
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerTick(LivingUpdateEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer) {
			if (!event.getEntityLiving().world.isRemote) {
				for (Entry<String, Integer> entry : AffinityData.For(event.getEntityLiving()).getCooldowns().entrySet()) {
					if (entry.getValue() > 0)
						AffinityData.For(event.getEntityLiving()).addCooldown(entry.getKey(), entry.getValue() - 1);
				}
			}
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getEntityLiving()))
					ability.applyTick((EntityPlayer) event.getEntityLiving());
				else
					ability.removeEffects((EntityPlayer) event.getEntityLiving());
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerHurt(LivingHurtEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getEntityLiving()))
					ability.applyHurt((EntityPlayer) event.getEntityLiving(), event, false);
			}
		}
		if (event.getSource().getTrueSource() != null && event.getSource().getTrueSource() instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getSource().getTrueSource()))
					ability.applyHurt((EntityPlayer) event.getSource().getTrueSource(), event, true);
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerFall(LivingFallEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getEntityLiving()))
					ability.applyFall((EntityPlayer) event.getEntityLiving(), event);
			}
		}
	}
	
	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getEntityLiving()))
					ability.applyDeath((EntityPlayer) event.getEntityLiving(), event);
			}
		}
		if (event.getSource().getTrueSource() != null && event.getSource().getTrueSource() instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getSource().getTrueSource()))
					ability.applyKill((EntityPlayer) event.getSource().getTrueSource(), event);
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerJump(LivingJumpEvent event) {
		if (event.getEntityLiving() instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.getEntityLiving()))
					ability.applyJump((EntityPlayer) event.getEntityLiving(), event);
			}
		}
	}
	
	@SubscribeEvent
	public void onSpellCast(SpellCastEvent.Post event) {
		if (event.entityLiving instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.entityLiving))
					ability.applySpellCast((EntityPlayer) event.entityLiving, event);
			}
		}
	}
	
	@SubscribeEvent
	public void onPreSpellCast(SpellCastEvent.Pre event) {
		if (event.entityLiving instanceof EntityPlayer) {
			for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
				if (ability.canApply((EntityPlayer) event.entityLiving))
					ability.applyPreSpellCast((EntityPlayer) event.entityLiving, event);
			}
		}
	}
}
