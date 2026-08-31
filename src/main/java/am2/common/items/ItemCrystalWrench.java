package am2.common.items;

import am2.ArsMagica;
import am2.api.math.AMVector3;
import am2.api.power.IPowerNode;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFadeOut;
import am2.client.particles.ParticleMoveOnHeading;
import am2.common.blocks.tileentity.TileEntityCrystalMarker;
import am2.common.blocks.tileentity.TileEntityFlickerHabitat;
import am2.common.blocks.tileentity.TileEntityParticleEmitter;
import am2.common.power.PowerNodeRegistry;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCrystalWrench extends ItemArsMagicaRotated {

    private static String KEY_PAIRLOC = "PAIRLOC";
    private static String HAB_PAIRLOC = "HABLOC";
    private static String KEEP_BINDING = "KEEPBINDING";
    private static String MODE = "WRENCHMODE";

    private static final int MODE_PAIR = 0;
    private static final int MODE_DISCONNECT = 1;

    public ItemCrystalWrench() {
        super();

        // Property for stored mode (when wrench has a stored location)
        this.addPropertyOverride(new ResourceLocation("stored"), new IItemPropertyGetter() {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                if (stack.hasTagCompound()) {
                    if (stack.getTagCompound().hasKey(KEY_PAIRLOC) || stack.getTagCompound().hasKey(HAB_PAIRLOC)) {
                        return 1.0F;
                    }
                }
                return 0.0F;
            }
        });

        // Property for disconnect mode
        this.addPropertyOverride(new ResourceLocation("disconnect"), new IItemPropertyGetter() {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                return getMode(stack) == MODE_DISCONNECT ? 1.0F : 0.0F;
            }
        });
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos,
                                           EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        TileEntity te = world.getTileEntity(pos);
        ItemStack stack = player.getHeldItem(hand);

        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        int cMode = getMode(stack);
        if (te != null && !(te instanceof IPowerNode || te instanceof TileEntityParticleEmitter) && cMode == MODE_DISCONNECT) {
            player.sendMessage(new TextComponentTranslation("am2.tooltip.wrongWrenchMode"));
            return EnumActionResult.FAIL;
        }

        if (te != null && te instanceof IPowerNode) {
            if (cMode == MODE_DISCONNECT) {
                doDisconnect((IPowerNode<?>) te, world, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ, player);
                return EnumActionResult.FAIL;
            }

            if (stack.getTagCompound().hasKey(KEY_PAIRLOC)) {
                doPairNodes(world, pos, stack, player, hitX, hitY, hitZ, te);
            } else {
                storePairLocation(world, te, stack, player, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ);
            }
        } else if (te != null && te instanceof TileEntityCrystalMarker && stack.getTagCompound() != null && stack.getTagCompound().hasKey(HAB_PAIRLOC)) {
            handleCMPair(stack, world, player, te, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ);
        } else if (player.isSneaking()) {
            handleModeChanges(stack);

        }
        return EnumActionResult.PASS;
    }

    private void handleCMPair(ItemStack stack, World world, EntityPlayer player, TileEntity te, double hitX, double hitY, double hitZ) {
        AMVector3 habLocation = AMVector3.readFromNBT(stack.getTagCompound().getCompoundTag(HAB_PAIRLOC));
        if (world.isRemote) {
            spawnLinkParticles(world, hitX, hitY, hitZ);
        } else {
            TileEntityCrystalMarker tecm = (TileEntityCrystalMarker) te;

            tecm.linkToHabitat(habLocation, player);

            if (!stack.getTagCompound().hasKey(KEEP_BINDING))
                stack.getTagCompound().removeTag(HAB_PAIRLOC);
        }
    }

    private void storePairLocation(World world, TileEntity te, ItemStack stack, EntityPlayer player, double hitX, double hitY, double hitZ) {
        AMVector3 destination = new AMVector3(te);
        if (!world.isRemote) {
            if (te instanceof TileEntityFlickerHabitat) {
                NBTTagCompound habLoc = new NBTTagCompound();
                destination.writeToNBT(habLoc);
                stack.getTagCompound().setTag(HAB_PAIRLOC, habLoc);
            } else {
                NBTTagCompound pairLoc = new NBTTagCompound();
                destination.writeToNBT(pairLoc);
                stack.getTagCompound().setTag(KEY_PAIRLOC, pairLoc);
            }

            if (player.isSneaking()) {
                stack.getTagCompound().setBoolean(KEEP_BINDING, true);
                player.sendMessage(new TextComponentTranslation("am2.tooltip.wrenchStored.persistent",
                        (int) destination.x, (int) destination.y, (int) destination.z));
            } else {
                player.sendMessage(new TextComponentTranslation("am2.tooltip.wrenchStored",
                        (int) destination.x, (int) destination.y, (int) destination.z));
            }
        } else {
            spawnLinkParticles(world, hitX, hitY, hitZ);
        }
    }

    private void doPairNodes(World world, BlockPos pos, ItemStack stack, EntityPlayer player, double hitX, double hitY, double hitZ, TileEntity te) {
        AMVector3 source = AMVector3.readFromNBT(stack.getTagCompound().getCompoundTag(KEY_PAIRLOC));
        TileEntity sourceTE = world.getTileEntity(source.toBlockPos());
        if (sourceTE != null && sourceTE instanceof IPowerNode && !world.isRemote) {
            player.sendMessage(PowerNodeRegistry.For(world).tryPairNodes((IPowerNode<?>) sourceTE, (IPowerNode<?>) te));
        } else if (world.isRemote) {
            spawnLinkParticles(world, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ);
        }
        if (!stack.getTagCompound().hasKey(KEEP_BINDING))
            stack.getTagCompound().removeTag(KEY_PAIRLOC);
    }

    private void doDisconnect(IPowerNode<?> node, World world, double hitX, double hitY, double hitZ, EntityPlayer player) {
        PowerNodeRegistry.For(world).tryDisconnectAllNodes(node);
        if (world.isRemote) {
            spawnLinkParticles(player.world, hitX, hitY, hitZ, true);
        } else {
            player.sendMessage(new TextComponentTranslation("am2.tooltip.disconnectPower"));
        }
    }

    private void handleModeChanges(ItemStack stack) {
        if (stack.getTagCompound().hasKey(KEEP_BINDING)) {
            stack.getTagCompound().removeTag(KEEP_BINDING);

            if (stack.getTagCompound().hasKey(KEY_PAIRLOC))
                stack.getTagCompound().removeTag(KEY_PAIRLOC);

            if (stack.getTagCompound().hasKey(HAB_PAIRLOC))
                stack.getTagCompound().removeTag(HAB_PAIRLOC);
        } else {
            if (getMode(stack) == MODE_PAIR)
                stack.getTagCompound().setInteger(MODE, MODE_DISCONNECT);
            else
                stack.getTagCompound().setInteger(MODE, MODE_PAIR);

        }
    }

    public static int getMode(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(MODE)) {
            return stack.getTagCompound().getInteger(MODE);
        }
        return 0;
    }

    private void spawnLinkParticles(World world, double hitX, double hitY, double hitZ) {
        spawnLinkParticles(world, hitX, hitY, hitZ, false);
    }

    private void spawnLinkParticles(World world, double hitX, double hitY, double hitZ, boolean disconnect) {
        for (int i = 0; i < 10; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "none_hand", hitX, hitY, hitZ);
            if (particle != null) {
                if (disconnect) {
                    particle.setRGBColorF(1, 0, 0);
                    particle.addRandomOffset(0.5f, 0.5f, 0.5f);
                }
                particle.setMaxAge(10);
                particle.setParticleScale(0.1f);
                particle.AddParticleController(new ParticleMoveOnHeading(particle, world.rand.nextInt(360), world.rand.nextInt(360), world.rand.nextDouble() * 0.2, 1, false));
                particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.1f));
            }
        }
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        int mode = getMode(stack);
        boolean hasStoredLocation = stack.hasTagCompound() &&
                (stack.getTagCompound().hasKey(KEY_PAIRLOC) || stack.getTagCompound().hasKey(HAB_PAIRLOC));

        if (mode == MODE_DISCONNECT) {
            tooltip.add(I18n.format("am2.tooltip.wrenchMode.disconnect"));
        } else if (hasStoredLocation) {
            tooltip.add(I18n.format("am2.tooltip.wrenchMode.stored"));

            // Show the stored location coordinates
            AMVector3 storedLoc = null;
            if (stack.getTagCompound().hasKey(KEY_PAIRLOC)) {
                storedLoc = AMVector3.readFromNBT(stack.getTagCompound().getCompoundTag(KEY_PAIRLOC));
            } else if (stack.getTagCompound().hasKey(HAB_PAIRLOC)) {
                storedLoc = AMVector3.readFromNBT(stack.getTagCompound().getCompoundTag(HAB_PAIRLOC));
            }

            if (storedLoc != null) {
                tooltip.add(I18n.format("am2.tooltip.wrenchMode.location",
                        (int) storedLoc.x, (int) storedLoc.y, (int) storedLoc.z));
            }
        } else {
            tooltip.add(I18n.format("am2.tooltip.wrenchMode.pair"));
        }

        super.addInformation(stack, world, tooltip, flag);
    }

    @Override
    public int getItemStackLimit() {
        return 1;
    }

}
