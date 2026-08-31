package am2.common.spell;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.event.SpellCastEvent;
import am2.api.extensions.IEntityExtension;
import am2.api.extensions.ISpellCaster;
import am2.api.spell.*;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMPotions;
import am2.common.utils.AffinityShiftUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.Map.Entry;

public class SpellCaster implements ISpellCaster, ICapabilityProvider, ICapabilitySerializable<NBTBase> {

    @CapabilityInject(ISpellCaster.class)
    public static Capability<ISpellCaster> INSTANCE = null;
    public static final ResourceLocation ID = new ResourceLocation(ArsMagica.MODID, "spell_caster");

    /** Convenience accessor — returns the ISpellCaster capability for the given stack, or null. */
    public static ISpellCaster of(ItemStack stack) {
        return stack.hasCapability(INSTANCE, null) ? stack.getCapability(INSTANCE, null) : null;
    }

    private ArrayList<List<List<SpellPart>>> shapeGroups = new ArrayList<>();
    private ArrayList<List<SpellPart>> spellCommon = new ArrayList<>();
    private ArrayList<Float> shapeGroupCosts = new ArrayList<>();
    private ArrayList<NBTTagCompound> shapeGroupStoredData = new ArrayList<>();
    private NBTTagCompound storedData = new NBTTagCompound();
    private HashMap<Affinity, Float> affinityShift = new HashMap<>();
    private UUID uuid = new UUID(0, 0);
    private int currentShapeGroup = 0;

    public SpellCaster() {
        this.gatherAffinityShift();
        this.gatherBaseManaCosts();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return INSTANCE != null && capability == INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (INSTANCE != null && capability == INSTANCE)
            return (T) this;
        return null;
    }

    @Override
    public float getManaCost(World world, EntityLivingBase caster) {
        float manaCost = this.getBaseManaCost(this.currentShapeGroup);
        IEntityExtension ext = EntityExtension.For(caster);
        manaCost *= (1 + (ext.getCurrentBurnout() / ext.getMaxBurnout()));
        
        if (caster.getAttributeMap() != null && caster.getAttributeMap().getAttributeInstance(ArsMagicaAPI.manaCostMultiplier) != null) {
            manaCost *= (float) caster.getAttributeMap().getAttributeInstance(ArsMagicaAPI.manaCostMultiplier).getAttributeValue();
        }
        
        return manaCost;
    }

    @Override
    public SpellData createSpellData(ItemStack source) {
        List<List<SpellPart>> stages = new ArrayList<>();

        // Add stages from the current shape group
        List<List<SpellPart>> shapeGroup = this.getShapeGroups().get(this.getCurrentShapeGroup());
        for (List<SpellPart> stage : shapeGroup) {
            if (stage != null && !stage.isEmpty()) {
                stages.add(new ArrayList<>(stage));
            }
        }

        // Add stages from spell common
        // If the first common stage doesn't have a shape, merge it with the last shape group stage
        boolean firstCommonStageProcessed = false;
        for (List<SpellPart> stage : this.spellCommon) {
            if (stage != null && !stage.isEmpty()) {
                if (!firstCommonStageProcessed && !stages.isEmpty()) {
                    // Check if this stage starts with a shape
                    boolean hasShape = stage.stream().anyMatch(part -> part instanceof SpellShape);
                    if (!hasShape) {
                        // Merge with last shape group stage
                        stages.get(stages.size() - 1).addAll(stage);
                        firstCommonStageProcessed = true;
                        continue;
                    }
                }
                firstCommonStageProcessed = true;
                stages.add(new ArrayList<>(stage));
            }
        }

        NBTTagCompound storedData = this.storedData.copy();
        storedData.merge(this.getStoredData(this.getCurrentShapeGroup()).copy());
        return new SpellData(source, stages, this.uuid, storedData);
    }

    @Override
    public boolean cast(ItemStack source, World world, EntityLivingBase caster) {
        // Block casting when the caster is silenced by AM2's Silence potion or
        // by EBWiz's Arcane Jammer (matching the reference SpellHelper.applyStackStage check).
        if (caster.isPotionActive(AMPotions.silence)) return false;
        if (EBWizardryCompatBootstrap.isArcaneJammed(caster)) return false;

        IEntityExtension ext = EntityExtension.For(caster);
        SpellData data = this.createSpellData(source);
        float manaCost = this.getManaCost(world, caster);

        SpellCastEvent.Pre preEvent = new SpellCastEvent.Pre(caster, data, manaCost);
        if (MinecraftForge.EVENT_BUS.post(preEvent)) {
            return false;
        }
        manaCost = preEvent.manaCost;

        if (ext.hasEnoughMana(manaCost)) {
            List<String> missingReagents = getMissingReagentNames(data, caster);
            if (!missingReagents.isEmpty()) {
                if (caster instanceof EntityPlayer && !world.isRemote) {
                    TextComponentTranslation msg = new TextComponentTranslation("am2.tooltip.missingReagents");
                    msg.appendText(String.join(", ", missingReagents));
                    ((EntityPlayer) caster).sendStatusMessage(msg, true);
                }
                return false;
            }
            SpellCastResult result = data.execute(world, caster);
            if (result != SpellCastResult.FREE_CAST) {
                ext.deductMana(manaCost);
            }
            if (result == SpellCastResult.SUCCESS || result == SpellCastResult.FREE_CAST) {
                consumeReagents(data, caster);
                if (!ArsMagica.config.getOldXpCalculations())
                    ext.addMagicXP(AffinityShiftUtils.calculateXPGains(caster, data));
            }
            float cost = 0F;
            float multiplier = 1F;
            float stageMultiplier = 1.0F;
            for (List<SpellPart> parts : data.getStages()) {
                float _cost = 0F;
                float _multiplier = stageMultiplier;
                parts.sort(Comparator.naturalOrder());
                for (SpellPart part : parts) {
                    if (part instanceof SpellModifier) {
                        multiplier *= ((SpellModifier) part).getEffectiveManaCostMultiplier();
                    } else if (part instanceof SpellShape) {
                        _multiplier *= ((SpellShape) part).getEffectiveManaCostMultiplier();
                        stageMultiplier = 1.0F;
                    } else if (part instanceof SpellComponent) {
                        _cost += ((SpellComponent) part).burnout(caster);
                    }
                }

                cost += _cost * _multiplier;
            }
            if (result != SpellCastResult.FREE_CAST) {
                float totalBurnout = cost * multiplier;
                if (caster.getAttributeMap() != null && caster.getAttributeMap().getAttributeInstance(ArsMagicaAPI.burnoutGenerationMultiplier) != null) {
                    totalBurnout *= (float) caster.getAttributeMap().getAttributeInstance(ArsMagicaAPI.burnoutGenerationMultiplier).getAttributeValue();
                }
                ext.setCurrentBurnout(Math.min(ext.getMaxBurnout(), ext.getCurrentBurnout() + totalBurnout));
            }
            if (result == SpellCastResult.SUCCESS || result == SpellCastResult.FREE_CAST) {
                MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(caster, data, manaCost));
            }
            return result == SpellCastResult.SUCCESS || result == SpellCastResult.FREE_CAST;
        }
        return false;
    }

    @Override
    public List<List<SpellPart>> getSpellCommon() {
        return this.spellCommon == null || this.spellCommon.isEmpty() ? ImmutableList.of(Lists.newArrayList()) : ImmutableList.copyOf(this.spellCommon);
    }

    @Override
    public List<List<List<SpellPart>>> getShapeGroups() {
        return this.shapeGroups == null || this.shapeGroups.isEmpty() ? ImmutableList.of(Lists.newArrayList()) : ImmutableList.copyOf(this.shapeGroups);
    }

    @Override
    public Map<Affinity, Float> getAffinityShift() {
        return ImmutableMap.copyOf(this.affinityShift);
    }

    @Override
    public int getShapeGroupCount() {
        return this.shapeGroups.size();
    }

    @Override
    public float getBaseManaCost(int shapeGroup) {
        Float f = this.shapeGroupCosts.get(MathHelper.clamp(shapeGroup, 0, Math.max(0, this.shapeGroupCosts.size() - 1)));
        return f != null ? f.floatValue() : 0;
    }

    @Override
    public UUID getSpellUUID() {
        return UUID.fromString(this.uuid.toString());
    }

    @Override
    public void setSpellCommon(List<List<SpellPart>> data) {
        this.spellCommon.clear();
        if (data != null) {
            for (List<SpellPart> ls : data) {
                if (ls != null) {
                    ArrayList<SpellPart> parts = new ArrayList<>();
                    for (SpellPart asp : ls) {
                        if (asp != null) {
                            parts.add(asp);
                        }
                    }
                    this.spellCommon.add(parts);
                }
            }
        }
        this.gatherBaseManaCosts();
    }

    @Override
    public void setShapeGroups(List<List<List<SpellPart>>> data) {
        this.shapeGroups.clear();
        if (data != null) {
            for (List<List<SpellPart>> ls : data) {
                if (ls != null) {
                    ArrayList<List<SpellPart>> stages = new ArrayList<>();
                    for (List<SpellPart> ls2 : ls) {
                        if (ls2 != null) {
                            ArrayList<SpellPart> parts = new ArrayList<>();
                            for (SpellPart asp : ls2) {
                                if (asp != null) {
                                    parts.add(asp);
                                }
                            }
                            stages.add(parts);
                        }
                    }
                    this.shapeGroups.add(stages);
                }
            }
        }
        this.gatherBaseManaCosts();
    }

    @Override
    public void setAffinityShift(Map<Affinity, Float> shift) {
        this.affinityShift.clear();
        if (shift != null) {
            for (Entry<Affinity, Float> entry : shift.entrySet()) {
                if (entry.getValue() != null && entry.getKey() != null) {
                    this.affinityShift.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public void setBaseManaCost(int shapeGroup, float manaCost) {
        this.shapeGroupCosts.ensureCapacity(shapeGroup + 1);
        while (this.shapeGroupCosts.size() < shapeGroup + 1) {
            this.shapeGroupCosts.add(0F);
        }
        this.shapeGroupCosts.set(shapeGroup, manaCost);
    }

    @Override
    public void setUUID(UUID uuid) {
        this.uuid = uuid == null ? new UUID(0, 0) : uuid;
    }

    @Override
    public void gatherBaseManaCosts() {
        this.shapeGroupCosts.clear();
        if (this.shapeGroups.isEmpty()) {
            float cost = 0F;
            float multiplier = 1F;
            float stageMultiplier = 1.0F;
            for (List<SpellPart> parts : this.spellCommon) {
                float _cost = 0F;
                float _multiplier = stageMultiplier;
                parts.sort(Comparator.naturalOrder());
                for (SpellPart part : parts) {
                    if (part instanceof SpellModifier) {
                        multiplier *= ((SpellModifier) part).getEffectiveManaCostMultiplier();
                    } else if (part instanceof SpellShape) {
                        _multiplier *= ((SpellShape) part).getEffectiveManaCostMultiplier();
                        stageMultiplier = 1.0F;
                    } else if (part instanceof SpellComponent) {
                        _cost += ((SpellComponent) part).getEffectiveMana();
                    }
                }

                cost += _cost * _multiplier;
            }
            this.shapeGroupCosts.add(cost * multiplier);
        } else {
            for (int i = 0; i < this.shapeGroups.size(); i++) {
                float cost = 0F;
                float multiplier = 1F;
                float stageMultiplier = 1.0F;
                for (List<SpellPart> parts : this.shapeGroups.get(i)) {
                    for (SpellPart part : parts) {
                        if (part instanceof SpellModifier) {
                            multiplier *= ((SpellModifier) part).getEffectiveManaCostMultiplier();
                        } else if (part instanceof SpellShape) {
                            stageMultiplier *= ((SpellShape) part).getEffectiveManaCostMultiplier();
                        }
                    }
                }
                for (List<SpellPart> parts : this.spellCommon) {
                    float _cost = 0F;
                    float _multiplier = stageMultiplier;
                    parts.sort((t, o) -> t.compareTo(o));
                    for (SpellPart part : parts) {
                        if (part instanceof SpellModifier) {
                            multiplier *= ((SpellModifier) part).getEffectiveManaCostMultiplier();
                        } else if (part instanceof SpellShape) {
                            _multiplier *= ((SpellShape) part).getEffectiveManaCostMultiplier();
                            stageMultiplier = 1.0F;
                        } else if (part instanceof SpellComponent) {
                            _cost += ((SpellComponent) part).getEffectiveMana();
                        }
                    }

                    cost += _cost * _multiplier;
                }
                this.shapeGroupCosts.add(cost * multiplier);
            }
        }
    }

    @Override
    public void gatherAffinityShift() {
        this.affinityShift.clear();
        for (List<SpellPart> parts : this.spellCommon) {
            for (SpellPart part : parts) {
                if (part instanceof SpellComponent) {
                    for (Affinity aff : ((SpellComponent) part).getAffinity()) {
                        if (this.affinityShift.get(aff) != null) {
                            this.affinityShift.put(aff, this.affinityShift.get(aff) + ((SpellComponent) part).getAffinityShift(aff));
                        } else {
                            this.affinityShift.put(aff, ((SpellComponent) part).getAffinityShift(aff));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void generateUUID() {
        this.uuid = UUID.randomUUID();
    }

    @Override
    public int getCurrentShapeGroup() {
        return this.currentShapeGroup;
    }

    @Override
    public void setCurentShapeGroup(int shapeGroup) {
        this.currentShapeGroup = MathHelper.clamp(shapeGroup, 0, Math.max(this.shapeGroups.size() - 1, 0));
    }

    @Override
    public boolean validate() {
        for (List<SpellPart> parts : this.getSpellCommon()) {
            if (parts == null)
                return false;
            boolean foundShape = false;
            for (SpellPart part : parts) {
                if (part == null)
                    return false;
                if (part instanceof SpellShape) {
                    if (foundShape)
                        return false;
                    else
                        foundShape = true;
                }
            }
        }
        for (List<List<SpellPart>> groups : this.getShapeGroups()) {
            if (groups == null)
                return false;
            boolean flag = false;
            for (List<SpellPart> parts : groups) {
                if (parts == null)
                    return false;
                boolean foundShape = false;
                for (SpellPart part : parts) {
                    if (part == null)
                        return false;
                    if (part instanceof SpellShape) {
                        if (foundShape)
                            return false;
                        else {
                            foundShape = true;
                            flag = true;
                        }
                    }
                }
            }
            if (!flag)
                return false;
        }
        return true;
    }

    @Override
    public NBTBase serializeNBT() {
        return INSTANCE.writeNBT(this, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        INSTANCE.readNBT(this, null, nbt);
    }

    @Override
    public NBTTagCompound getStoredData(int shapeGroup) {
        if (this.shapeGroupStoredData.isEmpty() || shapeGroup < 0 || shapeGroup >= this.shapeGroupStoredData.size())
            return new NBTTagCompound();
        NBTTagCompound tag = this.shapeGroupStoredData.get(MathHelper.clamp(shapeGroup, 0, Math.max(this.shapeGroupStoredData.size() - 1, 0)));
        return tag != null ? tag : new NBTTagCompound();
    }

    @Override
    public NBTTagCompound getCommonStoredData() {
        if (this.storedData == null)
            this.storedData = new NBTTagCompound();
        return this.storedData;
    }

    @Override
    public void setStoredData(int shapeGroup, NBTTagCompound tag) {
        this.shapeGroupStoredData.ensureCapacity(shapeGroup);
        while (this.shapeGroupStoredData.size() < shapeGroup + 1) {
            this.shapeGroupStoredData.add(new NBTTagCompound());
        }
        this.shapeGroupStoredData.set(shapeGroup, tag);
    }

    @Override
    public void setCommonStoredData(NBTTagCompound tag) {
        this.storedData = tag;
    }

    private static List<String> getMissingReagentNames(SpellData data, EntityLivingBase caster) {
        List<String> missing = new ArrayList<>();
        if (!(caster instanceof EntityPlayer)) return missing;
        EntityPlayer player = (EntityPlayer) caster;
        if (player.capabilities.isCreativeMode) return missing;
        for (List<SpellPart> stage : data.getStages()) {
            for (SpellPart part : stage) {
                if (!(part instanceof SpellComponent)) continue;
                ItemStack[] reagents = ((SpellComponent) part).getEffectiveReagents(caster);
                if (reagents == null) continue;
                for (ItemStack required : reagents) {
                    if (required == null || required.isEmpty()) continue;
                    if (!playerHasItem(player, required))
                        missing.add(required.getCount() + "x " + required.getDisplayName());
                }
            }
        }
        return missing;
    }

    private static boolean playerHasItem(EntityPlayer player, ItemStack required) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack is = player.inventory.getStackInSlot(i);
            if (is.isEmpty()) continue;
            if (is.getItem() == required.getItem()
                    && (required.getMetadata() == OreDictionary.WILDCARD_VALUE || is.getMetadata() == required.getMetadata())
                    && is.getCount() >= required.getCount()) {
                return true;
            }
        }
        return false;
    }

    private static void consumeReagents(SpellData data, EntityLivingBase caster) {
        if (!(caster instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) caster;
        if (player.capabilities.isCreativeMode) return;
        for (List<SpellPart> stage : data.getStages()) {
            for (SpellPart part : stage) {
                if (!(part instanceof SpellComponent)) continue;
                ItemStack[] reagents = ((SpellComponent) part).getEffectiveReagents(caster);
                if (reagents == null) continue;
                for (ItemStack required : reagents) {
                    if (required == null || required.isEmpty()) continue;
                    for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                        ItemStack is = player.inventory.getStackInSlot(i);
                        if (is.isEmpty()) continue;
                        if (is.getItem() == required.getItem()
                                && (required.getMetadata() == OreDictionary.WILDCARD_VALUE || is.getMetadata() == required.getMetadata())
                                && is.getCount() >= required.getCount()) {
                            is.shrink(required.getCount());
                            break;
                        }
                    }
                }
            }
        }
    }

}
