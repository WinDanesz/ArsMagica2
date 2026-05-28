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
import net.minecraft.util.text.translation.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class PlaceBlock extends SpellComponent {

    private static final String KEY_STATE = "PlaceState";

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
        if (spell.getStoredData().hasKey(KEY_STATE)) {
            return Block.getStateById(spell.getStoredData().getInteger(KEY_STATE));
        }
        return null;
    }

    private void setPlaceBlock(SpellData spell, IBlockState state) {
        spell.getStoredData().setInteger(KEY_STATE, Block.getStateId(state));

        if (!spell.getSource().hasTagCompound())
            spell.getSource().setTagCompound(new NBTTagCompound());
        //set lore entry so that the stack displays the name of the block to place
        if (!spell.getSource().getTagCompound().hasKey("Lore"))
            spell.getSource().getTagCompound().setTag("Lore", new NBTTagList());

        ItemStack blockStack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));

        NBTTagList tagList = spell.getSource().getTagCompound().getTagList("Lore", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); ++i) {
            String str = tagList.getStringTagAt(i);
            if (str.startsWith(String.format(I18n.translateToLocalFormatted("am2.tooltip.placeBlockSpell"), ""))) {
                tagList.removeTag(i);
            }
        }
        tagList.appendTag(new NBTTagString(String.format(I18n.translateToLocalFormatted("am2.tooltip.placeBlockSpell"), blockStack.getDisplayName())));

        spell.getSource().getTagCompound().setTag("Lore", tagList);
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

        IBlockState bd = getPlaceBlock(spell);

        if (bd != null && !caster.isSneaking()) {
            if (world.isAirBlock(pos) || !world.getBlockState(pos).isSideSolid(world, pos, blockFace))
                blockFace = null;
            if (blockFace != null) {
                pos = pos.add(blockFace.getDirectionVec());
            }
            if (world.isAirBlock(pos) || !world.getBlockState(pos).getMaterial().isSolid()) {
                ItemStack searchStack = new ItemStack(bd.getBlock(), 1, bd.getBlock().getMetaFromState(bd));
                if (!world.isRemote && (player.capabilities.isCreativeMode || InventoryUtilities.inventoryHasItem(player.inventory, searchStack, 1))) {
                    world.setBlockState(pos, bd);
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
