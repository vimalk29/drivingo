package com.example.drivingo.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.drivingo.Activities.BikeDetails;
import com.example.drivingo.Activities.BookedBikeDetails;
import com.example.drivingo.Common.CommonValues;
import com.example.drivingo.R;
import com.example.drivingo.model.Bike;
import com.example.drivingo.model.BookedBike;
import com.example.drivingo.model.Booking;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookedBikeAdapter extends RecyclerView.Adapter<BookedBikeAdapter.ViewHolder> {
    private Context context;
    private List<BookedBike> list;

    public BookedBikeAdapter(Context context, List<BookedBike> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.booked_bike_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        BookedBike bookedBike = list.get(i);
        Bike bike = bookedBike.getBike();
        Booking booking = bookedBike.getBooking();
        viewHolder.BikeModel.setText(bike.getModel());
        viewHolder.Date.setText(booking.getDate());
        Picasso.with(context).load(bike.getImage()).into(viewHolder.imageView);
        bookedBike.setTotalRent(calCalculateRent(booking.getFrom(), booking.getTo(), bike.getRent()));
        viewHolder.Rent.setText(bookedBike.getTotalRent());
    }

    private String calCalculateRent(String from, String to, int rentHrs) {
        int rent = 0;
        try {
            Date dateFrom = BikeDetails.dateFormat.parse(from);
            Date dateTo = BikeDetails.dateFormat.parse(to);
            long time = dateTo.getTime() - dateFrom.getTime();
            int hrs = (int) (time / (60 * 60 * 1000)) + 1;
            rent = rentHrs * hrs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(rent);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView BikeModel, Date, Rent;
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            BikeModel = itemView.findViewById(R.id.tv_bikeModel);
            Date = itemView.findViewById(R.id.tvDate);
            Rent = itemView.findViewById(R.id.tv_rent);
            imageView = itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                CommonValues.commonBookedList = list;
                Intent intent = new Intent(context, BookedBikeDetails.class);
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        }
    }
}
