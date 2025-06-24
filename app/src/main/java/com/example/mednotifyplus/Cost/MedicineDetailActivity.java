package com.example.mednotifyplus.Cost;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mednotifyplus.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MedicineDetailActivity extends AppCompatActivity {

    TextView textName, textType, textPrice, textPriceLastYear, textCategory, textInstructions, textReference, textMaxIntake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_detail);

        textName = findViewById(R.id.textDetailName);
        textType = findViewById(R.id.textDetailType);
        textPrice = findViewById(R.id.textDetailPrice);
        textPriceLastYear = findViewById(R.id.textDetailPriceLastYear);
        textCategory = findViewById(R.id.textDetailCategory);
        textInstructions = findViewById(R.id.textDetailInstructions);
        textReference = findViewById(R.id.textDetailReference);
        textMaxIntake = findViewById(R.id.textDetailMaxIntake);

        // Get medicine name from intent
        String medicineName = getIntent().getStringExtra("name");

        if (medicineName != null && !medicineName.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("medicines")
                    .child(medicineName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String type = snapshot.child("type").getValue(String.class);
                                Double priceThis = snapshot.child("price_this_year").getValue(Double.class);
                                Double priceLast = snapshot.child("price_last_year").getValue(Double.class);
                                String category = snapshot.child("category").getValue(String.class);
                                String instructions = snapshot.child("instructions").getValue(String.class);
                                String reference = snapshot.child("reference").getValue(String.class);
                                Integer maxIntake = snapshot.child("max_intake").getValue(Integer.class);

                                textName.setText("Name: " + medicineName);
                                textType.setText("Type: " + (type != null ? type : ""));
                                textPrice.setText(String.format("Price This Year: ₱%.2f", priceThis != null ? priceThis : 0.0));
                                textPriceLastYear.setText(String.format("Price Last Year: ₱%.2f", priceLast != null ? priceLast : 0.0));
                                textCategory.setText("Category: " + (category != null ? category : ""));
                                textInstructions.setText("Instructions:\n" + (instructions != null ? instructions : ""));
                                textReference.setText("Reference:\n" + (reference != null ? reference : ""));
                                textMaxIntake.setText("Max Intake Before Consult: " + (maxIntake != null ? maxIntake : 0) + " dose(s)");
                            } else {
                                Toast.makeText(MedicineDetailActivity.this, "Medicine not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MedicineDetailActivity.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Invalid medicine name", Toast.LENGTH_SHORT).show();
        }
    }
}
