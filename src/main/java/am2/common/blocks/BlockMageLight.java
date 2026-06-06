package am2.common.blocks;

import am2.ArsMagica;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFloatUpward;
import am2.client.particles.ParticleGrow;
import am2.client.particles.ParticleOrbitPoint;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockMageLight extends BlockAMSpecialRender {

    public static final PropertyEnum<EnumDyeColor> COLOR = PropertyEnum.create("color", EnumDyeColor.class);

    public BlockMageLight() {
        super(Material.CIRCUITS);
        this.setDefaultState(blockState.getBaseState().withProperty(COLOR, EnumDyeColor.WHITE));
        setBoundingBox(0.35f, 0.35f, 0.35f, 0.65f, 0.65f, 0.65f);
        this.setTickRandomly(true);
    }

    private EnumDyeColor getEnumDyeColorFromItemStack(ItemStack stack) {
        if (stack.getItem() == Items.DYE) {
            return EnumDyeColor.byMetadata(stack.getMetadata());
        }
        int[] ids = OreDictionary.getOreIDs(stack);
        for (int id : ids) {
            List<ItemStack> ores = OreDictionary.getOres(OreDictionary.getOreName(id));
            for (ItemStack s : ores) {
                if (s.getItem() == Items.DYE) {
                    return EnumDyeColor.byDyeDamage(s.getMetadata());
                }
            }
        }
        return null;
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if(!worldIn.isRemote) return;
        AxisAlignedBB hitbox = getBoundingBox(stateIn, worldIn, pos);
        int color = stateIn.getValue(COLOR).getColorValue();
        final float speed = 0.01F;
        for(int i = 0; i < 2 * ArsMagica.config.getGFXLevel(); ++i) {
            double ry = rand.nextDouble();
            double dx = (hitbox.maxX - hitbox.minX) * rand.nextDouble();
            double dz = (hitbox.maxZ - hitbox.minZ) * rand.nextDouble();
            double dy = (hitbox.maxY - hitbox.minY) * (1 - ry * ry);
            float scale = 0.05F + 0.25F * rand.nextFloat();
            int age = MathHelper.ceil(dy / speed);
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(
                    worldIn,
                    "sparkle",
                    pos.getX() + hitbox.minX + dx,
                    pos.getY() + hitbox.minY + dy,
                    pos.getZ() + hitbox.minZ + dz
            );
            if (particle != null) {
                particle.setIgnoreMaxAge(false);
                particle.setMaxAge(age);
                particle.setParticleScale(scale);
                particle.AddParticleController(new ParticleFloatUpward(particle, 0f, -speed, 1, false));
                particle.AddParticleController(new ParticleGrow(particle, -scale * speed, 1, false));
                particle.setRGBColorI(color);
            }
        }
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote && stack != null && !stack.isEmpty()) {
            EnumDyeColor dye = getEnumDyeColorFromItemStack(stack);
            if(dye != null) {
                world.setBlockState(pos, state.withProperty(COLOR, EnumDyeColor.byDyeDamage(dye.getMetadata())), 3);
//                stack.splitStack(1);
//                player.inventory.markDirty();
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 15;
    }

    @Override
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
        return super.getPackedLightmapCoords(state, source, pos);
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    public static final AxisAlignedBB COLLISION_AABB = new AxisAlignedBB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return COLLISION_AABB;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
                                      @Nullable Entity entityIn, boolean isActualState) {
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return ItemStack.EMPTY;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, COLOR);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(COLOR).getMetadata();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(COLOR, EnumDyeColor.byMetadata(meta));
    }

    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        return true; // disable particles
    }

    @Override
    public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
        return true; // disable particles
    }

    @Override
    public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {
        return true; // disable particles
    }

    @Override
    public boolean addRunningEffects(IBlockState state, World world, BlockPos pos, Entity entity) {
        return true; // disable particles
    }

}
