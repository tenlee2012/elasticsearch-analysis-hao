package com.itenlee.search.analysis.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author hankcs
 */
public class WordNet {

    /**
     * 归一化后每一个字
     */
    public List<TokenNode> nodes;
    /**
     * 节点，每一行都是前缀词，跟图的表示方式不同
     */
    public LinkedList<Vertex> vertexes[];

    /**
     * 共有多少个节点
     */
    int size;

    /**
     * 为一个句子生成空白词网
     *
     * @param defaultFreq 默认词频
     */
    public WordNet(List<TokenNode> nodes, double defaultFreq) {
        this.nodes = nodes;
        vertexes = new LinkedList[nodes.size() + 2];
        for (int i = 0; i < vertexes.length; ++i) {
            vertexes[i] = new LinkedList<Vertex>();
        }
        vertexes[0].add(Vertex.newB(defaultFreq));
        vertexes[vertexes.length - 1].add(Vertex.newE(defaultFreq));
        size = 2;
    }

    /**
     * 添加顶点
     *
     * @param line   行号
     * @param vertex 顶点
     */
    public void add(int line, Vertex vertex) {
        for (Vertex oldVertex : vertexes[line]) {
            // 保证唯一性
            if (oldVertex.length == vertex.length) return;
        }
        vertex.linkedListIndex = line;
        vertexes[line].add(vertex);
        ++size;
    }

    /**
     * 子词网转词图
     * 比如 奋发图强，还是要计算子分词的最短路。到底是 奋发|图强 奋|发图|强
     *
     * @param linkedStart vertexes的开始下标
     * @param length      子词的长度
     * @param nowVertex   当前的词组，避免重复
     * @param defaultFreq 默认词频
     * @return 词图
     */
    public Graph toGraph(int linkedStart, int length, Vertex nowVertex, double defaultFreq) {
        List<Vertex> vertexList = new ArrayList<>();
        List<Vertex> tmpVertexes[] = new List[length + 2];
        int size = 0;
        int index = 0;

        Vertex begin = Vertex.newB(defaultFreq);
        begin.index = index++;

        tmpVertexes[size++] = Collections.singletonList(begin);
        vertexList.add(begin);

        for (int row = 0; row < length; row++) {
            ArrayList<Vertex> vertices = new ArrayList<>();
            tmpVertexes[size++] = vertices;
            for (Vertex from : this.vertexes[linkedStart + row]) {
                if (from == nowVertex) {
                    continue;
                }
                int toIndex = row + from.length;
                if (toIndex > length) {
                    break;
                }
                vertices.add(from);
                from.index = index++;
                vertexList.add(from);
            }
        }
        Vertex end = Vertex.newE(defaultFreq);
        end.index = index;

        tmpVertexes[size++] = Collections.singletonList(end);
        vertexList.add(end);

        Vertex[] subVertices = new Vertex[vertexList.size()];
        vertexList.toArray(subVertices);
        Graph graph = new Graph(subVertices);

        for (int row = 0; row < size - 1; ++row) {
            List<Vertex> vertexListFrom = tmpVertexes[row];
            for (Vertex from : vertexListFrom) {
//                assert from.word.length() > 0 : "空节点会导致死循环！";
                int toIndex = row + from.length;
                for (Vertex to : tmpVertexes[toIndex]) {
                    graph.connect(from.index, to.index, Math.abs(from.weight + to.weight));
                }
            }
        }
        return graph;
    }

    /**
     * 词网转词图
     *
     * @return 词图
     */
    public Graph toGraph() {
        Graph graph = new Graph(getVertexesLineFirst());

        for (int row = 0; row < vertexes.length - 1; ++row) {
            List<Vertex> vertexListFrom = vertexes[row];
            for (Vertex from : vertexListFrom) {
//                assert from.word.length() > 0 : "空节点会导致死循环！";
                int toIndex = row + from.length;
                for (Vertex to : vertexes[toIndex]) {
                    // dijkstra不能处理负边权
                    graph.connect(from.index, to.index, Math.abs(from.weight + to.weight));
                }
            }
        }
        return graph;
    }

    /**
     * 获取顶点数组
     *
     * @return Vertex[] 按行优先列次之的顺序构造的顶点数组
     */
    private Vertex[] getVertexesLineFirst() {
        Vertex[] vertexes = new Vertex[size];
        int i = 0;
        for (List<Vertex> vertexList : this.vertexes) {
            for (Vertex v : vertexList) {
                v.index = i;    // 设置id
                vertexes[i++] = v;
            }
        }

        return vertexes;
    }

    /**
     * 获取内部顶点表格，谨慎操作！
     *
     * @return
     */
    public LinkedList<Vertex>[] getVertexes() {
        return vertexes;
    }

    /**
     * 获取某一行的逆序迭代器
     *
     * @param line 行号
     * @return 逆序迭代器
     */
    public Iterator<Vertex> descendingIterator(int line) {
        return vertexes[line].descendingIterator();
    }
}
