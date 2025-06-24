package com.example.mednotifyplus.Cost;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mednotifyplus.R;

import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {

    private List<LogEntry> logEntries;

    public LogsAdapter(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log_entry, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry entry = logEntries.get(position);
        holder.textLogTime.setText(entry.getTimestamp());
        holder.textLogAction.setText(entry.getAction());
        holder.textLogDetails.setText(entry.getDetails());
    }

    @Override
    public int getItemCount() {
        return logEntries != null ? logEntries.size() : 0;
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView textLogTime, textLogAction, textLogDetails;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            textLogTime = itemView.findViewById(R.id.textLogTime);
            textLogAction = itemView.findViewById(R.id.textLogAction);
            textLogDetails = itemView.findViewById(R.id.textLogDetails);
        }
    }
}
