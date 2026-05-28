package am2.common.items;

import am2.ArsMagica;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMItems;
import am2.common.registry.AMPotions;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemManaPotion extends Item {

    public ItemManaPotion() {
        super();
        this.setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack par1ItemStack) {
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        EntityExtension props = EntityExtension.For(player);
        if (props.getCurrentMana() < props.getMaxMana()) {
            player.setActiveHand(hand);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
    }


    private float getManaRestored() {
        if (this == AMItems.lesser_mana_potion) {
            return ArsMagica.config.getLesserManaPotionMana();
        } else if (this == AMItems.standard_mana_potion) {
            return ArsMagica.config.getStandardManaPotionMana();
        } else if (this == AMItems.greater_mana_potion) {
            return ArsMagica.config.getGreaterManaPotionMana();
        } else if (this == AMItems.epic_mana_potion) {
            return ArsMagica.config.getEpicManaPotionMana();
        } else if (this == AMItems.legendary_mana_potion) {
            return ArsMagica.config.getLegendaryManaPotionMana();
        }
        return 0;
    }

    private int getManaRegenLevel() {
        if (this == AMItems.lesser_mana_potion) {
            return ArsMagica.config.getLesserManaPotionRegenLevel();
        } else if (this == AMItems.standard_mana_potion) {
            return ArsMagica.config.getStandardManaPotionRegenLevel();
        } else if (this == AMItems.greater_mana_potion) {
            return ArsMagica.config.getGreaterManaPotionRegenLevel();
        } else if (this == AMItems.epic_mana_potion) {
            return ArsMagica.config.getEpicManaPotionRegenLevel();
        } else if (this == AMItems.legendary_mana_potion) {
            return ArsMagica.config.getLegendaryManaPotionRegenLevel();
        }
        return 0;
    }

    private int getManaRegenDuration() {
        if (this == AMItems.lesser_mana_potion) {
            return ArsMagica.config.getLesserManaPotionRegenDuration();
        } else if (this == AMItems.standard_mana_potion) {
            return ArsMagica.config.getStandardManaPotionRegenDuration();
        } else if (this == AMItems.greater_mana_potion) {
            return ArsMagica.config.getGreaterManaPotionRegenDuration();
        } else if (this == AMItems.epic_mana_potion) {
            return ArsMagica.config.getEpicManaPotionRegenDuration();
        } else if (this == AMItems.legendary_mana_potion) {
            return ArsMagica.config.getLegendaryManaPotionRegenDuration();
        }
        return 600;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World par2World, EntityLivingBase player) {
        stack = new ItemStack(Items.GLASS_BOTTLE);
        EntityExtension.For(player).setCurrentMana(EntityExtension.For(player).getCurrentMana() + getManaRestored());

        if (!par2World.isRemote) {
            player.addPotionEffect(new PotionEffect(AMPotions.mana_regeneration, getManaRegenDuration(), getManaRegenLevel()));
        }

        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.translateToLocalFormatted("am2.tooltip.mana_restored", (int) getManaRestored()));
        int regenLevel = getManaRegenLevel();
        int regenDuration = getManaRegenDuration() / 20;
        tooltip.add(I18n.translateToLocalFormatted("am2.tooltip.mana_regen_potion", regenLevel + 1, regenDuration));
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        if (this == AMItems.greater_mana_potion) {
            return EnumRarity.UNCOMMON;
        } else if (this == AMItems.epic_mana_potion) {
            return EnumRarity.RARE;
        } else if (this == AMItems.legendary_mana_potion) {
            return EnumRarity.EPIC;
        }
        return EnumRarity.COMMON;
    }

    @Override
    public boolean getHasSubtypes() {
        return false;
    }
}
