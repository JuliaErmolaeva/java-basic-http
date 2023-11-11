package ru.otus.ermolaeva.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private static final Logger logger = LogManager.getLogger(Request.class.getName());

    private String uri;
    private String raw;
    private Map<String, String> params;

    public String getUri() {
        return uri;
    }

    public Request(String rawRequest) {
        this.raw = rawRequest;
        this.uri = parseUri(raw);
        this.params = parseGetRequestParams(raw);
    }

    private static String parseUri(String request) {
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
        logger.info("Запрос:");
        logger.info("uri: " + uri);
        logger.info("params: " + params);
    }

    public String getParam(String key) {
        return params.get(key);
    }
}
