package com.yuuna.schat;

import static com.yuuna.schat.util.Client.BASE_URL;
import static com.yuuna.schat.util.SharedPref.SCHAT;
import static com.yuuna.schat.util.SharedPref.TAG_KEY;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
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
    private TextView tvName, tvAName, tvBio;
    private Switch sHide;

    private Context context;
    private Dialog dName, dBio;

    private String name, bio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        findViewById(R.id.sBack).setOnClickListener(v -> onBackPressed());
        civPhoto = findViewById(R.id.sPhoto);
        tvName = findViewById(R.id.sName);
        tvAName = findViewById(R.id.saName);
        tvBio = findViewById(R.id.saBio);
        sHide = findViewById(R.id.sHide);

        context = SettingActivity.this;

        findViewById(R.id.sBName).setOnClickListener(v -> nameDialog());
        findViewById(R.id.sBBio).setOnClickListener(v -> bioDialog());

        profile();
    }

    private void nameDialog() {
        dName = new Dialog(context);
        dName.setContentView(R.layout.dialog_edit_name);
        dName.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dName.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText etName = dName.findViewById(R.id.enName);
        TextView tvLimit = dName.findViewById(R.id.enLimit);

        etName.setText(name);
        tvLimit.setText(String.valueOf(25 - name.length()));

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tvLimit.setText(String.valueOf(25 - editable.length()));
            }
        });

        dName.show();
    }

    private void bioDialog() {
        dBio = new Dialog(context);
        dBio.setContentView(R.layout.dialog_edit_bio);
        dBio.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dBio.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText etBio = dBio.findViewById(R.id.ebBio);
        TextView tvLimit = dBio.findViewById(R.id.ebLimit);

        etBio.setText(bio);
        tvLimit.setText(String.valueOf(70 - bio.length()));

        etBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tvLimit.setText(String.valueOf(70 - editable.length()));
            }
        });

        dBio.findViewById(R.id.ebSave).setOnClickListener(v -> {
            //
        });

        dBio.show();
    }

    private void profile() {
        String key = getSharedPreferences(SCHAT, MODE_PRIVATE).getString(TAG_KEY, "");
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
                                name = jsonObject.getString("name");
                                bio = jsonObject.getString("bio");

                                tvName.setText(name);
                                tvAName.setText(name);
                                if (!bio.equals("")) tvBio.setText(bio);
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
