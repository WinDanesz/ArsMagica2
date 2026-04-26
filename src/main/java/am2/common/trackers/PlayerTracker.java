package am2.common.trackers;

import am2.ArsMagica;
import am2.common.armor.ArmorHelper;
import am2.common.extensions.AffinityData;
import am2.common.extensions.EntityExtension;
import am2.common.extensions.SkillData;
import am2.common.handler.ServerTickHandler;
import am2.common.lore.ArcaneCompendium;
import am2.common.registry.AMEnchantments;
import am2.common.registry.ImbuementRegistry;
import am2.common.utils.EntityUtils;
import am2.network.AMNetworkHandler;
import am2.network.packets.PacketPlayerLogin;
import am2.network.packets.PacketSyncWorldName;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

public class PlayerTracker {

    public static HashMap<UUID, HashMap<Integer, ItemStack>> soulbound_Storage;

    private TreeMap<String, Integer> aals;
    private TreeMap<String, String> clls;
    private TreeMap<String, Integer> cldm;

    public PlayerTracker() {
        soulbound_Storage = new HashMap<UUID, HashMap<Integer, ItemStack>>();

        aals = new TreeMap<String, Integer>();
        clls = new TreeMap<String, String>();
        cldm = new TreeMap<String, Integer>();
    }

    public void postInit() {

    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        syncPlayerDataOnJoin((EntityPlayerMP) event.player);
    }

    private void syncPlayerDataOnJoin(EntityPlayerMP player) {
        ArsMagica.disabledSkills.getDisabledSkills(true);
        int[] disabledSkills = ArsMagica.disabledSkills.getDisabledSkillIDs();

        AMNetworkHandler.getNetwork().sendTo(
                new PacketPlayerLogin(ArsMagica.config.getSkillTreeSecondaryTierCap(), disabledSkills, ArsMagica.config.getManaCap(), ArsMagica.config.getOldXpCalculations()),
                player);
        if (ServerTickHandler.lastWorldName != null)
            AMNetworkHandler.getNetwork().sendTo(new PacketSyncWorldName(ServerTickHandler.lastWorldName), player);

        EntityExtension.For(player).forceUpdate();
        AffinityData.For(player).forceUpdate();
        SkillData.For(player).forceUpdate();
        ArcaneCompendium.For(player).forceUpdate();
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        //kill any summoned creatures
        if (!event.player.world.isRemote) {
            List<Entity> list = event.player.world.loadedEntityList;
            for (Object o : list) {
                if (o instanceof EntityLivingBase && EntityUtils.isSummon((EntityLivingBase) o) && EntityUtils.getOwner((EntityLivingBase) o) == event.player.getEntityId()) {
                    ((EntityLivingBase) o).setDead();
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        //================================================================================
        //soulbound items
        //================================================================================
        if (soulbound_Storage.containsKey(event.player.getUniqueID())) {
            HashMap<Integer, ItemStack> soulboundItems = soulbound_Storage.get(event.player.getUniqueID());
            for (Integer i : soulboundItems.keySet()) {
                if (i < event.player.inventory.getSizeInventory())
                    event.player.inventory.setInventorySlotContents(i, soulboundItems.get(i));
                else
                    event.player.entityDropItem(soulboundItems.get(i), 0);
            }
        }
        //================================================================================
        //Syncing data.
        syncPlayerDataOnJoin((EntityPlayerMP) event.player);
    }

    public void onPlayerDeath(EntityPlayer player) {
        storeSoulboundItemsForRespawn(player);
    }

    public static void storeSoulboundItemsForRespawn(EntityPlayer player) {
        if (soulbound_Storage.containsKey(player.getUniqueID()))
            soulbound_Storage.remove(player.getUniqueID());

        HashMap<Integer, ItemStack> soulboundItems = new HashMap<Integer, ItemStack>();

        int slotCount = 0;
        for (ItemStack stack : player.inventory.mainInventory) {
            int soulbound_level = EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound, stack);
            if (soulbound_level > 0) {
                soulboundItems.put(slotCount, stack.copy());
                player.inventory.setInventorySlotContents(slotCount, ItemStack.EMPTY);
            }
            slotCount++;
        }
        slotCount = 0;
        for (ItemStack stack : player.inventory.armorInventory) {
            int soulbound_level = EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound, stack);
            if (soulbound_level > 0 || ArmorHelper.isInfusionPreset(stack, ImbuementRegistry.SOULBOUND)) {
                soulboundItems.put(slotCount + player.inventory.mainInventory.size(), stack.copy());
                player.inventory.setInventorySlotContents(slotCount + player.inventory.mainInventory.size(), ItemStack.EMPTY);
            }
            slotCount++;
        }

        soulbound_Storage.put(player.getUniqueID(), soulboundItems);
    }

    public static void storeSoulboundItemForRespawn(EntityPlayer player, ItemStack stack) {
        if (!soulbound_Storage.containsKey(player.getUniqueID()))
            return;

        HashMap<Integer, ItemStack> soulboundItems = soulbound_Storage.get(player.getUniqueID());

        int slotTest = 0;
        while (soulboundItems.containsKey(slotTest)) {
            slotTest++;
            if (slotTest == player.inventory.mainInventory.size())
                slotTest += player.inventory.armorInventory.size();
        }

        soulboundItems.put(slotTest, stack);
    }

    public String getCLF(String uuid) {
        return clls.get(uuid.toLowerCase());
    }

    public boolean hasCLS(String uuid) {
        return clls.containsKey(uuid.toLowerCase());
    }

    public boolean hasCLDM(String uuid) {
        return cldm.containsKey(uuid.toLowerCase());
    }

    public int getCLDM(String uuid) {
        return cldm.get(uuid.toLowerCase());
    }
}
