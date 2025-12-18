package com.example.electricitybillapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

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

    // ✅ NEW METHOD: Get all months that already have bills
    public List<String> getAllUsedMonths() {
        List<String> months = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + COLUMN_MONTH + " FROM " + TABLE_BILLS, null);

        if (cursor.moveToFirst()) {
            do {
                months.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return months;
    }

    // ✅ NEW METHOD: Check if a specific month already has a bill
    public boolean hasBillForMonth(String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BILLS +
                        " WHERE " + COLUMN_MONTH + " = ?",
                new String[]{month});

        boolean hasBill = false;
        if (cursor.moveToFirst()) {
            hasBill = cursor.getInt(0) > 0;
        }

        cursor.close();
        db.close();
        return hasBill;
    }

    public ArrayList<String> getAllBills() {
        ArrayList<String> billList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BILLS +
                " ORDER BY " + COLUMN_MONTH + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                String month = cursor.getString(1);
                double units = cursor.getDouble(2);
                double rebate = cursor.getDouble(3);
                double totalCharges = cursor.getDouble(4);
                double finalCost = cursor.getDouble(5);

                String display = month +
                        " | Units: " + units +
                        " | Rebate: " + rebate + "%" +
                        " | Total: RM" + String.format("%.2f", totalCharges) +
                        " | Final: RM" + String.format("%.2f", finalCost);
                billList.add(display);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return billList;
    }

    public String[] getBillDetails(int position) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BILLS +
                " ORDER BY " + COLUMN_MONTH + " ASC" +
                " LIMIT 1 OFFSET " + position, null);
        String[] details = new String[5];
        if (cursor.moveToFirst()) {
            details[0] = cursor.getString(1); // month
            details[1] = String.valueOf(cursor.getDouble(2)); // units
            details[2] = String.valueOf(cursor.getDouble(3)); // rebate
            details[3] = String.valueOf(cursor.getDouble(4)); // total charges
            details[4] = String.valueOf(cursor.getDouble(5)); // final cost
        }
        cursor.close();
        db.close();
        return details;
    }

    public int getBillIdAtPosition(int position) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + TABLE_BILLS +
                        " ORDER BY " + COLUMN_MONTH + " ASC" +
                        " LIMIT 1 OFFSET ?",
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

    public boolean deleteBillByMonth(String month) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_BILLS, COLUMN_MONTH + " = ?",
                new String[]{month});
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
                " ORDER BY " + COLUMN_MONTH + " ASC" +
                " LIMIT 1 OFFSET " + position, null);
        String[] details = new String[6]; // Added one more for ID
        if (cursor.moveToFirst()) {
            details[0] = cursor.getString(1); // month
            details[1] = String.valueOf(cursor.getDouble(2)); // units
            details[2] = String.valueOf(cursor.getDouble(3)); // rebate
            details[3] = String.valueOf(cursor.getDouble(4)); // total charges
            details[4] = String.valueOf(cursor.getDouble(5)); // final cost
            details[5] = String.valueOf(cursor.getInt(0)); // id
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

    // ✅ NEW METHOD: Get all bills with full details for display
    public ArrayList<Bill> getAllBillsDetailed() {
        ArrayList<Bill> billList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BILLS +
                " ORDER BY " + COLUMN_MONTH + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                Bill bill = new Bill();
                bill.setId(cursor.getInt(0));
                bill.setMonth(cursor.getString(1));
                bill.setUnits(cursor.getDouble(2));
                bill.setRebate(cursor.getDouble(3));
                bill.setTotalCharges(cursor.getDouble(4));
                bill.setFinalCost(cursor.getDouble(5));
                billList.add(bill);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return billList;
    }

    // ✅ NEW METHOD: Update existing bill
    public boolean updateBill(int id, double units, double rebate,
                              double totalCharges, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UNITS, units);
        values.put(COLUMN_REBATE, rebate);
        values.put(COLUMN_TOTAL_CHARGES, totalCharges);
        values.put(COLUMN_FINAL_COST, finalCost);

        int rowsUpdated = db.update(TABLE_BILLS, values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return rowsUpdated > 0;
    }

    // ✅ NEW METHOD: Get bill by month
    public String[] getBillByMonth(String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BILLS +
                        " WHERE " + COLUMN_MONTH + " = ?",
                new String[]{month});

        String[] details = new String[6];
        if (cursor.moveToFirst()) {
            details[0] = cursor.getString(1); // month
            details[1] = String.valueOf(cursor.getDouble(2)); // units
            details[2] = String.valueOf(cursor.getDouble(3)); // rebate
            details[3] = String.valueOf(cursor.getDouble(4)); // total charges
            details[4] = String.valueOf(cursor.getDouble(5)); // final cost
            details[5] = String.valueOf(cursor.getInt(0)); // id
        }
        cursor.close();
        db.close();
        return details;
    }

    // ✅ NEW METHOD: Clear all bills (for testing/reset)
    public void clearAllBills() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BILLS, null, null);
        db.close();
    }
}

// ✅ NEW CLASS: Bill model for better data handling
class Bill {
    private int id;
    private String month;
    private double units;
    private double rebate;
    private double totalCharges;
    private double finalCost;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public double getUnits() { return units; }
    public void setUnits(double units) { this.units = units; }

    public double getRebate() { return rebate; }
    public void setRebate(double rebate) { this.rebate = rebate; }

    public double getTotalCharges() { return totalCharges; }
    public void setTotalCharges(double totalCharges) { this.totalCharges = totalCharges; }

    public double getFinalCost() { return finalCost; }
    public void setFinalCost(double finalCost) { this.finalCost = finalCost; }

    @Override
    public String toString() {
        return month + " | Units: " + units + " | Rebate: " + rebate + "%" +
                " | Total: RM" + String.format("%.2f", totalCharges) +
                " | Final: RM" + String.format("%.2f", finalCost);
    }
}