package bsi.passwordWallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

class DataAccess {
    static SQLiteDatabase database;

    /** database table names */
    static final String USER_TABLE = "users";
    static final String PASSWORD_TABLE = "passwords";

    static final String USER_ID = "user_id";
    static final String LOGIN = "login";
    static final String ENCRYPTION_TYPE = "encryption_type";
    static final String PASSWORD_HASH = "password_hash";
    static final String SALT = "salt";

    static final String PASSWORD_ID = "password_id";
    static final String WEBSITE = "website";
    static final String DESCRIPTION = "description";
    static final String PASSWORD = "password";
    static final String IV = "iv";

    public static void initialize(Context context) {
        database = new DatabaseOpenHelper(context).getWritableDatabase();
    }

    /**
     * Retrieves user with specified login
     * @param login unique login
     * @return instance of User if login exists, null otherwise
     */
    public static User getUser(String login) {
        Cursor cursor = database.rawQuery("select * from " + USER_TABLE + " where login = ?", new String[] {login});
        // check if user with specified login exists
        if(cursor.getCount() == 0)
            return null;
        else {
            cursor.moveToFirst();
            // return instance of User
            return new User(
                    cursor.getLong(cursor.getColumnIndex(USER_ID)),
                    cursor.getString(cursor.getColumnIndex(LOGIN)),
                    cursor.getString(cursor.getColumnIndex(ENCRYPTION_TYPE)),
                    cursor.getString(cursor.getColumnIndex(PASSWORD_HASH)),
                    cursor.getString(cursor.getColumnIndex(SALT))
            );
        }
    }

    /**
     * Creates new user
     * @param login unique user login
     * @param encryptionMethod method for hash calculation
     * @param passwordHash password hash
     * @param salt salt used to calculate password hash
     * @return instance of User if created successfully, null otherwise
     */
    public static User createUser(String login, String encryptionMethod, String passwordHash, String salt) {
        ContentValues values = new ContentValues();
        values.put(LOGIN, login);
        values.put(ENCRYPTION_TYPE, encryptionMethod);
        values.put(PASSWORD_HASH, passwordHash);
        values.put(SALT, salt);

        long userID = database.insert(USER_TABLE, null, values);

        // if the row was successfully inserted
        if(userID != -1) {
            return new User(userID, login, encryptionMethod, passwordHash, salt);
        }
        else
            return null;
    }

    /**
     * Updates password hash and salt for user's account
     * @param userID id of user for whom account's password should be updated
     * @param newPassword hash of the new password
     * @param newSalt salt used to calculate password hash
     * @return true if updated successfully
     */
    public static boolean updateUserPassword(long userID, String newPassword, String newSalt) {
        ContentValues values = new ContentValues();
        values.put(PASSWORD_HASH, newPassword);
        values.put(SALT, newSalt);

        database.beginTransaction();
        if(database.update(USER_TABLE, values, "user_id = ?", new String[] {String.valueOf(userID)}) > 0)
            return true;
        else {
            database.endTransaction(); // rollback
            return false;
        }
    }

    /**
     * @param userID id of user for whom passwords should be retrieved
     * @return ArrayList of Password objects
     */
    public static ArrayList<Password> getPasswords(long userID) {
        Cursor cursor = database.rawQuery(
                "select * from " + PASSWORD_TABLE + " where user_id = ?",
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

    /**
     *
     * @param userID id of user for whom password should be created
     * @param login login to the website
     * @param password encrypted password
     * @param iv initialization vector for password encryption/decryption
     * @param website website name
     * @param description item description
     * @return new Password instance if created successfully, null if failed
     */
    public static Password createPassword(long userID, String login, String password, String iv, String website, String description) {
        ContentValues values = new ContentValues();
        values.put(USER_ID, userID);
        values.put(LOGIN, login);
        values.put(PASSWORD, password);
        values.put(IV, iv);
        values.put(WEBSITE, website);
        values.put(DESCRIPTION, description);

        long passwordID = database.insert(PASSWORD_TABLE, null, values);
        // check if the row was successfully inserted
        if(passwordID != -1)
            return new Password(passwordID, userID, login, password, iv, website, description);
        else
            return null;
    }

    /**
     *
     * @param passwordID id of password that should be deleted
     * @return true if password deleted successfully
     */
    public static boolean deletePassword(long passwordID) {
        return database.delete(
                PASSWORD_TABLE, "password_id = ?", new String[] {String.valueOf(passwordID)}
                ) > 0;
    }


    /**
     * Updates encrypted password.
     * Begin transaction before calling this method.
     * @param passwords ArrayList of Password objects
     * @return true if passwords updated successfully
     */
    public static boolean updatePasswords(ArrayList<Password> passwords) {
        ContentValues values = new ContentValues();

        for(Password p : passwords) {
            values.put(PASSWORD, p.getPassword());
            if(database.update(PASSWORD_TABLE, values, "password_id = ?",
                    new String[] {String.valueOf(p.getPasswordID())}) == 0) {

                // rollback
                database.endTransaction();
                return false;
            }
        }

        // transaction successful -> commit
        database.setTransactionSuccessful();
        database.endTransaction();
        return true;
    }

    /** Close the database connection */
    static void close() {
        database.close();
    }
}
