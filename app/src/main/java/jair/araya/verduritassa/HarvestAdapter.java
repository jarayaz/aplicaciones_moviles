package jair.araya.verduritassa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class HarvestAdapter extends RecyclerView.Adapter<HarvestAdapter.HarvestViewHolder> {
    private ArrayList<Harvest> harvests;
    private OnSettingsClickListener listener;

    public interface OnSettingsClickListener {
        void onSettingsClick(Harvest harvest);
    }

    public HarvestAdapter(ArrayList<Harvest> harvests, OnSettingsClickListener listener) {
        this.harvests = harvests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HarvestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_harvest, parent, false);
        return new HarvestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HarvestViewHolder holder, int position) {
        Harvest harvest = harvests.get(position);
        holder.cropName.setText(harvest.getCropName());
        holder.harvestDate.setText(harvest.getHarvestDate());
        holder.settingsButton.setOnClickListener(v -> listener.onSettingsClick(harvest));
    }

    @Override
    public int getItemCount() {
        return harvests.size();
    }

    public void removeHarvest(Harvest harvest) {
        int position = harvests.indexOf(harvest);
        if (position != -1) {
            harvests.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class HarvestViewHolder extends RecyclerView.ViewHolder {
        TextView cropName;
        TextView harvestDate;
        ImageButton settingsButton;

        HarvestViewHolder(View itemView) {
            super(itemView);
            cropName = itemView.findViewById(R.id.cropName);
            harvestDate = itemView.findViewById(R.id.harvestDate);
            settingsButton = itemView.findViewById(R.id.settingsButton);
        }
    }
}