package am2.common.blocks;

import am2.api.blocks.MultiblockDiagnosis;
import am2.common.blocks.tileentity.TileEntityCraftingAltar;
import am2.common.registry.AMTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.Map;

public class BlockCraftingAltar extends BlockAMPowered {

    public static final PropertyBool MIMIC = PropertyBool.create("mimic");

    public BlockCraftingAltar() {
        super(Material.ROCK);
        setCreativeTab(AMTabs.AMBLOCKS);
        this.setHardness(1.5f);
        this.setResistance(10.0f);
        this.setHarvestLevel("pickaxe", 0);
        setDefaultState(blockState.getBaseState().withProperty(MIMIC, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MIMIC);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        // bye bye TESR
        TileEntityCraftingAltar te = getTileEntity(worldIn, pos);
        return te != null &&  te.isStructureValid() && te.getMimicState() != null ? te.getMimicState() : state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCraftingAltar();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntityCraftingAltar te = getTileEntity(worldIn, pos);
            if (te != null) {
                playerIn.sendStatusMessage(new TextComponentTranslation(te.isStructureValid() ? "am2.tooltip.altarStructureValid" : "am2.tooltip.altarStructureInvalid"), true);
                if (hand == EnumHand.MAIN_HAND) {
                    MultiblockDiagnosis diagnosis = te.diagnoseStructure();
                    if (diagnosis != null)
                        sendStructureHints(playerIn, diagnosis);
                }
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
    }

    /**
     * Tells the player which blocks the altar structure is still missing, grouped by block type.
     */
    private static void sendStructureHints(EntityPlayer player, MultiblockDiagnosis diagnosis) {
        Map<String, ITextComponent> names = new LinkedHashMap<>();
        Map<String, Integer> needed = new LinkedHashMap<>();
        Map<String, Integer> wrongFacing = new LinkedHashMap<>();
        for (MultiblockDiagnosis.Problem problem : diagnosis.getProblems()) {
            ITextComponent name = describe(problem);
            String key = name instanceof TextComponentTranslation ? ((TextComponentTranslation) name).getKey() : name.getUnformattedText();
            names.putIfAbsent(key, name);
            (problem.wrongState ? wrongFacing : needed).merge(key, 1, Integer::sum);
        }
        if (!needed.isEmpty())
            player.sendMessage(new TextComponentTranslation("am2.tooltip.altarStructureNeeded", joinCounts(needed, names)));
        if (!wrongFacing.isEmpty())
            player.sendMessage(new TextComponentTranslation("am2.tooltip.altarStructureWrongFacing", joinCounts(wrongFacing, names)));
    }

    private static ITextComponent describe(MultiblockDiagnosis.Problem problem) {
        IBlockState expected = problem.expected;
        if (expected == null) {
            // Material not deducible yet
            if (problem.groupName.startsWith("catalysts"))
                return new TextComponentTranslation("am2.tooltip.altarPart.catalyst");
            if (problem.groupName.startsWith("out"))
                return new TextComponentTranslation(problem.typeId == 0 ? "am2.tooltip.altarPart.structureBlock" : "am2.tooltip.altarPart.structureStairs");
            return new TextComponentString(problem.groupName);
        }
        Block block = expected.getBlock();
        ItemStack stack = new ItemStack(block, 1, block.damageDropped(expected));
        return new TextComponentTranslation((stack.isEmpty() ? block.getTranslationKey() : stack.getTranslationKey()) + ".name");
    }

    private static ITextComponent joinCounts(Map<String, Integer> counts, Map<String, ITextComponent> names) {
        ITextComponent list = new TextComponentString("");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (!first)
                list.appendText(", ");
            first = false;
            list.appendText(entry.getValue() + "x ");
            list.appendSibling(names.get(entry.getKey()).createCopy());
        }
        return list;
    }

    public BlockCraftingAltar registerAndName(ResourceLocation rl) {
        this.setTranslationKey(rl.toString());
        // TODO: registry GameRegistry.register(this, rl);
        // TODO: registry GameRegistry.register(new ItemBlock(this), rl);
        return this;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.SOLID;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return true;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return true;
    }

    public static TileEntityCraftingAltar getTileEntity(IBlockAccess world, BlockPos pos) {
        if (world == null)
            return null;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityCraftingAltar))
            return null;

        return (TileEntityCraftingAltar) te;
    }


}
