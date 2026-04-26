package am2.common.defs;

import am2.api.blocks.IKeystoneLockable;
import am2.api.math.AMVector3;
import am2.common.blocks.tileentity.TileEntityKeystoneReceptacle;
import am2.common.utils.KeystoneUtilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;

public class KeystoneLocatons {

    public static HashMap<Integer, ArrayList<AMVector3>> KeystonePortalLocations = new HashMap<>();

    public void registerKeystonePortal(BlockPos pos, int dimension) {
        AMVector3 location = new AMVector3(pos);
        if (!KeystonePortalLocations.containsKey(dimension))
            KeystonePortalLocations.put(dimension, new ArrayList<AMVector3>());

        ArrayList<AMVector3> dimensionList = KeystonePortalLocations.get(dimension);

        if (!dimensionList.contains(location))
            dimensionList.add(location);
    }

    public void removeKeystonePortal(BlockPos pos, int dimension) {
        AMVector3 location = new AMVector3(pos);
        if (KeystonePortalLocations.containsKey(dimension)) {
            ArrayList<AMVector3> dimensionList = KeystonePortalLocations.get(dimension);

            if (dimensionList.contains(location))
                dimensionList.remove(location);
        }
    }

    public AMVector3 getNextKeystonePortalLocation(World world, BlockPos pos, boolean multidimensional, long key) {
        AMVector3 current = new AMVector3(pos);
        if (!multidimensional) {
            AMVector3 next = getNextKeystoneLocationInWorld(world, pos, key);
            if (next == null)
                next = current;
            return next;
        } else {
            return current;
        }
    }

    public AMVector3 getNextKeystoneLocationInWorld(World world, BlockPos pos, long key) {
        AMVector3 location = new AMVector3(pos);
        ArrayList<AMVector3> dimensionList = KeystonePortalLocations.get(world.provider.getDimension());
        if (dimensionList == null || dimensionList.isEmpty()) {
            return null;
        }

        int index = dimensionList.indexOf(location);
        index++;
        if (index >= dimensionList.size()) index = 0;
        AMVector3 newLocation = dimensionList.get(index);
        for (int i = 0; i < dimensionList.size(); i++) {
            TileEntity te = world.getTileEntity(newLocation.toBlockPos());
            if (te != null && te instanceof TileEntityKeystoneReceptacle) {
                if (KeystoneUtilities.instance.getKeyFromRunes(((IKeystoneLockable<?>) te).getRunesInKey()) == key) {
                    return newLocation;
                }
            }
            index++;
            if (index >= dimensionList.size()) index = 0;
            newLocation = dimensionList.get(index);
        }

        return location;
    }

    public void resetKnownPortalLocations() {
        KeystonePortalLocations.clear();
    }
}
