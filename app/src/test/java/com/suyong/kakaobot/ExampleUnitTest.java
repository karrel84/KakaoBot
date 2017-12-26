package com.suyong.kakaobot;

import com.google.gson.Gson;
import com.suyong.kakaobot.net.model.Simsim;

import org.junit.Test;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {

        String text = "hello";
        System.out.println(sendSimsim(text));
    }

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