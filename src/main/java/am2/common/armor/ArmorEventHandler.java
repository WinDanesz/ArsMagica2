package am2.common.armor;

import am2.ArsMagica;
import am2.api.extensions.IAffinityData;
import am2.api.items.armor.ArmorImbuement;
import am2.api.items.armor.ImbuementApplicationTypes;
import am2.common.extensions.AffinityData;
import am2.common.items.AMArmor;
import am2.common.registry.AMBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

public class ArmorEventHandler {

    @SubscribeEvent
    public void onEntityLiving(LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer))
            return;
        if (!event.getEntityLiving().world.isRemote)
            ArmorHelper.HandleArmorInfusion((EntityPlayer) event.getEntityLiving());
        doInfusions(ImbuementApplicationTypes.ON_TICK, event, (EntityPlayer) event.getEntityLiving());
    }

    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer))
            return;

        doInfusions(ImbuementApplicationTypes.ON_HIT, event, (EntityPlayer) event.getEntityLiving());

        if (event.getEntityLiving() instanceof EntityPlayer)
            doXPInfusion((EntityPlayer) event.getEntityLiving(), 0.01f, Math.max(0.05f, Math.min(event.getAmount(), 5)));
    }

    @SubscribeEvent
    public void onEntityJump(LivingJumpEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer))
            return;

        doInfusions(ImbuementApplicationTypes.ON_JUMP, event, (EntityPlayer) event.getEntityLiving());
    }

    @SubscribeEvent
    public void onMiningSpeed(BreakSpeed event) {
        doInfusions(ImbuementApplicationTypes.ON_MINING_SPEED, event, (EntityPlayer) event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (event.getSource().getTrueSource() instanceof EntityPlayer)
            doXPInfusion((EntityPlayer) event.getSource().getTrueSource(), 1, Math.min(20, event.getEntityLiving().getMaxHealth()));

        if (!(event.getEntityLiving() instanceof EntityPlayer))
            return;

        doInfusions(ImbuementApplicationTypes.ON_DEATH, event, (EntityPlayer) event.getEntityLiving());
    }

    private void doInfusions(ImbuementApplicationTypes type, Event event, EntityPlayer player) {
        IAffinityData props = AffinityData.For(player);
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() != EntityEquipmentSlot.Type.ARMOR)
                continue;
            ArmorImbuement[] infusions = ArmorHelper.getInfusionsOnArmor(player, slot);
            for (ArmorImbuement inf : infusions) {
                if (inf.getApplicationTypes().contains(type)) {
                    if (inf.canApply(player)) {
                        if (inf.applyEffect(player, player.world, player.getItemStackFromSlot(slot), type, event)) {
                            if (inf.getCooldown() > 0) {
                                if (props.getCooldown(inf.getRegistryName().toString()) < inf.getCooldown()) {
                                    props.addCooldown(inf.getRegistryName().toString(), inf.getCooldown());
                                    if (player instanceof EntityPlayerMP)
                                        ArsMagica.proxy.blackoutArmorPiece((EntityPlayerMP) player, slot, inf.getCooldown());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void doXPInfusion(EntityPlayer player, float xpMin, float xpMax) {
        float amt = (float) ((player.world.rand.nextFloat() * xpMin + (xpMax - xpMin)) * ArsMagica.config.getArmorXPInfusionFactor());
        ArmorHelper.addXPToArmor(amt, player);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemArmor) {
            double xp = 0;
            int armorLevel = 0;
            String[] effects = new String[0];

            if (stack.hasTagCompound()) {
                NBTTagCompound armorCompound = (NBTTagCompound) stack.getTagCompound().getTag(AMArmor.NBT_KEY_AMPROPS);
                if (armorCompound != null) {
                    xp = armorCompound.getDouble(AMArmor.NBT_KEY_TOTALXP);
                    armorLevel = armorCompound.getInteger(AMArmor.NBT_KEY_ARMORLEVEL);
                    String effectsList = armorCompound.getString(AMArmor.NBT_KEY_EFFECTS);
                    if (effectsList != null && !effectsList.isEmpty()) {
                        effects = effectsList.split(AMArmor.INFUSION_DELIMITER);
                    }
                }
            }

            // Remove vanilla's "Dyed" / "Color: #XXXXXX" line for AMArmor;
            // we re-add it below only when LSHIFT is held.
            if (stack.getItem() instanceof AMArmor && ((AMArmor) stack.getItem()).hasColor(stack)) {
                String dyedText = net.minecraft.client.resources.I18n.format("item.dyed");
                String colorPrefix = net.minecraft.client.resources.I18n.format("item.color", "").replace("#", "").trim();
                event.getToolTip().removeIf(line -> {
                    String plain = net.minecraft.util.text.TextFormatting.getTextWithoutFormattingCodes(line);
                    return plain != null && (plain.equals(dyedText) || plain.startsWith(colorPrefix));
                });
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                event.getToolTip().add(net.minecraft.client.resources.I18n.format("am2.tooltip.armorxp", String.format("%.2f", xp)));
                event.getToolTip().add(net.minecraft.client.resources.I18n.format("am2.tooltip.armorlevel", armorLevel));
                if (effects.length > 0)
                    event.getToolTip().add(net.minecraft.client.resources.I18n.format("am2.toolip.infusions"));
                for (String s : effects) {
                    event.getToolTip().add("-" + net.minecraft.client.resources.I18n.format("am2.tooltip." + s.replaceAll("arsmagica2:", "")));
                }
                // Show color hex code for dyed AMArmor
                if (stack.getItem() instanceof AMArmor && ((AMArmor) stack.getItem()).hasColor(stack)) {
                    int color = ((AMArmor) stack.getItem()).getColor(stack);
                    event.getToolTip().add(net.minecraft.client.resources.I18n.format("item.color", String.format("#%06X", color)));
                }

            } else {
                event.getToolTip().add(net.minecraft.client.resources.I18n.format("am2.tooltip.shiftForDetails"));
            }
        } else if (stack.getItem() instanceof ItemBlock) {
            if (((ItemBlock) stack.getItem()).getBlock() == AMBlocks.mana_battery) {
                if (stack.hasTagCompound()) {
                    NBTTagList list = stack.getTagCompound().getTagList("Lore", Constants.NBT.TAG_COMPOUND);
                    if (list != null) {
                        for (int i = 0; i < list.tagCount(); ++i) {
                            NBTBase tag = list.getCompoundTagAt(i);
                            if (tag instanceof NBTTagString) {
                                event.getToolTip().add((((NBTTagString) tag).getString()));
                            }
                        }
                    }
                }
            }
        }
    }
}
