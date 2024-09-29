package de.jmueller3.snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DirectionControlView extends View {
    private Paint paint;
    private Paint linePaint;
    private Paint arrowPaint;
    private OnDirectionChangeListener listener;

    public DirectionControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.parseColor("#3F48CC")); // Hintergrundfarbe
        paint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#22276E")); // Farbe für die Linien
        linePaint.setStrokeWidth(5); // Breite der Linien

        arrowPaint = new Paint();
        arrowPaint.setColor(Color.parseColor("#22276E")); // Farbe für die Pfeile
        arrowPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int padding = 10;
        int cornerRadius = 20;
        int left = padding;
        int top = padding;
        int right = width - padding;
        int bottom = height - padding;

        // Zeichne das Rechteck mit abgerundeten Ecken
        RectF rect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

        // Zeichne das Kreuz innerhalb des Rechtecks, beginnend an der Rundung
        float startX = left + cornerRadius;
        float startY = top + cornerRadius;
        float endX = right - cornerRadius;
        float endY = bottom - cornerRadius;
        canvas.drawLine(startX, startY, endX, endY, linePaint); // Linie von oben links nach unten rechts
        canvas.drawLine(endX, startY, startX, endY, linePaint); // Linie von oben rechts nach unten links

        // Zeichne die Pfeile in den vier Dreiecken
        drawArrow(canvas, startX, startY, endX, endY, 'U'); // Oben
        drawArrow(canvas, startX, startY, endX, endY, 'L'); // Links
        drawArrow(canvas, startX, startY, endX, endY, 'R'); // Rechts
        drawArrow(canvas, startX, startY, endX, endY, 'D'); // Unten
    }

    private void drawArrow(Canvas canvas, float startX, float startY, float endX, float endY, char direction) {
        Path path = new Path();
        int arrowSize = 80;
        int halfArrowSize = 40;
        switch (direction) {
            case 'U':
                path.moveTo((startX + endX) / 2, startY);
                path.lineTo((startX + endX) / 2 - halfArrowSize, startY + arrowSize);
                path.lineTo((startX + endX) / 2 + halfArrowSize, startY + arrowSize);
                path.close();
                break;
            case 'L':
                path.moveTo(startX, (startY + endY) / 2);
                path.lineTo(startX + arrowSize, (startY + endY) / 2 - halfArrowSize);
                path.lineTo(startX + arrowSize, (startY + endY) / 2 + halfArrowSize);
                path.close();
                break;
            case 'R':
                path.moveTo(endX, (startY + endY) / 2);
                path.lineTo(endX - arrowSize, (startY + endY) / 2 - halfArrowSize);
                path.lineTo(endX - arrowSize, (startY + endY) / 2 + halfArrowSize);
                path.close();
                break;
            case 'D':
                path.moveTo((startX + endX) / 2, endY);
                path.lineTo((startX + endX) / 2 - halfArrowSize, endY - arrowSize);
                path.lineTo((startX + endX) / 2 + halfArrowSize, endY - arrowSize);
                path.close();
                break;
        }
        canvas.drawPath(path, arrowPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            int width = getWidth();
            if (listener != null) {
                if (x > y && (x + y) < width) {
                    listener.onDirectionChange('U'); // Oben
                } else if (x < y && (x + y) < width) {
                    listener.onDirectionChange('L'); // Links
                } else if (x < y && (x + y) > width) {
                    listener.onDirectionChange('D'); // Unten
                } else if (x > y && (x + y) > width) {
                    listener.onDirectionChange('R'); // Rechts
                }
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setOnDirectionChangeListener(OnDirectionChangeListener listener) {
        this.listener = listener;
    }

    public interface OnDirectionChangeListener {
        void onDirectionChange(char direction);
    }
}
