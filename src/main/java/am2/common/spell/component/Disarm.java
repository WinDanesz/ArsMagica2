package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFadeOut;
import am2.client.particles.ParticleMoveOnHeading;
import am2.common.entity.EntityDarkMage;
import am2.common.entity.EntityLightMage;
import am2.common.registry.AMEnchantments;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;


public class Disarm extends SpellComponent {
    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (target instanceof EntityLightMage)
            return false;

        double damage = spell.getModifiedValue(ArsMagica.config.getDisarmDamageMultiplier(), SpellModifiers.DAMAGE, Operation.MULTIPLY, world, caster, target);

        if (target instanceof EntityDarkMage)
            return disarmDarkMage((EntityDarkMage) target, world);

        if (target instanceof EntityPlayer)
            return disarmPlayer((EntityPlayer) target, world, damage);

        if (target instanceof EntityEnderman) {
            disarmEnderman((EntityEnderman) target, world, caster);
            return true;
        }

        if (target instanceof EntityMob)
            return disarmMob((EntityMob) target, world, caster);

        return false;
    }

    private boolean disarmDarkMage(EntityDarkMage target, World world) {
        if (!world.isRemote) {
            ItemStack held = target.getHeldItemMainhand();
            if (!held.isEmpty())
                spawnDrop(world, target, held, true);
            target.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
            target.disarm();
        }
        return true;
    }

    private boolean disarmPlayer(EntityPlayer target, World world, double damage) {
        if (!ArsMagica.config.getDisarmAffectsPlayers())
            return false;
        if (!world.isRemote && !FMLCommonHandler.instance().getMinecraftServerInstance().isPVPEnabled())
            return false;

        if (!world.isRemote) {
            // Chance to also disarm offhand
            ItemStack offhand = target.getHeldItemOffhand();
            if (!offhand.isEmpty() && world.rand.nextInt(9) + 1 <= damage
                    && EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound, offhand) <= 0) {
                spawnDrop(world, target, offhand, false);
                target.inventory.offHandInventory.set(0, ItemStack.EMPTY);
            }

            ItemStack mainhand = target.getHeldItemMainhand();
            if (!mainhand.isEmpty()) {
                if (EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound, mainhand) > 0)
                    return true;
                target.dropItem(true);
                return true;
            }
        }
        return false;
    }

    private void disarmEnderman(EntityEnderman target, World world, EntityLivingBase caster) {
        if (!world.isRemote) {
            IBlockState held = target.getHeldBlockState();
            if (held != null) {
                target.setHeldBlockState(null);
                spawnDrop(world, target, new ItemStack(held.getBlock(), 1, held.getBlock().getMetaFromState(held)), false);
            }
        }
        target.setAttackTarget(caster);
    }

    private boolean disarmMob(EntityMob target, World world, EntityLivingBase caster) {
        ItemStack held = target.getHeldItemMainhand();
        if (held.isEmpty())
            return false;
        if (EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound, held) > 0)
            return true;

        if (!world.isRemote)
            spawnDrop(world, target, held, true);

        target.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
        target.setAttackTarget(caster);

        Iterator<EntityAITaskEntry> it = target.tasks.taskEntries.iterator();
        while (it.hasNext()) {
            if (it.next().action instanceof EntityAIAttackRanged) {
                it.remove();
                target.tasks.addTask(5, new EntityAIAttackMelee(target, 0.5, true));
                target.setCanPickUpLoot(true);
                break;
            }
        }
        return true;
    }

    /** Drops a copy of the stack at the target's position, optionally randomizing its damage value. */
    private void spawnDrop(World world, Entity at, ItemStack stack, boolean randomizeDamage) {
        ItemStack drop = stack.copy();
        if (randomizeDamage && drop.getMaxDamage() > 0)
            drop.setItemDamage((int) Math.floor(drop.getMaxDamage() * (0.8f + world.rand.nextFloat() * 0.19f)));
        EntityItem item = new EntityItem(world, at.posX, at.posY, at.posZ, drop);
        item.setDefaultPickupDelay();
        world.spawnEntity(item);
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DAMAGE);
    }

    @Override
    public float manaCost() {
        return 130;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 25; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", x, y, z);
            if (particle != null) {
                particle.addRandomOffset(1, 2, 1);
                particle.AddParticleController(new ParticleMoveOnHeading(particle, MathHelper.wrapDegrees((target instanceof EntityLivingBase ? ((EntityLivingBase) target).rotationYawHead : target.rotationYaw) + 90), MathHelper.wrapDegrees(target.rotationPitch), 0.1 + rand.nextDouble() * 0.5, 1, false));
                particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05f));
                particle.setAffectedByGravity();
                if (rand.nextBoolean())
                    particle.setRGBColorF(0.7f, 0.7f, 0.1f);
                else
                    particle.setRGBColorF(0.1f, 0.7f, 0.1f);
                particle.setMaxAge(40);
                particle.setParticleScale(0.1f);
                if (colorModifier > -1) {
                    particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                }
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.none);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.ORANGE.getDyeDamage()),
                Items.IRON_SWORD
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0;
    }
}
