package com.example.nfc;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.nfc.Notif.CHANNEL_1_ID;

public class test <notificationManager> extends AppCompatActivity {

    //notification
    private NotificationManagerCompat notificationManager;

    //HTTP request
    //private TextView mTextViewResult;

    private static final String FILE_NAME = "file.txt";
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        //notification
        notificationManager = NotificationManagerCompat.from(this);

        textView = findViewById(R.id.text_view_result);

        //mTextViewResult = findViewById(R.id.text_view_result);
        OkHttpClient client = new OkHttpClient();
        String url = "https://reqres.in/api/users?page=2"; /*http://izithumb.api/api/vehicle_types*/
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            /*@Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();

                    test.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FileOutputStream fileOutputStream = null;
                            try {
                                fileOutputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            try {
                                fileOutputStream.write(myResponse.getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            File fileDir = new File(getFilesDir(), FILE_NAME);
                            Toast.makeText(test.this, "File Saved at " +fileDir, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }*/

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();

                    test.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(myResponse);
                        }
                    });
                }
            }
        });
    }

    public void save(View v) {
        String text = textView.getText().toString();
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(text.getBytes());

            textView.setText("");
            Toast.makeText(this, "Saved to " + getFilesDir() + "/" + FILE_NAME,
                    Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void load(View v) {
        FileInputStream fis = null;

        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }

            textView.setText(sb.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //notification
    public void sendOnChannel1(View v) {
        String title = "Votre nouvelle licence";
        String message = null;
        if (MainActivity.VAR_LICENCE == null){
            message = "Aucune licence n'a été trouvée";
        }
        else
        {
            message = MainActivity.VAR_LICENCE;
        }


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_one)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);
    }
}
