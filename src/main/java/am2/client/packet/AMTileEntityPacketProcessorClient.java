package am2.client.packet;

import am2.common.LogHelper;
import am2.common.blocks.tileentity.ITileEntityPacketSync;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMTileEntityNetHandler;
import am2.common.packet.AMTileEntityPacketProcessorServer;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.relauncher.Side;

public class AMTileEntityPacketProcessorClient extends AMTileEntityPacketProcessorServer {
	 
	@SubscribeEvent 
	public void onPacketData(ClientCustomPacketEvent event) {
		ByteBufInputStream bbis = new ByteBufInputStream(event.getPacket().payload());
		byte packetID = -1;
		try{
			if (event.getPacket().getTarget() != Side.CLIENT){
				return;
			}
			//constant details all packets share:  ID, player, and remaining data
			packetID = bbis.readByte();
			EntityPlayer player = Minecraft.getMinecraft().player;
			World world = player.world;
			byte[] remaining = new byte[bbis.available()];
			bbis.readFully(remaining);
			AMDataReader reader = new AMDataReader(remaining, false);
			BlockPos target = new BlockPos(reader.getInt(), reader.getInt(), reader.getInt());
			TileEntity te = world.getTileEntity(target);
			if (te != null && te instanceof ITileEntityPacketSync) {
				ITileEntityPacketSync sync = (ITileEntityPacketSync)te;
				if (sync.handleSyncPacket(reader.getRemainingBytes()))
					AMTileEntityNetHandler.INSTANCE.sendPacketToServer(packetID, target, new byte[0]);
			}
		}catch (Throwable t){
			LogHelper.error("Client Packet Failed to Handle!");
			LogHelper.error("Packet Type: (Tile Entity) " + packetID);
			t.printStackTrace();
		}finally{
			try{
				if (bbis != null)
					bbis.close();
			}catch (Throwable t){
				t.printStackTrace();
			}
		}
	}
}
