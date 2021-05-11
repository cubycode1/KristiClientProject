package com.domain.gems.quizmodule;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.domain.gems.Configs;
import com.domain.gems.GemsMap;
import com.domain.gems.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {

    List<ParseObject> quizData = null;
    ParseObject quizObject;
    ArrayList<QuizOptionsModel> quizOptionsList = new ArrayList<>();
    ArrayList<QuizAnswersModel> quizAnswersList = new ArrayList<>();
    ArrayList<String> quizQuestionsArray = new ArrayList<>();
    RecyclerView rvQuiz;
    RecyclerViewAdapterQuiz recyclerViewAdapterQuiz;
    String optionOne,optionTwo,optionThree,optionFour;
    JSONArray optionJsonArray;
    JSONArray answersJsonArray;
    String  question;
    String correctOption;
    Button submitBtn;
    int userPoints;
    int gemsCollected;
    int userCollectedPoints;
    int userCollectedGems;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){ getSupportActionBar().hide(); }
        setContentView(R.layout.activity_quiz);
        init();
        queryQuestionAnswers();
    }

    private void init(){
        rvQuiz = findViewById(R.id.rv_quiz);
        submitBtn = findViewById(R.id.btn_submit_quiz);
        submitBtn.setOnClickListener(this);
    }

    void setAdapter(Context context, ArrayList<QuizModel> reportsList){
        recyclerViewAdapterQuiz = new RecyclerViewAdapterQuiz(context,reportsList);
        rvQuiz.setHasFixedSize(true);
        rvQuiz.setLayoutManager(new LinearLayoutManager(this));
        rvQuiz.setAdapter(recyclerViewAdapterQuiz);
    }

    void queryQuestionAnswers(){
        DataList.quizDataList.clear();
        Configs.showPD("Please wait...",QuizActivity.this);
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.QUIZ_CLASS_NAME);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    Configs.hidePD();
                    quizData = objects;
                    for (int position=0; position<quizData.size(); position++){
                        objects.get(position);
                        quizObject = quizData.get(position);
                        question = quizObject.getString(Configs.QUIZ_QUESTION);
                        optionJsonArray = quizObject.getJSONArray(Configs.QUIZ_OPTIONS);
                        answersJsonArray = quizObject.getJSONArray(Configs.QUIZ_CORRECT_ANSWERS);
                        try {
                        quizQuestionsArray.add(question);
                        quizAnswersList.add(new QuizAnswersModel(quizObject.getJSONArray(Configs.QUIZ_CORRECT_ANSWERS).get(0).toString()));
                            Log.i("answers_list_size", quizAnswersList.size()+"");
                            if (optionJsonArray.length() == 1){
                                quizOptionsList.add(new QuizOptionsModel(quizObject.getJSONArray(Configs.QUIZ_OPTIONS).get(0).toString(),
                                        null,null,null));
                            }else if (optionJsonArray.length() == 2){
                                quizOptionsList.add(new QuizOptionsModel(quizObject.getJSONArray(Configs.QUIZ_OPTIONS).get(0).toString(),
                                        quizObject.getJSONArray(Configs.QUIZ_OPTIONS).get(1).toString(),null,null));
                            }else if (optionJsonArray.length() == 3){
                                quizOptionsList.add(new QuizOptionsModel(quizObject.getJSONArray(Configs.QUIZ_OPTIONS).get(0).toString(),
                                        quizObject.getJSONArray(Configs.QUIZ_OPTIONS).get(1).toString(),
                                        quizObject.getJSONArray(Configs.QUIZ_OPTIONS).get(2).toString(),null));
                            }else if (optionJsonArray.length() == 4){
                                quizOptionsList.add(new QuizOptionsModel(quizObject.getJSONArray(Configs.QUIZ_OPTIONS).get(0).toString(),
                                        quizObject.getJSONArray(Configs.QUIZ_OPTIONS).get(1).toString(),
                                        quizObject.getJSONArray(Configs.QUIZ_OPTIONS).get(2).toString(),
                                        quizObject.getJSONArray(Configs.QUIZ_OPTIONS).get(3).toString()));
                            }

                            Log.i("options_list_size", quizOptionsList.size()+"");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i("json_error",e.getMessage());
                        }
                            optionOne = quizOptionsList.get(position).getOptionOne();
                            optionTwo = quizOptionsList.get(position).getOptionTwo();
                            optionThree = quizOptionsList.get(position).getOptionThree();
                            optionFour = quizOptionsList.get(position).getOptionFour();
                            ParseObject parseObject = quizData.get(position);
                            DataList.quizDataList.add(new QuizModel(parseObject,question,optionOne,optionTwo,optionThree,optionFour,correctOption));
                            Log.i("list_size",DataList.quizDataList.size()+"");
                            setAdapter(QuizActivity.this,DataList.quizDataList);
                    }
                }else {
                    Configs.hidePD();
                    Toast.makeText(QuizActivity.this, ""+error.getMessage(), LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_submit_quiz:
                readObject();
                break;
        }
    }

    public void readObject() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.USER_CLASS_NAME);
        // The query will search for a ParseObject, given its objectId.
        // When the query finishes running, it will invoke the GetCallback
        // with either the object, or the exception thrown
        query.getInBackground(ParseUser.getCurrentUser().getObjectId(), (object, e) -> {
            if (e == null) {
                userCollectedPoints = object.getInt("points");
                userCollectedGems = object.getInt("gemsCollected");
                makeQuizResult(userCollectedPoints,userCollectedGems);
                //Object was successfully retrieved
            } else {
                // something went wrong
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void makeQuizResult(int userCollectedPoints, int userCollectedGems){
        int totalMarks = quizAnswersList.size();
        int gainedScore = 0;
        if (RecyclerViewAdapterQuiz.selectedAnswersListByUser.size() != quizAnswersList.size()){
            makeText(this, "Please attempt all questions.", LENGTH_SHORT).show();
        }else {
        for (int position = 0; position<totalMarks; position++) {
            if (RecyclerViewAdapterQuiz.selectedAnswersListByUser.get(position).equals(quizAnswersList.get(position).getAnswer())) {
                gainedScore++;
            }
        }
        }
        ParseUser currUser = ParseUser.getCurrentUser();
        userPoints = currUser.getInt(Configs.USER_POINTS);
        gemsCollected = currUser.getInt(Configs.USER_GEMS_COLLECTED);
        ParseGeoPoint quizLocation = new ParseGeoPoint(GemsMap.currentLocation.getLatitude(),GemsMap.currentLocation.getLongitude());
        ParseUser currentUser = ParseUser.getCurrentUser();
        int totalScore = userCollectedPoints+gainedScore;
        int totalGemsCollected = userCollectedGems+1;
        uploadQuizData(quizLocation,GemsMap.quizTitle,currentUser,totalScore,totalGemsCollected);
    }

    void uploadQuizData(ParseGeoPoint quizLatLng, String quizName, ParseUser student_team_id, int totalScore,int totalGemsCollected){
        Configs.showPD("Please wait...",QuizActivity.this);
        // Configure Query
        ParseObject parseObject = new ParseObject(Configs.COLLECTED_CLASS_NAME);
        // Store an object
        parseObject.put(Configs.QUIZ_LAT_LNG,quizLatLng);
        parseObject.put(Configs.QUIZ_NAME, quizName);
        parseObject.put(Configs.STUDENT_TEAM_ID, student_team_id);
        parseObject.put(Configs.QUIZ_SCORE,""+totalScore);
        // Saving object
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // Success
                    Configs.hidePD();
                    updateUserData(totalScore,totalGemsCollected);
//                    makeText(QuizActivity.this, "Quiz Submitted.", LENGTH_SHORT).show();
                } else {
                    // Error
                    Configs.hidePD();
                    Toast.makeText(QuizActivity.this, ""+e.getMessage(), LENGTH_SHORT).show();
                }
            }
        });
    }

    void updateUserData(int quizScore,int gemsCollected){
        Configs.showPD("Please wait...",QuizActivity.this);
        // Configure Query
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(Configs.USER_CLASS_NAME);
        String currentUserID = ParseUser.getCurrentUser().getObjectId();
        // Retrieve the object by id
        parseQuery.getInBackground(currentUserID, new GetCallback<ParseObject>() {
            public void done(ParseObject player, ParseException e) {
                if (e == null) {
                    // Now let's update it with some new data. In this case, only cheatMode and score
                    // will get sent to the Parse Cloud. playerName hasn't changed.
                    // Store an object
                    player.put("points",quizScore);
                    player.put("gemsCollected", gemsCollected);
                    player.saveInBackground();
                    Configs.hidePD();
                    makeText(QuizActivity.this, "Quiz Submitted.", LENGTH_SHORT).show();
                    finish();
                } else {
                    // Failed
                    // Error
                    Configs.hidePD();
                    Toast.makeText(QuizActivity.this, ""+e.getMessage(), LENGTH_SHORT).show();
                }
            }
        });
    }


}
