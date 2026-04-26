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
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Wall extends SpellShape {

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.vinteum_dust),
                AMBlocks.magic_wall,
                Blocks.COBBLESTONE_WALL,
                Blocks.OAK_FENCE,
                AMBlocks.magic_wall,
                "E:*", 2500
        };
    }

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        if (world.isRemote) return SpellCastResult.SUCCESS;
        int radius = (int) spell.getModifiedValue(ArsMagica.config.getWallDefaultRadius(), SpellModifiers.RADIUS, Operation.MULTIPLY, world, caster, target);
        double gravity = spell.getModifiedValue(1, SpellModifiers.GRAVITY, Operation.ADD, world, caster, target);
        int duration = (int) spell.getModifiedValue(ArsMagica.config.getWallDefaultDuration(), SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target);

        EntitySpellEffect wall = new EntitySpellEffect(world);
        wall.setRadius(radius);
        wall.setTicksToExist(duration);
        wall.setGravity(gravity);
        wall.setTickRate(2);
        wall.SetCasterAndStack(caster, spell);
        wall.setPosition(x, y, z);
        wall.setWall(caster.rotationYaw);
        world.spawnEntity(wall);
        return SpellCastResult.SUCCESS;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.RADIUS, SpellModifiers.GRAVITY, SpellModifiers.DURATION, SpellModifiers.COLOR, SpellModifiers.TARGET_NONSOLID_BLOCKS);
    }




    @Override
    public float manaCostMultiplier() {
        return 2.5f;
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
