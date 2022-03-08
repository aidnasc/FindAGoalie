package com.example.findagoalie;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PartidaCad extends AppCompatActivity implements View.OnClickListener, LocationListener {
    private boolean gravando = false;
    private String profile_id = null;
    private EditText etDescricao = null;
    private EditText etEndereco = null;
    private EditText etLatitude = null;
    private EditText etLongitude = null;
    private EditText etData = null;
    private EditText etHora = null;
    private Button btnSalvar = null;
    private Button btnLimpar = null;
    private Button btnVoltar = null;
    private Button btnCoord = null;
    private SQLiteDatabase db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partida_cad);

        db = openOrCreateDatabase("BancoDados", Context.MODE_PRIVATE, null);

        inicializarComponentes();

        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private void inicializarComponentes() {
        Bundle bundle = getIntent().getExtras();
        this.profile_id = bundle.getString("profile_id");

        etDescricao = (EditText) findViewById(R.id.et_desc_partida);
        etEndereco = (EditText) findViewById(R.id.et_end_partida);
        etLatitude = (EditText) findViewById(R.id.et_latitude);
        etLongitude = (EditText) findViewById(R.id.et_longitude);
        etData = (EditText) findViewById(R.id.et_data_partida);
        etHora = (EditText) findViewById(R.id.et_hora_partida);
        btnSalvar = (Button) findViewById(R.id.btn_salvar_partida);
        btnSalvar.setOnClickListener(this);
        btnLimpar = (Button) findViewById(R.id.btn_limpar_partida);
        btnLimpar.setOnClickListener(this);
        btnVoltar = (Button) findViewById(R.id.btn_voltar_partidacad);
        btnVoltar.setOnClickListener(this);
        btnCoord = (Button) findViewById(R.id.btn_coord_partidacad);
        btnCoord.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_salvar_partida:
                salvarPartida();
                break;
            case R.id.btn_limpar_partida:
                limparCampos();
                break;
            case R.id.btn_coord_partidacad:
                gravarLocalizacao();
                break;
            case R.id.btn_voltar_partidacad:
                finish();
                break;
        }
    }

    private void limparCampos() {
        etDescricao.setText("");
        etEndereco.setText("");
        etLatitude.setText("");
        etLongitude.setText("");
        etData.setText("");
        etHora.setText("");
    }

    private void gravarLocalizacao() {
        gravando = true;
    }

    private void salvarPartida() {
        if (!isEmpty(etDescricao) && !isEmpty(etEndereco) && !isEmpty(etData) && !isEmpty(etHora)) {
            ContentValues dados = new ContentValues();
            dados.put("descricao", etDescricao.getText().toString());
            dados.put("endereco", etEndereco.getText().toString());

            if (!isEmpty(etLatitude)) {
                dados.put("latitude", etLatitude.getText().toString());
            }

            if (!isEmpty(etLongitude)) {
                dados.put("longitude", etLongitude.getText().toString());
            }

            dados.put("data", etData.getText().toString());
            dados.put("hora", etHora.getText().toString());
            dados.put("status", "Pendente");
            dados.put("id_usuario", profile_id);

            db.insert("partida", null, dados);
            Toast.makeText(PartidaCad.this,"Registro efetuado com sucesso!",
                    Toast.LENGTH_SHORT).show();

            limparCampos();
            gravando = false;
        } else {
            Toast.makeText(PartidaCad.this,"É necessário preencher todos os campos!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (gravando) {
            etLongitude.setText(location.getLongitude()+"");
            etLatitude.setText(location.getLatitude()+"");
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
