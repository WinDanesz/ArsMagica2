package am2.common.blocks.tileentity.flickers;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.api.flickers.IFlickerController;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFloatUpward;
import am2.common.entity.SpawnBlacklists;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;

public class FlickerOperatorButchery extends AbstractFlickerFunctionality {

    public final static FlickerOperatorButchery instance = new FlickerOperatorButchery();

    @Override
    public boolean RequiresPower() {
        return true;
    }

    @Override
    public int PowerPerOperation() {
        return 500;
    }

    @Override
    public boolean DoOperation(World worldObj, IFlickerController<?> habitat, boolean powered) {
        HashMap<Class<?>, Integer> entityCount = new HashMap<>();
        int radius = 6;
        List<EntityAnimal> creatures = worldObj.getEntitiesWithinAABB(EntityAnimal.class, new AxisAlignedBB(((TileEntity) habitat).getPos()).grow(radius));
        for (EntityAnimal creature : creatures) {
            Class<? extends EntityAnimal> clazz = creature.getClass();
            if (!SpawnBlacklists.canButcheryAffect(clazz))
                continue;
            Integer count = entityCount.get(clazz);
            if (count == null)
                count = 0;
            if (!creature.isChild())
                count++;
            entityCount.put(clazz, count);
            if (count > 2) {
                if (worldObj.isRemote) {
                    AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(worldObj, "ghost", ((TileEntity) habitat).getPos().getX() + 0.5, ((TileEntity) habitat).getPos().getY() + 0.7, ((TileEntity) habitat).getPos().getZ() + 0.5);
                    if (particle != null) {
                        particle.setMaxAge(20);
                        particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.05f, 1, false));
                    }
                } else {
                    creature.attackEntityFrom(DamageSource.GENERIC, 500);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean DoOperation(World worldObj, IFlickerController<?> habitat, boolean powered, Affinity[] flickers) {
        return DoOperation(worldObj, habitat, powered);
    }

    @Override
    public void RemoveOperator(World worldObj, IFlickerController<?> habitat, boolean powered) {
    }

    @Override
    public void RemoveOperator(World worldObj, IFlickerController<?> habitat, boolean powered, Affinity[] flickers) {
    }

    @Override
    public int TimeBetweenOperation(boolean powered, Affinity[] flickers) {
        return 600;
    }


    @Override
    public Object[] getRecipe() {
        return new Object[]{
                "PBC",
                "FGL",
                "RER",
                'P', new ItemStack(Items.PORKCHOP),
                Character.valueOf('B'), new ItemStack(Items.BEEF),
                Character.valueOf('C'), new ItemStack(Items.CHICKEN),
                Character.valueOf('F'), new ItemStack(AMItems.flicker_jar, 1, Affinities.fire.getID()),
                Character.valueOf('G'), new ItemStack(Items.GOLDEN_SWORD),
                Character.valueOf('L'), new ItemStack(AMItems.flicker_jar, 1, Affinities.life.getID()),
                Character.valueOf('R'), new ItemStack(AMItems.rune, 1, EnumDyeColor.RED.getDyeDamage()),
                Character.valueOf('E'), new ItemStack(AMItems.evil_book)
        };
    }

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public ResourceLocation getTexture() {
        return new ResourceLocation("arsmagica2", "FlickerOperatorButchery");
    }

    @Override
    public Affinity[] getMask() {
        return new Affinity[]{Affinities.fire, Affinities.life};
    }
}
