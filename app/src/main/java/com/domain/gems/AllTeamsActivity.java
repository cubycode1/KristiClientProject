package com.domain.gems;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.domain.gems.adapter.TeamAdapter;
import com.domain.gems.data.Team;
import com.domain.gems.interfaces.RecyclerviewClickListener;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class AllTeamsActivity extends AppCompatActivity implements RecyclerviewClickListener {

    private RecyclerView recyclerView;
    private TeamAdapter adapter;
    private ArrayList<Team> teamArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_teams);

        recyclerView = findViewById(R.id.rvTeams);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TeamAdapter(this);
        recyclerView.setAdapter(adapter);

        getTeams();

    }

    private void getTeams() {
        Configs.showPD("Please Wait...", AllTeamsActivity.this);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TeamsAdded");
        query.addAscendingOrder("team_title");
        query.whereEqualTo("adminId", ParseUser.getCurrentUser());
        query.findInBackground((teams, e) -> {
            if (e == null) {
                //Object was successfully retrieved
                for (ParseObject team : teams) {
                    String name = team.getString("team_title");
                    Team team1 = new Team();
                    team1.setTeamName(name);
                    team1.setParseObject(team);
                    teamArrayList.add(team1);
                }
                if (teamArrayList.size() > 0) {
                    adapter.setList(teamArrayList);
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
        Team team = teamArrayList.get(position);
        Intent intent = new Intent(this, ListMembersActivity.class);
        intent.putExtra("team", team.getParseObject());
        startActivity(intent);
    }
}