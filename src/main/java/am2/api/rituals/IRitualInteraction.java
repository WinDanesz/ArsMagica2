package am2.api.rituals;

import am2.api.blocks.IMultiblock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRitualInteraction {

    public ItemStack[] getRitualReagents();

    default int getRitualReagentSearchRadius() {
        return 3;
    }

    public IMultiblock getRitualShape();

    @SideOnly(Side.CLIENT)
    default ItemStack getResult() {
        return null;
    }

    public static class Wrapper {

        private final IRitualInteraction interaction;

        public Wrapper(IRitualInteraction interaction) {
            this.interaction = interaction;
        }

        public IRitualInteraction getRitualInteraction() {
            return interaction;
        }

    }
}
