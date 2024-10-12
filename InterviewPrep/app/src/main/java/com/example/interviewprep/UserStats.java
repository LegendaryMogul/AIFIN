package com.example.interviewprep;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class UserStats extends AppCompatActivity {
    private TextView tvAvgSpeechGrammar;
    private TextView tvAvgFluidity;
    private TextView tvAvgFacialExpressions;
    private TextView tvBestResume;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_layout);

        tvAvgSpeechGrammar = findViewById(R.id.Grammar2);
        tvAvgFluidity = findViewById(R.id.fluidity2);
        tvAvgFacialExpressions = findViewById(R.id.FacialGestures2);
        tvBestResume = findViewById(R.id.BestResume);

        tvAvgSpeechGrammar.setText("Speech Grammar: " + " " + MainActivity.speechGrammar);
        tvAvgFluidity.setText("Pause Count: " + MainActivity.avgPauses);
        tvAvgFacialExpressions.setText("Eye Contact Score: " + MainActivity.avgFacialExpressions);
        tvBestResume.setText("Best Resume: " + MainActivity.bestResume);
    }



    public void returnToMain(View view) {
        Intent intent = new Intent(UserStats.this, MainActivity.class);
        startActivity(intent);
    }
}
