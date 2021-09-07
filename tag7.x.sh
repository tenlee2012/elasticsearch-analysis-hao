version1=(
7.0.1 7.1.0 7.1.1 7.2.0 7.2.1 7.3.0 7.3.1 7.3.2
7.4.0 7.4.1 7.4.2 7.5.0 7.5.1 7.5.2 7.6.0 7.6.1 7.6.2
7.7.0 7.7.1 7.8.0 7.8.1 7.9.0 7.9.1 7.9.2 7.9.3
7.10.0 7.10.1 7.10.2
7.11.1 7.11.2 7.12.0 7.12.1
7.13.0 7.13.1 7.13.2 7.13.3 7.13.4 7.14.0 7.14.1
)
pre=7.0.0
for element in ${version1[@]}
do
  echo $element'#'$pre
  if [[ $element == 7.4.0 ]]; then

    echo "$?";
  fi
  if [[ $element == 7.4.0 ]]; then
    sed -i "" "s/super(indexSettings, settings)/super(indexSettings, settings, name)/" src/main/java/org/elasticsearch/index/analysis/hao/HaoTokenizerFactory.java
    echo "$?";
  fi
  if [[ $element == 7.14.0 ]]; then
    sed -i "" "s/org.elasticsearch.common.io.PathUtils/org.elasticsearch.core.PathUtils/" src/main/java/com/itenlee/search/analysis/lucence/Configuration.java
    sed -i "" "s/org.elasticsearch.common.io.PathUtils/org.elasticsearch.core.PathUtils/" src/main/java/com/itenlee/search/analysis/help/MyIIOAdapter.java
    echo "$?";
  fi
  sed -i "" "s/<elasticsearch.version>$pre<\/elasticsearch.version>/<elasticsearch.version>$element<\/elasticsearch.version>/g" pom.xml
#  mvn clean package -Dmaven.test.skip=true || exit 1
  pre=$element
  git add .
  git commit -m "auto v$element"
  git tag -d v$element
  git push --delete github v$element
  git push --delete origin v$element
  git tag v$element
  git push origin
  git push github
  git push --tag github
  git push --tag origin
done

