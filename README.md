# HAO ES 分词器
## 简介
一个elasticsearch 中文分词 插件。

QQ交流群：743457803

> **如何开发一个ES分词插件**请参考 [这里](https://github.com/tenlee2012/elasticsearch-analysis-demo)

主要参考了 [IK](https://github.com/medcl/elasticsearch-analysis-ik) 和 [HanLP](https://github.com/hankcs/HanLP)


### 特性

- 支持**复杂汉字**，有的汉字在java中长度**不是1**，比如`𡃁`，而`IK`等不支持。

- 支持**单字**分词和搜索，而`ik_max_word`模式不支持。

- 支持**自定义长度分词**，适合短文本下的人名等识别。
> 根据空格标点符号字母数字等分隔后的汉字文本长度`<=autoWordLength`会自动识别为一个词语。
  
- 支持emoji搜索

- 相比IK，比IK更智能，更准确。
  - 示例：比如IK `ik_max_word`是穷举所有可能词，导致搜索一些不相关的也会被搜到。
`任性冲动过`分词结果居然有`任性 性冲动 动过`,那么搜`性冲动`就会把这个doc搜索到。
`南京市长江大桥`，结果是`南京市 市长 长江大桥`，那么搜`市长`会把这个doc搜索到，而hao分词器不会，通过词频计算最短路，识别出可能性最高的词组。还可以根据自己场景，随意调节词频。

- [ik_smart 分词结果不是 ik_max_word 的子集](https://github.com/medcl/elasticsearch-analysis-ik/issues/584)，hao_search_mode 分词结果是 hao_index_mode 分词结果的子集

- 相比HanLp，比HanLP更轻量，**分词更可控**，没有一些智能的人名等预测功能，可能会导致分词不稳定不准确，机器学习对于长短文本不同，预测分词结果也不同。并且HanLP也没有官方的ES插件。

- 根据词频计算最短路，穷举出可能的词，而不是所有的词，如果穷举的词不对，可以调词频来纠正，词频文件是**可读性更好**的`txt`文件

- 支持元词，比如`俄罗斯`不会再拆分成`俄`和`罗斯`（`罗斯`是常用人名）。这样搜`罗斯`就不会把`俄罗斯`相关文档召回

- 但是不支持词性


提供
Analyzer: `hao_search_mode`, `hao_index_mode`
Tokenizer: `hao_search_mode`, `hao_index_mode`

Versions
--------

Git tag | ES version
-----------|-----------
master | ES最新稳定版
v7.17.1 | 7.17.1
vX.Y.Z | X.Y.Z

## 使用
### 安装
方式1. `bin/elasticsearch-plugin install file:///Users/xiaoming/Download/analysis-hao.zip`

方式2. 解压后，放在es plugins目录即可。

最后重启ES

### ES 版本升级
如果没有你需要的对应ES版本，要修改一下几个地方：
1. 修改`pom.xml`->`elasticsearch.version`的值为对应版本。
2. 编译，按照响应报错修改代码，比如可能有`HaoTokenizerFactory.java`的构造方法。
   最后执行 `mvn clean package -Dmaven.test.skip=true`，就可以得到插件的`zip`安装包。

### 分词器
下面是自定义分词器可用的配置项
#### 参数
---
配置项参数 | 功能 | 默认值
----|---|---
`enableIndexMode` | 是否使用index模式，index模式为细颗粒度。| `hao_search_mode`为`false`，`hao_index_mode`为`true`,细颗粒度适合Term Query,粗颗粒度适合Phrase查询
`enableFallBack` | 如果分词报错，是否启动最细粒度分词，即按字分。建议`search_mode`使用，不至于影响用户搜索。`index_mode`不启动，以便及时报错告警通知。| `false`不启动降级
`enableFailDingMsg` | 是否启动失败钉钉通知,通知地址为`HttpAnalyzer.cfg.xml`的`dingWebHookUrl`字段。| `false`
`enableSingleWord` | 是否使用细粒度返回的单字。比如`体力值`，分词结果只存`体力值`,`体力`,而不存`值` | `false`
`autoWordLength` | 根据空格标点符号字母数字等分隔后的汉字文本长度小于`autoWordLength`会自动识别为一个词语。 默认-1不开启，**>=2**视为开启| `-1`

#### 内置分词器介绍
- `hao_index_mode`

会根据词库的词条和权重，递归分词，直到该词不可分。如果设置了`enableSingleWord=true`，会一直分到单字为止。

例如这段文本`南京市长江大桥`
1. `南京市长江大桥` ==> `南京市`, `长江大桥`
2. `南京市`==>`南京`,`市`, `长江大桥`==>`长江`,`大桥`
3. 如果`enableSingleWord=false`，递归停止，得到分词为`南京市`,`南京`,`市`,`长江大桥`,`长江`,`大桥`
4. 如果`enableSingleWord=true`，继续递归，直到单字位置，得到分词为`南京市`,`南京`,`南`,`京`,`市`,`长江大桥`,`长江`,`长`,`江`,`大桥`,`大`,`桥`
- `hao_search_mode`

该模式下，相当于`hao_index_mode`模式只递归一次。
分词结果为`南京市`, `长江大桥`。因为该模式下`enableIndexMode=false`，如果改成`true`，则和`hao_index_mode`一样的效果。
### HaoAnalyzer.cfg.xml 配置

---
参数| 功能 | 备注
--- | --- | ---
`baseDictionary` |基础词库文件名 | 放在插件`config`目录或者es的`config`目录，不用更改
`customerDictionaryFile` | 用户自定义远程词库文件，多个文件用英文分号;分隔| 会存储在插件`config`目录或者es的`config`目录
`remoteFreqDict` | 远程用户自定义词库文件 | 方便热更新，热更新通过下面两个参数定时更新。
`syncDicTime` | 远程词库下次同步时间 `hh:mm:ss` | 不填使用`syncDicPeriodTime`作为下次同步时间
`syncDicPeriodTime` | 远程词库同步时间间隔,秒,最小值30 | 比如 `syncDicTime=20:00:00,syncDicPeriodTime=86400`，则是每天20点同步
`dingWebHookUrl` | 钉钉机器人url | 用于分词异常，同步词库异常/成功通知|
`dingMsgContent` | 机器人通知文案 | 注意配置钉钉机器人的时候关键词要和这个文案匹配，不然会消息发送失败

### 词库说明
> 优先读取 `{ES_HOME}/config/analysis-hao/`目录，没有读取 `{ES_HOME}/plugins/analysis-hao/config`目录下的文件

- 基础词库
  基础词库是`base_dictionary.txt`，以逗号分割，后面的数字表示词频。
  例如：`奋发图强` 分词结果是 `奋`, `发图`, `强`, 是因为`发图`这个词的词频太高了（因为出现次数高），则可以降低词频，手动修改`base_dictionary.txt`文件就好了。
- 远程词库
  用户自定义词库会按照配置的时间和周期定期执行。
  从远程词库更新完成后会自动覆盖现在的`customerDictionaryFile`。
  远程词库的文件格式**每行**格式为 `{词},{词频},{是否元词}`, 例如`俄罗斯,1000,1`。
  是否元词字段解释：
  `1`代表是元词，不会再细拆分，`俄罗斯`不会再拆分成`俄`和`罗斯`（罗斯是常用人名）。这样搜`罗斯`就不会把`俄罗斯`相关文档召回。
  `0`就是可以继续细拆分，比如`奋发图强`
- 远程词库是否重新加载是根据 http head 请求返回头中的两个字段是否有至少有一个发生变化，两个字段为：Last-Modified、ETag


### 示例索引demo
建索引：
```
PUT test/
{
  "settings": {
    "index": {
      "analysis": {
        "analyzer": {
          "search_analyzer": {
            "filter": [
              "lowercase"
            ],
            "char_filter": [
              "html_strip"
            ],
            "type": "custom",
            "tokenizer": "my_search_token"
          },
          "index_analyzer": {
            "char_filter": [
              "html_strip"
            ],
            "type": "custom",
            "tokenizer": "my_index_token"
          }
        },
        "tokenizer": {
          "my_index_token": {
            "enableFailDingMsg": "true",
            "type": "hao_index_mode",
            "enableSingleWord": "true",
            "enableFallBack": "true",
            "autoWordLength": 3
          },
          "my_search_token": {
            "enableFailDingMsg": "true",
            "type": "hao_search_mode",
            "enableSingleWord": "true",
            "enableFallBack": "true",
            "autoWordLength": 3
          }
        }
      },
      "number_of_replicas": "0"
    }
  },
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "index_options": "offsets",
        "analyzer": "index_analyzer",
        "search_analyzer": "search_analyzer"
      }
    }
  }
}
```
测试分词
```
test/_analyze
{
  "analyzer": "index_analyzer",
  "text": "徐庆年 奋发图强打篮球有利于提高人民生活，有的放矢，中华人民共和国家庭宣传委员会宣。🐶"
}

test/_analyze
{
  "analyzer": "search_analyzer",
  "text": "徐庆年 奋发图强打篮球有利于提高人民生活，有的放矢，中华人民共和国家庭宣传委员会宣。🐶"
}
```
`徐庆年`并不在词库中，但是通过`autoWordLength`识别为一个词。

