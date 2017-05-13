package com.crisnello.notereader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.crisnello.notereader.entitie.Usuario;
import com.crisnello.notereader.util.ConexaoInternet;
import com.crisnello.notereader.util.PreferencesUtil;
import com.crisnello.notereader.util.Util;
import com.loopj.android.image.SmartImageView;

import java.net.MalformedURLException;

public class MenuActivity extends AppCompatActivity {

    private String faceId;
    private Usuario user;
    private Button iv_notas, iv_filter, iv_share, iv_sair, iv_add_nota;
    public static final int ACTIVITY_REQUEST_CODE = 1;

    private TextView txt_msg;

    private Activity mainActivity;
    private SmartImageView smartImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        user = (Usuario) getIntent().getSerializableExtra("USER");
        String strMsg  = getIntent().getStringExtra("MSG");
        faceId = getIntent().getStringExtra("FACEID");
        smartImage = (SmartImageView) findViewById(R.id.meuSmartImage);

        try {
            if(faceId != null && !faceId.isEmpty()) {
                String fotoFaceURL = recuperaFotoPerfilFacebook(faceId);
                smartImage.setImageUrl(fotoFaceURL);
            }
        } catch (MalformedURLException e) {    e.printStackTrace();      }

        txt_msg = (TextView) findViewById(R.id.txt_msg);
        if(strMsg != null && !strMsg.isEmpty()){
            txt_msg.setText(strMsg);
        }else{
            txt_msg.setText(user.getEmail());
        }

        iv_add_nota = (Button) findViewById(R.id.btn_add_nota);
        iv_add_nota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                            intent.putExtra("USER", user);
                            intent.putExtra("SCAN", true);
                            intent.putExtra("FACEID", faceId);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                finish();
            }
        });

        iv_share = (Button) findViewById(R.id.btn_iv_share);
        iv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ConexaoInternet.verificaConexao(getApplicationContext())){
                    (new Util(MenuActivity.this)).showAlert("Você não está conectado na internet, efetue a conexão e tente compartilhar novamente!");
                }else {
                    Intent compartilha = new Intent(Intent.ACTION_SEND);
                    compartilha.setType("text/plain");
                    compartilha.putExtra(Intent.EXTRA_SUBJECT, "Install NF-e Reader");
                    compartilha.putExtra(Intent.EXTRA_TEXT, "http://nfereader.crisnello.com");
                    startActivity(Intent.createChooser(compartilha, "Install NF-e Reader"));
                }
            }
        });

        iv_filter = (Button) findViewById(R.id.btn_iv_filter);
        iv_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuActivity.this, FiltroActivity.class);
                intent.putExtra("USER", user);
                intent.putExtra("FACEID", faceId);
                startActivity(intent);
                finish();
            }
        });
//        iv_sair = (ImageView) findViewById(R.id.iv_sair);
//        iv_sair.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(), AutoLoginActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.putExtra("EXIT", true);
//                startActivity(intent);
//                finish();
//            }
//        });



        iv_notas = (Button) findViewById(R.id.btn_iv_notas);
        iv_notas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToApp();
            }
        });
    }


    private String recuperaFotoPerfilFacebook(String userID) throws MalformedURLException {
        Uri.Builder builder = Uri.parse("https://graph.facebook.com").buildUpon();
        builder.appendPath(userID).appendPath("picture").appendQueryParameter("type", "large");
        return builder.toString();
    }

    public void goToApp(){

        if(user == null || user.getId() == -1){
            (new Util(MenuActivity.this)).showAlert("Usuario nao encontrado. Obs: goToApp()");
        }else {
            try {
                PreferencesUtil.putPrefLong(PreferencesUtil.ID, user.getId(), getApplicationContext());
                PreferencesUtil.putPref(PreferencesUtil.NOME, user.getNome(), getApplicationContext());
                PreferencesUtil.putPref(PreferencesUtil.EMAIL, user.getEmail(), getApplicationContext());
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                intent.putExtra("USER", user);
                intent.putExtra("FACEID", faceId);
                startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
