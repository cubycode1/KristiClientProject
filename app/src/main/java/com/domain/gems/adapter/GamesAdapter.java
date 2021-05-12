package com.domain.gems.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.domain.gems.R;
import com.domain.gems.data.Game;
import com.domain.gems.data.Team;
import com.domain.gems.interfaces.RecyclerviewClickListener;

import java.util.ArrayList;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.ViewHolder> {
    private ArrayList<Game> listdata = new ArrayList<>(0);
    private RecyclerviewClickListener listener;

    // RecyclerView recyclerView;
    public GamesAdapter(RecyclerviewClickListener listener) {
        this.listener = listener;
    }

    public void setList(ArrayList<Game> list) {
        this.listdata = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.cell_gem, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Game item = listdata.get(position);
        holder.gameName.setText(item.getGameName());
        holder.relativeLayout.setOnClickListener((View.OnClickListener) view -> {
            listener.onClickListItem(position);
        });

    }

    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView gameName;
        public RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.gameName = (TextView) itemView.findViewById(R.id.cgGemNametxt);
            this.relativeLayout = (RelativeLayout) itemView.findViewById(R.id.rlContainer);
        }
    }
}
