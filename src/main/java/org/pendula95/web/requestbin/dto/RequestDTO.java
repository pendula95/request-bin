package org.pendula95.web.requestbin.dto;

import java.util.Map;

public class RequestDTO {

    private String method;
    private Map<String, String> headers;
    private Map<String, String> params;
    private String body;
    private long timestamp;
    private String ip;
    private String path;
    private String id;

    public RequestDTO() {
    }

    public RequestDTO(String method, Map<String, String> headers, Map<String, String> params, String body, long timestamp, String ip, String path, String id) {
        this.method = method;
        this.headers = headers;
        this.params = params;
        this.body = body;
        this.timestamp = timestamp;
        this.ip = ip;
        this.path = path;
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
