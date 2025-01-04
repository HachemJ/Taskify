package com.example.jads;

import android.content.Context;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;

public class AccessToken {

    private static final String TAG = "AccessToken";
    private static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";

    public static String getAccessToken(Context context) {
        try {
            // Open the JSON file from res/raw/service_account.json
            InputStream inputStream = context.getResources().openRawResource(R.raw.service_account);

            // Create GoogleCredentials from the JSON file
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(Lists.newArrayList(firebaseMessagingScope));

            // Refresh the token if needed
            googleCredentials.refreshIfExpired();

            // Return the access token
            String token = googleCredentials.getAccessToken().getTokenValue();
            Log.d(TAG, "Access token generated successfully: " + token);
            return token;

        } catch (IOException e) {
            Log.e(TAG, "Error obtaining access token: " + e.getMessage(), e);
            return null;
        }
    }
}
