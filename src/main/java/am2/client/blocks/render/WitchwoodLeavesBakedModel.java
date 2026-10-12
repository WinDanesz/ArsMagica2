package am2.client.blocks.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Picks the transparent (fancy) or the opaque (fast) witchwood leaves model depending on the graphics setting.
 * Chunks are rebuilt when the setting changes, so this is only evaluated at bake time of a chunk.
 */
@SideOnly(Side.CLIENT)
public class WitchwoodLeavesBakedModel extends BakedModelWrapper<IBakedModel> {

    private final IBakedModel fastModel;

    public WitchwoodLeavesBakedModel(IBakedModel fancyModel, IBakedModel fastModel) {
        super(fancyModel);
        this.fastModel = fastModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return Minecraft.getMinecraft().gameSettings.fancyGraphics
                ? originalModel.getQuads(state, side, rand)
                : fastModel.getQuads(state, side, rand);
    }
}
