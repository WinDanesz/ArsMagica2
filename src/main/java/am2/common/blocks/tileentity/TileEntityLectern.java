package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFadeOut;
import am2.client.particles.ParticleMoveOnHeading;
import am2.common.registry.AMLecternBooks;
import am2.network.AMNetworkHandler;
import am2.network.packets.PacketLecternSync;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;

public class TileEntityLectern extends TileEntityEnchantmentTable implements ITickable {
    private ItemStack stack = ItemStack.EMPTY;
    private ItemStack tooltipStack = ItemStack.EMPTY;
    private boolean needsBook;
    private boolean overPowered;
    public int particleAge;
    public int particleMaxAge = 150;
    private boolean increasing = true;

    public TileEntityLectern() {

    }

    public void resetParticleAge() {
        particleAge = 0;
        increasing = true;
    }

    public ItemStack getTooltipStack() {
        return tooltipStack;
    }

    public void setTooltipStack(ItemStack stack) {
        this.tooltipStack = stack;
    }

    @Override
    public void update() {
        if (world.isRemote) {
            updateBookRender();
        } else if (tickCount % 20 == 0) {
            IBlockState state = this.world.getBlockState(this.pos);
            //This is probably the fastest I can get it to go.
            //If you know of any better way, please feel free to suggest it.
            this.world.notifyBlockUpdate(this.pos, state, state, 2);
        }
    }

    private void updateBookRender() {
        particleAge++;
        if (increasing) {
            particleMaxAge += 2;
            if (particleMaxAge - particleAge > 120)
                increasing = false;
        } else {
            if (particleMaxAge - particleAge < 5)
                increasing = true;
        }
        this.bookSpreadPrev = this.bookSpread;
        this.bookRotationPrev = this.bookRotation;

        this.bookSpread += 0.1F;

        if (this.bookSpread < 0.5F || world.rand.nextInt(40) == 0) {
            float f1 = this.flipT;

            do {
                this.flipT += (float) (world.rand.nextInt(4) - world.rand.nextInt(4));
            }
            while (f1 == this.flipT);
        }

        while (this.bookRotation >= (float) Math.PI) {
            this.bookRotation -= ((float) Math.PI * 2F);
        }

        while (this.bookRotation < -(float) Math.PI) {
            this.bookRotation += ((float) Math.PI * 2F);
        }

        while (this.tRot >= (float) Math.PI) {
            this.tRot -= ((float) Math.PI * 2F);
        }

        while (this.tRot < -(float) Math.PI) {
            this.tRot += ((float) Math.PI * 2F);
        }

        float f2;

        for (f2 = this.tRot - this.bookRotation; f2 >= (float) Math.PI; f2 -= ((float) Math.PI * 2F)) ;

        while (f2 < -(float) Math.PI) {
            f2 += ((float) Math.PI * 2F);
        }

        this.bookRotation += f2 * 0.4F;

        if (this.bookSpread < 0.0F) {
            this.bookSpread = 0.0F;
        }

        if (this.bookSpread > 1.0F) {
            this.bookSpread = 1.0F;
        }

        ++this.tickCount;
        this.pageFlipPrev = this.pageFlip;
        float f = (this.flipT - this.pageFlip) * 0.4F;
        float f3 = 0.2F;
        f = MathHelper.clamp(f, -f3, f3);
        this.flipA += (f - this.flipA) * 0.9F;
        this.pageFlip += this.flipA;
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean setStack(ItemStack stack) {
        if (AMLecternBooks.isVaild(stack))
            stack.setCount(1);
        else if(stack == null || stack.isEmpty())
            stack = ItemStack.EMPTY;
        else return false;
        this.stack = stack;
        markDirty();
        if (!this.world.isRemote) {
            AMNetworkHandler.getNetwork().sendToAllAround(new PacketLecternSync(pos, stack), new net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 32));
        }
        return true;
    }

    public boolean hasStack() {
        return !stack.isEmpty();
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);
        SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, getBlockMetadata(), compound);
        return packet;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound comp) {
        super.readFromNBT(comp);
        if (comp.hasKey("placedBook")) {
            NBTTagCompound bewk = comp.getCompoundTag("placedBook");
            stack = new ItemStack((bewk));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound comp) {
        super.writeToNBT(comp);
        if (!stack.isEmpty()) {
            NBTTagCompound bewk = new NBTTagCompound();
            stack.writeToNBT(bewk);
            comp.setTag("placedBook", bewk);
        }
        return comp;
    }

    public void setNeedsBook(boolean b) {
        this.needsBook = b;
    }

    public boolean getNeedsBook() {
        return this.needsBook;
    }

    public void setOverpowered(boolean b) {
        this.overPowered = b;
    }

    public boolean getOverpowered() {
        return this.overPowered;
    }
}
