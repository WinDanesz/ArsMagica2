package am2.common.blocks;

import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.AMTabs;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;

import java.util.Random;

public class BlockAMOre extends BlockAM {

    public static final PropertyEnum<EnumOreType> ORE_TYPE = PropertyEnum.create("ore_type", EnumOreType.class);

    public BlockAMOre() {
        super(Material.ROCK);
        //  setDefaultState(blockState.getBaseState().withProperty(ORE_TYPE, EnumOreType.VINTEUM));
        setHardness(3.0F);
        setResistance(5.0F);
        setHarvestLevel("pickaxe", 2);
        setCreativeTab(AMTabs.AMBLOCKS);
    }

//	@Override
//	public BlockStateContainer createBlockState() {
//		return new BlockStateContainer(this, BlockArsMagicaOre.ORE_TYPE);
//	}

    /**
     * Chimerite drops 1-5 ores
     * Vinteum drops itself
     * Sunstone and moonstone drops 1-2 ores
     */
    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        int amount = 1;
        if (this == AMBlocks.chimerite_ore) {
            amount += random.nextInt(4);
        } else if (this != AMBlocks.vinteum_ore && random.nextBoolean()) {
            amount += 1;
        }
        return amount;
    }

    //	@Override
//	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
//		for (int i = 0; i < EnumOreType.values().length; i++) {
//			items.add(new ItemStack(this, 1, i));
//		}
//	}

    //	@Override
//	public int getMetaFromState(IBlockState state) {
//		return state.getValue(ORE_TYPE).ordinal();
//	}
//
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (this == AMBlocks.blue_topaz_ore) {
            return AMItems.blue_topaz;
        } else if (this == AMBlocks.moonstone_ore) {
            return AMItems.moonstone;
        } else if (this == AMBlocks.sunstone_ore) {
            return AMItems.sunstone;
        } else if (this == AMBlocks.chimerite_ore) {
            return AMItems.chimerite;
        }
        return super.getItemDropped(state, rand, fortune);
    }

//	@Override
//	public int damageDropped(IBlockState state) {
//		EnumOreType type = state.getValue(ORE_TYPE);
//		if (type == EnumOreType.VINTEUM)
//			return EnumOreType.VINTEUM.ordinal();
//		if (type == EnumOreType.CHIMERITE)
//			return ItemOre.META_CHIMERITE;
//		if (type == EnumOreType.BLUETOPAZ)
//			return ItemOre.META_BLUE_TOPAZ;
//		if (type == EnumOreType.MOONSTONE)
//			return ItemOre.META_MOONSTONE;
//		if (type == EnumOreType.SUNSTONE)
//			return ItemOre.META_SUNSTONE;
//		return super.damageDropped(state);
//	}

//	@Override
//	public IBlockState getStateFromMeta(int meta) {
//		return getDefaultState().withProperty(ORE_TYPE, EnumOreType.values()[MathHelper.clamp(meta, 0, EnumOreType.values().length - 1)]);
//	}

    public enum EnumOreType implements IStringSerializable {
        VINTEUM,
        CHIMERITE,
        BLUETOPAZ,
        MOONSTONE,
        SUNSTONE;

        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }

}
