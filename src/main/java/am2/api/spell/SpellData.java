package am2.api.spell;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.common.blocks.BlockSpellSealedDoor;
import am2.common.registry.AMBlocks;
import am2.common.registry.Affinities;
import am2.common.spell.SpellCastResult;
import am2.common.spell.shape.MissingShape;
import am2.common.utils.AffinityShiftUtils;
import am2.common.utils.EntityUtils;
import am2.common.utils.NBTUtils;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;

public class SpellData {

    public static final DataSerializer<Optional<SpellData>> OPTIONAL_SPELL_DATA = new DataSerializer<Optional<SpellData>>() {
        @Override
        public void write(PacketBuffer buf, Optional<SpellData> value) {
            buf.writeCompoundTag(value.isPresent() ? value.orNull().writeToNBT(new NBTTagCompound()) : null);
        }

        @Override
        public Optional<SpellData> read(PacketBuffer buf) {
            try {
                NBTTagCompound tag = buf.readCompoundTag();
                return Optional.fromNullable(tag != null ? readFromNBT(tag) : null);
            } catch (IOException e) {
                am2.ArsMagica.LOGGER.error("Exception caught: ", e);
            }
            return Optional.absent();
        }

        @Override
        public DataParameter<Optional<SpellData>> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        @Override
        public Optional<SpellData> copyValue(Optional<SpellData> value) {
            if (value.isPresent()) {
                SpellData data = new SpellData(value.get().source, Lists.newArrayList(value.get().stages), new UUID(value.get().uuid.getMostSignificantBits(), value.get().uuid.getLeastSignificantBits()), value.get().storedData);
                data.exec = value.get().exec;
                return Optional.of(data);
            }
            return Optional.absent();
        }
    };

    static {
        DataSerializers.registerSerializer(OPTIONAL_SPELL_DATA);
    }

    private List<List<SpellPart>> stages;
    private int exec;
    private NBTTagCompound storedData;
    private UUID uuid;
    private ItemStack source;

    public SpellData(ItemStack source, List<List<SpellPart>> stages, UUID uuid, NBTTagCompound storedData) {
        this.source = source;
        this.stages = Lists.newArrayList();
        for (List<SpellPart> stage : stages) {
            if (stage != null && !stage.isEmpty())
                this.stages.add(stage);
        }
        this.exec = 0;
        this.uuid = uuid;
        this.storedData = storedData.copy();
    }

    public double getModifiedValue(double defaultValue, SpellModifiers mod, BiFunction<Double, Double, Double> operation, World world, EntityLivingBase caster, Entity target) {
        double outValue = defaultValue;
        // Create a snapshot to avoid ConcurrentModificationException
        for (List<SpellPart> parts : Lists.newArrayList(stages)) {
            for (SpellPart part : parts) {
                if (part instanceof SpellModifier) {
                    if (((SpellModifier) part).getAspectsModified().contains(mod)) {
                        outValue = operation.apply(outValue, (double) ((SpellModifier) part).getModifier(mod, caster, target, world, storedData));
                    }
                }
            }
        }
        // Apply shape damage multiplier if this is a DAMAGE modifier
        if (mod == SpellModifiers.DAMAGE) {
            int shapeStageIndex = exec > 0 ? exec - 1 : 0;
            if (shapeStageIndex >= 0 && shapeStageIndex < stages.size()) {
                for (SpellPart part : stages.get(shapeStageIndex)) {
                    if (part instanceof SpellShape) {
                        outValue *= ((SpellShape) part).damageMultiplier();
                        break;
                    }
                }
            }
        }
        return outValue;
    }

    public double getModifiedValue(SpellModifiers mod, BiFunction<Double, Double, Double> operation, World world, EntityLivingBase caster, Entity target) {
        return getModifiedValue(mod.defaultValue, mod, operation, world, caster, target);
    }

    public int getColor(World world, EntityLivingBase caster, Entity target) {
        return (int) this.getModifiedValue(-1, SpellModifiers.COLOR, Operation.REPLACE, world, caster, target);
    }

    public boolean isModifierPresent(SpellModifiers mod) {
        // Create a snapshot to avoid ConcurrentModificationException
        for (List<SpellPart> parts : Lists.newArrayList(stages)) {
            for (SpellPart part : parts) {
                if (part instanceof SpellModifier) {
                    if (((SpellModifier) part).getAspectsModified().contains(mod))
                        return true;
                }
            }
        }
        return false;
    }

    public int getModifierCount(SpellModifiers mod) {
        int count = 0;
        // Create a snapshot to avoid ConcurrentModificationException
        for (List<SpellPart> parts : Lists.newArrayList(stages)) {
            for (SpellPart part : parts) {
                if (part instanceof SpellModifier) {
                    if (((SpellModifier) part).getAspectsModified().contains(mod))
                        count++;
                }
            }
        }
        return count;
    }

    public SpellCastResult execute(World world, EntityLivingBase caster, EntityLivingBase target, double x, double y, double z, @Nullable EnumFacing side) {
        if (exec < 0 || exec >= stages.size())
            return SpellCastResult.EFFECT_FAILED;
        List<SpellPart> parts = this.stages.get(exec);
        parts.sort(Comparator.naturalOrder());
        SpellShape shape = null;
        for (SpellPart part : parts) {
            if (part instanceof SpellShape) {
                if (shape != null)
                    return SpellCastResult.MALFORMED_SPELL_STACK;
                shape = (SpellShape) part;
            }
        }
        if (shape == null || shape instanceof MissingShape)
            return SpellCastResult.MALFORMED_SPELL_STACK;
        exec++;
        SpellCastResult result = shape.beginStackStage(this, caster, target, world, x, y, z, side, true, 0);
        SoundEvent soundEvent = shape.getSoundForAffinity(this.getMainShift(), this, world);
        if (soundEvent != null)
            world.playSound(x, y, z, soundEvent, SoundCategory.AMBIENT, 0.5F, 0, false);
        return result;
    }

    public SpellCastResult execute(World world, EntityLivingBase caster) {
        return execute(world, caster, null, caster.posX, caster.posY, caster.posZ, null);
    }

    public SpellCastResult applyComponentsToEntity(World world, EntityLivingBase caster, Entity target) {
        // For single-stage spells: Projectile+Components in stage 0, exec=1 after execute(), look at stage 0 (exec-1)
        // For multi-stage spells: Projectile in stage 0, Components in stage 1, exec=1 after execute(), look at stage 1 (exec)
        // Try exec first (multi-stage), fall back to exec-1 (single-stage)
        int componentStageIndex = hasMoreStages() ? exec : (exec > 0 ? exec - 1 : 0);
        if (componentStageIndex < 0 || componentStageIndex >= stages.size())
            return SpellCastResult.EFFECT_FAILED;
        List<SpellPart> parts = Lists.newArrayList(this.stages.get(componentStageIndex));
        parts.sort(Comparator.naturalOrder());
        boolean isPlayer = caster instanceof EntityPlayer;
        boolean flag = false;
        boolean success = false;
        SpellShape shape = null;
        for (SpellPart part : Lists.newArrayList(this.stages.get(exec > 0 ? exec - 1 : exec))) {
            if (part instanceof SpellShape) {
                if (shape != null)
                    return SpellCastResult.MALFORMED_SPELL_STACK;
                shape = (SpellShape) part;
            }
        }
        for (SpellPart part : parts) {
            if (part instanceof SpellComponent) {
                SpellComponent component = (SpellComponent) part;
                flag = true;
                if (component.applyEffectEntity(this, world, caster, target)) {
                    if (isPlayer && !world.isRemote) {
                        if (component.getAffinity() != null) {
                            AffinityShiftUtils.doAffinityShift(caster, component, shape);
                        }
                    }
                    success = true;
                    if (world.isRemote) {
                        component.spawnParticles(world, target.posX, target.posY + target.getEyeHeight(), target.posZ, caster, target, world.rand, getColor(world, caster, target));
                    }
                }
            }
        }
        return flag ? success ? SpellCastResult.SUCCESS : SpellCastResult.EFFECT_FAILED : SpellCastResult.SUCCESS;
    }

    public SpellCastResult applyComponentsToGround(World world, EntityLivingBase caster, BlockPos pos, EnumFacing facing, double x, double y, double z) {
        // For single-stage spells: Projectile+Components in stage 0, exec=1 after execute(), look at stage 0 (exec-1)
        // For multi-stage spells: Projectile in stage 0, Components in stage 1, exec=1 after execute(), look at stage 1 (exec)
        // Try exec first (multi-stage), fall back to exec-1 (single-stage)
        int componentStageIndex = hasMoreStages() ? exec : (exec > 0 ? exec - 1 : 0);
        if (componentStageIndex < 0 || componentStageIndex >= stages.size())
            return SpellCastResult.EFFECT_FAILED;
        List<SpellPart> parts = Lists.newArrayList(this.stages.get(componentStageIndex));
        parts.sort(Comparator.naturalOrder());
        boolean isPlayer = caster instanceof EntityPlayer;
        boolean flag = false;
        boolean success = false;
        SpellShape shape = null;
        for (SpellPart part : Lists.newArrayList(this.stages.get(exec > 0 ? exec - 1 : exec))) {
            if (part instanceof SpellShape) {
                if (shape != null)
                    return SpellCastResult.MALFORMED_SPELL_STACK;
                shape = (SpellShape) part;
            }
        }
        for (SpellPart part : parts) {
            if (part instanceof SpellComponent) {
                SpellComponent component = (SpellComponent) part;
                flag = true;
                // Special logic: if the target block is a spell-sealed door, register the
                // component as part of the "attempted key" and skip the normal block effect.
                // Mirrors the original 1.7.10 SpellHelper.applyStageToGround() behaviour.
                if (AMBlocks.spell_sealed_door instanceof BlockSpellSealedDoor) {
                    BlockSpellSealedDoor sealedDoor = (BlockSpellSealedDoor) AMBlocks.spell_sealed_door;
                    if (sealedDoor.applyComponentToDoor(world, component, pos)) {
                        success = true;
                        continue;
                    }
                }
                if (component.applyEffectBlock(this, world, pos, facing, x, y, z, caster)) {
                    if (isPlayer && !world.isRemote) {
                        if (component.getAffinity() != null) {
                            AffinityShiftUtils.doAffinityShift(caster, component, shape);
                        }
                    }
                    success = true;
                    if (world.isRemote) {
                        component.spawnParticles(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, caster, null, world.rand, getColor(world, caster, null));
                    }
                }
            }
        }
        return flag ? success ? SpellCastResult.SUCCESS : SpellCastResult.EFFECT_FAILED : SpellCastResult.SUCCESS;
    }

    public RayTraceResult raytrace(EntityLivingBase caster, World world, double range, boolean includeEntities, boolean targetWater) {
        RayTraceResult entityPos = null;
        if (includeEntities) {
            Entity pointedEntity = EntityUtils.getPointedEntity(world, caster, range, 1.0f, false, targetWater);
            if (pointedEntity != null) {
                entityPos = new RayTraceResult(pointedEntity);
            }
        }

        float factor = 1.0F;
        float interpPitch = caster.prevRotationPitch + (caster.rotationPitch - caster.prevRotationPitch) * factor;
        float interpYaw = caster.prevRotationYaw + (caster.rotationYaw - caster.prevRotationYaw) * factor;
        double interpPosX = caster.prevPosX + (caster.posX - caster.prevPosX) * factor;
        double interpPosY = caster.prevPosY + (caster.posY - caster.prevPosY) * factor + caster.getEyeHeight();
        double interpPosZ = caster.prevPosZ + (caster.posZ - caster.prevPosZ) * factor;
        Vec3d vec3 = new Vec3d(interpPosX, interpPosY, interpPosZ);
        float offsetYawCos = MathHelper.cos(-interpYaw * 0.017453292F - (float) Math.PI);
        float offsetYawSin = MathHelper.sin(-interpYaw * 0.017453292F - (float) Math.PI);
        float offsetPitchCos = -MathHelper.cos(-interpPitch * 0.017453292F);
        float offsetPitchSin = MathHelper.sin(-interpPitch * 0.017453292F);
        float finalXOffset = offsetYawSin * offsetPitchCos;
        float finalZOffset = offsetYawCos * offsetPitchCos;
        Vec3d targetVector = vec3.add(finalXOffset * range, offsetPitchSin * range, finalZOffset * range);
        RayTraceResult mop = world.rayTraceBlocks(vec3, targetVector, targetWater, !targetWater, false);

        if (entityPos != null && mop != null) {
            if (mop.hitVec.distanceTo(new RayTraceResult(caster).hitVec) < entityPos.hitVec.distanceTo(new RayTraceResult(caster).hitVec)) {
                return mop;
            } else {
                return entityPos;
            }
        }

        return entityPos != null ? entityPos : mop;
    }


    public Affinity getMainShift() {
        Affinity aff = Affinities.none;
        float maxDepth = 0F;
        HashMap<Affinity, Float> customDepthMap = new HashMap<>();
        for (List<SpellPart> parts : stages) {
            for (SpellPart part : parts) {
                if (part instanceof SpellComponent) {
                    SpellComponent component = (SpellComponent) part;
                    for (Affinity aff1 : component.getAffinity()) {
                        if (customDepthMap.get(aff1) != null) {
                            customDepthMap.put(aff1, customDepthMap.get(aff1) + component.getAffinityShift(aff1));
                        } else {
                            customDepthMap.put(aff1, component.getAffinityShift(aff1));
                        }
                    }
                }
            }
        }
        for (Entry<Affinity, Float> entry : customDepthMap.entrySet()) {
            if (entry.getValue() > maxDepth) {
                maxDepth = entry.getValue();
                aff = entry.getKey();
            }
        }
        return aff;
    }

    public List<List<SpellPart>> getStages() {
        return ImmutableList.copyOf(stages);
    }

    public UUID getUUID() {
        return uuid;
    }

    public ItemStack getSource() {
        return source;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagList spellCommon = new NBTTagList();
        for (int i = 0; i < stages.size(); i++) {
            NBTTagCompound tmp = new NBTTagCompound();
            tmp.setInteger("ID", i);
            NBTTagList stageTag = new NBTTagList();
            List<SpellPart> parts = stages.get(i);
            if (parts != null) {
                for (SpellPart p : parts) {
                    stageTag.appendTag(new NBTTagString(p.getRegistryName().toString()));
                }
            }
            tmp.setTag("Parts", stageTag);
            spellCommon.appendTag(tmp);
        }
        tag.setTag("Stages", spellCommon);
        tag.setTag("StoredData", storedData);
        tag.setLong("UUIDMost", uuid.getMostSignificantBits());
        tag.setLong("UUIDLeast", uuid.getLeastSignificantBits());
        tag.setInteger("ExecutionStage", exec);
        tag.setTag("Stack", source.writeToNBT(new NBTTagCompound()));
        return tag;
    }

    public static SpellData readFromNBT(NBTTagCompound tag) {
        NBTTagList spellCommon = tag.getTagList("Stages", Constants.NBT.TAG_COMPOUND);
        ArrayList<List<SpellPart>> stages = new ArrayList<>(spellCommon.tagCount());
        for (int i = 0; i < spellCommon.tagCount(); i++) {
            NBTTagCompound tmp = spellCommon.getCompoundTagAt(i);
            int id = tmp.getInteger("ID");
            NBTTagList parts = tmp.getTagList("Parts", Constants.NBT.TAG_STRING);
            ArrayList<SpellPart> pts = new ArrayList<>();
            for (int j = 0; j < parts.tagCount(); j++) {
                SpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(parts.getStringTagAt(j)));
                if (part != null) {
                    pts.add(part);
                }
            }
            NBTUtils.ensureSize(stages, id + 1);
            stages.set(id, pts);
        }
        NBTTagCompound storedData = tag.getCompoundTag("StoredData");
        SpellData data = new SpellData(new ItemStack(tag.getCompoundTag("Stack")), stages, new UUID(tag.getLong("UUIDMost"), tag.getLong("UUIDLeast")), storedData);
        data.exec = tag.getInteger("ExecutionStage");
        return data;
    }

    public SpellData pop() {
        SpellData data = new SpellData(source, stages, new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()), storedData);
        data.exec = exec + 1;
        return data;
    }

    /**
     * Copies this SpellData instance.
     */
    public SpellData copy() {
        SpellData data = new SpellData(source, Lists.newArrayList(stages), new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()), storedData);
        data.exec = exec;
        return data;
    }

    public boolean hasMoreStages() {
        return exec < stages.size();
    }

    /**
     * Advances the execution pointer by one stage and returns {@code this} for chaining.
     * Used by bound items to skip the Binding shape stage (stage 0) that created the item,
     * so execution starts from the actual effect stages (e.g. Touch + IceStatue).
     */
    public SpellData skipFirstStage() {
        if (hasMoreStages()) exec++;
        return this;
    }

    public int getExecutionStage() {
        return exec;
    }

    public int getStageCount() {
        return stages.size();
    }

    public NBTTagCompound getStoredData() {
        return storedData;
    }

    public void setStoredData(NBTTagCompound storedData) {
        this.storedData = storedData;
    }

    public boolean isChanneled() {
        if (this.stages.isEmpty())
            return false;
        for (SpellPart part : this.stages.get(0)) {
            if (part instanceof SpellShape)
                return ((SpellShape) part).isChanneled();
        }
        return false;
    }
}
