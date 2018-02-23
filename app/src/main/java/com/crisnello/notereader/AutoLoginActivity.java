package com.crisnello.notereader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.crisnello.notereader.config.Config;
import com.crisnello.notereader.entitie.Usuario;
import com.crisnello.notereader.entitie.UsuarioFacebook;
import com.crisnello.notereader.util.ConexaoInternet;
import com.crisnello.notereader.util.CustomAlert;
import com.crisnello.notereader.util.Internet;
import com.crisnello.notereader.util.PreferencesUtil;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import com.crisnello.notereader.util.Util;
public class AutoLoginActivity extends AppCompatActivity{ // implements LoaderCallbacks<Cursor> {


    private String facebookUserId;

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private Usuario user;
    private UsuarioFacebook userFacebook;
    public static final int ACTIVITY_REQUEST_CODE = 1;
    private UserLoginTask mAuthTask = null;

    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private boolean Connected = true;

    private Button btnHome, mSignInButton, mFacebookButton;

    public boolean isConnected() {
        return Connected;
    }

    public void setConnected(boolean connected) {
        Connected = connected;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_login);

        //Log.e("AutoLoginActivity","onCreate");

//        try {
//            PackageInfo info = getPackageManager().getPackageInfo("com.crisnello.notereader", PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("public_profile", "email");
        // If using in a fragment
        //loginButton.setFragment(this);
        // Other app specific specialization
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                executeGraphRequest(loginResult.getAccessToken().getUserId());
            }
            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }

        });
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                executeGraphRequest(loginResult.getAccessToken().getUserId());

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        btnHome = (Button) findViewById(R.id.btn_home);
        btnHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMenu();
            }
        });


        mFacebookButton = (Button) findViewById(R.id.facebook_button);
        mFacebookButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(AutoLoginActivity.this, Arrays.asList("public_profile", "email"));
            }
        });

        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.email_login_form);
        mProgressView = findViewById(R.id.login_progress);
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }else {
            long pId = PreferencesUtil.getPrefLong(PreferencesUtil.ID, getApplicationContext());
            String pNome = PreferencesUtil.getPref(PreferencesUtil.NOME, getApplicationContext());
            String pEmail = PreferencesUtil.getPref(PreferencesUtil.EMAIL, getApplicationContext());
            String pFacebookId = PreferencesUtil.getPref(PreferencesUtil.FACEBOOKID, getApplicationContext());

           // Log.e("PreferencesUtil", "id :" + pId + " nome :" + pNome + " email :" + pEmail);

            if (pId > 0) {
                Usuario user = new Usuario();
                user.setId(pId);
                user.setNome(pNome);
                user.setEmail(pEmail);
                Intent intent = new Intent(AutoLoginActivity.this, MenuActivity.class);
                intent.putExtra("USER", user);
                intent.putExtra("FACEID", pFacebookId);
                startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
            }
        }
    }

    public void disconnectFromFacebook() {

        LoginManager.getInstance().logOut();


    }

    private void executeGraphRequest(final String userId){
        GraphRequest request =new GraphRequest(AccessToken.getCurrentAccessToken(), userId, null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                //Log.i("FACEBOOK", Profile.getCurrentProfile().toString());

                String respFacebookJson = response.getJSONObject().toString();
                Log.i("Login FaceBook Usuario", respFacebookJson);

                userFacebook = new Gson().fromJson(respFacebookJson, UsuarioFacebook.class);
                Log.i("UsuarioFacebook",userFacebook.toString());

                if(!ConexaoInternet.verificaConexao(getApplicationContext())){
                    (new Util(AutoLoginActivity.this)).showAlert("Você não está conectado na internet, efetue a conexão e tente novamente!");
                }else {

                    facebookUserId = userId;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                HashMap<String, String> hash = new HashMap<String, String>();
                                hash.put("user_nome", userFacebook.getName());
                                hash.put("user_email", userFacebook.getEmail());
                                String respJson = Internet.postHttp(Config.WS_URL_ADD_USER, hash);
                                Log.i("Login ADD Usuario ", respJson);
                                user = new Gson().fromJson(respJson, Usuario.class);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (user.getId() == -1 || user.getIdCliente() == -1) {
                                                (new Util(AutoLoginActivity.this)).showAlert("Não foi possível logar/adicionar usuário no servidor do bancodenotas. \n contato: crisnello@crisnello.com");
                                            } else {
                                                mPasswordView.setVisibility(View.GONE);
                                                mSignInButton.setVisibility(View.GONE);
                                                mFacebookButton.setVisibility(View.GONE);

                                                mEmailView.setText(user.getEmail());
                                                btnHome.setVisibility(View.VISIBLE);
                                                goToMenu();
                                            }
                                        }
                                    });

                            }catch(Exception e){
                                Log.e("executeGraphRequest",e.getMessage());
                            }
                        }
                    }).start();


                }

            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, name, email");
        request.setParameters(parameters);
        request.executeAsync();

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


    public void goToMenu(){

        if(user == null || user.getId() == -1){
            (new Util(AutoLoginActivity.this)).showAlert("Usuario nao encontrado. Obs: AutoLogin - goToMenu");
        }else {
            try {
                (new Util(getApplicationContext())).showToast("Seja bem vindo " + user.getNome());
                PreferencesUtil.putPrefLong(PreferencesUtil.ID, user.getId(), getApplicationContext());
                PreferencesUtil.putPref(PreferencesUtil.NOME, user.getNome(), getApplicationContext());
                PreferencesUtil.putPref(PreferencesUtil.EMAIL, user.getEmail(), getApplicationContext());
                PreferencesUtil.putPref(PreferencesUtil.FACEBOOKID, facebookUserId, getApplicationContext());
                Intent intent = new Intent(AutoLoginActivity.this, MenuActivity.class);
                intent.putExtra("USER", user);
                intent.putExtra("FACEID", facebookUserId);
                startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.e("Login.onActivityResult","requestCode :"+requestCode+" resultCode : "+resultCode);
        if(requestCode == ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                disconnectFromFacebook();
                finish();
            }
        }else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getBooleanExtra("EXIT", false)) {
            disconnectFromFacebook();
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
            if(!ConexaoInternet.verificaConexao(getApplicationContext())){
                setConnected(false);
                return false;
            }else {
                setConnected(true);
                HashMap<String, String> hash = new HashMap<String, String>();
                hash.put("login_username", mEmail);
                hash.put("login_password", mPassword);
                String respJson = Internet.postHttp(Config.WS_URL_LOGIN, hash);
                //Log.i("Login Usuario ",respJson);
                user = new Gson().fromJson(respJson, Usuario.class);
                if (user.getId() == -1 || user.getIdCliente() == -1) {
                    return false;
                }
                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                try {
                    mPasswordView.setVisibility(View.GONE);
                    mSignInButton.setVisibility(View.GONE);
                    mFacebookButton.setVisibility(View.GONE);

                    btnHome.setVisibility(View.VISIBLE);
                }catch(Exception e){
                    e.printStackTrace();
                }
                goToMenu();


            } else {
                if(!isConnected()){
                    (new Util(AutoLoginActivity.this)).showAlert("Você não está conectado na internet, efetue a conexão e tente novamente!");
                }else {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

