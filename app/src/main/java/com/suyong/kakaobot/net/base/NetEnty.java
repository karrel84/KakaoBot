package com.suyong.kakaobot.net.base;

import android.net.Uri;

import com.android.volley.Request.Method;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class NetEnty {
    private static final String UTF_8 = "UTF-8";
    //req
    public boolean _DEBUG_OUT_HEADER = true;
    public boolean _DEBUG_OUT1 = true;
    public boolean _DEBUG_OUT2 = true;

    public boolean _DEBUG_IN_HEADER = true;
    public boolean _DEBUG_IN1 = true;
    public boolean _DEBUG_IN2 = true;
    public boolean _DEBUG_IN3 = true;

    protected final int method;
    protected String url;

    public NetEnty(int method) {
        this.method = method;
    }

    //req-post
    protected byte[] body;
    protected String contentType;
    protected Map<String, Object> params = new HashMap<String, Object>();
//    protected Map<String, Object> params = new HashMap<String, Object>();

    //header
    protected Map<String, String> headers = new HashMap<String, String>();

    //res
    protected boolean success;
    protected String errorMessage;
    protected String response;

    protected void parse(String json) {
        this.response = json;
    }

    protected void error(Exception error) {
        this.errorMessage = error.getMessage();
    }

    public Object getQueryParameter(String key) {
        if (method == Method.GET)
            return Uri.parse(url).getQueryParameter(key);
        else
            return params.get(key);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        if (body == null) {
            setBody();
        }
        return body;
    }

    public String getBodyContentType() {
        return contentType;
    }

    public String getResponse() {
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private void setBody() {
        if (isStringParams())
            setParametersBody();
        else
            setMultipartBody();
    }

    private boolean isStringParams() {
        for (Object obj : params.values()) {
            if (obj instanceof File /*|| obj instanceof ContentBody*/)
                return false;
        }
        return true;
    }

    private void setParametersBody() {
        Map<String, Object> stringParams = this.params;

        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Entry<String, ? extends Object> entry : stringParams.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                if (value == null)
                    continue;

                encodedParams.append(URLEncoder.encode(key, UTF_8));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(value.toString(), UTF_8));
                encodedParams.append('&');
            }
            this.body = encodedParams.toString().getBytes(UTF_8);
            this.contentType = "application/x-www-form-urlencoded; charset=" + UTF_8;
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + UTF_8, uee);
        }
    }

	private void setMultipartBody() {
	}

    private static final String LINE_FEED = "\r\n";

    public StringBuilder addFilePart(String fieldName, File uploadFile) throws IOException {
        StringBuilder encodedParams = new StringBuilder();
        String fileName = uploadFile.getName();
//        writer.append("--" + boundary).append(LINE_FEED);
        encodedParams.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
        encodedParams.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
        encodedParams.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        encodedParams.append(LINE_FEED);
        return encodedParams;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + (method == Method.POST ? "POST" : "GET") + ">" + url;
    }

}