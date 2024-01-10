package com.yuuna.schat.ui;

import static com.yuuna.schat.util.Client.BASE_PHOTO;
import static com.yuuna.schat.util.Client.BASE_URL;
import static com.yuuna.schat.util.AppConstants.SCHAT;
import static com.yuuna.schat.util.AppConstants.TAG_ACC;
import static com.yuuna.schat.util.AppConstants.TAG_KEY;
import static com.yuuna.schat.util.AppConstants.TAG_NAME;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuuna.schat.R;
import com.yuuna.schat.util.Client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends Activity {

    private CircleImageView civPhoto;
    private TextView tvName, tvAName, tvABio;
    private Switch sHide;

    private Context context;
    private Dialog dEdit;
    private SharedPreferences spSCHAT;

    private ArrayList<JSONObject> jsonObjectArrayList;

    private String setKey, setName, bio, dataAcc;
    private Integer TAG_GALLERY = 2, limitText = 0;;

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

        findViewById(R.id.sBPhoto).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 29) {
                // Open Galley
                try {
                    startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), TAG_GALLERY);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    // Open Storage
                    try {
                        startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), TAG_GALLERY);
                    } catch (ActivityNotFoundException q) {
                        q.printStackTrace();
                    }
                }
            } else ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, TAG_GALLERY);
        });
        findViewById(R.id.sBName).setOnClickListener(v -> editDialog("Name"));
        findViewById(R.id.sBBio).setOnClickListener(v -> editDialog("Bio"));
        sHide.setOnCheckedChangeListener((compoundButton, b) -> setPrivate(b));
        findViewById(R.id.sBSignOut).setOnClickListener(v -> {
            try {
                for (int i = 0; i < jsonObjectArrayList.size(); i++) {
                    if (jsonObjectArrayList.get(i).getString("key").equals(setKey)) jsonObjectArrayList.remove(i);
                }
                if (jsonObjectArrayList.size() != 0) {
                    spSCHAT.edit()
                            .putString(TAG_KEY, jsonObjectArrayList.get(0).getString("key"))
                            .putString(TAG_NAME, jsonObjectArrayList.get(0).getString("name"))
                            .putString(TAG_ACC, String.valueOf(jsonObjectArrayList))
                            .commit();
                } else spSCHAT.edit().clear().commit();
                onBackPressed();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        spSCHAT = getSharedPreferences(SCHAT, MODE_PRIVATE);
        setKey = spSCHAT.getString(TAG_KEY, "");
        setName = spSCHAT.getString(TAG_NAME, "");

        tvName.setText(setName);
        if (!setName.equals("")) tvAName.setText(setName);

        profile();
        loadAcc();
    }

    private void setPrivate(boolean isPrivate) {
        Integer iPrivate;
        if (isPrivate) iPrivate = 1;
        else iPrivate = 0;
        String last_online = "{\"request\":\"edit_private\",\"data\":{\"key\":\""+setKey+"\",\"private\":\""+iPrivate+"\"}}";
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

    private void editDialog(String edit) {
        dEdit = new Dialog(context);
        dEdit.setContentView(R.layout.dialog_edit);
        dEdit.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dEdit.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText etText = dEdit.findViewById(R.id.eText);
        TextView tvLimit = dEdit.findViewById(R.id.eLimit);
        TextView tvTitle = dEdit.findViewById(R.id.eTitle);

        if (edit.equals("Name")) {
            limitText = 25;
            etText.setText(setName);
            etText.setHint("Name");
            tvLimit.setText(String.valueOf(limitText - setName.length()));
            tvTitle.setText("Edit Name");
        } else if (edit.equals("Bio")) {
            limitText = 70;
            etText.setText(bio);
            etText.setHint("Bio");
            tvLimit.setText(String.valueOf(limitText - bio.length()));
            tvTitle.setText("Edit Bio");
        }

        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tvLimit.setText(String.valueOf(limitText - editable.length()));
            }
        });

        dEdit.findViewById(R.id.eBack).setOnClickListener(v -> dEdit.dismiss());

        dEdit.findViewById(R.id.eSave).setOnClickListener(v -> {
            if (edit.equals("Name")) saveAccount(etText.getText().toString(), true);
            else if (edit.equals("Bio")) saveAccount(etText.getText().toString(), false);
        });

        dEdit.show();
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
                                } else {
                                    if (!nb.equals("")) {
                                        bio = nb;
                                        tvABio.setText(nb);
                                    } else tvABio.setText("Empty");
                                }
                                dEdit.dismiss();
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
                                String photo = BASE_PHOTO + jsonObject.getString("photo");
                                if (!photo.equals(BASE_PHOTO)) Glide.with(context)
                                        .load(photo)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(civPhoto);

                                bio = jsonObject.getString("bio");
                                if (!bio.equals("")) tvABio.setText(bio);

                                if (jsonObject.getInt("private") == 1) sHide.setChecked(true);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == TAG_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Open Galley
                try {
                    startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), TAG_GALLERY);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    // Open Storage
                    try {
                        startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), TAG_GALLERY);
                    } catch (ActivityNotFoundException q) {
                        q.printStackTrace();
                    }
                }
            } else Toast.makeText(this, "No permission to access images", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == TAG_GALLERY) {
            try {
                Bitmap bGallery = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bGallery.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] bytes = stream.toByteArray();
                if (bytes.length/1024 > 1024) {
                    stream = new ByteArrayOutputStream();
                    bGallery.compress(Bitmap.CompressFormat.JPEG, 25, stream);
                    bytes = stream.toByteArray();
                }
                String sphoto = Base64.encodeToString(bytes, Base64.DEFAULT);
                if (sphoto != null) sendPhoto(sphoto);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendPhoto(String sphoto) {
        String sendphoto = "{\"request\":\"edit_photo\",\"data\":{\"key\":\""+setKey+"\",\"photo\":\""+sphoto+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(sendphoto).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> profile());
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
