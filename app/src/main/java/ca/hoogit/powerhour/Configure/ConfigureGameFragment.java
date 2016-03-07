package ca.hoogit.powerhour.Configure;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.gc.materialdesign.views.Slider;
import com.gc.materialdesign.views.Switch;
import com.gc.materialdesign.widgets.ColorSelector;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.hoogit.powerhour.Audio.AudioPlayer;
import ca.hoogit.powerhour.Audio.SoundRecorder;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Util.BusProvider;
import ca.hoogit.powerhourshared.DataLayer.ColorUtil;
import ca.hoogit.powerhour.Util.PowerHourUtils;
import ca.hoogit.powerhour.Util.PowerHourUtils.SoundFile;
import ca.hoogit.powerhour.Util.StatusBarUtil;
import ca.hoogit.powerhour.Views.PlusMinusButtons;
import ca.hoogit.soundchooser.SoundChooserDialog;
import info.hoang8f.widget.FButton;
import mbanje.kurt.fabbutton.FabButton;

/**
 */
public class ConfigureGameFragment extends Fragment {

    private final String TAG = ConfigureGameFragment.class.getSimpleName();

    private static final String ARG_OPTIONS = "game_options";

    private final int DEFAULT_SOUND = R.raw.argon;

    @Bind(R.id.appBar) Toolbar mToolbar;

    @Bind(R.id.configure_container) RelativeLayout mLayout;
    @Bind(R.id.configure_game_title) TextView mTitle;
    @Bind(R.id.configure_rounds_value) TextView mRoundsValue;

    @Bind(R.id.configure_rounds_slider) Slider mRoundsSlider;
    @Bind(R.id.configure_rounds_buttons) PlusMinusButtons mRoundsButtons;

    @Bind(R.id.configure_pauses_value) TextView mPausesValue;
    @Bind(R.id.configure_pauses_slider) Slider mPausesSlider;

    @Bind(R.id.configure_keep_screen_on) Switch mKeepScreenOn;

    @Bind(R.id.configure_sound_container) LinearLayout mSoundContainer;
    @Bind(R.id.configure_play) FabButton mPlayShotSound;
    @Bind(R.id.configure_sound_chooser) ImageButton mSoundChooser;
    @Bind(R.id.configure_record) FabButton mRecordSound;

    @Bind(R.id.configure_start) FButton mStartButton;

    private AppCompatActivity mActivity;

    private GameOptions mOptions;
    private int mPrimaryColor;
    private int mAccentColor;

    private int mRounds;
    private int mPauses;

    private boolean mIsMuted;
    private ArrayList<SoundFile> mSoundFiles;
    private int mShotSound = DEFAULT_SOUND;
    private String mCustomFileName = "";
    private ProgressButton mPlay;
    private SoundRecorder mRecorder;

    private boolean mGameStarting;

    public static ConfigureGameFragment newInstance(GameOptions options) {
        ConfigureGameFragment fragment = new ConfigureGameFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_OPTIONS, options);
        fragment.setArguments(args);
        return fragment;
    }

    public ConfigureGameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mOptions = (GameOptions) getArguments().getSerializable(ARG_OPTIONS);

            if (mOptions != null) {
                mPrimaryColor = mOptions.getBackgroundColor();
                mAccentColor = mOptions.getAccentColor();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
        if (mPlay != null) {
            mPlay.destroy();
        }
        if (mRecorder.isActive()) {
            mRecorder.toggle(false);
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!enter) {
            if (mGameStarting) {
                return AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_left);
            }
            return AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right);
        }
        return super.onCreateAnimation(transit, true, nextAnim);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                reset();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void reset() {
        changeColors(mOptions.getBackgroundColor(), mOptions.getAccentColor());
        setRoundsValue(mOptions.getRounds());
        setPausesValue(mOptions.getMaxPauses());
    }

    private void changeColors(int primary, int accent) {
        changePrimary(primary);
        changeAccent(accent);
    }

    private void changePrimary(int primary) {
        StatusBarUtil.getInstance().set(mActivity, primary);
        mToolbar.setBackgroundColor(primary);
        mLayout.setBackgroundColor(primary);
        mPlayShotSound.setColor(primary);
        mRecordSound.setColor(primary);

        mPrimaryColor = primary;
    }

    private void changeAccent(int accent) {
        mRoundsSlider.setBackgroundColor(accent);
        mPausesSlider.setBackgroundColor(accent);

        mStartButton.setButtonColor(accent);
        mStartButton.setShadowColor(ColorUtil.darken(accent));

        mKeepScreenOn.setBackgroundColor(accent);

        mPlayShotSound.setProgressColor(accent);
        mRecordSound.setProgressColor(accent);

        mAccentColor = accent;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configure_game, container, false);
        ButterKnife.bind(this, view);

        // Setup the toolbar
        mActivity = (AppCompatActivity) getActivity();
        mActivity.setSupportActionBar(mToolbar);

        try {
            mActivity.getSupportActionBar().setHomeButtonEnabled(true);
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ex) {
            Log.e(TAG, ex.getMessage());
        }

        // Setup layout elements
        changeColors(mOptions.getBackgroundColor(), mOptions.getAccentColor());
        mTitle.setText(mOptions.getTitle());

        if (savedInstanceState == null) {
            setRoundsValue(mOptions.getRounds());
            setPausesValue(mOptions.getMaxPauses());
        }

        mRoundsButtons.setOnButtonPressed(new PlusMinusButtons.ButtonInteraction() {
            @Override
            public void minusPressed(int amount) {
                setRoundsValue(mRounds - amount);
            }

            @Override
            public void resetPressed() {
                setRoundsValue(mOptions.getRounds());
            }

            @Override
            public void plusPressed(int amount) {
                setRoundsValue(mRounds + amount);
            }
        });

        mRoundsSlider.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int i) {
                setRoundsValue(i);
            }
        });

        mPausesSlider.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @Override
            public void onValueChanged(int i) {
                setPausesValue(i);
            }
        });

        // Shot sound buttons
        mSoundChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsMuted) {
                    if (mRecorder.isActive()) {
                        mRecorder.toggle(false);
                    }
                    if (mPlay.isActive()) {
                        mPlay.reset();
                    }
                    openSoundChooser();
                }
            }
        });

        mSoundFiles = PowerHourUtils.getSounds();

        AudioPlayer mAudioPlayer = new AudioPlayer(getActivity());
        mPlay = new ProgressButton(mPlayShotSound, mAudioPlayer);
        mPlayShotSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecorder.isActive()) {
                    mRecorder.toggle(false);
                } else if (!mPlay.isActive()) {
                    if (mShotSound == SoundRecorder.CUSTOM_RECORDED_SOUND) {
                        mPlay.start(Uri.parse(mCustomFileName));
                    } else {
                        mPlay.start(mShotSound);
                    }
                } else {
                    mPlay.reset();
                }
            }
        });

        mRecorder = new SoundRecorder(getActivity(), mRecordSound, new SoundRecorder.OnRecordFinished() {
            @Override
            public void recordingFinished(String filename) {
                File file = new File(filename);
                if (file.exists()) {
                    mShotSound = SoundRecorder.CUSTOM_RECORDED_SOUND;
                    mCustomFileName = filename;
                }
            }
        });

        mRecordSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlay.isActive()) {
                    mPlay.reset();
                }
                mRecorder.toggle(!mRecorder.isActive());
                Answers.getInstance().logCustom(new CustomEvent("Recorded Own Sound"));
            }
        });

        mGameStarting = false;

        return view;
    }

    private void stopAll() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("rounds", mRounds);
        outState.putInt("pauses", mPauses);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            setRoundsValue(savedInstanceState.getInt("rounds"));
            setPausesValue(savedInstanceState.getInt("pauses"));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRoundsButtons.removeOnButtonPressed();
        ButterKnife.unbind(this);
    }

    private void setRoundsValue(int rounds) {
        if (rounds <= mRoundsSlider.getMax() && rounds >= mRoundsSlider.getMin()) {
            if (rounds == mRoundsSlider.getMax()) {
                mRoundsValue.setText("∞");
            } else if (rounds == mRoundsSlider.getMin()) {
                mRoundsValue.setText("nil");
                mStartButton.setEnabled(false);
            } else {
                mRoundsValue.setText(String.format("%03d", rounds));
                mStartButton.setEnabled(true);
            }
            mRounds = rounds;
            mRoundsSlider.setValue(rounds);
        }
    }

    private void setPausesValue(int pauses) {
        if (pauses == mPausesSlider.getMax() || pauses == -1) {
            mPausesValue.setText("∞");
            mPausesSlider.setValue(mPausesSlider.getMax());
        } else if (pauses == mPausesSlider.getMin()) {
            mPausesValue.setText("0");
        } else {
            mPausesValue.setText(String.valueOf(pauses));
            mPausesSlider.setValue(pauses);
        }
        mPauses = pauses;
    }

    private void createColorPicker(int initialColor, ColorSelector.OnColorSelectedListener callback) {
        ColorSelector colorSelector = new ColorSelector(mActivity, initialColor, callback);
        colorSelector.show();
    }

    @OnClick({R.id.configure_color_background, R.id.configure_color_accent, R.id.configure_color_reset, R.id.configure_mute})
    public void chooseColor(View v) {
        switch (v.getId()) {
            case R.id.configure_color_background:
                createColorPicker(mPrimaryColor, new ColorSelector.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int i) {
                        changePrimary(i);
                    }
                });
                break;
            case R.id.configure_color_accent:
                createColorPicker(mAccentColor, new ColorSelector.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int i) {
                        changeAccent(i);
                        refreshUI();
                    }
                });
                break;
            case R.id.configure_color_reset:
                changeColors(mOptions.getBackgroundColor(), mOptions.getAccentColor());
                refreshUI();
                break;
            case R.id.configure_mute:
                mIsMuted = !mIsMuted;
                if (mIsMuted) {
                    ((ImageButton) v).setImageResource(R.drawable.ic_av_volume_off);
                    mSoundContainer.setVisibility(View.INVISIBLE);
                    if (mPlay.isActive()) {
                        mPlay.reset();
                    }
                    mRecorder.toggle(false);
                } else {
                    ((ImageButton) v).setImageResource(R.drawable.ic_av_volume_up);
                    mSoundContainer.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void openSoundChooser() {
        if (mSoundFiles == null) {
            mSoundFiles = PowerHourUtils.getSounds();
        }
        int[] soundIds = PowerHourUtils.soundArrayListToIdArray(getContext(), mSoundFiles);
        SoundChooserDialog dialog = SoundChooserDialog.newInstance("Choose a drinking alarm",
                soundIds, mPrimaryColor, 5, SoundChooserDialog.SIZE_SMALL);
        dialog.setAudioStreamType(AudioManager.STREAM_ALARM);
        dialog.setTheme(R.style.DialogTheme);
        dialog.setOnOptionChosen(new SoundChooserDialog.OnOptionChosen() {
            @Override
            public void onSoundSelected(int i) {
            }

            @Override
            public void onPositive(DialogInterface dialogInterface, int i) {
                mShotSound = i;
                Answers.getInstance().logCustom(
                        new CustomEvent("Chose Custom Alarm").putCustomAttribute("Sound ID", i));
            }

            @Override
            public void onNegative(DialogInterface dialogInterface) {
                mShotSound = DEFAULT_SOUND;
            }
        });
        dialog.show(getFragmentManager(), "soundChooser");
    }

    public void refreshUI() {
        setRoundsValue(mRounds);
        setPausesValue(mPauses);
        mKeepScreenOn.setChecked(mKeepScreenOn.isCheck());
    }

    @OnClick(R.id.configure_start)
    public void launchGame() {
        GameOptions options = createOptions();
        options.toLog();
        mGameStarting = true;
        BusProvider.getInstance().post(options);
    }

    private GameOptions createOptions() {
        GameOptions options = new GameOptions();
        options.setTitle(mOptions.getTitle());
        options.setType(mOptions.getType());
        options.setRounds(mRounds);
        options.setMaxPauses(mPauses);
        options.setColors(mPrimaryColor, mAccentColor);
        options.setIsMuted(mIsMuted);
        options.setShotSound(mShotSound);
        options.setAutoStart(true);
        options.setKeepScreenOn(mKeepScreenOn.isCheck());
        options.setWasConfigured(true);

        return options;
    }

}
