package am2.common.entity;

import am2.ArsMagica;
import am2.common.entity.ai.EntityAIRangedAttackSpell;
import am2.common.entity.ai.selectors.DarkMageEntitySelector;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMItems;
import am2.common.registry.AMLoot;
import am2.common.utils.NPCSpells;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EntityDarkMage extends EntityMob {

    private static ItemStack diminishedHeldItem = new ItemStack(AMItems.affinity_tome_earth);
    private static ItemStack normalHeldItem = new ItemStack(AMItems.affinity_tome_lightning);
    private static ItemStack augmentedHeldItem = new ItemStack(AMItems.affinity_tome_nature);

    public static final DataParameter<Integer> MAGE_SKIN = EntityDataManager.createKey(EntityDarkMage.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> MAGE_BOOK = EntityDataManager.createKey(EntityDarkMage.class, DataSerializers.VARINT);

    public EntityDarkMage(World world) {
        super(world);
        this.setSize(0.6F, 1.8F);
        EntityExtension.For(this).setMagicLevelWithMana(10 + this.rand.nextInt(20));
        this.initAI();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getDarkMageMaxHealth());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(MAGE_BOOK, 0);
        this.dataManager.register(MAGE_SKIN, this.rand.nextInt(10) + 1);
    }

    public void disarm() {
        if (this.dataManager.get(MAGE_BOOK) != -1) {
            this.dataManager.set(MAGE_BOOK, -1);
            for (Object a : this.tasks.taskEntries.toArray()) {
                EntityAIBase ai = ((EntityAITaskEntry) a).action;
                if (ai instanceof EntityAIRangedAttackSpell)
                    this.tasks.removeTask(ai);
            }
        }
        this.setCanPickUpLoot(true);
    }

    @Override
    public ItemStack getHeldItemMainhand() {
        int cm = this.dataManager.get(MAGE_BOOK);
        if (!this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty())
            return this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        if (cm == -1)
            return ItemStack.EMPTY;
        else if (cm == 0)
            return diminishedHeldItem;
        else if (cm == 1)
            return normalHeldItem;
        else
            return augmentedHeldItem;
    }

    private void initAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityLightMage.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.tasks.addTask(5, new EntityAIWander(this, this.MovementSpeed()));
        this.tasks.addTask(1, new EntityAIAvoidEntity<EntityManaVortex>(this, EntityManaVortex.class, 10, this.MovementSpeed(), this.ActionSpeed()));

        this.tasks.addTask(3, new EntityAIRangedAttackSpell(this, this.MovementSpeed(), 40, NPCSpells.getInstance().darkMage_DiminishedAttack));
        this.tasks.addTask(4, new EntityAIAttackMelee(this, this.MovementSpeed(), false));

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityLightMage>(this, EntityLightMage.class, 0, true, false, null));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 0, true, false, null));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class, 0, true, false, DarkMageEntitySelector.instance));

    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getDarkMageArmor();
    }

    protected float MovementSpeed() {
        return 0.4f;
    }

    protected float ActionSpeed() {
        return 0.5f;
    }
//
//	@Override
//	protected void dropFewItems(boolean par1, int par2){
//		if (par1 && getRNG().nextDouble() < 0.2)
//			for (int j = 0; j < getRNG().nextInt(3); ++j)
//				this.entityDropItem(new ItemStack(ItemDefs.rune, 1, getRNG().nextInt(16)), 0.0f);
//
//		if (par1 && getRNG().nextDouble() < 0.2)
//			this.entityDropItem(new ItemStack(ItemDefs.spellParchment, 1, 0), 0.0f);
//
//		if (par1 && getRNG().nextDouble() < 0.05)
//			this.entityDropItem(new ItemStack(ItemDefs.spellBook, 1, 0), 0.0f);
//	}

    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.DARK_MAGE_LOOT;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
        super.readEntityFromNBT(par1nbtTagCompound);
        if (par1nbtTagCompound.hasKey("am2_dm_skin"))
            this.dataManager.set(MAGE_SKIN, par1nbtTagCompound.getInteger("am2_dm_skin"));
        if (par1nbtTagCompound.hasKey("am2_dm_book"))
            this.dataManager.set(MAGE_BOOK, par1nbtTagCompound.getInteger("am2_dm_book"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound par1nbtTagCompound) {
        super.writeEntityToNBT(par1nbtTagCompound);
        par1nbtTagCompound.setInteger("am2_dm_skin", this.dataManager.get(MAGE_SKIN));
        par1nbtTagCompound.setInteger("am2_dm_book", this.dataManager.get(MAGE_BOOK));
    }

    private int getAverageNearbyPlayerMagicLevel() {
        if (this.world == null) return 0;
        List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.posX - 250, 0, this.posZ - 250, this.posX + 250, 250, this.posZ + 250));
        if (players.isEmpty()) return 0;
        int avgLvl = 0;
        for (EntityPlayer player : players) {
            avgLvl += EntityExtension.For(player).getCurrentLevel();
        }
        return (int) Math.ceil(avgLvl / players.size());
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!SpawnBlacklists.entityCanSpawnHere(this.getPosition(), this.world, this))
            return false;
        int avgLevel = this.getAverageNearbyPlayerMagicLevel();
        if (avgLevel < 8) {
            return false;
        }

        // Cap the number of dark mages that can exist within a 64-block radius
        int nearbyMages = this.world.getEntitiesWithinAABB(EntityDarkMage.class,
                new AxisAlignedBB(posX - 64, posY - 32, posZ - 64, posX + 64, posY + 32, posZ + 64)).size();
        if (nearbyMages >= 4) {
            return false;
        }

        // Scale down spawn rate at higher player levels to prevent overpopulation.
        if (avgLevel > 8) {
            float rejectChance = Math.min(0.75f, (avgLevel - 8) / 120.0f * 0.75f);
            if (this.rand.nextFloat() < rejectChance) {
                return false;
            }
        }

        EntityExtension.For(this).setMagicLevelWithMana(5);
        if (avgLevel * 2 < 0)
            avgLevel = Integer.MAX_VALUE / 2 - 1;
        if (avgLevel == 0) {
            if (this.rand.nextInt(100) < 10) {
                this.tasks.addTask(3, new EntityAIRangedAttackSpell(this, this.MovementSpeed(), 80, NPCSpells.getInstance().darkMage_NormalAttack));
                this.dataManager.set(MAGE_BOOK, 1);
            }
        } else {
            int levelRand = this.rand.nextInt(Math.min(avgLevel * 2, Integer.MAX_VALUE));
            if (levelRand > 60) {
                this.tasks.addTask(2, new EntityAIRangedAttackSpell(this, this.MovementSpeed(), 160, NPCSpells.getInstance().darkMage_AugmentedAttack));
                this.dataManager.set(MAGE_BOOK, 2);
            } else if (levelRand > 30) {
                this.tasks.addTask(3, new EntityAIRangedAttackSpell(this, this.MovementSpeed(), 80, NPCSpells.getInstance().darkMage_NormalAttack));
                this.dataManager.set(MAGE_BOOK, 1);
            }
        }
        return super.getCanSpawnHere();
    }

    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
        if (this.dataManager.get(MAGE_BOOK) == -1) {
            ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
            this.entityDropItem(itemstack, 0.0F);
        }
    }

    @SideOnly(Side.CLIENT)
    public String getTexture() {
        return String.format("arsmagica2:textures/entities/dark_mages/dark_mage_%d.png", this.dataManager.get(MAGE_SKIN).intValue());
    }


}
