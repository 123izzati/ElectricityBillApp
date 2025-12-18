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
    private Button buttonDelete, buttonEdit;

    private int position;
    private String month;
    private double units, rebate, totalCharges, finalCost;
    private BillDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initializeViews();
        dbHelper = new BillDatabaseHelper(this);

        position = getIntent().getIntExtra("position", 0);
        String[] details = dbHelper.getBillDetails(position);

        if (details[0] != null) {
            month = details[0];
            units = Double.parseDouble(details[1]);
            rebate = Double.parseDouble(details[2]);
            totalCharges = Double.parseDouble(details[3]);
            finalCost = Double.parseDouble(details[4]);

            textViewMonth.setText("Month: " + month);
            textViewUnits.setText("Units Used: " + details[1] + " kWh");
            textViewRebate.setText("Rebate: " + details[2] + "%");
            textViewTotalCharges.setText(String.format("Total Charges: RM %.2f", totalCharges));
            textViewFinalCost.setText(String.format("Final Cost: RM %.2f", finalCost));
        }

        // Setup delete button functionality
        buttonDelete.setOnClickListener(v -> {
            showDeleteConfirmation();
        });

        // Setup edit button functionality
        buttonEdit.setOnClickListener(v -> {
            showEditConfirmation();
        });
    }

    private void initializeViews() {
        textViewMonth = findViewById(R.id.textViewMonth);
        textViewUnits = findViewById(R.id.textViewUnits);
        textViewRebate = findViewById(R.id.textViewRebate);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewFinalCost = findViewById(R.id.textViewFinalCost);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonEdit = findViewById(R.id.buttonEdit);
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(DetailActivity.this)
                .setTitle("Delete Bill")
                .setMessage("Are you sure you want to delete the bill for " + month + "?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBill();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteBill() {
        boolean isDeleted = dbHelper.deleteBillByPosition(position);

        if (isDeleted) {
            Toast.makeText(this, "Bill for " + month + " deleted successfully", Toast.LENGTH_SHORT).show();

            // Return to ListActivity
            Intent intent = new Intent(this, ListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish(); // Close DetailActivity
        } else {
            Toast.makeText(this, "Failed to delete bill", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditConfirmation() {
        new AlertDialog.Builder(DetailActivity.this)
                .setTitle("Edit Bill")
                .setMessage("You will be redirected to edit the bill details for " + month + ".")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToEditActivity();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void goToEditActivity() {
        // Get the bill ID for the position
        String[] billDetails = dbHelper.getBillDetailsWithId(position);
        int billId = Integer.parseInt(billDetails[5]);

        // Navigate to EditActivity with all necessary data
        Intent intent = new Intent(this, EditBillActivity.class);
        intent.putExtra("billId", billId);
        intent.putExtra("month", month);
        intent.putExtra("units", units);
        intent.putExtra("rebate", rebate);
        intent.putExtra("totalCharges", totalCharges);
        intent.putExtra("finalCost", finalCost);
        startActivityForResult(intent, 1); // Request code 1 for edit
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Refresh the details if the bill was updated
            String[] updatedDetails = dbHelper.getBillDetails(position);

            if (updatedDetails[0] != null) {
                month = updatedDetails[0];
                units = Double.parseDouble(updatedDetails[1]);
                rebate = Double.parseDouble(updatedDetails[2]);
                totalCharges = Double.parseDouble(updatedDetails[3]);
                finalCost = Double.parseDouble(updatedDetails[4]);

                textViewMonth.setText("Month: " + month);
                textViewUnits.setText("Units Used: " + updatedDetails[1] + " kWh");
                textViewRebate.setText("Rebate: " + updatedDetails[2] + "%");
                textViewTotalCharges.setText(String.format("Total Charges: RM %.2f", totalCharges));
                textViewFinalCost.setText(String.format("Final Cost: RM %.2f", finalCost));

                Toast.makeText(this, "Bill updated successfully!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}