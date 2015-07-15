package ca.hoogit.powerhour;

import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.Configure.ConfigureGameFragment;
import ca.hoogit.powerhour.Game.GameOptions;
import ca.hoogit.powerhour.Game.GameScreen;
import ca.hoogit.powerhour.Selection.ItemSelectedEvent;
import ca.hoogit.powerhour.Util.StatusBarUtil;
import ca.hoogit.powerhour.Views.GameTypeItem;


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
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        BusProvider.getInstance().register(this);

        mToolbar.setTitle("Choose an Option");
        setSupportActionBar(mToolbar);

        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            StatusBarUtil.getInstance().init(this);
            setupListeners();
        } else {
            savedInstanceState.putInt("original_bar_color", StatusBarUtil.getInstance().getOriginal());
        }
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
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() != 0) {
            StatusBarUtil.getInstance().resetColor(this);
            mFragmentManager.popBackStack();
            mChosen = false;
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
                mFragmentManager.popBackStack();
                StatusBarUtil.getInstance().resetColor(this);
                mChosen = false;
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
                        if (v.getId() == R.id.type_custom) {
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
        Fragment fragment = ConfigureGameFragment.newInstance(options);
        mFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .replace(R.id.container, fragment)
                .addToBackStack("configureScreen")
                .commit();
    }

    private void launchGame(GameOptions options) {
        launchGame(options, false);
    }

    private void launchGame(GameOptions options, boolean animate) {
        Log.i(TAG, "Launching game in " + options.getType().name() + " mode");
        Fragment fragment = GameScreen.newInstance(options);
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        if (animate) {
            ft.setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right);
        }
        ft.replace(R.id.container, fragment);
        ft.addToBackStack("gameScreen");
        ft.commit();
    }

}
