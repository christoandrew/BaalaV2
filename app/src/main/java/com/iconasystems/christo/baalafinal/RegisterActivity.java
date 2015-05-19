package com.iconasystems.christo.baalafinal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.iconasystems.christo.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class RegisterActivity extends Activity {
    private static final String TAG_SUCCESS = "success";
    /*private static final String url_register = "http://api.baala-online.netii.net/register.php";*/
    private static final String url_register = "http://10.0.3.2/baala/register.php";
    private static final String TAG_USERNAME = "username";
    private static final String TAG_PASSWORD = "password";
    private static final String TAG_EMAIL = "email";

    private Button mCreateAccountButton;
    private EditText mUsername;
    private EditText mPassword;
    private EditText mEmail;
    private ProgressDialog progressDialog;
    private JSONParser jsonParser;

    private String username;
    private String password;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        jsonParser = new JSONParser();

        mUsername = (EditText) findViewById(R.id.register_username);
        mPassword = (EditText) findViewById(R.id.register_password);
        mEmail = (EditText) findViewById(R.id.register_email);

        mCreateAccountButton = (Button) findViewById(R.id.create_account_button);
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CreateAccount().execute();
            }
        });
    }

    class CreateAccount extends AsyncTask<String, String, String> {


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
        public void onPreExecute(){
            super.onPreExecute();
            progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setMessage("Creating Account ...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> data = new ArrayList<NameValuePair>();
            username = mUsername.getText().toString();
            password = mPassword.getText().toString();
            email = mEmail.getText().toString();

            data.add(new BasicNameValuePair(TAG_USERNAME, username));
            data.add(new BasicNameValuePair(TAG_PASSWORD, password));
            data.add(new BasicNameValuePair(TAG_EMAIL, email));

            try{
                JSONObject json = jsonParser.makeHttpRequest(url_register, "POST", data);
                Log.d("Baala Register", json.toString());

                try {
                    int success  = json.getInt(TAG_SUCCESS);

                    if (success == 1){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, "Successfully Registered",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        Intent home = new Intent(RegisterActivity.this, HomeActivity.class);
                        startActivity(home);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, "Error Registering Try Again Later",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }

            } catch (Exception e){
                e.printStackTrace();
            }


            return null;
        }

        @Override
        public void onPostExecute(String result){
            super.onPostExecute(result);
            progressDialog.dismiss();

        }
    }
}
