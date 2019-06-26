package com.cursoandroid.mobile;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BalizaAdapter extends RecyclerView.Adapter<BalizaAdapter.ViewHolder> {

    private List<BalizaModel> balizaModelList;

    BalizaAdapter() {
        this.balizaModelList = new ArrayList<>();
    }

    public void addBaliza(BalizaModel balizaModel) {
        balizaModelList.add(balizaModel);
        notifyItemChanged(balizaModelList.size() - 1);
    }

    @NonNull
    @Override
    public BalizaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_baliza, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BalizaAdapter.ViewHolder viewHolder, int position) {
        viewHolder.tvName.setText(balizaModelList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return balizaModelList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvName;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onItemSelect != null) onItemSelect.balizaItemSelect(balizaModelList.get(getAdapterPosition()));
        }
    }

    private OnItemSelect onItemSelect;

    interface OnItemSelect {
        void balizaItemSelect(BalizaModel balizaModel);
    }

    public void setOnItemSelect(OnItemSelect onItemSelect) {
        this.onItemSelect = onItemSelect;
    }
}
