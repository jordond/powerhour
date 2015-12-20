package ca.hoogit.powerhour_wear;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import ca.hoogit.powerhour_wear.Fragments.ControlsFragment;
import ca.hoogit.powerhour_wear.Fragments.GameScreenFragment;

public class MainActivity extends WearableActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Containing layout
     */
    private RelativeLayout mContainer;
    private GridViewPager mPager;

    /**
     * Fragments
     */
    private GameScreenFragment mGameScreen;
    private ControlsFragment mControls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        final WatchViewStub viewStub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        viewStub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mContainer = (RelativeLayout) stub.findViewById(R.id.game_screen);
                mPager = (GridViewPager) stub.findViewById(R.id.pager);
                mPager.setOffscreenPageCount(1);
                DotsPageIndicator indicator = (DotsPageIndicator) stub.findViewById(R.id.page_indicator);
                indicator.setDotSpacing((int) getResources().getDimension(R.dimen.dots_spacing));
                indicator.setPager(mPager);

                List<Fragment> pages = new ArrayList<>();
                mGameScreen = new GameScreenFragment();
                mControls = new ControlsFragment();
                pages.add(mGameScreen);
                pages.add(mControls);

                final GamePagerAdapter adapter = new GamePagerAdapter(getFragmentManager(), pages);
                mPager.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        Log.d(TAG, "updateDisplay: Ambient mode " + isAmbient());
        int colorId = isAmbient() ? android.R.color.black : R.color.primary;
        mPager.setCurrentItem(0, 0 , false);
        mContainer.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), colorId));
        mGameScreen.updateScreen(isAmbient());
    }
}
