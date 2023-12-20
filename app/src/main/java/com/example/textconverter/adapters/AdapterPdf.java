package com.example.textconverter.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.textconverter.MyApplication;
import com.example.textconverter.R;
import com.example.textconverter.RvListenerPdf;
import com.example.textconverter.models.ModelPdf;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdapterPdf extends RecyclerView.Adapter<AdapterPdf.HolderPdf> {


    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;
    private RvListenerPdf rvListenerPdf;

    private static final String TAG= "ADAPTER_PDF_TAG";

    public AdapterPdf(Context context, ArrayList<ModelPdf> pdfArrayList, RvListenerPdf rvListenerPdf) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.rvListenerPdf=rvListenerPdf;
    }

    @NonNull
    @Override
    public HolderPdf onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_pdf,parent,false);

        return new HolderPdf(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdf holder, int position) {

        ModelPdf modelPdf= pdfArrayList.get(position);

        String name= modelPdf.getFile().getName();
        long timestamp=modelPdf.getFile().lastModified();

        String formattedDate= MyApplication.formatTimestamp(timestamp);


        loadFileSize(modelPdf,holder);
        loadThubnailFromPdfFile(modelPdf,holder);


        holder.nameTv.setText(name);
        holder.dateTv.setText(formattedDate);

       holder.itemView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               rvListenerPdf.onPdfClick(modelPdf,position);
           }
       });

       holder.moreBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               rvListenerPdf.onPdfMoreClick(modelPdf,position);
           }
       });

    }

    private void loadThubnailFromPdfFile(ModelPdf modelPdf, HolderPdf holder) {

        ExecutorService executorService= Executors.newSingleThreadExecutor();
        Handler handler= new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                Bitmap thumbnailBitmap=null;
                int pageCount=0;

                try{

                    ParcelFileDescriptor parcelFileDescriptor=ParcelFileDescriptor.open(modelPdf.getFile(),ParcelFileDescriptor.MODE_READ_ONLY);
                    PdfRenderer pdfRenderer= new PdfRenderer(parcelFileDescriptor);

                    pageCount= pdfRenderer.getPageCount();
                    if(pageCount<=0){

                        Toast.makeText(context, "No pages found", Toast.LENGTH_SHORT).show();
                    }
                    else{

                        PdfRenderer.Page currentPage= pdfRenderer.openPage(0);
                        thumbnailBitmap=Bitmap.createBitmap(currentPage.getWidth(),currentPage.getHeight(), Bitmap.Config.ARGB_8888);

                        currentPage.render(thumbnailBitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    }


                }catch(Exception e){

                }

                Bitmap finalThumbnailBitmap = thumbnailBitmap;
                int finalPageCount = pageCount;
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        Glide.with(context)
                                .load(finalThumbnailBitmap)
                                .fitCenter()
                                .placeholder(R.drawable.baseline_picture_as_pdf_24)
                                .into(holder.thumbnailiv);

                        holder.pagesTv.setText(""+ finalPageCount+" Paages");
                    }
                });

            }
        });

    }

    private void loadFileSize(ModelPdf modelPdf, HolderPdf holder) {

        double bytes= modelPdf.getFile().length();

        double kb=bytes/1024;
        double mb=kb/1024;

        String size="";

        if(mb>1){

            size=String.format("%.2f",mb)+" MB";
        }
        else if(kb>=1){

            size=String.format("%.2f",kb)+" KB";
        }
        else{

            size=String.format("%.2f",bytes)+" bytes";
        }

        holder.sizeTv.setText(size);

    }

    @Override
    public int getItemCount() {



        return pdfArrayList.size();
    }

    public class HolderPdf extends RecyclerView.ViewHolder{


        private ImageView thumbnailiv;
        private TextView nameTv,pagesTv, sizeTv,dateTv;
        private ImageButton moreBtn;

        public HolderPdf(@NonNull View itemView) {
            super(itemView);

            thumbnailiv=itemView.findViewById(R.id.thumbnailIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            pagesTv=itemView.findViewById(R.id.pagesTv);
            sizeTv=itemView.findViewById(R.id.sizeTv);
            dateTv=itemView.findViewById(R.id.dateTv);
            moreBtn=itemView.findViewById(R.id.moreBtn);


        }
    }

}
