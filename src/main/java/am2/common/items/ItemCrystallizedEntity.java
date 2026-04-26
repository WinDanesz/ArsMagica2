package am2.common.items;

import am2.ArsMagica;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCrystallizedEntity extends Item {

    public static final String TAG_ENTITY_ID = "EntityID";
    public static final String TAG_ENTITY_DATA = "EntityData";
    public static final String TAG_ENTITY_NAME = "EntityName";

    private static final float CATEGORY_DEFAULT = 0.0f;
    private static final float CATEGORY_ANIMAL = 1.0f;
    private static final float CATEGORY_BIPED = 2.0f;

    public ItemCrystallizedEntity() {
        setMaxStackSize(1);
        this.addPropertyOverride(new ResourceLocation(ArsMagica.MODID, "entity_type"), new IItemPropertyGetter() {
            @Override
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(TAG_ENTITY_ID)) {
                    return CATEGORY_DEFAULT;
                }
                ResourceLocation entityId = new ResourceLocation(stack.getTagCompound().getString(TAG_ENTITY_ID));
                Class<? extends Entity> entityClass = EntityList.getClass(entityId);
                if (entityClass == null) {
                    return CATEGORY_DEFAULT;
                }
                if (EntityAnimal.class.isAssignableFrom(entityClass)) {
                    return CATEGORY_ANIMAL;
                }
                Render<?> renderer = Minecraft.getMinecraft().getRenderManager().getEntityClassRenderObject(entityClass);
                if (renderer instanceof RenderLivingBase && ((RenderLivingBase<?>) renderer).getMainModel() instanceof ModelBiped) {
                    return CATEGORY_BIPED;
                }
                return CATEGORY_DEFAULT;
            }
        });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG_ENTITY_NAME)) {
            String name = stack.getTagCompound().getString(TAG_ENTITY_NAME);
            String prefix = isVowel(name.charAt(0)) ? "an" : "a";
            tooltip.add(I18n.format("am2.tooltip.crystallizedEntity", prefix, name));
        }
    }

    private static boolean isVowel(char c) {
        return "AEIOUaeiou".indexOf(c) >= 0;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG_ENTITY_ID);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(TAG_ENTITY_ID)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        if (!world.isRemote) {
            NBTTagCompound tag = stack.getTagCompound();
            ResourceLocation entityId = new ResourceLocation(tag.getString(TAG_ENTITY_ID));
            Entity entity = EntityList.createEntityByIDFromName(entityId, world);

            if (entity != null) {
                NBTTagCompound entityData = tag.getCompoundTag(TAG_ENTITY_DATA);
                entity.readFromNBT(entityData);

                Vec3d look = player.getLookVec();
                double spawnX = player.posX + look.x * 2;
                double spawnY = player.posY;
                double spawnZ = player.posZ + look.z * 2;
                entity.setPosition(spawnX, spawnY, spawnZ);

                world.spawnEntity(entity);
                stack.shrink(1);
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static ItemStack createStackForEntity(Item item, Entity target) {
        ItemStack stack = new ItemStack(item);
        NBTTagCompound tag = new NBTTagCompound();

        ResourceLocation entityId = EntityList.getKey(target);
        if (entityId != null) {
            tag.setString(TAG_ENTITY_ID, entityId.toString());
        }

        tag.setString(TAG_ENTITY_NAME, target.getName());

        NBTTagCompound entityData = new NBTTagCompound();
        target.writeToNBT(entityData);
        tag.setTag(TAG_ENTITY_DATA, entityData);

        stack.setTagCompound(tag);
        return stack;
    }
}
