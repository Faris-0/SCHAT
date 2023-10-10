package com.yuuna.schat;

import static com.yuuna.schat.util.Client.BASE_PHOTO;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuuna.schat.adapter.AccountAdapter;
import com.yuuna.schat.adapter.ContactAdapter;
import com.yuuna.schat.adapter.MessageAdapter;
import com.yuuna.schat.util.Client;
import com.yuuna.schat.util.CustomLinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends Activity implements AccountAdapter.ItemClickListener, ContactAdapter.ItemClickListener, MessageAdapter.ItemClickListener {

    private EditText etUsername, etFind;
    private LinearLayout llToolbar, llFind, llClear;
    private RecyclerView rvMessage;

    private Context context;
    private Dialog dMenu, dSign, dContact, dAddContact;
    private SharedPreferences spSCHAT;

    private MessageAdapter messageAdapter;

    private ArrayList<JSONObject> jsonObjectArrayList, jsonObjectArrayList2, jsonObjectArrayList3, jsonObjectArrayList4;

    private String dataAcc, setKey, setName;
    private Boolean isAccount, isSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.mMenu).setOnClickListener(v -> menuDialog());
        findViewById(R.id.mContact).setOnClickListener(v -> contactDialog());

        llToolbar = findViewById(R.id.mToolbar);
        llFind = findViewById(R.id.mfLayout);
        etFind = findViewById(R.id.mfFind);
        llClear = findViewById(R.id.mfClear);
        rvMessage = findViewById(R.id.mMessage);

        rvMessage.setLayoutManager(new CustomLinearLayoutManager(context));

        findViewById(R.id.mFind).setOnClickListener(v -> {
            llToolbar.setVisibility(View.GONE);
            findViewById(R.id.mfLayout).setVisibility(View.VISIBLE);
        });
        findViewById(R.id.mfBack).setOnClickListener(v -> {
            llToolbar.setVisibility(View.VISIBLE);
            llFind.setVisibility(View.GONE);
            //
            etFind.setText("");
            llClear.setVisibility(View.GONE);
            messageAdapter.getFilter().filter("");
        });
        etFind.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                messageAdapter.getFilter().filter(editable);
                if (editable.toString().equals("")) llClear.setVisibility(View.GONE);
                else llClear.setVisibility(View.VISIBLE);
            }
        });

        llClear.setOnClickListener(v1 -> {
            etFind.setText("");
            llClear.setVisibility(View.GONE);
            messageAdapter.getFilter().filter("");
        });
    }

    private void contactDialog() {
        if (dAddContact != null) dAddContact.dismiss();
        dContact = new Dialog(context);
        dContact.setContentView(R.layout.dialog_contact);
        dContact.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dContact.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RecyclerView rvContact = dContact.findViewById(R.id.cName);
        rvContact.setLayoutManager(new CustomLinearLayoutManager(context));
        // Set to Adapter from Data Account
        if (jsonObjectArrayList2 == null) jsonObjectArrayList2 = new ArrayList<>();
        ContactAdapter contactAdapter = new ContactAdapter(jsonObjectArrayList2, context);
        rvContact.setAdapter(contactAdapter);
        contactAdapter.setClickListener(MainActivity.this);

        EditText etName = dContact.findViewById(R.id.cFind);
        LinearLayout llClear = dContact.findViewById(R.id.cClear);
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                contactAdapter.getFilter().filter(editable);
                if (editable.toString().equals("")) llClear.setVisibility(View.GONE);
                else llClear.setVisibility(View.VISIBLE);
            }
        });

        llClear.setOnClickListener(v1 -> {
            etName.setText("");
            llClear.setVisibility(View.GONE);
            contactAdapter.getFilter().filter("");
        });

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
                                loadContact();
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

        CircleImageView civPhoto = dMenu.findViewById(R.id.mPhoto);
        profile(civPhoto);

        RecyclerView rvAcc = dMenu.findViewById(R.id.maSub1);
        rvAcc.setLayoutManager(new CustomLinearLayoutManager(context));
        // Set to Adapter from Data Account
        AccountAdapter accountAdapter = new AccountAdapter(jsonObjectArrayList, context);
        rvAcc.setAdapter(accountAdapter);
        accountAdapter.setClickListener(MainActivity.this);

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

    private void profile(CircleImageView civPhoto) {
        String profile = "{\"request\":\"profile\",\"data\":{\"key\":\""+setKey+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(profile).getAsJsonObject();
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
                                    jsonObjectArrayList.add(new JSONObject()
                                                    .put("number", jsonObjectArrayList.size() + 1)
                                                    .put("key", setKey).put("name", setName));
                                    // Save Data
                                    spSCHAT.edit()
                                            .putBoolean(TAG_SIGN, isSign)
                                            .putString(TAG_KEY, setKey)
                                            .putString(TAG_NAME, setName)
                                            .putString(TAG_ACC, String.valueOf(jsonObjectArrayList))
                                            .commit();
                                    // Load Contact
                                    loadContact();
                                    // Load Message
                                    loadMessage();
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

    private void loadContact() {
        String add_contact = "{\"request\":\"contact\",\"data\":{\"key\":\""+setKey+"\"}}";
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
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                jsonObjectArrayList2 = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) jsonObjectArrayList2.add(jsonArray.getJSONObject(i));
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
    public void onItemClick(JSONObject jsonObject, View view) {
        try {
            Integer id = view.getId();
            if (id == R.id.cButton) {
                createMessage(jsonObject.getString("username"));
            } else if (id == R.id.aButton) {
                spSCHAT.edit()
                        .putString(TAG_KEY, jsonObject.getString("key"))
                        .putString(TAG_NAME, jsonObject.getString("name"))
                        .commit();
                setKey = jsonObject.getString("key");
                // Load Contact
                loadContact();
                // Load Message
                loadMessage();
                dMenu.dismiss();
            } else if (id == R.id.mButton) {
                startActivity(new Intent(context, ChatActivity.class));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createMessage(String username) {
        String add_message = "{\"request\":\"add_message\",\"data\":{\"key\":\""+setKey+"\",\"username\":\""+username+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(add_message).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        // Response
                        try {
                            if (new JSONObject(response).getBoolean("status")) {
                                // Load Message
                                loadMessage();
                                dContact.dismiss();
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

    @Override
    protected void onStart() {
        super.onStart();
        context = MainActivity.this;

        spSCHAT = getSharedPreferences(SCHAT, MODE_PRIVATE);
        isSign = spSCHAT.getBoolean(TAG_SIGN, false);
        setKey = spSCHAT.getString(TAG_KEY, "");
        setName = spSCHAT.getString(TAG_NAME, "");

        if (!isSign) sign();
        loadAcc();
        if (!setKey.equals("")) loadContact();
        loadMessage();
    }

    private void loadMessage() {
        String message = "{\"request\":\"message\",\"data\":{\"key\":\""+setKey+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        // Response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                jsonObjectArrayList3 = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) jsonObjectArrayList3.add(jsonArray.getJSONObject(i));
                                jsonObjectArrayList4 = new ArrayList<>();
                                if (jsonObjectArrayList3.size() == 0) {
                                    // Set to Adapter from Data Account
                                    messageAdapter = new MessageAdapter(jsonObjectArrayList4, context);
                                    rvMessage.setAdapter(messageAdapter);
                                    messageAdapter.setClickListener(MainActivity.this);
                                } else {
                                    for (int i = 0; i < jsonObjectArrayList3.size(); i++) {
                                        loadMessageDetail(jsonObjectArrayList3.get(i).getString("id"));
                                    }
                                }
                                // Test
//                                RecyclerView rvMessage = findViewById(R.id.mMessage);
//                                rvMessage.setLayoutManager(new CustomLinearLayoutManager(context));
//                                // Set to Adapter from Data Account
//                                messageAdapter = new MessageAdapter(jsonObjectArrayList3, context);
//                                rvMessage.setAdapter(messageAdapter);
////                                messageAdapter.setClickListener(MainActivity.this);
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

    private void loadMessageDetail(String id) {
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
                                JSONObject object = new JSONObject()
                                        .put("name", jsonObject.getString("name"))
                                        .put("username", jsonObject.getString("name"))
                                        .put("photo", jsonObject.getString("photo"));
                                jsonObjectArrayList4.add(object);
                                // Set to Adapter from Data Account
                                messageAdapter = new MessageAdapter(jsonObjectArrayList4, context);
                                rvMessage.setAdapter(messageAdapter);
                                messageAdapter.setClickListener(MainActivity.this);
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