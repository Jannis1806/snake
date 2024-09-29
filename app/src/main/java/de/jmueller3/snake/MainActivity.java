package de.jmueller3.snake;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.SQLDataException;

import de.jmueller3.snake.R;

public class MainActivity extends AppCompatActivity {
    private GameController gameController;
    private ControlView controlView;
    private HeaderView headerView;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout gameContainer = findViewById(R.id.gameContainer);
        controlView = findViewById(R.id.controlView);
        headerView = findViewById(R.id.headerView);

        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.open();
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }

        gameController = new GameController(this, gameContainer, controlView);
        controlView.setGameController(gameController);
        headerView.setGameController(gameController);

        gameController.setOnGameOverListener(new GameController.OnGameOverListener() {
            @Override
            public void onGameOver(int points) {
                showGameOverDialog(points);
            }
        });
    }

    private void showGameOverDialog(int points) {
        // Alle Timer ausblenden
        headerView.setBoostTimer(0);
        headerView.setWallTimer(0);
        headerView.setInvertControlTimer(0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over");
        builder.setMessage("Deine Punkte: " + points);
        builder.setCancelable(false); // Popup kann nur mit den Buttons verlassen werden

        builder.setPositiveButton("Neues Spiel", (dialog, which) -> {
            gameController.resetGame();
        });

        builder.setNegativeButton("Highscores", (dialog, which) -> {
            Intent intent = new Intent(MainActivity.this, HighscoreActivity.class);
            intent.putExtra("points", points);
            startActivity(intent);
        });

        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseManager.close();
    }
}
