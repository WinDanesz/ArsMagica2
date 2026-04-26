package am2.client.entity.render;

import am2.common.entity.EntityDarkMage;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class RenderDarkMage extends RenderBiped<EntityDarkMage> {

    private final HashMap<String, ResourceLocation> resourceLocations;

    public RenderDarkMage(RenderManager manager) {
        super(manager, new ModelBiped(), 0.5f);
        resourceLocations = new HashMap<String, ResourceLocation>();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDarkMage ent) {
        String tex = ent.getTexture();
        if (resourceLocations.containsKey(tex)) {
            return resourceLocations.get(tex);
        } else {
            ResourceLocation rLoc = new ResourceLocation(tex);
            resourceLocations.put(tex, rLoc);
            return rLoc;
        }
    }

}
