package ca.hoogit.powerhour.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
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
        maxPauses = attr.getInteger(R.styleable.GameControlButtons_gcb_maxPauses, -1);
        attr.recycle();

        // Grab the views
        mSound = (ImageButton) findViewById(R.id.sound_button);
        mControl = (CircleButton) findViewById(R.id.timer_control);
        mStop = (ImageButton) findViewById(R.id.timer_stop);

        currentPauses = 0;
        isActive = false;
    }

    public void updateControlIcon(boolean active) {
        if (active) {
            mControl.setImageResource(R.drawable.ic_av_pause);
        } else {
            mControl.setImageResource(R.drawable.ic_av_play_arrow);
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
                if (currentPauses <= maxPauses || maxPauses == -1) {
                    if (isActive) {
                        updateControlIcon(GameStates.ACTIVE);
                        currentPauses++;
                    } else {
                        updateControlIcon(GameStates.PAUSED);
                    }
                } else {
                    if (!isActive) {
                        updateControlIcon(GameStates.NO_MORE_PAUSES);
                    }
                }
                isActive = !isActive;
                mListener.controlPressed(currentPauses);
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

    public void setIsPlaying(boolean isPlaying) {
        this.isActive = isPlaying;
    }

    public int getMaxPauses() {
        return maxPauses;
    }

    public void setMaxPauses(int maxPauses) {
        this.maxPauses = maxPauses;
    }

    public interface GameControl {
        void soundPressed();

        void controlPressed(int numberOfPauses);

        void stopPressed();
    }
}
