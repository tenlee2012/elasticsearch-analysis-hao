package org.elasticsearch.index.analysis.hao;

import com.itenlee.search.analysis.help.ESPluginLoggerFactory;
import com.itenlee.search.analysis.lucence.Configuration;
import com.itenlee.search.analysis.lucence.HaoTokenizer;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;

/**
 * @author tenlee
 * @date 2020/5/28
 */
public class HaoTokenizerFactory extends AbstractTokenizerFactory {

    private static final Logger logger = ESPluginLoggerFactory.getLogger(HaoTokenizerFactory.class.getName());

    private Configuration configuration;

    /**
     * 构造函数
     *
     * @param indexSettings 索引配置
     * @param name          分析器或者分词器名称。如果是自定义分析器，则为自定义分析器名称
     * @param env           es环境配置
     * @param settings      自定义分析器配置参数
     */
    public HaoTokenizerFactory(IndexSettings indexSettings, String name, Environment env, Settings settings) {
        super(indexSettings, settings, name);
        configuration = new Configuration(indexSettings, name, env, settings);
    }

    public static TokenizerFactory getHttpSmartTokenizerFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        return new HaoTokenizerFactory(indexSettings, name, environment, settings).setSmart(false);
    }

    public static TokenizerFactory getHttpTokenizerFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        return new HaoTokenizerFactory(indexSettings, name, environment, settings).setSmart(true);
    }

    public HaoTokenizerFactory setSmart(boolean indexMode) {
        this.configuration.setIndexMode(indexMode);
        return this;
    }

    @Override
    public Tokenizer create() {
        return new HaoTokenizer(configuration);
    }
}
