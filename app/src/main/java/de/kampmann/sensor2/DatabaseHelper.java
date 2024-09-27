package de.kampmann.sensor2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "MY_DATABASE.db";
    static final int DATABASE_VERSION = 1;

    static final String DATABASE_TABLE = "HIGHSCORES";
    static final String SCORE_ID = "_ID";
    static final String PLAYER_NAME = "player_name";
    static final String SCORE = "score";
    private static final String CREATE_DB_QUERY = "CREATE TABLE " + DATABASE_TABLE + " ( "
            + SCORE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PLAYER_NAME + " TEXT NOT NULL, "
            + SCORE + " INTEGER NOT NULL " + " );";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }
}
