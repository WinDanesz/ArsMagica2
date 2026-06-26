package am2.common.lore;

import am2.api.SpellRegistryHelper;
import am2.api.blocks.IMultiblock;
import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.api.event.GenerateCompendiumLoreEvent;
import am2.api.rituals.RitualShapeHelper;
import am2.api.skill.Skill;
import am2.api.spell.SpellPart;
import am2.common.LogHelper;
import am2.common.blocks.tileentity.*;
import am2.common.registry.AMSkills;
import am2.common.registry.CompendiumRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

/**
 * Loader for the Arcane Compendium content from XML files.
 * Parses the XML and registers compendium entries accordingly.
 */
public class CompendiumXMLLoader {

    public static void loadFromXML() {
        try {
            ResourceLocation xmlLocation = new ResourceLocation("arsmagica2", "docs/arcanecompendium_en_us.xml");
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(xmlLocation);
            InputStream stream = resource.getInputStream();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(stream);

            Node parentNode = document.getChildNodes().item(0);
            NodeList categories = parentNode.getChildNodes();

            // Parse each category section
            for (int i = 0; i < categories.getLength(); ++i) {
                Node category = categories.item(i);
                String categoryName = category.getNodeName();

                // Skip version and text nodes
                if (categoryName.equals("version") || categoryName.equals("#text") || categoryName.equals("#comment")) {
                    continue;
                }

                // Parse category display names
                if (categoryName.equals("categories")) {
                    parseCategories(category);
                    continue;
                }

                // Parse entries within this category
                NodeList entries = category.getChildNodes();
                for (int j = 0; j < entries.getLength(); ++j) {
                    Node entryNode = entries.item(j);
                    String nodeName = entryNode.getNodeName();

                    // Skip text nodes
                    if (nodeName.equals("#text") || nodeName.equals("#comment")) {
                        continue;
                    }

                    // Parse based on category type
                    if (categoryName.equals("structures")) {
                        if (nodeName.equals("structure")) {
                            parseStructure(entryNode);
                        } else if (nodeName.equals("ritual")) {
                            parseRitual(entryNode);
                        }
                    } else if (categoryName.equals("items")) {
                        parseItem(entryNode);
                    } else if (categoryName.equals("shapes")) {
                        parseShape(entryNode);
                    } else if (categoryName.equals("components")) {
                        parseComponent(entryNode);
                    } else if (categoryName.equals("modifiers")) {
                        parseModifier(entryNode);
                    } else if (categoryName.equals("affinities")) {
                        parseAffinity(entryNode);
                    } else if (categoryName.equals("talents")) {
                        parseTalent(entryNode);
                    } else if (categoryName.equals("guides")) {
                        parseGuide(entryNode);
                    } else if (categoryName.equals("blocks")) {
                        parseBlock(entryNode);
                    } else if (categoryName.equals("mechanics")) {
                        parseMechanic(entryNode);
                    } else if (categoryName.equals("mobs")) {
                        parseMob(entryNode);
                    } else if (categoryName.equals("bosses")) {
                        parseBoss(entryNode);
                    }
                    // Add more categories as needed
                }
            }

            stream.close();
            LogHelper.info("Successfully loaded compendium from XML");
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to load compendium XML");
        }
    }

    private static void parseCategories(Node categoriesNode) {
        NodeList children = categoriesNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (!"category".equals(child.getNodeName())) {
                continue;
            }
            String id = child.getAttributes().getNamedItem("id").getNodeValue();
            String name = child.getAttributes().getNamedItem("name").getNodeValue();
            if (id != null && name != null) {
                CompendiumCategory category = CompendiumCategory.getCategoryFromID(id);
                if (category != null) {
                    category.setCategoryName(name);
                } else {
                    LogHelper.warn("Unknown compendium category ID in XML: " + id);
                }
            }
        }
    }

    private static void parseStructure(Node node) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;
            String controller = null;
            String relatedEntriesStr = null;

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = normalizeText(child.getTextContent());
                } else if (childName.equals("controller")) {
                    controller = normalizeText(child.getTextContent());
                } else if (childName.equals("relatedEntries")) {
                    relatedEntriesStr = normalizeText(child.getTextContent());
                }
            }

            if (name == null || id == null) {
                return;
            }

            // Create the entry
            IMultiblock multiblock = getMultiblockForController(controller);
            CompendiumEntry entry = new CompendiumEntry(multiblock, id);
            entry.setCategory(CompendiumCategory.STRUCTURE);
            if (name != null) entry.setName(name);

            // Add description pages
            addTextWithAutoPagination(entry, desc);

            // Add the multiblock structure visual if present
            if (multiblock != null) {
                entry.addObject(multiblock);
            }

            // Add related entries
            if (relatedEntriesStr != null && !relatedEntriesStr.trim().isEmpty()) {
                String[] related = relatedEntriesStr.split(",");
                for (int i = 0; i < related.length; i++) {
                    related[i] = related[i].trim();
                }
                entry.setRelatedEntries(related);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse structure entry");
        }
    }

    private static void parseRitual(Node node) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;
            String relatedEntriesStr = null;

            NodeList children = node.getChildNodes();
            boolean hasSubitems = false;

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = normalizeText(child.getTextContent());
                } else if (childName.equals("relatedEntries")) {
                    relatedEntriesStr = normalizeText(child.getTextContent());
                } else if (childName.equals("subitem")) {
                    hasSubitems = true;
                    parseRitualSubitem(child, id);
                }
            }

            if (name == null || id == null) {
                return;
            }

            // Create the main entry
            CompendiumEntry entry = new CompendiumEntry(null, id);
            entry.setCategory(CompendiumCategory.MECHANIC_RITUALS);
            if (name != null) entry.setName(name);

            // Add description pages
            addTextWithAutoPagination(entry, desc);

            // Add related entries
            if (relatedEntriesStr != null && !relatedEntriesStr.trim().isEmpty()) {
                String[] related = relatedEntriesStr.split(",");
                for (int i = 0; i < related.length; i++) {
                    related[i] = related[i].trim();
                }
                entry.setRelatedEntries(related);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse ritual entry");
        }
    }

    private static void parseRitualSubitem(Node node, String parentId) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;
            String ritualName = null;
            String relatedEntriesStr = null;

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = normalizeText(child.getTextContent());
                } else if (childName.equals("ritualName")) {
                    ritualName = normalizeText(child.getTextContent());
                } else if (childName.equals("relatedEntries")) {
                    relatedEntriesStr = normalizeText(child.getTextContent());
                }
            }

            if (name == null || id == null) {
                return;
            }

            // Get the ritual shape
            Object ritualShape = getRitualShape(ritualName);

            // Create the subitem entry
            CompendiumEntry entry = new CompendiumEntry(ritualShape, id);
            entry.setCategory(CompendiumCategory.MECHANIC_RITUALS);
            if (name != null) entry.setName(name);

            // Add description pages
            addTextWithAutoPagination(entry, desc);

            // Add the ritual shape as a page so it gets rendered
            if (ritualShape != null) {
                entry.addObject(ritualShape);
            }

            // Add related entries
            if (relatedEntriesStr != null && !relatedEntriesStr.trim().isEmpty()) {
                String[] related = relatedEntriesStr.split(",");
                for (int i = 0; i < related.length; i++) {
                    related[i] = related[i].trim();
                }
                entry.setRelatedEntries(related);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse ritual subitem entry");
        }
    }

    private static void parseItem(Node node) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;
            String relatedEntriesStr = null;

            NodeList children = node.getChildNodes();
            boolean hasSubitems = false;

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = normalizeText(child.getTextContent());
                } else if (childName.equals("relatedEntries")) {
                    relatedEntriesStr = normalizeText(child.getTextContent());
                } else if (childName.equals("subitem")) {
                    hasSubitems = true;
                    parseItemSubitem(child, id, desc);
                }
            }

            if (name == null || id == null) {
                return;
            }

            // If this item has subitems, it's a category grouping - don't create a parent entry
            // The subitems are registered under the appropriate sub-category
            if (hasSubitems) {
                return;
            }

            // Create the main item entry
            CompendiumEntry entry = new CompendiumEntry(null, id);
            entry.setCategory(CompendiumCategory.ITEM);
            if (name != null) entry.setName(name);

            // Add description pages
            addTextWithAutoPagination(entry, desc);

            // Add the item to display
            ItemStack itemStack = resolveItemFromId(id);
            if (!itemStack.isEmpty() && !itemStack.isEmpty()) {
                entry.addObject(itemStack);
            }

            // Add related entries
            if (relatedEntriesStr != null && !relatedEntriesStr.trim().isEmpty()) {
                String[] related = relatedEntriesStr.split(",");
                for (int i = 0; i < related.length; i++) {
                    related[i] = related[i].trim();
                }
                entry.setRelatedEntries(related);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse item entry");
        }
    }

    private static void parseItemSubitem(Node node, String parentId, String parentDesc) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;
            String relatedEntriesStr = null;

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = normalizeText(child.getTextContent());
                } else if (childName.equals("relatedEntries")) {
                    relatedEntriesStr = normalizeText(child.getTextContent());
                }
            }

            if (name == null || id == null) {
                return;
            }

            // Determine the sub-category based on parent ID
            CompendiumCategory category = getItemSubcategoryForParent(parentId);

            // Create the subitem entry
            CompendiumEntry entry = new CompendiumEntry(null, id);
            entry.setCategory(category);
            if (name != null) entry.setName(name);

            // Add description pages - use parent desc as fallback if subitem has no description
            String descToUse = (desc != null && !desc.trim().isEmpty()) ? desc : parentDesc;
            addTextWithAutoPagination(entry, descToUse);

            // Add the item to display
            ItemStack itemStack = resolveItemFromId(id);
            if (!itemStack.isEmpty() && !itemStack.isEmpty()) {
                entry.addObject(itemStack);
            }

            // Add related entries
            if (relatedEntriesStr != null && !relatedEntriesStr.trim().isEmpty()) {
                String[] related = relatedEntriesStr.split(",");
                for (int i = 0; i < related.length; i++) {
                    related[i] = related[i].trim();
                }
                entry.setRelatedEntries(related);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse item subitem entry");
        }
    }

    private static void parseBlock(Node node) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;
            String relatedEntriesStr = null;

            NodeList children = node.getChildNodes();
            boolean hasSubitems = false;

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = normalizeText(child.getTextContent());
                } else if (childName.equals("relatedEntries")) {
                    relatedEntriesStr = normalizeText(child.getTextContent());
                } else if (childName.equals("subitem")) {
                    hasSubitems = true;
                    parseBlockSubitem(child, id, desc);
                }
            }

            if (name == null || id == null) {
                return;
            }

            // If this block has subitems, it's a category grouping - don't create a parent entry
            // The subitems are registered under the appropriate sub-category
            if (hasSubitems) {
                return;
            }

            // Create the main block entry
            CompendiumEntry entry = new CompendiumEntry(null, id);
            entry.setCategory(CompendiumCategory.BLOCK);
            if (name != null) entry.setName(name);

            // Add description pages
            addTextWithAutoPagination(entry, desc);

            // Add the block to display
            ItemStack blockStack = resolveBlockFromId(id);
            if (!blockStack.isEmpty()) {
                entry.addObject(blockStack);
            }

            // Add related entries
            if (relatedEntriesStr != null && !relatedEntriesStr.trim().isEmpty()) {
                String[] related = relatedEntriesStr.split(",");
                for (int i = 0; i < related.length; i++) {
                    related[i] = related[i].trim();
                }
                entry.setRelatedEntries(related);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse block entry");
        }
    }

    private static void parseBlockSubitem(Node node, String parentId, String parentDesc) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;
            String relatedEntriesStr = null;

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = normalizeText(child.getTextContent());
                } else if (childName.equals("relatedEntries")) {
                    relatedEntriesStr = normalizeText(child.getTextContent());
                }
            }

            if (name == null || id == null) {
                return;
            }

            // Determine the sub-category based on parent ID
            CompendiumCategory category = getBlockSubcategoryForParent(parentId);

            // Create the subitem entry
            CompendiumEntry entry = new CompendiumEntry(null, id);
            entry.setCategory(category);
            if (name != null) entry.setName(name);

            // Add description pages - use parent desc as fallback if subitem has no description
            String descToUse = (desc != null && !desc.trim().isEmpty()) ? desc : parentDesc;
            addTextWithAutoPagination(entry, descToUse);

            // Add the block to display
            ItemStack blockStack = resolveBlockFromId(id);
            if (!blockStack.isEmpty()) {
                entry.addObject(blockStack);
            }

            // Add related entries
            if (relatedEntriesStr != null && !relatedEntriesStr.trim().isEmpty()) {
                String[] related = relatedEntriesStr.split(",");
                for (int i = 0; i < related.length; i++) {
                    related[i] = related[i].trim();
                }
                entry.setRelatedEntries(related);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse block subitem entry");
            ;
        }
    }

    private static void parseShape(Node node) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = normalizeText(child.getTextContent());
                }
            }

            if (id == null) {
                return;
            }

            // Get the spell shape from the registry
            SpellPart shape = SpellRegistryHelper.getShapeFromName("arsmagica2:" + id);

            // Create the entry
            CompendiumEntry entry = new CompendiumEntry(shape, id);
            entry.setCategory(CompendiumCategory.SPELL_SHAPE);
            if (name != null) entry.setName(name);

            // Add description pages
            addTextWithAutoPagination(entry, desc);

            // Add the shape object itself
            if (shape != null) {
                entry.addObject(shape);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse shape entry");
        }
    }

    private static void parseComponent(Node node) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = normalizeText(child.getTextContent());
                }
            }

            if (id == null) {
                return;
            }

            // Get the spell component from the registry
            SpellPart component = SpellRegistryHelper.getComponentFromName("arsmagica2:" + id);

            // Create the entry
            CompendiumEntry entry = new CompendiumEntry(component, id);
            entry.setCategory(CompendiumCategory.SPELL_COMPONENT);
            if (name != null) entry.setName(name);

            // Add description pages
            addTextWithAutoPagination(entry, desc);

            // Add the component object itself
            if (component != null) {
                entry.addObject(component);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse component entry");
        }
    }

    private static void parseModifier(Node node) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = child.getTextContent();
                }
            }

            if (id == null) {
                return;
            }

            // Get the spell modifier from the registry
            SpellPart modifier = SpellRegistryHelper.getModifierFromName("arsmagica2:" + id);

            // Create the entry
            CompendiumEntry entry = new CompendiumEntry(modifier, id);
            entry.setCategory(CompendiumCategory.SPELL_MODIFIER);
            if (name != null) entry.setName(name);

            // Add description pages
            addTextWithAutoPagination(entry, desc);

            // Add the modifier object itself
            if (modifier != null) {
                entry.addObject(modifier);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse modifier entry");
        }
    }

    private static void parseAffinity(Node node) {
        // TODO: Implement affinity parsing
        // This would parse affinity entries from XML
    }

    private static void parseTalent(Node node) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = child.getTextContent();
                }
            }

            if (id == null) {
                return;
            }

            // Convert CamelCase to snake_case for skill lookup
            String skillName = id.replaceAll("([A-Z])", "_$1").toLowerCase();
            if (skillName.startsWith("_")) {
                skillName = skillName.substring(1);
            }
            Skill skill = null;
            try {
                java.lang.reflect.Field field = AMSkills.class.getField(skillName);
                skill = (Skill) field.get(null);
            } catch (Exception e) {
                LogHelper.log(Level.ERROR, e, "Could not find skill field: " + skillName);
            }
            // Create the entry
            String entryId = skillName;
            CompendiumEntry entry = new CompendiumEntry(skill, entryId);
            entry.setCategory(CompendiumCategory.TALENT);
            if (name != null) entry.setName(name);
            // Add description pages
            addTextWithAutoPagination(entry, desc);

            // Add the skill object itself
            if (skill != null) {
                entry.addObject(skill);
            }
            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse talent entry");
        }
    }

    private static void parseGuide(Node node) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;
            int order = -1;
            if (node.getAttributes().getNamedItem("order") != null) {
                try {
                    order = Integer.parseInt(node.getAttributes().getNamedItem("order").getNodeValue());
                } catch (NumberFormatException e) {
                    // ignore
                }
            }

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = child.getTextContent();
                }
            }

            if (id == null) {
                return;
            }

            // Use the XML ID directly (don't convert case)
            String entryId = id;

            // Create the entry
            CompendiumEntry entry = new CompendiumEntry(null, entryId);
            entry.setCategory(CompendiumCategory.GUIDE);
            if (name != null) entry.setName(name);
            if (order != -1) entry.setOrder(order);

            // Add description pages - use !p for page breaks
            addTextWithAutoPagination(entry, desc);

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse guide entry");
        }
    }

    private static void parseMechanic(Node node) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;
            String relatedEntriesStr = null;

            NodeList children = node.getChildNodes();
            boolean hasSubitems = false;

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = child.getTextContent();
                } else if (childName.equals("relatedEntries")) {
                    relatedEntriesStr = normalizeText(child.getTextContent());
                } else if (childName.equals("subitem")) {
                    hasSubitems = true;
                    parseMechanicSubitem(child, id, desc);
                }
            }

            if (id == null) {
                return;
            }

            // If this mechanic has subitems, it's a category grouping - don't create a parent entry
            // The subitems are registered under the appropriate sub-category
            if (hasSubitems) {
                return;
            }

            // Create the entry
            CompendiumEntry entry = new CompendiumEntry(null, id);
            entry.setCategory(CompendiumCategory.MECHANIC);
            if (name != null) entry.setName(name);

            // Add description pages
            addTextWithAutoPagination(entry, desc);

            // Add related entries
            if (relatedEntriesStr != null && !relatedEntriesStr.trim().isEmpty()) {
                String[] related = relatedEntriesStr.split(",");
                for (int i = 0; i < related.length; i++) {
                    related[i] = related[i].trim();
                }
                entry.setRelatedEntries(related);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse mechanic entry");
        }
    }

    private static void parseMechanicSubitem(Node node, String parentId, String parentDesc) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;
            String relatedEntriesStr = null;

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = child.getTextContent();
                } else if (childName.equals("relatedEntries")) {
                    relatedEntriesStr = normalizeText(child.getTextContent());
                }
            }

            if (name == null || id == null) {
                return;
            }

            // Determine the sub-category based on parent ID
            CompendiumCategory category = getMechanicSubcategoryForParent(parentId);

            // Create the subitem entry
            CompendiumEntry entry = new CompendiumEntry(null, id);
            entry.setCategory(category);
            if (name != null) entry.setName(name);

            // Add description pages - use parent desc as fallback if subitem has no description
            String descToUse = (desc != null && !desc.trim().isEmpty()) ? desc : parentDesc;
            addTextWithAutoPagination(entry, descToUse);

            // Add related entries
            if (relatedEntriesStr != null && !relatedEntriesStr.trim().isEmpty()) {
                String[] related = relatedEntriesStr.split(",");
                for (int i = 0; i < related.length; i++) {
                    related[i] = related[i].trim();
                }
                entry.setRelatedEntries(related);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse mechanic subitem entry");
        }
    }

    private static void parseMob(Node node) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;
            String relatedEntriesStr = null;

            NodeList children = node.getChildNodes();
            boolean hasSubitems = false;

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = child.getTextContent();
                } else if (childName.equals("relatedEntries")) {
                    relatedEntriesStr = normalizeText(child.getTextContent());
                } else if (childName.equals("subitem")) {
                    hasSubitems = true;
                    parseMobSubitem(child, id, desc);
                }
            }

            if (id == null) {
                return;
            }

            // If this mob has subitems, it's a category grouping (like Flicker) - don't create a parent entry
            if (hasSubitems) {
                return;
            }

            // Create the entry
            CompendiumEntry entry = new CompendiumEntry(null, id);
            entry.setCategory(CompendiumCategory.MOB);
            if (name != null) entry.setName(name);

            // Add description pages
            addTextWithAutoPagination(entry, desc);

            // Add entity render page on the last page
            Class<? extends Entity> entityClass = resolveEntityClass(id);
            if (entityClass != null) {
                entry.addObject(entityClass);
            }

            // Add related entries
            if (relatedEntriesStr != null && !relatedEntriesStr.trim().isEmpty()) {
                String[] related = relatedEntriesStr.split(",");
                for (int i = 0; i < related.length; i++) {
                    related[i] = related[i].trim();
                }
                entry.setRelatedEntries(related);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse mob entry");
        }
    }

    private static void parseMobSubitem(Node node, String parentId, String parentDesc) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;
            String relatedEntriesStr = null;

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = child.getTextContent();
                } else if (childName.equals("relatedEntries")) {
                    relatedEntriesStr = normalizeText(child.getTextContent());
                }
            }

            if (name == null || id == null) {
                return;
            }

            // Determine the sub-category based on parent ID
            CompendiumCategory category = getMobSubcategoryForParent(parentId);

            // Create the subitem entry
            CompendiumEntry entry = new CompendiumEntry(null, id);
            entry.setCategory(category);
            if (name != null) entry.setName(name);

            // Add description pages - use parent desc as fallback if subitem has no description
            String descToUse = (desc != null && !desc.trim().isEmpty()) ? desc : parentDesc;
            addTextWithAutoPagination(entry, descToUse);

            // Add entity render page on the last page
            Class<? extends Entity> entityClass = resolveEntityClass(id);
            if (entityClass != null) {
                entry.addObject(entityClass);
            }

            // Add related entries
            if (relatedEntriesStr != null && !relatedEntriesStr.trim().isEmpty()) {
                String[] related = relatedEntriesStr.split(",");
                for (int i = 0; i < related.length; i++) {
                    related[i] = related[i].trim();
                }
                entry.setRelatedEntries(related);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse mob subitem entry");
        }
    }

    private static void parseBoss(Node node) {
        try {
            String id = node.getAttributes().getNamedItem("id").getNodeValue();
            String name = null;
            String desc = null;
            String relatedEntriesStr = null;

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String childName = child.getNodeName();

                if (childName.equals("name")) {
                    name = normalizeText(child.getTextContent());
                } else if (childName.equals("desc")) {
                    desc = child.getTextContent();
                } else if (childName.equals("relatedEntries")) {
                    relatedEntriesStr = normalizeText(child.getTextContent());
                }
            }

            if (id == null) {
                return;
            }

            // Create the entry
            CompendiumEntry entry = new CompendiumEntry(null, id);
            entry.setCategory(CompendiumCategory.BOSS);
            if (name != null) entry.setName(name);

            // Add description pages
            addTextWithAutoPagination(entry, desc);

            // Add entity render page on the last page
            Class<? extends Entity> entityClass = resolveEntityClass(id);
            if (entityClass != null) {
                entry.addObject(entityClass);
            }

            // Add related entries
            if (relatedEntriesStr != null && !relatedEntriesStr.trim().isEmpty()) {
                String[] related = relatedEntriesStr.split(",");
                for (int i = 0; i < related.length; i++) {
                    related[i] = related[i].trim();
                }
                entry.setRelatedEntries(related);
            }

            CompendiumRegistry.registerEntry(entry);
        } catch (Exception e) {
            LogHelper.log(Level.ERROR, e, "Failed to parse boss entry");
        }
    }

    /**
     * Get the appropriate mob sub-category based on the parent mob's ID.
     * Maps XML parent IDs to CompendiumCategory constants.
     */
    private static CompendiumCategory getMobSubcategoryForParent(String parentId) {
        if (parentId == null) {
            return CompendiumCategory.MOB;
        }

        switch (parentId) {
            case "Flicker":
            case "flicker":
                return CompendiumCategory.MOB_FLICKER;
            default:
                return CompendiumCategory.MOB;
        }
    }

    /**
     * Get the appropriate item sub-category based on the parent item's ID.
     * Maps XML parent IDs to CompendiumCategory constants.
     */
    private static CompendiumCategory getItemSubcategoryForParent(String parentId) {
        if (parentId == null) {
            return CompendiumCategory.ITEM;
        }

        switch (parentId) {
            case "ores":
                return CompendiumCategory.ITEM_ORE;
            case "essence":
                return CompendiumCategory.ITEM_ESSENCE;
            case "runes":
                return CompendiumCategory.ITEM_RUNE;
            case "armor":
                return CompendiumCategory.ITEM_ARMOR;
            case "armor_mage":
                return CompendiumCategory.ITEM_ARMOR_MAGE;
            case "armor_battlemage":
                return CompendiumCategory.ITEM_ARMOR_BATTLEMAGE;
            case "affinity_tomes":
                return CompendiumCategory.ITEM_AFFINITYTOME;
            case "foci":
                return CompendiumCategory.ITEM_FOCI;
            case "mana_potion":
                return CompendiumCategory.ITEM_MANAPOTION;
            case "binding_catalysts":
                return CompendiumCategory.ITEM_BINDINGCATALYST;
            case "flicker_focus":
                return CompendiumCategory.ITEM_FLICKERFOCUS;
            case "inscription_upgrades":
                return CompendiumCategory.ITEM_INSCRIPTIONUPGRADES;
            default:
                return CompendiumCategory.ITEM;
        }
    }

    /**
     * Get the appropriate block sub-category based on the parent block's ID.
     * Maps XML parent IDs to CompendiumCategory constants.
     */
    private static CompendiumCategory getBlockSubcategoryForParent(String parentId) {
        if (parentId == null) {
            return CompendiumCategory.BLOCK;
        }

        switch (parentId) {
            case "illusion_blocks":
                return CompendiumCategory.BLOCK_ILLUSIONBLOCKS;
            case "crystal_marker":
                return CompendiumCategory.BLOCK_CRYSTALMARKER;
            case "inlays":
                return CompendiumCategory.BLOCK_INLAYS;
            default:
                return CompendiumCategory.BLOCK;
        }
    }

    /**
     * Get the appropriate mechanic sub-category based on the parent mechanic's ID.
     * Maps XML parent IDs to CompendiumCategory constants.
     */
    private static CompendiumCategory getMechanicSubcategoryForParent(String parentId) {
        if (parentId == null) {
            return CompendiumCategory.MECHANIC;
        }

        switch (parentId) {
            case "affinity":
                return CompendiumCategory.MECHANIC_AFFINITY;
            case "enchantments":
                return CompendiumCategory.MECHANIC_ENCHANTS;
            case "imbuements":
                return CompendiumCategory.MECHANIC_INFUSIONS;
            default:
                return CompendiumCategory.MECHANIC;
        }
    }

    private static IMultiblock getMultiblockForController(String controller) {
        if (controller == null) {
            return null;
        }

        switch (controller) {
            case "obelisk":
            case "TileEntityObelisk":
                return new TileEntityObelisk().getMultiblockStructure();
            case "celestialprism":
            case "TileEntityCelestialPrism":
                return new TileEntityCelestialPrism().getMultiblockStructure();
            case "blackaurem":
            case "TileEntityBlackAurem":
                return new TileEntityBlackAurem().getMultiblockStructure();
            case "TileEntityKeystoneRecepticle":
            case "TileEntityKeystoneReceptacle":
                return new TileEntityKeystoneReceptacle().getMultiblockStructure();
            case "TileEntityCraftingAltar":
                return new TileEntityCraftingAltar().getMultiblockStructure();
            case "TileEntityManaDrain":
                return new TileEntityManaDrain().getMultiblockStructure();
            default:
                return null;
        }
    }

    private static Object getRitualShape(String ritualName) {
        if (ritualName == null) {
            return null;
        }

        // Map ritual names to their corresponding shapes
        switch (ritualName.toLowerCase()) {
            case "corruption":
                return RitualShapeHelper.instance.corruption;
            case "purification":
                return RitualShapeHelper.instance.purification;
            case "hourglass":
                return RitualShapeHelper.instance.hourglass;
            case "ringedcross":
            case "ringed_cross":
                return RitualShapeHelper.instance.ringedCross;
            default:
                return null;
        }
    }

    /**
     * Adds text to a compendium entry. Text will be paginated on the client side
     * when getPages() is called.
     */
    private static void addTextWithAutoPagination(CompendiumEntry entry, String text) {
        GenerateCompendiumLoreEvent event = new GenerateCompendiumLoreEvent(text, entry.getID());
        MinecraftForge.EVENT_BUS.post(event);
        text = event.lore;

        if (text == null || text.trim().isEmpty()) {
            return;
        }

        String[] pages = text.split("!p");
        for (String page : pages) {
            page = page.trim();
            if (page.isEmpty()) {
                return;
            }
            String converted = convertDescriptionText(page);
            entry.addObject(converted);
        }
    }

    /**
     * Attempts to resolve an entity class from a mob/boss entry ID.
     * Strips common prefixes (mob_, boss_) and looks up in ForgeRegistries.ENTITIES.
     */
    private static Class<? extends Entity> resolveEntityClass(String entryId) {
        // Strip common XML ID prefixes to get the entity registry name
        String entityId = entryId.replaceFirst("^mob_", "").replaceFirst("^boss_", "");

        EntityEntry entityEntry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation("arsmagica2", entityId));
        if (entityEntry != null) {
            return entityEntry.getEntityClass();
        }
        // Try without prefix stripping (e.g., "hell_cow", "earth_elemental")
        entityEntry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation("arsmagica2", entryId));
        if (entityEntry != null) {
            return entityEntry.getEntityClass();
        }
        // Any ID containing "flicker" maps to the shared EntityFlicker class
        if (entityId.contains("flicker")) {
            entityEntry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation("arsmagica2", "flicker"));
            if (entityEntry != null) {
                return entityEntry.getEntityClass();
            }
        }
        return null;
    }

    /**
     * Normalize whitespace in text from XML - replace tabs and newlines with spaces,
     * collapse multiple spaces, and trim.
     */
    private static String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        text = text.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
        text = text.replaceAll("\\s+", " ");
        return text.trim();
    }

    /**
     * Convert description text from XML format to in-game format.
     * Handles special tokens like @x for localization.
     */
    private static String convertDescriptionText(String text) {
        if (text == null) {
            return "";
        }

        // Normalize whitespace
        text = normalizeText(text);

        // Replace localization tokens
        // Format: @x where x is the localization key
        StringBuilder result = new StringBuilder();
        int lastIndex = 0;
        int atIndex;

        while ((atIndex = text.indexOf('@', lastIndex)) != -1) {
            // Add text before the @
            result.append(text.substring(lastIndex, atIndex));

            // Find the end of the localization key (space or end of string)
            int endIndex = atIndex + 1;
            while (endIndex < text.length() && !Character.isWhitespace(text.charAt(endIndex))) {
                endIndex++;
            }

            // Extract and localize the key
            String key = text.substring(atIndex + 1, endIndex);
            if (!key.isEmpty()) {
                String localized = net.minecraft.client.resources.I18n.format(key);
                result.append(localized);
            }

            lastIndex = endIndex;
        }

        // Add remaining text
        result.append(text.substring(lastIndex));

        return result.toString().trim();
    }

    /**
     * Resolves an ItemStack from an XML ID.
     * Format: "item_name" or "item_name@metadata"
     * Example: "affinity_tome@0" -> ItemStack(affinity_tome, 1, 0)
     */
    private static ItemStack resolveItemFromId(String id) {
        if (id == null || id.isEmpty()) {
            return ItemStack.EMPTY;
        }

        String itemName = id;
        int metadata = 0;

        // Check for metadata suffix (e.g., "item@3")
        int atIndex = id.indexOf('@');
        if (atIndex != -1) {
            itemName = id.substring(0, atIndex);
            try {
                metadata = Integer.parseInt(id.substring(atIndex + 1));
            } catch (NumberFormatException e) {
                LogHelper.log(Level.WARN, "Invalid metadata in item ID: " + id);
            }
        }

        // Try to find the item in the Forge registry
        ResourceLocation itemLocation = new ResourceLocation("arsmagica2", itemName);
        Item item = ForgeRegistries.ITEMS.getValue(itemLocation);

        if (item == null) {
            // Try vanilla/other mods without namespace
            itemLocation = new ResourceLocation(itemName);
            item = ForgeRegistries.ITEMS.getValue(itemLocation);
        }

        if (item != null) {
            return new ItemStack(item, 1, metadata);
        }

        // Item not found - could be registered later or doesn't exist
        LogHelper.log(Level.DEBUG, "Could not resolve item for compendium entry: " + id);
        return ItemStack.EMPTY;
    }

    /**
     * Resolves an ItemStack from a block XML ID.
     * Format: "block_name" or "block_name@metadata"
     * Example: "calefactor" -> ItemStack(calefactor block)
     */
    private static ItemStack resolveBlockFromId(String id) {
        if (id == null || id.isEmpty()) {
            return ItemStack.EMPTY;
        }

        String blockName = id;
        int metadata = 0;

        // Check for metadata suffix (e.g., "block@3")
        int atIndex = id.indexOf('@');
        if (atIndex != -1) {
            blockName = id.substring(0, atIndex);
            try {
                metadata = Integer.parseInt(id.substring(atIndex + 1));
            } catch (NumberFormatException e) {
                LogHelper.log(Level.WARN, "Invalid metadata in block ID: " + id);
            }
        }

        // Try to find the block in the Forge registry
        ResourceLocation blockLocation = new ResourceLocation("arsmagica2", blockName);
        Block block = ForgeRegistries.BLOCKS.getValue(blockLocation);

        if (block == null || block == Blocks.AIR) {
            // Try vanilla/other mods without namespace
            blockLocation = new ResourceLocation(blockName);
            block = ForgeRegistries.BLOCKS.getValue(blockLocation);
        }

        if (block != null && block != Blocks.AIR) {
            Item blockItem = Item.getItemFromBlock(block);
            if (blockItem != null) {
                return new ItemStack(blockItem, 1, metadata);
            }
        }

        // Block not found - could be registered later or doesn't exist
        LogHelper.log(Level.DEBUG, "Could not resolve block for compendium entry: " + id);
        return ItemStack.EMPTY;
    }
}
