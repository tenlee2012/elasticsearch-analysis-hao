package com.itenlee.search.analysis.algorithm;

/**
 * 顶点
 *
 * @author tenlee
 * @date 2020/7/13
 */
public class TokenNode {
    /**
     * 单词标签
     */
    public static final String ALPHA_TAG = "alpha";
    /**
     * 标点符号的标签
     */
    public static final String PUNC_TAG = "punc";
    /**
     * url标签
     */
    public static final String URL_TAG = "url";
    /**
     * 数字标签
     */
    public static final String NUM_TAG = "num";

    /**
     * 原始文本
     */
    private String text;
    /**
     * 清理过的文本
     */
    private String cleanedText;
    /**
     * 算法模型需要的text格式
     */
    private String nnText;
    private Integer tid;
    private Double frequency;
    private Integer startOffset;
    private String tag;

    public TokenNode(String text, Integer tid, String tag) {
        this.text = text;
        this.tid = tid;
        this.tag = tag;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getTid() {
        return tid;
    }

    public void setTid(Integer tid) {
        this.tid = tid;
    }

    public Integer getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(Integer startOffset) {
        this.startOffset = startOffset;
    }

    public String getCleanedText() {
        return cleanedText;
    }

    public void setCleanedText(String cleanedText) {
        this.cleanedText = cleanedText;
    }

    public String getNnText() {
        return nnText;
    }

    public void setNnText(String nnText) {
        this.nnText = nnText;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Double getFrequency() {
        return frequency;
    }

    public void setFrequency(Double frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "TokenNode{" +
                "text='" + text + '\'' +
                ", tid=" + tid +
                ", startOffset=" + startOffset +
                ", cleanedText='" + cleanedText + '\'' +
                ", nnText='" + nnText + '\'' +
                ", tag='" + tag + '\'' +
                ", frequency='" + frequency + '\'' +
                '}';
    }
}
