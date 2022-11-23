/**
 *
 */
package am2.common.container;

import am2.common.blocks.tileentity.TileEntityInertSpawner;
import am2.common.container.slot.AM2Container;
import am2.common.container.slot.SlotSpecifiedItemsOnly;
import am2.common.defs.ItemDefs;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author Mithion
 */
public class ContainerInertSpawner extends AM2Container{

	private TileEntityInertSpawner spawner;

	public ContainerInertSpawner(EntityPlayer player, TileEntityInertSpawner habitat){
		this.spawner = habitat;
		SlotSpecifiedItemsOnly slot;

		slot = new SlotSpecifiedItemsOnly(habitat, 0, 79, 47, ItemDefs.crystalPhylactery);

		slot.setMaxStackSize(1);
		addSlotToContainer(slot);

		this.addPlayerInventory(player, 8, 84);
		this.addPlayerActionBar(player, 8, 143);

	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer){
		return this.spawner.isUsableByPlayer(entityplayer);
	}

}
