package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.foh.studypolice.R;

import java.util.List;

import ModelClasses.GeneralShoutDetails;

import static Utils.SPDateUtils.getAppropriateTimeDisplay;

public class ShoutViewAdapter extends RecyclerView.Adapter<ShoutViewAdapter.ShoutViewHolder> {

    private Context mContext;
    private List<GeneralShoutDetails> mShoutList;
    private OnShoutClickHandler mClickHandler;

    public interface OnShoutClickHandler {
        void onShoutClicked(GeneralShoutDetails shoutDetails);
    }

    public ShoutViewAdapter(Context mContext, List<GeneralShoutDetails> mShoutList, OnShoutClickHandler mClickHandler) {
        this.mContext = mContext;
        this.mShoutList = mShoutList;
        this.mClickHandler = mClickHandler;
    }

    @NonNull
    @Override
    public ShoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.shout_list_item, parent, false);
        return new ShoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoutViewHolder holder, int position) {
        GeneralShoutDetails shoutDetails = mShoutList.get(position);

        holder.title.setText(shoutDetails.classroom.name);
        holder.details.setText("A new file has been uploaded.");
        holder.time.setText(getAppropriateTimeDisplay(mContext, shoutDetails.time));

        if(shoutDetails.seen == 0){
            holder.shoutCont.setBackgroundColor(mContext.getResources().getColor(R.color.colorShoutBG));
            holder.title.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
            holder.details.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
            holder.time.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
        } else {
            holder.shoutCont.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
            holder.title.setTextColor(mContext.getResources().getColor(R.color.colorShoutBG));
            holder.details.setTextColor(mContext.getResources().getColor(R.color.colorShoutBG));
            holder.time.setTextColor(mContext.getResources().getColor(R.color.colorShoutBG));
        }
    }

    @Override
    public int getItemCount() {
        if(mShoutList == null) return 0;
        return mShoutList.size();
    }

    public class ShoutViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ConstraintLayout shoutCont;
        final TextView title;
        final TextView details;
        final TextView time;

        public ShoutViewHolder(@NonNull View itemView) {
            super(itemView);

            shoutCont = itemView.findViewById(R.id.shout_cont);
            title = itemView.findViewById(R.id.title);
            details = itemView.findViewById(R.id.details);
            time = itemView.findViewById(R.id.time);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = getAdapterPosition();
            mClickHandler.onShoutClicked(mShoutList.get(id));
        }
    }
}
