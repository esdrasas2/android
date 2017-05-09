package com.crisnello.notereader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
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
import com.crisnello.notereader.util.CustomAlert;
import com.crisnello.notereader.util.Internet;
import com.crisnello.notereader.util.PreferencesUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {


    private ListView listaDeNotas;
    private AdapterListView adapterListView;
    private ArrayList<Nota> itens;
    //----------------

    private String format;
    private String contents;

    private Usuario user;
    public static final int ACTIVITY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanQR(view);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        user = (Usuario) getIntent().getSerializableExtra("USER");

        View navigationViewHeader = navigationView.getHeaderView(0);

        TextView tv_login = (TextView) navigationViewHeader.findViewById(R.id.tv_login);
        tv_login.setText(user.getEmail());

        listaDeNotas = (ListView) findViewById(R.id.lista);
        listaDeNotas.setOnItemClickListener(this);

        updateNotas();
    }

    public void showAlert(String pMsg){
        CustomAlert alert = new CustomAlert(MainActivity.this);
        alert.setMessage(pMsg);
        alert.show();
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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            createListView();
                        }
                    });
                }catch(Exception e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!ConexaoInternet.verificaConexao(getApplicationContext())){
                                showAlert("Você não está conectado na internet, efetue a conexão e tente novamente!");
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

        Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
        myWebLink.setData(Uri.parse(item.getNumeroFiscalCoo()));
        startActivityForResult(myWebLink,ACTIVITY_REQUEST_CODE);
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
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String strConteudo = result.getContents();
                //CAMPOS OBRIGATORIOS
                // chNFe=41170575121210000137650010002265611890643922
                // tpAmb=1
                // cIdToken=000001

                if(!strConteudo.contains("chNFe") || !strConteudo.contains("tpAmb") || !strConteudo.contains("cIdToken")){
                    showAlert("O conteúdo deste QR_CODE não é de uma NFC-e válida");
                }else {

                    Toast.makeText(this, "Scanned: " + strConteudo, Toast.LENGTH_LONG).show();
                    if (!ConexaoInternet.verificaConexao(getApplicationContext())) {
                        showAlert("Você não está conectado na internet, efetue a conexão e leia novamente essa nota!");
                    } else {
                        contents = result.getContents();

                        //VERIFICAR MELHOR LUGAR, TEMPORARIAMENTE
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                HashMap<String, String> hash = new HashMap<String, String>();
                                hash.put("id_usuario", String.valueOf(user.getId()));
                                hash.put("str_qr_code", contents);
                                String respJson = Internet.postHttp(Config.WS_URL_NOTA, hash);
                                //Log.i("Result .postHttp",respJson);
                                try {
                                    Nota notaInserida = new Gson().fromJson(respJson, Nota.class);
                                    itens.add(notaInserida);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((BaseAdapter) listaDeNotas.getAdapter()).notifyDataSetChanged();
                                        }
                                    });
                                } catch (Exception e) {
                                    updateNotas();
                                }
                            }
                        }).start();
                    }
                }

            }
        } else if (requestCode == ACTIVITY_REQUEST_CODE){
            updateNotas();
        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
           //super.onBackPressed();
           sair();
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
        }
        return super.onOptionsItemSelected(item);
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

        if (id == R.id.nav_sair) {
            //finish();
            PreferencesUtil.removePref(PreferencesUtil.NOME, getApplicationContext());
            PreferencesUtil.removePref(PreferencesUtil.EMAIL, getApplicationContext());
            PreferencesUtil.removePref(PreferencesUtil.ID, getApplicationContext());
            sair();
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

            if(!ConexaoInternet.verificaConexao(getApplicationContext())){
                showAlert("Você não está conectado na internet, efetue a conexão e tente compartilhar novamente!");
            }else {
                Intent compartilha = new Intent(Intent.ACTION_SEND);
                compartilha.setType("text/plain");
                compartilha.putExtra(Intent.EXTRA_SUBJECT, "Install Note Reader");
                compartilha.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.crisnello.notereader");
                startActivity(Intent.createChooser(compartilha, "install Note Reader"));
            }
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
