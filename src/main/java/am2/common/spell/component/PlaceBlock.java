package am2.common.spell.component;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.common.registry.Affinities;
import am2.common.utils.InventoryUtilities;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class PlaceBlock extends SpellComponent {

    public static final String KEY_STATE = "PlaceState";

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                Items.STONE_AXE,
                Items.STONE_PICKAXE,
                Items.STONE_SHOVEL,
                Blocks.CHEST
        };
    }

    private IBlockState getPlaceBlock(SpellData spell) {
        ItemStack sourceStack = spell.getSource();
        if (sourceStack.hasTagCompound()) {
            NBTTagCompound tag = sourceStack.getTagCompound();
            int stateId = tag.getInteger(KEY_STATE);
            if (stateId != 0) {
                return Block.getStateById(stateId);
            }
        }
        return null;
    }

    private void setPlaceBlock(SpellData spell, IBlockState state) {
        ItemStack sourceStack = spell.getSource();
        if (!sourceStack.hasTagCompound())
            sourceStack.setTagCompound(new NBTTagCompound());
        NBTTagCompound sourceTag = sourceStack.getTagCompound();
        sourceTag.setInteger(KEY_STATE, Block.getStateId(state));
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        if (!(caster instanceof EntityPlayer))
            return false;

        EntityPlayer player = (EntityPlayer) caster;
        IBlockState state = getPlaceBlock(spell);

        if (state != null && !caster.isSneaking()) {
            if (world.isAirBlock(pos) || !world.getBlockState(pos).isSideSolid(world, pos, blockFace))
                blockFace = null;
            if (blockFace != null) {
                pos = pos.add(blockFace.getDirectionVec());
            }
            if (world.isAirBlock(pos) || !world.getBlockState(pos).getMaterial().isSolid()) {
                ItemStack searchStack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
                if (!world.isRemote && (player.capabilities.isCreativeMode || InventoryUtilities.inventoryHasItem(player.inventory, searchStack, 1))) {
                    world.setBlockState(pos, state);
                    if (!player.capabilities.isCreativeMode)
                        InventoryUtilities.deductFromInventory(player.inventory, searchStack, 1, null);
                }
                return true;
            }
        } else if (caster.isSneaking()) {
            if (!world.isRemote && !world.isAirBlock(pos)) {
                setPlaceBlock(spell, world.getBlockState(pos));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        return false;
    }

    @Override
    public float manaCost() {
        return 5;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.earth, Affinities.ender);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.05f;
    }

}
