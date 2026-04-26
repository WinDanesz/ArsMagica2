package am2.common.compat.electroblob.spells;

import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import electroblob.wizardry.tileentity.TileEntityTimer;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Generic spell component that places any block at the impact position (block
 * target) or at an entity's feet (entity target), then removes it after a
 * configurable duration.
 *
 * <p>If the placed block has a {@link TileEntityTimer} tile entity (as EBWiz's
 * vanishing blocks do), the component automatically forwards the
 * Duration-modifier-scaled lifetime to it so the block self-destructs on time.
 * For blocks without a timer tile entity the block simply persists permanently,
 * so prefer pairing this with blocks that clean themselves up.
 *
 * <p>Only safe to register when Electroblob's Wizardry is present (this class
 * references EBWiz's {@link TileEntityTimer}).
 */
public class PlaceTemporaryBlock extends SpellComponent {

    private final Block block;
    private final int defaultLifetime;
    private final float mana;
    private final Supplier<Set<Affinity>> affinities;
    private final float affinityShift;
    private final Object[] recipe;

    /**
     * @param block           Block to place.
     * @param defaultLifetime Default block lifetime in ticks (before Duration modifiers).
     * @param mana            Base mana cost.
     * @param affinities      Supplier for the affinities this component belongs to (evaluated lazily at cast time).
     * @param affinityShift   Affinity depth shift per cast.
     * @param recipe          Crafting recipe ingredients shown in the Arcane Compendium.
     */
    public PlaceTemporaryBlock(Block block, int defaultLifetime, float mana,
                               Supplier<Set<Affinity>> affinities, float affinityShift, Object... recipe) {
        this.block = block;
        this.defaultLifetime = defaultLifetime;
        this.mana = mana;
        this.affinities = affinities;
        this.affinityShift = affinityShift;
        this.recipe = recipe;
    }

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos pos, EnumFacing blockFace,
                                    double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        if (world.isRemote) return false;

        BlockPos placePos = blockFace != null ? pos.offset(blockFace) : pos;
        if (!world.isAirBlock(placePos)) return false;

        world.setBlockState(placePos, block.getDefaultState());
        applyLifetime(spell, world, placePos, caster, null);
        return true;
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (world.isRemote) return false;

        // Use explicit floor of posX/Y/Z rather than Entity.getPosition() which adds +0.5 to Y.
        // When an entity is slightly above the floor (e.g. on a slope or mid-physics-tick),
        // getPosition() can round up to the block above, leaving a 1-block gap under the cobweb.
        BlockPos pos = new BlockPos(target.posX, target.posY, target.posZ);
        if (!world.isAirBlock(pos)) return false;

        world.setBlockState(pos, block.getDefaultState());
        applyLifetime(spell, world, pos, caster, target);
        return true;
    }

    /**
     * If the freshly placed block exposes a {@link TileEntityTimer}, writes the
     * duration-scaled lifetime into it so the block self-destructs on time.
     */
    private void applyLifetime(SpellData spell, World world, BlockPos pos,
                                EntityLivingBase caster, Entity target) {
        if (world.getTileEntity(pos) instanceof TileEntityTimer) {
            int duration = (int) spell.getModifiedValue(defaultLifetime, SpellModifiers.DURATION,
                    Operation.MULTIPLY, world, caster, target);
            ((TileEntityTimer) world.getTileEntity(pos)).setLifetime(duration);
        }
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DURATION);
    }

    @Override
    public float manaCost() {
        return mana;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster,
                               Entity target, Random rand, int colorModifier) {
        // The placed block is visually self-evident; no extra particles needed.
    }

    @Override
    public Set<Affinity> getAffinity() {
        return affinities.get();
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return affinityShift;
    }

    @Override
    public Object[] getRecipe() {
        return recipe;
    }

    @Override
    public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {
        // No custom NBT required
    }
}
