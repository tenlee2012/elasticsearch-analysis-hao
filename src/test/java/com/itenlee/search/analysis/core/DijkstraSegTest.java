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
        s = "俄罗斯 孙成功 GPS卫星定位,中华字诸葛明,中華字,徐骁snh48,99mm,66ka,66,demo,99.990.1,9.9 徐庆年";
        System.out.println("====start===");
        List<Term> terms = null;
        DijkstraSeg dijkstraSeg = new DijkstraSeg(dic);
        terms = dijkstraSeg.segSentence(s, true, 3, false);
        for (Term term : terms) {
            System.out.printf("start=%d, end=%d, %s\n", term.getOffset(), term.getEnd(), term.getText());
        }
    }

    @Test
    public void testPerformance() {
        Dictionary.initial(configuration);
        Dictionary dic = Dictionary.getInstance();
        String s = "俄罗斯大厦门🤣";
        System.out.println("====start===");
        s = "俄罗斯 孙成功 GPS卫星定位,中华字诸葛明,中華字,徐骁snh48,99mm,66ka,66,demo,99.990.1,9.9 徐庆年 ";
        for (int i = 0; i < 10; i++) {
            s += s;
        }
        DijkstraSeg dijkstraSeg = new DijkstraSeg(dic);
        long t1 = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            dijkstraSeg.segSentence(s, true, 3, false);
        }
        long t2 = System.nanoTime();
        System.out.println("cost " + (t2 - t1) / 1000 / 100 + " microseconds");
    }
}
