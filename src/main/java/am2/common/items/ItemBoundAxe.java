package am2.common.items;

import am2.api.items.IBoundItem;
import am2.common.registry.AMMaterials;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBoundAxe extends ItemAxe implements IBoundItem {

    public ItemBoundAxe() {
        super(AMMaterials.BOUND, 8, -3);
        this.maxStackSize = 1;
        this.setMaxDamage(0);
        this.setCreativeTab(null);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        return applySpellOnHitEntity(stack, target, attacker);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        return applySpellAtPosition(stack, worldIn, entityLiving, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        return unbindOnDrop(item, player);
    }

    @Override
    public float maintainCost(EntityPlayer player, ItemStack stack) {
        return normalMaintain;
    }
}
