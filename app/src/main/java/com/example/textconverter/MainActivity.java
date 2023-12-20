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
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button inputImageBtn,recognizeTextbtn;
    private Button btnText;
    private ShapeableImageView imageIv;
    private EditText recognizedTextEt;
    private static final String TAG="MAIN_TAG";
    private Uri imageUri=null;
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=101;



    private String[] cameraPermissions;
    private String[] storagePermissions;

    private ProgressDialog progressDialog;

    private TextRecognizer textRecognizer;
    private TextToSpeech textToSpeech;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputImageBtn=findViewById(R.id.inputImageBtn);
        recognizeTextbtn=findViewById(R.id.recognizeTextBtn);
        imageIv=findViewById(R.id.imageIv);
        recognizedTextEt=findViewById(R.id.recognizedTextEt);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        textRecognizer= TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);



        inputImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputImagedialog();
            }
        });

        recognizeTextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if(imageUri==null){
                   Toast.makeText(MainActivity.this, "Pick image....", Toast.LENGTH_SHORT).show();
               }else{
                   recognizeTextFromimage();

                }
            }
        });

    }












    private void recognizeTextFromimage() {

        Log.d(TAG, "recognizeTextFromimage: ");

        progressDialog.setMessage("Preparing image");
        progressDialog.show();


        try {

            InputImage inputimage = InputImage.fromFilePath(this, imageUri);

            progressDialog.setMessage("Recognizing text...");

            Task<Text> textTaskResult = textRecognizer.process(inputimage)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {

                            progressDialog.dismiss();

                            String recognizedText=text.getText();

                            Log.d(TAG, "onSuccess: recognizedText:"+recognizedText);
                            recognizedTextEt.setText(recognizedText);


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "no text"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


        }catch (Exception e){
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, "no text found"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

   /* private void texttospeech() {


        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });


        btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                textToSpeech.speak( recognizedTextEt.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
            }
        });

    }*/

    private void showInputImagedialog() {
        PopupMenu popupMenu=new PopupMenu(this,inputImageBtn);
        popupMenu.getMenu().add(Menu.NONE,1,1,"CAMERA");
        popupMenu.getMenu().add(Menu.NONE,2,2,"GALLERY");

        popupMenu.show();


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                int id=menuItem.getItemId();
                if(id==1){

                    if(checkCameraPermission()){
                        pickImageCamera();
                    }else{
                        requestcameraPermission();
                    }

                }
                else if(id==2){

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

    private ActivityResultLauncher<Intent>galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()== Activity.RESULT_OK){
                        Intent data=result.getData();
                        imageUri= data.getData();
                        imageIv.setImageURI(imageUri);
                    }else{
                        Toast.makeText(MainActivity.this, "Cancelled.....", Toast.LENGTH_SHORT).show();
                    }
                }

            }
    );

    private void pickImageCamera(){
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Sample Title");
        values.put(MediaStore.Images.Media.DESCRIPTION,"SAMPLE DESCRIPTION");

        imageUri= getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
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
                        imageIv.setImageURI(imageUri);
                    }else{
                        Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
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
                    boolean storageaccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && storageaccepted){
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