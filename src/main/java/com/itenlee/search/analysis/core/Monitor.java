package com.itenlee.search.analysis.core;

import com.itenlee.search.analysis.help.DingRotService;
import com.itenlee.search.analysis.help.ESPluginLoggerFactory;
import com.itenlee.search.analysis.help.HttpClientUtil;
import com.itenlee.search.analysis.lucence.Configuration;
import okhttp3.Response;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author tenlee
 * @date 2020/7/23
 */
public class Monitor implements Runnable {
    private static final Logger logger = ESPluginLoggerFactory.getLogger(Monitor.class.getName());

    private static HttpClientUtil httpClient = HttpClientUtil.getInstance();
    /*
     * 上次更改时间
     */
    static protected String lastModified = null;
    /*
     * 资源属性
     */
    static protected String eTags = null;

    /*
     * 请求地址
     */
    private String location;

    private Configuration configuration;

    public Monitor(String location, Configuration configuration) {
        this.location = location;
        // lastModified = null;     // 会导致初始化后，远程词典未修改的情况下，重复加载词典
        // eTags = null;            // 会导致初始化后，远程词典未修改的情况下，重复加载词典
        this.configuration = configuration;
    }

    public void run() {
        SpecialPermission.check();
        AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
            this.runUnprivileged();
            return null;
        });
    }

    /**
     * 监控流程：
     * ①向词库服务器发送get请求
     * ②从响应中获取Last-Modify、ETags字段值，判断是否变化
     * ③如果未变化，休眠1min，返回第①步
     * ④如果有变化，重新加载词典
     * ⑤休眠1min，返回第①步
     */

    public void runUnprivileged() {
        Response response = null;

        try {
            logger.info("check remote hao dict ...");
            response = httpClient.head(location, lastModified, eTags);

            //返回200 才做操作
            if (response.code() == 200) {

                if ((response.header("Last-Modified") != null && !response.header("Last-Modified")
                    .equalsIgnoreCase(lastModified)) || (response.header("ETag") != null && !response.header("ETag")
                    .equalsIgnoreCase(eTags))) {
                    DingRotService.sendDingTalkMessage("词库变化开始重构...", configuration.getDingWebHookUrl());

                    // 远程词库有更新,需要重新加载词典，并修改last_modified,eTags
                    lastModified = response.header("Last-Modified");
                    eTags = response.header("ETag");

                    Dictionary.getInstance().reLoadMainDict();

                    DingRotService.sendDingTalkMessage("词库重构成功结束", configuration.getDingWebHookUrl());
                }
            } else if (response.code() == 304) {
                //没有修改，不做操作
                //noop
            } else {
                DingRotService.sendDingTalkMessage("词库读取失败:" + location + ", code " + response.code(),
                    configuration.getDingWebHookUrl());
                logger.info("remote_ext_dict {} return bad code {}", location, response.code());
            }

        } catch (Exception e) {
            lastModified = null;
            eTags = null;
            DingRotService.sendDingTalkMessage("词库重构失败:" + e.getMessage(), configuration.getDingWebHookUrl());
            logger.error("remote_ext_dict {} error!", location, e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
