package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.api.CraftingAltarMaterials;
import am2.api.SpellRegistryHelper;
import am2.api.blocks.*;
import am2.api.extensions.ISpellCaster;
import am2.api.extensions.ISkillData;
import am2.api.power.IPowerNode;
import am2.api.spell.SpellPart;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleApproachPoint;
import am2.client.particles.ParticleFadeOut;
import am2.client.particles.ParticleMoveOnHeading;
import am2.common.blocks.BlockLectern;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.compat.electroblob.item.ItemEBWizSpellBinding;
import am2.common.extensions.SkillData;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.skill.Discipline;
import am2.common.spell.SpellCaster;
import am2.common.spell.component.Summon;
import am2.common.spell.shape.Binding;
import am2.common.utils.KeyValuePair;
import am2.common.utils.NBTUtils;
import am2.common.utils.SpellUtils;
import am2.common.registry.AMSounds;
import am2.network.AMNetworkHandler;
import am2.network.packets.PacketCraftingAltarSync;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLever.EnumOrientation;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStairs.EnumHalf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class TileEntityCraftingAltar extends TileEntityAMPower implements IMultiblockController, ITileEntityAMBase {

    private IMultiblock primary = new Multiblock("craftingAltar_alt");
    private IMultiblock secondary = new Multiblock("craftingAltar");

    private TypedMultiblockGroup out;
    private TypedMultiblockGroup out_alt;
    private TypedMultiblockGroup catalysts;
    private TypedMultiblockGroup catalysts_alt;
    private HashMap<IBlockState, Integer> capsPower = new HashMap<>();
    private HashMap<IBlockState, Integer> structurePower = new HashMap<>();

    private static final int BLOCKID = 0;
    private static final int STAIR_NORTH = 1;
    private static final int STAIR_SOUTH = 2;
    private static final int STAIR_EAST = 3;
    private static final int STAIR_WEST = 4;
    private static final int STAIR_NORTH_INVERTED = 5;
    private static final int STAIR_SOUTH_INVERTED = 6;
    private static final int STAIR_EAST_INVERTED = 7;
    private static final int STAIR_WEST_INVERTED = 8;


    private boolean isCrafting;
    private final ArrayList<ItemStack> allAddedItems;
    private final ArrayList<ItemStack> currentAddedItems;

    private final ArrayList<SpellPart> spellDef;
    private final NBTTagCompound savedData = new NBTTagCompound();
    private final ArrayList<KeyValuePair<ArrayList<SpellPart>, NBTTagCompound>> shapeGroups;
//	private boolean allShapeGroupsAdded = false;

    private int currentKey = -1;
    private int checkCounter;
    private boolean structureValid;
    private BlockPos podiumLocation;
    private BlockPos switchLocation;
    private int maxEffects;
    private boolean dirty = false;

    private ItemStack addedPhylactery = ItemStack.EMPTY;
    private ItemStack addedBindingCatalyst = ItemStack.EMPTY;

    /**
     * When the Crafting Altar is in the middle of finalizing an EBWiz binding
     * book, this holds the book so the correct binding item can be spawned at
     * the end instead of a normal AM2 spell.
     */
    private ItemStack ebwizPendingBook = ItemStack.EMPTY;

    private ItemStack[] spellGuide;
    private int[] outputCombo;
    private int[][] shapeGroupGuide;

    private int currentConsumedPower = 0;
    private int ticksExisted = 0;
    private ArrayList<PowerTypes> currentMainPowerTypes = Lists.newArrayList();
    /** Neutral etherium accumulated from the power network for EBWiz spell-book transcription. Reset when no EBWiz book is on the lectern. */
    private float ebwizAccumulatedPower = 0f;

    // ---- EBWiz Discovery Ritual state ----
    /** Whether a discovery ritual is currently in progress. */
    private boolean discoveryRitualActive = false;
    /** Discovery ritual phase: 0=ELEMENT, 1=TIER, 2=INGREDIENTS, 3=POWER. */
    private int discoveryPhase = 0;
    /** EBWiz Element ordinal (1–7) chosen for the current discovery ritual, or -1 if not yet chosen. */
    private int discoveryElementOrdinal = -1;
    /** EBWiz Tier ordinal (0–3) chosen for the current discovery ritual, or -1 if not yet chosen. */
    private int discoveryTierOrdinal = -1;
    /** Index into {@link #discoveryPlannedItems} for the next ingredient to consume. */
    private int discoveryIngredientIndex = 0;
    /** Ordered ingredient list built after element and tier are both selected. */
    private ItemStack[] discoveryPlannedItems = null;

    private static final byte CRAFTING_CHANGED = 1;
    private static final byte COMPONENT_ADDED = 2;
    private static final byte FULL_UPDATE = 3;

//	private static final int augmatl_mutex = 2;
//	private static final int lectern_mutex = 4;

    private String currentSpellName = "";
    private String lastCraftingPlayerName = "";
    private IBlockState mimicState;

    public TileEntityCraftingAltar() {
        super(ArsMagica.config.getCapacityCraftingAltar());
        setupMultiblock();
        allAddedItems = new ArrayList<ItemStack>();
        currentAddedItems = new ArrayList<ItemStack>();
        isCrafting = false;
        structureValid = false;
        checkCounter = 0;
        setNoPowerRequests();
        maxEffects = 2;

        spellDef = new ArrayList<>();
        shapeGroups = new ArrayList<>();

        for (int i = 0; i < 5; ++i) {
            shapeGroups.add(new KeyValuePair<ArrayList<SpellPart>, NBTTagCompound>(new ArrayList<>(), new NBTTagCompound()));
        }
    }

    private HashMap<Integer, IBlockState> createStateMap(IBlockState block, IBlockState stairs) {
        HashMap<Integer, IBlockState> map = new HashMap<>();
        map.put(BLOCKID, block);
        map.put(STAIR_NORTH, stairs.withProperty(BlockStairs.FACING, EnumFacing.NORTH));
        map.put(STAIR_SOUTH, stairs.withProperty(BlockStairs.FACING, EnumFacing.SOUTH));
        map.put(STAIR_EAST, stairs.withProperty(BlockStairs.FACING, EnumFacing.EAST));
        map.put(STAIR_WEST, stairs.withProperty(BlockStairs.FACING, EnumFacing.WEST));
        map.put(STAIR_NORTH_INVERTED, stairs.withProperty(BlockStairs.FACING, EnumFacing.NORTH).withProperty(BlockStairs.HALF, EnumHalf.TOP));
        map.put(STAIR_SOUTH_INVERTED, stairs.withProperty(BlockStairs.FACING, EnumFacing.SOUTH).withProperty(BlockStairs.HALF, EnumHalf.TOP));
        map.put(STAIR_EAST_INVERTED, stairs.withProperty(BlockStairs.FACING, EnumFacing.EAST).withProperty(BlockStairs.HALF, EnumHalf.TOP));
        map.put(STAIR_WEST_INVERTED, stairs.withProperty(BlockStairs.FACING, EnumFacing.WEST).withProperty(BlockStairs.HALF, EnumHalf.TOP));
        return map;
    }

    private static boolean isInOreDict(IBlockState state, String oreDictName) {
        Block block = state.getBlock();
        ItemStack stack = new ItemStack(block, 1, block.getMetaFromState(state));
        if (stack.isEmpty()) return false;
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (OreDictionary.getOreName(id).equals(oreDictName)) {
                return true;
            }
        }
        return false;
    }

    private static EnumFacing getStairFacingForGroup(int group) {
        switch (group) {
            case STAIR_NORTH: case STAIR_NORTH_INVERTED: return EnumFacing.NORTH;
            case STAIR_SOUTH: case STAIR_SOUTH_INVERTED: return EnumFacing.SOUTH;
            case STAIR_EAST:  case STAIR_EAST_INVERTED:  return EnumFacing.EAST;
            case STAIR_WEST:  case STAIR_WEST_INVERTED:  return EnumFacing.WEST;
            default: return null;
        }
    }

    private static boolean isStairGroupInverted(int group) {
        return group >= STAIR_NORTH_INVERTED;
    }

    /**
     * Oredict fallback matcher for the structure (out) group.
     * Accepts any "plankWood" oredict block for plank positions and any
     * {@link BlockStairs} for stair positions, enforcing that all planks
     * are the same block and all stairs are the same block.
     */
    private static boolean matchesOreDictWood(TypedMultiblockGroup outGroup, World w, BlockPos altarPos) {
        Block plankBlock = null;
        Block stairBlock = null;

        for (BlockPos pos : outGroup.getPositions()) {
            IBlockState checkState = w.getBlockState(altarPos.add(pos));
            int group = outGroup.getGroup(pos);

            if (group == BLOCKID) {
                if (!isInOreDict(checkState, "plankWood")) return false;
                if (plankBlock == null) plankBlock = checkState.getBlock();
                else if (plankBlock != checkState.getBlock()) return false;
            } else {
                if (!(checkState.getBlock() instanceof BlockStairs)) return false;
                if (stairBlock == null) stairBlock = checkState.getBlock();
                else if (stairBlock != checkState.getBlock()) return false;

                EnumFacing expectedFacing = getStairFacingForGroup(group);
                EnumHalf expectedHalf = isStairGroupInverted(group) ? EnumHalf.TOP : EnumHalf.BOTTOM;
                if (expectedFacing != null && checkState.getValue(BlockStairs.FACING) != expectedFacing) return false;
                if (checkState.getValue(BlockStairs.HALF) != expectedHalf) return false;
            }
        }
        return plankBlock != null;
    }

    private void setupMultiblock() {
        ArrayList<HashMap<Integer, IBlockState>> structureMaterials = new ArrayList<>();
        for (Entry<KeyValuePair<IBlockState, IBlockState>, Integer> entry : CraftingAltarMaterials.getMainMap().entrySet()) {
            structureMaterials.add(createStateMap(entry.getKey().key, entry.getKey().value));
            structurePower.put(entry.getKey().key, entry.getValue().intValue());
        }

        ArrayList<HashMap<Integer, IBlockState>> capsMaterials = new ArrayList<>();
        for (Entry<IBlockState, Integer> entry : CraftingAltarMaterials.getCapsMap().entrySet()) {
            HashMap<Integer, IBlockState> capMat = new HashMap<>();
            capMat.put(0, entry.getKey());
            capsMaterials.add(capMat);
            capsPower.put(entry.getKey(), entry.getValue().intValue());
        }

        catalysts = new TypedMultiblockGroup("catalysts", capsMaterials, false);
        out = new TypedMultiblockGroup("out", structureMaterials, false);
        out.setFallbackMatcher((w, p) -> matchesOreDictWood(out, w, p));
        MultiblockGroup altar = new MultiblockGroup("altar", Lists.newArrayList(AMBlocks.crafting_altar.getDefaultState()), true);

        catalysts.addBlock(new BlockPos(-1, 0, -2), 0);
        catalysts.addBlock(new BlockPos(1, 0, -2), 0);
        catalysts.addBlock(new BlockPos(-1, 0, 2), 0);
        catalysts.addBlock(new BlockPos(1, 0, 2), 0);
        catalysts.addBlock(new BlockPos(0, -4, 0), 0);

        out.addBlock(new BlockPos(-1, 0, -1), STAIR_EAST);
        out.addBlock(new BlockPos(-1, 0, 0), STAIR_EAST);
        out.addBlock(new BlockPos(-1, 0, 1), STAIR_EAST);
        out.addBlock(new BlockPos(1, 0, -1), STAIR_WEST);
        out.addBlock(new BlockPos(1, 0, 0), STAIR_WEST);
        out.addBlock(new BlockPos(1, 0, 1), STAIR_WEST);
        out.addBlock(new BlockPos(0, 0, -2), STAIR_SOUTH);
        out.addBlock(new BlockPos(0, 0, 2), STAIR_NORTH);
        out.addBlock(new BlockPos(-1, -1, -1), STAIR_NORTH_INVERTED);
        out.addBlock(new BlockPos(-1, -1, 1), STAIR_SOUTH_INVERTED);
        out.addBlock(new BlockPos(1, -1, -1), STAIR_NORTH_INVERTED);
        out.addBlock(new BlockPos(1, -1, 1), STAIR_SOUTH_INVERTED);

        out.addBlock(new BlockPos(0, 0, -1), 0);
        out.addBlock(new BlockPos(0, 0, 1), 0);
        out.addBlock(new BlockPos(1, -1, -2), 0);
        out.addBlock(new BlockPos(1, -1, 2), 0);
        out.addBlock(new BlockPos(-1, -1, -2), 0);
        out.addBlock(new BlockPos(-1, -1, 2), 0);
        out.addBlock(new BlockPos(1, -2, -2), 0);
        out.addBlock(new BlockPos(1, -2, 2), 0);
        out.addBlock(new BlockPos(-1, -2, -2), 0);
        out.addBlock(new BlockPos(-1, -2, 2), 0);
        out.addBlock(new BlockPos(1, -3, -2), 0);
        out.addBlock(new BlockPos(1, -3, 2), 0);
        out.addBlock(new BlockPos(-1, -3, -2), 0);
        out.addBlock(new BlockPos(-1, -3, 2), 0);
        out.addBlock(new BlockPos(-2, -4, -2), 0);
        out.addBlock(new BlockPos(-2, -4, -1), 0);
        out.addBlock(new BlockPos(-2, -4, 0), 0);
        out.addBlock(new BlockPos(-2, -4, 1), 0);
        out.addBlock(new BlockPos(-2, -4, 2), 0);
        out.addBlock(new BlockPos(-1, -4, -2), 0);
        out.addBlock(new BlockPos(-1, -4, -1), 0);
        out.addBlock(new BlockPos(-1, -4, 0), 0);
        out.addBlock(new BlockPos(-1, -4, 1), 0);
        out.addBlock(new BlockPos(-1, -4, 2), 0);
        out.addBlock(new BlockPos(0, -4, -2), 0);
        out.addBlock(new BlockPos(0, -4, -1), 0);
        out.addBlock(new BlockPos(0, -4, 1), 0);
        out.addBlock(new BlockPos(0, -4, 2), 0);
        out.addBlock(new BlockPos(1, -4, -2), 0);
        out.addBlock(new BlockPos(1, -4, -1), 0);
        out.addBlock(new BlockPos(1, -4, 0), 0);
        out.addBlock(new BlockPos(1, -4, 1), 0);
        out.addBlock(new BlockPos(1, -4, 2), 0);
        out.addBlock(new BlockPos(2, -4, -2), 0);
        out.addBlock(new BlockPos(2, -4, -1), 0);
        out.addBlock(new BlockPos(2, -4, 0), 0);
        out.addBlock(new BlockPos(2, -4, 1), 0);
        out.addBlock(new BlockPos(2, -4, 2), 0);

        MultiblockGroup wall = new MultiblockGroup("wall", Lists.newArrayList(AMBlocks.magic_wall.getDefaultState()), true);
        wall.addBlock(new BlockPos(0, -1, -2));
        wall.addBlock(new BlockPos(0, -2, -2));
        wall.addBlock(new BlockPos(0, -3, -2));
        wall.addBlock(new BlockPos(0, -1, 2));
        wall.addBlock(new BlockPos(0, -2, 2));
        wall.addBlock(new BlockPos(0, -3, 2));

        MultiblockGroup lever1 = new MultiblockGroup("lever1", Lists.newArrayList(
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.EAST),
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.EAST).withProperty(BlockLever.POWERED, true)
        ), false);
        MultiblockGroup lever2 = new MultiblockGroup("lever2", Lists.newArrayList(
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.EAST),
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.EAST).withProperty(BlockLever.POWERED, true)
        ), false);
        MultiblockGroup lever3 = new MultiblockGroup("lever3", Lists.newArrayList(
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.WEST),
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.WEST).withProperty(BlockLever.POWERED, true)
        ), false);
        MultiblockGroup lever4 = new MultiblockGroup("lever4", Lists.newArrayList(
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.WEST),
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.WEST).withProperty(BlockLever.POWERED, true)
        ), false);

        MultiblockGroup podium1 = new MultiblockGroup("podium1", Lists.newArrayList(
                AMBlocks.lectern.getDefaultState().withProperty(BlockLectern.FACING, EnumFacing.EAST)
        ), false);
        MultiblockGroup podium2 = new MultiblockGroup("podium2", Lists.newArrayList(
                AMBlocks.lectern.getDefaultState().withProperty(BlockLectern.FACING, EnumFacing.EAST)
        ), false);
        MultiblockGroup podium3 = new MultiblockGroup("podium3", Lists.newArrayList(
                AMBlocks.lectern.getDefaultState().withProperty(BlockLectern.FACING, EnumFacing.WEST)
        ), false);
        MultiblockGroup podium4 = new MultiblockGroup("podium4", Lists.newArrayList(
                AMBlocks.lectern.getDefaultState().withProperty(BlockLectern.FACING, EnumFacing.WEST)
        ), false);


        lever1.addBlock(new BlockPos(2, -2, 2));
        lever2.addBlock(new BlockPos(2, -2, -2));
        lever3.addBlock(new BlockPos(-2, -2, 2));
        lever4.addBlock(new BlockPos(-2, -2, -2));
        podium1.addBlock(new BlockPos(2, -3, 2));
        podium2.addBlock(new BlockPos(2, -3, -2));
        podium3.addBlock(new BlockPos(-2, -3, 2));
        podium4.addBlock(new BlockPos(-2, -3, -2));

        primary.addGroup(wall);
        primary.addGroup(lever1, lever2, lever3, lever4);
        primary.addGroup(out);
        primary.addGroup(catalysts);
        primary.addGroup(podium1, podium2, podium3, podium4);
        primary.addGroup(altar);

        catalysts_alt = new TypedMultiblockGroup("catalysts_alt", capsMaterials, false);

        out_alt = new TypedMultiblockGroup("out_alt", structureMaterials, false);
        out_alt.setFallbackMatcher((w, p) -> matchesOreDictWood(out_alt, w, p));

        MultiblockGroup wall_alt = new MultiblockGroup("wall_alt", Lists.newArrayList(AMBlocks.magic_wall.getDefaultState()), true);
        wall_alt.addBlock(new BlockPos(-2, -1, 0));
        wall_alt.addBlock(new BlockPos(-2, -2, 0));
        wall_alt.addBlock(new BlockPos(-2, -3, 0));
        wall_alt.addBlock(new BlockPos(2, -1, 0));
        wall_alt.addBlock(new BlockPos(2, -2, 0));
        wall_alt.addBlock(new BlockPos(2, -3, 0));


        catalysts_alt.addBlock(new BlockPos(-2, 0, -1), 0);
        catalysts_alt.addBlock(new BlockPos(-2, 0, 1), 0);
        catalysts_alt.addBlock(new BlockPos(2, 0, -1), 0);
        catalysts_alt.addBlock(new BlockPos(2, 0, 1), 0);
        catalysts_alt.addBlock(new BlockPos(0, -4, 0), 0);

        out_alt.addBlock(new BlockPos(-1, 0, -1), STAIR_SOUTH);
        out_alt.addBlock(new BlockPos(0, 0, -1), STAIR_SOUTH);
        out_alt.addBlock(new BlockPos(1, 0, -1), STAIR_SOUTH);
        out_alt.addBlock(new BlockPos(-1, 0, 1), STAIR_NORTH);
        out_alt.addBlock(new BlockPos(0, 0, 1), STAIR_NORTH);
        out_alt.addBlock(new BlockPos(1, 0, 1), STAIR_NORTH);
        out_alt.addBlock(new BlockPos(-2, 0, 0), STAIR_EAST);
        out_alt.addBlock(new BlockPos(2, 0, 0), STAIR_WEST);
        out_alt.addBlock(new BlockPos(-1, -1, -1), STAIR_WEST_INVERTED);
        out_alt.addBlock(new BlockPos(-1, -1, 1), STAIR_WEST_INVERTED);
        out_alt.addBlock(new BlockPos(1, -1, -1), STAIR_EAST_INVERTED);
        out_alt.addBlock(new BlockPos(1, -1, 1), STAIR_EAST_INVERTED);

        out_alt.addBlock(new BlockPos(-1, 0, 0), 0);
        out_alt.addBlock(new BlockPos(1, 0, 0), 0);
        out_alt.addBlock(new BlockPos(-2, -1, 1), 0);
        out_alt.addBlock(new BlockPos(2, -1, 1), 0);
        out_alt.addBlock(new BlockPos(-2, -1, -1), 0);
        out_alt.addBlock(new BlockPos(2, -1, -1), 0);
        out_alt.addBlock(new BlockPos(-2, -2, 1), 0);
        out_alt.addBlock(new BlockPos(2, -2, 1), 0);
        out_alt.addBlock(new BlockPos(-2, -2, -1), 0);
        out_alt.addBlock(new BlockPos(2, -2, -1), 0);
        out_alt.addBlock(new BlockPos(-2, -3, 1), 0);
        out_alt.addBlock(new BlockPos(2, -3, 1), 0);
        out_alt.addBlock(new BlockPos(-2, -3, -1), 0);
        out_alt.addBlock(new BlockPos(2, -3, -1), 0);
        out_alt.addBlock(new BlockPos(-2, -4, -2), 0);
        out_alt.addBlock(new BlockPos(-2, -4, -1), 0);
        out_alt.addBlock(new BlockPos(-2, -4, 0), 0);
        out_alt.addBlock(new BlockPos(-2, -4, 1), 0);
        out_alt.addBlock(new BlockPos(-2, -4, 2), 0);
        out_alt.addBlock(new BlockPos(-1, -4, -2), 0);
        out_alt.addBlock(new BlockPos(-1, -4, -1), 0);
        out_alt.addBlock(new BlockPos(-1, -4, 0), 0);
        out_alt.addBlock(new BlockPos(-1, -4, 1), 0);
        out_alt.addBlock(new BlockPos(-1, -4, 2), 0);
        out_alt.addBlock(new BlockPos(0, -4, -2), 0);
        out_alt.addBlock(new BlockPos(0, -4, -1), 0);
        out_alt.addBlock(new BlockPos(0, -4, 1), 0);
        out_alt.addBlock(new BlockPos(0, -4, 2), 0);
        out_alt.addBlock(new BlockPos(1, -4, -2), 0);
        out_alt.addBlock(new BlockPos(1, -4, -1), 0);
        out_alt.addBlock(new BlockPos(1, -4, 0), 0);
        out_alt.addBlock(new BlockPos(1, -4, 1), 0);
        out_alt.addBlock(new BlockPos(1, -4, 2), 0);
        out_alt.addBlock(new BlockPos(2, -4, -2), 0);
        out_alt.addBlock(new BlockPos(2, -4, -1), 0);
        out_alt.addBlock(new BlockPos(2, -4, 0), 0);
        out_alt.addBlock(new BlockPos(2, -4, 1), 0);
        out_alt.addBlock(new BlockPos(2, -4, 2), 0);

        MultiblockGroup lever1_alt = new MultiblockGroup("lever1_alt", Lists.newArrayList(
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.SOUTH),
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.SOUTH).withProperty(BlockLever.POWERED, true)
        ), false);
        MultiblockGroup lever2_alt = new MultiblockGroup("lever2_alt", Lists.newArrayList(
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.NORTH),
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.NORTH).withProperty(BlockLever.POWERED, true)
        ), false);
        MultiblockGroup lever3_alt = new MultiblockGroup("lever3_alt", Lists.newArrayList(
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.SOUTH),
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.SOUTH).withProperty(BlockLever.POWERED, true)
        ), false);
        MultiblockGroup lever4_alt = new MultiblockGroup("lever4_alt", Lists.newArrayList(
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.NORTH),
                Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.NORTH).withProperty(BlockLever.POWERED, true)
        ), false);

        MultiblockGroup podium1_alt = new MultiblockGroup("podium1_alt", Lists.newArrayList(
                AMBlocks.lectern.getDefaultState().withProperty(BlockLectern.FACING, EnumFacing.SOUTH)
        ), false);
        MultiblockGroup podium2_alt = new MultiblockGroup("podium2_alt", Lists.newArrayList(
                AMBlocks.lectern.getDefaultState().withProperty(BlockLectern.FACING, EnumFacing.NORTH)
        ), false);
        MultiblockGroup podium3_alt = new MultiblockGroup("podium3_alt", Lists.newArrayList(
                AMBlocks.lectern.getDefaultState().withProperty(BlockLectern.FACING, EnumFacing.SOUTH)
        ), false);
        MultiblockGroup podium4_alt = new MultiblockGroup("podium4_alt", Lists.newArrayList(
                AMBlocks.lectern.getDefaultState().withProperty(BlockLectern.FACING, EnumFacing.NORTH)
        ), false);
        lever1_alt.addBlock(new BlockPos(2, -2, 2));
        lever2_alt.addBlock(new BlockPos(2, -2, -2));
        lever3_alt.addBlock(new BlockPos(-2, -2, 2));
        lever4_alt.addBlock(new BlockPos(-2, -2, -2));
        podium1_alt.addBlock(new BlockPos(2, -3, 2));
        podium2_alt.addBlock(new BlockPos(2, -3, -2));
        podium3_alt.addBlock(new BlockPos(-2, -3, 2));
        podium4_alt.addBlock(new BlockPos(-2, -3, -2));
        secondary.addGroup(wall_alt);
        secondary.addGroup(lever1_alt, lever2_alt, lever3_alt, lever4_alt);
        secondary.addGroup(out_alt);
        secondary.addGroup(catalysts_alt);
        secondary.addGroup(podium1_alt, podium2_alt, podium3_alt, podium4_alt);
        secondary.addGroup(altar);

        MultiblockGroup center = new MultiblockGroup("center", Lists.newArrayList(AMBlocks.crafting_altar.getDefaultState()), true);
        center.addBlock(new BlockPos(0, 0, 0));
        primary.addGroup(center);
        secondary.addGroup(center);
    }

    @Override
    public IMultiblock getMultiblockStructure() {
        return secondary;
    }

    public ItemStack getNextPlannedItem() {
        if (spellGuide != null) {
            if (this.allAddedItems.size() < spellGuide.length) {
                return spellGuide[this.allAddedItems.size()].copy();
            } else {
                return new ItemStack(AMItems.spell_parchment);
            }
        }
        return null;
    }

    private int getNumPartsInSpell() {
        int parts = 0;
        if (outputCombo != null)
            parts = outputCombo.length;

        if (shapeGroupGuide != null) {
            for (int i = 0; i < shapeGroupGuide.length; ++i) {
                if (shapeGroupGuide[i] != null)
                    parts += shapeGroupGuide[i].length;
            }
        }
        return parts;
    }

    private boolean spellGuideIsWithinStructurePower() {
        return getNumPartsInSpell() <= maxEffects;
    }

    private boolean currentDefinitionIsWithinStructurePower() {
        int count = this.spellDef.size();
        for (KeyValuePair<ArrayList<SpellPart>, NBTTagCompound> part : shapeGroups)
            count += part.key.size();

        return count <= this.maxEffects;
    }

    @Override
    public boolean isStructureValid() {
        return this.structureValid;
    }

    public boolean isCrafting() {
        return this.isCrafting;
    }

    @Override
    public void update() {
        super.update();
        ticksExisted++;
        checkStructure();
        checkForStartCondition();
        updateEBWizPowerAccumulation();
        updateLecternInformation();
        if (isCrafting) {
            // Discovery ritual has its own update path
            if (discoveryRitualActive) {
                updateDiscoveryRitual();
                this.markDirty();
                return;
            }
            checkForEndCondition();
            updatePowerRequestData();
            if (!world.isRemote && !currentDefinitionIsWithinStructurePower() && this.ticksExisted > 100) {
                world.newExplosion(null, pos.getX() + 0.5, pos.getY() - 1.5, pos.getZ() + 0.5, 5, false, true);
                setCrafting(false);
                return;
            }
            if (world.isRemote && checkCounter == 1) {
                ArsMagica.proxy.particleManager.RibbonFromPointToPoint(world, pos.getX() + 0.5, pos.getY() - 2, pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() - 3, pos.getZ() + 0.5);
            }
            if (world.isRemote && ticksExisted % 5 == 0) {
                ItemStack etherStack = getNextPlannedItem();
                if (etherStack != null && !etherStack.isEmpty() && etherStack.getItem() == AMItems.etherium && switchIsOn()) {
                    PowerTypes powerType = PowerTypes.getByID(etherStack.getItemDamage());
                    int col = (powerType != null) ? powerType.getColor() : 0xffffff;
                    float r = ((col >> 16) & 0xFF) / 255f;
                    float g = ((col >> 8) & 0xFF) / 255f;
                    float b = (col & 0xFF) / 255f;
                    double cx = pos.getX() + 0.5;
                    double cy = pos.getY() + 0.5;
                    double cz = pos.getZ() + 0.5;
                    for (int i = 0; i < 3 * ArsMagica.config.getGFXLevel(); ++i) {
                        double ox = (world.rand.nextDouble() - 0.5) * 6;
                        double oy = (world.rand.nextDouble() - 0.5) * 2;
                        double oz = (world.rand.nextDouble() - 0.5) * 6;
                        AMParticle p = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "radiant", cx + ox, cy + oy, cz + oz);
                        if (p != null) {
                            p.setMaxAge(20 + world.rand.nextInt(20));
                            p.AddParticleController(new ParticleApproachPoint(p, cx, cy, cz, 0.05, 0.5, 1, false));
                            p.AddParticleController(new ParticleFadeOut(p, 1, false).setFadeSpeed(0.05f).setKillParticleOnFinish(true));
                            p.setParticleScale(0.02f);
                            p.setRGBColorF(r, g, b);
                        }
                    }
                }
            }
            List<EntityItem> components = lookForValidItems();
            ItemStack stack = getNextPlannedItem();
            if (stack != null) for (EntityItem item : components) {
                if (item.isDead) continue;
                ItemStack entityItemStack = item.getItem();
                // Etherium items must be handled exclusively by the power-network mechanism
                // (updatePowerRequestData + lever). Allowing physical etherium items to be
                // consumed here would bypass the obelisk drain entirely, matching the bug
                // where the original 1.7.10 used separate damage-value ranges for
                // "power tokens" vs physical essence items.
                if (stack.getItem() == AMItems.etherium) continue;
                if (!stack.isEmpty() && compareItemStacks(stack, entityItemStack)) {
                    if (!world.isRemote) {
                        updateCurrentRecipe(item);
                        item.setDead();
                    } else {
                        world.playSound(null, pos, AMSounds.CRAFTING_ALTAR_COMPONENT_ADDED, SoundCategory.BLOCKS, 1.0f, 0.4f + world.rand.nextFloat() * 0.6f);
                        for (int i = 0; i < 5 * ArsMagica.config.getGFXLevel(); ++i) {
                            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "radiant", item.posX, item.posY, item.posZ);
                            if (particle != null) {
                                particle.setMaxAge(40);
                                particle.AddParticleController(new ParticleMoveOnHeading(particle, world.rand.nextFloat() * 360, world.rand.nextFloat() * 360, 0.01f, 1, false));
                                particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05f).setKillParticleOnFinish(true));
                                particle.setParticleScale(0.02f);
                                particle.setRGBColorF(world.rand.nextFloat(), world.rand.nextFloat(), world.rand.nextFloat());
                            }
                        }
                    }
                }
            }
        }
        this.markDirty();
    }

    private void updateLecternInformation() {
        if (podiumLocation == null) return;
        TileEntityLectern lectern = (TileEntityLectern) world.getTileEntity(pos.add(podiumLocation));
        if (lectern != null) {
            if (lectern.hasStack()) {
                ItemStack lecternStack = lectern.getStack();
                if (lecternStack.hasTagCompound()) {
                    spellGuide = NBTUtils.getItemStackArray(lecternStack.getTagCompound(), "spell_combo");
                    outputCombo = lecternStack.getTagCompound().getIntArray("output_combo");
                    currentSpellName = lecternStack.getDisplayName();

                    int numShapeGroups = lecternStack.getTagCompound().getInteger("numShapeGroups");
                    shapeGroupGuide = new int[numShapeGroups][];

                    for (int i = 0; i < numShapeGroups; ++i) {
                        shapeGroupGuide[i] = lecternStack.getTagCompound().getIntArray("shapeGroupCombo_" + i);
                    }
                }

                if (isCrafting) {
                    if (discoveryRitualActive) {
                        // Discovery ritual hint based on current phase
                        lectern.setNeedsBook(false);
                        lectern.setTooltipStack(getDiscoveryHintStack());
                    } else if (spellGuide != null) {
                        lectern.setNeedsBook(false);
                        lectern.setTooltipStack(getNextPlannedItem());
                    } else {
                        lectern.setNeedsBook(true);
                    }
                } else if (lecternStack.getItem() == net.minecraft.init.Items.WRITABLE_BOOK
                        && EBWizardryCompatBootstrap.isDiscoveryRitualEnabled()) {
                    // Writable book on lectern, discovery enabled: show blank_rune hint to start
                    lectern.setTooltipStack(new ItemStack(AMItems.blank_rune));
                } else if (EBWizardryCompatBootstrap.isEBWizBindingBook(lecternStack)) {
                    // Binding book from the Scribing Desk: just show blank_rune hint (no etherium needed).
                    lectern.setTooltipStack(new ItemStack(AMItems.blank_rune));
                } else if (EBWizardryCompatBootstrap.isEBWizSpellBook(lecternStack)) {
                    // Alternate between showing blank_rune and neutral etherium so the
                    // player knows both ingredients for the EBWiz transcription.
                    if (ArsMagica.config.getEBWizTranscriptionEnabled()) {
                        ItemStack hint = ((ticksExisted / 60) % 2 == 0)
                                ? new ItemStack(AMItems.blank_rune)
                                : new ItemStack(AMItems.etherium, 1, PowerTypes.NEUTRAL.ID());
                        lectern.setTooltipStack(hint);
                    } else {
                        lectern.setTooltipStack(ItemStack.EMPTY);
                    }
                } else {
                    lectern.setTooltipStack(ItemStack.EMPTY);
                }
                if (spellGuideIsWithinStructurePower()) {
                    lectern.setOverpowered(false);
                } else {
                    lectern.setOverpowered(true);
                } 
            } else {
                if (isCrafting) {
                    lectern.setNeedsBook(true);
                }
                lectern.setTooltipStack(ItemStack.EMPTY);
            }
        }
    }

    public BlockPos getSwitchLocation() {
        return this.switchLocation;
    }

    public boolean switchIsOn() {
        if (switchLocation == null) return false;
        IBlockState block = world.getBlockState(pos.add(switchLocation));
        boolean b = false;
        if (block.getBlock() == Blocks.LEVER) {
            for (int i = 0; i < 6; ++i) {
                b |= block.getValue(BlockLever.POWERED);
                if (b) break;
            }
        }
        return b;
    }

    public void flipSwitch() {
        if (switchLocation == null) return;
        IBlockState block = world.getBlockState(pos.add(switchLocation));
        if (block.getBlock() == Blocks.LEVER) {
            world.setBlockState(pos.add(switchLocation), block.withProperty(BlockLever.POWERED, false));
        }
    }

    private void updatePowerRequestData() {
        ItemStack stack = getNextPlannedItem();
        if (stack == null || stack.isEmpty()) {
            setNoPowerRequests();
            return;
        }
        if (stack.getItem().equals(AMItems.etherium)) {
            if (switchIsOn()) {
                int flags = stack.getItemDamage();
                setPowerRequests();
                pickPowerType(stack);
                for (PowerTypes type : this.currentMainPowerTypes) {
                    if (PowerNodeRegistry.For(this.world).checkPower(this, type, Math.max(0, Math.min(100, stack.getCount() - currentConsumedPower)))) {
                        currentConsumedPower += PowerNodeRegistry.For(world).consumePower(this, type, Math.min(100, stack.getCount() - currentConsumedPower));
                    }
                }
                if (currentConsumedPower >= stack.getCount()) {
                    //PowerNodeRegistry.For(this.world).setPower(this, this.currentMainPowerTypes, 0);
                    if (!world.isRemote)
                        addItemToRecipe(new ItemStack(AMItems.etherium, stack.getCount(), flags));
                    setNoPowerRequests();
                    flipSwitch();
                }
            } else {
                setNoPowerRequests();
            }
        } else {
            setNoPowerRequests();
        }
    }

    @Override
    protected void setNoPowerRequests() {
        currentConsumedPower = 0;
        currentMainPowerTypes.clear();

        super.setNoPowerRequests();
    }

    private void pickPowerType(ItemStack stack) {
        if (!this.currentMainPowerTypes.isEmpty())
            return;
        List<PowerTypes> valids = PowerTypes.getTypes(stack.getItemDamage());
        this.currentMainPowerTypes.addAll(valids);
    }

    private void updateCurrentRecipe(EntityItem item) {
        ItemStack stack = item.getItem();
        addItemToRecipe(stack);
    }

    private void addItemToRecipe(ItemStack stack) {
        allAddedItems.add(stack);
        currentAddedItems.add(stack);

        if (!world.isRemote) {
            AMDataWriter writer = new AMDataWriter();
            writer.add(pos.getX());
            writer.add(pos.getY());
            writer.add(pos.getZ());
            writer.add(COMPONENT_ADDED);
            writer.add(stack);
            AMNetworkHandler.getNetwork().sendToAllAround(new PacketCraftingAltarSync(pos, writer.generate()), new net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 32));
        }

        if (matchCurrentRecipe()) {
            currentAddedItems.clear();
            return;
        }
    }

    private boolean matchCurrentRecipe() {
        SpellPart part = SpellRegistryHelper.getPartByRecipe(currentAddedItems);
        if (part == null) return false;

        KeyValuePair<ArrayList<SpellPart>, NBTTagCompound> currentShapeGroupList = getShapeGroupToAddTo();

        if (part instanceof Summon)
            handleSummonShape();
        if (part instanceof Binding)
            handleBindingShape();


        //if this is null, then we have already completed all of the shape groups that the book identifies
        //we're now creating the body of the spell
        if (currentShapeGroupList == null) {
            part.encodeBasicData(savedData, currentAddedItems.toArray());
            spellDef.add(part);
        } else {
            part.encodeBasicData(currentShapeGroupList.value, currentAddedItems.toArray());
            currentShapeGroupList.key.add(part);
        }
        return true;
    }

    private KeyValuePair<ArrayList<SpellPart>, NBTTagCompound> getShapeGroupToAddTo() {
        for (int i = 0; i < shapeGroupGuide.length; ++i) {
            int guideLength = shapeGroupGuide[i].length;
            int addedLength = shapeGroups.get(i).key.size();
            if (addedLength < guideLength)
                return shapeGroups.get(i);
        }

        return null;
    }

    private void handleSummonShape() {
        if (currentAddedItems.size() > 2)
            addedPhylactery = currentAddedItems.get(currentAddedItems.size() - 2);
    }

    private void handleBindingShape() {
        if (currentAddedItems.size() == 7)
            addedBindingCatalyst = currentAddedItems.get(currentAddedItems.size() - 1);
    }

    private List<EntityItem> lookForValidItems() {
        if (!isCrafting) return new ArrayList<EntityItem>();
        double radius = world.isRemote ? 2.1 : 2;
        List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.getX() - radius, pos.getY() - 3, pos.getZ() - radius, pos.getX() + radius, pos.getY(), pos.getZ() + radius));
        return items;
    }

    private void checkStructure() {
        maxEffects = 0;
        if (checkCounter++ > 50)
            checkCounter = 0;
        if (primary.matches(world, pos)) {
            for (IMultiblockGroup matching : primary.getMatchingGroups(world, pos)) {
                for (IBlockState state : matching.getStates()) {
                    if (state.getBlock().equals(Blocks.LEVER))
                        this.switchLocation = matching.getPositions().get(0);
                    else if (state.getBlock().equals(AMBlocks.lectern))
                        this.podiumLocation = matching.getPositions().get(0);
                }
                if (matching == catalysts || matching == catalysts_alt) {
                    Integer toAdd = capsPower.get(world.getBlockState(pos.down(4)));
                    maxEffects += toAdd != null ? toAdd : 0;
                } else if (matching == out || matching == out_alt) {
                    mimicState = world.getBlockState(pos.down(4).east());
                    Integer toAdd = structurePower.get(mimicState);
                    if (toAdd == null && isInOreDict(mimicState, "plankWood")) {
                        toAdd = 1;
                    }
                    maxEffects += toAdd != null ? toAdd : 0;
                }
            }
        } else if (secondary.matches(world, pos)) {
            for (IMultiblockGroup matching : secondary.getMatchingGroups(world, pos)) {
                for (IBlockState state : matching.getStates()) {
                    if (state.getBlock().equals(Blocks.LEVER))
                        this.switchLocation = matching.getPositions().get(0);
                    else if (state.getBlock().equals(AMBlocks.lectern))
                        this.podiumLocation = matching.getPositions().get(0);
                }
                if (matching == catalysts || matching == catalysts_alt) {
                    Integer toAdd = capsPower.get(world.getBlockState(pos.down(4)));
                    maxEffects += toAdd != null ? toAdd : 0;
                } else if (matching == out || matching == out_alt) {
                    mimicState = world.getBlockState(pos.down(4).east());
                    Integer toAdd = structurePower.get(mimicState);
                    if (toAdd == null && isInOreDict(mimicState, "plankWood")) {
                        toAdd = 1;
                    }
                    maxEffects += toAdd != null ? toAdd : 0;
                }
            }
        }
        setStructureValid(primary.matches(world, pos) || secondary.matches(world, pos));
    }

    private void checkForStartCondition() {
        if (this.world.isRemote || !structureValid || this.isCrafting) return;

        List<Entity> items = this.world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.getX() - 2, pos.getY() - 3, pos.getZ() - 2, pos.getX() + 2, pos.getY(), pos.getZ() + 2));

        // Locate the blank rune (may co-exist with essence items for EBWiz conversion).
        EntityItem blankRune = null;
        for (Entity e : items) {
            EntityItem ei = (EntityItem) e;
            if (!ei.isDead && ei.getItem().getItem() == AMItems.blank_rune) {
                blankRune = ei;
                break;
            }
        }
        if (blankRune == null) return;

        // EBWiz Discovery Ritual: if the lectern holds a writable book and discovery is enabled,
        // start the multi-phase discovery ritual.
        if (hasWritableBookOnLectern() && EBWizardryCompatBootstrap.isDiscoveryRitualEnabled()) {
            String thrower = blankRune.getThrower();
            lastCraftingPlayerName = (thrower != null && !thrower.isEmpty()) ? thrower : "";
            blankRune.setDead();
            discoveryRitualActive = true;
            discoveryPhase = 0;
            discoveryElementOrdinal = -1;
            discoveryTierOrdinal = -1;
            discoveryIngredientIndex = 0;
            discoveryPlannedItems = null;
            ebwizAccumulatedPower = 0f;
            setCrafting(true);
            // Re-set discoveryRitualActive after setCrafting (which resets it)
            discoveryRitualActive = true;
            return;
        }

        // EBWiz fast-path: if the lectern holds an EBWiz spell book, attempt transcription.
        // Etherium cost is drawn from the altar's power network buffer (not physical items).
        if (hasEBWizBookOnLectern()) {
            tryTranscribeEBWizBook(blankRune);
            return;
        }

        // EBWiz binding book fast-path: start the standard ingredient crafting flow.
        // The blank rune + modifier recipe items are required; the parchment finalizes.
        if (hasEBWizBindingBookOnLectern()) {
            TileEntityLectern lectern = (TileEntityLectern) world.getTileEntity(pos.add(podiumLocation));
            if (lectern != null) {
                ebwizPendingBook = lectern.getStack().copy();
                lectern.setStack(ItemStack.EMPTY);
            }
            blankRune.setDead();
            setCrafting(true);
            return;
        }

        // Normal spell crafting: require exactly the blank rune, no other items present,
        // AND the lectern must hold a valid spell recipe book. Without a recipe the altar
        // has no spell_combo guide, getNextPlannedItem() returns spell_parchment immediately
        // (spellGuide becomes an empty array, not null), and throwing parchment produces an
        // empty broken spell. This guard also prevents accidental crafting after an EBWiz
        // craft that cleared the lectern.
        if (items.size() == 1 && hasSpellRecipeOnLectern()) {
            String thrower = blankRune.getThrower();
            lastCraftingPlayerName = (thrower != null && !thrower.isEmpty()) ? thrower : "";
            blankRune.setDead();
            setCrafting(true);
        }
    }

    /**
     * Each server tick while an EBWiz spell book is on the lectern (and not crafting),
     * drain any available neutral etherium from the altar's power buffer into
     * {@link #ebwizAccumulatedPower} so we can track progress toward the tier cost
     * even though the buffer capacity (500) may be smaller than the cost.
     */
    private void updateEBWizPowerAccumulation() {
        if (world.isRemote || isCrafting) return;
        if (!hasEBWizBookOnLectern() || !ArsMagica.config.getEBWizTranscriptionEnabled()) {
            ebwizAccumulatedPower = 0f;
            setNoPowerRequests();
            return;
        }
        // Allow the base TileEntityAMPower.update() to fill the buffer from obelisks.
        setPowerRequests();
        // Drain whatever neutral etherium has been buffered into our accumulator.
        float buffered = PowerNodeRegistry.For(world).getPower(this, PowerTypes.NEUTRAL);
        if (buffered > 0) {
            ebwizAccumulatedPower += buffered;
            PowerNodeRegistry.For(world).setPower(this, PowerTypes.NEUTRAL, 0f);
        }
    }

    /**
     * Returns {@code true} if the altar's lectern holds a spell recipe book that contains
     * a {@code spell_combo} ingredient list. Without this, normal spell crafting must not
     * begin because {@link #getNextPlannedItem()} would return {@link AMItems#spell_parchment}
     * immediately (empty-array spellGuide), producing a broken empty-spell output.
     */
    private boolean hasSpellRecipeOnLectern() {
        if (podiumLocation == null) return false;
        TileEntityLectern lectern = (TileEntityLectern) world.getTileEntity(pos.add(podiumLocation));
        if (lectern == null || !lectern.hasStack()) return false;
        ItemStack stack = lectern.getStack();
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("spell_combo");
    }

    /** Returns {@code true} if the altar's lectern holds an EBWiz binding book from the Scribing Desk. */
    private boolean hasEBWizBindingBookOnLectern() {
        if (podiumLocation == null) return false;
        TileEntityLectern lectern = (TileEntityLectern) world.getTileEntity(pos.add(podiumLocation));
        if (lectern == null || !lectern.hasStack()) return false;
        return EBWizardryCompatBootstrap.isEBWizBindingBook(lectern.getStack());
    }

    /** Returns {@code true} if the altar's lectern currently holds an EBWiz spell book. */
    private boolean hasEBWizBookOnLectern() {
        if (podiumLocation == null) return false;
        TileEntityLectern lectern = (TileEntityLectern) world.getTileEntity(pos.add(podiumLocation));
        if (lectern == null || !lectern.hasStack()) return false;
        return EBWizardryCompatBootstrap.isEBWizSpellBook(lectern.getStack());
    }

    /** Returns {@code true} if the altar's lectern holds a writable book (book and quill). */
    private boolean hasWritableBookOnLectern() {
        if (podiumLocation == null) return false;
        TileEntityLectern lectern = (TileEntityLectern) world.getTileEntity(pos.add(podiumLocation));
        if (lectern == null || !lectern.hasStack()) return false;
        return lectern.getStack().getItem() == net.minecraft.init.Items.WRITABLE_BOOK;
    }

    /**
     * Returns the hint item to show on the lectern during the discovery ritual,
     * cycling through element items, tier catalysts, planned ingredients, or etherium
     * depending on the current phase.
     */
    private ItemStack getDiscoveryHintStack() {
        switch (discoveryPhase) {
            case 0: // Awaiting element: cycle through element items
            {
                // Cycle through element items using ticksExisted
                ItemStack[] elementHints = {
                        new ItemStack(net.minecraft.init.Items.COAL, 1, 1),          // charcoal = FIRE
                        new ItemStack(net.minecraft.init.Items.SNOWBALL),             // ICE
                        new ItemStack(net.minecraft.init.Items.REDSTONE),             // LIGHTNING
                        new ItemStack(net.minecraft.init.Items.BONE),                 // NECROMANCY
                        new ItemStack(net.minecraft.init.Blocks.STONE, 1, 1),         // granite = EARTH
                        new ItemStack(am2.common.registry.AMBlocks.cerublossom),      // SORCERY
                        new ItemStack(am2.common.registry.AMBlocks.aum),              // HEALING
                };
                int idx = (ticksExisted / 40) % elementHints.length;
                return elementHints[idx];
            }
            case 1: // Awaiting tier catalyst: cycle through tier items
            {
                ItemStack[] tierHints = {
                        EBWizardryCompatBootstrap.getMagicCrystalStack(),  // NOVICE
                        new ItemStack(am2.common.registry.AMItems.lesser_focus),          // APPRENTICE
                        new ItemStack(am2.common.registry.AMItems.standard_focus),        // ADVANCED
                        new ItemStack(am2.common.registry.AMItems.greater_focus),         // MASTER
                };
                int idx = (ticksExisted / 40) % tierHints.length;
                return tierHints[idx];
            }
            case 2: // Consuming ingredients: show current expected ingredient
                if (discoveryPlannedItems != null && discoveryIngredientIndex < discoveryPlannedItems.length) {
                    return discoveryPlannedItems[discoveryIngredientIndex];
                }
                return ItemStack.EMPTY;
            case 3: // Accumulating power: show etherium
                return new ItemStack(AMItems.etherium, 1, PowerTypes.NEUTRAL.ID());
            default:
                return ItemStack.EMPTY;
        }
    }

    /**
     * If EBWiz is loaded and the lectern contains an EBWiz {@code ItemSpellBook},
     * checks that the altar's power buffer has sufficient neutral etherium, then
     * consumes it along with the blank rune and the lectern book, and spawns an
     * {@link ItemEBWizSpellBinding} at the altar centre.
     *
     * <p>Etherium is consumed from the altar's power network buffer (supplied by obelisks),
     * not from physical item drops. Cost: 500 base + 500 per tier level:
     * Novice 1&thinsp;000 &bull; Apprentice 1&thinsp;500 &bull; Advanced 2&thinsp;000 &bull; Master 2&thinsp;500.
     *
     * @param runeItem the blank-rune {@link EntityItem} that triggered the check
     * @return {@code true} if transcription was performed
     */
    private boolean tryTranscribeEBWizBook(EntityItem runeItem) {
        if (!ArsMagica.config.getEBWizTranscriptionEnabled()) return false;
        if (podiumLocation == null) return false;
        TileEntityLectern lectern = (TileEntityLectern) world.getTileEntity(pos.add(podiumLocation));
        if (lectern == null || !lectern.hasStack()) return false;

        ItemStack lecternStack = lectern.getStack();
        if (!EBWizardryCompatBootstrap.isEBWizSpellBook(lecternStack)) return false;

        // ---- Neutral-etherium cost check (from accumulated power network buffer) --------
        float requiredPower = EBWizardryCompatBootstrap.getEBWizSpellBookEssenceCost(lecternStack);
        if (ebwizAccumulatedPower < requiredPower) {
            return false; // not enough neutral etherium accumulated yet
        }
        // --------------------------------------------------------------------

        ItemStack binding = EBWizardryCompatBootstrap.convertEBWizSpellBook(lecternStack);
        if (binding.isEmpty() || binding == lecternStack) return false; // conversion failed

        // Consume the blank rune, the lectern book, and reset accumulated power.
        runeItem.setDead();
        lectern.setStack(ItemStack.EMPTY);
        ebwizAccumulatedPower = 0f;
        setNoPowerRequests();

        // Spawn the resulting binding item at the altar centre.
        EntityItem output = new EntityItem(world,
                pos.getX() + 0.5, pos.getY() - 1.5, pos.getZ() + 0.5, binding);
        output.motionX = 0;
        output.motionY = 0.1;
        output.motionZ = 0;
        world.spawnEntity(output);

        // Visual feedback – burst of sparkle particles.
        if (world.isRemote) {
            for (int i = 0; i < 20 * ArsMagica.config.getGFXLevel(); i++) {
                AMParticle p = (AMParticle) ArsMagica.proxy.particleManager.spawn(world,
                        "sparkle",
                        pos.getX() + 0.5 + (world.rand.nextDouble() - 0.5),
                        pos.getY() - 1.5 + world.rand.nextDouble(),
                        pos.getZ() + 0.5 + (world.rand.nextDouble() - 0.5));
                if (p != null) {
                    p.AddParticleController(new ParticleMoveOnHeading(p,
                            world.rand.nextFloat() * 360, world.rand.nextFloat() * 360,
                            0.04f, 1, false));
                    p.AddParticleController(new ParticleFadeOut(p, 2, false)
                            .setFadeSpeed(0.04f).setKillParticleOnFinish(true));
                    p.setRGBColorF(0.5f + world.rand.nextFloat() * 0.5f,
                            0.2f,
                            0.8f + world.rand.nextFloat() * 0.2f);
                }
            }
        }
        return true;
    }

    public IBlockState getMimicState() {
        return mimicState;
    }

    // =========================================================================
    // EBWiz Discovery Ritual – multi-phase update
    // =========================================================================

    /**
     * Called each server tick while {@link #discoveryRitualActive} and {@link #isCrafting}.
     * Progresses through four phases:
     * <ol>
     *   <li><b>ELEMENT (0)</b> – wait for player to throw an element item</li>
     *   <li><b>TIER (1)</b> – wait for tier catalyst</li>
     *   <li><b>INGREDIENTS (2)</b> – consume planned ingredients one by one</li>
     *   <li><b>POWER (3)</b> – accumulate neutral etherium, then finalize</li>
     * </ol>
     */
    private void updateDiscoveryRitual() {
        if (world.isRemote || !discoveryRitualActive || !isCrafting) return;

        double radius = 2;
        List<EntityItem> nearbyItems = this.world.getEntitiesWithinAABB(EntityItem.class,
                new AxisAlignedBB(pos.getX() - radius, pos.getY() - 3, pos.getZ() - radius,
                        pos.getX() + radius, pos.getY(), pos.getZ() + radius));

        switch (discoveryPhase) {
            case 0: // AWAITING ELEMENT
                for (EntityItem ei : nearbyItems) {
                    if (ei.isDead) continue;
                    int element = EBWizardryCompatBootstrap.getElementOrdinalForItem(ei.getItem());
                    if (element >= 0) {
                        ei.setDead();
                        discoveryElementOrdinal = element;
                        discoveryPhase = 1;
                        markDirty();
                        return;
                    }
                }
                break;

            case 1: // AWAITING TIER
                for (EntityItem ei : nearbyItems) {
                    if (ei.isDead) continue;
                    int tier = EBWizardryCompatBootstrap.getTierOrdinalForItem(ei.getItem());
                    if (tier >= 0) {
                        ei.setDead();
                        discoveryTierOrdinal = tier;
                        discoveryPlannedItems = EBWizardryCompatBootstrap.getDiscoveryIngredients(discoveryElementOrdinal, discoveryTierOrdinal);
                        discoveryIngredientIndex = 0;
                        discoveryPhase = 2;
                        markDirty();
                        return;
                    }
                }
                break;

            case 2: // CONSUMING INGREDIENTS
                if (discoveryPlannedItems == null || discoveryIngredientIndex >= discoveryPlannedItems.length) {
                    // All ingredients consumed; advance to power phase
                    discoveryPhase = 3;
                    ebwizAccumulatedPower = 0f;
                    setPowerRequests();
                    markDirty();
                    return;
                }
                ItemStack expected = discoveryPlannedItems[discoveryIngredientIndex];
                for (EntityItem ei : nearbyItems) {
                    if (ei.isDead) continue;
                    if (compareItemStacks(expected, ei.getItem())) {
                        ei.setDead();
                        discoveryIngredientIndex++;
                        markDirty();
                        // Particle feedback for consumed ingredient
                        if (!world.isRemote) {
                            // Client-side particles handled in update() loop
                        }
                        if (discoveryIngredientIndex >= discoveryPlannedItems.length) {
                            discoveryPhase = 3;
                            ebwizAccumulatedPower = 0f;
                            setPowerRequests();
                            markDirty();
                        }
                        return;
                    }
                }
                break;

            case 3: // ACCUMULATING POWER
                // Drain neutral etherium from the power buffer
                setPowerRequests();
                float buffered = PowerNodeRegistry.For(world).getPower(this, PowerTypes.NEUTRAL);
                if (buffered > 0) {
                    ebwizAccumulatedPower += buffered;
                    PowerNodeRegistry.For(world).setPower(this, PowerTypes.NEUTRAL, 0f);
                }

                int requiredPower = EBWizardryCompatBootstrap.getDiscoveryPowerCost(discoveryTierOrdinal);
                if (ebwizAccumulatedPower >= requiredPower) {
                    finalizeDiscoveryRitual();
                }
                break;
        }
    }

    /**
     * Completes the discovery ritual: picks a random undiscovered spell, replaces the
     * writable book on the lectern with the spell book, marks it discovered, and resets.
     */
    private void finalizeDiscoveryRitual() {
        // Find the player who started the ritual
        net.minecraft.entity.player.EntityPlayer player = null;
        if (!lastCraftingPlayerName.isEmpty()) {
            player = world.getPlayerEntityByName(lastCraftingPlayerName);
        }
        // Fallback: EntityItem.getThrower() is often null for player-dropped items,
        // so find the closest player as a fallback.
        if (player == null) {
            player = world.getClosestPlayer(pos.getX() + 0.5, pos.getY() - 1.5, pos.getZ() + 0.5, 8, false);
        }

        // Check if the player has a discovery point to spend for this element's discipline
        Discipline discipline = EBWizardryCompatBootstrap.getDisciplineForElementOrdinal(discoveryElementOrdinal);
        if (discipline != null && player != null) {
            ISkillData skillData = SkillData.For(player);
            if (skillData != null && skillData.getAvailableDiscoveryPoints(discipline) <= 0) {
                String elemName = EBWizardryCompatBootstrap.getElementDisplayName(discoveryElementOrdinal);
                player.sendMessage(new net.minecraft.util.text.TextComponentTranslation(
                        "am2.craftingaltar.discovery.no_points", elemName));
                // Ritual pauses – do NOT reset state, power is preserved
                return;
            }
        }

        ItemStack spellBook = ItemStack.EMPTY;
        if (player != null) {
            spellBook = EBWizardryCompatBootstrap.selectRandomUndiscoveredSpell(player, discoveryElementOrdinal, discoveryTierOrdinal);
        }

        TileEntityLectern lectern = podiumLocation != null
                ? (TileEntityLectern) world.getTileEntity(pos.add(podiumLocation)) : null;

        if (spellBook.isEmpty()) {
            // Pool exhausted – notify player, keep writable book
            if (player != null) {
                String elemName = EBWizardryCompatBootstrap.getElementDisplayName(discoveryElementOrdinal);
                String tierName = EBWizardryCompatBootstrap.getTierDisplayName(discoveryTierOrdinal);
                player.sendMessage(new net.minecraft.util.text.TextComponentTranslation(
                        "am2.craftingaltar.discovery.exhausted", tierName, elemName));
            }
        } else {
            // Clear the writable book from the lectern
            if (lectern != null) {
                lectern.setStack(ItemStack.EMPTY);
            }
            // Spawn the spell book as a dropped item at the altar centre
            net.minecraft.entity.item.EntityItem entityItem = new net.minecraft.entity.item.EntityItem(
                    world, pos.getX() + 0.5, pos.getY() - 1.5, pos.getZ() + 0.5, spellBook);
            entityItem.motionX = 0;
            entityItem.motionY = 0.1;
            entityItem.motionZ = 0;
            entityItem.setPickupDelay(10);
            world.spawnEntity(entityItem);
            // Mark spell as discovered and spend the discipline point
            if (player != null) {
                EBWizardryCompatBootstrap.markSpellDiscovered(player, spellBook);
                if (discipline != null) {
                    ISkillData skillData = SkillData.For(player);
                    if (skillData != null) {
                        skillData.spendDiscoveryPoint(discipline);
                    }
                }
            }
        }

        // Reset state
        ebwizAccumulatedPower = 0f;
        setNoPowerRequests();
        setCrafting(false);
    }

    private void checkForEndCondition() {
        if (!structureValid || !this.isCrafting || world == null) return;

        double radius = world.isRemote ? 2.2 : 2;

        List<Entity> items = this.world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.getX() - radius, pos.getY() - 3, pos.getZ() - radius, pos.getX() + radius, pos.getY(), pos.getZ() + radius));
        if (items.size() == 1) {
            EntityItem item = (EntityItem) items.get(0);
            if (item != null && !item.isDead && item.getItem() != null && item.getItem().getItem() == AMItems.spell_parchment) {
                if (!world.isRemote) {
                    item.setDead();
                    // Capture ebwizPendingBook before setCrafting(false) resets it.
                    ItemStack capturedEBWizBook = ebwizPendingBook;
                    setCrafting(false);
                    EntityItem craftedItem = new EntityItem(world);
                    craftedItem.setPosition(pos.getX() + 0.5, pos.getY() - 1.5, pos.getZ() + 0.5);

                    ItemStack craftStack;
                    if (!capturedEBWizBook.isEmpty()) {
                        // Finalize as an EBWiz binding with the stored AM2 modifiers.
                        craftStack = EBWizardryCompatBootstrap.createBindingFromBook(capturedEBWizBook);
                        if (craftStack.isEmpty()) craftStack = new ItemStack(AMItems.spell); // fallback
                    } else {
                        craftStack = new ItemStack(AMItems.spell);
                        ISpellCaster caster = SpellCaster.of(craftStack);
                        if (caster != null) {
                            caster.setSpellCommon(SpellUtils.transformParts(spellDef));
                            caster.setCommonStoredData(savedData);
                            List<List<List<SpellPart>>> shapeGroups = Lists.newArrayList();
                            for (int i = 0; i < this.shapeGroups.size(); i++) {
                                KeyValuePair<ArrayList<SpellPart>, NBTTagCompound> entry = this.shapeGroups.get(i);
                                shapeGroups.add(SpellUtils.transformParts(entry.key));
                                caster.setStoredData(i, entry.value);
                            }
                            caster.setShapeGroups(shapeGroups);
                            // Write mana cost into the regular tagCompound so it is always synced
                            // to the client automatically (capabilities are not synced by default).
                            if (!craftStack.hasTagCompound())
                                craftStack.setTagCompound(new NBTTagCompound());
                            craftStack.getTagCompound().setFloat(am2.common.items.ItemSpellBase.KEY_MANA_COST_CACHED,
                                    caster.getBaseManaCost(caster.getCurrentShapeGroup()));
                        }
                        if (!craftStack.hasTagCompound())
                            craftStack.setTagCompound(new NBTTagCompound());
                        AddSpecialMetadata(craftStack);
                        craftStack.getTagCompound().setString("suggestedName", currentSpellName != null ? currentSpellName : "");
                        if (!lastCraftingPlayerName.isEmpty())
                            craftStack.getTagCompound().setString("creatorName", lastCraftingPlayerName);
                        if (getNextPlannedItem() == null || getNextPlannedItem().getItem() != AMItems.spell_parchment)
                            craftStack.setTagCompound(null);
                    }
                    craftedItem.setItem(craftStack);
                    world.spawnEntity(craftedItem);

                    allAddedItems.clear();
                    currentAddedItems.clear();
                } else {
                    //world.playSound(pos.getX(), pos.getY(), pos.getZ(), "arsmagica2:misc.craftingaltar.create_spell", 1.0f, 1.0f, true);
                }
            }
        }
    }

    private void AddSpecialMetadata(ItemStack craftStack) {
    }

    private void setCrafting(boolean crafting) {
        this.isCrafting = crafting;
        if (!crafting) {
            ebwizPendingBook = ItemStack.EMPTY;
            // Reset discovery ritual state
            discoveryRitualActive = false;
            discoveryPhase = 0;
            discoveryElementOrdinal = -1;
            discoveryTierOrdinal = -1;
            discoveryIngredientIndex = 0;
            discoveryPlannedItems = null;
        }
        if (!world.isRemote) {
            AMDataWriter writer = new AMDataWriter();
            writer.add(pos.getX());
            writer.add(pos.getY());
            writer.add(pos.getZ());
            writer.add(CRAFTING_CHANGED);
            writer.add(crafting);
            AMNetworkHandler.getNetwork().sendToAllAround(new PacketCraftingAltarSync(pos, writer.generate()), new net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 32));
        }
        if (crafting) {
            allAddedItems.clear();
            currentAddedItems.clear();

            spellDef.clear();
            shapeGroups.clear();

            // Reset guide arrays so stale data from a previous craft (or an EBWiz book that
            // left an empty spellGuide=[]) cannot bleed into the new crafting session.
            // updateLecternInformation() will repopulate them from the lectern book this tick.
            spellGuide = null;
            outputCombo = null;
            shapeGroupGuide = new int[0][];

            for (int i = 0; i < 5; ++i) {
                shapeGroups.add(new KeyValuePair<ArrayList<SpellPart>, NBTTagCompound>(new ArrayList<>(), new NBTTagCompound()));
            }

            //find otherworld auras
            IPowerNode<?>[] nodes = PowerNodeRegistry.For(world).getAllNearbyNodes(world, pos, PowerTypes.DARK);
            for (IPowerNode<?> node : nodes) {
                if (node instanceof TileEntityOtherworldAura) {
                    ((TileEntityOtherworldAura) node).setActive(true, this);
                    break;
                }
            }
        }
    }

    private void setStructureValid(boolean valid) {
        if (this.structureValid == valid) return;
        this.structureValid = valid;
        this.markDirty();
    }

    public void deactivate() {
        if (!world.isRemote) {
            this.setCrafting(false);
            for (ItemStack stack : allAddedItems) {
                if (stack.getItem() == AMItems.etherium)
                    continue;
                EntityItem eItem = new EntityItem(world);
                eItem.setPosition(pos.getX(), pos.getY() - 1, pos.getZ());
                eItem.setItem(stack);
                world.spawnEntity(eItem);
            }
            allAddedItems.clear();
        }
    }

    @Override
    public boolean canProvidePower(PowerTypes type) {
        return false;
    }

    private boolean compareItemStacks(ItemStack target, ItemStack input) {
        boolean tagCheck = target.getTagCompound() == null ? true : (input.getTagCompound() == null ? false : NBTUtils.contains(target.getTagCompound(), input.getTagCompound()));
        return OreDictionary.itemMatches(target, input, false) && tagCheck;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);

        NBTTagCompound altarCompound = new NBTTagCompound();
        altarCompound.setBoolean("isCrafting", this.isCrafting);
        altarCompound.setInteger("currentKey", this.currentKey);
        altarCompound.setString("currentSpellName", currentSpellName);
        altarCompound.setBoolean("StructureValid", structureValid);
        if (mimicState != null)
            altarCompound.setInteger("MimicState", Block.getStateId(mimicState));

        NBTTagList allAddedItemsList = new NBTTagList();
        for (ItemStack stack : allAddedItems) {
            NBTTagCompound addedItem = new NBTTagCompound();
            stack.writeToNBT(addedItem);
            allAddedItemsList.appendTag(addedItem);
        }

        altarCompound.setTag("allAddedItems", allAddedItemsList);

        NBTTagList currentAddedItemsList = new NBTTagList();
        for (ItemStack stack : currentAddedItems) {
            NBTTagCompound addedItem = new NBTTagCompound();
            stack.writeToNBT(addedItem);
            currentAddedItemsList.appendTag(addedItem);
        }

        altarCompound.setTag("currentAddedItems", currentAddedItemsList);

        if (addedPhylactery != null) {
            NBTTagCompound phylactery = new NBTTagCompound();
            addedPhylactery.writeToNBT(phylactery);
            altarCompound.setTag("phylactery", phylactery);
        }

        if (addedBindingCatalyst != null) {
            NBTTagCompound catalyst = new NBTTagCompound();
            addedBindingCatalyst.writeToNBT(catalyst);
            altarCompound.setTag("catalyst", catalyst);
        }

        if (!ebwizPendingBook.isEmpty()) {
            NBTTagCompound pendingBookTag = new NBTTagCompound();
            ebwizPendingBook.writeToNBT(pendingBookTag);
            altarCompound.setTag("ebwizPendingBook", pendingBookTag);
        }

        // ---- Discovery Ritual state ----
        if (discoveryRitualActive) {
            NBTTagCompound disc = new NBTTagCompound();
            disc.setInteger("phase", discoveryPhase);
            disc.setInteger("element", discoveryElementOrdinal);
            disc.setInteger("tier", discoveryTierOrdinal);
            disc.setInteger("ingredientIndex", discoveryIngredientIndex);
            disc.setFloat("accumulatedPower", ebwizAccumulatedPower);
            disc.setString("lastPlayer", lastCraftingPlayerName);
            altarCompound.setTag("discoveryRitual", disc);
        }

        //TODO CRAFTING Altar...

        NBTTagList shapeGroupData = new NBTTagList();
        for (KeyValuePair<ArrayList<SpellPart>, NBTTagCompound> list : shapeGroups) {
            shapeGroupData.appendTag(ISpellPartListToNBT(list));
        }
        altarCompound.setTag("shapeGroups", shapeGroupData);

        NBTTagCompound spellDefSave = ISpellPartListToNBT(new KeyValuePair<>(spellDef, savedData));
        altarCompound.setTag("spellDef", spellDefSave);

        nbttagcompound.setTag("altarData", altarCompound);
        return nbttagcompound;
    }

    private NBTTagCompound ISpellPartListToNBT(KeyValuePair<ArrayList<SpellPart>, NBTTagCompound> spellDef2) {
        return SpellUtils.encode(spellDef2);
    }

    private KeyValuePair<ArrayList<SpellPart>, NBTTagCompound> NBTToISpellPartList(NBTTagCompound compound) {
        return SpellUtils.decode(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);

        if (!nbttagcompound.hasKey("altarData"))
            return;

        NBTTagCompound altarCompound = nbttagcompound.getCompoundTag("altarData");
        mimicState = Block.getStateById(altarCompound.getInteger("MimicState"));
        NBTTagList allAddedItems = altarCompound.getTagList("allAddedItems", Constants.NBT.TAG_COMPOUND);
        NBTTagList currentAddedItems = altarCompound.getTagList("currentAddedItems", Constants.NBT.TAG_COMPOUND);
        structureValid = altarCompound.getBoolean("StructureValid");
        this.isCrafting = altarCompound.getBoolean("isCrafting");
        this.currentKey = altarCompound.getInteger("currentKey");
        this.currentSpellName = altarCompound.getString("currentSpellName");

        if (altarCompound.hasKey("phylactery")) {
            NBTTagCompound phylactery = altarCompound.getCompoundTag("phylactery");
            if (phylactery != null)
                this.addedPhylactery = new ItemStack((phylactery));
        }

        if (altarCompound.hasKey("catalyst")) {
            NBTTagCompound catalyst = altarCompound.getCompoundTag("catalyst");
            if (catalyst != null)
                this.addedBindingCatalyst = new ItemStack((catalyst));
        }

        if (altarCompound.hasKey("ebwizPendingBook")) {
            this.ebwizPendingBook = new ItemStack(altarCompound.getCompoundTag("ebwizPendingBook"));
        } else {
            this.ebwizPendingBook = ItemStack.EMPTY;
        }

        // ---- Discovery Ritual state ----
        if (altarCompound.hasKey("discoveryRitual")) {
            NBTTagCompound disc = altarCompound.getCompoundTag("discoveryRitual");
            this.discoveryRitualActive = true;
            this.discoveryPhase = disc.getInteger("phase");
            this.discoveryElementOrdinal = disc.getInteger("element");
            this.discoveryTierOrdinal = disc.getInteger("tier");
            this.discoveryIngredientIndex = disc.getInteger("ingredientIndex");
            this.ebwizAccumulatedPower = disc.getFloat("accumulatedPower");
            this.lastCraftingPlayerName = disc.getString("lastPlayer");
            // Rebuild planned items if we have enough state
            if (discoveryPhase >= 2 && discoveryElementOrdinal >= 0 && discoveryTierOrdinal >= 0) {
                this.discoveryPlannedItems = EBWizardryCompatBootstrap.getDiscoveryIngredients(discoveryElementOrdinal, discoveryTierOrdinal);
            }
        } else {
            this.discoveryRitualActive = false;
            this.discoveryPhase = 0;
            this.discoveryElementOrdinal = -1;
            this.discoveryTierOrdinal = -1;
            this.discoveryIngredientIndex = 0;
            this.discoveryPlannedItems = null;
        }

        // ---- Discovery Ritual state ----
        if (altarCompound.hasKey("discoveryRitual")) {
            NBTTagCompound disc = altarCompound.getCompoundTag("discoveryRitual");
            this.discoveryRitualActive = true;
            this.discoveryPhase = disc.getInteger("phase");
            this.discoveryElementOrdinal = disc.getInteger("element");
            this.discoveryTierOrdinal = disc.getInteger("tier");
            this.discoveryIngredientIndex = disc.getInteger("ingredientIndex");
            this.ebwizAccumulatedPower = disc.getFloat("accumulatedPower");
            this.lastCraftingPlayerName = disc.getString("lastPlayer");
            // Rebuild planned items if we have enough state
            if (discoveryPhase >= 2 && discoveryElementOrdinal >= 0 && discoveryTierOrdinal >= 0) {
                this.discoveryPlannedItems = EBWizardryCompatBootstrap.getDiscoveryIngredients(discoveryElementOrdinal, discoveryTierOrdinal);
            }
        } else {
            this.discoveryRitualActive = false;
            this.discoveryPhase = 0;
            this.discoveryElementOrdinal = -1;
            this.discoveryTierOrdinal = -1;
            this.discoveryIngredientIndex = 0;
            this.discoveryPlannedItems = null;
        }

        this.allAddedItems.clear();
        for (int i = 0; i < allAddedItems.tagCount(); ++i) {
            NBTTagCompound addedItem = (NBTTagCompound) allAddedItems.getCompoundTagAt(i);
            if (addedItem == null)
                continue;
            ItemStack stack = new ItemStack((addedItem));
            if (stack.isEmpty())
                continue;
            this.allAddedItems.add(stack);
        }

        this.currentAddedItems.clear();
        for (int i = 0; i < currentAddedItems.tagCount(); ++i) {
            NBTTagCompound addedItem = (NBTTagCompound) currentAddedItems.getCompoundTagAt(i);
            if (addedItem == null)
                continue;
            ItemStack stack = new ItemStack((addedItem));
            if (stack.isEmpty())
                continue;
            this.currentAddedItems.add(stack);
        }

        this.spellDef.clear();
        for (KeyValuePair<ArrayList<SpellPart>, NBTTagCompound> groups : shapeGroups)
            groups.key.clear();

        NBTTagCompound currentSpellDef = altarCompound.getCompoundTag("spellDef");
        this.spellDef.addAll(NBTToISpellPartList(currentSpellDef).key);
        this.savedData.merge(NBTToISpellPartList(currentSpellDef).value);

        NBTTagList currentShapeGroups = altarCompound.getTagList("shapeGroups", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < currentShapeGroups.tagCount(); ++i) {
            NBTTagCompound compound = (NBTTagCompound) currentShapeGroups.getCompoundTagAt(i);
            try {
                shapeGroups.get(i).key.addAll(NBTToISpellPartList(compound).key);
                shapeGroups.get(i).value.merge(NBTToISpellPartList(compound).value);
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                shapeGroups.add(i, new KeyValuePair<ArrayList<SpellPart>, NBTTagCompound>(new ArrayList<>(), new NBTTagCompound()));
                shapeGroups.get(i).key.addAll(NBTToISpellPartList(compound).key);
                shapeGroups.get(i).value.merge(NBTToISpellPartList(compound).value);
            }
        }
    }

    @Override
    public int getChargeRate() {
        return 250;
    }

    @Override
    public boolean canRelayPower(PowerTypes type) {
        return false;
    }


    public void HandleUpdatePacket(byte[] remainingBytes) {
        AMDataReader rdr = new AMDataReader(remainingBytes, false);
        byte subID = rdr.getByte();
        switch (subID) {
            case FULL_UPDATE:
                this.isCrafting = rdr.getBoolean();
                this.currentKey = rdr.getInt();

                this.allAddedItems.clear();
                this.currentAddedItems.clear();

                int itemCount = rdr.getInt();
                for (int i = 0; i < itemCount; ++i)
                    this.allAddedItems.add(rdr.getItemStack());
                break;
            case CRAFTING_CHANGED:
                this.setCrafting(rdr.getBoolean());
                break;
            case COMPONENT_ADDED:
                this.allAddedItems.add(rdr.getItemStack());
                break;
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, this.getBlockMetadata(), getUpdateTag());
        return packet;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
        this.markDirty();
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void markDirty() {
        this.markForUpdate();
        super.markDirty();
    }

    @Override
    public void markForUpdate() {
        this.dirty = true;
    }

    @Override
    public boolean needsUpdate() {
        return this.dirty;
    }

    @Override
    public void clean() {
        this.dirty = false;
    }
}
