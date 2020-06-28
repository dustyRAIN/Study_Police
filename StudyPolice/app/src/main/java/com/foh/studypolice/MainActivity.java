package com.foh.studypolice;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Adapters.CreatedClassAdapter;
import Adapters.FrequencyViewAdapter;
import Adapters.JoinedClassAdapter;
import Adapters.ShoutViewAdapter;
import ModelClasses.ClassDetails;
import ModelClasses.ContentFrequencyDetails;
import ModelClasses.DataExists;
import ModelClasses.GeneralShoutDetails;
import ModelClasses.JoinedClassDetails;
import ModelClasses.TokenKeyModel;
import ModelClasses.User;

import static AllConstants.IntegerKeys.MODE_VIEW_AND_FACE;
import static AllConstants.IntegerKeys.RC_POST_FACE;
import static AllConstants.IntegerKeys.RC_SIGN_IN;
import static AllConstants.IntegerKeys.TYPE_CREATED_CLASS;
import static AllConstants.IntegerKeys.TYPE_JOINED_CLASS;
import static AllConstants.IntegerKeys.TYPE_MATERIAL;
import static AllConstants.IntentKeys.EXTRA_KEY_CLASS_ID;
import static AllConstants.IntentKeys.EXTRA_KEY_MATERIAL_ID;
import static AllConstants.IntentKeys.EXTRA_KEY_MATERIAL_MODE;
import static AllConstants.RestApiUrlsAndKeys.API_CLASSES_CREATE;
import static AllConstants.RestApiUrlsAndKeys.API_CLASSES_CREATED;
import static AllConstants.RestApiUrlsAndKeys.API_CLASSES_JOIN;
import static AllConstants.RestApiUrlsAndKeys.API_CLASSES_JOINED;
import static AllConstants.RestApiUrlsAndKeys.API_FACE_EXISTS;
import static AllConstants.RestApiUrlsAndKeys.API_REST_AUTH_LOGIN;
import static AllConstants.RestApiUrlsAndKeys.API_REST_AUTH_LOGOUT;
import static AllConstants.RestApiUrlsAndKeys.API_REST_AUTH_USER;
import static AllConstants.RestApiUrlsAndKeys.API_SHOUTS_ALL;
import static AllConstants.RestApiUrlsAndKeys.API_SHOUTS_SEEN;
import static AllConstants.RestApiUrlsAndKeys.API_STATS_FREQUENCY_GET;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        CreatedClassAdapter.OnClassClickHandler, JoinedClassAdapter.OnClassClickHandler, ShoutViewAdapter.OnShoutClickHandler,
        FrequencyViewAdapter.OnClickHandler {

    private FloatingActionButton fabCreateJoinClass;

    private RecyclerView mCreatedClassRecyclerView;
    private CreatedClassAdapter mCreatedClassAdapter;

    private RecyclerView mJoinedClassRecyclerView;
    private JoinedClassAdapter mJoinedClassAdapter;

    private RecyclerView mShoutoutRecyclerView;
    private ShoutViewAdapter mShoutViewAdapter;

    private RecyclerView mFrequencyRecyclerView;
    private FrequencyViewAdapter mFrequencyAdapter;

    private Dialog mDialog;
    private TextView tvDiaJoinClass;
    private TextView tvbDiaJoinClass;
    private TextView tvDiaCreateClass;
    private TextView tvbDiaCreateClass;
    private TextView mtvUserName;
    private ImageView mivUserPic;
    private EditText etvAccessCode;
    private EditText etvClassName;
    private TextView mtvbLogOut;
    private ConstraintLayout diaCreateJoinCont;
    private ConstraintLayout diaJoinCont;
    private ConstraintLayout diaCreateCont;

    protected static User mUser;
    protected static String mToken;
    protected static String mUserEmail;
    protected static String mCSRFToken;
    protected static String mSessionId;

    private Gson mGson;

    private SharedPreferences mSharedPreferences;


    private List<ClassDetails> mClassDetailsList;
    private List<JoinedClassDetails> mJoinedClassList;
    private List<GeneralShoutDetails> mShoutoutList;
    private List<ContentFrequencyDetails> mFrequencyList;

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout mDrawer;

    private boolean mIsCreatedOpen = false;
    private boolean mIsJoinedOpen = false;

    private ConstraintLayout mCreatedCont;
    private ConstraintLayout mJoinedCont;
    private TextView mtvCreatedClass;
    private TextView mtvJoinedClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addToolBar();
        setDialog();

        mDrawer = findViewById(R.id.drawer_layout);
        mAppBarConfiguration = new AppBarConfiguration.Builder()
                .setDrawerLayout(mDrawer)
                .build();

        mSharedPreferences = this.getSharedPreferences(PREF_NAME_PRIVATE, Context.MODE_PRIVATE);

        mCreatedCont = findViewById(R.id.created_classs_cont);
        mJoinedCont = findViewById(R.id.joined_classs_cont);
        mtvCreatedClass = findViewById(R.id.tv_opened_class);
        mtvJoinedClass = findViewById(R.id.tv_joined_class);

        mtvUserName = findViewById(R.id.tv_side_name);
        mivUserPic = findViewById(R.id.iv_side_profile);

        fabCreateJoinClass = findViewById(R.id.fab_create_join_class);
        mtvbLogOut = findViewById(R.id.tvb_log_out);

        setUpRecyclerViews();
        mGson = new Gson();

        setUpContainers();

        mtvCreatedClass.setOnClickListener(this);
        mtvJoinedClass.setOnClickListener(this);
        fabCreateJoinClass.setOnClickListener(this);
        mtvbLogOut.setOnClickListener(this);
    }

    private void addToolBar(){
        Toolbar toolbar = findViewById(R.id.sp_main_toolbar);

        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
    }

    private void setUpRecyclerViews(){
        mCreatedClassRecyclerView = findViewById(R.id.rv_opened_class);
        LinearLayoutManager cratedClassLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        mCreatedClassRecyclerView.setLayoutManager(cratedClassLayoutManager);
        mCreatedClassRecyclerView.setHasFixedSize(true);


        mJoinedClassRecyclerView = findViewById(R.id.rv_joined_class);
        LinearLayoutManager joinedClassLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        mJoinedClassRecyclerView.setLayoutManager(joinedClassLayoutManager);
        mJoinedClassRecyclerView.setHasFixedSize(true);


        mShoutoutRecyclerView = findViewById(R.id.rv_noti);
        LinearLayoutManager shoutLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mShoutoutRecyclerView.setLayoutManager(shoutLayoutManager);
        mShoutoutRecyclerView.setHasFixedSize(true);

        mFrequencyRecyclerView = findViewById(R.id.rv_fac);
        LinearLayoutManager freqLayoutManger = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mFrequencyRecyclerView.setLayoutManager(freqLayoutManger);
        mFrequencyRecyclerView.setHasFixedSize(true);
    }


    @Override
    protected void onResume() {
        super.onResume();


        if(mClassDetailsList == null) mClassDetailsList = new ArrayList<>();
        mCreatedClassAdapter = new CreatedClassAdapter(this, mClassDetailsList, this);
        mCreatedClassRecyclerView.setAdapter(mCreatedClassAdapter);


        if(mJoinedClassList == null) mJoinedClassList = new ArrayList<>();
        mJoinedClassAdapter = new JoinedClassAdapter(this, mJoinedClassList, this);
        mJoinedClassRecyclerView.setAdapter(mJoinedClassAdapter);

        if(mShoutoutList == null){
            mShoutoutList = new ArrayList<>();
        }
        mShoutViewAdapter = new ShoutViewAdapter(this, mShoutoutList, this);
        mShoutoutRecyclerView.setAdapter(mShoutViewAdapter);

        if(mFrequencyList == null){
            mFrequencyList = new ArrayList<>();
        }
        mFrequencyAdapter = new FrequencyViewAdapter(this, mFrequencyList, this);
        mFrequencyRecyclerView.setAdapter(mFrequencyAdapter);


        if(mSharedPreferences.contains(PREF_HAS_TOKEN) &&
                mSharedPreferences.contains(PREF_HAS_CREDENTIALS) &&
                mSharedPreferences.contains(PREF_HAS_CSRF_TOKEN) &&
                mSharedPreferences.contains(PREF_HAS_SESSION_ID)){

            if(mSharedPreferences.getBoolean(PREF_HAS_TOKEN, false) &&
                    mSharedPreferences.getBoolean(PREF_HAS_CREDENTIALS, false) &&
                    mSharedPreferences.getBoolean(PREF_HAS_CSRF_TOKEN, false) &&
                    mSharedPreferences.getBoolean(PREF_HAS_SESSION_ID, false)){

                mToken = mSharedPreferences.getString(PREF_TOKEN_KEY, "");
                mUserEmail = mSharedPreferences.getString(PREF_CREDENTIAL_EMAIL, "");
                mCSRFToken = mSharedPreferences.getString(PREF_CSRF_TOKEN, "");
                mSessionId = mSharedPreferences.getString(PREF_SESSION_ID, "");

                userAuthenticated();

            } else if(!mSharedPreferences.getBoolean(PREF_HAS_TOKEN, false) &&
                    mSharedPreferences.getBoolean(PREF_HAS_CREDENTIALS, false)){

                updateToken();
            } else {
                userNotAuthenticated();
            }
        } else {
            userNotAuthenticated();
        }
    }


    /*
    * At the time of closing and opening created or joined class lists,
    * parameters of the list layouts are need to be modified.
    *
    * The method copies previous layout paremeters to the newly made layout
    */
    private ConstraintLayout.LayoutParams copyParams(ConstraintLayout.LayoutParams layoutParams, ConstraintLayout.LayoutParams params){
        layoutParams.verticalChainStyle = params.verticalChainStyle;
        layoutParams.bottomToTop = params.bottomToTop;
        layoutParams.endToEnd = params.endToEnd;
        layoutParams.topToTop = params.topToTop;
        layoutParams.startToStart = params.startToStart;
        layoutParams.bottomToBottom = params.bottomToBottom;
        layoutParams.topToBottom = params.topToBottom;
        return layoutParams;
    }

    /*
    * Created class list and Joined class list are shown on the side bar of main activity.
    * They are shown as dropdown lists like below,
    *           - Created Class
    *               CSE327
    *               MAT350
    *           - Joined Class
    *               CSE499
    * This method is written to control the visuals of the lists, if the lists should be shown open or closed.
    */
    private void setUpContainers(){
        /*
        * @param mIsCreatedOpen is true if user wants to open the created class list
        */
        if(mIsCreatedOpen){
            mtvCreatedClass.setText("- Created Class");
            mCreatedClassRecyclerView.setVisibility(View.VISIBLE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mCreatedCont.getLayoutParams();
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
            TransitionManager.beginDelayedTransition(mCreatedCont);
            mCreatedCont.setLayoutParams(copyParams(layoutParams, params));
        } else {
            mtvCreatedClass.setText("+ Created Class");
            mCreatedClassRecyclerView.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mCreatedCont.getLayoutParams();
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT);
            TransitionManager.beginDelayedTransition(mCreatedCont);
            mCreatedCont.setLayoutParams(copyParams(layoutParams, params));
        }



        /*
        * @param mIsJoinedOpen is true if user wants to open the created class list
        */
        if(mIsJoinedOpen){
            mtvJoinedClass.setText("- Joined Class");
            mJoinedClassRecyclerView.setVisibility(View.VISIBLE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mJoinedCont.getLayoutParams();
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
            TransitionManager.beginDelayedTransition(mJoinedCont);
            mJoinedCont.setLayoutParams(copyParams(layoutParams, params));
        } else {
            mtvJoinedClass.setText("+ Joined Class");
            mJoinedClassRecyclerView.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mJoinedCont.getLayoutParams();
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT);
            TransitionManager.beginDelayedTransition(mJoinedCont);
            mJoinedCont.setLayoutParams(copyParams(layoutParams, params));
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_SIGN_IN) {

            } else if(requestCode == RC_POST_FACE){

            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == RC_SIGN_IN) {
                finish();
            } else if(requestCode == RC_POST_FACE){
                finish();
            }
        }
    }


    /*
    * Before accessing the MainActivity, user needs to be authenticated.
    * After it is ensured that a user is authenticated, this method starts to build up the UI and
    * starts collecting necessary data from database. But before that, the app needs the information about the
    * current user.
    */
    private void userAuthenticated(){
        Ion.with(getApplicationContext())
                .load("GET", API_REST_AUTH_USER)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            Toast.makeText(MainActivity.this, "Error " + e, Toast.LENGTH_LONG).show();
                        } else {
                            mUser = mGson.fromJson(result, User.class);
                            if(mUser == null || mUser.email == null || mUser.email.equals("") || mUser.email.isEmpty()){
                                // No user is logged in
                                userNotAuthenticated();
                            } else {
                                getFaceInfo();
                                getNotifications();
                                getCreatedClassInfo();
                                getJoinedClassInfo();
                                getFrequentContent();
                                updateUI();
                            }
                        }
                    }
                });
    }



    /*
    *  from the @method userAuthenticated(), the data of current user is gathered and stored in @variable mUser.
    *  Inside @variable mUser, name, the profile picture url of the user is stored.
    *  But actual photo data needs to be retrieved as Bitmap format from the database using that url.
    */
    private void updateUI(){
        mtvUserName.setText(mUser.name);

        Ion.with(getApplicationContext())
                .load(mUser.image)
                .setHeader("X-CSRFToken", mCSRFToken)
                .asBitmap()
                .setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        if(e != null){
                            Log.d("MAIN", e.toString());
                        } else {
                            setCircularProfilePic(result);
                        }
                    }
                });
    }


    /*
    *  @param bitmap    @type Bitmap
    *
    *  After retrieving the profile picture from @method updateUI(), the bitmap should be set as a circular image.
    *
    * */
    private void setCircularProfilePic(Bitmap bitmap){
        try {
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            roundedBitmapDrawable.setCircular(true);
            mivUserPic.setImageDrawable(roundedBitmapDrawable);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void errorLoading(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finish();
    }

    /*
    *  A user should provide his/her facial information to let the app recognize him/her when he/she studies
    *
    * This method checks if the facial information of the user is available in the database or not.
    * If not available, it calls @method gotoPostingFace()
    */
    private void getFaceInfo(){
        Ion.with(getApplicationContext())
                .load("GET", API_FACE_EXISTS)
                .setHeader("X-CSRFToken", mCSRFToken)
                .setMultipartParameter("user", String.valueOf(mUser.id))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null){
                            Log.d("error ", e.toString());
                            errorLoading("Connection Error, try again later.");
                        } else {
                            DataExists dataExists = mGson.fromJson(result, DataExists.class);
                            if(dataExists != null){
                                if(dataExists.exists >= 0){
                                    if(dataExists.exists == 0){
                                        gotoPostingFace();
                                    }
                                } else {
                                    errorLoading("Bad Request or Connection Problem");
                                }
                            } else {
                                errorLoading("Bad Request or Connection Problem");
                            }
                        }
                    }
                });
    }


    /*
    *  This method is called because facial information of the current user is not available in the database.
    */
    private void gotoPostingFace(){
        Intent intent = new Intent(this, PostFaceActivity.class);
        startActivityForResult(intent, RC_POST_FACE);
    }


    /*
    *  MainActivity shows notifications for the current user.
    *
    *  It is called whenever @method onResume() and then @method userAuthenticated are called
    */
    private void getNotifications(){
        Ion.with(getApplicationContext())
                .load("GET", API_SHOUTS_ALL)
                .setHeader("X-CSRFToken", mCSRFToken)
                .setMultipartParameter("user", String.valueOf(mUser.id))
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if(e != null) {
                            Log.d("classerror", e.toString());
                            unableToCreate();
                        } else {
                            Type listType = new TypeToken<ArrayList<GeneralShoutDetails>>(){}.getType();
                            List<GeneralShoutDetails> shoutList = mGson.fromJson(result, listType);
                            if(shoutList != null){
                                mShoutoutList.clear();
                                mShoutoutList.addAll(shoutList);
                                mShoutViewAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }


    /*
     *  MainActivity shows classes that are created by the current user.
     *
     *  It is called whenever @method onResume() and then @method userAuthenticated are called
     */
    private void getCreatedClassInfo(){
        Ion.with(getApplicationContext())
                .load("GET", API_CLASSES_CREATED + mUser.id)
                .setHeader("X-CSRFToken", mCSRFToken)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if(e != null) {
                            Log.d("classerror", e.toString());
                            unableToCreate();
                        } else {
                            Type listType = new TypeToken<ArrayList<ClassDetails>>(){}.getType();
                            List<ClassDetails> classList = mGson.fromJson(result, listType);
                            if(classList != null){
                                mClassDetailsList.clear();
                                mClassDetailsList.addAll(classList);
                                mCreatedClassAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }


    /*
     *  MainActivity shows classes that the current user has joined.
     *
     *  It is called whenever @method onResume() and then @method userAuthenticated are called
     */
    private void getJoinedClassInfo(){
        Ion.with(getApplicationContext())
                .load("GET", API_CLASSES_JOINED + mUser.id)
                .setHeader("X-CSRFToken", mCSRFToken)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if(e != null) {
                            Log.d("classerror", e.toString());
                            unableToCreate();
                        } else {
                            Type listType = new TypeToken<ArrayList<JoinedClassDetails>>(){}.getType();
                            List<JoinedClassDetails> classList = mGson.fromJson(result, listType);
                            if(classList != null){
                                mJoinedClassList.clear();
                                mJoinedClassList.addAll(classList);
                                mJoinedClassAdapter.notifyDataSetChanged();
                                //Log.d("haha", "" + mJoinedClassList.get(0).classroom.creator.name);
                            }
                        }
                    }
                });
    }


    /*
    * To make the app user friendly, contents like class, material that are accessed frequently
    * by the user, are shown on top of the MainActivity. Clicking on them will lead him/her
    * directly to that content window.
    *
    * This method retrieves the information about all the frequent contents
    */
    private void getFrequentContent(){
        Ion.with(getApplicationContext())
                .load("GET", API_STATS_FREQUENCY_GET)
                .setHeader("X-CSRFToken", mCSRFToken)
                .setMultipartParameter("user", String.valueOf(mUser.id))
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if(e != null){
                            Log.d("no tag", e.toString());
                        } else {
                            Type listType = new TypeToken<ArrayList<ContentFrequencyDetails>>(){}.getType();
                            List<ContentFrequencyDetails> freqList = mGson.fromJson(result, listType);
                            if(freqList != null){
                                mFrequencyList.clear();
                                mFrequencyList.addAll(freqList);
                                mFrequencyAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }



    /*
    *  If the current session is expired then token will not be valid anymore.
    *  Hence, this method re-logins the user and updates the session along with tokens.
    */
    private void updateToken(){
        String email;
        String password;
        if(mSharedPreferences.contains(PREF_CREDENTIAL_EMAIL)){
            email = mSharedPreferences.getString(PREF_CREDENTIAL_EMAIL, "");
        } else {
            userNotAuthenticated();
            return;
        }
        if(mSharedPreferences.contains(PREF_CREDENTIAL_PASSWORD)){
            password = mSharedPreferences.getString(PREF_CREDENTIAL_PASSWORD, "");
        } else {
            userNotAuthenticated();
            return;
        }

        final Gson gson = new Gson();

        Ion.with(getApplicationContext())
                .load("POST", API_REST_AUTH_LOGIN)
                .setMultipartParameter("email", email)
                .setMultipartParameter("password", password)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            invalidCredentials();
                        } else {
                            TokenKeyModel token = gson.fromJson(result, TokenKeyModel.class);
                            saveToken(token);
                        }
                    }
                });
    }



    /*
    *  This method is called when no user is logged in
    *  So, it will initiate the sign in process.
    */
    private void userNotAuthenticated(){
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    /*
    *  User token that was received after loging in/ signing up is being stored in shared preference.
    */
    private void saveToken(TokenKeyModel token){
        mToken = token.key;
        mSharedPreferences.edit().putString(PREF_TOKEN_KEY, mToken).apply();
        mSharedPreferences.edit().putBoolean(PREF_HAS_TOKEN, true).apply();
    }

    private void invalidCredentials(){
        mSharedPreferences.edit().putBoolean(PREF_HAS_CREDENTIALS, false).apply();
        userNotAuthenticated();
    }


    private void logout(){

        Ion.with(getApplicationContext())
                .load("POST", API_REST_AUTH_LOGOUT)
                .setHeader("X-CSRFToken", mCSRFToken)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null){
                            Toast.makeText(MainActivity.this, "Not successful, try again", Toast.LENGTH_LONG).show();
                        } else {
                            setLogOutData();
                        }
                    }
                });

    }

    /*
    *   When a user logs out, all of his/her session informations should be cleared.
    */
    private void setLogOutData(){
        mSharedPreferences.edit().putBoolean(PREF_HAS_CREDENTIALS, false).apply();
        mSharedPreferences.edit().putBoolean(PREF_HAS_TOKEN, false).apply();
        mSharedPreferences.edit().putBoolean(PREF_HAS_CSRF_TOKEN, false).apply();
        mSharedPreferences.edit().putBoolean(PREF_HAS_SESSION_ID, false).apply();
        userNotAuthenticated();
    }








    private void setDialog(){
        mDialog = new Dialog(this);
        mDialog.setContentView(R.layout.dialog_create_join_class);
        tvDiaJoinClass = mDialog.findViewById(R.id.join_class_option);
        tvbDiaJoinClass = mDialog.findViewById(R.id.join_class_button);
        tvDiaCreateClass = mDialog.findViewById(R.id.create_class_option);
        tvbDiaCreateClass = mDialog.findViewById(R.id.create_class_button);
        diaCreateJoinCont = mDialog.findViewById(R.id.create_join_container);
        diaJoinCont = mDialog.findViewById(R.id.join_container);
        diaCreateCont = mDialog.findViewById(R.id.create_container);
        etvAccessCode = mDialog.findViewById(R.id.edit_join_class);
        etvClassName = mDialog.findViewById(R.id.edit_create_class);
        tvDiaJoinClass.setOnClickListener(this);
        tvbDiaJoinClass.setOnClickListener(this);
        tvDiaCreateClass.setOnClickListener(this);
        tvbDiaCreateClass.setOnClickListener(this);
    }

    /*
        It is invoked when the floating action button is clicked
        A dialog will be shown to the user to let them select 'Create Class' or 'Join Class'
    */
    private void showCreateJoin(){
        diaCreateJoinCont.setVisibility(View.VISIBLE);
        diaJoinCont.setVisibility(View.GONE);
        diaCreateCont.setVisibility(View.GONE);
        mDialog.show();
    }

    private void showJoin(){
        diaCreateJoinCont.setVisibility(View.GONE);
        diaJoinCont.setVisibility(View.VISIBLE);
        diaCreateCont.setVisibility(View.GONE);
        mDialog.show();
    }

    private void showCreate(){
        diaCreateJoinCont.setVisibility(View.GONE);
        diaJoinCont.setVisibility(View.GONE);
        diaCreateCont.setVisibility(View.VISIBLE);
        mDialog.show();
    }








    private void createClassStart(){
        String name = etvClassName.getText().toString().trim();
        if(name.equals("") || name.isEmpty()){
            etvClassName.setError("Can't be empty.");
            return;
        }
        createClassInDatabase(name);
    }



    /*
    *  The user is trying to open up a classroom.
    *  An unique access code will be created for the class in the database.
    *
    *  After creating the class, side bar of the MainActivity should show the updated class list.
    */
    private void createClassInDatabase(String name){
        while(mUser == null);
        Ion.with(getApplicationContext())
                .load("POST", API_CLASSES_CREATE)
                .setHeader("X-CSRFToken", mCSRFToken)
                .setMultipartParameter("name", name)
                .setMultipartParameter("creator", String.valueOf(mUser.id))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            //Toast.makeText(MainActivity.this, "Error " + e, Toast.LENGTH_LONG).show();
                            Log.d("ERROR", e.toString());
                            unableToCreate();
                        } else {
                            Toast.makeText(MainActivity.this, "Class successfully created!", Toast.LENGTH_LONG).show();
                            mDialog.dismiss();
                            getCreatedClassInfo();
                        }
                    }
                });
    }

    private void unableToCreate(){
        Toast.makeText(this, "Server Error!", Toast.LENGTH_LONG).show();
    }


    private void joinClassStart(){
        String code = etvAccessCode.getText().toString().trim();
        if(code.equals("") || code.isEmpty()){
            etvAccessCode.setError("Can't be empty.");
            return;
        }
        joinClassInDatabase(code);
    }


    /*
    *  The user is trying to join in a classroom by providing the access code.
    *  He/she will join the class if the code is right.
    * */
    private void joinClassInDatabase(String code){
        while(mUser == null);
        Ion.with(getApplicationContext())
                .load("POST", API_CLASSES_JOIN + mUser.id + "/" + code)
                .setHeader("X-CSRFToken", mCSRFToken)
                .setBodyParameter("classroom", "noclass")
                .setBodyParameter("student", String.valueOf(mUser.id))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            //Toast.makeText(MainActivity.this, "Error " + e, Toast.LENGTH_LONG).show();
                            Log.d("ERROR", e.toString());
                            unableToJoin();
                        } else {
                            Toast.makeText(MainActivity.this, "Joined class successfully!", Toast.LENGTH_LONG).show();
                            mDialog.dismiss();
                            getJoinedClassInfo();
                        }
                    }
                });
    }


    private void unableToJoin(){
        Toast.makeText(this, "Wrong Code or Server Error!", Toast.LENGTH_LONG).show();
    }





    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab_create_join_class:
                showCreateJoin();
                break;

            case R.id.join_class_option:
                showJoin();
                break;

            case R.id.join_class_button:
                joinClassStart();
                break;

            case R.id.create_class_option:
                showCreate();
                break;

            case R.id.create_class_button:
                createClassStart();
                break;


            case R.id.tvb_log_out:
                logout();
                break;

            case R.id.tv_opened_class:
                mIsCreatedOpen = !mIsCreatedOpen;
                setUpContainers();
                break;

            case R.id.tv_joined_class:
                mIsJoinedOpen = !mIsJoinedOpen;
                setUpContainers();
                break;
        }
    }

    @Override
    public void onClassClicked(ClassDetails classDetails) {
        Intent intent = new Intent(this, ClassDataActivity.class);
        intent.putExtra(EXTRA_KEY_CLASS_ID, classDetails.id);
        startActivity(intent);
    }

    @Override
    public void onJoinedClassClicked(JoinedClassDetails classDetails) {
        Intent intent = new Intent(this, JoinedClassActivity.class);
        intent.putExtra(EXTRA_KEY_CLASS_ID, classDetails.classroom.id);
        startActivity(intent);
    }

    @Override
    public void onShoutClicked(GeneralShoutDetails shoutDetails) {
        Ion.with(getApplicationContext())
                .load("POST", API_SHOUTS_SEEN)
                .setHeader("X-CSRFToken", mCSRFToken)
                .setMultipartParameter("id", String.valueOf(shoutDetails.id))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {

                    }
                });

        Intent intent = new Intent(this, MaterialListActivity.class);
        intent.putExtra(EXTRA_KEY_CLASS_ID, shoutDetails.classroom.id);
        startActivity(intent);
    }

    private void gotoMaterial(ContentFrequencyDetails frequencyDetails){
        Intent intent = new Intent(this, MaterialViewActivity.class);
        intent.putExtra(EXTRA_KEY_MATERIAL_ID, frequencyDetails.content_id);
        intent.putExtra(EXTRA_KEY_MATERIAL_MODE, MODE_VIEW_AND_FACE);
        startActivity(intent);
    }

    private void gotoJoinedClass(ContentFrequencyDetails frequencyDetails){
        Intent intent = new Intent(this, JoinedClassActivity.class);
        intent.putExtra(EXTRA_KEY_CLASS_ID, frequencyDetails.content_id);
        startActivity(intent);
    }

    private void gotoCreatedClass(ContentFrequencyDetails frequencyDetails){
        Intent intent = new Intent(this, ClassDataActivity.class);
        intent.putExtra(EXTRA_KEY_CLASS_ID, frequencyDetails.content_id);
        startActivity(intent);
    }

    @Override
    public void onContentClicked(ContentFrequencyDetails frequencyDetails) {
        switch(frequencyDetails.content_type){
            case TYPE_MATERIAL:
                gotoMaterial(frequencyDetails);
                break;

            case TYPE_JOINED_CLASS:
                gotoJoinedClass(frequencyDetails);
                break;

            case TYPE_CREATED_CLASS:
                gotoCreatedClass(frequencyDetails);
                break;
        }
    }
}
