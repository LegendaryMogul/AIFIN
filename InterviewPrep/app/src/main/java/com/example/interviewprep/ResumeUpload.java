package com.example.interviewprep;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.auth.User;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ResumeUpload extends AppCompatActivity {
    private TextView feedbackText;
    private TextView stringFeedbackText;
    private ImageView logo;
    UserStats statsObj = new UserStats();


    Handler handler = new Handler();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resume_layout);
        feedbackText = findViewById(R.id.FeedBack);
        stringFeedbackText = findViewById(R.id.writtenFeedback);
        logo = findViewById(R.id.prathamLogo);

    }


    public void returnFromResume(View view) {
        Intent intent = new Intent(ResumeUpload.this, MainActivity.class);
        startActivity(intent);
    }


    public void uploadResume(View view) {
        showFileChooser();
        //Implement spinning while waiting
        //Update Feedback Textview with feedback
    }

    private void showFileChooser(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 100);
        } catch (Exception e){
            Toast.makeText(this, "Please install a file manager", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
            assert uri != null;
            String path = uri.getPath();
            assert path != null;
            File pdfFile = new File(path);
            Uri fileUri = Uri.fromFile(pdfFile);
            String newFileName = UUID.randomUUID().toString();

            //Send File
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference pdfRef = storageRef.child("pdfs/" + newFileName + ".pdf");

            UploadTask uploadTask = pdfRef.putFile(uri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {

                ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(logo, "rotation", 0f, 1080f);
                rotateAnimator.setDuration(5700);
                rotateAnimator.start();

                handler.postDelayed(() -> checkForResults(newFileName), 6000);
            }).addOnFailureListener(e -> {
                // Handle upload failure
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkForResults(String fileName) {

        // Create a reference to the results file in Cloud Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference resultsRef = storageRef.child("pdfResults/" + fileName + ".json");

        resultsRef.getMetadata().addOnSuccessListener(metadata -> {
            // File exists, download it
            resultsRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {

                    String feedback = new String(bytes, StandardCharsets.UTF_8);
                    resumeTextViewsUpdate(feedback);
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Download Error", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Unexpected Failure restart and try again", Toast.LENGTH_SHORT).show();
        });
    }

    public void resumeTextViewsUpdate(String feedback){

        String ratingOutOfTen = "Resume rating: ";
        double rating = 5;
        for (int i = 0; i<feedback.length()-1; i++){
            if (Character.isDigit(feedback.charAt(i))){
                rating = Character.getNumericValue(feedback.charAt(i));
                if (rating == 1 && feedback.charAt(i+1) == 0){
                    rating = 10;
                }
                ratingOutOfTen += feedback.substring(i, i+5);
                break;
            }
        }

        if (MainActivity.bestResume < rating){
            MainActivity.bestResume = (int) rating;
        }

        feedbackText.setText(ratingOutOfTen);


        String writtenFeedbackString = "Feedback: ";
        int indexOfNewLine = feedback.indexOf("Feedback:");


        writtenFeedbackString += (feedback.substring(indexOfNewLine+10, feedback.length()-1));
        stringFeedbackText.setText(writtenFeedbackString);

        feedbackText.setVisibility(View.VISIBLE);
        stringFeedbackText.setVisibility(View.VISIBLE);
        logo.setVisibility(View.INVISIBLE);
    }
}

