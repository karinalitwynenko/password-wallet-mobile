package bsi.passwordWallet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    Context context;
    final static String DATABASE_NAME = "password-wallet.db";

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        if(!dbFile.exists())
            getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table if not exists users (" +
                        "user_id integer primary key autoincrement, " +
                        "login text not null unique, " +
                        "encryption_type text not null, " +
                        "password_hash text not null, " +
                        "salt text not null" +
                        ");"
        );

        db.execSQL(
                "create table if not exists passwords (" +
                        "password_id integer primary key autoincrement, " +
                        "user_id integer not null, " +
                        "login text not null, " +
                        "password text not null, " +
                        "iv text not null, " +
                        "description text, " +
                        "website text not null, " +
                        "foreign key (user_id) references users (user_id)" +
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

}
