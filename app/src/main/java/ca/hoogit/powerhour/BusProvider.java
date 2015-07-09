package ca.hoogit.powerhour;

import com.squareup.otto.Bus;

/**
 * Created by jordon on 08/07/15.
 */
public final class BusProvider {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
    }
}
