package am2.common.bosses;

import am2.ArsMagica;
import am2.common.bosses.ai.EntityAICastSpell;
import am2.common.bosses.ai.EntityAIDispel;
import am2.common.bosses.ai.EntityAISummonAllies;
import am2.common.bosses.ai.ISpellCastCallback;
import am2.common.entity.EntityDarkling;
import am2.common.entity.EntityEarthElemental;
import am2.common.entity.EntityFireElemental;
import am2.common.entity.EntityManaElemental;
import am2.common.registry.AMLoot;
import am2.common.registry.AMSounds;
import am2.common.utils.NPCSpells;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;

public class EntityLifeGuardian extends AM2Boss {

    private ArrayList<EntityLiving> minions;
    public ArrayList<EntityLiving> queued_minions;

    private static final DataParameter<Integer> DATA_MINION_COUNT = EntityDataManager.createKey(EntityLifeGuardian.class, DataSerializers.VARINT);

    public EntityLifeGuardian(World par1World) {
        super(par1World);
        this.setSize(1, 2);
        minions = new ArrayList<EntityLiving>();
        queued_minions = new ArrayList<EntityLiving>();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(DATA_MINION_COUNT, 0);
    }

    @Override
    protected void initSpecificAI() {
        this.tasks.addTask(1, new EntityAIDispel(this));
        this.tasks.addTask(1, new EntityAICastSpell<EntityLifeGuardian>(this, NPCSpells.getInstance().healSelf, 16, 23, 100, BossActions.CASTING, new ISpellCastCallback<EntityLifeGuardian>() {
            @Override
            public boolean shouldCast(EntityLifeGuardian host, ItemStack spell) {
                return host.getHealth() < host.getMaxHealth();
            }
        }));
        this.tasks.addTask(2, new EntityAICastSpell<EntityLifeGuardian>(this, NPCSpells.getInstance().nauseate, 16, 23, 20, BossActions.CASTING, new ISpellCastCallback<EntityLifeGuardian>() {
            @Override
            public boolean shouldCast(EntityLifeGuardian host, ItemStack spell) {
                return minions.isEmpty();
            }
        }));
        this.tasks.addTask(3, new EntityAISummonAllies(this, EntityEarthElemental.class, EntityFireElemental.class, EntityManaElemental.class, EntityDarkling.class));
    }

    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
        if (par1DamageSource.getTrueSource() != null && par1DamageSource.getTrueSource() instanceof EntityLivingBase) {
            for (EntityLivingBase minion : minions.toArray(new EntityLivingBase[minions.size()])) {
                ((EntityLiving) minion).setAttackTarget((EntityLivingBase) par1DamageSource.getTrueSource());
            }
        }
        return super.attackEntityFrom(par1DamageSource, par2);
    }

    @Override
    protected float modifyDamageAmount(DamageSource source, float damageAmt) {
        if (!minions.isEmpty()) {
            damageAmt = 0;
            minions.get(getRNG().nextInt(minions.size())).attackEntityFrom(source, damageAmt);
        }
        return damageAmt;
    }

    public int getNumMinions() {
        return this.dataManager.get(DATA_MINION_COUNT);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getLifeGuardianMaxHealth());
    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getLifeGuardianArmor();
    }

    @Override
    public void onUpdate() {
        //Minion management - add any queued minions to the minion list and prune out any fallen or nonexistant ones
        if (!world.isRemote) {
            minions.addAll(queued_minions);
            queued_minions.clear();
            Iterator<EntityLiving> it = minions.iterator();
            while (it.hasNext()) {
                EntityLiving minion = it.next();
                if (minion == null || minion.isDead)
                    it.remove();
            }

            this.dataManager.set(DATA_MINION_COUNT, minions.size());

            if (this.ticksExisted % 100 == 0) {
                for (EntityLivingBase e : minions)
                    ArsMagica.proxy.particleManager.spawn(world, ArsMagica.MODID + ":textures/blocks/sunstone_ore.png", this, e);
            }
        }

        if (this.ticksExisted % 40 == 0)
            this.heal(2f);

        super.onUpdate();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AMSounds.LIFE_GUARDIAN_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSounds.LIFE_GUARDIAN_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSounds.LIFE_GUARDIAN_IDLE;
    }

    @Override
    public SoundEvent getAttackSound() {
        return AMSounds.LIFE_GUARDIAN_HEAL;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.LIFE_GUARDIAN_LOOT;
    }

    @Override
    public float getEyeHeight() {
        return 1.5f;
    }

    @Override
    protected Color getBarColor() {
        return Color.PURPLE;
    }
}
