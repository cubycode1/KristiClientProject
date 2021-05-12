package com.domain.gems.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.domain.gems.R;
import com.domain.gems.data.Team;
import com.domain.gems.data.User;
import com.domain.gems.interfaces.RecyclerviewClickListener;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private ArrayList<User> listdata = new ArrayList<>(0);
    private boolean isDeleteButtonSHown = false;
    private RecyclerviewClickListener listener;

    // RecyclerView recyclerView;
    public UserAdapter(RecyclerviewClickListener listener, Boolean isDeleteButtonShow) {
        this.listener = listener;
        this.isDeleteButtonSHown = isDeleteButtonShow;
    }

    public void setList(ArrayList<User> list) {
//        this.listdata.clear();
        this.listdata = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.user_list_item, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final User item = listdata.get(position);
        holder.userName.setText(item.getUserName());
        if (isDeleteButtonSHown) {
            holder.delete.setVisibility(View.VISIBLE);
        } else {
            holder.delete.setVisibility(View.GONE);
        }
        if (isDeleteButtonSHown) {
            holder.delete.setOnClickListener(view -> {
                listener.onClickListItem(position);
            });
        } else {
            holder.relativeLayout.setOnClickListener((View.OnClickListener) view -> {
                listener.onClickListItem(position);
            });

        }

    }

    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        public Button delete;
        public RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.userName = (TextView) itemView.findViewById(R.id.ivUserName);
            this.delete = (Button) itemView.findViewById(R.id.bDelete);
            this.relativeLayout = (RelativeLayout) itemView.findViewById(R.id.rlContainer);
        }
    }
}
