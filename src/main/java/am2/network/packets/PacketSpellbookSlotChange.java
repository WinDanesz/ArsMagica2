package am2.network.packets;

import am2.common.items.ItemSpellBook;
import am2.common.registry.AMItems;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to change the active spell slot in a spellbook.
 * Sent from client to server when the player scrolls through spells.
 * <p>
 * Direction: Client -> Server
 */
public class PacketSpellbookSlotChange extends AMPacket<PacketSpellbookSlotChange> {

    /**
     * Sub-IDs from ItemSpellBook
     */
    public static final byte ID_NEXT_SPELL = 0;
    public static final byte ID_PREV_SPELL = 1;

    private byte subId;
    private int inventorySlot;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketSpellbookSlotChange() {
    }

    /**
     * Create a new spellbook slot change packet.
     *
     * @param subId         The sub-ID (next or previous)
     * @param inventorySlot The inventory slot containing the spellbook
     */
    public PacketSpellbookSlotChange(byte subId, int inventorySlot) {
        this.subId = subId;
        this.inventorySlot = inventorySlot;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(subId);
        buf.writeInt(inventorySlot);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        subId = buf.readByte();
        inventorySlot = buf.readInt();
    }

    @Override
    protected void handleServerSide(PacketSpellbookSlotChange message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null) {
            ItemStack stack;
            if (message.inventorySlot == -1) {
                // Baubles charm slot
                stack = ItemSpellBook.findSpellBook(player);
            } else {
                // Validate inventory slot
                if (message.inventorySlot < 0 || message.inventorySlot >= player.inventory.getSizeInventory()) {
                    return;
                }
                stack = player.inventory.getStackInSlot(message.inventorySlot);
            }

            if (stack.isEmpty() || !(stack.getItem() instanceof ItemSpellBook)) {
                return;
            }

            // Handle slot change based on sub-ID
            if (message.subId == ItemSpellBook.ID_NEXT_SPELL) {
                ((ItemSpellBook) AMItems.spellbook).setNextSlot(stack);
            } else if (message.subId == ItemSpellBook.ID_PREV_SPELL) {
                ((ItemSpellBook) AMItems.spellbook).setPrevSlot(stack);
            }

            // In-place NBT edits don't trigger baubles' onContentsChanged,
            // so explicitly write the stack back to mark the slot dirty.
            if (message.inventorySlot == -1) {
                ItemSpellBook.writeSpellBookToBaubles(player, stack);
            }
        }
    }
}
