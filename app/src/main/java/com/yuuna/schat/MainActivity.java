package com.yuuna.schat;

import static com.yuuna.schat.util.Client.BASE_URL;
import static com.yuuna.schat.util.SharedPref.TAG_ACC;
import static com.yuuna.schat.util.SharedPref.TAG_KEY;
import static com.yuuna.schat.util.SharedPref.TAG_NAME;
import static com.yuuna.schat.util.SharedPref.SCHAT;
import static com.yuuna.schat.util.SharedPref.TAG_SIGN;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.yuuna.schat.adapter.AccountAdapter;
import com.yuuna.schat.util.Client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity implements AccountAdapter.ItemClickListener {

    private EditText etUsername;

    private Context context;
    private Dialog dMenu, dSign, dContact, dAddContact;
    private SharedPreferences spSCHAT;

    private ArrayList<JSONObject> jsonObjectArrayList;

    private String dataAcc, setKey, setName;
    private Boolean isAccount, isSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.mMenu).setOnClickListener(v -> menuDialog());
        findViewById(R.id.mContact).setOnClickListener(v -> contactDialog());

        context = MainActivity.this;

        spSCHAT = getSharedPreferences(SCHAT, MODE_PRIVATE);
        isSign = spSCHAT.getBoolean(TAG_SIGN, false);
        setKey = spSCHAT.getString(TAG_KEY, "");
        setName = spSCHAT.getString(TAG_NAME, "");

        if (!isSign) sign();
        loadAcc();
    }

    private void contactDialog() {
        if (dAddContact != null) dAddContact.dismiss();
        dContact = new Dialog(context);
        dContact.setContentView(R.layout.dialog_contact);
        dContact.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dContact.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dContact.findViewById(R.id.cAdd).setOnClickListener(v -> addContactDialog());
        dContact.findViewById(R.id.cClose).setOnClickListener(v -> dContact.dismiss());

        dContact.show();
    }

    private void addContactDialog() {
        if (dContact != null) dContact.dismiss();
        dAddContact = new Dialog(context);
        dAddContact.setContentView(R.layout.dialog_add_contact);
        dAddContact.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dAddContact.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dAddContact.findViewById(R.id.acBack).setOnClickListener(v -> contactDialog());
        dAddContact.findViewById(R.id.acAdd).setOnClickListener(v -> {
            EditText etUsername = dAddContact.findViewById(R.id.acUsername);
            if (!etUsername.getText().toString().isEmpty()) addcontact(etUsername.getText().toString());
            else Toast.makeText(context, "Tag cannot be empty!", Toast.LENGTH_SHORT).show();
        });

        dAddContact.show();
    }

    private void addcontact(String username) {
        String add_contact = "{\"request\":\"add_contact\",\"data\":{\"key\":\""+setKey+"\",\"username\":\""+username+"\"}}";
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
                                Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                dAddContact.dismiss();
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

    private void menuDialog() {
        // Set Default Dialog Menu
        isAccount = false;
        // Dialog Menu
        dMenu = new Dialog(context);
        dMenu.setContentView(R.layout.dialog_menu);
        dMenu.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dMenu.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvName = dMenu.findViewById(R.id.mName);
        tvName.setText(spSCHAT.getString(TAG_NAME, ""));

        RecyclerView rvAcc = dMenu.findViewById(R.id.maSub1);
        rvAcc.setLayoutManager(new LinearLayoutManager(context));

        dMenu.findViewById(R.id.mClose).setOnClickListener(v -> dMenu.dismiss());

        dMenu.findViewById(R.id.mAccount).setOnClickListener(v -> {
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
                // Set to Adapter from Data Account
                AccountAdapter accountAdapter = new AccountAdapter(jsonObjectArrayList, context);
                rvAcc.setAdapter(accountAdapter);
                accountAdapter.setClickListener(MainActivity.this);
            }
        });
        
        dMenu.findViewById(R.id.maSub2).setOnClickListener(v -> sign());

        dMenu.findViewById(R.id.mSetting).setOnClickListener(v -> {
            if (setKey != null || !setKey.equals("")) {
                dMenu.dismiss();
                startActivity(new Intent(context, SettingActivity.class));
            }
        });

        dMenu.show();
    }

    private void sign() {
        // Set Default Dialog Menu
        isAccount = false;
        if (dMenu != null) dMenu.dismiss();
        // Dialog Login
        dSign = new Dialog(context);
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

        EditText etName, etPassword;
        etName = dSign.findViewById(R.id.sEName);
        etUsername = dSign.findViewById(R.id.sEUsername);
        etPassword = dSign.findViewById(R.id.sEPassword);

        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                etUsername.setTextColor(getResources().getColor(R.color.gray));
            }
        });

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
            etName.setText("");
            etUsername.setText("");
            etPassword.setText("");
        });

        dSign.findViewById(R.id.sLLogInReg).setOnClickListener(v -> {
            String name, username, password;
            name = etName.getText().toString();
            username = etUsername.getText().toString();
            password = etPassword.getText().toString();
            Boolean isEmpty = false;
            if (llName.getVisibility() == View.GONE) {
                // Login
                if (!username.isEmpty() && !password.isEmpty()) {
                    logreg(name, username, password, true);
                } else isEmpty = true;
            } else {
                // Register
                if (!name.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
                    logreg(name, username, password, false);
                } else isEmpty = true;
            }
            if (isEmpty) {
                Toast.makeText(context, "Data cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        dSign.show();
    }

    private void logreg(String name, String username, String password, Boolean isLogReg) {
        String LogReg = "";
        if (isLogReg) {
            LogReg = "{\"request\":\"login\",\"data\":{\"username\":\""+username+"\",\"password\":\""+password+"\"}}";
        } else {
            LogReg = "{\"request\":\"register\",\"data\":{\"name\":\""+name+"\",\"username\":\""+username+"\",\"password\":\""+password+"\"}}";
        }
        JsonObject jsonObject = JsonParser.parseString(LogReg).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        // Response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
                                Boolean isYour = false;
                                for (JSONObject object : jsonObjectArrayList) {
                                    if (jsonObject.getString("key").equals(object.getString("key"))) isYour = true;
                                }
                                if (!isYour) {
                                    isSign = true;
                                    // Set New Key
                                    setKey = jsonObject.getString("key");
                                    setName = jsonObject.getString("name");
                                    // Add Data JSON
                                    jsonObjectArrayList.add(jsonObject);
                                    // Save Data
                                    spSCHAT.edit()
                                            .putBoolean(TAG_SIGN, isSign)
                                            .putString(TAG_KEY, setKey)
                                            .putString(TAG_NAME, setName)
                                            .putString(TAG_ACC, String.valueOf(jsonObjectArrayList))
                                            .commit();
                                    dSign.dismiss();
                                    Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                } else Toast.makeText(context, "You're already logged in", Toast.LENGTH_SHORT).show();
                            } else {
                                if (jsonObject.getString("message").equals("Username has been taken!")) etUsername.setTextColor(Color.RED);
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

    @Override
    public void onItemClick(JSONObject jsonObject) {
        try {
            spSCHAT.edit()
                    .putString(TAG_KEY, jsonObject.getString("key"))
                    .putString(TAG_NAME, jsonObject.getString("name"))
                    .commit();
            dMenu.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}