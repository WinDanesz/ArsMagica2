package am2.common.compat.hwyla;

import am2.ArsMagica;
import am2.common.blocks.BlockLectern;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Hwyla/WAILA integration entry point, discovered automatically by Hwyla's own FML annotation
 * scan (looking for {@code @WailaPlugin}). This class is never referenced from our own
 * always-loaded code, so it's safe to ship even when Hwyla isn't installed: Hwyla's discovery is
 * the only thing that would ever load it, and that scan simply doesn't run without Hwyla present.
 */
@SideOnly(Side.CLIENT)
@WailaPlugin(ArsMagica.MODID)
public class AMWailaPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaRegistrar registrar) {
        registrar.registerHeadProvider(new LecternWailaProvider(), BlockLectern.class);
    }
}
