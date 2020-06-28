package com.foh.studypolice;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.cookie.CookieMiddleware;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;


import ModelClasses.TokenKeyModel;


import static AllConstants.IntentKeys.EXTRA_KEY_EMAIL;
import static AllConstants.RestApiUrlsAndKeys.API_KEY_CSRFTOKEN;
import static AllConstants.RestApiUrlsAndKeys.API_KEY_SESSIONID;
import static AllConstants.RestApiUrlsAndKeys.API_USER_REGISTRATION;
import static AllConstants.SharedPreferenceKeys.PREF_CREDENTIAL_EMAIL;
import static AllConstants.SharedPreferenceKeys.PREF_CREDENTIAL_PASSWORD;
import static AllConstants.SharedPreferenceKeys.PREF_CSRF_TOKEN;
import static AllConstants.SharedPreferenceKeys.PREF_HAS_CREDENTIALS;
import static AllConstants.SharedPreferenceKeys.PREF_HAS_CSRF_TOKEN;
import static AllConstants.SharedPreferenceKeys.PREF_HAS_SESSION_ID;
import static AllConstants.SharedPreferenceKeys.PREF_HAS_TOKEN;
import static AllConstants.SharedPreferenceKeys.PREF_NAME_PRIVATE;
import static AllConstants.SharedPreferenceKeys.PREF_SESSION_ID;
import static AllConstants.SharedPreferenceKeys.PREF_TOKEN_KEY;
import static Utils.FileUtils.getPath;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mivProfileImage;
    private EditText metvName;
    private TextView mtvEmail;
    private EditText metvPassword;
    private EditText metvRePassword;
    private TextView mtvbSignUp;
    private RadioGroup mrgGender;

    private SharedPreferences mSharedPreferences;
    private Ion mIon;

    private Uri mSelectedImageUri;

    private Bitmap mBitmap;

    private String passedEmail;

    protected static final int RC_BROWSE_IMAGE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        addToolBar();
        getIntentExtras();

        mSharedPreferences = this.getSharedPreferences(PREF_NAME_PRIVATE, Context.MODE_PRIVATE);

        mivProfileImage = findViewById(R.id.iv_profile);
        metvName = findViewById(R.id.etv_name);
        mtvEmail = findViewById(R.id.tv_email);
        metvPassword = findViewById(R.id.etv_pass);
        metvRePassword = findViewById(R.id.etv_repass);
        mtvbSignUp = findViewById(R.id.tvb_sign_up);
        mrgGender = findViewById(R.id.rg_gender);

        mtvEmail.setText(passedEmail);

        mIon = Ion.getDefault(getApplicationContext());

        mivProfileImage.setOnClickListener(this);
        mtvbSignUp.setOnClickListener(this);
    }

    private void addToolBar(){
        Toolbar toolbar = findViewById(R.id.sp_main_toolbar);

        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
    }

    private void getIntentExtras(){
        Intent intent = getIntent();
        if(intent.hasExtra(EXTRA_KEY_EMAIL)){
            passedEmail = intent.getStringExtra(EXTRA_KEY_EMAIL);
        } else {
            Toast.makeText(this, "Error, Exiting...", Toast.LENGTH_SHORT).show();
            finish();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_BROWSE_IMAGE) {
                if(data != null){
                    Uri uri = data.getData();
                    cropImage(uri);
                }
            } else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                if (data != null) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    mSelectedImageUri = result.getUri();
                    setCircularImage(mSelectedImageUri);
                }
            }
        } else if (requestCode == RESULT_CANCELED) {

        }
    }


    private void cropImage(Uri uri){
        CropImage.activity(uri).setAspectRatio(1, 1).start(this);
    }

    private void setCircularImage(Uri uri){
        try {
            mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), mBitmap);
            roundedBitmapDrawable.setCircular(true);

            mivProfileImage.setImageDrawable(roundedBitmapDrawable);
        } catch (Exception e){
            e.printStackTrace();
        }
    }




    /*
    * After the user has given all the necessary information about self, this method will be invoked.
    *
    * This method will send those data to the database, and database will create a new user and return the token.
    * */
    private void proceedToSignUp(){
        String name = metvName.getText().toString().trim();
        String gender;
        if(mrgGender.getCheckedRadioButtonId() == R.id.rad_male){
            gender = "1";
        } else {
            gender = "0";
        }
        final String password1 = metvPassword.getText().toString().trim();
        String password2 = metvRePassword.getText().toString().trim();

        File file = new File(getPath(this, mSelectedImageUri));
        final Gson gson = new Gson();



        Ion.with(getApplicationContext())
                .load("POST", API_USER_REGISTRATION)
                .setMultipartParameter("email", passedEmail)
                .setMultipartParameter("name", name)
                .setMultipartParameter("gender", gender)
                .setMultipartParameter("password1", password1)
                .setMultipartParameter("password2", password2)
                .setMultipartFile("image","multipart/form-data", file)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            Toast.makeText(SignUpActivity.this, "Error " + e, Toast.LENGTH_LONG).show();
                            unableToCreate();
                        } else {
                            TokenKeyModel token = gson.fromJson(result, TokenKeyModel.class);
                            if(token != null && token.key != null && !token.key.equals("") && !token.key.isEmpty()){
                                saveToken(token, password1);
                                getCookies();
                                finishGood();
                            } else {
                                unableToLogIn("Error occurred. Please try again.");
                            }
                        }
                    }
                });
    }

    private void unableToLogIn(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void finishGood(){
        setResult(RESULT_OK);
        finish();
    }



    /*
     * After the user gets signed up, database will set the CSRFToken in the cookies.
     * This method extracts the CSRFToken from the cookies and svaes in shared preference
     * */
    private void getCookies(){
        CookieMiddleware cookieMiddleware =  mIon.getCookieMiddleware();
        CookieManager manager = cookieMiddleware.getCookieManager();
        List<HttpCookie> cookies = manager.getCookieStore().get(URI.create(API_USER_REGISTRATION));

        boolean gotToken = false;
        boolean gotSessionid = false;

        String csrfToken = null;
        String sessionId = null;

        for (HttpCookie cookie : cookies) {
            Log.i("CookieTest", cookie.getName() + ": " + cookie.getValue());
            if(cookie.getName().equals(API_KEY_CSRFTOKEN)) {
                gotToken = true;
                csrfToken = cookie.getValue();
            } else if(cookie.getName().equals(API_KEY_SESSIONID)) {
                gotSessionid = true;
                sessionId = cookie.getValue();
            }
        }

        mSharedPreferences.edit().putBoolean(PREF_HAS_CSRF_TOKEN, gotToken).apply();
        mSharedPreferences.edit().putBoolean(PREF_HAS_SESSION_ID, gotSessionid).apply();

        if(gotToken){
            mSharedPreferences.edit().putString(PREF_CSRF_TOKEN, csrfToken).apply();
        }

        if(gotSessionid){
            mSharedPreferences.edit().putString(PREF_SESSION_ID, sessionId).apply();
        }
    }


    private void saveToken(TokenKeyModel token, String password){
        mSharedPreferences.edit().putString(PREF_TOKEN_KEY, token.key).apply();
        mSharedPreferences.edit().putString(PREF_CREDENTIAL_EMAIL, passedEmail).apply();
        mSharedPreferences.edit().putString(PREF_CREDENTIAL_PASSWORD, password).apply();
        mSharedPreferences.edit().putBoolean(PREF_HAS_CREDENTIALS, true).apply();
        mSharedPreferences.edit().putBoolean(PREF_HAS_TOKEN, true).apply();
    }

    private void unableToCreate(){
        Toast.makeText(this, "Unable to create account.", Toast.LENGTH_LONG).show();
        setResult(RESULT_CANCELED);
        finish();
    }


    private void checkAndStartProcess(){
        boolean noError = true;

        if(mSelectedImageUri == null){
            Toast.makeText(this, "Please upload a picture.", Toast.LENGTH_SHORT).show();
            noError = false;
        }

        if(metvName.getText().toString().trim().equals("") || metvName.getText().toString().trim().isEmpty()){
            metvName.setError("Can't be empty.");
            noError = false;
        }

        if(metvName.getText().toString().trim().length() > 50){
            metvName.setError("Name is too long.");
            noError = false;
        }

        if(mrgGender.getCheckedRadioButtonId() == -1){
            Toast.makeText(this, "Please select a gender.", Toast.LENGTH_SHORT).show();
            noError = false;
        }

        if(metvPassword.getText().toString().trim().equals("") || metvPassword.getText().toString().trim().isEmpty()){
            metvPassword.setError("Provide a password.");
            noError = false;
        }

        if(metvRePassword.getText().toString().trim().equals("") || metvRePassword.getText().toString().trim().isEmpty()){
            metvRePassword.setError("Retype your password.");
            noError = false;
        }

        if(metvPassword.getText().toString().length() < 8 || metvPassword.getText().toString().length() > 30){
            metvPassword.setError("length must be between 8 to 30 characters.");
            return;
        }

        String pass = metvPassword.getText().toString();

        for(int i=0; i<pass.length(); i++){
            char c = pass.charAt(i);
            if(c>='A' && c<='Z') continue;
            if(c>='a' && c<='z') continue;
            if(c>='0' && c<='9') continue;
            metvPassword.setError("Characters allowed a-z, A-Z, 0-9");
            return;
        }

        if(!metvPassword.getText().toString().equals(metvRePassword.getText().toString())){
            metvPassword.setError("Password didn't match.");
            metvRePassword.setError("Password didn't match.");
            noError = false;
        }



        if(noError){
            proceedToSignUp();
        }

    }






    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.iv_profile:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Select an Image"), RC_BROWSE_IMAGE);
                break;

            case R.id.tvb_sign_up:
                checkAndStartProcess();
                break;
        }
    }
}
