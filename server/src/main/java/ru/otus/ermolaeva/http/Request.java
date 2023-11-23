package ru.otus.ermolaeva.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private static final Logger logger = LogManager.getLogger(Request.class);
    private String uri;
    private String raw;
    private String body;
    private HttpMethod method;
    private Map<String, String> params;

    public Request(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[2048];
        int n = inputStream.read(buffer);
        this.raw = new String(buffer, 0, n);
        this.method = parseMethod(raw);
        this.uri = parseUri(raw);
        this.params = parseGetRequestParams(raw);
        this.body = parseBody(raw);
    }

    private HttpMethod parseMethod(String request) {
        int endIndex = request.indexOf(' ');
        String method = request.substring(0, endIndex);
        return HttpMethod.valueOf(method);
    }

    private String parseBody(String request) {
        int startIndex = request.indexOf("\r\n\r\n");
        return request.substring(startIndex + 4);
    }

    private String parseUri(String request) {
        int startIndex = request.indexOf(' ');
        int endIndex = request.indexOf(' ', startIndex + 1);
        String uri = request.substring(startIndex + 1, endIndex);
        if (uri.contains("?")) {
            return uri;
        }
        endIndex = uri.indexOf('?');
        if (endIndex < startIndex) {
            return uri;
        }
        return uri.substring(0, endIndex);
    }

    private Map<String, String> parseGetRequestParams(String request) {
        int startIndex = request.indexOf(' ');
        int endIndex = request.indexOf(' ', startIndex + 1);
        String uri = request.substring(startIndex + 1, endIndex);
        if (!uri.contains("?")) {
            return Collections.emptyMap();
        }
        String[] paramsKeyValue = uri.substring(uri.indexOf('?') + 1).split("&");
        Map<String, String> out = new HashMap<>();
        for (String p : paramsKeyValue) {
            String[] keyValue = p.split("=");
            out.put(keyValue[0], keyValue[1]);
        }
        return out;
    }

    public void show() {
        logger.info("Получен запрос:");
        logger.info("Запрос: " + method);
        logger.info("uri: " + uri);
        logger.info("params: " + params);
        if (method == HttpMethod.POST) {
            logger.info("body: " + body);
        }
    }

    public String getUri() {
        return uri;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }

    public String getParam(String key) {
        return params.get(key);
    }
}
