package com.example.textconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.textconverter.activities.imagetopdf;

public class home extends AppCompatActivity {

    private Button button,button1,button5,button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        button=findViewById(R.id.button);
        button1=findViewById(R.id.button1);
        button5=findViewById(R.id.button5);
        button2=findViewById(R.id.button2);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(home.this,MainActivity.class);
                startActivity(intent);
            }
        });
       button.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent i=new Intent(home.this,textspeech.class);
               startActivity(i);
           }
       });
       button5.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent=new Intent(home.this, imagetopdf.class);
               startActivity(intent);
           }
       });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(home.this, Scanner.class);
                startActivity(intent);
            }
        });


    }
}