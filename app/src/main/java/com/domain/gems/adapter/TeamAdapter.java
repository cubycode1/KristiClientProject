package com.domain.gems.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.domain.gems.R;
import com.domain.gems.data.Team;
import com.domain.gems.interfaces.RecyclerviewClickListener;

import java.util.ArrayList;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.ViewHolder> {
    private ArrayList<Team> listdata = new ArrayList<>(0);
    private RecyclerviewClickListener listener;

    // RecyclerView recyclerView;
    public TeamAdapter(RecyclerviewClickListener listener) {
        this.listener = listener;
    }

    public void setList(ArrayList<Team> list) {
        this.listdata = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.team_list_item, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Team item = listdata.get(position);
        holder.teamName.setText(item.getTeamName());
        holder.relativeLayout.setOnClickListener((View.OnClickListener) view -> {
            listener.onClickListItem(position);
        });

    }

    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView teamName;
        public RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.teamName = (TextView) itemView.findViewById(R.id.ivTeamName);
            this.relativeLayout = (RelativeLayout) itemView.findViewById(R.id.rlContainer);
        }
    }
}
