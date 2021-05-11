package com.domain.gems.commentmodule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.domain.gems.R;
import com.domain.gems.quizmodule.QuizModel;

import java.util.ArrayList;

public class RecyclerViewAdapterComments extends RecyclerView.Adapter<RecyclerViewAdapterComments.QuizViewHolder> {

    Context context;
    ArrayList<CommentsModel> commentsList;
//    public static ArrayList<String> selectedAnswersListByUser = new ArrayList<>();

    public RecyclerViewAdapterComments(Context context, ArrayList<CommentsModel> reportList) {
        this.context = context;
        this.commentsList = reportList;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.cell_comment, parent, false);
        QuizViewHolder reportsViewHolder = new QuizViewHolder(listItem);
        return reportsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, final int position) {



        if (commentsList.get(position).getCommentStatus().equals("Not Approved")){
            holder.rlComment.setVisibility(View.GONE);
        }else {
            holder.rlComment.setVisibility(View.VISIBLE);
            holder.tvComment.setText(""+commentsList.get(position).getComment());
        }

    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName;
        private TextView tvComment;
        private RelativeLayout rlComment;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.cCommUsernameTxt);
            tvComment = itemView.findViewById(R.id.cCommCommentTxt);
            rlComment = itemView.findViewById(R.id.rlComment);
        }
    }

}
