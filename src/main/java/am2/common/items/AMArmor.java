package am2.common.items;

import am2.common.armor.ArsMagicaArmorMaterial;
import am2.common.registry.AMTabs;
import com.google.common.collect.Multimap;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class AMArmor extends ItemArmor implements ISpecialArmor {

    private static final int[] maxDamageArray = {
            11, 16, 15, 13
    };
    public final EntityEquipmentSlot armorType;
    public final int damageReduceAmount;
    private final ArsMagicaArmorMaterial material;
    private final int damageReduction;
    private final float infusionCost;
    private final float infusionRepair;

    public static final String NBT_KEY_AMPROPS = "AMArmorProperties";
    public static final String NBT_KEY_EFFECTS = "armorEffects";
    public static final String NBT_KEY_TOTALXP = "infusedXP";
    public static final String NBT_KEY_ARMORLEVEL = "XPLevel";
    public static final String INFUSION_DELIMITER = "\\|";
    private static final String NBT_KEY_DISPLAY = "display";
    private static final String NBT_KEY_COLOR = "color";
    public static final int DEFAULT_COLOR = 0xA67C52; // lighter tan brown

    public AMArmor(ArmorMaterial material, ArsMagicaArmorMaterial armorMaterial, int renderIndex, EntityEquipmentSlot equipmentSlot) {
        super(material, renderIndex, equipmentSlot);
        this.material = armorMaterial;
        armorType = equipmentSlot;
        damageReduceAmount = 0;
        setMaxDamage(armorMaterial.getMaxDamage(equipmentSlot));
        setCreativeTab(AMTabs.AMITEMS);
        maxStackSize = 1;
        damageReduction = armorMaterial.getDamageReductionAmount(equipmentSlot);
        infusionCost = armorMaterial.getInfusionCost();
        infusionRepair = armorMaterial.getInfusionRepair();
    }

    @Override
    public int getItemEnchantability() {
        return material.getEnchantability();
    }

    public static int[] getMaxDamageArray() {
        return maxDamageArray;
    }

    public int GetDamageReduction() {
        return damageReduction;
    }

    public float GetInfusionCost() {
        return infusionCost;
    }

    public float GetInfusionRepair() {
        return infusionRepair;
    }

    public int getMaterialID() {
        return material.getMaterialID();
    }

    /**
     * Checks if the armor is "broken" - at maximum damage value.
     * Broken armor provides no stats but doesn't get destroyed.
     */
    public static boolean isArmorBroken(ItemStack armor) {
        return armor.getItemDamage() >= armor.getMaxDamage() - 1;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
        // Remove the vanilla ARMOR attribute — ISpecialArmor handles protection via getProperties()
        // and display via getArmorDisplay(). Keeping the attribute would stack both systems.
        multimap.removeAll(SharedMonsterAttributes.ARMOR.getName());
        multimap.removeAll(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName());
        return multimap;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable net.minecraft.world.World world, List<String> tooltip, ITooltipFlag flag) {
        int armorValue = GetDamageReduction();
        if (isArmorBroken(stack)) {
            armorValue = 0;
        }
        tooltip.add(I18n.translateToLocalFormatted("attribute.modifier.equals.0", armorValue, I18n.translateToLocalFormatted("attribute.name.generic.armor")));
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
        if (isArmorBroken(armor)) {
            return 0;
        }
        return GetDamageReduction();
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
        // Broken armor provides no protection
        if (isArmorBroken(armor)) {
            return new ArmorProperties(0, 0, 0);
        }
        ArmorProperties ap = new ArmorProperties(1, material.getDamageReduceRatio(slot), 1000);
        return ap;
    }

    @Override
    public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
        // Armor doesn't take damage from these sources
        if (source == DamageSource.FALL || source == DamageSource.IN_WALL || source == DamageSource.DROWN || source == DamageSource.STARVE) {
            return;
        }
        if (source.isUnblockable()) {
            return;
        }

        // Calculate damage amount
        int damageAmount;
        if (source == DamageSource.ON_FIRE || source == DamageSource.MAGIC) {
            damageAmount = damage * 7;
        } else {
            damageAmount = damage * 10;
        }

        // Apply damage but cap at max - 1 to prevent breaking
        int newDamage = Math.min(stack.getItemDamage() + damageAmount, stack.getMaxDamage() - 1);
        stack.setItemDamage(newDamage);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return null;
    }

    @Override
    public boolean hasColor(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey(NBT_KEY_DISPLAY, 10) && tag.getCompoundTag(NBT_KEY_DISPLAY).hasKey(NBT_KEY_COLOR, 3);
    }

    @Override
    public int getColor(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(NBT_KEY_DISPLAY, 10)) {
            NBTTagCompound display = tag.getCompoundTag(NBT_KEY_DISPLAY);
            if (display.hasKey(NBT_KEY_COLOR, 3)) {
                return display.getInteger(NBT_KEY_COLOR);
            }
        }
        return DEFAULT_COLOR;
    }

    @Override
    public void setColor(ItemStack stack, int color) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        NBTTagCompound display = tag.getCompoundTag(NBT_KEY_DISPLAY);
        if (!tag.hasKey(NBT_KEY_DISPLAY, 10)) {
            tag.setTag(NBT_KEY_DISPLAY, display);
        }
        display.setInteger(NBT_KEY_COLOR, color);
    }

    @Override
    public void removeColor(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(NBT_KEY_DISPLAY, 10)) {
            NBTTagCompound display = tag.getCompoundTag(NBT_KEY_DISPLAY);
            if (display.hasKey(NBT_KEY_COLOR)) {
                display.removeTag(NBT_KEY_COLOR);
            }
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_KEY_AMPROPS)) {
            String s = ((NBTTagCompound) stack.getTagCompound().getTag(NBT_KEY_AMPROPS)).getString(NBT_KEY_EFFECTS);
            return s != null && !s.isEmpty();
        }
        return super.hasEffect(stack);
    }

    /**
     * Override to prevent the armor from ever being destroyed.
     * Caps damage at maxDamage - 1 regardless of how damage is applied.
     */
    @Override
    public void setDamage(ItemStack stack, int damage) {
        int maxDamage = stack.getMaxDamage();
        // Cap damage at maxDamage - 1 to prevent breaking
        super.setDamage(stack, Math.min(damage, maxDamage - 1));
    }
}
