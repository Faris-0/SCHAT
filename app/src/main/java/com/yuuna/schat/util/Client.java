package com.yuuna.schat.util;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Client {

    public static final String BASE_URL = "http://127.0.0.1/schat/";
    public static final String BASE_PHOTO = BASE_URL + "photo/";
    private static final OkHttpClient client = new OkHttpClient();

    public interface OKHttpNetwork {
        void onSuccess(String response);
        void onFailure(IOException e);
    }

    // Request without Body (JSON)
    public void getOkHttpClient(String url, OKHttpNetwork callback) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "SCHAT-App/1.0")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String stackTrace = sw.toString();
                Log.e("SCHAT-StackTrace", stackTrace);
                callback.onFailure(e);
            }
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) callback.onSuccess(response.body().string());
            }
        });
    }

    // Request with Body (JSON)
    public void getOkHttpClient(String url, String json, OKHttpNetwork callback) {
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "SCHAT-App/1.0")
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String stackTrace = sw.toString();
                Log.e("SCHAT-StackTrace", stackTrace);
                callback.onFailure(e);
            }
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) callback.onSuccess(response.body().string());
            }
        });
    }
}
