package am2.common.items;

import am2.ArsMagica;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleHoldPosition;
import am2.common.blocks.BlockInvisibleUtility;
import am2.common.registry.AMBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCandle extends Item {

    private static final int radius = 15;
    private static final int short_radius = 5;
    private static final float immediate_radius = 2.5f;

    public ItemCandle() {
        super();
        setMaxStackSize(1);
        setMaxDamage(18000); //15 minutes (20 * 60 * 15)

        // Property override for proximity-based candle model
        // 0 = unattuned/far, 0.5 = within 15 blocks (blue), 1.0 = within 5 blocks (red)
        this.addPropertyOverride(new ResourceLocation(ArsMagica.MODID, "proximity"), new net.minecraft.item.IItemPropertyGetter() {
            @Override
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn) {
                if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("search_block")) {
                    return 0.0F; // unattuned
                }
                int proximity = stack.getTagCompound().getInteger("proximity");
                // 0 = far, 1 = within 15, 2 = within 5
                return proximity / 2.0F; // 0.0, 0.5, 1.0
            }
        });
    }


    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("search_block")) {
            IBlockState block = worldIn.getBlockState(pos);
            if (playerIn.isSneaking() && block != null && block.getBlockHardness(worldIn, pos) > 0f && worldIn.getTileEntity(pos) == null) {
                if (!worldIn.isRemote) {
                    setSearchBlock(block, stack);
                    worldIn.setBlockToAir(pos);
                } else {
                    AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(worldIn, "radiant", pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    if (particle != null) {
                        particle.AddParticleController(new ParticleHoldPosition(particle, 20, 1, false));
                        particle.setRGBColorF(0, 0.5f, 1);
                    }
                }
                return EnumActionResult.SUCCESS;
            }
        }

        // If already attuned, don't allow placing or any block interaction
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("search_block")) {
            if (!worldIn.isRemote) {
                playerIn.sendMessage(new TextComponentTranslation("am2.tooltip.candlecantplace"));
            }
            return EnumActionResult.FAIL;
        }

        if (!worldIn.isRemote) {

            pos = pos.offset(facing);

            IBlockState block = worldIn.getBlockState(pos);
            if (block.getBlock().isReplaceable(worldIn, pos)) {
                worldIn.setBlockState(pos, AMBlocks.warding_candle.getDefaultState(), 3);
                if (!playerIn.capabilities.isCreativeMode)
                    playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, ItemStack.EMPTY);
            }
            return EnumActionResult.PASS;
        }
        return EnumActionResult.PASS;
    }


//	@Override
//	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ){
//
//		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("search_block")){
//			Block block = world.getBlock(x, y, z);
//			if (player.isSneaking() && block != null && block.getBlockHardness(world, x, y, z) > 0f && world.getTileEntity(x, y, z) == null){
//				if (!world.isRemote){
//					setSearchBlock(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z), stack);
//					world.setBlockToAir(x, y, z);
//				}else{
//					AMParticle particle = (AMParticle)AMCore.proxy.particleManager.spawn(world, "radiant", x + 0.5, y + 0.5, z + 0.5);
//					if (particle != null){
//						particle.AddParticleController(new ParticleHoldPosition(particle, 20, 1, false));
//						particle.setRGBColorF(0, 0.5f, 1);
//					}
//				}
//				return true;
//			}
//		}
//
//		if (!world.isRemote){
//
//			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("search_block")){
//				player.sendMessage(new ChatComponentText(I18n.translateToLocalFormatted("am2.tooltip.candlecantplace")));
//				return false;
//			}
//
//			switch (side){
//			case 0:
//				y--;
//				break;
//			case 1:
//				y++;
//				break;
//			case 2:
//				z--;
//				break;
//			case 3:
//				z++;
//				break;
//			case 4:
//				x--;
//				break;
//			case 5:
//				x++;
//				break;
//			}
//
//			Block block = world.getBlock(x, y, z);
//			if (block == null || block.isReplaceable(world, x, y, z)){
//				int newMeta = (int)Math.ceil(stack.getItemDamage() / 1200);
//				world.setBlock(x, y, z, Blockdef.candle, newMeta, 2);
//				if (!player.capabilities.isCreativeMode)
//					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
//			}
//			return true;
//		}
//		return false;
//	}

    public void setSearchBlock(IBlockState state, ItemStack item) {
        if (!item.hasTagCompound())
            item.setTagCompound(new NBTTagCompound());

        item.getTagCompound().setInteger("search_block", Block.getStateId(state));
        item.getTagCompound().setInteger("proximity", 0); // default: farther than 15 blocks
    }

    public void search(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
        if (state == null || state.getBlock() == net.minecraft.init.Blocks.AIR) return;

        boolean found = false;
        int closestProximity = 0; // 0 = far, 1 = within 15, 2 = within 5
        Block searchBlock = state.getBlock();
        int searchMeta = searchBlock.getMetaFromState(state);

        // Match original: search horizontal radius but only Y -1 to +1
        for (int i = -radius; i <= radius; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -radius; k <= radius; ++k) {
                    IBlockState f_state = world.getBlockState(pos.add(i, j, k));
                    Block f_block = f_state.getBlock();
                    int f_meta = f_block.getMetaFromState(f_state);

                    // Compare block type and metadata
                    if (searchBlock == f_block && searchMeta == f_meta) {
                        found = true;
                        int dist = Math.max(Math.abs(i), Math.abs(k)); // horizontal distance
                        if (dist <= short_radius) {
                            // Within 5 blocks - red (highest priority)
                            closestProximity = 2;
                            // Early return since we found the closest possible
                            if (!stack.hasTagCompound())
                                stack.setTagCompound(new NBTTagCompound());
                            stack.getTagCompound().setInteger("proximity", closestProximity);
                            return;
                        } else if (closestProximity < 1) {
                            // Within 15 blocks but farther than 5 - blue
                            closestProximity = 1;
                        }
                    }
                }
            }
        }

        // Update proximity NBT for client-side model switching
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setInteger("proximity", closestProximity);
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int indexInInventory, boolean isCurrentlyHeld) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            boolean isMainHand = player.getHeldItemMainhand() == stack;
            boolean isOffHand = player.getHeldItemOffhand() == stack;
            isCurrentlyHeld = isMainHand || isOffHand;
            if (isCurrentlyHeld && !world.isRemote && ArsMagica.config.candlesAreRovingLights() && world.isAirBlock(player.getPosition()) && world.getLightFor(EnumSkyBlock.BLOCK, player.getPosition()) < 14) {
                world.setBlockState(player.getPosition(), AMBlocks.invisible_utility.getDefaultState().withProperty(BlockInvisibleUtility.TYPE, BlockInvisibleUtility.EnumInvisibleType.HIGH_ILLUMINATED), 2);
            }
            if (isCurrentlyHeld) {
                // Fix warding candle update bug: search more frequently and independently of damage
                if (!world.isRemote && stack.hasTagCompound() && stack.getTagCompound().hasKey("search_block") && world.getTotalWorldTime() % 10 == 0) {
                    int oldProximity = stack.getTagCompound().getInteger("proximity");
                    search(player, stack, world, player.getPosition(), Block.getStateById(stack.getTagCompound().getInteger("search_block")));
                    int newProximity = stack.getTagCompound().getInteger("proximity");
                    // Force sync to client when proximity changes
                    if (oldProximity != newProximity) {
                        if (isMainHand) {
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
                        } else if (isOffHand) {
                            player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, stack);
                        }
                    }
                }

                if (!player.capabilities.isCreativeMode)
                    stack.damageItem(1, player);
                if (!world.isRemote && stack.getItemDamage() >= stack.getMaxDamage())
                    player.setItemStackToSlot((isOffHand ? EntityEquipmentSlot.OFFHAND : EntityEquipmentSlot.MAINHAND), ItemStack.EMPTY);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        String name = net.minecraft.client.resources.I18n.format("item.arsmagica2:warding_candle.name");
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("search_block")) {
            int stateId = stack.getTagCompound().getInteger("search_block");
            if (stateId != 0) {
                IBlockState state = Block.getStateById(stateId);
                if (state != null && state.getBlock() != net.minecraft.init.Blocks.AIR) {
                    ItemStack blockStack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
                    if (!blockStack.isEmpty()) {
                        name += " (" + blockStack.getDisplayName() + ")";
                    } else {
                        name += " (" + state.getBlock().getLocalizedName() + ")";
                    }
                } else {
                    name += " (" + net.minecraft.client.resources.I18n.format("am2.tooltip.unattuned") + ")";
                }
            } else {
                name += " (" + net.minecraft.client.resources.I18n.format("am2.tooltip.unattuned") + ")";
            }
        } else {
            name += " (" + net.minecraft.client.resources.I18n.format("am2.tooltip.unattuned") + ")";
        }

        return name;
    }

    @Override
    public boolean getHasSubtypes() {
        return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.isEmpty()) return slotChanged;
        if (newStack.isEmpty()) return slotChanged;
        if (oldStack.getTagCompound() == null) return slotChanged;
        if (newStack.getTagCompound() == null) return slotChanged;
        if (oldStack.getTagCompound().equals(newStack.getTagCompound())) return false;
        return slotChanged;
    }
}

