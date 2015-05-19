package com.iconasystems.christo.baalafinal;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.SearchView;

import com.iconasystems.christo.utils.JSONParser;
import com.iconasystems.christo.utils.SearchDrinkAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SearchDrinksActivity extends ListActivity implements SearchView.OnQueryTextListener {
    private static final String TAG_SEARCH_CRITERIA = "search_criteria";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_DRINKS = "drinks";
    private static final String TAG_DRINK_NAME = "drink_name";
    private static final String TAG_PRICE = "price";
    private static final String TAG_BAR_NAME = "bar_name";
    private static final String TAG_DRINK_IMAGE = "drink_image";
    private JSONParser jsonParser;
    private ProgressDialog progressDialog;
    private JSONArray drinks;
    private ArrayList<HashMap<String, String>> drinksList;
    private String search_query;

    /*private static final String url_search_drinks = "http://api.baala-online.netii.net/find_drink.php";*/
    private static final String url_search_drinks = "http://10.0.3.2/baala/find_drink.php";
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_search_drinks);

        drinksList = new ArrayList<HashMap<String, String>>();
        jsonParser = new JSONParser();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_drinks, menu);
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
        mSearchView.setQueryHint("Search Drinks");
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
    public boolean onQueryTextSubmit(String s) {
        new SearchDrinks().execute(s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        search_query = s;

        return false;
    }

    class SearchDrinks extends AsyncTask<String, String, String> {


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
        protected void onPreExecute(){
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

            try{
                JSONObject json = jsonParser.makeHttpRequest(url_search_drinks, "GET", data);

                Log.d("Search Results Drinks", json.toString());
                try{
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1){
                        drinks = json.getJSONArray(TAG_DRINKS);

                        for (int i = 0; i < drinks.length(); i++){
                            JSONObject drinkResult = drinks.getJSONObject(i);

                            String drink_name  = drinkResult.getString(TAG_DRINK_NAME);
                            String bar_name = drinkResult.getString(TAG_BAR_NAME);
                            String price = drinkResult.getString(TAG_PRICE);
                            String drink_image = drinkResult.getString(TAG_DRINK_IMAGE);

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(TAG_BAR_NAME, bar_name);
                            map.put(TAG_DRINK_IMAGE, drink_image);
                            map.put(TAG_DRINK_NAME, drink_name);
                            map.put(TAG_PRICE, price);

                            drinksList.add(map);
                        }
                    }

                }catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            progressDialog.dismiss();

            SearchDrinkAdapter searchDrinkAdapter = new SearchDrinkAdapter(SearchDrinksActivity.this, drinksList);
            setListAdapter(searchDrinkAdapter);
            searchDrinkAdapter.notifyDataSetChanged();
        }
    }
}
