package bsi.passwordWallet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DataAccess {
    private static SQLiteDatabase database;
    private static final DataAccess instance = new DataAccess();
    /** database table names */
    static final String USER_TABLE = "users";
    static final String PASSWORD_TABLE = "passwords";
    static final String LOGIN_LOG_TABLE = "login_log";
    static final String BLOCKED_IPS_TABLE = "blocked_ips";
    static final String SHARED_PASSWORDS_TABLE = "shared_passwords";
    static final String ACTIVITY_LOGS_TABLE = "activity_logs";
    static final String PASSWORD_CHANGES_TABLE = "password_changes";

    private ContentValues contentValues = new ContentValues();

    public static DataAccess getInstance() {
        return instance;
    }

    public static void initialize(SQLiteDatabase database) {
        DataAccess.database = database;
    }

    public void setContentValues(ContentValues contentValues) {
        this.contentValues = contentValues;
    }

    /**
     * Retrieves user with specified login
     * @param login unique login
     * @return instance of User if login exists, null otherwise
     */
    public User getUser(String login) {
        Cursor cursor = database.rawQuery("select * from " + USER_TABLE + " where login = ?", new String[] {login});
        User user = null;

        if(cursor.moveToFirst()) {
            user = new User(
                    cursor.getLong(cursor.getColumnIndex(User.USER_ID)),
                    cursor.getString(cursor.getColumnIndex(User.LOGIN)),
                    cursor.getString(cursor.getColumnIndex(User.ENCRYPTION_TYPE)),
                    cursor.getString(cursor.getColumnIndex(User.PASSWORD_HASH)),
                    cursor.getString(cursor.getColumnIndex(User.SALT))
            );
        }

        cursor.close();
        return user;
    }

    /**
     * Creates new user
     * @param login unique user login
     * @param encryptionMethod method for hash calculation
     * @param passwordHash password hash
     * @param salt salt used to calculate password hash
     * @return instance of User if created successfully, null otherwise
     */
    public User createUser(String login, String encryptionMethod, String passwordHash, String salt) {
        contentValues.clear();
        contentValues.put(User.LOGIN, login);
        contentValues.put(User.ENCRYPTION_TYPE, encryptionMethod);
        contentValues.put(User.PASSWORD_HASH, passwordHash);
        contentValues.put(User.SALT, salt);

        long userID = database.insert(USER_TABLE, null, contentValues);

        // if the row was successfully inserted
        if(userID != -1)
            return new User(userID, login, encryptionMethod, passwordHash, salt);
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
    public boolean updateUserMasterPassword(long userID, String newPassword, String newSalt) {
        contentValues.clear();
        contentValues.put(User.PASSWORD_HASH, newPassword);
        contentValues.put(User.SALT, newSalt);

        database.beginTransaction();
        if(database.update(USER_TABLE, contentValues, "user_id = ?", new String[] {String.valueOf(userID)}) > 0)
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
    @NonNull
    public ArrayList<Password> getPasswords(long userID) {
        Cursor cursor = database.rawQuery(
                "select * from " + PASSWORD_TABLE + " where user_id = ?",
                new String[] {String.valueOf(userID)}
                );

        ArrayList<Password> passwords = new ArrayList<>();

        // loop through cursor
        while(cursor.moveToNext()) {
            passwords.add(
                    new Password(
                            cursor.getLong(cursor.getColumnIndex(Password.PASSWORD_ID)),
                            cursor.getLong(cursor.getColumnIndex(Password.USER_ID)),
                            cursor.getString(cursor.getColumnIndex(Password.LOGIN)),
                            cursor.getString(cursor.getColumnIndex(Password.PASSWORD)),
                            cursor.getString(cursor.getColumnIndex(Password.IV)),
                            cursor.getString(cursor.getColumnIndex(Password.WEBSITE)),
                            cursor.getString(cursor.getColumnIndex(Password.DESCRIPTION))
                    )
            );
        }
        cursor.close();

        return passwords;
    }

    /**
     * @param userID id of user for whom password should be created
     * @param login login to the website
     * @param password encrypted password
     * @param iv initialization vector for password encryption/decryption
     * @param website website name
     * @param description item description
     * @return new Password instance if created successfully, null if failed
     */
    public Password createPassword(long userID, String login, String password, String iv, String website, String description) {
        contentValues.clear();
        contentValues.put(Password.USER_ID, userID);
        contentValues.put(Password.LOGIN, login);
        contentValues.put(Password.PASSWORD, password);
        contentValues.put(Password.IV, iv);
        contentValues.put(Password.WEBSITE, website);
        contentValues.put(Password.DESCRIPTION, description);

        long passwordID = database.insert(PASSWORD_TABLE, null, contentValues);
        // check if the row was successfully inserted
        if(passwordID != -1)
            return new Password(passwordID, userID, login, password, iv, website, description);
        else
            return null;
    }

    public boolean updatePassword(Password password) {
        contentValues.clear();
        contentValues.put(Password.USER_ID, password.getUserId());
        contentValues.put(Password.LOGIN, password.getLogin());
        contentValues.put(Password.PASSWORD, password.getPassword());
        contentValues.put(Password.IV, password.getIV());
        contentValues.put(Password.WEBSITE, password.getWebsite());
        contentValues.put(Password.DESCRIPTION, password.getDescription());

        long passwordID = database.update(
                PASSWORD_TABLE,
                contentValues,
                "password_id=?",
                new String[] {password.getId() + ""}
                );

        return passwordID != -1;
    };

    /**
     * @param passwordID id of password that should be deleted
     * @return true if password deleted successfully
     */
    public boolean deletePassword(long passwordID) {
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
    public boolean updatePasswordHashes(ArrayList<Password> passwords) {
        for(Password p : passwords) {
            contentValues.clear();
            contentValues.put(Password.PASSWORD, p.getPassword());
            contentValues.put(Password.IV, p.getIV());
            if(database.update(PASSWORD_TABLE, contentValues, "password_id = ?",
                    new String[] {p.getId() + ""}) == 0) {

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

    public boolean createLoginLog(long userID, String ipAddress, long loginTime, String loginResult) {
        contentValues.clear();
        contentValues.put(LoginLog.USER_ID, userID);
        contentValues.put(LoginLog.IP_ADDRESS, ipAddress);
        contentValues.put(LoginLog.LOGIN_TIME, loginTime);
        contentValues.put(LoginLog.LOGIN_RESULT, loginResult);

        long logID = database.insert(LOGIN_LOG_TABLE, null, contentValues);

        if(logID != -1)
            return true;
        else
            return false;
    }

    public ArrayList<LoginLog> getLoginLogs(long userID, @Nullable String ipAddress, int limit) {
        Cursor cursor;
        String queryLimit;
        if(limit != -1)
            queryLimit = " limit " + limit;
        else
            queryLimit = "";

        if(ipAddress != null) {
                cursor = database.rawQuery(
                "select * from " + LOGIN_LOG_TABLE
                        + " where " + LoginLog.USER_ID + "=? and " + LoginLog.IP_ADDRESS + "=? "
                        + " order by " + LoginLog.LOG_ID + " desc " + queryLimit,
                    new String[] {userID + "", ipAddress}
                );
        }
        else {
            cursor = database.rawQuery(
                    "select * from " + LOGIN_LOG_TABLE
                            + " where " + LoginLog.USER_ID + "=?"
                            + " order by " + LoginLog.LOG_ID + " desc " + queryLimit,
                    new String[] {userID + ""}
            );
        }

        ArrayList<LoginLog> logs = new ArrayList<>();

        while(cursor.moveToNext()) {
            logs.add(
                    new LoginLog(
                            cursor.getLong(cursor.getColumnIndex(LoginLog.LOG_ID)),
                            cursor.getLong(cursor.getColumnIndex(LoginLog.USER_ID)),
                            cursor.getString(cursor.getColumnIndex(LoginLog.IP_ADDRESS)),
                            cursor.getLong(cursor.getColumnIndex(LoginLog.LOGIN_TIME)),
                            cursor.getString(cursor.getColumnIndex(LoginLog.LOGIN_RESULT)),
                            cursor.getInt(cursor.getColumnIndex(LoginLog.IGNORE_FAIL))
                    )
            );
        }
        cursor.close();

        return logs;
    }

    public boolean createBlockedIP(long userID, String ipAddress) {
        contentValues.clear();
        contentValues.put(LoginLog.USER_ID, userID);
        contentValues.put(LoginLog.IP_ADDRESS, ipAddress);

        long id = database.insert(BLOCKED_IPS_TABLE, null, contentValues);

        if(id != -1)
            return true;
        else
            return false;
    }

    public ArrayList<String> getBlockedIPs(long userID) {
        Cursor cursor = database.rawQuery(
                "select ip_address from " + BLOCKED_IPS_TABLE
                        + " where user_id = ?",
                new String[] {userID + ""}
        );

        ArrayList<String> ips = new ArrayList<>();
        while(cursor.moveToNext()) {
            ips.add(cursor.getString(0));
        }
        cursor.close();

        return ips;
    }

    public boolean deleteBlockedIP(String ipAddress) {
        database.beginTransaction();
        if(database.delete(BLOCKED_IPS_TABLE, "ip_address = ?", new String[] {ipAddress}) > 0) {
            contentValues.clear();
            contentValues.put(LoginLog.IGNORE_FAIL, 1);

            if(database.update(LOGIN_LOG_TABLE, contentValues, "ip_address = ?", new String[] {ipAddress}) > 0) {
                database.setTransactionSuccessful();
                database.endTransaction();
                return true;
            }
        }

        database.endTransaction();
        return false;
    }

    /** Closes the database connection */
    public static void close() {
        database.close();
    }

    public boolean addSharedPassword(long passwordId, long partOwnerId) {
        contentValues.clear();
        contentValues.put("password_id", passwordId);
        contentValues.put( "part_owner_id", partOwnerId);

        long sharedPasswordId = database.insert(SHARED_PASSWORDS_TABLE, null, contentValues);

        return sharedPasswordId != -1;
    }

    /**
     * Gets passwords that have been shared with user with specified id
     * @param userId
     * @return list of passwords shared with the user - empty if no passwords found
     */
    @NonNull
    public ArrayList<Password> getSharedPasswords(long userId) {
        ArrayList<Password> passwords = new ArrayList<>();

        String sql = "select p.* from " + PASSWORD_TABLE + " p inner join " + SHARED_PASSWORDS_TABLE
                + " sp on sp.password_id = p.password_id where sp.part_owner_id = " + userId;

        Cursor cursor = database.rawQuery(sql, null);

        if(cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                passwords.add(
                        new Password(
                                cursor.getLong(cursor.getColumnIndex(Password.PASSWORD_ID)),
                                cursor.getLong(cursor.getColumnIndex(Password.USER_ID)),
                                cursor.getString(cursor.getColumnIndex(Password.LOGIN)),
                                cursor.getString(cursor.getColumnIndex(Password.PASSWORD)),
                                cursor.getString(cursor.getColumnIndex(Password.IV)),
                                cursor.getString(cursor.getColumnIndex(Password.WEBSITE)),
                                cursor.getString(cursor.getColumnIndex(Password.DESCRIPTION))
                        )
                );
            }
        }
        cursor.close();

        return passwords;
    }

    @NonNull
    public ArrayList<String> getPartOwners(long passwordId) {
        ArrayList<String> userLogins = new ArrayList<>();

        String sql = "select u.login from " + SHARED_PASSWORDS_TABLE + " sp inner join " + PASSWORD_TABLE
                + " p on sp.password_id = p.password_id inner join " + USER_TABLE +
                " u on u.user_id = sp.part_owner_id where p.password_id = ?";

        Cursor cursor = database.rawQuery(sql, new String[] {passwordId + ""});

        if(cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                userLogins.add(cursor.getString(0));
            }
        }
        cursor.close();

        return userLogins;
    }

    @NonNull
    public String getPasswordOwner(long passwordId) {
        String resultLogin = "";

        String sql = "select u.login from " + PASSWORD_TABLE
                + " p inner join " + USER_TABLE +
                " u on u.user_id = p.user_id where p.password_id = ?";

        Cursor cursor = database.rawQuery(sql, new String[] {passwordId + ""});

        if(cursor.getCount() > 0) {
            cursor.moveToNext();
            resultLogin = cursor.getString(0);
        }
        cursor.close();

        return resultLogin;
    }

    @NonNull
    public ArrayList<ActivityLog> getActivityLogs(long userId) {
        ArrayList<ActivityLog> logs = new ArrayList<>();

        Cursor cursor = database.rawQuery(
                "select * from " + ACTIVITY_LOGS_TABLE + " where user_id = ?",
                new String[] {userId + ""}
                );

        while(cursor.moveToNext()) {
            logs.add(
                    new ActivityLog(
                            cursor.getLong(cursor.getColumnIndex(ActivityLog.ACTIVITY_ID)),
                            cursor.getLong(cursor.getColumnIndex(ActivityLog.USER_ID)),
                            cursor.getLong(cursor.getColumnIndex(ActivityLog.PASSWORD_ID)),
                            cursor.getLong(cursor.getColumnIndex(ActivityLog.TIME)),
                            cursor.getString(cursor.getColumnIndex(ActivityLog.FUNCTION))
                            )
            );
        }
        cursor.close();

        return logs;
    }

    public boolean addActivityLog(ActivityLog activityLog) {
        contentValues.clear();
        contentValues.put(ActivityLog.USER_ID, activityLog.getUserId());
        contentValues.put(ActivityLog.PASSWORD_ID, activityLog.getPasswordId());
        contentValues.put(ActivityLog.TIME, activityLog.getTime());
        contentValues.put(ActivityLog.FUNCTION, activityLog.getFunction());

        long logId = database.insert(ACTIVITY_LOGS_TABLE, null, contentValues);

        return logId != -1;
    }

}
