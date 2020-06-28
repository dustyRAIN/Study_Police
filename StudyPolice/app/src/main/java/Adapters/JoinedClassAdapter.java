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

import ModelClasses.JoinedClassDetails;

public class JoinedClassAdapter extends RecyclerView.Adapter<JoinedClassAdapter.ClassViewHolder> {

    private Context mContext;
    private List<JoinedClassDetails> mClassList;
    private JoinedClassAdapter.OnClassClickHandler mClickHandler;

    public interface OnClassClickHandler{
        void onJoinedClassClicked(JoinedClassDetails classDetails);
    }

    public JoinedClassAdapter(Context mContext, List<JoinedClassDetails> mClassList, OnClassClickHandler mClickHandler) {
        this.mContext = mContext;
        this.mClassList = mClassList;
        this.mClickHandler = mClickHandler;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.joined_class_list_item, parent, false);
        return new JoinedClassAdapter.ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        JoinedClassDetails classDetails = mClassList.get(position);

        holder.className.setText(classDetails.classroom.name);
        holder.creatorName.setText(classDetails.classroom.creator.name);
    }

    @Override
    public int getItemCount() {
        if(mClassList == null) return 0;
        return mClassList.size();
    }

    public class ClassViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView className;
        final TextView creatorName;

        public ClassViewHolder(View itemView) {
            super(itemView);

            className = itemView.findViewById(R.id.list_joined_class_name);
            creatorName = itemView.findViewById(R.id.list_creator_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            mClickHandler.onJoinedClassClicked(mClassList.get(pos));
        }
    }
}
