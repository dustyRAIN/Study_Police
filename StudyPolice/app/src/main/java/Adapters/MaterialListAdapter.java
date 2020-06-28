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

import ModelClasses.MaterialDetails;

public class MaterialListAdapter extends RecyclerView.Adapter<MaterialListAdapter.MaterialViewHolder> {

    private Context mContext;
    private List<MaterialDetails> mMaterialList;
    private MaterialClickHandler mClickHandler;

    public interface MaterialClickHandler{
        void onMaterialClick(MaterialDetails materialDetails);
    }

    public MaterialListAdapter(Context mContext, List<MaterialDetails> mMaterialList, MaterialClickHandler mClickHandler) {
        this.mContext = mContext;
        this.mMaterialList = mMaterialList;
        this.mClickHandler = mClickHandler;
    }

    @NonNull
    @Override
    public MaterialListAdapter.MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.materials_list_item, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialListAdapter.MaterialViewHolder holder, int position) {
        MaterialDetails materialDetails = mMaterialList.get(position);

        holder.materialName.setText(materialDetails.name);
    }

    @Override
    public int getItemCount() {
        if(mMaterialList==null) return 0;
        return mMaterialList.size();
    }

    public class MaterialViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView materialName;

        public MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            materialName = itemView.findViewById(R.id.tv_material_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            mClickHandler.onMaterialClick(mMaterialList.get(pos));
        }
    }
}
