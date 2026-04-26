package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.api.flickers.FlickerGenerationPool;
import am2.common.entity.EntityFlicker;
import am2.common.items.ItemFlickerJar;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMItems;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class TileEntityFlickerLure extends TileEntityAMPower {

    private int ticksExisted = 0;

    public TileEntityFlickerLure() {
        super(ArsMagica.config.getCapacityFlickerLure());
    }

    @Override
    public boolean canRelayPower(PowerTypes type) {
        return false;
    }

    @Override
    public int getChargeRate() {
        return 5;
    }

    @Override
    public void update() {
        if (world.isRemote)
            return;
        super.update();

        if (world.getStrongPower(pos) > 0) {
            if (world.rand.nextDouble() < 0.05f && PowerNodeRegistry.For(world).checkPower(this, 20)) {
                EntityFlicker flicker = new EntityFlicker(world);
                flicker.setPosition(pos.getX() + 0.5f, pos.getY() + 1.5f, pos.getZ() + 0.5f);
                flicker.setFlickerType(FlickerGenerationPool.INSTANCE.getWeightedAffinity().getID());
                world.spawnEntity(flicker);
                PowerNodeRegistry.For(world).consumePower(this, PowerNodeRegistry.For(world).getHighestPowerType(this), 20);
            }
        }

        if (ticksExisted % 200 == 0) {
            TileEntity eastTile = world.getTileEntity(pos.east());
            TileEntity westTile = world.getTileEntity(pos.west());
            TileEntity southTile = world.getTileEntity(pos.south());
            TileEntity northTile = world.getTileEntity(pos.north());
            List<EntityFlicker> flickers = world.getEntitiesWithinAABB(EntityFlicker.class, new AxisAlignedBB(pos).expand(5, 0, 0));
            for (EntityFlicker flicker : flickers) {
                IInventory inventory = null;
                if (northTile != null && northTile instanceof IInventory)
                    inventory = (IInventory) northTile;
                else if (southTile != null && southTile instanceof IInventory)
                    inventory = (IInventory) southTile;
                else if (eastTile != null && eastTile instanceof IInventory)
                    inventory = (IInventory) eastTile;
                else if (westTile != null && westTile instanceof IInventory)
                    inventory = (IInventory) westTile;
                if (inventory != null) {
                    ItemStack jar = ItemStack.EMPTY;
                    for (int i = 0; i < inventory.getSizeInventory(); i++) {
                        ItemStack is = inventory.getStackInSlot(i);

                        // TODO registry
//						if (is == null || is.getItem() != ItemDefs.flickerJar || is.getItemDamage() != ArsMagicaAPI.getAffinityRegistry().getId(Affinity.NONE)) continue;
//						is.shrink(1);
                        if (is.getCount() <= 0)
                            is = ItemStack.EMPTY;
                        inventory.setInventorySlotContents(i, is);
                        jar = new ItemStack(AMItems.flicker_jar, 1, 0);
                        ((ItemFlickerJar) AMItems.flicker_jar).setFlickerJarTypeFromFlicker(jar, flicker);
                        break;
                    }
                    if (!jar.isEmpty()) {
                        boolean placed = false;
                        for (int i = 0; i < inventory.getSizeInventory(); i++) {
                            ItemStack is = inventory.getStackInSlot(i);
                            if (is.isEmpty()) {
                                inventory.setInventorySlotContents(i, jar);
                                placed = true;
                            } else if (is.isItemEqual(jar) && is.getCount() < 64) {
                                is.grow(1);
                                placed = true;
                            }
                            if (placed) break;
                        }
                        if (!placed) {
                            EntityItem item = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, jar);
                            world.spawnEntity(item);
                        }
                        flicker.setDead();
                    }
                }
            }
        }
        ticksExisted++;
    }
}
