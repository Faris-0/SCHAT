package com.yuuna.schat.adapter;

import static com.yuuna.schat.util.Client.BASE_PHOTO;
import static com.yuuna.schat.util.Client.BASE_URL;
import static com.yuuna.schat.util.SharedPref.SCHAT;
import static com.yuuna.schat.util.SharedPref.TAG_KEY;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuuna.schat.R;
import com.yuuna.schat.util.Client;
import com.yuuna.schat.util.CustomLinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> implements Filterable {

    private ArrayList<JSONObject> jsonObjectDataList, listMessage;
    private Context mContext;
    private ItemClickListener clickListener;

    public MessageAdapter(ArrayList<JSONObject> jsonObjectArrayList4, Context context) {
        this.jsonObjectDataList = jsonObjectArrayList4;
        this.mContext = context;
        this.listMessage = jsonObjectArrayList4;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
//        String key = mContext.getSharedPreferences(SCHAT, Context.MODE_PRIVATE).getString(TAG_KEY, "");
        try {
//            detail(key, listMessage.get(position).getString("id"), holder.civPhoto, holder.tvName);
            String photo = BASE_PHOTO + listMessage.get(position).getString("photo");
            if (!photo.equals(BASE_PHOTO)) Glide.with(mContext)
                    .load(photo)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.civPhoto);
            else holder.civPhoto.setImageResource(R.drawable.photo);
            holder.tvName.setText(listMessage.get(position).getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    private void detail(String key, String id, CircleImageView civPhoto, TextView tvName) {
//        String message_detail = "{\"request\":\"message_detail\",\"data\":{\"key\":\""+key+"\",\"id\":\""+id+"\"}}";
//        JsonObject jsonObject = JsonParser.parseString(message_detail).getAsJsonObject();
//        try {
//            new Client().getOkHttpClient(BASE_URL, String.valueOf(jsonObject), new Client.OKHttpNetwork() {
//                @Override
//                public void onSuccess(String response) {
//                    ((Activity) mContext).runOnUiThread(() -> {
//                        // Response
//                        try {
//                            JSONObject jsonObject = new JSONObject(response);
//                            if (jsonObject.getBoolean("status")) {
//                                String photo = BASE_PHOTO + jsonObject.getString("photo");
//                                if (!photo.equals(BASE_PHOTO)) Glide.with(mContext)
//                                        .load(photo)
//                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
//                                        .skipMemoryCache(true)
//                                        .into(civPhoto);
//                                else civPhoto.setImageResource(R.drawable.photo);
//                                tvName.setText(jsonObject.getString("name"));
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    });
//                }
//
//                @Override
//                public void onFailure(IOException e) {
//                    e.printStackTrace();
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public int getItemCount() {
        return listMessage.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String searchString = charSequence.toString().toLowerCase().trim();

                if (searchString.isEmpty()) listMessage = jsonObjectDataList;
                else {
                    ArrayList<JSONObject> tempFilteredList = new ArrayList<>();
                    for (JSONObject jsonObject : jsonObjectDataList) {
                        try {
                            if (jsonObject.getString("name").toLowerCase().contains(searchString)) tempFilteredList.add(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    listMessage = tempFilteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = listMessage;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                listMessage = (ArrayList<JSONObject>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface ItemClickListener {
        void onItemClick(JSONObject jsonObject, View view);
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public class Holder extends RecyclerView.ViewHolder {
        private CircleImageView civPhoto;
        private TextView tvName, tvContent;

        public Holder(View itemView) {
            super(itemView);
            civPhoto = itemView.findViewById(R.id.mPhoto);
            tvName = itemView.findViewById(R.id.mName);
            tvContent = itemView.findViewById(R.id.mContent);
            itemView.findViewById(R.id.mButton).setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(listMessage.get(getBindingAdapterPosition()), v);
            });
        }
    }
}