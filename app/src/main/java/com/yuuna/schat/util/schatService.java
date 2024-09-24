package com.yuuna.schat.util;

import static com.yuuna.schat.util.Client.BASE_URL;
import static com.yuuna.schat.util.AppConstants.SCHAT;
import static com.yuuna.schat.util.AppConstants.TAG_KEY;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuuna.schat.R;
import com.yuuna.schat.ui.ChatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class schatService extends Service {

    // Refresh
    private Handler handler = new Handler();
    private Runnable refresh;

    // Check Message without set last online
    public static Boolean isLastOnline = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        refresh = () -> {
            String setKey = getSharedPreferences(SCHAT, Context.MODE_PRIVATE).getString(TAG_KEY, "");
            if (!setKey.isEmpty() && !isLastOnline) saveLastOnline(setKey);
            if (!setKey.isEmpty()) checkMessage(setKey);
            handler.postDelayed(refresh, 10000); // 1000 == 1sec
        };
        handler.post(refresh);
        return super.onStartCommand(intent, flags, startId);
    }

    private void checkMessage(String setKey) {
        String check_message = "{\"request\":\"message\",\"data\":{\"key\":\""+setKey+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(check_message).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    // Response
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("status")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("messages");
                            for (int i = 0; i < jsonArray.length(); i++) checkChat(setKey, jsonArray.getJSONObject(i).getString("id"), jsonArray.getJSONObject(i).getInt("send"));
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

    private void checkChat(String setKey, String id, Integer send) {
        String check_chat = "{\"request\":\"check_chat\",\"data\":{\"key\":\""+setKey+"\",\"id\":\""+id+"\",\"send\":\""+send+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(check_chat).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    // Response
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("status")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("messages");
                            ArrayList<JSONObject> jsonObjectArrayList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = new JSONObject().put("name", jsonObject.getString("name"))
                                        .put("chat", jsonArray.getJSONObject(i).getString("chat"))
                                        .put("time", jsonArray.getJSONObject(i).getString("time"));
                                jsonObjectArrayList.add(object);
                            }
                            Integer sender = jsonObject.getInt("date_created");
                            if (!jsonObjectArrayList.isEmpty()) notification(getApplicationContext(), id, send, sender, jsonObjectArrayList, false);
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

    private void saveLastOnline(String setKey) {
        String last_online = "{\"request\":\"edit_last_online\",\"data\":{\"key\":\""+setKey+"\",\"last_online\":\""+(System.currentTimeMillis()/1000)+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(last_online).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {

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

    public static void notification(Context context, String id, Integer send, Integer sender, ArrayList<JSONObject> jsonObjectArrayList, Boolean isRemove) {
        if (isRemove) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
            NotificationManagerCompat.from(context).cancel(sender);
        } else {
            //
            Intent intent = new Intent(context, ChatActivity.class)
                    .putExtra("id", id)
                    .putExtra("send", send)
                    .putExtra("isOPEN", true)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, sender, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            // Update Status Read
            Intent readIntent = new Intent(context, schatBroadcastReceiver.class).setAction("READ")
                    .putExtra("id", id)
                    .putExtra("send", send)
                    .putExtra("sender", sender)
                    .putExtra("chat", String.valueOf(jsonObjectArrayList));
            PendingIntent readPendingIntent = PendingIntent.getBroadcast(context, sender, readIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            // Reply Message
            Intent replyIntent = new Intent(context, schatBroadcastReceiver.class).setAction("REPLY")
                    .putExtra("id", id)
                    .putExtra("send", send)
                    .putExtra("sender", sender)
                    .putExtra("chat", String.valueOf(jsonObjectArrayList));
            PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, sender, replyIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            // Create the reply action and add the remote input.
            NotificationCompat.Action action = new NotificationCompat.Action.Builder(0, "Reply", replyPendingIntent)
                    .addRemoteInput(new RemoteInput.Builder("key_text_reply").setLabel("Reply").build()).build();

            // Style Message
            NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle("Anda");
            Date date = null;
            for (int i = 0; i < jsonObjectArrayList.size(); i++) {
                try {
                    String name = jsonObjectArrayList.get(i).getString("name");
                    String chat = jsonObjectArrayList.get(i).getString("chat");
                    Long time = jsonObjectArrayList.get(i).getLong("time");
                    date = new Date(jsonObjectArrayList.get(jsonObjectArrayList.size()-1).getLong("time"));
                    messagingStyle.addMessage(chat, time, name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (date == null) date = new Date(System.currentTimeMillis());

            // Set Notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.app_name))
                    .setSmallIcon(R.drawable.photo)
                    .setWhen(date.getTime() * 1000)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    // Set one alert with same id
                    .setOnlyAlertOnce(true)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setStyle(messagingStyle)
                    //.setColor(Color.parseColor("#8D9472"))
                    .addAction(action)
                    .addAction(0, "Mark as read", readPendingIntent);

            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(context.getString(R.string.app_name), context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("");
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
            }

            // notificationId is a unique int for each notification that you must define
            if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
            NotificationManagerCompat.from(context).notify(sender, builder.build());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(new NotificationChannel("SCHAT", "Background Service", NotificationManager.IMPORTANCE_NONE));
            startForeground(2, new NotificationCompat.Builder(this, "SCHAT").setOngoing(false).build());
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        isLastOnline = true;
    }
}
