package com.example.khangnt.weatherclient.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.khangnt.weatherclient.R;
import com.example.khangnt.weatherclient.activity.OnItemClickListenerRecycleView;
import com.example.khangnt.weatherclient.model.City;

import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = "CityAdapter";
    private Context mContext;
    private List<City> data;
    private static OnItemClickListenerRecycleView onItemClickListenerRecycleView;

    public CityAdapter(Context context, List<City> list) {
        this.mContext = context;
        this.data = list;
        onItemClickListenerRecycleView = (OnItemClickListenerRecycleView)mContext;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row3, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "position: " + position);
        if (holder instanceof MyViewHolder) {
            String name = data.get(position).getName();
            String detail = data.get(position).getDetail();
            ((MyViewHolder) holder).txt_city.setText(name);
            ((MyViewHolder) holder).txt_city_detail.setText(detail);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        protected TextView txt_city;
        protected TextView txt_city_detail;
        protected View root;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.txt_city = (TextView) itemView.findViewById(R.id.txt_city);
            this.txt_city_detail = (TextView) itemView.findViewById(R.id.txt_city_detail);
            this.root = (RelativeLayout)itemView.findViewById(R.id.root_layout_city);
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListenerRecycleView.onItemClick(getAdapterPosition(), root.getId());
                }
            });
        }
    }
}
