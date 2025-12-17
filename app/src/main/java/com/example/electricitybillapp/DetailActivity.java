package com.example.electricitybillapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.Intent;

public class DetailActivity extends AppCompatActivity {
    private TextView textViewMonth, textViewUnits, textViewRebate,
            textViewTotalCharges, textViewFinalCost;
    private Button buttonDelete;

    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initializeViews();

        position = getIntent().getIntExtra("position", 0);
        BillDatabaseHelper dbHelper = new BillDatabaseHelper(this);
        String[] details = dbHelper.getBillDetails(position);

        if (details[0] != null) {
            textViewMonth.setText("Month: " + details[0]);
            textViewUnits.setText("Units Used: " + details[1] + " kWh");
            textViewRebate.setText("Rebate: " + details[2] + "%");

            // Use String.format instead of DecimalFormat for consistent rounding
            double totalCharges = Double.parseDouble(details[3]);
            double finalCost = Double.parseDouble(details[4]);

            textViewTotalCharges.setText(String.format("Total Charges: RM %.2f", totalCharges));
            textViewFinalCost.setText(String.format("Final Cost: RM %.2f", finalCost));
        }

        // Setup delete button functionality
        buttonDelete.setOnClickListener(v -> {
            // Show confirmation dialog
            new AlertDialog.Builder(DetailActivity.this)
                    .setTitle("Delete Bill")
                    .setMessage("Are you sure you want to delete this bill?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteBill(dbHelper);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void initializeViews() {
        textViewMonth = findViewById(R.id.textViewMonth);
        textViewUnits = findViewById(R.id.textViewUnits);
        textViewRebate = findViewById(R.id.textViewRebate);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewFinalCost = findViewById(R.id.textViewFinalCost);
        buttonDelete = findViewById(R.id.buttonDelete);
    }

    private void deleteBill(BillDatabaseHelper dbHelper) {
        // Delete the bill at the current position
        boolean isDeleted = dbHelper.deleteBillByPosition(position);

        if (isDeleted) {
            Toast.makeText(this, "Bill deleted successfully", Toast.LENGTH_SHORT).show();

            // Return to ListActivity
            Intent intent = new Intent(this, ListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish(); // Close DetailActivity
        } else {
            Toast.makeText(this, "Failed to delete bill", Toast.LENGTH_SHORT).show();
        }
    }
}