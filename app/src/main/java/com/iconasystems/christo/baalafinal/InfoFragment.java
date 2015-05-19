package com.iconasystems.christo.baalafinal;


import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iconasystems.christo.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {
    public ProgressDialog progressDialog;
    public TextView mBarName;
    public TextView mBarWebsite;
    public TextView mBarContact;
    public TextView mBarEmail;
    public ImageView mCallBar;
    public ImageView mSendEmail;
    public ImageView mSearchWeb;
    public ImageView mLocateMaps;


    public static final String TAG_BAR_NAME = "bar_name";
    public static final String TAG_BAR_WEBSITE = "bar_website";
    public static final String TAG_BAR_CONTACT = "bar_contact";
    public static final String TAG_BAR_IMAGE = "bar_image";
    public static final String TAG_DATE_ADDED = "date_added";
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_BAR_ID = "bar_id";
    public static final String TAG_BAR_DETAILS = "bar_details";

    public JSONParser jsonParser;
    public JSONArray mBarDetails = null;

    /*public static final String url_get_info = "http://api.baala-online.netii.net/get_bar_details.php";*/
    public static final String url_get_info = "http://10.0.3.2/baala/get_bar_details.php";

    public InfoFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // mBarName = (TextView) getView().findViewById(R.id.bar_detail_name);
        mBarContact = (TextView) getView().findViewById(R.id.bar_contact_phone);
        mBarWebsite = (TextView) getView().findViewById(R.id.bar_website);
        mBarEmail = (TextView) getView().findViewById(R.id.bar_email);
        mCallBar = (ImageView) getView().findViewById(R.id.call_bar);
        mSendEmail = (ImageView) getView().findViewById(R.id.send_email);
        mSearchWeb = (ImageView) getView().findViewById(R.id.search_web);
        mLocateMaps = (ImageView) getView().findViewById(R.id.search_map);

        mSearchWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String q = mBarWebsite.getText().toString();
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH );
                intent.putExtra(SearchManager.QUERY, q);

                PackageManager packageManager = getActivity().getPackageManager();
                List<ResolveInfo> activities = packageManager
                        .queryIntentActivities(intent, 0);
                boolean isIntentSafe = activities.size() > 0;
                if (isIntentSafe) {
                    startActivity(intent);
                }
            }
        });
        mCallBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
                        + mBarContact.getText().toString()));

                PackageManager packageManager = getActivity().getPackageManager();
                List<ResolveInfo> activities = packageManager
                        .queryIntentActivities(intent, 0);
                boolean isIntentSafe = activities.size() > 0;

                if (isIntentSafe) {
                    startActivity(intent);
                }
            }
        });

        mSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mBarEmail.getText().toString();
                email(getActivity(), email, "Subject", "Sent From Baala");
            }
        });

        jsonParser = new JSONParser();

        new LoadInfo().execute();
    }

    public static void email(Context context, String to, String subject, String body) {
        StringBuilder builder = new StringBuilder("mailto:" + Uri.encode(to));
        if (subject != null) {
            builder.append("?subject=" + Uri.encode(Uri.encode(subject)));
            if (body != null) {
                builder.append("&body=" + Uri.encode(Uri.encode(body)));
            }
        }
        String uri = builder.toString();
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
        context.startActivity(intent);
    }

    class LoadInfo extends AsyncTask<String, String, String> {
        @Override
        public void onPreExecute(){
            super.onPreExecute();
            /*progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading Details...Please Wait");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();*/

        }

        @Override
        protected String doInBackground(String... params) {
            String bar_id = getActivity().getIntent().getStringExtra(TAG_BAR_ID);

            List<NameValuePair> data = new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair(TAG_BAR_ID, bar_id));
            JSONObject jsonObject = jsonParser.makeHttpRequest(url_get_info, "GET", data);

            Log.d("Bar details", jsonObject.toString());

            try{
                int success = jsonObject.getInt(TAG_SUCCESS);
                if (success == 1) {
                    mBarDetails = jsonObject.getJSONArray(TAG_BAR_DETAILS);
                    JSONObject details = mBarDetails.getJSONObject(0);
                    String bar_name = details.getString(TAG_BAR_NAME);
                    String bar_website = details.getString(TAG_BAR_WEBSITE);
                    String bar_contact = details.getString(TAG_BAR_CONTACT);
                    String bar_image = details.getString(TAG_BAR_IMAGE);
                    String date_added = details.getString(TAG_DATE_ADDED);

                    mBarWebsite.setText(bar_website);
                    mBarContact.setText(bar_contact);
                    // mBarName.setText(bar_name);

                } else {
                    // Todo get messages from the json and show in toasts
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

        }
    }

}
