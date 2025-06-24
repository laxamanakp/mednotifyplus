package com.example.mednotifyplus.Cost;

public class LogEntry {
    private String timestamp;
    private String action;
    private String details;

    public LogEntry(String timestamp, String action, String details) {
        this.timestamp = timestamp;
        this.action = action;
        this.details = details;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }
}
