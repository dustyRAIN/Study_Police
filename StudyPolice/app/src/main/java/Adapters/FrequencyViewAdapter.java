package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foh.studypolice.R;

import java.util.List;

import ModelClasses.ContentFrequencyDetails;

import static AllConstants.IntegerKeys.TYPE_CREATED_CLASS;
import static AllConstants.IntegerKeys.TYPE_JOINED_CLASS;
import static AllConstants.IntegerKeys.TYPE_MATERIAL;

public class FrequencyViewAdapter extends RecyclerView.Adapter<FrequencyViewAdapter.FrequencyViewHolder> {

    private Context mContext;
    private List<ContentFrequencyDetails> mFrequencyList;
    private OnClickHandler mOnClickHandler;

    public interface OnClickHandler{
        void onContentClicked(ContentFrequencyDetails frequencyDetails);
    }

    public FrequencyViewAdapter(Context mContext, List<ContentFrequencyDetails> mFrequencyList, OnClickHandler mOnClickHandler) {
        this.mContext = mContext;
        this.mFrequencyList = mFrequencyList;
        this.mOnClickHandler = mOnClickHandler;
    }

    @NonNull
    @Override
    public FrequencyViewAdapter.FrequencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.frequency_list_item, parent, false);
        return new FrequencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FrequencyViewAdapter.FrequencyViewHolder holder, int position) {
        ContentFrequencyDetails frequencyDetails = mFrequencyList.get(position);

        holder.contentTitle.setText(frequencyDetails.name);
        holder.material.setVisibility(View.GONE);
        holder.joinedClass.setVisibility(View.GONE);
        holder.createdClass.setVisibility(View.GONE);

        switch(frequencyDetails.content_type){
            case TYPE_MATERIAL:
                holder.material.setVisibility(View.VISIBLE);
                break;

            case TYPE_JOINED_CLASS:
                holder.joinedClass.setVisibility(View.VISIBLE);
                break;

            case TYPE_CREATED_CLASS:
                holder.createdClass.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if(mFrequencyList == null) return 0;
        return mFrequencyList.size();
    }

    public class FrequencyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView contentTitle;
        final ImageView material;
        final ImageView createdClass;
        final ImageView joinedClass;

        public FrequencyViewHolder(@NonNull View itemView) {
            super(itemView);
            contentTitle = itemView.findViewById(R.id.content_title);
            material = itemView.findViewById(R.id.ic_cont_material);
            createdClass = itemView.findViewById(R.id.ic_cont_created_class);
            joinedClass = itemView.findViewById(R.id.ic_cont_joined_class);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = getAdapterPosition();
            mOnClickHandler.onContentClicked(mFrequencyList.get(id));
        }
    }
}
