package com.iconasystems.christo.baalafinal;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.iconasystems.christo.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewFragment extends ListFragment {
    public static final String TAG_REVIEWER = "reviewer";
    public static final String TAG_REVIEW = "review";
    public static final String TAG_DATE_REVIEWED = "date_reviewed";
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_REVIEWS = "reviews";
    public static final String TAG_REVIEW_ID = "review_id";
    public static final String TAG_BAR_ID = "bar_id";

    public JSONParser jsonParser;
    public JSONArray mReviews = null;

    public ProgressDialog progressDialog;

    public ArrayList<HashMap<String, String>> mReviewsList;

    /*public static final String url_get_reviews = "http://api.baala-online.netii.net/get_reviews.php";*/
    public static final String url_get_reviews = "http://10.0.3.2/baala/get_reviews.php";

    public ReviewFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_review, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        jsonParser = new JSONParser();
        mReviewsList = new ArrayList<HashMap<String, String>>();
        new LoadReviews().execute();
    }

    class LoadReviews extends AsyncTask<String, String, String> {

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
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading Reviews...Please Wait");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String bar_id = getActivity().getIntent().getStringExtra(TAG_BAR_ID);

            List<NameValuePair> data = new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair(TAG_BAR_ID, bar_id));

            try {
                JSONObject jsonObject = jsonParser.makeHttpRequest(url_get_reviews, "GET", data);

                Log.d("Reviews", jsonObject.toString());

                try {
                    int success = jsonObject.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        mReviews = jsonObject.getJSONArray(TAG_REVIEWS);
                        for (int i = 0; i < mReviews.length(); i++) {
                            JSONObject mReviewsJSONObject = mReviews.getJSONObject(i);

                            String review = mReviewsJSONObject.getString(TAG_REVIEW);
                            String reviewer = mReviewsJSONObject.getString(TAG_REVIEWER).trim();
                            String date_reviewed = mReviewsJSONObject.getString(TAG_DATE_REVIEWED);
                            String review_id = mReviewsJSONObject.getString(TAG_REVIEW_ID);

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(TAG_REVIEW, review);
                            map.put(TAG_REVIEWER, reviewer);
                            map.put(TAG_DATE_REVIEWED, date_reviewed);
                            map.put(TAG_REVIEW_ID, review_id);

                            mReviewsList.add(map);
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
        public void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            ListAdapter listAdapter = new SimpleAdapter(getActivity(),
                    mReviewsList, R.layout.review_list_item,
                    new String[]{TAG_REVIEW_ID, TAG_REVIEW, TAG_DATE_REVIEWED, TAG_REVIEWER},
                    new int[]{R.id.review_id, R.id.bar_review, R.id.date_reviewed, R.id.bar_reviewer});

            setListAdapter(listAdapter);
        }
    }


}
