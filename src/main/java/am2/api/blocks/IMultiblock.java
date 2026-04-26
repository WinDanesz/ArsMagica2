package am2.api.blocks;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public interface IMultiblock {
    ArrayList<List<IMultiblockGroup>> getMultiblockGroups();

    boolean matches(World world, BlockPos pos);

    List<IMultiblockGroup> getMatchingGroups(World world, BlockPos startCheckPos);

    void addGroup(IMultiblockGroup group, IMultiblockGroup... rest);

    int getMinX();

    int getMinY();

    int getMinZ();

    int getMaxX();

    int getMaxY();

    int getMaxZ();

    int getWidth();

    int getLength();

    int getHeight();

    ResourceLocation getID();
}
