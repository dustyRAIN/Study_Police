package com.foh.studypolice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.koushikdutta.ion.cookie.CookieMiddleware;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import ModelClasses.TokenKeyModel;

import static AllConstants.IntentKeys.EXTRA_KEY_EMAIL;
import static AllConstants.RestApiUrlsAndKeys.API_KEY_CSRFTOKEN;
import static AllConstants.RestApiUrlsAndKeys.API_KEY_SESSIONID;
import static AllConstants.RestApiUrlsAndKeys.API_REST_AUTH_LOGIN;
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


/*
* If the user provides a existing email address during signing in/up, he/she will see this activity.
* This activity seeks the password of that email address and tries to sign in.
*
* */
public class LogInWithPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mtvEmailLabel;
    private EditText metvPassword;
    private TextView mtvbSignIn;

    private String passedEmail;

    private SharedPreferences mSharedPreferences;
    private Ion mIon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_with_password);


        mtvEmailLabel = findViewById(R.id.tv_email_label);
        metvPassword = findViewById(R.id.edit_password);
        mtvbSignIn = findViewById(R.id.tvb_proceed);

        getIntentExtras();

        mIon = Ion.getDefault(getApplicationContext());
        mSharedPreferences = this.getSharedPreferences(PREF_NAME_PRIVATE, Context.MODE_PRIVATE);

        mtvbSignIn.setOnClickListener(this);
    }

    private void getIntentExtras(){
        Intent intent = getIntent();
        if(intent.hasExtra(EXTRA_KEY_EMAIL)){
            passedEmail = intent.getStringExtra(EXTRA_KEY_EMAIL);
            mtvEmailLabel.setText(passedEmail);
        } else {
            Toast.makeText(this, "Error, Exiting...", Toast.LENGTH_SHORT).show();
            finish();
        }
    }



    /*
    * After the user inserts the password and hits sign in, this method will be invoved.
    * */
    private void checkAndSignIn(){
        if(metvPassword.getText().toString().equals("") || metvPassword.getText().toString().isEmpty()){
            metvPassword.setError("Can't be empty");
            return;
        }

        final Gson gson = new Gson();

        final String password = metvPassword.getText().toString();
        Ion.with(getApplicationContext())
                .load("POST", API_REST_AUTH_LOGIN)
                .setMultipartParameter("email", passedEmail)
                .setMultipartParameter("password", password)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            unableToLogIn("Error " + e);
                        } else {
                            TokenKeyModel token = gson.fromJson(result, TokenKeyModel.class);
                            if(token != null && token.key != null && !token.key.equals("") && !token.key.isEmpty()){
                                saveToken(token, password);
                                getCookies();
                                finishGood();
                            } else {
                                unableToLogIn("Wrong password.");
                            }

                        }
                    }
                });
    }


    /*
     * If the credentials are correct, the user will be signed in. Database will set the CSRFToken in the cookies.
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
        mSharedPreferences.edit().putString(PREF_CREDENTIAL_EMAIL, passedEmail).apply();
        mSharedPreferences.edit().putString(PREF_CREDENTIAL_PASSWORD, password).apply();
        mSharedPreferences.edit().putBoolean(PREF_HAS_CREDENTIALS, true).apply();
        mSharedPreferences.edit().putBoolean(PREF_HAS_TOKEN, true).apply();
    }

    private void unableToLogIn(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void finishGood(){
        setResult(RESULT_OK);
        finish();
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.tvb_proceed:
                checkAndSignIn();
                break;
        }
    }
}
