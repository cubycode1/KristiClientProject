package com.domain.gems;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.domain.gems.adapter.UserAdapter;
import com.domain.gems.data.Team;
import com.domain.gems.data.User;
import com.domain.gems.interfaces.RecyclerviewClickListener;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;

import static android.util.Log.d;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class ListMembersActivity extends AppCompatActivity implements RecyclerviewClickListener {


    private Button button;

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private ArrayList<User> userArrayList = new ArrayList<>();

    private ParseObject team;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_members);

        button = findViewById(R.id.bAddMember);

        team = getIntent().getExtras().getParcelable("team");

        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMemberActivity.class);
            intent.putExtra("team", team);
            startActivityForResult(intent, 1112);
        });

        recyclerView = findViewById(R.id.rvMembers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter(this, true);
        recyclerView.setAdapter(adapter);


        listMembersOfSpecificTeam(team);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1112) {
            userArrayList.clear();
            listMembersOfSpecificTeam(team);
        }
    }

    private void listMembersOfSpecificTeam(ParseObject team) {
        Configs.showPD("Please Wait...", ListMembersActivity.this);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TeamMembers");
        query.addAscendingOrder("userPointer");
        query.whereEqualTo("teamId", team);
        query.findInBackground((members, e) -> {
            if (e == null) {
                //Object was successfully retrieved
                for (ParseObject mem : members) {
                    String name = mem.getString("username");
                    User user = new User();
                    user.setUserName(name);
                    user.setParseObject(mem);
                    userArrayList.add(user);
                }
                if (userArrayList.size() > 0) {
                    adapter.setList(userArrayList);
                }
                Configs.hidePD();
            } else {
                // something went wrong
                Configs.hidePD();
                makeText(this, e.getMessage(), LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClickListItem(int position) {
        deleteMember(userArrayList.get(position).getParseObject().getObjectId(), position);
    }

    private void deleteMember(String objectID, int position) {
        Configs.showPD("Deleting",ListMembersActivity.this);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TeamMembers");
        query.getInBackground(objectID, (object, e) -> {
            if (e == null) {
                //Object was fetched
                //Deletes the fetched ParseObject from the database
                object.deleteInBackground(e2 -> {
                    if (e2 == null) {
                        userArrayList.remove(position);
                        adapter.setList(userArrayList);
                        Configs.hidePD();
                        Toast.makeText(this, "Delete Successful", Toast.LENGTH_SHORT).show();
                    } else {
                        //Something went wrong while deleting the Object
                        Configs.hidePD();
                        Toast.makeText(this, "Error: " + e2.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                //Something went wrong
                Configs.hidePD();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}