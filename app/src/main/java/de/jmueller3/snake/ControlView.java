package de.jmueller3.snake;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import de.jmueller3.snake.R;

public class ControlView extends LinearLayout implements SensorEventListener {
    private GameController gameController;
    private ImageButton buttonSensorControl;
    private ImageButton buttonPause;
    private DirectionControlView directionControlView;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private boolean sensorControlEnabled = false;
    private boolean referenceValuesSet = false;
    private float referenceX = 0;
    private float referenceY = 0;
    private float currentX = 0;
    private float currentY = 0;

    public ControlView(Context context) {
        super(context);
        init(context);
    }

    public ControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.control_view, this, true);

        buttonSensorControl = findViewById(R.id.buttonSensorControl);
        buttonPause = findViewById(R.id.buttonPause);
        directionControlView = findViewById(R.id.circleControlView);

        buttonPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameController != null) {
                    if (gameController.isPaused()) {
                        gameController.resumeGame();
                    } else {
                        gameController.pauseGame();
                    }
                }
            }
        });

        buttonSensorControl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameController != null) {
                    if (sensorControlEnabled) {
                        disableSensorControl();
                    } else {
                        enableSensorControl();
                    }
                }
            }
        });

        directionControlView.setOnDirectionChangeListener(new DirectionControlView.OnDirectionChangeListener() {
            @Override
            public void onDirectionChange(char direction) {
                if (gameController != null) {
                    gameController.setDirection(direction);
                    disableSensorControl();
                }
            }
        });

        // SensorManager und AccelerometerSensor initialisieren
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        this.gameController.setOnPauseStatusChangeListener(new GameController.OnPauseStatusChangeListener() {
            @Override
            public void onPauseStatusChange(boolean paused) {
                if (paused) {
                    buttonPause.setImageResource(R.drawable.ic_pause);
                } else {
                    buttonPause.setImageResource(R.drawable.ic_resume);
                }
            }
        });
    }

    private void enableSensorControl() {
        sensorControlEnabled = true;
        referenceValuesSet = false; // Referenzwerte zurücksetzen
        buttonSensorControl.setImageResource(R.drawable.ic_sensor_on);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
    }

    private void disableSensorControl() {
        sensorControlEnabled = false;
        buttonSensorControl.setImageResource(R.drawable.ic_sensor_off);
        sensorManager.unregisterListener(this);
    }

    public void invalidateReferenceValues() {
        referenceValuesSet = false; // Referenzwerte zurücksetzen
    }

    private void setReferenceValuesToCurrent() {
        this.referenceX = currentX; // Setze die Referenzwerte auf die aktuellen Werte
        this.referenceY = currentY;
        referenceValuesSet = true; // Flag setzen, dass die Referenzwerte gesetzt wurden
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sensorControlEnabled && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float xAcceleration = event.values[0];
            float yAcceleration = event.values[1];

            // Aktuelle Sensorwerte speichern
            currentX = xAcceleration;
            currentY = yAcceleration;

            // Referenzwerte setzen, wenn sie noch nicht gesetzt wurden
            if (!referenceValuesSet) {
                setReferenceValuesToCurrent();
            }

            // Berechne die Differenz zur Referenz
            float deltaX = xAcceleration - referenceX;
            float deltaY = yAcceleration - referenceY;

            // Schlange steuern basierend auf der Differenz
            if (deltaY > 2) {
                gameController.setDirection('D'); // Nach unten
            } else if (deltaY < -2) {
                gameController.setDirection('U'); // Nach oben
            } else if (deltaX > 2) {
                gameController.setDirection('L'); // Nach links
            } else if (deltaX < -2) {
                gameController.setDirection('R'); // Nach rechts
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nicht verwendet
    }
}
