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
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuuna.schat.ui.ChatActivity;

import java.io.IOException;

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
            if (!setKey.equals("") && !isLastOnline) saveLastOnline(setKey);
//            notification();
            handler.postDelayed(refresh, 10000); // 1000 == 1sec
        };
        handler.post(refresh);
        return super.onStartCommand(intent, flags, startId);
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

    private void notification(Context context, String id, Boolean isRemove) {
//        if (isRemove) {
//            //--//
//            // notificationId is a unique int for each notification that you must define
//            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
//            NotificationManagerCompat.from(context).cancel(Integer.parseInt(id));
//            //--//
//        } else {
//            // Create an explicit intent for an Activity in your app
//            Intent intent = new Intent(context, ChatActivity.class)
//                    .putExtra("konsultasi_id", body.get(0).getKonsultasi_id())
//                    .putExtra("meeting_code", body.get(0).getMeeting_code())
//                    .putExtra("isOPEN", true)
//                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            PendingIntent pendingIntent = PendingIntent.getActivity(context, sender_id, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
//            //--//
//            Intent readIntent = new Intent(context, NotifikasiBroadcastReceiver.class).setAction("READ")
//                    .putExtra("sender_id", sender_id)
//                    .putExtra("konsultasi_id", body.get(0).getKonsultasi_id());
//            PendingIntent readPendingIntent = PendingIntent.getBroadcast(context, sender_id, readIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
//            //--//
//            // Build a PendingIntent for the reply action to trigger.
//            Intent replyIntent = new Intent(context, NotifikasiBroadcastReceiver.class).setAction("REPLY")
//                    .putExtra("sender_id", sender_id)
//                    .putExtra("konsultasi_id", body.get(0).getKonsultasi_id());
//            PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, sender_id, replyIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
//            // Create the reply action and add the remote input.
//            NotificationCompat.Action action = new NotificationCompat.Action.Builder(0, "Reply", replyPendingIntent)
//                    .addRemoteInput(new RemoteInput.Builder("key_text_reply").setLabel("Reply").build()).build();
//            //--//
//            NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle("Anda");
//            Date date = null;
//            for (int i = 0; i < body.size(); i++) {
//                try {
//                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("id")).parse(body.get(i).getTime());
//                    messagingStyle.addMessage(body.get(i).getMessage(), date.getTime(), body.get(i).getNama());
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//            //--//
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.app_name))
//                    .setSmallIcon(R.drawable.sithole)
//                    .setWhen(date.getTime())
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                    // Set the intent that will fire when the user taps the notification
//                    .setContentIntent(pendingIntent)
//                    .setAutoCancel(true)
//                    // Set one alert with same id
//                    .setOnlyAlertOnce(true)
//                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
//                    .setStyle(messagingStyle)
//                    .setColor(Color.parseColor("#8D9472"))
//                    .addAction(action)
//                    .addAction(0, "Mark as read", readPendingIntent);
//            //--//
//            // Create the NotificationChannel, but only on API 26+ because
//            // the NotificationChannel class is new and not in the support library
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                NotificationChannel channel = new NotificationChannel(context.getString(R.string.app_name), context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
//                channel.setDescription("");
//                // Register the channel with the system; you can't change the importance
//                // or other notification behaviors after this
//                context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
//            }
//            //--//
//            // notificationId is a unique int for each notification that you must define
//            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
//            NotificationManagerCompat.from(context).notify(sender_id, builder.build());
//            //--//
//        }
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
