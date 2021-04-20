package com.example.bikie;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class MainActivity extends AppCompatActivity {
    //CollapsingToolbarLayout toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       AppBarLayout toolbar = findViewById(R.id.appBarLayout);

    }
}