package com.example.interviewprep;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import okhttp3.*;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static int avgPauses = 0;
    public static int avgFacialExpressions = 0;
    public static int speechGrammar = 0;
    public static int videosUploaded = 0;
    public static int bestResume = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goToStats(View view) {
        Intent intent = new Intent(MainActivity.this, UserStats.class);
        startActivity(intent);
    }

    public void goToResume(View view) {
        Intent intent = new Intent(MainActivity.this, ResumeUpload.class);
        startActivity(intent);
    }

    public void goToInterview(View view) {
        Intent intent = new Intent(MainActivity.this, InterviewUpload.class);
        startActivity(intent);
    }

    public void goToHelp(View view) {
        Intent intent = new Intent(MainActivity.this, HelpScreen.class);
        startActivity(intent);
    }
}