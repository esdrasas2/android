package com.crisnello.notereader;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.crisnello.notereader.entitie.Usuario;

import java.util.Calendar;


public class FiltroActivity extends AppCompatActivity {

    private TextView txt_msg_data;
    private EditText edt_valor, edt_data;
    private int year, month, day;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro);

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
                intent.putExtra("USER", (Usuario) getIntent().getSerializableExtra("USER"));
                intent.putExtra("FACEID", getIntent().getStringExtra("FACEID"));

                startActivity(intent);

                //finish();

            }
        });

    }
}
