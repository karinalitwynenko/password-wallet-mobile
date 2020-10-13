package bsi.passwordWallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class DataAccess {
    static SQLiteDatabase database;
    static String USER_ID = "user_id";
    static String LOGIN = "login";
    static String ENCRYPTION_TYPE = "encryption_type";
    static String PASSWORD_HASH = "password_hash";
    static String SALT = "salt";

    static String PASSWORD_ID = "password_id";
    static String WEBSITE = "website";
    static String DESCRIPTION = "description";
    static String PASSWORD = "password";




    public DataAccess(Context context) {
        database = new DatabaseOpenHelper(context).getReadableDatabase();
    }

    public static User getUser(String login) {
        Cursor cursor = database.rawQuery("select * from users where login = ?", new String[] {login});
        if(cursor.getCount() == 0)
            return null; // user's account doesn't exist
        else {
            cursor.moveToFirst();
            return new User(
                    cursor.getInt(cursor.getColumnIndex(USER_ID)),
                    cursor.getString(cursor.getColumnIndex(LOGIN)),
                    cursor.getString(cursor.getColumnIndex(ENCRYPTION_TYPE)),
                    cursor.getString(cursor.getColumnIndex(PASSWORD_HASH)),
                    cursor.getString(cursor.getColumnIndex(SALT))
            );
        }

    }

    public static User createUser(String login, String encryptionMethod, String passwordHash, String salt) {
        ContentValues values = new ContentValues();
        values.put(LOGIN, login);
        values.put(ENCRYPTION_TYPE, encryptionMethod);
        values.put(PASSWORD_HASH, passwordHash);
        values.put(SALT, salt);

        long userID = database.insert("users", null, values);

        // if the row was successfully inserted
        if(userID != -1) {
            return new User(userID, login, encryptionMethod, passwordHash, salt);
        }
        else
            return null;
    }

    public static Password getPassword(long passwordID) {
        Cursor cursor = database.rawQuery(
                "select * from passwords where password_id = ?",
                new String[] {String.valueOf(passwordID)}
                );

        if(cursor.getCount() == 0)
            return null; // password doesn't exist

        return new Password("23");
    }

    public static Password createPassword(String login, String password, String website, String description) {
        ContentValues values = new ContentValues();
        values.put(LOGIN, login);
        values.put(PASSWORD, password);
        values.put(WEBSITE, website);
        values.put(DESCRIPTION, description);

        long passwordID = database.insert("passwords", null, values);
        // check if the row was successfully inserted
        if(passwordID != -1)
            return new Password(passwordID, login, password, website, description);
        else
            return null;
    }


}
