package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.common.extensions.EntityExtension;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class Dig extends SpellComponent {

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(Items.IRON_SHOVEL),
                new ItemStack(Items.IRON_AXE),
                new ItemStack(Items.IRON_PICKAXE)
        };
    }

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos blockPos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        if (!(caster instanceof EntityPlayer))
            return false;
        if (world.isRemote)
            return true;
        if (spell.isModifierPresent(SpellModifiers.SILKTOUCH_LEVEL)) {
            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, spell.getSource()) <= 0) {
                spell.getSource().addEnchantment(Enchantments.SILK_TOUCH, 1);
            }
        } else if (spell.isModifierPresent(SpellModifiers.FORTUNE_LEVEL)) {

            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, spell.getSource()) <= 0) {
                spell.getSource().addEnchantment(Enchantments.FORTUNE, spell.getModifierCount(SpellModifiers.FORTUNE_LEVEL));
            }
        }

        TileEntity te = world.getTileEntity(blockPos);
        if (te != null && !ArsMagica.config.getDigBreaksTileEntities())
            return false;

        IBlockState state = world.getBlockState(blockPos);
        float hardness = state.getBlockHardness(world, blockPos);
        if (hardness != -1 && state.getBlock().getHarvestLevel(state) <= spell.getModifiedValue(2, SpellModifiers.MINING_POWER, Operation.ADD, world, caster, null)) {
            // Respect claim mods by firing BlockEvent.BreakEvent via ForgeHooks
            if (caster instanceof EntityPlayerMP) {
                int xp = ForgeHooks.onBlockBreakEvent(world, ((EntityPlayerMP) caster).interactionManager.getGameType(), (EntityPlayerMP) caster, blockPos);
                if (xp == -1) return false;
            }
            state.getBlock().harvestBlock(world, (EntityPlayer) caster, blockPos, state, null, spell.getSource());
            world.destroyBlock(blockPos, false);
            EntityExtension.For(caster).deductMana(hardness * 1.28f);
        }
        return true;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.FORTUNE_LEVEL, SpellModifiers.MINING_POWER);
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world,
                                     EntityLivingBase caster, Entity target) {
        return false;
    }

    @Override
    public float manaCost() {
        return 10;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z,
                               EntityLivingBase caster, Entity target, Random rand,
                               int colorModifier) {
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.earth);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.001F;
    }
}
