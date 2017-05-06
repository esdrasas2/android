package com.crisnello.notereader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crisnello.notereader.config.Config;
import com.crisnello.notereader.entitie.Usuario;
import com.crisnello.notereader.util.Internet;
import com.crisnello.notereader.util.PreferencesUtil;
import com.google.gson.Gson;

import java.util.HashMap;

public class AutoLoginActivity extends AppCompatActivity{ // implements LoaderCallbacks<Cursor> {

    private Usuario user;
    public static final int ACTIVITY_REQUEST_CODE = 1;
    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    public void showToast(String pMsg){
        Toast.makeText(getApplicationContext(), pMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_login);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }else {
            long pId = PreferencesUtil.getPrefLong(PreferencesUtil.ID, getApplicationContext());
            String pNome = PreferencesUtil.getPref(PreferencesUtil.NOME, getApplicationContext());
            String pEmail = PreferencesUtil.getPref(PreferencesUtil.EMAIL, getApplicationContext());

            Log.e("PreferencesUtil", "id :" + pId + " nome :" + pNome + " email :" + pEmail);

            if (pId > 0) {
                Usuario user = new Usuario();
                user.setId(pId);
                user.setNome(pNome);
                user.setEmail(pEmail);
                Intent intent = new Intent(AutoLoginActivity.this, MainActivity.class);
                intent.putExtra("USER", user);
                startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
            }
        }
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.e("Login.onActivityResult","requestCode :"+requestCode+" resultCode : "+resultCode);
        if(requestCode == ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {

                HashMap<String, String> hash = new HashMap<String, String>();

                hash.put("login_username",mEmail);
                hash.put("login_password",mPassword);

                String respJson = Internet.postHttp(Config.WS_URL_LOGIN,hash);
                user = new Gson().fromJson(respJson, Usuario.class);
               // Log.i("Usuario",user.toString());
                if(user.getId() == -1 || user.getIdCliente() == -1){
                    return false;
                }
                return  true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                showToast("Seja bem vindo "+user.getNome());

                PreferencesUtil.putPrefLong(PreferencesUtil.ID,user.getId(), getApplicationContext());
                PreferencesUtil.putPref(PreferencesUtil.NOME, user.getNome(),getApplicationContext());
                PreferencesUtil.putPref(PreferencesUtil.EMAIL,user.getEmail(),getApplicationContext());

                Intent intent = new Intent(AutoLoginActivity.this, MainActivity.class);
                intent.putExtra("USER",user);
                startActivityForResult(intent,ACTIVITY_REQUEST_CODE);

            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

