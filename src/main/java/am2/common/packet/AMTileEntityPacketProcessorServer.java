package am2.common.packet;

import am2.common.LogHelper;
import am2.common.blocks.tileentity.ITileEntityPacketSync;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.relauncher.Side;

public class AMTileEntityPacketProcessorServer {
	
	@SubscribeEvent
	public void onServerPacketData(ServerCustomPacketEvent event) {
		byte packetID = -1;
		try (ByteBufInputStream bbis = new ByteBufInputStream(event.getPacket().payload())){
			if (event.getPacket().getTarget() != Side.SERVER){
				return;
			}

			packetID = bbis.readByte();
			NetHandlerPlayServer srv = (NetHandlerPlayServer)event.getPacket().handler();
			EntityPlayerMP player = srv.player;
			World world = player.world;
			byte[] remaining = new byte[bbis.available()];
			bbis.readFully(remaining);
			AMDataReader reader = new AMDataReader(remaining, false);
			BlockPos target = new BlockPos(reader.getInt(), reader.getInt(), reader.getInt());
			TileEntity te = world.getTileEntity(target);
			if (te != null && te instanceof ITileEntityPacketSync) {
				ITileEntityPacketSync sync = (ITileEntityPacketSync)te;
				sync.confirm();
			}
		}catch (Throwable t){
			LogHelper.error("Server Packet Failed to Handle!");
			LogHelper.error("Packet Type: " + packetID);
			t.printStackTrace();
		}
	}

}
