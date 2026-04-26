package am2.network.packets;

import am2.common.container.ContainerMagiciansWorkbench;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to set a recipe in the Magician's Workbench.
 * Sent from client to server when the player selects a recipe.
 * <p>
 * Direction: Client -> Server
 */
public class PacketWorkbenchSetRecipe extends AMPacket<PacketWorkbenchSetRecipe> {

    private int recipeIndex;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketWorkbenchSetRecipe() {
    }

    /**
     * Create a new workbench set recipe packet.
     *
     * @param recipeIndex The recipe index to set
     */
    public PacketWorkbenchSetRecipe(int recipeIndex) {
        this.recipeIndex = recipeIndex;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(recipeIndex);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        recipeIndex = buf.readInt();
    }

    @Override
    protected void handleServerSide(PacketWorkbenchSetRecipe message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null && player.openContainer instanceof ContainerMagiciansWorkbench) {
            // Validate recipe index
            if (message.recipeIndex >= 0 && message.recipeIndex < 1000) {
                ((ContainerMagiciansWorkbench) player.openContainer).moveRecipeToCraftingGrid(message.recipeIndex);
            }
        }
    }
}
