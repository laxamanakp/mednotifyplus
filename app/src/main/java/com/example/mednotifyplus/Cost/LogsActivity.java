package com.example.mednotifyplus.Cost;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mednotifyplus.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LogsActivity extends AppCompatActivity {

    private RecyclerView recyclerLogs;
    private LogsAdapter logAdapter;
    private List<LogEntry> logList = new ArrayList<>();

    private DatabaseReference logsRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs); // Make sure recyclerLogs exists in this layout

        recyclerLogs = findViewById(R.id.recyclerLogs);
        recyclerLogs.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new LogsAdapter(logList);
        recyclerLogs.setAdapter(logAdapter);

        // Initialize Firebase reference
        logsRef = FirebaseDatabase.getInstance().getReference("logs");

        fetchLogsFromFirebase();
    }

    private void fetchLogsFromFirebase() {
        logsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                logList.clear();
                for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                    String timestamp = logSnapshot.child("timestamp").getValue(String.class);
                    String action = logSnapshot.child("action").getValue(String.class);
                    String details = logSnapshot.child("details").getValue(String.class);

                    if (timestamp != null && action != null && details != null) {
                        logList.add(new LogEntry(timestamp, action, details));
                    }
                }
                logAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("LogsActivity", "Failed to load logs: " + error.getMessage());
            }
        });
    }
}
