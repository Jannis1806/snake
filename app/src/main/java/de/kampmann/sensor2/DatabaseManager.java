package de.kampmann.sensor2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLDataException;

public class DatabaseManager {

    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public DatabaseManager(Context ctx) {
        context = ctx;
    }

    public DatabaseManager open() throws SQLDataException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String playerName, int score) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.PLAYER_NAME, playerName);
        contentValues.put(DatabaseHelper.SCORE, score);
        database.insert(DatabaseHelper.DATABASE_TABLE, null, contentValues);
    }

    public Cursor fetchTopFive() {
        String[] columns = new String[]{DatabaseHelper.SCORE_ID, DatabaseHelper.PLAYER_NAME, DatabaseHelper.SCORE};
        Cursor cursor = database.query(DatabaseHelper.DATABASE_TABLE, columns, null, null, null, null, DatabaseHelper.SCORE + " DESC", "5");
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, String playerName, int score) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.PLAYER_NAME, playerName);
        contentValues.put(DatabaseHelper.SCORE, score);
        return database.update(DatabaseHelper.DATABASE_TABLE, contentValues,
                DatabaseHelper.SCORE_ID + "=" + _id, null);
    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.DATABASE_TABLE, DatabaseHelper.SCORE_ID + "=" + _id, null);
    }
}
