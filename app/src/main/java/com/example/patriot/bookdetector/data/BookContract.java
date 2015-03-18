package com.example.patriot.bookdetector.data;

/**
 * Created by patriot on 3/11/2015.
 */

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by k on 3/4/2015.
 */
public class BookContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.patriot.bookdetector";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_BOOKS = "books";

    /* Inner class that defines the table contents of the location table */
    public static final class BookEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // Table name
        public static final String TABLE_NAME = "books";

        public static final String COLUMN_BOOK_TITLE = "book_title";
        public static final String COLUMN_BOOK_AUTHOR = "book_author";
        public static final String COLUMN_PUBLISHED_DATE = "published_date";
        public static final String  COLUMN_PUBLISHER = "publisher";
        public static final String  COLUMN_PAGES = "pages";


        public static Uri buildBooksUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


    }
}



