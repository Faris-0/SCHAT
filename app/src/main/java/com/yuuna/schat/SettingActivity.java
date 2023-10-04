package com.yuuna.schat;

import static com.yuuna.schat.util.Client.BASE_URL;
import static com.yuuna.schat.util.SharedPref.KEY;
import static com.yuuna.schat.util.SharedPref.SCHAT;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuuna.schat.util.Client;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends Activity {

    private CircleImageView civPhoto;
    private TextView tvName, tvTag, tvAName, tvBio;
    private Switch sHide;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        findViewById(R.id.sBack).setOnClickListener(v -> onBackPressed());
        civPhoto = findViewById(R.id.sPhoto);
        tvName = findViewById(R.id.sName);
        tvTag = findViewById(R.id.saTag);
        tvAName = findViewById(R.id.saName);
        tvBio = findViewById(R.id.saBio);
        sHide = findViewById(R.id.sHide);

        context = SettingActivity.this;

        profile();
    }

    private void profile() {
        String key = getSharedPreferences(SCHAT, MODE_PRIVATE).getString(KEY, "");
        String add_contact = "{\"request\":\"profile\",\"data\":{\"key\":\""+key+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(add_contact).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        // Response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
                                String tag = jsonObject.getString("tag");
                                String name = jsonObject.getString("name");
                                String bio = jsonObject.getString("bio");

                                if (tag.equals("")) tag = "Empty";
                                if (bio.equals("")) bio = "Empty";

                                tvName.setText(name);
                                tvTag.setText(tag);
                                tvAName.setText(name);
                                tvBio.setText(bio);

                                sHide.setChecked(true);
                            } else Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
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
