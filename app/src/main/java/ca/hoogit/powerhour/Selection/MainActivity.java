package ca.hoogit.powerhour.Selection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import ca.hoogit.powerhour.About.AboutActivity;
import ca.hoogit.powerhour.BaseActivity;
import ca.hoogit.powerhour.Configure.ConfigureGameFragment;
import ca.hoogit.powerhour.Configure.GameOptions;
import ca.hoogit.powerhour.Game.Action;
import ca.hoogit.powerhour.Game.Engine;
import ca.hoogit.powerhour.Game.GameEvent;
import ca.hoogit.powerhour.Game.GameModel;
import ca.hoogit.powerhour.Notifications.Constants;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Screen.GameScreen;
import ca.hoogit.powerhour.Util.BusProvider;
import ca.hoogit.powerhour.Util.StatusBarUtil;
import ca.hoogit.powerhour.Views.GameTypeItem;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends BaseActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    @Bind({R.id.type_power_hour, R.id.type_century_club, R.id.type_spartan, R.id.type_custom})
    List<GameTypeItem> mGameTypes;

    private FragmentManager mFragmentManager;

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
        mFragmentManager = getSupportFragmentManager();
        Fabric.with(this, new Crashlytics());

        super.onCreate(savedInstanceState);

        setupListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case android.R.id.home:
                reset();
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

    @Subscribe
    public void onGameEvent(GameEvent event) {
        switch (event.action) {
            case PRODUCE:
                if (findFragment("gameScreen") == null && !mChosen) {
                    launchGameScreen(event.game, false);
                    Log.i(TAG, "Game already exists, resuming game");
                }
                Log.d(TAG, "No game to resume, user must choose");
                break;
            case STOP:
                Fragment fragment = findFragment("gameScreen");
                mFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
                reset();
                break;
            case FINISH:
                // Show the game over screen
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
    }

    public Fragment findFragment(String name) {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }
        return mFragmentManager.findFragmentByTag(name);
    }

}
