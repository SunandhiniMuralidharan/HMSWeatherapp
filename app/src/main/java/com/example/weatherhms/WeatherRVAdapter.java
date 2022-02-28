package com.example.weatherhms;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Sunandhini Muralidharan
 * @version 1.0
 * @since 23.02.2022
 *
 * WeatherRVAdapter is the Adapter class for Recycler View
 *
 */
public class WeatherRVAdapter extends RecyclerView.Adapter<WeatherRVAdapter.ViewHolder> {

    private Context context;
    private ArrayList<WeatherRVModel> weatherRVModelArray;

    public WeatherRVAdapter(Context context, ArrayList<WeatherRVModel> weatherRVModelArray) {
        this.context = context;
        this.weatherRVModelArray = weatherRVModelArray;
    }

    @NonNull
    @Override
    public WeatherRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherRVAdapter.ViewHolder holder, int position) {

        WeatherRVModel model = weatherRVModelArray.get(position);
        holder.temperatureTV.setText(model.getTemperature()+"°c");
        Picasso.get().load("https:".concat(model.getIcon())).into(holder.conditionIV);
        holder.windTV.setText(model.getText());
        SimpleDateFormat input = new SimpleDateFormat("yyyy-mm-dd");
        SimpleDateFormat output = new SimpleDateFormat("mm-dd");
        try{
            Date t = input.parse(model.getDate());
            holder.timeTV.setText(output.format(t));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return weatherRVModelArray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView timeTV,temperatureTV,windTV;
        private ImageView conditionIV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTV = itemView.findViewById(R.id.idTVTime);
            temperatureTV = itemView.findViewById(R.id.idTVTemperature);
            windTV = itemView.findViewById(R.id.idTVWind);
            conditionIV = itemView.findViewById(R.id.idIVCondition);
        }
    }
}

