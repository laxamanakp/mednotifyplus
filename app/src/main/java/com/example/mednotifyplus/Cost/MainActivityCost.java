package com.example.mednotifyplus.Cost;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mednotifyplus.R;

import java.util.ArrayList;

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
        adapter = new MedicineAdapter(this, medicineList, this, false, true); // false hides Edit/Delete
        recyclerView.setAdapter(adapter);

        // Categories Spinner
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
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnSearch.setOnClickListener(v -> performSearch());

        performSearch(); // Load initial data
    }

    private void performSearch() {
        medicineList.clear();
        String searchText = inputSearch.getText().toString().trim();

        Cursor cursor = dbHelperCost.getMedicines(searchText, selectedCategory, selectedSortOrder);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                double priceThisYear = cursor.getDouble(cursor.getColumnIndexOrThrow("price_this_year"));
                double priceLastYear = cursor.getDouble(cursor.getColumnIndexOrThrow("price_last_year"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                int isFavInt = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite"));
                boolean isFav = isFavInt == 1;
                String instructions = cursor.getString(cursor.getColumnIndexOrThrow("instructions"));
                String reference = cursor.getString(cursor.getColumnIndexOrThrow("reference"));
                int maxIntake = cursor.getInt(cursor.getColumnIndexOrThrow("max_intake"));

                medicineList.add(new Medicine(name, type, priceThisYear, priceLastYear, category, isFav, instructions, reference, maxIntake));
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Toast.makeText(this, "No matching records found.", Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onFavoriteClick(Medicine medicine) {
        boolean newFavStatus = !medicine.isFavorite();
        medicine.setFavorite(newFavStatus);
        dbHelperCost.updateFavorite(medicine.getName(), newFavStatus ? 1 : 0);
        dbHelperCost.logAction("FAVORITE", (newFavStatus ? "Added" : "Removed") + " from favorites: " + medicine.getName());
        adapter.notifyDataSetChanged();
        Toast.makeText(this, medicine.getName() + (newFavStatus ? " added to favorites" : " removed from favorites"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEdit(Medicine medicine) {
        // Optional: Add editable dialog here if you want edit capability in MainActivityCost
    }

    @Override
    public void onDelete(Medicine medicine) {
        // Optional: Implement if deletion from main view is allowed
    }
}
