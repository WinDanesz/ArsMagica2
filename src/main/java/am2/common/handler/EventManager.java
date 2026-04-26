package am2.common.handler;

import am2.common.blocks.tileentity.ITileEntityAMBase;
import am2.common.network.packets.MessageTEUpdate;
import am2.network.AMNetworkHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Growlith1223 on 3/4/2017.
 */
public class EventManager {
    public static List<TileEntity> toUpdate = new ArrayList<TileEntity>();

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isRemote) {
            List<TileEntity> tiles = event.world.loadedTileEntityList;
            NBTTagList list = new NBTTagList();
            for (TileEntity te : tiles) {
                if (te instanceof ITileEntityAMBase) {
                    if (((ITileEntityAMBase) te).needsUpdate()) {
                        ((ITileEntityAMBase) te).clean();
                        list.appendTag(te.getUpdateTag());
                    }
                }
            }

            for (TileEntity te : toUpdate) {
                list.appendTag(te.getUpdateTag());
            }
            toUpdate.clear();

            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("data", list);
            AMNetworkHandler.getNetwork().sendToAll(new MessageTEUpdate(tag));
        }
    }
}
