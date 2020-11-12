package com.itenlee.search.analysis.lucence;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

/**
 * @author tenlee
 * @date 2020/5/28
 */
public final class HaoAnalyzer extends Analyzer {

    private Configuration configuration;

    /**
     * Lucene Analyzer接口实现类
     *
     * @param configuration IK配置
     */
    public HaoAnalyzer(Configuration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new HaoTokenizer(configuration);
        return new TokenStreamComponents(source);
    }
}
