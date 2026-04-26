package am2.common.items;

import am2.api.affinity.Affinity;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemEssence extends Item {

    private final String affinityName;
    private Affinity cachedAffinity;

    public ItemEssence(String affinityName) {
        super();
        this.affinityName = affinityName;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        Affinity affinity = getAffinity();
        if (affinity != null) {
            return I18n.format("item.arsmagica2:essence.name", affinity.getDisplayName());
        }
        return super.getItemStackDisplayName(stack);
    }

    @Nullable
    public Affinity getAffinity() {
        if (cachedAffinity == null) {
            ResourceLocation location = new ResourceLocation(affinityName);
            cachedAffinity = Affinity.registry.getValue(location);
        }
        return cachedAffinity;
    }
}
