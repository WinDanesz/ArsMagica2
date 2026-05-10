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
import am2.common.utils.AffinityShiftUtils;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;

public class FlickerOperatorProgeny extends AbstractFlickerFunctionality {

    private static final int BASE_COOLDOWN = 2000;
    private static final float COOLDOWN_BONUS = 0.75f;
    private static final int COOLDOWN_MINIMUM = 600; //minimum 30 seconds

    public final static FlickerOperatorProgeny instance = new FlickerOperatorProgeny();

    @Override
    public int getID() {
        return 12;
    }

    @Override
    public boolean RequiresPower() {
        return true;
    }

    @Override
    public int PowerPerOperation() {
        return 500;
    }

    @Override
    public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered) {
        HashMap<Class<? extends EntityAnimal>, Integer> entityCount = new HashMap<>();
        int radius = 8;
        List<EntityAnimal> creatures = world.getEntitiesWithinAABB(EntityAnimal.class, new AxisAlignedBB(((TileEntity) habitat).getPos()).grow(radius));
        for (EntityAnimal creature : creatures) {
            Class<? extends EntityAnimal> clazz = creature.getClass();
            if (!SpawnBlacklists.canProgenyAffect(clazz))
                continue;
            Integer count = entityCount.get(clazz);
            if (count == null)
                count = 0;
            if (!creature.isInLove() && creature.getGrowingAge() == 0)
                count++;
            entityCount.put(clazz, count);
            if (count == 2) {
                if (world.isRemote) {
                    AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "heart", ((TileEntity) habitat).getPos().getX() + 0.5, ((TileEntity) habitat).getPos().getY() + 0.7, ((TileEntity) habitat).getPos().getZ() + 0.5);
                    if (particle != null) {
                        particle.setMaxAge(20);
                        particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.05f, 1, false));
                    }
                } else {
                    creatures = world.getEntitiesWithinAABB(clazz, new AxisAlignedBB(((TileEntity) habitat).getPos()).grow(radius));
                    count = 0;
                    for (EntityAnimal animal : creatures) {
                        if (!animal.isChild()) {
                            animal.setInLove(null);
                            count++;
                            if (count == 2)
                                break;
                        }
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean DoOperation(World world, IFlickerController<?> controller, boolean powered, Affinity[] flickers) {
        return DoOperation(world, controller, powered);
    }

    @Override
    public void RemoveOperator(World world, IFlickerController<?> controller, boolean powered) {
    }

    @Override
    public int TimeBetweenOperation(boolean powered, Affinity[] flickers) {
        if (powered) {
            float base = BASE_COOLDOWN;
            for (Affinity aff : flickers)
                if (aff == Affinities.lightning)
                    base *= COOLDOWN_BONUS;

            return (int) Math.max(base, COOLDOWN_MINIMUM);
        } else {
            return BASE_COOLDOWN;
        }
    }

    @Override
    public void RemoveOperator(World world, IFlickerController<?> controller, boolean powered, Affinity[] flickers) {
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                "ELE",
                "EFE",
                "EWE",
                Character.valueOf('E'), Items.EGG,
                Character.valueOf('L'), AffinityShiftUtils.getEssenceForAffinity(Affinities.life),
                Character.valueOf('F'), new ItemStack(AMItems.flicker_jar, 1, Affinities.life.getID()),
                Character.valueOf('W'), new ItemStack(AMItems.rune, 1, EnumDyeColor.WHITE.getDyeDamage())

        };
    }

    @Override
    public ResourceLocation getTexture() {
        return new ResourceLocation("arsmagica2", "flickeroperatorprogeny");
    }

    @Override
    public Affinity[] getMask() {
        return new Affinity[]{Affinities.life};
    }


}
