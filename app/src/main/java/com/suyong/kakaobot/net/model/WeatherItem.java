package com.suyong.kakaobot.net.model;

import com.suyong.kakaobot.net.base.BFEnty;

/**
 * Created by Rell on 2017. 12. 22..
 */

public class WeatherItem extends BFEnty {
    public WeatherItem() {
        setUrl("http://www.kma.go.kr/weather/forecast/mid-term-rss3.jsp?stnId=109");

    }
}
