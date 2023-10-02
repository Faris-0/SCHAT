package com.yuuna.schat;

import static com.yuuna.schat.util.SharedPref.SCHAT;
import static com.yuuna.schat.util.SharedPref.SIGN;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuuna.schat.util.Client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

public class MainActivity extends Activity {

    private Dialog dMenu, dSign;

    private Boolean isAccount = false, isSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.mMenu).setOnClickListener(v -> menuDialog());

        isSign = getSharedPreferences(SCHAT, MODE_PRIVATE).getBoolean(SIGN, false);
        if (!isSign) sign();
    }

    private void menuDialog() {
        // Dialog Menu
        dMenu = new Dialog(this);
        dMenu.setContentView(R.layout.dialog_menu);
        dMenu.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dMenu.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dMenu.findViewById(R.id.mAkun).setOnClickListener(v -> {
            if (isAccount) {
                dMenu.findViewById(R.id.ma).setRotation(0);
                dMenu.findViewById(R.id.maSub).setAlpha(1);
                dMenu.findViewById(R.id.maSub).setVisibility(View.GONE);
                dMenu.findViewById(R.id.maSub).animate().alpha(0).setDuration(500);
                isAccount = false;
            } else {
                dMenu.findViewById(R.id.ma).setRotation(90);
                dMenu.findViewById(R.id.maSub).setAlpha(0);
                dMenu.findViewById(R.id.maSub).setVisibility(View.VISIBLE);
                dMenu.findViewById(R.id.maSub).animate().alpha(1).setDuration(500);
                isAccount = true;
            }
        });
        
        dMenu.findViewById(R.id.maSub2).setOnClickListener(v -> sign());

        dMenu.show();
    }

    private void sign() {
        // Set Default Dialog Menu
        isAccount = false;
        if (dMenu != null) dMenu.dismiss();
        // Dialog Login
        dSign = new Dialog(this);
        dSign.setContentView(R.layout.dialog_sign);
        dSign.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dSign.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (!isSign) dSign.setCancelable(false);

        LinearLayout llSign, llSignInUp, llName;
        llSign = dSign.findViewById(R.id.sLSign);
        llSignInUp = dSign.findViewById(R.id.sLSignInUp);
        llName = dSign.findViewById(R.id.sLName);

        TextView tvSign, tvLogReg;
        tvSign = dSign.findViewById(R.id.sTSignInUp);
        tvLogReg = dSign.findViewById(R.id.sTLogInReg);

        dSign.findViewById(R.id.sSignIn).setOnClickListener(v -> {
            tvSign.setText("Sign In");
            tvLogReg.setText("Login");
            llSign.setVisibility(View.GONE);
            llSignInUp.setVisibility(View.VISIBLE);
            llName.setVisibility(View.GONE);
        });

        dSign.findViewById(R.id.sSignUp).setOnClickListener(v -> {
            tvSign.setText("Sign Up");
            tvLogReg.setText("Register");
            llSign.setVisibility(View.GONE);
            llSignInUp.setVisibility(View.VISIBLE);
            llName.setVisibility(View.VISIBLE);
        });

        dSign.findViewById(R.id.sBack).setOnClickListener(v -> {
            llSign.setVisibility(View.VISIBLE);
            llSignInUp.setVisibility(View.GONE);
        });

        dSign.findViewById(R.id.sLLogInReg).setOnClickListener(v -> {
            if (llName.getVisibility() == View.GONE) {
                // Login
                login();
            } else {
                // Register
            }
        });

        dSign.show();
    }

    private void login() {
        String tes = "{\"request\":\"login\",\"data\":{\"username\":\"faris\",\"password\":\"faris\"}}";
        JsonObject jsonObject = JsonParser.parseString(tes).getAsJsonObject();
        try {
            new Client().getOkHttpClient("http://192.168.34.68/schat/index.php", new Gson().toJson(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    // Log Response
                    Log.d("HEHEHE1", response);

                    // Check JSON Object or Array
                    try {
                        Object json = new JSONTokener(response).nextValue();
                        if (json instanceof JSONObject) {
                            Log.d("JSONObject", "YES");
                        } else if (json instanceof JSONArray) {
                            Log.d("JSONArray", "YES");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Redirect
                    try {
                        Boolean status = new JSONObject(response).getBoolean("status");
                        if (status) {
                            Log.d("HEHEHE1", "YES");
                        } else {
                            Log.d("HEHEHE1", "No");
                        }
//                        Log.d("HEHEHE2", redirectUrl);
//                        startActivity(new Intent(MainActivity.this, RedirectView.class).putExtra("URL", redirectUrl));
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
}