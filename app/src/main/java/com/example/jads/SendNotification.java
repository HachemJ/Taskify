package com.example.jads;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendNotification {

    private static final String PROJECT_ID = "jads-4eb19";
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send";
    private static final String TAG = "SendNotification";

    public void sendPushNotification(Context context, String notificationTitle, String notificationBody, String fcmToken) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                // Prepare the JSON payload
                String jsonPayload = String.format(
                        "{\"message\":{\"token\":\"%s\",\"notification\":{\"title\":\"%s\",\"body\":\"%s\"}}}",
                        fcmToken, notificationTitle, notificationBody
                );
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String token = task.getResult();
                                Log.d("FCM Token", token);
                            }
                        });
                Log.d(TAG, "JSON Payload: " + jsonPayload);

                // Generate access token
                String accessToken = AccessToken.getAccessToken(context);
                if (accessToken == null || accessToken.isEmpty()) {
                    Log.e(TAG, "Access token is null or empty");
                    return;
                }
                Log.d(TAG, "Access Token: " + accessToken);

                // Create request body
                RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));

                // Build the HTTP request
                Request request = new Request.Builder()
                        .url(FCM_URL)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json")
                        .build();

                // Execute the request
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e(TAG, "Request failed: " + e.getMessage(), e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Notification successfully sent.");
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "No error body";
                            Log.e(TAG, "Error sending notification: " + response.code() + " " + response.message() + " " + errorBody);
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Exception in sendPushNotification: " + e.getMessage(), e);
            }
        }).start();
    }
}
