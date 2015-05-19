package com.iconasystems.christo.baalafinal;


import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iconasystems.christo.utils.BarListAdapter;
import com.iconasystems.christo.utils.JSONParser;
import com.iconasystems.christo.utils.SessionManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
public class FavoritesFragment extends ListFragment {
    public static final String TAG_BAR_NAME = "bar_name";
    public static final String TAG_BAR_ID = "bar_id";
    public static final String TAG_BAR_IMAGE = "bar_image";
    public static final String TAG_FAV_BARS = "fav_bars";
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_USER_ID = "user_id";
    private static final String TAG_DESCRIPTION = "description";

    private HashMap<String, String> userDetails;
    private String user_id;

    private SessionManager session;

    public JSONParser jsonParser;
    public JSONArray barsArray = null;
    public ProgressDialog progressDialog;
    public ArrayList<HashMap<String, String>> barsList;

   /* public static final String url_get_newest_bars = "http://api.baala-online.netii.net/get_favorites.php";*/
   public static final String url_get_newest_bars = "http://10.0.3.2/baala/get_favorites.php";

    public TextView mBarId;
    private Typeface mTypeface;
    private Typeface mTypefaceDesc;
    private ProgressBar mProgressBar;
    private SmoothProgressDrawable d;

    public FavoritesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        session = new SessionManager(getActivity());

        mTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Thin.ttf");
        mTypefaceDesc = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Belle-West.otf");
        jsonParser = new JSONParser();

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

        new LoadFavoriteBars().execute();
        barsList = new ArrayList<HashMap<String, String>>();

        userDetails = session.getUserDetails();
        user_id = userDetails.get(SessionManager.KEY_USER_ID);

        ListView listView = getListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBarId = (TextView) view.findViewById(R.id.bar_list_id);
                final String bar_id = mBarId.getText().toString();

                Intent i = new Intent(getActivity(), RealDetailsActivity.class);
                i.putExtra(TAG_BAR_ID, bar_id);
                startActivity(i);

            }
        });

    }

    public int dpToPx(int dp) {
        Resources r = getActivity().getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, r.getDisplayMetrics());
        return px;
    }


    class LoadFavoriteBars extends AsyncTask<String, String, String> {
        @Override
        public void onPreExecute() {
            super.onPreExecute();
            /*progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading Favorites...Please Wait");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();*/
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> data = new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair(TAG_USER_ID, user_id));

            JSONObject jsonObject = jsonParser.makeHttpRequest(url_get_newest_bars, "GET", data);

            Log.d("Favorite Bars", jsonObject.toString());

            try {
                int success = jsonObject.getInt(TAG_SUCCESS);

                if (success == 1) {
                    barsArray = jsonObject.getJSONArray(TAG_FAV_BARS);

                    for (int i = 0; i < barsArray.length(); i++) {
                        JSONObject json = barsArray.getJSONObject(i);

                        String bar_name = json.getString(TAG_BAR_NAME);
                        String bar_image = json.getString(TAG_BAR_IMAGE);
                        String bar_id = json.getString(TAG_BAR_ID);
                        String description = json.getString(TAG_DESCRIPTION);


                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap.put(TAG_DESCRIPTION, description);
                        hashMap.put(TAG_BAR_NAME, bar_name);
                        hashMap.put(TAG_BAR_ID, bar_id);
                        hashMap.put(TAG_BAR_IMAGE, bar_image);

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
           /* progressDialog.dismiss();*/
            mProgressBar.setVisibility(View.GONE);

            final BarListAdapter barListAdapter = new BarListAdapter(getActivity(), barsList, mTypeface, mTypefaceDesc);
            barListAdapter.notifyDataSetChanged();
            setListAdapter(barListAdapter);

            /*ListAdapter adapter = new SimpleAdapter(
                    getActivity(), barsList, R.layout.bar_list_item,
                    new String[]{TAG_BAR_NAME,  TAG_BAR_ID},
                    new int[]{R.id.bar_name_list,  R.id.bar_list_id});

            setListAdapter(adapter);*/
        }
    }



}
