package am2.common.entity.ai;

import am2.api.math.AMVector3;
import am2.common.entity.EntityBroom;
import am2.common.extensions.EntityExtension;
import am2.common.utils.DummyEntityPlayer;
import am2.common.utils.InventoryUtilities;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;

public class EntityAIChestDeposit extends EntityAIBase {

    private EntityBroom host;
    private boolean isDepositing = false;
    private int depositCounter = 0;

    public EntityAIChestDeposit(EntityBroom host) {
        this.host = host;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        AMVector3 iLoc = this.host.getChestLocation();
        if (iLoc == null) {
            return false;
        }

        boolean isEmpty = InventoryUtilities.isInventoryEmpty(this.host.getBroomInventory());
        if (isEmpty) {
            return false;
        }

        // If inventory is full, deposit immediately regardless of targets
        boolean isFull = this.host.isInventoryFull();
        if (isFull) {
            return true;
        }

        // If inventory has items but not full, only deposit if there's no pickup target
        boolean hasTarget = EntityExtension.For(this.host).getInanimateTarget() != null;
        return !hasTarget;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.isDepositing || super.shouldContinueExecuting();
    }

    @Override
    public void resetTask() {
        if (this.isDepositing) {
            AMVector3 iLoc = this.host.getChestLocation();
            if (iLoc != null) {
                TileEntity te = this.host.world.getTileEntity(iLoc.toBlockPos());
                if (te instanceof IInventory) {
                    ((IInventory) te).closeInventory(DummyEntityPlayer.fromEntityLiving(this.host));
                }
            }
        }
        this.isDepositing = false;
        this.depositCounter = 0;
    }

    @Override
    public void updateTask() {
        AMVector3 iLoc = this.host.getChestLocation();

        if (iLoc == null) {
            return;
        }

        TileEntity te = this.host.world.getTileEntity(iLoc.toBlockPos());
        if (te == null || !(te instanceof IInventory)) {
            return;
        }

        double distSq = new AMVector3(this.host).distanceSqTo(iLoc);

        if (distSq > 256) {
            this.host.setPosition(iLoc.x, iLoc.y, iLoc.z);
            return;
        }

        if (distSq > 9) {
            this.host.getNavigator().tryMoveToXYZ(iLoc.x + 0.5, iLoc.y, iLoc.z + 0.5, 0.5f);
        } else {
            IInventory inventory = (IInventory) te;
            if (!this.isDepositing) {
                inventory.openInventory(DummyEntityPlayer.fromEntityLiving(this.host));
            }

            this.isDepositing = true;
            this.depositCounter++;

            if (this.depositCounter > 10) {
                ItemStack mergeStack = InventoryUtilities.getFirstStackInInventory(this.host.getBroomInventory()).copy();
                if (mergeStack.isEmpty()) {
                    this.resetTask();
                    return;
                }
                // Keep an unmodified copy for deductFromInventory search -
                // mergeIntoInventory shrinks mergeStack, and if it hits count 0,
                // compareItemStacks treats it as empty and fails to match,
                // causing the broom to never remove the item (infinite duplication).
                ItemStack searchStack = mergeStack.copy();
                boolean merged = InventoryUtilities.mergeIntoInventory(inventory, mergeStack, 1);
                if (!merged) {
                    if (te instanceof TileEntityChest) {
                        TileEntityChest chest = (TileEntityChest) te;
                        TileEntityChest adjacent = null;
                        if (chest.adjacentChestXNeg != null)
                            adjacent = chest.adjacentChestXNeg;
                        else if (chest.adjacentChestXPos != null)
                            adjacent = chest.adjacentChestXPos;
                        else if (chest.adjacentChestZPos != null)
                            adjacent = chest.adjacentChestZPos;
                        else if (chest.adjacentChestZNeg != null)
                            adjacent = chest.adjacentChestZNeg;

                        if (adjacent != null) {
                            merged = InventoryUtilities.mergeIntoInventory(adjacent, mergeStack, 1);
                        }
                    }
                }
                if (merged) {
                    InventoryUtilities.deductFromInventory(this.host.getBroomInventory(), searchStack, 1, null);
                }
            }

            if (this.depositCounter > 10 && (InventoryUtilities.isInventoryEmpty(this.host.getBroomInventory()) || !InventoryUtilities.canMergeHappen(this.host.getBroomInventory(), inventory))) {
                this.resetTask();
            }
        }
    }

}
