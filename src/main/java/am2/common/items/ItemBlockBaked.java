package am2.common.items;

import am2.client.render.AMItemStackRenderer;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockBaked extends ItemBlock {

    public ItemBlockBaked(Block block) {
        super(block);
        if (FMLCommonHandler.instance().getSide().isClient()) {
            setTileEntityItemStackRendererClient();
        }
    }

    @SideOnly(Side.CLIENT)
    private void setTileEntityItemStackRendererClient() {
        this.setTileEntityItemStackRenderer(new AMItemStackRenderer());
    }

}
