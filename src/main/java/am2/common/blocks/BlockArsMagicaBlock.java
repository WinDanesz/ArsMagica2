package am2.common.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public class BlockArsMagicaBlock extends BlockAM {

    public static final PropertyEnum<EnumBlockType> BLOCK_TYPE = PropertyEnum.create("block_type", EnumBlockType.class);


    public BlockArsMagicaBlock() {
        super(Material.ROCK);
        // setDefaultState(blockState.getBaseState().withProperty(BLOCK_TYPE, EnumBlockType.VINTEUM));
    }

    public static enum EnumBlockType implements IStringSerializable {
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
