package com.yuuna.schat;

import static com.yuuna.schat.util.Client.BASE_PHOTO;
import static com.yuuna.schat.util.Client.BASE_URL;
import static com.yuuna.schat.util.SharedPref.SCHAT;
import static com.yuuna.schat.util.SharedPref.TAG_KEY;
import static com.yuuna.schat.util.SharedPref.TAG_SIGN;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuuna.schat.adapter.MessageAdapter;
import com.yuuna.schat.util.Client;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends Activity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        context = ChatActivity.this;

        findViewById(R.id.cBack).setOnClickListener(v -> onBackPressed());

        loadProfile(getIntent().getStringExtra("id"));
    }

    private void loadProfile(String id) {
        String setKey = getSharedPreferences(SCHAT, MODE_PRIVATE).getString(TAG_KEY, "");
        //
        String message_detail = "{\"request\":\"message_detail\",\"data\":{\"key\":\""+setKey+"\",\"id\":\""+id+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(message_detail).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        // Response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
                                TextView tvName = findViewById(R.id.cName);
                                tvName.setText(jsonObject.getString("name"));

                                CircleImageView civPhoto = findViewById(R.id.cPhoto);
                                String photo = BASE_PHOTO + jsonObject.getString("photo");
                                if (!photo.equals(BASE_PHOTO)) Glide.with(context)
                                        .load(photo)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(civPhoto);
                                else civPhoto.setImageResource(R.drawable.photo);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
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
}
