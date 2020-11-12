package com.itenlee.search.analysis.algorithm;

/**
 * 记录了起点的边
 *
 * @author hankcs
 */
public class EdgeFrom {
    public int from;
    /**
     * 花费
     */
    public double weight;
    /**
     * 节点名字，调试用
     */
    String name;

    public EdgeFrom(int from, double weight, String name) {
        this.from = from;
        this.weight = weight;
        this.name = name;
    }

    @Override
    public String toString() {
        return "EdgeFrom{" +
                "from=" + from +
                ", weight=" + weight +
                ", name='" + name + '\'' +
                '}';
    }
}
