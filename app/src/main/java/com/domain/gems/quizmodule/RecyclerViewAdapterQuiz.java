package com.domain.gems.quizmodule;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.domain.gems.R;
import com.domain.gems.commentmodule.ViewCommentActivity;
import com.parse.ParseObject;

import java.util.ArrayList;

public class RecyclerViewAdapterQuiz extends RecyclerView.Adapter<RecyclerViewAdapterQuiz.QuizViewHolder> {

    Context context;
    ArrayList<QuizModel> quizList;
    public static ArrayList<String> selectedAnswersListByUser = new ArrayList<>();
    public static ParseObject questionParseObject;

    public RecyclerViewAdapterQuiz(Context context, ArrayList<QuizModel> reportList) {
        this.context = context;
        this.quizList = reportList;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.adapter_quiz, parent, false);
        QuizViewHolder reportsViewHolder = new QuizViewHolder(listItem);
        return reportsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, final int position) {

        holder.tvQuestionNumber.setText(""+(position+1));
        holder.tvQuestion.setText("Q."+(position+1)+": "+quizList.get(position).getQuestion());
        holder.rbOptionOne.setText(""+quizList.get(position).getOptionOne());
        holder.rbOptionTwo.setText(""+quizList.get(position).getOptionTwo());
        holder.rbOptionThree.setText(""+quizList.get(position).getOptionThree());
        holder.rbOptionFour.setText(""+quizList.get(position).getOptionFour());

        if (quizList.get(position).getOptionOne() == null){
            holder.rbOptionOne.setVisibility(View.GONE);
        }else if (quizList.get(position).getOptionTwo() == null){
            holder.rbOptionTwo.setVisibility(View.GONE);
            holder.rbOptionThree.setVisibility(View.GONE);
            holder.rbOptionFour.setVisibility(View.GONE);
        }else if (quizList.get(position).getOptionThree() == null){
            holder.rbOptionThree.setVisibility(View.GONE);
            holder.rbOptionFour.setVisibility(View.GONE);
        }else if (quizList.get(position).getOptionFour() == null){
            holder.rbOptionFour.setVisibility(View.GONE);
        }

        holder.btnPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewCommentActivity.class);
                ParseObject parseObject = quizList.get(position).getParseObject();
                intent.putExtra("questionID",parseObject);
                questionParseObject = parseObject;
                view.getContext().startActivity(intent);
            }
        });

        holder.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonID);
                int idx = radioGroup.indexOfChild(radioButton);
                RadioButton r = (RadioButton) radioGroup.getChildAt(idx);
                String selectedText = r.getText().toString();
                selectedAnswersListByUser.add(selectedText);
            }
        });
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        private TextView tvQuestionNumber;
        private TextView tvQuestion;
        private RadioButton rbOptionOne;
        private RadioButton rbOptionTwo;
        private RadioButton rbOptionThree;
        private RadioButton rbOptionFour;
        private RadioGroup radioGroup;
        private RadioButton radioButton;
        private Button btnPostComment;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionNumber = itemView.findViewById(R.id.tv_question_number);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            rbOptionOne = itemView.findViewById(R.id.radio_btn_option_1);
            rbOptionTwo = itemView.findViewById(R.id.radio_btn_option_2);
            rbOptionThree = itemView.findViewById(R.id.radio_btn_option_3);
            rbOptionFour = itemView.findViewById(R.id.radio_btn_option_4);
            radioGroup = itemView.findViewById(R.id.radio_group_quiz);
            btnPostComment = itemView.findViewById(R.id.btn_view_comment);
            int radioButtonID = radioGroup.getCheckedRadioButtonId();
            radioButton = (RadioButton) radioGroup.findViewById(radioButtonID);
        }
    }

}
