package com.foh.studypolice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import Adapters.ReadTimeViewAdapter;
import Adapters.SpinnerMaterialAdapter;
import Adapters.SpinnerStudentAdapter;
import ModelClasses.ClassStudentDetails;
import ModelClasses.MaterialDetails;
import ModelClasses.ReadTimeDetails;
import ModelClasses.UserShortDetails;

import static AllConstants.IntentKeys.EXTRA_KEY_CLASS_ID;
import static AllConstants.IntentKeys.EXTRA_KEY_CLASS_NAME;
import static AllConstants.IntentKeys.EXTRA_KEY_PREVIEW;
import static AllConstants.RestApiUrlsAndKeys.API_CLASSES_STUDENTS;
import static AllConstants.RestApiUrlsAndKeys.API_MATERIALS;
import static AllConstants.RestApiUrlsAndKeys.API_STATS_READTIME_MATERIAL;
import static AllConstants.RestApiUrlsAndKeys.API_STATS_READTIME_STUDENT;
import static com.foh.studypolice.MainActivity.mCSRFToken;

public class ClassDataDetailsActivity extends AppCompatActivity implements ReadTimeViewAdapter.OnStatClickHandler, View.OnClickListener {

    private int mClassID;
    private String mClassName;

    private Gson mGson;

    public static final int MODE_STUDENT = 3;
    public static final int MODE_MATERIAL = 4;

    private static long mLastStudentRequestTime = -1;
    private static long mLastMaterialRequestTime = -1;

    private ReadTimeViewAdapter mTopStudentAdapter;
    private ReadTimeViewAdapter mTopMaterialAdapter;

    private TextView mtvClassName;
    private Spinner mspAllStudents;
    private Spinner mspAllMaterials;
    private TextView mtvbStudentInfo;
    private TextView mtvbMaterialInfo;
    private RecyclerView mrvStudents;
    private RecyclerView mrvMaterials;

    private List<ClassStudentDetails> mStudentList;
    private List<MaterialDetails> mMaterialList;

    private List<ReadTimeDetails> mStudentReadTimeList;
    private List<ReadTimeDetails> mMaterialReadTimeList;

    private MaterialDetails mSelectedMaterialDetails;
    private ClassStudentDetails mSelectedStudentDetails;

    private boolean mShowTopStudents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_data_details);

        addToolBar();

        mtvClassName = findViewById(R.id.class_title);
        mspAllStudents = findViewById(R.id.all_students);
        mspAllMaterials = findViewById(R.id.all_materials);
        mtvbStudentInfo = findViewById(R.id.tvb_stu_info);
        mtvbMaterialInfo = findViewById(R.id.tvb_mat_info);

        mrvStudents = findViewById(R.id.rv_students);
        mrvMaterials = findViewById(R.id.rv_materials);

        LinearLayoutManager studentLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        mrvStudents.setLayoutManager(studentLayoutManager);
        mrvStudents.setHasFixedSize(true);

        LinearLayoutManager materialLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);
        mrvMaterials.setLayoutManager(materialLayoutManager);
        mrvMaterials.setHasFixedSize(true);

        mGson = new Gson();

        getIntentExtras();

        mtvbStudentInfo.setOnClickListener(this);
        mtvbMaterialInfo.setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mStudentList = new ArrayList<>();
        mMaterialList = new ArrayList<>();

        mStudentReadTimeList = new ArrayList<>();
        mTopStudentAdapter = new ReadTimeViewAdapter(this, this, MODE_STUDENT, mStudentReadTimeList);
        mrvStudents.setAdapter(mTopStudentAdapter);

        mMaterialReadTimeList = new ArrayList<>();
        mTopMaterialAdapter = new ReadTimeViewAdapter(this, this, MODE_MATERIAL, mMaterialReadTimeList);
        mrvMaterials.setAdapter(mTopMaterialAdapter);

        mLastStudentRequestTime = -1;
        mLastMaterialRequestTime = -1;
    }

    private void addToolBar(){
        Toolbar toolbar = findViewById(R.id.sp_main_toolbar);

        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
    }

    private void getIntentExtras(){
        boolean good = true;

        if(getIntent().hasExtra(EXTRA_KEY_CLASS_ID)){
            mClassID = getIntent().getIntExtra(EXTRA_KEY_CLASS_ID, 0);
        } else good = false;

        if(getIntent().hasExtra(EXTRA_KEY_CLASS_NAME)){
            mClassName = getIntent().getStringExtra(EXTRA_KEY_CLASS_NAME);
        } else good = false;

        if(getIntent().hasExtra(EXTRA_KEY_PREVIEW)){
            mShowTopStudents = getIntent().getBooleanExtra(EXTRA_KEY_PREVIEW, true);
        } else {
            mShowTopStudents = true;
        }

        if(good){
            setUpUI();
        } else {
            noClassFoundError("Class not found. Quiting...");
        }
    }

    private void noClassFoundError(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finish();
    }

    private void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void setUpUI(){
        getAllStuddents();
        getAllMaterials();
        setListPreview();
        mtvClassName.setText(mClassName);
    }

    /*
    *  The student list will let the user to select a specific student or all student.
    *  Names of the students will be on a spinner.
    *  This method gets the information about the students
    * */
    private void getAllStuddents(){
        Ion.with(getApplicationContext())
                .load("GET", API_CLASSES_STUDENTS + mClassID)
                .setHeader("X-CSRFToken", mCSRFToken)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if(e != null){
                            Log.d("huh", e.toString());
                            showMsg("Connection Problem.");
                        } else {
                            Type listType = new TypeToken<ArrayList<ClassStudentDetails>>(){}.getType();
                            List<ClassStudentDetails> list = mGson.fromJson(result, listType);
                            if(list != null){
                                mStudentList.clear();
                                ClassStudentDetails studentDetails = new ClassStudentDetails(mClassID,
                                        new UserShortDetails(-1, "-All Students"));
                                mStudentList.add(studentDetails);
                                mStudentList.addAll(list);
                                setStudentSpinner();
                            }
                        }
                    }
                });
    }


    /*
     *  The material list will let the user to select a specific material or all material.
     *  Names of the materials will be on a spinner.
     *  This method gets the information about the materials
     * */
    private void getAllMaterials(){
        Ion.with(getApplicationContext())
                .load("GET", API_MATERIALS + mClassID)
                .setHeader("X-CSRFToken", mCSRFToken)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if(e != null){
                            Log.d("huh", e.toString());
                            showMsg("Connection Problem.");
                        } else {
                            Type listType = new TypeToken<ArrayList<MaterialDetails>>(){}.getType();
                            List<MaterialDetails> list = mGson.fromJson(result, listType);
                            if(list != null){
                                mMaterialList.clear();
                                MaterialDetails materialDetails = new MaterialDetails(-1, "-All Materials", mClassID,
                                        null, null);
                                mMaterialList.add(materialDetails);
                                mMaterialList.addAll(list);
                                setMaterialSpinner();
                            }
                        }
                    }
                });
    }



    /*
    *  Student reading time will be shown in the result list after selecting an ectry from
    *   the student spinner.
    *  This method sets up and defines the student spinner.
    * */
    private void setStudentSpinner(){
        SpinnerStudentAdapter studentAdapter = new SpinnerStudentAdapter(mStudentList, this);
        mspAllStudents.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedStudentDetails = mStudentList.get(i);
                if(mSelectedMaterialDetails != null){
                    getDataStats(MODE_STUDENT);
                    getDataStats(MODE_MATERIAL);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mspAllStudents.setAdapter(studentAdapter);
    }


    /*
     *  Material reading time will be shown in the result list after selecting an ectry from
     *   the material spinner.
     *  This method sets up and defines the material spinner.
     * */
    private void setMaterialSpinner(){
        SpinnerMaterialAdapter materialAdapter = new SpinnerMaterialAdapter(mMaterialList, this);
        mspAllMaterials.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedMaterialDetails = mMaterialList.get(i);
                if(mSelectedStudentDetails != null){
                    getDataStats(MODE_STUDENT);
                    getDataStats(MODE_MATERIAL);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mspAllMaterials.setAdapter(materialAdapter);
    }



    /*
    *   @param mode represents whether reading time of the students or for the materials would be shown
    * */
    private void getDataStats(int mode){
        long currentTime = System.currentTimeMillis();
        String URL = "http://codeforces.com/";

        switch (mode){
            case MODE_STUDENT:
                mLastStudentRequestTime = currentTime;
                URL = API_STATS_READTIME_STUDENT;
                break;

            case MODE_MATERIAL:
                mLastMaterialRequestTime = currentTime;
                URL = API_STATS_READTIME_MATERIAL;
                break;
        }

        Ion.with(this)
                .load("GET", URL)
                .setHeader("X-CSRFToken", mCSRFToken)
                .setMultipartParameter("classroom", String.valueOf(mClassID))
                .setMultipartParameter("student", String.valueOf(mSelectedStudentDetails.student.id))
                .setMultipartParameter("material", String.valueOf(mSelectedMaterialDetails.id))
                .asJsonArray()
                .setCallback(new ServerCallback(mode, currentTime));
    }



    private void setListPreview(){
        if(mShowTopStudents){
            mtvbStudentInfo.setBackgroundColor(getResources().getColor(R.color.colorMagenta));
            mtvbStudentInfo.setTextColor(getResources().getColor(R.color.colorWhite));
            mtvbMaterialInfo.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mtvbMaterialInfo.setTextColor(getResources().getColor(R.color.colorMaterial));
            mrvStudents.setVisibility(View.VISIBLE);
            mrvMaterials.setVisibility(View.GONE);
        } else {
            mtvbStudentInfo.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mtvbStudentInfo.setTextColor(getResources().getColor(R.color.colorMagenta));
            mtvbMaterialInfo.setBackgroundColor(getResources().getColor(R.color.colorMaterial));
            mtvbMaterialInfo.setTextColor(getResources().getColor(R.color.colorWhite));
            mrvStudents.setVisibility(View.GONE);
            mrvMaterials.setVisibility(View.VISIBLE);
        }
    }






    @Override
    public void onTimeClicked(ReadTimeDetails readTimeDetails, int mode) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.tvb_stu_info:
                mShowTopStudents = true;
                setListPreview();
                break;

            case R.id.tvb_mat_info:
                mShowTopStudents = false;
                setListPreview();
                break;
        }
    }


    /*
    *   A class about how data about reading time of the students and materials should be handled.
    * */
    private class ServerCallback implements FutureCallback<JsonArray> {

        private long calledTime;
        private int mode;

        public ServerCallback(int mode, long calledTime){
            this.calledTime = calledTime;
            this.mode = mode;
        }

        @Override
        public void onCompleted(Exception e, JsonArray result) {
            if(e != null){
                showMsg("Connection Problem.");
            } else {
                Type listType = new TypeToken<ArrayList<ReadTimeDetails>>(){}.getType();
                List<ReadTimeDetails> list = mGson.fromJson(result, listType);
                if(list != null){
                    switch (mode) {
                        case MODE_STUDENT:
                            if(calledTime < mLastStudentRequestTime) return;
                            mStudentReadTimeList.clear();
                            Collections.sort(list, new SortReadingTime());
                            mStudentReadTimeList.addAll(list);
                            mTopStudentAdapter.notifyDataSetChanged();
                            break;

                        case MODE_MATERIAL:
                            if(calledTime < mLastMaterialRequestTime) return;
                            mMaterialReadTimeList.clear();
                            Collections.sort(list, new SortReadingTime());
                            mMaterialReadTimeList.addAll(list);
                            mTopMaterialAdapter.notifyDataSetChanged();
                            break;
                    }
                }
            }
        }
    }

    private class SortReadingTime implements Comparator<ReadTimeDetails> {

        @Override
        public int compare(ReadTimeDetails t1, ReadTimeDetails t2) {
            return (int)(t2.duration - t1.duration);
        }
    }
}
