package de.jmueller3.snake;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HeaderView extends LinearLayout {
    private TextView pointsTextView;
    private TextView boostTimerTextView;
    private TextView wallTimerTextView;
    private TextView invertControlTimerTextView;
    private GameController gameController;

    public HeaderView(Context context) {
        super(context);
        init(context);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.header_view, this, true);

        pointsTextView = findViewById(R.id.points);
        boostTimerTextView = findViewById(R.id.boostTimer);
        wallTimerTextView = findViewById(R.id.wallTimer);
        invertControlTimerTextView = findViewById(R.id.invertControlTimer);
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        this.gameController.setOnPointsChangeListener(new GameController.OnPointsChangeListener() {
            @Override
            public void onPointsChange(int points) {
                setPoints(points);
            }
        });

        this.gameController.setOnBoostTimerChangeListener(new GameController.OnBoostTimerChangeListener() {
            @Override
            public void onBoostTimerChange(long remainingTime) {
                setBoostTimer(remainingTime);
            }
        });

        this.gameController.setOnWallTimerChangeListener(new GameController.OnWallTimerChangeListener() {
            @Override
            public void onWallTimerChange(long remainingTime) {
                setWallTimer(remainingTime);
            }
        });

        this.gameController.setOnInvertControlTimerChangeListener(new GameController.OnInvertControlTimerChangeListener() {
            @Override
            public void onInvertControlTimerChange(long remainingTime) {
                setInvertControlTimer(remainingTime);
            }
        });

        this.gameController.addOnGameOverListener(new GameController.OnGameOverListener() {
            @Override
            public void onGameOver(int points) {
                setBoostTimer(0);
                setWallTimer(0);
                setInvertControlTimer(0);
            }
        });
    }

    public void setPoints(int points) {
        pointsTextView.setText("Punkte: " + points);
    }

    public void setBoostTimer(long remainingTime) {
        if (remainingTime > 0) {
            int seconds = (int) (remainingTime / 1000);
            int tenthSeconds = (int) (remainingTime / 100) - 10 * seconds;
            boostTimerTextView.setText("Boost: " + seconds + "," + tenthSeconds + "s");
            boostTimerTextView.setVisibility(View.VISIBLE);
        } else {
            boostTimerTextView.setVisibility(View.GONE);
        }
    }

    public void setWallTimer(long remainingTime) {
        if (remainingTime > 0) {
            int seconds = (int) (remainingTime / 1000);
            int tenthSeconds = (int) (remainingTime / 100) - 10 * seconds;
            wallTimerTextView.setText("Wand: " + seconds + "," + tenthSeconds + "s");
            wallTimerTextView.setVisibility(View.VISIBLE);
        } else {
            wallTimerTextView.setVisibility(View.GONE);
        }
    }

    public void setInvertControlTimer(long remainingTime) {
        if (remainingTime > 0) {
            int seconds = (int) (remainingTime / 1000);
            int tenthSeconds = (int) (remainingTime / 100) - 10 * seconds;
            invertControlTimerTextView.setText("Invertiert: " + seconds + "," + tenthSeconds + "s");
            invertControlTimerTextView.setVisibility(View.VISIBLE);
        } else {
            invertControlTimerTextView.setVisibility(View.GONE);
        }
    }
}
