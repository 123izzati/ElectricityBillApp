package com.example.electricitybillapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText editTextUnits;
    private RadioGroup radioGroupRebate;
    private TextView textViewTotalCharges, textViewFinalCost;

    private Button btnCalculate, btnSave;
    private LinearLayout btnViewBills, btnAbout;

    private BillDatabaseHelper databaseHelper;

    private double totalCharges = 0;
    private double finalCost = 0;
    private double rebatePercentage = 0;

    private String[] allMonths = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        databaseHelper = new BillDatabaseHelper(this);
        setupMonthSpinner();

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBill();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBill();
            }
        });

        btnViewBills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Intent intent =
                        new android.content.Intent(MainActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Intent intent =
                        new android.content.Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh month list when returning from ListActivity or AboutActivity
        setupMonthSpinner();
    }

    private void initializeViews() {
        spinnerMonth = findViewById(R.id.spinnerMonth);
        editTextUnits = findViewById(R.id.editTextUnits);
        radioGroupRebate = findViewById(R.id.radioGroupRebate);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewFinalCost = findViewById(R.id.textViewFinalCost);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnSave = findViewById(R.id.btnSave);

        btnViewBills = findViewById(R.id.btnViewBills);
        btnAbout = findViewById(R.id.btnAbout);
    }

    private void setupMonthSpinner() {
        // Get used months from database
        List<String> usedMonths = databaseHelper.getAllUsedMonths();

        // Create a list of available months (not used yet)
        List<String> availableMonths = new ArrayList<>();
        for (String month : allMonths) {
            if (!usedMonths.contains(month)) {
                availableMonths.add(month);
            }
        }

        // If all months are used, show a message
        if (availableMonths.isEmpty()) {
            availableMonths.add("No months available");
            btnSave.setEnabled(false);
            Toast.makeText(this, "All months already have bills!", Toast.LENGTH_LONG).show();
        } else {
            btnSave.setEnabled(true);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, availableMonths);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);
    }

    private void calculateBill() {
        if (editTextUnits.getText().toString().isEmpty()) {
            editTextUnits.setError("Please enter electricity units");
            return;
        }

        double units = Double.parseDouble(editTextUnits.getText().toString());
        if (units <= 0) {
            editTextUnits.setError("Units must be greater than 0");
            return;
        }

        int selectedId = radioGroupRebate.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select rebate percentage", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedId);
        String rebateText = selectedRadio.getText().toString();
        rebatePercentage = Double.parseDouble(rebateText.replace("%", "")) / 100;

        totalCharges = calculateTariff(units);
        finalCost = totalCharges - (totalCharges * rebatePercentage);

        textViewTotalCharges.setText(String.format("Total Charges: RM %.2f", totalCharges));
        textViewFinalCost.setText(String.format("Final Cost: RM %.2f", finalCost));
    }

    private double calculateTariff(double units) {
        double charges;

        if (units <= 200) {
            charges = units * 0.218;
        } else if (units <= 300) {
            charges = (200 * 0.218) + ((units - 200) * 0.334);
        } else if (units <= 600) {
            charges = (200 * 0.218) + (100 * 0.334) + ((units - 300) * 0.516);
        } else if (units <= 900) {
            charges = (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((units - 600) * 0.546);
        } else {
            charges = (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + (300 * 0.546) + ((units - 900) * 0.571);
        }

        return charges;
    }

    private void saveBill() {
        if (totalCharges == 0) {
            Toast.makeText(this, "Please calculate bill first", Toast.LENGTH_SHORT).show();
            return;
        }

        String month = spinnerMonth.getSelectedItem().toString();

        // Check if month is already used (just in case)
        List<String> usedMonths = databaseHelper.getAllUsedMonths();
        if (usedMonths.contains(month)) {
            Toast.makeText(this, "This month already has a bill saved!", Toast.LENGTH_SHORT).show();
            return;
        }

        double units = Double.parseDouble(editTextUnits.getText().toString());

        databaseHelper.addBill(month, units, rebatePercentage * 100,
                totalCharges, finalCost);

        Toast.makeText(this, "Bill for " + month + " saved successfully!", Toast.LENGTH_SHORT).show();

        // Reset form
        editTextUnits.setText("");
        radioGroupRebate.clearCheck();
        textViewTotalCharges.setText("Total Charges: RM 0.00");
        textViewFinalCost.setText("Final Cost: RM 0.00");
        totalCharges = 0;
        finalCost = 0;

        // Refresh the month spinner to remove the saved month
        setupMonthSpinner();
    }
}