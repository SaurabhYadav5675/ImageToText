package com.svp.textrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityHome extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;
    ImageView imgBackButton;
    TextView txtToolbarTitle;

    private ImageView imgPreview;
    private Button btnSelfie, btnGallery, btnGetText;
    private CardView cardUserDetails;
    private TextView txtName, txtDOB, txtgender, txtAadharNo;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    String[] permissionsRequired = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int CAMERA_CAPTURE = 111;
    private static final int GALLARY_SELECTED = 112;
    private Uri photoURI;
    private String mCurrentPhotoPath;
    private Bitmap imgBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mCurrentPhotoPath = "";
        imgBitmap = null;
        photoURI = null;
        setToolBar();

        btnSelfie = (Button) findViewById(R.id.btnSelfie);
        btnGallery = (Button) findViewById(R.id.btnGallery);
        btnGetText = (Button) findViewById(R.id.btnGetText);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);

        cardUserDetails = (CardView) findViewById(R.id.cardUserDetails);
        txtName = (TextView) findViewById(R.id.txtName);
        txtDOB = (TextView) findViewById(R.id.txtDOB);
        txtgender = (TextView) findViewById(R.id.txtgender);
        txtAadharNo = (TextView) findViewById(R.id.txtAadharNo);

        btnGetText.setVisibility(View.GONE);
        cardUserDetails.setVisibility(View.GONE);
        btnGetText.setOnClickListener(this);
        btnSelfie.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        ExitFromApp();
    }

    private void setToolBar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        txtToolbarTitle = (TextView) findViewById(R.id.toolbar_TitleText);
        imgBackButton = (ImageView) findViewById(R.id.toolbar_backImage);
        txtToolbarTitle.setText("Home");
        imgBackButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_backImage:
                ExitFromApp();
                break;
            case R.id.btnSelfie:
                askCameraStoragePermission();
                break;
            case R.id.btnGallery:
                selectFromGallery();
                break;
            case R.id.btnGetText:
                detectText();
                break;
        }
    }

    private void detectText() {
        // we can also give options to rotate the image
        //because When we capture an image on android using intent it gets rotated 90 degrees on some devices
        cardUserDetails.setVisibility(View.VISIBLE);

        InputImage inputImage = InputImage.fromBitmap(imgBitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder res = new StringBuilder();
                for (Text.TextBlock block : text.getTextBlocks()) {
                    String optText = block.getText();
                    Point[] points = block.getCornerPoints();
                    Rect rect = block.getBoundingBox();
                    for (Text.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Point[] linePoints = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();
                        for (Text.Element element : line.getElements()) {
                            String elementText = element.getText();
                            res.append(elementText);
                            Log.e("Data11", elementText);
                        }
                        Log.e("Data22", optText);
                        Log.e("Data33", lineText);
                        setUserDetails(optText);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ActivityHome.this, "Faild to detect text. Please try again.", Toast.LENGTH_SHORT).show();
                cardUserDetails.setVisibility(View.GONE);
            }
        });
    }

    private void setUserDetails(String data) {
        //this method use to detect data from aadhar card
        if (data.replace(" ", "").matches("[a-zA-Z]+")) {
            if (!data.toLowerCase(Locale.ROOT).contains("government") || !data.toLowerCase(Locale.ROOT).contains("india")) {
                txtName.setText("Neme: " + data);
            }
        } else if (data.contains("Re/DOB:")) {
            String name[] = data.split("Re/DOB:");
            txtName.setText("Neme: " + name[0]);
            txtDOB.setText("DOB: " + name[1]);
        } else if (data.contains("gR/")) {
            String gender[] = data.split("gR/");
            txtgender.setText("Gender: " + gender[1]);
        } else if (data.contains("/MALE") || data.contains("/FEMALE")) {
            String gender[] = data.split("/");
            txtgender.setText("Gender: " + gender[1]);
        } else if (data.replace(" ", "").matches("[0-9]+") && data.length() >= 12) {
            txtAadharNo.setText("Aadhar No: " + data);
        }
        //get data from normal text
        else if (data.toLowerCase(Locale.ROOT).contains("name:")) {
            txtName.setText(data);
        } else if (data.toLowerCase(Locale.ROOT).contains("dob:")) {
            String dob[] = data.toLowerCase(Locale.ROOT).split("dob:");
            txtDOB.setText("DOB: " + dob[1]);
        } else if (data.toLowerCase(Locale.ROOT).contains("gender:")) {
            txtgender.setText(data);
        } else if (data.toLowerCase(Locale.ROOT).contains("aadhar:")) {
            txtAadharNo.setText(data);
        }
        // we can create many more conditions as per the input type
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(

                getPackageManager()) != null) {
            File photoFile = null;
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG" + timeStamp + "_";
                File storageDir = ActivityHome.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                photoFile = File.createTempFile(
                        imageFileName, ".jpg", storageDir
                );
            } catch (Exception ex) {
            }

            if (photoFile != null) {
                mCurrentPhotoPath = photoFile.getAbsolutePath();
                photoURI = FileProvider.getUriForFile(ActivityHome.this,
                        getResources().getString(R.string.AppFileProvider),
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAMERA_CAPTURE);
            } else {
                Toast.makeText(getBaseContext(), "Unable to create file.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getBaseContext(), "Unable to start Camera.", Toast.LENGTH_LONG).show();
        }

    }

    private void selectFromGallery() {
        Intent chooserIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(chooserIntent, GALLARY_SELECTED);
    }

    public void ExitFromApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHome.this);
        builder.setTitle("Exit?").setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void askCameraStoragePermission() {
        if (ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(ActivityHome.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this, permissionsRequired[1])) {
                //Information about why this app need permission
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHome.this);
                builder.setTitle("Need Permissions");
                builder.setMessage(getResources().getString(R.string.permissionMessage));
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ActivityHome.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                ActivityCompat.requestPermissions(ActivityHome.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }
        } else {
            openCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CAPTURE && resultCode == Activity.RESULT_OK) {
            File file = new File(mCurrentPhotoPath);
            Uri imgUri = Uri.fromFile(file);
            if (imgUri != null) {
                try {
                    Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);
                    imgBitmap = originalBitmap;
                    imgPreview.setVisibility(View.VISIBLE);
                    imgPreview.setImageBitmap(originalBitmap);
                    btnGetText.setVisibility(View.VISIBLE);
                    cardUserDetails.setVisibility(View.GONE);
                    txtName.setText(null);
                    txtDOB.setText(null);
                    txtgender.setText(null);
                    txtAadharNo.setText(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else if (requestCode == GALLARY_SELECTED && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                Bitmap originalBitmap = null;
                try {
                    originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    imgBitmap = originalBitmap;
                    imgPreview.setVisibility(View.VISIBLE);
                    imgPreview.setImageBitmap(originalBitmap);
                    btnGetText.setVisibility(View.VISIBLE);
                    cardUserDetails.setVisibility(View.GONE);
                    txtName.setText(null);
                    txtDOB.setText(null);
                    txtgender.setText(null);
                    txtAadharNo.setText(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CALLBACK_CONSTANT: {
                //check if all permissions are granted
                boolean allgranted = false;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        allgranted = true;
                    } else {
                        allgranted = false;
                        break;
                    }
                }
                if (allgranted) {
                    openCamera();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this, permissionsRequired[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(ActivityHome.this, permissionsRequired[1])) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHome.this);
                    builder.setTitle("Need Multiple Permissions");
                    builder.setMessage("This app needs Camera and Storage permissions to Capture Picture and Save it.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(ActivityHome.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(getBaseContext(), "App needs Camera and Storage permissions. Go To Settings->Apps->Teach Us and Grant Permission.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}