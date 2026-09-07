package am2.common.blocks;

import am2.common.container.ContainerEssenceRefiner;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CraftingEssenceExtractorTest {
    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    @Test
    void emptySlotsRemainSafeAfterExtractionAndClearing() {
        ContainerEssenceRefiner container = new ContainerEssenceRefiner(new InventoryPlayer(null), null) {
            @Override
            public void onCraftMatrixChanged(IInventory inventory) {
                // Exercise the inventory without a world or a refiner tile.
            }
        };
        CraftingEssenceExtractor inventory = new CraftingEssenceExtractor(container);
        assertEmptySlots(inventory);
        assertTrue(inventory.decrStackSize(0, 1).isEmpty());
        assertTrue(inventory.removeStackFromSlot(0).isEmpty());

        inventory.setInventorySlotContents(0, new ItemStack(Items.APPLE, 3));
        assertFalse(inventory.isEmpty());
        assertTrue(inventory.decrStackSize(0, 0).isEmpty());
        assertEquals(3, inventory.getStackInSlot(0).getCount());
        assertEquals(2, inventory.decrStackSize(0, 2).getCount());
        assertEquals(1, inventory.removeStackFromSlot(0).getCount());
        assertEmptySlots(inventory);

        inventory.setInventorySlotContents(1, new ItemStack(Items.APPLE));
        assertEquals(1, inventory.decrStackSize(1, 5).getCount());
        assertEmptySlots(inventory);
        inventory.setInventorySlotContents(2, new ItemStack(Items.APPLE));
        inventory.clear();
        assertEmptySlots(inventory);
    }

    private static void assertEmptySlots(CraftingEssenceExtractor inventory) {
        assertTrue(inventory.isEmpty());
        assertEquals(5, inventory.getSizeInventory());
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            assertNotNull(inventory.getStackInSlot(i));
            assertTrue(inventory.getStackInSlot(i).isEmpty());
        }
    }
}
