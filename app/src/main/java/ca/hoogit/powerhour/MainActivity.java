package ca.hoogit.powerhour;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.Configure.ConfigureGameFragment;
import ca.hoogit.powerhour.Game.GameOptions;
import ca.hoogit.powerhour.Selection.ItemSelectedEvent;
import ca.hoogit.powerhour.Util.StatusBarUtil;
import ca.hoogit.powerhour.Views.GameTypeItem;


public class MainActivity extends AppCompatActivity {

    public final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.appBar)
    Toolbar mToolbar;

    @Bind(R.id.type_statistics)
    GameTypeItem mStatistics;
    @Bind({R.id.type_power_hour, R.id.type_century_club, R.id.type_spartan, R.id.type_custom})
    List<GameTypeItem> mGameTypes;

    private FragmentManager mFragmentManager;
    private boolean mItemSelected;

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

    public void replaceFragment(Fragment fragment) {
        mFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
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
        if(mFragmentManager.getBackStackEntryCount() != 0) {
            StatusBarUtil.getInstance().resetColor(this);
            mFragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    public void onItemSelected(ItemSelectedEvent item) {
        if (item.isConfiguring || item.options.getType() == GameOptions.Type.CUSTOM) {
            Log.i(TAG, "Configuring  " + item.options.getTitle());
            Fragment fragment = ConfigureGameFragment.newInstance(item.options);
            replaceFragment(fragment);
        } else if (item.itemIsAGame) {
            // TODO Launch Game activity
            Log.i(TAG, "Starting new game in " + item.options.getTitle() + " mode.");
        }
    }

    @Subscribe
    public void onCloseFragment(CloseFragmentEvent e) {
        if (e.launchFragment) {
            //TODO make a fragment util?
            mFragmentManager.beginTransaction()
                    .replace(R.id.container, e.fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            mFragmentManager.popBackStack();
        }
        StatusBarUtil.getInstance().resetColor(this);
    }

    public void setupListeners() {
        for (GameTypeItem item : mGameTypes) {
            final GameOptions options = item.getOptions();

            // Has configure button
            if (item.hasButton()) {
                item.setConfigureOnClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Configure button for " + options.getTitle() + " was pressed");
                        ItemSelectedEvent event = new ItemSelectedEvent(options, true);
                        delayEvent(event);
                    }
                });
            }
            item.setItemOnClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, options.getTitle() + " mode was selected");
                    ItemSelectedEvent event = new ItemSelectedEvent(options);
                    delayEvent(event);
                }
            });
        }
        // TODO implement
        mStatistics.setItemOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Stats was pressed");
            }
        });
    }

    public void delayEvent(final Object event) {
        if (!mItemSelected) {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            onItemSelected((ItemSelectedEvent) event);
                            mItemSelected = false;
                        }
                    },
                    300);
            mItemSelected = true;
        }
    }
}
