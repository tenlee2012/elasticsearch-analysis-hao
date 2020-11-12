package org.elasticsearch.index.analysis.hao;

import com.itenlee.search.analysis.lucence.Configuration;
import com.itenlee.search.analysis.lucence.HaoAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.analysis.AnalyzerProvider;

/**
 * @author tenlee
 * @date 2020/5/28
 */
public class HaoAnalyzerProvider extends AbstractIndexAnalyzerProvider<HaoAnalyzer> {
    private final HaoAnalyzer analyzer;

    /**
     * Constructs a new analyzer component, with the index name and its settings and the analyzer name.
     *
     * @param indexSettings the settings and the name of the index
     * @param name          The analyzer name
     * @param settings
     */
    public HaoAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings, boolean indexMode) {
        super(indexSettings, name, settings);
        Configuration configuration = new Configuration(indexSettings, name, env, settings).setIndexMode(indexMode);

        analyzer = new HaoAnalyzer(configuration);
    }

    public static AnalyzerProvider<? extends Analyzer> getHttpSmartAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new HaoAnalyzerProvider(indexSettings, env, name, settings, false);
    }

    public static AnalyzerProvider<? extends Analyzer> getHttpAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new HaoAnalyzerProvider(indexSettings, env, name, settings, true);
    }

    @Override
    public HaoAnalyzer get() {
        return analyzer;
    }
}
