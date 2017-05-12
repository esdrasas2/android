package com.crisnello.notereader;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crisnello.notereader.config.Config;
import com.crisnello.notereader.entitie.Nota;
import com.crisnello.notereader.entitie.Usuario;
import com.crisnello.notereader.util.AdapterListView;
import com.crisnello.notereader.util.ConexaoInternet;
import com.crisnello.notereader.util.Util;
import com.crisnello.notereader.util.Internet;
import com.crisnello.notereader.util.PreferencesUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {


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

    private View mainView;
    private boolean startScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startScan = false;
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

        user = (Usuario) getIntent().getSerializableExtra("USER");
        View navigationViewHeader = navigationView.getHeaderView(0);
        TextView tv_login = (TextView) navigationViewHeader.findViewById(R.id.tv_login);
        tv_login.setText(user.getEmail());


        tValor = getIntent().getDoubleExtra("VALOR",0.0);
        tData  = getIntent().getStringExtra("DATA");

        if(tValor > 0.0 && tData != null && !tData.isEmpty())
            doisFiltros = true;

        //Log.e("MainActivity","onCreate - tValor "+tValor+" tData "+tData);

        updateNotas();


    }

    @Override
    protected void onResume() {
        super.onResume();
        String strScan = "false";

        if(!startScan) {
            startScan = getIntent().getBooleanExtra("SCAN", false);
            if (startScan) {
                scanQR(mainView);
                strScan = "true";
            }
        }else{
            startScan = false;
        }

        Log.e("MainActivity","onResume - SCAN "+strScan);
    }


    public void updateNotas(){
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                        pNota.setNumeroFiscalCoo("");
                        itens.add(pNota);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            createListView();
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


    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        Nota item = adapterListView.getItem(arg2);

        if(item.getCnpj().equals("")){
            scanQR(arg1);
        }else{
            Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
            myWebLink.setData(Uri.parse(item.getNumeroFiscalCoo()));
            startActivityForResult(myWebLink,ACTIVITY_REQUEST_CODE);
        }
    }


    public void scanQR(View v) {
//        new IntentIntegrator(this).initiateScan();
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("QR_CODE de uma NFC-e");
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
                Toast.makeText(this, "Cancelado", Toast.LENGTH_LONG).show();
            } else {
                String strConteudo = result.getContents();
                //CAMPOS OBRIGATORIOS
                // chNFe=41170575121210000137650010002265611890643922
                // tpAmb=1
                // cIdToken=000001

                if(!strConteudo.contains("chNFe") || !strConteudo.contains("tpAmb") || !strConteudo.contains("cIdToken")){
                    (new Util(MainActivity.this)).showAlert("O conteúdo deste QR_CODE não é de uma NFC-e válida");
                }else {

                    Toast.makeText(this, "Conteúdo: " + strConteudo, Toast.LENGTH_LONG).show();
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

                                        Nota notaInserida = new Gson().fromJson(respJson, Nota.class);
                                        itens.add(notaInserida);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateNotas();

                                            }
                                        });
                                } catch (Exception e) {
                                    (new Util(getApplicationContext())).showToast("Problema ao adicionar nota contate o criador crisnello@crisnello.com");
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
        startActivityForResult(intent,ACTIVITY_FILTRO_CODE);
    }

    public void chamaMenuActivity(){

        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
        intent.putExtra("USER", user);
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
                Intent compartilha = new Intent(Intent.ACTION_SEND);
                compartilha.setType("text/plain");
                compartilha.putExtra(Intent.EXTRA_SUBJECT, "Install NF-e Reader");
                compartilha.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.crisnello.notereader");
                startActivity(Intent.createChooser(compartilha, "Install NF-e Reader"));
            }
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
