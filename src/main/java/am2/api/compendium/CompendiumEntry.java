package am2.api.compendium;

import am2.ArsMagica;
import am2.api.compendium.pages.CompendiumPage;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompendiumEntry {

    /**
     * Width of text area in pixels
     */
    private static final int LINE_WIDTH = 140;
    /**
     * Max lines per page (after first)
     */
    private static final int MAX_LINES = 22;
    /**
     * Max lines on first page (less due to title)
     */
    private static final int MAX_LINES_FIRST_PAGE = MAX_LINES - 2;

    private CompendiumCategory category;
    private String id;
    private String customName;
    private Object renderObject;
    private ArrayList<Object> objects;
    private boolean isDefaultUnlocked;
    private List<String> relatedEntries;    private int order = -1;
    public CompendiumEntry(@Nullable Object renderObject, String id) {
        if (id.contains("\\.")) throw new IllegalArgumentException("Entry ids can't contain \"\\.\"");
        this.id = id;
        this.objects = new ArrayList<>();
        this.renderObject = renderObject;
        this.relatedEntries = new ArrayList<>();
    }

    public CompendiumEntry setCategory(CompendiumCategory category) {
        this.category = category;
        return this;
    }

    public CompendiumCategory getCategory() {
        return this.category;
    }

    public boolean canBeDisplayed(String categoryID) {
        return categoryID.equals(category.getID());
    }

    public String getID() {
        return category.getID() + "." + id;
    }

    public String getName() {
        if (customName != null) {
            return customName;
        }
        return I18n.format("compendium." + this.getID() + ".name");
    }

    public CompendiumEntry setName(String name) {
        this.customName = name;
        return this;
    }

    public String getDescription() {
        return I18n.format("compendium." + this.getID() + ".desc");
    }

    public boolean isDefaultUnlocked() {
        return isDefaultUnlocked;
    }

    public Object getRenderObject() {
        return renderObject;
    }

    public CompendiumEntry setUnlocked() {
        this.isDefaultUnlocked = true;
        return this;
    }

    public CompendiumEntry addObject(Object obj) {
        objects.add(obj);
        return this;
    }

    public CompendiumEntry setRelatedEntries(String... entryIds) {
        this.relatedEntries = Arrays.asList(entryIds);
        return this;
    }

    public List<String> getRelatedEntries() {
        return relatedEntries;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @SideOnly(Side.CLIENT)
    public ArrayList<CompendiumPage<?>> getPages() {
        ArrayList<CompendiumPage<?>> pages = new ArrayList<>();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        boolean isFirstTextPage = true;

        for (Object obj : objects) {
            if (obj == null) continue;

            if (obj instanceof String) {
                String text = (String) obj;
                List<String> textPages = splitStringToPages(fontRenderer, text, LINE_WIDTH,
                        isFirstTextPage ? MAX_LINES_FIRST_PAGE : MAX_LINES);

                for (String pageText : textPages) {
                    CompendiumPage<?> page = CompendiumPage.getCompendiumPage(String.class, pageText);
                    if (page != null) {
                        pages.add(page);
                    }
                    isFirstTextPage = false;
                }
            } else if (obj instanceof Class && Entity.class.isAssignableFrom((Class<?>) obj)) {
                World world = Minecraft.getMinecraft().world;
                if (world != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        Class<? extends Entity> entityClass = (Class<? extends Entity>) obj;
                        Entity entity = entityClass.getConstructor(World.class).newInstance(world);
                        CompendiumPage<?> page = CompendiumPage.getCompendiumPage(Entity.class, entity);
                        if (page != null) pages.add(page);
                    } catch (Exception e) {
                        ArsMagica.LOGGER.error("Failed to instantiate entity class for compendium: {}", obj, e);
                    }
                }
            } else {
                CompendiumPage<?> page = CompendiumPage.getCompendiumPage(obj.getClass(), obj);
                if (page != null)
                    pages.add(page);
            }
        }
        return pages;
    }

    /**
     * Splits a string into multiple page-sized chunks based on actual font width.
     * Adapted from original AM2 1.7.10 splitStringToLines.
     */
    @SideOnly(Side.CLIENT)
    private List<String> splitStringToPages(FontRenderer fontRenderer, String string, int lineWidth, int maxLinesFirstPage) {
        List<String> toReturn = new ArrayList<>();
        int numLines = 0;
        int len = 0;
        int pageCount = 0;

        // Convert markers for processing
        string = string.replace("!d", " !d ").replace("!l", " !l ");
        String[] words = string.split(" ");
        StringBuilder sb = new StringBuilder();
        String curLine = "";

        for (String s : words) {
            s = s.trim();
            if (s.isEmpty()) continue;

            // Calculate word width (removing color codes #X for width calculation)
            int wordWidth = fontRenderer.getStringWidth(s.replaceAll("#.", "") + " ");

            // Handle paragraph break (double newline)
            if (s.equals("!d")) {
                sb.append(curLine);
                curLine = "";
                len = 0;
                numLines++;

                int maxLines = (pageCount == 0) ? maxLinesFirstPage : MAX_LINES;
                if (numLines >= maxLines) {
                    toReturn.add(sb.toString());
                    sb = new StringBuilder();
                    pageCount++;
                    numLines = 0;
                } else {
                    sb.append(" !d ");
                    numLines++; // !d adds 2 lines total
                    if (numLines >= maxLines) {
                        toReturn.add(sb.toString());
                        sb = new StringBuilder();
                        pageCount++;
                        numLines = 0;
                    }
                }
                continue;
            }

            // Handle line break (single newline)
            if (s.equals("!l")) {
                sb.append(curLine);
                curLine = "";
                len = 0;
                numLines++;

                int maxLines = (pageCount == 0) ? maxLinesFirstPage : MAX_LINES;
                if (numLines >= maxLines) {
                    toReturn.add(sb.toString());
                    sb = new StringBuilder();
                    pageCount++;
                    numLines = 0;
                } else {
                    sb.append(" !l ");
                }
                continue;
            }

            // Check if word wraps to next line
            if (len + wordWidth > lineWidth) {
                sb.append(curLine);
                curLine = "";
                len = 0;
                numLines++;

                int maxLines = (pageCount == 0) ? maxLinesFirstPage : MAX_LINES;
                if (numLines >= maxLines) {
                    toReturn.add(sb.toString());
                    sb = new StringBuilder();
                    pageCount++;
                    numLines = 0;
                }
            }

            curLine = curLine + " " + s;
            len += wordWidth;
        }

        sb.append(curLine);

        if (sb.length() > 0) {
            toReturn.add(sb.toString());
        }

        return toReturn;
    }

    public ImmutableList<Object> getObjects() {
        if (objects == null) {
            return ImmutableList.copyOf(new ArrayList<Object>());
        }
        return ImmutableList.copyOf(objects);
    }
}
