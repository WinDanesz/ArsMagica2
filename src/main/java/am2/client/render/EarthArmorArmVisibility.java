package am2.client.render;

import am2.common.registry.AMItems;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.IdentityHashMap;
import java.util.Map;

/** Suppresses player skin geometry without disabling hand transforms. */
@SideOnly(Side.CLIENT)
public final class EarthArmorArmVisibility {
    private static final Map<ModelPlayer, HiddenParts> STATES = new IdentityHashMap<>();

    private EarthArmorArmVisibility() {}

    public static void begin(RenderPlayer renderer, EntityPlayer player) {
        ModelPlayer model = renderer.getMainModel();
        restore(model);
        if (player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == AMItems.earth_armor) {
            hide(model);
        }
    }

    public static void end(RenderPlayer renderer) {
        restore(renderer.getMainModel());
    }

    static void hide(ModelPlayer model) {
        HiddenParts state = STATES.computeIfAbsent(model, HiddenParts::new);
        state.restore(model);
        state.originals = parts(model);
        for (int i = 0; i < state.empty.length; i++) {
            state.empty[i].original = state.originals[i];
            copyPose(state.originals[i], state.empty[i]);
            state.empty[i].childModels = state.originals[i].childModels;
        }
        setParts(model, state.empty);
    }

    static void restore(ModelPlayer model) {
        HiddenParts state = STATES.get(model);
        if (state != null) state.restore(model);
    }

    private static ModelRenderer[] parts(ModelPlayer model) {
        return new ModelRenderer[] {model.bipedLeftArm, model.bipedRightArm,
                model.bipedLeftArmwear, model.bipedRightArmwear, model.bipedBody, model.bipedBodyWear};
    }

    private static void setParts(ModelPlayer model, ModelRenderer[] parts) {
        model.bipedLeftArm = parts[0];
        model.bipedRightArm = parts[1];
        model.bipedLeftArmwear = parts[2];
        model.bipedRightArmwear = parts[3];
        model.bipedBody = parts[4];
        model.bipedBodyWear = parts[5];
        // Cape, head and legs retain their original model parts.
    }

    private static void copyPose(ModelRenderer from, ModelRenderer to) {
        to.rotationPointX = from.rotationPointX;
        to.rotationPointY = from.rotationPointY;
        to.rotationPointZ = from.rotationPointZ;
        to.rotateAngleX = from.rotateAngleX;
        to.rotateAngleY = from.rotateAngleY;
        to.rotateAngleZ = from.rotateAngleZ;
        to.offsetX = from.offsetX;
        to.offsetY = from.offsetY;
        to.offsetZ = from.offsetZ;
        to.showModel = from.showModel;
        to.isHidden = from.isHidden;
        to.mirror = from.mirror;
    }

    private static final class HiddenParts {
        private final EmptyPart[] empty = new EmptyPart[6];
        private ModelRenderer[] originals;

        private HiddenParts(ModelPlayer model) {
            for (int i = 0; i < empty.length; i++) {
                empty[i] = new EmptyPart(model);
                // Arrow placement selects cuboids from this list; keep the original
                // populated parts there instead of adding empty candidates.
                model.boxList.remove(empty[i]);
            }
        }

        private void restore(ModelPlayer model) {
            if (originals == null) return;
            for (int i = 0; i < empty.length; i++) {
                copyPose(empty[i], originals[i]);
                empty[i].original = null;
                empty[i].childModels = null;
            }
            setParts(model, originals);
            originals = null;
        }
    }

    private static final class EmptyPart extends ModelRenderer {
        private ModelRenderer original;

        private EmptyPart(ModelPlayer model) {
            super(model);
        }

        @Override
        public void render(float scale) {
            // Keep the original pose current for layers holding references to it,
            // including vanilla arrows. Child attachments still render normally.
            if (original != null) copyPose(this, original);
            super.render(scale);
        }

        @Override
        public void renderWithRotation(float scale) {
            if (original != null) copyPose(this, original);
            super.renderWithRotation(scale);
        }
    }
}