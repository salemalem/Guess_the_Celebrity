package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
    int locationOfCorrectAnswer = 0;
    String[] answers = new String[4];

    ImageView celebrityImageView;
    Button button0, button1, button2, button3;


    public void celebChosen(View view) {
        if (view.getTag().toString() == Integer.toString(locationOfCorrectAnswer)) {
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong! It was " + celebrityNames.get(celebrityChosen), Toast.LENGTH_LONG).show();
        }
        createNewQuestion();
    }

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

    public void createNewQuestion() {
        Random random = new Random();
        celebrityChosen = random.nextInt(celebrityNames.size());

        CelebrityPhotoDownloader imageTask = new CelebrityPhotoDownloader();
        Bitmap celebrityImageBitmap;
        try {
            celebrityImageBitmap = imageTask.execute(celebrityPhotos.get(celebrityChosen)).get();
            celebrityImageView.setImageBitmap(celebrityImageBitmap);

            locationOfCorrectAnswer = random.nextInt(4);
            int locationOfIncorrectAnswer;

            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = celebrityNames.get(celebrityChosen);
                } else {
                    locationOfIncorrectAnswer = random.nextInt(celebrityNames.size());

                    while (locationOfIncorrectAnswer == celebrityChosen) {
                        locationOfIncorrectAnswer = random.nextInt(celebrityNames.size());
                    }
                    answers[i] = celebrityNames.get(locationOfIncorrectAnswer);
                }
                button0.setText(answers[0]);
                button1.setText(answers[1]);
                button2.setText(answers[2]);
                button3.setText(answers[3]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebrityImageView = (ImageView)findViewById(R.id.celebrityImageView);
        button0 = (Button)findViewById(R.id.chooseCelebButton1);
        button1 = (Button)findViewById(R.id.chooseCelebButton2);
        button2 = (Button)findViewById(R.id.chooseCelebButton3);
        button3 = (Button)findViewById(R.id.chooseCelebButton4);

        DownloadCelebrity task = new DownloadCelebrity();
        String html = null;
        try {
            html = task.execute("http://www.posh24.se/kandisar").get();
            String[] splittedHtml = html.split("<div class=\"sidebarContainer\">");
//            Log.i("Celebrities:", Arrays.toString(splittedHtml));
            Pattern pattern = Pattern.compile("<img src=\"(.*?)\"");
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

            createNewQuestion();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
