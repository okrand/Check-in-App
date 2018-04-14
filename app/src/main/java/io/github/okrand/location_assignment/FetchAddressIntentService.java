package io.github.okrand.location_assignment;


import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * IntentService for fetching the address for a given location.
 */
public class FetchAddressIntentService extends IntentService {
    private static String TAG = "FetchAddress";
    protected ResultReceiver mReceiver;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchAddressIntentService(String name) {
        super("FetchAddressIntentService");
    }

    /**
     * Fetches the address for a given location.
     *
     * @param intent The value passed to {@link #startService(Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        // Get receiver to send the results to
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        if (mReceiver == null) {
            Log.e("Geofencing", "No receiver received. There is nowhere to send the results.");
            return;
        }

        // Get location
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        if (location == null) {
            errorMessage = "No location specified.";
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            return;
        }

        // Get geocoder
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        if (geocoder.isPresent()) {

            List<Address> addresses = null;

            // Fetch address
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (IllegalArgumentException illegalArgumentException) {
                Log.e("Geofencing", errorMessage + ". " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " + location.getLongitude(), illegalArgumentException);
            }

            if (addresses == null || addresses.size() == 0) {
                errorMessage = "No address found at location.";
                deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            } else {
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread.
                for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                Log.i(TAG, "Address Found");
                deliverResultToReceiver(Constants.SUCCESS_RESULT,
                        TextUtils.join(System.getProperty("line.separator"),
                                addressFragments));

            }

        } else {
            errorMessage = "No geocoder present.";
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        }

    }

    /**
     * Send the results back to the receiver.
     *
     * @param resultCode The result code to send back to the receiver.
     * @param message   The message to send back to the receiver.
     */
    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

    /**
     * Constants for {@link FetchAddressIntentService}.
     */
    public final class Constants {
        /**
         * Constant to indicate a success.
         */
        public static final int SUCCESS_RESULT = 0;
        /**
         * Constant to indicate a failure.
         */
        public static final int FAILURE_RESULT = 1;
        /**
         * Constant for the package name.
         */
        public static final String PACKAGE_NAME =
                "com.awesomeness.justinwhitlock.locationlog";
        /**
         * Constant for the receiver extra of the Intent.
         */
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        /**
         * Constant for the result data.
         */
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        /**
         * Constant for the location extra of the Intent.
         */
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }
}
