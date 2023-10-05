package com.yuuna.schat.adapter;

import static com.yuuna.schat.util.SharedPref.TAG_KEY;
import static com.yuuna.schat.util.SharedPref.SCHAT;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.yuuna.schat.R;

import org.json.JSONException;
import org.json.JSONObject;

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
            if (key.equals(jsonObjectDataList.get(position).getString("key"))) holder.ivSelect.setVisibility(View.VISIBLE);
            else holder.ivSelect.setVisibility(View.GONE);
            holder.tvName.setText(jsonObjectDataList.get(position).getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return jsonObjectDataList.size();
    }

    public interface ItemClickListener {
        void onItemClick(JSONObject jsonObject);
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
            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(jsonObjectDataList.get(getBindingAdapterPosition()));
            });
//            itemView.setOnClickListener(v -> {
//                try {
//                    setKey = jsonObjectDataList.get(getBindingAdapterPosition()).getString("key");
//                    setTag = jsonObjectDataList.get(getBindingAdapterPosition()).getString("tag");
//                    setName = jsonObjectDataList.get(getBindingAdapterPosition()).getString("name");
//                    notifyDataSetChanged();
//                    dMenu.dismiss();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            });
        }
    }
}
