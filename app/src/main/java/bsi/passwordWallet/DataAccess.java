package bsi.passwordWallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

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
    static String IV = "iv";

    public DataAccess(Context context) {
        database = new DatabaseOpenHelper(context).getWritableDatabase();
    }

    public static User getUser(String login) {
        Cursor cursor = database.rawQuery("select * from users where login = ?", new String[] {login});
        if(cursor.getCount() == 0)
            return null; // user's account doesn't exist
        else {
            cursor.moveToFirst();
            return new User(
                    cursor.getLong(cursor.getColumnIndex(USER_ID)),
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

    public static ArrayList<Password> getPasswords(long userID) {
        Cursor cursor = database.rawQuery(
                "select * from passwords where user_id = ?",
                new String[] {String.valueOf(userID)}
                );

        ArrayList<Password> passwords = new ArrayList<>();

        // loop through cursor
        while(cursor.moveToNext()) {
            passwords.add(
                    new Password(
                            cursor.getLong(cursor.getColumnIndex(PASSWORD_ID)),
                            cursor.getLong(cursor.getColumnIndex(USER_ID)),
                            cursor.getString(cursor.getColumnIndex(LOGIN)),
                            cursor.getString(cursor.getColumnIndex(PASSWORD)),
                            cursor.getString(cursor.getColumnIndex(IV)),
                            cursor.getString(cursor.getColumnIndex(WEBSITE)),
                            cursor.getString(cursor.getColumnIndex(DESCRIPTION))
                    )
            );
        }

        return passwords;
    }

    public static Password createPassword(long userID, String login, String password, String iv, String website, String description) {
        ContentValues values = new ContentValues();
        values.put(USER_ID, userID);
        values.put(LOGIN, login);
        values.put(PASSWORD, password);
        values.put(IV, iv);
        values.put(WEBSITE, website);
        values.put(DESCRIPTION, description);

        long passwordID = database.insert("passwords", null, values);
        // check if the row was successfully inserted
        if(passwordID != -1)
            return new Password(passwordID, userID, login, password, iv, website, description);
        else
            return null;
    }

    public static boolean deletePassword(long passwordID) {
        return database.delete(
                "passwords", "password_id = ?", new String[] {String.valueOf(passwordID)}
                ) > 0;

    }

    static void close() {
        database.close();
    }
}
