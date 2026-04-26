package am2.api.blocks;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Multiblock implements IMultiblock {

    public ArrayList<List<IMultiblockGroup>> groups;
    protected ResourceLocation id;

    public Multiblock(ResourceLocation id) {
        groups = new ArrayList<>();
        this.id = id;
    }

    public Multiblock(String id) {
        this(new ResourceLocation(id));
    }

    @Override
    public void addGroup(IMultiblockGroup group, IMultiblockGroup... rest) {
        groups.add(Lists.asList(group, rest));
    }

    @Override
    public boolean matches(World world, BlockPos startCheckPos) {
        boolean subFlag = true;
        for (List<IMultiblockGroup> subGroup : groups) {
            boolean groupCheck = false;
            boolean hasCheck = false;
            for (IMultiblockGroup group : subGroup) {
                hasCheck = true;
                groupCheck |= group.matches(world, startCheckPos);
            }
            if (hasCheck)
                subFlag &= groupCheck;
        }
        return subFlag;
    }

    @Override
    public List<IMultiblockGroup> getMatchingGroups(World world, BlockPos startCheckPos) {
        List<IMultiblockGroup> list = new ArrayList<>();
        for (List<IMultiblockGroup> subGroup : groups) {
            for (IMultiblockGroup group : subGroup) {
                if (group.matches(world, startCheckPos)) {
                    list.add(group);
                }
            }
        }
        return list;
    }

    public HashMap<BlockPos, List<IBlockState>> getStructureLayer(MultiblockGroup selected, int layer) {
        HashMap<BlockPos, List<IBlockState>> stateMap = new HashMap<>();
        for (BlockPos entry : selected.getPositions()) {
            if (entry.getY() == layer)
                stateMap.put(entry, selected.getStates());
        }
        return stateMap;
    }

    @Override
    public int getMinX() {
        int min = Integer.MAX_VALUE;
        for (List<IMultiblockGroup> group : groups) {
            for (IMultiblockGroup gr : group) {
                if (gr.getMinX() < min)
                    min = gr.getMinX();
            }
        }
        return min;
    }

    @Override
    public int getMinY() {
        int min = Integer.MAX_VALUE;
        for (List<IMultiblockGroup> group : groups) {
            for (IMultiblockGroup gr : group) {
                if (gr.getMinY() < min)
                    min = gr.getMinY();
            }
        }
        return min;
    }

    @Override
    public int getMinZ() {
        int min = Integer.MAX_VALUE;
        for (List<IMultiblockGroup> group : groups) {
            for (IMultiblockGroup gr : group) {
                if (gr.getMinZ() < min)
                    min = gr.getMinZ();
            }
        }
        return min;
    }

    @Override
    public int getMaxX() {
        int max = Integer.MIN_VALUE;
        for (List<IMultiblockGroup> group : groups) {
            for (IMultiblockGroup gr : group) {
                if (gr.getMaxX() > max)
                    max = gr.getMaxX();
            }
        }
        return max;
    }

    @Override
    public int getMaxY() {
        int max = Integer.MIN_VALUE;
        for (List<IMultiblockGroup> group : groups) {
            for (IMultiblockGroup gr : group) {
                if (gr.getMaxY() > max)
                    max = gr.getMaxY();
            }
        }
        return max;
    }

    @Override
    public int getMaxZ() {
        int max = Integer.MIN_VALUE;
        for (List<IMultiblockGroup> group : groups) {
            for (IMultiblockGroup gr : group) {
                if (gr.getMaxZ() > max)
                    max = gr.getMaxZ();
            }
        }
        return max;
    }

    @Override
    public int getWidth() {
        return getMaxX() - getMinX();
    }

    @Override
    public int getLength() {
        return getMaxZ() - getMinZ();
    }

    @Override
    public int getHeight() {
        return getMaxY() - getMinY();
    }

    @Override
    public ArrayList<List<IMultiblockGroup>> getMultiblockGroups() {
        return groups;
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }
}
