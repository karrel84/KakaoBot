/*
 * Copyright (C) 2012 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.suyong.kakaobot.net.base;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RequestFuture;
import com.suyong.kakaobot.net.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NetRequest<T extends NetEnty> extends Request<T> {
    //log//
    private static final boolean LOG = true;
    private static final boolean OUT_1 = true;
    private static final boolean OUT_2 = false;
    private static final boolean OUT_HEADER = false;

    private static final boolean IN_1 = false;
    private static final boolean IN_2 = false;
    private static final boolean IN_3 = false;
    private static final boolean IN_HEADER = false;

    private T mEnty;

    // req//////////////////////////////////////////////////////////////////////////////////////////////
    public NetRequest(T enty, Net.OnNetResponse<T> netResponse) {
        super(enty.method, enty.url, netResponse);
        mEnty = enty;
        this.listener = netResponse;
        setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 3, 1f));
        _NETLOG_OUT();
    }

    public NetRequest(T enty, RequestFuture<T> future) {
        super(enty.method, enty.url, future);
        mEnty = enty;
        this.listener = future;
        setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 3, 1f));
        _NETLOG_OUT();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        _NETLOG_OUT(mEnty.headers);
        return mEnty.getHeaders();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return mEnty.getBody();
    }

    public String getBodyContentType() {
        return mEnty.getBodyContentType();
    }

    // res//////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        parseHeaderResponse(response.headers);

        _NETLOG_IN(response);
        try {
            try {
                mEnty.parse(new String(response.data, HttpHeaderParser.parseCharset(response.headers)));
            } catch (UnsupportedEncodingException e) {
                mEnty.parse(new String(response.data));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error(new ParseError(response));
        }

        if (mEnty.success)
            return Response.success(mEnty, HttpHeaderParser.parseCacheHeaders(response));
        else
            return Response.error(new VolleyError(mEnty.errorMessage));

    }

    /**
     * header
     */
    private void parseHeaderResponse(Map<String, String> headers) {
        _NETLOG_IN(headers);
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        _NETLOG_IN(volleyError);// log
        mEnty.error(volleyError);
        return super.parseNetworkError(volleyError);
    }

    //callback//////////////////////////////////////////////////////////////////////////////////////////////
    private Response.Listener<T> listener;

    @Override
    protected void deliverResponse(T response) {
        if (listener != null)
            listener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        super.deliverError(error);
    }
    //debug//////////////////////////////////////////////////////////////////////////////////////////////

    public static final String SPACE = "          ";

    //log res//
    private void _NETLOG_OUT() {
        if (!LOG)
            return;

        try {
            if (mEnty._DEBUG_OUT1 || mEnty._DEBUG_OUT2 || OUT_1 || OUT_2)
                Log.e("나가:", mEnty.toString());

            if (mEnty._DEBUG_OUT2 || OUT_2) {
                if (mEnty.params != null) {
                    final StringBuilder sb = new StringBuilder("\n");
                    final Set<Entry<String, Object>> set = mEnty.params.entrySet();
                    for (Entry<String, Object> entry : set) {
                        if (entry.getValue() == null)
                            continue;
                        sb.append("  ").append(entry.getKey()).append(SPACE.substring(Math.min(SPACE.length(), entry.getKey().length())));
                        sb.append(" = ").append(entry.getValue());
                        sb.append("\n");
                    }
                    Log.e(sb);
                }
//				Log.l(new String(mEnty.body));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void _NETLOG_OUT(Map<String, String> headers) {
        if (!LOG)
            return;
        if (!(mEnty._DEBUG_OUT_HEADER || OUT_HEADER))
            return;
        try {
            StringBuilder sb = new StringBuilder("\n");
            final Set<Entry<String, String>> set = mEnty.headers.entrySet();
            for (Entry<String, String> entry : set) {
                sb.append("\t").append(entry.getKey()).append(": ").append(prettyJson(entry.getValue())).append("\n");
            }
            Log.e("나해:", mEnty.getClass().getSimpleName(), sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void _NETLOG_IN(Map<String, String> headers) {
        if (!LOG)
            return;
        if (!(mEnty._DEBUG_IN_HEADER || IN_HEADER))
            return;
        try {
            StringBuilder sb = new StringBuilder("\n");
            final Set<Entry<String, String>> set = headers.entrySet();
            for (Entry<String, String> entry : set) {
                sb.append("\t").append(entry.getKey()).append(": ").append(prettyJson(entry.getValue())).append("\n");
            }
            Log.w("들해:", mEnty.getClass().getSimpleName(), sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void _NETLOG_IN(NetworkResponse response) {
        if (!LOG)
            return;

        try {
            if (mEnty._DEBUG_IN1 || mEnty._DEBUG_IN2 || IN_1 || IN_2) {
                Log.w("들와:", "" + response.statusCode + "," + mEnty);
            }
            if (mEnty._DEBUG_IN2 || IN_2) {
                String json = prettyJson(new String(response.data, getParamsEncoding()));
                while (true) {
                    if (json.length() > 1024) {
                        Log.w("들와:", json.substring(0, 1024));
                        json = json.substring(1024, json.length());
                    } else {
                        Log.w("들와:", json);
                        break;
                    }
                }
//                Log.w("들와:", response.statusCode + "\n" + prettyJson(new String(response.data, getParamsEncoding())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void _NETLOG_IN(VolleyError volleyError) {
        if (!LOG)
            return;
        try {
            if (mEnty._DEBUG_IN1 || IN_1) {
                Object statusCode = null;
                try {
                    statusCode = volleyError.networkResponse.statusCode;
                } catch (Exception e) {
                    statusCode = volleyError.getMessage();
                }
                Log.e("!들와:", mEnty, "" + statusCode);
            }
            if (mEnty._DEBUG_IN2 || IN_2) {
                Log.e("!들와1/5:", "" + volleyError.getClass().getSimpleName());
                Log.e("!들와2/5:", "" + volleyError.getMessage());
                Log.e("!들와3/5:", "" + volleyError.networkResponse);
                try {
                    Log.e("!들와4/5:", "" + volleyError.networkResponse.statusCode);
                } catch (Exception e) {
                }
                try {
                    Log.e("!들와5/5:", prettyJson(new String(volleyError.networkResponse.data, getParamsEncoding())));
                } catch (Exception e) {
                }
            }
            if (mEnty._DEBUG_IN3 || IN_3) {
                volleyError.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String prettyJson(String json) throws JSONException {
        if (json.length() > 0) {
            if (json.charAt(0) == '{') {
                return new JSONObject(json).toString(4);
            }
            if (json.charAt(0) == '[') {
                return new JSONArray(json).toString(4);
            }
        }
        return json;
    }
}
