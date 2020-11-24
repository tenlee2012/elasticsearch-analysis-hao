package com.itenlee.search.analysis.help;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author tenlee
 * @date 2020/6/3
 */
public class TextUtility {

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    //链接符号
    private static final String[] STOP_WORD = ("!\n" + "\"\n" + "#\n" + "$\n" + "%\n" + "&\n"
            + "'\n" + "(\n" + ")\n" + "*\n" + "+\n" + ",\n" + "-\n" + ".\n" + "/\n" + ":\n" + ";\n" + "<\n" + "=\n"
            + ">\n" + "?\n" + "@\n" + "\\\n" + "^\n" + "_\n" + "`\n" + "{\n" + "|\n" + "}\n" + "~\n" + "＂\n" + "＃\n"
            + "＄\n" + "％\n" + "＆\n" + "＇\n" + "（\n" + "）\n" + "＊\n" + "＋\n" + "，\n" + "－\n" + "／\n" + "：\n"
            + "；\n" + "＜\n" + "＝\n" + "＞\n" + "＠\n" + "［\n" + "＼\n" + "］\n" + "＾\n" + "＿\n" + "｀\n" + "｛\n"
            + "｜\n" + "｝\n" + "～\n" + "｟\n" + "｠\n" + "｢\n" + "｣\n" + "､\n" + "　\n" + "、\n" + "〃\n" + "〈\n"
            + "〉\n" + "《\n" + "》\n" + "「\n" + "」\n" + "『\n" + "』\n" + "〔\n" + "〕\n" + "〖\n" + "〗\n" + "〘\n"
            + "〙\n" + "〚\n" + "〛\n" + "〜\n" + "〝\n" + "〞\n" + "〟\n" + "〰\n" + "〾\n" + "〿\n" + "–\n" + "—\n"
            + "‘\n" + "’\n" + "‛\n" + "“\n" + "”\n" + "„\n" + "‟\n" + "…\n" + "‧\n" + "﹏\n" + "﹑\n" + "﹔\n"
            + "·\n" + "！\n" + "？\n" + "｡\n" + "。\n").split("\n");
    // 用map，时间复杂度小一点
    private static final Map<String, Boolean> STOP_WORD_MAP = Stream.of(STOP_WORD).collect(Collectors.toMap(data -> data, data -> true));

    private static final String whitespace_chars = ""       /* dummy empty string for homogeneity */
            + "\\u0009" // CHARACTER TABULATION
            + "\\u000A" // LINE FEED (LF)
            + "\\u000B" // LINE TABULATION
            + "\\u000C" // FORM FEED (FF)
            + "\\u000D" // CARRIAGE RETURN (CR)
            + "\\u0020" // SPACE
            + "\\u0085" // NEXT LINE (NEL)
            + "\\u00A0" // NO-BREAK SPACE
            + "\\u1680" // OGHAM SPACE MARK
            + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
            + "\\u2000" // EN QUAD
            + "\\u2001" // EM QUAD
            + "\\u2002" // EN SPACE
            + "\\u2003" // EM SPACE
            + "\\u2004" // THREE-PER-EM SPACE
            + "\\u2005" // FOUR-PER-EM SPACE
            + "\\u2006" // SIX-PER-EM SPACE
            + "\\u2007" // FIGURE SPACE
            + "\\u2008" // PUNCTUATION SPACE
            + "\\u2009" // THIN SPACE
            + "\\u200A" // HAIR SPACE
            + "\\u2028" // LINE SEPARATOR
            + "\\u2029" // PARAGRAPH SEPARATOR
            + "\\u202F" // NARROW NO-BREAK SPACE
            + "\\u205F" // MEDIUM MATHEMATICAL SPACE
            + "\\u3000" // IDEOGRAPHIC SPACE
            ;
    /* A \s that actually works for Java’s native character set: Unicode */
    public static final String WHITESPACE_CHARCLASS = "[" + whitespace_chars + "]";

    public static final char SBC_SPACE = 12288; // 全角空格 12288

    public static final char DBC_SPACE = 32; //半角空格 32

    public static final char UNICODE_START = 65281;

    public static final char UNICODE_END = 65374;

    public static final char DBC_SBC_STEP = 65248; // 全角半角转换间隔

    public static char sbc2dbc(char src) {
        if (src == 12288) {
            src = 32;
        } else if (src == '“' || src == '”') {
            src = '"';
        } else if (src == '‘' || src == '’') {
            src = '\'';
        } else if (src == '…') {
            src = '⋯';
        } else if (src == '【') {
            src = '[';
        } else if (src == '】') {
            src = ']';
        } else if (src == '』') {
            src = '」';
        } else if (src == '『') {
            src = '「';
        } else if (65281 <= src && src <= 65374) {
            src -= 65248;
        }

        return src;
    }

    /**
     * 全角转半角 ｈｅｌｌｏ ｗｏｒｌｄ -> hello world
     *
     * @param src
     * @return DBC case
     */
    public static String sbc2dbcCase(String src) {
        if (src == null) {
            return null;
        }
        char[] c = src.toCharArray();
        for (int i = 0; i < c.length; i++) {
            c[i] = sbc2dbc(c[i]);
        }
        return new String(c);
    }

    /**
     * 是否是不需要索引的词
     *
     * @param text
     * @return
     */
    public static boolean isStopWord(String text) {
        if (text == null || text.length() == 0 || isBlank(text)) {
            return true;
        }
        if (text.length() != 1) {
            return false;
        }
        return STOP_WORD_MAP.containsKey(text);
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

        }
        return true;
    }

    public static String toLowerCase(String cs) {
        char[] letter = cs.toCharArray();
        for (int i = 0; i < cs.length(); i++) {
            char ch = letter[i];
            letter[i] = Character.toLowerCase(ch);
        }
        return new String(letter);
    }
}
