package am2.api.compendium;

import java.util.ArrayList;
import java.util.List;

import am2.api.blocks.IMultiblock;
import am2.api.blocks.IMultiblockGroup;
import am2.client.gui.AMGuiHelper;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

public class AdvancedBlockRenderer {
	
	private BlockRenderWorld iblockaccess;
	
	public AdvancedBlockRenderer(BlockRenderWorld iblockaccess) {
		this.iblockaccess = iblockaccess;
	}

	public void renderMultiblock(IMultiblock multiblock, int layer, boolean clear) {
		if (multiblock == null)
			return;
		if (clear)
			iblockaccess.clear();
		if (layer < 0)
			layer = Integer.MAX_VALUE;
		int minY = multiblock.getMinY();
		for (List<IMultiblockGroup> groups : multiblock.getMultiblockGroups()) {
			if (groups.isEmpty())
				continue;
			int id = AMGuiHelper.instance.getSlowTicker() % groups.size();
			IMultiblockGroup group = groups.get(id);
			for (BlockPos pos : group.getPositions()) {
				if (pos.getY() > layer + minY)
					continue;
				ArrayList<IBlockState> states = group.getState(pos);
				if (states.isEmpty())
					continue;
				int stateSelector = AMGuiHelper.instance.getSlowTicker() % states.size();
				IBlockState state = states.get(stateSelector);
				iblockaccess.setBlockState(pos, state);
			}
		}
		
		for (List<IMultiblockGroup> groups : multiblock.getMultiblockGroups()) {
			if (groups.isEmpty())
				continue;
			int id = AMGuiHelper.instance.getSlowTicker() % groups.size();
			IMultiblockGroup group = groups.get(id);
			for (BlockPos pos : group.getPositions()) {
				if (pos.getY() > layer + minY)
					continue;
				IBlockState state = iblockaccess.getBlockState(pos);
				Tessellator.getInstance().getBuffer().begin(7, DefaultVertexFormats.BLOCK);
				Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(state, pos, iblockaccess, Tessellator.getInstance().getBuffer());
				Tessellator.getInstance().draw();
				try {
					if (state.getBlock() instanceof ITileEntityProvider) {
						TileEntityRendererDispatcher.instance.render(((ITileEntityProvider)state.getBlock()).createNewTileEntity(Minecraft.getMinecraft().world, state.getBlock().getMetaFromState(state)), pos.getX(), pos.getY(), pos.getZ(), Minecraft.getMinecraft().getRenderPartialTicks(), 0);
					
					}
				} catch (Exception e) {
					//We tried...
				}
			}
		}
	}
	
	public void renderBlock(IBlockState state, boolean clear) {
		if (state == null)
			return;
		if (clear)
			iblockaccess.clear();
		Tessellator.getInstance().getBuffer().begin(7, DefaultVertexFormats.BLOCK);
		iblockaccess.setBlockState(BlockPos.ORIGIN, state);
		Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(state, BlockPos.ORIGIN, iblockaccess, Tessellator.getInstance().getBuffer());
		Tessellator.getInstance().draw();
	}
}
