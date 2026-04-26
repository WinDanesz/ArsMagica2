package am2.common.items;

import am2.client.utils.ModelLibrary;
import am2.common.armor.ArsMagicaArmorMaterial;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMageRobe extends AMArmor {

    public ItemMageRobe(ArmorMaterial inheritFrom, ArsMagicaArmorMaterial material, int renderIndex, EntityEquipmentSlot slot) {
        super(inheritFrom, material, renderIndex, slot);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        ModelBiped model = ModelLibrary.instance.mageRobe;
        model.setModelAttributes(_default);
        return model;
    }
}
