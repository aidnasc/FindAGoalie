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

public class PartidaAprov extends AppCompatActivity implements View.OnClickListener {
    private int idGoleiro = 0;
    private int partida_id = 0;
    private EditText etDescricao = null;
    private EditText etGoleiro = null;
    private Button btnAprovar = null;
    private Button btnRejeitar = null;
    private Button btnInformacoes = null;
    private SQLiteDatabase db = null;
    private Cursor cur = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partida_aprov);

        db = openOrCreateDatabase("BancoDados", Context.MODE_PRIVATE, null);

        inicializarComponentes();
        preencherCampos();
    }

    private void inicializarComponentes() {
        Bundle bundle = getIntent().getExtras();
        this.idGoleiro = bundle.getInt("id_goleiro");
        this.partida_id = bundle.getInt("partida_id");

        etDescricao = (EditText) findViewById(R.id.et_desc_partidaaprov);
        etGoleiro = (EditText) findViewById(R.id.et_goleiro_partidaaprov);
        btnAprovar = (Button) findViewById(R.id.btn_aprovar);
        btnAprovar.setOnClickListener(this);
        btnRejeitar = (Button) findViewById(R.id.btn_rejeitar);
        btnRejeitar.setOnClickListener(this);
        btnInformacoes = (Button) findViewById(R.id.btn_informacoes);
        btnInformacoes.setOnClickListener(this);
    }

    private void preencherCampos() {
        cur = db.rawQuery("SELECT nome FROM usuario WHERE id = "+this.idGoleiro, null);
        cur.moveToFirst();
        etGoleiro.setText(cur.getString(0));

        cur = db.rawQuery("SELECT descricao FROM partida WHERE id = "+this.partida_id, null);
        cur.moveToFirst();
        etDescricao.setText(cur.getString(0));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_aprovar:
                aprovarGoleiro();
                break;
            case R.id.btn_rejeitar:
                rejeitarGoleiro();
                break;
            case R.id.btn_informacoes:
                irParaPerfil();
                break;
        }
    }

    private void aprovarGoleiro() {
        ContentValues cv = new ContentValues();
        cv.put("status", "Atendida");

        db.update("partida", cv, "id = "+this.partida_id, null);

        Toast.makeText(PartidaAprov.this,"Você aceitou o atendimento do goleiro "+etGoleiro.getText()+"!",
                Toast.LENGTH_SHORT).show();

        btnAprovar.setVisibility(View.INVISIBLE);
        btnRejeitar.setVisibility(View.INVISIBLE);
    }

    private void rejeitarGoleiro() {
        ContentValues cv = new ContentValues();
        cv.put("status", "Pendente");
        cv.put("id_goleiro", 0);

        db.update("partida", cv, "id = "+this.partida_id, null);

        Toast.makeText(PartidaAprov.this,"Você rejeitou o atendimento do goleiro "+etGoleiro.getText()+"!",
                Toast.LENGTH_SHORT).show();

        btnAprovar.setVisibility(View.INVISIBLE);
        btnRejeitar.setVisibility(View.INVISIBLE);
    }

    private void irParaPerfil() {
        Bundle bundle = new Bundle();
        bundle.putInt("id_goleiro", this.idGoleiro);
        bundle.putString("intent", "PartidaAprov");

        Intent intent = new Intent(PartidaAprov.this, Perfil.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
