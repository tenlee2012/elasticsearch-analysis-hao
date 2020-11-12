package com.vdurmont.emoji;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tenlee
 * @date 2020/7/22
 */
public class CustomEmojiParser {
    public static List<CustomEmoji> getUnicodeCandidates(String input) {
        List<EmojiParser.UnicodeCandidate> emojis = EmojiParser.getUnicodeCandidates(input);
        List<CustomEmoji> results = new ArrayList<>(emojis.size());
        for (EmojiParser.UnicodeCandidate emoji : emojis) {
            results.add(new CustomEmoji(emoji));
        }
        return results;
    }

    public static class CustomEmoji {
        private final Emoji emoji;
        // 皮肤肤色
        private final Fitzpatrick fitzpatrick;
        private int startOffset;
        private int endOffset;
        private String emojiStr;

        public CustomEmoji(EmojiParser.UnicodeCandidate unicodeCandidate) {
            this.emoji = unicodeCandidate.getEmoji();
            this.fitzpatrick = unicodeCandidate.getFitzpatrick();
            this.startOffset = unicodeCandidate.getEmojiStartIndex();
            if (unicodeCandidate.getEmoji().supportsFitzpatrick() && unicodeCandidate.hasFitzpatrick()) {
                emojiStr = unicodeCandidate.getEmoji().getUnicode(unicodeCandidate.getFitzpatrick());
            } else {
                emojiStr = unicodeCandidate.getEmoji().getUnicode();
            }
            this.endOffset = this.startOffset + emojiStr.length();
        }

        public Emoji getEmoji() {
            return emoji;
        }

        public Fitzpatrick getFitzpatrick() {
            return fitzpatrick;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public void setStartOffset(int startOffset) {
            this.startOffset = startOffset;
        }

        public String getEmojiStr() {
            return emojiStr;
        }

        public void setEmojiStr(String emojiStr) {
            this.emojiStr = emojiStr;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public void setEndOffset(int endOffset) {
            this.endOffset = endOffset;
        }
    }
}
