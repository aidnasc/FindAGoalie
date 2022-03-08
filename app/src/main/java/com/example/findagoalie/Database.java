package com.example.findagoalie;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    public Database(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public Database(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table usuario(id integer primary key autoincrement, " +
                "profile_id text not null, " +
                "nome text not null, " +
                "idade int not null, " +
                "telefone text not null, "+
                "email text not null, "+
                "posicao text not null, " +
                "avaliacao text);");

        db.execSQL("create table partida(id integer primary key autoincrement, " +
                "descricao text not null, " +
                "endereco text not null, " +
                "latitude decimal(10, 8), " +
                "longitude decimal(10, 8), "+
                "data text not null, "+
                "hora text not null, " +
                "status text not null, "+
                "id_usuario text not null, "+
                "id_goleiro int, "+
                "constraint fk_usuario_partida foreign key (id_usuario) references usuario (profile_id), "+
                "constraint fk_goleiro_partida foreign key (id_goleiro) references usuario (id));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
