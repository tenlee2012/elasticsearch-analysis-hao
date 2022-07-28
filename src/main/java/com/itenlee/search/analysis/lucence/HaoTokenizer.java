package com.itenlee.search.analysis.lucence;

import com.itenlee.search.analysis.help.ESPluginLoggerFactory;
import com.itenlee.search.analysis.help.TextUtility;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 注意：Lucene要求Tokenizer必须是不可继承的，也即class自己要是final，或者incToken方法是final的。可以不在Java启动参数中传递“ea”进行规避，也即禁用assert能力。
 *
 * @author tenlee
 * @date 2020/5/28
 */
public final class HaoTokenizer extends Tokenizer {
    private static final Logger logger = ESPluginLoggerFactory.getLogger(HaoTokenizer.class.getName());

    //词元文本属性
    private final CharTermAttribute termAtt;
    //词元位移属性
    private final OffsetAttribute offsetAtt;
    // 距离
    private final PositionIncrementAttribute positionAttr;

    /**
     * 单文档当前所在的总offset，当reset（切换multi-value fields中的value）的时候不清零，在end（切换field）时清零
     */
    private int totalOffset = 0;

    private AnalyzeContext analyzeContext;

    public HaoTokenizer(Configuration configuration) {
        super();
        offsetAtt = addAttribute(OffsetAttribute.class);
        termAtt = addAttribute(CharTermAttribute.class);
        positionAttr = addAttribute(PositionIncrementAttribute.class);

        analyzeContext = new AnalyzeContext(input, configuration);
    }

    /**
     * @return 返会true告知还有下个词元，返会false告知词元输出完毕
     * @throws IOException
     */
    @Override
    public boolean incrementToken() throws IOException {
        this.clearAttributes();

        int position = 0;
        Term term;
        boolean unIncreased = true;
        do {
            term = analyzeContext.next();
            if (term == null) {
                break;
            }
            if (TextUtility.isBlank(term.getText())) { // 过滤掉空白符，提高索引效率
                continue;
            }

            ++position;
            unIncreased = false;
        } while (unIncreased);

        if (term != null) {
            positionAttr.setPositionIncrement(position);
            if (term.getText() != null) {
                termAtt.setEmpty().append(term.getText());
            }
            offsetAtt.setOffset(correctOffset(totalOffset + term.getOffset()),
                    correctOffset(totalOffset + term.getOffset() + term.getText().length()));
            return true;
        } else {
            totalOffset += analyzeContext.offset;
            return false;
        }
    }

    @Override
    public void end() throws IOException {
        super.end();
        int finalOffset = correctOffset(this.totalOffset);
        offsetAtt.setOffset(finalOffset, finalOffset);
        totalOffset = 0;
    }

    /**
     * 必须重载的方法，否则在批量索引文件时将会导致文件索引失败
     */
    @Override
    public void reset() throws IOException {
        super.reset();
        analyzeContext.reset(new BufferedReader(this.input));
    }
}
