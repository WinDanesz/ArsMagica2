package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMLineArc;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class ManaLink extends SpellComponent {

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AMBlocks.mana_battery,
                AMBlocks.essence_conduit,
                AMItems.crystal_wrench,
                AMItems.mana_focus
        };
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (target instanceof EntityLivingBase) {
            EntityExtension.For((EntityLivingBase) target).updateManaLink(caster);
            return true;
        }
        return false;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }


    @Override
    public float manaCost() {
        return 0;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        AMLineArc arc = (AMLineArc) ArsMagica.proxy.particleManager.spawn(world, "textures/blocks/wipblock2.png", caster, target);
        if (arc != null) {
            arc.setExtendToTarget();
            arc.setIgnoreAge(false);
            arc.setRBGColorF(0.17f, 0.88f, 0.88f);
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.lightning, Affinities.ender, Affinities.arcane);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.25f;
    }


}
