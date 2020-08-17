package com.mburakcakir.androidtextrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class JavaTextRecognitionActivity extends AppCompatActivity implements View.OnClickListener {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_OPEN_IMAGE = 2;
    Bitmap imageBitmap;
    Bitmap selectedImage;
    String detectedText = "";
    private LinearLayout btnCaptureImage, btnDetectTextImage, btnOpenImage, btnShareText;
    private ImageView imgRecognition;
    private TextView txtDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    void init() {
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        btnDetectTextImage = findViewById(R.id.btnDetectTextImage);
        btnOpenImage = findViewById(R.id.btnOpenImage);
        btnShareText = findViewById(R.id.btnShareText);
        imgRecognition = findViewById(R.id.imgRecognition);
        txtDisplay = findViewById(R.id.txtDisplay);

        btnCaptureImage.setOnClickListener(this);
        btnDetectTextImage.setOnClickListener(this);
        btnOpenImage.setOnClickListener(this);
        btnShareText.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCaptureImage:
                dispatchTakePictureIntent();
                txtDisplay.setText("");
                break;
            case R.id.btnDetectTextImage:
                runTextRecognition();
                break;
            case R.id.btnOpenImage:
                openGallery();
                break;

            case R.id.btnShareText:
                shareText(detectedText);
        }
    }

    private void shareText(String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }


    private void runTextRecognition() {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(((BitmapDrawable) imgRecognition.getDrawable()).getBitmap());
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        firebaseVisionTextRecognizer.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processTextRecognitionResult(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(JavaTextRecognitionActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void processTextRecognitionResult(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> textBlockList = firebaseVisionText.getTextBlocks();
        if (textBlockList.size() == 0) {
            Toast.makeText(this, "No Text Found in image", Toast.LENGTH_SHORT).show();
        } else {
            for (FirebaseVisionText.TextBlock textBlock : textBlockList) {
                Log.d("codeblocks", textBlock.getText());
                detectedText += textBlock.getText();
            }
            txtDisplay.setText(detectedText);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_OPEN_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imgRecognition.setImageBitmap(imageBitmap);
            detectedText = "";
        } else if (requestCode == REQUEST_OPEN_IMAGE && resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                imgRecognition.setImageBitmap(selectedImage);
                detectedText = "";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
    }

}
