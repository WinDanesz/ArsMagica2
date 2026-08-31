package am2.common.armor;

import am2.ArsMagica;
import am2.api.items.armor.ArmorImbuement;
import am2.api.items.armor.ImbuementTiers;
import am2.common.extensions.EntityExtension;
import am2.common.items.AMArmor;
import am2.common.registry.ImbuementRegistry;
import am2.common.utils.EntityUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class ArmorHelper {

    /** Shared empty array to avoid repeated allocations. */
    private static final ArmorImbuement[] EMPTY_IMBUEMENTS = new ArmorImbuement[0];

    /**
     * Per-player, per-slot infusion cache. Keyed on the raw NBT infusion string so it
     * auto-invalidates on changes. WeakHashMap releases entries when the player is GC'd.
     */
    private static final Map<EntityPlayer, EnumMap<EntityEquipmentSlot, Object[]>>
            armorInfusionCache = Collections.synchronizedMap(new WeakHashMap<>());

    /** Call on player logout to release the cache entry promptly. */
    public static void clearArmorCache(EntityPlayer player) {
        armorInfusionCache.remove(player);
    }

    private static final int IMBUE_TIER_COST = 12;

    public static boolean PlayerHasArmorInSlot(EntityPlayer player, EntityEquipmentSlot armorSlot) {
        ItemStack stack = player.getItemStackFromSlot(armorSlot);
        return !stack.isEmpty();
    }

    /** Run infusion logic every N ticks; costs are scaled up to keep the same rate. */
    private static final int INFUSION_TICK_INTERVAL = 5;

    //======================================================================================================
    // Infusion
    //======================================================================================================
    public static void HandleArmorInfusion(EntityPlayer player) {
        if (player.ticksExisted % INFUSION_TICK_INTERVAL != 0) return;

        EntityExtension ext = EntityExtension.For(player);

        if (ext.getCurrentLevel() < 1) {
            return;
        }

        float infusionCost = 0.0f;
        boolean bFullSet = getFullArsMagicaArmorSet(player) != -1;
        for (EntityEquipmentSlot i : EntityEquipmentSlot.values()) {
            if (i.getSlotType() != Type.ARMOR)
                continue;
            infusionCost += GetArsMagicaArmorInfusionCostFromSlot(player, i);
        }
        if (bFullSet) {
            infusionCost *= 0.75;
        }

        infusionCost *= INFUSION_TICK_INTERVAL; // scale up to compensate for tick throttling
        infusionCost *= (float) ArsMagica.config.getArmorInfusionCostMultiplier();

        if (infusionCost > 0 && ext.hasEnoughMana(infusionCost)) {
            //deduct mana
            ext.deductMana(infusionCost);
            //bank infusion (scaled by interval to maintain the same repair rate)
            ext.bankedInfusionHelm += GetArsMagicaArmorRepairAmountFromSlot(player, EntityEquipmentSlot.HEAD) * INFUSION_TICK_INTERVAL;
            ext.bankedInfusionChest += GetArsMagicaArmorRepairAmountFromSlot(player, EntityEquipmentSlot.CHEST) * INFUSION_TICK_INTERVAL;
            ext.bankedInfusionLegs += GetArsMagicaArmorRepairAmountFromSlot(player, EntityEquipmentSlot.LEGS) * INFUSION_TICK_INTERVAL;
            ext.bankedInfusionBoots += GetArsMagicaArmorRepairAmountFromSlot(player, EntityEquipmentSlot.FEET) * INFUSION_TICK_INTERVAL;


            //repair armor if infusion bank is above 1
            if (ext.bankedInfusionHelm > 1) {
                int repairAmount = (int) Math.floor(ext.bankedInfusionHelm);
                RepairEquippedArsMagicaArmorItem(player, repairAmount, EntityEquipmentSlot.HEAD);
                ext.bankedInfusionHelm -= repairAmount;
            }

            if (ext.bankedInfusionChest > 1) {
                int repairAmount = (int) Math.floor(ext.bankedInfusionChest);
                RepairEquippedArsMagicaArmorItem(player, repairAmount, EntityEquipmentSlot.CHEST);
                ext.bankedInfusionChest -= repairAmount;
            }

            if (ext.bankedInfusionLegs > 1) {
                int repairAmount = (int) Math.floor(ext.bankedInfusionLegs);
                RepairEquippedArsMagicaArmorItem(player, repairAmount, EntityEquipmentSlot.LEGS);
                ext.bankedInfusionLegs -= repairAmount;
            }

            if (ext.bankedInfusionBoots > 1) {
                int repairAmount = (int) Math.floor(ext.bankedInfusionBoots);
                RepairEquippedArsMagicaArmorItem(player, repairAmount, EntityEquipmentSlot.FEET);
                ext.bankedInfusionBoots -= repairAmount;
            }
        }
    }

    private static void RepairEquippedArsMagicaArmorItem(EntityPlayer player, int repairAmount, EntityEquipmentSlot slot) {
        if (!PlayerHasArmorInSlot(player, slot)) {
            return;
        }
        ItemStack armor = player.getItemStackFromSlot(slot);
        if (armor.isItemDamaged()) {
            armor.setItemDamage(armor.getItemDamage() - repairAmount);
        }
    }

    private static boolean PlayerHasArsInfusableInSlot(EntityPlayer player, EntityEquipmentSlot armorSlot) {
        ItemStack stack = player.getItemStackFromSlot(armorSlot);
        return !stack.isEmpty() && (stack.getItem() instanceof AMArmor);
    }

    private static float GetArsMagicaArmorInfusionCostFromSlot(EntityPlayer player, EntityEquipmentSlot armorSlot) {
        if (!PlayerHasArsInfusableInSlot(player, armorSlot)) {
            return 0;
        }
        if (player.getItemStackFromSlot(armorSlot).isItemDamaged()) {
            AMArmor armor = (AMArmor) player.getItemStackFromSlot(armorSlot).getItem();
            return armor.GetInfusionCost();
        }
        return 0;
    }

    public static int getFullArsMagicaArmorSet(EntityPlayer player) {
        int matlID = -1;
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() != EntityEquipmentSlot.Type.ARMOR)
                continue;
            if (!PlayerHasArsInfusableInSlot(player, slot)) {
                return -1;
            }
            ItemStack stack = player.getItemStackFromSlot(slot);
            if (AMArmor.isArmorBroken(stack)) {
                return -1;
            }
            AMArmor armor = (AMArmor) stack.getItem();
            if (matlID == -1)
                matlID = armor.getMaterialID();
            else {
                if (matlID != armor.getMaterialID()) {
                    return -1;
                }
            }
        }
        return matlID;
    }

    private static float GetArsMagicaArmorRepairAmountFromSlot(EntityPlayer player, EntityEquipmentSlot armorSlot) {
        if (!PlayerHasArsInfusableInSlot(player, armorSlot)) {
            return 0;
        }
        if (player.getItemStackFromSlot(armorSlot).isItemDamaged()) {
            AMArmor armor = (AMArmor) player.getItemStackFromSlot(armorSlot).getItem();
            return armor.GetInfusionRepair();
        }
        return 0;
    }

    public static ArmorImbuement[] getInfusionsOnArmor(EntityPlayer player, EntityEquipmentSlot armorSlot) {
        ItemStack stack = player.getItemStackFromSlot(armorSlot);

        if (stack.isEmpty() || !(stack.getItem() instanceof ItemArmor)) {
            return EMPTY_IMBUEMENTS;
        }

        // Cache key: raw NBT infusion string (cheap to read; only parse on miss)
        String currentList = null;
        if (stack.hasTagCompound()) {
            NBTBase propsTag = stack.getTagCompound().getTag(AMArmor.NBT_KEY_AMPROPS);
            if (propsTag instanceof NBTTagCompound) {
                String s = ((NBTTagCompound) propsTag).getString(AMArmor.NBT_KEY_EFFECTS);
                if (!s.isEmpty()) currentList = s;
            }
        }

        // Cache hit
        EnumMap<EntityEquipmentSlot, Object[]> slotMap = armorInfusionCache.get(player);
        if (slotMap != null) {
            Object[] entry = slotMap.get(armorSlot);
            if (entry != null && Objects.equals(entry[0], currentList)) {
                return (ArmorImbuement[]) entry[1];
            }
        }

        // Cache miss: parse and store
        ArmorImbuement[] result;
        if (currentList == null) {
            result = EMPTY_IMBUEMENTS;
        } else {
            String[] ids = currentList.split(AMArmor.INFUSION_DELIMITER);
            result = new ArmorImbuement[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                result[i] = ImbuementRegistry.instance.getImbuementByID(new ResourceLocation(ids[i]));
            }
        }

        if (slotMap == null) {
            slotMap = new EnumMap<>(EntityEquipmentSlot.class);
            armorInfusionCache.put(player, slotMap);
        }
        slotMap.put(armorSlot, new Object[]{currentList, result});

        return result;
    }

    public static ArmorImbuement[] getInfusionsOnArmor(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound() || !(stack.getItem() instanceof ItemArmor))
            return EMPTY_IMBUEMENTS;
        NBTBase propsTag = stack.getTagCompound().getTag(AMArmor.NBT_KEY_AMPROPS);
        if (propsTag instanceof NBTTagCompound) {
            String infusionList = ((NBTTagCompound) propsTag).getString(AMArmor.NBT_KEY_EFFECTS);
            if (infusionList != null && !infusionList.isEmpty()) {
                String[] ids = infusionList.split(AMArmor.INFUSION_DELIMITER);
                ArmorImbuement[] infusions = new ArmorImbuement[ids.length];
                for (int i = 0; i < ids.length; ++i) {
                    infusions[i] = ImbuementRegistry.instance.getImbuementByID(new ResourceLocation(ids[i]));
                }
                return infusions;
            }
        }
        return EMPTY_IMBUEMENTS;
    }

    public static boolean isInfusionPreset(ItemStack stack, String id) {
        if (stack.isEmpty() || !stack.hasTagCompound())
            return false;
        NBTTagCompound armorProps = (NBTTagCompound) stack.getTagCompound().getTag(AMArmor.NBT_KEY_AMPROPS);
        if (armorProps != null) {
            String infusionList = armorProps.getString(AMArmor.NBT_KEY_EFFECTS);
            if (infusionList != null) {
                return infusionList.contains(id);
            }
        }
        return false;
    }

    public static void imbueArmor(ItemStack armorStack, ResourceLocation id, boolean ignoreLevelRequirement) {
        ArmorImbuement imbuement = ImbuementRegistry.instance.getImbuementByID(id);
        if (!armorStack.isEmpty() && imbuement != null && armorStack.getItem() instanceof ItemArmor) {

            if (!ignoreLevelRequirement && getArmorLevel(armorStack) < getImbueCost(imbuement.getTier()))
                return;

            for (EntityEquipmentSlot i : imbuement.getValidSlots()) {
                if (i == ((ItemArmor) armorStack.getItem()).armorType) {
                    if (!armorStack.hasTagCompound())
                        armorStack.setTagCompound(new NBTTagCompound());
                    NBTTagCompound armorProps = null;
                    if (armorStack.getTagCompound() != null) {
                        armorProps = (NBTTagCompound) armorStack.getTagCompound().getTag(AMArmor.NBT_KEY_AMPROPS);
                    }
                    if (armorProps == null)
                        armorProps = new NBTTagCompound();
                    String infusionList = armorProps.getString(AMArmor.NBT_KEY_EFFECTS);
                    if (infusionList == null || infusionList.isEmpty())
                        infusionList = id.toString();
                    else
                        infusionList += "|" + id;
                    armorProps.setString(AMArmor.NBT_KEY_EFFECTS, infusionList);
                    armorStack.getTagCompound().setTag(AMArmor.NBT_KEY_AMPROPS, armorProps);

                    deductXPFromArmor(EntityUtils.getXPFromLevel(getImbueCost(imbuement.getTier())), armorStack);
                    break;
                }
            }
        }
    }

    public static int getArmorLevel(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound())
            return 0;
        NBTTagCompound armorProps = null;
        if (stack.getTagCompound() != null) {
            armorProps = (NBTTagCompound) stack.getTagCompound().getTag(AMArmor.NBT_KEY_AMPROPS);
        }
        if (armorProps != null) {
            return armorProps.getInteger(AMArmor.NBT_KEY_ARMORLEVEL);
        }
        return 0;
    }

    public static int getImbueCost(ImbuementTiers tier) {
        return 30 + tier.ordinal() * IMBUE_TIER_COST;
    }

    public static void addXPToArmor(float totalAmt, EntityPlayer player) {
        int numPieces = 0;
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() != EntityEquipmentSlot.Type.ARMOR)
                continue;
            if (!player.getItemStackFromSlot(slot).isEmpty())
                numPieces++;
        }
        float xpPerPiece = totalAmt / numPieces;
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() != EntityEquipmentSlot.Type.ARMOR)
                continue;
            addXPToArmor(xpPerPiece, player.getItemStackFromSlot(slot));
        }
    }

    public static void deductXPFromArmor(float amt, ItemStack armor) {
        if (!armor.isEmpty() && armor.getItem() instanceof ItemArmor) {
            if (!armor.hasTagCompound())
                armor.setTagCompound(new NBTTagCompound());
            NBTTagCompound armorProps = null;
            if (armor.getTagCompound() != null) {
                armorProps = (NBTTagCompound) armor.getTagCompound().getTag(AMArmor.NBT_KEY_AMPROPS);
            }
            if (armorProps == null)
                armorProps = new NBTTagCompound();
            armorProps.setDouble(AMArmor.NBT_KEY_TOTALXP, Math.max(armorProps.getDouble(AMArmor.NBT_KEY_TOTALXP) - amt, 0));
            armorProps.setInteger(AMArmor.NBT_KEY_ARMORLEVEL, EntityUtils.getLevelFromXP((float) armorProps.getDouble(AMArmor.NBT_KEY_TOTALXP)));
            armor.getTagCompound().setTag(AMArmor.NBT_KEY_AMPROPS, armorProps);
        }
    }

    public static void addXPToArmor(float amt, ItemStack armor) {
        if (!armor.isEmpty() && armor.getItem() instanceof ItemArmor) {
            if (!armor.hasTagCompound())
                armor.setTagCompound(new NBTTagCompound());
            NBTTagCompound armorProps = null;
            if (armor.getTagCompound() != null) {
                armorProps = (NBTTagCompound) armor.getTagCompound().getTag(AMArmor.NBT_KEY_AMPROPS);
            }
            if (armorProps == null)
                armorProps = new NBTTagCompound();
            armorProps.setDouble(AMArmor.NBT_KEY_TOTALXP, armorProps.getDouble(AMArmor.NBT_KEY_TOTALXP) + amt);
            armorProps.setInteger(AMArmor.NBT_KEY_ARMORLEVEL, EntityUtils.getLevelFromXP((float) armorProps.getDouble(AMArmor.NBT_KEY_TOTALXP)));
            armor.getTagCompound().setTag(AMArmor.NBT_KEY_AMPROPS, armorProps);
        }
    }
}
