package am2.common.power;

import am2.api.power.IPowerNode;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

public class PowerNodePathfinder extends AStar<BlockPos> {

    private World world;
    private BlockPos end;
    private PowerTypes powerType;

    PowerNodePathfinder(World world, BlockPos start, BlockPos end, PowerTypes type) {
        this.world = world;
        this.end = end;
        this.powerType = type;
    }

    private IPowerNode<?> getPowerNode(World world, BlockPos location) {
        if (world.getChunk(new BlockPos(location)) != null) {
            Chunk chunk = world.getChunk(new BlockPos(location));
            if (chunk.isLoaded()) {
                TileEntity te = world.getTileEntity(new BlockPos(location));
                if (te instanceof IPowerNode)
                    return (IPowerNode<?>) te;
            }
        }
        return null;
    }

    @Override
    protected boolean isGoal(BlockPos node) {
        return node.equals(end);
    }

    @Override
    protected Double g(BlockPos from, BlockPos to) {
        return from.distanceSq(to);
    }

    @Override
    protected Double h(BlockPos from, BlockPos to) {
        return from.distanceSq(to);
    }

    @Override
    protected List<BlockPos> generateSuccessors(BlockPos node) {
        IPowerNode<?> powerNode = getPowerNode(world, node);
        if (powerNode == null)
            return new ArrayList<BlockPos>();

        IPowerNode<?>[] candidates = PowerNodeRegistry.For(world).getAllNearbyNodes(world, node, powerType);

        ArrayList<BlockPos> prunedCandidates = new ArrayList<BlockPos>();
        for (IPowerNode<?> candidate : candidates) {
            if (verifyCandidate(candidate)) {
                prunedCandidates.add(new BlockPos(((TileEntity) candidate).getPos()));
            }
        }

        return prunedCandidates;
    }

    private boolean verifyCandidate(IPowerNode<?> powerNode) {
        if (((TileEntity) powerNode).getPos().equals(end)) {
            for (PowerTypes type : powerNode.getValidPowerTypes())
                if (type == powerType)
                    return true;
        }
        return powerNode.canRelayPower(powerType);
    }
}
