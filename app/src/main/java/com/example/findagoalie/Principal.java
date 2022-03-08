package com.example.findagoalie;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class Principal extends AppCompatActivity implements View.OnClickListener {
    private String profile_id = null;
    private String web_id_token = null;
    private String posicao = null;
    private int idGoleiro = 0;
    private Button btnCadastrar = null;
    private Button btnEditarPerfil = null;
    private Button btnSair = null;
    private Button btnListarPartidas = null;
    private SQLiteDatabase db = null;
    private Cursor cur = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        db = openOrCreateDatabase("BancoDados", Context.MODE_PRIVATE, null);

        inicializarComponentes();
        consultarPosicao();

        if (this.posicao.equals("Goleiro")) {
            atualizarIdGoleiro();
        }
    }

    private void inicializarComponentes() {
        Bundle bundle = getIntent().getExtras();
        this.profile_id = bundle.getString("profile_id");
        this.web_id_token = bundle.getString("web_id_token");

        btnCadastrar = (Button) findViewById(R.id.btn_cadastrar_partida);
        btnCadastrar.setOnClickListener(this);
        btnEditarPerfil = (Button) findViewById(R.id.btn_editar_perfil);
        btnEditarPerfil.setOnClickListener(this);
        btnSair = (Button) findViewById(R.id.btn_sair_principal);
        btnSair.setOnClickListener(this);
        btnListarPartidas = (Button) findViewById(R.id.btn_listar_partidas);
        btnListarPartidas.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cadastrar_partida:
                irParaCadastrarPartida();
                break;
            case R.id.btn_sair_principal:
                deslogar();
                break;
            case R.id.btn_editar_perfil:
                irParaPerfil();
                break;
            case R.id.btn_listar_partidas:
                listarPendentes();
                break;
        }
    }

    private void irParaPerfil() {
        Bundle bundle = new Bundle();
        bundle.putString("profile_id", this.profile_id);
        bundle.putString("web_id_token", this.web_id_token);
        bundle.putString("intent", "Principal");

        Intent intent = new Intent(Principal.this, Perfil.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void irParaCadastrarPartida() {
        Bundle bundle = new Bundle();
        bundle.putString("profile_id", this.profile_id);

        Intent intent = new Intent(Principal.this, PartidaCad.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void consultarPosicao() {
        cur = db.rawQuery("SELECT posicao FROM usuario WHERE " +
                "profile_id = '"+this.profile_id+"'", null);

        cur.moveToFirst();
        this.posicao = cur.getString(0);
    }

    private void atualizarIdGoleiro() {
        cur = db.rawQuery("SELECT id FROM usuario WHERE " +
                "profile_id = '"+this.profile_id+"'", null);

        cur.moveToFirst();
        this.idGoleiro = cur.getInt(0);
    }

    private void listarPendentes() {
        Bundle bundle = new Bundle();
        bundle.putString("posicao", this.posicao);
        bundle.putInt("id_goleiro", this.idGoleiro);
        bundle.putString("profile_id", this.profile_id);

        Intent intent = new Intent(Principal.this, Listagens.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void deslogar() {
        FirebaseAuth.getInstance().signOut();
        disconnectFromFacebook();
        disconnectFromGoogle();
        Intent intent = new Intent(Principal.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
        finish();
    }

    private void disconnectFromGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(web_id_token)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
    }

    private void disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return;
        }

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                LoginManager.getInstance().logOut();
            }
        }).executeAsync();
    }
}
