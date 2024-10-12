package com.example.interviewprep;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class HelpScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.helpscreen_layout);
    }

    public void returnFromHelp(View view) {
        Intent intent = new Intent(HelpScreen.this, MainActivity.class);
        startActivity(intent);
    }


}
