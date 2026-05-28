package am2.common.items;

import am2.ArsMagica;
import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ItemLifeWard extends Item implements IBauble {

    public ItemLifeWard() {
        super();
    }

    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
        if (!ArsMagica.config.getLifeWardEnabled()) return;
        int tickInterval = ArsMagica.config.getLifeWardTickInterval();
        if (par4 < 9 && par3Entity.ticksExisted % tickInterval == 0 && par3Entity instanceof EntityLivingBase) {
            // Prevent stacking: only the Life Ward in the lowest hotbar slot should apply the effect
            if (par3Entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) par3Entity;
                for (int i = 0; i < par4; i++) {
                    if (player.inventory.getStackInSlot(i).getItem() instanceof ItemLifeWard) {
                        return; // A Life Ward in a lower slot will handle the effect
                    }
                }
            }
            applyAbsorption((EntityLivingBase) par3Entity);
        }
    }

    // ---------------------------------------------------------------
    // IBauble – Baubles body-slot usage
    // ---------------------------------------------------------------

    @Override
    @Optional.Method(modid = "baubles")
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.BODY;
    }

    @Override
    @Optional.Method(modid = "baubles")
    public void onWornTick(ItemStack stack, EntityLivingBase entity) {
        if (!ArsMagica.config.getLifeWardEnabled()) return;
        int tickInterval = ArsMagica.config.getLifeWardTickInterval();
        if (entity.ticksExisted % tickInterval == 0) {
            applyAbsorption(entity);
        }
    }

    private void applyAbsorption(EntityLivingBase entity) {
        float abs = entity.getAbsorptionAmount();
        int maxAbsorption = ArsMagica.config.getLifeWardMaxAbsorption();
        if (abs < maxAbsorption) {
            float perTick = ArsMagica.config.getLifeWardAbsorptionPerTick();
            entity.setAbsorptionAmount(Math.min(abs + perTick, maxAbsorption));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.translateToLocalFormatted("am2.tooltip.life_ward"));
        tooltip.add(I18n.translateToLocalFormatted("am2.tooltip.life_ward2"));
        super.addInformation(stack, world, tooltip, flag);
    }

}
