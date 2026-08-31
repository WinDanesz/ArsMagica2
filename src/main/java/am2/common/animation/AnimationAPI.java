package am2.common.animation;

import am2.common.animation.packet.PacketAnim;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class AnimationAPI {

    public static final String NAME = "AnimationAPI";
    public static final String MODID = "animationapi";
    public static final CommonProxy proxy = createProxy();
    public static SimpleNetworkWrapper wrapper;

    public static void init() {
        if (wrapper != null) {
            return;
        }

        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel("AnimAPI");
        wrapper.registerMessage(PacketAnim.Handler.class, PacketAnim.class, 0, Side.CLIENT);
    }

    public static void postInit() {
        proxy.initTimer();
    }

    private static CommonProxy createProxy() {
        if (!isClient()) {
            return new CommonProxy();
        }

        try {
            return (CommonProxy) Class.forName("am2.client.animation.ClientProxy").newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize AnimationAPI client proxy", e);
        }
    }

    public static boolean isClient() {
        return FMLCommonHandler.instance().getSide().isClient();
    }

    public static boolean isEffectiveClient() {
        return FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    public static void sendAnimPacket(IAnimatedEntity entity, int animID) {
        if (isEffectiveClient()) return;
        entity.setAnimID(animID);
        wrapper.sendToAll(new PacketAnim((byte) animID, ((Entity) entity).getEntityId()));
    }

    public static final String[] fTimer;

    static {
        fTimer = new String[]{"field_71428_T", "aa", "timer"};
    }
}
