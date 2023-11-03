package com.yuuna.schat.adapter;

import static com.yuuna.schat.util.Client.BASE_PHOTO;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yuuna.schat.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
        try {
            String photo = BASE_PHOTO + listMessage.get(position).getString("photo");
            if (!photo.equals(BASE_PHOTO)) Glide.with(mContext)
                    .load(photo)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.civPhoto);
            else holder.civPhoto.setImageResource(R.drawable.photo);
            holder.tvName.setText(listMessage.get(position).getString("name"));
            holder.tvContent.setText(listMessage.get(position).getString("last_message"));
            Long lTime = jsonObjectDataList.get(position).getLong("last_time");
            String sTime = new SimpleDateFormat("HH:mm").format(new Date(lTime * 1000L));
            holder.tvTime.setText(sTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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
        private TextView tvName, tvContent, tvTime;

        public Holder(View itemView) {
            super(itemView);
            civPhoto = itemView.findViewById(R.id.mPhoto);
            tvName = itemView.findViewById(R.id.mName);
            tvContent = itemView.findViewById(R.id.mContent);
            tvTime = itemView.findViewById(R.id.mTime);
            itemView.findViewById(R.id.mButton).setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(listMessage.get(getBindingAdapterPosition()), v);
            });
        }
    }
}
