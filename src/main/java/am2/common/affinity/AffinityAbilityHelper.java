package am2.common.affinity;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.event.SpellCastEvent;
import am2.common.extensions.AffinityData;
import am2.network.AMNetworkHandler;
import am2.network.packets.PacketKeyAbilityPress;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map.Entry;

public class AffinityAbilityHelper {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
            if (ability.getKey() != null && ability.getKey().isPressed()) {
                EntityPlayer player = ArsMagica.proxy.getLocalPlayer();
                if (ability.canApply(player)) {
                    ability.applyKeyPress(player);
                    AMNetworkHandler.getNetwork().sendToServer(new PacketKeyAbilityPress(player.getEntityId(), ability.getRegistryName()));
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
                for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
                    if (ability.canApply((EntityPlayer) event.getEntityLiving()))
                        ability.applyTick((EntityPlayer) event.getEntityLiving());
                    else
                        ability.removeEffects((EntityPlayer) event.getEntityLiving());
                }
            }
        }
    }

    /**
     * Client-side counterpart to {@link #onPlayerTick}, for abilities that need to run on the owning
     * client too (see {@link AbstractAffinityAbility#hasClientTick()}). Only ever runs for the local
     * player: other players' movement is already server-driven and interpolated normally, and running
     * ability logic against them here would just be duplicating/fighting that.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerTickClient(LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (!player.world.isRemote || player != ArsMagica.proxy.getLocalPlayer()) return;
        for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
            if (ability.hasClientTick() && ability.canApply(player))
                ability.applyTick(player);
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
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
            if (ability.canApply(player))
                ability.applyBreakSpeed(player, event);
        }
    }

    @SubscribeEvent
    public void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        EntityPlayer player = event.getEntityPlayer();
        for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
            if (ability.canApply(player))
                ability.applyHarvestCheck(player, event);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        EntityPlayer player = event.getEntityPlayer();
        for (AbstractAffinityAbility ability : GameRegistry.findRegistry(AbstractAffinityAbility.class).getValues()) {
            if (ability.canApply(player))
                ability.applyInteractEntity(player, event);
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
