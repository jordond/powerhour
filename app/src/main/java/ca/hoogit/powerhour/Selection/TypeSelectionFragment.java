package ca.hoogit.powerhour.Selection;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.powerhour.BusProvider;
import ca.hoogit.powerhour.Game.GameOptions;
import ca.hoogit.powerhour.R;
import ca.hoogit.powerhour.Views.GameTypeItem;

/**
 */
public class TypeSelectionFragment extends Fragment {

    public final String TAG = TypeSelectionFragment.class.getSimpleName();

    @Bind(R.id.appBar)
    Toolbar mToolbar;
    @Bind(R.id.type_statistics)
    GameTypeItem mStatistics;
    @Bind({R.id.type_power_hour, R.id.type_century_club, R.id.type_spartan, R.id.type_custom})
    List<GameTypeItem> mGameTypes;

    private boolean mItemSelected;

    public static TypeSelectionFragment newInstance() {
        return new TypeSelectionFragment();
    }

    public TypeSelectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_type_selection, container, false);
        ButterKnife.bind(this, view);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        mToolbar.setTitle("Choose an Option");
        activity.setSupportActionBar(mToolbar);

        setupListeners();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
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
                Log.d("TypeSelectionFragment", "Stats was pressed");
            }
        });
    }

    public void delayEvent(final Object event) {
        if (!mItemSelected) {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            BusProvider.getInstance().post(event);
                            mItemSelected = false;
                        }
                    },
                    300);
            mItemSelected = true;
        }
    }
}