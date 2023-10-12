package com.yuuna.schat.util;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Client {

    public static final String BASE_URL = "http://192.168.34.114/schat/";
    public static final String BASE_PHOTO = BASE_URL + "photo/";

    public interface OKHttpNetwork {
        void onSuccess(String response);
        void onFailure(IOException e);
    }

    // Request without Body (JSON)
    public void getOkHttpClient(String url, OKHttpNetwork okHttpCallBack) throws IOException {
        new OkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpCallBack.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) okHttpCallBack.onSuccess(response.body().string());
            }
        });
    }

    // Request with Body (JSON)
    public void getOkHttpClient(String url, String hashMap, OKHttpNetwork okHttpCallBack) throws IOException {
        RequestBody body = RequestBody.create(hashMap, MediaType.parse("application/json; charset=utf-8"));
        new OkHttpClient().newCall(new Request.Builder().url(url).post(body).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                okHttpCallBack.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) okHttpCallBack.onSuccess(response.body().string());
            }
        });
    }
}
