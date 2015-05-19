package com.iconasystems.christo.baalafinal;


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iconasystems.christo.utils.BarListAdapter;
import com.iconasystems.christo.utils.JSONParser;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable;


/**
 * A simple {@link Fragment} subclass.
 */
public class BarsFragment extends ListFragment /*implements ActionBar.OnNavigationListener*/ {
    public static final String TAG_BAR_NAME = "bar_name";
    public static final String TAG_BAR_ID = "bar_id";
    public static final String TAG_BAR_IMAGE = "bar_image";
    public static final String TAG_BARS = "bars";
    public static final String TAG_SUCCESS = "success";
    private static final String TAG_IMAGES = "images";
    private static final String TAG_IMAGE = "image";
    private static final String TAG_USER_ID = "user_id";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_POPULARITY = "popularity";

    public JSONParser jsonParser;
    public JSONArray barsArray = null;
    public ProgressDialog progressDialog;
    public ArrayList<HashMap<String, String>> barsList;

    public String user_id;

    public ImageView mBarImage;
    public TextView mBarName;
    public TextView mBarId;
    public ProgressBar mProgressBar;

    private SmoothProgressDrawable d;
    /*public static final String url_get_newest_bars = "http://api.baala-online.netii.net/get_bars.php";*/
    public static final String url_get_newest_bars = "http://10.0.3.2/baala/get_bars.php";
    private Typeface mTypefaceName;
    private Typeface mTypefaceDesc;
    private String[] imageUrls;
    private View rootView;
    private String[] colors;

    public BarsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_bars, container, false);
        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListView listView = getListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBarId = (TextView) view.findViewById(R.id.bar_list_id);
                final String bar_id = mBarId.getText().toString();

                Intent i = new Intent(getActivity(), RealDetailsActivity.class);
                i.putExtra(TAG_USER_ID, user_id);
                i.putExtra(TAG_BAR_ID, bar_id);
                startActivity(i);
            }
        });

        jsonParser = new JSONParser();



        barsList = new ArrayList<HashMap<String, String>>();

        user_id = getActivity().getIntent().getStringExtra(TAG_USER_ID);
        mProgressBar = new ProgressBar(getActivity(), null, R.attr.spbStyle);
        mProgressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 24));
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.GONE);

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

        new LoadNewestBars().execute();

        colors = new String[]{"#00000", "#FFFF00", "#FF0000"};

        mTypefaceName = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Thin.ttf");
        mTypefaceDesc = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Belle-West.otf");


        mBarImage = (ImageView) listView.findViewById(R.id.bar_photo);
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

                        Log.d("Bar Details", json.toString());

                        String bar_name = json.getString(TAG_BAR_NAME);
                        String bar_id = json.getString(TAG_BAR_ID);
                        String bar_image = json.getString(TAG_BAR_IMAGE);
                        String description = json.getString(TAG_DESCRIPTION);
                        String popularity = json.getString(TAG_POPULARITY);

                        // Log.d("Image Urls" , "Total Images = "+imageUrls.length);

                        HashMap<String, String> hashMap = new HashMap<String, String>();

                        hashMap.put(TAG_BAR_NAME, bar_name);
                        hashMap.put(TAG_BAR_IMAGE, bar_image);
                        hashMap.put(TAG_BAR_ID, bar_id);
                        hashMap.put(TAG_DESCRIPTION, description);
                        hashMap.put(TAG_POPULARITY, popularity);

                        barsList.add(hashMap);

                        Log.d("Details", barsList.toString());
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
            /*d.stop();*/
            BarListAdapter barListAdapter = new BarListAdapter(getActivity(), barsList, mTypefaceName, mTypefaceDesc);
            setListAdapter(barListAdapter);
            barListAdapter.notifyDataSetChanged();
        }
    }

    public int dpToPx(int dp) {
        Resources r = getActivity().getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, r.getDisplayMetrics());
        return px;
    }

}
