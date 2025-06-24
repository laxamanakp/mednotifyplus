package com.example.mednotifyplus.Reminder;

public class Reminder {
    int id;
    String name;
    String dosage;
    long time;

    public Reminder(int id, String name, String dosage, long time) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.time = time;
    }
}