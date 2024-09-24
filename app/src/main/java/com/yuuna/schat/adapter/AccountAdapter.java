package com.yuuna.schat.adapter;

import static com.yuuna.schat.util.Client.BASE_PHOTO;
import static com.yuuna.schat.util.Client.BASE_URL;
import static com.yuuna.schat.util.AppConstants.TAG_KEY;
import static com.yuuna.schat.util.AppConstants.SCHAT;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuuna.schat.R;
import com.yuuna.schat.util.Client;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.Holder> {

    private ArrayList<JSONObject> jsonObjectDataList;
    private Context mContext;
    private ItemClickListener clickListener;

    public AccountAdapter(ArrayList<JSONObject> jsonObjectArrayList, Context context) {
        this.jsonObjectDataList = jsonObjectArrayList;
        this.mContext = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        String key = mContext.getSharedPreferences(SCHAT, Context.MODE_PRIVATE).getString(TAG_KEY, "");
        try {
            profile(jsonObjectDataList.get(position).getString("key"), holder.civPhoto);
            holder.ivSelect.setVisibility(key.equals(jsonObjectDataList.get(position).getString("key")) ? View.VISIBLE : View.GONE);
            holder.tvName.setText(jsonObjectDataList.get(position).getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void profile(String key, CircleImageView civPhoto) {
        String profile = "{\"request\":\"profile\",\"data\":{\"key\":\""+key+"\"}}";
        JsonObject jsonObject = JsonParser.parseString(profile).getAsJsonObject();
        try {
            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
                @Override
                public void onSuccess(String response) {
                    ((Activity) mContext).runOnUiThread(() -> {
                        // Response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.getBoolean("status")) {
                                String photo = BASE_PHOTO + jsonObject.getString("photo");
                                if (!photo.equals(BASE_PHOTO)) Glide.with(mContext)
                                        .load(photo)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(civPhoto);
                                else civPhoto.setImageResource(R.drawable.photo);
                            } else Toast.makeText(mContext, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
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
    public int getItemCount() {
        return jsonObjectDataList.size();
    }

    public interface ItemClickListener {
        void onItemClick(JSONObject jsonObject, View view);
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public class Holder extends RecyclerView.ViewHolder {
        private CircleImageView civPhoto;
        private ImageView ivSelect;
        private TextView tvName;

        public Holder(View itemView) {
            super(itemView);
            civPhoto = itemView.findViewById(R.id.aPhoto);
            ivSelect = itemView.findViewById(R.id.aSelect);
            tvName = itemView.findViewById(R.id.aName);
            itemView.findViewById(R.id.aButton).setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(jsonObjectDataList.get(getBindingAdapterPosition()), v);
            });
        }
    }
}
