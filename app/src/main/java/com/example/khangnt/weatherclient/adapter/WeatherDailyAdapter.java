package com.example.khangnt.weatherclient.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.khangnt.weatherclient.R;
import com.example.khangnt.weatherclient.activity.OnItemClickListenerRecycleView;
import com.example.khangnt.weatherclient.model.WeatherData;
import com.thbs.skycons.library.CloudFogView;
import com.thbs.skycons.library.CloudMoonView;
import com.thbs.skycons.library.CloudRainView;
import com.thbs.skycons.library.CloudSnowView;
import com.thbs.skycons.library.CloudSunView;
import com.thbs.skycons.library.CloudView;
import com.thbs.skycons.library.MoonView;
import com.thbs.skycons.library.SunView;
import com.thbs.skycons.library.WindView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WeatherDailyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = "WeatherDailyAdapter";
    private Context mContext;
    private List<WeatherData> data;
    private static OnItemClickListenerRecycleView onItemClickListenerRecycleView;

    public WeatherDailyAdapter(Context context, List<WeatherData> list) {
        this.mContext = context;
        this.data = list;
        onItemClickListenerRecycleView = (OnItemClickListenerRecycleView)mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row2, parent, false);
            return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "position: " + position);
        if (holder instanceof MyViewHolder) {
            String tempMax = data.get(position).getTemperatureMax();
            double tempMaxD = (Double.parseDouble(tempMax) - 32)*(0.5556);

            String tempMin = data.get(position).getTemperatureMin();
            double tempMinD = (Double.parseDouble(tempMin) - 32)*(0.5556);

            String humidity = data.get(position).getRelativeHumidity();
            double humD = (Double.parseDouble(humidity))*100;

            String time = data.get(position).getTime();
            long unixSeconds = Long.parseLong(time);
            // convert seconds to milliseconds
            Date date = new Date(unixSeconds*1000L);
            // the format of your date
            SimpleDateFormat sdf = new SimpleDateFormat("E");
            // give a timezone reference for formatting (see comment at the bottom)
            sdf.setTimeZone(java.util.TimeZone.getTimeZone(data.get(position).getTimeZone()));
            String formattedDate = sdf.format(date);
            String ic = data.get(position).getWeatherIcon();
            Log.d(TAG, "ic: " + ic);

            ((MyViewHolder) holder).txt_temp_max.setText(Math.round(tempMaxD)+"");
            ((MyViewHolder) holder).txt_temp_min.setText(Math.round(tempMinD)+"");
            ((MyViewHolder) holder).txt_humidity.setText(Math.round(humD)+"%");
            ((MyViewHolder) holder).txt_day.setText(formattedDate);
            ((MyViewHolder) holder).viewIc.removeAllViews();
            addWeatherIc(((MyViewHolder) holder).viewIc, ic);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        protected TextView txt_temp_max;
        protected TextView txt_temp_min;
        protected TextView txt_humidity;
        protected LinearLayout viewIc;
        protected TextView txt_day;
        protected View root;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.txt_temp_max = (TextView) itemView.findViewById(R.id.txt_temp_daily_max);
            this.txt_temp_min = (TextView) itemView.findViewById(R.id.txt_temp_daily_min);
            this.txt_humidity = (TextView)itemView.findViewById(R.id.txt_hum_daily);
            this.txt_day = (TextView) itemView.findViewById(R.id.txt_day);
            this.viewIc = (LinearLayout)itemView.findViewById(R.id.img_temp_daily);
            this.root = (LinearLayout)itemView.findViewById(R.id.root_layout_daily);
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListenerRecycleView.onItemClick(getAdapterPosition(), root.getId());
                }
            });
        }
    }

    private void addWeatherIc(LinearLayout parent, String ic)  {
        View view = view = new SunView(mContext,true,false,
                ContextCompat.getColor(mContext, R.color.colorPrimary),
                ContextCompat.getColor(mContext, R.color.transparent));
        LinearLayout.LayoutParams params;

        if (ic.equals("clear-day")) {
            view = new SunView(mContext,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("clear-night")) {
            view = new MoonView(mContext,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("rain")) {
            view = new CloudRainView(mContext,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("snow")) {
            view = new CloudSnowView(mContext,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("sleet")) {
            view = new CloudSnowView(mContext,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("wind")) {
            view = new WindView(mContext,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("fog")) {
            view = new CloudFogView(mContext,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("cloudy")) {
            view = new CloudView(mContext,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("partly-cloudy-day")) {
            view = new CloudSunView(mContext,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        } else if(ic.equals("partly-cloudy-night")) {
            view = new CloudMoonView(mContext,true,false,
                    ContextCompat.getColor(mContext, R.color.colorPrimary),
                    ContextCompat.getColor(mContext, R.color.transparent));
        }

        if (view != null) {
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.width = 160;
            params.height = 160;
            view.setLayoutParams(params);
        }
        parent.addView(view);
    }
}
