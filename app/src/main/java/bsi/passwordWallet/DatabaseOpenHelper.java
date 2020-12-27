package bsi.passwordWallet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    Context context;
    public final static String DATABASE_NAME = "password-wallet.db";

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
                "create table if not exists login_log (" +
                        "log_id integer primary key autoincrement," +
                        "user_id integer not null," +
                        "ip_address text not null," +
                        "login_time integer not null, " +
                        "login_result text not null, " +
                        "ignore_fail integer not null default 0," +
                        "foreign key (user_id) references users (user_id)" +
                        ");"
        );

        db.execSQL(
                "create table if not exists blocked_ips (" +
                        "blocked_ip_id integer primary key autoincrement," +
                        "user_id integer not null," +
                        "ip_address text not null," +
                        "foreign key (user_id) references users (user_id)" +
                        ");"
        );

        db.execSQL(
                "create table if not exists passwords (" +
                        "password_id integer primary key autoincrement," +
                        "user_id integer not null," +
                        "login text not null," +
                        "password text not null," +
                        "iv text not null," +
                        "description text," +
                        "website text not null," +
                        "foreign key (user_id) references users (user_id)" +
                        ");"
        );

        db.execSQL(
                "create table if not exists shared_passwords (" +
                        "shared_password_id integer primary key autoincrement, " +
                        "password_id not null, " +
                        "part_owner_id not null, " +
                        "foreign key (password_id) references passwords (password_id)," +
                        "foreign key (part_owner_id) references users (user_id)" +
                        ");"
        );

        db.execSQL(
                "create table if not exists activity_logs (" +
                        "activity_id integer primary key autoincrement," +
                        "user_id integer not null," +
                        "password_id integer not null," +
                        "time integer not null," +
                        "function text not null," +
                        "foreign key (password_id) references passwords (password_id)," +
                        "foreign key (user_id) references users (user_id)" +
                        ");"
        );

        db.execSQL(
                "create table if not exists password_changes (" +
                        "password_change_id integer primary key autoincrement," +
                        "password_id integer not null," +
                        "time integer not null," +
                        "record_name text not null," +
                        "action_type text not null," +
                        "previous_value text not null," +
                        "new_value text not null," +
                        "foreign key (password_id) references passwords (password_id)" +
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

}
