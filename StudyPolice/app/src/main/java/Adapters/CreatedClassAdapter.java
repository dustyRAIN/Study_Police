package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foh.studypolice.R;

import java.util.List;

import ModelClasses.ClassDetails;

public class CreatedClassAdapter extends RecyclerView.Adapter<CreatedClassAdapter.ClassViewHolder> {

    private Context mContext;
    private List<ClassDetails> mClassList;
    private OnClassClickHandler mClickHandler;

    public interface OnClassClickHandler{
        void onClassClicked(ClassDetails classDetails);
    }

    public CreatedClassAdapter(Context mContext, List<ClassDetails> mClassList,
                               OnClassClickHandler clickHandler) {
        this.mContext = mContext;
        this.mClassList = mClassList;
        this.mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.created_class_list_item, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassDetails classDetails = mClassList.get(position);

        holder.className.setText(classDetails.name);
        holder.accessCode.setText(classDetails.access_code);
    }

    @Override
    public int getItemCount() {
        if(mClassList == null) return 0;
        return mClassList.size();
    }

    public class ClassViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView className;
        final TextView accessCode;

        public ClassViewHolder(View itemView) {
            super(itemView);

            className = itemView.findViewById(R.id.list_created_class_name);
            accessCode = itemView.findViewById(R.id.list_access_code);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            mClickHandler.onClassClicked(mClassList.get(pos));
        }
    }
}
