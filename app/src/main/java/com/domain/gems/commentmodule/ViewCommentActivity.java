package com.domain.gems.commentmodule;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.domain.gems.Configs;
import com.domain.gems.R;
import com.domain.gems.quizmodule.DataList;
import com.domain.gems.quizmodule.RecyclerViewAdapterQuiz;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class ViewCommentActivity extends AppCompatActivity {

    /* Views */
    TextView adTitleTxt;
    EditText commEditText;
    ListView commListView;
    List<ParseObject> commentsData = null;
    ParseObject commentsObject;
    /* Variables */
    String comment;
    String commentStatus;
    RecyclerView rvComments;
    RecyclerViewAdapterComments recyclerViewAdapterComments;
    Button btnSendComment;
    ParseObject questionParseObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewcomment);
        if (getSupportActionBar() != null){ getSupportActionBar().hide(); }
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


//        Intent getIntentData = getIntent();
        questionParseObject = RecyclerViewAdapterQuiz.questionParseObject;


        init();
        // Call query
        queryComments();

    }// end onCreate()

    void init(){
        btnSendComment = findViewById(R.id.commSendButt);
        rvComments = findViewById(R.id.commListView);
        commEditText = findViewById(R.id.commCommentEditText);

        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (commEditText.getText().toString().equals("")){
                    Toast.makeText(ViewCommentActivity.this, "Please type your comment.", LENGTH_SHORT).show();
                }else {
                    uploadComment(commEditText.getText().toString(), questionParseObject);
                }
            }
        });
    }

    void setAdapter(Context context, ArrayList<CommentsModel> comments){
        recyclerViewAdapterComments = new RecyclerViewAdapterComments(context,comments);
        rvComments.setHasFixedSize(true);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(recyclerViewAdapterComments);
    }

    void queryComments(){
        DataList.commentDataList.clear();
        Configs.showPD("Please wait...", ViewCommentActivity.this);
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.COMMENTS_CLASS_NAME);
//        query.whereEqualTo("objectId",questionParseObject.getObjectId());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    Configs.hidePD();
                    commentsData = objects;
                    for (int position = 0; position< commentsData.size(); position++){
                        commentsObject = commentsData.get(position);
                        comment = commentsObject.getString(Configs.COMMENTS_COMMENT);
                        commentStatus = commentsObject.getString(Configs.COMMENTS_STATUS);
                        DataList.commentDataList.add(new CommentsModel(comment,commentStatus));
                        Log.i("list_size",DataList.commentDataList.size()+"");
                        setAdapter(ViewCommentActivity.this,DataList.commentDataList);
                    }
                }else {
                    Configs.hidePD();
                    Toast.makeText(ViewCommentActivity.this, ""+error.getMessage(), LENGTH_SHORT).show();
                }
            }
        });
    }


    void uploadComment(String commentStr, ParseObject questionID){
        ParseUser currentUser = ParseUser.getCurrentUser();
        Configs.showPD("Please wait...", ViewCommentActivity.this);
        // Configure Query
        ParseObject parseObject = new ParseObject(Configs.COMMENTS_CLASS_NAME);
        // Store an object
        parseObject.put(Configs.COMMENTS_COMMENT,commentStr);
        parseObject.put(Configs.COMMENTS_STATUS,"Not Approved");
        parseObject.put(Configs.COMMENTS_FK_USER_ID, currentUser);
        parseObject.put(Configs.COMMENTS_QUESTION_ID,questionID);
        // Saving object
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // Success
                    Configs.hidePD();
                    queryComments();
                } else {
                    // Error
                    Configs.hidePD();
                    Toast.makeText(ViewCommentActivity.this, ""+e.getMessage(), LENGTH_SHORT).show();
                }
            }
        });
    }


//    // MARK: - QUERY COMMENTS --------------------------------------------------------------------
//    void queryComments() {
////        Configs.showPD(getString(R.string.progress_dialog_loading), Comments.this);
//
//        ParseQuery<ParseObject> query = ParseQuery.getQuery(Configs.COMMENTS_CLASS_NAME);
////        query.whereEqualTo(Configs.COMMENTS_AD_POINTER, adObj);
////        query.orderByDescending(Configs.COMMENTS_CREATED_AT);
//        query.findInBackground(new FindCallback<ParseObject>() {
//            public void done(List<ParseObject> objects, ParseException error) {
//                if (error == null) {
//                    commentsArray = objects;
//                    Configs.hidePD();
//
//                    // CUSTOM LIST ADAPTER
//                    class ListAdapter extends BaseAdapter {
//                        private Context context;
//
//                        public ListAdapter(Context context, List<ParseObject> objects) {
//                            super();
//                            this.context = context;
//                        }
//
//                        // CONFIGURE CELL
//                        @Override
//                        public View getView(int position, View cell, ViewGroup parent) {
//                            if (cell == null) {
//                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                                assert inflater != null;
//                                cell = inflater.inflate(R.layout.cell_comment, null);
//                            }
//                            final View finalCell = cell;
//
//                            // Get Parse object
//                            final ParseObject cObj = commentsArray.get(position);
//
//                            // Get userPointer
////                            cObj.getParseObject(Configs.COMMENTS_USER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
////                                public void done(ParseObject userPointer, ParseException e) {
////
////                                    // Get username
////                                    TextView uTxt = finalCell.findViewById(R.id.cCommUsernameTxt);
//////                                    uTxt.setTypeface(Configs.titSemibold);
////                                    uTxt.setText(userPointer.getString(Configs.USER_USERNAME));
////
////                                    // Get avatar
//////                                    final ImageView anImage = finalCell.findViewById(R.id.cCommAvatarImg);
//////                                    Configs.getParseImage(anImage, userPointer, Configs.USER_AVATAR);
////
////
////                                    // Get comment
////                                    TextView commTxt = finalCell.findViewById(R.id.cCommCommentTxt);
//////                                    commTxt.setTypeface(Configs.titRegular);
////                                    commTxt.setText(cObj.getString(Configs.COMMENTS_COMMENT));
////
////                                    // Get date
//////                                    TextView dateTxt = finalCell.findViewById(R.id.cCommDateTxt);
////////                                    dateTxt.setTypeface(Configs.titRegular);
//////                                    dateTxt.setText(Configs.timeAgoSinceDate(cObj.getCreatedAt()));
////                                }
////                            });// end userPointer
//
//                            return cell;
//                        }
//
//                        @Override
//                        public int getCount() {
//                            return commentsArray.size();
//                        }
//
//                        @Override
//                        public Object getItem(int position) {
//                            return commentsArray.get(position);
//                        }
//
//                        @Override
//                        public long getItemId(int position) {
//                            return position;
//                        }
//                    }
//
//                    // Init ListView and set its adapter
//                    commListView.setAdapter(new ListAdapter(ViewCommentActivity.this, commentsArray));
//                    commListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//                            // Get Parse object
//                            final ParseObject cObj = commentsArray.get(position);
//
//                            // Get userPointer
////                            cObj.getParseObject(Configs.COMMENTS_USER_POINTER).fetchIfNeededInBackground(new GetCallback<ParseObject>() {
////                                public void done(ParseObject userPointer, ParseException e) {
////                                    /*
////                                    Intent i = new Intent(Comments.this, UserPorfile.class);
////                                    Bundle extras = new Bundle();
////                                    extras.putString("objectID", userPointer.getObjectId());
////                                    i.putExtras(extras);
////                                    startActivity(i);
////                                    */
//////                                }
////                            });// end userPointer
//
//                        }
//                    });
//
//                    // Error in query
//                } else {
////                    Configs.hidePD();
////                    Configs.simpleAlert(error.getMessage(), Comments.this);
//                }
//            }
//        });
//    }
//
//    // MARK: - DISMISS KEYBOARD
//    public void dismissKeyboard() {
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(commEditText.getWindowToken(), 0);
//    }
}//@end