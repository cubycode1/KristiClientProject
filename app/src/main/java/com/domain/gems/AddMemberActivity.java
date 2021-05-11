package com.domain.gems;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.domain.gems.adapter.TeamAdapter;
import com.domain.gems.adapter.UserAdapter;
import com.domain.gems.data.Team;
import com.domain.gems.data.User;
import com.domain.gems.interfaces.RecyclerviewClickListener;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;

public class AddMemberActivity extends AppCompatActivity implements RecyclerviewClickListener {

    private EditText etSearch;

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private ArrayList<User> userArrayList = new ArrayList<>();

    private ParseObject team;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        team = getIntent().getExtras().getParcelable("team");

        etSearch = findViewById(R.id.etSearch);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                findUsers();
            }
        });

        recyclerView = findViewById(R.id.rvUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter(this, false);
        recyclerView.setAdapter(adapter);


        findUsers();

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    public void findUsers() {
        if (etSearch.getText().toString().equals("")) {
            Configs.showPD("Please Wait...", AddMemberActivity.this);
        }
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereStartsWith("username", etSearch.getText().toString());
        query.findInBackground((users, e) -> {
            if (e == null) {
                userArrayList.clear();
                for (ParseObject user : users) {
                    String name = user.getString("username");
                    User team1 = new User();
                    team1.setUserName(name);
                    team1.setParseObject(user);
                    userArrayList.add(team1);
                }
                if (userArrayList.size() > 0) {
                    adapter.setList(userArrayList);
                }
                Configs.hidePD();
            } else {
                // Something went wrong.
                Configs.hidePD();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMember(User user) {
        Configs.showPD("Please Wait...", AddMemberActivity.this);
        ParseObject entity = new ParseObject("TeamMembers");

        entity.put("teamId", team);
//        entity.put("adminId", ParseUser.getCurrentUser());
        entity.put("username", user.getParseObject().getString("username"));

        // Saves the new object.
        // Notice that the SaveCallback is totally optional!
        entity.saveInBackground(e -> {
            if (e == null) {
                //Save was done
                Configs.hidePD();
                Toast.makeText(this, "Team Member Added", Toast.LENGTH_SHORT).show();
            } else {
                //Something went wrong
                Configs.hidePD();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onClickListItem(int position) {
        addMember(userArrayList.get(position));
    }
}