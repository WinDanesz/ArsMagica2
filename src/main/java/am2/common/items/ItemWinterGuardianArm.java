package am2.common.items;

import am2.ArsMagica;
import am2.client.particles.AMParticle;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.entity.EntityWinterGuardianArm;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMPotions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemWinterGuardianArm extends Item {

    public ItemWinterGuardianArm() {
        super();
        setMaxStackSize(1);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();
        if (slot.equals(EntityEquipmentSlot.MAINHAND)) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 6, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -1, 0));
        }
        return multimap;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("am2.tooltip.winter_arm"));
        super.addInformation(stack, world, tooltip, flag);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        if (entity instanceof EntityLivingBase) {
            ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(AMPotions.frost_slow, 60, 3));
            if (player.world.isRemote) {
                if (!EBWizardryCompatBootstrap.spawnFrostParticles(player.world, entity.posX, entity.posY, entity.posZ, 5, player.world.rand)) {
                    for (int i = 0; i < 5; ++i) {
                        AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(player.world, "snowflakes", entity.posX + 0.5, entity.posY + 0.5, entity.posZ + 0.5);
                        if (particle != null) {
                            particle.addRandomOffset(1, 0.5, 1);
                            particle.addVelocity(player.world.rand.nextDouble() * 0.2 - 0.1, 0.3, player.world.rand.nextDouble() * 0.2 - 0.1);
                            particle.setAffectedByGravity();
                            particle.setDontRequireControllers();
                            particle.setMaxAge(10);
                            particle.setParticleScale(0.1f);
                        }
                    }
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (flingArm(stack, worldIn, player)) {
            return new ActionResult<>(EnumActionResult.SUCCESS, ItemStack.EMPTY);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public boolean flingArm(ItemStack stack, World world, EntityPlayer player) {
        if (!EntityExtension.For(player).hasEnoughMana(250) && !player.capabilities.isCreativeMode) {
            if (world.isRemote)
                ArsMagica.proxy.flashManaBar();
            return false;
        }
        if (!world.isRemote) {
            EntityWinterGuardianArm projectile = new EntityWinterGuardianArm(world, player, 1.25f);
            projectile.setThrowingEntity(player);
            projectile.setProjectileSpeed(2.0);
            world.spawnEntity(projectile);
        }
        EntityExtension.For(player).deductMana(250f);
        return true;
    }
}
