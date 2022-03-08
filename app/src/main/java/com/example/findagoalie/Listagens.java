package com.example.findagoalie;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Listagens extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private int idGoleiro = 0;
    private String profile_id = null;
    private String posicao = null;
    private ListView listaPendentes = null;
    private ListView listaAguardando = null;
    private ListView listaAtendidas = null;
    private TextView tvAtendidas = null;
    private TextView tvAguardando = null;
    private SQLiteDatabase db = null;
    private Cursor cur = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listagens);

        db = openOrCreateDatabase("BancoDados", Context.MODE_PRIVATE, null);

        inicializarComponentes();
        
        if (this.posicao.equals("Goleiro")) {
            tvAtendidas.setVisibility(View.VISIBLE);
            listaAtendidas.setVisibility(View.VISIBLE);
        } else {
            tvAguardando.setVisibility(View.VISIBLE);
            listaAguardando.setVisibility(View.VISIBLE);
        }
    }

    private void inicializarComponentes() {
        Bundle bundle = getIntent().getExtras();
        this.posicao = bundle.getString("posicao");
        this.idGoleiro = bundle.getInt("id_goleiro");
        this.profile_id = bundle.getString("profile_id");

        listaPendentes = (ListView) findViewById(R.id.list_view_pendentes);
        listaPendentes.setOnItemClickListener(this);
        listaAguardando = (ListView) findViewById(R.id.list_view_aguardando);
        listaAguardando.setOnItemClickListener(this);
        listaAtendidas = (ListView) findViewById(R.id.list_view_atendidas);
        listaAtendidas.setOnItemClickListener(this);
        tvAguardando = (TextView) findViewById(R.id.tv_list_aguardando);
        tvAtendidas = (TextView) findViewById(R.id.tv_list_atendidas);

        if (this.posicao.equals("Goleiro")) {
            cur = db.rawQuery("SELECT COUNT(*) FROM partida WHERE status = 'Pendente'", null);
        } else {
            cur = db.rawQuery("SELECT COUNT(*) FROM partida WHERE status = 'Pendente' AND id_usuario = '"+this.profile_id+"'", null);
        }

        cur.moveToFirst();
        int total = cur.getInt(0);

        String pendentes[] = new String[total];

        cur = db.rawQuery("SELECT id, descricao, data, hora FROM partida WHERE status = 'Pendente'", null);        for(int i = 0; i < total; i++) {
            cur.moveToNext();
            pendentes[i] = cur.getInt(0) + " " + cur.getString(1) + " " + cur.getString(2) + " " + cur.getString(3);
        }

        listaPendentes.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pendentes));

        cur = db.rawQuery("SELECT COUNT(*) FROM partida WHERE status = 'Atendida' AND id_goleiro = "+this.idGoleiro, null);
        cur.moveToFirst();
        total = cur.getInt(0);

        String[] atendidas = new String[total];

        cur = db.rawQuery("SELECT id, descricao, data, hora FROM partida WHERE status = 'Atendida' AND id_goleiro ="+this.idGoleiro, null);        for(int i = 0; i < total; i++) {
            cur.moveToNext();
            atendidas[i] = cur.getInt(0) + " " + cur.getString(1) + " " + cur.getString(2) + " " + cur.getString(3);
        }

        listaAtendidas.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, atendidas));

        cur = db.rawQuery("SELECT COUNT(*) FROM partida WHERE status = 'Aguardando' AND id_usuario = '"+this.profile_id+"'", null);
        cur.moveToFirst();
        total = cur.getInt(0);

        String[] aguardando = new String[total];

        cur = db.rawQuery("SELECT id, descricao, data, hora FROM partida WHERE status = 'Aguardando' AND id_usuario = '"+this.profile_id+"'", null);
        for(int i = 0; i < total; i++) {
            cur.moveToNext();
            aguardando[i] = cur.getInt(0) + " " + cur.getString(1) + " " + cur.getString(2) + " " + cur.getString(3);
        }

        listaAguardando.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, aguardando));
    }

    private void irParaConsultarPartida(int id_partida) {
        Bundle bundle = new Bundle();
        bundle.putInt("partida_id", id_partida);
        bundle.putString("posicao", this.posicao);
        bundle.putInt("id_goleiro", this.idGoleiro);

        Intent intent = new Intent(Listagens.this, PartidaCons.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void irParaAprovarPartida(int id_partida) {
        Bundle bundle = new Bundle();
        bundle.putInt("partida_id", id_partida);
        bundle.putInt("id_goleiro", obterIdGoleiro(id_partida));

        Intent intent = new Intent(Listagens.this, PartidaAprov.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private int obterIdGoleiro(int id_partida) {
        cur = db.rawQuery("SELECT id_goleiro FROM partida WHERE " +
                "id = "+id_partida, null);

        cur.moveToFirst();
        return cur.getInt(0);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        int itemSelecionado = i;
        String conteudo = (String) adapterView.getItemAtPosition(i);
        String[] dados = conteudo.split(" ");
        int id_partida = Integer.parseInt(dados[0]);

        if (adapterView.getId() == R.id.list_view_pendentes || adapterView.getId() == R.id.list_view_atendidas) {
            irParaConsultarPartida(id_partida);
        } else if (adapterView.getId() == R.id.list_view_aguardando) {
            irParaAprovarPartida(id_partida);
        }
    }
}
