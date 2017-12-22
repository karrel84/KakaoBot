package com.suyong.kakaobot.net.util;

import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

public class Pojo {
    private String name;
    private String json;
    private String pojo;

    public Pojo(Class<?> cls, String json) {
        this(cls.getSimpleName(), json);
    }

    public Pojo(String json) {
        this(new Exception().getStackTrace()[1].getClassName(), json);
    }

    public Pojo(String name, String json) {
        this.name = name;
        this.json = json;
    }

    public Pojo gen() {
        StringBuilder sb = new StringBuilder();
        try {
            JSONObject jo = new JSONObject(json);
            sb.append("public Data data;");
            generateClass(sb, "Data", jo);
            sb.insert("public Data data;public ".length(), "static ");
        } catch (JSONException ee) {
            ee.printStackTrace();
        }

        this.pojo = sb.toString();
        return this;
    }

    public void toLog() {
        System.out.println(pojo);
    }

    public void toFile() {
        try {

            final File dirPath = new File(Environment.getExternalStorageDirectory(), "/_flog");
            if (!dirPath.exists())
                dirPath.mkdirs();

            final File pojo = new File(dirPath, name + ".java");
            if (!pojo.exists()) {
                try {
                    pojo.createNewFile();
                } catch (IOException e) {
                }
            }

            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(pojo));
            writer.write(this.pojo);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return pojo;
    }

    private void generateClass(StringBuilder sb, String name, Object value) {
        sb.append("public class " + name + "{");
        JSONObject jo = (JSONObject) value;
        Iterator<String> it = jo.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object o = jo.opt(key);
            if (o instanceof String)
                sb.append("public String " + key + ";");
            if (o instanceof Integer)
                sb.append("public int " + key + ";");
            if (o instanceof Long)
                sb.append("public long " + key + ";");
            if (o instanceof Double)
                sb.append("public double " + key + ";");
            if (o instanceof JSONArray)
                generateArray(sb, key, o);
            if (o instanceof JSONObject) {
                sb.append("public " + key + " " + key + ";");
                generateClass(sb, key, o);
            }
        }
        sb.append("}");
    }

    private void generateArray(StringBuilder sb, String name, Object value) {
        JSONArray ja = (JSONArray) value;
        for (int i = 0; i < 1; i++) {
            Object o = ja.opt(i);
            if (o instanceof String)
                sb.append("public List<String> " + name + ";");
            if (o instanceof Integer)
                sb.append("public List<Integer> " + name + ";");
            if (o instanceof Long)
                sb.append("public List<Long> " + name + ";");
            if (o instanceof Double)
                sb.append("public List<Double> " + name + ";");
            if (o instanceof JSONArray)
                generateArray(sb, name, o);
            if (o instanceof JSONObject) {
                sb.append("public List<" + name + "> " + name + ";");
                generateClass(sb, name, o);
            }
        }
    }
}