package com.suyong.kakaobot.rellbot;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.suyong.kakaobot.KakaoTalkListener;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

/**
 * Created by kimmihye on 2018. 1. 10..
 */

public class TesterSender {
    private static TesterSender mInstance;
    private final Context mContext;
    private AIDataService aiDataService;
    private AIRequest aiRequest;

    private static final String clientAccessToken = "6a9d97f0c5734e82aa7f393e93c6a1ee";

    public static TesterSender getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TesterSender(context);
        }
        return mInstance;
    }

    public TesterSender(Context context) {
        mContext = context;

        setupDialogFlow();
    }

    private void setupDialogFlow() {
        final AIConfiguration config = new AIConfiguration(clientAccessToken,
                AIConfiguration.SupportedLanguages.Korean,
                AIConfiguration.RecognitionEngine.System);

        aiDataService = new AIDataService(mContext, config);

        aiRequest = new AIRequest();
    }

    public void execute(final KakaoTalkListener.Session session) {
        if (session.message.startsWith("@")) {
            session.message = session.message.substring(1);
        } else {
            return;
        }

        if (session.message.length() == 0) {
            session.message = "렐봇";
        }

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
                    KakaoTalkListener.send(session.room, speech);
                }
            }
        }.execute(aiRequest);
    }
}
