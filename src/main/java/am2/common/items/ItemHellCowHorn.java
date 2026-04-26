package am2.common.items;

import am2.common.registry.AMEnchantments;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedHashMap;
import java.util.Map;

public class ItemHellCowHorn extends Item {

    public ItemHellCowHorn() {
        super();
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player,
                                     Entity entity) {
        if (entity instanceof EntityLivingBase) {
			/*double dx = player.posX - entity.posX;
			double dz = player.posZ - entity.posZ;
			float angle = (float) Math.atan2(dz, dx);
			((EntityLivingBase)entity).addVelocity(-Math.cos(angle) * 3, 0.4f, -Math.sin(angle) * 3);
			//entity.attackEntityFrom(DamageSource.GENERIC, 7);
*/			
			/*if (player.world.rand.nextInt(10) < 3) 
				SoundHelper.instance.playSoundAtEntity(player.world, player, player.world.rand.nextBoolean() ? "mob.moo.neutral" : "mob.moo.death", 1.0f);*/
        }
        return false;
    }

    public ItemStack createItemStack() {
        ItemStack stack = new ItemStack(this, 1, 0);
        Map<Enchantment, Integer> map = new LinkedHashMap<Enchantment, Integer>();
        map.put(AMEnchantments.soulbound, 1);
        map.put(Enchantments.FIRE_ASPECT, 3);
        EnchantmentHelper.setEnchantments(map, stack);
        return stack;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;

        items.add(createItemStack());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldRotateAroundWhenRendering() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }
}
