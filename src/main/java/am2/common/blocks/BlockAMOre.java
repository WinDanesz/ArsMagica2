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

    public BlockAMOre() {
        super(Material.ROCK);
        setHardness(3.0F);
        setResistance(5.0F);
        setHarvestLevel("pickaxe", 2);
        setCreativeTab(AMTabs.AMBLOCKS);
    }

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
