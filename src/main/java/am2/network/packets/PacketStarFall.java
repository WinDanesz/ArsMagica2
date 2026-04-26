package am2.network.packets;

import am2.ArsMagica;
import am2.api.spell.SpellData;
import am2.client.particles.*;
import am2.common.utils.MathUtilities;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to spawn star fall visual effect on the client.
 * Sent from server to client when a star fall spell is cast.
 * <p>
 * Direction: Server -> Client
 */
public class PacketStarFall extends AMPacket<PacketStarFall> {

    private double x;
    private double y;
    private double z;
    private boolean hasSpellData;
    private NBTTagCompound spellDataNBT;

    public PacketStarFall() {
    }

    public PacketStarFall(double x, double y, double z, SpellData spellData) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.hasSpellData = spellData != null;
        this.spellDataNBT = hasSpellData ? spellData.writeToNBT(new NBTTagCompound()) : null;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeBoolean(hasSpellData);
        if (hasSpellData) {
            ByteBufUtils.writeTag(buf, spellDataNBT);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        hasSpellData = buf.readBoolean();
        if (hasSpellData) {
            spellDataNBT = ByteBufUtils.readTag(buf);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketStarFall message, MessageContext ctx) {
        World world = Minecraft.getMinecraft().world;
        if (world != null) {
            SpellData spellData = null;
            if (message.hasSpellData && message.spellDataNBT != null) {
                spellData = SpellData.readFromNBT(message.spellDataNBT);
            }

            int color = -1;
            if (spellData != null) {
                color = spellData.getColor(world, null, null);
            }

            int step = ArsMagica.config.FullGFX() ? 5 : ArsMagica.config.LowGFX() ? 10 : 20;
            for (int i = 0; i < 360; i += step) {
                AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", message.x, message.y + 1.5, message.z);
                if (effect != null) {
                    effect.setIgnoreMaxAge(true);
                    effect.AddParticleController(new ParticleMoveOnHeading(effect, i, 0, 0.7f, 1, false));
                    float clrMod = world.rand.nextFloat();
                    int finalColor;
                    if (color == -1) {
                        finalColor = MathUtilities.colorFloatsToInt(0.24f * clrMod, 0.58f * clrMod, 0.71f * clrMod);
                    } else {
                        float[] colors = MathUtilities.colorIntToFloats(color);
                        for (int c = 0; c < colors.length; ++c)
                            colors[c] = colors[c] * clrMod;
                        finalColor = MathUtilities.colorFloatsToInt(colors[0], colors[1], colors[2]);
                    }
                    effect.setParticleScale(1.2f);
                    effect.setRGBColorI(finalColor);
                    effect.AddParticleController(new ParticleFadeOut(effect, 1, false).setFadeSpeed(0.05f).setKillParticleOnFinish(true));
                    effect.AddParticleController(
                            new ParticleLeaveParticleTrail(effect, "sparkle2", false, 15, 1, false)
                                    .addControllerToParticleList(new ParticleChangeSize(effect, 1.2f, 0.01f, 15, 1, false))
                                    .setParticleRGB_I(finalColor)
                                    .setChildAffectedByGravity()
                                    .addRandomOffset(0.2f, 0.2f, 0.2f)
                    );
                }
            }
        }
    }
}
