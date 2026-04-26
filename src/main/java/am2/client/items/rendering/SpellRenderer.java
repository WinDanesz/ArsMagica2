package am2.client.items.rendering;

import am2.ArsMagica;
import am2.common.LogHelper;
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class SpellRenderer implements ItemMeshDefinition {

    public static List<ResourceLocation> resources;
    private static final String iconsPath = "/assets/arsmagica2/textures/items/spells/icons/";
    private static final String iconsPrefix = "spells/icons/";
    private List<ModelResourceLocation> locations = Lists.newArrayList();

    public SpellRenderer() {
        resources = getResourceListing();
        for (ResourceLocation resource : resources) {
            locations.add(new ModelResourceLocation(resource, "inventory"));
            // Model registration now happens in AMModels.onModelBake
        }
        LogHelper.info("Sucessfully Loaded " + locations.size() + " Spell Icons");
    }

    public static List<ResourceLocation> getResourceListing() {
        ArrayList<ResourceLocation> toReturn = new ArrayList<>();
        try {
            URI uri = ArsMagica.class.getResource(iconsPath).toURI();
            Path myPath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
                myPath = fs.getPath(iconsPath);
                toReturn = processDirectory(myPath, fs);
                fs.close();
                return toReturn;
            } else {
                myPath = Paths.get(uri);
                toReturn = processDirectory(myPath, FileSystems.getDefault());
                return toReturn;
            }
        } catch (URISyntaxException e) {
            am2.ArsMagica.LOGGER.error("SpellRenderer.getResourceListingURISyntaxException caught: ", e);
        } catch (IOException e) {
            am2.ArsMagica.LOGGER.error("SpellRenderer.getResourceListingIOException caught: ", e);
        }
        return Lists.newArrayList();
    }

    private static ArrayList<ResourceLocation> processDirectory(Path dir, FileSystem fs) {
        ArrayList<ResourceLocation> toReturn = new ArrayList<>();
        //Was a memory leak
        try (Stream<Path> walk = Files.walk(dir, 1)) {
            for (Iterator<Path> file = walk.iterator(); file.hasNext(); ) {
                String name = file.next().toString();
                if (name.lastIndexOf(fs.getSeparator()) + 1 > name.length()) continue;
                name = name.substring(name.lastIndexOf(fs.getSeparator()) + 1);
                if (name.equals("") || !name.endsWith(".png")) continue;
                toReturn.add(new ResourceLocation("arsmagica2:" + iconsPrefix + name.replace(".png", "")));
            }
        } catch (IOException e) {
            am2.ArsMagica.LOGGER.error("SpellRenderer.processDirectory IOException caught: ", e);
        }
        Collections.sort(toReturn, (o1, o2) -> o1.toString().compareTo(o2.toString()));
        return toReturn;
    }

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        return locations.get(MathHelper.clamp(stack.getItemDamage(), 0, locations.size() - 1));
    }

}
