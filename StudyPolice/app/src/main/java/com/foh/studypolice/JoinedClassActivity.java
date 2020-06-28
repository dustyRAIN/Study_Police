package com.foh.studypolice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Adapters.MaterialListAdapter;
import ModelClasses.ClassDetails;
import ModelClasses.MaterialDetails;
import Utils.FrequencyUpdater;

import static AllConstants.IntegerKeys.FREQ_VALUE_JOINED_CLASS;
import static AllConstants.IntegerKeys.MODE_VIEW_AND_FACE;
import static AllConstants.IntegerKeys.TYPE_JOINED_CLASS;
import static AllConstants.IntentKeys.EXTRA_KEY_CLASS_ID;
import static AllConstants.IntentKeys.EXTRA_KEY_MATERIAL_ID;
import static AllConstants.IntentKeys.EXTRA_KEY_MATERIAL_LINK;
import static AllConstants.IntentKeys.EXTRA_KEY_MATERIAL_MODE;
import static AllConstants.RestApiUrlsAndKeys.API_CLASSES;
import static AllConstants.RestApiUrlsAndKeys.API_MATERIALS;
import static com.foh.studypolice.MainActivity.mCSRFToken;
import static com.foh.studypolice.MainActivity.mUser;

public class JoinedClassActivity extends AppCompatActivity implements MaterialListAdapter.MaterialClickHandler {

    private int mClassID;

    private ClassDetails mClassDetails;

    private TextView mtvClassName;

    private RecyclerView mMaterialsView;
    private List<MaterialDetails> mMaterialList;
    private MaterialListAdapter mAdapter;

    private Gson mGson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joined_class);

        addToolBar();


        mGson = new Gson();

        mtvClassName = findViewById(R.id.tv_side_class_name);
        mMaterialsView = findViewById(R.id.rv_materials);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        mMaterialsView.setLayoutManager(layoutManager);
        mMaterialsView.setHasFixedSize(true);

        getIntentExtra();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMaterialList = new ArrayList<>();
        mAdapter = new MaterialListAdapter(this, mMaterialList, this);
        mMaterialsView.setAdapter(mAdapter);
    }

    private void addToolBar(){
        Toolbar toolbar = findViewById(R.id.sp_main_toolbar);

        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
    }

    private void getIntentExtra(){
        if(getIntent().hasExtra(EXTRA_KEY_CLASS_ID)){
            mClassID = getIntent().getIntExtra(EXTRA_KEY_CLASS_ID, 1);
            getClassData(mClassID);
            getMaterials();
        } else {
            noClassID();
        }
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


    /*
    * A user will see the materials available in a class that he/she has joined.
    * This method is written to get the information of all the materials that this class has
    * */
    private void getMaterials(){
        Ion.with(getApplicationContext())
                .load("GET", API_MATERIALS + mClassID)
                .setHeader("X-CSRFToken", mCSRFToken)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if(e != null){
                            noConnection();
                        } else {
                            Type listType = new TypeToken<ArrayList<MaterialDetails>>(){}.getType();
                            List<MaterialDetails> list = mGson.fromJson(result, listType);
                            if(list != null){
                                mMaterialList.clear();
                                mMaterialList.addAll(list);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    private void setUpUI(){
        FrequencyUpdater.updateFrequency(getApplicationContext(), mCSRFToken, mClassID, TYPE_JOINED_CLASS, mUser.id, FREQ_VALUE_JOINED_CLASS);
        mtvClassName.setText(mClassDetails.name);
    }

    private void noConnection(){
        Toast.makeText(this, "Bad network or server error", Toast.LENGTH_LONG).show();
    }


    private Intent putIntentExtras(Intent intent, MaterialDetails materialDetails){

        intent.putExtra(EXTRA_KEY_MATERIAL_ID, materialDetails.id);
        intent.putExtra(EXTRA_KEY_MATERIAL_LINK, materialDetails.the_file);
        intent.putExtra(EXTRA_KEY_CLASS_ID, mClassID);
        intent.putExtra(EXTRA_KEY_MATERIAL_MODE, MODE_VIEW_AND_FACE);

        return intent;
    }

    @Override
    public void onMaterialClick(MaterialDetails materialDetails) {
        Intent intent = new Intent(this, MaterialViewActivity.class);
        startActivity(putIntentExtras(intent, materialDetails));
    }
}
