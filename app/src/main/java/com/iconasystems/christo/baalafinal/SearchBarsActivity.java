package com.iconasystems.christo.baalafinal;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.iconasystems.christo.utils.BarListAdapter;
import com.iconasystems.christo.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SearchBarsActivity extends ListActivity implements SearchView.OnQueryTextListener {

    private static final String TAG_SEARCH_CRITERIA = "search_criteria";
    private SearchView mSearchView;
    private String search_query;

    public static final String TAG_BAR_NAME = "bar_name";
    public static final String TAG_BAR_ID = "bar_id";
    public static final String TAG_BAR_IMAGE = "bar_image";
    public static final String TAG_BARS = "bars";
    public static final String TAG_SUCCESS = "success";

    public ImageView mBarImage;
    public TextView mBarId;

    private Typeface mTypeface;

    private JSONParser jsonParser;
    /*private String url_search = "http://api.baala-online.netii.net/find_bar.php";*/
    private String url_search = "http://10.0.3.2/baala/find_bar.php";
    private JSONArray barsArray = null;


    public ProgressDialog progressDialog;
    public ArrayList<HashMap<String, String>> barsList;
    private Typeface mTypefaceDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_search);

        ListView listView = getListView();
        mBarImage = (ImageView) listView.findViewById(R.id.bar_photo);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBarId = (TextView) view.findViewById(R.id.bar_list_id);
                final String bar_id = mBarId.getText().toString();

                Intent i = new Intent(SearchBarsActivity.this, RealDetailsActivity.class);
                i.putExtra(TAG_BAR_ID, bar_id);
                startActivity(i);
            }
        });

        jsonParser = new JSONParser();
        mTypeface = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Thin.ttf");
        mTypefaceDesc = Typeface.createFromAsset(this.getAssets(), "fonts/Belle-West.otf");
        barsList = new ArrayList<HashMap<String, String>>();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        setupSearchView(searchItem);

        return true;
    }

    private void setupSearchView(MenuItem searchItem) {
        if (isAlwaysExpanded()) {
            mSearchView.setIconifiedByDefault(true);
        }/* else {
            searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }*/

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();

            // Try to use the "applications" global search provider
            SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
            for (SearchableInfo inf : searchables) {
                if (inf.getSuggestAuthority() != null
                        && inf.getSuggestAuthority().startsWith("applications")) {
                    info = inf;
                }
            }
            mSearchView.setSearchableInfo(info);
        }
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint("Search Bars");
        mSearchView.setOnQueryTextListener(this);
    }

    private boolean isAlwaysExpanded() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        search_query = query;
        new LoadResults().execute();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {

        return false;
    }

    class LoadResults extends AsyncTask<String, String, String> {

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
            progressDialog = new ProgressDialog(getApplicationContext());
            progressDialog.setMessage("Searching ...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(false);
            // progressDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> data = new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair(TAG_SEARCH_CRITERIA, search_query));

            try {
                JSONObject json = jsonParser.makeHttpRequest(url_search, "GET", data);

                Log.d("Search Results", json.toString());
                try {
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        barsArray = json.getJSONArray(TAG_BARS);

                        for (int i = 0; i < barsArray.length(); i++) {
                            JSONObject jsonObject = barsArray.getJSONObject(i);

                            String bar_name = jsonObject.getString(TAG_BAR_NAME);
                            String bar_id = jsonObject.getString(TAG_BAR_ID);
                            String bar_image = jsonObject.getString(TAG_BAR_IMAGE);

                            // Log.d("Image Urls" , "Total Images = "+imageUrls.length);

                            HashMap<String, String> hashMap = new HashMap<String, String>();

                            hashMap.put(TAG_BAR_NAME, bar_name);
                            hashMap.put(TAG_BAR_IMAGE, bar_image);
                            hashMap.put(TAG_BAR_ID, bar_id);

                            barsList.add(hashMap);
                        }
                    } else {
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               Toast.makeText(getApplicationContext(), "Error Occurred", Toast.LENGTH_LONG).show();
                           }
                       });
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
            progressDialog.dismiss();

            BarListAdapter barListAdapter = new BarListAdapter(SearchBarsActivity.this, barsList, mTypeface, mTypefaceDesc);
            setListAdapter(barListAdapter);
            barListAdapter.notifyDataSetChanged();
        }
    }
}
