package am2.common.world;

import am2.ArsMagica;
import am2.common.LogHelper;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMLoot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.Random;

/**
 * Generates Lost Archive structures underground. These are small arcane library rooms
 * sealed behind magic wall blocks, buried in stone with their entrance slightly exposed
 * into a cave or open space so they can be discovered.
 */
public class WorldGenLostArchive extends WorldGenerator {

    private static final ResourceLocation[] STRUCTURE_FILES = {
            new ResourceLocation(ArsMagica.MODID, "lost_archive_1"),
            new ResourceLocation(ArsMagica.MODID, "lost_archive_2")
    };

    private static final int MAX_ENTRANCES = 5;

    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) return false;

        ResourceLocation structureFile = STRUCTURE_FILES[rand.nextInt(STRUCTURE_FILES.length)];

        TemplateManager templateManager = world.getSaveHandler().getStructureTemplateManager();
        Template template = templateManager.getTemplate(server, structureFile);

        if (template.getSize().getX() == 0 || template.getSize().getY() == 0 || template.getSize().getZ() == 0) {
            LogHelper.warn("Lost Archive structure template %s is missing or empty!", structureFile);
            return false;
        }

        Rotation rotation = Rotation.values()[rand.nextInt(Rotation.values().length)];
        Mirror mirror = rand.nextBoolean() ? Mirror.NONE : Mirror.LEFT_RIGHT;

        PlacementSettings settings = new PlacementSettings()
                .setRotation(rotation)
                .setMirror(mirror)
                .setIgnoreEntities(false);

        BlockPos size = template.transformedSize(rotation);

        // Pick a position underground (Y 20-50), offset by 8 to minimize cascading worldgen lag
        int originX = pos.getX() - size.getX() / 2;
        int originY = 20 + rand.nextInt(31); // Y 20-50
        int originZ = pos.getZ() - size.getZ() / 2;
        BlockPos origin = new BlockPos(originX, originY, originZ);

        // Validate the position: floor must be solid, need at least 1 entrance (but not too many)
        BlockPos corner = origin.add(size.getX(), 1, size.getZ());

        int entrances = 0;

        for (BlockPos checkPos : BlockPos.getAllInBox(origin, corner)) {
            // Floor layer must be solid
            if (checkPos.getY() == origin.getY() && !world.getBlockState(checkPos).getMaterial().isSolid()) {
                return false;
            }

            // Count 2-high air openings on the perimeter (cave entrances)
            if ((checkPos.getX() == origin.getX() || checkPos.getX() == corner.getX()
                    || checkPos.getZ() == origin.getZ() || checkPos.getZ() == corner.getZ())
                    && checkPos.getY() == origin.getY() + 1
                    && world.isAirBlock(checkPos) && world.isAirBlock(checkPos.up())) {
                if (++entrances > MAX_ENTRANCES) return false;
            }
        }

        if (entrances == 0) return false;

        // Adjust origin for rotation/mirror
        origin = template.getZeroPositionWithTransform(origin, mirror, rotation);

        // Place the structure
        settings.setIntegrity(1.0F);
        template.addBlocksToWorld(world, origin, settings);

        // Expose the magic wall entrance: clear blocks in front of magic wall door blocks
        // so the structure is discoverable. We scan the placed area for magic wall blocks
        // and clear a small area in front of them.
        exposeDoorEntrance(world, origin, size, rotation);

        // Set loot tables for any chests in the structure
        setLootForChests(world, origin, size, rand);

        LogHelper.info("Generated Lost Archive '%s' at %s", structureFile, origin);

        return true;
    }

    /**
     * Finds magic_wall blocks in the structure and clears a small exposed area in front of them,
     * digging towards the nearest exterior face so the entrance is visible from a cave or tunnel.
     */
    private void exposeDoorEntrance(World world, BlockPos origin, BlockPos size, Rotation rotation) {
        IBlockState magicWallState = AMBlocks.magic_wall.getDefaultState();

        int minX = origin.getX();
        int minY = origin.getY();
        int minZ = origin.getZ();
        int maxX = minX + Math.abs(size.getX());
        int maxY = minY + Math.abs(size.getY());
        int maxZ = minZ + Math.abs(size.getZ());

        for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ))) {
            if (world.getBlockState(pos).getBlock() == AMBlocks.magic_wall) {
                // Found a magic wall block - determine which face is closest to the exterior
                // and clear blocks outward from it to create a visible approach
                int distMinX = pos.getX() - minX;
                int distMaxX = maxX - pos.getX();
                int distMinZ = pos.getZ() - minZ;
                int distMaxZ = maxZ - pos.getZ();

                int minDist = Math.min(Math.min(distMinX, distMaxX), Math.min(distMinZ, distMaxZ));

                int dx = 0, dz = 0;
                if (minDist == distMinX) dx = -1;
                else if (minDist == distMaxX) dx = 1;
                else if (minDist == distMinZ) dz = -1;
                else dz = 1;

                // Clear a 1-wide, 2-tall passage outward for 3-5 blocks to expose the entrance
                for (int depth = 1; depth <= 4; depth++) {
                    BlockPos clearPos = pos.add(dx * depth, 0, dz * depth);
                    BlockPos clearPosUp = clearPos.up();

                    // Only clear solid blocks (stone, dirt, etc.), stop at air/caves
                    if (world.isAirBlock(clearPos) && world.isAirBlock(clearPosUp)) {
                        break; // Already open, we've connected to a cave
                    }

                    IBlockState state = world.getBlockState(clearPos);
                    IBlockState stateUp = world.getBlockState(clearPosUp);

                    // Don't clear bedrock or other structures
                    if (state.getBlockHardness(world, clearPos) < 0) break;

                    if (state.getMaterial().isSolid()) {
                        world.setBlockState(clearPos, Blocks.AIR.getDefaultState(), 2);
                    }
                    if (stateUp.getMaterial().isSolid() && stateUp.getBlockHardness(world, clearPosUp) >= 0) {
                        world.setBlockState(clearPosUp, Blocks.AIR.getDefaultState(), 2);
                    }
                }

                // Only process the first magic wall column we find (the door)
                // The door is 2 blocks tall, so skip any remaining magic wall blocks
                return;
            }
        }
    }

    /**
     * Scans the structure area for chest blocks and assigns the lost archive loot table.
     */
    private void setLootForChests(World world, BlockPos origin, BlockPos size, Random rand) {
        int minX = origin.getX();
        int minY = origin.getY();
        int minZ = origin.getZ();
        int maxX = minX + Math.abs(size.getX());
        int maxY = minY + Math.abs(size.getY());
        int maxZ = minZ + Math.abs(size.getZ());

        for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ))) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityLockableLoot) {
                ((TileEntityLockableLoot) te).setLootTable(AMLoot.LOST_ARCHIVE_LOOT, rand.nextLong());
            }
        }
    }
}
