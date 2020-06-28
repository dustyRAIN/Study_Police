package com.foh.studypolice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.cookie.CookieMiddleware;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ModelClasses.DataExists;
import ModelClasses.ErrorDetails;
import ModelClasses.TokenKeyModel;

import static AllConstants.IntegerKeys.RC_GET_PASS;
import static AllConstants.IntegerKeys.RC_SIGN_UP;
import static AllConstants.IntentKeys.EXTRA_KEY_EMAIL;
import static AllConstants.RestApiUrlsAndKeys.API_KEY_CSRFTOKEN;
import static AllConstants.RestApiUrlsAndKeys.API_KEY_SESSIONID;
import static AllConstants.RestApiUrlsAndKeys.API_REST_AUTH_LOGIN;
import static AllConstants.RestApiUrlsAndKeys.API_USER_EXISTS;
import static AllConstants.RestApiUrlsAndKeys.API_USER_GOOGLE_LOGIN;
import static AllConstants.RestApiUrlsAndKeys.API_USER_PROVIDER_TAKEN;
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
import static com.foh.studypolice.ClassDataActivity.STORAGE_PERMISSION_READ;
import static com.foh.studypolice.ClassDataActivity.STORAGE_PERMISSION_WRITE;
import static com.foh.studypolice.MainActivity.mCSRFToken;


/*
* If no user is currently logged in, then this activity will be shown.
* The activity will recieve an email from user.
* Also, the user will be able to sign in/up through Google from this activity
* */
public class SignInActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "SignInActivity";
    private final int RC_GOOGLE_SIGN_IN = 6;

    private boolean CAN_WRITE = false;
    private boolean CAN_READ = false;

    private boolean ON_GOING_DOWNLOAD = false;
    private boolean ON_GOING_AUTHENTICATION = false;

    private EditText letvEmail;
    private TextView tvbProceed;
    private SignInButton mSignInGoogle;

    private GoogleSignInOptions mGSO;
    private GoogleSignInClient mGoogleSignInClient;
    private String idToken;
    private Uri photoUri;

    private String givenEmail;

    private SharedPreferences mSharedPreferences;
    private Gson mGson;
    private Ion mIon;

    private String HOME_DIR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        HOME_DIR = new ContextWrapper(this).getFilesDir().getPath();

        letvEmail = findViewById(R.id.edit_email);
        tvbProceed = findViewById(R.id.tvb_proceed);
        mSignInGoogle = findViewById(R.id.butt_google_sign_in);
        mGson = new Gson();

        mSharedPreferences = this.getSharedPreferences(PREF_NAME_PRIVATE, Context.MODE_PRIVATE);

        mIon = Ion.getDefault(getApplicationContext());

        tvbProceed.setOnClickListener(this);
        mSignInGoogle.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_SIGN_UP) {
                setResult(RESULT_OK);
                finish();
            } else if (requestCode == RC_GET_PASS) {
                setResult(RESULT_OK);
                finish();
            } else if(requestCode == RC_GOOGLE_SIGN_IN){
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleGoogleSignInResult(task);
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == RC_SIGN_UP) {
                Log.d("SignIn", "Not Success");
            } else if (requestCode == RC_GET_PASS) {
                Log.d("GetPass", "Not Success");
            } else if (requestCode == RC_GOOGLE_SIGN_IN){

            }
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
                } else {
                    CAN_WRITE = false;
                    showErrorQuit("Storage permission denied.");
                }
                return;
            }

            case STORAGE_PERMISSION_READ: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CAN_READ = true;
                } else {
                    CAN_READ = false;
                    showErrorQuit("Storage permission denied.");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }



















    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }



    /*
    * If the provided email already exists in database, then the app should only seek for the password.
    * Otherwise, should let the user sign up.
    *
    * This method receives the information about the existence of the email address from database.
    * */
    private void getUserExistenceInfo(){
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        try {
            String api_url = API_USER_EXISTS + givenEmail;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, api_url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            DataExists dataExists = null;
                            try {
                                Gson gson = new Gson();
                                dataExists = gson.fromJson(response.getJSONObject("data").toString(), DataExists.class);
                            } catch (JSONException e){
                                e.printStackTrace();
                            }
                            if(dataExists != null)
                                checkUserExistsAndProceed(givenEmail, dataExists);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //TODO: Show Failed To Connect
                            Toast.makeText(SignInActivity.this, "connection error", Toast.LENGTH_SHORT).show();
                        }
                    });

            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
    * A user can sign up in two methods-
    *           i. vanilla method means giving self info using @activity SignUpActivity
    *           ii. Using a provider- Google
    *
    * If a user signs up by a provider (Google), then the user do not need to provide any password.
    * So, later on the user will have to sign in through only by the provider.
    *
    * This method checks if the given email was registered through provider.
    * If so, the user should be notified to sign in through the provider method.
    * */
    private void takenByProviderOrNot(final String email){
        Ion.with(getApplicationContext())
                .load("GET", API_USER_PROVIDER_TAKEN)
                .setMultipartParameter("email", email)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null){
                            Log.d(TAG, e.toString());
                            Toast.makeText(SignInActivity.this, "Server Error or Connection Problem.", Toast.LENGTH_LONG).show();
                        } else {
                            DataExists dataExists = mGson.fromJson(result, DataExists.class);
                            if(dataExists != null){
                                if(dataExists.exists == 1){
                                    loginWithProvider(email);
                                } else if(dataExists.exists == 2){
                                    loginWithPassWord(email);
                                }
                            }
                        }
                    }
                });
    }

    private void loginWithProvider(String email){
        Toast.makeText(this, "Must login with Google with this email.", Toast.LENGTH_LONG).show();
    }

    private void loginWithPassWord(String email){
        Intent intent = new Intent(this, LogInWithPasswordActivity.class);
        intent.putExtra(EXTRA_KEY_EMAIL, email);
        startActivityForResult(intent, RC_GET_PASS);
    }



    private void checkUserExistsAndProceed(String email, DataExists dataExists){
        //TODO: Stop Progressbar

        if(dataExists.exists == 1){
            takenByProviderOrNot(email);
        } else if(dataExists.exists == 0){
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.putExtra(EXTRA_KEY_EMAIL, email);
            startActivityForResult(intent, RC_SIGN_UP);
        }
    }






    /*
    * checkAndProceed()
    *
    * User should enter a valid non empty email address to proceed to
    * sign in / sign up
    *
    * the valid email address will be used to check if an account is opened
    * with that email address
    *
    */
    private void checkAndProceed(){
        if(letvEmail.getText().toString().equals("") || letvEmail.getText().toString().isEmpty()){
            letvEmail.setError("Can't be empty.");
        } else if(!isEmailValid(letvEmail.getText().toString())){
            letvEmail.setError("Email address is not valid.");
        } else {
            givenEmail = letvEmail.getText().toString().trim();
            getUserExistenceInfo();
        }
    }






    /*
    * When a user tries to sign in through provider (Google), all the public information of the user will be provided by
    * Google. Google photo url will also be provided.
    * The photo should be downloaded and stored in the phone storage before sending it the database.
    *
    * This method checks and asks (if necessary) for permission to access the phone storage.
    * */
    private void checkForPermission(){

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
        }

    }



    /*
     * When a user tries to sign in through provider (Google), all the public information of the user will be provided by
     * Google. Google photo url will also be provided.
     * The photo should be downloaded and stored in the phone storage before sending it the database.
     *
     * This method downloads the photo and stores in the phone storage.
     * */
    private void getPhotoAndProceed(boolean shouldDownload){

        if(shouldDownload){
            File file = new File(HOME_DIR + "/" + "temp", "google_photo.jpg");
            if(file.exists()){
                if(file.delete()){
                    Log.d("file", "deleted");
                } else {
                    Log.d("file", "Not deleted");
                }
            }

            PRDownloader.download(
                    photoUri.toString(),
                    HOME_DIR + "/" + "temp",
                    "google_photo.jpg"
            ).setHeader("X-CSRFToken", mCSRFToken).build().start(new OnDownloadListener() {
                @Override
                public void onDownloadComplete() {
                    File downloadedFile = new File(HOME_DIR + "/" + "temp",
                            "google_photo.jpg");
                    sendIdTokenAndAuthenticate(idToken, downloadedFile);
                    ON_GOING_DOWNLOAD = false;
                }
                @Override
                public void onError(Error error) {
                    ON_GOING_DOWNLOAD = false;
                    showError("Check connection. Try again.");
                }
            });
        } else {
            showErrorQuit("Storage permission denied.");
        }
    }











    /*
    * Sign in through Google will be executed in this method.
    * */
    private void startSigningWithGoogle(){
        mGSO = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, mGSO);

        mGoogleSignInClient.signOut();

        Intent googleIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(googleIntent, RC_GOOGLE_SIGN_IN);
    }


    private void handleGoogleSignInResult(@NonNull Task<GoogleSignInAccount> task){
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            idToken = account.getIdToken();
            photoUri = account.getPhotoUrl();

            if(photoUri != null){
                if(CAN_READ && CAN_WRITE){
                    getPhotoAndProceed(true);
                } else {
                    getPhotoAndProceed(false);
                }
            } else {
                getPhotoAndProceed(false);
            }



        } catch (ApiException e) {
            Log.w(TAG, "handleSignInResult:error", e);
            showErrorQuit("Couldn't sign in with Google.");
        }
    }


    /*
    * Google will provide an idtoken that will be verified in the database.
    * If it gets verified, database will accept the user and log hi/her in.
    *
    * @variable ON_GOING_AUTHENTICATION ensures that only one instance of this method is running at any time.
    * */
    private void sendIdTokenAndAuthenticate(String idToken, File file){
        if(ON_GOING_AUTHENTICATION) return;
        ON_GOING_AUTHENTICATION = true;

        Log.d(TAG, idToken);

        Ion.with(getApplicationContext())
                .load("POST", API_USER_GOOGLE_LOGIN)
                .setMultipartParameter("token", idToken)
                .setMultipartFile("image", "multipart/form-data", file)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            Log.d(TAG, "Error " + e);
                            showError("Couldn't Login with Google. Check connection.");
                            ON_GOING_AUTHENTICATION = false;
                        } else {
                            ErrorDetails errorDetails = mGson.fromJson(result, ErrorDetails.class);
                            if(errorDetails != null && errorDetails.error != null && !errorDetails.error.equals("")
                                    && !errorDetails.error.isEmpty()) {
                                showError(errorDetails.error);
                                ON_GOING_AUTHENTICATION = false;
                            } else {
                                TokenKeyModel token = mGson.fromJson(result, TokenKeyModel.class);
                                if(token != null && token.key != null && !token.key.equals("") && !token.key.isEmpty()){
                                    saveToken(token, "");
                                    getCookies();
                                    finishGood();
                                    ON_GOING_AUTHENTICATION = false;
                                } else {
                                    showError("Couldn't Login with Google.");
                                    ON_GOING_AUTHENTICATION = false;
                                }
                            }

                        }
                    }
                });
    }


    /*
     * If the idtoken provided by Google is verified, the user will be signed in. Database will set the CSRFToken in the cookies.
     * This method extracts the CSRFToken from the cookies and svaes in shared preference
     * */
    private void getCookies(){
        CookieMiddleware cookieMiddleware =  mIon.getCookieMiddleware();
        CookieManager manager = cookieMiddleware.getCookieManager();
        List<HttpCookie> cookies = manager.getCookieStore().get(URI.create(API_REST_AUTH_LOGIN));

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
        mSharedPreferences.edit().putString(PREF_CREDENTIAL_EMAIL, "").apply();
        mSharedPreferences.edit().putString(PREF_CREDENTIAL_PASSWORD, password).apply();
        mSharedPreferences.edit().putBoolean(PREF_HAS_CREDENTIALS, true).apply();
        mSharedPreferences.edit().putBoolean(PREF_HAS_TOKEN, true).apply();
    }





    private void finishGood(){
        setResult(RESULT_OK);
        finish();
    }

    private void showErrorQuit(String mesg){
        Toast.makeText(this, mesg, Toast.LENGTH_LONG).show();
        finish();
    }

    private void showError(String mesg){
        Toast.makeText(this, mesg, Toast.LENGTH_LONG).show();
    }














    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.tvb_proceed:
                //TODO: Start Progressbar
                checkAndProceed();
                break;

            case R.id.butt_google_sign_in:
                startSigningWithGoogle();
                break;
        }
    }
}