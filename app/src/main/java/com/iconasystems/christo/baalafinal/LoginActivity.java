package com.iconasystems.christo.baalafinal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.iconasystems.christo.utils.JSONParser;
import com.iconasystems.christo.utils.SessionManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends Activity implements PlusClient.ConnectionCallbacks, PlusClient.OnConnectionFailedListener{
    private Button loginButton;
    private EditText usernameField;
    private EditText passwordField;

    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;
    private SignInButton mSignInButton;
    private TextView mDisplayNameTextView;

    private static final int DIALOG_GET_GOOGLE_PLAY_SERVICES = 1;

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 2;

    private ProgressDialog progressDialog;

    private static String TAG_USERNAME  = "username";
    private static String TAG_PASSWORD = "password";
    private static String TAG_SUCCESS = "success";
    private static String TAG_MESSAGE = "message";
    private static String TAG_USER_ID = "user_id";

    private SessionManager session;

    private JSONParser jsonParser = new JSONParser();

    /*private static final String url_login = "http://api.baala-online.netii.net/login.php";*/
    private static final String url_login = "http://10.0.3.2/baala/login.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(getApplicationContext());

        usernameField = (EditText) findViewById(R.id.username);
        passwordField = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    new Login().execute();
                }else {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class Login extends AsyncTask<String, String, String> {

        @Override
        public void onPreExecute(){
            super.onPreExecute();

            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Signing In Please...Wait");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }
        @Override
        public String doInBackground(String... params) {
            String username = usernameField.getText().toString();
            String password = passwordField.getText().toString();

            List<NameValuePair> credentials = new ArrayList<NameValuePair>();
            credentials.add(new BasicNameValuePair("username", username));
            credentials.add(new BasicNameValuePair("password", password));

            JSONObject jsonObject = jsonParser.makeHttpRequest(url_login,"POST", credentials);

            Log.d("Baala Login", jsonObject.toString());

            try {
                int success = jsonObject.getInt(TAG_SUCCESS);

                if (success == 1) {

                    final String user_id = jsonObject.getString(TAG_USER_ID);

                    session.createLoginSession(username,password,user_id);

                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                    i.putExtra(TAG_USER_ID, user_id);
                    startActivity(i);

                } else if (success == 0) {
                    final String message = jsonObject.getString(TAG_MESSAGE);

                    runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), message , Toast.LENGTH_LONG).show();
                                }
                            }
                    );

                } else if (success == 2) {
                    final String message = jsonObject.getString(TAG_MESSAGE);

                    runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), message , Toast.LENGTH_LONG).show();
                                }
                            }
                    );

                } else if (success == 3) {
                    final String message = jsonObject.getString(TAG_MESSAGE);

                    runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), message , Toast.LENGTH_LONG).show();
                                }
                            }
                    );

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        public void onPostExecute(String result) {
            progressDialog.dismiss();
        }
    }
}
