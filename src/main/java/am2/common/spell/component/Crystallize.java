package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFadeOut;
import am2.client.particles.ParticleFleePoint;
import am2.common.items.ItemCrystallizedEntity;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class Crystallize extends SpellComponent {

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                Items.DIAMOND,
                Items.ENDER_PEARL,
                new ItemStack(AMItems.crystal_phylactery)
        };
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (!(target instanceof EntityLivingBase))
            return false;

        if (target instanceof EntityPlayer)
            return false;

        if (!(caster instanceof EntityPlayer))
            return false;

        EntityLivingBase livingTarget = (EntityLivingBase) target;

        // Check max health threshold
        if (livingTarget.getMaxHealth() > ArsMagica.config.getCrystallizeMaxHealth())
            return false;

        // Check current health threshold
        if (livingTarget.getHealth() / livingTarget.getMaxHealth() > ArsMagica.config.getCrystallizeHealthThreshold())
            return false;

        // Check entity blacklist
        ResourceLocation entityId = EntityList.getKey(target);
        if (entityId != null) {
            for (String blacklisted : ArsMagica.config.getCrystallizeMobBlacklist()) {
                if (blacklisted != null && entityId.toString().equals(blacklisted)) {
                    return false;
                }
            }
        }

        if (!world.isRemote) {
            ItemStack crystalStack = ItemCrystallizedEntity.createStackForEntity(AMItems.crystallized_entity, target);

            EntityItem entityItem = new EntityItem(world, target.posX, target.posY + target.getEyeHeight(), target.posZ, crystalStack);
            entityItem.setNoPickupDelay();
            world.spawnEntity(entityItem);

            target.setDead();
        }

        return true;
    }

    @Override
    public float manaCost() {
        return 800;
    }

    @Override
    public ItemStack[] reagents(EntityLivingBase caster) {
        return new ItemStack[]{
                new ItemStack(Items.DIAMOND)
        };
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        double eyeY = target != null ? target.posY + target.getEyeHeight() : y + 0.5;
        for (int i = 0; i < 25; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", x, eyeY, z);
            if (particle != null) {
                particle.addRandomOffset(0.3, 0.3, 0.3);
                particle.setMaxAge(20 + rand.nextInt(15));
                particle.setParticleScale(0.08f + rand.nextFloat() * 0.07f);
                float r = 0.3f + rand.nextFloat() * 0.15f;
                float g = 0.75f + rand.nextFloat() * 0.2f;
                float b = 0.9f + rand.nextFloat() * 0.1f;
                if (colorModifier > -1) {
                    r = ((colorModifier >> 16) & 0xFF) / 255.0f;
                    g = ((colorModifier >> 8) & 0xFF) / 255.0f;
                    b = (colorModifier & 0xFF) / 255.0f;
                }
                particle.setRGBColorF(r, g, b);
                particle.AddParticleController(new ParticleFleePoint(particle,
                        new Vec3d(x, eyeY, z), 0.05 + rand.nextDouble() * 0.08, 2.0, 1, false));
                particle.AddParticleController(new ParticleFadeOut(particle, 0, false).setFadeSpeed(0.04f));
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.arcane, Affinities.earth);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.01f;
    }
}
