package am2.common.utils;

import am2.common.blocks.tileentity.TileEntityAstralBarrier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DimensionUtilities {

    private static final HashMap<Integer, ArrayList<TileEntityAstralBarrier>> barrierMap = new HashMap<>();

    public static void doDimensionTransfer(EntityLivingBase entity, int dimension) {

        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            new AMTeleporter(player.server.getWorld(dimension)).teleport(entity);
        } else {
            entity.world.profiler.startSection("changeDimension");
            MinecraftServer minecraftserver = FMLCommonHandler.instance().getMinecraftServerInstance();
            int j = entity.dimension;
            WorldServer worldserver = minecraftserver.getWorld(j);
            WorldServer worldserver1 = minecraftserver.getWorld(dimension);
            entity.dimension = dimension;
            entity.world.removeEntity(entity);
            entity.isDead = false;
            entity.world.profiler.startSection("reposition");
            minecraftserver.getPlayerList().transferEntityToWorld(entity, j, worldserver, worldserver1, new AMTeleporter(worldserver1));
            entity.world.profiler.endStartSection("reloading");
            Entity e = EntityList.createEntityByIDFromName(new ResourceLocation(EntityList.getEntityString(entity)), worldserver1);

            if (e != null) {
                e.readFromNBT(entity.writeToNBT(new NBTTagCompound()));
                worldserver1.spawnEntity(e);
            }

            entity.isDead = true;
            entity.world.profiler.endSection();
            worldserver.resetUpdateEntityTick();
            worldserver1.resetUpdateEntityTick();
            entity.world.profiler.endSection();
        }
    }

    public static void registerAstralBarrier(TileEntityAstralBarrier barrier) {
        int dimension = barrier.getWorld().provider.getDimension();
        ArrayList<TileEntityAstralBarrier> barriers = barrierMap.get(dimension);
        if (barriers == null) {
            barriers = new ArrayList<>();
            barrierMap.put(dimension, barriers);
        }
        if (!barriers.contains(barrier))
            barriers.add(barrier);
    }

    public static void invalidateAstralBarrier(TileEntityAstralBarrier barrier) {
        int dimension = barrier.getWorld().provider.getDimension();
        ArrayList<TileEntityAstralBarrier> barriers = barrierMap.get(dimension);
        if (barriers == null)
            return;
        if (barriers.contains(barrier))
            barriers.remove(barrier);
    }

    public static void cleanupDimension(int dimension) {
        barrierMap.remove(dimension);
    }

    public static TileEntityAstralBarrier GetBlockingAstralBarrier(World world, BlockPos pos, ArrayList<Long> keys) {
        int dimension = world.provider.getDimension();
        ArrayList<TileEntityAstralBarrier> barriers = barrierMap.get(dimension);
        if (barriers == null) {
            barriers = new ArrayList<>();
            barrierMap.put(dimension, barriers);
        }
        AxisAlignedBB aabb = new AxisAlignedBB(pos).expand(20.01, 0, 0); // .01 Because isVecInside is a strict inequality.
        Iterator<TileEntityAstralBarrier> iter = barriers.iterator();
        while (iter.hasNext()) {
            TileEntityAstralBarrier barrier = iter.next();
            if (barrier == null || barrier.getPos() == null) {
                //Invalid TE
                iter.remove();
                continue;
            }

            //Checks if any of the keystone in inventory are valid for the barrier.
            long barrierKey = KeystoneUtilities.instance.getKeyFromRunes(barrier.getRunesInKey());
            if ((barrierKey != 0 && keys.contains(barrierKey)) || !barrier.IsActive())
                continue;

            if (aabb.contains(new Vec3d(barrier.getPos())))
                return barrier;
        }
        //Old code
        //check for Astral Barrier
//		for (int i = -20; i <= 20; ++i){
//			for (int j = -20; j <= 20; ++j){
//				for (int k = -20; k <= 20; ++k){
//					if (world.getBlockState(pos.add(i, j, k)).getBlock() == BlockDefs.astralBarrier){
//
//						TileEntity te = world.getTileEntity(pos.add(i, j, k));
//						if (te == null || !(te instanceof TileEntityAstralBarrier)){
//							continue;
//						}
//						TileEntityAstralBarrier barrier = (TileEntityAstralBarrier)te;
//
//						long barrierKey = KeystoneUtilities.instance.getKeyFromRunes(barrier.getRunesInKey());
//						if ((barrierKey != 0 && keys.contains(barrierKey)) || !barrier.IsActive()) continue;
//
//						int sqDist = (int) pos.distanceSq(barrier.getPos());
//
//						if (sqDist < (barrier.getRadius() * barrier.getRadius())) return barrier;
//					}
//				}
//			}
//		}
        return null;
    }
}
