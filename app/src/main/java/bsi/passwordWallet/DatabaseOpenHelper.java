package bsi.passwordWallet;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

import androidx.annotation.Nullable;

import static android.content.Context.MODE_PRIVATE;

class DatabaseOpenHelper extends SQLiteOpenHelper {
    Context context;
    final static String DATABASE_NAME = "password-wallet";

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("APP", "oncreate");
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
                        "text description, " +
                        "login text not null, " +
                        "website text not null, " +
                        "foreign key (user_id) references users (user_id)" +
                        ");"
        );

        //db.execSQL("insert into users values(null, 'user_a', 'passwd')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    public SQLiteDatabase openDatabase() throws SQLException {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(
                context.getDatabasePath(DATABASE_NAME).getPath(),
                null,
                SQLiteDatabase.CREATE_IF_NECESSARY
        );
        return db;
    }
}
