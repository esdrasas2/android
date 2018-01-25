package com.crisnello.notereader;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.crisnello.notereader.config.Config;
import com.crisnello.notereader.entitie.Usuario;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import com.google.android.gms.ads.AdRequest;

import java.util.Calendar;


public class FiltroActivity extends AppCompatActivity {

    private AdView mAdView;

    private TextView txt_msg_data;
    private EditText edt_valor, edt_data;
    private int year, month, day;

    private Usuario user;
    private String facebookUserId;

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int theYear, int monthOfYear,
                              int dayOfMonth) {

            year = theYear;
            month = monthOfYear;
            day = dayOfMonth;
            edt_data.setText(String.format("%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), theYear));
        }
    };

    private void showDatePicker() {

        DatePickerDialog datepicker = new DatePickerDialog(FiltroActivity.this, ondate, year, month, day);
        datepicker.getDatePicker().setCalendarViewShown(false);

        datepicker.show();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(FiltroActivity.this, MenuActivity.class);
        intent.putExtra("USER", user);
        intent.putExtra("FACEID", facebookUserId);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro);

        MobileAds.initialize(this, Config.ADMOB_APP_ID);

        mAdView = (AdView) findViewById(R.id.adView);

        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."

        System.err.println(new AdRequest.Builder().addTestDevice("ABCDEF012345"));

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);

        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

        txt_msg_data = (TextView) findViewById(R.id.txt_msg_data);

        edt_data = (EditText) findViewById(R.id.edt_data);
        edt_data.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showDatePicker();
            }
        });


        edt_data.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                {
                    txt_msg_data.setText("Clique novamente para abrir o Calendário");
                }else{
                    txt_msg_data.setText("");
                }
            }
        });

        user = (Usuario) getIntent().getSerializableExtra("USER");
        facebookUserId =  getIntent().getStringExtra("FACEID");

        edt_valor = (EditText) findViewById(R.id.edt_valor);
        Button buscar = (Button) findViewById(R.id.btn_buscar);
        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double pValor;
                Intent intent = new Intent(FiltroActivity.this, MainActivity.class);

                try{pValor = Double.parseDouble(edt_valor.getText().toString());
                }catch(Exception e){pValor = 0.0;}

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("VALOR",pValor);
                intent.putExtra("DATA",edt_data.getText().toString());
                intent.putExtra("USER", user);
                intent.putExtra("FACEID",facebookUserId);

                startActivity(intent);

                //finish();

            }
        });

    }
}
