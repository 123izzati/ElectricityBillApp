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
import android.widget.LinearLayout;   // ✅ ADD THIS
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText editTextUnits;
    private RadioGroup radioGroupRebate;
    private TextView textViewTotalCharges, textViewFinalCost;

    private Button btnCalculate, btnSave;
    private LinearLayout btnViewBills, btnAbout;   // ✅ CHANGED

    private BillDatabaseHelper databaseHelper;

    private double totalCharges = 0;
    private double finalCost = 0;
    private double rebatePercentage = 0;

    private String[] months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupMonthSpinner();
        databaseHelper = new BillDatabaseHelper(this);

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

    private void initializeViews() {
        spinnerMonth = findViewById(R.id.spinnerMonth);
        editTextUnits = findViewById(R.id.editTextUnits);
        radioGroupRebate = findViewById(R.id.radioGroupRebate);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewFinalCost = findViewById(R.id.textViewFinalCost);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnSave = findViewById(R.id.btnSave);

        btnViewBills = findViewById(R.id.btnViewBills);   // ✅ LinearLayout
        btnAbout = findViewById(R.id.btnAbout);           // ✅ LinearLayout
    }

    private void setupMonthSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, months);
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

        DecimalFormat df = new DecimalFormat("0.00");
        textViewTotalCharges.setText("Total Charges: RM " + df.format(totalCharges));
        textViewFinalCost.setText("Final Cost: RM " + df.format(finalCost));
    }

    private double calculateTariff(double units) {
        double charges;

        if (units <= 200) {
            charges = units * 0.218;
        } else if (units <= 300) {
            charges = (200 * 0.218) + ((units - 200) * 0.334);
        } else if (units <= 600) {
            charges = (200 * 0.218) + (100 * 0.334) + ((units - 300) * 0.516);
        } else {
            charges = (200 * 0.218) + (100 * 0.334) + (300 * 0.516) + ((units - 600) * 0.546);
        }

        return charges;
    }

    private void saveBill() {
        if (totalCharges == 0) {
            Toast.makeText(this, "Please calculate bill first", Toast.LENGTH_SHORT).show();
            return;
        }

        String month = spinnerMonth.getSelectedItem().toString();
        double units = Double.parseDouble(editTextUnits.getText().toString());

        databaseHelper.addBill(month, units, rebatePercentage * 100,
                totalCharges, finalCost);

        Toast.makeText(this, "Bill saved successfully!", Toast.LENGTH_SHORT).show();

        editTextUnits.setText("");
        radioGroupRebate.clearCheck();
        textViewTotalCharges.setText("Total Charges: RM 0.00");
        textViewFinalCost.setText("Final Cost: RM 0.00");
        totalCharges = 0;
        finalCost = 0;
    }
}
