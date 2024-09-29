package de.jmueller3.snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

public class GameView extends View {
    private static final int NUM_BLOCKS = 15; // Anzahl der Blöcke in Breite und Höhe
    private static final int MIN_FRAME_WIDTH = 10; // Mindestbreite des Rahmens in Pixeln

    private int blockSize;
    private int fieldWidth;
    private int fieldHeight;
    private int offsetX;
    private int offsetY;
    private Paint paint;
    private GameController gameController;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();

        // Hinzufügen des OnGlobalLayoutListener
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Entfernen des Listeners, um Mehrfachaufrufe zu vermeiden
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // Initialisieren der Spielfeldgröße
                int viewSize = Math.min(getWidth(), getHeight()) - 2 * MIN_FRAME_WIDTH;
                blockSize = viewSize / NUM_BLOCKS;
                fieldWidth = blockSize * NUM_BLOCKS;
                fieldHeight = blockSize * NUM_BLOCKS;
                offsetX = (getWidth() - fieldWidth) / 2;
                offsetY = (getHeight() - fieldHeight) / 2;
                if (gameController != null) {
                    gameController.startGame();
                }
            }
        });
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            paint.setColor(Color.rgb(0, 80, 0)); // Standard-Hintergrundfarbe
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

            // Zeichne das Spielfeld
            for (int x = 0; x < NUM_BLOCKS; x++) {
                for (int y = 0; y < NUM_BLOCKS; y++) {
                    if ((x + y) % 2 == 0) {
                        paint.setColor(Color.rgb(0, 120, 0)); // Dunkelgrün
                    } else {
                        paint.setColor(Color.rgb(0, 140, 0)); // Waldgrün
                    }
                    canvas.drawRect(offsetX + x * blockSize, offsetY + y * blockSize, offsetX + (x + 1) * blockSize, offsetY + (y + 1) * blockSize, paint);
                }
            }

            if (gameController != null && gameController.isRunning()) {
                // Zeichne das Futter
                paint.setColor(gameController.getFood().getType().getColor());
                canvas.drawRect(offsetX + gameController.getFood().getPosition().getX() * blockSize, offsetY + gameController.getFood().getPosition().getY() * blockSize, offsetX + (gameController.getFood().getPosition().getX() + 1) * blockSize, offsetY + (gameController.getFood().getPosition().getY() + 1) * blockSize, paint);

                // Zeichne die Schlange
                for (int i = 0; i < gameController.getSnake().getBody().size(); i++) {
                    if (i == 0) {
                        paint.setColor(Color.rgb(0, 0, 139)); // Dunkelblauer Kopf
                    } else if (i % 2 == 0) {
                        paint.setColor(Color.rgb(0, 0, 255)); // Blau
                    } else {
                        paint.setColor(Color.rgb(0, 0, 205)); // Mittelblau
                    }
                    Position part = gameController.getSnake().getBody().get(i);
                    canvas.drawRect(offsetX + part.getX() * blockSize, offsetY + part.getY() * blockSize, offsetX + (part.getX() + 1) * blockSize, offsetY + (part.getY() + 1) * blockSize, paint);
                }

                // Zeichne die Mauer
                paint.setColor(Color.GRAY);
                for (Position pos : gameController.getWall()) {
                    canvas.drawRect(offsetX + pos.getX() * blockSize, offsetY + pos.getY() * blockSize, offsetX + (pos.getX() + 1) * blockSize, offsetY + (pos.getY() + 1) * blockSize, paint);
                }
            }
        } catch (Exception e) {
            Log.e("GameView", "Error drawing game", e);
        }
    }
}
