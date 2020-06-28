package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.foh.studypolice.R;

import java.util.List;

import ModelClasses.ClassStudentDetails;

public class SpinnerStudentAdapter extends BaseAdapter {

    private List<ClassStudentDetails> mStudentList;
    private Context mContext;

    public SpinnerStudentAdapter(List<ClassStudentDetails> mStudentList, Context mContext) {
        this.mStudentList = mStudentList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        if(mStudentList == null) return 0;
        return mStudentList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mContext).inflate(R.layout.spinner_list_item, null);
        TextView textView = view.findViewById(R.id.spinner_item_title);
        ClassStudentDetails studentDetails = mStudentList.get(i);
        textView.setText(studentDetails.student.name);

        return view;
    }
}
