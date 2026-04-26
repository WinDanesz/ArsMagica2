package am2.common.spell.component;

import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class ManaShield extends SpellComponent {

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AMItems.mana_focus,
                AMItems.battlemage_chestplate,
                AMItems.mage_robe
        };
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        float consumed = EntityExtension.For(caster).getCurrentMana();
        EntityExtension.For(caster).deductMana(consumed);
        EntityExtension.For((EntityLivingBase) target).addMagicShielding((float) ((consumed / 250F) * spell.getModifiedValue(1, SpellModifiers.BUFF_POWER, Operation.ADD, world, caster, target)));
        return true;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.BUFF_POWER);
    }

    @Override
    public float manaCost() {
        return 0;
    }

    @Override
    public float burnout(EntityLivingBase caster) {
        return 0;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.arcane);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.01f;
    }

}
