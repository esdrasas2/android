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
import com.crisnello.notereader.util.Internet;
import com.crisnello.notereader.util.PreferencesUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    public static final int ACTIVITY_REQUEST_QR_CODE = 0;
    public static final int ACTIVITY_REQUEST_CODE = 1;

    //private List<Nota> notas = new ArrayList<Nota>();

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
                            Toast.makeText(getApplicationContext(), "Não foi possível carregar as notas cadastradas! ", Toast.LENGTH_LONG).show();
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
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, ACTIVITY_REQUEST_QR_CODE);
        } catch (ActivityNotFoundException anfe) {
            showDialog(MainActivity.this, "Scanner não Encontrado", "Baixar o scanner de QRCODE do google?", "Sim", "Não").show();
        }
    }

    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ACTIVITY_REQUEST_QR_CODE) {
            if (resultCode == RESULT_OK) {
                contents = intent.getStringExtra("SCAN_RESULT");
                format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
                toast.show();

                //VERIFICAR MELHOR LUGAR, TEMPORARIAMENTE
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String, String> hash = new HashMap<String, String>();
                        hash.put("id_usuario",String.valueOf(user.getId()));
                        hash.put("str_qr_code",contents);
                        String respJson = Internet.postHttp(Config.WS_URL_NOTA,hash);
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
                        }catch(Exception e){
                            updateNotas();
                        }
                    }
                }).start();

            }
        }else if(requestCode == ACTIVITY_REQUEST_CODE){
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

            Intent compartilha = new Intent(Intent.ACTION_SEND);
            compartilha.setType("text/plain");
            compartilha.putExtra(Intent.EXTRA_SUBJECT, "Utilize o Leitor de Notas");
            compartilha.putExtra(Intent.EXTRA_TEXT, "bancodenotas.sytes.net");
            startActivity(Intent.createChooser(compartilha, "Compartilhando"));

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
