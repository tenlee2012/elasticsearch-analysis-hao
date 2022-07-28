package com.itenlee.search.analysis.core;

import com.itenlee.search.analysis.algorithm.EdgeFrom;
import com.itenlee.search.analysis.algorithm.Graph;
import com.itenlee.search.analysis.algorithm.PathState;
import com.itenlee.search.analysis.algorithm.TokenNode;
import com.itenlee.search.analysis.algorithm.Vertex;
import com.itenlee.search.analysis.algorithm.WordNet;
import com.itenlee.search.analysis.help.TextUtility;
import com.itenlee.search.analysis.lucence.Term;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * @author tenlee
 * @date 2020/9/22
 */
public class DijkstraSeg {
    private Dictionary dictionary;

    public DijkstraSeg(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public List<Term> segSentence(String sentence, boolean indexMode, int autoWordLength, boolean enableSingleWord) {
        List<TokenNode> nodes = Dictionary.buildNodes(sentence);
        List<Term> result;
        if (nodes.isEmpty()) {
            result = new ArrayList<>();
            return result;
        } else if (nodes.size() == 1) {
            TokenNode node = nodes.get(0);
            Term term =
                new Term(node.getStartOffset(), node.getStartOffset() + node.getText().length(), node.getText(), null);
            result = Collections.singletonList(term);
            return result;
        }
        ////////////////生成词网////////////////////
        WordNet wordNetAll = generateWordNet(nodes, autoWordLength);
        ///////////////生成二元词图////////////////////
        Graph graph = wordNetAll.toGraph();
        List<Vertex> vertexList = dijkstra(graph);

        List<Term> terms = decorateResultForIndexMode(vertexList, wordNetAll, indexMode, enableSingleWord);
        if (terms.size() > 0) {
            Collections.sort(terms);
            return terms;
        }
        // 说明全是不分词的无意义字符
        return Collections.singletonList(new Term(0, sentence.length(), null, null));
    }

    private WordNet generateWordNet(List<TokenNode> nodes, int autoWordLength) {
        StringBuilder sb = new StringBuilder();
        // 字符串字符对应的nodes数组的坐标
        List<Integer> char2NodeIndex = new ArrayList<>();
        int charLen = 0;
        for (int i = 0; i < nodes.size(); i++) {
            TokenNode node = nodes.get(i);
            String word = node.getCleanedText();
            int wordLen = word.length();
            if (wordLen == 1 || TokenNode.NUM_TAG.equals(node.getTag()) || TokenNode.ALPHA_TAG.equals(node.getTag())) {
                for (int j = 0; j < wordLen; j++) {
                    char2NodeIndex.add(i);
                }
                sb.append(word);
            } else {
                char2NodeIndex.add(i);
                sb.append("。");
                wordLen = 1;
            }
            charLen += wordLen;
        }
        WordNet wordNetStorage = new WordNet(nodes, dictionary.getLogTotal());

        TreeMap<Integer, Double>[] graph = dictionary.getDoubleArrayTrie().match(sb.toString());
        int chineseCount = 0; // 汉字数量

        for (int i = 0; i < graph.length; ) {
            int nodeIndex = char2NodeIndex.get(i);

            if (nodes.get(nodeIndex).getTag() != null) {
                // 把小于等于长度的短语作为词
                if (chineseCount > 1 && chineseCount <= autoWordLength && graph[i - chineseCount].get(i - 1) == null) {
                    int offset = char2NodeIndex.get(i);
                    wordNetStorage.add(nodeIndex - chineseCount + 1,
                        new Vertex(nodes.subList(offset - chineseCount, offset), dictionary.getLogTotal()));
                }
                chineseCount = 0;
            } else {
                chineseCount++; // 汉字数量++
            }

            if (TokenNode.NUM_TAG.equals(nodes.get(nodeIndex).getTag()) || TokenNode.ALPHA_TAG
                .equals(nodes.get(nodeIndex).getTag())) {
                // 把 数字/字母 加入到图中, 比如 SNH48
                for (int j = nodeIndex; j < nodes.size(); j++) {
                    // 如果后续是 字母或者数字，把连续的的当成一个词语。
                    if (!TokenNode.NUM_TAG.equals(nodes.get(j).getTag()) && !TokenNode.ALPHA_TAG
                        .equals(nodes.get(j).getTag())) {
                        break;
                    }
                    wordNetStorage.add(nodeIndex + 1, new Vertex(nodes.subList(nodeIndex, j + 1), dictionary.getLogTotal()));
                }
                for (Map.Entry<Integer, Double> entry : graph[i].entrySet()) {
                    int offset = char2NodeIndex.get(entry.getKey()); // 比如[12月SNH48和giao哥过来] [哥=13,真实是7, nodeIndex=7] [月=2,真实是1,nodeIndex=1]
                    if (offset <= nodeIndex) {
                        continue;
                    }
                    wordNetStorage.add(nodeIndex + 1,
                            new Vertex(nodes.subList(nodeIndex, offset + 1), entry.getValue() != null ? entry.getValue() : dictionary.getLogTotal()));
                }
                i += nodes.get(nodeIndex).getCleanedText().length(); // i = 3 + 4
            } else {
                for (Map.Entry<Integer, Double> entry : graph[i].entrySet()) {
                    int offset = char2NodeIndex.get(entry.getKey());
                    wordNetStorage.add(nodeIndex + 1,
                            new Vertex(nodes.subList(nodeIndex, offset + 1), entry.getValue() != null ? entry.getValue() : dictionary.getLogTotal()));
                }
                i++;
            }
        }

        // 最后在扫一遍，把小于等于长度的短语作为词
        if (chineseCount > 1 && chineseCount <= autoWordLength) {
            int nodeIndex = char2NodeIndex.get(graph.length - 1) + 1;
            wordNetStorage.add(nodeIndex - chineseCount + 1,
                new Vertex(nodes.subList(nodeIndex - chineseCount, nodeIndex), dictionary.getLogTotal()));
        }
        return wordNetStorage;
    }

    /**
     * dijkstra最短路径
     *
     * @param graph
     * @return
     */
    private static List<Vertex> dijkstra(Graph graph) {
        List<Vertex> resultList = new LinkedList<Vertex>();
        Vertex[] vertexes = graph.getVertexes();
        List<EdgeFrom>[] edgesTo = graph.getEdgesTo();
        double[] d = new double[vertexes.length];
        Arrays.fill(d, Double.MAX_VALUE);
        d[d.length - 1] = 0;
        int[] path = new int[vertexes.length];
        Arrays.fill(path, -1);
        PriorityQueue<PathState> que = new PriorityQueue<PathState>();
        que.add(new PathState(0, vertexes.length - 1));
        while (!que.isEmpty()) {
            PathState p = que.poll();
            if (d[p.vertex] < p.cost)
                continue;
            for (EdgeFrom edgeFrom : edgesTo[p.vertex]) {
                if (d[edgeFrom.from] > d[p.vertex] + edgeFrom.weight) {
                    d[edgeFrom.from] = d[p.vertex] + edgeFrom.weight;
                    que.add(new PathState(d[edgeFrom.from], edgeFrom.from));
                    path[edgeFrom.from] = p.vertex;
                }
            }
        }
        for (int t = 0; t != -1; t = path[t]) {
            resultList.add(vertexes[t]);
        }
        return resultList;
    }

    protected List<Term> decorateResultForIndexMode(final List<Vertex> vertexList, WordNet wordNetAll, boolean isIndexMode, boolean enableSingleWord) {
        List<Term> termList = new LinkedList<Term>();
        int line = 1;
        ListIterator<Vertex> listIterator = vertexList.listIterator();
        listIterator.next();
        int length = vertexList.size() - 2;
        for (int i = 0; i < length; ++i) {
            Vertex vertex = listIterator.next();
            Term termMain = convert(vertex);
            if (Objects.isNull(termMain)) {
                line += vertex.length;
                continue;
            }
            termList.add(termMain);

            if (vertex.length > 1 && isIndexMode) {
                // 过长词所在的行
                int currentLine = vertex.linkedListIndex;
                // ==== 优化hanlp的逻辑开始。子词也要走最短路分词
                if (!dictionary.getMetaWords().contains(vertex.getWord())) {
                    Graph subGraph = wordNetAll.toGraph(currentLine, vertex.length, vertex, dictionary.getLogTotal());
                    // 剩下的都是单字了，不再递归了
                    if (enableSingleWord || subGraph.vertexes.length != vertex.length + 2) {
                        List<Vertex> subVertex = dijkstra(subGraph);
                        List<Term> subTerms = decorateResultForIndexMode(subVertex, wordNetAll, isIndexMode, enableSingleWord);
                        for (Term subTerm : subTerms) {
                            // 一个词拆后的单字不要了。比如 体力|值，[值]不要了
                            if (enableSingleWord || subTerm.getEnd() - subTerm.getOffset() != 1) {
                                termList.add(subTerm);
                            }
                        }
                    }
                }
            }
            line += vertex.length;
        }

        return termList;
    }

    private Term convert(Vertex vertex) {
        String token = vertex.getWord();
        if (TextUtility.isStopWord(token)) {
            return null;
        }
        TokenNode last = vertex.nodes.get(vertex.nodes.size() - 1);
        Term term = new Term(vertex.nodes.get(0).getStartOffset(), last.getStartOffset() + last.getText().length(),
                token, null);
        return term;
    }
}
