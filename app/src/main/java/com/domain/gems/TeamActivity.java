package com.domain.gems;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseUser;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class TeamActivity extends AppCompatActivity {

    private EditText etTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);

        etTeam = findViewById(R.id.etTeam);


        findViewById(R.id.bSubmitTeam).setOnClickListener(v -> {
            Configs.showPD("Please Wait...", TeamActivity.this);
            ParseObject entity = new ParseObject("TeamsAdded");

            entity.put("team_title", etTeam.getText().toString());
//            entity.put("team_category", "A string");
            entity.put("adminId", ParseUser.getCurrentUser());
//            entity.put("userPointer", ParseUser.getCurrentUser());

            // Saves the new object.
            // Notice that the SaveCallback is totally optional!
            entity.saveInBackground(e -> {
                if (e == null) {
                    //Save was done
                    Configs.hidePD();
                    etTeam.setText("");
                    makeText(this, "Team Added Successfully", LENGTH_SHORT).show();
                } else {
                    //Something went wrong
                    Configs.hidePD();
                    makeText(this, e.getMessage(), LENGTH_SHORT).show();
                }
            });
        });
    }
}