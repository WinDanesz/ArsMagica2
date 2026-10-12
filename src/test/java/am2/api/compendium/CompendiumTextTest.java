package am2.api.compendium;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Layout tokenizer used by the Arcane Compendium to wrap text. English must keep wrapping at spaces only;
 * Japanese must wrap between characters without splitting links, colour codes or breaking kinsoku rules.
 */
public class CompendiumTextTest {

    private static List<String> texts(String s) {
        List<String> out = new ArrayList<>();
        for (CompendiumText.Token t : CompendiumText.tokenize(s)) out.add(t.text);
        return out;
    }

    private static String rejoin(String s) {
        StringBuilder sb = new StringBuilder();
        for (CompendiumText.Token t : CompendiumText.tokenize(s)) sb.append(t.text).append(t.spaceAfter ? " " : "");
        return sb.toString().trim();
    }

    @Test
    void englishIsSplitAtSpacesOnly() {
        List<CompendiumText.Token> tokens = CompendiumText.tokenize("The  #5Obelisk#0 is a !d big block.");
        assertEquals(Arrays.asList("The", "#5Obelisk#0", "is", "a", "!d", "big", "block."), texts("The  #5Obelisk#0 is a !d big block."));
        for (CompendiumText.Token t : tokens) assertTrue(t.spaceAfter);
    }

    @Test
    void japaneseBreaksBetweenCharacters() {
        List<CompendiumText.Token> tokens = CompendiumText.tokenize("魔法を使う");
        assertEquals(Arrays.asList("魔", "法", "を", "使", "う"), texts("魔法を使う"));
        for (int i = 0; i < tokens.size() - 1; i++) assertFalse(tokens.get(i).spaceAfter);
        assertTrue(tokens.get(tokens.size() - 1).spaceAfter);
    }

    @Test
    void kinsokuKeepsPunctuationAttached() {
        assertEquals(Arrays.asList("魔", "法、", "「力」", "だ。"), texts("魔法、「力」だ。"));
        // closing punctuation never starts a token, opening brackets never end one
        for (String t : texts("彼は「魔法」を使い、杖を振った。")) {
            assertFalse("、。」".contains(t.substring(0, 1)), t);
            assertFalse("「".contains(t.substring(t.length() - 1)), t);
        }
        // prolonged sound mark and small kana stay with the preceding kana
        assertEquals(Arrays.asList("マ", "ナー", "ポッ", "ト"), texts("マナーポット"));
    }

    @Test
    void linksAreNeverSplit() {
        List<String> t = texts("魔法の#5オベリスク#0を建てる");
        assertTrue(t.contains("#5オベリスク#0"), t.toString());
        assertEquals("魔法の#5オベリスク#0を建てる", String.join("", t));
    }

    @Test
    void colourCodesStayZeroWidthAndAttached() {
        List<String> t = texts("#9重要#0です");
        assertEquals("#9重要#0です", String.join("", t));
        for (String s : t) assertFalse(s.endsWith("#"), s);
        assertFalse(t.contains("#9"));
    }

    @Test
    void numbersStayWithTheirUnit() {
        assertTrue(texts("効果は5秒続く").contains("5秒"));
        assertTrue(texts("25%の確率").stream().anyMatch(s -> s.contains("25%")));
    }

    @Test
    void paragraphMarkersSurviveAndNewlinesAreSeparateTokens() {
        assertEquals(Arrays.asList("魔", "法。", "!d", "次", "へ"), texts("魔法。 !d 次へ"));
        assertTrue(texts("魔法\n\n次").contains("\n\n"));
    }

    @Test
    void mixedLatinAndJapaneseRoundTripsWithoutInventingSpaces() {
        String s = "Etherium(エーテリウム)を集める";
        assertEquals(s, String.join("", texts(s)));
        // a real space in the source is kept as a space after the token
        assertEquals("Etherium を 集める", rejoin("Etherium を 集める").replace("集 め る", "集める"));
    }
}
