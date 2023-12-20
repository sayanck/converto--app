package com.example.textconverter.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.textconverter.PdfListFragment;
import com.example.textconverter.R;
import com.example.textconverter.imageListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class imagetopdf extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagetopdf);

        bottomNavigationView=findViewById(R.id.bottonNavigationView);

        loadImagesFragment();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId=item.getItemId();

                if(itemId==R.id.bottom_menu_images){

                    loadImagesFragment();

                }
                else if(itemId==R.id.bottom_menu_pdfs){

                    loadPdfsFragment();

                }

                return true;
            }
        });


    }

    private void loadImagesFragment() {

        setTitle("Images");
        imageListFragment imageListFragment= new imageListFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, imageListFragment,"imageListFragment");
        fragmentTransaction.commit();

    }

    private void loadPdfsFragment() {

        setTitle("PDFs");
        PdfListFragment pdfListFragment= new PdfListFragment();
        FragmentTransaction fragmentTransaction= getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,pdfListFragment,"PdfListFragment");
        fragmentTransaction.commit();

    }
}


