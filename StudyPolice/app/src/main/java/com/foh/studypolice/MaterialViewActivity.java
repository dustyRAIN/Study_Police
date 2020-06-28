package com.foh.studypolice;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;

import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.FileOutputStream;

import ModelClasses.ErrorDetails;
import ModelClasses.FaceMatchDetails;
import ModelClasses.MaterialDetails;
import Utils.EyeTracker;
import Utils.FaceTrackerBot;
import Utils.FrequencyUpdater;

import static AllConstants.IntegerKeys.FREQ_VALUE_MATERIAL_LONG;
import static AllConstants.IntegerKeys.FREQ_VALUE_MATERIAL_SHORT;
import static AllConstants.IntegerKeys.MODE_ONLY_VIEW;
import static AllConstants.IntegerKeys.MODE_VIEW_AND_FACE;
import static AllConstants.IntegerKeys.TYPE_MATERIAL;
import static AllConstants.IntentKeys.EXTRA_KEY_CLASS_ID;
import static AllConstants.IntentKeys.EXTRA_KEY_MATERIAL_ID;
import static AllConstants.IntentKeys.EXTRA_KEY_MATERIAL_LINK;
import static AllConstants.IntentKeys.EXTRA_KEY_MATERIAL_MODE;
import static AllConstants.RestApiUrlsAndKeys.API_FACE_MATCH;
import static AllConstants.RestApiUrlsAndKeys.API_MATERIALS_GET;
import static AllConstants.RestApiUrlsAndKeys.API_STATS_READTIME_ADD;
import static AllConstants.RestApiUrlsAndKeys.BASE_URL;
import static com.foh.studypolice.ClassDataActivity.STORAGE_PERMISSION_READ;
import static com.foh.studypolice.ClassDataActivity.STORAGE_PERMISSION_WRITE;
import static com.foh.studypolice.MainActivity.mCSRFToken;
import static com.foh.studypolice.MainActivity.mUser;


public class MaterialViewActivity extends AppCompatActivity implements EyeTracker.EyeTrackCallback {

    private String TAG = "huh";

    protected static final int PERMISSION_CAMERA = 50;
    private final int INITIAL_RETRY_LIMIT = 30;
    private final int SECOND_IN_MILLIES = 1000;
    private final int WAIT_SECOND_AFTER_MATCH = 90;
    private final int WAIT_SECOND_AFTER_NOT_MATCH = 10;

    private CameraSource mCamera;
    //private Camera mCamera;
    private EyeTracker mEyeTracker;

    private boolean ON_GOING_FACE = false;

    private int freq_value = 0;
    private int mClassID;
    private int mMaterialID;
    private String mMaterialLink;
    private int mViewMode;

    private boolean CAN_WRITE = false;
    private boolean CAN_READ = false;
    private boolean CAN_SNAP = false;
    private boolean CAN_TRACK_EYES = false;

    private boolean ON_GOING = false;
    private boolean ON_GOING_CAMERA = false;

    private long mLastMatchTime = -1;
    private long mTotalFaceDetectedTime = 0;
    private int mRetryRemained = 30;
    private int mWaitingSeconds = 60;
    private long mLastEyeFoundTime = -1;
    private long mEyeDetectedTime = -1;
    private long mTotalEyeOpenedTime = 0;


    private MaterialDetails mMaterialDetails;
    private Gson mGson;

    private String  HOME_DIR;

    private PDFView mPDFView;
    private FrameLayout mFaceIndicator;
    private FrameLayout mEyeIndicator;


    private String tFilename;
    private File tPictureFile;

    private ConstraintLayout mBottomCont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_view);

        mPDFView = findViewById(R.id.pdf_viewer);
        mFaceIndicator = findViewById(R.id.indicate_face);
        mEyeIndicator = findViewById(R.id.indicate_eye);
        mBottomCont = findViewById(R.id.bottom_cont);

        HOME_DIR = new ContextWrapper(this).getFilesDir().getPath();

        mGson = new Gson();


        getIntentExtras();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(mCamera != null){
            mCamera.stop();
            mCamera.release();
            mCamera = null;
        }
        if(mViewMode == MODE_VIEW_AND_FACE){
            updateReadingTime();
        }
    }


    /*
    *   When this activity is getting paused, total reading time till then is recorded in the database.
    *   Involked by @method onPause()
    * */
    private void updateReadingTime(){
        long currentTime = System.currentTimeMillis();
        if(mLastMatchTime > 0){
            mTotalFaceDetectedTime += (currentTime - mLastMatchTime);
        }
        mLastMatchTime = -1;
        if(mEyeDetectedTime > 0){
            mTotalEyeOpenedTime += (currentTime - mEyeDetectedTime);
        }
        mEyeDetectedTime = -1;

        long readTime;
        //readTime = Math.min(mTotalEyeOpenedTime, mTotalFaceDetectedTime);
        readTime = mTotalFaceDetectedTime;
        readTime /= SECOND_IN_MILLIES;

        if(readTime == 0) return;

        Ion.with(getApplicationContext())
                .load("POST", API_STATS_READTIME_ADD)
                .setHeader("X-CSRFToken", mCSRFToken)
                .setMultipartParameter("classroom", String.valueOf(mClassID))
                .setMultipartParameter("student", String.valueOf(mUser.id))
                .setMultipartParameter("material", String.valueOf(mMaterialID))
                .setMultipartParameter("duration", String.valueOf(readTime))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null){
                            Log.d(TAG, e.toString());
                        }
                    }
                });
    }

    private void getIntentExtras(){
        boolean good = true;
        boolean has_mat_id = false;

        if(getIntent().hasExtra(EXTRA_KEY_CLASS_ID)){
            mClassID = getIntent().getIntExtra(EXTRA_KEY_CLASS_ID, 0);
        } else good = false;

        if(getIntent().hasExtra(EXTRA_KEY_MATERIAL_ID)){
            mMaterialID = getIntent().getIntExtra(EXTRA_KEY_MATERIAL_ID, 0);
            has_mat_id = true;
        } else good = false;

        if(getIntent().hasExtra(EXTRA_KEY_MATERIAL_LINK)){
            mMaterialLink = getIntent().getStringExtra(EXTRA_KEY_MATERIAL_LINK);
        } else good = false;

        if(getIntent().hasExtra(EXTRA_KEY_MATERIAL_MODE)){
            mViewMode = getIntent().getIntExtra(EXTRA_KEY_MATERIAL_MODE, MODE_ONLY_VIEW);
        } else {
            mViewMode = MODE_ONLY_VIEW;
        }

        if(good){
            freq_value = FREQ_VALUE_MATERIAL_LONG;
            checkForPermissionAndProceed();
            setUpUI();
        } else if(has_mat_id){
            getMaterialInfo();
        } else{
            noMaterialError("Material not found. Quiting...");
        }
    }

    private void noMaterialError(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finish();
    }


    private void setUpUI(){
        if(mViewMode == MODE_ONLY_VIEW){
            mBottomCont.setVisibility(View.GONE);
        } else if(mViewMode == MODE_VIEW_AND_FACE){
            mBottomCont.setVisibility(View.VISIBLE);
            FrequencyUpdater.updateFrequency(getApplicationContext(), mCSRFToken, mMaterialID, TYPE_MATERIAL, mUser.id, freq_value);
        }
    }


    /*
    *   This activity is triggered with the material id that is to be opened in this activity.
    *   This method retrieves details about that material from the database.
    * */
    private void getMaterialInfo(){
        Ion.with(getApplicationContext())
                .load("get", API_MATERIALS_GET)
                .setHeader("X-CSRFToken", mCSRFToken)
                .setMultipartParameter("id", String.valueOf(mMaterialID))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null){
                            noMaterialError("Connection or server problem.");
                            Log.d(TAG, e.toString());
                        } else {
                            ErrorDetails errorDetails = mGson.fromJson(result, ErrorDetails.class);
                            if(errorDetails == null || errorDetails.error == null || errorDetails.error.equals("") || errorDetails.error.isEmpty()){
                                mMaterialDetails = mGson.fromJson(result, MaterialDetails.class);
                                if(mMaterialDetails != null){
                                    mMaterialLink = mMaterialDetails.the_file;
                                    mClassID = mMaterialDetails.classroom;
                                    freq_value = FREQ_VALUE_MATERIAL_SHORT;
                                    checkForPermissionAndProceed();
                                    setUpUI();
                                } else {
                                    noMaterialError("Interruption occured. Try again.");
                                }
                            } else{
                                noMaterialError(errorDetails.error);
                            }
                        }
                    }
                });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CAN_WRITE = true;
                    if(CAN_READ) findMaterial();
                } else {
                    CAN_WRITE = false;
                    Toast.makeText(this, "Permission denied. Can't load file.", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case STORAGE_PERMISSION_READ: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CAN_READ = true;
                    if(CAN_WRITE) findMaterial();
                } else {
                    CAN_READ = false;
                    Toast.makeText(this, "Permission denied. Can't load file.", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case PERMISSION_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CAN_SNAP = true;
                    identifyFaceAndTrackEyes();
                } else {
                    CAN_SNAP = false;
                    Toast.makeText(this, "Permission denied. Can't identify reader.", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }












    /*
     * The material file once downloaded from the database, will be saved in phone storage.
     *
     * This method is initiated at the beginning of the activity to check for permissions to access the storage.
     * */
    private void checkForPermissionAndProceed(){

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
                findMaterial();
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
                findMaterial();
        }

    }




    /*
    *   Before loading the material from database, this method looks for the material , if it were downloaded before.
    *   If the file was not downloaded before, this method initiates the download process. Otherwise, starts the process of
    *   loading the downloaded file
    * */
    private void findMaterial(){
        if(ON_GOING) return;
        ON_GOING = true;
        //Log.i("diraru", HOME_DIR);
        File file = new File(HOME_DIR + "/" + mClassID, mMaterialID + ".pdf");
        if(!file.exists()){

            PRDownloader.download(
                    BASE_URL + mMaterialLink.substring(1),
                    HOME_DIR + "/" + mClassID,
                    mMaterialID + ".pdf"
            ).setHeader("X-CSRFToken", mCSRFToken).build().start(new OnDownloadListener() {
                @Override
                public void onDownloadComplete() {
                    File downloadedFile = new File(HOME_DIR + "/" + mClassID, mMaterialID + ".pdf");
                    loadMaterial(downloadedFile);
                }

                @Override
                public void onError(Error error) {
                    noMaterialError("ERROR " + error);
                }
            });


        } else {
            loadMaterial(file);
        }
    }

    private void loadMaterial(File file){
        mPDFView.fromFile(file).enableDoubletap(true).spacing(10).load();
        if(mViewMode == MODE_VIEW_AND_FACE){
            checkForCameraPermission();
        }
    }



    /*
    *   To recognize face, camera access is needed.
    *   This method checks for camera access permission.
    * */
    private void checkForCameraPermission(){
        if(mViewMode != MODE_VIEW_AND_FACE) return;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            CAN_SNAP = false;

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CAMERA
                );

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA
                );
            }
        } else {
            CAN_SNAP = true;
            identifyFaceAndTrackEyes();
        }
    }

    /*
    * @variable ON_GOING_CAMERA ensures that only one instance of this method is running at any time.
    *
    * This method restarts the process of recognizing face 1.5s after completion of every cycle.
    * */
    private void identifyFaceAndTrackEyes(){
        if(ON_GOING_CAMERA) return;
        ON_GOING_CAMERA = true;
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startRecognizingFace();
            }
        },1500);
        ON_GOING_CAMERA = false;
    }


    /*
    *  This method sets up the camera for taking pictures and detecting eyes so that the app can understand
    *   if the user has keeping his/her eyes open.
    * */
    private void startRecognizingFace(){
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG).show();
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
            } else {

                FaceDetector detector = new FaceDetector.Builder(this)
                        .setTrackingEnabled(true)
                        .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                        .setMode(FaceDetector.FAST_MODE)
                        .build();
                FaceTrackerBot faceTrackerBot = new FaceTrackerBot(this, this);
                detector.setProcessor(new MultiProcessor.Builder<>(faceTrackerBot).build());

                if (!detector.isOperational()) {
                    // Note: The first time that an app using face API is installed on a device, GMS will
                    // download a native library to the device in order to do detection.  Usually this
                    // completes before the app is run for the first time.  But if that download has not yet
                    // completed, then the above call will not detect any faces.
                    //
                    // isOperational() can be used to check if the required native library is currently
                    // available.  The detector will automatically become operational once the library
                    // download completes on device.
                    Log.d(TAG, "Face detector dependencies are not yet available.");
                }

                mCamera = new CameraSource.Builder(this, detector)
                        .setRequestedPreviewSize(1024, 768)
                        .setFacing(CameraSource.CAMERA_FACING_FRONT)
                        .setRequestedFps(30.0f)
                        .build();
                try{
                    mCamera.start();
                    takeSnapAndRecognize();
                } catch (Exception e){
                    Toast.makeText(this, "Can't open camera", Toast.LENGTH_LONG).show();
                    identifyFaceAndTrackEyes();
                }
            }
        }
    }






    /*
    *   The app will capture a picture in the background and start the process of sending it to the database for matching
    *   with the user's face. After capturing the image, the app stores the image in the phone storage.
    *   From their the image is sent to the database.
    *
    *   @variable ON_GOING_FACE ensures that only one instance of this method is running at any time.
    * */
    private void takeSnapAndRecognize(){
        if(mCamera == null) {
            Toast.makeText(this, "Unable to access the camera", Toast.LENGTH_LONG).show();
            return;
        }
        if(ON_GOING_FACE) return;
        ON_GOING_FACE = true;
        Log.d(TAG, "inside takesnap");

        String homeDir = new ContextWrapper(getApplicationContext()).getFilesDir().getPath();
        File pictureFileDir =  new File(homeDir, "tempPhoto");

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()){
            Log.d(TAG, "Can't create directory to save image.");
            Toast.makeText(this, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
            ON_GOING_FACE = false;
            return;
        }

        tFilename = pictureFileDir.getPath() + File.separator + mUser.id + ".jpg";
        tPictureFile = new File(tFilename);



        mCamera.takePicture(null,  new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {
                try {
                    FileOutputStream fos = new FileOutputStream(tPictureFile);
                    fos.write(bytes);
                    fos.close();
                    Log.i(TAG, "saved");
                    recognizeFace(tPictureFile);
                } catch (Exception error) {
                    Log.d(TAG, "File" + tFilename + "not saved: " + error.getMessage());
                }
            }
        });
    }


    /*
    *  The snap of the face of the user is sent to the database to find a match.
    * */
    private void recognizeFace(File faceImage){
        Ion.with(getApplicationContext())
                .load("POST", API_FACE_MATCH)
                .setHeader("X-CSRFToken", mCSRFToken)
                .setMultipartFile("test_image", "multipart/form-data", faceImage)
                .setMultipartParameter("user", String.valueOf(mUser.id))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null){
                            connectionError(e.toString());
                            faceDidNotMatch();
                        } else {
                            FaceMatchDetails matchDetails = mGson.fromJson(result, FaceMatchDetails.class);
                            if(matchDetails == null){
                                connectionError("No Internet");
                                faceDidNotMatch();
                            } else if(matchDetails.matched == 1){
                                faceMatched();
                            } else {
                                faceDidNotMatch();
                            }
                        }
                    }
                });
    }


    /*
    *   If the taken snap of the face of the user does not match with the user's actual face, this method is invoked.
    *
    *   @variable mLastMatchTime indicates the time when the face matched for the last time.
    *   @variable mTotalFaceDetectedTime indicates for how long the face has been matching with the user.
    *
    *   Since the face did not match, after waiting for some times, the app should try again for a match by
    *   restarting the process of taking a photo.
    *
    *   @variable mWaitingSeconds idicates the cooldown before restarting the face recognition process
    * */
    private void faceDidNotMatch(){
        Log.d(TAG, "did not match");
        mFaceIndicator.setBackgroundColor(getResources().getColor(R.color.colorLightRed));
        long currentTime = System.currentTimeMillis();
        if(mLastMatchTime > 0){
            mTotalFaceDetectedTime += (currentTime - mLastMatchTime);
        }
        mLastMatchTime = -1;
        mRetryRemained--;
        mWaitingSeconds = WAIT_SECOND_AFTER_NOT_MATCH;
        waitAndRecognizeAgain();
    }


    /*
     *   If the taken snap of the face of the user matches with the user's actual face, this method is invoked.
     *
     *   @variable mLastMatchTime indicates the time when the face matched for the last time.
     *   @variable mTotalFaceDetectedTime indicates for how long the face has been matching with the user.
     *
     *   Since the face did match, after waiting for some time, the app should again check if the reader's face is still
     *   matching with the actual user by restarting the process of taking a photo.
     *
     *   @variable mWaitingSeconds idicates the cooldown before restarting the face recognition process
     * */
    private void faceMatched(){
        Log.d(TAG, "matched  " + mTotalFaceDetectedTime);
        mFaceIndicator.setBackgroundColor(getResources().getColor(R.color.colorLightGreen));
        long currentTime = System.currentTimeMillis();
        if(mLastMatchTime > 0){
            mTotalFaceDetectedTime += (currentTime - mLastMatchTime);
        }
        mLastMatchTime = currentTime;
        mRetryRemained = INITIAL_RETRY_LIMIT;
        mWaitingSeconds = WAIT_SECOND_AFTER_MATCH;
        waitAndRecognizeAgain();
    }

    @Override
    public void onEyeDetected() {
        Log.d(TAG, "beautiful eye seen");
        long currentTime = System.currentTimeMillis();
        if(mEyeDetectedTime > 0){
            mTotalEyeOpenedTime += (currentTime - mEyeDetectedTime);
        }
        mEyeDetectedTime = currentTime;
        mLastEyeFoundTime = currentTime;
        mEyeIndicator.setBackgroundColor(getResources().getColor(R.color.colorLightGreen));
    }

    @Override
    public void onEyeNotDetected() {
        Log.d(TAG, "blindfolded");
        long currentTime = System.currentTimeMillis();
        if(mEyeDetectedTime > 0){
            mTotalEyeOpenedTime += (currentTime - mEyeDetectedTime);
        }
        mEyeDetectedTime = -1;
        if(currentTime - mLastEyeFoundTime > 5000){
            mEyeIndicator.setBackgroundColor(getResources().getColor(R.color.colorLightRed));
        }
    }





    /*
    *   Before restarting the face recognition process, it will be experiencing cooldown. This method ensures that
    * */
    private void waitAndRecognizeAgain(){
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ON_GOING_FACE = false;
                if(mRetryRemained>0){
                    takeSnapAndRecognize();
                }
            }
        }, mWaitingSeconds * SECOND_IN_MILLIES);
    }





    private void connectionError(String msg){
        Toast.makeText(this, "Connection error or server problem. Can't identify face.", Toast.LENGTH_LONG).show();
        Log.d(TAG, msg);
    }





    private int findFrontFacingCamera() {
        int cameraId = -1;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            int numberOfCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraId = i;
                    break;
                }
            }
            return cameraId;
        } else {
            String[] idList;
            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
            try{
                idList = manager.getCameraIdList();
                for(String id: idList){
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                    if(characteristics == null) continue;
                    if(characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                        return 1;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            return cameraId;
        }

    }



}
