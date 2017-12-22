package com.suyong.kakaobot.net.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public enum SDF {

    /**
     * Wed, 14 Jan 2015 05:46:36 GMT
     */
    _long("EEE, d MMM yyyy HH:mm:ss z", Locale.US) //
    , yy("yy") //
    , yyyymm2("yyyy년 MM월") //
    , yyyy("yyyy") //
    , yyyy__("yyyy년") //

    , weekofmonth("W") //

    , mmdd__("MM월dd일") //
    , mmdd_2("MM.dd") //

    , mm("MM") //
    , m__("M월") //
    , mm__("MM월") //

    , d("d") //
    , dd("dd") //
    , dd__("dd일") //

    , EEE_US("EEE", Locale.US) //
    , EEEE_US("EEEE", Locale.US) //
    , E("E") //
    , E__("E요일") //

    , emdhs("(E)M/d HH:mm") //
    , mdeahs("M/d(E) a hh:mm") //

    , mde("M/d(E)") //

    , yyyy_mmdd("yyyy\nMM.dd") //
    , yyyy__mmdd("yyyy.\nMM.dd") //

    , yyyymmddE__("yyyy년 MM월 dd일 E요일") //
    , yyyymmddE__2("yyyy년 MM월 dd일(E)") //

    , mmddE("MM월 dd일\nE요일") //

    , yyyymmdd("yyyyMMdd") //
    , yyyymmdd_("yyyy/MM/dd") //
    , yyyymmdd__("yyyy년 MM월 dd일") //
    , yyyymmdd_1("yyyy-MM-dd") //
    , yyyymmdd_2("yyyy.MM.dd") //
    , yyyymmdd_3("yy.MM.dd") //

    , yyyymm("yyyy.MM") //1
    , yyyymm4("yyyy. MM") //1
    , yyyymm3("yyyy-MM") //

    , yyyymmddhhmm("yyyyMMddHHmm") //
    , yyyymmddhhmm_1("yyyy-MM-dd HH:mm") //

    , yyyymmddahmm_("yyyy/MM/dd a hh:mm") //

    , yyyymmddhhmm__("yyyy년 MM월 dd일 HH:mm") //

    , yyyymmddhhmmss("yyyyMMddHHmmss") //
    , yyyymmddhhmmss_("yyyy/MM/dd HH:mm:ss") //
    , yyyymmddhhmmss_1("yyyy-MM-dd HH:mm:ss") //
    , yyyymmddhhmmss_2("yyyy.MM.dd HH:mm:ss") //
    , yyyymmddhhmmss_3("yyyy-MM-dd hh:mm aa") //

    , yyyymmddhhmmsssss_("yyyy/MM/dd HH:mm:ss.SSS") //
    , yyyymmddhhmmsssss_1("yyyy-MM-dd HH:mm:ss.SSS") //

    , mmddhhmm__("MM월dd일 HH:mm") //
    , mmddhhmm_("MM/dd HH:mm") //

    , hhmmss("HHmmss") //
    , hhmmss2("HH:mm:ss") //
    , ahhmm("a hh:mm") //

    , hhmm("HHmm") //
    , hhmm_("HH:mm") //

    , mmss("mm:ss") //

    , ms("m:s") //
    , ahm("a h시 m분")//
    , HH("HH")//
    , mm2("mm");

    public SimpleDateFormat sdf;
    public String format;

    SDF(String format) {
        this(format, Locale.getDefault());
    }

    SDF(String format, Locale locale) {
        this.format = format;
        sdf = new SimpleDateFormat(format, locale);
    }

    public String format(Date date) {
        if (date == null)
            return "";
        return sdf.format(date);
    }

    public String format(long milliseconds) {
        return format(new Date(milliseconds));
    }

    public String now() {
        return format(System.currentTimeMillis());
    }

    public Date parseDate(String date) throws ParseException {
        return sdf.parse(date);
    }

    public long parse(String date) throws ParseException {
        return sdf.parse(date).getTime();
    }

    public long parse(String date, long default_date) {
        try {
            return sdf.parse(date).getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}