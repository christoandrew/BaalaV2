package com.iconasystems.christo.baalafinal;


import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.iconasystems.christo.utils.EventListAdapter;
import com.iconasystems.christo.utils.JSONParser;
import com.iconasystems.christo.utils.LocationUtils;
import com.iconasystems.christo.utils.PhotoStripAdapter;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.TwoWayView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    private static final String TAG_EVENT_DATE = "event_date";
    private static final String TAG_EVENT_NAME = "event_name";
    private static final String TAG_EVENT_ID = "event_id";
    private static final String TAG_EVENT_LOCATION = "bar_name";
    private static final String TAG_EVENT_DRESS_CODE = "event_dress_code";
    private static final String TAG_EVENT_IMAGE = "event_image";
    private static final String TAG_EVENT_ENTRANCE = "entrance";
    private static final String TAG_EVENTS = "events";
    public static final String TAG_BAR_NAME = "bar_name";
    public static final String TAG_BAR_ID = "bar_id";
    public static final String TAG_BAR_IMAGE = "bar_image";
    public static final String TAG_BARS = "bars";
    public static final String TAG_SUCCESS = "success";

    private JSONParser jsonParser;
    private ArrayList<HashMap<String, String>> eventsList;
    private JSONArray events;

    private ProgressDialog progressDialog;

    /*private static final String url_get_all_events = "http://api.baala-online.netii.net/get_all_events.php";
    public static final String url_get_newest_bars = "http://api.baala-online.netii.net/get_bars.php";*/
    private static final String url_get_all_events = "http://10.0.3.2/baala/get_all_events.php";
    public static final String url_get_newest_bars = "http://10.0.3.2/baala/get_bars.php";

    private ListView listView;
    private View rootView;

    private GoogleMap googleMap;
    private ProgressBar mProgressBar;
    private SmoothProgressDrawable d;
    private TwoWayView mPhotoStrip;
    private JSONArray barsArray = null;
    private ArrayList<HashMap<String, String>> barsList;

    private SupportMapFragment fragment;

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;
    private boolean mUpdatesRequested;

    public HomeFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mPhotoStrip = (TwoWayView) rootView.findViewById(R.id.two_way_view);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {

            initializeMap();

            // Changing map type
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            //  googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            // googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            // googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            // googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

            // Showing / hiding your current location
            googleMap.setMyLocationEnabled(true);

            // Enable / Disable zooming controls
            googleMap.getUiSettings().setZoomControlsEnabled(false);

            // Enable / Disable my location button
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);

            // Enable / Disable Compass icon
            googleMap.getUiSettings().setCompassEnabled(false);

            // Enable / Disable Rotate gesture
            googleMap.getUiSettings().setRotateGesturesEnabled(true);

            // Enable / Disable zooming functionality
            googleMap.getUiSettings().setZoomGesturesEnabled(false);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Note that location updates are off until the user turns them on
        mUpdatesRequested = false;

        // Open Shared Preferences
        mPrefs = getActivity().getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get an editor
        mEditor = mPrefs.edit();

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(getActivity(), this, this);


        eventsList = new ArrayList<HashMap<String, String>>();
        jsonParser = new JSONParser();

        listView = (ListView) rootView.findViewById(R.id.list_view_event);

        barsList = new ArrayList<HashMap<String, String>>();

        mProgressBar = new ProgressBar(getActivity(), null, R.attr.spbStyle);
        mProgressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 24));
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.GONE);

        final ItemClickSupport itemClick = ItemClickSupport.addTo(mPhotoStrip);
        mPhotoStrip.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                if (!(mPhotoStrip.getFirstVisiblePosition() == 0 || mPhotoStrip.getFirstVisiblePosition() > 0))
                    mPhotoStrip.scrollToPosition(0);
            }
        });

        final FrameLayout decorView = (FrameLayout) getActivity().getWindow().getDecorView();

        SmoothProgressDrawable.Builder builder = new SmoothProgressDrawable.Builder(getActivity());
        builder.speed(5)
                .sectionsCount(3)
                .separatorLength(dpToPx(0))
                .width(dpToPx(4))
                .mirrorMode(true)
                .reversed(true);

        Interpolator interpolator = new AccelerateDecelerateInterpolator();
        builder.interpolator(interpolator);

        builder.colors(this.getResources().getIntArray(R.array.uganda));
        d = builder.build();

        d.setBounds(mProgressBar.getIndeterminateDrawable().getBounds());
        mProgressBar.setIndeterminateDrawable(d);
        d.start();

        decorView.addView(mProgressBar);

        final ViewTreeObserver observer = mProgressBar.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                View contentView = decorView.findViewById(android.R.id.content);
                mProgressBar.setY(contentView.getY() - 10);

                ViewTreeObserver observer1 = mProgressBar.getViewTreeObserver();
                observer1.removeOnGlobalLayoutListener(this);
            }
        });

        new LoadEvents().execute();
        new LoadNewestBars().execute();
    }


    public int dpToPx(int dp) {
        Resources r = this.getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, r.getDisplayMetrics());
        return px;
    }

    /*
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
    @Override
    public void onStop() {

        // If the client is connected
        if (mLocationClient.isConnected()) {
        }

        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();

        super.onStop();
    }

    /*
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
    @Override
    public void onPause() {

        // Save the current setting for updates
        mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
        mEditor.commit();

        super.onPause();
    }

    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {

        super.onStart();
        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        mLocationClient.connect();

    }

    @Override
    public void onResume() {
        super.onResume();
        initializeMap();

        // If the app already has a setting for getting location updates, get it
        if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
            mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);

            // Otherwise, turn off location updates until requested
        } else {
            mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            mEditor.commit();
        }
    }

    /**
     * Invoked by the "Get Location" button.
     * <p/>
     * Calls getLastLocation() to get the current location
     */
    /*public void getLocation() {

        // If Google Play Services is available
        if (servicesConnected()) {

            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bmp = Bitmap.createBitmap(150, 100, conf);
            Canvas canvas1 = new Canvas(bmp);

            // paint defines the text color,
            // stroke width, size
            Paint color = new Paint();
            color.setStrokeWidth(5);
            color.setColor(getActivity().getResources().getColor(R.color.transparent));

            // modify canvas
            *//*canvas1.drawCircle(55,55,55,color);
            canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.avatar), 150, 100, color);*//*

            // canvas1.drawText("User Name!", 30, 40, color);
            // Get the current location
            Location currentLocation = mLocationClient.getLastLocation();
            // Display the current location in the UI
            // Adding a marker
            MarkerOptions marker = new MarkerOptions();
            marker.title("You are Here");
            marker.position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

            googleMap.addMarker(marker);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 20));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))      // Sets the center of the map to location user
                    .zoom(14)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
                            // .tilt(45)                  // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            // Display the current location in the UI
            *//*mLatLng.setText(LocationUtils.getLatLng(getActivity(), currentLocation));*//*
        }
    }*/

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
            }
            return false;
        }
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /**
     * Called when the location has changed.
     * <p/>
     * <p> There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */
    @Override
    public void onLocationChanged(Location location) {

    }

    /**
     * Called when the provider status changes. This method is called when
     * a provider is unable to fetch a location or if the provider has recently
     * become available after a period of unavailability.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     * @param status   {@link android.location.LocationProvider#OUT_OF_SERVICE} if the
     *                 provider is out of service, and this is not expected to change in the
     *                 near future; {@link android.location.LocationProvider#TEMPORARILY_UNAVAILABLE} if
     *                 the provider is temporarily unavailable but is expected to be available
     *                 shortly; and {@link android.location.LocationProvider#AVAILABLE} if the
     *                 provider is currently available.
     * @param extras   an optional Bundle which will contain provider specific
     *                 status variables.
     *                 <p/>
     *                 <p> A number of common key/value pairs for the extras Bundle are listed
     *                 below. Providers that use any of the keys on this list must
     *                 provide the corresponding value as described below.
     *                 <p/>
     *                 <ul>
     *                 <li> satellites - the number of satellites used to derive the fix
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderEnabled(String provider) {

    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        //getLocation();
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * function to load map If map is not created it will create it for you
     */
    private void initializeMap() {
        if (googleMap == null) {
            FragmentManager fragmentManager = getChildFragmentManager();
            fragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.current_location);
            if (fragment == null) {
                fragment = SupportMapFragment.newInstance();
            }
            googleMap = fragment.getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getActivity(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    class LoadNewestBars extends AsyncTask<String, String, String> {

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> data = new ArrayList<NameValuePair>();

            JSONObject jsonObject = jsonParser.makeHttpRequest(url_get_newest_bars, "GET", data);

            Log.d("All Bars", jsonObject.toString());

            try {
                int success = jsonObject.getInt(TAG_SUCCESS);

                if (success == 1) {
                    barsArray = jsonObject.getJSONArray(TAG_BARS);

                    for (int i = 0; i < barsArray.length(); i++) {
                        JSONObject json = barsArray.getJSONObject(i);

                        String bar_name = json.getString(TAG_BAR_NAME);
                        String bar_id = json.getString(TAG_BAR_ID);
                        String bar_image = json.getString(TAG_BAR_IMAGE);

                        // Log.d("Image Urls" , "Total Images = "+imageUrls.length);

                        HashMap<String, String> hashMap = new HashMap<String, String>();

                        hashMap.put(TAG_BAR_NAME, bar_name);
                        hashMap.put(TAG_BAR_IMAGE, bar_image);
                        hashMap.put(TAG_BAR_ID, bar_id);


                        barsList.add(hashMap);
                    }
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Error Occurred", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);
            /*progressDialog.dismiss();*/

            mProgressBar.setVisibility(View.GONE);

            mPhotoStrip.setAdapter(new PhotoStripAdapter(barsList, getActivity(), mPhotoStrip));
        }
    }


    class LoadEvents extends AsyncTask<String, String, String> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> data = new ArrayList<NameValuePair>();

            try {
                JSONObject jsonObject = jsonParser.makeHttpRequest(url_get_all_events, "GET", data);

                Log.d("All Events", jsonObject.toString());

                try {
                    int success = jsonObject.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        events = jsonObject.getJSONArray(TAG_EVENTS);
                        for (int i = 0; i < events.length(); i++) {
                            JSONObject event = events.getJSONObject(i);

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(TAG_EVENT_DATE, event.getString(TAG_EVENT_DATE));
                            map.put(TAG_EVENT_DRESS_CODE, event.getString(TAG_EVENT_DRESS_CODE));
                            map.put(TAG_EVENT_ENTRANCE, event.getString(TAG_EVENT_ENTRANCE));
                            map.put(TAG_EVENT_IMAGE, event.getString(TAG_EVENT_IMAGE));
                            map.put(TAG_EVENT_LOCATION, event.getString(TAG_EVENT_LOCATION));
                            map.put(TAG_EVENT_ID, event.getString(TAG_EVENT_ID));
                            map.put(TAG_EVENT_NAME, event.getString(TAG_EVENT_NAME));

                            eventsList.add(map);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            EventListAdapter eventListAdapter = new EventListAdapter(getActivity(), eventsList);
            eventListAdapter.notifyDataSetChanged();
            listView.setAdapter(eventListAdapter);

            mProgressBar.setVisibility(View.GONE);
        }
    }

}
