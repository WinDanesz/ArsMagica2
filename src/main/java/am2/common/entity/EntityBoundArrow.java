package am2.common.entity;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

import am2.api.spell.SpellData;
import am2.common.defs.ItemDefs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class EntityBoundArrow extends EntityArrow {
	
	private static final DataParameter<Optional<SpellData>> SPELL_STACK = EntityDataManager.createKey(EntityBoundArrow.class, SpellData.OPTIONAL_SPELL_DATA);
	
	public EntityBoundArrow(World worldIn, EntityLivingBase shooter) {
		super(worldIn, shooter);
	}
	
	public EntityBoundArrow(World worldIn) {
		super(worldIn);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(SPELL_STACK, Optional.of(null));
	}
	
	public void setSpellStack(@Nullable SpellData stack) {
		this.dataManager.set(SPELL_STACK, Optional.fromNullable(stack));
	}
	
	@Override
	protected void arrowHit(EntityLivingBase living) {
		SpellData stack = dataManager.get(SPELL_STACK).orNull();
		if (stack == null || !(shootingEntity instanceof EntityPlayer))
			return;
		stack.execute(world, (EntityLivingBase) shootingEntity, living, living.posX, living.posY, living.posZ, null);
	}
	
	@Override
	protected ItemStack getArrowStack() {
		return new ItemStack(ItemDefs.BoundArrow);
	}

}
