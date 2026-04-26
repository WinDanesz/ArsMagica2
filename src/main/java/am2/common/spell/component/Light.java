package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.blocks.IMultiblock;
import am2.api.blocks.IMultiblockGroup;
import am2.api.blocks.Multiblock;
import am2.api.blocks.MultiblockGroup;
import am2.api.power.IPowerNode;
import am2.api.rituals.IRitualInteraction;
import am2.api.rituals.RitualShapeHelper;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.common.blocks.BlockMageLight;
import am2.common.power.PowerNodeRegistry;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.AMPotions;
import am2.common.registry.Affinities;
import am2.common.utils.SpellUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class Light extends SpellComponent implements IRitualInteraction {

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        am2.common.LogHelper.info("Light applyEffectBlock called at %s, block is %s, obelisk is %s",
                pos, world.getBlockState(pos).getBlock().getRegistryName(),
                AMBlocks.obelisk != null ? AMBlocks.obelisk.getRegistryName() : "NULL");
        if (world.getBlockState(pos).getBlock().equals(AMBlocks.obelisk)) {
            am2.common.LogHelper.info("Light spell hit obelisk at %s, checking ritual...", pos);
            if (RitualShapeHelper.instance.matchesRitual(this, world, pos)) {
                am2.common.LogHelper.info("Ritual matched! Transforming obelisk to celestial prism");
                if (!world.isRemote) {
                    RitualShapeHelper.instance.consumeReagents(this, world, pos);
                    RitualShapeHelper.instance.consumeShape(this, world, pos);
                    world.setBlockState(pos, AMBlocks.celestial_prism.getDefaultState());
                    PowerNodeRegistry.For(world).registerPowerNode((IPowerNode<?>) world.getTileEntity(pos));
                } else {

                }

                return true;
            }
        }

        if (world.getBlockState(pos).getBlock() == Blocks.AIR) blockFace = null;
        if (blockFace != null) {
            pos = pos.offset(blockFace);
        }

        if (world.getBlockState(pos).getBlock() != Blocks.AIR)
            return false;

        if (!world.isRemote) {
            world.setBlockState(pos, AMBlocks.block_mage_light.getDefaultState().withProperty(BlockMageLight.COLOR, EnumDyeColor.byMetadata(getColor(spell))));
        }

        return true;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.COLOR);
    }

    private int getColor(SpellData spell) {
        int dye_color_num = 15;
        if (spell.isModifierPresent(SpellModifiers.COLOR)) {
            dye_color_num = spell.getStoredData().getInteger("Color");
        }
        return 15 - dye_color_num;
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (target instanceof EntityLivingBase) {
            int duration = (int) spell.getModifiedValue(ArsMagica.config.getDefaultBuffDuration(), SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target);
            duration = SpellUtils.modifyDurationBasedOnArmor(caster, duration);
            if (!world.isRemote)
                ((EntityLivingBase) target).addPotionEffect(new PotionEffect(AMPotions.illumination, duration, spell.getModifierCount(SpellModifiers.BUFF_POWER)));
            return true;
        }
        return false;
    }

    @Override
    public float manaCost() {
        return 50;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 5; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", x, y, z);
            if (particle != null) {
                particle.addRandomOffset(1, 0.5, 1);
                particle.addVelocity(rand.nextDouble() * 0.2 - 0.1, rand.nextDouble() * 0.2, rand.nextDouble() * 0.2 - 0.1);
                particle.setAffectedByGravity();
                particle.setDontRequireControllers();
                particle.setMaxAge(5);
                particle.setParticleScale(0.1f);
                particle.setRGBColorF(0.6f, 0.2f, 0.8f);
                if (colorModifier > -1) {
                    particle.setRGBColorI(colorModifier);
                }
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.none);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.WHITE.getDyeDamage()),
                AMBlocks.cerublossom,
                Blocks.TORCH,
                AMBlocks.vinteum_torch
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.01f;
    }

    @Override
    public IMultiblock getRitualShape() {
        Multiblock newDef = new Multiblock("celestialPurification");
        newDef.groups = Lists.newArrayList(RitualShapeHelper.instance.purification.getMultiblockGroups());
        IMultiblockGroup obelisk = new MultiblockGroup("obelisk", Lists.newArrayList(AMBlocks.obelisk.getDefaultState()), true);
        obelisk.addBlock(new BlockPos(0, 0, 0));
        newDef.addGroup(obelisk);
        return newDef;
    }

    @Override
    public ItemStack[] getRitualReagents() {
        return new ItemStack[]{
                new ItemStack(AMItems.moonstone),
                new ItemStack(AMItems.mana_focus)
        };
    }
    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getResult() {
        return new ItemStack(AMBlocks.celestial_prism);
    }
}
