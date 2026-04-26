package am2.common.spell.shape;

import am2.ArsMagica;
import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.entity.EntitySpellEffect;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Zone extends SpellShape {

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        if (world.isRemote) return SpellCastResult.SUCCESS;
        int radius = (int) spell.getModifiedValue(ArsMagica.config.getZoneDefaultRadius(), SpellModifiers.RADIUS, Operation.ADD, world, caster, target);
        double gravity = spell.getModifiedValue(0, SpellModifiers.GRAVITY, Operation.ADD, world, caster, target);
        int duration = (int) spell.getModifiedValue(ArsMagica.config.getZoneDefaultDuration(), SpellModifiers.DURATION, Operation.ADD, world, caster, target);
        EntitySpellEffect zone = new EntitySpellEffect(world);
        zone.setRadius(radius);
        zone.setTicksToExist(duration);
        zone.setGravity(gravity);
        zone.SetCasterAndStack(caster, spell);
        zone.setPosition(x, y, z);
        world.spawnEntity(zone);
        return SpellCastResult.SUCCESS;
    }



    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.RADIUS, SpellModifiers.GRAVITY, SpellModifiers.DURATION, SpellModifiers.COLOR, SpellModifiers.TARGET_NONSOLID_BLOCKS);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AMBlocks.tarma_root,
                new ItemStack(AMItems.moonstone),
                new ItemStack(AMItems.sunstone),
                Items.DIAMOND
        };
    }

    @Override
    public float manaCostMultiplier() {
        return 4.5f;
    }

    @Override
    public boolean isTerminusShape() {
        return false;
    }

    @Override
    public boolean isPrincipumShape() {
        return true;
    }
}
