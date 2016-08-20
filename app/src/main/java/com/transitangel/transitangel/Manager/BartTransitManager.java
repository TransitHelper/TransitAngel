package com.transitangel.transitangel.Manager;

import com.loopj.android.http.AsyncHttpClient;
import com.transitangel.transitangel.utils.TAConstants;

/**
 * Created by vidhurvoora on 8/18/16.
 */
public class BartTransitManager extends TransitManager {
    private static BartTransitManager sInstance;

    public static synchronized BartTransitManager getSharedInstance() {
        if ( sInstance == null ) {
            sInstance = new BartTransitManager();
            httpClient = new AsyncHttpClient();
            mTransitType = TAConstants.TRANSIT_TYPE.BART;
        }
        return sInstance;
    }


}
