package Adapters;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.foh.studypolice.R;

import java.util.List;

import ModelClasses.ReadTimeDetails;

import static com.foh.studypolice.ClassDataDetailsActivity.MODE_MATERIAL;
import static com.foh.studypolice.ClassDataDetailsActivity.MODE_STUDENT;

public class ReadTimeViewAdapter extends RecyclerView.Adapter<ReadTimeViewAdapter.TimeViewHolder> {

    private Context mContext;
    private OnStatClickHandler mClickHandler;
    private int mMode;
    private List<ReadTimeDetails> mReadTimeList;


    public interface OnStatClickHandler{
        void onTimeClicked(ReadTimeDetails readTimeDetails, int mode);
    }

    public ReadTimeViewAdapter(Context mContext, OnStatClickHandler mClickHandler, int mMode, List<ReadTimeDetails> mReadTimeList) {
        this.mContext = mContext;
        this.mClickHandler = mClickHandler;
        this.mMode = mMode;
        this.mReadTimeList = mReadTimeList;
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.top_stats_list_item, parent, false);
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        ReadTimeDetails timeDetails = mReadTimeList.get(position);
        if(timeDetails.name.length() > 15) {
            holder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        } else {
            holder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        }

        holder.name.setText(timeDetails.name);
        double time;
        String unit;

        if(timeDetails.duration >= 3600){
            time = (timeDetails.duration * 1.0 ) / 3600.0;
            time = Math.round(time*10)/10.0;
            unit = "hr";
        } else {
            time = (timeDetails.duration * 1.0) / 60.0;
            time = Math.round(time*10)/10.0;
            unit = "mn";
        }
        holder.readTime.setText(String.valueOf(time));
        holder.timeUnit.setText(unit);
    }

    @Override
    public int getItemCount() {
        if(mReadTimeList == null) return 0;
        return mReadTimeList.size();
    }

    public class TimeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ConstraintLayout container;
        TextView name;
        TextView readTime;
        TextView timeUnit;

        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.top_stat_cont);
            name = itemView.findViewById(R.id.stat_name);
            readTime = itemView.findViewById(R.id.study_hr);
            timeUnit = itemView.findViewById(R.id.label_hr);

            switch (mMode){
                case MODE_STUDENT:
                    container.setBackgroundColor(mContext.getResources().getColor(R.color.colorMagenta));
                    break;

                case MODE_MATERIAL:
                    container.setBackgroundColor(mContext.getResources().getColor(R.color.colorMaterial));
                    break;
            }

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = getAdapterPosition();
            mClickHandler.onTimeClicked(mReadTimeList.get(id), mMode);
        }
    }
}
