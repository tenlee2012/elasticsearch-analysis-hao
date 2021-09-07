package com.itenlee.search.analysis.core;

import com.itenlee.search.analysis.lucence.Configuration;
import com.itenlee.search.analysis.lucence.Term;
import junit.framework.TestCase;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.junit.Test;

import java.util.List;

/**
 * @author tenlee
 */
public class DijkstraSegTest extends TestCase {

    Settings settings = Settings.builder().put("path.home", "~/workspace/elasticsearch-analysis-hao/target")
        .put("enableFallBack", "true").put("enableFailDingMsg", "false").build();
    Environment env = new Environment(settings, null);
    Configuration configuration = new Configuration(null, null, env, settings).setIndexMode(true);

    @Test
    public void testSegSentence() {
        Dictionary.initial(configuration);
        Dictionary dic = Dictionary.getInstance();
        String s = "俄罗斯大厦门🤣";
        s = "GPS卫星定位,中华字,中華字,snh48,99mm,66ka,66,demo,99.990.1,9.9";
        System.out.println("====start===");
        List<Term> terms = null;
        DijkstraSeg dijkstraSeg = new DijkstraSeg(dic);
        terms = dijkstraSeg.segSentence(s, true, true, false);
        for (Term term : terms) {
            System.out.printf("start=%d, end=%d, %s\n", term.getOffset(), term.getEnd(), term.getText());
        }
    }
}
