package com.itenlee.search.analysis.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hankcs.hanlp.HanLP;
import com.itenlee.search.analysis.algorithm.AhoCorasickDoubleArrayTrie;
import com.itenlee.search.analysis.algorithm.TokenNode;
import com.itenlee.search.analysis.help.DateUtil;
import com.itenlee.search.analysis.help.ESPluginLoggerFactory;
import com.itenlee.search.analysis.help.JSONUtil;
import com.itenlee.search.analysis.help.TextUtility;
import com.itenlee.search.analysis.lucence.Configuration;
import com.vdurmont.emoji.CustomEmojiParser;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tenlee
 * @date 2020/7/10
 */
public class Dictionary {
    private static final Logger logger = ESPluginLoggerFactory.getLogger(Dictionary.class.getName());

    private static final Pattern URL_NUM_ALPHA_CH_NUM_REG = Pattern.compile("(?<url>(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|])|(?<num>[0-9]+(\\.[0-9]+)*)|(?<alpha>[a-zA-Z]+)");
    private static final Pattern CH_NUM_REG = Pattern.compile("[一两二三四五六七八九十零][一两二三四五六七八九十零百千万兆亿]*");
    private static final String PUNC = TextUtility.sbc2dbcCase(" 。[],?!+-*/'\"…<>=《》`~！@#￥%……&();:{}\\.|^_、」「");

    private static volatile Dictionary instance;
    private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

    private Configuration configuration;
    private HashSet<String> metaWords = new HashSet<>();
    private long total;
    private double logTotal;
    private AhoCorasickDoubleArrayTrie doubleArrayTrie = new AhoCorasickDoubleArrayTrie();

    public static Dictionary getInstance() {
        if (instance == null) {
            throw new IllegalStateException("dict has not been initialized yet, please call initial method first.");
        }
        return instance;
    }

    synchronized public static void initial(Configuration cfg) {
        if (instance == null) {
            synchronized (Dictionary.class) {
                if (instance == null) {
                    try {
                        instance = new Dictionary(cfg);
                        instance.loadDict();

                        if (cfg.getRemoteFreqDict() != null) {
                            // 建立监控线程
                            logger.info("start monitor when {}", cfg.getSyncDicTime());
                            // 600 秒是初始延迟可以修改的 600是间隔时间 单位秒
                            pool.scheduleAtFixedRate(new Monitor(cfg.getRemoteFreqDict(), cfg), DateUtil.calcTimeGap(cfg.getSyncDicTime()), cfg.getSyncDicPeriodTime(), TimeUnit.SECONDS);
                        }
                        logger.info("dic init ok");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private Dictionary() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    private Dictionary(Configuration configuration) throws Exception {
        this.configuration = configuration;
    }

    /**
     * 读取META_WORDS_FILE，里边的词不再做更细颗粒度的分割
     */
    private void loadDict() throws Exception {
        TreeMap<String, Long> baseDictionary = new TreeMap<>();
        try {
            baseDictionary = JSONUtil.parseJSON(new File(configuration.getBaseDictionaryFile()), new TypeReference<TreeMap<String, Long>>() {
            });
        } catch (Exception e) {
            logger.error("base dictionary load fail:{}", e.getMessage());
        }
        TreeMap<String, Long> extFreq = this.loadCustomerDictionary();
        this.total = baseDictionary.values().stream().reduce(0L, Long::sum);
        this.total = baseDictionary.values().stream().reduce(this.total, Long::sum);
        this.logTotal = Math.log(total);
        this.buildTrie(baseDictionary, extFreq);
    }

    /**
     * 读取加载外部自定义词典
     *
     * @return
     * @throws IOException
     */
    private TreeMap<String, Long> loadCustomerDictionary() throws IOException {
        TreeMap<String, Long> extFreq = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(configuration.getCustomerDictionaryFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                String[] wordFreq = line.split(",");
                if (wordFreq.length == 3 && "1".equals(wordFreq[2])) {
                    // 是元词
                    this.metaWords.add(wordFreq[0]);
                }
                if (wordFreq.length == 1) {
                    extFreq.put(wordFreq[0], 100000L);
                } else {
                    extFreq.put(wordFreq[0], Long.parseLong(wordFreq[1]));
                }
            }

        } catch (IOException e) {
            logger.error("custom dictionary load fail:{}", e.getMessage(), e);
        }
        return extFreq;
    }

    /**
     * 从词典构建字典树
     */
    private void buildTrie(TreeMap<String, Long> freq, TreeMap<String, Long> extFreq) {
        TreeMap<String, Double> wordFreq = new TreeMap<String, Double>();
        freq.forEach((k, v) -> {
            //保证了单字的词频大概率比词低
            if (k.length() > 1 && v <= 100) {
                v = 100 * v;
            } else if (k.length() == 1 && v >= 10000) {
                v = v / 1000;
            }
            wordFreq.put(k, Math.abs(this.logTotal - Math.log(v)));
        });
        extFreq.forEach((k, v) -> {
            wordFreq.put(k, Math.abs(this.logTotal - Math.log(v)));
        });
        this.doubleArrayTrie.build(wordFreq);
    }

    /**
     * 将每一个字转化为结点，储存各种分词过程中需要的临时数据
     *
     * @param sentence
     * @return
     */
    public static ArrayList<TokenNode> buildNodes(String sentence) {
        String cleanedSen = clean(sentence);
        Matcher matcher = URL_NUM_ALPHA_CH_NUM_REG.matcher(cleanedSen);
        // 非中文字符的在原字符串的偏移量，key=start offset, value=end offset
        HashMap<Integer, Integer> alphaSpanMap = new HashMap<>();
        HashMap<Integer, String> alphaTagMap = new HashMap<>();
        while (matcher.find()) {
            alphaSpanMap.put(matcher.start(), matcher.end());
            if (matcher.start(TokenNode.URL_TAG) != -1) {
                alphaTagMap.put(matcher.start(), TokenNode.URL_TAG);
            } else if (matcher.start(TokenNode.NUM_TAG) != -1) {
                alphaTagMap.put(matcher.start(), TokenNode.NUM_TAG);
            } else if (matcher.start(TokenNode.ALPHA_TAG) != -1) {
                alphaTagMap.put(matcher.start(), TokenNode.ALPHA_TAG);
            }
        }
        // 处理emoji表情，emoji表情的长度是不一样的，比如👨‍👩‍👧‍👦=8，😂=2
        List<CustomEmojiParser.CustomEmoji> emojis = CustomEmojiParser.getUnicodeCandidates(sentence);
        int start = 0, wordNum = 0, emojiIndex = 0;
        int senLength = sentence.length();
        ArrayList<TokenNode> nodes = new ArrayList<>();
        while (start < senLength) {
            TokenNode node;
            Integer alphaSpanEnd = alphaSpanMap.get(start);
            if (alphaSpanEnd != null) {
                // 是特殊符号
                node = new TokenNode(sentence.substring(start, alphaSpanEnd), wordNum, alphaTagMap.get(start));
                node.setCleanedText(cleanedSen.substring(start, alphaSpanEnd));
                node.setStartOffset(start);
                start = alphaSpanEnd;
            } else {
                String subCleanStr;
                String tag = null;
                if (emojis.size() > emojiIndex && emojis.get(emojiIndex).getStartOffset() == start) {
                    // 是emoji
                    subCleanStr = emojis.get(emojiIndex).getEmojiStr();
                    // emoji当做标点符号处理
                    tag = TokenNode.PUNC_TAG;
                    emojiIndex++;
                } else {
                    // 特殊的汉字长度不一定是1，比如 𡃁=2
                    subCleanStr = new String(Character.toChars(cleanedSen.codePointAt(start)));
                    if (PUNC.contains(subCleanStr)) {
                        // 标点符号
                        tag = TokenNode.PUNC_TAG;
                    }
                }
                node = new TokenNode(sentence.substring(start, start + subCleanStr.length()), wordNum, tag);
                node.setCleanedText(subCleanStr);
                node.setStartOffset(start);
                start = start + subCleanStr.length();
            }
            nodes.add(node);

            wordNum++;
        }
        return nodes;
    }

    private static String clean(String sentence) {
        sentence = sentence.replaceAll(TextUtility.WHITESPACE_CHARCLASS, " ");
        sentence = TextUtility.sbc2dbcCase(sentence);
        String sentenceFinal = sentence;
        String simpleCN = AccessController.doPrivileged((PrivilegedAction<String>) () -> {
            return HanLP.convertToSimplifiedChinese(sentenceFinal);
        });
        // 如果opencc识别成功,长度应该是相等的
        //opencc会把不认识的繁体字转换成下面𠡠字符,而这个字符的长度是2
        if (simpleCN.length() == sentence.length()) {
            sentence = simpleCN;
        }
        return TextUtility.toLowerCase(sentence);
    }

    public void reLoadMainDict() throws Exception {
        logger.info("start to reload dict.");
        // 新开一个实例加载词典，减少加载过程对当前词典使用的影响
        Dictionary tmpDict = new Dictionary(configuration);
        tmpDict.loadDict();
        instance.total = tmpDict.total;
        instance.logTotal = tmpDict.logTotal;
        instance.doubleArrayTrie = tmpDict.doubleArrayTrie;
        instance.metaWords = tmpDict.metaWords;
        logger.info("reload dict finished.");
    }

    public HashSet<String> getMetaWords() {
        return metaWords;
    }

    public double getLogTotal() {
        return logTotal;
    }

    public AhoCorasickDoubleArrayTrie getDoubleArrayTrie() {
        return doubleArrayTrie;
    }
}