package am2.client.animation;

import am2.common.animation.AnimationAPI;
import am2.common.animation.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void initTimer() {
        mcTimer = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), AnimationAPI.fTimer);
    }

    @Override
    public float getPartialTick() {
        return mcTimer.renderPartialTicks;
    }

    @Override
    public World getWorldClient() {
        return FMLClientHandler.instance().getWorldClient();
    }

    private Timer mcTimer;
}
