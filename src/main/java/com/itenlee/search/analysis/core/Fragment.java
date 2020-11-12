package com.itenlee.search.analysis.core;

import com.itenlee.search.analysis.algorithm.TokenNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tenlee
 * @date 2020/7/15
 */
public class Fragment {
    List<TokenNode> nodes;
    List<Fragment> childFragments = new ArrayList<>();

    protected Fragment(List<TokenNode> nodes) {
        this.nodes = nodes;
    }
}