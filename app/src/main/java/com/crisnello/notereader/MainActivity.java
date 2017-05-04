package com.crisnello.notereader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crisnello.notereader.config.Config;
import com.crisnello.notereader.entitie.Nota;
import com.crisnello.notereader.entitie.Usuario;
import com.crisnello.notereader.util.Internet;
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
    private ArrayList<ItemListView> itens;
    //----------------

    private String format;
    private String contents;

    private Usuario user;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private List<Nota> notas = new ArrayList<Nota>();

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
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
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

        /*TEMP HARD CODE*/
        Nota nota = new Nota();
        nota.setId(0);
        nota.setCnpj("00.000.000/0001-00");

        notas.add(nota);

        listaDeNotas = (ListView) findViewById(R.id.lista);
        listaDeNotas.setOnItemClickListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> hash = new HashMap<String, String>();
                hash.put("id_usuario",String.valueOf(user.getId()));
                String respJson = Internet.postHttp(Config.WS_URL_NOTAS,hash);
                //Log.i("Result .postHttp",respJson);
                Gson gson = new GsonBuilder().setDateFormat("MMM dd, yyyy").create();
                Nota[] notaArray = gson.fromJson(respJson, Nota[].class);
                notas = new ArrayList<Nota>(Arrays.asList(notaArray));
//                for(int x=0;x<notas.size();x++){
//                    Nota nota = notas.get(x);
//                    Log.e("Notas",nota.toString());
//                }
                createListView();

            }
        }).start();




    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        //Pega o item que foi selecionado.
        ItemListView item = adapterListView.getItem(arg2);
        //Demostração
        Toast.makeText(this, "Você Clicou em: " + item.getTexto(), Toast.LENGTH_LONG).show();
    }

    private void createListView()
    {
        //Criamos nossa lista que preenchera o ListView
        itens = new ArrayList<ItemListView>();
        Log.e("createListView","Vou carregar as notas TAMANHO:"+notas.size());
        for(int i=0;i<notas.size();i++){
            Nota nota = notas.get(i);
            itens.add(new ItemListView(nota.toString()));
        }

        //Cria o adapter
        adapterListView = new AdapterListView(this, itens);

        //Define o Adapter
        listaDeNotas.setAdapter(adapterListView);
        //Cor quando a lista é selecionada para ralagem.
        listaDeNotas.setCacheColorHint(Color.TRANSPARENT);
    }

    public void scanQR(View v) {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            showDialog(MainActivity.this, "Scanner não Encontrado", "Download o scanner de QRCODE do google?", "Sim", "Não").show();
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
        if (requestCode == 0) {
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

                        Nota notaInserida = new Gson().fromJson(respJson,Nota.class);
                        notas.add(notaInserida);
                    }
                }).start();

            }
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            //finish();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
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
