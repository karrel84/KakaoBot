package com.suyong.kakaobot.net.base;


import com.android.volley.Request.Method;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.suyong.kakaobot.net.util.Log;
import com.suyong.kakaobot.net.util.Pojo;
import com.suyong.kakaobot.net.util.SDF;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

public class BFEnty extends NetEnty {

    String err;
    String err_msg;
    String session_msg;

    private boolean isShowProgress = true;

    protected void setEnableProgress(boolean isEnable) {
        isShowProgress = isEnable;
    }

    public String getSessionMsg() {
        return session_msg;
    }

    private Gson gson;

    public BFEnty() {
        super(Method.POST);

        gson = new GsonBuilder()//
                .registerTypeAdapter(Date.class, deserializer_date)//
                .registerTypeAdapter(boolean.class, deserializer_boolean)//
                .registerTypeAdapter(ArrayList.class, deserializer_stringarraylist)//
                .create();
    }

    protected void setParam(Object... key_value) {
        if (key_value.length % 2 == 1)
            throw new IllegalArgumentException("!!key value must pair");

        int N = (key_value.length) / 2;
        for (int i = 0; i < N; i++) {
            final String key = (String) key_value[i * 2];
            final Object value = key_value[i * 2 + 1];
            // Log.l(key, value);
            if (value == null) {
                params.put(key, null);
            } else if (value.getClass().isArray()) {
                final Object[] values = (Object[]) value;
                for (int j = 0; j < values.length; j++)
                    params.put(key + "[" + j + "]", values[j]);
            } else if (value.getClass().isAssignableFrom(ArrayList.class)) {
                final ArrayList<?> values = (ArrayList<?>) value;
                for (int j = 0; j < values.size(); j++)
                    params.put(key + "[" + j + "]", values.get(j));
            } else if (value instanceof Boolean) {
                params.put(key, (((Boolean) value) ? "Y" : "N"));
            } else {
                params.put(key, value);
            }
        }
    }

    @Override
    protected void parse(String json) {
        super.parse(json);

        json = json.replaceAll("\"n\"", "N");
        json = json.replaceAll("\"y\"", "Y");
//        Log.d(json.toString());

        // 프로그래스 종료
        hideProgress();

        // 세선 만료 체크
        if (isSessionOut(json)) return;

        // 데이터에 값을 넣음
        insertData(json);

        // 성공여부 체크
        checkSuccess(json);
    }

    /**
     * 성공여부 체크
     *
     * @param json
     */
    private void checkSuccess(String json) {
        try {
            JSONObject jo = new JSONObject(json);
            err = jo.isNull("err") ? "N" : jo.getString("err");
            err_msg = jo.isNull("err_msg") ? "성공" : jo.getString("err_msg");
            success = !err.equals("Y");
            errorMessage = err_msg;
            session_msg = jo.isNull("session_msg") ? "" : jo.getString("session_msg");

        } catch (JSONException e) {
            e.printStackTrace();
            error(e);
        }
    }

    /**
     * 리플렉션을 이용하여 데이터에 값을 넣어준다
     *
     * @param json
     */
    private void insertData(String json) {
        try {
            final Class<?> cls = Class.forName(getClass().getName() + "$Data");
            final Field field = getClass().getField("data");
            field.set(this, gson.fromJson(json, cls));
        } catch (Exception e) {
            Log.l(this, e.getMessage());
            Log.l(this, "Pojo!!!!!!!");
            new Pojo(getClass(), json).gen().toLog();
        }
    }

    /**
     * 세션이 만료되었는가?
     *
     * @param json
     * @return
     */
    private boolean isSessionOut(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            boolean isSessionOut = jsonObject.getInt("resultCode") == 999;
            if (isSessionOut) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 프로그래스바 숨기기
     */
    private void hideProgress() {
    }

    @Override
    protected void error(Exception error) {
        super.error(error);
        success = false;
    }

    private JsonDeserializer<Date> deserializer_date = new JsonDeserializer<Date>() {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonNull() || json.getAsString().length() <= 0)
                return null;

            // Log.l(json.getAsString());
            try {
                return SDF.yyyymmddhhmmss_1.parseDate(json.getAsString());
            } catch (Exception e) {
                return null;
            }
        }
    };
    private JsonDeserializer<Boolean> deserializer_boolean = new JsonDeserializer<Boolean>() {
        public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return !json.isJsonNull() && json.getAsString().equals("Y");
        }

        ;
    };
    private JsonDeserializer<ArrayList<String>> deserializer_stringarraylist = new JsonDeserializer<ArrayList<String>>() {
        public ArrayList<String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonNull())
                return null;

            ArrayList<String> list = new ArrayList<>();
            JsonArray ja = json.getAsJsonArray();
            for (JsonElement je : ja) {
                String spf_file = je.getAsJsonObject().get("spf_file").getAsString();
                // Log.l(spf_file);
                list.add(spf_file);
            }
            return list;
        }
    };

    public void setUrl(String url) {

        this.url = url;
    }

}
