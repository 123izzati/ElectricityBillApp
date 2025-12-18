package com.example.electricitybillapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditBillActivity extends AppCompatActivity {

    private TextView textViewMonth;
    private EditText editTextUnits, editTextRebate;
    private Button buttonUpdate, buttonCancel;

    private int billId;
    private String month;
    private BillDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bill);

        initializeViews();
        dbHelper = new BillDatabaseHelper(this);

        // Get data from intent
        billId = getIntent().getIntExtra("billId", -1);
        month = getIntent().getStringExtra("month");
        double units = getIntent().getDoubleExtra("units", 0);
        double rebate = getIntent().getDoubleExtra("rebate", 0);

        textViewMonth.setText("Editing Bill for: " + month);
        editTextUnits.setText(String.valueOf(units));
        editTextRebate.setText(String.valueOf(rebate));

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBill();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeViews() {
        textViewMonth = findViewById(R.id.textViewMonth);
        editTextUnits = findViewById(R.id.editTextUnits);
        editTextRebate = findViewById(R.id.editTextRebate);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonCancel = findViewById(R.id.buttonCancel);
    }

    private void updateBill() {
        String unitsStr = editTextUnits.getText().toString();
        String rebateStr = editTextRebate.getText().toString();

        if (unitsStr.isEmpty() || rebateStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double newUnits = Double.parseDouble(unitsStr);
            double newRebate = Double.parseDouble(rebateStr);

            if (newUnits <= 0) {
                editTextUnits.setError("Units must be greater than 0");
                return;
            }

            if (newRebate < 0 || newRebate > 100) {
                editTextRebate.setError("Rebate must be between 0 and 100");
                return;
            }

            // Recalculate charges using the same tariff formula
            double newTotalCharges = calculateTariff(newUnits);
            double newFinalCost = newTotalCharges - (newTotalCharges * (newRebate/100));

            // Update database
            boolean updated = dbHelper.updateBill(billId, newUnits, newRebate,
                    newTotalCharges, newFinalCost);

            if (updated) {
                Toast.makeText(this, "Bill updated successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to update bill", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
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
}