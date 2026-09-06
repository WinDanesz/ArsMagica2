package am2.common.entity;

import am2.ArsMagica;
import am2.common.entity.ai.EntityAIDefendDryads;
import am2.common.entity.ai.EntityAIDruidAttackSpell;
import am2.common.entity.ai.EntityAIWanderNearDryads;
import am2.common.entity.ai.selectors.LightMageEntitySelector;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMLoot;
import am2.common.utils.DruidTrades;
import am2.common.utils.NPCSpells;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * A guardian that naturally spawns alongside dryad groups (see {@link EntityDryad#onInitialSpawn}),
 * defends them with earth spells, and offers a villager-style limited trade in Ars Magica plants
 * (see {@link DruidTrades}) via the standard vanilla trading screen.
 */
public class EntityDruid extends EntityCreature implements IMerchant {

    @Nullable
    private EntityPlayer customer;
    @Nullable
    private MerchantRecipeList tradeList;
    private int timeUntilReset;
    private boolean needsRestock;

    public EntityDruid(World world) {
        super(world);
        this.setSize(0.6F, 1.95F);
        EntityExtension.For(this).setMagicLevelWithMana(15 + this.rand.nextInt(10));
        this.initAI();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getDruidMaxHealth());
    }

    private void initAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIDruidAttackSpell(this, MovementSpeed(), 60,
                NPCSpells.getInstance().druid_RockThrow, NPCSpells.getInstance().druid_RootWave,
                NPCSpells.getInstance().druid_WolfSummon));
        this.tasks.addTask(5, new EntityAIWanderNearDryads(this, WanderSpeed()));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAIDefendDryads(this, ArsMagica.config.getDruidDefendRadius()));
        // Match light mage hostile targets, with retaliation and dryad defense taking priority.
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityDarkMage>(this, EntityDarkMage.class, 0, true, false, null));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntitySlime>(this, EntitySlime.class, 0, true, false, null));
        this.targetTasks.addTask(4, new EntityAINearestAttackableTarget<EntityMob>(this, EntityMob.class, 0, true, false, LightMageEntitySelector.instance));
    }

    protected float MovementSpeed() {
        return 0.4f;
    }

    protected float WanderSpeed() {
        return 0.3f;
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase target) {
        super.setAttackTarget(isNatureAlly(target) ? null : target);
    }

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> targetClass) {
        return !EntityNatureElemental.class.isAssignableFrom(targetClass)
                && !EntityEarthElemental.class.isAssignableFrom(targetClass)
                && super.canAttackClass(targetClass);
    }

    private boolean isNatureAlly(@Nullable EntityLivingBase entity) {
        return entity instanceof EntityNatureElemental || entity instanceof EntityEarthElemental;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.DRUID_LOOT;
    }

    @Override
    protected boolean canDespawn() {
        return ArsMagica.config.canDruidsDespawn();
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!SpawnBlacklists.entityCanSpawnHere(this.getPosition(), world, this))
            return false;
        return super.getCanSpawnHere();
    }

    @Override
    public void onDeath(DamageSource cause) {
        this.setCustomer(null);
        super.onDeath(cause);
    }

    // ---- Trading (IMerchant) ----

    private boolean isTrading() {
        return this.customer != null;
    }

    @Override
    public void setCustomer(@Nullable EntityPlayer player) {
        this.customer = player;
    }

    @Nullable
    @Override
    public EntityPlayer getCustomer() {
        return this.customer;
    }

    @Nullable
    @Override
    public MerchantRecipeList getRecipes(EntityPlayer player) {
        if (this.tradeList == null) {
            this.tradeList = DruidTrades.generate(this.rand);
        }
        return this.tradeList;
    }

    @Override
    public void setRecipes(@Nullable MerchantRecipeList recipeList) {
        // Server is authoritative on tradeList; nothing to do client-side (matches EntityVillager).
    }

    @Override
    public void useRecipe(MerchantRecipe recipe) {
        recipe.incrementToolUses();
        this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
        if (recipe.getToolUses() == 1 || this.rand.nextInt(5) == 0) {
            this.timeUntilReset = 40;
            this.needsRestock = true;
        }
    }

    @Override
    public void verifySellingItem(ItemStack stack) {
        if (!this.world.isRemote) {
            this.playSound(stack.isEmpty() ? SoundEvents.ENTITY_VILLAGER_NO : SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
        }
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public BlockPos getPos() {
        return this.getPosition();
    }

    @Override
    protected void updateAITasks() {
        if (!this.isTrading() && this.timeUntilReset > 0) {
            --this.timeUntilReset;
            if (this.timeUntilReset <= 0 && this.needsRestock) {
                DruidTrades.restock(this.tradeList, this.rand);
                this.needsRestock = false;
            }
        }
        super.updateAITasks();
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (this.world.isRemote) return EnumActionResult.PASS;
        if (hand == EnumHand.OFF_HAND) return EnumActionResult.PASS;
        if (player.isSneaking()) return EnumActionResult.PASS;
        if (!this.isEntityAlive() || this.isTrading()) return EnumActionResult.PASS;

        if (this.tradeList == null) {
            this.tradeList = DruidTrades.generate(this.rand);
        }
        if (!this.tradeList.isEmpty()) {
            this.setCustomer(player);
            player.displayVillagerTradeGui(this);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.tradeList != null) {
            compound.setTag("Offers", this.tradeList.getRecipiesAsTags());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("Offers", 10)) {
            this.tradeList = new MerchantRecipeList(compound.getCompoundTag("Offers"));
        }
    }
}
