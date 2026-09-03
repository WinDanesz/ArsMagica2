package am2.common.items;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.extensions.IAffinityData;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemAffinityTome extends Item {

    private final String affinityName;
    private Affinity cachedAffinity;

    public ItemAffinityTome(String affinityName) {
        super();
        this.affinityName = affinityName;
        setMaxStackSize(16);
    }

    @Nullable
    public Affinity getAffinity() {
        if (cachedAffinity == null) {
            ResourceLocation location = new ResourceLocation(affinityName);
            cachedAffinity = Affinity.registry.getValue(location);
        }
        return cachedAffinity;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        Affinity affinity = getAffinity();
        IAffinityData data = AffinityData.For(player);

        if (affinity == Affinities.none) {
            boolean hasAffinity = false;
            for (Affinity aff : ArsMagicaAPI.getAffinityRegistry().getValues()) {
                if (data.getAffinityDepth(aff) > 0) {
                    hasAffinity = true;
                    break;
                }
            }
            if (!hasAffinity) {
                return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemStack);
            }
            if (!world.isRemote) {
                data.setLocked(false);
                for (Affinity aff : ArsMagicaAPI.getAffinityRegistry().getValues()) {
                    data.setAffinityDepth(aff, data.getAffinityDepth(aff) * AffinityData.MAX_DEPTH - 20);
                }
            }
        } else {
            if (data.getAffinityDepth(affinity) >= 1.0) {
                return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemStack);
            }
            if (!world.isRemote) {
                data.incrementAffinity(affinity, 20);
            }
        }

        if (!world.isRemote) {
            itemStack.shrink(1);
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        Affinity affinity = getAffinity();
        if (affinity != null) {
            return I18n.translateToLocalFormatted("item.arsmagica2:affinity_tome.name", affinity.getDisplayName());
        }
        return super.getItemStackDisplayName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        Affinity affinity = getAffinity();
        if (affinity == Affinities.none) {
            tooltip.add(I18n.translateToLocalFormatted("am2.tooltip.affinity_tome_none"));
        } else {
            tooltip.add(I18n.translateToLocalFormatted("am2.tooltip.affinity_tome_boost"));
        }
    }
}
