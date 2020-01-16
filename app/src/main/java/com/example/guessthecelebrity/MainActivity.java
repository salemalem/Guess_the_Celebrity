package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> celebrityPhotos = new ArrayList<>();
    ArrayList<String> celebrityNames  = new ArrayList<>();
    int celebrityChosen = 0;

    ImageView celebrityImageView;



    public class DownloadCelebrity extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
              url = new URL(urls[0]);
              urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }

                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public class CelebrityPhotoDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            URL url;
            try {
                url = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
                return imageBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void celebChosen(View view) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebrityImageView = (ImageView)findViewById(R.id.celebrityImageView);

        DownloadCelebrity task = new DownloadCelebrity();
        String html = null;
        try {
            html = task.execute("http://www.posh24.se/kandisar").get();
            String[] splittedHtml = html.split("<div class=\"sidebarContainer\">");
//            Log.i("Celebrities:", Arrays.toString(splittedHtml));
            Pattern pattern = Pattern.compile("src=\"(.*?)\"");
            Matcher matcher = pattern.matcher(splittedHtml[0]);
//            Log.i("Celebrities", result);
            while (matcher.find()) {
//                System.out.println(matcher.group(1));
                celebrityPhotos.add(matcher.group(1));
            }

            pattern = Pattern.compile("alt=\"(.*?)\"");
            matcher = pattern.matcher(splittedHtml[0]);
//            Log.i("Celebrities", result);
            while (matcher.find()) {
//                System.out.println(matcher.group(1));
                celebrityNames.add(matcher.group(1));
            }

            Random random = new Random();
            celebrityChosen = random.nextInt(celebrityNames.size());

            CelebrityPhotoDownloader imageTask = new CelebrityPhotoDownloader();
            Bitmap celebrityImageBitmap;
            celebrityImageBitmap = imageTask.execute(celebrityPhotos.get(celebrityChosen)).get();
            celebrityImageView.setImageBitmap(celebrityImageBitmap);
//            Log.i("Finish", celebrityPhotos.get(celebrityChosen));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
