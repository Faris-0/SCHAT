package com.yuuna.schat.util;

import static com.yuuna.schat.util.Client.BASE_URL;
import static com.yuuna.schat.util.schatService.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.RemoteInput;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class schatBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new Task(goAsync(), intent, context).execute();
    }

    private static class Task extends AsyncTask<String, Integer, String> {

        private final PendingResult pendingResult;
        private final Intent intent;
        private final Context context;

        private ArrayList<JSONObject> jsonObjectArrayList1;
        private ArrayList<JSONObject> jsonObjectArrayList2;

        private Task(PendingResult pendingResult, Intent intent, Context context) {
            this.pendingResult = pendingResult;
            this.intent = intent;
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            String message = String.valueOf(getMessageText(intent));
            String id = intent.getStringExtra("id");
            Integer send = intent.getIntExtra("send", 0);
            Integer sender = intent.getIntExtra("sender", 0);
            String chat = intent.getStringExtra("chat");

            try {
                JSONArray jsonArray = new JSONArray(chat);
                if (jsonObjectArrayList1 == null) jsonObjectArrayList1 = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) jsonObjectArrayList1.add(jsonArray.getJSONObject(i));
                JSONObject object = new JSONObject()
                        .put("name", "Anda")
                        .put("chat", message)
                        .put("time", System.currentTimeMillis()/1000);
                if (jsonObjectArrayList2 == null) jsonObjectArrayList2 = new ArrayList<>();
                jsonObjectArrayList2.add(object);
                jsonObjectArrayList1.addAll(jsonObjectArrayList2);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (intent.getAction().equals("READ")) setView(id, send, sender, null);
            else if (intent.getAction().equals("REPLY")) sendChat(message, id, send, sender, jsonObjectArrayList1);
            return null;
        }

        private void sendChat(String message, String id, Integer send, Integer sender, ArrayList<JSONObject> jsonObjectArrayList) {
            String send_chat = "{\"request\":\"send_chat\",\"data\":{\"id\":\""+id+"\",\"chat\":\""+message+"\",\"send\":\""+send+"\"}}";
            JsonObject jsonObject = JsonParser.parseString(send_chat).getAsJsonObject();
            try {
                new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                    @Override
                    public void onSuccess(String response) {
                        // Response
                        try {
                            if (new JSONObject(response).getBoolean("status")) setView(id, send, sender, jsonObjectArrayList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void setView(String id, Integer send, Integer sender, ArrayList<JSONObject> jsonObjectArrayList) {
            String message_detail = "{\"request\":\"edit_view\",\"data\":{\"id\":\""+id+"\",\"send\":\""+send+"\"}}";
            JsonObject jsonObject = JsonParser.parseString(message_detail).getAsJsonObject();
            try {
                new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                    @Override
                    public void onSuccess(String response) {
                        // Response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
//                                if (intent.getAction().equals("READ")) notification(context, null, null, null, sender, null, true);
//                                else if (intent.getAction().equals("REPLY")) notification(context, "Anda", id, send, sender, jsonObjectArrayList, false);
                                if (intent.getAction().equals("READ")) notification(context, null, null, sender, null, true);
                                else if (intent.getAction().equals("REPLY")) notification(context, id, send, sender, jsonObjectArrayList, false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private CharSequence getMessageText(Intent intent) {
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (remoteInput != null) return remoteInput.getCharSequence("key_text_reply");
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Must call finish() so the BroadcastReceiver can be recycled.
            pendingResult.finish();
        }
    }
}
