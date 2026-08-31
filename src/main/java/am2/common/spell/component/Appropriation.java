package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleOrbitPoint;
import am2.common.registry.AMBlocks;
import am2.common.registry.Affinities;
import am2.common.utils.DummyEntityPlayer;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Appropriation extends SpellComponent {

    private static final String storageKey = "stored_data";
    private static final String storageType = "storage_type";

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                Items.ENDER_PEARL,
                new ItemStack(AMBlocks.chimerite_block),
                Blocks.CHEST
        };
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (target instanceof EntityPlayer)
            return false;

        if (!(target instanceof EntityLivingBase))
            return false;

        for (Class clazz : ArsMagica.config.getAppropriationMobBlacklist())
            if (clazz != null && target.getClass() == clazz)
                return false;

        if (!(caster instanceof EntityPlayer))
            return false;

        ItemStack originalSpellStack = spell.getSource();
        if (originalSpellStack.isEmpty())
            return false;

        if (!world.isRemote) {
            if (originalSpellStack.getTagCompound().hasKey(storageKey)) {
                restore((EntityPlayer) caster, world, originalSpellStack, target.getPosition(), target.posX, target.posY + target.getEyeHeight(), target.posZ);
            } else {
                NBTTagCompound data = new NBTTagCompound();
                data.setString("class", target.getClass().getName());
                data.setString(storageType, "ent");

                NBTTagCompound targetData = new NBTTagCompound();
                target.writeToNBT(targetData);

                if (isNBTTooDeepOrContains(targetData, storageKey, 0, 20)) {
                    ((EntityPlayer) caster).sendMessage(new TextComponentTranslation("am2.tooltip.approTooComplex"));
                    return false;
                }

                data.setTag("targetNBT", targetData);

                originalSpellStack.getTagCompound().setTag(storageKey, data);

                target.setDead();
            }
        }

        return true;
    }

    private void restore(EntityPlayer player, World world, ItemStack stack, BlockPos pos, double hitX, double hitY, double hitZ) {
        if (stack.getTagCompound().hasKey(storageKey)) {
            NBTTagCompound storageCompound = stack.getTagCompound().getCompoundTag(storageKey);
            if (storageCompound != null) {
                String type = storageCompound.getString(storageType);
                if (type.equals("ent")) {
                    String clazz = storageCompound.getString("class");
                    NBTTagCompound entData = storageCompound.getCompoundTag("targetNBT");
                    try {
                        Entity ent = (Entity) Class.forName(clazz).getConstructor(World.class).newInstance(world);
                        ent.readFromNBT(entData);
                        ent.setPosition(hitX, hitY, hitZ);
                        world.spawnEntity(ent);
                    } catch (Throwable t) {
                        ArsMagica.LOGGER.error("Exception caught: ", t);
                    }
                } else if (type.equals("block")) {
                    //String blockName = storageCompound.getString("blockName");
                    int stateID = storageCompound.getInteger("stateID");

                    //Block block = Block.getBlockFromName(blockName);
                    IBlockState state = Block.getStateById(stateID);
                    if (state != null) {
                        world.setBlockState(pos, state, 2);
                    } else {
                        if (!player.world.isRemote)
                            player.sendMessage(new TextComponentTranslation("am2.tooltip.approError"));
                        stack.getTagCompound().removeTag(storageKey);
                        return;
                    }


                    if (storageCompound.hasKey("tileEntity")) {
                        TileEntity te = world.getTileEntity(pos);
                        if (te != null) {
                            te.readFromNBT(storageCompound.getCompoundTag("tileEntity"));
                            te.setPos(pos);
                            te.setWorld(world);
                        }
                    }
                }
            }

            stack.getTagCompound().removeTag(storageKey);
        }
    }

    private static boolean isNBTTooDeepOrContains(NBTBase nbt, String key, int currentDepth, int maxDepth) {
        if (currentDepth > maxDepth) return true;
        if (nbt instanceof NBTTagCompound compound) {
            if (compound.hasKey(key)) return true;
            for (String k : compound.getKeySet()) {
                if (isNBTTooDeepOrContains(compound.getTag(k), key, currentDepth + 1, maxDepth)) {
                    return true;
                }
            }
        } else if (nbt instanceof NBTTagList list) {
            for (int i = 0; i < list.tagCount(); i++) {
                if (isNBTTooDeepOrContains(list.get(i), key, currentDepth + 1, maxDepth)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public float manaCost() {
        return 415;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 5 + 5 * ArsMagica.config.getGFXLevel(); ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "water_ball", x, y, z);
            if (particle != null) {
                particle.addRandomOffset(1, 1, 1);
                particle.setMaxAge(10);
                particle.setParticleScale(0.1f);
                particle.AddParticleController(new ParticleOrbitPoint(particle, x, y, z, 1, false).SetTargetDistance(world.rand.nextDouble() + 0.1f).SetOrbitSpeed(0.2f));
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.water);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0;
    }

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos blockPos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        if (!(caster instanceof EntityPlayer))
            return false;

        ItemStack originalSpellStack = spell.getSource();
        if (originalSpellStack.isEmpty()) {
            return false;
        }

        if (originalSpellStack.getTagCompound() == null) {
            return false;
        }

        IBlockState state = world.getBlockState(blockPos);
        for (String s : ArsMagica.config.getAppropriationBlockBlacklist())
            if (!s.isEmpty() && state.getBlock().getRegistryName().toString().equals(s))
                return false;

        if (!world.isRemote) {
            if (originalSpellStack.getTagCompound().hasKey(storageKey)) {

                if (world.getBlockState(blockPos).equals(Blocks.AIR.getDefaultState())) blockFace = null;
                if (blockFace != null) {
                    blockPos = blockPos.add(blockFace.getDirectionVec());
                }

                if (world.isAirBlock(blockPos) || !world.getBlockState(blockPos).getMaterial().isSolid()) {

                    // save current spell
                    NBTTagCompound nbt = null;
                    if (spell.getStoredData() != null) {
                        nbt = spell.getStoredData().copy();
                    }


                    EntityPlayerMP casterPlayer = (EntityPlayerMP) DummyEntityPlayer.fromEntityLiving(caster);
                    world.captureBlockSnapshots = true;
                    restore((EntityPlayer) caster, world, originalSpellStack, blockPos, impactX, impactY, impactZ);
                    world.captureBlockSnapshots = false;

                    // save new spell data
                    NBTTagCompound newNBT = null;
                    if (spell.getStoredData() != null) {
                        newNBT = spell.getStoredData().copy();
                    }

                    BlockEvent.PlaceEvent placeEvent = null;
                    @SuppressWarnings("unchecked")
                    List<BlockSnapshot> blockSnapshots = (List<BlockSnapshot>) world.capturedBlockSnapshots.clone();
                    world.capturedBlockSnapshots.clear();

                    // restore original item data for event
                    if (nbt != null) {
                        spell.setStoredData(nbt);
                    }
                    if (blockSnapshots.size() > 1) {
                        placeEvent = ForgeEventFactory.onPlayerMultiBlockPlace(casterPlayer, blockSnapshots, blockFace, EnumHand.MAIN_HAND);
                    } else if (blockSnapshots.size() == 1) {
                        placeEvent = ForgeEventFactory.onPlayerBlockPlace(casterPlayer, blockSnapshots.get(0), blockFace, EnumHand.MAIN_HAND);
                    }

                    if (placeEvent != null && (placeEvent.isCanceled())) {
                        // revert back all captured blocks
                        for (BlockSnapshot blocksnapshot : blockSnapshots) {
                            world.restoringBlockSnapshots = true;
                            blocksnapshot.restore(true, false);
                            world.restoringBlockSnapshots = false;
                        }
                        return false;
                    } else {
                        // Change the stack to its new content
                        if (nbt != null) {
                            spell.setStoredData(newNBT);
                        }

                        for (BlockSnapshot blocksnapshot : blockSnapshots) {
                            BlockPos pos = blocksnapshot.getPos();
                            int updateFlag = blocksnapshot.getFlag();
                            IBlockState oldBlock = blocksnapshot.getReplacedBlock();
                            IBlockState newBlock = world.getBlockState(pos);
                            if (newBlock != null && !(newBlock.getBlock().hasTileEntity(newBlock))) { // Containers get placed automatically
                                newBlock.getBlock().onBlockAdded(world, pos, newBlock);
                            }

                            world.markAndNotifyBlock(pos, null, oldBlock, newBlock, updateFlag);
                        }
                    }
                    world.capturedBlockSnapshots.clear();

                    // restore((EntityPlayer)caster, world, originalSpellStack, blockx, blocky, blockz, impactX, impactY, impactZ);
                }
            } else {

                if (state == null || state.getBlockHardness(world, blockPos) == -1.0f) {
                    return false;
                }

                NBTTagCompound data = new NBTTagCompound();
                data.setString(storageType, "block");
                //data.setString("blockName", block.getUnlocalizedName().replace("tile.", ""));
                data.setInteger("stateID", Block.getStateId(state));

                EntityPlayerMP casterPlayer = (EntityPlayerMP) DummyEntityPlayer.fromEntityLiving(caster);
                if (!ForgeEventFactory.doPlayerHarvestCheck(casterPlayer, world.getBlockState(blockPos), true)) {
                    return false;
                }

                int event = ForgeHooks.onBlockBreakEvent(world, casterPlayer.interactionManager.getGameType(), casterPlayer, blockPos);
                if (event == -1) {
                    return false;
                }

                TileEntity te = world.getTileEntity(blockPos);
                if (te != null) {
                    NBTTagCompound teData = new NBTTagCompound();
                    te.writeToNBT(teData);

                    if (isNBTTooDeepOrContains(teData, storageKey, 0, 20)) {
                        ((EntityPlayer) caster).sendMessage(new TextComponentTranslation("am2.tooltip.approTooComplex"));
                        return false;
                    }

                    data.setTag("tileEntity", teData);

                    // remove tile entity first to prevent content dropping which is already saved in the NBT
                    try {
                        world.removeTileEntity(blockPos);
                    } catch (Throwable exception) {
                        ArsMagica.LOGGER.error("Exception caught: ", exception);
                    }
                }

                originalSpellStack.getTagCompound().setTag(storageKey, data);

                world.setBlockToAir(blockPos);
            }
        }

        return true;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

}
