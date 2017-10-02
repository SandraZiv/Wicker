package hr.fer.android.wicker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import hr.fer.android.wicker.WickerConstant;
import hr.fer.android.wicker.entity.Counter;


public class CounterDatabase implements Serializable {

    //creating names and indexes for table
    public static final String KEY_ID = "ID"; //index
    public static final String KEY_NAME_COLUMN = "COUNTER_NAME_COLUMN";
    public static final String KEY_VALUE_COLUMN = "COUNTER_VALUE_COLUMN";
    public static final String KEY_STEP_COLUMN = "COUNTER_STEP_COLUMN";
    public static final String KEY_DATE_CREATED_COLUMN = "COUNTER_DATE_CREATED_COLUMN";
    public static final String KEY_DATE_MODIFIED_COLUMN = "COUNTER_DATE_MODIFIED_COLUMN";
    public static final String KEY_NOTE_COLUMN = "COUNTER_NOTE_COLUMN";

    private CounterDatabaseHelper counterDatabaseHelper;

    private SQLiteDatabase dbWritable;
    private SQLiteDatabase dbReadable;

    //constructor to initiate open helper
    public CounterDatabase(Context context) {
        counterDatabaseHelper = new CounterDatabaseHelper(context,
                CounterDatabaseHelper.DATABASE_NAME, null, CounterDatabaseHelper.DATABASE_VERSION);

        dbWritable = counterDatabaseHelper.getWritableDatabase();
        dbReadable = counterDatabaseHelper.getReadableDatabase();
    }

    /**
     * @return id of saved data
     */
    public void closeDatabase() {
        counterDatabaseHelper.close();
    }

    //add counter to database

    /**
     * @param counter value to add in database
     * @return id of newly created row in database, -1 if unsuccessful
     */
    public long addCounter(Counter counter) {
        //create new row of values to insert
        ContentValues newValues = new ContentValues();

        //add data from counter to values
        newValues.put(KEY_NAME_COLUMN, counter.getName());
        newValues.put(KEY_VALUE_COLUMN, counter.getValue());
        newValues.put(KEY_STEP_COLUMN, counter.getStep());
        newValues.put(KEY_DATE_CREATED_COLUMN, new Timestamp(counter.getDateCreated()).toString());
        newValues.put(KEY_DATE_MODIFIED_COLUMN, new Timestamp(counter.getDateModified()).toString());
        newValues.put(KEY_NOTE_COLUMN, counter.getNote());

        //insert into table
        return dbWritable.insert(CounterDatabaseHelper.DATABASE_TABLE, null, newValues);
        //in case we put null there is exception (second parameter)
    }

    public long updateCounter(Counter counter) {
        //container to update row
        ContentValues updateValues = new ContentValues();

        //add data from counter to values
        updateValues.put(KEY_NAME_COLUMN, counter.getName());
        updateValues.put(KEY_VALUE_COLUMN, counter.getValue());
        updateValues.put(KEY_STEP_COLUMN, counter.getStep());
        updateValues.put(KEY_DATE_CREATED_COLUMN, new Timestamp(counter.getDateCreated()).toString());
        updateValues.put(KEY_DATE_MODIFIED_COLUMN, new Timestamp(counter.getDateModified()).toString());
        updateValues.put(KEY_NOTE_COLUMN, counter.getNote());

        try {
            dbWritable.update(CounterDatabaseHelper.DATABASE_TABLE, updateValues, " id=" + counter.getId(), null);
            return counter.getId();
        } catch (SQLiteConstraintException e) {
            return WickerConstant.ERROR_CODE_LONG;
        }


    }

    public Counter getDatabaseCounterData(Long id) {
        String sqlQuery = "select * from " + CounterDatabaseHelper.DATABASE_TABLE
                + " where " + KEY_ID + "=" + id;

        Cursor cursor = dbReadable.rawQuery(sqlQuery, null);

        if (cursor.moveToFirst()) {
            Calendar created = Calendar.getInstance();
            created.setTime(getDateFromString(cursor.getString(4)));

            Calendar modified = Calendar.getInstance();
            modified.setTime(getDateFromString(cursor.getString(5)));

            return new Counter(cursor.getLong(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3),
                    created.getTimeInMillis(), modified.getTimeInMillis(), cursor.getString(6));
        }
        return null;
    }

    public List<Counter> getDatabaseCounterListData() {
        List<Counter> data = new LinkedList<>();

        String sqlQuery = "select * from " + CounterDatabaseHelper.DATABASE_TABLE;
        Cursor cursor = dbReadable.rawQuery(sqlQuery, null); //cursor is similar to iterator

        if (cursor.moveToFirst()) {
            do {
                Calendar created = Calendar.getInstance();
                created.setTime(getDateFromString(cursor.getString(4)));

                Calendar modified = Calendar.getInstance();
                modified.setTime(getDateFromString(cursor.getString(5)));

                Counter counter = new Counter(cursor.getLong(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3),
                        created.getTimeInMillis(), modified.getTimeInMillis(), cursor.getString(6));
                data.add(counter);
            } while (cursor.moveToNext());
        }
        return data;
    }

    public void deleteCounter(Counter counter) {
        dbWritable.delete(CounterDatabaseHelper.DATABASE_TABLE, " " + KEY_ID + "=" + counter.getId(), null);
    }

    public void deleteAllData() {
        dbWritable.delete(CounterDatabaseHelper.DATABASE_TABLE, "", null);
    }

    private java.util.Date getDateFromString(String tmpDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date date = null;
        try {
            date = dateFormat.parse(tmpDate);
        } catch (ParseException e) {
            Log.e("date", "Parsing datetime failed", e);
        }
        return date;
    }


    //counter database open helper
    private class CounterDatabaseHelper extends SQLiteOpenHelper {

        //string for database name, table and version
        private static final String DATABASE_NAME = "counter_database.db";
        private static final String DATABASE_TABLE = "counterTable";
        private static final int DATABASE_VERSION = 8;

        //SQL statement to create table
        private static final String CREATE_TABLE =
                "create table " + DATABASE_TABLE + " ("
                        + KEY_ID + " integer primary key autoincrement,"
                        + KEY_NAME_COLUMN + " text not null unique,"
                        + KEY_VALUE_COLUMN + " numeric(15,2),"
                        + KEY_STEP_COLUMN + " numeric(15,2),"
                        + KEY_DATE_CREATED_COLUMN + " timestamp,"
                        + KEY_DATE_MODIFIED_COLUMN + " timestamp,"
                        + KEY_NOTE_COLUMN + " text" + ");";


        public CounterDatabaseHelper(Context context, String name,
                                     SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("alter table " + DATABASE_TABLE + " rename to old");
            db.execSQL(CREATE_TABLE);
            db.execSQL("insert into " + DATABASE_TABLE + " select * from old");
            db.execSQL("drop table old");
        }
    }
}
