package de.kampmann.sensor2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.SQLDataException;

public class MainActivity extends AppCompatActivity {
    private GameController gameController;
    private ControlView controlView;
    private TextView pointsTextView;
    private TextView boostTimerTextView;
    private TextView wallTimerTextView;
    private TextView invertControlTimerTextView;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout gameContainer = findViewById(R.id.gameContainer);
        controlView = findViewById(R.id.controlView);
        pointsTextView = findViewById(R.id.points);
        boostTimerTextView = findViewById(R.id.boostTimer);
        wallTimerTextView = findViewById(R.id.wallTimer);
        invertControlTimerTextView = findViewById(R.id.invertControlTimer);

        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.open();
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }

        gameController = new GameController(this, gameContainer, controlView);
        controlView.setGameController(gameController);

        gameController.setOnPointsChangeListener(new GameController.OnPointsChangeListener() {
            @Override
            public void onPointsChange(int points) {
                pointsTextView.setText("Punkte: " + points);
            }
        });

        gameController.setOnBoostTimerChangeListener(new GameController.OnBoostTimerChangeListener() {
            @Override
            public void onBoostTimerChange(long remainingTime) {
                if (remainingTime > 0) {
                    boostTimerTextView.setText("Boost: " + (remainingTime / 1000) + "s");
                    boostTimerTextView.setVisibility(View.VISIBLE);
                } else {
                    boostTimerTextView.setVisibility(View.GONE);
                }
            }
        });

        gameController.setOnWallTimerChangeListener(new GameController.OnWallTimerChangeListener() {
            @Override
            public void onWallTimerChange(long remainingTime) {
                if (remainingTime > 0) {
                    wallTimerTextView.setText("Wall: " + (remainingTime / 1000) + "s");
                    wallTimerTextView.setVisibility(View.VISIBLE);
                } else {
                    wallTimerTextView.setVisibility(View.GONE);
                }
            }
        });

        gameController.setOnInvertControlTimerChangeListener(new GameController.OnInvertControlTimerChangeListener() {
            @Override
            public void onInvertControlTimerChange(long remainingTime) {
                if (remainingTime > 0) {
                    invertControlTimerTextView.setText("Invert: " + (remainingTime / 1000) + "s");
                    invertControlTimerTextView.setVisibility(View.VISIBLE);
                } else {
                    invertControlTimerTextView.setVisibility(View.GONE);
                }
            }
        });

        gameController.setOnGameOverListener(new GameController.OnGameOverListener() {
            @Override
            public void onGameOver(int points) {
                showGameOverDialog(points);
            }
        });
    }

    private void showGameOverDialog(int points) {
        // Alle Timer ausblenden
        boostTimerTextView.setVisibility(View.GONE);
        wallTimerTextView.setVisibility(View.GONE);
        invertControlTimerTextView.setVisibility(View.GONE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over");
        builder.setMessage("Your score: " + points);
        builder.setCancelable(false); // Popup kann nur mit den Buttons verlassen werden

        builder.setPositiveButton("Restart", (dialog, which) -> {
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
