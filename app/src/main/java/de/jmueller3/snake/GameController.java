package de.jmueller3.snake;

import android.content.Context;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameController {
    private static final int NUM_BLOCKS = 15; // Anzahl der Blöcke in Breite und Höhe
    private static final int START_DELAY = 300; // Startverzögerung in Millisekunden
    private static final int MIN_DELAY = START_DELAY / 2; // Minimale Verzögerung
    private static final int DELAY_DECREMENT = 10; // Verringerung der Verzögerung pro Futter
    private static final double CHOCOLATE_SPEED_FACTOR = 1.5; // 50% schneller
    private static final int CHOCOLATE_SPEED_DURATION = 10000; // 10 Sekunden
    private static final int CANDY_WALL_DURATION = 20000; // 20 Sekunden
    private static final int LICORICE_INVERT_DURATION = 5000; // 5 Sekunden

    private Snake snake;
    private Food food;
    private List<Position> wall = new ArrayList<>();
    private boolean running = true;
    private boolean paused = true; // Spiel ist zu Beginn pausiert
    private int points = 0;
    private int currentDelay = START_DELAY; // Aktuelle Verzögerung
    private int previousDelay = START_DELAY; // Verzögerung vor Speedboost
    private boolean chocolateBoostActive = false; // Schokoladen-Boost aktiv
    private boolean wallActive = false; // Wand aktiv
    private boolean invertControlActive = false; // Steuerung invertiert
    private long chocolateBoostRemainingTime = 0;
    private long wallRemainingTime = 0;
    private long invertControlRemainingTime = 0;
    private long chocolateBoostEndTime = 0;
    private long wallEndTime = 0;
    private long invertControlEndTime = 0;
    private Handler handler;
    private Runnable gameLoop;
    private OnPointsChangeListener onPointsChangeListener;
    private OnPauseStatusChangeListener onPauseStatusChangeListener;
    private OnBoostTimerChangeListener onBoostTimerChangeListener;
    private OnWallTimerChangeListener onWallTimerChangeListener;
    private OnInvertControlTimerChangeListener onInvertControlTimerChangeListener;
    private OnGameOverListener onGameOverListener;
    private GameView gameView;
    private ControlView controlView;

    public GameController(Context context, ViewGroup parent, ControlView controlView) {
        this.controlView = controlView;
        gameView = new GameView(context);
        parent.addView(gameView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        gameView.setGameController(this);

        handler = new Handler();
        gameLoop = new Runnable() {
            @Override
            public void run() {
                handleGameTick();
                handler.postDelayed(this, currentDelay);
            }
        };
    }

    private void handleGameTick() {
        if (running && !paused) {
            snake.move();
            if (snake.isHeadOnFood(food.getPosition())) {
                handleFoodAction();
                newFood();
            } else {
                snake.shrink();
            }
            if (snake.checkCollision(NUM_BLOCKS, NUM_BLOCKS) || checkWallCollision()) {
                running = false;
                pauseGame();
                removeAllEventsAndTimers();
                if (onGameOverListener != null) {
                    onGameOverListener.onGameOver(points);
                }
            }
            gameView.invalidate();
        }
    }

    private void handleFoodAction() {
        switch (food.getType()) {
            case APPLE:
                points += 1;
                break;
            case BANANA:
                points += 5;
                break;
            default:
                points += 3;
                break;
        }
        if (onPointsChangeListener != null) {
            onPointsChangeListener.onPointsChange(points);
        }
        if (food.getType() == Food.FoodType.CHOCOLATE) {
            activateChocolateBoost();
        } else if (food.getType() == Food.FoodType.CANDY) {
            createWall();
        } else if (food.getType() == Food.FoodType.LICORICE) {
            activateInvertControl();
        } else {
            // Verzögerung verringern
            currentDelay = Math.max(MIN_DELAY, currentDelay - DELAY_DECREMENT);
        }
    }

    public void resumeGame() {
        paused = false;
        if (onPauseStatusChangeListener != null) {
            onPauseStatusChangeListener.onPauseStatusChange(false);
        }
        if (controlView != null) {
            controlView.invalidateReferenceValues();
        }
        // Timer-Callbacks wieder hinzufügen und verbleibende Zeit verwenden
        long currentTime = System.currentTimeMillis();
        if (chocolateBoostRemainingTime > 0) {
            chocolateBoostEndTime = currentTime + chocolateBoostRemainingTime;
            startChocolateBoostTimer(chocolateBoostRemainingTime);
        }
        if (wallRemainingTime > 0) {
            wallEndTime = currentTime + wallRemainingTime;
            startWallTimer(wallRemainingTime);
        }
        if (invertControlRemainingTime > 0) {
            invertControlEndTime = currentTime + invertControlRemainingTime;
            startInvertControlTimer(invertControlRemainingTime);
        }
        handler.post(gameLoop);
    }

    public void setDirection(char newDirection) {
        if (invertControlActive) {
            switch (newDirection) {
                case 'U':
                    newDirection = 'D';
                    break;
                case 'D':
                    newDirection = 'U';
                    break;
                case 'L':
                    newDirection = 'R';
                    break;
                case 'R':
                    newDirection = 'L';
                    break;
            }
        }
        snake.setDirection(newDirection);
    }

    private void newFood() {
        try {
            Random random = new Random();
            Position position;
            Food.FoodType type;
            boolean validFood = false;
            while (!validFood) {
                position = new Position(random.nextInt(NUM_BLOCKS), random.nextInt(NUM_BLOCKS));
                type = Food.FoodType.getRandomType();
                if ((chocolateBoostActive && type == Food.FoodType.CHOCOLATE) ||
                        (wallActive && type == Food.FoodType.CANDY) ||
                        (invertControlActive && type == Food.FoodType.LICORICE) ||
                        wall.contains(position) ||
                        snake.getBody().contains(position)) {
                    continue; // Überspringe Food, dessen Event noch aktiv ist, Positionen unter der Mauer und Positionen der Schlange
                }
                validFood = true;
                food = new Food(position, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void activateChocolateBoost() {
        if (!chocolateBoostActive) {
            chocolateBoostActive = true;
            previousDelay = currentDelay;
            currentDelay = Math.max(MIN_DELAY, (int) (currentDelay / CHOCOLATE_SPEED_FACTOR));
            chocolateBoostEndTime = System.currentTimeMillis() + CHOCOLATE_SPEED_DURATION;
            startChocolateBoostTimer(CHOCOLATE_SPEED_DURATION);
        }
    }

    private void createWall() {
        Random random = new Random();
        wall.clear();
        boolean validWall = false;
        while (!validWall) {
            int startX = random.nextInt(NUM_BLOCKS - 5);
            int startY = random.nextInt(NUM_BLOCKS - 5);
            boolean horizontal = random.nextBoolean();
            List<Position> newWall = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                if (horizontal) {
                    newWall.add(new Position(startX + i, startY));
                } else {
                    newWall.add(new Position(startX, startY + i));
                }
            }
            validWall = true;
            for (Position pos : newWall) {
                if (snake.getBody().contains(pos) || pos.equals(food.getPosition())) {
                    validWall = false;
                    break;
                }
            }
            if (validWall) {
                wall = newWall;
            }
        }
        wallActive = true;
        wallEndTime = System.currentTimeMillis() + CANDY_WALL_DURATION;
        startWallTimer(CANDY_WALL_DURATION);
    }

    private void activateInvertControl() {
        if (!invertControlActive) {
            invertControlActive = true;
            invertControlEndTime = System.currentTimeMillis() + LICORICE_INVERT_DURATION;
            startInvertControlTimer(LICORICE_INVERT_DURATION);
        }
    }

    private void startChocolateBoostTimer(long duration) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                chocolateBoostActive = false;
                currentDelay = previousDelay;
                if (onBoostTimerChangeListener != null) {
                    onBoostTimerChangeListener.onBoostTimerChange(0);
                }
            }
        }, duration);

        // Timer für die Anzeige der verbleibenden Zeit
        Runnable chocolateBoostTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (chocolateBoostActive) {
                    long remainingTime = chocolateBoostEndTime - System.currentTimeMillis();
                    if (onBoostTimerChangeListener != null) {
                        onBoostTimerChangeListener.onBoostTimerChange(remainingTime);
                    }
                    handler.postDelayed(this, 100);
                }
            }
        };
        handler.post(chocolateBoostTimerRunnable);
    }

    private void startWallTimer(long duration) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wall.clear();
                wallActive = false;
                if (onWallTimerChangeListener != null) {
                    onWallTimerChangeListener.onWallTimerChange(0);
                }
            }
        }, duration);

        // Timer für die Anzeige der verbleibenden Zeit der Wand
        Runnable wallTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (wallActive) {
                    long remainingTime = wallEndTime - System.currentTimeMillis();
                    if (onWallTimerChangeListener != null) {
                        onWallTimerChangeListener.onWallTimerChange(remainingTime);
                    }
                    handler.postDelayed(this, 100);
                }
            }
        };
        handler.post(wallTimerRunnable);
    }

    private void startInvertControlTimer(long duration) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                invertControlActive = false;
                if (onInvertControlTimerChangeListener != null) {
                    onInvertControlTimerChangeListener.onInvertControlTimerChange(0);
                }
            }
        }, duration);

        // Timer für die Anzeige der verbleibenden Zeit der invertierten Steuerung
        Runnable invertControlTimerRunnable = new Runnable() {
            @Override
            public void run() {
                if (invertControlActive) {
                    long remainingTime = invertControlEndTime - System.currentTimeMillis();
                    if (onInvertControlTimerChangeListener != null) {
                        onInvertControlTimerChangeListener.onInvertControlTimerChange(remainingTime);
                    }
                    handler.postDelayed(this, 100);
                }
            }
        };
        handler.post(invertControlTimerRunnable);
    }

    private boolean checkWallCollision() {
        Position head = snake.getBody().get(0);
        for (Position pos : wall) {
            if (head.equals(pos)) {
                return true;
            }
        }
        return false;
    }

    private void removeAllEventsAndTimers() {
        handler.removeCallbacksAndMessages(null);
        chocolateBoostActive = false;
        wallActive = false;
        invertControlActive = false;
        if (onBoostTimerChangeListener != null) {
            onBoostTimerChangeListener.onBoostTimerChange(0);
        }
        if (onWallTimerChangeListener != null) {
            onWallTimerChangeListener.onWallTimerChange(0);
        }
        if (onInvertControlTimerChangeListener != null) {
            onInvertControlTimerChangeListener.onInvertControlTimerChange(0);
        }
    }

    public void pauseGame() {
        paused = true;
        if (onPauseStatusChangeListener != null) {
            onPauseStatusChangeListener.onPauseStatusChange(true);
        }
        // Timer-Callbacks entfernen und verbleibende Zeit speichern
        handler.removeCallbacks(gameLoop);
        long currentTime = System.currentTimeMillis();
        if (chocolateBoostActive) {
            chocolateBoostRemainingTime = chocolateBoostEndTime - currentTime;
        }
        if (wallActive) {
            wallRemainingTime = wallEndTime - currentTime;
        }
        if (invertControlActive) {
            invertControlRemainingTime = invertControlEndTime - currentTime;
        }
        handler.removeCallbacksAndMessages(null);
    }

    public void startGame() {
        try {
            setStartValues();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void resetGame() {
        try {
            handler.removeCallbacks(gameLoop); // Stoppe den aktuellen Runnable
            setStartValues();
            gameView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setStartValues() {
        running = true;
        paused = true; // Spiel ist zu Beginn pausiert
        snake = new Snake(NUM_BLOCKS / 2, NUM_BLOCKS / 2);
        points = 0; // Punkte zurücksetzen
        currentDelay = START_DELAY; // Verzögerung zurücksetzen
        previousDelay = START_DELAY; // Vorherige Verzögerung zurücksetzen
        chocolateBoostActive = false; // Schokoladen-Boost deaktivieren
        wall.clear(); // Mauer zurücksetzen
        wallActive = false; // Wand deaktivieren
        invertControlActive = false; // Steuerung normal
        if (onPointsChangeListener != null) {
            onPointsChangeListener.onPointsChange(points);
        }
        if (onBoostTimerChangeListener != null) {
            onBoostTimerChangeListener.onBoostTimerChange(0);
        }
        if (onWallTimerChangeListener != null) {
            onWallTimerChangeListener.onWallTimerChange(0);
        }
        if (onInvertControlTimerChangeListener != null) {
            onInvertControlTimerChangeListener.onInvertControlTimerChange(0);
        }
        newFood();
    }

    public void setOnPointsChangeListener(OnPointsChangeListener listener) {
        this.onPointsChangeListener = listener;
    }

    public void setOnPauseStatusChangeListener(OnPauseStatusChangeListener listener) {
        this.onPauseStatusChangeListener = listener;
    }

    public void setOnBoostTimerChangeListener(OnBoostTimerChangeListener listener) {
        this.onBoostTimerChangeListener = listener;
    }

    public void setOnWallTimerChangeListener(OnWallTimerChangeListener listener) {
        this.onWallTimerChangeListener = listener;
    }

    public void setOnInvertControlTimerChangeListener(OnInvertControlTimerChangeListener listener) {
        this.onInvertControlTimerChangeListener = listener;
    }

    public void setOnGameOverListener(OnGameOverListener listener) {
        this.onGameOverListener = listener;
    }

    public Snake getSnake() {
        return snake;
    }

    public Food getFood() {
        return food;
    }

    public List<Position> getWall() {
        return wall;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    public interface OnPointsChangeListener {
        void onPointsChange(int points);
    }

    public interface OnPauseStatusChangeListener {
        void onPauseStatusChange(boolean pauseStatus);
    }

    public interface OnBoostTimerChangeListener {
        void onBoostTimerChange(long remainingTime);
    }

    public interface OnWallTimerChangeListener {
        void onWallTimerChange(long remainingTime);
    }

    public interface OnInvertControlTimerChangeListener {
        void onInvertControlTimerChange(long remainingTime);
    }

    public interface OnGameOverListener {
        void onGameOver(int points);
    }
}
