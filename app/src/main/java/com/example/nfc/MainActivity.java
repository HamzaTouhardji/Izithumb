package com.example.nfc;

import java.io.File;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

public class MainActivity extends Activity {

    public static String VAR_LICENCE = null;

    /*** VARIABLES ***/

    //json file
    private Button json_bnt;

    //Material file picker
    private Button btn_file;
    private TextView text_file;

    //NFC
    private NfcAdapter nfcAdapter;

    //annimation ProgressBar rotation
    private ProgressBar progressBar;
    boolean androidBeamAvailable = false;

    //Vibrate
    Button b_short, b_long;
    Vibrator vibrator;

    //permission
    private int STORAGE_PERMISSION_CODE = 1;
    Button btnPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Material file picker
        btn_file = (Button) findViewById(R.id.btn_file);
        text_file = (TextView) findViewById(R.id.text_file);

        //vibrate
        b_short = findViewById(R.id.shortVibrate);
        b_long = findViewById(R.id.longVibrate);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        //permission
        btnPermission = (Button) findViewById(R.id.permission);

        json_bnt = findViewById(R.id.jsonBtn);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);


        btn_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(1000)
                        .withHiddenFiles(true) // Show hidden files and folders
                        .start();
            }
        });
        b_short.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(50);
            }
        });
        b_long.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(500);
            }
        });
        btnPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "La permission a déja été activé !", Toast.LENGTH_SHORT).show();
                } else {
                    requestStoragePermission();
                }
            }
        });
        json_bnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity();
            }
        });

        //test json request
        //contains information about all packages installed on the device.
        PackageManager pm = this.getPackageManager();
        // Check whether NFC is available on device
        if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            // NFC is not available on the device.
            Toast.makeText(this, "Votre appareil n'a pas la technologie NFC.", Toast.LENGTH_SHORT).show();
        }

        // Check whether device is running Android 4.1 or higher
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            // Android Beam feature is not supported.
            Toast.makeText(this, "Android Beam n'est pas pris en charge.", Toast.LENGTH_SHORT).show();
        } else {
            // NFC and Android Beam file transfer is supported.
            Toast.makeText(this, "Android Beam est pris en charge sur votre appareil.", Toast.LENGTH_SHORT).show();
        }

        //test permission about write external storage
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission
                (Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }
    }

    private void openActivity() {
        Intent intent = new Intent(this, test.class);
        startActivity(intent);
    }

    public void sendFile(View view) {
        //manage the exchange of data between two NFC-enabled devices.
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Check whether NFC is enabled on device

        if (!nfcAdapter.isEnabled()) {
            // NFC is disabled, show the settings UI
            // to enable NFC
            Toast.makeText(this, "Activer le NFC.",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
        // Check whether Android Beam feature is enabled on device
        else if (!nfcAdapter.isNdefPushEnabled()) {
            // Android Beam is disabled, show the settings UI
            // to enable Android Beam
            Toast.makeText(this, "Please enable Android Beam.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
        } else {
            progressBar.setVisibility(View.VISIBLE);
            //Toast.makeText(this, "Fichier en cours de transfert", Toast.LENGTH_SHORT).show();
            // NFC and Android Beam both are enabled

            String fileName = "wallpaper.png";

            // Retrieve the path to the user's public pictures directory
            File fileDirectory = Environment
                    .getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES);

            File fileToTransfer = new File(fileDirectory, fileName);

            fileToTransfer.setReadable(true, false);
            Uri fileUri = Uri.fromFile(fileToTransfer);
            if (fileUri == null) {
                Toast.makeText(this, "Le fichier est introuvable", Toast.LENGTH_SHORT).show();
            } else {
                //Send the image
                nfcAdapter.setBeamPushUris(new Uri[]{Uri.fromFile(fileToTransfer)}, this);
                Toast.makeText(this, "Fichier envoyé", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //methodes for ask permission to users
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("La permission est requise car nous devous acceder a vos données !")
                    .setPositiveButton("Accepter", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Refuser", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permision granted ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Material file picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file
            VAR_LICENCE = filePath;
            text_file.setText(filePath);
        }
    }

}
