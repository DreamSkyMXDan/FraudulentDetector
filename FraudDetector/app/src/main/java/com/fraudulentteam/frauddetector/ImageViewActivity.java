package com.fraudulentteam.frauddetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ImageViewActivity extends AppCompatActivity {
    Button mSaveButton;
    Bitmap mSelectedImage;
    List<String> allLinesTexts = new ArrayList<>();
    String legalName = "Tianning Shen";
    String AUTH_SIG = "authorized signature";
    private Classifier mClassifier;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageview_layout);
        init();
        ImageView imageView = findViewById(R.id.image);
        byte[] checks = Repository.getInstance().getChecks();
        mSelectedImage = BitmapFactory.decodeByteArray(checks, 0, checks.length);
        imageView.setImageBitmap(mSelectedImage);

        rotateImage();

        mSaveButton = findViewById(R.id.saveBtn);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap resized = Bitmap.createScaledBitmap(mSelectedImage, 28, 28, true);

                Result result = mClassifier.classify(resized);
                showToast(Integer.toString(result.getNumber()));
                if (isNetworkConnectionAvailable()) {
                    runCloudTextRecognition();
                } else {
                    runTextRecognition();
                }
            }
        });

    }

    private void init() {
        try {
            mClassifier = new Classifier(this);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to load model", Toast.LENGTH_LONG).show();
            Log.e(ImageViewActivity.class.getSimpleName(), "init(): Failed to create Classifier", e);
        }
    }

    private void rotateImage(){
        Matrix matrix = new Matrix();

        matrix.postRotate(90);

        mSelectedImage = Bitmap.createBitmap(mSelectedImage, 0, 0, mSelectedImage.getWidth(), mSelectedImage.getHeight(), matrix, true);
    }

    private void runCloudTextRecognition() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mSelectedImage);
        FirebaseVisionDocumentTextRecognizer recognizer = FirebaseVision.getInstance()
                .getCloudDocumentTextRecognizer();
        recognizer.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionDocumentText>() {
                            @Override
                            public void onSuccess(FirebaseVisionDocumentText texts) {
                                mSaveButton.setEnabled(true);
                                processCloudTextRecognitionResult(texts);
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

    private void processCloudTextRecognitionResult(FirebaseVisionDocumentText text) {
        // Task completed successfully
        if (text == null) {
            showToast("No text found");
            return;
        }
        List<FirebaseVisionDocumentText.Block> blocks = text.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionDocumentText.Paragraph> paragraphs = blocks.get(i).getParagraphs();
            for (int j = 0; j < paragraphs.size(); j++) {
                allLinesTexts.add(paragraphs.get(j).getText().replace("\n", "").replace("\r", ""));
                List<FirebaseVisionDocumentText.Word> words = paragraphs.get(j).getWords();
                for (int l = 0; l < words.size(); l++) {

                }
            }
        }

        checkValidility();
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
    private boolean hasAuthSig = false;
    private boolean hasValidDate = false;

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            showToast("Fake Check");
            return;
        }
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                allLinesTexts.add(lines.get(j).getText().replace("\n", "").replace("\r", ""));
            }
        }

        checkValidility();

    }

    private void checkValidility(){
        isLegalNameIn = false;
        hasAuthSig = false;
        hasValidDate = false;

        for (String line : allLinesTexts) {
            if (line.contains(legalName)) {
                isLegalNameIn = true;
            } else if (line.toLowerCase().contains(AUTH_SIG)) {
                hasAuthSig = true;
            } else if (isValidFormat("MM/dd/yy", line) || isValidFormat("MM-dd-yyyy", line)) {
                hasValidDate = true;
            }

        }
        // Date must be no greater than today
        // Authorized signature
        // Dollars xxx.xx

        // can do routing number and account number with cloud vision API
        if (!isLegalNameIn || !hasAuthSig || !hasValidDate) {
            showToast("Fake Check");
        } else {
            showToast("Great Work");
        }

        allLinesTexts.clear();
    }

    public void onCancel(View view) {
        this.finish();
    }

    public static boolean isValidFormat(String format, String value) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            date = sdf.parse(value);
            if (!value.equals(sdf.format(date)) || !(sdf.format(new Date()).compareTo(value) >= 0)) {
                date = null;
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return date != null;
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }
}
