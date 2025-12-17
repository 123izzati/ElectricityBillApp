package com.example.electricitybillapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    private ListView listView;
    private BillDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);



        listView = findViewById(R.id.listView);
        databaseHelper = new BillDatabaseHelper(this);

        displayBills();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Open detail activity
                Intent intent = new Intent(ListActivity.this, DetailActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }

    private void displayBills() {
        ArrayList<String> bills = databaseHelper.getAllBills();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, bills);
        listView.setAdapter(adapter);
    }
}