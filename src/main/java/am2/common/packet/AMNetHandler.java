package am2.common.packet;

import am2.api.power.IPowerNode;
import am2.api.spell.SpellData;
import am2.common.blocks.tileentity.TileEntityCalefactor;
import am2.common.blocks.tileentity.TileEntityObelisk;
import am2.common.bosses.IArsMagicaBoss;
import am2.common.entity.EntityHecate;
import am2.common.power.PowerNodeRegistry;
import am2.network.AMNetworkHandler;
import am2.network.packets.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * Legacy facade — helper methods that delegate to typed packets in {@link AMNetworkHandler}.
 * <p>
 * Kept alive because many callers still reference {@code AMNetHandler.INSTANCE.xxx()}.
 * All methods now send through the new {@code AMNetworkHandler / AMPacket} system.
 */
public class AMNetHandler {

    private AMNetHandler() {
    }

    public static final AMNetHandler INSTANCE = new AMNetHandler();

    // ── velocity ───────────────────────────────────────────────────────────────

    public void sendVelocityAddPacket(World world, EntityLivingBase target, double velX, double velY, double velZ) {
        if (world.isRemote) {
            return;
        }
        AMNetworkHandler.getNetwork().sendToAllAround(
                new PacketVelocityAdd(target, velX, velY, velZ),
                new TargetPoint(world.provider.getDimension(), target.posX, target.posY, target.posZ, 50));
    }

    // ── login / world name ─────────────────────────────────────────────────────

    public void syncWorldName(EntityPlayerMP player, String name) {
        AMNetworkHandler.getNetwork().sendTo(new PacketSyncWorldName(name), player);
    }

    // ── boss action updates ────────────────────────────────────────────────────

    public <T extends EntityLivingBase & IArsMagicaBoss> void sendActionUpdateToAllAround(T boss) {
        if (boss.world != null && !boss.world.isRemote) {
            AMNetworkHandler.getNetwork().sendToAllAround(
                    new PacketEntityActionUpdate(boss.getEntityId(), boss.getCurrentAction(), boss.getTicksInCurrentAction()),
                    new TargetPoint(boss.world.provider.getDimension(), boss.posX, boss.posY, boss.posZ, 64));
        }
    }

    // ── visual effects ─────────────────────────────────────────────────────────

    public void sendStarImpactToClients(double x, double y, double z, World world, SpellData spellData) {
        AMNetworkHandler.getNetwork().sendToAllAround(
                new PacketStarFall(x, y, z, spellData),
                new TargetPoint(world.provider.getDimension(), x, y, z, 64));
    }

    public void sendHecateDeathToAllAround(EntityHecate hecate) {
        AMNetworkHandler.getNetwork().sendToAllAround(
                new PacketHecateDeath(hecate.posX, hecate.posY, hecate.posZ),
                new TargetPoint(hecate.world.provider.getDimension(), hecate.posX, hecate.posY, hecate.posZ, 32));
    }

    // ── power paths ────────────────────────────────────────────────────────────

    public void syncPowerPaths(IPowerNode<?> node, EntityPlayerMP player) {
        if (!((TileEntity) node).getWorld().isRemote) {
            NBTTagCompound compound = PowerNodeRegistry.For(((TileEntity) node).getWorld()).getDataCompoundForNode(node);
            if (compound != null) {
                AMNetworkHandler.getNetwork().sendTo(new PacketPowerPathResponse(compound), player);
            }
        }
    }

    public void sendPowerResponseToClient(NBTTagCompound powerData, EntityPlayerMP player, TileEntity te) {
        AMNetworkHandler.getNetwork().sendTo(
                new PacketPowerPathResponse((byte) 1, powerData, te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()),
                player);
    }

    // ── tile entity sync ───────────────────────────────────────────────────────

    public void sendCalefactorCookUpdate(TileEntityCalefactor calefactor, byte[] data) {
        AMNetworkHandler.getNetwork().sendToAllAround(
                new PacketCalefactorSync(calefactor.getPos(), data),
                new TargetPoint(calefactor.getWorld().provider.getDimension(), calefactor.getPos().getX(), calefactor.getPos().getY(), calefactor.getPos().getZ(), 32));
    }

    public void sendObeliskUpdate(TileEntityObelisk obelisk, byte[] data) {
        AMNetworkHandler.getNetwork().sendToAllAround(
                new PacketObeliskSync(obelisk.getPos(), data),
                new TargetPoint(obelisk.getWorld().provider.getDimension(), obelisk.getPos().getX(), obelisk.getPos().getY(), obelisk.getPos().getZ(), 32));
    }
}
