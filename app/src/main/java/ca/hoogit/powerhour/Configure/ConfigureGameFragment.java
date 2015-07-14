package ca.hoogit.powerhour.Configure;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gc.materialdesign.views.Slider;
import com.gc.materialdesign.widgets.ColorSelector;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ca.hoogit.powerhour.BusProvider;
import ca.hoogit.powerhour.CloseFragmentEvent;
import ca.hoogit.powerhour.Game.GameOptions;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Util.ChangeStatusColor;
import ca.hoogit.powerhour.Util.ColorUtil;
import ca.hoogit.powerhour.Views.PlusMinusButtons;
import info.hoang8f.widget.FButton;

/**
 */
public class ConfigureGameFragment extends Fragment {

    @Bind(R.id.appBar) private Toolbar mToolbar;
    @Bind(R.id.configure_container) private RelativeLayout mLayout;
    @Bind(R.id.configure_game_title) private TextView mTitle;

    @Bind(R.id.configure_rounds_value) private TextView mRoundsValue;
    @Bind(R.id.configure_rounds_slider)
    private Slider mRoundsSlider;
    @Bind(R.id.configure_rounds_buttons)
    private PlusMinusButtons mRoundsButtons;

    @Bind(R.id.configure_pauses_value)
    private TextView mPausesValue;
    @Bind(R.id.configure_pauses_slider)
    private Slider mPausesSlider;

    @Bind(R.id.configure_color_background)
    private FButton mChangeBackground;

    @Bind(R.id.configure_color_accent)
    private FButton mChangeAccent;

    @Bind(R.id.configure_start)
    private FButton mStartButton;

    private static final String ARG_OPTIONS = "game_options";
    private final String TAG = ConfigureGameFragment.class.getSimpleName();

    private AppCompatActivity mActivity;

    private GameOptions mOptions;
    private int mPrimaryColor;
    private int mAccentColor;

    private int mRounds;
    private int mPauses;

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
            mPrimaryColor = mOptions.getBackgroundColor();
            mAccentColor = mOptions.getAccentColor();
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
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!enter) {
            return AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right);
        }
        return super.onCreateAnimation(transit, true, nextAnim);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                BusProvider.getInstance().post(new CloseFragmentEvent(false));
                return true;
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
        mToolbar.setBackgroundColor(primary);
        mActivity.setSupportActionBar(mToolbar);

        mLayout.setBackgroundColor(primary);

        // Change status bar color
        BusProvider.getInstance().post(
                new ChangeStatusColor(mActivity, primary));

        mPrimaryColor = primary;
    }

    private void changeAccent(int accent) {
        mRoundsSlider.setBackgroundColor(accent);
        mPausesSlider.setBackgroundColor(accent);

        mChangeBackground.setButtonColor(accent);
        mChangeBackground.setShadowColor(ColorUtil.darken(accent));
        mChangeAccent.setButtonColor(accent);
        mChangeAccent.setShadowColor(ColorUtil.darken(accent));

        mStartButton.setButtonColor(accent);
        mStartButton.setShadowColor(ColorUtil.darken(accent));

        mAccentColor = accent;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configure_game, container, false);
        ButterKnife.bind(this, view);

        // Setup the toolbar
        mActivity = (AppCompatActivity) getActivity();
        mToolbar.setTitle("Configure");
        mActivity.setSupportActionBar(mToolbar);

        try {
            mActivity.getSupportActionBar().setHomeButtonEnabled(true);
            mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("rounds", mRoundsSlider.getValue());
        outState.putInt("pauses", mPausesSlider.getValue());
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
        if (rounds <= mRoundsSlider.getMax()) {
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
            mPausesValue.setText("none");
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

    @OnClick({R.id.configure_color_background, R.id.configure_color_accent})
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
                        setRoundsValue(mRounds);
                        setPausesValue(mPauses);
                    }
                });
                break;
            default:
                Log.e(TAG, "Invalid view was clicked?");
        }
    }

    @OnClick(R.id.configure_start)
    public void launchGame() {
        createOptions().toLog();
    }

    private GameOptions createOptions() {
        GameOptions options = new GameOptions();
        options.setTitle(mOptions.getTitle());
        options.setType(mOptions.getType());
        options.setRounds(mRounds);
        options.setMaxPauses(mPauses);
        options.setColors(mPrimaryColor, mAccentColor);

        return options;
    }

}
