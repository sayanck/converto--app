package com.example.textconverter.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.textconverter.R;

public class ImageViewActivity extends AppCompatActivity {

    private String image;
    private ImageView imageiv;
    private static final String TAG="IMAGE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        getSupportActionBar().setTitle("ImageView");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        imageiv=findViewById(R.id.imageiv);


        image=getIntent().getStringExtra("imageUri");

        Glide.with(this)
                .load(image)
                .placeholder(R.drawable.baseline_image_24)
                .into(imageiv);

    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}