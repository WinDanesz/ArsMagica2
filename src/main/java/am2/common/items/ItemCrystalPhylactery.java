package am2.common.items;

import am2.ArsMagica;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.resources.I18n;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ItemCrystalPhylactery extends Item {

    public final HashMap<String, Integer> spawnableEntities;

    public static final int META_EMPTY = 0;
    public static final int META_QUARTER = 1;
    public static final int META_HALF = 2;
    public static final int META_FULL = 3;


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
            String className = stack.getTagCompound().getString("SpawnClassName");
            tooltip.add(I18n.format("am2.tooltip.phyEss", I18n.format("entity." + className + ".name")));
            float pct = stack.getTagCompound().getFloat("PercentFilled");
            tooltip.add(I18n.format("am2.tooltip.pctFull", pct));
        } else {
            tooltip.add(I18n.format("am2.tooltip.empty"));
        }
    }

    public void addFill(ItemStack stack) {
        if (stack.hasTagCompound()) {
            float pct = stack.getTagCompound().getFloat("PercentFilled");
            pct += itemRand.nextFloat() * 5;
            if (pct > 100) pct = 100;
            stack.getTagCompound().setFloat("PercentFilled", pct);
            if (pct == 100)
                stack.setItemDamage(META_FULL);
            else if (pct > 50)
                stack.setItemDamage(META_HALF);
            else if (pct > 25)
                stack.setItemDamage(META_QUARTER);
            else
                stack.setItemDamage(META_EMPTY);

        }
    }

    public void addFill(ItemStack stack, float amt) {
        if (stack.hasTagCompound()) {
            float pct = stack.getTagCompound().getFloat("PercentFilled");
            pct += amt;
            if (pct > 100) pct = 100;
            stack.getTagCompound().setFloat("PercentFilled", pct);
            if (pct == 100)
                stack.setItemDamage(META_FULL);
            else if (pct > 50)
                stack.setItemDamage(META_HALF);
            else if (pct > 25)
                stack.setItemDamage(META_QUARTER);
            else
                stack.setItemDamage(META_EMPTY);

        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack par1ItemStack) {
        return par1ItemStack.getItemDamage() == META_FULL;
    }

    // TODO
    public void setSpawnClass(ItemStack stack, Class<? extends Entity> clazz) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        String s = EntityList.getTranslationName(EntityList.getKey(clazz));
        if (s != null) {
            assert stack.getTagCompound() != null;
            stack.getTagCompound().setString("SpawnClassName", s);
        }
    }

    public boolean canStore(ItemStack stack, EntityLiving entity) {
        if (!entity.isNonBoss()) return false;
        if (stack.getItemDamage() == META_FULL)
            return false;
        if (!stack.hasTagCompound())
            return true;

        String e = stack.getTagCompound().getString("SpawnClassName");
        String s = EntityList.getEntityString(entity);

        return e.equals(s);
    }

    public boolean isFull(ItemStack stack) {
        return stack.getItemDamage() == META_FULL;
    }

    public String getSpawnClass(ItemStack stack) {
        if (!stack.hasTagCompound())
            return null;
        return stack.getTagCompound().getString("SpawnClassName");
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;
        for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
            String name = ent.getName();
            Class<? extends Entity> c = ent.getEntityClass();
            try {
                if (EntityLiving.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers()) && ent.getEntityClass().getName().startsWith("net.minecraft.entity")) {
                    int color = 0;
                    boolean found = false;
                    for (EntityList.EntityEggInfo info : EntityList.ENTITY_EGGS.values()) {
                        Class<? extends Entity> spawnClass = Objects.requireNonNull(ForgeRegistries.ENTITIES.getValue(info.spawnedID)).getEntityClass();
                        if (spawnClass == c) {
                            color = info.primaryColor;
                            found = true;
                            break;
                        }
                    }
                    if (!found) color = new Random().nextInt();
                    if (Minecraft.getMinecraft().getRenderManager().getEntityClassRenderObject(c) instanceof RenderLivingBase)
                        spawnableEntities.put(name, color);
                }
            } catch (SecurityException ignored) {
            }
        }
        items.add(new ItemStack(this));
        for (String s : this.spawnableEntities.keySet()) {
            if (s == null) continue;
            ItemStack stack = new ItemStack(this, 1, META_FULL);
            stack.setTagCompound(new NBTTagCompound());
            assert stack.getTagCompound() != null;
            stack.getTagCompound().setString("SpawnClassName", s);
            stack.getTagCompound().setFloat("PercentFilled", 100);
            items.add(stack);
        }
    }
}
