package com.example.patriot.bookdetector.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by patriot on 3/11/2015.
 */



public class BookProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private BookDbHelper mOpenHelper;
    public static final String SQL_INSERT_OR_REPLACE = "__sql_insert_or_replace__";
    static final int BOOKS = 100;
    static final int BOOKS_WITH_AUTHOR = 101;

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BookContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, BookContract.PATH_BOOKS, BOOKS);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new BookDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case BOOKS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BookContract.BookEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case BOOKS_WITH_AUTHOR:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BookContract.BookEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder



                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case BOOKS:
                return BookContract.BookEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;
        boolean replace = false;
        long _id;
        if (contentValues.containsKey(SQL_INSERT_OR_REPLACE)) {
            replace = contentValues.getAsBoolean(SQL_INSERT_OR_REPLACE);
            // Clone the values object, so we don't modify the original.
            // This is not strictly necessary, but depends on your needs
            contentValues = new ContentValues(contentValues);

            // Remove the key, so we don't pass that on to db.insert() or db.replace()
            contentValues.remove(SQL_INSERT_OR_REPLACE);
        }

        switch (match) {
            case BOOKS:
                if (replace) {
                    _id = db.replace(BookContract.BookEntry.TABLE_NAME, null, contentValues);
                } else {
                    _id = db.insert(BookContract.BookEntry.TABLE_NAME, null, contentValues);
                }
                if (_id > 0)
                    returnUri = BookContract.BookEntry.buildBooksUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case BOOKS:
                rowsUpdated = db.update(BookContract.BookEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        long _id;
        boolean replace = false;
        switch (match) {
            case BOOKS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {

                        if (value.containsKey(SQL_INSERT_OR_REPLACE)) {
                            replace = value.getAsBoolean(SQL_INSERT_OR_REPLACE);
                            // Clone the values object, so we don't modify the original.
                            // This is not strictly necessary, but depends on your needs
                            value = new ContentValues(value);

                            // Remove the key, so we don't pass that on to db.insert() or db.replace()
                            value.remove(SQL_INSERT_OR_REPLACE);
                        }

                                            }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
