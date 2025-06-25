package com.example.mednotifyplus.Cost;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mednotifyplus.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivityCost extends AppCompatActivity implements MedicineAdapter.OnMedicineActionListener {

    EditText inputSearch;
    Spinner spinnerCategory, spinnerSortOrder;
    Button btnSearch;
    RecyclerView recyclerView;
    DBHelperCost dbHelperCost;
    MedicineAdapter adapter;
    ArrayList<Medicine> medicineList = new ArrayList<>();
    String selectedCategory = "All";
    String selectedSortOrder = "ASC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_cost);

        inputSearch = findViewById(R.id.inputSearch);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerSortOrder = findViewById(R.id.spinnerSortOrder);
        btnSearch = findViewById(R.id.btnSearch);
        recyclerView = findViewById(R.id.recyclerView);

        dbHelperCost = new DBHelperCost(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineAdapter(this, medicineList, this, false, true);
        recyclerView.setAdapter(adapter);

        // Category Spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "Headache", "Cold & Flu", "Allergy", "Stomach", "Vitamins"});
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setSelection(0);
        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Sort Order Spinner
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Price: Low to High", "Price: High to Low"});
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOrder.setAdapter(sortAdapter);
        spinnerSortOrder.setSelection(0);
        spinnerSortOrder.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedSortOrder = (position == 0) ? "ASC" : "DESC";
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnSearch.setOnClickListener(v -> applyFilters()); // just refilters the already synced Firebase list

        setupRealTimeListener(); // Load from Firebase
    }

    private void setupRealTimeListener() {
        dbHelperCost.getFirebaseRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicineList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    try {
                        String name = child.child("name").getValue(String.class);
                        String type = child.child("type").getValue(String.class);
                        Double priceThis = child.child("price_this_year").getValue(Double.class);
                        Double priceLast = child.child("price_last_year").getValue(Double.class);
                        String category = child.child("category").getValue(String.class);
                        String instructions = child.child("instructions").getValue(String.class);
                        String reference = child.child("reference").getValue(String.class);
                        Integer max = child.child("max_intake").getValue(Integer.class);

                        if (name != null && type != null && priceThis != null && priceLast != null &&
                                category != null && instructions != null && reference != null && max != null) {
                            Medicine med = new Medicine(name, type, priceThis, priceLast, category, false, instructions, reference, max);
                            medicineList.add(med);
                        }
                    } catch (Exception e) {
                        // Skip invalid entries
                    }
                }
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivityCost.this, "Failed to sync Firebase data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String searchText = inputSearch.getText().toString().trim().toLowerCase();
        ArrayList<Medicine> filteredList = new ArrayList<>();

        for (Medicine med : medicineList) {
            boolean matchesCategory = selectedCategory.equals("All") || med.getCategory().equalsIgnoreCase(selectedCategory);
            boolean matchesSearch = searchText.isEmpty() || med.getName().toLowerCase().contains(searchText);
            if (matchesCategory && matchesSearch) {
                filteredList.add(med);
            }
        }

        // Sort by price_this_year
        Collections.sort(filteredList, new Comparator<Medicine>() {
            @Override
            public int compare(Medicine a, Medicine b) {
                return selectedSortOrder.equals("ASC") ?
                        Double.compare(a.getPriceThisYear(), b.getPriceThisYear()) :
                        Double.compare(b.getPriceThisYear(), a.getPriceThisYear());
            }
        });

        adapter.updateList(filteredList);
    }

    @Override
    public void onFavoriteClick(Medicine medicine) {
        boolean newFavStatus = !medicine.isFavorite();
        medicine.setFavorite(newFavStatus);
        dbHelperCost.updateFavorite(medicine.getName(), newFavStatus ? 1 : 0);
        dbHelperCost.logAction("FAVORITE", (newFavStatus ? "Added" : "Removed") + " from favorites: " + medicine.getName());
        Toast.makeText(this, medicine.getName() + (newFavStatus ? " added to favorites" : " removed from favorites"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEdit(Medicine medicine) {
        // Optional: Implement edit dialog here
    }

    @Override
    public void onDelete(Medicine medicine) {
        // Optional: Implement delete confirmation here
    }
}
