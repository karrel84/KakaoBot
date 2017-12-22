package com.suyong.kakaobot.net.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.suyong.kakaobot.net.util.Log;
import com.suyong.kakaobot.net.util.SDF;
import com.suyong.kakaobot.net.base.BFEnty;
import com.suyong.kakaobot.net.base.NetConst;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Created by 이주영 on 2016-03-11.
 */
public class CarteListByDate extends BFEnty {

    private String url = "restaurant/api/CarteListByDate.json";

    public CarteListByDate(String startDate, String endDate) {
        setEnableProgress(false);
        setUrl(url);
        setParam(
                "startDate", startDate
                , "endDate", endDate
                , "type", "");
    }

    @Override
    protected void parse(String json) {
        super.parse(json);

        if (data == null) return;
        if (data.resultData == null) return;

        List<Data.resultData> tmpData = new ArrayList<>();
        for (int i = data.resultData.size() - 1; i >= 0; i--) {
            tmpData.add(data.resultData.get(i));
        }
        data.resultData = tmpData;

        for (Data.resultData d : data.resultData) {
            // 이미지 주소 보정
            for (Data.resultData.imgList img : d.imgList) {
                img.imgSrc = NetConst.host + img.imgSrc;
                // img변수에 이미지 주소 하나만 담는다.
                if (d.img == null || d.img.isEmpty()) d.img = img.imgSrc;
            }

            d.orgContent = d.content;

            // 식단표 보정
            String[] strs = d.content.split("\r\n"); // 식단표에 도트 추가
            String content = "";
            for (String str : strs) {
                if (content.isEmpty()) {
                    content += str;
                } else {
                    content += String.format(" · %s", str);
                }
            }
            d.content = content;

            d.isMark = isCurrentTime(d.date, d.type.equalsIgnoreCase("LUNCH"));

        }
    }

    public Data data;

    public static class Data {
        public List<resultData> resultData;

        public static class resultData implements Parcelable {
            public String date;
            public String type;
            public int seq;
            public String content;
            public String orgContent;
            public List<imgList> imgList;
            public String img;
            public boolean isMark;

            @Override
            public String toString() {
                return "resultData{" +
                        "date='" + date + '\'' +
                        ", type='" + type + '\'' +
                        ", seq=" + seq +
                        ", content='" + content + '\'' +
                        ", orgContent='" + orgContent + '\'' +
                        ", imgList=" + imgList +
                        ", img='" + img + '\'' +
                        ", isMark=" + isMark +
                        '}';
            }

            protected resultData(Parcel in) {
                date = in.readString();
                type = in.readString();
                seq = in.readInt();
                orgContent = in.readString();
                content = in.readString();
                img = in.readString();
            }

            public static final Creator<resultData> CREATOR = new Creator<resultData>() {
                @Override
                public resultData createFromParcel(Parcel in) {
                    return new resultData(in);
                }

                @Override
                public resultData[] newArray(int size) {
                    return new resultData[size];
                }
            };

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(date);
                dest.writeString(type);
                dest.writeInt(seq);
                dest.writeString(orgContent);
                dest.writeString(content);
                dest.writeString(img);
            }

            public static class imgList implements Parcelable {
                public int seq;
                public int carteSeq;
                public String imgSrc;

                protected imgList(Parcel in) {
                    seq = in.readInt();
                    carteSeq = in.readInt();
                    imgSrc = in.readString();
                }

                public static final Creator<imgList> CREATOR = new Creator<imgList>() {
                    @Override
                    public imgList createFromParcel(Parcel in) {
                        return new imgList(in);
                    }

                    @Override
                    public imgList[] newArray(int size) {
                        return new imgList[size];
                    }
                };

                @Override
                public String toString() {
                    return "imgList{" +
                            "seq=" + seq +
                            ", carteSeq=" + carteSeq +
                            ", imgSrc='" + imgSrc + '\'' +
                            '}';
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    dest.writeInt(seq);
                    dest.writeInt(carteSeq);
                    dest.writeString(imgSrc);
                }
            }
        }

        public int resultCode;
    }

//    "resultData": [
//    {
//        "date": "2016-07-01",
//            "type": "LUNCH",
//            "seq": 163,
//            "content": "잡곡밥\/현미밥\r\n게살스프\r\n양장피\r\n메밀전병\r\n비엔나파인애플볶음\r\n도라지생채\r\n김치\r\n자스민차",
//            "imgList": [
//        {
//            "seq": 63,
//                "carteSeq": 163,
//                "imgSrc": "\/tdbf\/restaurant\/20160701\/1467344447635.jpg"
//        }
//        ]
//    },
//    {
//        "date": "2016-07-01",
//            "type": "DINNER",
//            "seq": 164,
//            "content": "잡곡밥\/현미밥\r\n미역국\r\n닭강정\r\n유부초밥\r\n메밀막국수\r\n치킨무\r\n김치\r\n추가반찬",
//            "imgList": []
//    },
//    {
//        "date": "2016-07-04",
//            "type": "LUNCH",
//            "seq": 165,
//            "content": "잡곡밥\/현미밥\r\n콩나물냉국\r\n해물찜\r\n두부구이&양념장\r\n묵말랭이볶음\r\n얼갈이양파무침\r\n김치\r\n현미녹차",
//            "imgList": [
//        {
//            "seq": 64,
//                "carteSeq": 165,
//                "imgSrc": "\/tdbf\/restaurant:at (NetRequest.java:217)
//            W: >>,들와:,\/20160704\/1467601047531.jpg"
//        }
//        ]
//    },
//    {
//        "date": "2016-07-04",
//            "type": "DINNER",
//            "seq": 166,
//            "content": "잡곡밥\/현미밥\r\n청국장찌개\r\n쇠고기편육무침\r\n메추리알감자조림\r\n다시다튀각\r\n무나물\r\n김치\r\n추가반찬",
//            "imgList": []
//    },
//    {
//        "date": "2016-07-05",
//            "type": "LUNCH",
//            "seq": 167,
//            "content": "잡곡밥\/현미밥\r\n김치국\r\n고등어조림\r\n버섯전\r\n마늘쫑볶음\r\n오이생채\r\n김치\r\n감주스",
//            "imgList": [
//        {
//            "seq": 65,
//                "carteSeq": 167,
//                "imgSrc": "\/tdbf\/restaurant\/20160705\/1467687576420.jpg"
//        }
//        ]
//    },
//    {
//        "date": "2016-07-05",
//            "type": "DINNER",
//            "seq": 168,
//            "content": "잡곡밥\/현미밥\r\n얼큰무채국\r\n적어구이\r\n간장비빔국수\r\n브로컬리숙회\r\n쑥갓두부나물\r\n김치\r\n추가반찬",
//            "imgList": []
//    },
//    {
//        "date": "2016-07-06",
//            "type": "LUN:at (NetRequest.java:217)
//        W: >>,들와:,CH",
//        "seq": 169,
//            "content": "잡곡밥\/현미밥\r\n물냉면\r\n닭가슴살냉채\r\n단호박견과찜\r\n미나리나물\r\n과일\r\n김치\r\n숭늉",
//            "imgList": [
//        {
//            "seq": 66,
//                "carteSeq": 169,
//                "imgSrc": "\/tdbf\/restaurant\/20160706\/1467774522867.jpg"
//        },
//        {
//            "seq": 67,
//                "carteSeq": 169,
//                "imgSrc": "\/tdbf\/restaurant\/20160706\/1467774398519.jpg"
//        }
//        ]
//    },
//    {
//        "date": "2016-07-06",
//            "type": "DINNER",
//            "seq": 170,
//            "content": "잡곡밥\/현미밥\r\n우동국물\r\n등심돈까스&소스\r\n모닝빵샌드위치\r\n구운야채샐러드\r\n할라피뇨\r\n김치\r\n추가반찬",
//            "imgList": []
//    },
//    {
//        "date": "2016-07-07",
//            "type": "LUNCH",
//            "seq": 171,
//            "content": "김치볶음밥&계란\r\n유부주머니전골\r\n모듬튀김\r\n떡볶이\r\n단무지\r\n도시락김\r\n김치\r\n수제요거트",
//            "imgList": [
//        {
//            "s:at (NetRequest.java:217)
//            W: >>,들와:,eq": 68,
//            "carteSeq": 171,
//                "imgSrc": "\/tdbf\/restaurant\/20160707\/1467860451168.jpg"
//        }
//        ]
//    },
//    {
//        "date": "2016-07-07",
//            "type": "DINNER",
//            "seq": 172,
//            "content": "잡곡밥\/현미밥\r\n미역국\r\n오리주물럭\r\n양배추쌈&쌈장\r\n명엽채볶음\r\n참나물겉절이\r\n김치\r\n추가반찬",
//            "imgList": []
//    },
//    {
//        "date": "2016-07-08",
//            "type": "LUNCH",
//            "seq": 173,
//            "content": "잡곡밥\/현미밥\r\n호박잎된장국\r\n돼지고기숙주볶음\r\n날치알계란찜\r\n야채참치\r\n더덕장아찌\r\n김치\r\n청포도식초",
//            "imgList": []
//    },
//    {
//        "date": "2016-07-08",
//            "type": "DINNER",
//            "seq": 174,
//            "content": "111111잡곡밥\/현미밥\r\n사골떡만두국\r\n충무김밥\r\n오징어무침\r\n멸치볶음\r\n깻순나물\r\n김치\r\n추가반찬",
//            "imgList": []
//    }
//    ],

    /**
     * 현재 시간인가?
     * 라벨표기를 위한 값
     */
    private boolean isCurrentTime(String str, boolean isLunch) {
        // 오늘인가?
        boolean isToday = str.equals(SDF.yyyymmdd_1.now());
        // 오늘이면서 점심인가?

        Log.d("************************************");
        Log.d("str => " + str);
        Log.d("today => " + SDF.yyyymmdd_1.now());

        int i = Calendar.getInstance().get(Calendar.AM_PM);
        boolean isTime = false;
        if (i == Calendar.AM && isLunch) {
            isTime = true;
        } else if (i == Calendar.PM && !isLunch) {
            isTime = true;
        }
        return isToday && isTime;
    }
}
