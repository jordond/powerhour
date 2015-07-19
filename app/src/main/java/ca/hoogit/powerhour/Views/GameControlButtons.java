package ca.hoogit.powerhour.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import at.markushi.ui.CircleButton;
import ca.hoogit.powerhour.R;

/**
 * Created by jordon on 16/07/15.
 * Simple view for controlling the timer buttons
 */
public class GameControlButtons extends LinearLayout {

    private static final String TAG = GameControlButtons.class.getSimpleName();

    public enum GameStates {
        ACTIVE, PAUSED, STOPPED, NO_MORE_PAUSES
    }

    private GameControl mListener;

    private ImageButton mSound;
    private CircleButton mControl;
    private ImageButton mStop;

    private int color;
    private int maxPauses;
    private int currentPauses;
    private boolean isActive;

    public GameControlButtons(Context context) {
        super(context);
        init(context, null);
    }

    public GameControlButtons(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GameControlButtons(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        inflate(getContext(), R.layout.game_control_buttons, this);

        // Get the attributes
        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.GameControlButtons, 0, 0);
        color = attr.getColor(R.styleable.GameControlButtons_gcb_color, Color.BLACK);
        maxPauses = attr.getInteger(R.styleable.GameControlButtons_gcb_maxPauses, -1);
        attr.recycle();

        // Grab the views
        mSound = (ImageButton) findViewById(R.id.sound_button);
        mControl = (CircleButton) findViewById(R.id.timer_control);
        mStop = (ImageButton) findViewById(R.id.timer_stop);

        mControl.setColor(color);

        currentPauses = 0;
        isActive = false;
    }

    public void updateControlIcon(boolean active) {
        if (active) {
            mControl.setImageResource(R.drawable.ic_av_play_arrow);
            Log.d(TAG, "Changing icon to play");
        } else {
            mControl.setImageResource(R.drawable.ic_av_pause);
            Log.d(TAG, "Changing icon to pause");
        }
    }

    public void updateControlIcon(GameStates status) {
        switch (status) {
            case ACTIVE:
                updateControlIcon(true);
                break;
            case PAUSED:
            case STOPPED:
                updateControlIcon(false);
                break;
            case NO_MORE_PAUSES:
                mControl.setVisibility(INVISIBLE);
                mControl.setEnabled(false);
        }
    }

    private void registerListeners() {
        mSound.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.soundPressed();
            }
        });
        mControl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPauses < maxPauses || maxPauses == -1) {
                    if (isActive) {
                        updateControlIcon(GameStates.ACTIVE);
                        currentPauses++;
                    } else {
                        updateControlIcon(GameStates.PAUSED);
                    }
                } else {
                    if (!isActive) {
                        updateControlIcon(GameStates.NO_MORE_PAUSES);
                        Log.d(TAG, "Disabling the control button");
                    }
                }
                isActive = !isActive;
                mListener.controlPressed(isActive, currentPauses);
            }
        });
        mStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.stopPressed();
            }
        });
    }

    private void removeListeners() {
        mSound.setOnClickListener(null);
        mControl.setOnClickListener(null);
        mStop.setOnClickListener(null);
    }

    public GameControl getListener() {
        return mListener;
    }

    public void setOnButtonPressed(GameControl mListener) {
        this.mListener = mListener;
        registerListeners();
    }

    public void removeOnButtonPressed() {
        this.mListener = null;
        removeListeners();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsAcitve(boolean isPlaying) {
        this.isActive = isPlaying;
        updateControlIcon(isPlaying);
    }

    public int getMaxPauses() {
        return maxPauses;
    }

    public void setColor(int color) {
        this.color = color;
        mControl.setColor(color);
    }

    public void setPauseCount(int currentPauses) {
        this.currentPauses = currentPauses;
    }

    public void setMaxPauses(int maxPauses) {
        this.maxPauses = maxPauses;
    }

    public interface GameControl {
        void soundPressed();

        void controlPressed(boolean isActive, int numberOfPauses);

        void stopPressed();
    }
}
