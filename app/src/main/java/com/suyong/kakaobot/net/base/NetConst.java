package com.suyong.kakaobot.net.base;



public class NetConst {

    public static final String HOST_REAL = "http://t.bodyfriend.co.kr:8070/";
    public static final String HOST_TEST_웅찬 = "http://172.30.40.44:8080/"; // 웅찬주임
    public static final String HOST_TEST_민과장님 = "http://172.30.40.19:8080/"; // 민과장님
    public static final String HOST_TEST = HOST_TEST_웅찬;

    public static String host;

    static {
        host = HOST_REAL;
    }
}