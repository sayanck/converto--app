package com.example.textconverter;

import static com.example.textconverter.Constants.IMAGES_FOLDER;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.textconverter.adapters.AdapterImage;
import com.example.textconverter.models.ModelImage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class imageListFragment extends Fragment {

    private Context mContext;
    private String[] cameraPermissions;
    private String[] storagePermissions;
    private static final int STORAGE_REQUEST_CODE=100;
    private static final int CAMERA_REQUEST_CODE=101;
    private FloatingActionButton addImageFab;
    private Uri imageUri=null;

    private static final String TAG="IMAGE_LIST_TAG";

    private RecyclerView imagesRv;

    private ArrayList<ModelImage> allImageArrayList;

    private AdapterImage adapterImage;

    private ProgressDialog progressDialog;
    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    List<String> imagesEncodedList;




    public imageListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext= context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        addImageFab= view.findViewById(R.id.addImageFab);
        imagesRv=view.findViewById(R.id.imagesRv);


        progressDialog=new ProgressDialog(mContext);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);


        loadImages();


        addImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showInputImageDialog();

            }
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.menu_images,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId=item.getItemId();

        if(itemId==R.id.images_item_delete){
            AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
            builder.setTitle("Delete Images")
                    .setMessage("Are you sure you want to delete All/Selected images?")
                    .setPositiveButton("Delete All", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            deleteImages(true);
                        }
                    })
                    .setNeutralButton("Delete Selected", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteImages(false);

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    })
                    .show();

        }
        else if(itemId==R.id.images_item_pdf){

            AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
            builder.setTitle("Convert To PDF")
                    .setMessage("Convert ALL or Selected Images to PDF")
                    .setPositiveButton("Convert All", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            convertImagesToPdf(true);
                        }
                    })
                    .setNeutralButton("Convert Selected", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            convertImagesToPdf(false);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                        }
                    })
                    .show();

        }


        return super.onOptionsItemSelected(item);
    }


    private void convertImagesToPdf(boolean convertAll){


        progressDialog.setMessage("Convrting To PDF.......");
        progressDialog.show();

        ExecutorService executorService= Executors.newSingleThreadExecutor();
        Handler handler=new Handler(Looper.getMainLooper());


        executorService.execute(new Runnable() {
            @Override
            public void run() {

                ArrayList<ModelImage> imageToPdfList= new ArrayList<>();
                if(convertAll){

                    imageToPdfList=allImageArrayList;
                }
                else{

                    for(int i=0; i<allImageArrayList.size(); i++){

                        if(allImageArrayList.get(i).isChecked()){

                            imageToPdfList.add(allImageArrayList.get(i));
                        }
                    }

                }

                try {

                    File root=new File(mContext.getExternalFilesDir(null),Constants.PDF_FOLDER);
                    root.mkdirs();

                    long timestamp=System.currentTimeMillis();
                    String fileName="PDF_"+timestamp +".pdf";

                    File file =new File(root,fileName);
                    FileOutputStream fileOutputStream= new FileOutputStream(file);
                    PdfDocument pdfDocument=new PdfDocument();


                    for(int i=0; i<imageToPdfList.size(); i++){

                        Uri imageToAdInPdfUri= imageToPdfList.get(i).getImageUri();

                        try{

                            Bitmap bitmap;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                bitmap= ImageDecoder.decodeBitmap(ImageDecoder.createSource(mContext.getContentResolver(),imageToAdInPdfUri));
                            }
                            else{

                                bitmap=MediaStore.Images.Media.getBitmap(mContext.getContentResolver(),imageToAdInPdfUri);
                            }

                            bitmap=bitmap.copy(Bitmap.Config.ARGB_8888,false);

                            PdfDocument.PageInfo pageInfo=new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), i+1).create();

                            PdfDocument.Page page=pdfDocument.startPage(pageInfo);


                            Paint paint=new Paint();
                            paint.setColor(Color.WHITE);
                            Canvas canvas=page.getCanvas();
                            canvas.drawPaint(paint);

                            canvas.drawBitmap(bitmap,0f,0f,null);

                            pdfDocument.finishPage(page);
                            bitmap.recycle();


                        }
                        catch(Exception e){

                        }

                    }

                    pdfDocument.writeTo(fileOutputStream);
                    pdfDocument.close();

                }
                catch (Exception e){
                    
                    progressDialog.dismiss();


                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {


                        progressDialog.dismiss();
                        Toast.makeText(mContext, "Converted...", Toast.LENGTH_SHORT).show();


                    }
                });

            }


        });

    }


    private void deleteImages(boolean deleteAll){


        ArrayList<ModelImage> imagesToDeleteList= new ArrayList<>();

        if(deleteAll){

            imagesToDeleteList=allImageArrayList;

        }
        else{

            for(int i=0; i<allImageArrayList.size(); i++){

                if(allImageArrayList.get(i).isChecked()){

                    imagesToDeleteList.add(allImageArrayList.get(i));

                }

            }
        }


        for(int i=0; i<imagesToDeleteList.size(); i++){

            try {


                String pathOfImageToDelete = imagesToDeleteList.get(i).getImageUri().getPath();
                File file = new File(pathOfImageToDelete);

                if(file.exists()){
                    boolean isDeleted = file.delete();
                }



            }
            catch (Exception e){
                Toast.makeText(mContext, "delete images", Toast.LENGTH_SHORT).show();
            }

        }

        Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();

        loadImages();


    }


    private void loadImages(){

        allImageArrayList=new ArrayList<>();
        adapterImage=new AdapterImage(mContext,allImageArrayList);

        imagesRv.setAdapter(adapterImage);

        File folder=new File(mContext.getExternalFilesDir(null), IMAGES_FOLDER);

        if(folder.exists()){

            File[] files=folder.listFiles();

            if(files!=null){

                for(File file: files){
                    Log.d(TAG,"loadImages:fileName:"+file.getName());

                    Uri imageUri=Uri.fromFile(file);
                    ModelImage modelImage=new ModelImage(imageUri,false);
                    allImageArrayList.add(modelImage);
                    adapterImage.notifyItemInserted(allImageArrayList.size());

                }

            }else{
                Toast.makeText(mContext, "folder empty", Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(mContext, "Folder not exsist", Toast.LENGTH_SHORT).show();

        }

    }

    private void saveImageToAppLevelDirectory(Uri imageUriToBeSaved){

        try{

            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap= ImageDecoder.decodeBitmap(ImageDecoder.createSource(mContext.getContentResolver(),imageUriToBeSaved));
            }
            else{
                bitmap=MediaStore.Images.Media.getBitmap(mContext.getContentResolver(),imageUriToBeSaved);
            }

            File directory=new File(mContext.getExternalFilesDir(null),Constants.IMAGES_FOLDER);
            directory.mkdirs();


            long timestamp=System.currentTimeMillis();
            String fileName= timestamp+".jpeg";

            File file =new File(mContext.getExternalFilesDir(null),""+ Constants.IMAGES_FOLDER+"/"+ fileName);

            try{

                FileOutputStream fos=new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                fos.flush();
                fos.close();
                Toast.makeText(mContext, "image saved", Toast.LENGTH_SHORT).show();

            }catch (Exception e){
                Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
            }




        }catch (Exception e){
            Toast.makeText(mContext, "Failed.....", Toast.LENGTH_SHORT).show();

        }


    }

    private void showInputImageDialog(){

        PopupMenu popupMenu=new PopupMenu(mContext,addImageFab);
        popupMenu.getMenu().add(Menu.NONE,1,1,"CAMERA");
        popupMenu.getMenu().add(Menu.NONE,2,2,"GALLERY");

        popupMenu.show();


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                int itemId=menuItem.getItemId();
                if(itemId==1){

                    if(checkCameraPermission()){
                        pickImageCamera();
                    }else{
                        requestCameraPermission();
                    }

                }
                else if(itemId==2){

                    if(checkStoragePermission()){
                        pickImageGallery();
                    }else{
                        requestStoragePermission();
                    }
                }

                return true;
            }
        });


    }

    private void pickImageGallery(){

        Intent intent= new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }



    private ActivityResultLauncher<Intent>galleryActivityResultLauncher=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                  if(result.getResultCode()== Activity.RESULT_OK){
                        Intent data=result.getData();

                        imageUri=data.getData();

                        saveImageToAppLevelDirectory(imageUri);


                        ModelImage modelImage=new ModelImage(imageUri,false);
                        allImageArrayList.add(modelImage);
                        adapterImage.notifyItemInserted(allImageArrayList.size());


                    }else {
                        Toast.makeText(mContext, "cancelled...", Toast.LENGTH_SHORT).show();
                    }






                }
            }
    );

    private void pickImageCamera(){

        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"TEMP IMAGE TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"TEMP IMAGE DESCRIPTION");

        imageUri=mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);

    }

    private ActivityResultLauncher<Intent>cameraActivityResultLauncher=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if(result.getResultCode()== Activity.RESULT_OK){

                        saveImageToAppLevelDirectory(imageUri);


                        ModelImage modelImage=new ModelImage(imageUri,false);
                        allImageArrayList.add(modelImage);
                        adapterImage.notifyItemInserted(allImageArrayList.size());


                    }else {
                        Toast.makeText(mContext, "cancelled...", Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );

    private boolean checkStoragePermission(){

        boolean result= ContextCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                PackageManager.PERMISSION_GRANTED;

        return result;

    }

    private void requestStoragePermission(){

        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);

    }

    private boolean checkCameraPermission(){

        boolean cameraResult=ContextCompat.checkSelfPermission(mContext,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED;
        boolean storageResult=ContextCompat.checkSelfPermission(mContext,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;

        return cameraResult && storageResult;

    }

    private void requestCameraPermission(){

        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case CAMERA_REQUEST_CODE:{

                if(grantResults.length>0){

                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && storageAccepted){
                        //



                        pickImageCamera();



                    }else{
                        Toast.makeText(mContext, "permision required", Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(mContext, "cancelled....", Toast.LENGTH_SHORT).show();
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{

                if(grantResults.length>0){

                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;

                    if(storageAccepted){

                        pickImageGallery();


                    }else{
                        Toast.makeText(mContext, "permission required", Toast.LENGTH_SHORT).show();
                    }


                }else{
                    Toast.makeText(mContext, "cancelled..", Toast.LENGTH_SHORT).show();
                }

            }
            break;
        }

    }
}