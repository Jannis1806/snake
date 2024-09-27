package de.kampmann.sensor2;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.SQLDataException;
import java.util.ArrayList;

public class HighscoreActivity extends AppCompatActivity {
    private DatabaseManager databaseManager;
    private ArrayList<String> highscoreList;
    private ArrayAdapter<String> adapter;
    private int newScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);

        ListView listView = findViewById(R.id.highscoreListView);
        Button newGameButton = findViewById(R.id.newGameButton);

        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.open();
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }

        highscoreList = new ArrayList<>();
        Cursor cursor = databaseManager.fetchTopFive();
        int playerNameIndex = cursor.getColumnIndex(DatabaseHelper.PLAYER_NAME);
        int scoreIndex = cursor.getColumnIndex(DatabaseHelper.SCORE);
        if (cursor.moveToFirst()) {
            int position = 1;
            do {
                String playerName = cursor.getString(playerNameIndex);
                int score = cursor.getInt(scoreIndex);
                highscoreList.add(position + ". " + playerName + ": " + score + " Punkte");
                position++;
            } while (cursor.moveToNext());
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, highscoreList);
        listView.setAdapter(adapter);

        newScore = getIntent().getIntExtra("points", 0);
        if (newScore > 0 && (highscoreList.size() < 5 || newScore > Integer.parseInt(highscoreList.get(highscoreList.size() - 1).split(": ")[1].split(" ")[0]))) {
            showNameInputDialog(newScore);
        }

        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HighscoreActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showNameInputDialog(int score) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Highscore!");

        final EditText input = new EditText(this);
        input.setHint("Enter your name");
        builder.setView(input);
        builder.setCancelable(false); // Popup kann nur mit den Buttons verlassen werden

        builder.setPositiveButton("OK", (dialog, which) -> {
            String playerName = input.getText().toString();
            databaseManager.insert(playerName, score);
            updateHighscoreList();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateHighscoreList() {
        highscoreList.clear();
        Cursor cursor = databaseManager.fetchTopFive();
        int playerNameIndex = cursor.getColumnIndex(DatabaseHelper.PLAYER_NAME);
        int scoreIndex = cursor.getColumnIndex(DatabaseHelper.SCORE);
        if (cursor.moveToFirst()) {
            int position = 1;
            do {
                String playerName = cursor.getString(playerNameIndex);
                int score = cursor.getInt(scoreIndex);
                highscoreList.add(position + ". " + playerName + ": " + score + " Punkte");
                position++;
            } while (cursor.moveToNext());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseManager.close();
    }
}
