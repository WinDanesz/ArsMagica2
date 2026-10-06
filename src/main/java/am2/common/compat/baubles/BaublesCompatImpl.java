package am2.common.compat.baubles;

import am2.ArsMagica;
import am2.common.items.ItemSpellBook;
import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The part of the Baubles integration that touches the Baubles API. Only ever loaded through
 * {@link BaublesCompat}, after it has confirmed Baubles is installed. Public only because Forge's
 * event bus cannot invoke handlers on a package-private class; do not use it directly.
 */
public final class BaublesCompatImpl {

    private static final ResourceLocation CAPABILITY_KEY = new ResourceLocation(ArsMagica.MODID, "bauble");

    /** One stateless provider per item; the stack is passed into every call, so it can be shared. */
    private static final Map<Item, BaubleProvider> PROVIDERS = new ConcurrentHashMap<>();

    /**
     * Gives every {@link IBaubleItem} stack Baubles' item capability, which is what Baubles checks
     * to decide whether a stack fits a slot (it does not require the item to implement IBauble).
     */
    @SubscribeEvent
    public void onAttachItemStackCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.getItem() instanceof IBaubleItem) {
            event.addCapability(CAPABILITY_KEY, PROVIDERS.computeIfAbsent(stack.getItem(), i -> new BaubleProvider((IBaubleItem) i)));
        }
    }

    @Nullable
    private static IBaublesItemHandler getHandler(EntityPlayer player) {
        if (player == null || !player.hasCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null)) return null;
        return BaublesApi.getBaublesHandler(player);
    }

    static ItemStack findSpellBook(EntityPlayer player) {
        IBaublesItemHandler handler = getHandler(player);
        if (handler == null) return ItemStack.EMPTY;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemSpellBook) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    static void writeSpellBook(EntityPlayer player, ItemStack bookStack) {
        IBaublesItemHandler handler = getHandler(player);
        if (handler == null) return;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemSpellBook) {
                handler.setStackInSlot(i, bookStack);
                return;
            }
        }
    }

    static boolean isEquipped(EntityPlayer player, Item item) {
        IBaublesItemHandler handler = getHandler(player);
        if (handler == null) return false;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }

    private static final class BaubleProvider implements ICapabilityProvider, IBauble {

        private final IBaubleItem item;

        BaubleProvider(IBaubleItem item) {
            this.item = item;
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE
                    ? BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(this)
                    : null;
        }

        @Override
        public BaubleType getBaubleType(ItemStack stack) {
            return BaubleType.valueOf(item.getBaubleSlot(stack).name());
        }

        @Override
        public void onWornTick(ItemStack stack, EntityLivingBase entity) {
            item.onWornTick(stack, entity);
        }
    }
}
