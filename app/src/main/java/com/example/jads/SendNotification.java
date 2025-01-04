package com.example.jads;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendNotification {

    private static final String PROJECT_ID = "jads-4eb19";
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send";
    private static final String TAG = "SendNotification";

    public void sendPushNotification(String notificationTitle, String notificationBody, String fcmToken) {
        try {
            OkHttpClient client = new OkHttpClient();
            // notification payload
            String jsonPayload = String.format("{\"message\":{\"token\":\"%s\",\"notification\":\"%s\",\"body\":\"%s\"}}}",
                    fcmToken, notificationTitle, notificationBody);

            RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf=8"));
            // access token
            String accessToken = AccessToken.getAccessToken();
            Log.d("ACCESSToken", accessToken);
            Request request = new Request.Builder()
                    .url(FCM_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer" + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // request send
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Notification successfully sent");
                    } else {
                        Log.d(TAG, response.code() + " " + response.message());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
