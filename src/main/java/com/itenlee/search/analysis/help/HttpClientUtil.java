package com.itenlee.search.analysis.help;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.TimeUnit;

/**
 * @author tenlee
 * @date 2020/6/4
 */
public class HttpClientUtil {

    private static final Logger logger = ESPluginLoggerFactory.getLogger(HttpClientUtil.class.getName());

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static volatile HttpClientUtil instance;

    private OkHttpClient okHttpClient;

    public static HttpClientUtil getInstance() {
        if (instance == null) {
            synchronized (HttpClientUtil.class) {
                if (instance == null) {
                    OkHttpClient okHttpClient = initOkHttpClient(50, 50000, 5000, 32, 1200);
                    instance = new HttpClientUtil(okHttpClient);
                }
            }
        }
        return instance;
    }

    public static HttpClientUtil getInstance(Integer httpConnectTimeoutMill, Integer httpReadTimeoutMill,
        Integer httpWriteTimeoutMill, Integer httpConnectPool, Integer httpConnectKeepAliveSecond) {

        if (instance == null) {
            synchronized (HttpClientUtil.class) {
                if (instance == null) {
                    OkHttpClient okHttpClient =
                        initOkHttpClient(httpConnectTimeoutMill, httpReadTimeoutMill, httpWriteTimeoutMill,
                            httpConnectPool, httpConnectKeepAliveSecond);
                    instance = new HttpClientUtil(okHttpClient);
                }
            }
        }
        return instance;
    }

    private static OkHttpClient initOkHttpClient(Integer httpConnectTimeoutMill, Integer httpReadTimeoutMill,
        Integer httpWriteTimeoutMill, Integer httpConnectPool, Integer httpConnectKeepAliveSecond) {
        SpecialPermission.check();
        return AccessController.doPrivileged((PrivilegedAction<OkHttpClient>)() -> {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (httpConnectTimeoutMill != null) {
                builder.connectTimeout(httpConnectTimeoutMill, TimeUnit.MILLISECONDS);
            }
            if (httpReadTimeoutMill != null) {
                builder.readTimeout(httpReadTimeoutMill, TimeUnit.MILLISECONDS);
            }
            if (httpWriteTimeoutMill != null) {
                builder.writeTimeout(httpWriteTimeoutMill, TimeUnit.MILLISECONDS);
            }

            if (httpConnectPool != null) {
                int keepAliveSecond = httpConnectKeepAliveSecond == null ? 300 : httpConnectKeepAliveSecond;
                builder.connectionPool(new ConnectionPool(httpConnectPool, keepAliveSecond, TimeUnit.SECONDS));
            }
            return builder.build();
        });
    }

    private HttpClientUtil(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public String postJSON(String url, String jsonContent) throws PrivilegedActionException, IOException {
        return postJSONWithResponse(url, jsonContent).body().string();
    }

    public Response postJSONWithResponse(String url, String jsonContent) throws PrivilegedActionException {
        SpecialPermission.check();
        return AccessController.doPrivileged((PrivilegedExceptionAction<Response>)() -> {
            RequestBody body = RequestBody.create(jsonContent, HttpClientUtil.JSON);
            Request request = new Request.Builder().url(url).post(body).build();
            Response response = okHttpClient.newCall(request).execute();
            return response;
        });
    }

    public String postJSON(String url, String json, int retryCount) throws Exception {
        Response response = null;
        Exception exception = null;
        while (retryCount > 0) {
            try {
                response = postJSONWithResponse(url, json);
                if (response.code() == 200) {
                    return response.body().string();
                }
            } catch (Exception e) {
                exception = e;
            } finally {
                if (response != null) {
                    response.close();
                }
            }
            retryCount--;
        }
        if (exception == null) {
            throw new NullPointerException("response is null");
        }
        throw exception;
    }

    public Response get(String url) throws PrivilegedActionException {
        SpecialPermission.check();
        return AccessController.doPrivileged((PrivilegedExceptionAction<Response>)() -> {
            Request.Builder builder = new Request.Builder().url(url);
            Request request = builder.get().build();
            Response response = okHttpClient.newCall(request).execute();
            return response;
        });
    }

    public Response head(String url, String lastModified, String eTags) throws PrivilegedActionException {
        SpecialPermission.check();
        return AccessController.doPrivileged((PrivilegedExceptionAction<Response>)() -> {
            Request.Builder builder = new Request.Builder().url(url);
            if (lastModified != null) {
                builder.header("If-Modified-Since", lastModified);
            }
            if (eTags != null) {
                builder.header("If-None-Match", eTags);
            }

            Request request = builder.head().build();
            Response response = okHttpClient.newCall(request).execute();
            return response;
        });
    }
}
