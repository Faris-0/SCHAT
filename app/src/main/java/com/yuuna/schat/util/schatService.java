package com.yuuna.schat.util;

import static com.yuuna.schat.util.Client.BASE_URL;
import static com.yuuna.schat.util.SharedPref.SCHAT;
import static com.yuuna.schat.util.SharedPref.TAG_KEY;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

public class schatService extends Service {

    // Refresh
    private Handler handler = new Handler();
    private Runnable refresh;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        refresh = () -> {
            String setKey = getSharedPreferences(SCHAT, Context.MODE_PRIVATE).getString(TAG_KEY, "");
            if (!setKey.equals("")) saveLastOnline(setKey);
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

    // Can still running in background but no network connection
//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        super.onTaskRemoved(rootIntent);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            stopForeground(Service.STOP_FOREGROUND_DETACH);
//            NotificationManagerCompat.from(this).cancel(2);
//        }
//    }
}
