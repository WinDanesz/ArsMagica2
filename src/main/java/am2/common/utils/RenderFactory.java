package am2.common.utils;

import am2.common.LogHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class RenderFactory<T extends Entity> implements IRenderFactory<T> {

    private Constructor<Render<T>> constructor;

    public RenderFactory(Class<Render<T>> clazz) {
        try {
            this.constructor = clazz.getConstructor(RenderManager.class);
        } catch (NoSuchMethodException | SecurityException e) {
            am2.ArsMagica.LOGGER.error("Exception caught: ", e);
        }
    }

    @Override
    public Render<T> createRenderFor(RenderManager manager) {
        try {
            Render<T> render = constructor.newInstance(manager);
            LogHelper.debug("Successfully created instance for : " + constructor.getDeclaringClass().getName());
            return render;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
