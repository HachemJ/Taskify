package com.example.jads;

import android.util.Log;

import com.google.common.collect.Lists;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AccessToken {

    private static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";

    public static String getAccessToken() {
        try {
            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"jads-4eb19\",\n" +
                    "  \"private_key_id\": \"b94f7227837ec721d3baa1de489c12b6dd18d6b3\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC2kLEwNagEFoPj\\nakWa/KYiahXMskty2xVPulhsvEGBq2fDfgixG3NCDGQUn75BjcHv8nmM3aA2wtSS\\njXEUp65v+UFVMW7hbfMxpfN2Veat2YHgqbvXX6mKQx1Ug7EgntdFBakjxrezWnp2\\n8AKjETNQGNkKnu24zuBLirL67NWL1ld9JNA95WewulF+7PSLoWwle74d1PlO12aD\\nyCMNZc0G2hFLDIg3UwPfTgH25xbDne0Po08gYYzal4uHAKv8n2kTZCinuK12oOLo\\n01g+YZlHIJZVFKKEgeXn/cAYhG+MMxka9zs43mGfBDaJklJEaiDrZpMdDRHUG8jI\\nAS2M8orpAgMBAAECggEAArfyqenRB8dac56UZpBTj8vUaXkDaAFWKgFEdtE/PBIu\\nC3p+RtdGgJnICO/JOMp554izTnZjmXbBc2TSzCcKqtUwQV4Ck8didmWzuecU9jEs\\nEdXezua4RhXlkANQz50KtJbqFvb+RLGL64B7pMT8gB+8/6XKAW7NmgjFmh7AhIJu\\nnl6WZovmF9uhKJmLz/W83SyooB7fREKMTCtT5wTUAA0Dc2zzypbxZ/32i2BoI49d\\nJ1+E+x3R+DAoBSHMPG7tfLfdwlxsz7evjO2d1Zs852KesmftuDQsWOMPXuqtPjpK\\n9SJn+GDOiEvj3Rppq05pMmomj4jNCzENqsX12/L3gQKBgQDtJQYh07APgFwdmn92\\nnmJ+smzWlszF70/IpRgxkTaIJfFNmOTXD6R+kzmhVSVFHPCwjP0JA0HFd1Mecb5C\\ntMDrtM23qMPFeJmtZwfWHFzV78Uhvfbx0nDruNeOmbNJE4FKht50bsGhK8lScyBa\\nY4yDkp4Y9zAdks/J5/eDraMUSQKBgQDFFLpMbb/4ousDRr6msKLkZWlaYKD0QAVi\\njelmNMXIzkfhxiSlBlTi6SPK3KKLza7pXu2jM9CPsdNxIDhEjCk4Sb7K3Dq3opwV\\nyePBtrW/vcmpXNj7PS1zundrV+2s25tUbyvfUhsmGVpGbxcHTEP4yRjOfQQ6vjBp\\nAOz7ttqBoQKBgQCOUn4PpyAu7XZbPfHpmry6FRNE5lQXJrzXSl3uwZvcsgjT7ULJ\\n31V1sZ4QuS0ZAXliO/tQgWfjvu1fhBwPpLNzwpsD8oEr7mksrH0DfxooOzIwQfYG\\nw6mJjvGvX4c+ADrEI5U2g5IpzEGS/g0Ysk4OWNwRV6jE1utl3ZWqrXcGIQKBgEIQ\\nhbbzH2QosOFDSMu18JThjeR2+d2GzIdSDQhu7lI3HA+KDWSob7I09wOc3HxC8bxX\\nTw/jYamZsch9RX9tALBWwDdehexi/TGsoMWiCuEArOYyMqZdxqvBuSBptEm6wCIY\\ntjtBWcp2uzVbLfwLS8nNF5y4eb97W90HK2nhzPOhAoGBAMZ/xjVmSstLBPBOJECb\\n9K2Jv07bbXeHSiZ/nE7aEzMQHAGNKGU7u6yYwjt+hsbO+H1wEz5kHvkW/caHGbT3\\n53DO/1beExb1AVU8TM6WoPgS0OKYWZ0pwuREK4YeroaNjQfaTBz+4XGIlGGwWGH3\\nn2kJqCTlgOSbjFqdEgG8v5Oy\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-1u3r3@jads-4eb19.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"110603362296812958040\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-1u3r3%40jads-4eb19.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}";

            InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(Lists.newArrayList(firebaseMessagingScope));
            googleCredentials.refreshIfExpired();
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            Log.e("error", e.getMessage());
            return null;
        }
    }
}
