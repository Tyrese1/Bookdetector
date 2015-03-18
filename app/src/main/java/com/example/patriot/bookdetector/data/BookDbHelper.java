package com.example.patriot.bookdetector.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by patriot on 3/11/2015.
 */


public class BookDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    //Bbookdetector
    static final String DATABASE_NAME = "bookdetector.db";

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " +  BookContract.BookEntry.TABLE_NAME + " (" +
                 BookContract.BookEntry._ID + " INTEGER PRIMARY KEY," +
                 BookContract.BookEntry.COLUMN_BOOK_TITLE + " TEXT NOT NULL, " +
                 BookContract.BookEntry.COLUMN_BOOK_AUTHOR + " TEXT NOT NULL, " +
                 BookContract.BookEntry.COLUMN_PUBLISHED_DATE + " TEXT NOT NULL, " +
                 BookContract.BookEntry.COLUMN_PUBLISHER + " TEXT, " +
                 BookContract.BookEntry.COLUMN_PAGES + " LONGTEXT NOT NULL, " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BookContract.BookEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}

