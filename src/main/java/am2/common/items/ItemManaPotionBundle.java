package am2.common.items;

import am2.api.items.IMultiTexturedItem;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMItems;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemManaPotionBundle extends Item implements IMultiTexturedItem {

    public ItemManaPotionBundle() {
        super();
        setMaxDamage(0);
        setMaxStackSize(1);
        setHasSubtypes(true);
    }

    private Item getPotion(int damage) {
        int id = damage >> 8;
        switch (id) {
            case 0:
                return AMItems.lesser_mana_potion;
            case 1:
                return AMItems.standard_mana_potion;
            case 2:
                return AMItems.greater_mana_potion;
            case 3:
                return AMItems.epic_mana_potion;
            case 4:
                return AMItems.legendary_mana_potion;
        }
        return AMItems.lesser_mana_potion;
    }

    private int getUses(int damage) {
        return (damage & 0x0F);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        EntityExtension props = EntityExtension.For(player);
        if (props.getCurrentMana() < props.getMaxMana()) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World par2World, EntityLivingBase ent) {
        if (!(ent instanceof EntityPlayer)) {
            return super.onItemUseFinish(stack, par2World, ent);
        }
        EntityPlayer par3EntityPlayer = (EntityPlayer) ent;
        Item potion = getPotion(stack.getItemDamage());
        if (potion == AMItems.lesser_mana_potion) {
            AMItems.lesser_mana_potion.onItemUseFinish(stack, par2World, par3EntityPlayer);
        } else if (potion == AMItems.standard_mana_potion) {
            AMItems.standard_mana_potion.onItemUseFinish(stack, par2World, par3EntityPlayer);
        } else if (potion == AMItems.greater_mana_potion) {
            AMItems.greater_mana_potion.onItemUseFinish(stack, par2World, par3EntityPlayer);
        } else if (potion == AMItems.epic_mana_potion) {
            AMItems.epic_mana_potion.onItemUseFinish(stack, par2World, par3EntityPlayer);
        } else if (potion == AMItems.legendary_mana_potion) {
            AMItems.legendary_mana_potion.onItemUseFinish(stack, par2World, par3EntityPlayer);
        }

        stack.setItemDamage(((stack.getItemDamage() >> 8) << 8) + getUses(stack.getItemDamage()) - 1);

        if (getUses(stack.getItemDamage()) == 0) {
            giveOrDropItem(par3EntityPlayer, new ItemStack(Items.STRING));
            if (stack.getCount() == 0) {
                stack = ItemStack.EMPTY;
            }
        }

        giveOrDropItem(par3EntityPlayer, new ItemStack(Items.GLASS_BOTTLE));

        return stack;
    }

    private void giveOrDropItem(EntityPlayer player, ItemStack stack) {
        if (!player.inventory.addItemStackToInventory(stack)) {
            player.dropItem(stack, true);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        Item potion = getPotion(stack.getItemDamage());
        if (potion == AMItems.lesser_mana_potion) {
            tooltip.add("Lesser Mana Restoration");
        } else if (potion == AMItems.standard_mana_potion) {
            tooltip.add("Standard Mana Restoration");
        } else if (potion == AMItems.greater_mana_potion) {
            tooltip.add("Greater Mana Restoration");
        } else if (potion == AMItems.epic_mana_potion) {
            tooltip.add("Epic Mana Restoration");
        } else if (potion == AMItems.legendary_mana_potion) {
            tooltip.add("Legendary Mana Restoration");
        }
        tooltip.add("" + getUses(stack.getItemDamage()) + " " + I18n.translateToLocalFormatted("am2.tooltip.uses") + ".");
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;

        items.add(new ItemStack(AMItems.mana_potion_bundle, 1, 3));
        items.add(new ItemStack(AMItems.mana_potion_bundle, 1, (1 << 8) + 3));
        items.add(new ItemStack(AMItems.mana_potion_bundle, 1, (2 << 8) + 3));
        items.add(new ItemStack(AMItems.mana_potion_bundle, 1, (3 << 8) + 3));
        items.add(new ItemStack(AMItems.mana_potion_bundle, 1, (4 << 8) + 3));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        Item potion = getPotion(stack.getItemDamage());
        if (potion == AMItems.lesser_mana_potion) {
            return String.format("%s %s", I18n.translateToLocalFormatted("item.arsmagica2:lesser_mana_potion.name"), I18n.translateToLocalFormatted("item.arsmagica2:potion_bundle.name"));
        } else if (potion == AMItems.standard_mana_potion) {
            return String.format("%s %s", I18n.translateToLocalFormatted("item.arsmagica2:standard_mana_potion.name"), I18n.translateToLocalFormatted("item.arsmagica2:potion_bundle.name"));
        } else if (potion == AMItems.greater_mana_potion) {
            return String.format("%s %s", I18n.translateToLocalFormatted("item.arsmagica2:greater_mana_potion.name"), I18n.translateToLocalFormatted("item.arsmagica2:potion_bundle.name"));
        } else if (potion == AMItems.epic_mana_potion) {
            return String.format("%s %s", I18n.translateToLocalFormatted("item.arsmagica2:epic_mana_potion.name"), I18n.translateToLocalFormatted("item.arsmagica2:potion_bundle.name"));
        } else if (potion == AMItems.legendary_mana_potion) {
            return String.format("%s %s", I18n.translateToLocalFormatted("item.arsmagica2:legendary_mana_potion.name"), I18n.translateToLocalFormatted("item.arsmagica2:potion_bundle.name"));
        }
        return "? " + I18n.translateToLocalFormatted("am2.items.bundle");
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        Item potion = getPotion(stack.getItemDamage());
        if (potion instanceof ItemManaPotion) {
            return potion.getRarity(stack);
        }
        return EnumRarity.COMMON;
    }

    @Override
    public ResourceLocation getModelName(ItemStack stack) {
        int meta = stack.getItemDamage();
        return new ResourceLocation(getPotion(meta).getRegistryName() + "_bundle");
    }
}
