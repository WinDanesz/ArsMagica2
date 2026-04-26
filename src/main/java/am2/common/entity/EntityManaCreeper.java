package am2.common.entity;

import am2.ArsMagica;
import am2.common.registry.AMLoot;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class EntityManaCreeper extends EntityCreeper {

    int timeSinceIgnited_Local;
    int lastActiveTime;

    protected int fuseLength = 20;

    public EntityManaCreeper(World par1World) {
        super(par1World);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getManaCreeperMaxHealth());
    }

    @Override
    public float getCreeperFlashIntensity(float par1) {
        return (this.lastActiveTime + (this.timeSinceIgnited_Local - this.lastActiveTime) * par1) / (this.fuseLength - 2);
    }

    @Override
    public void onUpdate() {
        if (this.isEntityAlive()) {
            this.lastActiveTime = this.timeSinceIgnited_Local;
            int var1 = this.getCreeperState();

            if (var1 > 0 && this.timeSinceIgnited_Local == 0) {
                this.world.playSound(posX, posY, posZ, SoundEvents.ENTITY_CREEPER_PRIMED, SoundCategory.HOSTILE, 1.0F, 0.5F, false);
            }

            this.timeSinceIgnited_Local += var1;

            if (this.timeSinceIgnited_Local < 0) {
                this.timeSinceIgnited_Local = 0;
            }

            if (this.timeSinceIgnited_Local >= 10) {
                this.timeSinceIgnited_Local = 10;

                if (!this.world.isRemote) {
                    createManaVortex();
                    this.onDeath(DamageSource.GENERIC);
                    this.setDead();
                }
            }
        }

        super.onUpdate();
    }

    private void createManaVortex() {
        if (world.isRemote) {
            return;
        }
        EntityManaVortex vortex = new EntityManaVortex(world);
        vortex.setPosition(this.posX, this.posY + 1, this.posZ);
        world.spawnEntity(vortex);
    }

    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.MANA_CREEPER_LOOT;
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!SpawnBlacklists.entityCanSpawnHere(this.getPosition(), world, this))
            return false;
        return super.getCanSpawnHere();
    }
}
