package com.suyong.kakaobot;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.suyong.kakaobot.net.model.CarteListByDate;
import com.suyong.kakaobot.net.model.FoodBookList;
import com.suyong.kakaobot.net.base.Net;
import com.suyong.kakaobot.net.util.SDF;
import com.suyong.kakaobot.script.JSScriptEngine;
import com.suyong.kakaobot.script.PythonScriptEngine;

import java.util.ArrayList;

/*
import org.python.core.PyBoolean;
import org.python.core.PyObject;
import org.python.core.PyString;*/


public class KakaoTalkListener extends NotificationListenerService {
    private static final String KAKAOTALK_PACKAGE = "com.kakao.talk";
    private static ArrayList<Session> sessions = new ArrayList<>();
    private static ArrayList<JSScriptEngine> jsEngines = new ArrayList<>();
    private static ArrayList<PythonScriptEngine> pythonEngines = new ArrayList<>();
    private static Context context;

    private final String TAG = "KakaoBot/Listener";
    private Type.Message mMessage;

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) { // @author ManDongI
        super.onNotificationPosted(statusBarNotification);

        Log.d(TAG, "name: " + statusBarNotification.getPackageName());

        if (statusBarNotification.getPackageName().equals(KAKAOTALK_PACKAGE)) {
            Notification.WearableExtender extender = new Notification.WearableExtender(statusBarNotification.getNotification());

            Log.d(TAG, "Kakao!");

            for (Notification.Action act : extender.getActions()) {

                if (act.getRemoteInputs() != null && act.getRemoteInputs().length > 0) {
                    String title = statusBarNotification.getNotification().extras.getString("android.title");
                    Object index = statusBarNotification.getNotification().extras.get("android.text");

                    Type.Message message = parsingMessage(title, index);

                    context = getApplicationContext();

                    Session session = new Session();
                    session.session = act;
                    session.message = message.message;
                    session.sender = message.sender;
                    session.room = message.room;

                    Log.d("KakaoBot/Listener", "message : " + message);
                    Log.d("KakaoBot/Listener", "act.title : " + act.title);

                    sessions.add(session);

                    for (JSScriptEngine engine : jsEngines) {
                        try {
                            engine.invoke("talkReceivedHook", message.room, message.message, message.sender, !message.room.equals(message.sender));
                        } catch (final Exception e) {
                            MainActivity.UIThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, e.toString().split("SCRIPTSPLITTAG")[1], Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    for (PythonScriptEngine engine : pythonEngines) {
                        /*engine.invokeFunction("talkReceivedHook", new PyObject[]{
                                new PyString(message.room),
                                new PyString(message.message),
                                new PyString(message.sender),
                                new PyBoolean(!message.room.equals(message.sender))
                        });*/
                        Log.d("KakaoBot/Listener", "Python Received! " + message.message);
                    }


                    // 내가 원하는 방에 커스텀 메세지를 보낸다.
                    sendCustomMessage(act, message);
                }
            }
        }
    }

    private void sendCustomMessage(Notification.Action act, Type.Message message) {
        if (act.title.toString().contains("bf개발팀")) {
            mMessage = message;
            if (message.message.equals("@?")) {
                String msg =
                        "뜨뜨르뜨뜨띠 영봇 명령어" +
                                "\n@점심 : 오늘 점심"
                                + "\n@저녁 : 오늘 저녁"
                                + "\n@반찬 : 이번주 반찬";
                send(mMessage.room, msg);
            } else if (message.message.equals("@점심")) {
                Net.async(new CarteListByDate(SDF.yyyymmdd_1.now(), SDF.yyyymmdd_1.now())).setOnNetResponse(onListener);
            } else if (message.message.equals("@저녁")) {
                Net.async(new CarteListByDate(SDF.yyyymmdd_1.now(), SDF.yyyymmdd_1.now())).setOnNetResponse(onListener);
            } else if (message.message.equals("@반찬")) {
                Net.async(new FoodBookList()).setOnNetResponse(onFoodListener);
            } else if (message.message.equals("@오진주")) {
                send(mMessage.room, "똥멍청이");
            } else if (message.message.equals("@이주영")) {
                send(mMessage.room, "뜨르뜨띠뜨띠 존경하는 저의 창조주님");
            }
        }
    }

    public static void send(String room, String message) throws IllegalArgumentException { // @author ManDongI
        Notification.Action session = null;

        for (Session i : sessions) {
            if (i.room.equals(room)) {
                session = i.session;

                break;
            }
        }

        if (session == null) {
            throw new IllegalArgumentException("Can't find the room");
        }

        Intent sendIntent = new Intent();
        Bundle msg = new Bundle();
        for (RemoteInput inputable : session.getRemoteInputs())
            msg.putCharSequence(inputable.getResultKey(), message);
        RemoteInput.addResultsToIntent(session.getRemoteInputs(), sendIntent, msg);

        try {
            session.actionIntent.send(context, 0, sendIntent);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Session> getSessions() {
        return sessions;
    }

    public static JSScriptEngine[] getJsEngines() {
        return jsEngines.toArray(new JSScriptEngine[0]);
    }

    public static PythonScriptEngine[] getPythonEngines() {
        return pythonEngines.toArray(new PythonScriptEngine[0]);
    }

    private Type.Message parsingMessage(String title, Object index) {
        Type.Message result = new Type.Message();
        result.room = title;

        if (index instanceof String) {
            result.sender = title;
            result.message = (String) index;
        } else {
            String html = Html.toHtml((SpannableString) index);
            result.sender = Html.fromHtml(html.split("<b>")[1].split("</b>")[0]).toString();
            result.message = Html.fromHtml(html.split("</b>")[1].split("</p>")[0].substring(1)).toString();
        }

        return result;
    }

    public static void addJsEngine(JSScriptEngine engine) throws Exception {
        engine.run();

        jsEngines.add(engine);
    }

    public static void addPythonEngine(PythonScriptEngine engine) throws Exception {
        //engine.execute();

        pythonEngines.add(engine);
    }

    public static void clearEngine() {
        jsEngines.clear();
        pythonEngines.clear();
        Log.d("KakaoBot/Listener", jsEngines.size() + " " + pythonEngines.size());
    }

    public class Session {
        public Notification.Action session;
        public String room;
        public String sender;
        public String message;
    }

    private Net.OnNetResponse<CarteListByDate> onListener = new Net.OnNetResponse<CarteListByDate>() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }

        @Override
        public void onResponse(CarteListByDate response) {
            try {
                for (CarteListByDate.Data.resultData d : response.data.resultData) {
                    if (mMessage.message.equals("@점심") && d.type.equals("LUNCH")) {
                        send(mMessage.room, String.format("점심 \n%s", d.content));
                        break;
                    }
                    if (mMessage.message.equals("@저녁") && d.type.equals("DINNER")) {
                        send(mMessage.room, String.format("저녁 \n%s", d.content));
                        break;
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
                send(mMessage.room, e.getMessage());
            }
        }
    };

    private Net.OnNetResponse<FoodBookList> onFoodListener = new Net.OnNetResponse<FoodBookList>() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }

        @Override
        public void onResponse(FoodBookList response) {
            try {
                String s = "";
                for (FoodBookList.Data.resultData d : response.data.resultData) {
                    s += d.sNm + "\n";
                }
                send(mMessage.room, s);


            } catch (Exception e) {
                e.printStackTrace();
                send(mMessage.room, e.getMessage());
            }
        }
    };
}
