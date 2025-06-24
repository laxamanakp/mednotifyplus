package com.example.mednotifyplus.Cost;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class AdminActivity extends AppCompatActivity {

    private EditText editSearchName;
    private Spinner spinnerCategoryFilter, spinnerSortOrder;
    private Button btnInsert, btnSync, btnViewLogs;

    private RecyclerView recyclerMedicines;
    private DBHelperCost dbHelper;
    private MedicineAdapter adapter;

    private final ArrayList<Medicine> medicineList = new ArrayList<>();
    private final ArrayList<Medicine> filteredList = new ArrayList<>();

    private String selectedCategory = "All";
    private String selectedSortOrder = "ASC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_pane);

        dbHelper = new DBHelperCost(this);

        editSearchName = findViewById(R.id.editSearchName);
        spinnerCategoryFilter = findViewById(R.id.spinnerCategoryFilter);
        spinnerSortOrder = findViewById(R.id.spinnerSortOrder);
        recyclerMedicines = findViewById(R.id.recyclerMedicines);
        btnInsert = findViewById(R.id.btnInsertMedicine);
        btnSync = findViewById(R.id.btnSyncFirebase);
        btnViewLogs = findViewById(R.id.btnViewLogs);

        recyclerMedicines.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MedicineAdapter(this, filteredList, new MedicineAdapter.OnMedicineActionListener() {
            @Override
            public void onFavoriteClick(Medicine medicine) {
                dbHelper.toggleFavorite(medicine.getName(), !medicine.isFavorite());
                dbHelper.logAction("FAVORITE", "Toggled favorite for " + medicine.getName());
            }

            @Override
            public void onEdit(Medicine medicine) {
                showEditDialog(medicine);
            }

            @Override
            public void onDelete(Medicine medicine) {
                dbHelper.deleteMedicine(medicine.getName());
                dbHelper.logAction("DELETE", "Deleted medicine: " + medicine.getName());
                medicineList.remove(medicine);
                filteredList.remove(medicine);
                adapter.notifyDataSetChanged();
                Toast.makeText(AdminActivity.this, "Medicine Deleted", Toast.LENGTH_SHORT).show();
            }
        }, true, false);

        recyclerMedicines.setAdapter(adapter);

        setupCategorySpinner();
        setupSortSpinner();
        setupSearchListener();

        btnInsert.setOnClickListener(v -> showAddDialog());
        btnSync.setOnClickListener(v -> {
            dbHelper.syncFromFirebase();
            Toast.makeText(this, "Synced with Firebase", Toast.LENGTH_SHORT).show();
        });
        btnViewLogs.setOnClickListener(v -> startActivity(new Intent(this, LogsActivity.class)));

        setupRealTimeListener();
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "Headache", "Cold & Flu", "Allergy", "Stomach", "Vitamins"});
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryFilter.setAdapter(categoryAdapter);
        spinnerCategoryFilter.setSelection(0);
        spinnerCategoryFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupSortSpinner() {
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Price: Low to High", "Price: High to Low"});
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOrder.setAdapter(sortAdapter);
        spinnerSortOrder.setSelection(0);
        spinnerSortOrder.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedSortOrder = (position == 0) ? "ASC" : "DESC";
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupSearchListener() {
        editSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilters() {
        String searchText = editSearchName.getText().toString().toLowerCase().trim();
        filteredList.clear();

        for (Medicine med : medicineList) {
            boolean matchesSearch = med.getName().toLowerCase().contains(searchText);
            boolean matchesCategory = selectedCategory.equals("All") || med.getCategory().equalsIgnoreCase(selectedCategory);
            if (matchesSearch && matchesCategory) {
                filteredList.add(med);
            }
        }

        Collections.sort(filteredList, (o1, o2) -> selectedSortOrder.equals("ASC") ?
                Double.compare(o1.getPriceThisYear(), o2.getPriceThisYear()) :
                Double.compare(o2.getPriceThisYear(), o1.getPriceThisYear()));

        adapter.notifyDataSetChanged();
    }

    private void setupRealTimeListener() {
        dbHelper.getFirebaseRef().addValueEventListener(new ValueEventListener() {
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
                        // skip faulty entry
                    }
                }
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminActivity.this, "Failed to load: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_medicine, null);

        EditText editName = view.findViewById(R.id.editName);
        Spinner spinnerType = view.findViewById(R.id.spinnerType);
        EditText editPriceThisYear = view.findViewById(R.id.editPriceThisYear);
        EditText editPriceLastYear = view.findViewById(R.id.editPriceLastYear);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        EditText editInstructions = view.findViewById(R.id.editInstructions);
        EditText editReference = view.findViewById(R.id.editReference);
        EditText editMaxIntake = view.findViewById(R.id.editMaxIntake);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Generic", "Branded"});
        spinnerType.setAdapter(typeAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Headache", "Cold & Flu", "Allergy", "Stomach", "Vitamins"});
        spinnerCategory.setAdapter(categoryAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add New Medicine")
                .setView(view)
                .setPositiveButton("Save", (dialog, i) -> {
                    try {
                        String name = editName.getText().toString().trim();
                        String type = spinnerType.getSelectedItem().toString();
                        double priceThis = Double.parseDouble(editPriceThisYear.getText().toString().trim());
                        double priceLast = Double.parseDouble(editPriceLastYear.getText().toString().trim());
                        String category = spinnerCategory.getSelectedItem().toString();
                        String instructions = editInstructions.getText().toString().trim();
                        String reference = editReference.getText().toString().trim();
                        int max = Integer.parseInt(editMaxIntake.getText().toString().trim());

                        dbHelper.insertMedicineOfflineAndSync(name, type, priceThis, priceLast, category, instructions, reference, max);
                        dbHelper.logAction("INSERT", "Added new medicine: " + name);

                        Medicine newMed = new Medicine(name, type, priceThis, priceLast, category, false, instructions, reference, max);
                        medicineList.add(newMed);
                        applyFilters();

                        Toast.makeText(this, "Medicine Added", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid input. Please check all fields.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Medicine medicine) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_medicine, null);

        EditText editName = view.findViewById(R.id.editName);
        Spinner spinnerType = view.findViewById(R.id.spinnerType);
        EditText editPriceThisYear = view.findViewById(R.id.editPriceThisYear);
        EditText editPriceLastYear = view.findViewById(R.id.editPriceLastYear);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        EditText editInstructions = view.findViewById(R.id.editInstructions);
        EditText editReference = view.findViewById(R.id.editReference);
        EditText editMaxIntake = view.findViewById(R.id.editMaxIntake);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Generic", "Branded"});
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setSelection(medicine.getType().equalsIgnoreCase("Generic") ? 0 : 1);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Headache", "Cold & Flu", "Allergy", "Stomach", "Vitamins"});
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setSelection(categoryAdapter.getPosition(medicine.getCategory()));

        editName.setText(medicine.getName());
        editPriceThisYear.setText(String.valueOf(medicine.getPriceThisYear()));
        editPriceLastYear.setText(String.valueOf(medicine.getPriceLastYear()));
        editInstructions.setText(medicine.getInstructions());
        editReference.setText(medicine.getReference());
        editMaxIntake.setText(String.valueOf(medicine.getMaxIntakeBeforeConsult()));

        new AlertDialog.Builder(this)
                .setTitle("Edit Medicine")
                .setView(view)
                .setPositiveButton("Update", (dialog, i) -> {
                    try {
                        String newName = editName.getText().toString().trim();
                        String type = spinnerType.getSelectedItem().toString();
                        double priceThis = Double.parseDouble(editPriceThisYear.getText().toString().trim());
                        double priceLast = Double.parseDouble(editPriceLastYear.getText().toString().trim());
                        String category = spinnerCategory.getSelectedItem().toString();
                        String instructions = editInstructions.getText().toString().trim();
                        String reference = editReference.getText().toString().trim();
                        int max = Integer.parseInt(editMaxIntake.getText().toString().trim());

                        dbHelper.updateMedicine(medicine.getName(), newName, type, priceThis, priceLast, category, instructions, reference, max);
                        dbHelper.logAction("UPDATE", "Updated medicine: " + medicine.getName() + " to " + newName);
                        Toast.makeText(this, "Medicine Updated", Toast.LENGTH_SHORT).show();
                        setupRealTimeListener();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to update. Check fields.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
