package com.example.patriot.bookdetector.syncservice;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.patriot.bookdetector.R;
import com.example.patriot.bookdetector.data.BookContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by patriot on 3/11/2015.
 */


public class BookSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = BookSyncAdapter.class.getSimpleName();
    private static int pageCount = 0;
    public static final String SQL_INSERT_OR_REPLACE = "__sql_insert_or_replace__";

    // Interval at which to sync with the blog posts, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours *3 = 9 hours
    public static final int SYNC_INTERVAL = 60 * 180 * 3;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    public BookSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");

        String numberOfPosts = "4";

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String questionAnswerJsonStr = null;
        int defaultPage = 1;

        try {
            // Construct the URL for the helpaturdesk query
            do {
                final String QUESTIONS_BASE_URL =
                        "http://helpaturdesk.imkalpit.com/?json=1";
                final String QUERY_PARAM = "count";
                final String QUERY_PAGE = "page";
                Uri builtUri;
                if (pageCount == 0) {
                    builtUri = Uri.parse(QUESTIONS_BASE_URL).buildUpon()
                            .appendQueryParameter(QUERY_PARAM, numberOfPosts)
                            .appendQueryParameter(QUERY_PAGE, String.valueOf(defaultPage))
                            .build();
                } else {
                    builtUri = Uri.parse(QUESTIONS_BASE_URL).buildUpon()
                            .appendQueryParameter(QUERY_PARAM, numberOfPosts)
                            .appendQueryParameter(QUERY_PAGE, String.valueOf(defaultPage))
                            .build();
                }
                defaultPage++;
                URL url = new URL(builtUri.toString());

                // Create the request to blog posts, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return;
                }
                questionAnswerJsonStr = buffer.toString();
                getQuestionsListFromJson(questionAnswerJsonStr);
            } while (pageCount + 1 != defaultPage);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the data, there's no point in attemping
            // to parse it.
            return;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return;

    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getQuestionsListFromJson(String questionAnswerInfoJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String BOOK_TITLE = "book_title";
        final String BOOK_AUTHOR = "book_author";
        final String PUBLISHED_DATE = "published_date";
        final String PUBLISHER = "publisher";
        final String PAGES = "pages";



        try {
            JSONObject questionAnswerInfoJson = new JSONObject(questionAnswerInfoJsonStr);
            JSONArray postsArray = questionAnswerInfoJson.getJSONArray(BOOK_TITLE);
            pageCount = questionAnswerInfoJson.getInt("pages");
            // Insert the new post information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(postsArray.length());
            for (int i = 0; i < postsArray.length(); i++) {
                // Get the JSON object representing the day
                String url, answer, author, modifiedDate, category;
                JSONObject currentPostObject = postsArray.getJSONObject(i);

                author = currentPostObject.get(BOOK_AUTHOR).toString();
                answer = currentPostObject.get(PUBLISHED_DATE).toString();
                modifiedDate = currentPostObject.get(PUBLISHER).toString();
                category = currentPostObject.get(PAGES).toString();

                ContentValues BookValues = new ContentValues();
                BookValues.put(SQL_INSERT_OR_REPLACE, true);
                BookValues.put(BookContract.BookEntry. COLUMN_BOOK_TITLE, "book_title");
                BookValues.put(BookContract.BookEntry.COLUMN_BOOK_AUTHOR, "book_author");
                BookValues.put(BookContract.BookEntry.COLUMN_PUBLISHED_DATE, "published_date");

                BookValues.put(BookContract.BookEntry.COLUMN_PUBLISHER,"publisher");
                BookValues.put(BookContract.BookEntry.COLUMN_PAGES, "pages");




                cVVector.add(BookValues);
            }

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = getContext().getContentResolver().bulkInsert(BookContract.BookEntry.CONTENT_URI, cvArray);
            }

            Log.w(LOG_TAG, "FetchingPosts Complete. " + inserted + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        BookSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);

        }
        return newAccount;
    }
}
