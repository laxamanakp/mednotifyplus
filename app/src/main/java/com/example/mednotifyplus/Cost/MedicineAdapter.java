package com.example.mednotifyplus.Cost;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mednotifyplus.R;

import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedViewHolder> {

    private List<Medicine> medicines;
    private final Context context;
    private final OnMedicineActionListener listener;
    private final boolean showEditDelete;
    private final boolean showFavorite;

    public interface OnMedicineActionListener {
        void onFavoriteClick(Medicine medicine);
        void onEdit(Medicine medicine);
        void onDelete(Medicine medicine);
    }

    /**
     * @param context         The context.
     * @param medicines       List of medicines to bind.
     * @param listener        Action callbacks for edit/delete/favorite (can be null if unused).
     * @param showEditDelete  Whether to show edit/delete buttons.
     * @param showFavorite    Whether to show the favorite button.
     */
    public MedicineAdapter(Context context, List<Medicine> medicines, OnMedicineActionListener listener, boolean showEditDelete, boolean showFavorite) {
        this.context = context;
        this.medicines = medicines;
        this.listener = listener;
        this.showEditDelete = showEditDelete;
        this.showFavorite = showFavorite;
    }

    @NonNull
    @Override
    public MedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.medicine_item, parent, false);
        return new MedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedViewHolder holder, int position) {
        Medicine med = medicines.get(position);

        holder.textName.setText(med.getName());
        holder.textType.setText(med.getType());
        holder.textPrice.setText(String.format("₱%.2f", med.getPriceThisYear()));

        // Handle favorite
        if (showFavorite) {
            holder.imageFavorite.setVisibility(View.VISIBLE);
            holder.imageFavorite.setImageResource(
                    med.isFavorite() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
            );
            holder.imageFavorite.setOnClickListener(v -> {
                if (listener != null) listener.onFavoriteClick(med);
            });
        } else {
            holder.imageFavorite.setVisibility(View.GONE);
        }

        // Handle detail view
        holder.textName.setOnClickListener(v -> {
            Intent intent = new Intent(context, MedicineDetailActivity.class);
            intent.putExtra("name", med.getName());
            intent.putExtra("type", med.getType());
            intent.putExtra("price_this_year", med.getPriceThisYear());
            intent.putExtra("price_last_year", med.getPriceLastYear());
            intent.putExtra("category", med.getCategory());
            intent.putExtra("instructions", med.getInstructions());
            intent.putExtra("reference", med.getReference());
            intent.putExtra("max_intake", med.getMaxIntakeBeforeConsult());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

        // Handle edit/delete
        if (showEditDelete) {
            holder.imageEdit.setVisibility(View.VISIBLE);
            holder.imageDelete.setVisibility(View.VISIBLE);

            holder.imageEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(med);
            });

            holder.imageDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(med);
            });
        } else {
            holder.imageEdit.setVisibility(View.GONE);
            holder.imageDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    /**
     * This method allows the adapter to refresh its data.
     * Call this from MainActivityCost after filtering.
     */
    public void updateList(List<Medicine> newList) {
        this.medicines = newList;
        notifyDataSetChanged();
    }

    static class MedViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textType, textPrice;
        ImageView imageFavorite, imageEdit, imageDelete;

        public MedViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textType = itemView.findViewById(R.id.textType);
            textPrice = itemView.findViewById(R.id.textPrice);
            imageFavorite = itemView.findViewById(R.id.imageFavorite);
            imageEdit = itemView.findViewById(R.id.imageEdit);
            imageDelete = itemView.findViewById(R.id.imageDelete);
        }
    }
}
