package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.extensions.ISpellCaster;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.common.extensions.EntityExtension;
import am2.common.items.ItemCrystalPhylactery;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.utils.EntityUtils;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class Summon extends SpellComponent {

    private static final ResourceLocation DEFAULT_SUMMON = new ResourceLocation("minecraft", "skeleton");

    public EntityLiving summonCreature(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z) {
        ResourceLocation key = getSummonType(spell);
        Entity spawned = EntityList.createEntityByIDFromName(key, world);
        if (!(spawned instanceof EntityLiving)) {
            ArsMagica.LOGGER.error("Summon: entity '{}' is not an EntityLiving", key);
            return null;
        }
        EntityLiving entity = (EntityLiving) spawned;

        if (entity instanceof EntitySkeleton) {
            entity.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.BOW));
        } else if (entity instanceof EntityHorse && caster instanceof EntityPlayer) {
            ((EntityHorse) entity).setTamedBy(((EntityPlayer) caster));
        }
        BlockPos pos = findNearbyFloorSpace(world, new BlockPos(x, y, z), 3, 3, false);
        if (pos == null) {
            pos = new BlockPos(x + 0.5f, y + 0.5f, z + 0.5f);
        }
        entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        world.spawnEntity(entity);
        if (caster instanceof EntityPlayer) {
            EntityUtils.makeSummon_PlayerFaction((EntityCreature) entity, (EntityPlayer) caster, false);
        } else {
            EntityUtils.makeSummon_MonsterFaction((EntityCreature) entity, false);
        }
        EntityUtils.setOwner(entity, caster);

        String creatureName = EntityList.getEntityString(entity);
        if (creatureName == null || creatureName.isEmpty()) creatureName = entity.getClass().getSimpleName();
        entity.setCustomNameTag(caster.getName() + "'s " + creatureName);

        int duration = (int) spell.getModifiedValue(4800, SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target);

        EntityUtils.setSummonDuration(entity, duration);

        spell.applyComponentsToEntity(world, caster, entity);

        return entity;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DURATION);
    }

    @Override
    public Object[] getRecipe() {
        //Chimerite, purified vinteum, blue orchid, monster focus, any filled crystal phylactery, 1500 dark power
        return new Object[]{
                new ItemStack(AMItems.chimerite),
                new ItemStack(AMItems.purified_vinteum_dust),
                AMBlocks.cerublossom,
                AMItems.mob_focus,
                new ItemStack(AMItems.crystal_phylactery, 1, ItemCrystalPhylactery.META_FULL),
                "E:" + PowerTypes.DARK.ID(), 1500
        };
    }

    public void setSummonType(NBTTagCompound stack, ItemStack phylacteryStack) {
        if (phylacteryStack.getItemDamage() == ItemCrystalPhylactery.META_FULL && phylacteryStack.getItem() instanceof ItemCrystalPhylactery) {
            setSummonType(stack, ((ItemCrystalPhylactery) AMItems.crystal_phylactery).getSpawnClass(phylacteryStack));
        }
    }


    public ResourceLocation getSummonType(SpellData spell) {
        String s = spell.getStoredData().getString(ItemCrystalPhylactery.TAG_SUMMON_TYPE);
        if (s == null || s.isEmpty())
            return DEFAULT_SUMMON;
        ResourceLocation key = new ResourceLocation(s);
        return EntityList.isRegistered(key) ? key : DEFAULT_SUMMON;
    }

    public ResourceLocation getSummonType(ISpellCaster spell) {
        String s = spell.getCommonStoredData().getString(ItemCrystalPhylactery.TAG_SUMMON_TYPE);
        if (s == null || s.isEmpty())
            return DEFAULT_SUMMON;
        ResourceLocation key = new ResourceLocation(s);
        return EntityList.isRegistered(key) ? key : DEFAULT_SUMMON;
    }


    public void setSummonType(NBTTagCompound stack, String s) {
        if (s == null || s.isEmpty()) return;
        ResourceLocation key = new ResourceLocation(s);
        if (!EntityList.isRegistered(key)) return;
        stack.setString(ItemCrystalPhylactery.TAG_SUMMON_TYPE, key.toString());
    }

    public void setSummonType(NBTTagCompound stack, Class<? extends Entity> clazz) {
        if (clazz == null) return;
        ResourceLocation key = EntityList.getKey(clazz);
        if (key == null) return;
        stack.setString(ItemCrystalPhylactery.TAG_SUMMON_TYPE, key.toString());
    }

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos blockPos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        if (!world.isRemote) {
            if (EntityExtension.For(caster).getCanHaveMoreSummons()) {
                if (summonCreature(spell, caster, caster, world, impactX, impactY, impactZ) == null) {
                    return false;
                }
            } else {
                if (caster instanceof EntityPlayer) {
                    ((EntityPlayer) caster).sendStatusMessage(new TextComponentTranslation("am2.tooltip.noMoreSummons"), false);
                }
            }
        }

        return true;
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {

        if (target instanceof EntityLivingBase && EntityUtils.isSummon((EntityLivingBase) target))
            return false;

        if (!world.isRemote) {
            if (EntityExtension.For(caster).getCanHaveMoreSummons()) {
                if (summonCreature(spell, caster, caster, world, target.posX, target.posY, target.posZ) == null) {
                    return false;
                }
            } else {
                if (caster instanceof EntityPlayer) {
                    ((EntityPlayer) caster).sendStatusMessage(new TextComponentTranslation("am2.tooltip.noMoreSummons"), false);
                }
            }
        }

        return true;
    }

    @Override
    public float manaCost() {
        return 400;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {

    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.ender, Affinities.life);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.01f;
    }

    @Override
    public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {
        for (Object obj : recipe) {
            if (obj instanceof ItemStack) {
                ItemStack is = (ItemStack) obj;
                if (is.getItem().equals(AMItems.crystal_phylactery))
                    setSummonType(tag, is);
            }
        }
    }

    @Nullable
    public static Integer getNearestFloor(World world, BlockPos pos, int range){
        return getNearestSurface(world, pos, EnumFacing.UP, range, true);
    }

    @Nullable
    public static Integer getNearestSurface(World world, BlockPos pos, EnumFacing direction, int range,
                                            boolean doubleSided){

        // This is a neat trick that allows a default 'not found' return value for integers where all possible integer
        // values could, in theory, be returned. The alternative is to use a double and have NaN as the default, but
        // that would introduce extra casting, and since NaN can be calculated with, it could produce strange results
        // when unaccounted for. Using an Integer means it'll immediately throw an NPE instead.
        Integer surface = null;
        int currentBest = Integer.MAX_VALUE;

        for(int i = doubleSided ? -range : 0; i <= range && i < currentBest; i++){ // Now short-circuits for efficiency

            BlockPos testPos = pos.offset(direction, i);
            IBlockState state = world.getBlockState(testPos);

            if(state.getCollisionBoundingBox(world, testPos) != Block.NULL_AABB){
                // Because the loop now short-circuits, this must be closer than the previous surface found
                int coord;
                switch (direction.getAxis()) {
                    case X: coord = testPos.getX(); break;
                    case Y: coord = testPos.getY(); break;
                    case Z: coord = testPos.getZ(); break;
                    default: throw new Error(); // Should never happen
                }
                surface = direction.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? coord + 1 : coord;
                currentBest = Math.abs(i);
            }
        }

        return surface;
    }


    @Nullable
    public static BlockPos findNearbyFloorSpace(World world, BlockPos origin, int horizontalRange, int verticalRange, boolean lineOfSight){

        List<BlockPos> possibleLocations = new ArrayList<>();

        final Vec3d centre = new Vec3d(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5);

        for(int x = -horizontalRange; x <= horizontalRange; x++){
            for(int z = -horizontalRange; z <= horizontalRange; z++){

                Integer y = getNearestFloor(world, origin.add(x, 0, z), verticalRange);

                if(y != null){

                    BlockPos location = new BlockPos(origin.getX() + x, y, origin.getZ() + z);

                    if(lineOfSight){
                        // Since we're only using finding collidable surfaces, it doesn't make much sense to include
                        // non-collidable blocks here!
                        RayTraceResult rayTrace = world.rayTraceBlocks(centre, new Vec3d(location.getX() + 0.5, location.getY() + 0.5, location.getZ() + 0.5),
                                false, true, false);
                        if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK) continue;
                    }

                    possibleLocations.add(location);
                }
            }
        }

        if(possibleLocations.isEmpty()){
            return null;
        }else{
            return possibleLocations.get(world.rand.nextInt(possibleLocations.size()));
        }
    }
}
