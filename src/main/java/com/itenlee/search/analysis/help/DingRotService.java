package com.itenlee.search.analysis.help;

import org.apache.logging.log4j.Logger;

/**
 * @author tenlee
 * @date 2020/6/4
 */
public class DingRotService {
    private static final Logger logger = ESPluginLoggerFactory.getLogger(DingRotService.class.getName());


    private static final String MESSAGE_TEXT = "{\n" +
            "    \"msgtype\": \"text\", \n" +
            "    \"text\": {\n" +
            "        \"content\": \"%s\"\n" +
            "    }, \n" +
            "    \"at\": {\n" +
            "        \"atMobiles\": [\n" +
            "        ], \n" +
            "        \"isAtAll\": false\n" +
            "    }\n" +
            "}";

    public static void sendDingTalkMessage(String content, String webHook) {
        content = content.replace("\\", "\\\\").replace("\"", "\\\"");
        String json = String.format(MESSAGE_TEXT, content);
        try {
            String body = HttpClientUtil.getInstance().postJSON(webHook, json);
            logger.info("error send ding talk content:{}, response: {}", content, body);
        } catch (Exception e) {
            logger.error("error send ding talk content:{} error", content, e);
        }
    }
}
