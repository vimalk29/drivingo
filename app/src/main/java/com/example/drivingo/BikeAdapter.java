package com.example.drivingo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.drivingo.model.Bike;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

public class BikeAdapter extends RecyclerView.Adapter<BikeAdapter.ViewHolder>{
    Context context;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View bikeView = inflater.inflate(R.layout.bike_list_item,viewGroup,false);
        ViewHolder viewHolder = new ViewHolder(context,bikeView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Bike bike = mBikes.get(i);
        TextView model = viewHolder.BikeModel;
        TextView distance = viewHolder.Distance;
        ImageView imageView = viewHolder.imageView;

        model.setText(bike.getModel().toUpperCase());
        int TotalMeter = (int)(bike.getDistance()*1000);
        int km = TotalMeter/1000;
        int mtr = TotalMeter%1000;
        String Distance="Distance : "+km+"Km "+mtr+" Mtr";
        distance.setText(Distance);

        Picasso.with(context).load(bike.getImage())
                .placeholder(R.drawable.icon_bike)
                .fit()
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return mBikes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView BikeModel,Distance;
        public ImageView imageView;
        private Context context;
        public ViewHolder(Context context,@NonNull View itemView) {
            super(itemView);
            BikeModel = itemView.findViewById(R.id.tv_bikeModel);
            Distance = itemView.findViewById(R.id.tv_distance);
            imageView = itemView.findViewById(R.id.imageView);
            this.context = context;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position!=RecyclerView.NO_POSITION){
                Bike bike = mBikes.get(position);
                Intent intent = new Intent(context,BikeDetails.class);
                intent.putExtra("model",bike.getModel());
                intent.putExtra("bikeNo",bike.getBikeNo());
                intent.putExtra("image",bike.getImage());
                intent.putExtra("distance",bike.getDistance());
                intent.putExtra("location",bike.getLocation());
                context.startActivity(intent);
                //Toast.makeText(context, bike.getModel(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private List<Bike> mBikes;

    public BikeAdapter(List<Bike> mBikes) {
        this.mBikes = mBikes;
    }

}
