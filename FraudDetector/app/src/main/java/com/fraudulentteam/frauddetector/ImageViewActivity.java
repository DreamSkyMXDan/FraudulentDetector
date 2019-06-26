package com.fraudulentteam.frauddetector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.ArrayList;
import java.util.List;

public class ImageViewActivity extends AppCompatActivity {
    Button mSaveButton;
    Bitmap mSelectedImage;
    List<String> allLinesTexts = new ArrayList<>();
    String legalName = "Tianning Shen";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageview_layout);
        ImageView imageView = findViewById(R.id.image);
        byte[] checks = Repository.getInstance().getChecks();
        mSelectedImage = BitmapFactory.decodeByteArray(checks, 0, checks.length);
        imageView.setImageBitmap(mSelectedImage);

        rotateImage();

        mSaveButton = findViewById(R.id.saveBtn);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTextRecognition();
            }
        });

    }

    private void rotateImage(){
        Matrix matrix = new Matrix();

        matrix.postRotate(90);

        mSelectedImage = Bitmap.createBitmap(mSelectedImage, 0, 0, mSelectedImage.getWidth(), mSelectedImage.getHeight(), matrix, true);
    }

    private void runTextRecognition() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mSelectedImage);
        FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        mSaveButton.setEnabled(false);
        recognizer.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
                                mSaveButton.setEnabled(true);
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                mSaveButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
    }

    private boolean isLegalNameIn = false;

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            showToast("Fake Check");
            return;
        }
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                String line = lines.get(j).toString();
                allLinesTexts.add(lines.get(j).getText());
            }
        }

        for (String line : allLinesTexts) {
            if (line.contains(legalName)) {
                isLegalNameIn = true;
            }
        }
        // Date must be no greater than today
        // Authorized signature
        // Dollars xxx.xx

        // can do routing number and account number with cloud vision API
        if (!isLegalNameIn) {
            showToast("Fake Check");
        } else {
            showToast("Great Work");
        }

        allLinesTexts.clear();

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
