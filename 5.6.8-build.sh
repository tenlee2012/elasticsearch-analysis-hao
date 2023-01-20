
element=5.6.8

sed -i "s/super(indexSettings, settings, name)/super(indexSettings, name, settings)/" src/main/java/org/elasticsearch/index/analysis/hao/HaoTokenizerFactory.java
sed -i "s/org.elasticsearch.core.PathUtils/org.elasticsearch.common.io.PathUtils/" src/main/java/com/itenlee/search/analysis/lucence/Configuration.java
sed -i "s/org.elasticsearch.core.PathUtils/org.elasticsearch.common.io.PathUtils/" src/main/java/com/itenlee/search/analysis/help/MyIIOAdapter.java

sed -i "s/org.elasticsearch.SpecialPermission/com.itenlee.search.analysis.help.SpecialPermission/" src/main/java/com/itenlee/search/analysis/core/Monitor.java
sed -i "s/org.elasticsearch.SpecialPermission/com.itenlee.search.analysis.help.SpecialPermission/" src/main/java/com/itenlee/search/analysis/help/HttpClientUtil.java

sed -i "s/<includeBaseDirectory>false<\/includeBaseDirectory>/<includeBaseDirectory>true<\/includeBaseDirectory><baseDirectory>elasticsearch<\/baseDirectory>/" src/main/assembly/plugin.xml

cat > src/main/java/com/itenlee/search/analysis/help/SpecialPermission.java << EOF

package com.itenlee.search.analysis.help;

public final class SpecialPermission {
    public SpecialPermission() {
    }
    public static void check() {
    }
}
EOF

sed -i "s/<elasticsearch\.version>.*<\/elasticsearch\.version>/<elasticsearch\.version>$element<\/elasticsearch\.version>/g" pom.xml

# mvn clean package -Dmaven.test.skip=true
