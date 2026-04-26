package am2.common.handler;

import am2.ArsMagica;
import am2.common.bosses.BossSpawnHelper;
import am2.common.trackers.EntityItemWatcher;
import am2.common.utils.DimensionUtilities;
import am2.common.world.MeteorSpawnHelper;
import am2.network.AMNetworkHandler;
import am2.network.packets.PacketFlashArmorPiece;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class ServerTickHandler {

    private boolean firstTick = true;
    public static HashMap<EntityLiving, EntityLivingBase> targetsToSet = new HashMap<EntityLiving, EntityLivingBase>();

    public static String lastWorldName;

    private void gameTick_Start() {

        if (FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName() != lastWorldName) {
            lastWorldName = FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName();
            firstTick = true;
        }

        if (firstTick) {
            // TODO ItemDefs.crystalPhylactery.getSpawnableEntities(FMLCommonHandler.instance().getMinecraftServerInstance().getWorldServer[0]);
            firstTick = false;
        }

        // ItemFrameWatcher compendium mechanic removed — replaced by CompendiumDiscoveryHandler
        // ArsMagica.proxy.itemFrameWatcher.checkWatchedFrames();
    }

    private void gameTick_End() {
        BossSpawnHelper.instance.tick();
        MeteorSpawnHelper.instance.tick();
        EntityItemWatcher.instance.tick();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            gameTick_Start();
        } else if (event.phase == TickEvent.Phase.END) {
            gameTick_End();
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
//		LogHelper.info(event.side);
//		if (AMCore.config.retroactiveWorldgen())
//			RetroactiveWorldgenerator.instance.continueRetrogen(event.world);
//
        applyDeferredPotionEffects();
        applyDeferredTargetSets();
        if (event.phase == TickEvent.Phase.END) {
            applyDeferredDimensionTransfers();
        }
    }

    private void applyDeferredPotionEffects() {
        for (EntityLivingBase ent : ArsMagica.proxy.getDeferredPotionEffects().keySet()) {
            ArrayList<PotionEffect> potions = ArsMagica.proxy.getDeferredPotionEffects().get(ent);
            for (PotionEffect effect : potions)
                ent.addPotionEffect(effect);
        }

        ArsMagica.proxy.clearDeferredPotionEffects();
    }

    private void applyDeferredDimensionTransfers() {
        for (EntityLivingBase ent : ArsMagica.proxy.getDeferredDimensionTransfers().keySet()) {
            DimensionUtilities.doDimensionTransfer(ent, ArsMagica.proxy.getDeferredDimensionTransfers().get(ent));
        }

        ArsMagica.proxy.clearDeferredDimensionTransfers();
    }

    private void applyDeferredTargetSets() {
        Iterator<Entry<EntityLiving, EntityLivingBase>> it = targetsToSet.entrySet().iterator();
        while (it.hasNext()) {
            Entry<EntityLiving, EntityLivingBase> entry = it.next();
            if (entry.getKey() != null && !entry.getKey().isDead)
                entry.getKey().setAttackTarget(entry.getValue());
            it.remove();
        }
    }

    public void addDeferredTarget(EntityLiving ent, EntityLivingBase target) {
        targetsToSet.put(ent, target);
    }

    public void blackoutArmorPiece(EntityPlayerMP player, EntityEquipmentSlot slot, int cooldown) {
        AMNetworkHandler.getNetwork().sendTo(new PacketFlashArmorPiece(player.getEntityId(), slot.getIndex()), player);
    }

}
