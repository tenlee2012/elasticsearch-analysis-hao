package com.itenlee.search.analysis.core;

import com.itenlee.search.analysis.algorithm.TokenNode;
import com.itenlee.search.analysis.help.DingRotService;
import com.itenlee.search.analysis.help.ESPluginLoggerFactory;
import com.itenlee.search.analysis.help.TextUtility;
import com.itenlee.search.analysis.lucence.Configuration;
import com.itenlee.search.analysis.lucence.Term;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tenlee
 * @date 2020/7/20
 */
public class TokenizerService {

    private static final Logger logger = ESPluginLoggerFactory.getLogger(TokenizerService.class.getName());

    private Dictionary dictionary;
    private DijkstraSeg dijkstraSeg;

    private Configuration configuration;

    public TokenizerService(Configuration configuration) {
        this.configuration = configuration;
        try {
            dictionary = Dictionary.getInstance();
            dijkstraSeg = new DijkstraSeg(dictionary);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Term> segment(String text) throws IOException {
        List<Term> terms = new ArrayList<>();
        if (text == null || text.length() == 0) {
            return terms;
        }
        try {
            terms = dijkstraSeg.segSentence(text, configuration.isIndexMode(), configuration.getAutoWordLength(),
                configuration.isEnableSingleWord());
        } catch (Exception e) {
            logger.error("text:{}, get remote tokenizer error.", text, e);
            if (configuration.isEnableFailDingMsg() && configuration.getDingWebHookUrl() != null && configuration.getDingMsgContent() != null) {
                DingRotService.sendDingTalkMessage(configuration.getDingMsgContent().replace("{text}", text)
                        .replace("{reason}", e.toString()), configuration.getDingWebHookUrl());
            }

            if (configuration.isEnableFallBack()) {
                this.fallBackTokenizer(terms, text);
            } else {
                throw new IOException(e);
            }
        }
        if (terms.size() > 0) {
            Collections.sort(terms);
        }
        return terms;
    }

    private void fallBackTokenizer(List<Term> terms, String text) {
        for (int i = 0; i < text.length(); i++) {
            terms.add(new Term(i, i + 1, String.valueOf(text.charAt(i)), null));
        }
    }

    public static void toTerm(ArrayList<Term> terms, List<Fragment> fragments, boolean indexMode) {
        for (Fragment fragment : fragments) {
            String token =
                    fragment.nodes.stream().map(TokenNode::getText).collect(Collectors.joining());
            if (TextUtility.isStopWord(token)) {
                continue;
            }

            TokenNode last = fragment.nodes.get(fragment.nodes.size() - 1);
            terms.add(new Term(fragment.nodes.get(0).getStartOffset(), last.getStartOffset() + last.getText().length(),
                    token, null));
            if (indexMode) {
                toTerm(terms, fragment.childFragments, true);
            }
        }
    }
}
