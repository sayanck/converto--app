package com.example.textconverter.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import com.example.textconverter.R;
import com.example.textconverter.adapters.AdapterPdfView;
import com.example.textconverter.models.ModelPdfView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdfViewActivity extends AppCompatActivity {

    private String pdfUri;

    private RecyclerView pdfViewRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);


        getSupportActionBar().setTitle("PDF VIEWER");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        pdfViewRv=findViewById(R.id.pdfViewRv);

        pdfUri= getIntent().getStringExtra("pdfuri");

        loadPdfPages();

    }

    private PdfRenderer.Page mCurrentPage=null;

    private void loadPdfPages() {

        ArrayList<ModelPdfView>pdfViewArrayList=new ArrayList<>();
        AdapterPdfView adapterPdfView=new AdapterPdfView(this,pdfViewArrayList);

        pdfViewRv.setAdapter(adapterPdfView);

        File file=new File(Uri.parse(pdfUri).getPath());
        try{

            getSupportActionBar().setSubtitle(file.getName());
        }catch(Exception e){

        }

        ExecutorService executorService= Executors.newSingleThreadExecutor();
        Handler handler=new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override
            public void run() {

                try{

                    ParcelFileDescriptor parcelFileDescriptor=ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY);

                    PdfRenderer mPdfRender= new PdfRenderer(parcelFileDescriptor);

                    int pageCount=mPdfRender.getPageCount();

                    if(pageCount<=0){

                        Toast.makeText(PdfViewActivity.this, "no page available", Toast.LENGTH_SHORT).show();
                    }
                    else{

                        for(int i=0; i<pageCount; i++){

                            if(mCurrentPage != null){

                                mCurrentPage.close();
                            }

                            mCurrentPage=mPdfRender.openPage(i);

                            Bitmap bitmap= Bitmap.createBitmap(mCurrentPage.getWidth(),mCurrentPage.getHeight(),Bitmap.Config.ARGB_8888);

                            mCurrentPage.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                            pdfViewArrayList.add(new ModelPdfView(Uri.parse(pdfUri),(i+1),pageCount,bitmap));
                        }
                    }

                }catch(Exception e){

                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapterPdfView.notifyDataSetChanged();
                    }
                });
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return super.onSupportNavigateUp();
    }
}