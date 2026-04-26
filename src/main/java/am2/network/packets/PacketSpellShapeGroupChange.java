package am2.network.packets;

import am2.common.items.ItemSpellBook;
import am2.common.registry.AMItems;
import am2.common.spell.SpellCaster;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to change spell shape group (casting mode).
 * Sent from client to server when the player changes their spell shape group.
 * <p>
 * Direction: Client -> Server
 */
public class PacketSpellShapeGroupChange extends AMPacket<PacketSpellShapeGroupChange> {

    private int shapeGroupOrdinal;
    private int entityId;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketSpellShapeGroupChange() {
    }

    /**
     * Create a new spell shape group change packet.
     *
     * @param shapeGroupOrdinal The ordinal of the shape group to set
     * @param entityId          The entity ID (typically the player)
     */
    public PacketSpellShapeGroupChange(int shapeGroupOrdinal, int entityId) {
        this.shapeGroupOrdinal = shapeGroupOrdinal;
        this.entityId = entityId;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(shapeGroupOrdinal);
        buf.writeInt(entityId);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        shapeGroupOrdinal = buf.readInt();
        entityId = buf.readInt();
    }

    @Override
    protected void handleServerSide(PacketSpellShapeGroupChange message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null) {
            // Validate shape group ordinal (reasonable range check)
            if (message.shapeGroupOrdinal < 0 || message.shapeGroupOrdinal > 10) {
                return;
            }

            ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
            // If main hand doesn't have a spell or spell book, check baubles
            if (stack.isEmpty() || (stack.getItem() != AMItems.spell && !(stack.getItem() instanceof ItemSpellBook))) {
                stack = ItemSpellBook.findSpellBook(player);
            }
            if (!stack.isEmpty()) {
                if (stack.getItem() == AMItems.spell && stack.hasCapability(SpellCaster.INSTANCE, null)) {
                    // Update spell item's shape group in the capability.
                    SpellCaster.of(stack)
                            .setCurentShapeGroup(message.shapeGroupOrdinal);
                    // Minecraft's slot change detection only compares tagCompound, not
                    // capabilities.  Writing the group into tagCompound makes
                    // detectAndSendChanges() see a change and call getNBTShareTag(),
                    // which embeds the full updated capability and syncs it to the client.
                    NBTTagCompound tag = stack.getTagCompound();
                    if (tag == null) tag = new NBTTagCompound();
                    tag.setInteger("CurrentShapeGroup", message.shapeGroupOrdinal);
                    stack.setTagCompound(tag);
                } else if (stack.getItem() == AMItems.spellbook || stack.getItem() == AMItems.arcane_spellbook) {
                    // Update spellbook's active spell's shape group
                    ItemStack spellStack = ((ItemSpellBook) stack.getItem()).getActiveItemStack(stack);
                    if (spellStack.hasCapability(SpellCaster.INSTANCE, null)) {
                        SpellCaster.of(spellStack)
                                .setCurentShapeGroup(message.shapeGroupOrdinal);
                        ((ItemSpellBook) stack.getItem()).replaceActiveItemStack(stack, spellStack);
                        // If the book came from baubles, write it back to trigger sync
                        if (ItemSpellBook.isSpellBookInBaubles(player)) {
                            ItemSpellBook.writeSpellBookToBaubles(player, stack);
                        }
                    }
                }
            }
        }
    }
}
