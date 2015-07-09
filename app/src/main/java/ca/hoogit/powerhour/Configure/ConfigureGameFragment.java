package ca.hoogit.powerhour.Configure;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.BusProvider;
import ca.hoogit.powerhour.CloseFragmentEvent;
import ca.hoogit.powerhour.Game.GameOptions;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Util.ChangeStatusColor;
import ca.hoogit.powerhour.Util.ColorUtil;

/**
 */
public class ConfigureGameFragment extends Fragment {

    @Bind(R.id.appBar) Toolbar mToolbar;
    @Bind(R.id.configure_container) FrameLayout mLayout;

    private static final String ARG_OPTIONS = "game_options";
    private final String TAG = ConfigureGameFragment.class.getSimpleName();

    private GameOptions mOptions;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            BusProvider.getInstance().post(new CloseFragmentEvent(false));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configure_game, container, false);
        ButterKnife.bind(this, view);

        // Setup the toolbar
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        mToolbar.setTitle("Configure " + mOptions.getTitle());
        mToolbar.setBackgroundColor(mOptions.getBackgroundColor());
        activity.setSupportActionBar(mToolbar);

        try {
            activity.getSupportActionBar().setHomeButtonEnabled(true);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException ex) {
            Log.e(TAG, ex.getMessage());
        }

        mLayout.setBackgroundColor(mOptions.getBackgroundColor());

        // Change status bar color
        BusProvider.getInstance().post(new ChangeStatusColor(mOptions.getBackgroundColor()));

        return view;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void darkenStatusBar(AppCompatActivity activity) {
        int darkerBackground = ColorUtil.darken(mOptions.getBackgroundColor(), 0.5);
        activity.getWindow().setStatusBarColor(darkerBackground);

        mLayout.setBackgroundColor(mOptions.getBackgroundColor());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}
