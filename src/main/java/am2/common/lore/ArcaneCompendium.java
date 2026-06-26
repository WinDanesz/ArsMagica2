package am2.common.lore;

import am2.ArsMagica;
import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.api.extensions.IArcaneCompendium;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import am2.common.utils.NBTUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import java.util.ArrayList;

public class ArcaneCompendium implements IArcaneCompendium, ICapabilityProvider, ICapabilitySerializable<NBTBase> {

    @CapabilityInject(IArcaneCompendium.class)
    public static Capability<IArcaneCompendium> INSTANCE = null;

    public static final int SYNC_COMPENDIUM = 0x1;

    private EntityPlayer player;
    private String path = "";
    private int syncCode = 0;

    private ArrayList<String> compendium;

    public ArcaneCompendium() {
        compendium = new ArrayList<>();
    }

    public void unlockEntry(String name) {
        if (!isUnlocked(name)) {
            compendium.add(name);
            syncCode |= SYNC_COMPENDIUM;
            ArsMagica.proxy.showCompendiumToast(name);
        }
    }

    public boolean isUnlocked(String name) {
        if (!ArsMagica.config.stagedCompendium())
            return true;
        // entry.getID() returns "category.id" but unlock calls may store just "id" (bare name)
        // so compare only the last segment of each to handle both cases
        String simpleName = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name;
        for (String str : compendium) {
            String simpleStr = str.contains(".") ? str.substring(str.lastIndexOf('.') + 1) : str;
            if (simpleStr.equalsIgnoreCase(simpleName))
                return true;
        }
        return false;
    }

    public void init(EntityPlayer player) {
        this.player = player;
    }

    public static IArcaneCompendium For(EntityPlayer entityPlayer) {
        return entityPlayer.getCapability(INSTANCE, null);
    }

    @Override
    public boolean isNew(String id) {
        return false;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == INSTANCE)
            return (T) this;
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return new IArcaneCompendium.Storage().writeNBT(INSTANCE, this, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        // Directly populate the list without calling unlockEntry(), which would
        // trigger showCompendiumToast() for every entry on every world load.
        compendium.clear();
        NBTTagCompound am2tag = NBTUtils.getAM2Tag((NBTTagCompound) nbt);
        NBTTagList unlocks = NBTUtils.addCompoundList(am2tag, "Unlocks");
        for (int i = 0; i < unlocks.tagCount(); i++) {
            NBTTagCompound tmp = unlocks.getCompoundTagAt(i);
            if (tmp.getBoolean("Unlocked")) {
                String id = tmp.getString("ID");
                if (!compendium.contains(id))
                    compendium.add(id);
            }
        }
        this.path = am2tag.getString("Path");
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String str) {
        this.path = str;
    }

    @Override
    public void unlockRelatedItems(ItemStack crafting) {
        for (CompendiumEntry entry : CompendiumCategory.getAllEntries()) {
            Object obj = entry.getRenderObject();
            if (obj == null)
                continue;
            else if (obj instanceof ItemStack && ((ItemStack) obj).isItemEqual(crafting))
                unlockEntry(entry.getFullID());
            else if (obj instanceof Item && crafting.getItem() == obj)
                unlockEntry(entry.getFullID());
            else if (obj instanceof Block && crafting.getItem() instanceof ItemBlock && ((ItemBlock) crafting.getItem()).getBlock() == obj)
                unlockEntry(entry.getFullID());
        }
    }

    @Override
    public ArrayList<CompendiumEntry> getEntriesForCategory(String categoryName) {
        return null;
    }

    @Override
    public boolean shouldUpdate() {
        return syncCode != 0;
    }

    @Override
    public byte[] generateUpdatePacket() {
        AMDataWriter writer = new AMDataWriter();
        writer.add(syncCode);
        if ((syncCode & SYNC_COMPENDIUM) == SYNC_COMPENDIUM) {
            writer.add(compendium.size());
            for (String entry : compendium)
                writer.add(entry);
        }
        syncCode = 0;
        return writer.generate();
    }

    @Override
    public void handleUpdatePacket(byte[] bytes) {
        AMDataReader reader = new AMDataReader(bytes, false);
        int syncCode = reader.getInt();
        if ((syncCode & SYNC_COMPENDIUM) == SYNC_COMPENDIUM) {
            compendium.clear();
            int size = reader.getInt();
            for (int i = 0; i < size; i++) {
                compendium.add(reader.getString());
            }
        }
    }

    @Override
    public void forceUpdate() {
        this.syncCode = 0xFFFFFFFF;
    }
}
