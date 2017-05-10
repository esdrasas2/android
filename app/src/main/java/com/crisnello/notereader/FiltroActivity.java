package com.crisnello.notereader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.crisnello.notereader.entitie.Usuario;


public class FiltroActivity extends AppCompatActivity {

    private EditText edt_valor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro);

        edt_valor = (EditText) findViewById(R.id.edt_valor);
        Button buscar = (Button) findViewById(R.id.btn_buscar);
        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double pValor;
                try{
                     pValor = Double.parseDouble(edt_valor.getText().toString());
                }catch(Exception e){
                    //(new Util(FiltroActivity.this)).showAlert("Favor entrar com um valor válido");
                    pValor = 0.0;
                }

                Intent intent = new Intent(FiltroActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("VALOR",pValor);
                intent.putExtra("USER", (Usuario) getIntent().getSerializableExtra("USER"));

                startActivity(intent);
                finish();


            }
        });

    }
}
