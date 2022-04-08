package com.itenlee.search.analysis.algorithm;

import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;

import java.util.TreeMap;

public class DoubleArrayTriePro extends com.hankcs.hanlp.collection.trie.DoubleArrayTrie<Double> {
    /**
     * 构建词图
     * [ 0: [0, 1, 3, 6], 1: [1, 2], 2: [2, 3, 6], 3: [3], 4: [4, 5, 6], 5: [5], 6: [6] ]
     *
     * @param text
     * @return
     */
    public TreeMap<Integer, Double>[] match(String text) {
        DoubleArrayTrie<Double>.Searcher searcher = getSearcher(text);
        TreeMap<Integer, Double>[] positionOffsetList = new TreeMap[text.length()];
        for (int i = 0; i < text.length(); ++i) {
            TreeMap<Integer, Double> offsetMap = new TreeMap<>();
            offsetMap.put(i, null);
            positionOffsetList[i] = offsetMap;
        }
        while ((searcher.next())) {
            positionOffsetList[searcher.begin].put(searcher.begin + searcher.length - 1, searcher.value);
        }
        return positionOffsetList;
    }
}
