package am2.api.compendium;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits Arcane Compendium text into layout tokens.
 * <p>
 * Text is normally wrapped at spaces. Japanese (and other CJK) text has no spaces, so within a
 * space-separated word this class also offers a break between CJK characters, honouring the usual
 * line-start/line-end prohibitions, and never inside a {@code #5...#0} link or a {@code #x} colour code.
 * Words without CJK characters are returned unchanged, so English layout is not affected.
 */
public final class CompendiumText {

    /**
     * A piece of text that is laid out as a unit.
     */
    public static final class Token {
        public final String text;
        /**
         * True if a space follows this token in the source text (i.e. it should be drawn with a trailing space).
         */
        public final boolean spaceAfter;

        Token(String text, boolean spaceAfter) {
            this.text = text;
            this.spaceAfter = spaceAfter;
        }
    }

    /**
     * Characters that must not start a line.
     */
    private static final String NO_LINE_START = "、。，．・：；？！゛゜ヽヾーァィゥェォッャュョヮヵヶぁぃぅぇぉっゃゅょゎゕゖ々〃〜～…‥"
            + ")）]］}｝〉》」』】〕〙〗”’%％.,:;!?";
    /**
     * Characters that must not end a line.
     */
    private static final String NO_LINE_END = "（([［{｛〈《「『【〔〘〖“‘";

    private CompendiumText() {
    }

    public static boolean isCjk(char c) {
        return (c >= 0x3040 && c <= 0x30FF)   // Hiragana, Katakana
                || (c >= 0x31F0 && c <= 0x31FF)   // Katakana phonetic extensions
                || (c >= 0x3400 && c <= 0x4DBF)   // CJK extension A
                || (c >= 0x4E00 && c <= 0x9FFF)   // CJK unified ideographs
                || (c >= 0xF900 && c <= 0xFAFF)   // CJK compatibility ideographs
                || (c >= 0x3000 && c <= 0x303F)   // CJK symbols and punctuation
                || (c >= 0xFF00 && c <= 0xFFEF)   // Full/half width forms
                || (c >= 0xAC00 && c <= 0xD7AF);  // Hangul syllables
    }

    private static boolean containsCjk(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (isCjk(s.charAt(i))) return true;
        }
        return false;
    }

    /**
     * Tokenizes text for layout. Spaces separate words; empty words are dropped.
     */
    public static List<Token> tokenize(String text) {
        List<Token> tokens = new ArrayList<>();
        for (String word : text.split(" ")) {
            if (word.isEmpty()) continue;
            if (!containsCjk(word)) {
                tokens.add(new Token(word, true));
                continue;
            }
            List<String> pieces = splitCjkWord(word);
            for (int i = 0; i < pieces.size(); i++) {
                tokens.add(new Token(pieces.get(i), i == pieces.size() - 1));
            }
        }
        return tokens;
    }

    private static boolean isAsciiDigitLike(char c) {
        return (c >= '0' && c <= '9') || c == '%';
    }

    private static boolean breakAllowed(char prev, char next) {
        if (!isCjk(prev) && !isCjk(next)) return false;
        if (NO_LINE_START.indexOf(next) >= 0) return false;
        if (NO_LINE_END.indexOf(prev) >= 0) return false;
        // keep a number attached to the unit after it ("5秒", "25%の")
        if (isAsciiDigitLike(prev) && isCjk(next)) return false;
        return true;
    }

    /**
     * Splits one space-free word containing CJK characters into break-able pieces.
     * Runs of newlines become their own piece. Colour codes ({@code #} plus one character) are
     * zero-width: {@code #0} sticks to the text before it, any other code to the text after it.
     */
    private static List<String> splitCjkWord(String word) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        StringBuilder prefix = new StringBuilder();
        boolean inLink = false;         // between #5 and #0 at the current position
        boolean prevInLink = false;     // the previous visible character was inside a link
        char prev = 0;
        boolean havePrev = false;

        int i = 0;
        while (i < word.length()) {
            char c = word.charAt(i);
            if (c == '#' && i + 1 < word.length()) {
                char code = word.charAt(i + 1);
                String codeStr = "#" + code;
                if (code == '0') {
                    cur.append(codeStr);
                    inLink = false;
                } else {
                    prefix.append(codeStr);
                    if (code == '5') inLink = true;
                }
                i += 2;
                continue;
            }
            if (c == '\n') {
                int j = i;
                while (j < word.length() && word.charAt(j) == '\n') j++;
                if (cur.length() > 0) out.add(cur.toString());
                out.add(word.substring(i, j));
                cur = new StringBuilder();
                havePrev = false;
                i = j;
                continue;
            }
            boolean linkBlocks = prevInLink && inLink;
            if (havePrev && !linkBlocks && breakAllowed(prev, c) && cur.length() > 0) {
                out.add(cur.toString());
                cur = new StringBuilder();
            }
            cur.append(prefix);
            prefix.setLength(0);
            cur.append(c);
            prevInLink = inLink;
            prev = c;
            havePrev = true;
            i++;
        }
        cur.append(prefix);
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }
}
