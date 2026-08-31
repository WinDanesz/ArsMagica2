package am2.common.items;

import am2.ArsMagica;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemCrystalPhylactery extends Item {

    public static final String TAG_SUMMON_TYPE     = "SummonType";
    private static final String TAG_PERCENT_FILLED = "PercentFilled";

    public final Map<String, Integer> spawnableEntities;

    public static final int META_EMPTY   = 0;
    public static final int META_QUARTER = 1;
    public static final int META_HALF    = 2;
    public static final int META_FULL    = 3;

    public ItemCrystalPhylactery() {
        super();
        this.spawnableEntities = new HashMap<>();
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
        this.addPropertyOverride(new ResourceLocation(ArsMagica.MODID, "fill"), (stack, world, entity) -> {
            switch (stack.getItemDamage()) {
                case META_QUARTER: return 0.25f;
                case META_HALF:    return 0.5f;
                case META_FULL:    return 1.0f;
                default:           return 0.0f;
            }
        });
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (stack.hasTagCompound()) {
            if (stack.getTagCompound().hasKey(TAG_SUMMON_TYPE)) {
                String registryKey = stack.getTagCompound().getString(TAG_SUMMON_TYPE);
                String entityName = EntityList.getTranslationName(new ResourceLocation(registryKey));
                if (entityName == null) entityName = new ResourceLocation(registryKey).getPath();
                tooltip.add(I18n.translateToLocalFormatted("am2.tooltip.phyEss", I18n.translateToLocalFormatted("entity." + entityName + ".name")));
            }
            float pct = stack.getTagCompound().getFloat(TAG_PERCENT_FILLED);
            tooltip.add(I18n.translateToLocalFormatted("am2.tooltip.pctFull", pct));
        } else {
            tooltip.add(I18n.translateToLocalFormatted("am2.tooltip.empty"));
        }
    }

    public void addFill(ItemStack stack) {
        if (!stack.hasTagCompound()) return;
        if (!stack.getTagCompound().hasKey(TAG_SUMMON_TYPE)) return;
        float pct = stack.getTagCompound().getFloat(TAG_PERCENT_FILLED);
        pct = Math.min(pct + itemRand.nextFloat() * 5, 100);
        stack.getTagCompound().setFloat(TAG_PERCENT_FILLED, pct);
        updateDamageMeta(stack, pct);
    }

    public void addFill(ItemStack stack, float amt) {
        if (!stack.hasTagCompound()) return;
        float pct = Math.min(stack.getTagCompound().getFloat(TAG_PERCENT_FILLED) + amt, 100);
        stack.getTagCompound().setFloat(TAG_PERCENT_FILLED, pct);
        updateDamageMeta(stack, pct);
    }

    private void updateDamageMeta(ItemStack stack, float pct) {
        if (pct >= 100)    stack.setItemDamage(META_FULL);
        else if (pct > 50) stack.setItemDamage(META_HALF);
        else if (pct > 25) stack.setItemDamage(META_QUARTER);
        else               stack.setItemDamage(META_EMPTY);
    }

    private NBTTagCompound getOrCreateTag(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }

    @Override
    public boolean hasEffect(ItemStack par1ItemStack) {
        return par1ItemStack.getItemDamage() == META_FULL;
    }

    public void setSpawnClass(ItemStack stack, Class<? extends Entity> clazz) {
        ResourceLocation key = EntityList.getKey(clazz);
        if (key == null) return;
        getOrCreateTag(stack).setString(TAG_SUMMON_TYPE, key.toString());
    }

    public boolean canStore(ItemStack stack, EntityLiving entity) {
        if (!entity.isNonBoss()) return false;
        if (stack.getItemDamage() == META_FULL) return false;
        if (!stack.hasTagCompound()) return true;
        ResourceLocation key = EntityList.getKey(entity);
        if (key == null) return false;
        return stack.getTagCompound().getString(TAG_SUMMON_TYPE).equals(key.toString());
    }

    public boolean isFull(ItemStack stack) {
        return stack.getItemDamage() == META_FULL;
    }

    @Nullable
    public String getSpawnClass(ItemStack stack) {
        if (!stack.hasTagCompound()) return null;
        return stack.getTagCompound().getString(TAG_SUMMON_TYPE);
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;
        ensureSpawnableEntities();
        items.add(new ItemStack(this));
        for (String s : spawnableEntities.keySet()) {
            if (s == null) continue;
            ItemStack stack = new ItemStack(this, 1, META_FULL);
            NBTTagCompound tag = getOrCreateTag(stack);
            tag.setString(TAG_SUMMON_TYPE, s);
            tag.setFloat(TAG_PERCENT_FILLED, 100);
            items.add(stack);
        }
    }

    private void ensureSpawnableEntities() {
        if (!spawnableEntities.isEmpty()) return;
        for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
            ResourceLocation regName = ent.getRegistryName();
            if (regName == null) continue;
            Class<? extends Entity> c = ent.getEntityClass();
            try {
                if (!EntityLiving.class.isAssignableFrom(c)) continue;
                if (Modifier.isAbstract(c.getModifiers())) continue;
                if (!c.getName().startsWith("net.minecraft.entity")) continue;
                int color = itemRand.nextInt();
                for (EntityList.EntityEggInfo info : EntityList.ENTITY_EGGS.values()) {
                    Class<? extends Entity> spawnClass = Objects.requireNonNull(ForgeRegistries.ENTITIES.getValue(info.spawnedID)).getEntityClass();
                    if (spawnClass == c) {
                        color = info.primaryColor;
                        break;
                    }
                }
                spawnableEntities.put(regName.toString(), color);
            } catch (SecurityException ignored) {
            }
        }
    }
}
