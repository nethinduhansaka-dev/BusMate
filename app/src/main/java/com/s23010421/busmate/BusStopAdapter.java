package com.s23010421.busmate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * BusStopAdapter - RecyclerView adapter for bus stop information
 */
public class BusStopAdapter extends RecyclerView.Adapter<BusStopAdapter.ViewHolder> {

    private List<BusRouteInformationActivity.BusStop> stops;
    private OnStopClickListener clickListener;

    public interface OnStopClickListener {
        void onStopSelected(BusRouteInformationActivity.BusStop stop);
    }

    public BusStopAdapter(List<BusRouteInformationActivity.BusStop> stops, OnStopClickListener listener) {
        this.stops = stops;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bus_stop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusRouteInformationActivity.BusStop stop = stops.get(position);
        holder.bind(stop);
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView stopName, stopDescription, nextArrival;

        ViewHolder(View itemView) {
            super(itemView);
            stopName = itemView.findViewById(R.id.textViewStopName);
            stopDescription = itemView.findViewById(R.id.textViewStopDescription);
            nextArrival = itemView.findViewById(R.id.textViewNextArrival);
        }

        void bind(BusRouteInformationActivity.BusStop stop) {
            stopName.setText(stop.getName());
            stopDescription.setText(stop.getDescription());
            nextArrival.setText("Next: " + stop.getNextArrival());

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onStopSelected(stop);
                }
            });
        }
    }
}
