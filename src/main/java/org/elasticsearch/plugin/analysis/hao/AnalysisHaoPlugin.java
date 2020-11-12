package org.elasticsearch.plugin.analysis.hao;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.index.analysis.hao.HaoAnalyzerProvider;
import org.elasticsearch.index.analysis.hao.HaoTokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tenlee
 * @date 2020/5/28
 */
public class AnalysisHaoPlugin extends Plugin implements AnalysisPlugin {

    public static String PLUGIN_NAME = "analysis-hao";

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> extra = new HashMap<>();

        extra.put("hao_search_mode", HaoTokenizerFactory::getHttpSmartTokenizerFactory);
        extra.put("hao_index_mode", HaoTokenizerFactory::getHttpTokenizerFactory);

        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> extra = new HashMap<>();

        extra.put("hao_search_mode", HaoAnalyzerProvider::getHttpSmartAnalyzerProvider);
        extra.put("hao_index_mode", HaoAnalyzerProvider::getHttpAnalyzerProvider);

        return extra;
    }
}
