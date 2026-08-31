package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleApproachPoint;
import am2.common.extensions.EntityExtension;
import am2.common.items.ItemSpellBase;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class Telekinesis extends SpellComponent {
    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos blockPos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        if (!world.isRemote && caster instanceof EntityPlayer) {
            IBlockState state = world.getBlockState(blockPos);
            Block block = state.getBlock();
            if (block instanceof BlockLever || block instanceof BlockButton ||
                    block instanceof BlockDoor || block instanceof BlockTrapDoor ||
                    block instanceof BlockFenceGate) {
                block.onBlockActivated(world, blockPos, state, (EntityPlayer) caster, EnumHand.MAIN_HAND, blockFace,
                        (float) (impactX - blockPos.getX()), (float) (impactY - blockPos.getY()), (float) (impactZ - blockPos.getZ()));
            }
        }
        return doTK_Extrapolated(spell, world, impactX, impactY, impactZ, caster);
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        return doTK_Extrapolated(spell, world, target.posX, target.posY, target.posZ, caster);
    }

    private boolean doTK_Extrapolated(SpellData spell, World world, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        if (caster instanceof EntityPlayer) {
            double range = ((EntityExtension) EntityExtension.For(caster)).getTKDistance();
            RayTraceResult mop = spell.raytrace(caster, world, range, false, false);
            if (mop == null) {
                impactX = caster.posX + (Math.cos(Math.toRadians(caster.rotationYaw + 90)) * range);
                impactZ = caster.posZ + (Math.sin(Math.toRadians(caster.rotationYaw + 90)) * range);
                impactY = caster.posY + caster.getEyeHeight() + (-Math.sin(Math.toRadians(caster.rotationPitch)) * range);
            }
        }

        double distance = 16;
        int hDist = 3;
        List<Entity> entities = new ArrayList<Entity>();
        entities.addAll(world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(impactX - distance, impactY - hDist, impactZ - distance, impactX + distance, impactY + hDist, impactZ + distance)));
        entities.addAll(world.getEntitiesWithinAABB(EntityXPOrb.class, new AxisAlignedBB(impactX - distance, impactY - hDist, impactZ - distance, impactX + distance, impactY + hDist, impactZ + distance)));
        for (Entity e : entities) {
            if (e.ticksExisted < 20) {
                continue;
            }
            Vec3d movement = new Vec3d(e.posX, e.posY, e.posZ).subtract(new Vec3d(impactX, impactY, impactZ)).normalize();

            if (!world.isRemote) {
                float factor = 0.15f;
                if (movement.y > 0) movement = new Vec3d(movement.x, 0, movement.z);
                double x = -(movement.x * factor);
                double y = -(movement.y * factor);
                double z = -(movement.z * factor);

                e.addVelocity(x, y, z);
                if (Math.abs(e.motionX) > Math.abs(x * 2)) {
                    e.motionX = x * (e.motionX / e.motionX);
                }
                if (Math.abs(e.motionY) > Math.abs(y * 2)) {
                    e.motionY = y * (e.motionY / e.motionY);
                }
                if (Math.abs(e.motionZ) > Math.abs(z * 2)) {
                    e.motionZ = z * (e.motionZ / e.motionZ);
                }
            }
        }
        return true;
    }

    @Override
    public float manaCost() {
        return 6;
    }



    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {

        if (caster instanceof EntityPlayer) {
            double range = EntityExtension.For(caster).getTKDistance();
            RayTraceResult mop = ItemSpellBase.getMovingObjectPosition(caster, world, range, false, false);
            if (mop == null) {
                x = caster.posX + (Math.cos(Math.toRadians(caster.rotationYaw + 90)) * range);
                z = caster.posZ + (Math.sin(Math.toRadians(caster.rotationYaw + 90)) * range);
                y = caster.posY + caster.getEyeHeight() + (-Math.sin(Math.toRadians(caster.rotationPitch)) * range);
            }
        }


        AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "arcane", x - 0.5 + rand.nextDouble(), y - 0.5 + rand.nextDouble(), z - 0.5 + rand.nextDouble());
        if (effect != null) {
            effect.AddParticleController(new ParticleApproachPoint(effect, x, y, z, 0.025f, 0.025f, 1, false));
            effect.setRGBColorF(0.8f, 0.3f, 0.7f);
            if (colorModifier > -1) {
                effect.setRGBColorI(colorModifier);
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.arcane);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.PURPLE.getDyeDamage()),
                Blocks.STICKY_PISTON,
                Blocks.CHEST
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.001f;
    }
}
