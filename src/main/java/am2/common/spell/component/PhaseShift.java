package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFadeOut;
import am2.common.blocks.tileentity.TileEntityPhaseShift;
import am2.common.registry.AMBlocks;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class PhaseShift extends SpellComponent {
    private static final int BASE_DURATION = 200; // 10 seconds

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        if (world.isRemote) return true;

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Don't phase shift air, liquids, bedrock, tile entities, or already-phased blocks
        if (block == Blocks.AIR || block == Blocks.BEDROCK)
            return false;
        if (state.getMaterial().isLiquid())
            return false;
        if (block == AMBlocks.phase_shift)
            return false;
        // Don't replace blocks that have tile entities (chests, furnaces, etc.)
        if (block.hasTileEntity(state) && !(world.getTileEntity(pos) instanceof TileEntityPhaseShift))
            return false;

        int duration = (int) spell.getModifiedValue(BASE_DURATION, SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, null);

        world.setBlockState(pos, AMBlocks.phase_shift.getDefaultState(), 3);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPhaseShift) {
            ((TileEntityPhaseShift) te).setOriginalState(state, duration);
        }

        return true;
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        return false;
    }

    @Override
    public float manaCost() {
        return 200;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DURATION);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 15; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle", x + 0.5, y + 0.5, z + 0.5);
            if (particle != null) {
                particle.addRandomOffset(0.6, 0.6, 0.6);
                particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05f));
                particle.setMaxAge(30);
                particle.setParticleScale(0.15f);
                if (colorModifier > -1) {
                    particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                } else {
                    particle.setRGBColorF(0.3f, 0.1f, 0.6f); // purple/void
                }
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.ender);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.02f;
    }
}
