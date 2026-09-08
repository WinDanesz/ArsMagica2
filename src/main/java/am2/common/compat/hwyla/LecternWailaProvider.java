package am2.common.compat.hwyla;

import am2.common.blocks.tileentity.TileEntityLectern;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Shows what's actually placed on the Lectern (if anything) as its Hwyla tooltip name, instead of
 * the plain "Lectern" - reads {@code tile.arsmagica2:lectern.name.waila} as a {@code %s} format
 * string, substituting the held stack's own display name, or the plain
 * {@code tile.arsmagica2:lectern.name} when the lectern is empty.
 *
 * <p>Deliberately a separate key from {@code tile.arsmagica2:lectern.name}: that key is the
 * block's canonical, argument-free name (used by vanilla's {@code ItemBlock.getItemStackDisplayName},
 * JEI, anvils, etc.), so it can never be a format template - only this Waila-only key is.
 */
@SideOnly(Side.CLIENT)
public class LecternWailaProvider implements IWailaDataProvider {

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity te = accessor.getTileEntity();
        if (!(te instanceof TileEntityLectern))
            return currenttip;

        ItemStack held = ((TileEntityLectern) te).getStack();
        String formatted = held.isEmpty()
                ? I18n.format("tile.arsmagica2:lectern.name")
                : I18n.format("tile.arsmagica2:lectern.name.waila", held.getDisplayName());

        if (currenttip.isEmpty()) {
            currenttip.add(formatted);
        } else {
            currenttip.set(0, formatted);
        }
        return currenttip;
    }
}
