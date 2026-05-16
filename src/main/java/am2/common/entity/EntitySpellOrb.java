package am2.common.entity;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellData;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
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
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class EntitySpellOrb extends Entity {

    private static final DataParameter<Optional<SpellData>> DW_EFFECT = EntityDataManager.createKey(EntitySpellOrb.class, SpellData.OPTIONAL_SPELL_DATA);
    private static final DataParameter<String> DW_ICON_NAME = EntityDataManager.createKey(EntitySpellOrb.class, DataSerializers.STRING);
    private static final DataParameter<Integer> DW_COLOR = EntityDataManager.createKey(EntitySpellOrb.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DW_SHOOTER = EntityDataManager.createKey(EntitySpellOrb.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DW_ORB_INDEX = EntityDataManager.createKey(EntitySpellOrb.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DW_TOTAL_ORBS = EntityDataManager.createKey(EntitySpellOrb.class, DataSerializers.VARINT);

    // Color constants for different affinities
    private static final int COLOR_ENDER = 0x550055;
    private static final int COLOR_ICE = 0x2299FF;
    private static final int COLOR_LIFE = 0x22FF44;

    // Orbital mechanics constants
    private static final int MAX_LIFETIME_TICKS = 600; // 30 seconds
    private static final int REDISTRIBUTION_INTERVAL = 20; // Re-check spacing every second

    private float orbitAngle = 0.0F;
    public static final float ORBIT_RADIUS = 2.0F;
    public static final float ORBIT_SPEED = 0.1F;

    public EntitySpellOrb(World worldIn) {
        super(worldIn);
        setSize(0.25F, 0.25F);
        this.noClip = true;
    }

    @Override
    protected void entityInit() {
        this.getDataManager().register(DW_EFFECT, Optional.<SpellData>absent());
        this.getDataManager().register(DW_ICON_NAME, "arcane");
        this.getDataManager().register(DW_COLOR, 0xFFFFFF);
        this.getDataManager().register(DW_SHOOTER, 0);
        this.getDataManager().register(DW_ORB_INDEX, 0);
        this.getDataManager().register(DW_TOTAL_ORBS, 1);
    }

    public void setShooter(EntityLivingBase living) {
        this.getDataManager().set(DW_SHOOTER, living.getEntityId());
    }

    public EntityLivingBase getShooter() {
        try {
            return (EntityLivingBase) world.getEntityByID(this.getDataManager().get(DW_SHOOTER));
        } catch (RuntimeException e) {
            this.setDead();
            return null;
        }
    }

    public SpellData getSpell() {
        return this.getDataManager().get(DW_EFFECT).orNull();
    }

    public void setSpell(SpellData spell) {
        this.getDataManager().set(DW_EFFECT, Optional.fromNullable(spell));
        Affinity mainAff = spell.getMainShift();
        if (mainAff.equals(Affinities.ender)) this.getDataManager().set(DW_COLOR, COLOR_ENDER);
        else if (mainAff.equals(Affinities.ice)) this.getDataManager().set(DW_COLOR, COLOR_ICE);
        else if (mainAff.equals(Affinities.life)) this.getDataManager().set(DW_COLOR, COLOR_LIFE);
    }

    public void setIcon(String icon) {
        this.getDataManager().set(DW_ICON_NAME, icon);
    }

    public String getIcon() {
        return this.getDataManager().get(DW_ICON_NAME);
    }

    public int getColor() {
        return this.getDataManager().get(DW_COLOR);
    }

    public void setOrbIndex(int index) {
        this.getDataManager().set(DW_ORB_INDEX, index);
    }

    public int getOrbIndex() {
        return this.getDataManager().get(DW_ORB_INDEX);
    }

    public void setTotalOrbs(int total) {
        this.getDataManager().set(DW_TOTAL_ORBS, total);
    }

    public int getTotalOrbs() {
        return this.getDataManager().get(DW_TOTAL_ORBS);
    }

    @Override
    public void onUpdate() {
        EntityLivingBase caster = getShooter();

        // If caster is dead or missing, remove the orb
        if (caster == null || caster.isDead) {
            this.setDead();
            return;
        }

        // Maximum lifetime of 600 ticks (30 seconds)
        if (ticksExisted > MAX_LIFETIME_TICKS) {
            this.setDead();
            return;
        }

        // Only update position on server side to prevent desync/jittering
        if (!world.isRemote) {
            try {
                // Periodically re-count orbs and redistribute indices evenly
                if (ticksExisted % REDISTRIBUTION_INTERVAL == 0) {
                    List<EntitySpellOrb> siblings = world.getEntitiesWithinAABB(EntitySpellOrb.class,
                            caster.getEntityBoundingBox().grow(6))
                            .stream()
                            .filter(e -> e.getShooter() == caster)
                            .sorted(java.util.Comparator.comparingInt(EntitySpellOrb::getOrbIndex))
                            .collect(java.util.stream.Collectors.toList());
                    int total = siblings.size();
                    for (int i = 0; i < total; i++) {
                        EntitySpellOrb sibling = siblings.get(i);
                        sibling.setOrbIndex(i);
                        sibling.setTotalOrbs(total);
                    }
                }

                // Update orbit angle based on orb index to distribute orbs evenly
                // Use world time (not per-entity ticksExisted) so all orbs rotate in lockstep
                int totalOrbs = getTotalOrbs();
                int orbIndex = getOrbIndex();
                float angleOffset = (float) (2.0F * Math.PI * orbIndex / totalOrbs);
                orbitAngle = (world.getTotalWorldTime() * ORBIT_SPEED) + angleOffset;

                // Calculate orbital position around caster
                double offsetX = Math.cos(orbitAngle) * ORBIT_RADIUS;
                double offsetZ = Math.sin(orbitAngle) * ORBIT_RADIUS;
                double offsetY = Math.sin(orbitAngle * 2) * 0.5; // Bob up and down

                // Set position directly — no lerp so orbs maintain exact even spacing
                double targetX = caster.posX + offsetX;
                double targetY = caster.posY + caster.getEyeHeight() * 0.5 + offsetY;
                double targetZ = caster.posZ + offsetZ;

                setPosition(targetX, targetY, targetZ);

                // Check for entity collisions
                List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this,
                        getEntityBoundingBox().grow(0.5D, 0.5D, 0.5D));

                for (Entity entity : entities) {
                    if (entity instanceof EntityLivingBase && !entity.equals(caster)) {
                        // Apply spell effects on collision
                        if (getSpell() != null) {
                            getSpell().applyComponentsToEntity(world, caster, entity);
                            // Execute if there are more stages
                            if (getSpell().hasMoreStages()) {
                                getSpell().execute(world, caster, (EntityLivingBase) entity,
                                        entity.posX, entity.posY, entity.posZ, null);
                            }
                        }
                        // Remove orb after hitting entity
                        this.setDead();
                        break;
                    }
                }
            } catch (NullPointerException e) {
                // Entity collision can sometimes result in null references
                // when entities despawn during processing
                this.setDead();
            }
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompound) {
        NBTTagCompound am2Tag = NBTUtils.getAM2Tag(tagCompound);
        dataManager.set(DW_EFFECT, Optional.of(SpellData.readFromNBT(am2Tag.getCompoundTag("Effect"))));
        dataManager.set(DW_ICON_NAME, am2Tag.getString("IconName"));
        dataManager.set(DW_COLOR, am2Tag.getInteger("Color"));
        dataManager.set(DW_SHOOTER, am2Tag.getInteger("Shooter"));
        dataManager.set(DW_ORB_INDEX, am2Tag.getInteger("OrbIndex"));
        dataManager.set(DW_TOTAL_ORBS, am2Tag.getInteger("TotalOrbs"));
        orbitAngle = am2Tag.getFloat("OrbitAngle");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        NBTTagCompound am2Tag = NBTUtils.getAM2Tag(tagCompound);
        NBTTagCompound tmp = new NBTTagCompound();
        dataManager.get(DW_EFFECT).or(new SpellData(new ItemStack(AMItems.spell), Lists.newArrayList(), UUID.randomUUID(), new NBTTagCompound())).writeToNBT(tmp);
        am2Tag.setTag("Effect", tmp);
        am2Tag.setString("IconName", dataManager.get(DW_ICON_NAME));
        am2Tag.setInteger("Color", dataManager.get(DW_COLOR));
        am2Tag.setInteger("Shooter", dataManager.get(DW_SHOOTER));
        am2Tag.setInteger("OrbIndex", dataManager.get(DW_ORB_INDEX));
        am2Tag.setInteger("TotalOrbs", dataManager.get(DW_TOTAL_ORBS));
        am2Tag.setFloat("OrbitAngle", orbitAngle);
    }
}
