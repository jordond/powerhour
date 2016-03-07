package ca.hoogit.powerhour.Selection;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import ca.hoogit.powerhour.About.AboutActivity;
import ca.hoogit.powerhour.About.TourActivity;
import ca.hoogit.powerhour.BaseActivity;
import ca.hoogit.powerhour.Configure.ConfigureGameFragment;
import ca.hoogit.powerhour.Configure.GameOptions;
import ca.hoogit.powerhour.Game.Action;
import ca.hoogit.powerhour.Game.Engine;
import ca.hoogit.powerhour.Game.GameEvent;
import ca.hoogit.powerhour.Game.GameModel;
import ca.hoogit.powerhour.Game.State;
import ca.hoogit.powerhour.Game.WearData;
import ca.hoogit.powerhour.GameOver.GameOver;
import ca.hoogit.powerhour.Notifications.Constants;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Screen.GameScreen;
import ca.hoogit.powerhour.Util.BusProvider;
import ca.hoogit.powerhour.Util.PowerHourUtils;
import ca.hoogit.powerhour.Util.SharedPrefs;
import ca.hoogit.powerhour.Util.StatusBarUtil;
import ca.hoogit.powerhour.Views.GameTypeItem;
import ca.hoogit.powerhourshared.DataLayer.Consts;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_RESOLVE_ERROR = 1000;
    private final String TAG = MainActivity.class.getSimpleName();

    @Bind({R.id.type_power_hour, R.id.type_century_club, R.id.type_spartan, R.id.type_custom})
    List<GameTypeItem> mGameTypes;

    private FragmentManager mFragmentManager;

    private WearData mWearData;
    private boolean mResolvingError = false;

    private boolean mChosen;

    @Override
    protected int getToolbarColor() {
        return getResources().getColor(R.color.primary);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.menu_main;
    }

    @Override
    protected boolean getDisplayHomeAsUpEnabled() {
        return false;
    }

    @Override
    protected boolean getEventBusEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fabric.with(this, new Crashlytics());
        mFragmentManager = getSupportFragmentManager();
        mWearData = new WearData(this);

        super.onCreate(savedInstanceState);

        SharedPrefs prefs = SharedPrefs.get(this);

        if (prefs.isFirstRun()) {
            prefs.setFirstRun(false);
            startActivity(new Intent(this, TourActivity.class));
        }


        setupListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWearData.connect();
    }

    @Override
    protected void onStop() {
        mWearData.disconnect();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                startActivity(new Intent(this, TourActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case android.R.id.home:
                reset();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() != 0) {
            reset();
        } else if (Engine.initialized && !Engine.started()) {
            BusProvider.getInstance().post(new GameEvent(Action.STOP));
        } else {
            super.onBackPressed();
        }
    }

    int count = 0;

    @Subscribe
    public void onGameEvent(final GameEvent event) {
        switch (event.action) {
            case PRODUCE:
                if (findFragment("gameScreen") == null && !mChosen) {
                    launchGameScreen(event.game, false);
                    Log.i(TAG, "Game already exists, resuming game");
                } else {
                    Log.d(TAG, "Game already active");
                }
                break;
            case UPDATE:
                count += 100;
                if (event.game.is(State.NEW_ROUND)) {
                    mWearData.sendMessage(Consts.Paths.GAME_SHOT, "");
                } else if (count >= Consts.Game.WEAR_UPDATE_INTERVAL_IN_MILLISECONDS) {
                    mWearData.sendGameInformation(event.game);
                    count = 0;
                }
                break;
            case STOP:
                if (event.game != null && event.game.hasStarted()) {
                    Intent gameOver = new Intent(getApplication(), GameOver.class);
                    gameOver.putExtra("game", event.game);
                    mWearData.sendFinish(event.game);
                    startActivity(gameOver);
                    finish();
                } else {
                    Fragment fragment = findFragment("gameScreen");
                    mFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
                    reset();
                    mWearData.sendMessage(Consts.Paths.GAME_STOP, Consts.Game.FLAG_GAME_STOP);
                }
                Log.i(TAG, "Game was stopped early");
                break;
            case FINISH:
                PowerHourUtils.delay(500, new PowerHourUtils.OnDelay() {
                    @Override
                    public void run() {
                        Intent gameOver = new Intent(getApplication(), GameOver.class);
                        gameOver.putExtra("game", event.game);
                        mWearData.sendFinish(event.game);
                        reset();
                        startActivity(gameOver);
                        finish();
                    }
                });
                Log.i(TAG, "Game has been completed");
                break;
        }
    }

    private void setupListeners() {
        for (GameTypeItem item : mGameTypes) {
            final GameOptions options = item.getOptions();

            // Has configure button
            if (item.hasButton()) {
                item.setConfigureOnClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mChosen) {
                            Log.d(TAG, "Configure button for " + options.getType().name() + " was pressed");
                            Answers.getInstance().logCustom(new CustomEvent("Configuring Game")
                                    .putCustomAttribute("Type", options.getType().name()));
                            configureGame(options);
                            mChosen = true;
                        } else {
                            Log.d(TAG, "Someones been tapping too fast");
                        }
                    }
                });
            }
            item.setItemOnClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mChosen) {
                        Log.d(TAG, options.getType().name() + " mode was selected");
                        Answers.getInstance().logCustom(new CustomEvent("Game Chosen")
                                .putCustomAttribute("Type", options.getType().name()));
                        if (options.getType() == GameOptions.Type.CUSTOM) {
                            configureGame(options);
                        } else {
                            launchGame(options, true);
                        }
                        mChosen = true;
                    } else {
                        Log.d(TAG, "Someones been tapping too fast");
                    }
                }
            });
        }
    }

    private void configureGame(GameOptions options) {
        Log.i(TAG, "Configuring  " + options.getType().name());
        Fragment configure = findFragment("configure");
        if (configure == null) {
            configure = ConfigureGameFragment.newInstance(options);
        }
        mFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .replace(R.id.container, configure)
                .addToBackStack("configure")
                .commit();
    }

    private void reset() {
        mFragmentManager.popBackStack();
        StatusBarUtil.getInstance().resetColor(this);
        mChosen = false;
    }

    /**
     * Otto event
     * Called from {@link ConfigureGameFragment#launchGame()}
     *
     * @param options game settings
     */
    @Subscribe
    public void onStartGame(GameOptions options) {
        launchGame(options, true);
    }

    private void launchGame(GameOptions options, boolean animate) {
        GameModel gameModel = new GameModel(options);
        if (!Engine.initialized) {
            Intent initEngine = new Intent(this, Engine.class);
            initEngine.setAction(Constants.ACTION.INITIALIZE_GAME);
            initEngine.putExtra("game", gameModel);
            startService(initEngine);
            Log.d(TAG, "Starting new game");
        }
        Log.i(TAG, "Launching game in " + options.getType().name() + " mode");
        launchGameScreen(gameModel, animate);
    }

    /**
     * Launch the game given the options
     *
     * @param animate whether or not to animate the transition
     */
    private void launchGameScreen(GameModel gameModel, boolean animate) {
        Fragment gameScreen = findFragment("gameScreen");
        if (gameScreen == null) {
            gameScreen = GameScreen.newInstance(gameModel);
        }
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        if (animate) {
            ft.setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right);
        }
        ft.replace(R.id.container, gameScreen, "gameScreen");
        ft.commit();

        mWearData.sendStartActivity();
        mWearData.sendGameInformation(gameModel);
    }

    public Fragment findFragment(String name) {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }
        return mFragmentManager.findFragmentByTag(name);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("chosen", mChosen);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mChosen = savedInstanceState.getBoolean("chosen");
    }
}
