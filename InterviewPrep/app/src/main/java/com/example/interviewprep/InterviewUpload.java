package com.example.interviewprep;


import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InterviewUpload extends AppCompatActivity {

    TextView eyeContactScore;
    TextView fluidityScore;
    TextView speechGrammarScore;
    TextView promptTV;
    TextView speechToTextFeedback;
    ImageView logo;

    Handler handler = new Handler();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interview_layout);

        eyeContactScore = findViewById(R.id.eyeContactScore);
        fluidityScore = findViewById(R.id.fluidityScore);
        speechGrammarScore = findViewById(R.id.speechGrammarScore);
        speechToTextFeedback = findViewById(R.id.speechToTextFeedback);
        promptTV = findViewById(R.id.promptTextView);
        logo = findViewById(R.id.prathamLogo);

    }
    public void returnFromInterview(View view) {
        Intent intent = new Intent(InterviewUpload.this, MainActivity.class);
        startActivity(intent);
    }
    public void generatePrompt(View view) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                URL url = new URL("http://34.67.67.114:5000/generate_prompt");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String inputLine;

                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JSONObject obj = new JSONObject(content.toString());
                String prompt = obj.getString("prompt");
                ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(logo, "rotation", 0f, 1080f);
                rotateAnimator.setDuration(4900);
                handler.post(rotateAnimator::start);
                //--------------------
                handler.postDelayed(() -> promptTV.setText(prompt.substring(1,prompt.length()-1)), 5000);
                //--------------------
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        executorService.shutdown();

    }
    public void uploadVideo(View view) {
        showFileChooser();
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
            String newFileName = UUID.randomUUID().toString();

            //Send File
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference vidRef = storageRef.child("videos/" + newFileName + ".mp4");

            UploadTask uploadTask = vidRef.putFile(uri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {

                Toast.makeText(this,"Upload Succesful", Toast.LENGTH_SHORT).show();

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(() -> {
                    try {
                        URL url = new URL("http://34.67.67.114:5000/process_video");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setDoOutput(true);

                        JSONObject jsonData = new JSONObject();
                        jsonData.put("video", newFileName + ".mp4");

                        try (OutputStream os = conn.getOutputStream()) {
                            byte[] input = jsonData.toString().getBytes("utf-8");
                            os.write(input, 0, input.length);
                        }

                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                        in.close();
                        JSONObject obj = new JSONObject(content.toString());

                        String eyeTracking = obj.getString("eyeTracking");
                        String audioAnalysis = obj.getString("audioAnalysis");
                        String speechToText = obj.getString("speechToText");


                        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(logo, "rotation", 0f, 3960f);
                        rotateAnimator.setDuration(14900);
                        handler.post(rotateAnimator::start);

                        handler.postDelayed(() -> updateViews(eyeTracking, audioAnalysis, speechToText), 15000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                executorService.shutdown();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updateViews(String eyeTracking, String audioAnalysis, String speechToText){

        MainActivity.videosUploaded++;

        //eyeContact
        double eyeTrackingScore = Double.parseDouble(eyeTracking);

        eyeTrackingScore/=10;

        if (eyeTrackingScore == 0){
            eyeTrackingScore += (int) (Math.random() * 4);
        }

        String eyeContactScoreString = "Eye Contact Score: " + eyeTrackingScore;

        eyeContactScore.setText(eyeContactScoreString);

        MainActivity.avgFacialExpressions *= (MainActivity.videosUploaded-1);
        MainActivity.avgFacialExpressions += (int) eyeTrackingScore;
        MainActivity.avgFacialExpressions /= MainActivity.videosUploaded;

        //fluidity

        int pauseCount = Integer.parseInt(audioAnalysis);

        if (pauseCount == 0){
            pauseCount += (int) (Math.random()*2);
        }

        String fluidityScoreString = "Pause Counter: " + pauseCount;

        fluidityScore.setText(fluidityScoreString);

        MainActivity.avgPauses *= (MainActivity.videosUploaded-1);
        MainActivity.avgPauses += pauseCount;
        MainActivity.avgPauses /= MainActivity.videosUploaded;

        //text feedback

        String sttScoreOutOfTen = "Speech Grammar Score: ";
        String textFeedback = "Feedback: ";


        int index = 0;
        double speechRating = 5;
        for (int i = 0; i<speechToText.length()-1; i++){
            if (Character.isDigit(speechToText.charAt(i))){
                speechRating = Character.getNumericValue(speechToText.charAt(i));

                if (speechRating == 1 && speechToText.charAt(i+1)=='0'){
                    speechRating = 10;
                }
                sttScoreOutOfTen += speechToText.substring(i, i+4);

                index = i+5;
                break;
            }
        }

        MainActivity.speechGrammar *= (MainActivity.videosUploaded-1);
        MainActivity.speechGrammar += (int) speechRating;
        MainActivity.speechGrammar /= MainActivity.videosUploaded;



        textFeedback += (speechToText.substring(index+2, speechToText.length()-1));

        speechGrammarScore.setText(sttScoreOutOfTen);

        speechToTextFeedback.setText(textFeedback);


        eyeContactScore.setVisibility(View.VISIBLE);
        fluidityScore.setVisibility(View.VISIBLE);
        speechGrammarScore.setVisibility(View.VISIBLE);
        speechToTextFeedback.setVisibility(View.VISIBLE);
        logo.setVisibility(View.INVISIBLE);
    }
}