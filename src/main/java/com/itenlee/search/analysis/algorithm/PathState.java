package com.itenlee.search.analysis.algorithm;

/**
 * @author tenlee
 */
public class PathState implements Comparable<PathState> {
    /**
     * 路径花费
     */
    public double cost;
    /**
     * 当前位置
     */
    public int vertex;

    @Override
    public int compareTo(PathState o) {
        return Double.compare(cost, o.cost);
    }

    public PathState(double cost, int vertex) {
        this.cost = cost;
        this.vertex = vertex;
    }
}