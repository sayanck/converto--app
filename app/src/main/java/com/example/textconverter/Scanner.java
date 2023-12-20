package com.example.textconverter;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class Scanner extends AppCompatActivity {

    private Button cameraQr,galleryQr,scanBtn;
    private ImageView imageQr;
    private TextView resultQr;

    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=101;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri imageUri=null;

    private BarcodeScanner barcodeScanner;
    private BarcodeScannerOptions barcodeScannerOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        cameraQr=findViewById(R.id.cameraQr);
        galleryQr=findViewById(R.id.galleryQr);
        scanBtn=findViewById(R.id.scanBtn);
        imageQr=findViewById(R.id.imageQr);
        resultQr=findViewById(R.id. resultQr);


        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        barcodeScannerOptions=new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build();

        barcodeScanner= BarcodeScanning.getClient(barcodeScannerOptions);


        cameraQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkCameraPermission()){
                    pickImageCamera();
                }else{
                    requestcameraPermission();
                }


            }
        });

        galleryQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkStoragePermission()){
                    pickImageGallery();
                }else{
                    requestStoragePermission();
                }



            }
        });

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(imageUri==null){

                    Toast.makeText(Scanner.this, "Please select an image first....", Toast.LENGTH_SHORT).show();
                }
                else{

                    detectResultfromImage();
                }
            }
        });

    }

    private void detectResultfromImage() {

        try{

            InputImage inputImage= InputImage.fromFilePath(this,imageUri);
            Task<List<Barcode>> barcodeResult= barcodeScanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {

                            extractBarCodeQRCodeInfo(barcodes);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(Scanner.this, "failed to scan !!! invalid !!!", Toast.LENGTH_SHORT).show();
                        }
                    });


        }catch(Exception e){

            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }

    }

    private void extractBarCodeQRCodeInfo(List<Barcode> barcodes) {

        for(Barcode barcode: barcodes){

            Rect bounds =barcode.getBoundingBox();
            Point[] corners=barcode.getCornerPoints();

            String rawValue= barcode.getRawValue();

            int valueType=barcode.getValueType();

            switch(valueType){

                case Barcode.TYPE_WIFI:{

                    Barcode.WiFi typeWiFi= barcode.getWifi();

                    String ssid= ""+ typeWiFi.getSsid();
                    String password= ""+typeWiFi.getPassword();
                    String enecryptionType= ""+typeWiFi.getEncryptionType();

                    resultQr.setText("TYPE: TYPE_WIFI \nssid: "+ ssid +"\npassword:"+password+"\nencryptionType"+enecryptionType+"\nraw value:"+rawValue);

                }
                break;

                case Barcode.TYPE_URL:{

                    Barcode.UrlBookmark typeUrl= barcode.getUrl();

                    String title = ""+ typeUrl.getTitle();
                    String url=""+typeUrl.getUrl();

                    resultQr.setText("TYPE: TYPE_URL \ntitle: "+ title +"\nurl:"+url+"\nraw value:"+rawValue);

                }
                break;

                case Barcode.TYPE_EMAIL:{

                    Barcode.Email typeEmail= barcode.getEmail();

                    String address=""+ typeEmail.getAddress();
                    String body=""+ typeEmail.getBody();
                    String subject=""+ typeEmail.getSubject();

                    resultQr.setText("TYPE: TYPE_EMAIL \naddress: "+ address +"\nbody:"+body+"\nsubject:"+subject+"\nraw value:"+rawValue);

                }
                break;

                case Barcode.TYPE_CONTACT_INFO: {

                    Barcode.ContactInfo typeContact= barcode.getContactInfo();

                    String title=""+typeContact.getTitle();
                    String organizer=""+typeContact.getOrganization();
                    String name=""+typeContact.getName();
                    String phone=""+typeContact.getPhones().get(0).getNumber();

                    resultQr.setText("TYPE: TYPE_CONTACT_INFO \ntitle: "+ title +"\norganizer:"+organizer+"\nname"+name+"\nphone"+phone+"\nraw value:"+rawValue);

                }
                break;

                default:{

                    resultQr.setText("raw value: "+ rawValue);
                }


            }


        }

    }


    private void pickImageGallery(){
        Intent intent= new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
         galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()== Activity.RESULT_OK){
                        Intent data=result.getData();
                        imageUri= data.getData();
                        imageQr.setImageURI(imageUri);
                    }else{
                        Toast.makeText(Scanner.this, "Cancelled.....", Toast.LENGTH_SHORT).show();
                    }
                }

            }
    );

    private void pickImageCamera(){
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Sample IMAGE Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"SAMPLE IMAGE DESCRIPTION");

        imageUri= getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent>cameraActivityResultLauncher=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()==Activity.RESULT_OK){

                        Intent data=result.getData();
                        imageQr.setImageURI(imageUri);
                    }else{
                        Toast.makeText(Scanner.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    private boolean checkStoragePermission(){

        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }


    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission(){
        boolean cameraResult=ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean storageResult=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return cameraResult && storageResult;

    }
    private void requestcameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
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
                        pickImageCamera();

                    }else{
                        Toast.makeText(this, "permission required", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{

                if(grantResults.length>0){
                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickImageGallery();

                    }else{

                        Toast.makeText(this, "permission required", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }
    }


}