package com.iconasystems.christo.baalafinal;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iconasystems.christo.utils.JSONParser;
import com.iconasystems.christo.utils.SessionManager;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.TabPageIndicator;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class RealDetailsActivity extends FragmentActivity {
    private static final String TAG_BAR_IMAGE = "bar_image";
    private static final String TAG_IMAGES = "images";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    private Button mCheckMenuButton;
    private ProgressDialog progressDialog;
    private JSONParser mJsonParser;
    private JSONArray mDetails = null;
    private ArrayList<HashMap<String, String>> mBarDetails;
    private RatingBar mBarRating;
    private TextView mBarName;
    private ViewPager mBarImage;
    private TextView mPhotoPosition;

    private float mRating;
    private String bar_id;
    private String[] imageUrls;

    private static final String TAG_BAR_ID = "bar_id";
    private static final String TAG_BAR_NAME = "bar_name";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BAR_DETAILS = "bar_details";
    private static final String url_get_details = "http://10.0.3.2/baala/get_bar_details.php";
    private static String TAG_USER_ID = "user_id";
    private static String TAG_MESSAGE = "message";
    private SessionManager sessionManager;

    // This the url that contains the script that adds favorites
    private static final String url_add_to_fav = "http://10.0.3.2/baala/add_to_favorites.php";

    String user_id;

    private HashMap userDetails;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private String bar_image;

    private DisplayImageOptions options;
    private CirclePageIndicator mIndicator;
    private ImageView mNextButton;
    private ImageView mPrevButton;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImageLoader(getApplicationContext());
       // getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_real_details);

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        sessionManager = new SessionManager(getApplicationContext());
        userDetails = sessionManager.getUserDetails();

        user_id = getIntent().getStringExtra(TAG_USER_ID);

        mBarDetails = new ArrayList<HashMap<String, String>>();
        mJsonParser = new JSONParser();
        Intent i = getIntent();
        bar_id = i.getStringExtra(TAG_BAR_ID);
        Typeface mTypeface = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Thin.ttf");
        mBarImage = (ViewPager) findViewById(R.id.bar_detail_image);
        // mBarName = (TextView) findViewById(R.id.bar_detail_name);
        // mBarName.setTypeface(mTypeface);


        mBarRating = (RatingBar) findViewById(R.id.ratingBar);
        mBarRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingBar.isIndicator();
                mRating = rating;
            }
        });
        new LoadDetails().execute();

        mPhotoPosition = (TextView) findViewById(R.id.image_position);
        mCheckMenuButton = (Button) findViewById(R.id.check_menu_button);

        mCheckMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RealDetailsActivity.this, DrinkListActivity.class);
                intent.putExtra(TAG_BAR_ID, bar_id);
                startActivity(intent);
            }
        });

        final TabPageIndicator tabPageIndicator = (TabPageIndicator) findViewById(R.id.indicator);


        FragmentManager fragmentManager = getSupportFragmentManager();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(fragmentManager);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager_divided);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mIndicator = (CirclePageIndicator) findViewById(R.id.circle_indicator);


        mIndicator.setViewPager(mViewPager);
        tabPageIndicator.setViewPager(mViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_real_details, menu);
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
        if (id == R.id.action_add_to_fav){
            new AddFavorites().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /*
    *  Async Tasks that adds the current bar to favorites
    * */
    public class AddFavorites extends AsyncTask<String, String, String> {
        @Override
        public void onPreExecute() {
            super.onPreExecute();
           progressDialog = new ProgressDialog(RealDetailsActivity.this);
            progressDialog.setMessage("Adding To Favorites");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            final String bar_id = getIntent().getStringExtra(TAG_BAR_ID);

            List<NameValuePair> data = new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair("user_id", user_id));
            data.add(new BasicNameValuePair("bar_id", bar_id));

            JSONObject jsonObject = mJsonParser.makeHttpRequest(url_add_to_fav, "GET", data);

            Log.d("Baala Add Favorites", jsonObject.toString());

            try {
                int success = jsonObject.getInt(TAG_SUCCESS);

                if (success == 1) {
                    final String message = jsonObject.getString(TAG_MESSAGE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }
                    });

                } else if (success == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error Occurred", Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (success == 3) {
                    final String message = jsonObject.getString(TAG_MESSAGE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (success == 5) {
                    final String message = jsonObject.getString(TAG_MESSAGE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Unknown Error Occurred", Toast.LENGTH_LONG).show();
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
            progressDialog.dismiss();
        }

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            if (position == 0) {
                fragment = new InfoFragment();
            }
            if (position == 1) {
                fragment = new ReviewFragment();
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "Info".toUpperCase(l);
                case 1:
                    return "Reviews".toUpperCase(l);
            }
            return null;
        }
    }

    public class LoadDetails extends AsyncTask<String, String, JSONArray> {

        private JSONArray images;

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
            progressDialog = new ProgressDialog(RealDetailsActivity.this);
            progressDialog.setMessage("Loading Details...Please Wait");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            String mBarId = getIntent().getStringExtra(TAG_BAR_ID);
            List<NameValuePair> data = new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair(TAG_BAR_ID, mBarId));

            JSONObject mJsonObject = mJsonParser.makeHttpRequest(url_get_details, "GET", data);

            try {
               // Log.d("Bar Details Real Details", mJsonObject.toString());
                try {
                    int success = mJsonObject.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        mDetails = mJsonObject.getJSONArray(TAG_BAR_DETAILS);
                        JSONObject details = mDetails.getJSONObject(0);
                        String bar_name = details.getString(TAG_BAR_NAME);
                        getActionBar().setTitle(bar_name);
                        bar_image = details.getString(TAG_BAR_IMAGE);
                       // mBarName.setText(bar_name);

                       images = details.getJSONArray(TAG_IMAGES);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return images;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            Log.d("Array = ", result.toString());
            ImageAdapter imageAdapter = new ImageAdapter(result);

            //Picasso.with(getApplicationContext()).load(bar_image).into(mBarImage);
            mBarImage.setAdapter(imageAdapter);
        }

    }


    private class ImageAdapter extends PagerAdapter {
        private LayoutInflater inflater;
        private JSONArray image_urls = null;
        private String image_url;
        private int image_number;


        ImageAdapter(JSONArray urls) {
            inflater = getLayoutInflater();
            this.image_urls = urls;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return image_urls.length();
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
            assert imageLayout != null;
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
            mPrevButton = (ImageView) imageLayout.findViewById(R.id.prev_button);
            mNextButton = (ImageView) imageLayout.findViewById(R.id.next_button);

            mPrevButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            mPhotoPosition.setText(new StringBuilder().append( position + 1 ).append(" of ").append(image_urls.length()) );


            try {
                JSONObject json = image_urls.getJSONObject(position);
                image_number = position+1;
                image_url = "http://10.0.3.2/baala/assets/images/bars/"+json.getString("image_"+image_number);
                Log.d("Baala Image URL", json.toString());
                //Log.d("Baala Image Extracted URL", image_url);
            } catch (JSONException e) {
                e.printStackTrace();
            }



            ImageLoader.getInstance().displayImage(image_url, imageView, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    spinner.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    String message = null;
                    switch (failReason.getType()) {
                        case IO_ERROR:
                            message = "Input/Output error";
                            break;
                        case DECODING_ERROR:
                            message = "Image can't be decoded";
                            break;
                        case NETWORK_DENIED:
                            message = "Downloads are denied";
                            break;
                        case OUT_OF_MEMORY:
                            message = "Out Of Memory error";
                            break;
                        case UNKNOWN:
                            message = "Unknown error";
                            break;
                    }
                    Toast.makeText(RealDetailsActivity.this, message, Toast.LENGTH_SHORT).show();

                    spinner.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    spinner.setVisibility(View.GONE);
                }
            });


            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }
}
