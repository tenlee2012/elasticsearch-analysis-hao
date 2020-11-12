package com.itenlee.search.analysis.lucence;

import java.io.Serializable;

/**
 * @author tenlee
 * @date 2020/5/29
 */
public class Term implements Comparable<Term>, Serializable {
    //词元的起始位移
    private int offset;
    //词元的相对起始位置
    private int end;
    //词元文本
    private String text;
    //词元类型
    private String lexemeType;

    public Term() {
    }

    public Term(int offset, int end, String text, String lexemeType) {
        this.offset = offset;
        this.end = end;
        this.text = text;
        this.lexemeType = lexemeType;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLexemeType() {
        return lexemeType;
    }

    public void setLexemeType(String lexemeType) {
        this.lexemeType = lexemeType;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public int compareTo(Term other) {
        //起始位置优先
        if (this.offset < other.getOffset()) {
            return -1;
        } else if (this.offset == other.getOffset()) {
            //词元长度优先
            if (this.end > other.getEnd()) {
                return -1;
            } else if (this.end == other.getEnd()) {
                return 0;
            } else {//this.length < other.getLength()
                return 1;
            }

        } else {//this.begin > other.getBegin()
            return 1;
        }
    }

    @Override
    public String toString() {
        return "Term{" +
                "offset=" + offset +
                ", end=" + end +
                ", text='" + text + '\'' +
                ", lexemeType='" + lexemeType + '\'' +
                '}';
    }
}
