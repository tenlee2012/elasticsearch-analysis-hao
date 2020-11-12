/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/05/2014/5/21 18:05</create-date>
 *
 * <copyright file="Graph.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.itenlee.search.analysis.algorithm;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author hankcs
 */
public class Graph {
    /**
     * 顶点
     */
    public Vertex[] vertexes;

    /**
     * 边，到达下标i
     */
    public List<EdgeFrom>[] edgesTo;

    /**
     * 将一个词网转为词图
     *
     * @param vertexes 顶点数组
     */
    public Graph(Vertex[] vertexes) {
        int size = vertexes.length;
        this.vertexes = vertexes;
        edgesTo = new List[size];
        for (int i = 0; i < size; ++i) {
            edgesTo[i] = new LinkedList<EdgeFrom>();
        }
    }

    /**
     * 连接两个节点
     *
     * @param from   起点
     * @param to     终点
     * @param weight 花费
     */
    public void connect(int from, int to, double weight) {
        edgesTo[to].add(new EdgeFrom(from, weight, null));
    }


    /**
     * 获取到达顶点to的边列表
     *
     * @param to 到达顶点to
     * @return 到达顶点to的边列表
     */
    public List<EdgeFrom> getEdgeListTo(int to) {
        return edgesTo[to];
    }

    public Vertex[] getVertexes() {
        return vertexes;
    }

    public List<EdgeFrom>[] getEdgesTo() {
        return edgesTo;
    }

    @Override
    public String toString() {
        return "Graph{" +
                "vertexes=" + Arrays.toString(vertexes) +
                ", edgesTo=" + Arrays.toString(edgesTo) +
                '}';
    }
}
