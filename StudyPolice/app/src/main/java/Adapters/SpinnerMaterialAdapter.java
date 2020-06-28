package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.foh.studypolice.R;

import java.util.List;

import ModelClasses.MaterialDetails;

public class SpinnerMaterialAdapter extends BaseAdapter {

    List<MaterialDetails> mMaterialList;
    private Context mContext;

    public SpinnerMaterialAdapter(List<MaterialDetails> mMaterialList, Context mContext) {
        this.mMaterialList = mMaterialList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        if(mMaterialList == null) return 0;
        return mMaterialList.size();
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
        MaterialDetails materialDetails = mMaterialList.get(i);
        textView.setText(materialDetails.name);

        return view;
    }
}
