package com.suyong.kakaobot;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.suyong.kakaobot.net.base.Net;
import com.suyong.kakaobot.net.model.CarteListByDate;
import com.suyong.kakaobot.net.model.FoodBookList;
import com.suyong.kakaobot.net.model.Simsim;
import com.suyong.kakaobot.net.model.WeatherItem;
import com.suyong.kakaobot.net.util.SDF;
import com.suyong.kakaobot.rellbot.BfSender;
import com.suyong.kakaobot.rellbot.KakaoAndroidSender;
import com.suyong.kakaobot.rellbot.TesterSender;
import com.suyong.kakaobot.rellbot.SenderListener;
import com.suyong.kakaobot.script.JSScriptEngine;
import com.suyong.kakaobot.script.PythonScriptEngine;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sun.rmi.log.ReliableLog;

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
                    String room = (String) act.title;
                    session.room = room = room.substring(4, room.length() - 1);

                    Log.d("KakaoBot/Listener", session.toString());

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

                    if (room.equals("bf개발팀")) {
                        BfSender
                                .getInstance(getApplicationContext())
                                .setIdentifier("@")
                                .execute(session);
                    } else if (room.equals("이주영")) {
                        BfSender
                                .getInstance(getApplicationContext())
                                .setIdentifier("")
                                .execute(session);
                    } else if (room.equals("안드로이드 개발 Q&A및 팁")) {
                        if (!message.sender.equals("렐")) return;
                        KakaoAndroidSender.getInstance(getApplicationContext())
                                .setIdentifier("@렐봇")
                                .execute(session);
                    } else {
                        BfSender
                                .getInstance(getApplicationContext())
                                .setIdentifier("")
                                .execute(session);
                    }
                }
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

        @Override
        public String toString() {
            return "Session{" +
                    "session=" + session +
                    ", room='" + room + '\'' +
                    ", sender='" + sender + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
