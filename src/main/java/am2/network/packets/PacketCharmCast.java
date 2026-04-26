package am2.network.packets;

import am2.api.extensions.ISpellCaster;
import am2.common.items.ItemSpellBook;
import am2.common.spell.SpellCaster;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet sent from the client to cast the active spell from the baubles charm
 * slot spell book.  Supports both instant and channeled spells via a simple
 * action byte: {@link #BEGIN}, {@link #TICK}, or {@link #STOP}.
 * <p>
 * Direction: Client -> Server
 */
public class PacketCharmCast extends AMPacket<PacketCharmCast> {

    /** First press – cast instant spells, or start channeled casting. */
    public static final byte BEGIN = 0;
    /** Held each tick – continues channeled spells. */
    public static final byte TICK  = 1;
    /** Released – stop channeled casting. */
    public static final byte STOP  = 2;

    private byte action;

    public PacketCharmCast() {}

    public PacketCharmCast(byte action) {
        this.action = action;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(action);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = buf.readByte();
    }

    @Override
    protected void handleServerSide(PacketCharmCast message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player == null) return;

        // Only look in baubles – if book is in main hand, normal right-click handles it
        if (!ItemSpellBook.isSpellBookInBaubles(player)) return;

        ItemStack bookStack = ItemSpellBook.findSpellBook(player);
        if (bookStack.isEmpty() || !(bookStack.getItem() instanceof ItemSpellBook)) return;

        ItemSpellBook book = (ItemSpellBook) bookStack.getItem();
        ItemStack spellStack = book.getActiveItemStack(bookStack);
        if (spellStack.isEmpty() || !spellStack.hasCapability(SpellCaster.INSTANCE, null)) return;

        ISpellCaster caster = SpellCaster.of(spellStack);
        boolean channeled = caster.createSpellData(spellStack).isChanneled();

        switch (message.action) {
            case BEGIN:
                // For both instant and channeled: cast once on begin
                caster.cast(spellStack, player.world, player);
                break;
            case TICK:
                // Only channeled spells get tick casts
                if (channeled) {
                    caster.cast(spellStack, player.world, player);
                }
                break;
            case STOP:
                // Nothing needed on stop – just stops the client from sending TICK
                break;
            default:
                break;
        }
    }
}
