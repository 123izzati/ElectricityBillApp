package com.example.electricitybillapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class BillDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "bills.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_BILLS = "bills";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_MONTH = "month";
    private static final String COLUMN_UNITS = "units";
    private static final String COLUMN_REBATE = "rebate";
    private static final String COLUMN_TOTAL_CHARGES = "total_charges";
    private static final String COLUMN_FINAL_COST = "final_cost";

    public BillDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_BILLS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_MONTH + " TEXT," +
                COLUMN_UNITS + " REAL," +
                COLUMN_REBATE + " REAL," +
                COLUMN_TOTAL_CHARGES + " REAL," +
                COLUMN_FINAL_COST + " REAL)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        onCreate(db);
    }

    public void addBill(String month, double units, double rebate,
                        double totalCharges, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_UNITS, units);
        values.put(COLUMN_REBATE, rebate);
        values.put(COLUMN_TOTAL_CHARGES, totalCharges);
        values.put(COLUMN_FINAL_COST, finalCost);
        db.insert(TABLE_BILLS, null, values);
        db.close();
    }

    public ArrayList<String> getAllBills() {
        ArrayList<String> billList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BILLS, null);

        if (cursor.moveToFirst()) {
            do {
                String month = cursor.getString(1);
                double finalCost = cursor.getDouble(5);
                billList.add(month + " - RM " + String.format("%.2f", finalCost));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return billList;
    }

    public String[] getBillDetails(int position) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BILLS +
                " LIMIT 1 OFFSET " + position, null);
        String[] details = new String[5];
        if (cursor.moveToFirst()) {
            details[0] = cursor.getString(1); // month
            details[1] = cursor.getString(2); // units
            details[2] = cursor.getString(3); // rebate
            details[3] = cursor.getString(4); // total charges
            details[4] = cursor.getString(5); // final cost
        }
        cursor.close();
        db.close();
        return details;
    }

    public int getBillIdAtPosition(int position) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + TABLE_BILLS +
                        " ORDER BY " + COLUMN_ID + " ASC LIMIT 1 OFFSET ?",
                new String[]{String.valueOf(position)});

        int id = -1;
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return id;
    }

    public boolean deleteBill(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_BILLS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return rowsDeleted > 0;
    }

    public boolean deleteBillByPosition(int position) {
        int id = getBillIdAtPosition(position);
        if (id != -1) {
            return deleteBill(id);
        }
        return false;
    }

    public String[] getBillDetailsWithId(int position) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BILLS +
                " LIMIT 1 OFFSET " + position, null);
        String[] details = new String[6]; // Added one more for ID
        if (cursor.moveToFirst()) {
            details[0] = cursor.getString(1); // month
            details[1] = cursor.getString(2); // units
            details[2] = cursor.getString(3); // rebate
            details[3] = cursor.getString(4); // total charges
            details[4] = cursor.getString(5); // final cost
            details[5] = cursor.getString(0); // id
        }
        cursor.close();
        db.close();
        return details;
    }

    public int getBillCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BILLS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
}