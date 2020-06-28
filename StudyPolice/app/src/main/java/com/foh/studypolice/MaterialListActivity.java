package com.foh.studypolice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Adapters.MaterialListAdapter;
import ModelClasses.MaterialDetails;

import static AllConstants.IntegerKeys.MODE_ONLY_VIEW;
import static AllConstants.IntentKeys.EXTRA_KEY_CLASS_ID;
import static AllConstants.IntentKeys.EXTRA_KEY_MATERIAL_ID;
import static AllConstants.IntentKeys.EXTRA_KEY_MATERIAL_LINK;
import static AllConstants.IntentKeys.EXTRA_KEY_MATERIAL_MODE;
import static AllConstants.RestApiUrlsAndKeys.API_MATERIALS;
import static com.foh.studypolice.MainActivity.mCSRFToken;

public class MaterialListActivity extends AppCompatActivity implements MaterialListAdapter.MaterialClickHandler {

    private int mClassID;

    private RecyclerView mMaterialsView;
    private List<MaterialDetails> mMaterialList;
    private MaterialListAdapter mAdapter;

    private Gson mGson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_list);

        addToolBar();
        getIntentExtra();

        mGson = new Gson();

        mMaterialsView = findViewById(R.id.rv_materials);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        mMaterialsView.setLayoutManager(layoutManager);
        mMaterialsView.setHasFixedSize(true);
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
        getMaterials();
    }

    private void addToolBar(){
        Toolbar toolbar = findViewById(R.id.sp_main_toolbar);

        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
    }

    private void getIntentExtra(){
        if(getIntent().hasExtra(EXTRA_KEY_CLASS_ID)){
            mClassID = getIntent().getIntExtra(EXTRA_KEY_CLASS_ID, 1);
        } else {
            noClassID();
        }
    }

    private void noClassID(){
        Toast.makeText(this, "No class found. Exiting...", Toast.LENGTH_LONG).show();
        finish();
    }


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

    private void noConnection(){
        Toast.makeText(this, "Bad network or server error", Toast.LENGTH_LONG).show();
    }


    private Intent putIntentExtras(Intent intent, MaterialDetails materialDetails){

        intent.putExtra(EXTRA_KEY_MATERIAL_ID, materialDetails.id);
        intent.putExtra(EXTRA_KEY_MATERIAL_LINK, materialDetails.the_file);
        intent.putExtra(EXTRA_KEY_CLASS_ID, mClassID);
        intent.putExtra(EXTRA_KEY_MATERIAL_MODE, MODE_ONLY_VIEW);

        return intent;
    }

    @Override
    public void onMaterialClick(MaterialDetails materialDetails) {
        Intent intent = new Intent(this, MaterialViewActivity.class);
        startActivity(putIntentExtras(intent, materialDetails));
    }
}
