package com.yuuna.schat;

import static com.yuuna.schat.util.Client.BASE_URL;
import static com.yuuna.schat.util.SharedPref.SCHAT;
import static com.yuuna.schat.util.SharedPref.TAG_ACC;
import static com.yuuna.schat.util.SharedPref.TAG_KEY;
import static com.yuuna.schat.util.SharedPref.TAG_NAME;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuuna.schat.util.Client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends Activity {

    private CircleImageView civPhoto;
    private TextView tvName, tvAName, tvABio;
    private Switch sHide;

    private Context context;
    private Dialog dName, dBio;
    private SharedPreferences spSCHAT;

    private ArrayList<JSONObject> jsonObjectArrayList, cJsonObjectArrayList;

    private String setKey, setName, bio, dataAcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        findViewById(R.id.sBack).setOnClickListener(v -> onBackPressed());
        civPhoto = findViewById(R.id.sPhoto);
        tvName = findViewById(R.id.sName);
        tvAName = findViewById(R.id.saName);
        tvABio = findViewById(R.id.saBio);
        sHide = findViewById(R.id.sHide);

        context = SettingActivity.this;

        findViewById(R.id.sBName).setOnClickListener(v -> nameDialog());
        findViewById(R.id.sBBio).setOnClickListener(v -> bioDialog());

        spSCHAT = getSharedPreferences(SCHAT, MODE_PRIVATE);
        setKey = spSCHAT.getString(TAG_KEY, "");
        setName = spSCHAT.getString(TAG_NAME, "");

        tvName.setText(setName);
        if (!setName.equals("")) tvAName.setText(setName);

        profile();
        loadAcc();
    }

    private void loadAcc() {
        // Load Data JSON
        dataAcc = spSCHAT.getString(TAG_ACC, "");
        try {
            jsonObjectArrayList = new ArrayList<>();
            if (!dataAcc.equals("")) {
                JSONArray jsonArray = new JSONArray(dataAcc);
                for (int i = 0; i < jsonArray.length(); i++) jsonObjectArrayList.add(jsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void nameDialog() {
        dName = new Dialog(context);
        dName.setContentView(R.layout.dialog_edit_name);
        dName.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dName.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText etName = dName.findViewById(R.id.enName);
        TextView tvLimit = dName.findViewById(R.id.enLimit);

        etName.setText(setName);
        tvLimit.setText(String.valueOf(25 - setName.length()));

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

        dName.findViewById(R.id.enSave).setOnClickListener(v -> saveAccount(etName.getText().toString(), true));

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

        dBio.findViewById(R.id.ebSave).setOnClickListener(v -> saveAccount(etBio.getText().toString(), false));

        dBio.show();
    }

    private void saveAccount(String nb, boolean b) {
        String namebio;
        if (b) namebio = "{\"request\":\"edit_name\",\"data\":{\"key\":\""+setKey+"\",\"name\":\""+nb+"\"}}";
        else namebio = "{\"request\":\"edit_bio\",\"data\":{\"key\":\""+setKey+"\",\"bio\":\""+nb+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(namebio).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        // Response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
                                if (b) {
                                    if (!nb.equals("")) {
                                        setName = nb;
                                        tvName.setText(nb);
                                        tvAName.setText(nb);
                                    } else {
                                        tvName.setText("");
                                        tvAName.setText("Empty");
                                    }
                                    Integer number = 0;
                                    for (int i = 0; i < jsonObjectArrayList.size(); i++) {
                                        if (jsonObjectArrayList.get(i).getString("key").equals(setKey)) {
                                            number = i;
                                            jsonObjectArrayList.remove(i);
                                        }
                                    }
                                    jsonObjectArrayList.add(new JSONObject()
                                                    .put("number", number + 1)
                                                    .put("key", setKey).put("name", nb));
                                    Collections.sort(jsonObjectArrayList, (a, b) -> {
                                        try {
                                            return a.getInt("number") - b.getInt("number");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            return 0;
                                        }
                                    });
                                    spSCHAT.edit().putString(TAG_NAME, nb).putString(TAG_ACC, String.valueOf(jsonObjectArrayList)).commit();
                                    dName.dismiss();
                                } else {
                                    if (!nb.equals("")) {
                                        bio = nb;
                                        tvABio.setText(nb);
                                    } else tvABio.setText("Empty");
                                    dBio.dismiss();
                                }
                                Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
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

    private void profile() {
        String add_contact = "{\"request\":\"profile\",\"data\":{\"key\":\""+setKey+"\"}}";
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
                                bio = jsonObject.getString("bio");
                                if (!bio.equals("")) tvABio.setText(bio);
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
