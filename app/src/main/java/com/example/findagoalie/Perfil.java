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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class Perfil extends AppCompatActivity implements View.OnClickListener {
    private String posicao = null;
    private String profile_id = null;
    private String nome = null;
    private String web_id_token;
    private String intent = null;
    private int idGoleiro = 0;
    private TextView tvPosicao = null;
    private EditText etNome = null;
    private EditText etIdade = null;
    private EditText etTelefone = null;
    private EditText etEmail = null;
    private RadioGroup rdgPosicao = null;
    private Button btnSalvar = null;
    private Button btnSair = null;
    private SQLiteDatabase db = null;
    private Cursor cur = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        db = openOrCreateDatabase("BancoDados", Context.MODE_PRIVATE, null);

        recebeInformacoes();

        if (this.intent.equals("Login")) {
            if (verificarDadosPerfil()) {
                irParaPrincipal();
            } else {
                inicializarComponentes();
            }
        } else if (this.intent.equals("Principal")) {
            inicializarComponentes();
            preencherCampos();
        } else if (this.intent.equals("PartidaAprov")) {
            inicializarComponentes();
            preencherCampos();
            trancarCampos();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_salvar:
                salvarPerfil();
                break;
            case R.id.btn_sair:
                deslogar();
                break;
        }
    }

    private void recebeInformacoes() {
        Bundle bundle = getIntent().getExtras();
        this.nome = bundle.getString("nome");
        this.profile_id = bundle.getString("profile_id");
        this.web_id_token = bundle.getString("web_id_token");
        this.intent = bundle.getString("intent");
        this.idGoleiro = bundle.getInt("id_goleiro");
    }

    private void preencherCampos() {
       if (this.intent.equals("Principal")) {
            cur = db.rawQuery("SELECT * FROM usuario WHERE profile_id = '" + this.profile_id + "'", null);
            cur.moveToFirst();
            etNome.setText(cur.getString(2));
            etIdade.setText(cur.getString(3));
            etTelefone.setText(cur.getString(4));
            etEmail.setText(cur.getString(5));

            if (cur.getString(6).equals("Goleiro")) {
                rdgPosicao.check(R.id.rbt_goleiro);
            } else {
                rdgPosicao.check(R.id.rbt_jogador);
            }
       } else {
            cur = db.rawQuery("SELECT * FROM usuario WHERE id = "+this.idGoleiro, null);
            cur.moveToFirst();
            etNome.setText(cur.getString(2));
            etIdade.setText(cur.getString(3));
            etTelefone.setText(cur.getString(4));
            etEmail.setText(cur.getString(5));

            if (cur.getString(6).equals("Goleiro")) {
                rdgPosicao.check(R.id.rbt_goleiro);
            } else {
                rdgPosicao.check(R.id.rbt_jogador);
            }
       }
    }

    private void trancarCampos() {
        tvPosicao.setVisibility(View.INVISIBLE);
        etNome.setEnabled(false);
        etIdade.setEnabled(false);
        etTelefone.setEnabled(false);
        etEmail.setEnabled(false);
        rdgPosicao.setVisibility(View.INVISIBLE);
        btnSalvar.setVisibility(View.INVISIBLE);
        btnSair.setVisibility(View.INVISIBLE);
    }

    private void inicializarComponentes() {
        tvPosicao = (TextView) findViewById(R.id.tv_posicao);
        etNome = (EditText) findViewById(R.id.et_nome);
        etNome.setText(nome);
        etIdade = (EditText) findViewById(R.id.et_idade);
        etTelefone = (EditText) findViewById(R.id.et_telefone);
        etEmail = (EditText) findViewById(R.id.et_email);
        btnSalvar = (Button) findViewById(R.id.btn_salvar);
        btnSalvar.setOnClickListener(this);
        btnSair = (Button) findViewById(R.id.btn_sair);
        btnSair.setOnClickListener(this);
        rdgPosicao = (RadioGroup) findViewById(R.id.rdg_posicao);

        rdgPosicao.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rbt_goleiro:
                        posicao = "Goleiro";
                        break;
                    case R.id.rbt_jogador:
                        posicao = "Jogador";
                        break;
                }
            }
        });
    }

    private boolean verificarDadosPerfil() {
        cur = db.rawQuery("SELECT * FROM usuario WHERE " +
                "profile_id = '"+this.profile_id+"'", null);
        if (cur != null && cur.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    private void irParaPrincipal() {
        Bundle bundle = new Bundle();
        bundle.putString("profile_id", this.profile_id);
        bundle.putString("web_id_token", this.web_id_token);

        Intent intent = new Intent(Perfil.this, Principal.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void salvarPerfil() {
        if (rdgPosicao.getCheckedRadioButtonId() == -1) {
            Toast.makeText(Perfil.this,"Informe a posição!",
                    Toast.LENGTH_SHORT).show();
        } else {
            if (!isEmpty(etNome) && !isEmpty(etIdade) && !isEmpty(etTelefone) && !isEmpty(etEmail)) {
                if (this.intent.equals("Principal")) {
                    ContentValues dados = new ContentValues();
                    dados.put("nome", etNome.getText().toString());
                    dados.put("idade", Integer.parseInt(etIdade.getText().toString()));

                    switch (rdgPosicao.getCheckedRadioButtonId()) {
                        case R.id.rbt_goleiro:
                            dados.put("posicao", posicao);
                            break;
                        case R.id.rbt_jogador:
                            dados.put("posicao", posicao);
                            break;
                    }

                    dados.put("telefone", etTelefone.getText().toString());
                    dados.put("email", etEmail.getText().toString());

                    db.update("usuario", dados, "profile_id = '"+this.profile_id+"'", null);
                    Toast.makeText(Perfil.this,"Informações atualizadas!",
                            Toast.LENGTH_SHORT).show();

                    irParaPrincipal();
                } else {
                    ContentValues dados = new ContentValues();
                    dados.put("profile_id", profile_id);
                    dados.put("nome", etNome.getText().toString());
                    dados.put("idade", Integer.parseInt(etIdade.getText().toString()));

                    switch (rdgPosicao.getCheckedRadioButtonId()) {
                        case R.id.rbt_goleiro:
                            dados.put("posicao", posicao);
                            break;
                        case R.id.rbt_jogador:
                            dados.put("posicao", posicao);
                            break;
                    }

                    dados.put("telefone", etTelefone.getText().toString());
                    dados.put("email", etEmail.getText().toString());
                    dados.put("avaliacao", "Não consta");

                    db.insert("usuario", null, dados);
                    Toast.makeText(Perfil.this,"Registro efetuado com sucesso!",
                            Toast.LENGTH_SHORT).show();

                    irParaPrincipal();
                }
            } else {
                Toast.makeText(Perfil.this,"É necessário preencher todos os campos!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    private void deslogar() {
        FirebaseAuth.getInstance().signOut();
        disconnectFromFacebook();
        disconnectFromGoogle();
        Intent intent = new Intent(Perfil.this, MainActivity.class);
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
