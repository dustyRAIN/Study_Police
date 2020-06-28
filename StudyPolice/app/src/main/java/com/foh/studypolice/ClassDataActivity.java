package com.foh.studypolice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;

import ModelClasses.ClassDetails;
import Utils.FrequencyUpdater;

import static AllConstants.IntegerKeys.FREQ_VALUE_CREATED_CLASS;
import static AllConstants.IntegerKeys.TYPE_CREATED_CLASS;
import static AllConstants.IntentKeys.EXTRA_KEY_CLASS_ID;
import static AllConstants.IntentKeys.EXTRA_KEY_CLASS_NAME;
import static AllConstants.IntentKeys.EXTRA_KEY_PREVIEW;
import static AllConstants.RestApiUrlsAndKeys.API_CLASSES;
import static AllConstants.RestApiUrlsAndKeys.API_MATERIALS_UPLOAD;
import static Utils.FileUtils.getPath;
import static Utils.FileUtils.getPathFromURI;
import static com.foh.studypolice.MainActivity.mCSRFToken;
import static com.foh.studypolice.MainActivity.mUser;

public class ClassDataActivity extends AppCompatActivity implements View.OnClickListener {

    protected static final int STORAGE_PERMISSION_WRITE = 26;
    protected static final int STORAGE_PERMISSION_READ = 19;

    private boolean CAN_WRITE = false;
    private boolean CAN_READ = false;

    private boolean ON_GOING = false;

    private static final int PICK_PDF_FILE = 25;

    private Uri mSelectedPDFUri;
    private Dialog mDialog;
    private EditText etvFileName;
    private TextView tvbUploadedFile;
    private TextView tvbUpload;

    private ClassDetails mClassDetails;

    private Gson mGson;

    private TextView mtvClassName;
    private TextView mtvbClassStudent;
    private TextView mtvbClassMaterial;
    private TextView mtvAccessCode;
    private TextView mtvbUploadMaterrial;
    private TextView mtvbViewAllStudent;
    private TextView mtvbViewAllMaterial;

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_data);

        mGson = new Gson();

        addToolBar();
        getIntentExtra();
        setUpDialog();

        mDrawer = findViewById(R.id.drawer_layout);
        mAppBarConfiguration = new AppBarConfiguration.Builder()
                .setDrawerLayout(mDrawer)
                .build();

        mtvClassName = findViewById(R.id.tv_side_class_name);
        mtvbClassStudent = findViewById(R.id.tvb_students);
        mtvbClassMaterial = findViewById(R.id.tvb_materials);
        mtvAccessCode = findViewById(R.id.tvb_access_code);
        mtvbUploadMaterrial = findViewById(R.id.tvb_upload_material);
        mtvbViewAllStudent = findViewById(R.id.view_all_student);
        mtvbViewAllMaterial = findViewById(R.id.view_all_material);



        mtvbViewAllStudent.setOnClickListener(this);
        mtvbViewAllMaterial.setOnClickListener(this);
        mtvbClassMaterial.setOnClickListener(this);
        mtvbClassStudent.setOnClickListener(this);
        mtvbUploadMaterrial.setOnClickListener(this);
    }

    private void addToolBar(){
        Toolbar toolbar = findViewById(R.id.sp_main_toolbar);

        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
    }

    private void getIntentExtra(){
        mClassDetails = null;
        if(getIntent().hasExtra(EXTRA_KEY_CLASS_ID)){
            int id = getIntent().getIntExtra(EXTRA_KEY_CLASS_ID, 1);
            getClassData(id);
        } else {
            noClassID();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CAN_WRITE = true;
                    if(CAN_READ) selectMaterialFile();
                } else {
                    CAN_WRITE = false;
                    Toast.makeText(this, "Permission denied. Can't upload file.", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case STORAGE_PERMISSION_READ: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CAN_READ = true;
                    if(CAN_WRITE) selectMaterialFile();
                } else {
                    CAN_READ = false;
                    Toast.makeText(this, "Permission denied. Can't upload file.", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == PICK_PDF_FILE){
                ON_GOING = false;
                if(data != null){
                    mSelectedPDFUri = data.getData();
                    showPopUpDialog();
                }
            }
        } else if(resultCode == RESULT_CANCELED){
            if(requestCode == PICK_PDF_FILE){
                ON_GOING = false;
            }
        }
    }



    /*
    * When a user selects a file from their phone memory, this dialog is shown.
    * using the dialog they can confirm the file or select again.
    * */
    private void setUpDialog(){
        mDialog = new Dialog(this);
        mDialog.setContentView(R.layout.dialog_upload_material);
        etvFileName = mDialog.findViewById(R.id.edit_file_name);
        tvbUploadedFile = mDialog.findViewById(R.id.tvb_uploaded_file);
        tvbUpload = mDialog.findViewById(R.id.tvb_upload);

        tvbUploadedFile.setOnClickListener(this);
        tvbUpload.setOnClickListener(this);
    }

    private void showPopUpDialog(){
        if(mSelectedPDFUri != null) {
            String name = getFileNameFromUri(mSelectedPDFUri);
            tvbUploadedFile.setText(name);
            etvFileName.setText(name);
        }

        mDialog.show();
    }


    /*
    * After the selection of a file, the file name should be extracted from the uri to save in database
    * */
    private String getFileNameFromUri(Uri uri){
        String path = getPathFromURI(this, uri);
        Log.i("path", path);
        String filename = path.substring(path.lastIndexOf("/")+1);
        String file;
        if (filename.indexOf(".") > 0) {
            file = filename.substring(0, filename.lastIndexOf("."));
        } else {
            file =  filename;
        }
        return file;
    }



    /*
    * @variable CAN_WRITE and @variable CAN_READ represents if the app has the permission to access the storage.
    * @variable mSelectedPDFUri stores the file uri of the file selected by the user to be uploaded.
    * */
    private void uploadFileToDatabase(){
        if(mSelectedPDFUri == null || mClassDetails == null || !CAN_READ || !CAN_WRITE) return;

        String filename = etvFileName.getText().toString().trim();

        if(filename.equals("") || filename.isEmpty()){
            etvFileName.setError("Can't be empty.");
            return;
        }

        File file = new File(getPath(this, mSelectedPDFUri));

        Ion.with(getApplicationContext())
                .load("POST", API_MATERIALS_UPLOAD)
                .setHeader("X-CSRFToken", mCSRFToken)
                .setMultipartParameter("classroom", String.valueOf(mClassDetails.id))
                .setMultipartParameter("name", filename)
                .setMultipartFile("the_file", "multipart/form-data", file)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null){
                            Log.d( "Error", e.toString() );
                            Toast.makeText(ClassDataActivity.this, "Error " + e, Toast.LENGTH_LONG).show();
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(ClassDataActivity.this, "Successfully uploaded.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }








    private void noClassID(){
        Toast.makeText(this, "No class found. Exiting...", Toast.LENGTH_LONG).show();
        finish();
    }


    /*
    * This activity represents a class created by the current user.
    * This method retrieves details about this class from database using the class id sent as @param id
    * */
    private void getClassData(int id){
        Ion.with(getApplicationContext())
                .load("GET", API_CLASSES + id)
                .setHeader("X-CSRFToken", mCSRFToken)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null){
                            noClassID();
                        } else {
                            mClassDetails = mGson.fromJson(result, ClassDetails.class);
                            if(mClassDetails == null){
                                noClassID();
                            } else {
                                setUpUI();
                            }
                        }
                    }
                });
    }

    private void setUpUI(){
        FrequencyUpdater.updateFrequency(getApplicationContext(), mCSRFToken, mClassDetails.id, TYPE_CREATED_CLASS, mUser.id, FREQ_VALUE_CREATED_CLASS);
        mtvClassName.setText(mClassDetails.name);
        mtvAccessCode.setText(mClassDetails.access_code);
    }


    /*
    * This method is provoked when a material of this class is clicked
    * */
    private void gotoClassMaterials(){
        Intent intent = new Intent(this, MaterialListActivity.class);
        intent.putExtra(EXTRA_KEY_CLASS_ID, mClassDetails.id);
        startActivity(intent);
    }

    /*
    *  Detailed information such as reading time of students and the amount of time a material is read
    *  can be seen in @activity ClassDataDetailsActivity. This metod takes a user to that activity
    * */
    private void gotoClassDataDetails(boolean isStu){
        Intent intent = new Intent(this, ClassDataDetailsActivity.class);
        intent.putExtra(EXTRA_KEY_CLASS_ID, mClassDetails.id);
        intent.putExtra(EXTRA_KEY_CLASS_NAME, mClassDetails.name);
        intent.putExtra(EXTRA_KEY_PREVIEW, isStu);
        startActivity(intent);
    }


    /*
    * @variable CAN_WRITE and @variable CAN_READ represents if the app has the permission to access the storage.
    * @variable ON_GOING ensures that only one instance of this method is being executed at any point of time.
    * */
    private void selectMaterialFile(){
        if(!CAN_READ || !CAN_WRITE) return;
        if(ON_GOING) return;
        ON_GOING = true;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        startActivityForResult(intent, PICK_PDF_FILE);
    }


    /*
    * To select a file from storage, the app need the storage permission.
    * This method is initiated at the beginning of the activity to check for permissions to access the storage.
    * */
    private void checkPermissionToAccessStorage(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            CAN_WRITE = false;

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_WRITE
                );

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_WRITE
                );
            }
        } else {
            CAN_WRITE = true;
            if(CAN_READ)
                selectMaterialFile();
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            CAN_READ = false;

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_READ
                );

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_READ
                );
            }
        } else {
            CAN_READ = true;
            if(CAN_WRITE)
                selectMaterialFile();
        }

    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){

            case R.id.tvb_students:
                break;

            case R.id.tvb_materials:
                gotoClassMaterials();
                break;

            case R.id.tvb_uploaded_file:
            case R.id.tvb_upload_material:
                checkPermissionToAccessStorage();
                break;

            case R.id.tvb_upload:
                uploadFileToDatabase();
                break;

            case R.id.view_all_student:
                gotoClassDataDetails(true);
                break;

            case R.id.view_all_material:
                gotoClassDataDetails(false);
                break;
        }
    }
}
