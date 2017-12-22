package com.suyong.kakaobot.net.model;

import com.suyong.kakaobot.net.util.SDF;
import com.suyong.kakaobot.net.base.BFEnty;

import java.util.List;


/**
 * Created by 이주영 on 2016-11-30.
 */

public class FoodBookList extends BFEnty {
    public FoodBookList() {
        setUrl("http://t.bodyfriend.co.kr:8070/restaurant/sidedish/api/list.json");
        setParam("saleDate", SDF.yyyymmdd_1.now());

    }

    public Data data;

    public static class Data {
        public int resultCode;
        public List<resultData> resultData;

        public class resultData {
            public int seq;
            public String sNm;
            public String sContent;
            public String regDate;
            public int curPage;
            public int pageSize;
            public int totalCnt;
            public int applyCount;
            public String saleDate;
            public String sCount;

            public boolean isCheck = false;
        }
    }

}
