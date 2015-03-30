package com.example.patriot.bookdetector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DetailPanelFragment extends Fragment {
    private ImageView BookCover;
    private TextView Title;
    private TextView Author;
    private TextView Publisher;
    private TextView PageCount;
    private TextView FirstSentence;
    private BookClient client;
    private Book book;


    public static final DetailPanelFragment newInstance(Book book) {
        DetailPanelFragment fragment = new DetailPanelFragment();
        fragment.book = book;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_panel, container, false);
        BookCover = (ImageView) rootView.findViewById(R.id.BookCover);
        Title = (TextView)  rootView.findViewById(R.id.Title);
        Author = (TextView)  rootView.findViewById(R.id.Author);
        Publisher = (TextView)  rootView.findViewById(R.id.Publisher);
        PageCount = (TextView)  rootView.findViewById(R.id.PageCount);
        FirstSentence = (TextView)  rootView.findViewById(R.id.PublishedDate);
        if(book != null)
            loadBook(book);
        return rootView;
    }

    // Populate data for the book
    private void loadBook(Book book) {
        Picasso.with(getActivity()).load(Uri.parse(book.getLargeCoverUrl())).placeholder(R.drawable.img_books_loading).error(R.drawable.ic_nocover).into(BookCover);
        Title.setText(book.getTitle());
        Author.setText(book.getAuthor());
        FirstSentence.setText(book.getPublished_date());
        // fetch extra book data from books API
        client = new BookClient();
        client.getExtraBookDetails(book.getOpenLibraryId(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("publishers")) {
                        // display comma separated list of publishers
                        final JSONArray publisher = response.getJSONArray("publishers");
                        final int numPublishers = publisher.length();
                        final String[] publishers = new String[numPublishers];
                        for (int i = 0; i < numPublishers; ++i) {
                            publishers[i] = publisher.getString(i);
                        }
                        Publisher.setText(TextUtils.join(", ", publishers));
                    }
                    if (response.has("number_of_pages")) {
                        PageCount.setText(Integer.toString(response.getInt("number_of_pages")) + " :Pages");
                    }

                    if (response.has("publish_date")) {
                        PageCount.setText(Integer.toString(response.getInt("publish_date")) + " :Date");
                    }



                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void setShareIntent() {
        ImageView ivImage = (ImageView) getActivity().findViewById(R.id.BookCover);
        final TextView tvTitle = (TextView)getActivity().findViewById(R.id.Title);
        // Get access to the URI for the bitmap
        Uri bmpUri = getLocalBitmapUri(ivImage);
        // Construct a ShareIntent with link to image
        Intent shareIntent = new Intent();
        // Construct a ShareIntent with link to image
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                "#Book Recommendation!");
        shareIntent.putExtra(Intent.EXTRA_TEXT, (String)tvTitle.getText());
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        // Launch share menu
        startActivity(Intent.createChooser(shareIntent, "Share Image"));

    }

    // Returns the URI path to the Bitmap displayed in cover imageview
    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}
