package de.kampmann.sensor2;

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
    private static final int MIN_DELAY = START_DELAY / 2; // Minimale Verzögerung (doppelte Geschwindigkeit)
    private static final int DELAY_DECREMENT = 10; // Verringerung der Verzögerung pro Futter
    private static final int CHOCOLATE_SPEED_BOOST = START_DELAY / 3; // 33% schneller
    private static final int CHOCOLATE_DURATION = 10000; // 10 Sekunden
    private static final int CANDY_WALL_DURATION = 20000; // 20 Sekunden
    private static final int LICORICE_INVERT_DURATION = 5000; // 5 Sekunden

    private Snake snake;
    private Food food;
    private List<Position> wall;
    private boolean running = true;
    private boolean paused = true; // Spiel ist zu Beginn pausiert
    private int points = 0; // Punkte zählen
    private int currentDelay = START_DELAY; // Aktuelle Verzögerung
    private int previousDelay = START_DELAY; // Vorherige Verzögerung
    private boolean chocolateBoostActive = false; // Schokoladen-Boost aktiv
    private boolean wallActive = false; // Wand aktiv
    private boolean invertControlActive = false; // Steuerung invertiert
    private Handler handler;
    private Runnable gameLoop;
    private OnPointsChangeListener onPointsChangeListener;
    private OnBoostTimerChangeListener onBoostTimerChangeListener;
    private OnWallTimerChangeListener onWallTimerChangeListener;
    private OnInvertControlTimerChangeListener onInvertControlTimerChangeListener;
    private OnGameOverListener onGameOverListener;
    private GameView gameView;

    public GameController(Context context, ViewGroup parent) {
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
                if (running && !paused) {
                    snake.move();
                    if (snake.isHeadOnFood(food.getPosition())) {
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
                        newFood(); // In jedem Fall neues Food erstellen
                    } else {
                        snake.shrink();
                    }
                    if (snake.checkCollision(NUM_BLOCKS, NUM_BLOCKS) || checkWallCollision()) {
                        running = false;
                        removeAllEventsAndTimers();
                        if (onGameOverListener != null) {
                            onGameOverListener.onGameOver(points);
                        }
                    }
                    gameView.invalidate();
                }
                handler.postDelayed(this, currentDelay);
            }
        };
    }

    public void startGame() {
        try {
            snake = new Snake(NUM_BLOCKS / 2, NUM_BLOCKS / 2);
            points = 0; // Punkte zurücksetzen
            currentDelay = START_DELAY; // Verzögerung zurücksetzen
            previousDelay = START_DELAY; // Vorherige Verzögerung zurücksetzen
            chocolateBoostActive = false; // Schokoladen-Boost deaktivieren
            wallActive = false; // Wand deaktivieren
            invertControlActive = false; // Steuerung normal
            wall = new ArrayList<>(); // Mauer zurücksetzen
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
            handler.post(gameLoop);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resumeGame() {
        paused = false;
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
                    continue; // Überspringe Schokolade während des Boosts, Bonbons während der Wand, Lakritz während der invertierten Steuerung, Positionen unter der Mauer und Positionen der Schlange
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
            currentDelay = Math.max(MIN_DELAY, currentDelay - CHOCOLATE_SPEED_BOOST);
            final long startTime = System.currentTimeMillis();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    chocolateBoostActive = false;
                    currentDelay = previousDelay;
                    if (onBoostTimerChangeListener != null) {
                        onBoostTimerChangeListener.onBoostTimerChange(0);
                    }
                }
            }, CHOCOLATE_DURATION);

            // Timer für die Anzeige der verbleibenden Zeit
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (chocolateBoostActive) {
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        long remainingTime = CHOCOLATE_DURATION - elapsedTime;
                        if (onBoostTimerChangeListener != null) {
                            onBoostTimerChangeListener.onBoostTimerChange(remainingTime);
                        }
                        handler.postDelayed(this, 100);
                    }
                }
            });
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
        final long startTime = System.currentTimeMillis();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wall.clear();
                wallActive = false;
                if (onWallTimerChangeListener != null) {
                    onWallTimerChangeListener.onWallTimerChange(0);
                }
            }
        }, CANDY_WALL_DURATION);

        // Timer für die Anzeige der verbleibenden Zeit der Wand
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (wallActive) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    long remainingTime = CANDY_WALL_DURATION - elapsedTime;
                    if (onWallTimerChangeListener != null) {
                        onWallTimerChangeListener.onWallTimerChange(remainingTime);
                    }
                    handler.postDelayed(this, 100);
                }
            }
        });
    }

    private void activateInvertControl() {
        if (!invertControlActive) {
            invertControlActive = true;
            final long startTime = System.currentTimeMillis();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    invertControlActive = false;
                    if (onInvertControlTimerChangeListener != null) {
                        onInvertControlTimerChangeListener.onInvertControlTimerChange(0);
                    }
                }
            }, LICORICE_INVERT_DURATION);

            // Timer für die Anzeige der verbleibenden Zeit der invertierten Steuerung
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (invertControlActive) {
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        long remainingTime = LICORICE_INVERT_DURATION - elapsedTime;
                        if (onInvertControlTimerChangeListener != null) {
                            onInvertControlTimerChangeListener.onInvertControlTimerChange(remainingTime);
                        }
                        handler.postDelayed(this, 100);
                    }
                }
            });
        }
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

    public void pauseGame() {
        paused = true;
    }

    public void resetGame() {
        try {
            handler.removeCallbacks(gameLoop); // Stoppe den aktuellen Runnable
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
            handler.post(gameLoop); // Starte den Runnable neu
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnPointsChangeListener(OnPointsChangeListener listener) {
        this.onPointsChangeListener = listener;
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
