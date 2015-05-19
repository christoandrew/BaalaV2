package com.iconasystems.christo.baalafinal;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.iconasystems.christo.utils.DirectionsJSONParser;
import com.iconasystems.christo.utils.GMapV2Direction;
import com.iconasystems.christo.utils.JSONParser;
import com.iconasystems.christo.utils.LocationsAdapter;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RouteActivity extends FragmentActivity implements LocationListener {
    GoogleMap mGoogleMap;
    SupportMapFragment fragment;
    ArrayList<LatLng> mMarkerPoints;
    double mLatitude = 0;
    double mLongitude = 0;
    JSONParser jsonParser = new JSONParser();
    LatLng origin = new LatLng(0.31578,32.589086);
    LatLng dest = new LatLng(0.3110758, 32.62515);
    private TextView mInstructions;
    private ListView mDirections;
    private static final String TAG_INSTRUCTION = "instruction";
    private static final String TAG_DURATION = "duration";
    private static final String TAG_DISTANCE = "distance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        //mInstructions = (TextView) findViewById(R.id.instruction);

        mDirections = (ListView) findViewById(R.id.list);
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getBaseContext());
        if (status != ConnectionResult.SUCCESS) { // Google Play Services are
            // not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
                    requestCode);
            dialog.show();

        } else { // Google Play Services are available

            // Initializing
            mMarkerPoints = new ArrayList<LatLng>();

			/*
			 * // Getting reference to SupportMapFragment of the activity_main
			 * MapFragment fm =
			 * (MapFragment)getFragmentManager().findFragmentById(R.id.map);
			 *
			 * // Getting Map for the SupportMapFragment mGoogleMap =
			 * fm.getMap();
			 */
            initializeMap();

            // Enable MyLocation Button in the Map
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            // Getting LocationManager object from System Service
            // LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location From GPS
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                onLocationChanged(location);
            }

            // locationManager.requestLocationUpdates(provider, 20000, 0, this);

            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);

            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
            getLocation(origin);

            // Setting onclick event listener for the map
            mGoogleMap
                    .setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                        @Override
                        public void onMapClick(LatLng point) {

                            // Already map contain destination location
                            if (mMarkerPoints.size() > 1) {

                                FragmentManager fm = getSupportFragmentManager();
                                mMarkerPoints.clear();
                                mGoogleMap.clear();
                                LatLng startPoint = new LatLng(mLatitude,
                                        mLongitude);
                                drawMarker(startPoint);
                            }

                            drawMarker(point);

                            // Checks, whether start and end locations are
                            // captured
                            if (mMarkerPoints.size() >= 2) {
                                LatLng origin = mMarkerPoints.get(0);
                                LatLng dest = mMarkerPoints.get(1);

                            }
                        }
                    });

        }
    }

    public void getLocation(LatLng startPoint) {

        // If Google Play Services is available

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(150, 100, conf);
        Canvas canvas1 = new Canvas(bmp);

        // paint defines the text color,
        // stroke width, size
		/*
		 * Paint color = new Paint(); color.setStrokeWidth(5);
		 * color.setColor(this.getResources().getColor(R.color.transparent));
		 *
		 * // modify canvas canvas1.drawCircle(55,55,55,color);
		 * canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
		 * R.drawable.avatar), 150, 100, color);
		 */

        // canvas1.drawText("User Name!", 30, 40, color);
        // Get the current location

        // Display the current location in the UI
        // Adding a marker
        MarkerOptions marker = new MarkerOptions();
        marker.title("You are Here");
        marker.position(new LatLng(0.3110758, 32.62515));
        marker.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

        mGoogleMap.addMarker(marker);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 20));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(startPoint) // Sets the center of
                        // the map to
                        // location user
                .zoom(14) // Sets the zoom
                .bearing(0) // Sets the orientation of the camera to east
                        // .tilt(45) // Sets the tilt of the camera to 30 degrees
                .build(); // Creates a CameraPosition from the builder
        mGoogleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
        // Display the current location in the UI
        // *mLatLng.setText(LocationUtils.getLatLng(getActivity(),
        // currentLocation));*//*

    }

    /**
     * function to load map If map is not created it will create it for you
     */
    private void initializeMap() {
        if (mGoogleMap == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragment = (SupportMapFragment) fragmentManager
                    .findFragmentById(R.id.routeView);
            if (fragment == null) {
                fragment = SupportMapFragment.newInstance();
            }
            mGoogleMap = fragment.getMap();

            // check if map is created successfully or not
            if (mGoogleMap == null) {
                Toast.makeText(this, "Sorry! unable to create maps",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + ","
                + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters;

        return url;
    }

    /** A class to download data from Google Directions URL */
    private class DownloadTask extends AsyncTask<String, Void, JSONObject> {
        JSONObject jsonObject;
        List<List<HashMap<String, String>>> routes = null;

        // Downloading data in non-ui thread
        @Override
        protected JSONObject doInBackground(String... url) {
            List<NameValuePair> data = new ArrayList<NameValuePair>();

            try {
                // Get the json object and send it to the directionsparser
                jsonObject = jsonParser.makeHttpRequest(url[0], "GET", data);

                //Log.d("Array Size", ""+writeInstructions(jsonObject));
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return jsonObject;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            LocationsAdapter adapter = new LocationsAdapter(getData(result), RouteActivity.this);
            mDirections.setAdapter(adapter);

            DirectionsJSONParser parser = new DirectionsJSONParser();
            routes = parser.parse(result);

            drawPolyline(routes);

            // Log.d("Routes", result.toString());

            // ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            // parserTask.execute(result);

        }
    }

    protected ArrayList<HashMap<String, String>> getData(JSONObject jObj) {
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

        int length = 0;
        try {
            jRoutes = jObj.getJSONArray("routes");
            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                JSONObject legObj = jRoutes.getJSONObject(i);
                jLegs = legObj.getJSONArray("legs");
                for (int j = 0; j < jLegs.length(); j++) {
                    JSONObject obj = jLegs.getJSONObject(j);
                    JSONArray steps = obj.getJSONArray("steps");

                    for (int k = 0; k < steps.length(); k++) {
                        JSONObject stepObj = steps.getJSONObject(k);
                        String stepDuration = stepObj.getJSONObject("duration").getString("text");
                        String stepDistance = stepObj.getJSONObject("distance").getString("text");
                        String stepInstruction = stepObj.getString("html_instructions");

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(TAG_INSTRUCTION, stepInstruction);
                        map.put(TAG_DISTANCE, stepDistance);
                        map.put(TAG_DURATION, stepDuration);

                        data.add(map);

                        //Log.d("Directions",stepInstruction+" "+stepDuration+" "+stepDistance );

                    }

                }

            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return data;
    }



    /** A class to parse the Google Directions in JSON format */
    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);

            }

            // Drawing polyline in the Google Map for the i-th route
            mGoogleMap.addPolyline(lineOptions);
        }
    }

    protected void drawPolyline(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(2);
            lineOptions.color(Color.RED);

        }

        // Drawing polyline in the Google Map for the i-th route
        mGoogleMap.addPolyline(lineOptions);
    }

    private void drawMarker(LatLng point) {
        mMarkerPoints.add(point);

        // Creating MarkerOptions
        MarkerOptions options = new MarkerOptions();

        // Setting the position of the marker
        options.position(point);

        /**
         * For the start location, the color of marker is GREEN and for the end
         * location, the color of marker is RED.
         */
        if (mMarkerPoints.size() == 1) {
            options.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else if (mMarkerPoints.size() == 2) {
            options.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        // Add new marker to the Google Map Android API V2
        mGoogleMap.addMarker(options);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
