package am2.common.lore;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Static audit of the compendium: every gated entry (one with a render object) must be reachable
 * in Survival, and every unlock rule must point at a real entry. Reads the XML and sources directly,
 * so it needs no Minecraft runtime.
 */
public class CompendiumProgressionAuditTest {

    private static final File SRC = new File("src/main");
    private static final Set<String> RITUAL_SHAPES = new HashSet<>(Arrays.asList("corruption", "purification", "hourglass", "ringedcross", "ringed_cross"));

    private static Element xml() throws Exception {
        File f = new File(SRC, "resources/assets/arsmagica2/docs/arcanecompendium_en_us.xml");
        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
        return d.getDocumentElement();
    }

    private static List<Element> children(Element e, String tag) {
        List<Element> out = new ArrayList<>();
        NodeList nl = e.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n instanceof Element && (tag == null || n.getNodeName().equals(tag))) out.add((Element) n);
        }
        return out;
    }

    private static String text(Element e, String tag) {
        List<Element> c = children(e, tag);
        return c.isEmpty() ? "" : c.get(0).getTextContent().trim();
    }

    private static String source(String rel) throws Exception {
        return new String(Files.readAllBytes(new File(SRC, "java/am2/" + rel).toPath()), StandardCharsets.UTF_8);
    }

    /** skill name -> [point, tree] for skills that can appear in the Occulus. */
    private static Map<String, String[]> skills() throws Exception {
        Map<String, String[]> out = new HashMap<>();
        Matcher m = Pattern.compile("new Skill\\(\"(\\w+)\",[^,]*(?:\\([^)]*\\))?[^,]*,\\s*(SkillPoint\\.\\w+|null),\\s*-?\\d+,\\s*-?\\d+,\\s*(SkillTrees\\.\\w+|null)")
                .matcher(source("common/registry/AMSkills.java"));
        while (m.find()) out.put(m.group(1), new String[]{m.group(2), m.group(3)});
        return out;
    }

    private static String normalize(String id) {
        String s = id.contains(".") ? id.substring(id.lastIndexOf('.') + 1) : id;
        return s.replace("_", "").toLowerCase();
    }

    private static Set<String> allXmlIds(Element root) {
        Set<String> ids = new HashSet<>();
        for (Element sec : children(root, null)) {
            for (Element e : children(sec, null)) {
                if (e.hasAttribute("id")) ids.add(normalize(e.getAttribute("id")));
                for (Element sub : children(e, "subitem")) ids.add(normalize(sub.getAttribute("id")));
            }
        }
        return ids;
    }

    @Test
    void everySpellPartAndTalentEntryIsObtainableThroughASkill() throws Exception {
        Map<String, String[]> skills = skills();
        assertTrue(skills.size() > 100, "skill parse found only " + skills.size());
        Element root = xml();
        List<String> problems = new ArrayList<>();
        for (String sec : new String[]{"shapes", "components", "modifiers", "talents"}) {
            for (Element sectionNode : children(root, sec)) {
                for (Element e : children(sectionNode, null)) {
                    String id = e.getAttribute("id");
                    if (sec.equals("talents")) id = id.replaceAll("([A-Z])", "_$1").toLowerCase().replaceFirst("^_", "");
                    String[] s = skills.get(id);
                    if (s == null) problems.add(sec + "." + id + ": no such skill");
                    else if (s[0].equals("null") || s[1].equals("null")) problems.add(sec + "." + id + ": skill has no point/tree");
                }
            }
        }
        assertTrue(problems.isEmpty(), problems.toString());
    }

    @Test
    void everyStructureAndRitualEntryHasAnUnlockRule() throws Exception {
        Element root = xml();
        String blocks = source("common/registry/AMBlocks.java");
        List<String> problems = new ArrayList<>();
        Set<String> structureIds = new HashSet<>();
        for (Element sectionNode : children(root, "structures")) {
            for (Element e : children(sectionNode, null)) {
                String id = e.getAttribute("id");
                if (e.getNodeName().equals("structure") && !text(e, "controller").isEmpty()) {
                    structureIds.add(id);
                    String block = CompendiumProgression.STRUCTURE_CONTROLLERS.get(id);
                    if (block == null) problems.add("structure." + id + ": gated by its multiblock but nothing unlocks it");
                    else if (!blocks.contains("\"" + block + "\"")) problems.add("structure." + id + ": controller block '" + block + "' is not registered");
                }
                for (Element sub : children(e, "subitem")) {
                    String ritual = text(sub, "ritualName").toLowerCase();
                    if (RITUAL_SHAPES.contains(ritual) && CompendiumProgression.RITUAL_LEVEL <= 0)
                        problems.add("ritual " + sub.getAttribute("id") + ": no unlock level");
                }
            }
        }
        for (String key : CompendiumProgression.STRUCTURE_CONTROLLERS.keySet())
            if (!structureIds.contains(key)) problems.add("rule for unknown structure entry " + key);
        assertTrue(problems.isEmpty(), problems.toString());
    }

    @Test
    void everyLevelRuleTargetsARealEntry() throws Exception {
        Set<String> ids = allXmlIds(xml());
        List<String> problems = new ArrayList<>();
        for (Map.Entry<Integer, String[]> e : CompendiumProgression.LEVEL_UNLOCKS.entrySet())
            for (String id : e.getValue())
                if (!ids.contains(normalize(id))) problems.add("level " + e.getKey() + " -> '" + id + "' matches no entry");
        assertTrue(problems.isEmpty(), problems.toString());
    }
}
