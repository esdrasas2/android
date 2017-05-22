package com.crisnello.notereader;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crisnello.notereader.config.Config;
import com.crisnello.notereader.entitie.Nota;
import com.crisnello.notereader.entitie.Usuario;
import com.crisnello.notereader.util.AdapterListView;
import com.crisnello.notereader.util.ConexaoInternet;
import com.crisnello.notereader.util.Internet;
import com.crisnello.notereader.util.PreferencesUtil;
import com.crisnello.notereader.util.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.image.SmartImageView;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {

    private Bitmap mIcon1;
    private URL img_value ;
    private ImageView userpicture;
    //---------
    private SmartImageView smartImage;
    private String faceId;

    private TextView tv_login;
    private FloatingActionButton fab;

    private ListView listaDeNotas;
    private AdapterListView adapterListView;
    private ArrayList<Nota> itens;
    //----------------Filter
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private double tValor;
    private String tData;
    private boolean doisFiltros;
    //-----------------
    private String format;
    private String contents;

    private Usuario user;
    public static final int ACTIVITY_REQUEST_CODE = 1;
    public static final int ACTIVITY_FILTRO_CODE = 2;
    public static final int ACTIVITY_MENU_CODE = 3;

    public static final int CONTEXT_MENU_VER = 1;
    public static final int CONTEXT_MENU_ENVIAR = 2;

    private View mainView;
    private boolean startScan;

    private View mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgress = findViewById(R.id.progress);

        doisFiltros = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQR(view);
                mainView = view;
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listaDeNotas = (ListView) findViewById(R.id.lista);
        listaDeNotas.setOnItemClickListener(this);
        listaDeNotas.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                            ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.add(Menu.NONE, CONTEXT_MENU_VER, Menu.NONE, "Ver");
                contextMenu.add(Menu.NONE, CONTEXT_MENU_ENVIAR, Menu.NONE, "Enviar");
            }
        });



        user = (Usuario) getIntent().getSerializableExtra("USER");
        View navigationViewHeader = navigationView.getHeaderView(0);
        tv_login = (TextView) navigationViewHeader.findViewById(R.id.tv_login);
        tv_login.setText(user.getEmail());


        smartImage = (SmartImageView) navigationViewHeader.findViewById(R.id.meuSmartImage);

        faceId = getIntent().getStringExtra("FACEID");
        try {
            //Log.e("MinActivity","onCreate USER_ID FACEBOOK "+faceId);
            if(faceId != null && !faceId.isEmpty()) {
                String fotoFaceURL = recuperaFotoPerfilFacebook(faceId);
                smartImage.setImageUrl(fotoFaceURL);
            }

        } catch (MalformedURLException e) {

            e.printStackTrace();
        }


        tValor = getIntent().getDoubleExtra("VALOR",0.0);
        tData  = getIntent().getStringExtra("DATA");

        if(tValor > 0.0 && tData != null && !tData.isEmpty())
            doisFiltros = true;

        //Log.e("MainActivity","onCreate - tValor "+tValor+" tData "+tData);

        updateNotas();


    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                listaDeNotas.setVisibility(show ? View.GONE : View.VISIBLE);
                listaDeNotas.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        listaDeNotas.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

                mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                mProgress.animate().setDuration(shortAnimTime).alpha(
                        show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
            } else {
                // The ViewPropertyAnimator APIs are not available, so simply show
                // and hide the relevant UI components.
                mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                listaDeNotas.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        }catch(Exception e){
            //Caso de qualquer erro, oculta progresso e mostra lista
            mProgress.setVisibility(View.GONE);
            listaDeNotas.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = menuInfo.position;
        Nota itemNota = adapterListView.getItem(position);

        //Log.e("MainActivity","onContextItemSelected MenuItem position "+position+" Nota "+itemNota.getId()+" CNPJ "+itemNota.getCnpj()+" Valor "+itemNota.getValor());

        switch (item.getItemId()) {
            case CONTEXT_MENU_ENVIAR:
                compartilhar("Enviar Nota ",itemNota.getNumeroFiscalCoo());
                break;
            case CONTEXT_MENU_VER:
                verNota(itemNota.getNumeroFiscalCoo());
                break;
        }
        return super.onContextItemSelected(item);
    }

    private String recuperaFotoPerfilFacebook(String userID) throws MalformedURLException {
        Uri.Builder builder = Uri.parse("https://graph.facebook.com").buildUpon();
        builder.appendPath(userID).appendPath("picture").appendQueryParameter("type", "large");
        return builder.toString();
    }

    @Override
    protected void onStart() {
        super.onStart();
        String strStartScan;
        if(!startScan) {
            startScan = false;
            strStartScan = "false";
        }else{
            strStartScan = "true";
        }

        // Log.e("MainActivity","onStart - SCAN is "+strStartScan);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String strScan = "false";

        if(!startScan) {
            startScan = getIntent().getBooleanExtra("SCAN", false);
            getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            getIntent().putExtra("SCAN",false);
            if (startScan) {
                scanQR(mainView);
                strScan = "true";
            }
        }else{
            startScan = false;
        }

//        //Log.e("MainActivity","onResume - SCAN "+strScan);
//        if(mIcon1 != null)
//            userpicture.setImageBitmap(mIcon1);
    }


    public void updateNotas(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(true);
                    }
                });

                try {
                    HashMap<String, String> hash = new HashMap<String, String>();
                    hash.put("id_usuario", String.valueOf(user.getId()));
                    String respJson = Internet.postHttp(Config.WS_URL_NOTAS, hash);
                    //Log.i("Result .postHttp",respJson);
                    Gson gson = new GsonBuilder().setDateFormat("MMM dd, yyyy").create();
                    Nota[] notaArray = gson.fromJson(respJson, Nota[].class);
                    itens = new ArrayList<Nota>(Arrays.asList(notaArray));
                    if(tValor > 0.0 || (tData != null && !tData.isEmpty())){
                        ArrayList<Nota> tNotas = new ArrayList<Nota>();
                        for (Nota n : itens) {
                            String strDataEmissao = sdf.format(n.getDataEmissao());
                            if(doisFiltros){
                                if (n.getValor() == tValor && strDataEmissao.equals(tData)) {
                                    tNotas.add(n);
                                }
                            }else {
                                if (n.getValor() == tValor || strDataEmissao.equals(tData)) {
                                    tNotas.add(n);
                                }
                            }
                        }
                        itens = tNotas;
                    }
                    if(itens.size() <= 0){
                        Nota pNota = new Nota();
                        pNota.setCnpj("");
                        pNota.setNumeroFiscalCoo(" Adicionar uma nota !");
                        itens.add(pNota);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            createListView();
                            showProgress(false);
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!ConexaoInternet.verificaConexao(getApplicationContext())){
                                (new Util(MainActivity.this)).showAlert("Você não está conectado na internet, efetue a conexão e tente novamente!");
                            }else {
                                Toast.makeText(getApplicationContext(), "Não foi possível carregar as notas cadastradas! ", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }finally {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                        }
                    });
                }
            }
        }).start();
    }

    private void createListView()
    {
        adapterListView = new AdapterListView(this, itens);
        listaDeNotas.setAdapter(adapterListView);
        listaDeNotas.setCacheColorHint(Color.TRANSPARENT);
    }

    public void verNota(String pUrlNota){
        Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
        myWebLink.setData(Uri.parse(pUrlNota));
        startActivityForResult(myWebLink,ACTIVITY_REQUEST_CODE);
    }


    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        Nota item = adapterListView.getItem(arg2);

        if(item.getCnpj().equals("")){
            scanQR(arg1);
        }else if(item.getCnpj().trim().equals("relatorio")){
            //tira o click quando setar string "relatorio" no CNPJ
        } else{
            Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
            myWebLink.setData(Uri.parse(item.getNumeroFiscalCoo()));
            startActivityForResult(myWebLink,ACTIVITY_REQUEST_CODE);
        }
    }


    public void scanQR(View v) {
//        new IntentIntegrator(this).initiateScan();
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("QR_CODE de uma NF-e");
//        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //Log.e("onActivityResult","requestCode :"+requestCode);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result != null) {

            if(result.getContents() == null) {
                Toast.makeText(this, "Clique novamente para menu", Toast.LENGTH_LONG).show();
            } else {
                String strConteudo = result.getContents();
                //CAMPOS OBRIGATORIOS
                // chNFe=41170575121210000137650010002265611890643922
                // tpAmb=1
                // cIdToken=000001

                if(!strConteudo.contains("chNFe") || !strConteudo.contains("tpAmb") || !strConteudo.contains("cIdToken")){
                    (new Util(MainActivity.this)).showAlert("O conteúdo deste QR_CODE não é de uma NFC-e do Paraná válida");
                }else {

                    (new Util(MainActivity.this)).showToast( "Conteúdo: " + strConteudo);

                    if (!ConexaoInternet.verificaConexao(getApplicationContext())) {
                        (new Util(MainActivity.this)).showAlert("Você não está conectado na internet, efetue a conexão e leia novamente essa nota!");
                    } else {
                        contents = result.getContents();

                        //VERIFICAR MELHOR LUGAR, TEMPORARIAMENTE
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    HashMap<String, String> hash = new HashMap<String, String>();
                                    hash.put("id_usuario", String.valueOf(user.getId()));
                                    hash.put("str_qr_code", contents);
                                    String respJson = Internet.postHttp(Config.WS_URL_NOTA, hash);
                                    //Log.i("Result .postHttp",respJson);

//                                    Nota notaInserida = new Gson().fromJson(respJson, Nota.class);
//                                    itens.add(notaInserida);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateNotas();

                                        }
                                    });
                                } catch (Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            (new Util(MainActivity.this)).showToast("Clique em Todas as Notas para Atualizar a lista");
                                        }
                                    });

                                }
                            }
                        }).start();
                    }
                }

            }
        } else if (requestCode == ACTIVITY_REQUEST_CODE){
            updateNotas();
        } else if (requestCode == ACTIVITY_FILTRO_CODE){

            Log.e("onActivityResult","resultCode "+resultCode);

        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            //sair();
            chamaMenuActivity();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_filtrar){

            chamaFiltroActivity();

            return true;
        }else if(id == R.id.action_todas){
            tValor = 0.0;
            tData = "";
            updateNotas();
        }
        return super.onOptionsItemSelected(item);
    }

    public void chamaFiltroActivity(){

        Intent intent = new Intent(MainActivity.this, FiltroActivity.class);
        intent.putExtra("USER", user);
        intent.putExtra("FACEID", faceId);
        startActivityForResult(intent,ACTIVITY_FILTRO_CODE);
    }

    public void chamaMenuActivity(){

        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
        intent.putExtra("USER", user);
        intent.putExtra("FACEID", faceId);
        startActivityForResult(intent,ACTIVITY_MENU_CODE);
    }

    public void sair(){
        Intent intent = new Intent(getApplicationContext(), AutoLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            //finish();
            PreferencesUtil.removePref(PreferencesUtil.NOME, getApplicationContext());
            PreferencesUtil.removePref(PreferencesUtil.EMAIL, getApplicationContext());
            PreferencesUtil.removePref(PreferencesUtil.ID, getApplicationContext());
            PreferencesUtil.removePref(PreferencesUtil.FACEBOOKID, getApplicationContext());
            sair();
        }else if(id == R.id.nav_filter){
            chamaFiltroActivity();
        }else if(id == R.id.nav_home){
            chamaMenuActivity();
        }
        else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

            if(!ConexaoInternet.verificaConexao(getApplicationContext())){
                (new Util(MainActivity.this)).showAlert("Você não está conectado na internet, efetue a conexão e tente compartilhar novamente!");
            }else {
               compartilhar("Install NF-e Reader","http://nfereader.crisnello.com");
            }
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void compartilhar(String pAssunto, String pConteudo){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(true);
            }
        });

        Intent compartilha = new Intent(Intent.ACTION_SEND);
        compartilha.setType("text/plain");
        compartilha.putExtra(Intent.EXTRA_SUBJECT, pAssunto);
        compartilha.putExtra(Intent.EXTRA_TEXT, pConteudo);

        startActivity(Intent.createChooser(compartilha, pAssunto));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false);
            }
        });
    }

}
