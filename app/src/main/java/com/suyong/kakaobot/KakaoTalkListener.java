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


                    // 내가 원하는 방에 커스텀 메세지를 보낸다.
                    if (session.room.equals("이주영")) { // 내방, Tester의 메세지이다
                        messageJuyoung(session);
                    }

                    if (session.room.equals("bf개발팀")) { // 회사 개발팀방
                        messageBfTeam(session);
                    }

                    if (session.room.equals("안드로이드 개발 Q&A및 팁")) { // kakao 안드로이드 방

                    }

                }
            }
        }
    }

    private void messageBfTeam(final Session session) {
        if (!session.message.startsWith("@")) {
            return;
        }
        final AIConfiguration config = new AIConfiguration("6a9d97f0c5734e82aa7f393e93c6a1ee",
                AIConfiguration.SupportedLanguages.Korean,
                AIConfiguration.RecognitionEngine.System);

        final AIDataService aiDataService = new AIDataService(getApplicationContext(), config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(session.message);

        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(AIResponse response) {
                Log.d("onPostExecute", "onPostExecute");
                if (response != null) {
                    final Result result = response.getResult();
                    String speech = result.getFulfillment().getSpeech();
                    speech = speech.replace("name", session.sender);
                    send(session.room, speech);
                }
            }
        }.execute(aiRequest);


    }

    private void messageJuyoung(final Session session) {
        if (!session.message.startsWith("@")) {
            return;
        }

        final AIConfiguration config = new AIConfiguration("6a9d97f0c5734e82aa7f393e93c6a1ee",
                AIConfiguration.SupportedLanguages.Korean,
                AIConfiguration.RecognitionEngine.System);

        final AIDataService aiDataService = new AIDataService(getApplicationContext(), config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(session.message);

        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(AIResponse response) {
                Log.d("onPostExecute", "onPostExecute");
                if (response != null) {
                    final Result result = response.getResult();
                    String speech = result.getFulfillment().getSpeech();
                    speech = speech.replace("name", session.sender);
                    send(session.room, speech);
                }
            }
        }.execute(aiRequest);


    }

    private void getWeather2() {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = f.newDocumentBuilder();

            Document xmlDoc = null;
            String url = "http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=1168065500";
            xmlDoc = parser.parse(url);

            Element root = xmlDoc.getDocumentElement();

            String pubDate = root.getElementsByTagName("pubDate").item(0).getTextContent();
            System.out.println("pubDate :" + pubDate);

            String category = root.getElementsByTagName("category").item(0).getTextContent();
            System.out.println("category :" + category);

            NodeList nodeList = root.getElementsByTagName("data");

            StringBuilder builder = new StringBuilder();

            builder.append(category);

            builder.append("\n");
            builder.append(String.format("%s 기준", pubDate));
            builder.append("\n");
            builder.append("\n");

            List<Map> maps = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NodeList childNodes = node.getChildNodes();

                Map<String, String> map = new HashMap<>();
                maps.add(map);
                for (int j = 0; j < childNodes.getLength(); j++) {
                    String name = childNodes.item(j).getNodeName();
                    String value = childNodes.item(j).getTextContent();
                    System.out.println(String.format("name : %s, value : %s", name, value));

                    if (name.equals("hour")) {
                        map.put("hour", value);
                    }
                    if (name.equals("day")) {
                        map.put("day", value);
                    }
                    if (name.equals("temp")) {
                        map.put("temp", value);
                    }
                    if (name.equals("wfKor")) {
                        map.put("wfKor", value);
                    }
                }
            }

            for (Map<String, String> map : maps) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(map.get("day")));
                String msg = String.format("%s %02d시 온도:%s 상태:%s", SDF.mmdd__.format(calendar.getTime()), Integer.parseInt(map.get("hour")), map.get("temp"), map.get("wfKor"));
                builder.append(msg);
                builder.append("\n");
            }
            send(mMessage.room, builder.toString());

        } catch (Exception e) {
            send(mMessage.room, e.getMessage());
        }
    }

    private void getWeather() {
        try {

            Log.e(TAG, "getWeather()");
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = null;
            try {
                parser = f.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

            Document xmlDoc = null;
            String url = "http://www.kma.go.kr/weather/forecast/mid-term-rss3.jsp?stnId=109";
            try {
                xmlDoc = parser.parse(url);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Element root = xmlDoc.getDocumentElement();

            String title = root.getElementsByTagName("title").item(1).getTextContent();
            String content = root.getElementsByTagName("wf").item(0).getTextContent();
            content = content.replaceAll("<br />", "\n");

            String msg = String.format("%s\n\n%s", title, content);
            send(mMessage.room, msg);
        } catch (Exception e) {
            send(mMessage.room, e.getMessage());
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
                send(mMessage.room, "식단이 등록되지 않았습니다.");
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
                send(mMessage.room, "반찬이 등록되지 않았습니다.");
            }
        }
    };

    private Net.OnNetResponse<WeatherItem> onWeatherListener = new Net.OnNetResponse<WeatherItem>() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }

        @Override
        public void onResponse(WeatherItem response) {

        }
    };

    // OkHttp3를 활용하여 구현함
    public String sendSimsim(String text) throws IOException {
        String url = String.format("http://sandbox.api.simsimi.com/request.p?key=e2e2be41-a6d7-44fd-9846-65b2425386d4&lc=en&ft=1.0&text=%s", text);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            System.out.println("json : " + json);

            Gson gson = new Gson();
            Simsim simsim = gson.fromJson(json, Simsim.class);


            return simsim.response;
        } catch (Exception e) {
            return "";
        }
    }
}
