package am2.common.items;

import am2.ArsMagica;
import am2.common.entity.EntityThrownSickle;
import am2.common.extensions.EntityExtension;
import am2.common.utils.DummyEntityPlayer;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemNatureGuardianSickle extends Item {

    public ItemNatureGuardianSickle() {
        super();
        setMaxStackSize(1);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();
        if (slot.equals(EntityEquipmentSlot.MAINHAND)) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 9, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -3, 0));
        }
        return multimap;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.translateToLocalFormatted("am2.tooltip.nature_scythe"));
        super.addInformation(stack, world, tooltip, flag);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        int radius = 1;
        for (int i = -radius; i <= radius; ++i) {
            for (int j = -radius; j <= radius; ++j) {
                for (int k = -radius; k <= radius; ++k) {

                    if (EntityExtension.For(entityLiving).getCurrentMana() < 5f) {
                        if (worldIn.isRemote)
                            ArsMagica.proxy.flashManaBar();
                        return false;
                    }

                    IBlockState nextBlock = worldIn.getBlockState(pos.add(i, j, k));
                    if (nextBlock == null) continue;
                    if (nextBlock.getBlock() instanceof BlockLeaves) {
                        if (ForgeEventFactory.doPlayerHarvestCheck(DummyEntityPlayer.fromEntityLiving(entityLiving), nextBlock, true))
                            if (!worldIn.isRemote)
                                worldIn.destroyBlock(pos.add(i, j, k), true);
                        EntityExtension.For(entityLiving).deductMana(5f);
                    }
                }
            }
        }

        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (flingSickle(stack, world, player)) {
            player.setItemStackToSlot(hand == EnumHand.MAIN_HAND ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);//inventory.setInventorySlotContents(player.inventory.currentItem, null);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public boolean flingSickle(ItemStack stack, World world, EntityPlayer player) {
        if (!EntityExtension.For(player).hasEnoughMana(250) && !player.capabilities.isCreativeMode) {
            if (world.isRemote)
                ArsMagica.proxy.flashManaBar();
            return false;
        }
        if (!world.isRemote) {
            EntityThrownSickle projectile = new EntityThrownSickle(world, player, 1.25f);
            projectile.setSickleNBT(stack);
            projectile.setThrowingEntity(player);
            projectile.setProjectileSpeed(2.0);
            //projectile.setInMotion(1.25);
            world.spawnEntity(projectile);
            EntityExtension.For(player).deductMana(250f);
        }
        return true;
    }
}
