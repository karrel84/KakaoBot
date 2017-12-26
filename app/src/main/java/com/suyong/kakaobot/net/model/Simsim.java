package com.suyong.kakaobot.net.model;

/**
 * Created by Rell on 2017. 12. 26..
 */

public class Simsim {
    public String response;
    public String id;
    public int result;
    public String msg;


    @Override
    public String toString() {
        return "Simsim{" +
                "id='" + id + '\'' +
                ", msg='" + msg + '\'' +
                ", response='" + response + '\'' +
                ", result=" + result +
                '}';
    }
}
