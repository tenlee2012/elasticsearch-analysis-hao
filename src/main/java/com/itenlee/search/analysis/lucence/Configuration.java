package com.itenlee.search.analysis.lucence;

import com.hankcs.hanlp.utility.Predefine;
import com.itenlee.search.analysis.core.Dictionary;
import com.itenlee.search.analysis.help.ESPluginLoggerFactory;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.core.PathUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.plugin.analysis.hao.AnalysisHaoPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author tenlee
 * @date 2020/6/3
 */
public class Configuration {
    private static final Logger logger = ESPluginLoggerFactory.getLogger(Configuration.class.getName());

    private Environment environment;

    private Settings settings;

    /**
     * 是否启用索引（细颗粒度）模式
     */
    private boolean indexMode;
    /**
     * 是否使用算法模型预测分词
     */
    private boolean enableOOV;
    /**
     * 索引名称
     */
    private String indexName;
    /**
     * 是否使用细粒度返回的单字
     */
    private boolean enableSingleWord;
    /**
     * 是否启用失败降级
     */
    private boolean enableFallBack;
    /**
     * 是否启用失败钉钉通知
     */
    private boolean enableFailDingMsg;

    /**
     * 钉钉通知url
     */
    private String dingWebHookUrl;
    /**
     * 钉钉通知文案
     */
    private String dingMsgContent;

    private String baseDictionaryFile;
    private String customerDictionaryFile;
    /**
     * 远程freq.json词库
     */
    private String remoteFreqDict;
    /**
     * 第一次执行的时间 HH:MM:ss
     */
    private String syncDicTime;
    /**
     * 执行周期
     */
    private Integer syncDicPeriodTime;

    private static Path confDir;

    private static Properties props;

    /**
     * 配置文件名
     */
    private final static String FILE_NAME = "HaoAnalyzer.cfg.xml";
    /**
     * Hanlp配置文件名
     */
    private static final String CONFIG_FILE_NAME = "hanlp.properties";

    /**
     * @param indexSettings
     * @param name          分词器名称
     * @param env
     * @param settings
     */
    @Inject
    public Configuration(IndexSettings indexSettings, String name, Environment env, Settings settings) {
        this.environment = env;
        this.settings = settings;
        initialProperties(env);

        this.indexMode = settings.get("enableIndexMode", "false").equals("true");
        this.enableOOV = settings.get("enableOOV", "false").equals("true");
        this.enableFallBack = settings.get("enableFallBack", "false").equals("true");
        this.enableFailDingMsg = settings.get("enableFailDingMsg", "false").equals("true");
        this.enableSingleWord = settings.get("enableSingleWord", "false").equals("true");

        this.dingMsgContent = props.getProperty("dingMsgContent");
        this.dingWebHookUrl = props.getProperty("dingWebHookUrl");

        this.remoteFreqDict = props.getProperty("remoteFreqDict");
        this.syncDicTime = props.getProperty("syncDicTime");
        this.syncDicPeriodTime = Integer.parseInt(props.getProperty("syncDicPeriodTime"));

        Path dataDir = getDataInPluginDir();
        // hanlp 要设置配置文件路径
        Predefine.HANLP_PROPERTIES_PATH = dataDir.resolve(CONFIG_FILE_NAME).toString();
        this.customerDictionaryFile = getFilePath(props.getProperty("customerDictionaryFile"));
        this.baseDictionaryFile = getFilePath(props.getProperty("baseDictionary"));
        Dictionary.initial(this);

        if (indexSettings != null) {
            this.indexName = indexSettings.getIndex().getName();
        }

    }

    private static Path getDataInPluginDir() {
        return PathUtils.get(
            new File(AnalysisHaoPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(),
            "data").toAbsolutePath();
    }

    private static String getFilePath(String fileName) {
        assert confDir != null : "confDir is null, not init";
        Path filePath = confDir.resolve(fileName);
        String path = null;
        if (filePath.toFile().exists()) {
            path = filePath.toString();
        } else {
            path = getConfigInPluginDir().resolve(fileName).toString();
        }
        return path;
    }

    private static Path getConfigInPluginDir() {
        return PathUtils.get(
            new File(AnalysisHaoPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(),
            "config").toAbsolutePath();
    }

    private static void initialProperties(Environment env) {
        if (props == null) {
            synchronized (Configuration.class) {
                if (props == null) {
                    props = new Properties();
                    confDir = env.configFile().resolve(AnalysisHaoPlugin.PLUGIN_NAME);
                    Path configFile = confDir.resolve(FILE_NAME);

                    InputStream input = null;
                    try {
                        logger.info("try load config from {}", configFile);
                        input = new FileInputStream(configFile.toFile());
                    } catch (FileNotFoundException e) {
                        confDir = getConfigInPluginDir();
                        configFile = confDir.resolve(FILE_NAME);
                        try {
                            logger.info("try load config from {}", configFile);
                            input = new FileInputStream(configFile.toFile());
                        } catch (FileNotFoundException ex) {
                            logger.error("hao-analyzer", e);
                        }
                    }
                    if (input != null) {
                        try {
                            props.loadFromXML(input);
                        } catch (IOException e) {
                            logger.error("hao-analyzer", e);
                        }
                    }
                }
            }
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public boolean isIndexMode() {
        return indexMode;
    }

    public Configuration setIndexMode(boolean indexMode) {
        this.indexMode = indexMode;
        return this;
    }

    public boolean isEnableOOV() {
        return enableOOV;
    }

    public void setEnableOOV(boolean enableOOV) {
        this.enableOOV = enableOOV;
    }

    public boolean isEnableFallBack() {
        return enableFallBack;
    }

    public void setEnableFallBack(boolean enableFallBack) {
        this.enableFallBack = enableFallBack;
    }

    public boolean isEnableFailDingMsg() {
        return enableFailDingMsg;
    }

    public void setEnableFailDingMsg(boolean enableFailDingMsg) {
        this.enableFailDingMsg = enableFailDingMsg;
    }

    public String getDingWebHookUrl() {
        return dingWebHookUrl;
    }

    public void setDingWebHookUrl(String dingWebHookUrl) {
        this.dingWebHookUrl = dingWebHookUrl;
    }

    public String getDingMsgContent() {
        return dingMsgContent;
    }

    public boolean isEnableSingleWord() {
        return enableSingleWord;
    }

    public void setEnableSingleWord(boolean enableSingleWord) {
        this.enableSingleWord = enableSingleWord;
    }

    public void setDingMsgContent(String dingMsgContent) {
        this.dingMsgContent = dingMsgContent;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public void setBaseDictionaryFile(String baseDictionaryFile) {
        this.baseDictionaryFile = baseDictionaryFile;
    }

    public String getBaseDictionaryFile() {
        return baseDictionaryFile;
    }

    public String getCustomerDictionaryFile() {
        return customerDictionaryFile;
    }

    public void setCustomerDictionaryFile(String customerDictionaryFile) {
        this.customerDictionaryFile = customerDictionaryFile;
    }

    public String getRemoteFreqDict() {
        return remoteFreqDict;
    }

    public void setRemoteFreqDict(String remoteFreqDict) {
        this.remoteFreqDict = remoteFreqDict;
    }

    public String getSyncDicTime() {
        return syncDicTime;
    }

    public void setSyncDicTime(String syncDicTime) {
        this.syncDicTime = syncDicTime;
    }

    public Integer getSyncDicPeriodTime() {
        return syncDicPeriodTime;
    }

    public void setSyncDicPeriodTime(Integer syncDicPeriodTime) {
        this.syncDicPeriodTime = syncDicPeriodTime;
    }

    @Override
    public String toString() {
        return "Configuration{" + "environment=" + environment + ", settings=" + settings + ", indexMode=" + indexMode
            + ", enableOOV=" + enableOOV + ", indexName='" + indexName + '\'' + ", enableSingleWord=" + enableSingleWord
            + ", enableFallBack=" + enableFallBack + ", enableFailDingMsg=" + enableFailDingMsg + ", dingWebHookUrl='"
            + dingWebHookUrl + '\'' + ", dingMsgContent='" + dingMsgContent + '\'' + ", customerDictionaryFile='"
            + customerDictionaryFile + '\'' + ", freqFile='" + baseDictionaryFile + '\'' + ", remoteFreqDict='"
            + remoteFreqDict + '\'' + ", syncDicTime='" + syncDicTime + '\'' + ", syncDicPeriodTime='"
            + syncDicPeriodTime + '\'' + '}';
    }
}
