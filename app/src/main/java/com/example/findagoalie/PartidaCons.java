package com.example.findagoalie;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PartidaCons extends AppCompatActivity implements View.OnClickListener {
    private int partida_id = 0;
    private int idGoleiro = 0;
    private String posicao = null;
    private String status = null;
    private EditText etDescricao = null;
    private EditText etEndereco = null;
    private EditText etLatitude = null;
    private EditText etLongitude = null;
    private EditText etData = null;
    private EditText etHora = null;
    private EditText etStatus = null;
    private Button btnCoordenadas = null;
    private Button btnVoltar = null;
    private Button btnAtender = null;
    private SQLiteDatabase db = null;
    private Cursor cur = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partida_cons);

        db = openOrCreateDatabase("BancoDados", Context.MODE_PRIVATE, null);

        inicializarComponentes();
        preencherCampos();

        if ((Double.parseDouble(etLatitude.getText().toString()) != 0.0 && Double.parseDouble(etLongitude.getText().toString()) != 0.0)) {
            btnCoordenadas.setVisibility(View.VISIBLE);
        }

        if (this.posicao.equals("Goleiro") && this.status.equals("Pendente")) {
            btnAtender.setVisibility(View.VISIBLE);
        }
    }

    private void inicializarComponentes() {
        Bundle bundle = getIntent().getExtras();
        this.partida_id = bundle.getInt("partida_id");
        this.posicao = bundle.getString("posicao");
        this.idGoleiro = bundle.getInt("id_goleiro");
        this.status = atualizarStatus();

        etDescricao = (EditText) findViewById(R.id.et_desc_consulta);
        etEndereco = (EditText) findViewById(R.id.et_end_consulta);
        etLatitude = (EditText) findViewById(R.id.et_lat_consulta);
        etLongitude = (EditText) findViewById(R.id.et_long_consulta);
        etData = (EditText) findViewById(R.id.et_data_consulta);
        etHora = (EditText) findViewById(R.id.et_hora_consulta);
        etStatus = (EditText) findViewById(R.id.et_status_consulta);
        btnCoordenadas = (Button) findViewById(R.id.btn_mapa_partida);
        btnCoordenadas.setOnClickListener(this);
        btnAtender = (Button) findViewById(R.id.btn_atender_partidacons);
        btnAtender.setOnClickListener(this);
        btnVoltar = (Button) findViewById(R.id.btn_voltar_partidacons);
        btnVoltar.setOnClickListener(this);
    }

    private void preencherCampos() {
        cur = db.rawQuery("SELECT * FROM partida WHERE " +
                "id = "+this.partida_id, null);

        if (cur != null && cur.moveToFirst()) {
            etDescricao.setText(cur.getString(1));
            etEndereco.setText(cur.getString(2));
            etLatitude.setText(cur.getDouble(3)+"");
            etLongitude.setText(cur.getDouble(4)+"");
            etData.setText(cur.getString(5));
            etHora.setText(cur.getString(6));
            etStatus.setText(cur.getString(7));
        } else {
            Toast.makeText(PartidaCons.this,"Não foi possível encontrar informações da partida!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_mapa_partida:
                exibirLocalizacao();
                break;
            case R.id.btn_atender_partidacons:
                atenderPartida();
                break;
            case R.id.btn_voltar_partidacons:
                finish();
                break;
        }
    }

    private void exibirLocalizacao() {
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", Double.parseDouble(etLatitude.getText().toString()));
        bundle.putDouble("long", Double.parseDouble(etLongitude.getText().toString()));

        Intent intent = new Intent(PartidaCons.this, MapsActivity.class);
        intent.putExtras(bundle);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
    }

    private void atenderPartida() {
        cur = db.rawQuery("SELECT id_goleiro FROM partida WHERE " +
                "id = "+this.partida_id, null);

        cur.moveToFirst();
        if (cur.isNull(0) || cur.getInt(0) == 0) {
            ContentValues cv = new ContentValues();
            cv.put("id_goleiro", this.idGoleiro);
            cv.put("status", "Aguardando");

            db.update("partida", cv, "id = "+this.partida_id, null);

            Toast.makeText(PartidaCons.this,"Sua solicitação será verificada pelo responsável da partida!",
                    Toast.LENGTH_SHORT).show();

            etStatus.setText("Aguardando");
            btnAtender.setVisibility(View.INVISIBLE);
        } else {
            Toast.makeText(PartidaCons.this,"Já existe outra pessoa aguardando aprovação para atender esta partida!",
                    Toast.LENGTH_SHORT).show();

            finish();
        }
    }

    private String atualizarStatus() {
        cur = db.rawQuery("SELECT status FROM partida WHERE " +
                "id = "+this.partida_id, null);

        cur.moveToFirst();
        return cur.getString(0);
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }
}
