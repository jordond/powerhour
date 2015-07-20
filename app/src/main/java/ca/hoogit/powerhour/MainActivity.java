package ca.hoogit.powerhour;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.Configure.ConfigureGameFragment;
import ca.hoogit.powerhour.Game.Engine;
import ca.hoogit.powerhour.Game.Game;
import ca.hoogit.powerhour.Game.GameEvent;
import ca.hoogit.powerhour.Game.GameOptions;
import ca.hoogit.powerhour.Game.GameScreen;
import ca.hoogit.powerhour.Util.StatusBarUtil;
import ca.hoogit.powerhour.Views.GameTypeItem;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.appBar)
    Toolbar mToolbar;

    @Bind(R.id.type_statistics)
    GameTypeItem mStatistics;
    @Bind({R.id.type_power_hour, R.id.type_century_club, R.id.type_spartan, R.id.type_custom})
    List<GameTypeItem> mGameTypes;

    private FragmentManager mFragmentManager;

    private boolean mChosen;

    public enum FragmentEvents {
        HOME_PRESSED, BACK_PRESSED, NOTHING
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        BusProvider.getInstance().register(this);

        mToolbar.setTitle("Choose an Option");
        setSupportActionBar(mToolbar);

        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            savedInstanceState = new Bundle();
            StatusBarUtil.getInstance().init(this);
            savedInstanceState.putInt("original_bar_color",
                    StatusBarUtil.getInstance().getOriginal());
        }

        if (Engine.started()) {
            launchGameScreen(Engine.details(), false);
            Log.i(TAG, "Game already exists, resuming game");
        }
        setupListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() { // TODO check if game has been started
        if (mFragmentManager.getBackStackEntryCount() != 0) {
            reset();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onFragmentEvent(FragmentEvents event) {
        switch (event) {
            case HOME_PRESSED:
            case BACK_PRESSED:
                reset();
                break;
            case NOTHING:
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
        // TODO implement
        mStatistics.setItemOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Stats was pressed");
                Toast.makeText(getApplication(), "Not implemented yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configureGame(GameOptions options) {
        Log.i(TAG, "Configuring  " + options.getType().name());
        Fragment configure = mFragmentManager.findFragmentByTag("configure");
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

    /**
     * Launch the game given the options
     *
     * @param animate whether or not to animate the transition
     */
    private void launchGameScreen(Game game, boolean animate) {
        Fragment gameScreen = mFragmentManager.findFragmentByTag("gameScreen");
        if (gameScreen == null) {
            gameScreen = GameScreen.newInstance(game);
        }
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        if (animate) {
            ft.setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right);
        }
        ft.replace(R.id.container, gameScreen, "gameScreen");
        ft.commit();
    }

    private void launchGame(GameOptions options, boolean animate) {
        Game game = new Game(options);
        if (!Engine.initialized) {
            Intent initEngine = new Intent(this, Engine.class);
            initEngine.setAction(Engine.ACTION_INITIALIZE_GAME);
            initEngine.putExtra("game", game);
            startService(initEngine);
            Log.d(TAG, "Starting new game");
        }
        Log.i(TAG, "Launching game in " + options.getType().name() + " mode");
        launchGameScreen(game, animate);
    }

    @Subscribe
    public void onGameEvent(GameEvent event) {
        switch (event.action) {
            case STOP:
                Fragment fragment = mFragmentManager.findFragmentByTag("gameScreen");
                mFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
                reset();
                break;
            case FINISH:
                // Show the game over screen
                break;
        }
    }

}
