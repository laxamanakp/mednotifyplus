package com.example.mednotifyplus.Reminder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mednotifyplus.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
    Context context;
    ArrayList<Reminder> list;

    public ReminderAdapter(Context context, ArrayList<Reminder> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = list.get(position);
        holder.tvName.setText(reminder.name);
        holder.tvDosage.setText(reminder.dosage);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String formattedTime = sdf.format(new Date(reminder.time));
        holder.tvTime.setText(formattedTime);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditReminderActivity.class);
            intent.putExtra("id", reminder.id);
            intent.putExtra("name", reminder.name);
            intent.putExtra("dosage", reminder.dosage);
            intent.putExtra("time", reminder.time);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}