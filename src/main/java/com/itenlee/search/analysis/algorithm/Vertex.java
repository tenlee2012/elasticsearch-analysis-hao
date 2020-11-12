package com.itenlee.search.analysis.algorithm;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 顶点
 */
public class Vertex {
    /**
     * 节点对应的词或等效词（如#）调试用
     */
    private String word;
    /**
     * 节点对应的归一化字
     */
    public List<TokenNode> nodes;

    /**
     * 归一化后字长度,起点和终点的词长度是1
     * 比如abc的词长度是1，他们算一个字
     */
    public int length;

    /**
     * 在linked数组中的下标
     */
    public int linkedListIndex;

    /**
     * 在一维顶点数组中的下标，可以视作这个顶点的id
     */
    public int index;

    /**
     * 最短路径对应的权重
     */
    public double weight;

    public Vertex(List<TokenNode> nodes, String realWord, Double weight) {
        this.nodes = nodes;
        this.length = nodes != null ? nodes.size() : 1;
        // 调试用，生产不能开
        if (realWord != null) {
            this.word = realWord;
        }
//        this.word = realWord != null ? realWord : nodes.stream().map(TokenNode::getText).collect(Collectors.joining());
        this.weight = weight;
    }

    public Vertex(List<TokenNode> nodes, Double weight) {
        this(nodes, null, weight);
    }

    /**
     * 生成线程安全的起始节点
     *
     * @return
     */
    public static Vertex newB(double defaultFreq) {
        return new Vertex(null, "#", defaultFreq);
    }

    /**
     * 生成线程安全的终止节点
     *
     * @return
     */
    public static Vertex newE(double defaultFreq) {
        return new Vertex(null, "#", defaultFreq);
    }

    public String getWord() {
        if (word == null && this.nodes != null) {
            this.word = this.nodes.stream().map(TokenNode::getText).collect(Collectors.joining());
        }
        return word;
    }

    @Override
    public String toString() {
        return word;
    }
}
