package am2.common.items;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.SkillPointRegistry;
import am2.api.items.IMultiTexturedItem;
import am2.api.skill.SkillPoint;
import am2.common.extensions.EntityExtension;
import am2.common.extensions.SkillData;
import am2.common.registry.AMTabs;
import am2.common.utils.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemInfinityOrb extends Item implements IMultiTexturedItem {

    public ItemInfinityOrb() {
        super();
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setCreativeTab(AMTabs.AMITEMS);
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        SkillPoint point = SkillPointRegistry.getPointForTier(playerIn.getHeldItem(handIn).getItemDamage());
        if (point == null) playerIn.sendMessage(new TextComponentString("Broken Item : Please use a trash bin."));
        playerIn.setHeldItem(handIn, doGiveSkillPoints(playerIn, playerIn.getHeldItem(handIn), point));
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;
        items.add(new ItemStack(this, 1, 0));
        items.add(new ItemStack(this, 1, 1));
        items.add(new ItemStack(this, 1, 2));
        // only used as icon for the last Advancement
        if (ArsMagicaAPI.hasTier4()) {
            items.add(new ItemStack(this, 1, 3));
        }
        if (ArsMagicaAPI.hasTier5()) {
            items.add(new ItemStack(this, 1, 4));
        }
        if (ArsMagicaAPI.hasTier6()) {
            items.add(new ItemStack(this, 1, 5));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Nonnull
    public String getItemStackDisplayName(ItemStack stack) {
        SkillPoint point = SkillPointRegistry.getPointForTier(stack.getItemDamage());
        if (point == null) return "Unavailable Item";
        return point.getChatColor() + I18n.format("item.arsmagica2:infinity_orb_" + point.toString().toLowerCase() + ".name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        SkillPoint point = SkillPointRegistry.getPointForTier(stack.getItemDamage());
        if (point == null) {
            tooltip.add(I18n.format("am2.tooltip.infOrbConvert"));
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player != null && EntityExtension.For(player).getCurrentLevel() > 0) {
            tooltip.add(I18n.format("am2.tooltip.infOrbUse", point.getChatColor() + point.getName()));
        } else {
            tooltip.add(I18n.format("am2.tooltip.infOrbUnknown"));
        }
    }

    private static final ResourceLocation COMPENDIUM_ADVANCEMENT = new ResourceLocation("arsmagica2:compendium_data");

    private boolean hasCompendiumAchievement(EntityPlayer player) {
        if (!player.world.isRemote) {
            return EntityUtils.playerHasAdvancement(player, COMPENDIUM_ADVANCEMENT);
        }
        // Client side: fall back to level check (level is synced from server)
        return EntityExtension.For(player).getCurrentLevel() > 0;
    }

    private ItemStack doGiveSkillPoints(EntityPlayer player, ItemStack stack, SkillPoint type) {
        if (hasCompendiumAchievement(player)) {
            if (!player.world.isRemote)
                SkillData.For(player).setSkillPoint(type, SkillData.For(player).getSkillPoint(type) + 1);
            if (player.world.isRemote) {
                player.sendMessage(new TextComponentString(I18n.format("am2.tooltip.infOrb" + type.toString())));
            }
            if (!player.capabilities.isCreativeMode)
                stack.shrink(1);
            if (stack.getCount() < 1) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
            }
        } else {
            if (player.world.isRemote) {
                int message = player.world.rand.nextInt(10);
                player.sendMessage(new TextComponentString(I18n.format("am2.tooltip.infOrbFail" + message)));
            }
        }
        return stack;
    }


    @Override
    public void onUpdate(ItemStack stack, @Nonnull World worldIn, @Nonnull Entity entityIn, int itemSlot, boolean isSelected) {
        SkillPoint point = SkillPointRegistry.getPointForTier(stack.getItemDamage());
        if (point == null && stack.getItemDamage() > 0) stack.setItemDamage(stack.getItemDamage() - 1);
    }

    @Override
    public ResourceLocation getModelName(ItemStack stack) {
        int meta = stack.getMetadata();

        if (meta == 0) return new ResourceLocation(ArsMagica.MODID, "infinity_orb_blue");
        if (meta == 1) return new ResourceLocation(ArsMagica.MODID, "infinity_orb_green");
        if (meta == 2) return new ResourceLocation(ArsMagica.MODID, "infinity_orb_red");
        if (meta == 3) return new ResourceLocation(ArsMagica.MODID, "infinity_orb_cyan");
        if (meta == 4) return new ResourceLocation(ArsMagica.MODID, "infinity_orb_magenta");

        return new ResourceLocation(ArsMagica.MODID, "infinity_orb");
    }
}
