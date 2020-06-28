package com.foh.studypolice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.FileOutputStream;

import ModelClasses.ErrorDetails;
import Utils.CameraPreview;
import Utils.CameraSourcePreview;
import Utils.GraphicOverlay;

import static AllConstants.RestApiUrlsAndKeys.API_FACE_DELETE;
import static AllConstants.RestApiUrlsAndKeys.API_FACE_POST;
import static AllConstants.RestApiUrlsAndKeys.API_MEDIA_FACES;
import static com.foh.studypolice.ClassDataActivity.STORAGE_PERMISSION_READ;
import static com.foh.studypolice.ClassDataActivity.STORAGE_PERMISSION_WRITE;
import static com.foh.studypolice.MainActivity.mCSRFToken;
import static com.foh.studypolice.MainActivity.mUser;
import static com.foh.studypolice.MaterialViewActivity.PERMISSION_CAMERA;


/*
* This activity is called if the current user has not provided his/her facial information to the sever
* This activity will snap a photo of the user and upload it to the database after the confirmation from the user
* */
public class PostFaceActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "postface";

    private boolean ON_GOING = false;

    private Gson mGson;

    private CameraSource mCamera;
    private CameraSourcePreview mCameraSourcePreview;
    private CameraPreview mCameraPreview;
    private GraphicOverlay mGraphicOverlay;

    private FrameLayout mDisplaySurface;

    private boolean CAN_WRITE = false;
    private boolean CAN_READ = false;
    private boolean CAN_SNAP = false;

    private Dialog mMessageDialog;
    private TextView mtvbDontTakePhoto;
    private TextView mtvbTakePhoto;
    private TextView mtvbSnapPhoto;

    private Dialog mFacePreviewDialog;
    private ConstraintLayout mLowerPart;
    private ImageView mivFacePreview;
    private TextView mtvPleaseWait;
    private TextView mtvbYesMe;
    private TextView mtvbSnapAgain;
    private TextView mtvPreviewTitle;
    private boolean shouldWait;

    private String tFilename;
    private File tPictureFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_face);

        setMessageDialog();
        setFacePreviewDialog();

        mDisplaySurface = findViewById(R.id.camera_view);
        mtvbSnapPhoto = findViewById(R.id.take_picture);

        mGson = new Gson();

        mtvbSnapPhoto.setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkAllPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCamera != null){
            try{
                mCamera.stop();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCamera != null){
            mCamera.release();
        }
    }

    private void showImportantMsg(){
        if(!CAN_READ || !CAN_WRITE || !CAN_SNAP) return;
        mMessageDialog.show();
    }

    /*
    * The dialog reminds the user about the purpose of the facial information and asks permission before proceeding
    * */
    private void setMessageDialog(){
        mMessageDialog = new Dialog(this);
        mMessageDialog.setContentView(R.layout.dialog_imprtant_message);
        mtvbDontTakePhoto = mMessageDialog.findViewById(R.id.dia_no_taking_photo);
        mtvbTakePhoto = mMessageDialog.findViewById(R.id.dia_take_photo);
        mtvbDontTakePhoto.setOnClickListener(this);
        mtvbTakePhoto.setOnClickListener(this);
    }


    /*
    * After the user snaps a photo of his/her face, this dialog will show how the photo looks like.
    * The user can confirm the photo or snap again.
    * If the user decides to snap again, then the current photo will be discarded.
    */
    private void setFacePreviewDialog(){
        mFacePreviewDialog = new Dialog(this);
        mFacePreviewDialog.setContentView(R.layout.dialog_face_preview);
        mLowerPart = mFacePreviewDialog.findViewById(R.id.confirm_lower_part);
        mivFacePreview = mFacePreviewDialog.findViewById(R.id.img_face_prev);
        mtvPreviewTitle = mFacePreviewDialog.findViewById(R.id.prev_title);
        mtvPleaseWait = mFacePreviewDialog.findViewById(R.id.tv_msg_wait);
        mtvbYesMe = mFacePreviewDialog.findViewById(R.id.dia_its_me);
        mtvbSnapAgain = mFacePreviewDialog.findViewById(R.id.dia_not_me);
        mtvbYesMe.setOnClickListener(this);
        mtvbSnapAgain.setOnClickListener(this);
        shouldWait = false;
    }


    /*
    * The user can snap a photo using this app.
    * This method sets up the camera.
    * */
    private void startCamera(){
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            errorAndQuit("No camera on this device");
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId < 0) {
                errorAndQuit("No front facing camera.");
            } else {

                FaceDetector detector = new FaceDetector.Builder(this)
                        .setTrackingEnabled(false)
                        .build();

                mCamera = new CameraSource.Builder(this, detector)
                        .setRequestedPreviewSize(1024, 768)
                        .setFacing(CameraSource.CAMERA_FACING_FRONT)
                        .setRequestedFps(30.0f)
                        .build();

                mCameraPreview = new CameraPreview(this, mCamera);
                /*mCameraSourcePreview = new CameraSourcePreview(this, null);
                mGraphicOverlay = new GraphicOverlay(this, null);
                try{
                    mCameraSourcePreview.start(mCamera, mGraphicOverlay);
                } catch (Exception e){
                    e.printStackTrace();
                }*/
                mDisplaySurface.addView(mCameraPreview);
            }
        }
    }


    /*
    * @variable ON_GOING ensures that only one instance of this method is running at any time
    * The picture will be saved on the storage of the phone.
    * */
    private void takePhotoAndProceed(){
        if(ON_GOING) return;
        ON_GOING = true;

        String homeDir = new ContextWrapper(getApplicationContext()).getFilesDir().getPath();
        File pictureFileDir =  new File(homeDir, "tempPhoto");

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()){
            Log.d(TAG, "Can't create directory to save image.");
            Toast.makeText(this, "Can't create directory to save image.", Toast.LENGTH_LONG).show();
            ON_GOING = false;
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
                    postFace(tPictureFile);
                } catch (Exception error) {
                    Log.d(TAG, "File" + tFilename + "not saved: " + error.getMessage());
                }
            }
        });
    }


    /*
    * When the user snaps the photo it will be posted to the database.
    * Database will return a result based on if the photo has a clear facial data or not, and also if the face already exists or not
    * */
    private void postFace(File pictureFile){
        Ion.with(getApplicationContext())
                .load(API_FACE_POST)
                .setHeader("X-CSRFToken", mCSRFToken)
                .setMultipartParameter("user", String.valueOf(mUser.id))
                .setMultipartFile("test_image", "multipart/form-data", pictureFile)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null){
                            showToast("Connection problem. Please try again.");
                        } else {
                            ErrorDetails errorDetails = mGson.fromJson(result, ErrorDetails.class);
                            if(errorDetails == null){
                                getPostedFace();
                            } else {
                                if(errorDetails.error != null){
                                    showToast(errorDetails.error);
                                    ON_GOING = false;
                                } else {
                                    getPostedFace();
                                }
                            }
                        }
                    }
                });
    }


    /*
    * If the taken photo is acceptable in database, then a dialog should be shown to the user.
    * The dialog will contain a cropped photo only having the face of the user.
    * And the dialog will ask to the user to confirm it.
    *
    * This method glides the cropped photo from the database to the dialog.
    * */
    private void getPostedFace(){
        showPostedFacePreview();
        Ion.with(getApplicationContext())
                .load(API_MEDIA_FACES + mUser.id + ".jpg")
                .setHeader("X-CSRFToken", mCSRFToken)
                .withBitmap()
                .intoImageView(mivFacePreview);
    }

    /*
     * If the taken photo is acceptable in database, then a dialog should be shown to the user.
     * The dialog will contain a cropped photo only having the face of the user.
     * And the dialog will ask to the user to confirm it.
     *
     * This method shows the dialog
     * @variable shouldWait if true indicates that the current photo is being deleted
     * @variable shouldWait if false indicates that the dialog can show the photo
     * */
    private void showPostedFacePreview(){
        if(shouldWait){
            mLowerPart.setVisibility(View.GONE);
            mivFacePreview.setVisibility(View.GONE);
            mtvPreviewTitle.setVisibility(View.GONE);
            mtvPleaseWait.setVisibility(View.VISIBLE);
        } else {
            mLowerPart.setVisibility(View.VISIBLE);
            mivFacePreview.setVisibility(View.VISIBLE);
            mtvPreviewTitle.setVisibility(View.VISIBLE);
            mtvPleaseWait.setVisibility(View.GONE);
        }
        mFacePreviewDialog.show();
    }

    private void faceConfirmed(){
        mFacePreviewDialog.dismiss();
        setResult(RESULT_OK);
        finish();
    }


    /*
    * If user decides to snap again, then the previously snapped photo should be deleted from database.
    *
    * @variable shouldWait and @method showPostedFacePreview() shows a dialog indicating the user to
    * wait before the deletion is completed. After having deleted, the dialog is dismissed.
    * */
    private void deleteFaceAndSnapAgain(){
        shouldWait = true;
        showPostedFacePreview();
        Ion.with(getApplicationContext())
                .load("GET", API_FACE_DELETE)
                .setHeader("X-CSRFToken", mCSRFToken)
                .setMultipartParameter("user", String.valueOf(mUser.id))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null){
                            showToast("Server error. Trying again...");
                            showPostedFacePreview();
                        } else {
                            shouldWait = false;
                            ON_GOING = false;
                            mFacePreviewDialog.dismiss();
                        }
                    }
                });
    }














    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void errorAndQuit(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CAN_WRITE = true;
                    showImportantMsg();
                } else {
                    CAN_WRITE = false;
                    errorAndQuit("Storage permission denied. Can't save image. Quiting...");
                }
                return;
            }

            case STORAGE_PERMISSION_READ: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CAN_READ = true;
                    showImportantMsg();
                } else {
                    CAN_READ = false;
                    errorAndQuit("Storage permission denied. Can't save image. Quiting...");
                }
                return;
            }

            case PERMISSION_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CAN_SNAP = true;
                    showImportantMsg();
                } else {
                    CAN_SNAP = false;
                    errorAndQuit("Storage permission denied. Can't save image. Quiting...");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    /*
    * Taking picture requires camera access permission
    * Saving the temporary photo in the phone storage needs storage permission
    *
    * This method checks for all these permissions, and asks to user if any is not given.
    * */
    private void checkAllPermission(){
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
            showImportantMsg();
        }


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
            showImportantMsg();
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
            showImportantMsg();
        }
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


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.dia_no_taking_photo:
                mMessageDialog.dismiss();
                errorAndQuit("Can't Take Photo. Quiting...");
                break;

            case R.id.dia_take_photo:
                mMessageDialog.dismiss();
                startCamera();
                break;

            case R.id.take_picture:
                takePhotoAndProceed();
                break;

            case R.id.dia_its_me:
                faceConfirmed();
                break;

            case R.id.dia_not_me:
                deleteFaceAndSnapAgain();
                break;
        }
    }
}
