package am2.common.entity;

import am2.api.spell.SpellData;
import am2.common.registry.AMItems;
import am2.common.utils.NBTUtils;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * EntitySpellSlice – a wide, flat spell-shape entity that flies in a straight line,
 * slicing through every entity it passes (applying the spell once per entity) and
 * despawning only when it hits solid terrain or its maximum lifetime expires.
 */
public class EntitySpellSlice extends Entity {

    // Half-width of the slice perpendicular to motion, in blocks (total width = 2× this)
    public static final float SLICE_HALF_WIDTH = 1.0f;

    private static final DataParameter<Optional<SpellData>> DW_EFFECT =
            EntityDataManager.createKey(EntitySpellSlice.class, SpellData.OPTIONAL_SPELL_DATA);
    private static final DataParameter<Integer> DW_SHOOTER =
            EntityDataManager.createKey(EntitySpellSlice.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DW_MAX_TICKS =
            EntityDataManager.createKey(EntitySpellSlice.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DW_COLOR =
            EntityDataManager.createKey(EntitySpellSlice.class, DataSerializers.VARINT);

    /** Entities already hit by this slice – each entity is only affected once. */
    private final List<Integer> hitEntityIds = new ArrayList<>();

    public EntitySpellSlice(World worldIn) {
        super(worldIn);
        // Wide (2 blocks) and very flat (0.25 blocks tall)
        setSize(2.0F, 0.25F);
        noClip = false;
    }

    @Override
    protected void entityInit() {
        this.getDataManager().register(DW_EFFECT, Optional.<SpellData>absent());
        this.getDataManager().register(DW_SHOOTER, -1);
        this.getDataManager().register(DW_MAX_TICKS, 60);
        this.getDataManager().register(DW_COLOR, 0xFFFFFF);
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    public void setSpell(SpellData spell) {
        this.getDataManager().set(DW_EFFECT, Optional.fromNullable(spell));
    }

    public SpellData getSpell() {
        return this.getDataManager().get(DW_EFFECT).orNull();
    }

    public void setShooter(EntityLivingBase shooter) {
        this.getDataManager().set(DW_SHOOTER, shooter.getEntityId());
    }

    public EntityLivingBase getShooter() {
        int id = this.getDataManager().get(DW_SHOOTER);
        if (id < 0) return null;
        try {
            Entity e = world.getEntityByID(id);
            return (e instanceof EntityLivingBase) ? (EntityLivingBase) e : null;
        } catch (Exception ex) {
            return null;
        }
    }

    /** Maximum number of ticks this slice will live before despawning. */
    public void setMaxTicks(int ticks) {
        this.getDataManager().set(DW_MAX_TICKS, ticks);
    }

    public void setColor(int color) {
        this.getDataManager().set(DW_COLOR, color);
    }

    public int getColor() {
        return this.getDataManager().get(DW_COLOR);
    }

    // -----------------------------------------------------------------------
    // Update logic
    // -----------------------------------------------------------------------

    @Override
    public void onUpdate() {
        // 1. Lifetime check
        if (ticksExisted > getDataManager().get(DW_MAX_TICKS)) {
            setDead();
            return;
        }

        // 2. Terrain collision – ray trace one step ahead in the movement direction
        Vec3d pos     = new Vec3d(posX, posY, posZ);
        Vec3d nextPos = pos.add(motionX, motionY, motionZ);

        RayTraceResult mop = world.rayTraceBlocks(pos, nextPos, false, false, false);
        if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
            if (world.getBlockState(mop.getBlockPos()).getBlock()
                    .isSideSolid(world.getBlockState(mop.getBlockPos()), world, mop.getBlockPos(), mop.sideHit)) {
                // Apply ground effect, then die
                if (!world.isRemote && getSpell() != null && getShooter() != null) {
                    SpellData copy = getSpell().copy();
                    copy.applyComponentsToGround(world, getShooter(),
                            mop.getBlockPos(), mop.sideHit,
                            mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
                }
                setDead();
                return;
            }
        }

        // 3. Entity hit detection
        //    Build a wide AABB that covers the slice's footprint plus the movement step.
        //    The growth is large on the axes perpendicular to motion so the slice is "wide",
        //    and just enough along the motion axis to cover one tick of travel.
        if (!world.isRemote && getSpell() != null && getShooter() != null) {
            double absX = Math.abs(motionX);
            double absY = Math.abs(motionY);
            double absZ = Math.abs(motionZ);

            // Expand perpendicular to motion direction
            double growX = absX > 0.01 ? absX : SLICE_HALF_WIDTH;
            double growZ = absZ > 0.01 ? absZ : SLICE_HALF_WIDTH;

            AxisAlignedBB checkBox = getEntityBoundingBox()
                    .grow(growX + SLICE_HALF_WIDTH, absY + 0.5, growZ + SLICE_HALF_WIDTH)
                    .expand(0.1, 0.1, 0.1);

            List<Entity> nearby = world.getEntitiesWithinAABBExcludingEntity(this, checkBox);
            for (Entity entity : nearby) {
                if (!(entity instanceof EntityLivingBase)) continue;
                if (entity.equals(getShooter())) continue;
                if (hitEntityIds.contains(entity.getEntityId())) continue;

                // Mark as hit before applying to avoid double-hits from execute() callbacks
                hitEntityIds.add(entity.getEntityId());

                SpellData copy = getSpell().copy();
                copy.applyComponentsToEntity(world, getShooter(), entity);
                if (getSpell().hasMoreStages()) {
                    getSpell().execute(world, getShooter(), (EntityLivingBase) entity,
                            entity.posX, entity.posY, entity.posZ, null);
                }
            }
        }

        // 4. Advance position
        setPosition(posX + motionX, posY + motionY, posZ + motionZ);
    }

    // -----------------------------------------------------------------------
    // NBT
    // -----------------------------------------------------------------------

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompound) {
        NBTTagCompound am2Tag = NBTUtils.getAM2Tag(tagCompound);
        if (am2Tag.hasKey("Effect")) {
            getDataManager().set(DW_EFFECT,
                    Optional.of(SpellData.readFromNBT(am2Tag.getCompoundTag("Effect"))));
        }
        getDataManager().set(DW_SHOOTER, am2Tag.getInteger("Shooter"));
        getDataManager().set(DW_MAX_TICKS, am2Tag.getInteger("MaxTicks"));
        getDataManager().set(DW_COLOR, am2Tag.getInteger("Color"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        NBTTagCompound am2Tag = NBTUtils.getAM2Tag(tagCompound);

        NBTTagCompound effectTag = new NBTTagCompound();
        (getDataManager().get(DW_EFFECT)
                .or(new SpellData(new ItemStack(AMItems.spell), Lists.newArrayList(),
                        UUID.randomUUID(), new NBTTagCompound())))
                .writeToNBT(effectTag);
        am2Tag.setTag("Effect", effectTag);

        am2Tag.setInteger("Shooter", getDataManager().get(DW_SHOOTER));
        am2Tag.setInteger("MaxTicks", getDataManager().get(DW_MAX_TICKS));
        am2Tag.setInteger("Color", getDataManager().get(DW_COLOR));
    }
}
