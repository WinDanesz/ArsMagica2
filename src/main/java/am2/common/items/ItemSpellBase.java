package am2.common.items;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.extensions.IEntityExtension;
import am2.api.extensions.ISpellCaster;
import am2.api.spell.*;
import am2.common.defs.IDDefs;
import am2.common.extensions.EntityExtension;
import am2.common.extensions.SkillData;
import am2.common.spell.SpellCaster;
import am2.common.utils.EntityUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemSpellBase extends Item {

    public ItemSpellBase() {
        super();
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable net.minecraft.nbt.NBTTagCompound nbt) {
        return new SpellCaster();
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        return EnumAction.BOW;
    }

    /**
     * Key used to embed SpellCaster capability data in the share tag (NBTTagCompound sent over network).
     * Item capabilities are NOT automatically synced in Forge 1.12.2 - only the regular tagCompound
     * is transmitted via {@link net.minecraft.network.PacketBuffer#writeItemStack}. We embed the capability data here so
     * the client receives and can restore spell parts, enabling correct spell casting.
     */
    private static final String KEY_SPELL_CASTER_CAPS = "SpellCasterCaps";
    /**
     * Cached base mana cost stored as a plain float in the item's tagCompound so it is always
     * reliably transmitted and available for tooltip display without depending on capability sync.
     */
    public static final String KEY_MANA_COST_CACHED = "ManaCostCached";

    @Override
    @Nullable
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        NBTTagCompound tag = super.getNBTShareTag(stack);
        ISpellCaster caster = SpellCaster.of(stack);
        if (caster instanceof SpellCaster) {
            if (tag == null) tag = new NBTTagCompound();
            else tag = tag.copy();
            // Embed full capability data so spell casting works client-side too.
            tag.setTag(KEY_SPELL_CASTER_CAPS, ((SpellCaster) caster).serializeNBT());
            // Also cache the base mana cost as a plain float so tooltips never show 0
            // even if capability deserialization is delayed or fails.
            tag.setFloat(KEY_MANA_COST_CACHED, caster.getBaseManaCost(caster.getCurrentShapeGroup()));
        }
        return tag;
    }

    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        if (nbt != null && nbt.hasKey(KEY_SPELL_CASTER_CAPS)) {
            ISpellCaster caster = SpellCaster.of(stack);
            if (caster instanceof SpellCaster) {
                ((SpellCaster) caster).deserializeNBT(nbt.getTag(KEY_SPELL_CASTER_CAPS));
            }
            // Strip the large capability blob from the tag before storing it – the
            // plain-float ManaCostCached is kept so addInformation can read it.
            NBTTagCompound cleanNbt = nbt.copy();
            cleanNbt.removeTag(KEY_SPELL_CASTER_CAPS);
            super.readNBTShareTag(stack, cleanNbt.isEmpty() ? null : cleanNbt);
        } else {
            super.readNBTShareTag(stack, nbt);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack par1ItemStack) {
        if (par1ItemStack.getTagCompound() == null) {
            return "\247bMalformed Spell";
        }
        return "Unnamed Spell";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (!stack.hasTagCompound()) {
            return;
        }
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null || player.world == null) return;

        float manaCost;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(KEY_MANA_COST_CACHED)) {
            // Use the reliable plain-float cache synced via getNBTShareTag.
            manaCost = tag.getFloat(KEY_MANA_COST_CACHED);
            // Apply the player's current burnout multiplier (local data, always accurate).
            IEntityExtension ext = EntityExtension.For(player);
            if (ext != null) {
                manaCost *= (1 + (ext.getCurrentBurnout() / ext.getMaxBurnout()));
            }
        } else {
            // Fallback: read from capability (works in singleplayer / when cap is synced).
            ISpellCaster caster = SpellCaster.of(stack);
            if (caster == null) return;
            manaCost = caster.getManaCost(player.world, player);
        }
        tooltip.add(String.format("Mana Cost : %.1f", manaCost));
        if (flagIn.isAdvanced()) {
            if (tag != null && tag.hasKey("creatorName")) {
                String creator = tag.getString("creatorName");
                if (!creator.isEmpty()) {
                    tooltip.add("§7Created by " + creator);
                }
            }
            ISpellCaster caster = SpellCaster.of(stack);
            if (caster != null) {
                int currentGroup = caster.getCurrentShapeGroup();
                List<List<List<SpellPart>>> shapeGroups = caster.getShapeGroups();
                List<List<SpellPart>> common = caster.getSpellCommon();
                List<List<SpellPart>> stages = new java.util.ArrayList<>();
                if (currentGroup < shapeGroups.size()) {
                    stages.addAll(shapeGroups.get(currentGroup));
                }
                stages.addAll(common);
                if (!stages.isEmpty()) {
                    tooltip.add("§7Spell Parts:");
                    int stageIndex = 0;
                    for (List<SpellPart> stage : stages) {
                        if (stage == null || stage.isEmpty()) continue;
                        tooltip.add("§8  Stage " + (++stageIndex) + ":");
                        for (SpellPart part : stage) {
                            if (part == null) continue;
                            ResourceLocation regName = part.getRegistryName();
                            if (regName == null) continue;
                            String locKey = "skill." + regName.toString() + ".name";
                            String partName = I18n.format(locKey);
                            String prefix;
                            if (part instanceof SpellShape) {
                                prefix = "§6    [Shape] ";
                            } else if (part instanceof SpellModifier) {
                                prefix = "§b    [Modifier] ";
                            } else {
                                prefix = "§a    [Component] ";
                            }
                            tooltip.add(prefix + partName);
                        }
                    }
                }
                Map<Affinity, Float> affinityShift = new HashMap<>();
                for (List<SpellPart> stage : stages) {
                    if (stage == null) continue;
                    for (SpellPart part : stage) {
                        if (!(part instanceof SpellComponent)) continue;
                        SpellComponent comp = (SpellComponent) part;
                        for (Affinity aff : comp.getAffinity()) {
                            float shift = comp.getAffinityShift(aff);
                            affinityShift.merge(aff, shift, Float::sum);
                        }
                    }
                }
                if (!affinityShift.isEmpty()) {
                    tooltip.add("§7Affinity Gain:");
                    affinityShift.entrySet().stream()
                        .filter(e -> e.getValue() != null && e.getValue() != 0f)
                        .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                        .forEach(e -> tooltip.add(String.format("§5  %s: §d%.2f", e.getKey().getLocalizedName(), e.getValue())));
                }
            }
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 72000;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.hasTagCompound()) {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }
        if (!stack.hasDisplayName()) {
            if (!world.isRemote) {
                player.openGui(ArsMagica.instance, IDDefs.GUI_SPELL_CUSTOMIZATION, world, (int) player.posX, (int) player.posY, (int) player.posZ);
            }
        } else {
            player.setActiveHand(hand);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean hasEffect(ItemStack par1ItemStack) {
        return par1ItemStack.getTagCompound() != null && par1ItemStack.getTagCompound().getBoolean("HasEffect");
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase player, int timeLeft) {
        if (stack.hasCapability(SpellCaster.INSTANCE, null) && player != null) {
            ISpellCaster spell = stack.getCapability(SpellCaster.INSTANCE, null);
            spell.cast(stack, world, player);
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase caster, int count) {
        if (stack.hasCapability(SpellCaster.INSTANCE, null) && caster != null) {
            ISpellCaster spell = stack.getCapability(SpellCaster.INSTANCE, null);
            if (spell.createSpellData(stack).isChanneled()) {
                spell.cast(stack, caster.world, caster);
            }
        }
        super.onUsingTick(stack, caster, count);
    }

    public static RayTraceResult getMovingObjectPosition(EntityLivingBase caster, World world, double range, boolean includeEntities, boolean targetWater) {
        RayTraceResult entityPos = null;
        if (includeEntities) {
            Entity pointedEntity = EntityUtils.getPointedEntity(world, caster, range, 1.0f, false, targetWater);
            if (pointedEntity != null) {
                entityPos = new RayTraceResult(pointedEntity);
            }
        }

        float factor = 1.0F;
        float interpPitch = caster.prevRotationPitch + (caster.rotationPitch - caster.prevRotationPitch) * factor;
        float interpYaw = caster.prevRotationYaw + (caster.rotationYaw - caster.prevRotationYaw) * factor;
        double interpPosX = caster.prevPosX + (caster.posX - caster.prevPosX) * factor;
        double interpPosY = caster.prevPosY + (caster.posY - caster.prevPosY) * factor + caster.getEyeHeight();
        double interpPosZ = caster.prevPosZ + (caster.posZ - caster.prevPosZ) * factor;
        Vec3d vec3 = new Vec3d(interpPosX, interpPosY, interpPosZ);
        float offsetYawCos = MathHelper.cos(-interpYaw * 0.017453292F - (float) Math.PI);
        float offsetYawSin = MathHelper.sin(-interpYaw * 0.017453292F - (float) Math.PI);
        float offsetPitchCos = -MathHelper.cos(-interpPitch * 0.017453292F);
        float offsetPitchSin = MathHelper.sin(-interpPitch * 0.017453292F);
        float finalXOffset = offsetYawSin * offsetPitchCos;
        float finalZOffset = offsetYawCos * offsetPitchCos;
        Vec3d targetVector = vec3.add(finalXOffset * range, offsetPitchSin * range, finalZOffset * range);
        RayTraceResult mop = world.rayTraceBlocks(vec3, targetVector, targetWater, !targetWater, false);

        if (entityPos != null && mop != null) {
            if (mop.hitVec.distanceTo(new RayTraceResult(caster).hitVec) < entityPos.hitVec.distanceTo(new RayTraceResult(caster).hitVec)) {
                return mop;
            } else {
                return entityPos;
            }
        }

        return entityPos != null ? entityPos : mop;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        super.onUpdate(stack, world, entity, par4, par5);
        if (entity instanceof EntityPlayerSP && ((EntityPlayerSP) entity).getActiveHand() != null) {
            EntityPlayerSP player = (EntityPlayerSP) entity;
            ItemStack usingItem = player.getActiveItemStack();
            if (!usingItem.isEmpty() && usingItem.getItem() == this) {
                if (SkillData.For(player).hasSkill("spellMovement")) {
                    player.movementInput.moveForward *= 2.5F;
                    player.movementInput.moveStrafe *= 2.5F;
                }
            }
        }
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        player.world.destroyBlock(pos, player.canHarvestBlock(player.world.getBlockState(pos)));
        return true;
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState) {
        if (stack.hasCapability(SpellCaster.INSTANCE, null) && player != null) {
            ISpellCaster caster = stack.getCapability(SpellCaster.INSTANCE, null);
            return (int) caster.createSpellData(stack).getModifiedValue(2, SpellModifiers.MINING_POWER, Operation.ADD, player.world, player, null);
        }
        return -1;
    }
}
