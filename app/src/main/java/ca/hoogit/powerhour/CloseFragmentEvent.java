package ca.hoogit.powerhour;

import android.support.v4.app.Fragment;

/**
 * Created by jordon on 09/07/15.
 */
public class CloseFragmentEvent {
    public final boolean launchFragment;
    public Fragment fragment;

    public CloseFragmentEvent(boolean launchFragment) {
        this.launchFragment = launchFragment;
    }

    public CloseFragmentEvent(boolean launchFragment, Fragment fragment) {
        this.launchFragment = launchFragment;
        this.fragment = fragment;
    }
}
