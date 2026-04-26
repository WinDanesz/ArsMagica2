package am2.network.packets;

import am2.common.blocks.tileentity.TileEntityMagiciansWorkbench;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to lock/unlock a recipe in the Magician's Workbench.
 * Sent from client to server when the player locks or unlocks a recipe.
 * <p>
 * Direction: Client -> Server
 */
public class PacketWorkbenchLockRecipe extends AMPacket<PacketWorkbenchLockRecipe> {

    private BlockPos pos;
    private int recipeIndex;
    private boolean locked;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketWorkbenchLockRecipe() {
    }

    /**
     * Create a new workbench lock recipe packet.
     *
     * @param pos         The position of the workbench
     * @param recipeIndex The recipe index to lock/unlock
     * @param locked      True to lock, false to unlock
     */
    public PacketWorkbenchLockRecipe(BlockPos pos, int recipeIndex, boolean locked) {
        this.pos = pos;
        this.recipeIndex = recipeIndex;
        this.locked = locked;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(recipeIndex);
        buf.writeBoolean(locked);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        pos = new BlockPos(x, y, z);
        recipeIndex = buf.readInt();
        locked = buf.readBoolean();
    }

    @Override
    protected void handleServerSide(PacketWorkbenchLockRecipe message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null) {
            // Validate recipe index
            if (message.recipeIndex >= 0 && message.recipeIndex < 100) {
                TileEntity te = player.world.getTileEntity(message.pos);
                if (te instanceof TileEntityMagiciansWorkbench) {
                    ((TileEntityMagiciansWorkbench) te).setRecipeLocked(message.recipeIndex, message.locked);
                    te.getWorld().markAndNotifyBlock(te.getPos(), te.getWorld().getChunk(te.getPos()),
                            te.getWorld().getBlockState(te.getPos()), te.getWorld().getBlockState(te.getPos()), 2);
                }
            }
        }
    }
}
