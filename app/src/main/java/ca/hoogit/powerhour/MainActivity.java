package ca.hoogit.powerhour;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.Configure.ConfigureGameFragment;
import ca.hoogit.powerhour.Selection.ItemSelectedEvent;
import ca.hoogit.powerhour.Selection.TypeSelectionFragment;
import ca.hoogit.powerhour.Util.ChangeStatusColor;
import ca.hoogit.powerhour.Util.ColorUtil;


public class MainActivity extends AppCompatActivity {

    public final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.appBar)
    Toolbar mToolbar;

    private FragmentManager mFragmentManager;
    private ChangeStatusColor mOriginalStatusColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        BusProvider.getInstance().register(this);

        setSupportActionBar(mToolbar);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            mOriginalStatusColor = new ChangeStatusColor(getWindow().getStatusBarColor());
        }

        mFragmentManager = getFragmentManager();

        Fragment typeSelection = TypeSelectionFragment.newInstance();
        mFragmentManager.beginTransaction()
                .replace(R.id.container, typeSelection)
                .commit();
    }

    public void replaceFragment(Fragment fragment) {
        mFragmentManager.beginTransaction()
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

    @Subscribe
    public void onItemSelected(ItemSelectedEvent item) {
        if (item.isConfiguring) {
            Fragment fragment = ConfigureGameFragment.newInstance(item.options);
            replaceFragment(fragment);
        } else if (item.itemIsAGame) {
            // TODO Launch Game activity
            Log.d(TAG, "Starting new game in " + item.options.getTitle() + " mode.");
        }
    }

    @Subscribe
    public void onCloseFragment(CloseFragmentEvent e) {
        if (e.launchFragment) {
            replaceFragment(e.fragment);
        } else {
            this.getFragmentManager().popBackStack();
        }
        onChangeStatusColor(mOriginalStatusColor);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Subscribe
    public void onChangeStatusColor(ChangeStatusColor color) {
        if (Build.VERSION.SDK_INT >= 21) {
            int darkerBackground = ColorUtil.darken(color.getColor(), 0.3);
            getWindow().setStatusBarColor(darkerBackground);
        }
    }
}
