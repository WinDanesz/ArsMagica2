package am2.common.items;

import am2.client.utils.ModelLibrary;
import am2.common.armor.ArsMagicaArmorMaterial;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemEarthGuardianArmor extends AMArmor {

    private static final UUID EARTH_ARMOR_SPEED_MODIFIER = UUID.fromString("a3d23c84-5e2b-4c8a-9f1d-7b6e8d9a0c3f");

    public ItemEarthGuardianArmor(ArmorMaterial inheritFrom, ArsMagicaArmorMaterial enumarmormaterial, int par3, EntityEquipmentSlot par4) {
        super(inheritFrom, enumarmormaterial, par3, par4);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        return ModelLibrary.instance.earthArmor;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "arsmagica2:textures/entities/bosses/earth_guardian.png";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.translateToLocalFormatted("am2.tooltip.earth_armor"));
        tooltip.add(I18n.translateToLocalFormatted("am2.tooltip.earth_armor_slow"));
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> modifiers = super.getAttributeModifiers(slot, stack);
        if (slot == EntityEquipmentSlot.CHEST) {
            Multimap<String, AttributeModifier> result = HashMultimap.create(modifiers);
            result.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
                    new AttributeModifier(EARTH_ARMOR_SPEED_MODIFIER, "Earth Armor Speed Penalty", -0.15, 2));
            return result;
        }
        return modifiers;
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
        return 16;
    }

}
