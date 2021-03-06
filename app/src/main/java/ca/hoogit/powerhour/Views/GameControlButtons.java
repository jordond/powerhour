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
        ACTIVE, PAUSED, STOPPED, NO_MORE_PAUSES, HIDE
    }

    private GameControl mListener;

    private ImageButton mSound;
    private ImageButton mScreenLock;
    private CircleButton mControl;
    private ImageButton mStop;

    private int color;
    private int mIcon;

    private final int STATE_PLAY_ICON = R.drawable.ic_av_play_arrow;
    private final int STATE_PAUSE_ICON = R.drawable.ic_av_pause;

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
        attr.recycle();

        // Grab the views
        mSound = (ImageButton) findViewById(R.id.sound_button);
        mScreenLock = (ImageButton) findViewById(R.id.screen_on);
        mControl = (CircleButton) findViewById(R.id.timer_control);
        mStop = (ImageButton) findViewById(R.id.timer_stop);

        mIcon = STATE_PLAY_ICON;

        mControl.setColor(color);

    }

    public void setColors() {

    }

    private void registerListeners() {
        mSound.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.soundPressed();
            }
        });
        mSound.setLongClickable(true);
        mSound.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.soundLongPressed();
                return true;
            }
        });
        mScreenLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.screenLockPressed();
            }
        });
        mControl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.controlPressed();
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
        mScreenLock.setOnClickListener(null);
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

    public void setColor(int color) {
        this.color = color;
        mControl.setColor(color);
    }

    public void setIcon(boolean toPlay) {
        if (toPlay) {
            mIcon = STATE_PLAY_ICON;
        } else {
            mIcon = STATE_PAUSE_ICON;
        }
        mControl.setImageResource(mIcon);
    }

    public void setMuteIcon(boolean isMuted) {
        if (isMuted) {
            mSound.setImageResource(R.drawable.ic_av_volume_off);
        } else {
            mSound.setImageResource(R.drawable.ic_av_volume_up);
        }
    }

    public boolean toggleScreenLock(boolean state) {
        if (state) {
            mScreenLock.setImageResource(R.drawable.ic_screen_on_true);
        } else {
            mScreenLock.setImageResource(R.drawable.ic_screen_on_false);
        }
        Log.d(TAG, "ToggleScreenLock setting to " + state);
        return state;
    }

    public void toggleCenterButton() {
        if (mControl.getVisibility() == View.VISIBLE) {
            if (mIcon == STATE_PLAY_ICON) {
                mIcon = STATE_PAUSE_ICON;
                Log.v(TAG, "Setting icon to Pause");
            } else {
                mIcon = STATE_PLAY_ICON;
                Log.v(TAG, "Setting icon to Play");
            }
            mControl.setImageResource(mIcon);
        }
    }

    public void hideCenter() {
        if (mControl.getVisibility() == View.VISIBLE) {
            mControl.setVisibility(View.INVISIBLE);
            Log.d(TAG, "Hiding the center control button");
        }
    }

    public void showCenter() {
        if (mControl.getVisibility() == View.INVISIBLE) {
            mControl.setVisibility(View.VISIBLE);
        }
    }

    public int getIcon() {
        return mIcon;
    }

    public void setIcon(int mIcon) {
        this.mIcon = mIcon;
    }

    public interface GameControl {
        void soundPressed();

        void soundLongPressed();

        void screenLockPressed();

        void controlPressed();

        void stopPressed();
    }
}
